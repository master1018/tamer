public class ColorStateListTest extends AndroidTestCase {
    private Resources mResources;
    private int mFailureColor;
    @Override
    protected void setUp() throws Exception {
        super.setUp();
        mResources = mContext.getResources();
        mFailureColor = mResources.getColor(R.color.failColor);
    }
    @SmallTest
    public void testStateIsInList() throws Exception {
        ColorStateList colorStateList = mResources.getColorStateList(R.color.color1);
        int[] focusedState = {android.R.attr.state_focused};
        int focusColor = colorStateList.getColorForState(focusedState, R.color.failColor);
        assertEquals(mResources.getColor(R.color.testcolor1), focusColor);
    }
    @SmallTest
    public void testEmptyState() throws Exception {
        ColorStateList colorStateList = mResources.getColorStateList(R.color.color1);
        int[] emptyState = {};
        int defaultColor = colorStateList.getColorForState(emptyState, mFailureColor);
        assertEquals(mResources.getColor(R.color.testcolor2), defaultColor);
    }
    @SmallTest
    public void testGetColor() throws Exception {
        int defaultColor = mResources.getColor(R.color.color1);
        assertEquals(mResources.getColor(R.color.testcolor2), defaultColor);
    }
    @SmallTest
    public void testGetColorWhenListHasNoDefault() throws Exception {
        int defaultColor = mResources.getColor(R.color.color_no_default);
        assertEquals(mResources.getColor(R.color.testcolor1), defaultColor);
    }
}
