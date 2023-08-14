@TestTargetClass(AbsSpinner.class)
public class AbsSpinnerTest extends ActivityInstrumentationTestCase2<RelativeLayoutStubActivity> {
    private Context mContext;
    public AbsSpinnerTest() {
        super("com.android.cts.stub", RelativeLayoutStubActivity.class);
    }
    @Override
    protected void setUp() throws Exception {
        super.setUp();
        mContext = getInstrumentation().getTargetContext();
    }
    @ToBeFixed(bug="1448885", explanation="AbsSpinner is an abstract class and its abstract " +
            "method layout(int, boolean) is package private, we can not extends it directly " +
            "to test. So, we use its subclass Spinner and Gallery to test")
    @TestTargets({
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            notes = "",
            method = "AbsSpinner",
            args = {android.content.Context.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            notes = "",
            method = "AbsSpinner",
            args = {android.content.Context.class, android.util.AttributeSet.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            notes = "",
            method = "AbsSpinner",
            args = {android.content.Context.class, android.util.AttributeSet.class, int.class}
        )
    })
    public void testConstructor() {
        new Spinner(mContext);
        new Spinner(mContext, null);
        new Spinner(mContext, null, com.android.internal.R.attr.spinnerStyle);
        new Gallery(mContext);
        new Gallery(mContext, null);
        new Gallery(mContext, null, 0);
        XmlPullParser parser = mContext.getResources().getXml(R.layout.gallery_test);
        AttributeSet attrs = Xml.asAttributeSet(parser);
        new Gallery(mContext, attrs);
        new Gallery(mContext, attrs, 0);
    }
    @TestTargets({
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            notes = "",
            method = "setSelection",
            args = {int.class, boolean.class}
        )
    })
    @ToBeFixed(bug = "1695243", explanation = "the javadoc for setSelection() is incomplete." +
            "1. no description about the @param and @return.")
    @UiThreadTest
    public void testSetSelectionIntBoolean() {
        AbsSpinner absSpinner = (AbsSpinner) getActivity().findViewById(R.id.spinner1);
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(mContext,
                com.android.cts.stub.R.array.string, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        absSpinner.setAdapter(adapter);
        assertEquals(0, absSpinner.getSelectedItemPosition());
        absSpinner.setSelection(1, true);
        assertEquals(1, absSpinner.getSelectedItemPosition());
        absSpinner.setSelection(absSpinner.getCount() - 1, false);
        assertEquals(absSpinner.getCount() - 1, absSpinner.getSelectedItemPosition());
    }
    @TestTargets({
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            notes = "",
            method = "setSelection",
            args = {int.class}
        )
    })
    @ToBeFixed(bug = "1695243", explanation = "the javadoc for setSelection() is incomplete." +
            "1. no description about the @param and @return.")
    @UiThreadTest
    public void testSetSelectionInt() {
        AbsSpinner absSpinner = (AbsSpinner) getActivity().findViewById(R.id.spinner1);
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(mContext,
                com.android.cts.stub.R.array.string, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        absSpinner.setAdapter(adapter);
        assertEquals(0, absSpinner.getSelectedItemPosition());
        absSpinner.setSelection(1);
        assertEquals(1, absSpinner.getSelectedItemPosition());
        absSpinner.setSelection(absSpinner.getCount() - 1);
        assertEquals(absSpinner.getCount() - 1, absSpinner.getSelectedItemPosition());
    }
    @TestTargets({
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            notes = "",
            method = "setAdapter",
            args = {android.widget.SpinnerAdapter.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            notes = "",
            method = "getAdapter",
            args = {}
        )
    })
    @ToBeFixed(bug = "1695243", explanation = "the javadoc for setAdapter() is incomplete." +
            "1. no description about the situation that adapter is null.")
    @UiThreadTest
    public void testAccessAdapter() {
        AbsSpinner absSpinner = (AbsSpinner) getActivity().findViewById(R.id.spinner1);
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(mContext,
                com.android.cts.stub.R.array.string, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        absSpinner.setAdapter(adapter);
        assertSame(adapter, absSpinner.getAdapter());
        assertEquals(adapter.getCount(), absSpinner.getCount());
        assertEquals(0, absSpinner.getSelectedItemPosition());
        assertEquals(adapter.getItemId(0), absSpinner.getSelectedItemId());
        absSpinner.setSelection(1);
        assertEquals(1, absSpinner.getSelectedItemPosition());
        assertEquals(adapter.getItemId(1), absSpinner.getSelectedItemId());
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "requestLayout",
        args = {}
    )
    public void testRequestLayout() {
        AbsSpinner absSpinner = new Spinner(mContext);
        absSpinner.layout(0, 0, 200, 300);
        assertFalse(absSpinner.isLayoutRequested());
        absSpinner.requestLayout();
        assertTrue(absSpinner.isLayoutRequested());
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "getCount",
        args = {}
    )
    @UiThreadTest
    public void testGetCount() {
        AbsSpinner absSpinner = (AbsSpinner) getActivity().findViewById(R.id.spinner1);
        ArrayAdapter<CharSequence> adapter1 = ArrayAdapter.createFromResource(mContext,
                com.android.cts.stub.R.array.string, android.R.layout.simple_spinner_item);
        absSpinner.setAdapter(adapter1);
        assertEquals(adapter1.getCount(), absSpinner.getCount());
        CharSequence anotherStringArray[] = { "another array string 1", "another array string 2" };
        ArrayAdapter<CharSequence> adapter2 = new ArrayAdapter<CharSequence>(mContext,
                android.R.layout.simple_spinner_item, anotherStringArray);
        absSpinner.setAdapter(adapter2);
        assertEquals(anotherStringArray.length, absSpinner.getCount());
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "pointToPosition",
        args = {int.class, int.class}
    )
    public void testPointToPosition() {
        AbsSpinner absSpinner = new Gallery(mContext);
        MockSpinnerAdapter adapter = new MockSpinnerAdapter();
        assertEquals(AdapterView.INVALID_POSITION, absSpinner.pointToPosition(10, 10));
        adapter.setCount(3);
        absSpinner.setAdapter(adapter);
        absSpinner.measure(View.MeasureSpec.UNSPECIFIED, View.MeasureSpec.UNSPECIFIED);
        Rect rc = new Rect(0, 0, 100, 100);
        Rect rcChild0 = new Rect(0, 0, 20, rc.bottom);
        Rect rcChild1 = new Rect(rcChild0.right, 0, 70, rc.bottom);
        Rect rcChild2 = new Rect(rcChild1.right, 0, rc.right, rc.bottom);
        absSpinner.layout(rc.left, rc.top, rc.right, rc.bottom);
        absSpinner.getChildAt(0).layout(rcChild0.left, rcChild0.top,
                rcChild0.right, rcChild0.bottom);
        absSpinner.getChildAt(1).layout(rcChild1.left, rcChild1.top,
                rcChild1.right, rcChild1.bottom);
        absSpinner.getChildAt(2).layout(rcChild2.left, rcChild2.top,
                rcChild2.right, rcChild2.bottom);
        assertEquals(AdapterView.INVALID_POSITION, absSpinner.pointToPosition(-1, -1));
        assertEquals(0, absSpinner.pointToPosition(rcChild0.left + 1, rc.bottom - 1));
        assertEquals(1, absSpinner.pointToPosition(rcChild1.left + 1, rc.bottom - 1));
        assertEquals(2, absSpinner.pointToPosition(rcChild2.left + 1, rc.bottom - 1));
        assertEquals(AdapterView.INVALID_POSITION,
                absSpinner.pointToPosition(rc.right + 1, rc.bottom - 1));
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "getSelectedView",
        args = {}
    )
    public void testGetSelectedView() {
        AbsSpinner absSpinner = new Gallery(mContext);
        MockSpinnerAdapter adapter = new MockSpinnerAdapter();
        assertNull(absSpinner.getSelectedView());
        absSpinner.setAdapter(adapter);
        absSpinner.layout(0, 0, 20, 20);
        assertSame(absSpinner.getChildAt(0), absSpinner.getSelectedView());
        absSpinner.setSelection(1, true);
        assertSame(absSpinner.getChildAt(1), absSpinner.getSelectedView());
    }
    @TestTargets({
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            notes = "",
            method = "onSaveInstanceState",
            args = {}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            notes = "",
            method = "onRestoreInstanceState",
            args = {android.os.Parcelable.class}
        )
    })
    @UiThreadTest
    public void testOnSaveAndRestoreInstanceState() {
        AbsSpinner absSpinner = (AbsSpinner) getActivity().findViewById(R.id.spinner1);
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(mContext,
                com.android.cts.stub.R.array.string, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        absSpinner.setAdapter(adapter);
        assertEquals(0, absSpinner.getSelectedItemPosition());
        assertEquals(adapter.getItemId(0), absSpinner.getSelectedItemId());
        Parcelable parcelable = absSpinner.onSaveInstanceState();
        absSpinner.setSelection(1);
        assertEquals(1, absSpinner.getSelectedItemPosition());
        assertEquals(adapter.getItemId(1), absSpinner.getSelectedItemId());
        absSpinner.onRestoreInstanceState(parcelable);
        absSpinner.measure(View.MeasureSpec.EXACTLY, View.MeasureSpec.EXACTLY);
        absSpinner.layout(absSpinner.getLeft(), absSpinner.getTop(), absSpinner.getRight(),
                absSpinner.getBottom());
        assertEquals(0, absSpinner.getSelectedItemPosition());
        assertEquals(adapter.getItemId(0), absSpinner.getSelectedItemId());
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "generateDefaultLayoutParams",
        args = {}
    )
    @ToBeFixed(bug = "1448885", explanation = "AbsSpinner#generateDefaultLayoutParams() is" +
            " protected method, we have to test it in MockSpinner which extends from" +
            " AbsSpinner. class MockSpinner must implement the inherited abstract method" +
            " AbsSpinner.layout(int, boolean), but cannot override it since it is not" +
            " visible from MockSpinner. So we cannot test this method.")
    public void testGenerateDefaultLayoutParams() {
    }
    @TestTargetNew(
        level = TestLevel.NOT_NECESSARY,
        notes = "Test onMeasure(int, int) function.",
        method = "onMeasure",
        args = {int.class, int.class}
    )
    public void testOnMeasure() {
    }
    private class MockSpinnerAdapter implements SpinnerAdapter {
        public static final int DEFAULT_COUNT = 1;
        private int mCount = DEFAULT_COUNT;
        public View getDropDownView(int position, View convertView,
                ViewGroup parent) {
            return null;
        }
        public int getCount() {
            return mCount;
        }
        public void setCount(int count) {
            mCount = count;
        }
        public Object getItem(int position) {
            return null;
        }
        public long getItemId(int position) {
            return position;
        }
        public int getItemViewType(int position) {
            return 0;
        }
        public View getView(int position, View convertView, ViewGroup parent) {
            return new ImageView(mContext);
        }
        public int getViewTypeCount() {
            return 0;
        }
        public boolean hasStableIds() {
            return false;
        }
        public boolean isEmpty() {
            return false;
        }
        public void registerDataSetObserver(DataSetObserver observer) {
        }
        public void unregisterDataSetObserver(DataSetObserver observer) {
        }
    }
}
