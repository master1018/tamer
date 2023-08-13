@TestTargetClass(RowSetEvent.class)
public class RowSetEventTest extends TestCase {
    @TestTargetNew(
        level = TestLevel.SUFFICIENT,
        notes = "functional test missing but not feasible: no implementation available.",
        method = "RowSetEvent",
        args = {javax.sql.RowSet.class}
    )    
    public void testConstructor() {
        try {
            new RowSetEvent(null);
            fail("illegal argument exception expected");
        } catch (IllegalArgumentException e) {
        }
        Impl_RowSet irs = new Impl_RowSet();
        RowSetEvent rse = new RowSetEvent(irs);
        assertSame(irs, rse.getSource());
    }
}
