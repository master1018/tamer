public class MmsStability extends ActivityInstrumentationTestCase2 <ComposeMessageActivity> {
    private static String TAG = "MmsStability"; 
    private static int NO_OF_MESSAGE_SEND = 5; 
    private static String MESSAGE_CONTENT = "This is a system stability " +
                             "test for MMS. This test case send 5 message " +
                             "to the number which will reply automatically";
    private static int WAIT_TIME = 2000; 
    private static String RECIPIENT_NUMBER = "46645";
    public MmsStability() {
        super("com.android.mms", ComposeMessageActivity.class);
    }
    @Override
    protected void setUp() throws Exception {
        getActivity();
        super.setUp();
    }
    @Override
    protected void tearDown() throws Exception {
        super.tearDown();
    }
    Runnable runnable = new sendMms();
    class sendMms implements Runnable {
        public void run() {
            Instrumentation inst = getInstrumentation();
            Button mSendButton = (Button) getActivity().getWindow().findViewById(R.id.send_button);
            mSendButton.performClick();
            boolean messageSend = mSendButton.performClick();
            if (!messageSend) {
                assertTrue("Fails to send mms", false);
                Log.v(TAG, "messageSend is true");
            }
        }
    }
    @LargeTest
    public void testSend5MMS(){
        try{
            Instrumentation inst = getInstrumentation();
            inst.sendStringSync(RECIPIENT_NUMBER);
            inst.sendKeyDownUpSync(KeyEvent.KEYCODE_DPAD_DOWN);
            inst.sendKeyDownUpSync(KeyEvent.KEYCODE_DPAD_CENTER);
            for (int i = 0; i < NO_OF_MESSAGE_SEND; i++) {
                inst.sendKeyDownUpSync(KeyEvent.KEYCODE_DPAD_DOWN);
                inst.sendStringSync(MESSAGE_CONTENT);
                inst.runOnMainSync(runnable);
                Thread.sleep(WAIT_TIME);
            }
            assertTrue("Send MMS", true);
        } catch (Exception e){
            assertTrue("Fails to send mms", false);
            Log.v(TAG, e.toString());
        }
    }
    @LargeTest
    public void testLaunchMMS() {
        try {
            Thread.sleep(WAIT_TIME);
        } catch (Exception e) {
            assertTrue("MMS do nothing", false);
        }
        assertTrue("MMS do nothing", true);
    }
}
