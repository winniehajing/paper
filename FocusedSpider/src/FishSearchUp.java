import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.*;

import org.jsoup.*;
import org.jsoup.nodes.*;
import org.jsoup.select.Elements;


public class FishSearchUp {
	
	public class LinkNode
	{
		String linkValue;
		String linkName;
		Double potential_score;
		Integer depth;
	}
	
	
	
	/**
	 * @param args
	 */
	String[] userAgents = new String[]{"Mozilla/4.0 (compatible; MSIE 6.0; Windows NT 5.1;SV1)",
			"Mozilla/5.0 (Windows NT 6.1; rv:22.0) Gecko/20100101 Firefox/22.0",
			"Mozilla/5.0 (Windows NT 6.2; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/32.0.1667.0 Safari/537.36",
			"Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/31.0.1623.0 Safari/537.36",
			"Mozilla/5.0 (Macintosh; PPC Mac OS X; U; en) Opera 8.0"};
	
	//初始URL数组
    String[] urlArray = new String[]{"http://sports.sina.com.cn/"};
	
    //待爬取队列
    LinkedList<LinkNode> waitList = new LinkedList<LinkNode>();
    
    //fishSearch算法中的固定参数
    Integer width = 50;
    Double a = 1.5;
    Integer D = 2;
    Double p = 0.25;//α
    Double k = 0.75;//β
    
    //主题
    String theme = "足球";
    
   	DBConnect dbcon = new DBConnect();
	Random random = new Random();
	
	//终止条件
	final static Integer ENDSIZE = 1000;
	
	//总页面数
	Integer totalSize = 0;
	
	public void SpiderRun(){
		Date date1=new Date();
		long time1 = date1.getTime();
		for(int i = 0; i < urlArray.length; i++)
		{
			LinkNode rootNode = new LinkNode();
			rootNode.depth = D;
			rootNode.linkName = theme;
			rootNode.linkValue = urlArray[i];
			waitList.offer(rootNode);
		}
		while(!waitList.isEmpty())
		{
			LinkNode node = waitList.poll();
			processNode(node);
			if(totalSize >= ENDSIZE)
			{
				System.out.println(node.potential_score);				
				break;
			}
		}
		Date date2=new Date();
		long time2 = date2.getTime();
		long time = time2 -time1;
		System.out.println(time);
	}
	
	public void processNode(LinkNode linkNode){
		//爬取之前 读取数据库看是否有重复
		if(totalSize >= ENDSIZE)
		{
			return;
		}
		
		if(linkNode.depth <= 0)
		{
			return;
		}
		if(dbcon.searchRecordForFishSearchUp(linkNode.linkValue))
		{
			//URL重复
			return;
		}
		Document doc = null;
		Random random = new Random();
		int i = random.nextInt(5);
		try {
			doc = Jsoup.connect(linkNode.linkValue).userAgent(userAgents[i]).timeout(10000).get();
		} catch (IOException e) {
			e.printStackTrace();
		}
		if(doc != null)
		{
			//doc获取成功表明爬取成功 加入数据库
			totalSize ++;
			
			//获取标题
			String title = "";
			Element titleEle = doc.getElementsByTag("title").first();
			if(titleEle !=  null)
			{
				title = titleEle.text().trim();
			}
			//获取文本
			String text = doc.text();
			String str = doc.toString();
			
			if(dbcon.insertLinksForFishSearchUp(linkNode.linkName, linkNode.linkValue, title, text) != -1)
			{
				//成功加入数据库
				System.out.println("成功爬取存入数据库，link:"+linkNode.linkValue);
			}
			
			//获取描文本
			String aStr = "";
			if(totalSize >= ENDSIZE)
			{
				System.out.println(linkNode.potential_score);
				return;
			}
			//获取子链接
			Queue<LinkNode> subLinks = new LinkedList<LinkNode>();
			Elements links = doc.select("a[href]");
			if(links != null)
			{
				for(Element link : links)
				{
					String childLinkValue = link.attr("href");
					if(childLinkValue.startsWith("http"))
					{
						String childLinkName = link.text().trim();
/*						if((!dbcon.searchRecordForFishSearch(childLinkValue))&&childLinkName.contains(theme))
*/						if(!dbcon.searchRecordForFishSearch(childLinkValue))

						{
							LinkNode newLinkNode = new LinkNode();
							newLinkNode.linkValue = childLinkValue;
							newLinkNode.linkName = childLinkName;
							subLinks.offer(newLinkNode);
						}
					}
					aStr = aStr + link.text().trim();
				}
			}
			
			//相关度计算
			double likeValue = 0.0;
			//获取关键词
			String keywords = "";
			Elements metas = doc.head().select("meta");  
	        for (Element meta : metas) {  
	            String content = meta.attr("content");  
	            if ("keywords".equalsIgnoreCase(meta.attr("name"))) {  
	                keywords = keywords + content;  
	            }  
//	            if ("description".equalsIgnoreCase(meta.attr("name"))) {  
//	                System.out.println("网站内容描述:"+content);  
//	            }  
	        }
	        
	        String[] titleAr = ("," + title + ",").split(theme);
	        String[] keywordsAr = ("," + keywords + ",").split(theme);
	        String[] aStrAr = ("," + aStr + ",").split(theme);
	        double keywordsValue = 0.0;
	        if(keywordsAr.length > 1)
	        {
	        	keywordsValue = 1.0;
	        }
	        double titleValue = 0.0;
	        if(title.length() > 0)
	        {
	        	titleValue= ((double)((titleAr.length-1)*(theme.length())))/(title.length());
	        }
	        double aValue = 0.0;
	        if(aStr.length() > 0)
	        {
	        	aValue = ((double)((aStrAr.length-1)*(theme.length())))/(aStr.length());
	        }
	        likeValue = keywordsValue*k + titleValue*1 + aValue*p;
	        
			//判断是否相关
			if(likeValue > 0.5)
			{
				//相关
				Iterator<LinkNode> iter = subLinks.iterator();
				int j = 0;
				for(; iter.hasNext(); j++)
				{
					LinkNode link = iter.next();
					if(j < width*a)
					{
						link.potential_score = likeValue;
					}
					else
					{
						link.potential_score = 0.0;
					}
					link.depth = linkNode.depth;
				}
			}
			else
			{
				//不相关
				int j = 0;
				Iterator<LinkNode> iter = subLinks.iterator();
				while(iter.hasNext())
				{
					LinkNode link = iter.next();
					if(j < width)
					{
						link.potential_score = likeValue;
					}
					else
					{
						link.potential_score = 0.0;
					}
					link.depth = linkNode.depth - 1;
					j++;
				}
			}
			Iterator<LinkNode> iter = subLinks.iterator();
			while(iter.hasNext())
			{
				LinkNode link = iter.next();
				boolean isAdd = true;
				
				Iterator<LinkNode> waitIter = waitList.iterator();
				while(waitIter.hasNext())
				{
					LinkNode tmp = waitIter.next();
					if(tmp.linkValue == link.linkValue)
					{
						if(tmp.potential_score < link.potential_score)
						{
							waitIter.remove();
						}
						else
						{
							iter.remove();
							isAdd = false;
							break;
						}
					}
				}
				if(isAdd)
				{
					Iterator<LinkNode> waitIter2 = waitList.iterator();
					int tmpI = 0;
					while(waitIter2.hasNext())
					{
						LinkNode tmp = waitIter2.next();
						if(tmp.potential_score < link.potential_score)
						{
							waitList.add(tmpI, link);
							isAdd = false;
							break;
						}
						tmpI++;
					}
					if(isAdd)
					{
						waitList.push(link);
					}
				}
			}
		}
		
		
	}
	/**
	 * 判断link是否相关
	 * @param link
	 * @return
	 */
	public boolean Is(String link)
	{
		return false;
	}
	
	//主函数
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		FishSearchUp fsu = new FishSearchUp();
		fsu.SpiderRun();
		//ganji.parseEmployPage("http://cd.ganji.com/zpshichangyingxiao/1223643628x.htm", "大公司", 1);
	}
}
