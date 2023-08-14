public class MultiPartSmsTests
extends ActivityInstrumentationTestCase2<ComposeMessageActivity> {
    private TextView mRecipientsView;
    private EditText mTextEditor;
    static final String TAG = "MultiPartSmsTests";
    final String mLongMessage =
        "Is this a dagger which I see before me,"
        +" The handle toward my hand? Come, let me clutch thee."
        +" I have thee not, and yet I see thee still."
        +" Art thou not, fatal vision, sensible"
        +" To feeling as to sight? or art thou but"
        +" A dagger of the mind, a false creation,"
        +" Proceeding from the heat-oppressed brain?"
        +" I see thee yet, in form as palpable"
        +" As this which now I draw.";
    private String mMyNumber;
    public MultiPartSmsTests() {
        super("com.android.mms", ComposeMessageActivity.class);
    }
    @Override
    protected void setUp() throws Exception {
        super.setUp();
        ComposeMessageActivity a = getActivity();
        mRecipientsView = (TextView)a.findViewById(R.id.recipients_editor);
        mTextEditor = (EditText)a.findViewById(R.id.embedded_text_editor);
        mMyNumber = MessageUtils.getLocalNumber();
        assertNotNull("null number for this phone", mMyNumber);
        mMyNumber = "6502782055";
    }
    private abstract class MessageRunnable implements Runnable {
        protected String mRecipient;
        public void setRecipient(String recipient) {
            mRecipient = recipient;
        }
    }
    private MessageRunnable mSendSmsMessage = new MessageRunnable() {
        public void run() {
            if (mRecipientsView.getVisibility() == View.VISIBLE) {
                mRecipientsView.setText(mRecipient);
            }
            mTextEditor.setText(mLongMessage);
            final ComposeMessageActivity a = getActivity();
            Button send = (Button)a.findViewById(R.id.send_button);
            send.performClick();
        }
    };
    @LargeTest
    public void testLongSmsMessage() throws Throwable {
        final ComposeMessageActivity a = getActivity();
        a.runOnUiThread(new Runnable() {
            public void run() {
                a.initialize(null);
                a.loadMessageContent();
            }
        });
        int msgCount = a.mMsgListAdapter.getCount();
        mSendSmsMessage.setRecipient(mMyNumber);
        runTestOnUiThread(mSendSmsMessage);
        boolean received = false;
        for (int i = 0; i < 100; i++) {
            Thread.sleep(5000);     
            if (msgCount + 2 >= a.mMsgListAdapter.getCount()) {
                Cursor cursor = a.mMsgListAdapter.getCursor();
                cursor.moveToLast();
                String type = cursor.getString(COLUMN_MSG_TYPE);
                long msgId = cursor.getLong(COLUMN_ID);
                MessageItem msgItem = a.mMsgListAdapter.getCachedMessageItem(type, msgId, cursor);
                assertNotNull("got a null last MessageItem", msgItem);
                assertEquals("The sent and received messages aren't the same",
                        mLongMessage,
                        msgItem.mBody);
                received = true;
                break;
            }
        }
        assertTrue("Never received the sent message", received);
    }
}
