@TestTargetClass(TableLayout.LayoutParams.class)
public class TableLayout_LayoutParamsTest extends InstrumentationTestCase {
    private Context mTargetContext;
    @Override
    protected void setUp() throws Exception {
        super.setUp();
        mTargetContext = getInstrumentation().getTargetContext();
    }
    @TestTargets({
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            notes = "Test constructor(s) of {@link TableLayout.LayoutParams}.",
            method = "TableLayout.LayoutParams",
            args = {}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            notes = "Test constructor(s) of {@link TableLayout.LayoutParams}.",
            method = "TableLayout.LayoutParams",
            args = {android.content.Context.class, android.util.AttributeSet.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            notes = "Test constructor(s) of {@link TableLayout.LayoutParams}.",
            method = "TableLayout.LayoutParams",
            args = {int.class, int.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            notes = "Test constructor(s) of {@link TableLayout.LayoutParams}.",
            method = "TableLayout.LayoutParams",
            args = {int.class, int.class, float.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            notes = "Test constructor(s) of {@link TableLayout.LayoutParams}.",
            method = "TableLayout.LayoutParams",
            args = {android.view.ViewGroup.LayoutParams.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            notes = "Test constructor(s) of {@link TableLayout.LayoutParams}.",
            method = "TableLayout.LayoutParams",
            args = {android.view.ViewGroup.MarginLayoutParams.class}
        )
    })
    public void testConstructor() {
        new TableLayout.LayoutParams(mTargetContext, null);
        TableLayout.LayoutParams layoutParams = new TableLayout.LayoutParams(200, 300);
        assertEquals(TableLayout.LayoutParams.MATCH_PARENT, layoutParams.width);
        assertEquals(300, layoutParams.height);
        TableLayout.LayoutParams oldParams = layoutParams;
        layoutParams = new TableLayout.LayoutParams(200, 300, 1.2f);
        assertEquals(TableLayout.LayoutParams.MATCH_PARENT, layoutParams.width);
        assertEquals(300, layoutParams.height);
        assertEquals(1.2f, layoutParams.weight);
        TableLayout.LayoutParams oldMarginParams = layoutParams;
        new TableLayout.LayoutParams();
        layoutParams = new TableLayout.LayoutParams(oldParams);
        assertEquals(TableLayout.LayoutParams.MATCH_PARENT, layoutParams.width);
        assertEquals(300, layoutParams.height);
        layoutParams = new TableLayout.LayoutParams(oldMarginParams);
        assertEquals(TableLayout.LayoutParams.MATCH_PARENT, layoutParams.width);
        assertEquals(300, layoutParams.height);
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "Test setBaseAttributes(TypedArray a, int widthAttr, int heightAttr)",
        method = "setBaseAttributes",
        args = {android.content.res.TypedArray.class, int.class, int.class}
    )
    @ToBeFixed( bug = "1417734", explanation = "NullPointerException issue")
    public void testSetBaseAttributes() {
        MockTableLayout_LayoutParams mockLayoutParams = new MockTableLayout_LayoutParams(200, 300);
        assertEquals(TableLayout.LayoutParams.MATCH_PARENT, mockLayoutParams.width);
        assertEquals(300, mockLayoutParams.height);
        AttributeSet attrs = getAttrs("base_attr_pixel");
        TypedArray a = mTargetContext.obtainStyledAttributes(attrs, R.styleable.ViewGroup_Layout);
        mockLayoutParams.setBaseAttributes(a, R.styleable.ViewGroup_Layout_layout_width,
                R.styleable.ViewGroup_Layout_layout_height);
        assertEquals(TableLayout.LayoutParams.MATCH_PARENT, mockLayoutParams.width);
        assertEquals(600, mockLayoutParams.height);
        mockLayoutParams.setBaseAttributes(a, R.styleable.ViewGroup_Layout_layout_height,
                R.styleable.ViewGroup_Layout_layout_width);
        assertEquals(TableLayout.LayoutParams.MATCH_PARENT, mockLayoutParams.width);
        assertEquals(400, mockLayoutParams.height);
        a.recycle();
        attrs = getAttrs("base_attr_fillwrap");
        a = mTargetContext.obtainStyledAttributes(attrs, R.styleable.ViewGroup_Layout);
        mockLayoutParams.setBaseAttributes(a, R.styleable.ViewGroup_Layout_layout_width,
                R.styleable.ViewGroup_Layout_layout_height);
        assertEquals(TableLayout.LayoutParams.MATCH_PARENT, mockLayoutParams.width);
        assertEquals(TableLayout.LayoutParams.WRAP_CONTENT, mockLayoutParams.height);
        mockLayoutParams.setBaseAttributes(a, R.styleable.ViewGroup_Layout_layout_height,
                R.styleable.ViewGroup_Layout_layout_width);
        assertEquals(TableLayout.LayoutParams.MATCH_PARENT, mockLayoutParams.width);
        assertEquals(TableLayout.LayoutParams.MATCH_PARENT, mockLayoutParams.height);
        a.recycle();
        attrs = getAttrs("base_attr_noheight");
        a = mTargetContext.obtainStyledAttributes(attrs, R.styleable.ViewGroup_Layout);
        mockLayoutParams.setBaseAttributes(a, R.styleable.ViewGroup_Layout_layout_width,
                R.styleable.ViewGroup_Layout_layout_height);
        assertEquals(TableLayout.LayoutParams.MATCH_PARENT, mockLayoutParams.width);
        assertEquals(TableLayout.LayoutParams.WRAP_CONTENT, mockLayoutParams.height);
        mockLayoutParams.setBaseAttributes(a, R.styleable.ViewGroup_Layout_layout_height,
                R.styleable.ViewGroup_Layout_layout_width);
        assertEquals(TableLayout.LayoutParams.MATCH_PARENT, mockLayoutParams.width);
        assertEquals(600, mockLayoutParams.height);
        try {
            mockLayoutParams.setBaseAttributes(null, R.styleable.ViewGroup_Layout_layout_width,
                    R.styleable.ViewGroup_Layout_layout_height);
            fail("Should throw NullPointerException");
        } catch (NullPointerException e) {
        }
        mockLayoutParams.setBaseAttributes(a, -1, R.styleable.ViewGroup_Layout_layout_height);
        assertEquals(TableLayout.LayoutParams.MATCH_PARENT, mockLayoutParams.width);
        assertEquals(TableLayout.LayoutParams.WRAP_CONTENT, mockLayoutParams.height);
        try {
            mockLayoutParams.setBaseAttributes(null, -1, -1);
            fail("Should throw NullPointerException");
        } catch (NullPointerException e) {
        }
    }
    private AttributeSet getAttrs(String searchedNodeName) {
        XmlResourceParser parser = null;
        AttributeSet attrs = null;
        try {
            parser = mTargetContext.getResources()
                    .getXml(com.android.cts.stub.R.xml.base_attributes);
            int type;
            while ((type = parser.next()) != XmlPullParser.END_DOCUMENT
                    && type != XmlPullParser.START_TAG) {
            }
            String nodeName = parser.getName();
            if (!"alias".equals(nodeName)) {
                throw new RuntimeException();
            }
            int outerDepth = parser.getDepth();
            while ((type = parser.next()) != XmlPullParser.END_DOCUMENT
                    && (type != XmlPullParser.END_TAG || parser.getDepth() > outerDepth)) {
                if (type == XmlPullParser.END_TAG || type == XmlPullParser.TEXT) {
                    continue;
                }
                nodeName = parser.getName();
                if (searchedNodeName.equals(nodeName)) {
                    outerDepth = parser.getDepth();
                    while ((type = parser.next()) != XmlPullParser.END_DOCUMENT
                            && (type != XmlPullParser.END_TAG || parser.getDepth() > outerDepth)) {
                        if (type == XmlPullParser.END_TAG || type == XmlPullParser.TEXT) {
                            continue;
                        }
                        nodeName = parser.getName();
                        if ("Attributes".equals(nodeName)) {
                            attrs = Xml.asAttributeSet(parser);
                            break;
                        } else {
                            XmlUtils.skipCurrentTag(parser);
                        }
                    }
                    break;
                } else {
                    XmlUtils.skipCurrentTag(parser);
                }
            }
        } catch (Exception e) {
        }
        return attrs;
    }
    private class MockTableLayout_LayoutParams extends TableLayout.LayoutParams {
        public MockTableLayout_LayoutParams(int w, int h) {
            super(w, h);
        }
        @Override
        protected void setBaseAttributes(TypedArray a, int widthAttr,
                int heightAttr) {
            super.setBaseAttributes(a, widthAttr, heightAttr);
        }
    }
}
