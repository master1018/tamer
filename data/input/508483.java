public class CallerInfoUnitTest extends AndroidTestCase {
    private CallerInfo mInfo;
    private Context mContext;
    private static final String kEmergencyNumber = "Emergency Number";
    private static final int kToken = 0xdeadbeef;
    private static final String TAG = "CallerInfoUnitTest";
    @Override
    protected void setUp() throws Exception {
        super.setUp();
        mContext = new MockContext();
        mInfo = new CallerInfo();
    }
    @Override
    protected void tearDown() throws Exception {
        super.tearDown();
    }
    @SmallTest
    public void testEmergencyIsProperlySet() throws Exception {
        assertFalse(mInfo.isEmergencyNumber());
        mInfo = CallerInfo.getCallerInfo(mContext, "911");
        assertIsValidEmergencyCallerInfo();
        mInfo = CallerInfo.getCallerInfo(mContext, "tel:911");
        assertIsValidEmergencyCallerInfo();
        mInfo = CallerInfo.getCallerInfo(mContext, "18001234567");
        assertFalse(mInfo.isEmergencyNumber());
    }
    @SmallTest
    public void testEmergencyIsProperlySetUsingAsyncQuery() throws Exception {
        QueryRunner query;
        query = new QueryRunner("911");
        query.runAndCheckCompletion();
        assertIsValidEmergencyCallerInfo();
        query = new QueryRunner("tel:911");
        query.runAndCheckCompletion();
        assertIsValidEmergencyCallerInfo();
        query = new QueryRunner("18001234567");
        query.runAndCheckCompletion();
        assertFalse(mInfo.isEmergencyNumber());
    }
    @SmallTest
    public void testEmergencyNumberAndPhotoAreSet() throws Exception {
        mInfo = CallerInfo.getCallerInfo(mContext, "911");
        assertIsValidEmergencyCallerInfo();
    }
    public class MockResources extends android.test.mock.MockResources
    {
        @Override
        public String getString(int resId) throws Resources.NotFoundException {
            switch (resId) {
                case com.android.internal.R.string.emergency_call_dialog_number_for_display:
                    return kEmergencyNumber;
                default:
                    throw new UnsupportedOperationException("Missing handling for resid " + resId);
            }
        }
    }
    public class MockContext extends android.test.mock.MockContext {
        private ContentResolver mResolver;
        private Resources mResources;
        public MockContext() {
            mResolver = new android.test.mock.MockContentResolver();
            mResources = new MockResources();
        }
        @Override
        public ContentResolver getContentResolver() {
            return mResolver;
        }
        @Override
        public Resources getResources() {
            return mResources;
        }
    }
    private class QueryRunner extends Thread
            implements CallerInfoAsyncQuery.OnQueryCompleteListener {
        private Looper mLooper;
        private String mNumber;
        private boolean mAsyncCompleted;
        public QueryRunner(String number) {
            super();
            mNumber = number;
        }
        public void runAndCheckCompletion() throws InterruptedException {
            start();
            join();
            assertTrue(mAsyncCompleted);
        }
        @Override
        public void run() {
            Looper.prepare();
            mLooper = Looper.myLooper();
            mAsyncCompleted = false;
            CallerInfoAsyncQuery.startQuery(kToken, mContext, mNumber, this, null);
            mLooper.loop();
        }
        public void onQueryComplete(int token, Object cookie, CallerInfo info) {
            mAsyncCompleted = true;
            mInfo = info;
            mLooper.quit();
        }
    }
    private void assertIsValidEmergencyCallerInfo() throws Exception {
        assertTrue(mInfo.isEmergencyNumber());
        assertEquals(kEmergencyNumber, mInfo.phoneNumber);
        assertEquals(com.android.internal.R.drawable.picture_emergency, mInfo.photoResource);
        assertNull(mInfo.name);
        assertEquals(0, mInfo.namePresentation);
        assertNull(mInfo.cnapName);
        assertEquals(0, mInfo.numberPresentation);
        assertFalse(mInfo.contactExists);
        assertEquals(0, mInfo.person_id);
        assertFalse(mInfo.needUpdate);
        assertNull(mInfo.contactRefUri);
        assertNull(mInfo.phoneLabel);
        assertEquals(0, mInfo.numberType);
        assertNull(mInfo.numberLabel);
        assertNull(mInfo.contactRingtoneUri);
        assertFalse(mInfo.shouldSendToVoicemail);
        assertNull(mInfo.cachedPhoto);
        assertFalse(mInfo.isCachedPhotoCurrent);
    }
}
