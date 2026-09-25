import java.sql.*;
public class batchProcessing {

	public static void main(String[] args) {
		String url = "jdbc:mysql://localhost:3306/practice_db";
		String user ="root";
		String pass = "7894561230";
		
		Connection con = null;
		PreparedStatement insert = null;
		PreparedStatement delete = null;
		
		try {
			Class.forName("com.mysql.cj.jdbc.Driver");
			con = DriverManager.getConnection(url,user,pass);
			con.setAutoCommit(false);
			
			String in = "insert into students_table(name,email) values(?,?)";
			insert = con.prepareStatement(in);
			insert.setString(1, "Beckham");
			insert.setString(2, "admin@222");
			insert.addBatch();
			
			insert.setString(1, "Rolandhino");
			insert.setString(2, "admin@223");
			insert.addBatch();
			
			insert.setString(1, "Zidane");
			insert.setString(2, "admin@224");
			insert.addBatch();
			
			insert.setString(1, "Kroos");
			insert.setString(2, "admin@225");
			insert.addBatch();
			
			insert.executeBatch();
			
			System.out.println("Succeess");
			
//			String del = "delete from students_table where id =?";
//			delete = con.prepareStatement(del);
//			delete.setInt(1,14);
//			delete.addBatch();
//			
//			delete.setInt(1,15);
//			delete.addBatch();
//			
//			delete.setInt(1,16);
//			delete.addBatch();
//			
//			delete.setInt(1,17);
//			delete.addBatch();
//			
//			delete.setInt(1,18);
//			delete.addBatch();
//			
//			delete.setInt(1,19);
//			delete.addBatch();
//			
//			delete.setInt(1,20);
//			delete.addBatch();
//			
//			delete.setInt(1,21);
//			delete.addBatch();
//			
//			delete.setInt(1,22);
//			delete.addBatch();
//			
//			delete.executeBatch();
			
			System.out.println("Success");
			
			con.commit();
			
		}
		
		
		catch(Exception e) {
			System.out.println(e);
		}
		
		finally
		{
			try {
				if(insert != null) insert.close();
				if(con != null) con.close();
			}
			catch(SQLException e) {
				System.out.println(e);
			}
		}
		

	}

}
