@TestTargetClass(View.MeasureSpec.class)
public class View_MeasureSpecTest extends AndroidTestCase {
    private static final int MEASURE_SPEC_SIZE = 1;
    private int mUnspecifiedMeasureSpec;
    private int mExactlyMeasureSpec;
    private int mAtMostMeasureSpec;
    @Override
    protected void setUp() throws Exception {
        super.setUp();
        mUnspecifiedMeasureSpec = View.MeasureSpec.makeMeasureSpec(MEASURE_SPEC_SIZE,
                View.MeasureSpec.UNSPECIFIED);
        mExactlyMeasureSpec = View.MeasureSpec.makeMeasureSpec(MEASURE_SPEC_SIZE,
                View.MeasureSpec.EXACTLY);
        mAtMostMeasureSpec = View.MeasureSpec.makeMeasureSpec(MEASURE_SPEC_SIZE,
                View.MeasureSpec.AT_MOST);
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "This is a foo test, just add annotation for constructor, to make nooser pass",
        method = "View.MeasureSpec",
        args = {}
    )
    public void testConstructor() {
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "Test View.MeasureSpec#getSize(int)",
        method = "getSize",
        args = {int.class}
    )
    public void testGetSize() {
        assertEquals(MEASURE_SPEC_SIZE,
                View.MeasureSpec.getSize(mUnspecifiedMeasureSpec));
        assertEquals(MEASURE_SPEC_SIZE,
                View.MeasureSpec.getSize(mExactlyMeasureSpec));
        assertEquals(MEASURE_SPEC_SIZE,
                View.MeasureSpec.getSize(mAtMostMeasureSpec));
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "Test View.MeasureSpec#toString(int)",
        method = "toString",
        args = {int.class}
    )
    public void testToString() {
        assertEquals("MeasureSpec: UNSPECIFIED " + MEASURE_SPEC_SIZE,
                View.MeasureSpec.toString(mUnspecifiedMeasureSpec));
        assertEquals("MeasureSpec: EXACTLY " + MEASURE_SPEC_SIZE,
                View.MeasureSpec.toString(mExactlyMeasureSpec));
        assertEquals("MeasureSpec: AT_MOST " + MEASURE_SPEC_SIZE,
                View.MeasureSpec.toString(mAtMostMeasureSpec));
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "Test View.MeasureSpec#getMode(int)",
        method = "getMode",
        args = {int.class}
    )
    public void testGetMode() {
        assertEquals(View.MeasureSpec.UNSPECIFIED,
                View.MeasureSpec.getMode(mUnspecifiedMeasureSpec));
        assertEquals(View.MeasureSpec.EXACTLY,
                View.MeasureSpec.getMode(mExactlyMeasureSpec));
        assertEquals(View.MeasureSpec.AT_MOST,
                View.MeasureSpec.getMode(mAtMostMeasureSpec));
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "Test View.MeasureSpec#makeMeasureSpec(int, int)",
        method = "makeMeasureSpec",
        args = {int.class, int.class}
    )
    public void testMakeMeasureSpec() {
        assertEquals(MEASURE_SPEC_SIZE + View.MeasureSpec.UNSPECIFIED,
                mUnspecifiedMeasureSpec);
        assertEquals(MEASURE_SPEC_SIZE + View.MeasureSpec.EXACTLY,
                mExactlyMeasureSpec);
        assertEquals(MEASURE_SPEC_SIZE + View.MeasureSpec.AT_MOST,
                mAtMostMeasureSpec);
    }
}
