public class ComposeMessageActivityTests
extends ActivityInstrumentationTestCase2<ComposeMessageActivity> {
    private Context mContext;
    private TextView mRecipientsView;
    private EditText mTextEditor;
    private MessageListView mMsgListView;
    private MessageListAdapter mMsgListAdapter;
    private ColumnsMap mColumnsMap;
    public ComposeMessageActivityTests() {
        super("com.android.mms", ComposeMessageActivity.class);
    }
    @Override
    protected void setUp() throws Exception {
        super.setUp();
        mContext = getInstrumentation().getTargetContext();
        ComposeMessageActivity a = getActivity();
        mRecipientsView = (TextView)a.findViewById(R.id.recipients_editor);
        mTextEditor = (EditText)a.findViewById(R.id.embedded_text_editor);
        mMsgListView = (MessageListView)a.findViewById(R.id.history);
        mMsgListAdapter = (MessageListAdapter)mMsgListView.getAdapter();
    }
    class BoxChecker {
        private int[] mExpectedBoxStates;
        private boolean mDone;
        private String mError;
        public BoxChecker(int[] expectedBoxStates) {
            mExpectedBoxStates = expectedBoxStates;
            mDone = false;
            mError = null;
            mMsgListAdapter.setOnDataSetChangedListener(mDataSetChangedListener);
        }
        private final MessageListAdapter.OnDataSetChangedListener
        mDataSetChangedListener = new MessageListAdapter.OnDataSetChangedListener() {
            public void onDataSetChanged(MessageListAdapter adapter) {
                int count = adapter.getCount();
                if (count > 0) {
                    MessageItem item = getMessageItem(count - 1);   
                    int boxId = item.getBoxId();
                    boolean found = false;
                    boolean isLast = false;
                    for (int i = 0; i < mExpectedBoxStates.length; i++) {
                        if (mExpectedBoxStates[i] == boxId) {
                            found = true;
                            isLast = i == mExpectedBoxStates.length - 1;
                            break;
                        }
                    }
                    if (!found) {
                        setError("Unexpected box state");
                        return;
                    }
                    if (isLast) {
                        mDone = true;
                    }
               }
            }
            public void onContentChanged(MessageListAdapter adapter) {
            }
        };
        private void setError(String error) {
            mError = error;
            mDone = true;
        }
        public String getError() {
            return mError;
        }
        public boolean isDone() {
            return mDone;
        }
        private MessageItem getMessageItem(int index) {
            Cursor cursor = (Cursor)mMsgListAdapter.getItem(index);
            mColumnsMap = new MessageListAdapter.ColumnsMap(cursor);
            String type = cursor.getString(mColumnsMap.mColumnMsgType);
            long msgId = cursor.getLong(mColumnsMap.mColumnMsgId);
            MessageItem msgItem = mMsgListAdapter.getCachedMessageItem(type, msgId, cursor);
            return msgItem;
        }
}
    @LargeTest
    public void testSendMessage() throws Throwable {
        final ComposeMessageActivity a = getActivity();
        runTestOnUiThread(new Runnable() {
            public void run() {
                checkFocused(mRecipientsView);
                mRecipientsView.setText("2012130903");
                mTextEditor.setText("This is a test message");
                Button send = (Button)a.findViewById(R.id.send_button);
                send.performClick();
            }
        });
        BoxChecker boxChecker = new BoxChecker(new int[] {4, 2});    
        long now = System.currentTimeMillis();
        boolean success = true;
        while (!boxChecker.isDone()) {
            Thread.sleep(1000);
            if (System.currentTimeMillis() - now > 10000) {
                success = false;
                break;
            }
        }
        assertTrue(success && boxChecker.getError() == null);
    }
    @SmallTest
    private void checkFocused(View focused) {
        assertEquals(focused == mRecipientsView, mRecipientsView.isFocused());
        assertEquals(focused == mTextEditor, mTextEditor.isFocused());
    }
}
