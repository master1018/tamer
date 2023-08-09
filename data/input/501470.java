@TestTargetClass(Statement.class)
public class SQLTest extends TestCase {
    static Connection conn;
    @Override
    public void setUp() throws Exception {
        getSQLiteConnection();
        createZoo();
    }
    protected File dbFile;
    protected void getSQLiteConnection() throws Exception {
        String tmp = System.getProperty("java.io.tmpdir");
        assertEquals(tmp, System.getProperty("java.io.tmpdir"));
        File tmpDir = new File(tmp);
        if (tmpDir.isDirectory()) {
            dbFile = File.createTempFile("sqliteTest", ".db", tmpDir);
            dbFile.deleteOnExit();
        } else {
            System.err.println("java.io.tmpdir does not exist");
        }
        Class.forName("SQLite.JDBCDriver").newInstance();
        conn = DriverManager.getConnection("jdbc:sqlite:/" + dbFile.getPath());
        assertNotNull("Connection created ", conn);
    }
    @Override
    public void tearDown() {
        Statement st = null;
        try {
            if (! conn.isClosed()) {
                st = conn.createStatement();
                st.execute("drop table if exists zoo");
            }
        } catch (SQLException e) {
            fail("Couldn't drop table: " + e.getMessage());
        } finally {
            try {
                if (st != null) {
                    st.close();
                    conn.close();
                }
            } catch(SQLException ee) {
            }
        }
    }
    public void createZoo() {
        String[] queries = {
                "create table zoo(id smallint,  name varchar(10), family varchar(10))",
                "insert into zoo values (1, 'Kesha', 'parrot')",
                "insert into zoo values (2, 'Yasha', 'sparrow')" };
        Statement st = null;    
        try {
            st = conn.createStatement();
            for (int i = 0; i < queries.length; i++) {
                st.execute(queries[i]);
            }
        } catch (SQLException e) {
            e.printStackTrace();
            fail("Unexpected exception: " + e.getMessage());
        } finally {
            try {
                if (st != null) {
                    st.close();
                }
             } catch (SQLException ee) {} 
        }
    }
    public void createProcedure() {
        String proc = "CREATE PROCEDURE welcomeAnimal (IN parameter1 integer, IN parameter2 char(20), IN parameter3 char(20)) "
                + " BEGIN "
                + " INSERT INTO zoo(id, name, family) VALUES (parameter1, parameter2, parameter3); "
                + "SELECT * FROM zoo;" + " END;";
        Statement st = null;
        try {
            st = conn.createStatement();
            st.execute("DROP PROCEDURE IF EXISTS welcomeAnimal");
            st.execute(proc);
        } catch (SQLException e) {
            fail("Unexpected exception: " + e.getMessage());
        } finally {
            try {
                st.close();
             } catch (SQLException ee) {} 
        }
    }
    public int getCount(ResultSet rs) {
        int count = 0;
        try {
            while (rs.next()) {
                count++;
            }
        } catch (SQLException e) {
            fail("SQLException is thrown");
        }
        return count;
    }
}
