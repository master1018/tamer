public class RequestFocusTest extends ActivityInstrumentationTestCase<RequestFocus> {
    private Button mTopLeftButton;
    private Button mBottomLeftButton;
    private Button mTopRightButton;
    private Button mBottomRightButton;
    private Handler mHandler;
    public RequestFocusTest() {
        super("com.android.frameworks.coretests", RequestFocus.class);
    }
    @Override
    public void setUp() throws Exception {
        super.setUp();
        final RequestFocus a = getActivity();
        mHandler = a.getHandler();
        mTopLeftButton = (Button) a.findViewById(R.id.topLeftButton);
        mBottomLeftButton = (Button) a.findViewById(R.id.bottomLeftButton);
        mTopRightButton = (Button) a.findViewById(R.id.topRightButton);
        mBottomRightButton = (Button) a.findViewById(R.id.bottomRightButton);
    }
    @MediumTest
    public void testSetUpConditions() throws Exception {
        assertNotNull(mHandler);
        assertNotNull(mTopLeftButton);
        assertNotNull(mTopRightButton);
        assertNotNull(mBottomLeftButton);
        assertNotNull(mBottomRightButton);
        assertTrue("requestFocus() should work from onCreate.", mBottomRightButton.hasFocus());
    }
    @LargeTest
    public void testPostedRequestFocus() throws Exception {
        mHandler.post(new Runnable() { public void run() {
            mBottomLeftButton.requestFocus();
        }});
        synchronized(this) {
            try {
                wait(500);
            } catch (InterruptedException e) {
            }
        }
        assertTrue("Focus should move to bottom left", mBottomLeftButton.hasFocus());
    }
    @MediumTest
    public void testWrongThreadRequestFocusFails() throws Exception {
        try {
            mTopRightButton.requestFocus();
            fail("requestFocus from wrong thread should raise exception.");
        } catch (AndroidRuntimeException e) {
            assertEquals("android.view.ViewRoot$CalledFromWrongThreadException",
                         e.getClass().getName());
        }
    }
}
