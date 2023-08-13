public class MessageViewTests 
        extends ActivityInstrumentationTestCase2<MessageView> {
    private static final String EXTRA_MESSAGE_ID = "com.android.email.MessageView_message_id";
    private static final String EXTRA_MAILBOX_ID = "com.android.email.MessageView_mailbox_id";
    private TextView mToView;
    private TextView mSubjectView;
    private WebView mMessageContentView;
    private Context mContext;
    public MessageViewTests() {
        super(MessageView.class);
    }
    @Override
    protected void setUp() throws Exception {
        super.setUp();
        mContext = getInstrumentation().getTargetContext();
        Email.setServicesEnabled(mContext);
        Intent i = new Intent()
            .putExtra(EXTRA_MESSAGE_ID, Long.MIN_VALUE)
            .putExtra(EXTRA_MAILBOX_ID, Long.MIN_VALUE);
        this.setActivityIntent(i);
        MessagingController mockController = 
            new MockMessagingController(getActivity().getApplication());
        MessagingController.injectMockController(mockController);
        final MessageView a = getActivity();
        mToView = (TextView) a.findViewById(R.id.to);
        mSubjectView = (TextView) a.findViewById(R.id.subject);
        mMessageContentView = (WebView) a.findViewById(R.id.message_content);
        BinaryTempFileBody.setTempDirectory(getActivity().getCacheDir());
    }
    public void testPreconditions() {
        assertNotNull(mToView);
        assertEquals(0, mToView.length());
        assertNotNull(mSubjectView);
        assertEquals(0, mSubjectView.length());
        assertNotNull(mMessageContentView);
    }
    public void testAttachmentWritePermissions() throws FileNotFoundException, IOException {
        File file = null;
        try {
            if (!Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
                return;
            }
            file = MessageView.createUniqueFile(Environment.getExternalStorageDirectory(),
                    "write-test");
            OutputStream out = new FileOutputStream(file);
            out.write(1);
            out.close();
        } finally {
            try {
                if (file != null) {
                    if (file.exists()) {
                        file.delete();
                    }
                }
            } catch (Exception e) {
            }
        }
    }
    @Suppress
    public void testUiRaceConditions() {
        MessageView a = getActivity();
        a.onClick(a.findViewById(R.id.reply));
        a.onClick(a.findViewById(R.id.reply_all));
        a.onClick(a.findViewById(R.id.delete));
        a.onClick(a.findViewById(R.id.moveToOlder));
        a.onClick(a.findViewById(R.id.moveToNewer));
        a.onClick(a.findViewById(R.id.show_pictures));
        a.handleMenuItem(R.id.delete);
        a.handleMenuItem(R.id.reply);
        a.handleMenuItem(R.id.reply_all);
        a.handleMenuItem(R.id.forward);
        a.handleMenuItem(R.id.mark_as_unread);
    }
    @UiThreadTest
    public void testDisableReply() {
        MessageView a = getActivity();
        View replyButton = a.findViewById(R.id.reply);
        Intent i = new Intent();
        a.setIntent(i);
        a.initFromIntent();
        assertTrue(replyButton.isEnabled());
        i.putExtra(MessageView.EXTRA_DISABLE_REPLY, true);
        a.setIntent(i);
        a.initFromIntent();
        assertFalse(replyButton.isEnabled());
    }
    private static class MockMessagingController extends MessagingController {
        private MockMessagingController(Application application) {
            super(application);
        }
    }
}
