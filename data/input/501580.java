@TestTargetClass(AnalogClock.class)
public class AnalogClockTest extends ActivityInstrumentationTestCase2<FrameLayoutStubActivity> {
    private AttributeSet mAttrSet;
    private Activity mActivity;
    public AnalogClockTest() {
        super("com.android.cts.stub", FrameLayoutStubActivity.class);
    }
    @Override
    protected void setUp() throws Exception {
        super.setUp();
        XmlPullParser parser = getActivity().getResources().getXml(R.layout.analogclock);
        mAttrSet = Xml.asAttributeSet(parser);
        mActivity = getActivity();
    }
    @TestTargets({
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            notes = "Test constructors of AnalogClock.",
            method = "AnalogClock",
            args = {android.content.Context.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            notes = "Test constructors of AnalogClock.",
            method = "AnalogClock",
            args = {android.content.Context.class, android.util.AttributeSet.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            notes = "Test constructors of AnalogClock.",
            method = "AnalogClock",
            args = {android.content.Context.class, android.util.AttributeSet.class, int.class}
        )
    })
    @ToBeFixed(bug = "1695243", explanation = "Android API javadocs are incomplete")
    public void testConstructor() {
        new AnalogClock(mActivity);
        new AnalogClock(mActivity, mAttrSet);
        new AnalogClock(mActivity, mAttrSet, 0);
        try {
            new AnalogClock(null);
            fail("There should be a NullPointerException thrown out.");
        } catch (NullPointerException e) {
        }
        try {
            new AnalogClock(null, null);
            fail("There should be a NullPointerException thrown out.");
        } catch (NullPointerException e) {
        }
        try {
            new AnalogClock(null, null, -1);
            fail("There should be a NullPointerException thrown out.");
        } catch (NullPointerException e) {
        }
    }
    @TestTargetNew(
        level = TestLevel.NOT_NECESSARY,
        notes = "Test onMeasure() function.",
        method = "onMeasure",
        args = {int.class, int.class}
    )
    public void testOnMeasure() {
    }
    @TestTargetNew(
        level = TestLevel.NOT_NECESSARY,
        notes = "Test onSizeChanged(int, int, int, int) function.",
        method = "onSizeChanged",
        args = {int.class, int.class, int.class, int.class}
    )
    public void testOnSizeChanged() {
    }
    @TestTargetNew(
        level = TestLevel.NOT_NECESSARY,
        notes = "Test onDraw(Canvas) function.",
        method = "onDraw",
        args = {android.graphics.Canvas.class}
    )
    public void testOnDraw() {
    }
    @TestTargetNew(
        level = TestLevel.NOT_NECESSARY,
        notes = "Test onDetachedFromWindow() function.",
        method = "onDetachedFromWindow",
        args = {}
    )
    public void testOnDetachedFromWindow() {
    }
    @TestTargetNew(
        level = TestLevel.NOT_NECESSARY,
        notes = "Test onAttachedToWindow() function.",
        method = "onAttachedToWindow",
        args = {}
    )
    public void testOnAttachedToWindow() {
    }
}
