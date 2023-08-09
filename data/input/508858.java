@TestTargetClass(Chronometer.class)
public class ChronometerTest extends ActivityInstrumentationTestCase2<ChronometerStubActivity> {
    private ChronometerStubActivity mActivity;
    public ChronometerTest() {
        super("com.android.cts.stub", ChronometerStubActivity.class);
    }
    @Override
    protected void setUp() throws Exception {
        super.setUp();
        mActivity = getActivity();
    }
    @TestTargets({
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "Chronometer",
            args = {android.content.Context.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "Chronometer",
            args = {android.content.Context.class, android.util.AttributeSet.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "Chronometer",
            args = {android.content.Context.class, android.util.AttributeSet.class, int.class}
        )
    })
    public void testConstructor() {
        new Chronometer(mActivity);
        new Chronometer(mActivity, null);
        new Chronometer(mActivity, null, 0);
    }
    @TestTargets({
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "getBase",
            args = {}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "setBase",
            args = {long.class}
        )
    })
    @UiThreadTest
    public void testAccessBase() {
        Chronometer chronometer = mActivity.getChronometer();
        CharSequence oldText = chronometer.getText();
        int expected = 100000;
        chronometer.setBase(expected);
        assertEquals(expected, chronometer.getBase());
        assertNotSame(oldText, chronometer.getText());
        expected = 100;
        oldText = chronometer.getText();
        chronometer.setBase(expected);
        assertEquals(expected, chronometer.getBase());
        assertNotSame(oldText, chronometer.getText());
        expected = -1;
        oldText = chronometer.getText();
        chronometer.setBase(expected);
        assertEquals(expected, chronometer.getBase());
        assertNotSame(oldText, chronometer.getText());
        expected = Integer.MAX_VALUE;
        oldText = chronometer.getText();
        chronometer.setBase(expected);
        assertEquals(expected, chronometer.getBase());
        assertNotSame(oldText, chronometer.getText());
    }
    @TestTargets({
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "getFormat",
            args = {}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "setFormat",
            args = {java.lang.String.class}
        )
    })
    @UiThreadTest
    public void testAccessFormat() {
        Chronometer chronometer = mActivity.getChronometer();
        String expected = "header-%S-trail";
        chronometer.setFormat(expected);
        assertEquals(expected, chronometer.getFormat());
        chronometer.start();
        String text = chronometer.getText().toString();
        assertTrue(text.startsWith("header"));
        assertTrue(text.endsWith("trail"));
    }
    @TestTargets({
        @TestTargetNew(
            level = TestLevel.NOT_NECESSARY,
            method = "onWindowVisibilityChanged",
            args = {int.class}
        ),
        @TestTargetNew(
            level = TestLevel.NOT_NECESSARY,
            method = "onDetachedFromWindow",
            args = {}
        )
    })
    public void testFoo() {
    }
    @TestTargets({
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "start",
            args = {}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "stop",
            args = {}
        )
    })
    public void testStartAndStop() throws Throwable {
        final Chronometer chronometer = mActivity.getChronometer();
        runTestOnUiThread(new Runnable() {
            public void run() {
                CharSequence expected = chronometer.getText();
                chronometer.start();
                assertNotSame(expected, chronometer.getText());
            }
        });
        getInstrumentation().waitForIdleSync();
        CharSequence expected = chronometer.getText();
        Thread.sleep(1500);
        assertFalse(expected.equals(chronometer.getText()));
        runTestOnUiThread(new Runnable() {
            public void run() {
                CharSequence expected = chronometer.getText();
                chronometer.stop();
                assertSame(expected, chronometer.getText());
            }
        });
        getInstrumentation().waitForIdleSync();
        expected = chronometer.getText();
        Thread.sleep(1500);
        assertTrue(expected.equals(chronometer.getText()));
    }
    @TestTargets({
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "getOnChronometerTickListener",
            args = {}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "setOnChronometerTickListener",
            args = {OnChronometerTickListener.class}
        )
    })
    public void testAccessOnChronometerTickListener() throws Throwable {
        final Chronometer chronometer = mActivity.getChronometer();
        final MockOnChronometerTickListener listener = new MockOnChronometerTickListener();
        runTestOnUiThread(new Runnable() {
            public void run() {
                chronometer.setOnChronometerTickListener(listener);
                chronometer.start();
            }
        });
        getInstrumentation().waitForIdleSync();
        assertEquals(listener, chronometer.getOnChronometerTickListener());
        assertTrue(listener.hasCalledOnChronometerTick());
        listener.reset();
        Thread.sleep(1500);
        assertTrue(listener.hasCalledOnChronometerTick());
    }
    private static class MockOnChronometerTickListener implements OnChronometerTickListener {
        private boolean mCalledOnChronometerTick = false;
        public void onChronometerTick(Chronometer chronometer) {
            mCalledOnChronometerTick = true;
        }
        public boolean hasCalledOnChronometerTick() {
            return mCalledOnChronometerTick;
        }
        public void reset() {
            mCalledOnChronometerTick = false;
        }
    }
}
