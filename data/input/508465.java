@TestTargetClass(Statement.class)
public class StatementTest extends SQLTest {
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "addBatch",
        args = {java.lang.String.class}
    )
    public void testAddBatch() throws SQLException {
        Statement st = null;
        try {
            st = conn.createStatement();
            st.addBatch("INSERT INTO zoo VALUES (3,'Tuzik','dog')");
            st.addBatch("INSERT INTO zoo VALUES (4,'Mashka','cat')");
            int[] updateCounts = st.executeBatch();
            assertEquals(2, updateCounts.length);
            assertEquals(1, updateCounts[0]);
            assertEquals(1, updateCounts[1]);
        } catch (SQLException e) {
            fail("SQLException is thrown");
        } finally {
            try {
                st.close();
            } catch (SQLException ee) {
            }
        }
        try {
            st = conn.createStatement();
            st.addBatch("");
            st.executeBatch();
            fail("SQLException is not thrown");
        } catch (SQLException e) {
        } finally {
            try {
                st.close();
            } catch (SQLException ee) {
            }
        }
        try {
            st = conn.createStatement();
            st.addBatch(null);
            st.executeBatch();
        } catch (SQLException e) {
        } finally {
            try {
                st.close();
            } catch (SQLException ee) {
            }
        }
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "clearWarnings",
        args = {}
    )
    public void testClearWarnings() {
        Statement st = null;
        try {
            st = conn.createStatement();
            st.execute("select animals from zoo");
        } catch (SQLException e) {
        } finally {
            try {
                st.close();
            } catch (SQLException ee) {
            }
        }
        try {
            st = conn.createStatement();
            st.clearWarnings();
            SQLWarning w = st.getWarnings();
            assertNull(w);
        } catch (Exception e) {
            fail("Unexpected Exception: " + e.getMessage());
        } finally {
            try {
                st.close();
            } catch (SQLException ee) {
            }
        }
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "not supported. always returns null. ",
        method = "getWarnings",
        args = {}
    )
    public void testGetWarnings() {
        Statement st = null;
        int errorCode1 = -1;
        int errorCode2 = -1;
        try {
            st = conn.createStatement();
            st.execute("select animals from zoooo");
            fail("SQLException was not thrown");
        } catch (SQLException e) {
            errorCode1 = e.getErrorCode();
        }
        try {
            SQLWarning wrs = st.getWarnings();
            assertNull(wrs);
        } catch (Exception e) {
            fail("Change test implementation: get warnings is supported now");
        }
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "clearBatch",
        args = {}
    )
    public void testClearBatch() throws SQLException {
        Statement st = null;
        try {
            st = conn.createStatement();
            st.addBatch("INSERT INTO zoo VALUES (3,'Tuzik','dog'); ");
            st.addBatch("INSERT INTO zoo VALUES (4,'Mashka','cat')");
            st.clearBatch();
            int[] updateCounts = st.executeBatch();
            for (int i = 0; i < updateCounts.length; i++) {
                assertEquals(0, updateCounts[i]);
            }
        } catch (SQLException e) {
            fail("SQLException is thrown");
        } finally {
            try {
                st.close();
            } catch (SQLException ee) {
            }
        }
        try {
            st = conn.createStatement();
            st.addBatch("");
            st.executeBatch();
            fail("SQLException is not thrown");
        } catch (SQLException e) {
        } finally {
            try {
                st.close();
            } catch (SQLException ee) {
            }
        }
        try {
            st = conn.createStatement();
            st.addBatch(null);
            st.executeBatch();
        } catch (SQLException e) {
        } finally {
            try {
                st.close();
            } catch (SQLException ee) {
            }
        }
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "",
        method = "execute",
        args = {java.lang.String.class}
    )
    @KnownFailure("Return value wrong for queries below.")
    public void testExecute() throws SQLException {
        String[] queries = {
                "update zoo set name='Masha', family='cat' where id=2;",
                "drop table if exists hutch",
                "create table hutch (id integer not null, animal_id integer, address char(20), primary key (id));",
                "insert into hutch (id, animal_id, address) values (1, 2, 'Birds-house, 1');",
                "insert into hutch (id, animal_id, address) values (2, 1, 'Horse-house, 5');",
                "select animal_id, address from hutch where animal_id=1;",
                "create view address as select address from hutch where animal_id=2",
                "drop view address;", "drop table hutch;" };
        boolean[] results = {false, false, false, false, false, true, false,
                false, false};
        for (int i = 0; i < queries.length; i++) {
            Statement st = null;
            try {
                st = conn.createStatement();
                boolean res = st.execute(queries[i]);
                assertEquals("different result for statement no. "+i, results[i], res);
            } catch (SQLException e) {
                fail("SQLException is thrown: " + e.getMessage());
            } finally {
                try {
                    st.close();
                } catch (Exception ee) {
                }
            }
        }
        String[] inc_queries = {
                "update zoo_zoo set name='Masha', family='cat' where id=5;",
                "drop table hutchNO",
                "insert into hutch (id, animal_id, address) values (1, 2, 10);",
                "select animal_id, from hutch where animal_id=1;",
                "drop view address;", "drop table hutch;", "", null };
        for (int i = 0; i < inc_queries.length; i++) {
            Statement st = null;
            try {
                st = conn.createStatement();
                st.execute(inc_queries[i]);
                fail("SQLException is not thrown for query: " + inc_queries[i]);
            } catch (SQLException e) {
            } finally {
                try {
                    st.close();
                } catch (SQLException ee) {
                }
            } 
        }
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "Missing implementation for Statement.RETURN_GENERATED_KEYS: keys not yet supported",
        method = "execute",
        args = {java.lang.String.class, int.class}
    )
   public void testExecute_String_int() {
        String[] queries = {
                "update zoo set name='Masha', family='cat' where id=2;",
                "drop table if exists hutch",
                "create table hutch (id integer not null, animal_id integer, address char(20), primary key (id));",
                "insert into hutch (id, animal_id, address) values (1, 2, 'Birds-house, 1');",
                "insert into hutch (id, animal_id, address) values (2, 1, 'Horse-house, 5');",
                "select animal_id, address from hutch where animal_id=1;",
                "create view address as select address from hutch where animal_id=2",
                "drop view address;", "drop table hutch;" };
        for (int i = 0; i < queries.length; i++) {
            Statement st = null;
            try {
                st = conn.createStatement();
                st.execute(queries[i], Statement.NO_GENERATED_KEYS);
                ResultSet rs = st.getGeneratedKeys();
                assertFalse(rs.next());
            } catch (SQLException e) {
            } finally {
                try {
                    st.close();
                } catch (SQLException ee) {
                }
            } 
        }
        for (int i = 0; i < queries.length; i++) {
            Statement st = null;
            try {
                st = conn.createStatement();
                st.execute(queries[i], Statement.RETURN_GENERATED_KEYS);
                fail("Exception expected: Not supported");
            } catch (SQLException e) {
            } finally {
                try {
                    st.close();
                } catch (SQLException ee) {
                }
            } 
        }
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "SQLException test fails",
        method = "getConnection",
        args = {}
    )
    @KnownFailure("statment.close() does not wrap up")
    public void testGetConnection() {
        Statement st = null;
        try {
            st = conn.createStatement();
            assertSame(conn, st.getConnection());
        } catch (SQLException e) {
            fail("SQLException is thrown: " + e.getMessage());
        } finally {
            try {
                st.close();
            } catch (SQLException ee) {
            }
        }
        try {
            st.close();
            st.getConnection();
            fail("Exception expected");
        } catch (SQLException e) {
        }
    }
    @TestTargetNew(
        level = TestLevel.SUFFICIENT,
        notes = "SQLException test fails. Not all Fetch directions supported.",
        method = "getFetchDirection",
        args = {}
    )
    @KnownFailure("statment.close() does not wrap up")
    public void testGetFetchDirection() {
        Statement st = null;
        try {
            st = conn.createStatement();
            assertEquals(ResultSet.FETCH_UNKNOWN, st.getFetchDirection());
        } catch (SQLException e) {
            fail("SQLException is thrown " + e.getMessage());
        }  finally {
            try {
                st.close();
            } catch (SQLException ee) {
            }
        }
        try {
            st = conn.createStatement();
            st.setFetchDirection(ResultSet.FETCH_FORWARD);
            assertEquals(ResultSet.FETCH_FORWARD, st.getFetchDirection());
            fail("Exception expected: not supported");
        } catch (SQLException e) {
        }  finally {
            try {
                st.close();
            } catch (SQLException ee) {
            }
        }
        try {
            st.getFetchDirection();
            fail("Exception expected");
        } catch (SQLException e) {
        }
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "not supported. ",
        method = "setFetchDirection",
        args = {int.class}
    )
    public void testSetFetchDirection() {
        Statement st = null;
        try {
            st = conn.createStatement();
            st.setFetchDirection(ResultSet.FETCH_FORWARD);
            st.executeQuery("select * from zoo;");
            fail("Revise test implemenation for feature impl. has changed");
        } catch (SQLException e) {
        } finally {
            try {
                st.close();
            } catch (SQLException ee) {
            }
        }
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "SQLException test fails",
        method = "getFetchSize",
        args = {}
    )
    @KnownFailure("statment.close() does not wrap up")
    public void testGetFetchSize() {
        Statement st = null;
        try {
            st = conn.createStatement();
            st.execute("select * from zoo;");
            assertEquals(1, st.getFetchSize());
        } catch (SQLException e) {
            fail("SQLException is thrown");
        } finally {
            try {
                st.close();
            } catch (SQLException ee) {
            }
        }
        try {
            st.close();
            st.getFetchSize();
            fail("Exception expected");
        } catch (SQLException e) {
        }
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "not supported.",
        method = "setFetchSize",
        args = {int.class}
    )
    public void testSetFetchSize() {
        Statement st = null;
        try {
            st = conn.createStatement();
            int rows = 100;
            for (int i = 0; i < rows; i++) {
                try {
                    st.setFetchSize(i);
                    assertEquals(i, st.getFetchSize());
                    fail("Revise: test implemenation for feature impl. has changed");
                } catch (SQLException sqle) {
                    assertEquals("not supported", sqle.getMessage());
                }
            }
        } catch (SQLException e) {
            fail("SQLException is thrown");
        } finally {
            try {
                st.close();
            } catch (SQLException ee) {
            }
        }
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "not supported",
        method = "setMaxFieldSize",
        args = {int.class}
    )
    public void testSetMaxFieldSize() {
        Statement st = null;
        try {
            st = conn.createStatement();
            for (int i = 0; i < 300; i += 50) {
                try {
                    st.setMaxFieldSize(i);
                    assertEquals(i, st.getMaxFieldSize());
                    fail("Revise test implemenation for feature impl. has changed");
                } catch (SQLException sqle) {
                    assertEquals("not supported", sqle.getMessage());
                }
            }
        } catch (SQLException e) {
            fail("Can't create statement, SQLException is thrown: "
                    + e.getMessage());
        } finally {
            try {
                st.close();
            } catch (SQLException ee) {
            }
        }
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "not supported",
        method = "getMaxFieldSize",
        args = {}
    )
    public void testGetMaxFieldSize() {
        Statement st = null;
        try {
            st = conn.createStatement();
            for (int i = 200; i < 500; i += 50) {
                try {
                    st.setMaxFieldSize(i);
                    fail("Revise test implemenation for feature impl. has changed");
                } catch (SQLException sqle) {
                    assertEquals("not supported", sqle.getMessage());
                }
            }
        } catch (SQLException e) {
            fail("Can't create statement, SQLException is thrown: "
                    + e.getMessage());
        } finally {
            try {
                st.close();
            } catch (SQLException ee) {
            }
        }
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "not supported",
        method = "setMaxRows",
        args = {int.class}
    )
    public void testSetMaxRows() {
        Statement st = null;
        try {
            st = conn.createStatement();
            st.execute("select * from zoo;");
            for (int i = 0; i < 300; i += 50) {
                try {
                    st.setMaxRows(i);
                    assertEquals(i, st.getMaxRows());
                    fail("Revise test implemenation for feature impl. has changed");
                } catch (SQLException sqle) {
                    assertEquals("not supported", sqle.getMessage());
                }
            }
            try {
                st.setMaxRows(-1);
                fail("SQLException isn't thrown");
            } catch (SQLException sqle) {
            }
        } catch (SQLException e) {
            fail("Can't create statement, SQLException is thrown: "
                    + e.getMessage());
        } finally {
            try {
                st.close();
            } catch (SQLException ee) {
            }
        }
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "not supported",
        method = "getMaxRows",
        args = {}
    )
    public void testGetMaxRows() {
        Statement st = null;
        try {
            st = conn.createStatement();
            for (int i = 200; i < 500; i += 50) {
                try {
                    st.setMaxRows(i);
                    assertEquals(i, st.getMaxRows());
                    fail("Revise test implemenation for feature impl. has changed");
                } catch (SQLException sqle) {
                    assertEquals("not supported", sqle.getMessage());
                }
            }
        } catch (SQLException e) {
            fail("Can't create statement, SQLException is thrown: "
                    + e.getMessage());
        } finally {
            try {
                st.close();
            } catch (SQLException ee) {
            }
        }
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "close",
        args = {}
    )
    @KnownFailure("statment.close() does not wrap up")
    public void testClose() {
        Statement st = null;
        ResultSet res = null;
        try {
            String[] queries = {
                    "update zoo set name='Masha', family='cat' where id=2;",
                    "insert into zoo (id, name, family) values (3, 'Vorobey', 'sparrow');",
                    "insert into zoo (id, name, family) values (4, 'Slon', 'elephant');",
                    "select * from zoo"};
            st = conn.createStatement();
            for (int i = 0; i < queries.length; i++) {
                st.execute(queries[i]);
            }
            res = st.getResultSet();
            assertNotNull(res);
            assertTrue(res.next());
            st.close();
        } catch (SQLException e) {
            fail("SQLException is thrown: " + e.getMessage());
        } finally {
            try {
                st.close();
            } catch (SQLException ee) {
            }
        }
        try {
            res.next();
            fail("Exception expected");
        } catch (SQLException e) {
        }
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "not supported",
        method = "execute",
        args = {java.lang.String.class, int[].class}
    )
    public void testExecute_String_intArray() {
        Statement st = null;
        try {
            String[] queries = {
                    "update zoo set name='Masha', family='cat' where id=2;",
                    "insert zoo(id, name, family) values (3, 'Vorobey', 'sparrow');",
                    "insert zoo(id, name, family) values (4, 'Slon', 'elephant');",
                    "select * from zoo" };
            Vector<int[]> array = new Vector<int[]>();
            array.addElement(null);
            array.addElement(new int[] { 1, 2, 3 });
            array.addElement(new int[] { 1, 2, 10, 100 });
            array.addElement(new int[] {});
            st = conn.createStatement();
            for (int i = 0; i < queries.length; i++) {
                st.execute(queries[i], (int[]) array.elementAt(i));
                fail("SQLException expected: not supported");
            }
        } catch (SQLException e) {
        } finally {
            try {
                st.close();
            } catch (SQLException ee) {
            }
        }
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "not supported",
        method = "execute",
        args = {java.lang.String.class, java.lang.String[].class}
    )
    public void testExecute_String_StringArray() {
        Statement st = null;
        try {
            String[] queries = {
                    "update zoo set name='Masha', family='cat' where id=2;",
                    "insert zoo(id, name, family) values (3, 'Vorobey', 'sparrow');",
                    "insert zoo(id, name, family) values (4, 'Slon', 'elephant');",
                    "select * from zoo" };
            Vector<String[]> array = new Vector<String[]>();
            array.addElement(null);
            array.addElement(new String[] { "", "", "", "", "", "", "", "" });
            array.addElement(new String[] { "field 1", "", "field2" });
            array.addElement(new String[] { "id", "family", "name" });
            st = conn.createStatement();
            for (int i = 0; i < queries.length; i++) {
                st.execute(queries[i], (String[]) array.elementAt(i));
                fail("Exception expected: not supported");
            }
            fail("Revise test implemenation for feature impl. has changed");
            assertNotNull(st.getResultSet());
            st.close();
            assertNull(st.getResultSet());
        } catch (SQLException e) {
            try {
                st.close();
            } catch (SQLException ee) {
            }
        } 
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "Test fails: dropping table hutch affects at least 2 rows.executeBatch() always returns same result: 1.",
        method = "executeBatch",
        args = {}
    )
    @KnownFailure("always returns 1 for no. of updates")
    public void testExecuteBatch() {
        String[] queries = {
                "update zoo set name='Masha', family='cat' where id=2;",
                "drop table if exists hutch",
                "create table hutch (id integer not null, animal_id integer, address char(20), primary key (id));",
                "insert into hutch (id, animal_id, address) values (1, 2, 'Birds-house, 1');",
                "insert into hutch (id, animal_id, address) values (2, 1, 'Horse-house, 5');",
                "create view address as select address from hutch where animal_id=2",
                "drop view address;", "drop table hutch;" };
        String[] wrongQueries = {
                "update zoo set name='Masha', family='cat' where;",
                "drop table if exists hutch;",
                "create view address as select address from hutch where animal_id=2;",
                "drop view address;", "drop table hutch;" };
        int[] result = { 1, 1, 1, 1, 1, 1, 1, 1 };
        Statement st = null;
        try {
            st = conn.createStatement();
            assertEquals(0, st.executeBatch().length);
            for (int i = 0; i < wrongQueries.length; i++) {
                st.addBatch(wrongQueries[i]);
            }
            st.executeBatch();
            fail("BatchupdateException expected");
        } catch (BatchUpdateException e) {
        } catch (SQLException e) {
            fail("BatchupdateException expected");
        } finally {
            try {
                st.close();
            } catch (SQLException ee) {
            }
        } 
        try {
            st = conn.createStatement();
            assertEquals(0, st.executeBatch().length);
            for (int i = 0; i < queries.length; i++) {
                st.addBatch(queries[i]);
            }
            int[] resArray = st.executeBatch();
            assertTrue(java.util.Arrays.equals(result, resArray));
        } catch (SQLException e) {
            fail("SQLException is thrown: " + e.getMessage());
        } finally {
            try {
                st.close();
            } catch (SQLException ee) {
            }
        }
        try {
            st = conn.createStatement();
            st.addBatch("select * from zoo");
            st.executeBatch();
            fail("Exception expected");
        } catch (BatchUpdateException bue) {
        } catch (SQLException sqle) {
            fail("Unknown SQLException is thrown: " + sqle.getMessage());
        } finally {
            try {
                st.close();
            } catch (SQLException ee) {
            }
        }
        try {
            st.close();
            st.executeBatch();
            fail("SQLException not thrown");
        } catch (SQLException e) {
        }
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "Not according to spec.",
        method = "executeQuery",
        args = {java.lang.String.class}
    )
    @KnownFailure("Does throw an exception on non select statment.")
    public void testExecuteQuery_String() {
        String[] queries1 = { "select * from zoo",
                "select name, family from zoo where id = 1" };
        String[] queries2 = {
                "update zoo set name='Masha', family='cat' where id=2;",
                "drop table if exists hutch",
                "create table hutch (id integer not null, animal_id integer, address char(20), primary key (id));",
                "insert into hutch (id, animal_id, address) values (1, 2, 'Birds-house, 1');",
                "insert into hutch (id, animal_id, address) values (2, 1, 'Horse-house, 5');",
                "create view address as select address from hutch where animal_id=2",
                "drop view address;", "drop table hutch;", "select from zoo" };
        Statement st = null;
        try {
            st = conn.createStatement();
            for (int i = 0; i < queries1.length; i++) {
                try {
                    ResultSet rs = st.executeQuery(queries1[i]);
                    assertNotNull(rs);
                } catch (SQLException sqle) {
                    fail("SQLException is thrown for query: " + queries1[i]);
                }
            }
        } catch (SQLException e) {
            fail("SQLException is thrown: " + e.getMessage());
        } finally {
            try {
                st.close();
            } catch (Exception ee) {
            }
        }
        try {
            st = conn.createStatement();
            for (int i = 0; i < queries2.length; i++) {
                try {
                    ResultSet rs = st.executeQuery(queries2[i]);
                    assertNotNull(rs);
                    fail("SQLException is not thrown for query: " + queries2[i]);
                } catch (SQLException sqle) {
                }
            }
        } catch (SQLException sqle) {
            fail("Unknown SQLException is thrown: " + sqle.getMessage());
        } finally {
            try {
                st.close();
            } catch (Exception ee) {
            }
        } 
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "impl not according to spec.",
        method = "executeUpdate",
        args = {java.lang.String.class}
    )
    @KnownFailure("Spec is not precise enough: should be: number of rows affected."+
            " eg. to be consistent for deletes: 'delete from s1;' should be different from "+
            "'delete from s1 where c1 = 1;' ")
    public void testExecuteUpdate_String() throws SQLException {
        String[] queries1 = {
                "update zoo set name='Masha', family='cat' where id=2;",
                "drop table if exists hutch",
                "create table hutch (id integer not null, animal_id integer, address char(20), primary key (id));",
                "insert into hutch (id, animal_id, address) values (1, 2, 'Birds-house, 1');",
                "insert into hutch (id, animal_id, address) values (2, 1, 'Horse-house, 5');",
                "create view address as select address from hutch where animal_id=2;",
                "drop view address;", "drop table hutch;"};
        String queries2 = "select * from zoo;";
        Statement st = null;
        try {
            st = conn.createStatement();
            for (int i = 0; i < queries1.length; i++) {
                try {
                    int count = st.executeUpdate(queries1[i]);
                    assertTrue(count > 0);
                } catch (SQLException e) {
                    fail("SQLException is thrown: " + e.getMessage());
                }
            }
            try {
               assertEquals(0, st.executeUpdate(queries2));
            } catch (SQLException e) {
               fail("SQLException is thrown: " + e.getMessage());
            }
        } catch (SQLException e) {
            fail("SQLException is thrown: " + e.getMessage());
        } finally {
            try {
                st.close();
            } catch (Exception ee) {
            }
        }
        Statement stat = conn.createStatement();
        assertEquals(0 ,stat.executeUpdate("create table s1 (c1);"));
        assertEquals(1, stat.executeUpdate("insert into s1 values (0);"));
        assertEquals(1, stat.executeUpdate("insert into s1 values (1);"));
        assertEquals(1, stat.executeUpdate("insert into s1 values (2);"));
        assertEquals(1,stat.executeUpdate("delete from s1 where c1 = 1;"));
        assertEquals(2, stat.executeUpdate("update s1 set c1 = 5;"));
        assertEquals(2,stat.executeUpdate("delete from s1;"));
        assertEquals(0, stat.executeUpdate("drop table s1;"));
        stat.executeUpdate("create table s1 (c1);"); 
        stat.executeUpdate("insert into s1 values (0);");
        stat.executeUpdate("insert into s1 values (1);");
        stat.executeUpdate("insert into s1 values (2);");
        assertEquals(3, stat.executeUpdate("drop table s1;"));
        stat.close();
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "not supported",
        method = "executeUpdate",
        args = {java.lang.String.class, int[].class}
    )
    public void testExecuteUpdate_String_intArray() {
        Statement st = null;
        try {
            String[] queries1 = {
                    "update zoo set name='Masha', family='cat' where id=2;",
                    "drop table if exists hutch",
                    "create table hutch (id integer not null, animal_id integer, address char(20), primary key (id));",
                    "insert into hutch (id, animal_id, address) values (1, 2, 'Birds-house, 1');",
                    "insert into hutch (id, animal_id, address) values (2, 1, 'Horse-house, 5');",
                    "create view address as select address from hutch where animal_id=2",
                    "drop view address;", "drop table hutch;" };
            Vector<int[]> array = new Vector<int[]>();
            array.addElement(null);
            array.addElement(new int[] { 1, 2, 3 });
            array.addElement(new int[] { 1, 2, 10, 100 });
            array.addElement(new int[] {});
            array.addElement(new int[] { 100, 200 });
            array.addElement(new int[] { -1, 0 });
            array.addElement(new int[] { 0, 0, 0, 1, 2, 3 });
            array.addElement(new int[] { -100, -200 });
            st = conn.createStatement();
            for (int i = 0; i < queries1.length; i++) {
                st.executeUpdate(queries1[i], (int[]) array.elementAt(i));
                fail("Exception expected");
            }
        } catch (SQLException e) {
            assertEquals("not supported",e.getMessage());
        } finally {
            try {
                st.close();
            } catch (SQLException ee) {
            }
        } 
    }
    @TestTargetNew(
        level = TestLevel.SUFFICIENT,
        notes = "not supported",
        method = "executeUpdate",
        args = {java.lang.String.class, int.class}
    )
    public void testExecuteUpdate_String_int() {
        String[] queries = {
                "update zoo set name='Masha', family='cat' where id=2;",
                "drop table if exists hutch",
                "create table hutch (id integer not null, animal_id integer, address char(20), primary key (id));",
                "insert into hutch (id, animal_id, address) values (1, 2, 'Birds-house, 1');",
                "insert into hutch (id, animal_id, address) values (2, 1, 'Horse-house, 5');",
                "select animal_id, address from hutch where animal_id=1;",
                "create view address as select address from hutch where animal_id=2",
                "drop view address;", "drop table hutch;" };
            Statement st = null;
            ResultSet rs = null;
            try {
                st = conn.createStatement();
                st.executeUpdate(queries[1], Statement.NO_GENERATED_KEYS);
                rs = st.getGeneratedKeys();
                assertFalse(rs.next());
                fail("Exception expected: not supported");
            } catch (SQLException e) {
            } finally {
                try {
                    rs.close();
                    st.close();
                } catch (Exception ee) {
                }
            }
            try {
                st = conn.createStatement();
                st.executeUpdate(queries[1], Statement.RETURN_GENERATED_KEYS);
                rs = st.getGeneratedKeys();
                assertTrue(rs.next());
                fail("Exception expected: not supported");
            } catch (SQLException e) {
            } finally {
                try {
                    rs.close();
                    st.close();
                } catch (Exception ee) {
                }
            }
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "not supported",
        method = "executeUpdate",
        args = {java.lang.String.class, java.lang.String[].class}
    )
    public void testExecuteUpdate_String_StringArray() {
        Statement st = null;
        try {
            String[] queries = {
                    "update zoo set name='Masha', family='cat' where id=2;",
                    "drop table if exists hutch",
                    "create table hutch (id integer not null, animal_id integer, address char(20), primary key (id));",
                    "insert into hutch (id, animal_id, address) values (1, 2, 'Birds-house, 1');",
                    "insert into hutch (id, animal_id, address) values (2, 1, 'Horse-house, 5');",
                    "create view address as select address from hutch where animal_id=2",
                    "drop view address;", "drop table hutch;" };
            Vector<String[]> array = new Vector<String[]>();
            array.addElement(null);
            array.addElement(new String[] { "", "", "", "", "", "", "", "" });
            array.addElement(new String[] { "field 1", "", "field2" });
            array.addElement(new String[] { "id", "family", "name" });
            array
                    .addElement(new String[] { "id", null, "family", null,
                            "name" });
            array.addElement(new String[] { "id", " ", "name" });
            array.addElement(new String[] { null, null, null, null });
            array.addElement(new String[] { " ", "123 21", "~!@#$%^&*()_+ ",
                    null });
            st = conn.createStatement();
            for (int i = 0; i < queries.length; i++) {
                st.executeUpdate(queries[i], (String[]) array.elementAt(i));
                fail("Revise test implemenation for feature impl. has changed");
            }
        } catch (SQLException e) {
            assertEquals("not supported", e.getMessage());
        } finally {
            try {
                st.close();
            } catch (SQLException ee) {
            }
        } 
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "SQLException test fails",
        method = "getUpdateCount",
        args = {}
    )
    @KnownFailure("statment.close() does not wrap up")
    public void testGetUpdateCount() {
        Statement st = null;
        try {
            String query = "update zoo set name='Masha', family='cat' where id=2;";
            st = conn.createStatement();
            st.executeUpdate(query);
            assertEquals(1, st.getUpdateCount());
            query = "update zoo set name='Masha', family='cat' where id=5;";
            st.executeUpdate(query);
            assertEquals(0, st.getUpdateCount());
        } catch (SQLException e) {
            fail("SQLException is thrown: " + e.getMessage());
        } finally {
            try {
                st.close();
            } catch (SQLException ee) {
            }
        } 
        try {
            st.getUpdateCount();
            fail("Exception expected");
        } catch (SQLException e) {
        }
    }
    @TestTargetNew(
        level = TestLevel.SUFFICIENT,
        notes = "not supported",
        method = "getGeneratedKeys",
        args = {}
    )
    public void testGeneratedKeys() {
        Statement st = null;
        try {
            String insert = "insert into zoo (id, name, family) values (8, 'Tuzik', 'dog');";
            st = conn.createStatement();
            assertNull(st.getGeneratedKeys());
            fail("Fail: statement does not fail");
        } catch (SQLException e) {
          assertEquals("not supported", e.getMessage());
        }
    }
    @TestTargetNew(
        level = TestLevel.SUFFICIENT,
        notes = "not supported",
        method = "setCursorName",
        args = {java.lang.String.class}
    )
    public void testSetCursorName() {
        Statement st = null;
        try {
            String select = "select * from zoo";
            st = conn.createStatement();
            st.setCursorName("test");
            fail("Fail: statement does not fail");
        } catch (SQLException e) {
          assertEquals("not supported", e.getMessage());
        }
    }
    @TestTargetNew(
        level = TestLevel.SUFFICIENT,
        notes = "not supported",
        method = "setEscapeProcessing",
        args = {boolean.class}
    )
    public void testSetEscapeProcessing() {
        Statement st = null;
        try {
            String select = "select * from zoo";
            st = conn.createStatement();
            st.setEscapeProcessing(true);
            fail("Fail: statement does not fail");
        } catch (SQLException e) {
          assertEquals("not supported", e.getMessage());
        }
    }
    @TestTargets({
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "Error in impl. default query timeout for sqlite dbs is 0.",
        method = "setQueryTimeout",
        args = {int.class}
    ),
    @TestTargetNew(
            level = TestLevel.PARTIAL_COMPLETE,
            notes = "Error in impl. default query timeout for sqlite dbs is 0.",
            method = "getQueryTimeout",
            args = {}
        )
    })
    @KnownFailure("Error in implementation either setter or getter fails. "+
            "Getter spec is not explicit about unit.")
    public void testSetQueryTimeout() {
        Statement st = null;
        try {
            st = conn.createStatement();
            st.setQueryTimeout(2000);
            assertEquals(2000, st.getQueryTimeout());
        } catch (SQLException e) {
            fail("SQLException is thrown: " + e.getMessage());
        }
        st = null;
        try {
            st = conn.createStatement();
            st.setQueryTimeout(-1);
           fail("SQLException is not thrown");
        } catch (SQLException e) {
        }
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "Tests fail. not fully supported: returns only ResultSet.TYPE_SCROLL_INSENSITIVE. Either should throw an unsupported exception or behave according to spec.",
        method = "getResultSetType",
        args = {}
    )
    @KnownFailure("not fully supported")
    public void testGetResultSetType() {
        Statement st = null;
        try {
            st = conn.createStatement();
            st.getResultSetType();
            assertEquals(ResultSet.TYPE_SCROLL_INSENSITIVE, st
                    .getResultSetType());
        } catch (SQLException e) {
            assertEquals("not supported", e.getMessage());
        }
        try {
            st = conn.createStatement(ResultSet.TYPE_SCROLL_SENSITIVE,
                    ResultSet.CONCUR_UPDATABLE);
            st.getResultSetType();
            assertEquals(ResultSet.TYPE_SCROLL_SENSITIVE, st.getResultSetType());
        } catch (SQLException e) {
            assertEquals("not supported", e.getMessage());
        }
        try {
            st = conn.createStatement(ResultSet.TYPE_SCROLL_SENSITIVE,
                    ResultSet.CONCUR_UPDATABLE);
            st.getResultSetType();
            assertEquals(ResultSet.TYPE_SCROLL_SENSITIVE, st.getResultSetType());
        } catch (SQLException e) {
            assertEquals("not supported", e.getMessage());
        }
        try {
            st = conn.createStatement(ResultSet.TYPE_FORWARD_ONLY,
                    ResultSet.CONCUR_UPDATABLE);
            st.getResultSetType();
            assertEquals(ResultSet.TYPE_FORWARD_ONLY, st.getResultSetType());
        } catch (SQLException e) {
            assertEquals("not supported", e.getMessage());
        }
    }
    @TestTargetNew(
        level = TestLevel.SUFFICIENT,
        notes = "not supported",
        method = "getResultSetHoldability",
        args = {}
    )
    @KnownFailure("Test for default value fails")
    public void testGetResultSetHoldability() {
        Statement st = null;
        try {
            st = conn.createStatement();
            assertEquals(ResultSet.CLOSE_CURSORS_AT_COMMIT, st
                    .getResultSetHoldability());
        } catch (SQLException e) {
            assertEquals("not supported", e.getMessage());
        }
        try {
            st = conn.createStatement(ResultSet.TYPE_SCROLL_SENSITIVE,
                    ResultSet.CONCUR_READ_ONLY,
                    ResultSet.HOLD_CURSORS_OVER_COMMIT);
            fail("Exception expected: not supported");
        } catch (SQLException e) {
        }
        try {
            st = conn.createStatement(ResultSet.TYPE_SCROLL_SENSITIVE,
                    ResultSet.CONCUR_READ_ONLY,
                    ResultSet.CLOSE_CURSORS_AT_COMMIT);
            fail("Exception expected: not supported");
        } catch (SQLException e) {
        }
    }
    @TestTargetNew(
        level = TestLevel.SUFFICIENT,
        notes = "Tests fail. returns only ResultSet.TYPE_SCROLL_INSENSITIVE. Either should throw an unsupported exception or behave according to spec.",
        method = "getResultSetConcurrency",
        args = {}
    )
    @KnownFailure("Not supported")
    public void testGetResultSetConcurrency() {
        Statement st = null;
        try {
            st = conn.createStatement();
            st.getResultSetConcurrency();
            assertEquals(ResultSet.CONCUR_READ_ONLY, st
                    .getResultSetConcurrency());
        } catch (SQLException e) {
            assertEquals("not supported", e.getMessage());
        }
        try {
            st = conn.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE,
                    ResultSet.CONCUR_UPDATABLE);
            st.getResultSetConcurrency();
            assertEquals(ResultSet.CONCUR_UPDATABLE, st.getResultSetConcurrency());
            fail("Exception expected: not supported");
        } catch (SQLException e) {
        }
        try {
            st = conn.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE,
                    ResultSet.CONCUR_READ_ONLY);
            st.getResultSetConcurrency();
            assertEquals(ResultSet.CONCUR_READ_ONLY, st.getResultSetConcurrency());
            fail("Exception expected: not supported");
        } catch (SQLException e) {
        }
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "Error in implementation. Is not according to spec:if updateCount > 0 resultset must be null.",
        method = "getResultSet",
        args = {}
    )
    @KnownFailure("Does not return null on update count > 0 (not a select statement) ")
    public void testGetResultSet() {
        Statement st = null;
        ResultSet res = null;
        try {
            st = conn.createStatement(ResultSet.TYPE_FORWARD_ONLY,
                    ResultSet.CONCUR_READ_ONLY,
                    ResultSet.CLOSE_CURSORS_AT_COMMIT);
            st.execute("create table test (c1);");
            res = st.getResultSet();
            assertNull(res);
        } catch (SQLException e) {
            fail("Unexpected Exception "+e);
        }
        try {
            st = conn.createStatement();
            String select = "select * from zoo where id == 4;";
            String insert =  "insert into zoo (id, name, family) values (4, 'Vorobuy', 'bear');";
            st.execute(insert);
            st.execute(select);
            assertEquals(-1, st.getUpdateCount());
            res = st.getResultSet();
            assertNotNull(res);
            res.next();
            assertEquals(4,res.getInt(1));
            assertEquals("Vorobuy",res.getString(2));
            assertEquals("bear",res.getString(3));
            assertFalse(res.next());
        } catch (SQLException e) {
            fail("SQLException is thrown:"+e.getMessage());
        }
        try {
            st = conn.createStatement();
            String insert = "insert into zoo (id, name, family) values (3, 'Vorobey', 'sparrow');";
            st
            .execute(insert);
            res = st.getResultSet();
            if (st.getUpdateCount() > 0)  {
                assertNull(res);
            }
        } catch (SQLException e) {
            fail("SQLException is thrown:"+e.getMessage());
        }
        try {
            st.close();
            st.getResultSet();
            fail("Exception expected");
        } catch (SQLException e) {
        }
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "Errors in impl.An other value is returned than was set (X * 1000)Default query timeout for sqlite dbs is 0.",
        method = "getQueryTimeout",
        args = {}
    )
    @KnownFailure("An other value is returned than was set (X * 1000)")
    public void testGetQueryTimeout() {
        Statement st = null;
        try {
            st = conn.createStatement();
            st.setQueryTimeout(2000);
            assertEquals(2000, st.getQueryTimeout());
        } catch (SQLException e) {
            fail("SQLException is thrown: " + e.getMessage());
        }
        try {
            st = conn.createStatement();
            assertEquals(0,st.getQueryTimeout());
        } catch (SQLException e) {
            fail("SQLException is thrown: " + e.getMessage());
        }
        try {
            st.close();
            st.getQueryTimeout();
            fail("Exception expected");
        } catch (SQLException e) {
        }
    }
    @TestTargetNew(
        level = TestLevel.SUFFICIENT,
        notes = "not fully supported",
        method = "getMoreResults",
        args = {}
    )
    @KnownFailure("not supported")
    public void testGetMoreResults() {
        Statement st = null;
        ResultSet res1 = null;
        ResultSet res2 = null;
        String[] queries = {
                "insert into zoo values (3,'John','bird');",
                "update zoo set name='Masha', family='cat' where id=3;",
                "update zoo set name='Masha', family='bear' where id=3;"};
       try {
            st = conn.createStatement();
            st.execute(queries[0]);
            assertFalse(st.getMoreResults());
            try {
                st.getResultSet();
                fail("Exception expected");
            } catch (SQLException e) {
            }
        } catch (SQLException e) {
            fail("SQLException is thrown: " + e.getMessage());
        } finally {
            try {
                st.close();
            } catch (SQLException ee) {
            }
        }
        try {
            st.getMoreResults();
            fail("Exception expected");
        } catch (SQLException e) {
        }
    }
    @TestTargetNew(
        level = TestLevel.NOT_FEASIBLE,
        notes = "Callable Statements are not supported",
        method = "getMoreResults",
        args = {int.class}
    )
    public void testGetMoreResultsInt() {
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "Test fails. See also SQLite.DatabaseTest test of interrupt().",
        method = "cancel",
        args = {}
    )
    @KnownFailure("Bug in implementation of cancel: Does not fulfill spec.")
    public void testCancel() {
        Statement st = null;
        try {
            st = conn.prepareStatement("insert into zoo values (7,'Speedy Gonzales','Mouse');");
            CancelThread c = new CancelThread(st);
            InsertThread ins = new InsertThread((PreparedStatement)st);
            try {
                ins.t.join();
                c.t.join();
            } catch (InterruptedException e) {
                fail("Error in test setup: ");
            } catch (Exception e){
            }
            ResultSet res = st.executeQuery("select * from zoo where id=7");
            assertFalse(res.next());
        } catch (SQLException e) {
            fail("SQLException is thrown: " + e.getMessage());
        }
        try {
            st.close();
            st.cancel();
            fail("Exception expected");
        } catch (SQLException e) {
        }
    }
    class CancelThread implements Runnable{
        Thread t;
        Statement stmt;
        CancelThread (Statement aSt) {
           this.stmt = aSt;
           t = new Thread(this,"Cancel thread");
           t.start();
        }
        public void run() {
           Logger.global.info("*Cancel* thread started");
           try {
             Thread.sleep(1500);
         } catch (InterruptedException e1) {
             fail("Error in test setup");
             e1.printStackTrace();
         }             
           try {
               Logger.global.info("*Cancel* thread, about to do stmt.cancel()");  
               stmt.cancel();
               Logger.global.info("*Cancel* thread, stmt.cancel() done");  
           } catch (SQLException e) {
               fail("Error in test setup");
               e.printStackTrace();
           }
           Logger.global.info("*Cancel* thread terminated");
        }
     }
    class InsertThread implements Runnable{
        Thread t;
        PreparedStatement stmt;
        InsertThread (PreparedStatement aSt) {
           this.stmt = aSt;
           t = new Thread(this,"Insert thread");
           t.start();
        }
        public void run() {
          Logger.global.info("*Insert* thread started");
           try {
             Thread.sleep(1500);
         } catch (InterruptedException e1) {
             fail("Error in test setup");
             e1.printStackTrace();
         }             
           try {
               Logger.global.info("*Insert* thread, about to do insertion");  
               stmt.execute();
               stmt.execute();
               Logger.global.info("*Insert* thread inserted");  
           } catch (SQLException e) {
               fail("Error in test setup");
               e.printStackTrace();
           }
          Logger.global.info("*Insert* thread terminated");
        }
     }
}
