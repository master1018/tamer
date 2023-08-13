@TestTargetClass(Standard.class)
public class TabStopSpan_StandardTest extends TestCase {
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "Test constructor(s) of Standard.",
        method = "TabStopSpan.Standard",
        args = {int.class}
    )
    public void testConstructor() {
        new TabStopSpan.Standard(3);
        new TabStopSpan.Standard(-3);
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "Test getTabStop().",
        method = "getTabStop",
        args = {}
    )
    public void testGetTabStop() {
        Standard standard = new Standard(3);
        assertEquals(3, standard.getTabStop());
        standard = new Standard(-4);
        assertEquals(-4, standard.getTabStop());
    }
}
