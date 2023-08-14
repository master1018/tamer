public class test {
    public Connection initiateCon() {
        Connection connection;
        try {
            Class.forName("com.mysql.jdbc.Driver");
            url = "jdbc:mysql:
            connection = DriverManager.getConnection(url, "root", "vkmohan123");
            return connection;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
}
