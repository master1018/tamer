public class RemoteViewsActivityTest
        extends ActivityInstrumentationTestCase2<RemoteViewsStubActivity> {
    private static final String PACKAGE_NAME = "com.android.cts.stub";
    private Activity mActivity;
    public RemoteViewsActivityTest() {
        super(PACKAGE_NAME, RemoteViewsStubActivity.class);
    }
    @Override
    public void setUp() throws Exception {
        super.setUp();
        mActivity = getActivity();
    }
    @MediumTest
    public void testGood() throws Exception {
        RemoteViews orig = new RemoteViews(PACKAGE_NAME, R.layout.remote_view_test_good);
        Parcel p = Parcel.obtain();
        orig.writeToParcel(p, 0);
        p.setDataPosition(0);
        RemoteViews r = RemoteViews.CREATOR.createFromParcel(p);
        ViewGroup parent = (ViewGroup) mActivity.findViewById(R.id.remoteView_host);
        View result = r.apply(mActivity, parent);
        p.recycle();
        assertTrue("LinearLayout not inflated", result.findViewById(R.id.linear) != null);
        assertTrue("TextView not inflated", result.findViewById(R.id.text) != null);
        assertTrue("ImageView not inflated", result.findViewById(R.id.image) != null);
        assertTrue("FrameLayout not inflated", result.findViewById(R.id.frame) != null);
        assertTrue("RelateiveLayout not inflated", result.findViewById(R.id.relative) != null);
        assertTrue("AbsoluteLayout not inflated", result.findViewById(R.id.absolute) != null);
        assertTrue("ProgressBar not inflated", result.findViewById(R.id.progress) != null);
        assertTrue("ImageButton not inflated", result.findViewById(R.id.image_button) != null);
        assertTrue("Button not inflated", result.findViewById(R.id.button) != null);
    }
    @MediumTest
    public void testDerivedClass() throws Exception {
        RemoteViews orig = new RemoteViews(PACKAGE_NAME, R.layout.remote_view_test_bad_1);
        Parcel p = Parcel.obtain();
        orig.writeToParcel(p, 0);
        p.setDataPosition(0);
        RemoteViews r = RemoteViews.CREATOR.createFromParcel(p);
        ViewGroup parent = (ViewGroup) mActivity.findViewById(R.id.remoteView_host);
        boolean exceptionThrown = false;
        View result = null;
        try {
            result = r.apply(mActivity, parent);
        } catch (InflateException e) {
            exceptionThrown = true;
        }
        p.recycle();
        assertTrue("Derived class (EditText) allowed to be inflated", exceptionThrown);
        assertNull("Derived class (EditText) allowed to be inflated", result);
    }
    @MediumTest
    public void testWebView() throws Exception {
        RemoteViews orig = new RemoteViews(PACKAGE_NAME, R.layout.remote_view_test_bad_2);
        Parcel p = Parcel.obtain();
        orig.writeToParcel(p, 0);
        p.setDataPosition(0);
        RemoteViews r = RemoteViews.CREATOR.createFromParcel(p);
        ViewGroup parent = (ViewGroup) mActivity.findViewById(R.id.remoteView_host);
        boolean exceptionThrown = false;
        View result = null;
        try {
            result = r.apply(mActivity, parent);
        } catch (InflateException e) {
            exceptionThrown = true;
        }
        p.recycle();
        assertTrue("WebView allowed to be inflated", exceptionThrown);
        assertNull("WebView allowed to be inflated", result);
    }
}
