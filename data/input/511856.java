@TestTargetClass(Instrumentation.ActivityResult.class)
public class Instrumentation_ActivityResultTest extends AndroidTestCase {
    @TestTargets({
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "Instrumentation.ActivityResult",
            args = {int.class, Intent.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "getResultCode",
            args = {}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "getResultData",
            args = {}
        )
    })
    public void testActivityResultOp() {
        Intent intent = new Intent();
        ActivityResult result = new ActivityResult(Activity.RESULT_OK, intent);
        assertEquals(Activity.RESULT_OK, result.getResultCode());
        assertEquals(intent, result.getResultData());
        result = new ActivityResult(Activity.RESULT_CANCELED, intent);
        assertEquals(Activity.RESULT_CANCELED, result.getResultCode());
    }
}
