import java.sql.DriverManager;
import java.sql.*;


public class test {

	public static void main(String[] args) {
		try {
			Class.forName("com.mysql.cj.jdbc.Driver");
			
			Connection con = DriverManager.getConnection("jdbc:mysql://localhost:3306/practice_db","root", "7894561230");
			System.out.println("Connection started");
			con.close();
		}
		catch(Exception e)
		{
			System.out.println(e);
		}

	}

}
