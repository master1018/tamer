public class AutoCompleteTextViewPopup
        extends ActivityInstrumentationTestCase2<AutoCompleteTextViewSimple> {
    private static final int SLEEP_TIME = 50;
    private static final int LOOP_AMOUNT = 10;
    public AutoCompleteTextViewPopup() {
        super("com.android.frameworks.coretests", AutoCompleteTextViewSimple.class);
    }
    @FlakyTest(tolerance=3)
    public void testPopupSetListSelection() throws Throwable {
        AutoCompleteTextViewSimple theActivity = getActivity();
        final AutoCompleteTextView textView = theActivity.getTextView();
        final Instrumentation instrumentation = getInstrumentation();
        textView.requestFocus();
        instrumentation.waitForIdleSync();
        sendKeys("A");
        waitAssertListSelection(textView, ListView.INVALID_POSITION);
        runTestOnUiThread(new Runnable() {
            public void run() {
                textView.setListSelection(0);
            }
        });
        instrumentation.waitForIdleSync();
        waitAssertListSelection("set selection to (0)", textView, 0);
        sendKeys("DPAD_DOWN");
        waitAssertListSelection("move selection to (1)", textView, 1);
        clearText(textView);
    }
    @FlakyTest(tolerance=3)
    public void testPopupGetListSelection() throws Throwable {
        AutoCompleteTextViewSimple theActivity = getActivity();
        final AutoCompleteTextView textView = theActivity.getTextView();
        final Instrumentation instrumentation = getInstrumentation();
        textView.requestFocus();
        instrumentation.waitForIdleSync();
        sendKeys("A");
        waitAssertListSelection(textView, ListView.INVALID_POSITION);
        sendKeys("DPAD_DOWN");
        waitAssertListSelection("move selection to (0)", textView, 0);
        sendKeys("DPAD_DOWN");
        waitAssertListSelection("move selection to (1)", textView, 1);
        clearText(textView);
    }
    @FlakyTest(tolerance=3)
    public void testPopupClearListSelection() throws Throwable {
        AutoCompleteTextViewSimple theActivity = getActivity();
        final AutoCompleteTextView textView = theActivity.getTextView();
        final Instrumentation instrumentation = getInstrumentation();
        textView.requestFocus();
        instrumentation.waitForIdleSync();
        sendKeys("A");
        waitAssertListSelection(textView, ListView.INVALID_POSITION);
        sendKeys("DPAD_DOWN");
        waitAssertListSelection(textView, 0);
        runTestOnUiThread(new Runnable() {
            public void run() {
                textView.clearListSelection();
            }
        });
        instrumentation.waitForIdleSync();
        waitAssertListSelection("setListSelection(ListView.INVALID_POSITION)", textView,
                ListView.INVALID_POSITION);
        clearText(textView);
    }
    @FlakyTest(tolerance=3)
    public void testPopupNavigateNoAdapter() throws Throwable {
        AutoCompleteTextViewSimple theActivity = getActivity();
        final AutoCompleteTextView textView = theActivity.getTextView();
        final Instrumentation instrumentation = getInstrumentation();
        textView.requestFocus();
        instrumentation.waitForIdleSync();
        sendKeys("A");
         waitAssertListSelection(textView, ListView.INVALID_POSITION);
        sendKeys("DPAD_DOWN");
        waitAssertListSelection(textView, 0);
        runTestOnUiThread(new Runnable() {
            public void run() {
                textView.setAdapter((ArrayAdapter<?>) null);
            }
        });
        instrumentation.waitForIdleSync();
        sendKeys("DPAD_DOWN");
        clearText(textView);
    }
    @FlakyTest(tolerance=3)
    public void testPopupShow() throws Throwable {
        AutoCompleteTextViewSimple theActivity = getActivity();
        final AutoCompleteTextView textView = theActivity.getTextView();
        final Instrumentation instrumentation = getInstrumentation();
        assertFalse("isPopupShowing() on start", textView.isPopupShowing());
        textView.requestFocus();
        instrumentation.waitForIdleSync();
        sendKeys("A");
        waitAssertPopupShowState("isPopupShowing() after typing", textView, true);
        runTestOnUiThread(new Runnable() {
            public void run() {
                textView.setText("");
            }
        });
        instrumentation.waitForIdleSync();
        waitAssertPopupShowState("isPopupShowing() after text cleared", textView, false);
        runTestOnUiThread(new Runnable() {
            public void run() {
                textView.setText("a", false);
            }
        });
        instrumentation.waitForIdleSync();
        waitAssertPopupShowState("isPopupShowing() after setText(\"a\", false)", textView, false);
        runTestOnUiThread(new Runnable() {
            public void run() {
                textView.setText("a");
            }
        });
        instrumentation.waitForIdleSync();
        waitAssertPopupShowState("isPopupShowing() after text set", textView, true);
        clearText(textView);
    }
    private void waitAssertPopupShowState(String message, AutoCompleteTextView textView,
            boolean expected) throws InterruptedException {
        for (int i = 0; i < LOOP_AMOUNT; i++) {
            if (textView.isPopupShowing() == expected) {
                return;
            }
            Thread.sleep(SLEEP_TIME);
        }
        assertEquals(message, expected, textView.isPopupShowing());
    }
    private void waitAssertListSelection(AutoCompleteTextView textView, int expected)
            throws Exception {
        waitAssertListSelection("getListSelection()", textView, expected);
    }
    private void waitAssertListSelection(String message, AutoCompleteTextView textView,
            int expected) throws Exception {
        int currentSelection = ListView.INVALID_POSITION;
        for (int i = 0; i < LOOP_AMOUNT; i++) {
            currentSelection = textView.getListSelection();
            if (expected == currentSelection) {
                return;
            }
            Thread.sleep(SLEEP_TIME);
        }
        assertEquals(message, expected, textView.getListSelection());
    }
    private void clearText(final AutoCompleteTextView textView) throws Throwable {
        runTestOnUiThread(new Runnable() {
            public void run() {
                textView.setText("");
            }
        });
    }
}
