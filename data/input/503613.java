@TestTargetClass(DatabaseMetaData.class)
public class DatabaseMetaDataNotSupportedTest extends TestCase {
    private static String VIEW_NAME = "myView";
    private static String CREATE_VIEW_QUERY = "CREATE VIEW " + VIEW_NAME
            + " AS SELECT * FROM " + DatabaseCreator.TEST_TABLE1;
    private static String DROP_VIEW_QUERY = "DROP VIEW " + VIEW_NAME;
    protected static Connection conn;
    protected static DatabaseMetaData meta;
    protected static Statement statement;
    protected static Statement statementForward;
    private static int id = 1;
    public void setUp() throws Exception {
        super.setUp();
        Support_SQL.loadDriver();
        try {
            conn = Support_SQL.getConnection();
            meta = conn.getMetaData();
            statement = conn.createStatement();
            createTestTables();
        } catch (SQLException e) {
            System.out.println("Error in test setup: "+e.getMessage());
        }
    }
    public void tearDown() throws Exception {
        try {
            conn = Support_SQL.getConnection();
            meta = conn.getMetaData();
            statement = conn.createStatement();
            deleteTestTables();
        } catch (SQLException e) {
            System.out.println("Error in teardown: "+e.getMessage());
        } finally {
            try {
                conn.close();
            } catch (SQLException e) {
            }
        }
        super.tearDown();
    }
    private void createTestTables() {
        try {
            ResultSet userTab = meta.getTables(null, null, null, null);
            while (userTab.next()) {
                String tableName = userTab.getString("TABLE_NAME");
                if (tableName.equals(DatabaseCreator.TEST_TABLE1)) {
                    statement.execute(DatabaseCreator.DROP_TABLE1);
                } else if (tableName
                        .equals(DatabaseCreator.TEST_TABLE3)) {
                    statement.execute(DatabaseCreator.DROP_TABLE3);
                } else if (tableName.equals(VIEW_NAME)) {
                    statement.execute(DROP_VIEW_QUERY);
                }
            }
            userTab.close();
            statement.execute(DatabaseCreator.CREATE_TABLE3);
            statement.execute(DatabaseCreator.CREATE_TABLE1);
            statement.execute(CREATE_VIEW_QUERY);
            meta = conn.getMetaData();
        } catch (SQLException e) {
            fail("Unexpected SQLException " + e.toString());
        }
    }
    private void deleteTestTables() {
        try {
            statement.execute(DatabaseCreator.DROP_TABLE1);
            statement.execute(DatabaseCreator.DROP_TABLE3);
            statement.execute(DROP_VIEW_QUERY);
        } catch (SQLException e) {
            fail("Unexpected SQLException " + e.toString());
        } finally {
            try {
            if (! conn.isClosed()) {
                conn.close();
            }
            } catch (SQLException e) {
            }
        }
    }
    @TestTargetNew(
        level = TestLevel.NOT_FEASIBLE,
        notes = "Granting not supported.",
        method = "allProceduresAreCallable",
        args = {}
    )
    public void test_allProceduresAreCallable() throws SQLException {
        assertFalse(meta.allProceduresAreCallable());
    }
    @TestTargetNew(
        level = TestLevel.NOT_FEASIBLE,
        notes = "test fails. GRANT and REVOKE not supported",
        method = "allTablesAreSelectable",
        args = {}
    )
    @KnownFailure("Not supported ops applied")
    public void test_allTablesAreSelectable() throws SQLException {
        String query = "GRANT CREATE, SELECT ON " + DatabaseCreator.TEST_TABLE1
                + " TO " + Support_SQL.sqlUser;
        statement.execute(query);
        Connection userConn = Support_SQL.getConnection(Support_SQL.sqlUrl,
                Support_SQL.sqlUser, Support_SQL.sqlUser);
        DatabaseMetaData userMeta = userConn.getMetaData();
        ResultSet userTab = userMeta.getTables(null, null, null, null);
        assertTrue("Tables are not obtained", userTab.next());
        assertEquals("Incorrect name of obtained table",
                DatabaseCreator.TEST_TABLE1.toLowerCase(), userTab.getString(
                        "TABLE_NAME").toLowerCase());
        assertTrue("Not all of obtained tables are selectable", userMeta
                .allTablesAreSelectable());
        userTab.close();
        query = "REVOKE SELECT ON " + DatabaseCreator.TEST_TABLE1 + " FROM "
                + Support_SQL.sqlUser;
        statement.execute(query);
        userTab = userMeta.getTables(null, null, null, null);
        assertTrue("Tables are not obtained", userTab.next());
        assertEquals("Incorrect name of obtained table",
                DatabaseCreator.TEST_TABLE1.toLowerCase(), userTab.getString(
                        "TABLE_NAME").toLowerCase());
        assertFalse("No SELECT privileges", userMeta.allTablesAreSelectable());
        userTab.close();
        query = "REVOKE CREATE ON " + DatabaseCreator.TEST_TABLE1 + " FROM "
                + Support_SQL.sqlUser;
        statement.execute(query);
        userConn.close();
    }
    @TestTargetNew(
        level = TestLevel.NOT_FEASIBLE,
        notes = "not supported",
        method = "dataDefinitionCausesTransactionCommit",
        args = {}
    )
    public void test_dataDefinitionCausesTransactionCommit()
            throws SQLException {
    }
    @TestTargetNew(
        level = TestLevel.NOT_FEASIBLE,
        notes = "not supported",
        method = "dataDefinitionIgnoredInTransactions",
        args = {}
    )
    public void test_dataDefinitionIgnoredInTransactions() throws SQLException {
        assertFalse(meta.dataDefinitionIgnoredInTransactions());
    }
    @TestTargetNew(
        level = TestLevel.NOT_FEASIBLE,
        notes = "not supported",
        method = "deletesAreDetected",
        args = {int.class}
    )
    public void test_deletesAreDetectedI() throws SQLException {
        assertFalse(meta.deletesAreDetected(0));
    }
    @TestTargetNew(
        level = TestLevel.NOT_FEASIBLE,
        notes = "not supported",
        method = "doesMaxRowSizeIncludeBlobs",
        args = {}
    )
    @KnownFailure("not supported")
    public void test_doesMaxRowSizeIncludeBlobs() throws SQLException {
        assertFalse(meta.doesMaxRowSizeIncludeBlobs());
    }
    @TestTargetNew(
        level = TestLevel.NOT_FEASIBLE,
        notes = "not supported",
        method = "getAttributes",
        args = {java.lang.String.class, java.lang.String.class, java.lang.String.class, java.lang.String.class}
    )
    public void test_getAttributesLjava_lang_StringLjava_lang_StringLjava_lang_StringLjava_lang_String()
            throws SQLException {
    }
    @TestTargetNew(
        level = TestLevel.NOT_FEASIBLE,
        notes = "not supported. not supported. Received result wasn't checked.",
        method = "getCatalogs",
        args = {}
    )
    public void test_getCatalogs() throws SQLException {
        ResultSet rs = meta.getCatalogs();
        rs.close();
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "not supported",
        method = "getCatalogSeparator",
        args = {}
    )
    public void test_getCatalogSeparator() throws SQLException {
        assertTrue("Incorrect catalog separator", "".equals(meta
                .getCatalogSeparator().trim()));
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "not supported",
        method = "getCatalogTerm",
        args = {}
    )
    public void test_getCatalogTerm() throws SQLException {
        assertTrue("Incorrect catalog term", "".equals(meta
                .getCatalogSeparator().trim()));
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "not supported",
        method = "getExtraNameCharacters",
        args = {}
    )
    public void test_getExtraNameCharacters() throws SQLException {
        assertNotNull("Incorrect extra name characters", meta
                .getExtraNameCharacters());
    }
    @TestTargetNew(
        level = TestLevel.NOT_FEASIBLE,
        notes = "not supported. not supported. Received result wasn't checked.",
        method = "getIndexInfo",
        args = {java.lang.String.class, java.lang.String.class, java.lang.String.class, boolean.class, boolean.class}
    )
    @KnownFailure("not supported")
    public void test_getIndexInfoLjava_lang_StringLjava_lang_StringLjava_lang_StringZZ()
            throws SQLException {
        boolean unique = false;
        ResultSet rs = meta.getIndexInfo(conn.getCatalog(), null,
                DatabaseCreator.TEST_TABLE1, unique, true);
        ResultSetMetaData rsmd = rs.getMetaData();
        assertTrue("Rows do not obtained", rs.next());
        int col = rsmd.getColumnCount();
        assertEquals("Incorrect number of columns", 13, col);
        String[] columnNames = { "TABLE_CAT", "TABLE_SCHEM", "TABLE_NAME",
                "NON_UNIQUE", "INDEX_QUALIFIER", "INDEX_NAME", "TYPE",
                "ORDINAL_POSITION", "COLUMN_NAME", "ASC_OR_DESC",
                "CARDINALITY", "PAGES", "FILTER_CONDITION" };
        for (int c = 1; c <= col; ++c) {
            assertEquals("Incorrect column name", columnNames[c - 1], rsmd
                    .getColumnName(c));
        }
        assertEquals("Incorrect table catalog", conn.getCatalog(), rs
                .getString("TABLE_CAT"));
        assertEquals("Incorrect table schema", null, rs
                .getString("TABLE_SCHEM"));
        assertEquals("Incorrect table name", DatabaseCreator.TEST_TABLE1, rs
                .getString("TABLE_NAME"));
        assertEquals("Incorrect state of uniquess", unique, rs
                .getBoolean("NON_UNIQUE"));
        assertEquals("Incorrect index catalog", "", rs
                .getString("INDEX_QUALIFIER"));
        assertEquals("Incorrect index name", "primary", rs.getString(
                "INDEX_NAME").toLowerCase());
        assertEquals("Incorrect index type", DatabaseMetaData.tableIndexOther,
                rs.getShort("TYPE"));
        assertEquals("Incorrect column sequence number within index", 1, rs
                .getShort("ORDINAL_POSITION"));
        assertEquals("Incorrect column name", "id", rs.getString("COLUMN_NAME"));
        assertEquals("Incorrect column sort sequence", "a", rs.getString(
                "ASC_OR_DESC").toLowerCase());
        assertEquals("Incorrect cardinality", 1, rs.getInt("CARDINALITY"));
        assertEquals("Incorrect value of pages", 0, rs.getInt("PAGES"));
        assertEquals("Incorrect filter condition", null, rs
                .getString("FILTER_CONDITION"));
        rs.close();
    }
    @TestTargetNew(
        level = TestLevel.NOT_FEASIBLE,
        notes = "not supported. Received result wasn't checked.",
        method = "getColumnPrivileges",
        args = {java.lang.String.class, java.lang.String.class, java.lang.String.class, java.lang.String.class}
    )
    @KnownFailure("not supported. Privileges are not supported.")
     public void test_getColumnPrivilegesLjava_lang_StringLjava_lang_StringLjava_lang_StringLjava_lang_String()
            throws SQLException {
        ResultSet rs = meta.getColumnPrivileges(conn.getCatalog(), null,
                DatabaseCreator.TEST_TABLE1, "id");
        ResultSetMetaData rsmd = rs.getMetaData();
        assertFalse("Rows are obtained", rs.next());
        rs.close();
        String query = "GRANT REFERENCES(id) ON " + DatabaseCreator.TEST_TABLE1
                + " TO " + Support_SQL.sqlLogin;
        statement.execute(query);
        rs = meta.getColumnPrivileges(conn.getCatalog(), null,
                DatabaseCreator.TEST_TABLE1, "id");
        rsmd = rs.getMetaData();
        assertTrue("Rows do not obtained", rs.next());
        int col = rsmd.getColumnCount();
        assertEquals("Incorrect number of columns", 8, col);
        String[] columnNames = { "TABLE_CAT", "TABLE_SCHEM", "TABLE_NAME",
                "COLUMN_NAME", "GRANTOR", "GRANTEE", "PRIVILEGE",
                "IS_GRANTABLE" };
        for (int c = 1; c <= col; ++c) {
            assertEquals("Incorrect column name", columnNames[c - 1], rsmd
                    .getColumnName(c));
        }
        assertEquals("Incorrect table catalogue", conn.getCatalog(), rs
                .getString("TABLE_CAT").toLowerCase());
        assertEquals("Incorrect table schema", null, rs
                .getString("TABLE_SCHEM"));
        assertEquals("Incorrect table name", DatabaseCreator.TEST_TABLE1, rs
                .getString("TABLE_NAME").toLowerCase());
        assertEquals("Incorrect column name", "id", rs.getString("COLUMN_NAME")
                .toLowerCase());
        assertEquals("Incorrect grantor", Support_SQL.sqlLogin + "@"
                + Support_SQL.sqlHost, rs.getString("GRANTOR").toLowerCase());
        assertTrue("Incorrect grantee",
                rs.getString("GRANTEE").indexOf("root") != -1);
        assertEquals("Incorrect privilege", "references", rs.getString(
                "PRIVILEGE").toLowerCase());
        query = "REVOKE REFERENCES(id) ON " + DatabaseCreator.TEST_TABLE1
                + " FROM " + Support_SQL.sqlLogin;
        statement.execute(query);
        rs.close();
    }
    @TestTargetNew(
        level = TestLevel.NOT_FEASIBLE,
        notes = "not supported. not supported. Received result wasn't checked.",
        method = "getExportedKeys",
        args = {java.lang.String.class, java.lang.String.class, java.lang.String.class}
    )
    @KnownFailure("not supported")
     public void test_getExportedKeysLjava_lang_StringLjava_lang_StringLjava_lang_String()
            throws SQLException {
        ResultSet rs = meta.getExportedKeys(conn.getCatalog(), null,
                DatabaseCreator.TEST_TABLE3);
        ResultSetMetaData rsmd = rs.getMetaData();
        assertTrue("Rows do not obtained", rs.next());
        int col = rsmd.getColumnCount();
        assertEquals("Incorrect number of columns", 14, col);
        String[] columnNames = { "PKTABLE_CAT", "PKTABLE_SCHEM",
                "PKTABLE_NAME", "PKCOLUMN_NAME", "FKTABLE_CAT",
                "FKTABLE_SCHEM", "FKTABLE_NAME", "FKCOLUMN_NAME", "KEY_SEQ",
                "UPDATE_RULE", "DELETE_RULE", "FK_NAME", "PK_NAME",
                "DEFERRABILITY" };
        for (int c = 1; c <= col; ++c) {
            assertEquals("Incorrect column name", columnNames[c - 1], rsmd
                    .getColumnName(c));
        }
        assertEquals("Incorrect primary key table catalog", conn.getCatalog(),
                rs.getString("PKTABLE_CAT"));
        assertEquals("Incorrect primary key table schema", null, rs
                .getString("PKTABLE_SCHEM"));
        assertEquals("Incorrect primary key table name",
                DatabaseCreator.TEST_TABLE3, rs.getString("PKTABLE_NAME"));
        assertEquals("Incorrect primary key column name", "fk", rs
                .getString("PKCOLUMN_NAME"));
        assertEquals("Incorrect foreign key table catalog", conn.getCatalog(),
                rs.getString("FKTABLE_CAT"));
        assertEquals("Incorrect foreign key table schema", null, rs
                .getString("FKTABLE_SCHEM"));
        assertEquals("Incorrect foreign key table name",
                DatabaseCreator.TEST_TABLE1, rs.getString("FKTABLE_NAME"));
        assertEquals("Incorrect foreign key column name", "fkey", rs
                .getString("FKCOLUMN_NAME"));
        assertEquals("Incorrect sequence number within foreign key", 1, rs
                .getShort("KEY_SEQ"));
        assertEquals("Incorrect update rule value",
                DatabaseMetaData.importedKeyNoAction, rs
                        .getShort("UPDATE_RULE"));
        assertEquals("Incorrect delete rule value",
                DatabaseMetaData.importedKeyNoAction, rs
                        .getShort("DELETE_RULE"));
        assertNotNull("Incorrect foreign key name", rs.getString("FK_NAME"));
        assertEquals("Incorrect primary key name", null, rs
                .getString("PK_NAME"));
        assertEquals("Incorrect deferrability",
                DatabaseMetaData.importedKeyNotDeferrable, rs
                        .getShort("DEFERRABILITY"));
        rs.close();
    }
    @TestTargetNew(
        level = TestLevel.NOT_FEASIBLE,
        notes = "not supported",
        method = "getProcedureColumns",
        args = {java.lang.String.class, java.lang.String.class, java.lang.String.class, java.lang.String.class}
    )
    public void test_getProcedureColumnsLjava_lang_StringLjava_lang_StringLjava_lang_StringLjava_lang_String()
            throws SQLException {
        meta.getProcedureColumns("", "", "", "");
    }
    @TestTargetNew(
        level = TestLevel.NOT_FEASIBLE,
        notes = "not supported",
        method = "getProcedures",
        args = {java.lang.String.class, java.lang.String.class, java.lang.String.class}
    )
    public void test_getProceduresLjava_lang_StringLjava_lang_StringLjava_lang_String()
            throws SQLException {
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "getProcedureTerm",
        args = {}
    )
    @KnownFailure("Exception test fails")
    public void test_getProcedureTerm() throws SQLException {
        assertTrue("Incorrect procedure term", "".equals(meta
                .getProcedureTerm().trim()));
        conn.close();
         try {
             meta.getProcedureTerm();
             fail("SQLException not thrown");
         } catch (SQLException e) {
         }
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "not supported",
        method = "getSchemaTerm",
        args = {}
    )
    @KnownFailure("Exception test fails")
    public void test_getSchemaTerm() throws SQLException {
        String term = meta.getSchemaTerm();
        assertNotNull("Incorrect schema term", term );
        assertTrue("".equals(term));
        conn.close();
         try {
             meta.getSchemaTerm();
             fail("SQLException not thrown");
         } catch (SQLException e) {
         }
    }
    @TestTargetNew(
        level = TestLevel.NOT_FEASIBLE,
        notes = "not supported",
        method = "getSuperTables",
        args = {java.lang.String.class, java.lang.String.class, java.lang.String.class}
    )
    public void test_getSuperTablesLjava_lang_StringLjava_lang_StringLjava_lang_String()
            throws SQLException {
    }
    @TestTargetNew(
        level = TestLevel.NOT_FEASIBLE,
        notes = "not supported",
        method = "getSuperTypes",
        args = {java.lang.String.class, java.lang.String.class, java.lang.String.class}
    )
    public void test_getSuperTypesLjava_lang_StringLjava_lang_StringLjava_lang_String()
            throws SQLException {
    }
    @TestTargetNew(
        level = TestLevel.NOT_FEASIBLE,
        notes = "not supported. Received result wasn't checked.",
        method = "getTablePrivileges",
        args = {java.lang.String.class, java.lang.String.class, java.lang.String.class}
    )
    @KnownFailure("not supported. Privileges are not supported.")
    public void test_getTablePrivilegesLjava_lang_StringLjava_lang_StringLjava_lang_String()
            throws SQLException {
        ResultSet privileges = meta.getTablePrivileges(conn.getCatalog(), "%",
                DatabaseCreator.TEST_TABLE3);
        assertFalse("Some privilegies exist", privileges.next());
        privileges.close();
        privileges = meta.getTablePrivileges(null, null, null);
        assertFalse("Some privilegies exist", privileges.next());
        privileges.close();
        HashSet<String> expectedPrivs = new HashSet<String>();
        expectedPrivs.add("CREATE");
        expectedPrivs.add("SELECT");
        String query = "GRANT CREATE, SELECT ON " + DatabaseCreator.TEST_TABLE3
                + " TO " + Support_SQL.sqlUser;
        statement.execute(query);
        privileges = meta.getTablePrivileges(conn.getCatalog(), null,
                DatabaseCreator.TEST_TABLE3);
        while (privileges.next()) {
            assertEquals("Wrong catalog name", Support_SQL.sqlCatalog,
                    privileges.getString("TABLE_CAT"));
            assertNull("Wrong schema", privileges.getString("TABLE_SCHEM"));
            assertEquals("Wrong table name", DatabaseCreator.TEST_TABLE3,
                    privileges.getString("TABLE_NAME"));
            assertTrue("Wrong privilege " + privileges.getString("PRIVILEGE"),
                    expectedPrivs.remove(privileges.getString("PRIVILEGE")));
            assertEquals("Wrong grantor", Support_SQL.sqlLogin + "@"
                    + Support_SQL.sqlHost, privileges.getString("GRANTOR"));
            assertEquals("Wrong grantee", Support_SQL.sqlUser + "@%",
                    privileges.getString("GRANTEE"));
            assertNull("Wrong value of IS_GRANTABLE", privileges
                    .getString("IS_GRANTABLE"));
        }
        privileges.close();
        assertTrue("Wrong privileges were returned", expectedPrivs.isEmpty());
        query = "REVOKE CREATE, SELECT ON " + DatabaseCreator.TEST_TABLE3
                + " FROM " + Support_SQL.sqlUser;
        statement.execute(query);
        String[] privs = new String[] { "ALTER", "CREATE", "CREATE VIEW",
                "DELETE", "DROP", "INDEX", "INSERT", "REFERENCES", "SELECT",
                "SHOW VIEW", "UPDATE" };
        expectedPrivs = new HashSet<String>();
        for (int i = 0; i < privs.length; i++) {
            expectedPrivs.add(privs[i]);
        }
        query = "GRANT ALL ON " + DatabaseCreator.TEST_TABLE3 + " TO "
                + Support_SQL.sqlUser;
        statement.execute(query);
        privileges = meta.getTablePrivileges(conn.getCatalog(), null,
                DatabaseCreator.TEST_TABLE3);
        while (privileges.next()) {
            assertEquals("Wrong catalog name", Support_SQL.sqlCatalog,
                    privileges.getString("TABLE_CAT"));
            assertNull("Wrong schema", privileges.getString("TABLE_SCHEM"));
            assertEquals("Wrong table name", DatabaseCreator.TEST_TABLE3,
                    privileges.getString("TABLE_NAME"));
            assertTrue("Wrong privilege " + privileges.getString("PRIVILEGE"),
                    expectedPrivs.remove(privileges.getString("PRIVILEGE")));
            assertEquals("Wrong grantor", Support_SQL.sqlLogin + "@"
                    + Support_SQL.sqlHost, privileges.getString("GRANTOR"));
            assertEquals("Wrong grantee", Support_SQL.sqlUser + "@%",
                    privileges.getString("GRANTEE"));
            assertNull("Wrong value of IS_GRANTABLE", privileges
                    .getString("IS_GRANTABLE"));
        }
        privileges.close();
        assertTrue("Wrong privileges were returned", expectedPrivs.isEmpty());
        query = "REVOKE ALL ON " + DatabaseCreator.TEST_TABLE3 + " FROM "
                + Support_SQL.sqlUser;
        statement.execute(query);
        privileges = meta.getTablePrivileges(conn.getCatalog(), "%",
                DatabaseCreator.TEST_TABLE3);
        assertFalse("Some privilegies exist", privileges.next());
        privileges.close();
        privileges = meta.getTablePrivileges(null, null, null);
        assertFalse("Some privilegies exist", privileges.next());
        privileges.close();
    }
    @TestTargetNew(
        level = TestLevel.NOT_FEASIBLE,
        notes = "not supported",
        method = "getUDTs",
        args = {java.lang.String.class, java.lang.String.class, java.lang.String.class, int[].class}
    )
    public void test_getUDTsLjava_lang_StringLjava_lang_StringLjava_lang_String$I()
            throws SQLException {
    }
    @TestTargetNew(
        level = TestLevel.NOT_FEASIBLE,
        notes = "not supported. Received result wasn't checked.Triggers not supported",
        method = "getVersionColumns",
        args = {java.lang.String.class, java.lang.String.class, java.lang.String.class}
    )
    @KnownFailure("Not supported ops applied")
    public void test_getVersionColumnsLjava_lang_StringLjava_lang_StringLjava_lang_String()
            throws SQLException {
        DatabaseMetaDataTest.insertNewRecord();
        String triggerName = "updateTrigger";
        String triggerQuery = "CREATE TRIGGER " + triggerName
                + " AFTER UPDATE ON " + DatabaseCreator.TEST_TABLE1
                + " FOR EACH ROW BEGIN INSERT INTO "
                + DatabaseCreator.TEST_TABLE3 + " SET fk = 10; END;";
        statementForward.execute(triggerQuery);
        String updateQuery = "UPDATE " + DatabaseCreator.TEST_TABLE1
                + " SET field1='fffff' WHERE id=1";
        statementForward.execute(updateQuery);
        ResultSet rs = meta.getVersionColumns(conn.getCatalog(), null,
                DatabaseCreator.TEST_TABLE1);
        assertTrue("Result set is empty", rs.next());
        rs.close();
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "isCatalogAtStart",
        args = {}
    )
    @KnownFailure("Exception test fails")
    public void test_isCatalogAtStart() throws SQLException {
        assertFalse(
                "catalog doesn't appear at the start of a fully qualified table name",
                meta.isCatalogAtStart());
        conn.close();
         try {
             meta.isCatalogAtStart();
             fail("SQLException not thrown");
         } catch (SQLException e) {
         }
    }
    @TestTargetNew(
        level = TestLevel.NOT_FEASIBLE,
        notes = "not supported",
        method = "locatorsUpdateCopy",
        args = {}
    )
    @KnownFailure("not supported")
    public void test_locatorsUpdateCopy() throws SQLException {
        assertFalse(meta.locatorsUpdateCopy());
    }
    @TestTargetNew(
        level = TestLevel.NOT_FEASIBLE,
        notes = "not supported",
        method = "nullPlusNonNullIsNull",
        args = {}
    )
    public void test_nullPlusNonNullIsNull() throws SQLException {
        assertFalse(meta.nullPlusNonNullIsNull());
    }
    @TestTargetNew(
        level = TestLevel.NOT_FEASIBLE,
        notes = "not supported",
        method = "nullsAreSortedAtEnd",
        args = {}
    )
    public void test_nullsAreSortedAtEnd() throws SQLException {
        assertFalse(meta.nullsAreSortedAtEnd());
    }
    @TestTargetNew(
        level = TestLevel.NOT_FEASIBLE,
        notes = "not supported",
        method = "nullsAreSortedAtStart",
        args = {}
    )
    public void test_nullsAreSortedAtStart() throws SQLException {
        assertFalse(meta.nullsAreSortedAtStart());
    }
    @TestTargetNew(
        level = TestLevel.NOT_FEASIBLE,
        notes = "not supported",
        method = "nullsAreSortedHigh",
        args = {}
    )
    public void test_nullsAreSortedHigh() throws SQLException {
        assertFalse(meta.nullsAreSortedHigh());
    }
    @TestTargetNew(
        level = TestLevel.NOT_FEASIBLE,
        notes = "not supported",
        method = "nullsAreSortedLow",
        args = {}
    )
    public void test_nullsAreSortedLow() throws SQLException {
        assertFalse(meta.nullsAreSortedLow());
    }
    @TestTargetNew(
        level = TestLevel.SUFFICIENT,
        notes = "Not Fully Supported.",
        method = "ownDeletesAreVisible",
        args = {int.class}
    )
    public void test_ownDeletesAreVisibleI() throws SQLException {
        assertFalse("result set's own deletes are visible for unknown type",
                meta.ownDeletesAreVisible(100));
    }
    @TestTargetNew(
        level = TestLevel.SUFFICIENT,
        notes = "not supported.",
        method = "ownInsertsAreVisible",
        args = {int.class}
    )
    public void test_ownInsertsAreVisibleI() throws SQLException {
        assertFalse("result set's own inserts are visible for unknown type",
                meta.ownInsertsAreVisible(100));
    }
    @TestTargetNew(
        level = TestLevel.NOT_FEASIBLE,
        notes = "not supported. Verification with invalid parameters missed.",
        method = "ownUpdatesAreVisible",
        args = {int.class}
    )
    public void test_ownUpdatesAreVisibleI() throws SQLException {
        assertFalse(
                "result set's own updates are visible for TYPE_FORWARD_ONLY type",
                meta.ownUpdatesAreVisible(ResultSet.TYPE_FORWARD_ONLY));
        assertFalse(
                "result set's own updates are visible for TYPE_SCROLL_INSENSITIVE type",
                meta.ownUpdatesAreVisible(ResultSet.TYPE_SCROLL_INSENSITIVE));
        assertFalse(
                "result set's own updates are visible for TYPE_SCROLL_SENSITIVE type",
                meta.ownUpdatesAreVisible(ResultSet.TYPE_SCROLL_SENSITIVE));
        assertFalse("result set's own updates are visible for unknown type",
                meta.ownUpdatesAreVisible(100));
    }
    @TestTargetNew(
        level = TestLevel.NOT_FEASIBLE,
        notes = "not supported",
        method = "storesLowerCaseIdentifiers",
        args = {}
    )
    public void test_storesLowerCaseIdentifiers() throws SQLException {
        assertFalse(meta.storesLowerCaseIdentifiers());
    }
    @TestTargetNew(
        level = TestLevel.NOT_FEASIBLE,
        notes = "not supported",
        method = "storesLowerCaseQuotedIdentifiers",
        args = {}
    )
    public void test_storesLowerCaseQuotedIdentifiers() throws SQLException {
        assertFalse(meta.storesLowerCaseQuotedIdentifiers());
    }
    @TestTargetNew(
        level = TestLevel.NOT_FEASIBLE,
        notes = "not supported",
        method = "storesUpperCaseIdentifiers",
        args = {}
    )
    public void test_storesUpperCaseIdentifiers() throws SQLException {
        assertFalse(meta.storesUpperCaseIdentifiers());
    }
    @TestTargetNew(
        level = TestLevel.NOT_FEASIBLE,
        notes = "not supported",
        method = "storesUpperCaseQuotedIdentifiers",
        args = {}
    )
    public void test_storesUpperCaseQuotedIdentifiers() throws SQLException {
        assertFalse(meta.storesUpperCaseQuotedIdentifiers());
    }
    @TestTargetNew(
        level = TestLevel.NOT_FEASIBLE,
        notes = "not supported",
        method = "supportsANSI92EntryLevelSQL",
        args = {}
    )
    @KnownFailure("not supported")
    public void test_supportsANSI92EntryLevelSQL() throws SQLException {
        assertFalse(meta.supportsANSI92EntryLevelSQL());
    }
    @TestTargetNew(
        level = TestLevel.NOT_FEASIBLE,
        notes = "not supported",
        method = "supportsANSI92FullSQL",
        args = {}
    )
    public void test_supportsANSI92FullSQL() throws SQLException {
        assertFalse(meta.supportsANSI92FullSQL());
    }
    @TestTargetNew(
        level = TestLevel.NOT_FEASIBLE,
        notes = "not supported",
        method = "supportsANSI92IntermediateSQL",
        args = {}
    )
    public void test_supportsANSI92IntermediateSQL() throws SQLException {
        assertFalse(meta.supportsANSI92IntermediateSQL());
    }
    @TestTargetNew(
        level = TestLevel.NOT_FEASIBLE,
        notes = "not supported",
        method = "supportsAlterTableWithAddColumn",
        args = {}
    )
    public void test_supportsAlterTableWithAddColumn() throws SQLException {
        assertFalse(meta.supportsAlterTableWithAddColumn());
    }
    @TestTargetNew(
        level = TestLevel.NOT_FEASIBLE,
        notes = "not supported",
        method = "supportsAlterTableWithDropColumn",
        args = {}
    )
    public void test_supportsAlterTableWithDropColumn() throws SQLException {
        assertFalse(meta.supportsAlterTableWithDropColumn());
    }
    @TestTargetNew(
        level = TestLevel.NOT_FEASIBLE,
        notes = "not supported",
        method = "supportsBatchUpdates",
        args = {}
    )
    public void test_supportsBatchUpdates() throws SQLException {
        assertFalse(meta.supportsBatchUpdates());
    }
    @TestTargetNew(
        level = TestLevel.NOT_FEASIBLE,
        notes = "not supported",
        method = "supportsCatalogsInDataManipulation",
        args = {}
    )
    public void test_supportsCatalogsInDataManipulation() throws SQLException {
        assertFalse(meta.supportsCatalogsInDataManipulation());
    }
    @TestTargetNew(
        level = TestLevel.NOT_FEASIBLE,
        notes = "not supported",
        method = "supportsCatalogsInIndexDefinitions",
        args = {}
    )
    public void test_supportsCatalogsInIndexDefinitions() throws SQLException {
        assertFalse(meta.supportsCatalogsInIndexDefinitions());
    }
    @TestTargetNew(
        level = TestLevel.NOT_FEASIBLE,
        notes = "not supported",
        method = "supportsCatalogsInPrivilegeDefinitions",
        args = {}
    )
    public void test_supportsCatalogsInPrivilegeDefinitions()
            throws SQLException {
        assertFalse(meta.supportsCatalogsInPrivilegeDefinitions());
    }
    @TestTargetNew(
        level = TestLevel.NOT_FEASIBLE,
        notes = "not supported",
        method = "supportsCatalogsInProcedureCalls",
        args = {}
    )
    public void test_supportsCatalogsInProcedureCalls() throws SQLException {
        assertFalse(meta.supportsCatalogsInProcedureCalls());
    }
    @TestTargetNew(
        level = TestLevel.NOT_FEASIBLE,
        notes = "not supported",
        method = "supportsCatalogsInTableDefinitions",
        args = {}
    )
    public void test_supportsCatalogsInTableDefinitions() throws SQLException {
        assertFalse(meta.supportsCatalogsInTableDefinitions());
    }
    @TestTargetNew(
        level = TestLevel.NOT_FEASIBLE,
        notes = "not supported",
        method = "supportsConvert",
        args = {}
    )
    public void test_supportsConvert() throws SQLException {
        assertFalse(meta.supportsConvert());
    }
    @TestTargetNew(
        level = TestLevel.NOT_FEASIBLE,
        notes = "not supported",
        method = "supportsConvert",
        args = {int.class, int.class}
    )
    public void test_supportsConvertII() throws SQLException {
        assertFalse(meta.supportsConvert());
    }
    @TestTargetNew(
        level = TestLevel.NOT_FEASIBLE,
        notes = "not supported",
        method = "supportsCoreSQLGrammar",
        args = {}
    )
    public void test_supportsCoreSQLGrammar() throws SQLException {
        assertFalse(meta.supportsCoreSQLGrammar());
    }
    @TestTargetNew(
        level = TestLevel.NOT_FEASIBLE,
        notes = "not supported",
        method = "supportsCorrelatedSubqueries",
        args = {}
    )
    public void test_supportsCorrelatedSubqueries() throws SQLException {
        assertFalse(meta.supportsCorrelatedSubqueries());
    }
    @TestTargetNew(
        level = TestLevel.NOT_FEASIBLE,
        notes = "not supported",
        method = "supportsDataDefinitionAndDataManipulationTransactions",
        args = {}
    )
    @KnownFailure("not supported")
    public void test_supportsDataDefinitionAndDataManipulationTransactions()
            throws SQLException {
        assertFalse(meta.supportsDataDefinitionAndDataManipulationTransactions());
    }
    @TestTargetNew(
        level = TestLevel.NOT_FEASIBLE,
        notes = "not supported",
        method = "supportsDataManipulationTransactionsOnly",
        args = {}
    )
    public void test_supportsDataManipulationTransactionsOnly()
            throws SQLException {
        assertFalse(meta.supportsDataManipulationTransactionsOnly());
    }
    @TestTargetNew(
        level = TestLevel.NOT_FEASIBLE,
        notes = "not supported",
        method = "supportsDifferentTableCorrelationNames",
        args = {}
    )
    public void test_supportsDifferentTableCorrelationNames()
            throws SQLException {
        assertFalse(meta.supportsDifferentTableCorrelationNames());
    }
    @TestTargetNew(
        level = TestLevel.NOT_FEASIBLE,
        notes = "not supported",
        method = "supportsExtendedSQLGrammar",
        args = {}
    )
    public void test_supportsExtendedSQLGrammar() throws SQLException {
        assertFalse(meta.supportsExtendedSQLGrammar());
    }
    @TestTargetNew(
        level = TestLevel.NOT_FEASIBLE,
        notes = "not supported",
        method = "supportsFullOuterJoins",
        args = {}
    )
    public void test_supportsFullOuterJoins() throws SQLException {
        assertFalse(meta.supportsFullOuterJoins());
    }
    @TestTargetNew(
        level = TestLevel.NOT_FEASIBLE,
        notes = "not supported",
        method = "supportsGetGeneratedKeys",
        args = {}
    )
    public void test_supportsGetGeneratedKeys() throws SQLException {
        assertFalse(meta.supportsGetGeneratedKeys());
    }
    @TestTargetNew(
        level = TestLevel.NOT_FEASIBLE,
        notes = "not supported",
        method = "supportsGroupByBeyondSelect",
        args = {}
    )
    public void test_supportsGroupByBeyondSelect() throws SQLException {
        assertFalse(meta.supportsGroupByBeyondSelect());
    }
    @TestTargetNew(
        level = TestLevel.NOT_FEASIBLE,
        notes = "not supported",
        method = "supportsIntegrityEnhancementFacility",
        args = {}
    )
    public void test_supportsIntegrityEnhancementFacility() throws SQLException {
        assertFalse(meta.supportsIntegrityEnhancementFacility());
    }
    @TestTargetNew(
        level = TestLevel.NOT_FEASIBLE,
        notes = "not supported",
        method = "supportsLikeEscapeClause",
        args = {}
    )
    public void test_supportsLikeEscapeClause() throws SQLException {
        assertFalse(meta.supportsLikeEscapeClause());
    }
    @TestTargetNew(
        level = TestLevel.NOT_FEASIBLE,
        notes = "not supported",
        method = "supportsLimitedOuterJoins",
        args = {}
    )
    public void test_supportsLimitedOuterJoins() throws SQLException {
        assertFalse(meta.supportsLimitedOuterJoins());
    }
    @TestTargetNew(
        level = TestLevel.NOT_FEASIBLE,
        notes = "not supported",
        method = "supportsMinimumSQLGrammar",
        args = {}
    )
    @KnownFailure("not supported")
    public void test_supportsMinimumSQLGrammar() throws SQLException {
        assertFalse(meta.supportsMinimumSQLGrammar());
    }
    @TestTargetNew(
        level = TestLevel.NOT_FEASIBLE,
        notes = "not supported",
        method = "supportsMixedCaseIdentifiers",
        args = {}
    )
    public void test_supportsMixedCaseIdentifiers() throws SQLException {
        assertFalse(meta.supportsMixedCaseIdentifiers());
    }
    @TestTargetNew(
        level = TestLevel.NOT_FEASIBLE,
        notes = "not supported",
        method = "supportsMixedCaseQuotedIdentifiers",
        args = {}
    )
    public void test_supportsMixedCaseQuotedIdentifiers() throws SQLException {
        assertFalse(meta.supportsMixedCaseQuotedIdentifiers());
    }
    @TestTargetNew(
        level = TestLevel.NOT_FEASIBLE,
        notes = "not supported",
        method = "supportsMultipleOpenResults",
        args = {}
    )
    public void test_supportsMultipleOpenResults() throws SQLException {
        assertFalse(meta.supportsMultipleOpenResults());
    }
    @TestTargetNew(
        level = TestLevel.NOT_FEASIBLE,
        notes = "not supported",
        method = "supportsMultipleResultSets",
        args = {}
    )
    public void test_supportsMultipleResultSets() throws SQLException {
        assertFalse(meta.supportsMultipleResultSets());
    }
    @TestTargetNew(
        level = TestLevel.NOT_FEASIBLE,
        notes = "not supported",
        method = "supportsMultipleTransactions",
        args = {}
    )
    public void test_supportsMultipleTransactions() throws SQLException {
        assertFalse(meta.supportsMultipleTransactions());
    }
    @TestTargetNew(
        level = TestLevel.NOT_FEASIBLE,
        notes = "not supported",
        method = "supportsNamedParameters",
        args = {}
    )
    public void test_supportsNamedParameters() throws SQLException {
        assertFalse(meta.supportsNamedParameters());
    }
    @TestTargetNew(
        level = TestLevel.NOT_FEASIBLE,
        notes = "not supported",
        method = "supportsOpenCursorsAcrossCommit",
        args = {}
    )
    public void test_supportsOpenCursorsAcrossCommit() throws SQLException {
        assertFalse(meta.supportsOpenCursorsAcrossCommit());
    }
    @TestTargetNew(
        level = TestLevel.NOT_FEASIBLE,
        notes = "not supported",
        method = "supportsOpenCursorsAcrossRollback",
        args = {}
    )
    public void test_supportsOpenCursorsAcrossRollback() throws SQLException {
        assertFalse(meta.supportsOpenCursorsAcrossRollback());
    }
    @TestTargetNew(
        level = TestLevel.NOT_FEASIBLE,
        notes = "not supported",
        method = "supportsOpenStatementsAcrossCommit",
        args = {}
    )
    public void test_supportsOpenStatementsAcrossCommit() throws SQLException {
        assertFalse(meta.supportsOpenStatementsAcrossCommit());
    }
    @TestTargetNew(
        level = TestLevel.NOT_FEASIBLE,
        notes = "not supported",
        method = "supportsOpenStatementsAcrossRollback",
        args = {}
    )
    public void test_supportsOpenStatementsAcrossRollback() throws SQLException {
        assertFalse(meta.supportsOpenStatementsAcrossRollback());
    }
    @TestTargetNew(
        level = TestLevel.NOT_FEASIBLE,
        notes = "not supported",
        method = "supportsOuterJoins",
        args = {}
    )
    public void test_supportsOuterJoins() throws SQLException {
        assertFalse(meta.supportsOuterJoins());
    }
    @TestTargetNew(
        level = TestLevel.NOT_FEASIBLE,
        notes = "not supported",
        method = "supportsPositionedDelete",
        args = {}
    )
    public void test_supportsPositionedDelete() throws SQLException {
        assertFalse(meta.supportsPositionedDelete());
    }
    @TestTargetNew(
        level = TestLevel.NOT_FEASIBLE,
        notes = "not supported",
        method = "supportsPositionedUpdate",
        args = {}
    )
    public void test_supportsPositionedUpdate() throws SQLException {
        assertFalse(meta.supportsPositionedUpdate());
    }
    @TestTargetNew(
        level = TestLevel.NOT_FEASIBLE,
        notes = "not supported",
        method = "supportsResultSetConcurrency",
        args = {int.class, int.class}
    )
    public void test_supportsResultSetConcurrencyII() throws SQLException {
        assertFalse(meta.supportsResultSetConcurrency(0,0));
    }
    @TestTargetNew(
        level = TestLevel.NOT_FEASIBLE,
        notes = "not supported",
        method = "supportsResultSetHoldability",
        args = {int.class}
    )
    public void test_supportsResultSetHoldabilityI() throws SQLException {
        assertFalse(meta.supportsResultSetHoldability(0));
    }
    @TestTargetNew(
        level = TestLevel.NOT_FEASIBLE,
        notes = "not supported. Verification with invalid parameters missed.",
        method = "supportsResultSetType",
        args = {int.class}
    )
    @KnownFailure("not supported")
    public void test_supportsResultSetTypeI() throws SQLException {
        assertTrue("database supports TYPE_FORWARD_ONLY type", meta
                .supportsResultSetType(ResultSet.TYPE_FORWARD_ONLY));
        assertFalse("database doesn't support TYPE_SCROLL_INSENSITIVE type",
                meta.supportsResultSetType(ResultSet.TYPE_SCROLL_INSENSITIVE));
        assertFalse("database supports TYPE_SCROLL_SENSITIVE type", meta
                .supportsResultSetType(ResultSet.TYPE_SCROLL_SENSITIVE));
        assertFalse("database supports unknown type", meta
                .supportsResultSetType(100));
    }
    @TestTargetNew(
        level = TestLevel.NOT_FEASIBLE,
        notes = "not supported",
        method = "supportsSavepoints",
        args = {}
    )
    public void test_supportsSavepoints() throws SQLException {
        assertFalse(meta.supportsSavepoints());
    }
    @TestTargetNew(
        level = TestLevel.NOT_FEASIBLE,
        notes = "not supported",
        method = "supportsSchemasInDataManipulation",
        args = {}
    )
    public void test_supportsSchemasInDataManipulation() throws SQLException {
        assertFalse(meta.supportsSchemasInDataManipulation());
    }
    @TestTargetNew(
        level = TestLevel.NOT_FEASIBLE,
        notes = "not supported",
        method = "supportsSchemasInIndexDefinitions",
        args = {}
    )
    public void test_supportsSchemasInIndexDefinitions() throws SQLException {
        assertFalse(meta.supportsSchemasInIndexDefinitions());
    }
    @TestTargetNew(
        level = TestLevel.NOT_FEASIBLE,
        notes = "not supported",
        method = "supportsSchemasInPrivilegeDefinitions",
        args = {}
    )
    public void test_supportsSchemasInPrivilegeDefinitions()
            throws SQLException {
    }
    @TestTargetNew(
        level = TestLevel.NOT_FEASIBLE,
        notes = "not supported",
        method = "supportsSchemasInProcedureCalls",
        args = {}
    )
    public void test_supportsSchemasInProcedureCalls() throws SQLException {
        assertFalse(meta.supportsSchemasInProcedureCalls());
    }
    @TestTargetNew(
        level = TestLevel.NOT_FEASIBLE,
        notes = "not supported",
        method = "supportsSchemasInTableDefinitions",
        args = {}
    )
    public void test_supportsSchemasInTableDefinitions() throws SQLException {
        assertFalse(meta.supportsSchemasInTableDefinitions());
    }
    @TestTargetNew(
        level = TestLevel.NOT_FEASIBLE,
        notes = "not supported",
        method = "supportsStatementPooling",
        args = {}
    )
    public void test_supportsStatementPooling() throws SQLException {
        assertFalse(meta.supportsStatementPooling());
    }
    @TestTargetNew(
        level = TestLevel.NOT_FEASIBLE,
        notes = "not supported",
        method = "supportsStoredProcedures",
        args = {}
    )
    public void test_supportsStoredProcedures() throws SQLException {
        assertFalse(meta.supportsStoredProcedures());
    }
    @TestTargetNew(
        level = TestLevel.NOT_FEASIBLE,
        notes = "not supported",
        method = "supportsSubqueriesInComparisons",
        args = {}
    )
    @KnownFailure("not supported")
    public void test_supportsSubqueriesInComparisons() throws SQLException {
        assertFalse(meta.supportsSubqueriesInComparisons());
    }
    @TestTargetNew(
        level = TestLevel.NOT_FEASIBLE,
        notes = "not supported",
        method = "supportsSubqueriesInIns",
        args = {}
    )
    @KnownFailure("not supported")
    public void test_supportsSubqueriesInIns() throws SQLException {
        assertFalse(meta.supportsSubqueriesInIns());
    }
    @TestTargetNew(
        level = TestLevel.NOT_FEASIBLE,
        notes = "not supported",
        method = "supportsSubqueriesInQuantifieds",
        args = {}
    )
    public void test_supportsSubqueriesInQuantifieds() throws SQLException {
        assertFalse(meta.supportsSubqueriesInQuantifieds());
    }
    @TestTargetNew(
        level = TestLevel.NOT_FEASIBLE,
        notes = "not supported",
        method = "supportsTransactions",
        args = {}
    )
    @KnownFailure("not supported")
    public void test_supportsTransactions() throws SQLException {
        assertFalse(meta.supportsTransactions());
    }
    @TestTargetNew(
        level = TestLevel.NOT_FEASIBLE,
        notes = "not supported",
        method = "supportsUnion",
        args = {}
    )
    public void test_supportsUnion() throws SQLException {
        assertFalse(meta.supportsUnion());
    }
    @TestTargetNew(
        level = TestLevel.NOT_FEASIBLE,
        notes = "not supported",
        method = "supportsUnionAll",
        args = {}
    )
    public void test_supportsUnionAll() throws SQLException {
        assertFalse(meta.supportsUnionAll());
    }
    @TestTargetNew(
        level = TestLevel.NOT_FEASIBLE,
        notes = "not supported",
        method = "usesLocalFilePerTable",
        args = {}
    )
    public void test_usesLocalFilePerTable() throws SQLException {
        assertFalse(meta.usesLocalFilePerTable());
    }
    @TestTargetNew(
        level = TestLevel.NOT_FEASIBLE,
        notes = "not supported",
        method = "usesLocalFiles",
        args = {}
    )
    @KnownFailure("not supported")
    public void test_usesLocalFiles() throws SQLException {
        assertFalse(meta.usesLocalFiles());
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "getMaxBinaryLiteralLength",
        args = {}
    )
    public void test_getMaxBinaryLiteralLength() throws SQLException {
        assertTrue("Incorrect binary literal length", meta
                .getMaxBinaryLiteralLength() == 0);
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "getMaxCatalogNameLength",
        args = {}
    )
    public void test_getMaxCatalogNameLength() throws SQLException {
        assertTrue("Incorrect name length", meta.getMaxCatalogNameLength() == 0);
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "getMaxCharLiteralLength",
        args = {}
    )
    public void test_getMaxCharLiteralLength() throws SQLException {
        assertTrue("Incorrect char literal length", meta
                .getMaxCharLiteralLength() == 0);
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "getMaxColumnNameLength",
        args = {}
    )
    public void test_getMaxColumnNameLength() throws SQLException {
        assertTrue("Incorrect column name length", meta
                .getMaxColumnNameLength() == 0);
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "getMaxColumnsInGroupBy",
        args = {}
    )
    public void test_getMaxColumnsInGroupBy() throws SQLException {
        assertTrue("Incorrect number of columns",
                meta.getMaxColumnsInGroupBy() == 0);
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "getMaxColumnsInIndex",
        args = {}
    )
    public void test_getMaxColumnsInIndex() throws SQLException {
        assertTrue("Incorrect number of columns",
                meta.getMaxColumnsInIndex() == 0);
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "getMaxColumnsInOrderBy",
        args = {}
    )
    public void test_getMaxColumnsInOrderBy() throws SQLException {
        assertTrue("Incorrect number of columns",
                meta.getMaxColumnsInOrderBy() == 0);
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "getMaxColumnsInSelect",
        args = {}
    )
    public void test_getMaxColumnsInSelect() throws SQLException {
        assertTrue("Incorrect number of columns",
                meta.getMaxColumnsInSelect() == 0);
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "getMaxColumnsInTable",
        args = {}
    )
    public void test_getMaxColumnsInTable() throws SQLException {
        assertTrue("Incorrect number of columns",
                meta.getMaxColumnsInTable() == 0);
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "getMaxConnections",
        args = {}
    )
    public void test_getMaxConnections() throws SQLException {
        assertTrue("Incorrect number of connections",
                meta.getMaxConnections() == 0);
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "not supported",
        method = "getMaxIndexLength",
        args = {}
    )
    public void test_getMaxIndexLength() throws SQLException {
        assertTrue("Incorrect length of index", meta.getMaxIndexLength() == 0);
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "not supported",
        method = "getMaxProcedureNameLength",
        args = {}
    )
    public void test_getMaxProcedureNameLength() throws SQLException {
        assertTrue("Incorrect length of procedure name", meta
                .getMaxProcedureNameLength() == 0);
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "not supported",
        method = "getMaxRowSize",
        args = {}
    )
    public void test_getMaxRowSize() throws SQLException {
        assertTrue("Incorrect size of row", meta.getMaxRowSize() == 0);
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "not supported",
        method = "getMaxSchemaNameLength",
        args = {}
    )
    public void test_getMaxSchemaNameLength() throws SQLException {
        assertTrue("Incorrect length of schema name", meta
                .getMaxSchemaNameLength() == 0);
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "not supported",
        method = "getMaxStatementLength",
        args = {}
    )
    public void test_getMaxStatementLength() throws SQLException {
        assertTrue("Incorrect length of statement", meta
                .getMaxStatementLength() == 0);
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "not supported",
        method = "getMaxStatements",
        args = {}
    )
    public void test_getMaxStatements() throws SQLException {
        assertTrue("Incorrect number of statements",
                meta.getMaxStatements() == 0);
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "not supported",
        method = "getMaxTableNameLength",
        args = {}
    )
    @KnownFailure("Exception test fails")
    public void test_getMaxTableNameLength() throws SQLException {
        assertTrue("Now supported", meta
                .getMaxTableNameLength() == 0);
        conn.close();
         try {
             meta.getMaxTableNameLength();
             fail("SQLException not thrown");
         } catch (SQLException e) {
         }
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "not supported",
        method = "getMaxTablesInSelect",
        args = {}
    )
    @KnownFailure("Exception test fails")
    public void test_getMaxTablesInSelect() throws SQLException {
        assertTrue("Tables in select is now supported: change test implementation\"",
                meta.getMaxTablesInSelect() == 0);
        conn.close();
         try {
             meta.getMaxTablesInSelect();
             fail("SQLException not thrown");
         } catch (SQLException e) {
         }
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "usernames not supported",
        method = "getMaxUserNameLength",
        args = {}
    )
    @KnownFailure("Exception test fails")
    public void test_getMaxUserNameLength() throws SQLException {
        assertTrue("Usernames are now supported: change test implementation",
                meta.getMaxUserNameLength() == 0);
        conn.close();
         try {
             meta.getMaxUserNameLength();
             fail("SQLException not thrown");
         } catch (SQLException e) {
         }
    }
}
