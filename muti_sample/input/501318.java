@TestTargetClass(AdapterView.class)
public class AdapterViewTest extends ActivityInstrumentationTestCase2<AdapterViewStubActivity> {
    private final static int INVALID_ID = -1;
    private final static int LAYOUT_WIDTH = 200;
    private final static int LAYOUT_HEIGHT = 200;
    final String[] FRUIT = { "1", "2", "3", "4", "5", "6", "7", "8" };
    private Activity mActivity;
    private AdapterView<ListAdapter> mAdapterView;
    public AdapterViewTest() {
        super("com.android.cts.stub", AdapterViewStubActivity.class);
    }
    @Override
    protected void setUp() throws Exception {
        super.setUp();
        mActivity = getActivity();
        mAdapterView = new ListView(mActivity);
    }
    @TestTargets({
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "AdapterView",
            args = {android.content.Context.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "AdapterView",
            args = {android.content.Context.class, android.util.AttributeSet.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "AdapterView",
            args = {android.content.Context.class, android.util.AttributeSet.class, int.class}
        )
    })
    public void testConstructor() {
        XmlPullParser parser = mActivity.getResources().getXml(R.layout.adapterview_layout);
        AttributeSet attrs = Xml.asAttributeSet(parser);
        new MockAdapterView(mActivity);
        new MockAdapterView(mActivity, attrs);
        new MockAdapterView(mActivity, attrs, 0);
        try {
            new MockAdapterView(null);
            fail("Should throw NullPointerException");
        } catch (NullPointerException e) {
        }
        new MockAdapterView(mActivity, null, INVALID_ID);
    }
    @TestTargets({
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "addView",
            args = {android.view.View.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "addView",
            args = {android.view.View.class, int.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "addView",
            args = {android.view.View.class, int.class, android.view.ViewGroup.LayoutParams.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "addView",
            args = {android.view.View.class, android.view.ViewGroup.LayoutParams.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "removeView",
            args = {android.view.View.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "removeAllViews",
            args = {}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "removeViewAt",
            args = {int.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "setOnClickListener",
            args = {android.view.View.OnClickListener.class}
        )
    })
    public void testUnsupportedMethods() {
        ListView subView = new ListView(mActivity);
        try {
            mAdapterView.addView(subView);
            fail("addView(View) is not supported in AdapterView.");
        } catch (UnsupportedOperationException e) {
        }
        try {
            mAdapterView.addView(subView, 0);
            fail("addView(View, int) is not supported in AdapterView.");
        } catch (UnsupportedOperationException e) {
        }
        try {
            mAdapterView.addView(subView, (ViewGroup.LayoutParams) null);
            fail("addView(View, ViewGroup.LayoutParams) is not supported in AdapterView.");
        } catch (UnsupportedOperationException e) {
        }
        try {
            mAdapterView.addView(subView, 0, (ViewGroup.LayoutParams) null);
            fail("addView(View, int, ViewGroup.LayoutParams) is not supported in AdapterView.");
        } catch (UnsupportedOperationException e) {
        }
        try {
            mAdapterView.removeViewAt(0);
            fail("removeViewAt(int) is not supported in AdapterView");
        } catch (UnsupportedOperationException e) {
        }
        try {
            mAdapterView.removeAllViews();
            fail("removeAllViews() is not supported in AdapterView");
        } catch (UnsupportedOperationException e) {
        }
        try {
            mAdapterView.removeView(subView);
            fail("removeView(View) is not supported in AdapterView");
        } catch (UnsupportedOperationException e) {
        }
        try {
            mAdapterView.setOnClickListener(new android.view.View.OnClickListener() {
                public void onClick(View v) {
                }
            });
            fail("function setOnClickListener(android.view.View.OnClickListener) "
                    + "should throw out runtime exception");
        } catch (RuntimeException e) {
        }
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        method = "getCount",
        args = {}
    )
    public void testGetCount() {
        assertEquals(0, mAdapterView.getCount());
        setArrayAdapter(mAdapterView);
        assertEquals(FRUIT.length, mAdapterView.getCount());
    }
    @TestTargets({
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "getEmptyView",
            args = {}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "setEmptyView",
            args = {android.view.View.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "setAdapter",
            args = {java.lang.Object.class}
        )
    })
    public void testAccessEmptyView() {
        ImageView emptyView = new ImageView(mActivity);
        assertEquals(null, mAdapterView.getEmptyView());
        mAdapterView.setAdapter(new ArrayAdapter<String>(
                mActivity, R.layout.adapterview_layout, new String[]{}));
        emptyView.setVisibility(View.INVISIBLE);
        assertEquals(View.INVISIBLE, emptyView.getVisibility());
        mAdapterView.setEmptyView(emptyView);
        assertSame(emptyView, mAdapterView.getEmptyView());
        assertEquals(View.VISIBLE, emptyView.getVisibility());
        setArrayAdapter(mAdapterView);
        emptyView = new ImageView(mActivity);
        assertEquals(View.VISIBLE, emptyView.getVisibility());
        mAdapterView.setEmptyView(emptyView);
        assertEquals(emptyView, mAdapterView.getEmptyView());
        assertEquals(View.GONE, emptyView.getVisibility());
        mAdapterView.setAdapter(null);
        emptyView = new ImageView(mActivity);
        emptyView.setVisibility(View.INVISIBLE);
        assertEquals(View.INVISIBLE, emptyView.getVisibility());
        mAdapterView.setEmptyView(emptyView);
        assertEquals(emptyView, mAdapterView.getEmptyView());
        assertEquals(View.VISIBLE, emptyView.getVisibility());
    }
    @TestTargets({
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "getFirstVisiblePosition",
            args = {}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "getLastVisiblePosition",
            args = {}
        )
    })
    public void testAccessVisiblePosition() {
        assertEquals(0, mAdapterView.getFirstVisiblePosition());
        assertEquals(-1, mAdapterView.getLastVisiblePosition());
        setArrayAdapter(mAdapterView);
        mAdapterView.layout(0, 0, LAYOUT_WIDTH, LAYOUT_HEIGHT+50);
        assertEquals(FRUIT.length - 1, mAdapterView.getLastVisiblePosition());
    }
    @TestTargets({
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "getItemAtPosition",
            args = {int.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "getItemIdAtPosition",
            args = {int.class}
        )
    })
    @ToBeFixed(bug = "1695243", explanation = "Android API javadocs are incomplete")
    public void testItemOrItemIdAtPosition() {
        assertNull(mAdapterView.getItemAtPosition(0));
        assertEquals(AdapterView.INVALID_ROW_ID, mAdapterView.getItemIdAtPosition(1));
        setArrayAdapter(mAdapterView);
        int count = mAdapterView.getAdapter().getCount();
        for (int i = 0; i < count; i++) {
            assertEquals(FRUIT[i], mAdapterView.getItemAtPosition(i));
        }
        assertNull(mAdapterView.getItemAtPosition(-1));
        try {
            mAdapterView.getItemAtPosition(FRUIT.length);
            fail("should throw IndexOutOfBoundsException");
        } catch (IndexOutOfBoundsException e) {
        }
        for (int i = 0; i < count; i++) {
            assertEquals(i, mAdapterView.getItemIdAtPosition(i));
        }
        assertEquals(AdapterView.INVALID_ROW_ID, mAdapterView.getItemIdAtPosition(-1));
        assertEquals(FRUIT.length, mAdapterView.getItemIdAtPosition(FRUIT.length));
    }
    @TestTargets({
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "getOnItemClickListener",
            args = {}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "setOnItemClickListener",
            args = {android.widget.AdapterView.OnItemClickListener.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "getOnItemLongClickListener",
            args = {}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "setOnItemLongClickListener",
            args = {android.widget.AdapterView.OnItemLongClickListener.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "performItemClick",
            args = {android.view.View.class, int.class, long.class}
        )
    })
    public void testAccessOnItemClickAndLongClickListener() {
        MockOnItemClickListener clickListener = new MockOnItemClickListener();
        MockOnItemLongClickListener longClickListener = new MockOnItemLongClickListener();
        assertFalse(mAdapterView.performItemClick(null, 0, 0));
        mAdapterView.setOnItemClickListener(clickListener);
        mAdapterView.setOnItemLongClickListener(longClickListener);
        assertFalse(clickListener.isClicked());
        assertTrue(mAdapterView.performItemClick(null, 0, 0));
        assertTrue(clickListener.isClicked());
        setArrayAdapter(mAdapterView);
        assertFalse(longClickListener.isClicked());
        mAdapterView.layout(0, 0, LAYOUT_WIDTH, LAYOUT_HEIGHT);
        assertTrue(mAdapterView.showContextMenuForChild(mAdapterView.getChildAt(0)));
        assertTrue(longClickListener.isClicked());
    }
    @TestTargets({
        @TestTargetNew(
            level = TestLevel.TODO,
            method = "getOnItemSelectedListener",
            args = {}
        ),
        @TestTargetNew(
            level = TestLevel.TODO,
            method = "setOnItemSelectedListener",
            args = {android.widget.AdapterView.OnItemSelectedListener.class}
        )
    })
    public void testAccessOnItemSelectedListener() {
        setArrayAdapter(mAdapterView);
        MockOnItemSelectedListener selectedListener = new MockOnItemSelectedListener();
        mAdapterView.setOnItemSelectedListener(selectedListener);
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        method = "getPositionForView",
        args = {android.view.View.class}
    )
    @ToBeFixed(bug = "1695243", explanation = "should add NullPointerException @throws"
        + " clause into javadoc.")
    public void testGetPositionForView() {
        setArrayAdapter(mAdapterView);
        mAdapterView.layout(0, 0, LAYOUT_WIDTH, LAYOUT_HEIGHT);
        int count = mAdapterView.getChildCount();
        for (int i = 0; i < count; i++) {
            assertEquals(i, mAdapterView.getPositionForView(mAdapterView.getChildAt(i)));
        }
        try {
            assertEquals(AdapterView.INVALID_POSITION, mAdapterView.getPositionForView(null));
            fail("Should throw NullPointerException");
        } catch (NullPointerException e) {
        }
        try {
            assertEquals(AdapterView.INVALID_POSITION,
                    mAdapterView.getPositionForView(new ImageView(mActivity)));
            fail("Should throw NullPointerException");
        } catch (NullPointerException e) {
        }
    }
    @TestTargets({
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "setFocusable",
            args = {boolean.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "setFocusableInTouchMode",
            args = {boolean.class}
        )
    })
    public void testChangeFocusable() {
        assertFalse(mAdapterView.isFocusable());
        assertFalse(mAdapterView.isFocusableInTouchMode());
        assertNull(mAdapterView.getAdapter());
        mAdapterView.setFocusable(true);
        assertFalse(mAdapterView.isFocusable());
        assertFalse(mAdapterView.isFocusableInTouchMode());
        setArrayAdapter(mAdapterView);
        assertTrue(mAdapterView.getAdapter().getCount() > 0);
        mAdapterView.setFocusable(true);
        assertTrue(mAdapterView.isFocusable());
        assertTrue(mAdapterView.isFocusableInTouchMode());
        mAdapterView.setFocusable(false);
        assertFalse(mAdapterView.isFocusable());
        assertFalse(mAdapterView.isFocusableInTouchMode());
    }
    @TestTargetNew(
        level = TestLevel.NOT_NECESSARY,
        method = "onLayout",
        args = {boolean.class, int.class, int.class, int.class, int.class}
    )
    public void testOnLayout() {
    }
    @TestTargets({
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "setSelected",
            args = {}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "getSelectedItemId",
            args = {}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "getSelectedItemPosition",
            args = {}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "getSelectedItem",
            args = {}
        )
    })
    public void testGetSelected() {
        assertEquals(AdapterView.INVALID_ROW_ID, mAdapterView.getSelectedItemId());
        assertEquals(AdapterView.INVALID_POSITION, mAdapterView.getSelectedItemPosition());
        assertEquals(null, mAdapterView.getSelectedItem());
        setArrayAdapter(mAdapterView);
        assertEquals(0, mAdapterView.getSelectedItemId());
        assertEquals(0, mAdapterView.getSelectedItemPosition());
        assertEquals(FRUIT[0], mAdapterView.getSelectedItem());
        int expectedId = 1;
        mAdapterView.setSelection(expectedId);
        assertEquals(expectedId, mAdapterView.getSelectedItemId());
        assertEquals(expectedId, mAdapterView.getSelectedItemPosition());
        assertEquals(FRUIT[expectedId], mAdapterView.getSelectedItem());
        expectedId = -1;
        mAdapterView.setSelection(expectedId);
        assertEquals(1, mAdapterView.getSelectedItemId());
        assertEquals(1, mAdapterView.getSelectedItemPosition());
        assertEquals(FRUIT[1], mAdapterView.getSelectedItem());
        expectedId = mAdapterView.getCount();
        mAdapterView.setSelection(expectedId);
        assertEquals(1, mAdapterView.getSelectedItemId());
        assertEquals(1, mAdapterView.getSelectedItemPosition());
        assertEquals(FRUIT[1], mAdapterView.getSelectedItem());
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        method = "dispatchSaveInstanceState",
        args = {android.util.SparseArray.class}
    )
    public void testDispatchSaveInstanceState() {
        MockAdapterView adapterView = new MockAdapterView(mActivity);
        adapterView.setSaveEnabled(true);
        adapterView.setId(1);
        SparseArray<Parcelable> sa = new SparseArray<Parcelable>();
        adapterView.dispatchSaveInstanceState(sa);
        assertTrue(sa.size() > 0);
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        method = "dispatchRestoreInstanceState",
        args = {android.util.SparseArray.class}
    )
    public void testDispatchRestoreInstanceState() {
        MockAdapterView adapterView = new MockAdapterView(mActivity);
        adapterView.setSaveEnabled(true);
        adapterView.setId(1);
        SparseArray<Parcelable> sparseArray = new SparseArray<Parcelable>();
        adapterView.dispatchRestoreInstanceState(sparseArray);
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        method = "canAnimate",
        args = {}
    )
    public void testCanAnimate() {
        MockAdapterView adapterView = new MockAdapterView(mActivity);
        LayoutAnimationController lAC = new LayoutAnimationController(new AnimationSet(true));
        assertNull(adapterView.getAdapter());
        adapterView.setLayoutAnimation(lAC);
        assertFalse(adapterView.canAnimate());
        setArrayAdapter(adapterView);
        assertTrue(adapterView.getAdapter().getCount() > 0);
        assertTrue(adapterView.canAnimate());
    }
    private static class MockAdapterView extends ListView{
        public MockAdapterView(Context context) {
            super(context);
        }
        public MockAdapterView(Context context, AttributeSet attrs) {
            super(context, attrs);
        }
        public MockAdapterView(Context context, AttributeSet attrs, int defStyle) {
            super(context, attrs, defStyle);
        }
        @Override
        protected void dispatchRestoreInstanceState(SparseArray<Parcelable> container) {
            super.dispatchRestoreInstanceState(container);
        }
        @Override
        protected void dispatchSaveInstanceState(SparseArray<Parcelable> container) {
            super.dispatchSaveInstanceState(container);
        }
        @Override
        protected boolean canAnimate() {
            return super.canAnimate();
        }
    }
    private void setArrayAdapter(AdapterView<ListAdapter> adapterView) {
        ((ListView) adapterView).setAdapter(new ArrayAdapter<String>(
                mActivity, R.layout.adapterview_layout, FRUIT));
    }
    private class MockOnItemClickListener implements OnItemClickListener {
        private boolean mClicked;
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            mClicked = true;
        }
        protected boolean isClicked() {
            return mClicked;
        }
    }
    private class MockOnItemLongClickListener implements OnItemLongClickListener {
        private boolean mClicked;
        public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
            mClicked = true;
            return true;
        }
        protected boolean isClicked() {
            return mClicked;
        }
    }
    private class MockOnItemSelectedListener implements OnItemSelectedListener {
        private boolean mIsItemSelected;
        private boolean mIsNothingSelected;
        public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
            mIsItemSelected = true;
        }
        public void onNothingSelected(AdapterView<?> parent) {
            mIsNothingSelected = true;
        }
        protected boolean isItemSelected() {
            return mIsItemSelected;
        }
        protected boolean isNothingSelected() {
            return mIsNothingSelected;
        }
    }
}
