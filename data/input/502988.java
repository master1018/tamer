public class SimpleCursorAdapterTest extends AndroidTestCase {
    String[] mFrom;
    int[] mTo;
    int mLayout;
    Context mContext;
    ArrayList<ArrayList> mData2x2;
    Cursor mCursor2x2;
    @Override
    public void setUp() throws Exception {
        super.setUp();
        mFrom = new String[]{"Column1", "Column2"};
        mTo = new int[]{com.android.internal.R.id.text1, com.android.internal.R.id.text2};
        mLayout = com.android.internal.R.layout.simple_list_item_2;
        mContext = getContext();
        mData2x2 = createTestList(2, 2);
        mCursor2x2 = new ArrayListCursor(mFrom, mData2x2);
    }
    private ArrayList<ArrayList> createTestList(int rows, int cols) {
        ArrayList<ArrayList> list = Lists.newArrayList();
        Random generator = new Random();
        for (int i = 0; i < rows; i++) {
            ArrayList<Integer> col = Lists.newArrayList();
            list.add(col);
            for (int j = 0; j < cols; j++) {
                Integer r = generator.nextInt();
                col.add(r);
            }
        }
        return list;
    }
    @SmallTest
    public void testCreateLive() {
        SimpleCursorAdapter ca = new SimpleCursorAdapter(mContext, mLayout, mCursor2x2, mFrom, mTo);
        assertEquals(2, ca.getCount());
    }
    @SmallTest
    public void testCreateNull() {
        SimpleCursorAdapter ca = new SimpleCursorAdapter(mContext, mLayout, null, mFrom, mTo);
        assertEquals(0, ca.getCount());
    }
    @SmallTest
    public void testChangeCursorLive() {
        SimpleCursorAdapter ca = new SimpleCursorAdapter(mContext, mLayout, mCursor2x2, mFrom, mTo);
        assertEquals(2, ca.getCount());
        ArrayList<ArrayList> data2 = createTestList(5, 2);
        Cursor c2 = new ArrayListCursor(mFrom, data2);
        ca.changeCursor(c2);
        assertEquals(5, ca.getCount());
    }
    @SmallTest
    public void testChangeCursorNull() {
        SimpleCursorAdapter ca = new SimpleCursorAdapter(mContext, mLayout, mCursor2x2, mFrom, mTo);
        assertEquals(2, ca.getCount());
        ca.changeCursor(null);
        assertEquals(0, ca.getCount());
    }
    @SmallTest
    public void testChangeCursorColumns() {
        TestSimpleCursorAdapter ca = new TestSimpleCursorAdapter(mContext, mLayout, mCursor2x2, 
                mFrom, mTo);
        int[] columns = ca.getConvertedFrom();
        assertEquals(columns[0], 0);
        assertEquals(columns[1], 1);
        String[] swappedFrom = new String[]{"Column2", "Column1"};
        Cursor c2 = new ArrayListCursor(swappedFrom, mData2x2);
        ca.changeCursor(c2);
        assertEquals(2, ca.getCount());
        columns = ca.getConvertedFrom();
        assertEquals(columns[0], 1);
        assertEquals(columns[1], 0);
    }
    @SmallTest
    public void testNullConstructor() {
        SimpleCursorAdapter ca = new SimpleCursorAdapter(mContext, mLayout, null, null, null);
        assertEquals(0, ca.getCount());
    }
    @SmallTest
    public void testChangeNullToMapped() {
        TestSimpleCursorAdapter ca = new TestSimpleCursorAdapter(mContext, mLayout, null, null, null);
        assertEquals(0, ca.getCount());
        ca.changeCursorAndColumns(mCursor2x2, mFrom, mTo);
        assertEquals(2, ca.getCount());
        int[] columns = ca.getConvertedFrom();
        assertEquals(2, columns.length);
        assertEquals(0, columns[0]);
        assertEquals(1, columns[1]);
        int[] viewIds = ca.getTo();
        assertEquals(2, viewIds.length);
        assertEquals(com.android.internal.R.id.text1, viewIds[0]);
        assertEquals(com.android.internal.R.id.text2, viewIds[1]);
    }
    @SmallTest
    public void testChangeMapping() {
        TestSimpleCursorAdapter ca = new TestSimpleCursorAdapter(mContext, mLayout, mCursor2x2, 
                mFrom, mTo);
        assertEquals(2, ca.getCount());
        String[] singleFrom = new String[]{"Column1"};
        int[] singleTo = new int[]{com.android.internal.R.id.text1};
        ca.changeCursorAndColumns(mCursor2x2, singleFrom, singleTo);
        int[] columns = ca.getConvertedFrom();
        assertEquals(1, columns.length);
        assertEquals(0, columns[0]);
        int[] viewIds = ca.getTo();
        assertEquals(1, viewIds.length);
        assertEquals(com.android.internal.R.id.text1, viewIds[0]);
        singleFrom = new String[]{"Column2"};
        singleTo = new int[]{com.android.internal.R.id.text2};
        ca.changeCursorAndColumns(mCursor2x2, singleFrom, singleTo);
        columns = ca.getConvertedFrom();
        assertEquals(1, columns.length);
        assertEquals(1, columns[0]);
        viewIds = ca.getTo();
        assertEquals(1, viewIds.length);
        assertEquals(com.android.internal.R.id.text2, viewIds[0]);
    }
    private static class TestSimpleCursorAdapter extends SimpleCursorAdapter {
        public TestSimpleCursorAdapter(Context context, int layout, Cursor c,
                String[] from, int[] to) {
            super(context, layout, c, from, to);
        }
        int[] getConvertedFrom() {
            return mFrom;
        }
        int[] getTo() {
            return mTo;
        }
    }
}
