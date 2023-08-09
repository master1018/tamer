@TestTargetClass(ArrayAdapter.class)
public class ArrayAdapterTest extends AndroidTestCase {
    private static final int INVALD_ID = -1;
    private static final String STR1 = "string1";
    private static final String STR2 = "string2";
    private static final String STR3 = "string3";
    private ArrayAdapter<String> mArrayAdapter;
    @Override
    protected void setUp() throws Exception {
        super.setUp();
          mArrayAdapter = new ArrayAdapter<String>(mContext, R.layout.simple_dropdown_item_1line);
    }
    @TestTargets({
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "ArrayAdapter",
            args = {android.content.Context.class, int.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "ArrayAdapter",
            args = {android.content.Context.class, int.class, int.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "ArrayAdapter",
            args = {android.content.Context.class, int.class, int.class, java.util.List.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "ArrayAdapter",
            args = {android.content.Context.class, int.class, java.util.List.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "ArrayAdapter",
            args = {android.content.Context.class, int.class, int.class, java.lang.Object[].class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "ArrayAdapter",
            args = {android.content.Context.class, int.class, java.lang.Object[].class}
        )
    })
    @ToBeFixed(bug = "1695243", explanation = "should add NullPointerException @throws"
        + " clause into javadoc.")
    public void testConstructor() {
        new ArrayAdapter<String>(mContext, R.layout.simple_dropdown_item_1line);
        new ArrayAdapter<String>(mContext, INVALD_ID);
        new ArrayAdapter<String>(mContext, R.layout.simple_dropdown_item_1line, R.id.text1);
        new ArrayAdapter<String>(mContext, R.layout.simple_dropdown_item_1line, INVALD_ID);
        new ArrayAdapter<String>(mContext, R.layout.simple_dropdown_item_1line,
                new String[] {"str1", "str2"});
        new ArrayAdapter<String>(mContext, R.layout.simple_dropdown_item_1line, R.id.text1,
                new String[] {"str1", "str2"});
        List<String> list = new ArrayList<String>();
        list.add(STR1);
        list.add(STR2);
        new ArrayAdapter<String>(mContext, R.layout.simple_dropdown_item_1line, list);
        new ArrayAdapter<String>(mContext, R.layout.simple_dropdown_item_1line, R.id.text1, list);
        try {
            new ArrayAdapter<String>(null, R.layout.simple_dropdown_item_1line);
            fail("should throw NullPointerException");
        } catch (NullPointerException e) {
        }
    }
    @TestTargets({
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "setNotifyOnChange",
            args = {boolean.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "notifyDataSetChanged",
            args = {}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "add",
            args = {java.lang.Object.class}
        ),
        @TestTargetNew(
                level = TestLevel.COMPLETE,
                method = "clear",
                args = {}
        )
    })
    public void testDataChangeEvent() {
        final MockDataSetObserver mockDataSetObserver = new MockDataSetObserver();
        mArrayAdapter.registerDataSetObserver(mockDataSetObserver);
        mArrayAdapter.setNotifyOnChange(true);
        assertEquals(0, mockDataSetObserver.getCalledOnChangedCount());
        mArrayAdapter.add(STR1);
        assertEquals(1, mArrayAdapter.getCount());
        assertEquals(1, mockDataSetObserver.getCalledOnChangedCount());
        mArrayAdapter.add(STR2);
        assertEquals(2, mArrayAdapter.getCount());
        assertEquals(2, mockDataSetObserver.getCalledOnChangedCount());
        mArrayAdapter.clear();
        assertEquals(3, mockDataSetObserver.getCalledOnChangedCount());
        assertEquals(0, mArrayAdapter.getCount());
        mArrayAdapter.clear();
        assertEquals(4, mockDataSetObserver.getCalledOnChangedCount());
        mockDataSetObserver.clearCount();
        assertEquals(0, mockDataSetObserver.getCalledOnChangedCount());
        mArrayAdapter.setNotifyOnChange(false);
        mArrayAdapter.add(STR3);
        assertEquals(1, mArrayAdapter.getCount());
        assertEquals(0, mockDataSetObserver.getCalledOnChangedCount());
        mArrayAdapter.notifyDataSetChanged();
        assertEquals(1, mockDataSetObserver.getCalledOnChangedCount());
        mArrayAdapter.notifyDataSetChanged();
        assertEquals(2, mockDataSetObserver.getCalledOnChangedCount());
        mArrayAdapter.add(STR3);
        assertEquals(3, mockDataSetObserver.getCalledOnChangedCount());
    }
    @TestTargets({
        @TestTargetNew(
                level = TestLevel.COMPLETE,
                method = "getContext",
                args = {}
            ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "getCount",
            args = {}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "getView",
            args = {int.class, android.view.View.class, android.view.ViewGroup.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "getDropDownView",
            args = {int.class, android.view.View.class, android.view.ViewGroup.class}
        )
    })
    @ToBeFixed(bug = "1695243", explanation = "should add NullPointerException @throws"
        + " clause into javadoc.")
    public void testAccessView() {
        final TextView textView = new TextView(mContext);
        textView.setText(STR3);
        assertNotNull(mArrayAdapter.getContext());
        assertEquals(0, mArrayAdapter.getCount());
        mArrayAdapter.add(STR1);
        mArrayAdapter.add(STR2);
        mArrayAdapter.add(STR3);
        assertEquals(3, mArrayAdapter.getCount());
        assertEquals(STR1, ((TextView) mArrayAdapter.getView(0, null, null)).getText());
        assertEquals(STR2, ((TextView) mArrayAdapter.getView(1, null, null)).getText());
        assertEquals(STR3, ((TextView) mArrayAdapter.getDropDownView(2, null, null)).getText());
        assertEquals(STR3, textView.getText());
        assertSame(textView, mArrayAdapter.getView(0, textView, null));
        assertSame(textView, mArrayAdapter.getDropDownView(0, textView, null));
        assertEquals(STR1, textView.getText());
        try {
            assertEquals(textView, mArrayAdapter.getView(-1, textView, null));
            fail("should throw IndexOutOfBoundsException");
        } catch (IndexOutOfBoundsException e) {
        }
        try {
            assertEquals(textView, mArrayAdapter.getDropDownView(-1, textView, null));
            fail("should throw IndexOutOfBoundsException");
        } catch (IndexOutOfBoundsException e) {
        }
        try {
            assertEquals(textView,
                    mArrayAdapter.getView(mArrayAdapter.getCount(), textView, null));
            fail("should throw IndexOutOfBoundsException");
        } catch (IndexOutOfBoundsException e) {
        }
        try {
            assertEquals(textView,
                    mArrayAdapter.getDropDownView(mArrayAdapter.getCount(), textView, null));
            fail("should throw IndexOutOfBoundsException");
        } catch (IndexOutOfBoundsException e) {
        }
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        method = "getFilter",
        args = {}
    )
    @ToBeFixed(bug = "", explanation = "Can not check the filter's filting result.")
    public void testGetFilter() {
        Filter filter = mArrayAdapter.getFilter();
        assertNotNull(mArrayAdapter.getFilter());
        assertSame(filter, mArrayAdapter.getFilter());
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        method = "setDropDownViewResource",
        args = {int.class}
    )
    public void testSetDropDownViewResouce() {
        mArrayAdapter.add(STR1);
        mArrayAdapter.getDropDownView(0, null, null);
        mArrayAdapter.setDropDownViewResource(R.layout.tabhost_layout);
        mArrayAdapter.getView(0, null, null);
        try {
            mArrayAdapter.getDropDownView(0, null, null);
            fail("should throw IllegalStateException");
        } catch (IllegalStateException e) {
        }
        mArrayAdapter.setDropDownViewResource(INVALD_ID);
    }
    @TestTargets({
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "insert",
            args = {java.lang.Object.class, int.class}
        )
    })
    public void testInsert() {
        mArrayAdapter.setNotifyOnChange(true);
        final MockDataSetObserver mockDataSetObserver = new MockDataSetObserver();
        mArrayAdapter.registerDataSetObserver(mockDataSetObserver);
        mArrayAdapter.insert(STR1, 0);
        assertEquals(1, mArrayAdapter.getCount());
        assertEquals(0, mArrayAdapter.getPosition(STR1));
        assertEquals(1, mockDataSetObserver.getCalledOnChangedCount());
        mArrayAdapter.insert(STR2, 0);
        assertEquals(2, mArrayAdapter.getCount());
        assertEquals(1, mArrayAdapter.getPosition(STR1));
        assertEquals(0, mArrayAdapter.getPosition(STR2));
        mArrayAdapter.insert(STR3, mArrayAdapter.getCount());
        assertEquals(mArrayAdapter.getCount() - 1, mArrayAdapter.getPosition(STR3));
        mArrayAdapter.insert(null, 0);
        assertEquals(0, mArrayAdapter.getPosition(null));
        try {
            mArrayAdapter.insert(STR1, -1);
            fail("should throw IndexOutOfBoundsException");
        } catch (IndexOutOfBoundsException e) {
        }
        try {
            mArrayAdapter.insert(STR1, mArrayAdapter.getCount() + 1);
            fail("should throw IndexOutOfBoundsException");
        } catch (IndexOutOfBoundsException e) {
        }
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        method = "getItem",
        args = {int.class}
    )
    public void testGetItem() {
        mArrayAdapter.add(STR1);
        mArrayAdapter.add(STR2);
        mArrayAdapter.add(STR3);
        assertSame(STR1, mArrayAdapter.getItem(0));
        assertSame(STR2, mArrayAdapter.getItem(1));
        assertSame(STR3, mArrayAdapter.getItem(2));
        try {
            mArrayAdapter.getItem(-1);
            fail("should throw IndexOutOfBoundsException");
        } catch (IndexOutOfBoundsException e) {
        }
        try {
            mArrayAdapter.getItem(mArrayAdapter.getCount());
            fail("should throw IndexOutOfBoundsException");
        } catch (IndexOutOfBoundsException e) {
        }
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        method = "getItemId",
        args = {int.class}
    )
    public void testGetItemId() {
        mArrayAdapter.add(STR1);
        mArrayAdapter.add(STR2);
        mArrayAdapter.add(STR3);
        assertEquals(0, mArrayAdapter.getItemId(0));
        assertEquals(1, mArrayAdapter.getItemId(1));
        assertEquals(2, mArrayAdapter.getItemId(2));
        assertEquals(-1, mArrayAdapter.getItemId(-1));
        assertEquals(mArrayAdapter.getCount(),
                mArrayAdapter.getItemId(mArrayAdapter.getCount()));
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        method = "getPosition",
        args = {java.lang.Object.class}
    )
    public void testGetPosition() {
        mArrayAdapter.add(STR1);
        mArrayAdapter.add(STR2);
        mArrayAdapter.add(STR1);
        assertEquals(0, mArrayAdapter.getPosition(STR1));
        assertEquals(1, mArrayAdapter.getPosition(STR2));
        assertEquals(0, mArrayAdapter.getPosition(STR1));
        assertEquals(-1, mArrayAdapter.getPosition(STR3));
        assertEquals(-1, mArrayAdapter.getPosition(null));
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        method = "remove",
        args = {java.lang.Object.class}
    )
    public void testRemove() {
        final MockDataSetObserver mockDataSetObserver = new MockDataSetObserver();
        mArrayAdapter.registerDataSetObserver(mockDataSetObserver);
        mArrayAdapter.setNotifyOnChange(true);
        assertEquals(0, mArrayAdapter.getCount());
        assertEquals(0, mockDataSetObserver.getCalledOnChangedCount());
        mArrayAdapter.remove(STR1);
        assertEquals(1, mockDataSetObserver.getCalledOnChangedCount());
        mArrayAdapter.add(STR1);
        mArrayAdapter.add(STR2);
        mArrayAdapter.add(STR3);
        mArrayAdapter.add(STR2);
        mockDataSetObserver.clearCount();
        assertEquals(0, mockDataSetObserver.getCalledOnChangedCount());
        assertEquals(4, mArrayAdapter.getCount());
        mArrayAdapter.remove(STR1);
        assertEquals(3, mArrayAdapter.getCount());
        assertEquals(-1, mArrayAdapter.getPosition(STR1));
        assertEquals(0, mArrayAdapter.getPosition(STR2));
        assertEquals(1, mArrayAdapter.getPosition(STR3));
        assertEquals(1, mockDataSetObserver.getCalledOnChangedCount());
        mArrayAdapter.remove(STR2);
        assertEquals(2, mArrayAdapter.getCount());
        assertEquals(1, mArrayAdapter.getPosition(STR2));
        assertEquals(0, mArrayAdapter.getPosition(STR3));
        mArrayAdapter.remove(STR2);
        assertEquals(1, mArrayAdapter.getCount());
        assertEquals(-1, mArrayAdapter.getPosition(STR2));
        assertEquals(0, mArrayAdapter.getPosition(STR3));
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        method = "createFromResource",
        args = {android.content.Context.class, int.class, int.class}
    )
    @ToBeFixed(bug = "1695243", explanation = "should add NullPointerException @throws"
        + " clause into javadoc.")
    public void testCreateFromResource() {
        ArrayAdapter.createFromResource(mContext, R.array.string, R.layout.simple_spinner_item);
        try {
            ArrayAdapter.createFromResource(null, R.array.string, R.layout.simple_spinner_item);
            fail("should throw NullPointerException");
        } catch (NullPointerException e) {
        }
        try {
            ArrayAdapter.createFromResource(mContext, INVALD_ID, R.layout.simple_spinner_item);
            fail("should throw NullPointerException");
        } catch (NullPointerException e) {
        }
       ArrayAdapter.createFromResource(mContext, R.array.string, INVALD_ID);
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        method = "createFromResource",
        args = {android.content.Context.class, int.class, int.class}
    )
    public void testSort() {
        final MockDataSetObserver mockDataSetObserver = new MockDataSetObserver();
        mArrayAdapter.registerDataSetObserver(mockDataSetObserver);
        mArrayAdapter.setNotifyOnChange(true);
        assertEquals(0, mockDataSetObserver.getCalledOnChangedCount());
        mArrayAdapter.sort( new Comparator<String>() {
            public int compare(String o1, String o2) {
                return 0;
            }
        });
        assertEquals(1, mockDataSetObserver.getCalledOnChangedCount());
        mArrayAdapter.sort(null);
        assertEquals(2, mockDataSetObserver.getCalledOnChangedCount());
    }
    private static class MockDataSetObserver extends DataSetObserver {
        private int mCalledOnChangedCount;
        private int mOnCalledInvalidatedCount;
        public MockDataSetObserver() {
            clearCount();
        }
        public int getCalledOnChangedCount() {
            return mCalledOnChangedCount;
        }
        public int getCalledOnInvalidatedCount() {
            return mOnCalledInvalidatedCount;
        }
        public void clearCount() {
            mCalledOnChangedCount = 0;
            mOnCalledInvalidatedCount = 0;
        }
        public void onChanged() {
            mCalledOnChangedCount++;
        }
        public void onInvalidated() {
            mOnCalledInvalidatedCount++;
        }
    }
}
