@TestTargetClass(LayoutParams.class)
public class AbsoluteLayout_LayoutParamsTest extends AndroidTestCase {
    private AttributeSet getAttributeSet() throws XmlPullParserException, IOException {
        XmlPullParser parser = mContext.getResources().getLayout(R.layout.absolute_layout);
        WidgetTestUtils.beginDocument(parser, "LinearLayout");
        return Xml.asAttributeSet(parser);
    }
    @TestTargets({
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "AbsoluteLayout.LayoutParams",
            args = {int.class, int.class, int.class, int.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "AbsoluteLayout.LayoutParams",
            args = {android.content.Context.class, android.util.AttributeSet.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "AbsoluteLayout.LayoutParams",
            args = {android.view.ViewGroup.LayoutParams.class}
        )
    })
    public void testConstructor() throws XmlPullParserException, IOException {
        LayoutParams layoutParams;
        layoutParams = new AbsoluteLayout.LayoutParams(1, 2, 3, 4);
        assertEquals(1, layoutParams.width);
        assertEquals(2, layoutParams.height);
        assertEquals(3, layoutParams.x);
        assertEquals(4, layoutParams.y);
        LayoutParams params = new AbsoluteLayout.LayoutParams(layoutParams);
        assertEquals(1, params.width);
        assertEquals(2, params.height);
        assertEquals(0, params.x);
        assertEquals(0, params.y);
        new AbsoluteLayout.LayoutParams(mContext, getAttributeSet());
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        method = "debug",
        args = {java.lang.String.class}
    )
    public void testDebug() {
        LayoutParams layoutParams = new AbsoluteLayout.LayoutParams(1, 2, 3, 4);
        assertNotNull(layoutParams.debug("test: "));
    }
}
