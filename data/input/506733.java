public class AccountSetupOutgoingTests extends 
        ActivityInstrumentationTestCase2<AccountSetupOutgoing> {
    private AccountSetupOutgoing mActivity;
    private EditText mServerView;
    private Button mNextButton;
    public AccountSetupOutgoingTests() {
        super(AccountSetupOutgoing.class);
    }
    @Override
    protected void setUp() throws Exception {
        super.setUp();
        Intent i = getTestIntent("smtp:
        setActivityIntent(i);
    }
    public void testGoodUri() {
        getActivityAndFields();
        assertTrue(mNextButton.isEnabled());
    }
    public void testBadUriNoUser() {
        Intent i = getTestIntent("smtp:
        setActivityIntent(i);
        getActivityAndFields();
        assertFalse(mNextButton.isEnabled());
    }
    public void testBadUriNoPassword() {
        Intent i = getTestIntent("smtp:
        setActivityIntent(i);
        getActivityAndFields();
        assertFalse(mNextButton.isEnabled());
    }
    public void testGoodUriNoPort() {
        Intent i = getTestIntent("smtp:
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
    private Intent getTestIntent(String senderUriString) {
        EmailContent.Account account = new EmailContent.Account();
        account.setSenderUri(this.getInstrumentation().getTargetContext(), senderUriString);
        Intent i = new Intent(Intent.ACTION_MAIN);
        i.putExtra("account", account);     
        return i;
    }
}
