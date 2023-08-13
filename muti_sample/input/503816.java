@TestTargetClass(AliasActivity.class)
public class AliasActivityTest extends AndroidTestCase {
    private static final long SLEEP_TIME = 1000;
    @TestTargets({
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "onCreate",
            args = {android.os.Bundle.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "AliasActivity",
            args = {}
        )
    })
    public void testAliasActivity() throws InterruptedException {
        new AliasActivity();
        Intent intent = new Intent();
        intent.setClass(getContext(), AliasActivityStub.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        assertFalse(ChildActivity.isStarted);
        assertFalse(AliasActivityStub.isOnCreateCalled);
        getContext().startActivity(intent);
        Thread.sleep(SLEEP_TIME);
        assertTrue(AliasActivityStub.isOnCreateCalled);
        assertTrue(ChildActivity.isStarted);
        assertTrue(AliasActivityStub.isFinished);
    }
}
