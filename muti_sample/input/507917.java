@TestTargetClass(Face.class)
public class FaceDetector_FaceTest extends InstrumentationTestCase {
    private FaceDetectorStub mActivity;
    @Override
    protected void setUp() throws Exception {
        super.setUp();
        Intent intent = new Intent();
        intent.setClass(getInstrumentation().getTargetContext(), FaceDetectorStub.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.putExtra(FaceDetectorStub.IMAGE_ID, R.drawable.single_face);
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
            method = "eyesDistance",
            args = {}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "confidence",
            args = {}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "getMidPoint",
            args = {PointF.class}
        ),
        @TestTargetNew(
            level = TestLevel.SUFFICIENT,
            notes = "This method is not currently implemented (returns 0)",
            method = "pose",
            args = {int.class}
        )
    })
    public void testFaceProperties() throws Exception {
        long waitMsec = 5000;
        Thread.sleep(waitMsec);
        List<Face> detectedFaces = mActivity.getDetectedFaces();
        assertEquals(1, detectedFaces.size());
        Face face = detectedFaces.get(0);
        PointF eyesMP = new PointF();
        face.getMidPoint(eyesMP);
        float tolerance = 5f;
        float goodConfidence = 0.3f;
        assertTrue(face.confidence() >= goodConfidence);
        float eyesDistance = 20.0f;
        assertEquals(eyesDistance, face.eyesDistance(), tolerance);
        float eyesMidpointX = 60.0f;
        float eyesMidpointY = 60.0f;
        assertEquals(eyesMidpointX, eyesMP.x, tolerance);
        assertEquals(eyesMidpointY, eyesMP.y, tolerance);
        face.pose(FaceDetector.Face.EULER_X);
        face.pose(FaceDetector.Face.EULER_Y);
        face.pose(FaceDetector.Face.EULER_Z);
        int ErrorEuler = 100;
        try {
            face.pose(ErrorEuler);
            fail("Should throw IllegalArgumentException");
        } catch (IllegalArgumentException e) {
        }
    }
}
