import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.*;

import org.jsoup.*;
import org.jsoup.nodes.*;
import org.jsoup.select.Elements;

public class PageRank {
	
	public class LinkNode
	{
		String linkValue;
		String linkName;
		Double potential_score;
		Map<String, LinkNode> subLink = new HashMap<String, LinkNode>();;
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
	
    //待处理队列
    LinkedList<LinkNode> nodeList = new LinkedList<LinkNode>();
    
    //已处理map，以linkvalue为键值
    Map<String, LinkNode> nodeMap = new HashMap<String, LinkNode>();
    
   	DBConnect dbcon = new DBConnect();
	Random random = new Random();
	boolean recordeExist = true;

	//主题
	String theme = "足球";
	//终止条件
	final static Integer ENDSIZE = 1000;
	
	//有效页面
	Integer validSize = 0;
	//无效页面
	Integer totalSize = 0;
	
	public void SpiderRun(){
		Date d = new Date();
		d.getTime();
		for(int i=0;i<urlArray.length;i++)
		{
			LinkNode rootNode = new LinkNode();
			rootNode.linkName = theme;
			rootNode.linkValue = urlArray[i];
			rootNode.potential_score = 1.0;
			nodeList.offer(rootNode);
		}
		while(!nodeList.isEmpty())
		{
			LinkNode node = nodeList.poll();
			processNode(node);
			if(totalSize >= ENDSIZE)
			{
				break;
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
		if(nodeMap.get(linkNode.linkValue) != null)
		{
			return;
		}
		if(dbcon.searchRecordForPageRank(linkNode.linkValue))
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
			String text = "";
			
			if(dbcon.insertLinksForPageRank(linkNode.linkName, linkNode.linkValue, title, text) != -1)
			{
				//成功加入数据库
				System.out.println("成功爬取存入数据库，link:"+linkNode.linkValue);
			}
			
			if(totalSize >= ENDSIZE)
			{
				return;
			}
			
			//计算PageRank值
			Double pageRankValue= 0.0;
			for(Map.Entry<String, LinkNode> entry:nodeMap.entrySet())
			{
				String fatherLinkValue = entry.getKey();
				LinkNode fatherLinkNode = entry.getValue();
				for(Map.Entry<String, LinkNode> fatherEntry:fatherLinkNode.subLink.entrySet())
				{
					if(fatherEntry.getKey().equals(linkNode.linkValue))
					{
						pageRankValue = pageRankValue + fatherLinkNode.potential_score;
					}
				}
			}
			if(pageRankValue == 0.0)
			{
				pageRankValue = 1.0;
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
						if(!dbcon.searchRecordForPageRank(childLinkValue))
						{
							LinkNode newLinkNode = new LinkNode();
							newLinkNode.linkValue = childLinkValue;
							newLinkNode.linkName = childLinkName;
							subLinks.offer(newLinkNode);
							linkNode.subLink.put(childLinkValue, newLinkNode);
						}
					}
				}
			}
			
			//设置子链接的重要度，并同时加入待爬取队列
			for(LinkNode subLink:subLinks)
			{
				subLink.potential_score = pageRankValue/(subLinks.size());
				boolean isAdd = true;
				Iterator<LinkNode> iter = nodeList.iterator();
				while(iter.hasNext())
				{
					LinkNode tmp = iter.next();
					if(tmp.linkValue == subLink.linkValue)
					{
						if(tmp.potential_score < subLink.potential_score)
						{
							iter.remove();
						}
						else
						{
							isAdd = false;
							break;
						}
					}
				}
				if(isAdd)
				{
					Iterator<LinkNode> waitIter = nodeList.iterator();
					int tmpI = 0;
					while(waitIter.hasNext())
					{
						LinkNode tmp = waitIter.next();
						if(tmp.potential_score < subLink.potential_score)
						{
							nodeList.add(tmpI, subLink);
							isAdd = false;
							break;
						}
						tmpI++;
					}
					if(isAdd)
					{
						nodeList.push(subLink);
					}
				}
			}
			
			//设置当前节点的重要度，并加入已爬取队列
			if(subLinks.size() > 0)
			{
				linkNode.potential_score = pageRankValue/(subLinks.size());
			}
			nodeMap.put(linkNode.linkValue, linkNode);
			
		}
	}
	
	//主函数
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		PageRank pr = new PageRank();
		pr.SpiderRun();
		//ganji.parseEmployPage("http://cd.ganji.com/zpshichangyingxiao/1223643628x.htm", "大公司", 1);
	}
}