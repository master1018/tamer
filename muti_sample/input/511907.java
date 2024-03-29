@TestTargetClass(TimePickerDialog.class)
public class TimePickerDialogTest extends ActivityInstrumentationTestCase2<DialogStubActivity> {
    private static final String HOUR = "hour";
    private static final String MINUTE = "minute";
    private static final String IS_24_HOUR = "is24hour";
    private static final int TARGET_HOUR = 15;
    private static final int TARGET_MINUTE = 9;
    private int mCallbackHour;
    private int mCallbackMinute;
    private OnTimeSetListener mOnTimeSetListener;
    private Context mContext;
    private DialogStubActivity mActivity;
    private TimePickerDialog mTimePickerDialog;
    public TimePickerDialogTest() {
        super("com.android.cts.stub", DialogStubActivity.class);
    }
    @Override
    protected void setUp() throws Exception {
        super.setUp();
        mContext = getInstrumentation().getContext();
        mActivity = getActivity();
        mOnTimeSetListener = new OnTimeSetListener(){
            public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
                mCallbackHour = hourOfDay;
                mCallbackMinute = minute;
            }
        };
        mTimePickerDialog = new TimePickerDialog( mContext, mOnTimeSetListener, TARGET_HOUR,
                TARGET_MINUTE, true);
    }
    @TestTargets({
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "TimePickerDialog",
            args = {android.content.Context.class,
                    android.app.TimePickerDialog.OnTimeSetListener.class, int.class, int.class,
                    boolean.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "TimePickerDialog",
            args = {android.content.Context.class,
                    int.class, android.app.TimePickerDialog.OnTimeSetListener.class,
                    int.class, int.class, boolean.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "onSaveInstanceState",
            args = {}
        )
    })
    public void testSaveInstanceState(){
        TimePickerDialog tD = new TimePickerDialog( mContext, mOnTimeSetListener, TARGET_HOUR,
                TARGET_MINUTE, true);
        Bundle b = tD.onSaveInstanceState();
        assertEquals(TARGET_HOUR, b.getInt(HOUR));
        assertEquals(TARGET_MINUTE, b.getInt(MINUTE));
        assertTrue(b.getBoolean(IS_24_HOUR));
        int minute = 13;
        tD = new TimePickerDialog( mContext, com.android.cts.stub.R.style.Theme_AlertDialog,
                mOnTimeSetListener, TARGET_HOUR, minute, false);
        b = tD.onSaveInstanceState();
        assertEquals(TARGET_HOUR, b.getInt(HOUR));
        assertEquals(minute, b.getInt(MINUTE));
        assertFalse(b.getBoolean(IS_24_HOUR));
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        method = "onClick",
        args = {android.content.DialogInterface.class, int.class}
    )
    public void testOnClick(){
        mTimePickerDialog.onClick(null, 0);
        assertEquals(TARGET_HOUR, mCallbackHour);
        assertEquals(TARGET_MINUTE, mCallbackMinute);
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        method = "onTimeChanged",
        args = {android.widget.TimePicker.class, int.class, int.class}
    )
    public void testOnTimeChanged() throws Throwable {
        final int minute = 34;
        popDialog(DialogStubActivity.TEST_TIMEPICKERDIALOG);
        final TimePickerDialog d = (TimePickerDialog) mActivity.getDialog();
        runTestOnUiThread(new Runnable() {
            public void run() {
                d.onTimeChanged(null, TARGET_HOUR, minute);
            }
        });
        getInstrumentation().waitForIdleSync();
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        method = "updateTime",
        args = {int.class, int.class}
    )
    public void testUpdateTime(){
        int minute = 18;
        mTimePickerDialog.updateTime(TARGET_HOUR, minute);
        Bundle b = mTimePickerDialog.onSaveInstanceState();
        assertEquals(TARGET_HOUR, b.getInt(HOUR));
        assertEquals(minute, b.getInt(MINUTE));
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        method = "onRestoreInstanceState",
        args = {android.os.Bundle.class}
    )
    public void testOnRestoreInstanceState(){
        int minute = 27;
        Bundle b1 = new Bundle();
        b1.putInt(HOUR, TARGET_HOUR);
        b1.putInt(MINUTE, minute);
        b1.putBoolean(IS_24_HOUR, false);
        mTimePickerDialog.onRestoreInstanceState(b1);
        Bundle b2 = mTimePickerDialog.onSaveInstanceState();
        assertEquals(TARGET_HOUR, b2.getInt(HOUR));
        assertEquals(minute, b2.getInt(MINUTE));
        assertFalse(b2.getBoolean(IS_24_HOUR));
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
