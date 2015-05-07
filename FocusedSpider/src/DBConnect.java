import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Date;

public class DBConnect {
	//private static Connection con;
	public static Connection getCon() throws Exception{
		Connection con = null;
		
		String url = "jdbc:mysql://localhost:3309/";
		String db = "xiaobao";
		String driverName = "com.mysql.jdbc.Driver";
		String userName = "root";
		String password = "mysql123";
		
		try{
			Class.forName(driverName);
		}catch(Exception e){
			System.out.println(e);
		}
		url = url + db + "?user=" +userName + "&password=" + password + "&characterEncoding=utf-8";
		//con = DriverManager.getConnection(url+db, userName, password);
		con = DriverManager.getConnection(url);
		return con;
	}
	
	public int insertLinks(String linkName, String linkValue, String title, String text){
		int result = -1;
		try{
			Connection con = getCon();
			if(!con.isClosed()){
				PreparedStatement pstm = con.prepareStatement("insert into reptile(linkName, linkValue, title, text) values(?,?,?,?)");
				
				pstm.setString(1, linkName);
				pstm.setString(2, linkValue);
				pstm.setString(3, title);
				pstm.setString(4, text);
				
				result = pstm.executeUpdate();

				if(pstm != null){
					pstm.close();
					pstm = null;
				}if(con != null){
					con.close();
					con = null;
				}
			}else{
				System.out.println("数据库已关闭！");
			}
		}catch(Exception e){
			e.printStackTrace();
		}
		
		return result;
	}
	//查重函数
	public boolean searchRecord(String linkValue){
		boolean result = false;
		
		try{
			Connection con = getCon();
			if(!con.isClosed()){
				PreparedStatement pstm = con.prepareStatement("select * from reptile where linkValue=?");
				pstm.setString(1, linkValue);
				
				ResultSet rs = pstm.executeQuery();
				
				if(rs.next())
					result = true;
				
				if(rs !=null){
					rs.close();
					rs = null;
				}if(pstm != null){
					pstm.close();
					pstm = null;
				}if(con != null){
					con.close();
					con = null;
				}
			}else{
				System.out.println("数据库已关闭！");
			}
		}catch(Exception e){
			e.printStackTrace();
		}
		
		return result;
	}
	
	public int insertLinksPR(String linkName, String linkValue, String title, String text){
		int result = -1;
		try{
			Connection con = getCon();
			if(!con.isClosed()){
				PreparedStatement pstm = con.prepareStatement("insert into pagerank(linkName, linkValue, title, text) values(?,?,?,?)");
				
				pstm.setString(1, linkName);
				pstm.setString(2, linkValue);
				pstm.setString(3, title);
				pstm.setString(4, text);
				
				result = pstm.executeUpdate();

				if(pstm != null){
					pstm.close();
					pstm = null;
				}if(con != null){
					con.close();
					con = null;
				}
			}else{
				System.out.println("数据库已关闭！");
			}
		}catch(Exception e){
			e.printStackTrace();
		}
		
		return result;
	}
	//查重函数
	public boolean searchRecordPR(String linkValue){
		boolean result = false;
		
		try{
			Connection con = getCon();
			if(!con.isClosed()){
				PreparedStatement pstm = con.prepareStatement("select * from pagerank where linkValue=?");
				pstm.setString(1, linkValue);
				
				ResultSet rs = pstm.executeQuery();
				
				if(rs.next())
					result = true;
				
				if(rs !=null){
					rs.close();
					rs = null;
				}if(pstm != null){
					pstm.close();
					pstm = null;
				}if(con != null){
					con.close();
					con = null;
				}
			}else{
				System.out.println("数据库已关闭！");
			}
		}catch(Exception e){
			e.printStackTrace();
		}
		
		return result;
	}
	
	public int insertLinksForFishSearch(String linkName, String linkValue, String title, String text){
		int result = -1;
		try{
			Connection con = getCon();
			if(!con.isClosed()){
				PreparedStatement pstm = con.prepareStatement("insert into fishsearch(linkName, linkValue, title, text) values(?,?,?,?)");
				
				pstm.setString(1, linkName);
				pstm.setString(2, linkValue);
				pstm.setString(3, title);
				pstm.setString(4, text);
				
				result = pstm.executeUpdate();

				if(pstm != null){
					pstm.close();
					pstm = null;
				}if(con != null){
					con.close();
					con = null;
				}
			}else{
				System.out.println("数据库已关闭！");
			}
		}catch(Exception e){
			e.printStackTrace();
		}
		
		return result;
	}
	
	//查重函数
		public boolean searchRecordForFishSearch(String linkValue){
			boolean result = false;
			
			try{
				Connection con = getCon();
				if(!con.isClosed()){
					PreparedStatement pstm = con.prepareStatement("select * from fishsearch where linkValue=?");
					pstm.setString(1, linkValue);
					
					ResultSet rs = pstm.executeQuery();
					
					if(rs.next())
						result = true;
					
					if(rs !=null){
						rs.close();
						rs = null;
					}if(pstm != null){
						pstm.close();
						pstm = null;
					}if(con != null){
						con.close();
						con = null;
					}
				}else{
					System.out.println("数据库已关闭！");
				}
			}catch(Exception e){
				e.printStackTrace();
			}
			
			return result;
		}
		public int insertLinksForFishSearchUp(String linkName, String linkValue, String title, String text){
			int result = -1;
			try{
				Connection con = getCon();
				if(!con.isClosed()){
					PreparedStatement pstm = con.prepareStatement("insert into fishsearchup(linkName, linkValue, title, text) values(?,?,?,?)");
					
					pstm.setString(1, linkName);
					pstm.setString(2, linkValue);
					pstm.setString(3, title);
					pstm.setString(4, text);
					
					result = pstm.executeUpdate();

					if(pstm != null){
						pstm.close();
						pstm = null;
					}if(con != null){
						con.close();
						con = null;
					}
				}else{
					System.out.println("数据库已关闭！");
				}
			}catch(Exception e){
				e.printStackTrace();
			}
			
			return result;
		}
		
		//查重函数
		public boolean searchRecordForFishSearchUp(String linkValue){
			boolean result = false;
			
			try{
				Connection con = getCon();
				if(!con.isClosed()){
					PreparedStatement pstm = con.prepareStatement("select * from fishsearchup where linkValue=?");
					pstm.setString(1, linkValue);
					
					ResultSet rs = pstm.executeQuery();
					
					if(rs.next())
						result = true;
					
					if(rs !=null){
						rs.close();
						rs = null;
					}if(pstm != null){
						pstm.close();
						pstm = null;
					}if(con != null){
						con.close();
						con = null;
					}
				}else{
					System.out.println("数据库已关闭！");
				}
			}catch(Exception e){
				e.printStackTrace();
			}
			
			return result;
		}
		public int insertLinksForPageRank(String linkName, String linkValue, String title, String text){
			int result = -1;
			try{
				Connection con = getCon();
				if(!con.isClosed()){
					PreparedStatement pstm = con.prepareStatement("insert into pagerank(linkName, linkValue, title, text) values(?,?,?,?)");
					
					pstm.setString(1, linkName);
					pstm.setString(2, linkValue);
					pstm.setString(3, title);
					pstm.setString(4, text);
					
					result = pstm.executeUpdate();

					if(pstm != null){
						pstm.close();
						pstm = null;
					}if(con != null){
						con.close();
						con = null;
					}
				}else{
					System.out.println("数据库已关闭！");
				}
			}catch(Exception e){
				e.printStackTrace();
			}
			
			return result;
		}
		
		//查重函数
		public boolean searchRecordForPageRank(String linkValue){
			boolean result = false;
			
			try{
				Connection con = getCon();
				if(!con.isClosed()){
					PreparedStatement pstm = con.prepareStatement("select * from pagerank where linkValue=?");
					pstm.setString(1, linkValue);
					
					ResultSet rs = pstm.executeQuery();
					
					if(rs.next())
						result = true;
					
					if(rs !=null){
						rs.close();
						rs = null;
					}if(pstm != null){
						pstm.close();
						pstm = null;
					}if(con != null){
						con.close();
						con = null;
					}
				}else{
					System.out.println("数据库已关闭！");
				}
			}catch(Exception e){
				e.printStackTrace();
			}
			
			return result;
		}
		public int insertLinksForPRankAndFSearch(String linkName, String linkValue, String title, String text){
			int result = -1;
			try{
				Connection con = getCon();
				if(!con.isClosed()){
					PreparedStatement pstm = con.prepareStatement("insert into pagerankandfishsearch(linkName, linkValue, title, text) values(?,?,?,?)");
					
					pstm.setString(1, linkName);
					pstm.setString(2, linkValue);
					pstm.setString(3, title);
					pstm.setString(4, text);
					
					result = pstm.executeUpdate();

					if(pstm != null){
						pstm.close();
						pstm = null;
					}if(con != null){
						con.close();
						con = null;
					}
				}else{
					System.out.println("数据库已关闭！");
				}
			}catch(Exception e){
				e.printStackTrace();
			}
			
			return result;
		}
		
		//查重函数
		public boolean searchRecordForPRankAndFSearch(String linkValue){
			boolean result = false;
			
			try{
				Connection con = getCon();
				if(!con.isClosed()){
					PreparedStatement pstm = con.prepareStatement("select * from pagerankandfishsearch where linkValue=?");
					pstm.setString(1, linkValue);
					
					ResultSet rs = pstm.executeQuery();
					
					if(rs.next())
						result = true;
					
					if(rs !=null){
						rs.close();
						rs = null;
					}if(pstm != null){
						pstm.close();
						pstm = null;
					}if(con != null){
						con.close();
						con = null;
					}
				}else{
					System.out.println("数据库已关闭！");
				}
			}catch(Exception e){
				e.printStackTrace();
			}
			
			return result;
		}
	public static void main(String[] args) throws Exception{
		DBConnect con = new DBConnect();
		if(!con.getCon().isClosed())
			System.out.println("成功！");
//		String t1="04-01";
//		  String t2="04-02";
//		  int result = t1.compareTo(t2);
//		  System.out.print(result);
	}
}

