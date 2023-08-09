@TestTargetClass(Statement.class)
public class QueryTimeoutTest extends TestCase {
    private static Statement statement;
    private static final int TIMEOUT = 1; 
    private static final int CONNECTIONS = 100;
    private static Connection[] connections = new Connection[CONNECTIONS];
    private static void printSQLException(SQLException e) {
        while (e != null) {
            e.printStackTrace();
            e = e.getNextException();
        }
    }
    private static class TestFailedException extends Exception {
        private Throwable cause;
        public TestFailedException(Throwable t) {
            super();
            cause = t;
        }
        public TestFailedException(String message) {
            super(message);
            cause = null;
        }
        public TestFailedException(String message, Throwable t) {
            super(message);
            cause = t;
        }
        public String toString() {
            if (cause != null) {
                return super.toString() + ": " + cause.toString();
            } else {
                return super.toString();
            }
        }
        public void printStackTrace() {
            super.printStackTrace();
            if (cause != null) {
                if (cause instanceof SQLException) {
                    QueryTimeoutTest.printSQLException((SQLException) cause);
                } else {
                    cause.printStackTrace();
                }
            }
        }
    }
    private static void exec(Connection connection, String queryString,
            Collection ignoreExceptions) throws TestFailedException {
        Statement statement = null;
        try {
            statement = connection.createStatement();
            System.out.println(" Executing "+queryString);
            statement.execute(queryString);
        } catch (SQLException e) {
            String sqlState = e.getSQLState();
            if (!ignoreExceptions.contains(sqlState)) {
                throw new TestFailedException(e); 
            }
        } finally {
            if (statement != null) {
                try {
                    statement.close();
                } catch (SQLException ee) {
                    throw new TestFailedException(ee);
                }
            }
        }
    }
    private static void exec(Connection connection, String queryString)
            throws TestFailedException {
        exec(connection, queryString, Collections.EMPTY_SET);
    }
    private static void dropTables(Connection conn, String tablePrefix)
            throws TestFailedException {
        Collection ignore = new HashSet();
        exec(conn, "drop table if exists " + tablePrefix + "_orig;", ignore);
        exec(conn, "drop table if exists " + tablePrefix + "_copy;", ignore);
    }
    private static void prepareTables(Connection conn, String tablePrefix)
            throws TestFailedException {
        System.out.println("Initializing tables with prefix " + tablePrefix);
        dropTables(conn, tablePrefix);
        exec(conn, "create table " + tablePrefix + "_orig (a int)");
        exec(conn, "create table " + tablePrefix + "_copy (a int)");
        for (int i = 0; i < 7; i++) {
        exec(conn, "insert into " + tablePrefix + "_orig"
                + " values ("+i+");");
        }
    }
    private static String getFetchQuery(String tablePrefix) {
        return "select a from " + tablePrefix
                + "_orig where mod(DELAY(1,a),3)=0";
    }
    private static String getExecQuery(String tablePrefix) {
        return "insert into " + tablePrefix + "_copy select a from "
                + tablePrefix + "_orig where DELAY(1,1)=1";
    }
    private static class StatementExecutor extends Thread {
        private PreparedStatement statement;
        private boolean doFetch;
        private int timeout;
        private SQLException sqlException;
        private String name;
        private long highestRunTime;
        public StatementExecutor(PreparedStatement statement, boolean doFetch,
                int timeout) {
            this.statement = statement;
            this.doFetch = doFetch;
            this.timeout = timeout;
            highestRunTime = 0;
            sqlException = null;
            if (timeout > 0) {
                try {
                    statement.setQueryTimeout(timeout);
                } catch (SQLException e) {
                    sqlException = e;
                }
            }
        }
        private void setHighestRunTime(long runTime) {
            synchronized (this) {
                highestRunTime = runTime;
            }
        }
        public long getHighestRunTime() {
            synchronized (this) {
                return highestRunTime;
            }
        }
        private boolean fetchRow(ResultSet resultSet) throws SQLException {
            long startTime = System.currentTimeMillis();
            boolean hasNext = resultSet.next();
            long endTime = System.currentTimeMillis();
            long runTime = endTime - startTime;
            if (runTime > highestRunTime) setHighestRunTime(runTime);
            return hasNext;
        }
        public void run() {
            if (sqlException != null) return;
            ResultSet resultSet = null;
            try {
                if (doFetch) {
                    long startTime = System.currentTimeMillis();
                    resultSet = statement.executeQuery();
                    long endTime = System.currentTimeMillis();
                    setHighestRunTime(endTime - startTime);
                    while (fetchRow(resultSet)) {
                        yield();
                    }
                } else {
                    long startTime = System.currentTimeMillis();
                    statement.execute();
                    long endTime = System.currentTimeMillis();
                    setHighestRunTime(endTime - startTime);
                }
            } catch (SQLException e) {
                synchronized (this) {
                    sqlException = e;
                }
            } finally {
                if (resultSet != null) {
                    try {
                        resultSet.close();
                    } catch (SQLException ex) {
                        if (sqlException != null) {
                            System.err.println("Discarding previous exception");
                            sqlException.printStackTrace();
                        }
                        sqlException = ex;
                    }
                }
            }
        }
        public SQLException getSQLException() {
            synchronized (this) {
                return sqlException;
            }
        }
    }
    private static void expectException(String expectSqlState,
            SQLException sqlException, String failMsg)
            throws TestFailedException {
        if (sqlException == null) {
            throw new TestFailedException(failMsg);
        } else {
            String sqlState = sqlException.getSQLState();
            if (!expectSqlState.equals(sqlState)) {
                throw new TestFailedException(sqlException);
            }
        }
    }
    private static PreparedStatement prepare(Connection conn, String query)
            throws TestFailedException {
        try {
            return conn.prepareStatement(query);
        } catch (SQLException e) {
            throw new TestFailedException(e);
        }
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "Testing timeout with fetch operations",
        method = "setQueryTimeout",
        args = {int.class}
    )
    public static void testTimeoutWithFetch() throws TestFailedException {
        System.out.println("Testing timeout with fetch operations");
        Connection conn1 = connections[0];
        Connection conn2 = connections[1];
        try {
            conn1.setAutoCommit(false);
            conn2.setAutoCommit(false);
        } catch (SQLException e) {
            throw new TestFailedException("Unexpected Exception", e);
        }
        PreparedStatement statementA = prepare(conn1, getFetchQuery("t"));
        PreparedStatement statementB = prepare(conn1, getFetchQuery("t"));
        PreparedStatement statementC = prepare(conn2, getFetchQuery("t"));
        PreparedStatement statementD = prepare(conn2, getFetchQuery("t"));
        StatementExecutor[] statementExecutor = new StatementExecutor[4];
        statementExecutor[0] = new StatementExecutor(statementA, true, TIMEOUT);
        statementExecutor[1] = new StatementExecutor(statementB, true, 0);
        statementExecutor[2] = new StatementExecutor(statementC, true, 0);
        statementExecutor[3] = new StatementExecutor(statementD, true, 0);
        for (int i = 3; i >= 0; --i) {
            statementExecutor[i].start();
        }
        for (int i = 0; i < 4; ++i) {
            try {
                statementExecutor[i].join();
            } catch (InterruptedException e) {
                throw new TestFailedException("Should never happen", e);
            }
        }
        expectException("XCL52", statementExecutor[0].getSQLException(),
                "fetch did not time out. Highest execution time: "
                        + statementExecutor[0].getHighestRunTime() + " ms");
        System.out.println("Statement 0 timed out");
        for (int i = 1; i < 4; ++i) {
            SQLException sqlException = statementExecutor[i].getSQLException();
            if (sqlException != null) {
                throw new TestFailedException("Unexpected exception in " + i,
                        sqlException);
            }
            System.out.println("Statement " + i + " completed");
        }
        try {
            statementA.close();
            statementB.close();
            statementC.close();
            statementD.close();
            conn1.commit();
            conn2.commit();
        } catch (SQLException e) {
            throw new TestFailedException(e);
        }
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "test timeout with st.exec()",
        method = "setQueryTimeout",
        args = {int.class}
    )
    public static void testTimeoutWithExec()
            throws TestFailedException {
        System.out.println("Testing timeout with an execute operation");
        for (int i = 0; i < connections.length; ++i) {
            try {
                connections[i].setAutoCommit(true);
            } catch (SQLException e) {
                throw new TestFailedException("Unexpected Exception", e);
            }
        }
        PreparedStatement statements[] = new PreparedStatement[connections.length];
        for (int i = 0; i < statements.length; ++i) {
            statements[i] = prepare(connections[i], getExecQuery("t"));
        }
        StatementExecutor[] executors = new StatementExecutor[statements.length];
        for (int i = 0; i < executors.length; ++i) {
            int timeout = (i % 2 == 0) ? TIMEOUT : 0;
            executors[i] = new StatementExecutor(statements[i], false, timeout);
        }
        for (int i = 0; i < executors.length; ++i) {
            executors[i].start();
        }
        for (int i = 0; i < executors.length; ++i) {
            try {
                executors[i].join();
            } catch (InterruptedException e) {
                throw new TestFailedException("Should never happen", e);
            }
        }
        for (int i = 0; i < executors.length; ++i) {
            int timeout = (i % 2 == 0) ? TIMEOUT : 0;
            if (timeout > 0) {
                expectException("XCL52", executors[i].getSQLException(),
                        "exec did not time out. Execution time: "
                                + executors[i].getHighestRunTime() + " ms");
            } else {
                SQLException sqlException = executors[i].getSQLException();
                if (sqlException != null) {
                    throw new TestFailedException(sqlException);
                }
            }
        }
        System.out
                .println("Statements that should time out timed out, and statements that should complete completed");
        for (int i = 0; i < statements.length; ++i) {
            try {
                statements[i].close();
            } catch (SQLException e) {
                throw new TestFailedException(e);
            }
        }
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "Testing setting a negative timeout value",
        method = "setQueryTimeout",
        args = {int.class}
    )
    public static void testInvalidTimeoutValue(Connection conn)
            throws TestFailedException {
        try {
            conn.setAutoCommit(true);
        } catch (SQLException e) {
            throw new TestFailedException("Unexpected Exception", e);
        }
        PreparedStatement stmt = null;
        try {
            stmt = conn.prepareStatement("select * from sys.systables");
        } catch (SQLException e) {
            throw new TestFailedException("Unexpected Exception", e);
        }
        try {
            stmt.setQueryTimeout(-1);
        } catch (SQLException e) {
            expectException("XJ074", e,
                    "negative timeout value should give exception");
        }
        System.out
                .println("Negative timeout value caused exception, as expected");
        ResultSet rs = null;
        try {
            rs = stmt.executeQuery();
            System.out.println("Execute returned a ResultSet");
            rs.close();
        } catch (SQLException e) {
            throw new TestFailedException("Unexpected Exception", e);
        } finally {
            try {
                stmt.close();
            } catch (SQLException e) {
                throw new TestFailedException("close should not throw", e);
            }
        }
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "timeout with executeUpdate call",
        method = "setQueryTimeout",
        args = {int.class}
    )
    public static void testTimeoutWithExecuteUpdate()
            throws TestFailedException {
        System.out.println("Testing timeout with executeUpdate call.");
        try {
            Statement stmt = connections[0].createStatement();
            stmt.setQueryTimeout(TIMEOUT);
            stmt.executeUpdate(getExecQuery("t"));
        } catch (SQLException sqle) {
            expectException("XCL52", sqle, "Should have timed out.");
        }
    }
    @TestTargetNew(
            level = TestLevel.PARTIAL_COMPLETE,
            notes = "Testing that Statement considers timeout.",
            method = "setQueryTimeout",
            args = {int.class}
        )
    public static void testRememberTimeoutValue()
            throws TestFailedException {
        String sql = getFetchQuery("t");
        try {
            Statement stmt = connections[0].createStatement();
            statementRemembersTimeout(stmt);
            PreparedStatement ps = connections[0].prepareStatement(sql);
            statementRemembersTimeout(ps);
            CallableStatement cs = connections[0].prepareCall(sql);
            statementRemembersTimeout(cs);
        } catch (SQLException sqle) {
            throw new TestFailedException("Unexpected Exception", sqle);
        }
    }
    public static void statementRemembersTimeout(Statement stmt)
            throws SQLException, TestFailedException {
        System.out.println("Testing that Statement remembers timeout.");
        stmt.setQueryTimeout(1);
        for (int i = 0; i < 3; i++) {
            try {
                ResultSet rs = stmt.executeQuery(getFetchQuery("t"));
                while (rs.next()) {
                }
                throw new TestFailedException("Should have timed out.");
            } catch (SQLException sqle) {
                expectException("XCL52", sqle, "Should have timed out.");
            }
        }
        stmt.close();
    }
    private static void statementRemembersTimeout(PreparedStatement ps)
            throws SQLException, TestFailedException {
        String name = (ps instanceof CallableStatement) ? "CallableStatement"
                : "PreparedStatement";
        System.out.println("Testing that " + name + " remembers timeout.");
        ps.setQueryTimeout(1);
        for (int i = 0; i < 3; i++) {
            try {
                ResultSet rs = ps.executeQuery();
                while (rs.next()) {
                }
                throw new TestFailedException("Should have timed out.");
            } catch (SQLException sqle) {
                expectException("XCL52", sqle, "Should have timed out.");
            }
        }
        ps.close();
    }
    static class Delay implements SQLite.Function {
        public void function(FunctionContext fc, String[] args) {
            int seconds = new Integer(args[0]).intValue();
            int value = new Integer(args[1]).intValue();
            try {
                Thread.sleep(seconds * 1000);
            } catch (InterruptedException e) {
            }
            fc.set_result(value);
        }
        public void last_step(FunctionContext fc) {
        }
        public void step(FunctionContext fc, String[] args) {
        }
    }
    public static Test suite() {
        TestSetup setup = new TestSetup( new TestSuite (QueryTimeoutTest.class)) {
            public void setUp() {
                Support_SQL.loadDriver();
                try {
                    for (int i = 0; i < connections.length; ++i) {
                        connections[i] = Support_SQL.getConnection();
                    }
                    for (int i = 0; i < connections.length; ++i) {
                        connections[i]
                                .setTransactionIsolation(Connection.TRANSACTION_READ_UNCOMMITTED);
                    }
                    prepare();
                } catch (Throwable e) {
                    fail("Unexpected SQLException " + e.toString());
                }
                System.out.println("Connections set up");
            }
            public void tearDown() {
                for (int i = connections.length - 1; i >= 0; --i) {
                    if (connections[i] != null) {
                        try {
                            connections[i].close();
                        } catch (SQLException ex) {
                            printSQLException(ex);
                        }
                    }
                }
                System.out.println("Closed connections");
            }
            public void prepare() throws TestFailedException {
                System.out.println("Preparing for testing queries with timeout");
                Database db = new Database();
                Connection conn = connections[0];
                try {
                    db.open(Support_SQL.getFilename(), 1);
                    conn.setAutoCommit(true);
                } catch (Exception e) {
                    throw new TestFailedException("Unexpected Exception", e);
                }
                Function delayFc = new Delay();
                db.create_function("DELAY", 2, delayFc);
                prepareTables(conn, "t");
            }
        };
        TestSuite ts = new TestSuite();
        ts.addTestSuite(QueryTimeoutTest.class);
        return setup;
    }
}
