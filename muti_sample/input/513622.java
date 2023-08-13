@TestTargetClass(Destroyable.class) 
public class DestroyableTest extends TestCase {
    @TestTargets({
        @TestTargetNew(
            level = TestLevel.SUFFICIENT,
            notes = "",
            method = "destroy",
            args = {}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            notes = "",
            method = "isDestroyed",
            args = {}
        )
    })
    public void test_destroy() {
        myDestroyable md = new myDestroyable();
        try {
            assertFalse(md.isDestroyed());
            md.destroy();
            assertTrue(md.isDestroyed());
        } catch (Exception e) {
            fail("Unexpected exception " + e);
        }
    }
    private class myDestroyable implements Destroyable {
        boolean destroyDone = false;
        myDestroyable() {
        }
        public void destroy() throws DestroyFailedException {
            destroyDone = true;
        }
        public boolean isDestroyed() {
            return destroyDone;
        }
    }
}
