public class CameraTest extends ActivityInstrumentationTestCase<MediaFrameworkTest> {    
    private String TAG = "CameraTest";
    private boolean rawPreviewCallbackResult = false;
    private boolean shutterCallbackResult = false;
    private boolean rawPictureCallbackResult = false;
    private boolean jpegPictureCallbackResult = false;
    private static int WAIT_FOR_COMMAND_TO_COMPLETE = 10000;  
    private RawPreviewCallback mRawPreviewCallback = new RawPreviewCallback();
    private TestShutterCallback mShutterCallback = new TestShutterCallback();
    private RawPictureCallback mRawPictureCallback = new RawPictureCallback();
    private JpegPictureCallback mJpegPictureCallback = new JpegPictureCallback();
    private boolean mInitialized = false;
    private Looper mLooper = null;
    private final ConditionVariable mPreviewDone = new ConditionVariable();
    private final ConditionVariable mSnapshotDone = new ConditionVariable();
    Camera mCamera;
    Context mContext;
    public CameraTest() {
        super("com.android.mediaframeworktest", MediaFrameworkTest.class);
    }
    protected void setUp() throws Exception {
        super.setUp(); 
    }
    private void initializeMessageLooper() {
        final ConditionVariable startDone = new ConditionVariable();
        Log.v(TAG, "start looper");
        new Thread() {
            @Override
            public void run() {
                Looper.prepare();
                Log.v(TAG, "start loopRun");
                mLooper = Looper.myLooper();
                mCamera = Camera.open();
                startDone.open();
                Looper.loop();  
                Log.v(TAG, "initializeMessageLooper: quit.");
            }
        }.start();
        if (!startDone.block(WAIT_FOR_COMMAND_TO_COMPLETE)) {
            fail("initializeMessageLooper: start timeout");
        }
    }
    private void terminateMessageLooper() throws Exception {
        mLooper.quit();
        mLooper.getThread().join();
        mCamera.release();
    }
    private final class RawPreviewCallback implements PreviewCallback { 
        public void onPreviewFrame(byte [] rawData, Camera camera) {         
            Log.v(TAG, "Preview callback start");            
            int rawDataLength = 0;
            if (rawData != null) {
                rawDataLength = rawData.length;
            }
            if (rawDataLength > 0) {
                rawPreviewCallbackResult = true;
            } else {
                rawPreviewCallbackResult = false;
            }
            mPreviewDone.open();
            Log.v(TAG, "Preview callback stop");
        }
    };
    private final class TestShutterCallback implements ShutterCallback {
        public void onShutter() {
            shutterCallbackResult = true;
            Log.v(TAG, "onShutter called");
        }
    };
    private final class RawPictureCallback implements PictureCallback { 
        public void onPictureTaken(byte [] rawData, Camera camera) {
            rawPictureCallbackResult = true;
            Log.v(TAG, "RawPictureCallback callback");
        }
    };
    private final class JpegPictureCallback implements PictureCallback {   
        public void onPictureTaken(byte [] rawData, Camera camera) {           
            try {         
                if (rawData != null) {
                    int rawDataLength = rawData.length;
                    File rawoutput = new File("/sdcard/test.bmp");
                    FileOutputStream outstream = new FileOutputStream(rawoutput);
                    outstream.write(rawData);                   
                    Log.v(TAG, "JpegPictureCallback rawDataLength = " + rawDataLength);
                    jpegPictureCallbackResult = true;
                } else {
                    jpegPictureCallbackResult = false;
                }
                mSnapshotDone.open();
                Log.v(TAG, "Jpeg Picture callback");
            } catch (Exception e) {
                Log.v(TAG, e.toString());
            }
        }
    };
    private void waitForPreviewDone() {
        if (!mPreviewDone.block(WAIT_FOR_COMMAND_TO_COMPLETE)) {
            Log.v(TAG, "waitForPreviewDone: timeout");
        }
        mPreviewDone.close();
    }
    private void waitForSnapshotDone() {
        if (!mSnapshotDone.block(MediaNames.WAIT_SNAPSHOT_TIME)) {
            Log.v(TAG, "waitForSnapshotDone: timeout");
        }
        mSnapshotDone.close();
    }
    private void checkTakePicture() { 
        SurfaceHolder mSurfaceHolder;
        try {
            mSurfaceHolder = MediaFrameworkTest.mSurfaceView.getHolder();
            mCamera.setPreviewDisplay(mSurfaceHolder);
            Log.v(TAG, "Start preview");
            mCamera.startPreview();
            waitForPreviewDone();
            mCamera.setPreviewCallback(null);
            mCamera.takePicture(mShutterCallback, mRawPictureCallback, mJpegPictureCallback);
            waitForSnapshotDone();
        } catch (Exception e) {
            Log.v(TAG, e.toString());
        }      
    }
    private void checkPreviewCallback() { 
        SurfaceHolder mSurfaceHolder;
        try {
            mSurfaceHolder = MediaFrameworkTest.mSurfaceView.getHolder();
            mCamera.setPreviewDisplay(mSurfaceHolder);
            Log.v(TAG, "start preview");
            mCamera.startPreview();
            waitForPreviewDone();
            mCamera.setPreviewCallback(null);
        } catch (Exception e) {
            Log.v(TAG, e.toString());
        }      
    }
    @LargeTest
    public void testTakePicture() throws Exception {  
        initializeMessageLooper();
        mCamera.setPreviewCallback(mRawPreviewCallback);
        checkTakePicture();
        terminateMessageLooper();
        assertTrue("shutterCallbackResult", shutterCallbackResult);
        assertTrue("rawPictureCallbackResult", rawPictureCallbackResult);
        assertTrue("jpegPictureCallbackResult", jpegPictureCallbackResult);
    }
    @LargeTest
    public void testCheckPreview() throws Exception {  
        initializeMessageLooper();
        mCamera.setPreviewCallback(mRawPreviewCallback);
        checkPreviewCallback();     
        terminateMessageLooper();
        assertTrue("RawPreviewCallbackResult", rawPreviewCallbackResult);
    }
}
