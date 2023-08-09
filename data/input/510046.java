@TestTargetClass(TableRow.LayoutParams.class)
public class TableRow_LayoutParamsTest
        extends ActivityInstrumentationTestCase2<TableStubActivity> {
    Context mTargetContext;
    public TableRow_LayoutParamsTest() {
        super("com.android.cts.stub", TableStubActivity.class);
    }
    @Override
    protected void setUp() throws Exception {
        super.setUp();
        mTargetContext = getInstrumentation().getTargetContext();
    }
    @TestTargets({
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            notes = "Test constructor(s) of {@link TableRow.LayoutParams}",
            method = "TableRow.LayoutParams",
            args = {}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            notes = "Test constructor(s) of {@link TableRow.LayoutParams}",
            method = "TableRow.LayoutParams",
            args = {android.content.Context.class, android.util.AttributeSet.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            notes = "Test constructor(s) of {@link TableRow.LayoutParams}",
            method = "TableRow.LayoutParams",
            args = {int.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            notes = "Test constructor(s) of {@link TableRow.LayoutParams}",
            method = "TableRow.LayoutParams",
            args = {int.class, int.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            notes = "Test constructor(s) of {@link TableRow.LayoutParams}",
            method = "TableRow.LayoutParams",
            args = {int.class, int.class, float.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            notes = "Test constructor(s) of {@link TableRow.LayoutParams}",
            method = "TableRow.LayoutParams",
            args = {android.view.ViewGroup.LayoutParams.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            notes = "Test constructor(s) of {@link TableRow.LayoutParams}",
            method = "TableRow.LayoutParams",
            args = {android.view.ViewGroup.MarginLayoutParams.class}
        )
    })
    @UiThreadTest
    public void testConstructor() {
        new TableRow.LayoutParams(mTargetContext, null);
        TableRow.LayoutParams layoutParams = new TableRow.LayoutParams(200, 300);
        assertEquals(200, layoutParams.width);
        assertEquals(300, layoutParams.height);
        assertEquals(-1, layoutParams.column);
        assertEquals(1, layoutParams.span);
        ViewGroup.LayoutParams oldParams = layoutParams;
        layoutParams = new TableRow.LayoutParams(200, 300, 1.2f);
        assertEquals(200, layoutParams.width);
        assertEquals(300, layoutParams.height);
        assertEquals(1.2f, layoutParams.weight);
        assertEquals(-1, layoutParams.column);
        assertEquals(1, layoutParams.span);
        MarginLayoutParams oldMarginParams = layoutParams;
        layoutParams = new TableRow.LayoutParams();
        assertEquals(-1, layoutParams.column);
        assertEquals(1, layoutParams.span);
        layoutParams = new TableRow.LayoutParams(5);
        assertEquals(5, layoutParams.column);
        assertEquals(1, layoutParams.span);
        layoutParams = new TableRow.LayoutParams(oldParams);
        assertEquals(200, layoutParams.width);
        assertEquals(300, layoutParams.height);
        assertEquals(0, layoutParams.column);
        assertEquals(0, layoutParams.span);
        layoutParams = new TableRow.LayoutParams(oldMarginParams);
        assertEquals(200, layoutParams.width);
        assertEquals(300, layoutParams.height);
        assertEquals(0, layoutParams.column);
        assertEquals(0, layoutParams.span);
        TableStubActivity activity = getActivity();
        activity.setContentView(com.android.cts.stub.R.layout.table_layout_2);
        int idTable = com.android.cts.stub.R.id.table2;
        TableLayout tableLayout = (TableLayout) activity.findViewById(idTable);
        View vVitural1 = ((TableRow) tableLayout.getChildAt(0)).getVirtualChildAt(1);
        layoutParams = (TableRow.LayoutParams) vVitural1.getLayoutParams();
        assertEquals(1, layoutParams.column);
        View vVitural2 = ((TableRow) tableLayout.getChildAt(0)).getVirtualChildAt(2);
        layoutParams = (TableRow.LayoutParams) vVitural2.getLayoutParams();
        assertEquals(2, layoutParams.span);
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "Test setBaseAttributes(TypedArray a, int widthAttr, int heightAttr)",
        method = "setBaseAttributes",
        args = {android.content.res.TypedArray.class, int.class, int.class}
    )
    @ToBeFixed( bug = "1417734", explanation = "NullPointerException and " +
            "ArrayIndexOutOfBoundsException issue")
    public void testSetBaseAttributes() {
        MockTableRow_LayoutParams mockLayoutParams = new MockTableRow_LayoutParams(200, 300);
        assertEquals(200, mockLayoutParams.width);
        assertEquals(300, mockLayoutParams.height);
        AttributeSet attrs = getAttrs("base_attr_pixel");
        TypedArray a = mTargetContext.obtainStyledAttributes(attrs, R.styleable.ViewGroup_Layout);
        mockLayoutParams.setBaseAttributes(a, R.styleable.ViewGroup_Layout_layout_width,
                R.styleable.ViewGroup_Layout_layout_height);
        assertEquals(400, mockLayoutParams.width);
        assertEquals(600, mockLayoutParams.height);
        mockLayoutParams.setBaseAttributes(a, R.styleable.ViewGroup_Layout_layout_height,
                R.styleable.ViewGroup_Layout_layout_width);
        assertEquals(600, mockLayoutParams.width);
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
        assertEquals(TableLayout.LayoutParams.WRAP_CONTENT, mockLayoutParams.width);
        assertEquals(TableLayout.LayoutParams.MATCH_PARENT, mockLayoutParams.height);
        a.recycle();
        attrs = getAttrs("base_attr_noheight");
        a = mTargetContext.obtainStyledAttributes(attrs, R.styleable.ViewGroup_Layout);
        mockLayoutParams.setBaseAttributes(a, R.styleable.ViewGroup_Layout_layout_width,
                R.styleable.ViewGroup_Layout_layout_height);
        assertEquals(600, mockLayoutParams.width);
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
        try {
            mockLayoutParams.setBaseAttributes(a, -1,
                    R.styleable.ViewGroup_Layout_layout_height);
            fail("Should throw ArrayIndexOutOfBoundsException");
        } catch (ArrayIndexOutOfBoundsException e) {
        }
        try {
            mockLayoutParams.setBaseAttributes(null,
                    R.styleable.ViewGroup_Layout_layout_width, -1);
            fail("Should throw ArrayIndexOutOfBoundsException");
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
    private class MockTableRow_LayoutParams extends TableRow.LayoutParams {
        public MockTableRow_LayoutParams(int w, int h) {
            super(w, h);
        }
        @Override
        protected void setBaseAttributes(TypedArray a, int widthAttr,
                int heightAttr) {
            super.setBaseAttributes(a, widthAttr, heightAttr);
        }
    }
}
