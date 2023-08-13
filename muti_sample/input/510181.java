@TestTargetClass(DatePicker.class)
public class DatePickerTest extends AndroidTestCase {
    @TestTargets({
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            notes = "Test constructors of DatePicker.",
            method = "DatePicker",
            args = {android.content.Context.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            notes = "Test constructors of DatePicker.",
            method = "DatePicker",
            args = {android.content.Context.class, android.util.AttributeSet.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            notes = "Test constructors of DatePicker.",
            method = "DatePicker",
            args = {android.content.Context.class, android.util.AttributeSet.class, int.class}
        )
    })
    @ToBeFixed(bug = "1417734", explanation = "should add NullPointerException @throws clause into"
         + "javadoc.")
    public void testConstructor() {
        new DatePicker(mContext);
        new DatePicker(mContext, null);
        new DatePicker(mContext, getAttributeSet(R.layout.datepicker_layout));
        new DatePicker(mContext, getAttributeSet(R.layout.datepicker_layout), 0);
        try {
            new DatePicker(null, getAttributeSet(R.layout.datepicker_layout), 0);
            fail("should throw NullPointerException");
        } catch (Exception e) {
        }
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "Test {@link DatePicker#setEnabled(boolean)}.",
        method = "setEnabled",
        args = {boolean.class}
    )
    public void testSetEnabled() {
        MockDatePicker datePicker = createDatePicker();
        assertTrue(datePicker.isEnabled());
        datePicker.setEnabled(false);
        assertFalse(datePicker.isEnabled());
        datePicker.setEnabled(true);
        assertTrue(datePicker.isEnabled());
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "Test {@link DatePicker#init()}.",
        method = "init",
        args = {int.class, int.class, int.class,
                android.widget.DatePicker.OnDateChangedListener.class}
    )
    public void testInit() {
        MockOnDateChangedListener onDateChangedListener = new MockOnDateChangedListener();
        DatePicker datePicker = createDatePicker();
        datePicker.init(2000, 10, 15, onDateChangedListener);
        assertEquals(2000, datePicker.getYear());
        assertEquals(10, datePicker.getMonth());
        assertEquals(15, datePicker.getDayOfMonth());
    }
    @TestTargets({
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "updateDate",
            args = {int.class, int.class, int.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "getYear",
            args = {}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "getMonth",
            args = {}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "getDayOfMonth",
            args = {}
        )
    })
    public void testAccessDate() {
        DatePicker datePicker = createDatePicker();
        MockOnDateChangedListener onDateChangedListener = new MockOnDateChangedListener();
        datePicker.init(2000, 10, 15, onDateChangedListener);
        assertEquals(2000, datePicker.getYear());
        assertEquals(10, datePicker.getMonth());
        assertEquals(15, datePicker.getDayOfMonth());
        datePicker.updateDate(1989, 9, 19);
        assertEquals(1989, datePicker.getYear());
        assertEquals(9, datePicker.getMonth());
        assertEquals(19, datePicker.getDayOfMonth());
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "Test {@link DatePicker#updateDate(int, int, int)}.",
        method = "updateDate",
        args = {int.class, int.class, int.class}
    )
    public void testUpdateDate() {
        DatePicker datePicker = createDatePicker();
        datePicker.updateDate(1989, 9, 19);
        assertEquals(1989, datePicker.getYear());
        assertEquals(9, datePicker.getMonth());
        assertEquals(19, datePicker.getDayOfMonth());
    }
    @TestTargets({
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "onSaveInstanceState",
            args = {}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "dispatchRestoreInstanceState",
            args = {android.util.SparseArray.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "onRestoreInstanceState",
            args = {android.os.Parcelable.class}
        )
    })
    public void testOnSaveInstanceState() {
        MockDatePicker datePicker = createDatePicker();
        datePicker.updateDate(2008, 9, 10);
        SparseArray<Parcelable> container = new SparseArray<Parcelable>();
        assertEquals(View.NO_ID, datePicker.getId());
        datePicker.setId(99);
        assertFalse(datePicker.hasCalledOnSaveInstanceState());
        datePicker.saveHierarchyState(container);
        assertEquals(1, datePicker.getChildCount());
        assertTrue(datePicker.hasCalledOnSaveInstanceState());
        datePicker = createDatePicker();
        datePicker.setId(99);
        assertFalse(datePicker.hasCalledOnRestoreInstanceState());
        datePicker.dispatchRestoreInstanceState(container);
        assertEquals(2008, datePicker.getYear());
        assertEquals(9, datePicker.getMonth());
        assertEquals(10, datePicker.getDayOfMonth());
        assertTrue(datePicker.hasCalledOnRestoreInstanceState());
    }
    private AttributeSet getAttributeSet(int resourceId) {
        final XmlResourceParser parser = mContext.getResources().getXml(resourceId);
        try {
            XmlUtils.beginDocument(parser, "RelativeLayout");
        } catch (Exception e) {
            fail("Found unexpected loading process error before invoking generateLayoutParams.");
        }
        final AttributeSet attr = Xml.asAttributeSet(parser);
        assertNotNull(attr);
        return attr;
    }
    private MockDatePicker createDatePicker() {
        MockDatePicker datePicker = new MockDatePicker(mContext,
                getAttributeSet(R.layout.datepicker_layout));
        return datePicker;
    }
    private class MockDatePicker extends DatePicker {
        private boolean mCalledOnSaveInstanceState = false;
        private boolean mCalledOnRestoreInstanceState = false;
        public MockDatePicker(Context context, AttributeSet attrs) {
            super(context, attrs);
        }
        @Override
        protected void dispatchRestoreInstanceState(SparseArray<Parcelable> container) {
            super.dispatchRestoreInstanceState(container);
        }
        @Override
        protected Parcelable onSaveInstanceState() {
            mCalledOnSaveInstanceState = true;
            return super.onSaveInstanceState();
        }
        public boolean hasCalledOnSaveInstanceState() {
            return mCalledOnSaveInstanceState;
        }
        @Override
        protected void onRestoreInstanceState(Parcelable state) {
            mCalledOnRestoreInstanceState = true;
            super.onRestoreInstanceState(state);
        }
        public boolean hasCalledOnRestoreInstanceState() {
            return mCalledOnRestoreInstanceState;
        }
    }
    private class MockOnDateChangedListener implements DatePicker.OnDateChangedListener {
        public void onDateChanged(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
        }
    }
}
