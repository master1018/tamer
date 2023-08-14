public class TestHelper_Driver4 implements Driver {
    int majorVersion = 1;
    int minorVersion = 0;
    String baseURL;
    String[] dataSources = { "data1", "data2", "data3" };
    static {
        Driver theDriver = new TestHelper_Driver4();
        try {
            DriverManager.registerDriver(theDriver);
        } catch (SQLException e) {
            System.out.println("Failed to register driver!");
        }
    } 
    protected TestHelper_Driver4() {
        super();
        baseURL = "jdbc:mikes4";
    } 
    public boolean acceptsURL(String url) throws SQLException {
        if (url == null) {
            return false;
        }
        if (url.startsWith(baseURL)) {
            return true;
        }
        return false;
    } 
    static String validuser = "theuser";
    static String validpassword = "thepassword";
    static String userProperty = "user";
    static String passwordProperty = "password";
    public Connection connect(String url, Properties info) throws SQLException {
        if (this.acceptsURL(url)) {
            String datasource = url.substring(baseURL.length() + 1);
            for (String element : dataSources) {
                if (datasource.equals(element)) {
                    if (datasource.equals("data1")) {
                    } else {
                        if (info == null) {
                            throw new SQLException("Properties bundle is null");
                        }
                        String user = (String) info.get(userProperty);
                        String password = (String) info.get(passwordProperty);
                        if (user == null || password == null) {
                            throw new SQLException(
                                    "Userid and/or password not supplied");
                        }
                        if (!user.equals(validuser)
                                || !password.equals(validpassword)) {
                            throw new SQLException(
                                    "Userid and/or password not valid");
                        } 
                    } 
                    Connection connection = new TestHelper_Connection1();
                    return connection;
                } 
            } 
        } 
        return null;
    } 
    public int getMajorVersion() {
        return majorVersion;
    } 
    public int getMinorVersion() {
        return minorVersion;
    } 
    public DriverPropertyInfo[] getPropertyInfo(String url, Properties info)
            throws SQLException {
        DriverPropertyInfo[] theInfos = {
                new DriverPropertyInfo(userProperty, "*"),
                new DriverPropertyInfo(passwordProperty, "*"), };
        return theInfos;
    }
    public boolean jdbcCompliant() {
        return false;
    }
}
