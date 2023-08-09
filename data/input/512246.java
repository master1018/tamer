@TestTargetClass(ViewGroup.LayoutParams.class)
public class ViewGroup_LayoutParamsTest extends AndroidTestCase {
    private ViewGroup.LayoutParams mLayoutParams;
    @TestTargets({
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "ViewGroup.LayoutParams",
            args = {int.class, int.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "ViewGroup.LayoutParams",
            args = {android.content.Context.class, android.util.AttributeSet.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "ViewGroup.LayoutParams",
            args = {android.view.ViewGroup.LayoutParams.class}
        )
    })
    public void testConstructor() throws XmlPullParserException, IOException {
        XmlResourceParser parser = mContext.getResources().getLayout(
                R.layout.viewgroup_margin_layout);
        XmlUtils.beginDocument(parser, "LinearLayout");
        new ViewGroup.LayoutParams(mContext, parser);
        LayoutParams temp = new ViewGroup.LayoutParams(320, 480);
        new ViewGroup.LayoutParams(temp);
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "Test setBaseAttributes(TypedArray, int, int)",
        method = "setBaseAttributes",
        args = {android.content.res.TypedArray.class, int.class, int.class}
    )
    public void testSetBaseAttributes() throws XmlPullParserException, IOException {
        MockLayoutParams mockLayoutParams = new MockLayoutParams(240, 320);
        int[] attrs = R.styleable.style1;
        TypedArray array = mContext.getTheme().obtainStyledAttributes(R.style.Whatever, attrs);
        mockLayoutParams.setBaseAttributes(array, R.styleable.style1_type6,
                R.styleable.style1_type7);
        int defValue = -1;
        assertEquals(array.getDimensionPixelSize(R.styleable.style1_type6, defValue),
                mockLayoutParams.width);
        assertEquals(array.getDimensionPixelSize(R.styleable.style1_type7, defValue),
                mockLayoutParams.height);
        array.recycle();
    }
    private class MockLayoutParams extends LayoutParams {
        public MockLayoutParams(Context c, AttributeSet attrs) {
            super(c, attrs);
        }
        public MockLayoutParams(int width, int height) {
            super(width, height);
        }
        public MockLayoutParams(LayoutParams source) {
            super(source);
        }
        protected void setBaseAttributes(TypedArray a, int widthAttr, int heightAttr) {
            super.setBaseAttributes(a, widthAttr, heightAttr);
        }
    }
}
