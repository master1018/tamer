@TestTargetClass(SQLData.class)
public class SQLDataTest extends TestCase {
    @TestTargetNew(
      level = TestLevel.NOT_FEASIBLE,
      notes = "",
          method = "getSQLTypeName",
          args = {}
        )
    public void testGetSQLTypeName() {
        fail("Not yet implemented");
    }
    @TestTargetNew(
      level = TestLevel.NOT_FEASIBLE,
      notes = "",
          method = "readSQL",
          args = {SQLInput.class, String.class}
        )
    public void testReadSQL() {
        fail("Not yet implemented");
    }
    @TestTargetNew(
      level = TestLevel.NOT_FEASIBLE,
      notes = "",
          method = "writeSQL",
          args = {SQLOutput.class}
        )
    public void testWriteSQL() {
        fail("Not yet implemented");
    }
}
