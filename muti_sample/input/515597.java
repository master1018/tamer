@TestTargetClass(Statement.class)
public class InsertFunctionalityTest extends TestCase {
    private static Connection conn = null;
    private static Statement statement = null;
    public void setUp() throws Exception {
        super.setUp();
        Support_SQL.loadDriver();
        conn = Support_SQL.getConnection();
        statement = conn.createStatement();
        createTestTables();
    }
    public void tearDown() throws Exception {
        deleteTestTables();
        statement.close();
        conn.close();
        super.tearDown();
    }
    public void createTestTables() {
        try {
            DatabaseMetaData meta = conn.getMetaData();
            ResultSet userTab = meta.getTables(null, null, null, null);
            while (userTab.next()) {
                String tableName = userTab.getString("TABLE_NAME");
                if (tableName.equals(DatabaseCreator.PARENT_TABLE)) {
                    statement
                            .execute(DatabaseCreator.DROP_TABLE_PARENT);
                } else if (tableName
                        .equals(DatabaseCreator.FKCASCADE_TABLE)) {
                    statement
                            .execute(DatabaseCreator.DROP_TABLE_FKCASCADE);
                } else if (tableName
                        .equals(DatabaseCreator.FKSTRICT_TABLE)) {
                    statement
                            .execute(DatabaseCreator.DROP_TABLE_FKSTRICT);
                } else if (tableName
                        .equals(DatabaseCreator.SIMPLE_TABLE1)) {
                    statement
                            .execute(DatabaseCreator.DROP_TABLE_SIMPLE1);
                } else if (tableName
                        .equals(DatabaseCreator.SIMPLE_TABLE2)) {
                    statement
                            .execute(DatabaseCreator.DROP_TABLE_SIMPLE2);
                } else if (tableName
                        .equals(DatabaseCreator.TEST_TABLE5)) {
                    statement.execute(DatabaseCreator.DROP_TABLE5);
                }
            }
            userTab.close();
            statement.execute(DatabaseCreator.CREATE_TABLE_PARENT);
            statement.execute(DatabaseCreator.CREATE_TABLE_FKSTRICT);
            statement.execute(DatabaseCreator.CREATE_TABLE_FKCASCADE);
            statement.execute(DatabaseCreator.CREATE_TABLE_SIMPLE2);
            statement.execute(DatabaseCreator.CREATE_TABLE_SIMPLE1);
            statement.execute(DatabaseCreator.CREATE_TABLE5);
        } catch (SQLException e) {
            fail("Unexpected SQLException " + e.toString());
        }
    }
    public void deleteTestTables() {
        try {
            statement.execute(DatabaseCreator.DROP_TABLE_FKCASCADE);
            statement.execute(DatabaseCreator.DROP_TABLE_FKSTRICT);
            statement.execute(DatabaseCreator.DROP_TABLE_PARENT);
            statement.execute(DatabaseCreator.DROP_TABLE_SIMPLE2);
            statement.execute(DatabaseCreator.DROP_TABLE_SIMPLE1);
            statement.execute(DatabaseCreator.DROP_TABLE5);
        } catch (SQLException e) {
            fail("Unexpected SQLException " + e.toString());
        }
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "Functionality test: Attempts to insert row into table with integrity checking",
        method = "execute",
        args = {java.lang.String.class}
    )
    public void testInsert1() throws SQLException {
        DatabaseCreator.fillParentTable(conn);
        DatabaseCreator.fillFKStrictTable(conn);
        DatabaseCreator.fillFKCascadeTable(conn);
        statement.execute("INSERT INTO " + DatabaseCreator.FKSTRICT_TABLE
                + " VALUES(4, 1, 'testInsert')");
        statement.execute("INSERT INTO " + DatabaseCreator.FKCASCADE_TABLE
                + " VALUES(4, 1, 'testInsert')");
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "Functionality test: Attempts to insert row into table with integrity checking when row has incorrect foreign key value - expecting SQLException",
        method = "execute",
        args = {java.lang.String.class}
    )
    public void testInsert2() throws SQLException {
        DatabaseCreator.fillParentTable(conn);
        DatabaseCreator.fillFKStrictTable(conn);
        DatabaseCreator.fillFKCascadeTable(conn);
        try {
            statement.execute("INSERT INTO " + DatabaseCreator.FKSTRICT_TABLE
                    + " VALUES(4, 4, 'testInsert')");
        } catch (SQLException ex) {
        }
        try {
            statement.execute("INSERT INTO " + DatabaseCreator.FKCASCADE_TABLE
                    + " VALUES(4, 4, 'testInsert')");
        } catch (SQLException ex) {
        }
    }
    @TestTargets({
        @TestTargetNew(
            level = TestLevel.PARTIAL_COMPLETE,
            notes = "Functionality test: Tests INSERT ... SELECT functionality",
            method = "execute",
            args = {java.lang.String.class}
        ),
        @TestTargetNew(
            level = TestLevel.PARTIAL_COMPLETE,
            notes = "Functionality test: Tests INSERT ... SELECT functionality",
            method = "executeQuery",
            args = {java.lang.String.class}
        )
    })
    public void testInsert3() throws SQLException {
        DatabaseCreator.fillParentTable(conn);
        DatabaseCreator.fillFKStrictTable(conn);
        statement.execute("INSERT INTO " + DatabaseCreator.TEST_TABLE5
                + " SELECT id AS testId, value AS testValue " + "FROM "
                + DatabaseCreator.FKSTRICT_TABLE + " WHERE name_id = 1");
        ResultSet r = statement.executeQuery("SELECT COUNT(*) FROM "
                + DatabaseCreator.TEST_TABLE5);
        r.next();
        assertEquals("Should be 2 rows", 2, r.getInt(1));
        r.close();
    }
    @TestTargets({
        @TestTargetNew(
            level = TestLevel.PARTIAL_COMPLETE,
            notes = "Functionality test: Tests INSERT ... SELECT with expressions in SELECT query",
            method = "execute",
            args = {java.lang.String.class}
        ),
        @TestTargetNew(
            level = TestLevel.PARTIAL_COMPLETE,
            notes = "Functionality test: Tests INSERT ... SELECT with expressions in SELECT query",
            method = "executeQuery",
            args = {java.lang.String.class}
        )
    })
    public void testInsert4() throws SQLException {
        DatabaseCreator.fillSimpleTable1(conn);
        statement.execute("INSERT INTO " + DatabaseCreator.SIMPLE_TABLE2
                + " SELECT id, speed*10 AS speed, size-1 AS size FROM "
                + DatabaseCreator.SIMPLE_TABLE1);
        ResultSet r = statement.executeQuery("SELECT COUNT(*) FROM "
                + DatabaseCreator.SIMPLE_TABLE2 + " AS a JOIN "
                + DatabaseCreator.SIMPLE_TABLE1
                + " AS b ON a.speed = 10*b.speed AND a.size = b.size-1");
        r.next();
        assertEquals("Should be 2 rows", 2, r.getInt(1));
        r.close();
    }
    @TestTargets({
        @TestTargetNew(
            level = TestLevel.PARTIAL_COMPLETE,
            notes = "Functionality test: Inserts multiple rows using UNION ALL",
            method = "execute",
            args = {java.lang.String.class}
        ),
        @TestTargetNew(
            level = TestLevel.PARTIAL_COMPLETE,
            notes = "Functionality test: Inserts multiple rows using UNION ALL",
            method = "executeQuery",
            args = {java.lang.String.class}
        )
    })
    public void testInsert5() throws SQLException {
        statement.execute("INSERT INTO " + DatabaseCreator.TEST_TABLE5
                + " SELECT 1 as testId, 2 as testValue "
                + "UNION SELECT 2 as testId, 3 as testValue "
                + "UNION SELECT 3 as testId, 4 as testValue");
        ResultSet r = statement.executeQuery("SELECT COUNT(*) FROM "
                + DatabaseCreator.TEST_TABLE5);
        r.next();
        assertEquals("Should be 3 rows", 3, r.getInt(1));
        r.close();
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "Functionality test: Tests INSERT with PreparedStatement",
        method = "executeQuery",
        args = {java.lang.String.class}
    )
    public void testInsertPrepared() throws SQLException {
        PreparedStatement stat = conn.prepareStatement("INSERT INTO "
                + DatabaseCreator.TEST_TABLE5 + " VALUES(?, ?)");
        stat.setInt(1, 1);
        stat.setString(2, "1");
        stat.execute();
        stat.setInt(1, 2);
        stat.setString(2, "3");
        stat.execute();
        ResultSet r = statement.executeQuery("SELECT COUNT(*) FROM "
                + DatabaseCreator.TEST_TABLE5
                + " WHERE (testId = 1 AND testValue = '1') "
                + "OR (testId = 2 AND testValue = '3')");
        r.next();
        assertEquals("Incorrect number of records", 2, r.getInt(1));
        r.close();
        stat.close();
    }
}
