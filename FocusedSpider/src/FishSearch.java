import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.*;

import org.jsoup.*;
import org.jsoup.nodes.*;
import org.jsoup.select.Elements;


public class FishSearch {
	
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
	
    //前端队列
    Queue<LinkNode> frontList = new LinkedList<LinkNode>();
    //中间队列
    Queue<LinkNode> midList = new LinkedList<LinkNode>();
    //末尾队列
    Queue<LinkNode> endList = new LinkedList<LinkNode>();
    
    //fishSearch算法中的固定参数
    Integer width = 20;
    Double a = 1.5;
    Integer D = 2;
    
    //主题
    String theme = "足球";
    
   	DBConnect dbcon = new DBConnect();
	Random random = new Random();
	
	//终止条件
	final static Integer ENDSIZE = 1000;
	
	//总页面数
	Integer totalSize = 0;
	
	public void SpiderRun(){
		Date d = new Date();
		d.getTime();
		for(int i = 0; i < urlArray.length; i++)
		{
			LinkNode rootNode = new LinkNode();
			rootNode.depth = D;
			rootNode.linkName = theme;
			rootNode.linkValue = urlArray[i];
			frontList.offer(rootNode);
		}
		while((!frontList.isEmpty())||(!midList.isEmpty())||(!endList.isEmpty()))
		{
			if(!frontList.isEmpty())
			{
				LinkNode node = frontList.poll();
				processNode(node);
				if(totalSize >= ENDSIZE)
				{
					break;
				}
			}
			else if(!midList.isEmpty())
			{
				LinkNode node = midList.poll();
				processNode(node);
				if(totalSize >= ENDSIZE)
				{
					break;
				}
			}
			else if(!endList.isEmpty())
			{
				LinkNode node = endList.poll();
				processNode(node);
				if(totalSize >= ENDSIZE)
				{
					break;
				}
			}
		}
		
		Date d2 = new Date();
		long a = d2.getTime()-d.getTime();
		System.out.println(a); 
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
		if(dbcon.searchRecordForFishSearch(linkNode.linkValue))
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
			
			if(dbcon.insertLinksForFishSearch(linkNode.linkName, linkNode.linkValue, title, text) != -1)
			{
				//成功加入数据库
				System.out.println("成功爬取存入数据库，link:"+linkNode.linkValue);
			}
			
			if(totalSize >= ENDSIZE)
			{
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
						if(!dbcon.searchRecordForFishSearch(childLinkValue))
						{
							LinkNode newLinkNode = new LinkNode();
							newLinkNode.linkValue = childLinkValue;
							newLinkNode.linkName = childLinkName;
							subLinks.offer(newLinkNode);
						}
					}
				}
			}
			
			//是否相关
			boolean isLike = false;
			if(doc.text().contains(theme))
			{
				isLike = true;
			}
			
			//判断是否相关
			if(isLike)
			{
				//相关
				Iterator<LinkNode> iter = subLinks.iterator();
				int j = 0;
				for(; iter.hasNext(); j++)
				{
					LinkNode link = iter.next();
					if(j < width*a)
					{
						link.potential_score = 1.0;
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
						link.potential_score = 0.5;
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
				
				Iterator<LinkNode> frontIter = frontList.iterator();
				while(frontIter.hasNext())
				{
					LinkNode tmp = frontIter.next();
					if(tmp.linkValue == link.linkValue)
					{
						if(tmp.potential_score < link.potential_score)
						{
							frontIter.remove();
						}
						else
						{
							iter.remove();
							isAdd = false;
							break;
						}
					}
				}
				
				Iterator<LinkNode> midIter = midList.iterator();
				while(midIter.hasNext())
				{
					LinkNode tmp = midIter.next();
					if(tmp.linkValue == link.linkValue)
					{
						if(tmp.potential_score < link.potential_score)
						{
							midIter.remove();
						}
						else
						{
							iter.remove();
							isAdd = false;
							break;
						}
					}
				}
				
				Iterator<LinkNode> endIter = endList.iterator();
				while(endIter.hasNext())
				{
					LinkNode tmp = endIter.next();
					if(tmp.linkValue == link.linkValue)
					{
						if(tmp.potential_score < link.potential_score)
						{
							endIter.remove();
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
					if(link.potential_score == 0.0)
					{
						endList.add(link);
					}
					else if(link.potential_score == 0.5)
					{
						midList.add(link);
					}
					else
					{
						frontList.add(link);
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
		FishSearch fs = new FishSearch();
		fs.SpiderRun();
		//ganji.parseEmployPage("http://cd.ganji.com/zpshichangyingxiao/1223643628x.htm", "大公司", 1);
	}
}
