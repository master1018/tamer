@TestTargetClass(Statement.class)
public class UpdateFunctionalityTest extends TestCase {
    private static final int numberOfRecords = 20;
    private static Connection conn;
    private static Statement statement;
    public void setUp() throws Exception {
        super.setUp();
        Support_SQL.loadDriver();
        try {
            conn = Support_SQL.getConnection();
            statement = conn.createStatement();
            createTestTables();
        } catch (SQLException e) {
            fail("Unexpected SQLException " + e.toString());
        }
        DatabaseCreator.fillTestTable1(conn, numberOfRecords);
        DatabaseCreator.fillTestTable2(conn, numberOfRecords);
    }
    public void tearDown() throws Exception {
        deleteTestTables();
        statement.close();
        conn.close();
        super.tearDown();
    }
    protected void createTestTables() {
        try {
            DatabaseMetaData meta = conn.getMetaData();
            ResultSet userTab = meta.getTables(null, null, null, null);
            while (userTab.next()) {
                String tableName = userTab.getString("TABLE_NAME");
                if (tableName.equals(DatabaseCreator.TEST_TABLE1)) {
                    statement.execute(DatabaseCreator.DROP_TABLE1);
                } else if (tableName
                        .equals(DatabaseCreator.TEST_TABLE2)) {
                    statement.execute(DatabaseCreator.DROP_TABLE2);
                } else if (tableName
                        .equals(DatabaseCreator.TEST_TABLE3)) {
                    statement.execute(DatabaseCreator.DROP_TABLE3);
                }
            }
            userTab.close();
            statement.execute(DatabaseCreator.CREATE_TABLE3);
            statement.execute(DatabaseCreator.CREATE_TABLE2);
            statement.execute(DatabaseCreator.CREATE_TABLE1);
        } catch (SQLException e) {
            fail("Unexpected SQLException " + e.toString());
        }
    }
    protected void deleteTestTables() {
        try {
            statement.execute(DatabaseCreator.DROP_TABLE1);
            statement.execute(DatabaseCreator.DROP_TABLE2);
            statement.execute(DatabaseCreator.DROP_TABLE3);
        } catch (SQLException e) {
            fail("Unexpected SQLException " + e.toString());
        }
    }
    @TestTargets({
        @TestTargetNew(
            level = TestLevel.PARTIAL_COMPLETE,
            notes = "Functionality test: Updates all values in one column in the table",
            method = "executeUpdate",
            args = {java.lang.String.class}
        ),
        @TestTargetNew(
            level = TestLevel.PARTIAL_COMPLETE,
            notes = "Functionality test: Updates all values in one column in the table",
            method = "executeQuery",
            args = {java.lang.String.class}
        )
    })
    public void testUpdate1() {
        String newValue = "newValue";
        String updateQuery = "UPDATE " + DatabaseCreator.TEST_TABLE1
                + " SET field1='" + newValue + "'";
        try {
            int num = statement.executeUpdate(updateQuery);
            assertEquals("Not all records in the database were updated",
                    numberOfRecords, num);
            String selectQuery = "SELECT field1 FROM "
                    + DatabaseCreator.TEST_TABLE1;
            ResultSet result = statement.executeQuery(selectQuery);
            while (result.next()) {
                assertEquals("The field field1 was not updated", newValue,
                        result.getString("field1"));
            }
            result.close();
        } catch (SQLException e) {
            fail("Unexpected exception" + e.getMessage());
        }
    }
    @TestTargets({
        @TestTargetNew(
            level = TestLevel.PARTIAL_COMPLETE,
            notes = "Functionality test: Updates values in one column in the table using where condition in update command",
            method = "executeUpdate",
            args = {java.lang.String.class}
        ),
        @TestTargetNew(
            level = TestLevel.PARTIAL_COMPLETE,
            notes = "Functionality test: Updates values in one column in the table using where condition in update command",
            method = "executeQuery",
            args = {java.lang.String.class}
        )
    })
    public void testUpdate2() {
        String newValue = "newValue";
        String updateQuery = "UPDATE " + DatabaseCreator.TEST_TABLE1
                + " SET field1='" + newValue + "' WHERE (id > 2) and (id < 10)";
        try {
            int num = statement.executeUpdate(updateQuery);
            int expectedUpdated = 7;
            assertEquals("Not all records in the database were updated",
                    expectedUpdated, num);
            String selectQuery = "SELECT * FROM " + DatabaseCreator.TEST_TABLE1;
            ResultSet result = statement.executeQuery(selectQuery);
            while (result.next()) {
                int id = result.getInt("id");
                String field1 = result.getString("field1");
                if ((id > 2) && (id < 10)) {
                    assertEquals("The field field1 was not updated", newValue,
                            field1);
                } else {
                    assertEquals("The field field1 was not updated",
                            DatabaseCreator.defaultString + id, field1);
                }
            }
            result.close();
        } catch (SQLException e) {
            fail("Unexpected exception" + e.getMessage());
        }
    }
    @TestTargets({
        @TestTargetNew(
            level = TestLevel.PARTIAL_COMPLETE,
            notes = "Functionality test: Updates values in a several columns in the table",
            method = "executeUpdate",
            args = {java.lang.String.class}
        ),
        @TestTargetNew(
            level = TestLevel.PARTIAL_COMPLETE,
            notes = "Functionality test: Updates values in a several columns in the table",
            method = "executeQuery",
            args = {java.lang.String.class}
        )
    })
    public void testUpdate3() {
        int newValue1 = -1;
        int newValue2 = -2;
        String updateQuery = "UPDATE " + DatabaseCreator.TEST_TABLE1
                + " SET field2=" + newValue1 + ", field3=" + newValue2;
        try {
            int num = statement.executeUpdate(updateQuery);
            assertEquals("Not all records in the database were updated",
                    numberOfRecords, num);
            String selectQuery = "SELECT field2, field3 FROM "
                    + DatabaseCreator.TEST_TABLE1;
            ResultSet result = statement.executeQuery(selectQuery);
            while (result.next()) {
            }
            result.close();
        } catch (SQLException e) {
            fail("Unexpected exception" + e.getMessage());
        }
    }
    @TestTargets({
        @TestTargetNew(
            level = TestLevel.PARTIAL_COMPLETE,
            notes = "Functionality test: Updates values in a several columns in the table using where condition in update command",
            method = "executeUpdate",
            args = {java.lang.String.class}
        ),
        @TestTargetNew(
            level = TestLevel.PARTIAL_COMPLETE,
            notes = "Functionality test: Updates values in a several columns in the table using where condition in update command",
            method = "executeQuery",
            args = {java.lang.String.class}
        )
    })
    public void testUpdate4() {
        int newValue1 = -1;
        int newValue2 = -2;
        String updateQuery = "UPDATE " + DatabaseCreator.TEST_TABLE1
                + " SET field2=" + newValue1 + ", field3=" + newValue2
                + " WHERE id > 10";
        try {
            int num = statement.executeUpdate(updateQuery);
            int expectedUpdated = 9;
            assertEquals("Not all records in the database were updated",
                    expectedUpdated, num);
            String selectQuery = "SELECT id, field2, field3 FROM "
                    + DatabaseCreator.TEST_TABLE1;
            ResultSet result = statement.executeQuery(selectQuery);
            while (result.next()) {
                int id = result.getInt("id");
            }
            result.close();
        } catch (SQLException e) {
            fail("Unexpected exception" + e.getMessage());
        }
    }
    @TestTargets({
        @TestTargetNew(
            level = TestLevel.PARTIAL_COMPLETE,
            notes = "Functionality test: Updates values in one columns in the table using condition",
            method = "executeUpdate",
            args = {java.lang.String.class}
        ),
        @TestTargetNew(
            level = TestLevel.PARTIAL_COMPLETE,
            notes = "Functionality test: Updates values in one columns in the table using condition",
            method = "executeQuery",
            args = {java.lang.String.class}
        )
    })
    public void testUpdate5() {
        int factor = 3;
        String updateQuery = "UPDATE " + DatabaseCreator.TEST_TABLE1
                + " SET field2=field2 *" + factor;
        try {
            String selectQuery = "SELECT field2 FROM "
                    + DatabaseCreator.TEST_TABLE1;
            ResultSet result = statement.executeQuery(selectQuery);
            HashSet<BigDecimal> values = new HashSet<BigDecimal>();
            int num = statement.executeUpdate(updateQuery);
            assertEquals("Not all records in the database were updated",
                    numberOfRecords, num);
            result = statement.executeQuery(selectQuery);
            assertTrue("Not all records were updated", values.isEmpty());
            result.close();
        } catch (SQLException e) {
            fail("Unexpected exception" + e.getMessage());
        }
    }
    @TestTargets({
        @TestTargetNew(
            level = TestLevel.PARTIAL_COMPLETE,
            notes = "Functionality test: Sets value of field2 to default",
            method = "executeUpdate",
            args = {java.lang.String.class}
        ),
        @TestTargetNew(
            level = TestLevel.PARTIAL_COMPLETE,
            notes = "Functionality test: Sets value of field2 to default",
            method = "executeQuery",
            args = {java.lang.String.class}
        )
    })
    public void testUpdate6() {
        String updateQuery = "UPDATE " + DatabaseCreator.TEST_TABLE1
                + " SET field2='1'";
        try {
            int num = statement.executeUpdate(updateQuery);
            assertEquals("Not all records in the database were updated",
                    numberOfRecords, num);
            String selectQuery = "SELECT field2 FROM "
                    + DatabaseCreator.TEST_TABLE1;
            ResultSet result = statement.executeQuery(selectQuery);
            result.close();
        } catch (SQLException e) {
            fail("Unexpected exception" + e.getMessage());
        }
    }
    @TestTargets({
        @TestTargetNew(
            level = TestLevel.PARTIAL_COMPLETE,
            notes = "Functionality test: Updates records in the table using subquery in update command",
            method = "executeUpdate",
            args = {java.lang.String.class}
        ),
        @TestTargetNew(
            level = TestLevel.PARTIAL_COMPLETE,
            notes = "Functionality test: Updates records in the table using subquery in update command",
            method = "executeQuery",
            args = {java.lang.String.class}
        )
    })
    public void testUpdate7() {
        String updateQuery = "UPDATE " + DatabaseCreator.TEST_TABLE1
                + " SET field2='1' WHERE id < ( SELECT COUNT(*) FROM "
                + DatabaseCreator.TEST_TABLE2 + " WHERE finteger > 15)";
        try {
            int num = statement.executeUpdate(updateQuery);
            int expectedUpdated = 4;
            assertEquals("Not all records in the database were updated",
                    expectedUpdated, num);
            String selectQuery = "SELECT id, field2 FROM "
                    + DatabaseCreator.TEST_TABLE1;
            ResultSet result = statement.executeQuery(selectQuery);
            while (result.next()) {
            }
            result.close();
        } catch (SQLException e) {
            fail("Unexpected exception" + e.getMessage());
        }
    }
    @TestTargets({
        @TestTargetNew(
            level = TestLevel.PARTIAL_COMPLETE,
            notes = "Functionality test: Sets value of field2 to NULL",
            method = "executeUpdate",
            args = {java.lang.String.class}
        ),
        @TestTargetNew(
            level = TestLevel.PARTIAL_COMPLETE,
            notes = "Functionality test: Sets value of field2 to NULL",
            method = "executeQuery",
            args = {java.lang.String.class}
        )
    })
    public void testUpdate8() {
        String updateQuery = "UPDATE " + DatabaseCreator.TEST_TABLE1
                + " SET field2=NULL";
        try {
            int num = statement.executeUpdate(updateQuery);
            assertEquals("Not all records in the database were updated",
                    numberOfRecords, num);
            String selectQuery = "SELECT field2 FROM "
                    + DatabaseCreator.TEST_TABLE1;
            ResultSet result = statement.executeQuery(selectQuery);
            while (result.next()) {
                assertNull("value of field2 should be NULL", result
                        .getObject("field2"));
            }
            result.close();
        } catch (SQLException e) {
            fail("Unexpected exception" + e.getMessage());
        }
    }
}
