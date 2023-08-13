@TestTargetClass(Statement.class)
public class DeleteFunctionalityTest extends TestCase {
    private static Connection conn = null;
    private static Statement statement = null;
    public void setUp() throws Exception {
        super.setUp();
        Support_SQL.loadDriver();
        conn = Support_SQL.getConnection();
        statement = conn.createStatement();
        createTestTables();
        DatabaseCreator.fillParentTable(conn);
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
                        .equals(DatabaseCreator.TEST_TABLE5)) {
                    statement.execute(DatabaseCreator.DROP_TABLE5);
                }
            }
            userTab.close();
            statement.execute(DatabaseCreator.CREATE_TABLE_PARENT);
            statement.execute(DatabaseCreator.CREATE_TABLE_FKSTRICT);
            statement.execute(DatabaseCreator.CREATE_TABLE_FKCASCADE);
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
            statement.execute(DatabaseCreator.DROP_TABLE5);
        } catch (SQLException e) {
            fail("Unexpected SQLException " + e.toString());
        }
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "Functionality test: Deletes row with no referencing ones and RESTRICT action",
        method = "execute",
        args = {java.lang.String.class}
    )
    public void testDelete1() throws SQLException {
        DatabaseCreator.fillFKStrictTable(conn);
        statement.execute("DELETE FROM " + DatabaseCreator.PARENT_TABLE
                + " WHERE id = 3;");
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "Functionality test: Deletes all referencing rows and then deletes referenced one",
        method = "execute",
        args = {java.lang.String.class}
    )
    public void testDelete3() throws SQLException {
        statement.execute("DELETE FROM " + DatabaseCreator.FKSTRICT_TABLE
                + " WHERE name_id = 1;");
        statement.execute("DELETE FROM " + DatabaseCreator.FKSTRICT_TABLE
                + " WHERE id = 1;");
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "Functionality test: Deletes row with no referencing ones and CASCADE action",
        method = "execute",
        args = {java.lang.String.class}
    )
    public void testDelete4() throws SQLException {
        DatabaseCreator.fillFKCascadeTable(conn);
        statement.execute("DELETE FROM " + DatabaseCreator.PARENT_TABLE
                + " WHERE id = 3;");
    }
    @TestTargets({
        @TestTargetNew(
            level = TestLevel.PARTIAL_COMPLETE,
            notes = "Functionality test: Attempts to delete row with referencing ones and CASCADE action - expecting all referencing rows will also be deleted",
            method = "execute",
            args = {java.lang.String.class}
        ),
        @TestTargetNew(
            level = TestLevel.PARTIAL_COMPLETE,
            notes = "Functionality test: Attempts to delete row with referencing ones and CASCADE action - expecting all referencing rows will also be deleted",
            method = "executeQuery",
            args = {java.lang.String.class}
        )
    })
    public void testDelete5() throws SQLException {
        statement.execute("DELETE FROM " + DatabaseCreator.PARENT_TABLE
                + " WHERE id = 1;");
        ResultSet r = statement.executeQuery("SELECT COUNT(*) FROM "
                + DatabaseCreator.FKCASCADE_TABLE + " WHERE name_id = 1;");
        r.next();
        assertEquals("Should be no rows", 0, r.getInt(1));
        r.close();
    }
    @TestTargetNew(
      level = TestLevel.PARTIAL_COMPLETE,
      notes = "Deletes rows using subquery in WHERE clause with foreign keys. Foreign keys not supported.",
      method = "execute",
      args = {String.class}
    )
    @KnownFailure("not supported")
    public void testDelete6() throws SQLException {
        DatabaseCreator.fillFKStrictTable(conn);
        statement.execute("DELETE FROM " + DatabaseCreator.FKSTRICT_TABLE
                + " WHERE name_id = ANY (SELECT id FROM "
                + DatabaseCreator.PARENT_TABLE + " WHERE id > 1)");
        ResultSet r = statement.executeQuery("SELECT COUNT(*) FROM "
                + DatabaseCreator.FKSTRICT_TABLE + " WHERE name_id = 1;");
        r.next();
        assertEquals("Should be 2 rows", 2, r.getInt(1));
        r.close();
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "Functionality test: Deletes rows using PreparedStatement",
        method = "executeQuery",
        args = {java.lang.String.class}
    )
    public void testDelete7() throws SQLException {
        DatabaseCreator.fillTestTable5(conn);
        PreparedStatement stat = conn.prepareStatement("DELETE FROM "
                + DatabaseCreator.TEST_TABLE5 + " WHERE testID = ?");
        stat.setInt(1, 1);
        stat.execute();
        stat.setInt(1, 2);
        stat.execute();
        ResultSet r = statement.executeQuery("SELECT COUNT(*) FROM "
                + DatabaseCreator.TEST_TABLE5 + " WHERE testID < 3 ");
        r.next();
        assertEquals(0, r.getInt(1));
        r.close();
        stat.close();
    }
}
