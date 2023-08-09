@TestTargetClass(RowSetListener.class)
public class RowSetListenerTest extends TestCase {
    @TestTargetNew(
        level = TestLevel.NOT_FEASIBLE,
        notes = "",
        method = "cursorMoved",
        args = {javax.sql.RowSetEvent.class}
    )
    public void testCursorMoved() {
        fail("Not yet implemented");
    }
    @TestTargetNew(
        level = TestLevel.NOT_FEASIBLE,
        notes = "",
        method = "rowChanged",
        args = {javax.sql.RowSetEvent.class}
    )
    public void testRowChanged() {
        fail("Not yet implemented");
    }
    @TestTargetNew(
        level = TestLevel.NOT_FEASIBLE,
        notes = "",
        method = "rowSetChanged",
        args = {javax.sql.RowSetEvent.class}
    )
    public void testRowSetChanged() {
        fail("Not yet implemented");
    }
}
