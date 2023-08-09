public abstract class AbstractJDBCDriverTest extends TestCase {
    @MediumTest
    public void testJDBCDriver() throws Exception {
        Connection firstConnection = null;
        Connection secondConnection = null;
        File dbFile = getDbFile();
        String connectionURL = getConnectionURL();
        Statement firstStmt = null;
        Statement secondStmt = null;
        try {
            Class.forName(getJDBCDriverClassName());
            firstConnection = DriverManager.getConnection(connectionURL);
            secondConnection = DriverManager.getConnection(connectionURL);
            String[] ones = {"hello!", "goodbye"};
            short[] twos = {10, 20};
            String[] onesUpdated = new String[ones.length];
            for (int i = 0; i < ones.length; i++) {
                onesUpdated[i] = ones[i] + twos[i];
            }
            firstStmt = firstConnection.createStatement();
            firstStmt.execute("create table tbl1(one varchar(10), two smallint)");
            secondStmt = secondConnection.createStatement();
            autoCommitInsertSelectTest(firstStmt, ones, twos);
            updateSelectCommitSelectTest(firstStmt, secondStmt, ones, onesUpdated, twos);
            updateSelectRollbackSelectTest(firstStmt, secondStmt, onesUpdated, ones, twos);
        } finally {
            closeConnections(firstConnection, secondConnection, dbFile, firstStmt, secondStmt);
        }
    }
    protected abstract String getJDBCDriverClassName();
    protected abstract String getConnectionURL();
    protected abstract File getDbFile();
    private void closeConnections(Connection firstConnection, Connection secondConnection,
            File dbFile, Statement firstStmt, Statement secondStmt) {
        String failText = null;
        try {
            if (firstStmt != null) {
                firstStmt.execute("drop table tbl1");
            }
        } catch (SQLException e) {
            failText = e.getLocalizedMessage();
        }
        try {
            if (firstStmt != null) {
                firstStmt.close();
            }
        } catch (SQLException e) {
            failText = e.getLocalizedMessage();
        }
        try {
            if (firstConnection != null) {
                firstConnection.close();
            }
        } catch (SQLException e) {
            failText = e.getLocalizedMessage();
        }
        try {
            if (secondStmt != null) {
                secondStmt.close();
            }
        } catch (SQLException e) {
            failText = e.getLocalizedMessage();
        }
        try {
            if (secondConnection != null) {
                secondConnection.close();
            }
        } catch (SQLException e) {
            failText = e.getLocalizedMessage();
        }
        dbFile.delete();
        assertNull(failText, failText);
    }
    private void autoCommitInsertSelectTest(Statement stmt, String[] ones,
            short[] twos) throws SQLException {
        for (int i = 0; i < ones.length; i++) {
            stmt.execute("insert into tbl1 values('" + ones[i] + "'," + twos[i]
                    + ")");
        }
        assertAllFromTbl1(stmt, ones, twos);
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
        assertEquals(i, ones.length);
    }
    private void updateSelectCommitSelectTest(Statement firstStmt,
            Statement secondStmt, String[] ones, String[] onesUpdated,
            short[] twos) throws SQLException {
        firstStmt.getConnection().setAutoCommit(false);
        try {
            updateOnes(firstStmt, onesUpdated, twos);
            assertAllFromTbl1(secondStmt, ones, twos);
            firstStmt.getConnection().commit();
            assertAllFromTbl1(secondStmt, onesUpdated, twos);
        } finally {
            firstStmt.getConnection().setAutoCommit(true);
        }
    }
    private void updateSelectRollbackSelectTest(Statement firstStmt,
            Statement secondStmt, String[] ones, String[] onesUpdated,
            short[] twos) throws SQLException {
        firstStmt.getConnection().setAutoCommit(false);
        try {
            updateOnes(firstStmt, onesUpdated, twos);
            assertAllFromTbl1(secondStmt, ones, twos);
            firstStmt.getConnection().rollback();
            assertAllFromTbl1(secondStmt, ones, twos);
        } finally {
            firstStmt.getConnection().setAutoCommit(true);
        }
    }
    private void updateOnes(Statement stmt, String[] onesUpdated, short[] twos)
            throws SQLException {
        for (int i = 0; i < onesUpdated.length; i++) {
            stmt.execute("UPDATE tbl1 SET one = '" + onesUpdated[i]
                    + "' WHERE two = " + twos[i]);
        }
    }
}
