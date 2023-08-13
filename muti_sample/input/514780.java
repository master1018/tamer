public class CameraLatency extends ActivityInstrumentationTestCase2 <Camera> {
    private String TAG = "CameraLatency";
    private static final int TOTAL_NUMBER_OF_IMAGECAPTURE = 20;
    private static final long WAIT_FOR_IMAGE_CAPTURE_TO_BE_TAKEN = 4000;
    private static final String CAMERA_TEST_OUTPUT_FILE = "/sdcard/mediaStressOut.txt";
    private long mTotalAutoFocusTime;
    private long mTotalShutterLag;
    private long mTotalShutterToPictureDisplayedTime;
    private long mTotalPictureDisplayedToJpegCallbackTime;
    private long mTotalJpegCallbackFinishTime;
    private long mAvgAutoFocusTime;
    private long mAvgShutterLag = mTotalShutterLag;
    private long mAvgShutterToPictureDisplayedTime;
    private long mAvgPictureDisplayedToJpegCallbackTime;
    private long mAvgJpegCallbackFinishTime;
    public CameraLatency() {
        super("com.google.android.camera", Camera.class);
    }
    @Override
    protected void setUp() throws Exception {
        getActivity();
        super.setUp();
    }
    @Override
    protected void tearDown() throws Exception {
        super.tearDown();
    }
    @LargeTest
    public void testImageCapture() {
        Log.v(TAG, "start testImageCapture test");
        Instrumentation inst = getInstrumentation();
        try {
            for (int i = 0; i < TOTAL_NUMBER_OF_IMAGECAPTURE; i++) {
                Thread.sleep(WAIT_FOR_IMAGE_CAPTURE_TO_BE_TAKEN);
                inst.sendKeyDownUpSync(KeyEvent.KEYCODE_DPAD_UP);
                inst.sendKeyDownUpSync(KeyEvent.KEYCODE_DPAD_CENTER);
                Thread.sleep(WAIT_FOR_IMAGE_CAPTURE_TO_BE_TAKEN);
                if (i != 0) {
                    Camera c = getActivity();
                    mTotalAutoFocusTime += c.mAutoFocusTime;
                    mTotalShutterLag += c.mShutterLag;
                    mTotalShutterToPictureDisplayedTime +=
                            c.mShutterToPictureDisplayedTime;
                    mTotalPictureDisplayedToJpegCallbackTime +=
                            c.mPictureDisplayedToJpegCallbackTime;
                    mTotalJpegCallbackFinishTime += c.mJpegCallbackFinishTime;
                }
            }
        } catch (Exception e) {
            Log.v(TAG, e.toString());
        }
        int numberofRun = TOTAL_NUMBER_OF_IMAGECAPTURE - 1;
        mAvgAutoFocusTime = mTotalAutoFocusTime / numberofRun;
        mAvgShutterLag = mTotalShutterLag / numberofRun;
        mAvgShutterToPictureDisplayedTime =
                mTotalShutterToPictureDisplayedTime / numberofRun;
        mAvgPictureDisplayedToJpegCallbackTime =
                mTotalPictureDisplayedToJpegCallbackTime / numberofRun;
        mAvgJpegCallbackFinishTime =
                mTotalJpegCallbackFinishTime / numberofRun;
        try {
            FileWriter fstream = null;
            fstream = new FileWriter(CAMERA_TEST_OUTPUT_FILE, true);
            BufferedWriter out = new BufferedWriter(fstream);
            out.write("Camera Latency : \n");
            out.write("Number of loop: " + TOTAL_NUMBER_OF_IMAGECAPTURE + "\n");
            out.write("Avg AutoFocus = " + mAvgAutoFocusTime + "\n");
            out.write("Avg mShutterLag = " + mAvgShutterLag + "\n");
            out.write("Avg mShutterToPictureDisplayedTime = "
                    + mAvgShutterToPictureDisplayedTime + "\n");
            out.write("Avg mPictureDisplayedToJpegCallbackTime = "
                    + mAvgPictureDisplayedToJpegCallbackTime + "\n");
            out.write("Avg mJpegCallbackFinishTime = " +
                    mAvgJpegCallbackFinishTime + "\n");
            out.close();
            fstream.close();
        } catch (Exception e) {
            fail("Camera Latency write output to file");
        }
        Log.v(TAG, "The Image capture wait time = " +
            WAIT_FOR_IMAGE_CAPTURE_TO_BE_TAKEN);
        Log.v(TAG, "Avg AutoFocus = " + mAvgAutoFocusTime);
        Log.v(TAG, "Avg mShutterLag = " + mAvgShutterLag);
        Log.v(TAG, "Avg mShutterToPictureDisplayedTime = "
                + mAvgShutterToPictureDisplayedTime);
        Log.v(TAG, "Avg mPictureDisplayedToJpegCallbackTime = "
                + mAvgPictureDisplayedToJpegCallbackTime);
        Log.v(TAG, "Avg mJpegCallbackFinishTime = " + mAvgJpegCallbackFinishTime);
        assertTrue("testImageCapture", true);
    }
}
