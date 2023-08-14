public class SQLiteTest extends TestCase {
    public static Connection conn;
    public static File dbFile = null;
    public void setUp() throws Exception {
        String tmp = System.getProperty("java.io.tmpdir");
        File tmpDir = new File(tmp);
        try {
            if (tmpDir.isDirectory()) {
                dbFile = File.createTempFile("sqliteTest", ".db", tmpDir);
                dbFile.deleteOnExit();
            } else {
                System.out.println("ctsdir does not exist");
            }
            Class.forName("SQLite.JDBCDriver").newInstance();
            if (!dbFile.exists()) {
              Logger.global.severe("DB file could not be created. Tests can not be executed.");
            } else {
            conn = DriverManager.getConnection("jdbc:sqlite:/"
                    + dbFile.getPath());
            }
            assertNotNull("Error creating connection",conn);
        } catch (IOException e) {
            System.out.println("Problem creating test file in " + tmp);
        } catch (SQLException e) {
            fail("Exception: " + e.toString());
        } catch (java.lang.Exception e) {
            fail("Exception: " + e.toString());
        }
    }
    public void tearDown() {
        try {
            if (!conn.isClosed()) {
                conn.close();
            }
        } catch (SQLException e) {
            fail("Couldn't close Connection: " + e.getMessage());
        }
    }
}
