@TestTargetClass(DigitalClock.class)
public class DigitalClockTest extends ActivityInstrumentationTestCase<DigitalClockStubActivity> {
    private Activity mActivity;
    private Context mContext;
    public DigitalClockTest() {
        super("com.android.cts.stub", DigitalClockStubActivity.class);
    }
    @Override
    protected void setUp() throws Exception {
        super.setUp();
        mActivity = getActivity();
        mContext = getInstrumentation().getContext();
    }
    @TestTargets({
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            notes = "",
            method = "DigitalClock",
            args = {android.content.Context.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            notes = "",
            method = "DigitalClock",
            args = {android.content.Context.class, android.util.AttributeSet.class}
        )
    })
    public void testConstructor() {
        new DigitalClock(mContext);
        new DigitalClock(mContext, null);
        new DigitalClock(mContext, getAttributeSet(R.layout.digitalclock_layout));
        try {
            new DigitalClock(null, getAttributeSet(R.layout.digitalclock_layout));
            fail("should throw NullPointerException");
        } catch (Exception e) {
        }
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "Test {@link DigitalClock#onDetachedFromWindow()}.",
        method = "onDetachedFromWindow",
        args = {}
    )
    @UiThreadTest
    public void testOnDetachedFromWindow() {
        final MockDigitalClock digitalClock = createDigitalClock();
        final LinearLayout linearLayout = (LinearLayout) mActivity.findViewById(
                R.id.digitalclock_root);
        assertFalse(digitalClock.hasCalledOnAttachedToWindow());
        linearLayout.addView(digitalClock);
        assertTrue(digitalClock.hasCalledOnAttachedToWindow());
        linearLayout.removeView(digitalClock);
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "Test {@link DigitalClock#onAttachedToWindow()}.",
        method = "onAttachedToWindow",
        args = {}
    )
    @UiThreadTest
    public void testOnAttachedToWindow() {
        final MockDigitalClock digitalClock = createDigitalClock();
        final LinearLayout linearLayout = (LinearLayout) mActivity.findViewById(
                R.id.digitalclock_root);
        linearLayout.addView(digitalClock);
        assertFalse(digitalClock.hasCalledOnDetachedFromWindow());
        linearLayout.removeView(digitalClock);
        assertTrue(digitalClock.hasCalledOnDetachedFromWindow());
    }
    private MockDigitalClock createDigitalClock() {
        MockDigitalClock datePicker = new MockDigitalClock(mContext,
                getAttributeSet(R.layout.digitalclock_layout));
        return datePicker;
    }
    private AttributeSet getAttributeSet(int resourceId) {
        XmlResourceParser parser = mActivity.getResources().getXml(resourceId);
        try {
            XmlUtils.beginDocument(parser, "com.android.cts.stub.alarmclock.DigitalClock");
        } catch (XmlPullParserException e) {
            fail("unexpected XmlPullParserException.");
        } catch (IOException e) {
            fail("unexpected IOException.");
        }
        AttributeSet attr = Xml.asAttributeSet(parser);
        assertNotNull(attr);
        return attr;
    }
    private class MockDigitalClock extends DigitalClock {
        private boolean mCalledOnAttachedToWindow   = false;
        private boolean mCalledOnDetachedFromWindow = false;
        public MockDigitalClock(Context context) {
            super(context);
        }
        public MockDigitalClock(Context context, AttributeSet attrs) {
            super(context, attrs);
        }
        @Override
        protected void onAttachedToWindow() {
            super.onAttachedToWindow();
            mCalledOnAttachedToWindow = true;
        }
        public boolean hasCalledOnAttachedToWindow() {
            return mCalledOnAttachedToWindow;
        }
        @Override
        protected void onDetachedFromWindow() {
            super.onDetachedFromWindow();
            mCalledOnDetachedFromWindow = true;
        }
        public boolean hasCalledOnDetachedFromWindow() {
            return mCalledOnDetachedFromWindow;
        }
    }
}
