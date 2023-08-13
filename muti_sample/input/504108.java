@TestTargetClass(DatePickerDialog.class)
public class DatePickerDialogTest extends ActivityInstrumentationTestCase2<DialogStubActivity> {
    private Instrumentation mInstrumentation;
    private DialogStubActivity mActivity;
    private int mOrientation;
    public DatePickerDialogTest() {
        super("com.android.cts.stub", DialogStubActivity.class);
    }
    @Override
    protected void setUp() throws Exception {
        super.setUp();
        mInstrumentation = getInstrumentation();
        mActivity = getActivity();
        mOrientation = mActivity.getRequestedOrientation();
    }
    @Override
    protected void tearDown() throws Exception {
        if (mActivity != null) {
            mActivity.setRequestedOrientation(mOrientation);
            mActivity.finish();
        }
        super.tearDown();
    }
    @TestTargets({
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "DatePickerDialog",
            args = {Context.class, int.class, OnDateSetListener.class, int.class, int.class,
                    int.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "onSaveInstanceState",
            args = {}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "onClick",
            args = {DialogInterface.class, int.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "onDateChanged",
            args = {DatePicker.class, int.class, int.class, int.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "onRestoreInstanceState",
            args = {android.os.Bundle.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "show",
            args = {}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "updateDate",
            args = {int.class, int.class, int.class}
        )
    })
    @BrokenTest("assume layout of DatePickerDialog")
    public void testDatePickerDialogWithTheme() throws Exception {
        doTestDatePickerDialog(DialogStubActivity.TEST_DATEPICKERDIALOG_WITH_THEME);
    }
    @TestTargets({
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "DatePickerDialog",
            args = {Context.class, OnDateSetListener.class, int.class, int.class, int.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "onSaveInstanceState",
            args = {}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "onClick",
            args = {DialogInterface.class, int.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "onDateChanged",
            args = {DatePicker.class, int.class, int.class, int.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "onRestoreInstanceState",
            args = {android.os.Bundle.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "show",
            args = {}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "updateDate",
            args = {int.class, int.class, int.class}
        )
    })
    @BrokenTest("assume layout of DatePickerDialog")
    public void testDatePickerDialog() throws Exception {
        doTestDatePickerDialog(DialogStubActivity.TEST_DATEPICKERDIALOG);
    }
    private void doTestDatePickerDialog(int index) throws Exception {
        popDialog(index);
        final DatePickerDialog datePickerDialog = (DatePickerDialog) mActivity.getDialog();
        assertTrue(datePickerDialog.isShowing());
        final TextView title = (TextView) datePickerDialog.findViewById(
                com.android.internal.R.id.alertTitle);
        assertEquals(TruncateAt.END, title.getEllipsize());
        mInstrumentation.sendKeyDownUpSync(KeyEvent.KEYCODE_DPAD_DOWN);
        mInstrumentation.sendKeyDownUpSync(KeyEvent.KEYCODE_DPAD_UP);
        mInstrumentation.sendKeyDownUpSync(KeyEvent.KEYCODE_ENTER);
        mInstrumentation.sendKeyDownUpSync(KeyEvent.KEYCODE_DPAD_DOWN);
        mInstrumentation.sendKeyDownUpSync(KeyEvent.KEYCODE_DPAD_DOWN);
        mInstrumentation.sendKeyDownUpSync(KeyEvent.KEYCODE_ENTER);
        mInstrumentation.waitForIdleSync();
        assertTrue(mActivity.onClickCalled);
        assertEquals(mActivity.updatedYear, mActivity.INITIAL_YEAR);
        assertEquals(mActivity.updatedMonth + 1, mActivity.INITIAL_MONTH);
        assertEquals(mActivity.updatedDay, mActivity.INITIAL_DAY_OF_MONTH);
        assertTrue(DialogStubActivity.onDateChangedCalled);
        mActivity.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
        mInstrumentation.waitForIdleSync();
        mActivity.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        mInstrumentation.waitForIdleSync();
        assertTrue(mActivity.onSaveInstanceStateCalled);
        assertTrue(DialogStubActivity.onRestoreInstanceStateCalled);
    }
    private void popDialog(int index) {
        assertTrue(index > 0);
        while (index != 0) {
            sendKeys(KeyEvent.KEYCODE_DPAD_DOWN);
            index--;
        }
        sendKeys(KeyEvent.KEYCODE_DPAD_CENTER);
    }
}
