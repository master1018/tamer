@TestTargetClass(ObjectStreamConstants.class)
public class ObjectStreamConstantsTest extends TestCase {
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "Constant test, still many constants not tested",
        method = "!Constants",
        args = {}
    )
    public void test_Constants() {
        assertEquals(126, ObjectStreamConstants.TC_ENUM);
        assertEquals(16, ObjectStreamConstants.SC_ENUM);
        assertEquals(126, ObjectStreamConstants.TC_MAX);
    }
}
