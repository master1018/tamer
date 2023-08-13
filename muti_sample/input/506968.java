@TestTargetClass(SimpleAdapter.class)
public class SimpleAdapterTest extends InstrumentationTestCase {
    private static final int DEFAULT_ROW_COUNT = 20;
    private static final int DEFAULT_COLUMN_COUNT = 2;
    private static final int[] VIEWS_TO = new int[] { R.id.text1 };
    private static final String[] COLUMNS_FROM = new String[] { "column1" };
    private SimpleAdapter mSimpleAdapter;
    private Context mContext;
    private LinearLayout mAdapterHost;
    private LayoutInflater mInflater;
    @Override
    protected void setUp() throws Exception {
        super.setUp();
        mContext = getInstrumentation().getTargetContext();
        mInflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        mAdapterHost = (LinearLayout) mInflater.inflate(
                com.android.cts.stub.R.layout.cursoradapter_host, null);
        mSimpleAdapter = new SimpleAdapter(mContext,
                createTestList(DEFAULT_COLUMN_COUNT, DEFAULT_ROW_COUNT),
                R.layout.simple_list_item_1, COLUMNS_FROM, VIEWS_TO);
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "Test constructor",
        method = "SimpleAdapter",
        args = {android.content.Context.class, java.util.List.class, int.class,
                java.lang.String[].class, int[].class}
    )
    public void testConstructor() {
        new SimpleAdapter(mContext, createTestList(DEFAULT_COLUMN_COUNT, DEFAULT_ROW_COUNT),
                R.layout.simple_list_item_1, COLUMNS_FROM, VIEWS_TO);
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "Test {@link SimpleAdapter#getCount()}",
        method = "getCount",
        args = {}
    )
    public void testGetCount() {
        mSimpleAdapter = new SimpleAdapter(mContext,
                createTestList(DEFAULT_COLUMN_COUNT, DEFAULT_ROW_COUNT),
                R.layout.simple_list_item_1, COLUMNS_FROM, VIEWS_TO);
        assertEquals(20, mSimpleAdapter.getCount());
        mSimpleAdapter = new SimpleAdapter(mContext, createTestList(DEFAULT_COLUMN_COUNT, 10),
                R.layout.simple_list_item_1, COLUMNS_FROM, VIEWS_TO);
        assertEquals(10, mSimpleAdapter.getCount());
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "Test {@link SimpleAdapter#getItem(int)}",
        method = "getItem",
        args = {int.class}
    )
    @ToBeFixed(bug = "1417734", explanation = "should add @throws clause into javadoc of "
            + "SimpleAdapter#getItem(int) if the param position is out of index")
    public void testGetItem() {
        assertEquals("01", ((Map<?, ?>) mSimpleAdapter.getItem(0)).get("column1"));
        assertEquals("191", ((Map<?, ?>) mSimpleAdapter.getItem(19)).get("column1"));
        try {
            mSimpleAdapter.getItem(-1);
            fail("Should throw IndexOutOfBoundsException if index is negative");
        } catch (IndexOutOfBoundsException e) {
        }
        try {
            mSimpleAdapter.getItem(20);
            fail("Should throw IndexOutOfBoundsException if index is beyond the list's size");
        } catch (IndexOutOfBoundsException e) {
        }
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "Test {@link SimpleAdapter#getItemId(int)}",
        method = "getItemId",
        args = {int.class}
    )
    @ToBeFixed(explanation = "SimpleAdapter#getItemId(int) should check whether "
            + "the param position is out of index")
    public void testGetItemId() {
        assertEquals(0, mSimpleAdapter.getItemId(0));
        assertEquals(19, mSimpleAdapter.getItemId(19));
        assertEquals(-1, mSimpleAdapter.getItemId(-1));
        assertEquals(20, mSimpleAdapter.getItemId(20));
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "Test {@link SimpleAdapter#getView(int, View, ViewGroup)}",
        method = "getView",
        args = {int.class, android.view.View.class, android.view.ViewGroup.class}
    )
    @ToBeFixed(bug = "1417734", explanation = "should add @throws clause into javadoc of "
            + "SimpleAdapter#getView(int, View, ViewGroup) if the param position is out of index")
    public void testGetView() {
        View result = mSimpleAdapter.getView(0, null, mAdapterHost);
        assertTrue(result instanceof TextView);
        assertEquals("01", ((TextView) result).getText().toString());
        result = mSimpleAdapter.getView(19, null, mAdapterHost);
        assertTrue(result instanceof TextView);
        assertEquals("191", ((TextView) result).getText().toString());
        TextView convertView = (TextView) result;
        result = mSimpleAdapter.getView(0, convertView, mAdapterHost);
        assertEquals("01", ((TextView) result).getText().toString());
        assertSame(convertView, result);
        result = mSimpleAdapter.getView(10, convertView, null);
        assertEquals("101", ((TextView) result).getText().toString());
        MockViewBinder binder = new MockViewBinder(true);
        mSimpleAdapter.setViewBinder(binder);
        binder.reset();
        mSimpleAdapter.getView(0, null, mAdapterHost);
        assertTrue(binder.hasCalledSetViewValue());
        binder = new MockViewBinder(false);
        mSimpleAdapter.setViewBinder(binder);
        binder.reset();
        result = mSimpleAdapter.getView(0, null, mAdapterHost);
        assertTrue(binder.hasCalledSetViewValue());
        assertEquals("01", ((TextView) result).getText().toString());
        try {
            mSimpleAdapter.getView(-1, convertView, null);
            fail("Should throw IndexOutOfBoundsException if index is negative");
        } catch (IndexOutOfBoundsException e) {
        }
        try {
            mSimpleAdapter.getView(20, convertView, null);
            fail("Should throw IndexOutOfBoundsException if index is beyond the list's size");
        } catch (IndexOutOfBoundsException e) {
        }
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "Test {@link SimpleAdapter#setDropDownViewResource(int)}",
        method = "setDropDownViewResource",
        args = {int.class}
    )
    public void testSetDropDownViewResource() {
        mSimpleAdapter.setDropDownViewResource(R.layout.simple_list_item_2);
        View result = mSimpleAdapter.getDropDownView(0, null, mAdapterHost);
        assertTrue(result instanceof TwoLineListItem);
        assertEquals("01", ((TextView) result.findViewById(R.id.text1)).getText().toString());
        result = mSimpleAdapter.getDropDownView(19, null, mAdapterHost);
        assertTrue(result instanceof TwoLineListItem);
        assertEquals("191", ((TextView) result.findViewById(R.id.text1)).getText().toString());
        mSimpleAdapter.setDropDownViewResource(R.layout.simple_list_item_1);
        result = mSimpleAdapter.getDropDownView(0, null, mAdapterHost);
        assertTrue(result instanceof TextView);
        assertEquals("01", ((TextView) result).getText().toString());
        result = mSimpleAdapter.getDropDownView(19, null, mAdapterHost);
        assertTrue(result instanceof TextView);
        assertEquals("191", ((TextView) result).getText().toString());
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "Test {@link SimpleAdapter#getDropDownView(int, View, ViewGroup)}",
        method = "getDropDownView",
        args = {int.class, android.view.View.class, android.view.ViewGroup.class}
    )
    @ToBeFixed(bug = "1417734", explanation = "should add @throws clause into j"
            + "avadoc of SimpleAdapter#getDropDownView(int, View, ViewGroup) if "
            + "the param position is out of index")
    public void testGetDropDownView() {
        View result = mSimpleAdapter.getDropDownView(0, null, mAdapterHost);
        assertTrue(result instanceof TextView);
        assertEquals("01", ((TextView) result).getText().toString());
        result = mSimpleAdapter.getDropDownView(19, null, mAdapterHost);
        assertTrue(result instanceof TextView);
        assertEquals("191", ((TextView) result).getText().toString());
        TextView convertView = (TextView) result;
        result = mSimpleAdapter.getDropDownView(0, convertView, mAdapterHost);
        assertEquals("01", convertView.getText().toString());
        assertSame(convertView, result);
        result = mSimpleAdapter.getDropDownView(10, convertView, null);
        assertEquals("101", ((TextView) result).getText().toString());
        MockViewBinder binder = new MockViewBinder(true);
        mSimpleAdapter.setViewBinder(binder);
        binder.reset();
        mSimpleAdapter.getDropDownView(19, null, mAdapterHost);
        assertTrue(binder.hasCalledSetViewValue());
        binder = new MockViewBinder(false);
        mSimpleAdapter.setViewBinder(binder);
        binder.reset();
        result = mSimpleAdapter.getDropDownView(19, null, mAdapterHost);
        assertTrue(binder.hasCalledSetViewValue());
        assertEquals("191", ((TextView)result).getText().toString());
        try {
            mSimpleAdapter.getDropDownView(-1, convertView, null);
            fail("Should throw IndexOutOfBoundsException if index is negative");
        } catch (IndexOutOfBoundsException e) {
        }
        try {
            mSimpleAdapter.getDropDownView(20, convertView, null);
            fail("Should throw IndexOutOfBoundsException if index is beyond the list's size");
        } catch (IndexOutOfBoundsException e) {
        }
    }
    @TestTargets({
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "setViewBinder",
            args = {android.widget.SimpleAdapter.ViewBinder.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "getViewBinder",
            args = {}
        )
    })
    public void testAccessViewBinder() {
        assertNull(mSimpleAdapter.getViewBinder());
        MockViewBinder binder = new MockViewBinder(true);
        mSimpleAdapter.setViewBinder(binder);
        assertSame(binder, mSimpleAdapter.getViewBinder());
        binder = new MockViewBinder(false);
        mSimpleAdapter.setViewBinder(binder);
        assertSame(binder, mSimpleAdapter.getViewBinder());
        mSimpleAdapter.setViewBinder(null);
        assertNull(mSimpleAdapter.getViewBinder());
    }
    @TestTargets({
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            notes = "Test {@link SimpleAdapter#setViewImage(ImageView, String)",
            method = "setViewImage",
            args = {android.widget.ImageView.class, java.lang.String.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            notes = "Test {@link SimpleAdapter#setViewImage(ImageView, String)",
            method = "setViewImage",
            args = {android.widget.ImageView.class, int.class}
        )
    })
    @ToBeFixed(bug = "1417734", explanation = "should add @throws clause into javadoc of "
            + "SimpleAdapter#setViewImage(ImageView, String) if the param String is null")
    public void testSetViewImage() {
        ImageView view = new ImageView(mContext);
        assertNull(view.getDrawable());
        mSimpleAdapter.setViewImage(view, String.valueOf(com.android.cts.stub.R.drawable.scenery));
        BitmapDrawable d = (BitmapDrawable) mContext.getResources().getDrawable(
                com.android.cts.stub.R.drawable.scenery);
        WidgetTestUtils.assertEquals(d.getBitmap(),
                ((BitmapDrawable) view.getDrawable()).getBitmap());
        view = new ImageView(mContext);
        assertNull(view.getDrawable());
        mSimpleAdapter.setViewImage(view, "");
        assertNull(view.getDrawable());
        view = new ImageView(mContext);
        assertNull(view.getDrawable());
        try {
            mSimpleAdapter.setViewImage(view, null);
            fail("Should throw NullPointerException if the uri or value is null");
        } catch (NullPointerException e) {
        }
        view = new ImageView(mContext);
        assertNull(view.getDrawable());
        mSimpleAdapter.setViewImage(view, com.android.cts.stub.R.drawable.scenery);
        d = (BitmapDrawable) mContext.getResources()
                .getDrawable(com.android.cts.stub.R.drawable.scenery);
        WidgetTestUtils.assertEquals(d.getBitmap(),
                ((BitmapDrawable) view.getDrawable()).getBitmap());
        view = new ImageView(mContext);
        assertNull(view.getDrawable());
        mSimpleAdapter.setViewImage(view, Integer.MAX_VALUE);
        assertNull(view.getDrawable());
        view = new ImageView(mContext);
        assertNull(view.getDrawable());
        try {
            mSimpleAdapter.setViewImage(view, SimpleCursorAdapterTest.createTestImage(mContext,
                    "testimage", com.android.cts.stub.R.raw.testimage));
            assertNotNull(view.getDrawable());
            Bitmap testBitmap = WidgetTestUtils.getUnscaledBitmap(mContext.getResources(),
                    com.android.cts.stub.R.raw.testimage);
            WidgetTestUtils.assertEquals(testBitmap,
                    ((BitmapDrawable) view.getDrawable()).getBitmap());
        } finally {
            SimpleCursorAdapterTest.destroyTestImage(mContext,"testimage");
        }
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "Test {@link SimpleAdapter#setViewText(TextView, String)}",
        method = "setViewText",
        args = {android.widget.TextView.class, java.lang.String.class}
    )
    public void testSetViewText() {
        TextView view = new TextView(mContext);
        mSimpleAdapter.setViewText(view, "expected");
        assertEquals("expected", view.getText().toString());
        mSimpleAdapter.setViewText(view, null);
        assertEquals("", view.getText().toString());
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "Test {@link SimpleAdapter#getFilter()}",
        method = "getFilter",
        args = {}
    )
    public void testGetFilter() {
        assertNotNull(mSimpleAdapter.getFilter());
    }
    private ArrayList<HashMap<String, String>> createTestList(int colCount, int rowCount) {
        ArrayList<HashMap<String, String>> list = new ArrayList<HashMap<String, String>>();
        String[] columns = new String[colCount];
        for (int i = 0; i < colCount; i++) {
            columns[i] = "column" + i;
        }
        for (int i = 0; i < rowCount; i++) {
            HashMap<String, String> row = new HashMap<String, String>();
            for (int j = 0; j < colCount; j++) {
                row.put(columns[j], "" + i + "" + j);
            }
            list.add(row);
        }
        return list;
    }
    private class MockViewBinder implements ViewBinder {
        private boolean mExpectedResult;
        private boolean mHasCalledSetViewValue;
        public MockViewBinder(boolean expectedResult) {
            mExpectedResult = expectedResult;
        }
        public void reset(){
            mHasCalledSetViewValue = false;
        }
        public boolean hasCalledSetViewValue() {
            return mHasCalledSetViewValue;
        }
        public boolean setViewValue(View view, Object data, String textRepresentation) {
            mHasCalledSetViewValue = true;
            return mExpectedResult;
        }
    }
}
