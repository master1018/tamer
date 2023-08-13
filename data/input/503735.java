public class Support_SQL {
    public static String sqlDriver = null;
    public static String sqlLogin = null;
    public static String sqlCatalog = null;
    public static String sqlHost = null;
    public static String sqlUrl = null;
    public static String sqlPassword = null;
    public static String sqlUser = null;
    public static int sqlMaxConnections = 5;
    public static int sqlMaxTasks = 1;
    private static File dbFile = null;
    public static void loadDriver() {
        try {
            loadProperties(Class.forName("tests.support.Support_SQL")
                    .getResourceAsStream("/connection.properties"));
            String tmp = System.getProperty("java.io.tmpdir");
            File tmpDir = new File(tmp);
            if (tmpDir.isDirectory()) {
                dbFile = File.createTempFile("sqliteTest", ".db", tmpDir);
                dbFile.deleteOnExit();
            } else {
                System.err.println("java.io.tmpdir does not exist");
            }
            Class.forName("SQLite.JDBCDriver").newInstance();
            sqlUrl = "jdbc:sqlite:/" + dbFile.getPath();
            Class.forName(sqlDriver).newInstance();
        } catch (Exception ex) {
            System.err.println("Unexpected exception " + ex.toString());
        }
    }
    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(Support_SQL.sqlUrl,
                Support_SQL.sqlLogin, Support_SQL.sqlPassword);
    }
    public static Connection getConnection(String url, String login,
            String password) throws SQLException {
        return DriverManager.getConnection(url, login, password);
    }
    public static boolean isEqual(byte[] b1, int off1, byte[] b2, int off2,
            int len) {
        for (int i = 0; i < len; ++i)
            if (b1[i + off1] != b2[i + off2])
                return false;
        return true;
    }
    private static void loadProperties(InputStream fileName) throws IOException {
        Properties properties = new Properties();
        properties.load(fileName);
        sqlDriver = properties.getProperty("sqlDriver");
        sqlLogin = properties.getProperty("sqlLogin");
        sqlCatalog = properties.getProperty("sqlCatalog");
        sqlHost = properties.getProperty("sqlHost");
        sqlUrl = properties.getProperty("sqlUrlPrefix") + sqlHost + "/"
                + sqlCatalog;
        sqlPassword = properties.getProperty("sqlPassword");
        sqlUser = properties.getProperty("sqlUser");
        sqlMaxConnections = Integer.parseInt(properties
                .getProperty("sqlMaxConnections"));
        sqlMaxTasks = Integer.parseInt(properties.getProperty("sqlMaxTasks"));
    }
    public static String getFilename() {
        return dbFile.getPath();
    }
}
