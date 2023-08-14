public class LongThreadTest
extends ActivityInstrumentationTestCase2<ComposeMessageActivity> {
    private TextView mRecipientsView;
    private EditText mTextEditor;
    private EditText mSubjectTextEditor;    
    static final String TAG = "LongThreadTest";
    private ArrayList<String> mWords;
    private ArrayList<String> mRecipients;
    private int mWordCount;
    private Random mRandom = new Random();
    public LongThreadTest() {
        super("com.android.mms", ComposeMessageActivity.class);
    }
    @Override
    protected void setUp() throws Exception {
        super.setUp();
        ComposeMessageActivity a = getActivity();
        mRecipientsView = (TextView)a.findViewById(R.id.recipients_editor);
        mTextEditor = (EditText)a.findViewById(R.id.embedded_text_editor);
        mSubjectTextEditor = (EditText)a.findViewById(R.id.subject);
        mWords = new ArrayList<String>(98568);      
        StringBuilder sb = new StringBuilder();
        try {
            Log.v(TAG, "Loading dictionary of words");
            FileInputStream words = a.openFileInput("words");
            int c;
            while ((c = words.read()) != -1) {
                if (c == '\r' || c == '\n') {
                    String word = sb.toString().trim();
                    if (word.length() > 0) {
                        mWords.add(word);
                    }
                    sb.setLength(0);
                } else {
                    sb.append((char)c);
                }
            }
            words.close();
            mWordCount = mWords.size();
            Log.v(TAG, "Loaded dictionary word count: " + mWordCount);
        } catch (Exception e) {
            Log.e(TAG, "can't open words file at /data/data/com.android.mms/files/words");
            return;
        }
        mRecipients = new ArrayList<String>();
        try {
            Log.v(TAG, "Loading recipients");
            FileInputStream recipients = a.openFileInput("recipients");
            int c;
            while ((c = recipients.read()) != -1) {
                if (c == '\r' || c == '\n' || c == ',') {
                    String recipient = sb.toString().trim();
                    if (recipient.length() > 0) {
                        mRecipients.add(recipient);
                    }
                    sb.setLength(0);
                } else {
                    sb.append((char)c);
                }
            }
            recipients.close();
            Log.v(TAG, "Loaded recipients: " + mRecipients.size());
        } catch (Exception e) {
            Log.e(TAG, "can't open recipients file at /data/data/com.android.mms/files/recipients");
            return;
        }
    }
    private String generateMessage() {
        int wordsInMessage = mRandom.nextInt(9) + 1;   
        StringBuilder msg = new StringBuilder();
        for (int i = 0; i < wordsInMessage; i++) {
            msg.append(mWords.get(mRandom.nextInt(mWordCount)) + " ");
        }
        return msg.toString();
    }
    private class AddSubjectMenuItem implements MenuItem {
        private static final int MENU_ADD_SUBJECT = 0;
        public char getAlphabeticShortcut() {
            return 0;
        }
        public int getGroupId() {
            return 0;
        }
        public Drawable getIcon() {
            return null;
        }
        public Intent getIntent() {
            return null;
        }
        public int getItemId() {
            return MENU_ADD_SUBJECT;
        }
        public ContextMenuInfo getMenuInfo() {
            return null;
        }
        public char getNumericShortcut() {
            return 0;
        }
        public int getOrder() {
            return 0;
        }
        public SubMenu getSubMenu() {
            return null;
        }
        public CharSequence getTitle() {
            return null;
        }
        public CharSequence getTitleCondensed() {
            return null;
        }
        public boolean hasSubMenu() {
            return false;
        }
        public boolean isCheckable() {
            return false;
        }
        public boolean isChecked() {
            return false;
        }
        public boolean isEnabled() {
            return false;
        }
        public boolean isVisible() {
            return false;
        }
        public MenuItem setAlphabeticShortcut(char alphaChar) {
            return null;
        }
        public MenuItem setCheckable(boolean checkable) {
            return null;
        }
        public MenuItem setChecked(boolean checked) {
            return null;
        }
        public MenuItem setEnabled(boolean enabled) {
            return null;
        }
        public MenuItem setIcon(Drawable icon) {
            return null;
        }
        public MenuItem setIcon(int iconRes) {
            return null;
        }
        public MenuItem setIntent(Intent intent) {
            return null;
        }
        public MenuItem setNumericShortcut(char numericChar) {
            return null;
        }
        public MenuItem setOnMenuItemClickListener(
                OnMenuItemClickListener menuItemClickListener) {
            return null;
        }
        public MenuItem setShortcut(char numericChar, char alphaChar) {
            return null;
        }
        public MenuItem setTitle(CharSequence title) {
            return null;
        }
        public MenuItem setTitle(int title) {
            return null;
        }
        public MenuItem setTitleCondensed(CharSequence title) {
            return null;
        }
        public MenuItem setVisible(boolean visible) {
            return null;
        }
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
            mTextEditor.setText(generateMessage());
            final ComposeMessageActivity a = getActivity();
            Button send = (Button)a.findViewById(R.id.send_button);
            send.performClick();
        }
    };
    private MessageRunnable mSendMmsMessage = new MessageRunnable() {
        public void run() {
            if (mRecipientsView.getVisibility() == View.VISIBLE) {
                mRecipientsView.setText(mRecipient);
            }
            final ComposeMessageActivity a = getActivity();
            MenuItem item = new AddSubjectMenuItem();
            a.onOptionsItemSelected(item);
            mSubjectTextEditor.setText(generateMessage());
            mTextEditor.setText(generateMessage());
            Button send = (Button)a.findViewById(R.id.send_button);
            send.performClick();
        }
    };
    @LargeTest
    public void testSendManyMessages() throws Throwable {
        final int MAXSEND = 30;
        final int MSG_PER_RECIPIENT = MAXSEND / mRecipients.size();
        final int MMS_FREQ = Math.min(MSG_PER_RECIPIENT / 10, 1);
        final ComposeMessageActivity a = getActivity();
        for (String recipient : mRecipients) {
            a.runOnUiThread(new Runnable() {
                public void run() {
                    a.initialize(null);
                    a.loadMessageContent();
                }
            });
            for (int i = 0; i < MSG_PER_RECIPIENT; i++) {
                Log.v(TAG, "Sending msg: " + i);
                if (i % MMS_FREQ == 0) {
                    mSendMmsMessage.setRecipient(recipient);
                    runTestOnUiThread(mSendMmsMessage);
                } else {
                    mSendSmsMessage.setRecipient(recipient);
                    runTestOnUiThread(mSendSmsMessage);
                }
                Thread.sleep(5000);     
            }
        }
        assertTrue(true);
    }
}
