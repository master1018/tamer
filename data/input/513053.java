@TestTargetClass(DebugUtils.class)
public class DebugUtilsTest extends TestCase {
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        method = "isObjectSelected",
        args = {Object.class}
    )
    public void testIsObjectSelected(){
        assertFalse(DebugUtils.isObjectSelected(new Object()));
    }
}
