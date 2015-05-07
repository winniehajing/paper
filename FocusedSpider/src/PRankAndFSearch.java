import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.*;

import org.jsoup.*;
import org.jsoup.nodes.*;
import org.jsoup.select.Elements;

//import FishSearchUp.LinkNode;

public class PRankAndFSearch {
	
	public class LinkNode
	{
		String linkValue;
		String linkName;
		Integer depth;
		Double important_score;
		Double all_score;
		Map<String, LinkNode> subLink = new HashMap<String, LinkNode>();
	}
	/**
	 * @param args
	 */
	String[] userAgents = new String[]{"Mozilla/4.0 (compatible; MSIE 6.0; Windows NT 5.1;SV1)",
			"Mozilla/5.0 (Windows NT 6.1; rv:22.0) Gecko/20100101 Firefox/22.0",
			"Mozilla/5.0 (Windows NT 6.2; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/32.0.1667.0 Safari/537.36",
			"Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/31.0.1623.0 Safari/537.36",
			"Mozilla/5.0 (Macintosh; PPC Mac OS X; U; en) Opera 8.0"};
	
	//��ʼURL����
    String[] urlArray = new String[]{"http://sports.sina.com.cn/"};
	
    //���������
    LinkedList<LinkNode> nodeList = new LinkedList<LinkNode>();
    
    //�Ѵ���map����linkvalueΪ��ֵ
    Map<String, LinkNode> nodeMap = new HashMap<String, LinkNode>();
    
   	DBConnect dbcon = new DBConnect();
	Random random = new Random();
	boolean recordeExist = true;

	//����
	String theme = "����";
	//��ֹ����
	final static Integer ENDSIZE = 1000;
	
    //�㷨�еĹ̶�����
    Integer width = 50;
    Double a = 1.5;
    Integer D = 2;
    Double p = 0.25;//��
    Double k = 0.75;//��
    Double r = 0.8;//pagerank ��fishsearch�ı�������
	
	//��Чҳ��
	Integer validSize = 0;
	//��Чҳ��
	Integer totalSize = 0;
	
	public void SpiderRun(){
		for(int i=0;i<urlArray.length;i++)
		{
			LinkNode rootNode = new LinkNode();
			rootNode.linkName = theme;
			rootNode.depth = D;
			rootNode.linkValue = urlArray[i];
			rootNode.important_score = 1.0;
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
	}
	public void processNode(LinkNode linkNode){
		//��ȡ֮ǰ ��ȡ���ݿ⿴�Ƿ����ظ�
		if(totalSize >= ENDSIZE)
		{
			return;
		}
		if(nodeMap.get(linkNode.linkValue) != null)
		{
			return;
		}
		if(linkNode.depth <= 0)
		{
			return;
		}
		if(dbcon.searchRecordForPRankAndFSearch(linkNode.linkValue))
		{
			//URL�ظ�
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
			//doc��ȡ�ɹ�������ȡ�ɹ� �������ݿ�
			totalSize ++;
			
			//��ȡ����
			String title = "";
			Element titleEle = doc.getElementsByTag("title").first();
			if(titleEle !=  null)
			{
				title = titleEle.text().trim();
			}
			//��ȡ�ı�
			String text = doc.text();
			String str = doc.toString();
			
			if(dbcon.insertLinksForPRankAndFSearch(linkNode.linkName, linkNode.linkValue, title, text) != -1)
			{
				//�ɹ��������ݿ�
				System.out.println("�ɹ���ȡ�������ݿ⣬link:"+linkNode.linkValue);
			}
			//��ȡ���ı�
			String aStr = "";
			if(totalSize >= ENDSIZE)
			{
				return;
			}
			
			//����PageRankֵ
			Double pageRankValue= 0.0;
			for(Map.Entry<String, LinkNode> entry:nodeMap.entrySet())
			{
				String fatherLinkValue = entry.getKey();
				LinkNode fatherLinkNode = entry.getValue();
				for(Map.Entry<String, LinkNode> fatherEntry:fatherLinkNode.subLink.entrySet())
				{
					if(fatherEntry.getKey().equals(linkNode.linkValue))
					{
						pageRankValue = pageRankValue + fatherLinkNode.important_score;
					}
				}
			}
			if(pageRankValue == 0.0)
			{
				pageRankValue = 1.0;
			}
			
			//��ȡ������
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
							linkNode.subLink.put(childLinkValue, newLinkNode);
						}
					}
					aStr = aStr + link.text().trim();
				}
			}
			
			//��ضȼ���
			double likeValue = 0.0;
			//��ȡ�ؼ���
			String keywords = "";
			Elements metas = doc.head().select("meta");  
	        for (Element meta : metas) {  
	            String content = meta.attr("content");  
	            if ("keywords".equalsIgnoreCase(meta.attr("name"))) {  
	                keywords = keywords + content;  
	            }  
//	            if ("description".equalsIgnoreCase(meta.attr("name"))) {  
//	                System.out.println("��վ��������:"+content);  
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
	        
			//�����ۺ�Ȩֵ
	        if(subLinks.size() > 0)
	        {
	        	likeValue = (pageRankValue/(subLinks.size()))*(1-r) + likeValue*r;
	        }
	        else
	        {
	        	likeValue = pageRankValue*(1-r) + likeValue*r;
	        }
	        
	      //�ж��Ƿ����
			if(likeValue > 0.5)
			{
				//���
				Iterator<LinkNode> iter = subLinks.iterator();
				int j = 0;
				for(; iter.hasNext(); j++)
				{
					LinkNode link = iter.next();
					if(j < width*a)
					{
						link.all_score = likeValue;
					}
					else
					{
						link.all_score = 0.0;
					}
					link.depth = linkNode.depth;
				}
			}
			else
			{
				//�����
				int j = 0;
				Iterator<LinkNode> iter = subLinks.iterator();
				while(iter.hasNext())
				{
					LinkNode link = iter.next();
					if(j < width)
					{
						link.all_score = likeValue;
					}
					else
					{
						link.all_score = 0.0;
					}
					link.depth = linkNode.depth - 1;
					j++;
				}
			}
			
	             
			//���������ӵ���Ҫ�ȣ���ͬʱ�������ȡ����
			for(LinkNode subLink:subLinks)
			{
				subLink.important_score = pageRankValue/(subLinks.size());
				boolean isAdd = true;
				Iterator<LinkNode> iter = nodeList.iterator();
				while(iter.hasNext())
				{
					LinkNode tmp = iter.next();
					if(tmp.linkValue == subLink.linkValue)
					{
						if(tmp.all_score < subLink.all_score)
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
						if(tmp.all_score < subLink.all_score)
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
			
			//���õ�ǰ�ڵ����Ҫ�ȣ�����������ȡ����
			if(subLinks.size() > 0)
			{
				linkNode.important_score = pageRankValue/(subLinks.size());
			}
			nodeMap.put(linkNode.linkValue, linkNode);
			
		}
	}
	
	//������
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		PRankAndFSearch pr = new PRankAndFSearch();
		pr.SpiderRun();
		//ganji.parseEmployPage("http://cd.ganji.com/zpshichangyingxiao/1223643628x.htm", "��˾", 1);
	}
}
