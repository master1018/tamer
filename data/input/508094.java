@TestTargetClass(Dialog.class)
public class DialogTest extends ActivityInstrumentationTestCase2<DialogStubActivity> {
    protected static final long SLEEP_TIME = 200;
    private static final long MOTION_DOWN_TIME = 0L;
    private static final long MOTION_EVENT_TIME = 0L;
    private static final float MOTION_X = -20.0f;
    private static final float MOTION_Y = -20.0f;
    private static final String STUB_ACTIVITY_PACKAGE = "com.android.cts.stub";
    private static final int DISMISS = 0x43;
    private static final int CANCEL = 0x44;
    private boolean mCalledCallback;
    private boolean mIsKey0Listened;
    private boolean mIsKey1Listened;
    private boolean mOnCancelListenerCalled;
    private Instrumentation mInstrumentation;
    private Context mContext;
    private DialogStubActivity mActivity;
    public DialogTest() {
        super(STUB_ACTIVITY_PACKAGE, DialogStubActivity.class);
    }
    @Override
    protected void setUp() throws Exception {
        super.setUp();
        mInstrumentation = getInstrumentation();
        mContext = mInstrumentation.getContext();
        mActivity = getActivity();
    }
    @Override
    protected void tearDown() throws Exception {
        super.tearDown();
        if (mActivity != null) {
            mActivity.finish();
        }
    }
    protected void popDialog(int index) {
        assertTrue(index >= 0);
        while (index != 0) {
            sendKeys(KeyEvent.KEYCODE_DPAD_DOWN);
            index--;
        }
        sendKeys(KeyEvent.KEYCODE_DPAD_CENTER);
    }
    @TestTargets({
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "Dialog",
            args = {android.content.Context.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "Dialog",
            args = {android.content.Context.class, int.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            notes = "test Dialog protected Constructors through mock dialog",
            method = "Dialog",
            args = {android.content.Context.class, boolean.class,
                    android.content.DialogInterface.OnCancelListener.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "getContext",
            args = {}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "getWindow",
            args = {}
        )
    })
    public void testDialog(){
        new Dialog(mContext);
        Dialog d = new Dialog(mContext, 0);
        d = new Dialog(mContext, R.style.TextAppearance);
        TypedArray ta =
            d.getContext().getTheme().obtainStyledAttributes(R.styleable.TextAppearance);
        assertTextAppearanceStyle(ta);
        final Window w = d.getWindow();
        ta = w.getContext().getTheme().obtainStyledAttributes(R.styleable.TextAppearance);
        assertTextAppearanceStyle(ta);
        mActivity.onCancelListenerCalled = false;
        popDialog(DialogStubActivity.TEST_PROTECTED_CANCELABLE);
        sendKeys(KeyEvent.KEYCODE_BACK);
        assertTrue(mActivity.onCancelListenerCalled);
        sendKeys(KeyEvent.KEYCODE_DPAD_DOWN, KeyEvent.KEYCODE_DPAD_CENTER);
        mActivity.onCancelListenerCalled = false;
        sendKeys(KeyEvent.KEYCODE_BACK);
        assertFalse(mActivity.onCancelListenerCalled);
    }
    private void assertTextAppearanceStyle(TypedArray ta) {
        final int defValue = -1;
        final Resources.Theme expected = mContext.getResources().newTheme();
        expected.setTo(mContext.getTheme());
        expected.applyStyle(R.style.TextAppearance, true);
        TypedArray expectedTa = expected.obtainStyledAttributes(R.styleable.TextAppearance);
        assertEquals(expectedTa.getIndexCount(), ta.getIndexCount());
        assertEquals(expectedTa.getColor(R.styleable.TextAppearance_textColor, defValue),
                ta.getColor(R.styleable.TextAppearance_textColor, defValue));
        assertEquals(expectedTa.getColor(R.styleable.TextAppearance_textColorHint, defValue),
                ta.getColor(R.styleable.TextAppearance_textColorHint, defValue));
        assertEquals(expectedTa.getColor(R.styleable.TextAppearance_textColorLink, defValue),
                ta.getColor(R.styleable.TextAppearance_textColorLink, defValue));
        assertEquals(expectedTa.getColor(R.styleable.TextAppearance_textColorHighlight, defValue),
                ta.getColor(R.styleable.TextAppearance_textColorHighlight, defValue));
        assertEquals(expectedTa.getDimension(R.styleable.TextAppearance_textSize, defValue),
                ta.getDimension(R.styleable.TextAppearance_textSize, defValue));
        assertEquals(expectedTa.getInt(R.styleable.TextAppearance_textStyle, defValue),
                ta.getInt(R.styleable.TextAppearance_textStyle, defValue));
    }
    @TestTargets({
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "onStart",
            args = {}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "onCreate",
            args = {android.os.Bundle.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "onStop",
            args = {}
        )
    })
    public void testOnStartCreateStop(){
        popDialog(DialogStubActivity.TEST_ONSTART_AND_ONSTOP);
        final TestDialog d = (TestDialog) mActivity.getDialog();
        assertTrue(d.isOnStartCalled);
        assertTrue(d.isOnCreateCalled);
        assertFalse(d.isOnStopCalled);
        sendKeys(KeyEvent.KEYCODE_BACK);
        assertTrue(d.isOnStopCalled);
    }
    @TestTargets({
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "setOwnerActivity",
            args = {android.app.Activity.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "getOwnerActivity",
            args = {}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "setVolumeControlStream",
            args = {int.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "getVolumeControlStream",
            args = {}
        )
    })
    public void testAccessOwnerActivity() {
        popDialog(DialogStubActivity.TEST_DIALOG_WITHOUT_THEME);
        Dialog d = mActivity.getDialog();
        assertNotNull(d);
        assertSame(mActivity, d.getOwnerActivity());
        d.setVolumeControlStream(d.getVolumeControlStream() + 1);
        assertEquals(d.getOwnerActivity().getVolumeControlStream() + 1, d.getVolumeControlStream());
        try {
            d.setOwnerActivity(null);
            fail("Should throw NullPointerException");
        } catch (NullPointerException e) {
        }
        d = new Dialog(mContext);
        assertNull(d.getOwnerActivity());
    }
    @TestTargets({
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "show",
            args = {}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "hide",
            args = {}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "isShowing",
            args = {}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "dismiss",
            args = {}
        )
    })
    public void testShow() throws Throwable {
        popDialog(DialogStubActivity.TEST_DIALOG_WITHOUT_THEME);
        final Dialog d = mActivity.getDialog();
        final View decor = d.getWindow().getDecorView();
        runTestOnUiThread(new Runnable() {
            public void run() {
                d.hide();
            }
        });
        mInstrumentation.waitForIdleSync();
        assertEquals(View.GONE, decor.getVisibility());
        assertTrue(d.isShowing());
        runTestOnUiThread(new Runnable() {
            public void run() {
                d.show();
            }
        });
        mInstrumentation.waitForIdleSync();
        assertEquals(View.VISIBLE, decor.getVisibility());
        assertTrue(d.isShowing());
        dialogDismiss(d);
        assertFalse(d.isShowing());
    }
    @TestTargets({
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "onRestoreInstanceState",
            args = {android.os.Bundle.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "onSaveInstanceState",
            args = {}
        )
    })
    public void testOnSaveInstanceState() {
        popDialog(DialogStubActivity.TEST_ONSTART_AND_ONSTOP);
        final TestDialog d = (TestDialog) mActivity.getDialog();
        mActivity.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
        mInstrumentation.waitForIdleSync();
        mActivity.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        mInstrumentation.waitForIdleSync();
        assertTrue(d.isOnSaveInstanceStateCalled);
        assertTrue(TestDialog.isOnRestoreInstanceStateCalled);
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        method = "getCurrentFocus",
        args = {}
    )
    public void testGetCurrentFocus() throws Throwable {
        popDialog(DialogStubActivity.TEST_ONSTART_AND_ONSTOP);
        final TestDialog d = (TestDialog) mActivity.getDialog();
        assertNull(d.getCurrentFocus());
        runTestOnUiThread(new Runnable() {
            public void run() {
                d.takeKeyEvents(true);
                d.setContentView(R.layout.alert_dialog_text_entry);
            }
        });
        mInstrumentation.waitForIdleSync();
        sendKeys(KeyEvent.KEYCODE_0);
        assertEquals(d.getWindow().getCurrentFocus(), d.getCurrentFocus());
    }
    @TestTargets({
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "setContentView",
            args = {int.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "setContentView",
            args = {android.view.View.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "setContentView",
            args = {android.view.View.class, android.view.ViewGroup.LayoutParams.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "findViewById",
            args = {int.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "addContentView",
            args = {android.view.View.class, android.view.ViewGroup.LayoutParams.class}
        )
    })
    public void testSetContentView() throws Throwable {
        popDialog(DialogStubActivity.TEST_DIALOG_WITHOUT_THEME);
        final Dialog d = mActivity.getDialog();
        assertNotNull(d);
        runTestOnUiThread(new Runnable() {
            public void run() {
                d.setContentView(R.layout.alert_dialog_text_entry);
            }
        });
        mInstrumentation.waitForIdleSync();
        assertNotNull(d.findViewById(R.id.username_view));
        assertNotNull(d.findViewById(R.id.username_edit));
        assertNotNull(d.findViewById(R.id.password_view));
        assertNotNull(d.findViewById(R.id.password_edit));
        final LayoutInflater inflate1 = d.getLayoutInflater();
        runTestOnUiThread(new Runnable() {
            public void run() {
                d.setContentView(inflate1.inflate(R.layout.alert_dialog_text_entry_2, null));
            }
        });
        mInstrumentation.waitForIdleSync();
        assertNotNull(d.findViewById(R.id.username_view));
        assertNotNull(d.findViewById(R.id.username_edit));
        assertNull(d.findViewById(R.id.password_view));
        assertNull(d.findViewById(R.id.password_edit));
        final WindowManager.LayoutParams lp = d.getWindow().getAttributes();
        final LayoutInflater inflate2 = mActivity.getLayoutInflater();
        runTestOnUiThread(new Runnable() {
            public void run() {
                d.setContentView(inflate2.inflate(R.layout.alert_dialog_text_entry, null), lp);
            }
        });
        mInstrumentation.waitForIdleSync();
        assertNotNull(d.findViewById(R.id.username_view));
        assertNotNull(d.findViewById(R.id.username_edit));
        assertNotNull(d.findViewById(R.id.password_view));
        assertNotNull(d.findViewById(R.id.password_edit));
        final WindowManager.LayoutParams lp2 = d.getWindow().getAttributes();
        final LayoutInflater inflate3 = mActivity.getLayoutInflater();
        lp2.height = ViewGroup.LayoutParams.WRAP_CONTENT;
        lp2.width = ViewGroup.LayoutParams.WRAP_CONTENT;
        runTestOnUiThread(new Runnable() {
            public void run() {
                d.addContentView(inflate3.inflate(R.layout.checkbox_layout, null), lp2);
            }
        });
        mInstrumentation.waitForIdleSync();
        assertNotNull(d.findViewById(R.id.check_box));
        assertNotNull(d.findViewById(R.id.username_view));
        assertNotNull(d.findViewById(R.id.username_edit));
        assertNotNull(d.findViewById(R.id.password_view));
        assertNotNull(d.findViewById(R.id.password_edit));
    }
    @TestTargets({
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "setTitle",
            args = {java.lang.CharSequence.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "setTitle",
            args = {int.class}
        )
    })
    public void testSetTitle() {
        final String expectedTitle = "Test Dialog Without theme";
        popDialog(DialogStubActivity.TEST_DIALOG_WITHOUT_THEME);
        assertNotNull(mActivity.getDialog());
        mActivity.setUpTitle(expectedTitle);
        mInstrumentation.waitForIdleSync();
        final Dialog d = mActivity.getDialog();
        assertEquals(expectedTitle, (String) d.getWindow().getAttributes().getTitle());
        mActivity.setUpTitle(R.string.hello_android);
        mInstrumentation.waitForIdleSync();
        assertEquals(mActivity.getResources().getString(R.string.hello_android),
                (String) d.getWindow().getAttributes().getTitle());
    }
    @TestTargets({
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "onKeyDown",
            args = {int.class, android.view.KeyEvent.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "onKeyUp",
            args = {int.class, android.view.KeyEvent.class}
        )
    })
    public void testOnKeyDownKeyUp() {
        popDialog(DialogStubActivity.TEST_ONSTART_AND_ONSTOP);
        final TestDialog d = (TestDialog) mActivity.getDialog();
        assertFalse(d.isOnKeyDownCalled);
        assertFalse(d.isOnKeyUpCalled);
        mInstrumentation.sendKeyDownUpSync(KeyEvent.KEYCODE_0);
        assertTrue(d.isOnKeyDownCalled);
        assertTrue(d.isOnKeyUpCalled);
        assertEquals(KeyEvent.KEYCODE_0, d.keyDownCode);
        assertFalse(d.onKeyDownReturn);
        mInstrumentation.sendKeyDownUpSync(KeyEvent.KEYCODE_BACK);
        assertEquals(KeyEvent.KEYCODE_BACK, d.keyDownCode);
        assertTrue(d.onKeyDownReturn);
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        method = "onKeyMultiple",
        args = {int.class, int.class, android.view.KeyEvent.class}
    )
    public void testOnKeyMultiple() {
        popDialog(DialogStubActivity.TEST_ONSTART_AND_ONSTOP);
        final TestDialog d = (TestDialog) mActivity.getDialog();
        assertNull(d.keyMultipleEvent);
        mInstrumentation.sendKeySync(
                new KeyEvent(KeyEvent.ACTION_MULTIPLE, KeyEvent.KEYCODE_UNKNOWN));
        assertTrue(d.isOnKeyMultipleCalled);
        assertFalse(d.onKeyMultipleReturn);
        assertEquals(KeyEvent.KEYCODE_UNKNOWN, d.keyMultipleEvent.getKeyCode());
        assertEquals(KeyEvent.ACTION_MULTIPLE, d.keyMultipleEvent.getAction());
    }
    @TestTargets({
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "onTouchEvent",
            args = {android.view.MotionEvent.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "setCanceledOnTouchOutside",
            args = {boolean.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "dispatchTouchEvent",
            args = {android.view.MotionEvent.class}
        )
    })
    public void testTouchEvent() {
        popDialog(DialogStubActivity.TEST_ONSTART_AND_ONSTOP);
        final TestDialog d = (TestDialog) mActivity.getDialog();
        assertNull(d.onTouchEvent);
        assertNull(d.touchEvent);
        assertFalse(d.isOnTouchEventCalled);
        MotionEvent touchMotionEvent = MotionEvent.obtain(MOTION_DOWN_TIME,
                MOTION_EVENT_TIME, MotionEvent.ACTION_DOWN,
                MOTION_X, MOTION_Y, 0);
        mInstrumentation.sendPointerSync(touchMotionEvent);
        assertFalse(d.dispatchTouchEventResult);
        assertMotionEventEquals(touchMotionEvent, d.touchEvent);
        assertTrue(d.isOnTouchEventCalled);
        assertMotionEventEquals(touchMotionEvent, d.onTouchEvent);
        d.isOnTouchEventCalled = false;
        assertTrue(d.isShowing());
        d.setCanceledOnTouchOutside(true);
        touchMotionEvent = MotionEvent.obtain(MOTION_DOWN_TIME + 1,
                MOTION_EVENT_TIME, MotionEvent.ACTION_DOWN,
                MOTION_X, MOTION_Y, 0);
        mInstrumentation.sendPointerSync(touchMotionEvent);
        assertTrue(d.dispatchTouchEventResult);
        assertMotionEventEquals(touchMotionEvent, d.touchEvent);
        assertTrue(d.isOnTouchEventCalled);
        assertMotionEventEquals(touchMotionEvent, d.onTouchEvent);
        assertFalse(d.isShowing());
    }
    @TestTargets({
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "onTrackballEvent",
            args = {android.view.MotionEvent.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "dispatchTrackballEvent",
            args = {android.view.MotionEvent.class}
        )
    })
    public void testTrackballEvent() {
        popDialog(DialogStubActivity.TEST_ONSTART_AND_ONSTOP);
        final TestDialog d = (TestDialog) mActivity.getDialog();
        final MotionEvent trackBallEvent = MotionEvent.obtain(MOTION_DOWN_TIME, MOTION_EVENT_TIME,
                MotionEvent.ACTION_DOWN, MOTION_X, MOTION_Y, 0);
        assertNull(d.trackballEvent);
        assertNull(d.onTrackballEvent);
        assertFalse(d.isOnTrackballEventCalled);
        mInstrumentation.sendTrackballEventSync(trackBallEvent);
        assertTrue(d.isOnTrackballEventCalled);
        assertMotionEventEquals(trackBallEvent, d.trackballEvent);
        assertMotionEventEquals(trackBallEvent, d.onTrackballEvent);
    }
    private void assertMotionEventEquals(final MotionEvent expected, final MotionEvent actual) {
        assertEquals(expected.getDownTime(), actual.getDownTime());
        assertEquals(expected.getEventTime(), actual.getEventTime());
        assertEquals(expected.getAction(), actual.getAction());
        assertEquals(expected.getMetaState(), actual.getMetaState());
        assertEquals(expected.getSize(), actual.getSize());
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        method = "onWindowAttributesChanged",
        args = {android.view.WindowManager.LayoutParams.class}
    )
    public void testOnWindowAttributesChanged() throws Throwable {
        popDialog(DialogStubActivity.TEST_ONSTART_AND_ONSTOP);
        final TestDialog d = (TestDialog) mActivity.getDialog();
        assertTrue(d.isOnWindowAttributesChangedCalled);
        d.isOnWindowAttributesChangedCalled = false;
        final WindowManager.LayoutParams lp = d.getWindow().getAttributes();
        lp.setTitle("test OnWindowAttributesChanged");
        runTestOnUiThread(new Runnable() {
            public void run() {
                d.getWindow().setAttributes(lp);
            }
        });
        mInstrumentation.waitForIdleSync();
        assertTrue(d.isOnWindowAttributesChangedCalled);
        assertSame(lp, d.getWindow().getAttributes());
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        method = "onContentChanged",
        args = {}
    )
    public void testOnContentChanged() throws Throwable {
        popDialog(DialogStubActivity.TEST_ONSTART_AND_ONSTOP);
        final TestDialog d = (TestDialog) mActivity.getDialog();
        assertNotNull(d);
        assertFalse(d.isOnContentChangedCalled);
        runTestOnUiThread(new Runnable() {
            public void run() {
                d.setContentView(R.layout.alert_dialog_text_entry);
            }
        });
        mInstrumentation.waitForIdleSync();
        assertTrue(d.isOnContentChangedCalled);
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        method = "onWindowFocusChanged",
        args = {boolean.class}
    )
    public void testOnWindowFocusChanged() throws Throwable {
        popDialog(DialogStubActivity.TEST_ONSTART_AND_ONSTOP);
        final TestDialog d = (TestDialog) mActivity.getDialog();
        assertTrue(d.isOnWindowFocusChangedCalled);
        d.isOnWindowFocusChangedCalled = false;
        runTestOnUiThread(new Runnable() {
            public void run() {
                mActivity.showDialog(DialogStubActivity.TEST_DIALOG_WITHOUT_THEME);
            }
        });
        mInstrumentation.waitForIdleSync();
        assertTrue(d.isOnWindowFocusChangedCalled);
    }
    @TestTargets({
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "dispatchKeyEvent",
            args = {android.view.KeyEvent.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "setOnKeyListener",
            args = {android.content.DialogInterface.OnKeyListener.class}
        )
    })
    public void testDispatchKeyEvent() {
        popDialog(DialogStubActivity.TEST_ONSTART_AND_ONSTOP);
        final TestDialog d = (TestDialog) mActivity.getDialog();
        sendKeys(KeyEvent.KEYCODE_0);
        assertFalse(d.dispatchKeyEventResult);
        assertEquals(KeyEvent.KEYCODE_0, d.keyEvent.getKeyCode());
        d.setOnKeyListener(new OnKeyListener() {
            public boolean onKey(DialogInterface dialog, int keyCode, KeyEvent event) {
                if (KeyEvent.ACTION_DOWN == event.getAction()) {
                    if (KeyEvent.KEYCODE_0 == keyCode) {
                        mIsKey0Listened = true;
                        return true;
                    }
                    if (KeyEvent.KEYCODE_1 == keyCode) {
                        mIsKey1Listened = true;
                        return true;
                    }
                }
                return false;
            }
        });
        mIsKey1Listened = false;
        sendKeys(KeyEvent.KEYCODE_1);
        assertTrue(mIsKey1Listened);
        mIsKey0Listened = false;
        sendKeys(KeyEvent.KEYCODE_0);
        assertTrue(mIsKey0Listened);
    }
    @TestTargets({
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "openOptionsMenu",
            args = {}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "closeOptionsMenu",
            args = {}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "onMenuOpened",
            args = {int.class, android.view.Menu.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "onCreatePanelView",
            args = {int.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "onCreatePanelMenu",
            args = {int.class, android.view.Menu.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "onCreateOptionsMenu",
            args = {android.view.Menu.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "onPreparePanel",
            args = {int.class, android.view.View.class, android.view.Menu.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "onPrepareOptionsMenu",
            args = {android.view.Menu.class}
        ),
        @TestTargetNew(
            level = TestLevel.PARTIAL,
            method = "onPanelClosed",
            args = {int.class, android.view.Menu.class}
        ),
        @TestTargetNew(
            level = TestLevel.PARTIAL,
            notes = "onOptionsMenuClosed should be called when onPanelClosed Called.",
            method = "onOptionsMenuClosed",
            args = {android.view.Menu.class}
        ),
        @TestTargetNew(
            level = TestLevel.PARTIAL,
            method = "onMenuItemSelected",
            args = {int.class, android.view.MenuItem.class}
        ),
        @TestTargetNew(
            level = TestLevel.PARTIAL,
            method = "onOptionsItemSelected",
            args = {MenuItem.class}
        )
    })
    @ToBeFixed(bug = "1716918", explanation = "As Javadoc of onMenuItemSelected() and "
            + "onPanelClosed(), onOptionsItemSelected() and onContextItemSelected() should be "
            + "called in onMenuItemSelected() source code, onOptionMenuClosed() and "
            + "onContextMenuClosed() should be called in onPanelClosed() source code, "
            + "but now onMenuItemSelected() and onPanelClosed() method are empty, is this a bug?")
    public void testOptionMenu() throws Throwable {
        popDialog(DialogStubActivity.TEST_ONSTART_AND_ONSTOP);
        final TestDialog d = (TestDialog) mActivity.getDialog();
        assertFalse(d.isOnMenuOpenedCalled);
        assertFalse(d.isOnCreatePanelViewCalled);
        assertFalse(d.isOnCreatePanelMenuCalled);
        assertFalse(d.isOnCreateOptionsMenuCalled);
        assertFalse(d.isOnPreparePanelCalled);
        assertFalse(d.isOnPrepareOptionsMenuCalled);
        dialogOpenOptionMenu(d);
        assertTrue(d.isOnMenuOpenedCalled);
        assertTrue(d.isOnCreatePanelViewCalled);
        assertTrue(d.isOnCreatePanelMenuCalled);
        assertTrue(d.isOnCreateOptionsMenuCalled);
        assertTrue(d.isOnPreparePanelCalled);
        assertTrue(d.isOnPrepareOptionsMenuCalled);
        assertFalse(d.isOnPanelClosedCalled);
        runTestOnUiThread(new Runnable() {
            public void run() {
                d.closeOptionsMenu();
            }
        });
        mInstrumentation.waitForIdleSync();
        assertTrue(d.isOnPanelClosedCalled);
        d.isOnCreatePanelViewCalled = false;
        d.isOnCreatePanelMenuCalled = false;
        d.isOnPreparePanelCalled = false;
        assertFalse(d.isOnOptionsMenuClosedCalled);
        dialogOpenOptionMenu(d);
        assertTrue(d.isOnCreatePanelViewCalled);
        assertFalse(d.isOnCreatePanelMenuCalled);
        assertTrue(d.isOnPreparePanelCalled);
        assertFalse(d.isOnOptionsMenuClosedCalled);
        assertFalse(d.isOnMenuItemSelectedCalled);
        assertFalse(d.isOnOptionsItemSelectedCalled);
        sendKeys(KeyEvent.KEYCODE_DPAD_CENTER);
        assertTrue(d.isOnMenuItemSelectedCalled);
        assertFalse(d.isOnOptionsItemSelectedCalled);
    }
    private void dialogOpenOptionMenu(final Dialog d) throws Throwable {
        runTestOnUiThread(new Runnable() {
            public void run() {
                d.openOptionsMenu();
            }
        });
        mInstrumentation.waitForIdleSync();
    }
    @TestTargets({
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "registerForContextMenu",
            args = {android.view.View.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "unregisterForContextMenu",
            args = {android.view.View.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "onCreateContextMenu",
            args = {android.view.ContextMenu.class, android.view.View.class,
                    android.view.ContextMenu.ContextMenuInfo.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "openContextMenu",
            args = {android.view.View.class}
        ),
        @TestTargetNew(
            level = TestLevel.PARTIAL,
            notes = "test method: onContextItemSelected",
            method = "onContextItemSelected",
            args = {android.view.MenuItem.class}
         ),
         @TestTargetNew(
             level = TestLevel.COMPLETE,
             notes = "test method: onContextMenuClosed",
             method = "onContextMenuClosed",
             args = {android.view.Menu.class}
         )
    })
    @ToBeFixed(bug = "1716918", explanation = "As Javadoc of onMenuItemSelected() and "
            + "onPanelClosed(), onOptionsItemSelected() and onContextItemSelected() should be "
            + "called in onMenuItemSelected() source code, onOptionMenuClosed() and "
            + "onContextMenuClosed() should be called in onPanelClosed() source code, "
            + "but now onMenuItemSelected() and onPanelClosed() method are empty, is this a bug?")
    public void testContextMenu() throws Throwable {
        popDialog(DialogStubActivity.TEST_ONSTART_AND_ONSTOP);
        final TestDialog d = (TestDialog) mActivity.getDialog();
        final LinearLayout parent = new LinearLayout(mContext);
        final MockView v = new MockView(mContext);
        parent.addView(v);
        assertFalse(v.isShowContextMenuCalled);
        runTestOnUiThread(new Runnable() {
            public void run() {
                d.addContentView(parent, new LinearLayout.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.WRAP_CONTENT));
                d.registerForContextMenu(v);
                d.openContextMenu(v);
            }
        });
        mInstrumentation.waitForIdleSync();
        assertTrue(v.isShowContextMenuCalled);
        assertTrue(d.isOnCreateContextMenuCalled);
        assertFalse(d.isOnPanelClosedCalled);
        assertFalse(d.isOnContextMenuClosedCalled);
        sendKeys(KeyEvent.KEYCODE_BACK);
        assertFalse(d.isOnPanelClosedCalled);
        assertFalse(d.isOnContextMenuClosedCalled);
        v.isShowContextMenuCalled = false;
        d.isOnCreateContextMenuCalled = false;
        runTestOnUiThread(new Runnable() {
            public void run() {
                d.unregisterForContextMenu(v);
            }
        });
        mInstrumentation.waitForIdleSync();
        runTestOnUiThread(new Runnable() {
            public void run() {
                d.openContextMenu(v);
            }
        });
        mInstrumentation.waitForIdleSync();
        assertTrue(v.isShowContextMenuCalled);
        assertFalse(d.isOnCreateContextMenuCalled);
        runTestOnUiThread(new Runnable() {
            public void run() {
                d.registerForContextMenu(v);
                d.openContextMenu(v);
            }
        });
        mInstrumentation.waitForIdleSync();
        assertFalse(d.isOnContextItemSelectedCalled);
        assertFalse(d.isOnMenuItemSelectedCalled);
        d.isOnPanelClosedCalled = false;
        assertFalse(d.isOnContextMenuClosedCalled);
        sendKeys(KeyEvent.KEYCODE_DPAD_CENTER);
        assertTrue(d.isOnMenuItemSelectedCalled);
        assertFalse(d.isOnContextItemSelectedCalled);
        assertTrue(d.isOnPanelClosedCalled);
        assertFalse(d.isOnContextMenuClosedCalled);
    }
    @TestTargetNew(
        level = TestLevel.NOT_FEASIBLE,
        method = "onSearchRequested",
        args = {}
    )
    @ToBeFixed(bug = "1695243", explanation = "From the javadoc of onSearchRequested,"
            + "we see it will be called when the user signals the desire to start a search."
            + "But there is a comment in it source code says \"not during dialogs, no.\","
            + "But onSearchRequested() didn't be called after start search.")
    public void testOnSearchRequested() {
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL,
        method = "takeKeyEvents",
        args = {boolean.class}
    )
    @ToBeFixed(bug = "1695243",
            explanation = "It still get KeyEvent while set takeKeyEvents to false")
    public void testTakeKeyEvents() throws Throwable {
        popDialog(DialogStubActivity.TEST_ONSTART_AND_ONSTOP);
        final TestDialog d = (TestDialog) mActivity.getDialog();
        final View v = d.getWindow().getDecorView();
        assertNull(d.getCurrentFocus());
        takeKeyEvents(d, true);
        assertTrue(v.isFocusable());
        sendKeys(KeyEvent.KEYCODE_0);
        assertEquals(KeyEvent.KEYCODE_0, d.keyEvent.getKeyCode());
        d.keyEvent = null;
        takeKeyEvents(d, false);
        assertNull(d.getCurrentFocus());
        assertFalse(v.isFocusable());
        sendKeys(KeyEvent.KEYCODE_0);
    }
    private void takeKeyEvents(final Dialog d, final boolean get) throws Throwable {
        runTestOnUiThread(new Runnable() {
            public void run() {
                d.takeKeyEvents(get);
            }
        });
        mInstrumentation.waitForIdleSync();
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        method = "requestWindowFeature",
        args = {int.class}
    )
    public void testRequestWindowFeature() {
        popDialog(DialogStubActivity.TEST_ONSTART_AND_ONSTOP);
        assertTrue(((TestDialog) mActivity.getDialog()).isRequestWindowFeature);
    }
    @TestTargetNew(
        level = TestLevel.NOT_FEASIBLE,
        notes = "There is no way to assert whether the drawable resource is properly shown."
            + "So we only call setFeatureDrawableResource once, with no asserts.",
        method = "setFeatureDrawableResource",
        args = {int.class, int.class}
    )
    public void testSetFeatureDrawableResource() throws Throwable {
        popDialog(DialogStubActivity.TEST_ONSTART_AND_ONSTOP);
        runTestOnUiThread(new Runnable() {
            public void run() {
                mActivity.getDialog().setFeatureDrawableResource(Window.FEATURE_LEFT_ICON,
                        R.drawable.robot);
            }
        });
        mInstrumentation.waitForIdleSync();
    }
    @TestTargetNew(
        level = TestLevel.NOT_FEASIBLE,
        notes = "There is no way to assert whether the drawable resource is properly shown."
            + "So we only call setFeatureDrawableUri once, with no asserts.",
        method = "setFeatureDrawableUri",
        args = {int.class, android.net.Uri.class}
    )
    public void testSetFeatureDrawableUri() {
        popDialog(DialogStubActivity.TEST_ONSTART_AND_ONSTOP);
        mActivity.getDialog().setFeatureDrawableUri(0, Uri.parse("http:
    }
    @TestTargetNew(
        level = TestLevel.NOT_FEASIBLE,
        notes = "There is no way to assert whether the drawable resource is properly shown."
            + "So we only call setFeatureDrawable once, with no asserts.",
        method = "setFeatureDrawable",
        args = {int.class, android.graphics.drawable.Drawable.class}
    )
    public void testSetFeatureDrawable() {
        popDialog(DialogStubActivity.TEST_ONSTART_AND_ONSTOP);
        mActivity.getDialog().setFeatureDrawable(0, new MockDrawable());
    }
    @TestTargetNew(
        level = TestLevel.NOT_FEASIBLE,
        notes = "There is no way to assert whether the drawable resource is properly shown."
            + "So we only call setFeatureDrawableAlpha once, with no asserts.",
        method = "setFeatureDrawableAlpha",
        args = {int.class, int.class}
    )
    public void testSetFeatureDrawableAlpha() {
        popDialog(DialogStubActivity.TEST_ONSTART_AND_ONSTOP);
        mActivity.getDialog().setFeatureDrawableAlpha(0, 0);
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        method = "getLayoutInflater",
        args = {}
    )
    public void testGetLayoutInflater() {
        popDialog(DialogStubActivity.TEST_DIALOG_WITHOUT_THEME);
        final Dialog d = mActivity.getDialog();
        assertEquals(d.getWindow().getLayoutInflater(), d.getLayoutInflater());
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        method = "setCancelable",
        args = {boolean.class}
    )
    public void testSetCancelable() {
        popDialog(DialogStubActivity.TEST_DIALOG_WITHOUT_THEME);
        final Dialog d = mActivity.getDialog();
        d.setCancelable(true);
        assertTrue(d.isShowing());
        sendKeys(KeyEvent.KEYCODE_BACK);
        assertFalse(d.isShowing());
        d.setCancelable(false);
        sendKeys(KeyEvent.KEYCODE_DPAD_CENTER);
        assertTrue(d.isShowing());
        sendKeys(KeyEvent.KEYCODE_BACK);
        assertTrue(d.isShowing());
    }
    @TestTargets({
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "cancel",
            args = {}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "setOnCancelListener",
            args = {android.content.DialogInterface.OnCancelListener.class}
        )
    })
    public void testCancel() throws Throwable {
        popDialog(DialogStubActivity.TEST_DIALOG_WITHOUT_THEME);
        final Dialog d = mActivity.getDialog();
        assertTrue(d.isShowing());
        mOnCancelListenerCalled = false;
        d.setOnCancelListener(new OnCancelListener() {
            public void onCancel(DialogInterface dialog) {
                mOnCancelListenerCalled = true;
            }
        });
        dialogCancel(d);
        assertFalse(d.isShowing());
        assertTrue(mOnCancelListenerCalled);
        sendKeys(KeyEvent.KEYCODE_DPAD_CENTER);
        assertTrue(d.isShowing());
        mOnCancelListenerCalled = false;
        d.setOnCancelListener(null);
        dialogCancel(d);
        assertFalse(d.isShowing());
        assertFalse(mOnCancelListenerCalled);
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "test method: setCancelMessage",
        method = "setCancelMessage",
        args = {android.os.Message.class}
    )
    public void testSetCancelMessage() throws Exception {
        mCalledCallback = false;
        popDialog(DialogStubActivity.TEST_ONSTART_AND_ONSTOP);
        final TestDialog d = (TestDialog) mActivity.getDialog();
        final HandlerThread ht = new HandlerThread("DialogTest");
        ht.start();
        d.setCancelMessage(new MockDismissCancelHandler(d, ht.getLooper()).obtainMessage(CANCEL,
                new OnCancelListener() {
                    public void onCancel(DialogInterface dialog) {
                        mCalledCallback = true;
                    }
                }));
        assertTrue(d.isShowing());
        assertFalse(mCalledCallback);
        sendKeys(KeyEvent.KEYCODE_BACK);
        assertTrue(mCalledCallback);
        assertFalse(d.isShowing());
        ht.join(100);
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        method = "setOnDismissListener",
        args = {android.content.DialogInterface.OnDismissListener.class}
    )
    public void testSetOnDismissListener() throws Throwable {
        mCalledCallback = false;
        popDialog(DialogStubActivity.TEST_DIALOG_WITHOUT_THEME);
        final Dialog d = mActivity.getDialog();
        d.setOnDismissListener(new OnDismissListener() {
            public void onDismiss(DialogInterface dialog) {
                mCalledCallback = true;
            }
        });
        assertTrue(d.isShowing());
        assertFalse(mCalledCallback);
        dialogDismiss(d);
        assertTrue(mCalledCallback);
        assertFalse(d.isShowing());
        sendKeys(KeyEvent.KEYCODE_DPAD_CENTER);
        assertTrue(d.isShowing());
        mCalledCallback = false;
        d.setOnDismissListener(null);
        dialogDismiss(d);
        assertFalse(mCalledCallback);
        assertFalse(d.isShowing());
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        method = "setDismissMessage",
        args = {android.os.Message.class}
    )
    public void testSetDismissMessage() throws Throwable {
        mCalledCallback = false;
        popDialog(DialogStubActivity.TEST_DIALOG_WITHOUT_THEME);
        final Dialog d = mActivity.getDialog();
        final HandlerThread ht = new HandlerThread("DialogTest");
        ht.start();
        d.setDismissMessage(new MockDismissCancelHandler(d, ht.getLooper()).obtainMessage(DISMISS,
                new OnDismissListener() {
                    public void onDismiss(DialogInterface dialog) {
                        mCalledCallback = true;
                    }
                }));
        assertTrue(d.isShowing());
        assertFalse(mCalledCallback);
        dialogDismiss(d);
        assertTrue(mCalledCallback);
        assertFalse(d.isShowing());
        ht.join(100);
    }
    private void dialogDismiss(final Dialog d) throws Throwable {
        runTestOnUiThread(new Runnable() {
            public void run() {
                d.dismiss();
            }
        });
        mInstrumentation.waitForIdleSync();
    }
    private void dialogCancel(final Dialog d) throws Throwable {
        runTestOnUiThread(new Runnable() {
            public void run() {
                d.cancel();
            }
        });
        mInstrumentation.waitForIdleSync();
    }
    private static class MockDismissCancelHandler extends Handler {
        private WeakReference<DialogInterface> mDialog;
        public MockDismissCancelHandler(Dialog dialog, Looper looper) {
            super(looper);
            mDialog = new WeakReference<DialogInterface>(dialog);
        }
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
            case DISMISS:
                ((OnDismissListener) msg.obj).onDismiss(mDialog.get());
                break;
            case CANCEL:
                ((OnCancelListener) msg.obj).onCancel(mDialog.get());
                break;
            }
        }
    }
    private static class MockDrawable extends Drawable {
        @Override
        public void draw(Canvas canvas) {
        }
        @Override
        public int getOpacity() {
            return 0;
        }
        @Override
        public void setAlpha(int alpha) {
        }
        @Override
        public void setColorFilter(ColorFilter cf) {
        }
    }
    private static class MockView extends View {
        public boolean isShowContextMenuCalled;
        public MockView(Context context) {
            super(context);
        }
        public OnCreateContextMenuListener getOnCreateContextMenuListener() {
            return mOnCreateContextMenuListener;
        }
        @Override
        public boolean showContextMenu() {
            isShowContextMenuCalled = true;
            return super.showContextMenu();
        }
    }
}
