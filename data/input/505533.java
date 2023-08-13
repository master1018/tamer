@TestTargetClass(LinearLayout.LayoutParams.class)
public class LinearLayout_LayoutParamsTest extends AndroidTestCase {
    @TestTargets({
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            notes = "Test constructors",
            method = "LinearLayout.LayoutParams",
            args = {android.content.Context.class, android.util.AttributeSet.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            notes = "Test constructors",
            method = "LinearLayout.LayoutParams",
            args = {int.class, int.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            notes = "Test constructors",
            method = "LinearLayout.LayoutParams",
            args = {android.view.ViewGroup.LayoutParams.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            notes = "Test constructors",
            method = "LinearLayout.LayoutParams",
            args = {int.class, int.class, float.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            notes = "Test constructors",
            method = "LinearLayout.LayoutParams",
            args = {android.view.ViewGroup.MarginLayoutParams.class}
        )
    })
    public void testConstructor() throws XmlPullParserException, IOException {
        XmlResourceParser p = mContext.getResources().getLayout(R.layout.linearlayout_layout);
        XmlUtils.beginDocument(p, "LinearLayout");
        new LinearLayout.LayoutParams(getContext(), p);
        new LinearLayout.LayoutParams(320, 240);
        new LinearLayout.LayoutParams(320, 240, 0);
        LayoutParams layoutParams = new LayoutParams(320, 480);
        new LinearLayout.LayoutParams(layoutParams);
        MarginLayoutParams marginLayoutParams = new MarginLayoutParams(320, 480);
        new LinearLayout.LayoutParams(marginLayoutParams);
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        method = "debug",
        args = {java.lang.String.class}
    )
    public void testDebug() {
        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(320, 240);
        assertNotNull(layoutParams.debug("test: "));
    }
}
