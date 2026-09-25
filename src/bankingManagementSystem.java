import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.sql.SQLException;
import java.util.Scanner;

public class bankingManagementSystem {

    static final String URL = "jdbc:mysql://localhost:3306/company_db";
    static final String USER = "root";
    static final String PASS = "7894561230";

    static Connection con;

    public static void main(String[] args) {

        Scanner sc = new Scanner(System.in);

        try {

            Class.forName("com.mysql.cj.jdbc.Driver");

            con = DriverManager.getConnection(URL, USER, PASS);

            System.out.println("Database Connected Successfully!");

            createTables();

            int choice;

            do {

                System.out.println("\n========== BANKING MANAGEMENT SYSTEM ==========");
                System.out.println("1. Create Customer & Account");
                System.out.println("2. Deposit");
                System.out.println("3. Withdraw");
                System.out.println("4. Fund Transfer");
                System.out.println("5. Transaction History");
                System.out.println("6. Display Account");
                System.out.println("7. Exit");
                System.out.println("===============================================");
                System.out.print("Enter your choice: ");

                while (!sc.hasNextInt()) {
                    System.out.println("Invalid input!");
                    sc.next();
                    System.out.print("Enter your choice: ");
                }

                choice = sc.nextInt();

                switch (choice) {

                    case 1:
                        createCustomerAndAccount(sc);
                        break;

                    case 2:
                        deposit(sc);
                        break;

                    case 3:
                        withdraw(sc);
                        break;

                    case 4:
                        fundTransfer(sc);
                        break;

                    case 5:
                        transactionHistory(sc);
                        break;

                    case 6:
                        displayAccount(sc);
                        break;

                    case 7:
                        System.out.println("Thank you for using Banking Management System!    Visit again!!!");
                        break;

                    default:
                        System.out.println("Invalid choice! Please select 1 to 7.");
                }

            } while (choice != 7);

            con.close();
            sc.close();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // ================= CREATE TABLES =================

    public static void createTables() throws SQLException {

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

        System.out.println("Tables are ready.");
    }

    // ================= CREATE CUSTOMER & ACCOUNT =================

    public static void createCustomerAndAccount(Scanner sc)
            throws SQLException {

        sc.nextLine();

        String name;
        String phone;
        String email;
        double initialDeposit;

        // NAME VALIDATION

        while (true) {

            System.out.print("Enter customer name: ");
            name = sc.nextLine().trim();

            if (name.matches("[a-zA-Z ]+")) {
                break;
            }

            System.out.println("Invalid name!");
            System.out.println("Name should contain only alphabets.");
        }

        // MOBILE VALIDATION

        while (true) {

            System.out.print("Enter mobile number: ");
            phone = sc.nextLine().trim();

            if (phone.matches("[0-9]{10}")) {
                break;
            }

            System.out.println("Invalid mobile number!");
            System.out.println("Mobile number must contain exactly 10 digits.");
        }

        // EMAIL VALIDATION

        while (true) {

            System.out.print("Enter email: ");
            email = sc.nextLine().trim();

            if (email.matches(
                    "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$")) {
                break;
            }

            System.out.println("Invalid email!");
            System.out.println("Please enter a valid email.");
        }

        // INITIAL DEPOSIT

        while (true) {

            System.out.print("Enter initial deposit: ");

            if (sc.hasNextDouble()) {

                initialDeposit = sc.nextDouble();

                if (initialDeposit >= 0) {
                    break;
                }

                System.out.println(
                        "Initial deposit cannot be negative.");

            } else {

                System.out.println("Invalid amount!");
                sc.next();
            }
        }

        try {

            con.setAutoCommit(false);

            // INSERT CUSTOMER

            String customerSql = """
                    INSERT INTO customers(name, phone, email)
                    VALUES (?, ?, ?)
                    """;

            PreparedStatement customerPs =
                    con.prepareStatement(
                            customerSql,
                            Statement.RETURN_GENERATED_KEYS);

            customerPs.setString(1, name);
            customerPs.setString(2, phone);
            customerPs.setString(3, email);

            customerPs.executeUpdate();

            ResultSet customerKeys =
                    customerPs.getGeneratedKeys();

            int customerId;

            if (customerKeys.next()) {
                customerId = customerKeys.getInt(1);
            } else {
                throw new SQLException(
                        "Customer ID could not be generated.");
            }

            // GENERATE ACCOUNT NUMBER

            String accountNumber =
                    generateAccountNumber();

            // INSERT ACCOUNT

            String accountSql = """
                    INSERT INTO accounts
                    (customer_id, account_number, balance)
                    VALUES (?, ?, ?)
                    """;

            PreparedStatement accountPs =
                    con.prepareStatement(accountSql);

            accountPs.setInt(1, customerId);
            accountPs.setString(2, accountNumber);
            accountPs.setDouble(3, initialDeposit);

            accountPs.executeUpdate();

            // GET ACCOUNT ID

            String accountIdSql =
                    "SELECT account_id FROM accounts WHERE account_number=?";

            PreparedStatement accountIdPs =
                    con.prepareStatement(accountIdSql);

            accountIdPs.setString(1, accountNumber);

            ResultSet accountRs =
                    accountIdPs.executeQuery();

            int accountId;

            if (accountRs.next()) {
                accountId = accountRs.getInt("account_id");
            } else {
                throw new SQLException(
                        "Account ID could not be found.");
            }

            // RECORD INITIAL DEPOSIT

            if (initialDeposit > 0) {

                String transactionSql = """
                        INSERT INTO transactions
                        (account_id, transaction_type, amount)
                        VALUES (?, 'ACCOUNT_OPENING', ?)
                        """;

                PreparedStatement transactionPs =
                        con.prepareStatement(transactionSql);

                transactionPs.setInt(1, accountId);
                transactionPs.setDouble(2, initialDeposit);

                transactionPs.executeUpdate();
            }

            con.commit();

            con.setAutoCommit(true);

            System.out.println("\nCustomer and account created successfully!");
            System.out.println("Customer ID: " + customerId);
            System.out.println("Account Number: " + accountNumber);
            System.out.println("Initial Balance: ₹" + initialDeposit);

        } catch (SQLException e) {

            con.rollback();
            con.setAutoCommit(true);

            System.out.println("Customer and account creation failed.");

            if (e.getErrorCode() == 1062) {

                String message = e.getMessage();

                if (message.contains("phone")) {
                    System.out.println("Mobile number already exists!");
                } 
                else if (message.contains("email")) {
                    System.out.println("Email already exists!");
                } 
                else {
                    System.out.println("Duplicate value already exists!");
                }

            } else {

                e.printStackTrace();
            }
        }
        }

    // ================= GENERATE ACCOUNT NUMBER =================

    public static String generateAccountNumber() {

        long number =
                System.currentTimeMillis() % 9000000000L
                + 1000000000L;

        return String.valueOf(number);
    }

    // ================= DEPOSIT =================

    public static void deposit(Scanner sc) throws SQLException {

        System.out.print("Enter account number: ");
        String accountNumber = sc.next();

        double amount;

        while (true) {

            System.out.print("Enter deposit amount: ");

            if (sc.hasNextDouble()) {

                amount = sc.nextDouble();

                if (amount > 0) {
                    break;
                }

                System.out.println(
                        "Amount must be greater than 0.");

            } else {

                System.out.println("Invalid amount!");
                sc.next();
            }
        }

        String check =
                "SELECT account_id FROM accounts WHERE account_number=?";

        PreparedStatement checkPs =
                con.prepareStatement(check);

        checkPs.setString(1, accountNumber);

        ResultSet rs = checkPs.executeQuery();

        if (!rs.next()) {

            System.out.println("Account not found!");
            return;
        }

        int accountId =
                rs.getInt("account_id");

        String update = """
                UPDATE accounts
                SET balance = balance + ?
                WHERE account_number=?
                """;

        PreparedStatement ps =
                con.prepareStatement(update);

        ps.setDouble(1, amount);
        ps.setString(2, accountNumber);

        ps.executeUpdate();

        String transaction = """
                INSERT INTO transactions
                (account_id, transaction_type, amount)
                VALUES (?, 'DEPOSIT', ?)
                """;

        PreparedStatement ts =
                con.prepareStatement(transaction);

        ts.setInt(1, accountId);
        ts.setDouble(2, amount);

        ts.executeUpdate();

        System.out.println("Deposit successful!");
    }

    // ================= WITHDRAW =================

    public static void withdraw(Scanner sc) throws SQLException {

        System.out.print("Enter account number: ");
        String accountNumber = sc.next();

        double amount;

        while (true) {

            System.out.print("Enter withdrawal amount: ");

            if (sc.hasNextDouble()) {

                amount = sc.nextDouble();

                if (amount > 0) {
                    break;
                }

                System.out.println(
                        "Amount must be greater than 0.");

            } else {

                System.out.println("Invalid amount!");
                sc.next();
            }
        }

        String check = """
                SELECT account_id, balance
                FROM accounts
                WHERE account_number=?
                """;

        PreparedStatement checkPs =
                con.prepareStatement(check);

        checkPs.setString(1, accountNumber);

        ResultSet rs =
                checkPs.executeQuery();

        if (!rs.next()) {

            System.out.println("Account not found!");
            return;
        }

        int accountId =
                rs.getInt("account_id");

        double balance =
                rs.getDouble("balance");

        if (amount > balance) {

            System.out.println("Insufficient balance!");
            return;
        }

        String update = """
                UPDATE accounts
                SET balance = balance - ?
                WHERE account_number=?
                """;

        PreparedStatement ps =
                con.prepareStatement(update);

        ps.setDouble(1, amount);
        ps.setString(2, accountNumber);

        ps.executeUpdate();

        String transaction = """
                INSERT INTO transactions
                (account_id, transaction_type, amount)
                VALUES (?, 'WITHDRAW', ?)
                """;

        PreparedStatement ts =
                con.prepareStatement(transaction);

        ts.setInt(1, accountId);
        ts.setDouble(2, amount);

        ts.executeUpdate();

        System.out.println("Withdrawal successful!");
    }

    // ================= FUND TRANSFER =================

    public static void fundTransfer(Scanner sc)
            throws SQLException {

        System.out.print("Enter sender account number: ");
        String sender = sc.next();

        System.out.print("Enter receiver account number: ");
        String receiver = sc.next();

        if (sender.equals(receiver)) {

            System.out.println(
                    "Sender and receiver cannot be the same.");

            return;
        }

        double amount;

        while (true) {

            System.out.print("Enter transfer amount: ");

            if (sc.hasNextDouble()) {

                amount = sc.nextDouble();

                if (amount > 0) {
                    break;
                }

                System.out.println(
                        "Amount must be greater than 0.");

            } else {

                System.out.println("Invalid amount!");
                sc.next();
            }
        }

        try {

            con.setAutoCommit(false);

            // CHECK SENDER

            String senderSql = """
                    SELECT account_id, balance
                    FROM accounts
                    WHERE account_number=?
                    """;

            PreparedStatement senderPs =
                    con.prepareStatement(senderSql);

            senderPs.setString(1, sender);

            ResultSet senderRs =
                    senderPs.executeQuery();

            if (!senderRs.next()) {

                System.out.println(
                        "Sender account not found!");

                con.rollback();
                con.setAutoCommit(true);

                return;
            }

            int senderId =
                    senderRs.getInt("account_id");

            double senderBalance =
                    senderRs.getDouble("balance");

            // CHECK BALANCE

            if (amount > senderBalance) {

                System.out.println(
                        "Insufficient balance!");

                con.rollback();
                con.setAutoCommit(true);

                return;
            }

            // CHECK RECEIVER

            String receiverSql =
                    "SELECT account_id FROM accounts WHERE account_number=?";

            PreparedStatement receiverPs =
                    con.prepareStatement(receiverSql);

            receiverPs.setString(1, receiver);

            ResultSet receiverRs =
                    receiverPs.executeQuery();

            if (!receiverRs.next()) {

                System.out.println(
                        "Receiver account not found!");

                con.rollback();
                con.setAutoCommit(true);

                return;
            }

            int receiverId =
                    receiverRs.getInt("account_id");

            // DEDUCT FROM SENDER

            String deduct = """
                    UPDATE accounts
                    SET balance = balance - ?
                    WHERE account_number=?
                    """;

            PreparedStatement deductPs =
                    con.prepareStatement(deduct);

            deductPs.setDouble(1, amount);
            deductPs.setString(2, sender);

            deductPs.executeUpdate();

            // ADD TO RECEIVER

            String add = """
                    UPDATE accounts
                    SET balance = balance + ?
                    WHERE account_number=?
                    """;

            PreparedStatement addPs =
                    con.prepareStatement(add);

            addPs.setDouble(1, amount);
            addPs.setString(2, receiver);

            addPs.executeUpdate();

            // SENDER TRANSACTION

            String senderTransaction = """
                    INSERT INTO transactions
                    (account_id, transaction_type, amount)
                    VALUES (?, 'TRANSFER_SENT', ?)
                    """;

            PreparedStatement senderTs =
                    con.prepareStatement(senderTransaction);

            senderTs.setInt(1, senderId);
            senderTs.setDouble(2, amount);

            senderTs.executeUpdate();

            // RECEIVER TRANSACTION

            String receiverTransaction = """
                    INSERT INTO transactions
                    (account_id, transaction_type, amount)
                    VALUES (?, 'TRANSFER_RECEIVED', ?)
                    """;

            PreparedStatement receiverTs =
                    con.prepareStatement(receiverTransaction);

            receiverTs.setInt(1, receiverId);
            receiverTs.setDouble(2, amount);

            receiverTs.executeUpdate();

            // COMMIT

            con.commit();

            con.setAutoCommit(true);

            System.out.println(
                    "Fund transfer successful!");

        } catch (Exception e) {

            con.rollback();

            con.setAutoCommit(true);

            System.out.println(
                    "Transfer failed!");

            System.out.println(
                    "All changes have been rolled back.");
        }
    }

    // ================= TRANSACTION HISTORY =================

    public static void transactionHistory(Scanner sc)
            throws SQLException {

        System.out.print("Enter account number: ");
        String accountNumber = sc.next();

        String sql = """
                SELECT t.transaction_id,
                       t.transaction_type,
                       t.amount,
                       t.transaction_date
                FROM transactions t
                JOIN accounts a
                ON t.account_id = a.account_id
                WHERE a.account_number=?
                ORDER BY t.transaction_id
                """;

        PreparedStatement ps =
                con.prepareStatement(sql);

        ps.setString(1, accountNumber);

        ResultSet rs =
                ps.executeQuery();

        System.out.println(
                "\n========== TRANSACTION HISTORY ==========");

        boolean found = false;

        while (rs.next()) {

            found = true;

            System.out.println(
                    "ID: "
                    + rs.getInt("transaction_id")
                    + " | Type: "
                    + rs.getString("transaction_type")
                    + " | Amount: ₹"
                    + rs.getDouble("amount")
                    + " | Date: "
                    + rs.getTimestamp("transaction_date")
            );
        }

        if (!found) {
            System.out.println("No transactions found.");
        }
    }

    // ================= DISPLAY ACCOUNT =================

    public static void displayAccount(Scanner sc)
            throws SQLException {

        System.out.print("Enter account number: ");
        String accountNumber = sc.next();

        String sql = """
                SELECT a.account_id,
                       a.account_number,
                       a.balance,
                       c.name,
                       c.phone,
                       c.email
                FROM accounts a
                JOIN customers c
                ON a.customer_id = c.customer_id
                WHERE a.account_number=?
                """;

        PreparedStatement ps =
                con.prepareStatement(sql);

        ps.setString(1, accountNumber);

        ResultSet rs =
                ps.executeQuery();

        if (rs.next()) {

            System.out.println(
                    "\n========== ACCOUNT DETAILS ==========");

            System.out.println(
                    "Account ID: "
                    + rs.getInt("account_id"));

            System.out.println(
                    "Account Number: "
                    + rs.getString("account_number"));

            System.out.println(
                    "Customer Name: "
                    + rs.getString("name"));

            System.out.println(
                    "Phone: "
                    + rs.getString("phone"));

            System.out.println(
                    "Email: "
                    + rs.getString("email"));

            System.out.println(
                    "Balance: ₹"
                    + rs.getDouble("balance"));

        } else {

            System.out.println(
                    "Account not found!");
        }
    }
}