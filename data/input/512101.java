@TestTargetClass(FaceDetector.class)
public class FaceDetectorTest extends InstrumentationTestCase {
    private FaceDetectorStub mActivity;
    @Override
    protected void setUp() throws Exception {
        super.setUp();
        Intent intent = new Intent();
        intent.setClass(getInstrumentation().getTargetContext(), FaceDetectorStub.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.putExtra(FaceDetectorStub.IMAGE_ID, R.drawable.faces);
        mActivity = (FaceDetectorStub) getInstrumentation().startActivitySync(intent);
    }
    @Override
    protected void tearDown() throws Exception {
        super.tearDown();
        mActivity.finish();
    }
    @TestTargets({
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "findFaces",
            args = {Bitmap.class, Face[].class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "FaceDetector",
            args = {int.class, int.class, int.class}
        )
    })
    public void testFindFaces() throws Exception {
        long waitMsec = 5000;
        Thread.sleep(waitMsec);
        assertTrue(mActivity.getDetectedFaces().size() == 5);
    }
}
