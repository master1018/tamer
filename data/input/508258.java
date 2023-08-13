@TestTargetClass(LayoutParams.class)
public class AbsListView_LayoutParamsTest extends AndroidTestCase {
    private AttributeSet mAttributeSet;
    @Override
    protected void setUp() throws Exception {
        super.setUp();
        XmlPullParser parser = mContext.getResources().getXml(R.layout.abslistview_layout);
        WidgetTestUtils.beginDocument(parser, "ViewGroup_Layout");
        mAttributeSet = Xml.asAttributeSet(parser);
    }
    @TestTargets({
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "AbsListView.LayoutParams",
            args = {android.content.Context.class, android.util.AttributeSet.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "AbsListView.LayoutParams",
            args = {int.class, int.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "AbsListView.LayoutParams",
            args = {int.class, int.class, int.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "AbsListView.LayoutParams",
            args = {android.view.ViewGroup.LayoutParams.class}
        )
    })
    public void testConstructors() {
        int TEST_WIDTH = 25;
        int TEST_HEIGHT = 25;
        int TEST_HEIGHT2 = 30;
        AbsListView.LayoutParams layoutParams;
        layoutParams = new AbsListView.LayoutParams(getContext(), mAttributeSet);
        assertEquals(TEST_WIDTH, layoutParams.width);
        assertEquals(TEST_HEIGHT, layoutParams.height);
        layoutParams = new AbsListView.LayoutParams(LayoutParams.MATCH_PARENT,
                LayoutParams.MATCH_PARENT);
        assertEquals(LayoutParams.MATCH_PARENT, layoutParams.width);
        assertEquals(LayoutParams.MATCH_PARENT, layoutParams.height);
        layoutParams = new AbsListView.LayoutParams(LayoutParams.MATCH_PARENT,
                LayoutParams.MATCH_PARENT, 0);
        assertEquals(LayoutParams.MATCH_PARENT, layoutParams.width);
        assertEquals(LayoutParams.MATCH_PARENT, layoutParams.height);
        AbsListView.LayoutParams tmpParams = new AbsListView.LayoutParams(TEST_WIDTH, TEST_HEIGHT2);
        layoutParams = new AbsListView.LayoutParams(tmpParams);
        assertEquals(TEST_WIDTH, layoutParams.width);
        assertEquals(TEST_HEIGHT2, layoutParams.height);
    }
}
