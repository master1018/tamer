@TestTargetClass(Database.class)
abstract class AbstractSqlTest extends TestCase {
    private Connection firstConnection;
    private Connection secondConnection;
    private Statement firstStmt;
    private Statement secondStmt;
    private final String[] ones = {"hello!", "goodbye"};
    private final short[] twos = {10, 20};
    private final String[] ones_updated;
    public AbstractSqlTest() {
        super();
        ones_updated = new String[ones.length];
        for (int i = 0; i < ones.length; i++) {
            ones_updated[i] = ones[i] + twos[i];
        }
    }
    @Override
    protected void setUp() throws InstantiationException,
            IllegalAccessException, ClassNotFoundException, SQLException, Exception {
        Class.forName(getDriverClassName()).newInstance();
        firstConnection = DriverManager.getConnection(getConnectionURL());
        firstConnection.setTransactionIsolation(getTransactionIsolation());
        secondConnection = DriverManager.getConnection(getConnectionURL());
        secondConnection.setTransactionIsolation(getTransactionIsolation());
        firstStmt = firstConnection.createStatement();
        firstStmt.execute("create table tbl1(one varchar(10), two smallint)");
        secondStmt = secondConnection.createStatement();
    }
    @Override
    protected void tearDown() throws SQLException {
        firstStmt.close();
        secondStmt.close();
        firstConnection.setAutoCommit(true);
        firstStmt = firstConnection.createStatement();
        firstStmt.execute("drop table tbl1");
        firstStmt.close();
        firstConnection.close();
        secondConnection.close();
    }
    private void autoCommitInsertSelect() throws SQLException {
        firstStmt.getConnection().setAutoCommit(true);
        for (int i = 0; i < ones.length; i++) {
            firstStmt.execute("insert into tbl1 values('" + ones[i] + "',"
                    + twos[i] + ")");
        }
        assertAllFromTbl1(firstStmt, ones, twos);
    }
    private void assertAllFromTbl1(Statement stmt, String[] ones, short[] twos)
            throws SQLException {
        ResultSet rs = stmt.executeQuery("select * from tbl1");
        int i = 0;
        for (; rs.next(); i++) {
            assertTrue(i < ones.length);
            assertEquals(ones[i], rs.getString("one"));
            assertEquals(twos[i], rs.getShort("two"));
        }
        assertTrue(i == ones.length);
    }
    @TestTargetNew(
      level = TestLevel.PARTIAL_COMPLETE,
      notes = "",
      clazz = Database.class,
      method = "exec",
      args = {String.class, Callback.class}
    )
    public void testAutoCommitInsertSelect() throws SQLException{
        autoCommitInsertSelect();
    }
    @TestTargetNew(
      level = TestLevel.PARTIAL_COMPLETE,
      notes = "",
      clazz = Database.class,
      method = "exec",
      args = {String.class, Callback.class}
      )
    public void testUpdateSelectCommitSelect() throws SQLException {
        autoCommitInsertSelect();
        firstStmt.getConnection().setAutoCommit(false);
        updateOnes(firstStmt, ones_updated, twos);
        assertAllFromTbl1(secondStmt, ones, twos);
        firstStmt.getConnection().commit();
        assertAllFromTbl1(secondStmt, ones_updated, twos);
    }
    @TestTargetNew(
            level = TestLevel.PARTIAL_COMPLETE,
            notes = "",
            clazz = Database.class,
            method = "exec",
            args = {String.class, Callback.class}
    )
    public void testUpdateSelectRollbackSelect() throws SQLException {
        autoCommitInsertSelect();
        firstStmt.getConnection().setAutoCommit(false);
        updateOnes(firstStmt, ones_updated, twos);
        assertAllFromTbl1(secondStmt, ones, twos);
        firstStmt.getConnection().rollback();
        assertAllFromTbl1(secondStmt, ones, twos);
    }
    private void updateOnes(Statement stmt, String[] ones_updated, short[] twos)
            throws SQLException {
        for (int i = 0; i < ones_updated.length; i++) {
            stmt.execute("UPDATE tbl1 SET one = '" + ones_updated[i]
                    + "' WHERE two = " + twos[i]);
        }
    }
    protected abstract String getConnectionURL();
    protected abstract String getDriverClassName();
    protected abstract int getTransactionIsolation();
}
