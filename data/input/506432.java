@TestTargetClass(URLSpan.class)
public class URLSpanTest extends ActivityInstrumentationTestCase2<URLSpanStubActivity> {
    private static final String TEST_URL = "ctstest:
    private Activity mActivity;
    public URLSpanTest() {
        super("com.android.cts.stub", URLSpanStubActivity.class);
    }
    @Override
    protected void setUp() throws Exception {
        super.setUp();
        mActivity = getActivity();
    }
    @TestTargets({
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            notes = "Test constructor(s) of URLSpan.",
            method = "URLSpan",
            args = {java.lang.String.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            notes = "Test constructor(s) of URLSpan.",
            method = "URLSpan",
            args = {android.os.Parcel.class}
        )
    })
    @ToBeFixed(bug = "1695243", explanation = "miss javadoc")
    public void testConstructor() {
        URLSpan urlSpan = new URLSpan(TEST_URL);
        final Parcel p = Parcel.obtain();
        urlSpan.writeToParcel(p, 0);
        p.setDataPosition(0);
        new URLSpan(p);
        p.recycle();
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "Test getURL().",
        method = "getURL",
        args = {}
    )
    @ToBeFixed(bug = "1695243", explanation = "miss javadoc")
    public void testGetURL() {
        URLSpan urlSpan = new URLSpan(TEST_URL);
        assertEquals(TEST_URL, urlSpan.getURL());
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "Test onClick(View widget).",
        method = "onClick",
        args = {android.view.View.class}
    )
    public void testOnClick() {
        final URLSpan urlSpan = new URLSpan(TEST_URL);
        final TextView textView = (TextView) mActivity.findViewById(R.id.url);
        Instrumentation instrumentation = getInstrumentation();
        ActivityMonitor am = instrumentation.addMonitor(MockURLSpanTestActivity.class.getName(),
                null, false);
        try {
            runTestOnUiThread(new Runnable() {
                public void run() {
                    urlSpan.onClick(textView);
                }
            });
        } catch (Throwable e) {
            fail("Exception error!");
        }
        Activity newActivity = am.waitForActivityWithTimeout(5000);
        assertNotNull(newActivity);
        newActivity.finish();
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "Test onClick(View widget).",
        method = "onClick",
        args = {android.view.View.class}
    )
    @ToBeFixed(bug = "1695243", explanation = "should add @throws NullPointerException clause" +
            " into javadoc when input View is null")
    public void testOnClickFailure() {
        URLSpan urlSpan = new URLSpan(TEST_URL);
        try {
            urlSpan.onClick(null);
            fail("should throw NullPointerException.");
        } catch (NullPointerException e) {
        }
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "Test describeContents().",
        method = "describeContents",
        args = {}
    )
    public void testDescribeContents() {
        URLSpan urlSpan = new URLSpan(TEST_URL);
        urlSpan.describeContents();
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "Test getSpanTypeId().",
        method = "getSpanTypeId",
        args = {}
    )
    public void testGetSpanTypeId() {
        URLSpan urlSpan = new URLSpan(TEST_URL);
        urlSpan.getSpanTypeId();
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "Test writeToParcel(Parcel dest, int flags).",
        method = "writeToParcel",
        args = {Parcel.class, int.class}
    )
    public void testWriteToParcel() {
        Parcel p = Parcel.obtain();
        URLSpan urlSpan = new URLSpan(TEST_URL);
        urlSpan.writeToParcel(p, 0);
        p.setDataPosition(0);
        URLSpan u = new URLSpan(p);
        assertEquals(TEST_URL, u.getURL());
        p.recycle();
    }
}
