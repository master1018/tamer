@TestTargetClass(LayoutParams.class)
public class Gallery_LayoutParamsTest extends AndroidTestCase {
    @TestTargets({
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            notes = "Test constructor(s) of {@link LayoutParams}",
            method = "LayoutParams",
            args = {android.content.Context.class, android.util.AttributeSet.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            notes = "Test constructor(s) of {@link LayoutParams}",
            method = "LayoutParams",
            args = {int.class, int.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            notes = "Test constructor(s) of {@link LayoutParams}",
            method = "LayoutParams",
            args = {android.view.ViewGroup.LayoutParams.class}
        )
    })
    public void testConstructor() throws XmlPullParserException, IOException {
        XmlResourceParser p = mContext.getResources().getLayout(R.layout.gallery_test);
        WidgetTestUtils.beginDocument(p, "LinearLayout");
        new LayoutParams(getContext(), p);
        LayoutParams params = new LayoutParams(320, 480);
        new LayoutParams(params);
    }
}
