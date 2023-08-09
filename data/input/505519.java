@TestTargetClass(ResourceCursorAdapter.class)
public class ResourceCursorAdapterTest extends InstrumentationTestCase {
    private ResourceCursorAdapter mResourceCursorAdapter;
    private Context mContext;
    private ViewGroup mParent;
    private Cursor mCursor;
    @Override
    protected void setUp() throws Exception {
        super.setUp();
        mResourceCursorAdapter = null;
        mContext = getInstrumentation().getTargetContext();
        LayoutInflater layoutInflater = (LayoutInflater) mContext.getSystemService(
                Context.LAYOUT_INFLATER_SERVICE);
        mParent = (ViewGroup) layoutInflater.inflate(R.layout.cursoradapter_host, null);
        mCursor = createTestCursor(3, 3);
    }
    @TestTargets({
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "ResourceCursorAdapter",
            args = {Context.class, int.class, Cursor.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "ResourceCursorAdapter",
            args = {Context.class, int.class, Cursor.class, boolean.class}
        )
    })
    public void testConstructor() {
        MockResourceCursorAdapter adapter = new MockResourceCursorAdapter(mContext, -1, null);
        assertTrue(adapter.isAutoRequery());
        assertNull(adapter.getCursor());
        adapter = new MockResourceCursorAdapter(mContext, R.layout.cursoradapter_item0, mCursor);
        assertTrue(adapter.isAutoRequery());
        assertSame(mCursor, adapter.getCursor());
        adapter = new MockResourceCursorAdapter(mContext,
                R.layout.cursoradapter_item0, mCursor, false);
        assertFalse(adapter.isAutoRequery());
        assertSame(mCursor, adapter.getCursor());
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        method = "setViewResource",
        args = {int.class}
    )
    public void testSetViewResource() {
        mResourceCursorAdapter = new MockResourceCursorAdapter(mContext,
                R.layout.cursoradapter_item0, mCursor);
        View result = mResourceCursorAdapter.newView(null, null, mParent);
        assertNotNull(result);
        assertEquals(R.id.cursorAdapter_item0, result.getId());
        mResourceCursorAdapter.setViewResource(R.layout.cursoradapter_item1);
        result = mResourceCursorAdapter.newView(null, null, mParent);
        assertNotNull(result);
        assertEquals(R.id.cursorAdapter_item1, result.getId());
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "Test {@link ResourceCursorAdapter#setDropDownViewResource(int)}",
        method = "setDropDownViewResource",
        args = {int.class}
    )
    public void testSetDropDownViewResource() {
        mResourceCursorAdapter = new MockResourceCursorAdapter(mContext,
                R.layout.cursoradapter_item0, mCursor);
        View result = mResourceCursorAdapter.newDropDownView(null, null, mParent);
        assertNotNull(result);
        assertEquals(R.id.cursorAdapter_item0, result.getId());
        mResourceCursorAdapter.setDropDownViewResource(R.layout.cursoradapter_item1);
        result = mResourceCursorAdapter.newDropDownView(null, null, mParent);
        assertNotNull(result);
        assertEquals(R.id.cursorAdapter_item1, result.getId());
        result = mResourceCursorAdapter.newView(null, null, mParent);
        assertNotNull(result);
        assertEquals(R.id.cursorAdapter_item0, result.getId());
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        method = "newDropDownView",
        args = {Context.class, Cursor.class, ViewGroup.class}
    )
    public void testNewDropDownView() {
        mResourceCursorAdapter = new MockResourceCursorAdapter(mContext,
                R.layout.cursoradapter_item0, mCursor);
        View result = mResourceCursorAdapter.newDropDownView(null, null, mParent);
        assertNotNull(result);
        assertEquals(R.id.cursorAdapter_item0, result.getId());
        mResourceCursorAdapter = new MockResourceCursorAdapter(mContext,
                R.layout.cursoradapter_item1, mCursor);
        result = mResourceCursorAdapter.newDropDownView(null, null, mParent);
        assertNotNull(result);
        assertEquals(R.id.cursorAdapter_item1, result.getId());
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        method = "newView",
        args = {Context.class, Cursor.class, ViewGroup.class}
    )
    public void testNewView() {
        mResourceCursorAdapter = new MockResourceCursorAdapter(mContext,
                R.layout.cursoradapter_item0, mCursor);
        View result = mResourceCursorAdapter.newView(null, null, mParent);
        assertNotNull(result);
        assertEquals(R.id.cursorAdapter_item0, result.getId());
        mResourceCursorAdapter = new MockResourceCursorAdapter(mContext,
                R.layout.cursoradapter_item1, mCursor);
        result = mResourceCursorAdapter.newView(null, null, mParent);
        assertNotNull(result);
        assertEquals(R.id.cursorAdapter_item1, result.getId());
    }
    @SuppressWarnings("unchecked")
    private Cursor createTestCursor(int colCount, int rowCount) {
        ArrayList<ArrayList> list = new ArrayList<ArrayList>();
        String[] columns = new String[colCount];
        for (int i = 0; i < colCount; i++) {
            columns[i] = "column" + i;
        }
        for (int i = 0; i < rowCount; i++) {
            ArrayList<String> row = new ArrayList<String>();
            for (int j = 0; j < colCount; j++) {
                row.add("" + i + "" + j);
            }
            list.add(row);
        }
        return new ArrayListCursor(columns, list);
    }
    private static class MockResourceCursorAdapter extends ResourceCursorAdapter {
        public MockResourceCursorAdapter(Context context, int layout, Cursor c) {
            super(context, layout, c);
        }
        public MockResourceCursorAdapter(Context context, int layout,
                Cursor c, boolean autoRequery) {
            super(context, layout, c, autoRequery);
        }
        @Override
        public void bindView(View view, Context context, Cursor cursor) {
        }
        public boolean isAutoRequery() {
            return mAutoRequery;
        }
    }
}
