public class AccountSetupIncomingTests extends 
        ActivityInstrumentationTestCase2<AccountSetupIncoming> {
    private static final String EXTRA_ACCOUNT = "account";
    private AccountSetupIncoming mActivity;
    private EditText mServerView;
    private Button mNextButton;
    public AccountSetupIncomingTests() {
        super(AccountSetupIncoming.class);
    }
    @Override
    protected void setUp() throws Exception {
        super.setUp();
        Intent i = getTestIntent("imap:
        setActivityIntent(i);
    }
    public void testGoodUri() {
        Intent i = getTestIntent("imap:
        setActivityIntent(i);
        getActivityAndFields();
        assertTrue(mNextButton.isEnabled());
    }
    public void testBadUriNoUser() {
        Intent i = getTestIntent("imap:
        setActivityIntent(i);
        getActivityAndFields();
        assertFalse(mNextButton.isEnabled());
    }
    public void testBadUriNoPassword() {
        Intent i = getTestIntent("imap:
        setActivityIntent(i);
        getActivityAndFields();
        assertFalse(mNextButton.isEnabled());
    }
    public void testGoodUriNoPort() {
        Intent i = getTestIntent("imap:
        setActivityIntent(i);
        getActivityAndFields();
        assertTrue(mNextButton.isEnabled());
    }
    @UiThreadTest
    public void testGoodServerVariants() {
        getActivityAndFields();
        assertTrue(mNextButton.isEnabled());
        mServerView.setText("  server.com  ");
        assertTrue(mNextButton.isEnabled());
    }
    @UiThreadTest
    public void testBadServerVariants() {
        getActivityAndFields();
        assertTrue(mNextButton.isEnabled());
        mServerView.setText("  ");
        assertFalse(mNextButton.isEnabled());
        mServerView.setText("serv$er.com");
        assertFalse(mNextButton.isEnabled());
    }
    private void getActivityAndFields() {
        mActivity = getActivity();
        mServerView = (EditText) mActivity.findViewById(R.id.account_server);
        mNextButton = (Button) mActivity.findViewById(R.id.next);
    }
    private Intent getTestIntent(String storeUriString) {
        EmailContent.Account account = new EmailContent.Account();
        account.setStoreUri(getInstrumentation().getTargetContext(), storeUriString);
        Intent i = new Intent(Intent.ACTION_MAIN);
        i.putExtra(EXTRA_ACCOUNT, account);
        return i;
    }
}
