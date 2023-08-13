@TestTargetClass(JDBCDriver.class)
public class JDBCDriverFunctionalTest extends AbstractSqlTest {
    private  File dbFile = null;
    private String connectionURL = "empty";
    @Override
    public void setUp() throws ClassNotFoundException, SQLException, InstantiationException, IllegalAccessException, Exception { 
        super.setUp();
    }
    @Override
    protected void tearDown() throws SQLException {
        super.tearDown();
        dbFile.delete();
    }
    @Override
    protected String getConnectionURL() {
        if (connectionURL.equals("empty")) {
            String tmp = System.getProperty("java.io.tmpdir");
            File tmpDir = new File(tmp);
            if (tmpDir.isDirectory()) {
                try {
                    dbFile = File.createTempFile("JDBCDriverFunctionalTest",
                            ".db", tmpDir);
                } catch (IOException e) {
                    System.err.println("error creating temporary DB file.");
                }
                dbFile.deleteOnExit();
            } else {
                System.err.println("java.io.tmpdir does not exist");
            }
            connectionURL = "jdbc:sqlite:/" + dbFile.getPath();
        }
        return connectionURL;
    }
    @Override
    protected String getDriverClassName() {
        return "SQLite.JDBCDriver";
    }
    @Override
    protected int getTransactionIsolation() {
        return Connection.TRANSACTION_SERIALIZABLE;
    }
}
