import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.Statement;

public class databaseSetup {

    static final String URL = "jdbc:mysql://localhost:3306/company_db";
    static final String USER = "root";
    static final String PASS = "7894561230";

    public static void main(String[] args) {

        try {
            Class.forName("com.mysql.cj.jdbc.Driver");

            Connection con = DriverManager.getConnection(URL, USER, PASS);
            Statement st = con.createStatement();

            String customers = """
                    CREATE TABLE IF NOT EXISTS customers (
                        customer_id INT AUTO_INCREMENT PRIMARY KEY,
                        name VARCHAR(100) NOT NULL,
                        phone VARCHAR(15) NOT NULL UNIQUE,
                        email VARCHAR(100) NOT NULL UNIQUE
                    )
                    """;

            String accounts = """
                    CREATE TABLE IF NOT EXISTS accounts (
                        account_id INT AUTO_INCREMENT PRIMARY KEY,
                        customer_id INT NOT NULL,
                        account_number VARCHAR(20) NOT NULL UNIQUE,
                        balance DECIMAL(12,2) DEFAULT 0.00,
                        FOREIGN KEY (customer_id) REFERENCES customers(customer_id)
                    )
                    """;

            String transactions = """
                    CREATE TABLE IF NOT EXISTS transactions (
                        transaction_id INT AUTO_INCREMENT PRIMARY KEY,
                        account_id INT NOT NULL,
                        transaction_type VARCHAR(30) NOT NULL,
                        amount DECIMAL(12,2) NOT NULL,
                        transaction_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                        FOREIGN KEY (account_id) REFERENCES accounts(account_id)
                    )
                    """;

            st.executeUpdate(customers);
            st.executeUpdate(accounts);
            st.executeUpdate(transactions);

            System.out.println("All tables created successfully!");

            con.close();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}