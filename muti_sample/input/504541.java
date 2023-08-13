@TestTargetClass(GuardedObject.class)
public class GuardedObjectTest extends TestCase {
    public static void main(String[] args) {
        junit.textui.TestRunner.run(GuardedObjectTest.class);
    }
    @TestTargets({
        @TestTargetNew(
            level = TestLevel.PARTIAL_COMPLETE,
            notes = "",
            method = "GuardedObject",
            args = {java.lang.Object.class, java.security.Guard.class}
        ),
        @TestTargetNew(
            level = TestLevel.PARTIAL_COMPLETE,
            notes = "",
            method = "getObject",
            args = {}
        )
    })
    public void testNoGuard() {
        Object obj = null;
        GuardedObject go = new GuardedObject(obj, null);
        assertNull(go.getObject());
        obj = "ewte rtw3456";
        go = new GuardedObject(obj, null);
        assertEquals(obj, go.getObject());
    }
    @TestTargets({
        @TestTargetNew(
            level = TestLevel.PARTIAL_COMPLETE,
            notes = "",
            method = "GuardedObject",
            args = {java.lang.Object.class, java.security.Guard.class}
        ),
        @TestTargetNew(
            level = TestLevel.PARTIAL_COMPLETE,
            notes = "",
            method = "getObject",
            args = {}
        )
    })
    public void testGuard() {
        final String message = "test message";
        final StringBuffer objBuffer = new StringBuffer("235345 t");
        GuardedObject go = new GuardedObject(objBuffer, new Guard() {
            public void checkGuard(Object object) throws SecurityException {
                if (object == objBuffer && objBuffer.length() == 0) {
                    throw new SecurityException(message);
                }
            }
        });
        assertEquals(objBuffer, go.getObject());
        objBuffer.setLength(0);
        try {
            go.getObject();
            fail("SecurityException is not thrown");
        } catch (Exception ok) {
            assertEquals(message, ok.getMessage());
        }
    }
}
