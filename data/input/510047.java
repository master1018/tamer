public class InterceptSendSms extends ActivityInstrumentationTestCase2 <ComposeMessageActivity> {
    private static String TAG = "InterceptSendSms";
    private static int WAIT_TIME = 4000; 
    private static String RECIPIENTS = "4258365497,4258365496";
    private static String MESSAGE = "This is a test message of intercepting a SMS";
    private InterceptSmsReceiver mInterceptReceiver;
    private TextView mRecipientsView;
    private EditText mTextEditor;
    private boolean mInterceptedSend;
    public InterceptSendSms() {
        super("com.android.mms", ComposeMessageActivity.class);
    }
    @Override
    protected void setUp() throws Exception {
        Activity activity = getActivity();
        super.setUp();
        mRecipientsView = (TextView)activity.findViewById(R.id.recipients_editor);
        mTextEditor = (EditText)activity.findViewById(R.id.embedded_text_editor);
        mInterceptReceiver = new InterceptSmsReceiver();
        IntentFilter filter = new IntentFilter(WorkingMessage.ACTION_SENDING_SMS);
        activity.registerReceiver(mInterceptReceiver, filter);
    }
    @Override
    protected void tearDown() throws Exception {
        getActivity().unregisterReceiver(mInterceptReceiver);
        super.tearDown();
    }
    Runnable runnable = new sendMms();
    class sendMms implements Runnable {
        public void run() {
            Instrumentation inst = getInstrumentation();
            mRecipientsView.setText(RECIPIENTS);
            mTextEditor.setText(MESSAGE);
            Button mSendButton = (Button) getActivity().getWindow().findViewById(R.id.send_button);
            mSendButton.performClick();
            Log.v(TAG, "sendMms hitting send now");
            boolean messageSend = mSendButton.performClick();
            if (!messageSend) {
                assertTrue("Fails to send mms", false);
                Log.v(TAG, "messageSend is true");
            }
        }
    }
    @LargeTest
    public void testInterceptSendSms(){
        try{
            Instrumentation inst = getInstrumentation();
            inst.runOnMainSync(runnable);
            Thread.sleep(WAIT_TIME);
            assertTrue("Intercepted send SMS", mInterceptedSend);
        } catch (Exception e){
            assertTrue("Failed to send sms", false);
            Log.v(TAG, e.toString());
        }
    }
    public class InterceptSmsReceiver extends BroadcastReceiver {
        public void onReceive(Context context, Intent intent) {
            Log.v(TAG, "doReceive: " + intent);
            mInterceptedSend = true;
            final String msgText = intent.getStringExtra(WorkingMessage.EXTRA_SMS_MESSAGE);
            final String semiSepRecipients =
                intent.getStringExtra(WorkingMessage.EXTRA_SMS_RECIPIENTS);
            final long threadId = intent.getLongExtra(WorkingMessage.EXTRA_SMS_THREAD_ID, 0);
            assertEquals(msgText, MESSAGE);
            assertEquals(semiSepRecipients, RECIPIENTS.replace(',', ';'));
            assertTrue(threadId > 0);
            setResultCode(android.app.Activity.RESULT_OK);
        }
    }
}
