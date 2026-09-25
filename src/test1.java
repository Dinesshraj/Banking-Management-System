import java.sql.DriverManager;
import java.sql.*;

public class test1 {

	
	static final String url = "jdbc:mysql://localhost:3306/practice_db";
	static final String username = "root";
	static final String pass = "7894561230";
	
	public static void main(String[] args) {
		
		try {
			Class.forName("com.mysql.cj.jdbc.Driver");
			Connection con = DriverManager.getConnection(url,username,pass);
			
			/*String insert = "insert into students_table(name,email) values(?,?)";
			PreparedStatement ps = con.prepareStatement(insert);
			ps.setString(1,"Neymer");
			ps.setString(2, "admin@123");
			ps.executeUpdate();
			System.out.println("Executed successfully");*/
			
			String update = "update students_table set email=? where id=?";
			PreparedStatement pt = con.prepareStatement(update);
			pt.setString(1,"admin@121");
			pt.setInt(2, 1);
			pt.executeUpdate();
			System.out.println("Updated");
			
			String delete = "delete from students_table where id=?";
			PreparedStatement del = con.prepareStatement(delete);
			del.setInt(1, 3);
			del.executeUpdate();
			System.out.println("Deleted");
			
			String fetch = "select *  from students_table";
			Statement ts = con.createStatement();
			ResultSet tr = ts.executeQuery(fetch);
			while(tr.next()) {
				System.out.println(tr.getInt("id") + " " + tr.getString("name") + " " + tr.getString("email"));
			}
			
			
			
			
			con.close();
		}
		catch(Exception e) {
			System.out.println(e);
		}
      
		
		
	}

}
