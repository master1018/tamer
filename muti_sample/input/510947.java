public class CalculatorHitSomeButtons extends ActivityInstrumentationTestCase <Calculator>{
    public boolean setup = false;
    private static final String TAG = "CalculatorTests";
    Calculator mActivity = null;
    Instrumentation mInst = null;
    public CalculatorHitSomeButtons() {
        super("com.android.calculator2", Calculator.class);
    }
    @Override
    protected void setUp() throws Exception {
        super.setUp();
        mActivity = getActivity();
        mInst = getInstrumentation();
    }
    @Override
    protected void tearDown() throws Exception {
        super.tearDown();
    }
    @LargeTest
    public void testPressSomeKeys() {
        Log.v(TAG, "Pressing some keys!");
        press(KeyEvent.KEYCODE_ENTER);
        press(KeyEvent.KEYCODE_CLEAR);
        press(KeyEvent.KEYCODE_3);
        press(KeyEvent.KEYCODE_PLUS);
        press(KeyEvent.KEYCODE_4);
        press(KeyEvent.KEYCODE_9 | KeyEvent.META_SHIFT_ON);
        press(KeyEvent.KEYCODE_5);
        press(KeyEvent.KEYCODE_ENTER);
        assertEquals(displayVal(), "23");
    }
    @LargeTest
    public void testTapSomeButtons() {
        Log.v(TAG, "Tapping some buttons!");
        tap(R.id.equal);
        tap(R.id.del);
        tap(R.id.digit5);
        tap(R.id.digit6);
        tap(R.id.digit7);
        tap(R.id.div);
        tap(R.id.digit3);
        tap(R.id.equal);
        assertEquals(displayVal(), "189");
        tap(R.id.minus);
        tap(R.id.digit7);
        tap(R.id.digit8);
        tap(R.id.digit9);
        tap(R.id.equal);
        assertEquals(displayVal(), mActivity.getString(R.string.minus) + "600");
    }
    private void press(int keycode) {
        mInst.sendKeyDownUpSync(keycode);
    }
    private boolean tap(int id) {
        View view = mActivity.findViewById(id);
        if(view != null) {
            TouchUtils.clickView(this, view);
            return true;
        }
        return false;
    }
    private String displayVal() {
        CalculatorDisplay display = (CalculatorDisplay) mActivity.findViewById(R.id.display);
        assertNotNull(display);
        EditText box = (EditText) display.getCurrentView();
        assertNotNull(box);
        return box.getText().toString();
    }
}
