public class AccountSetupNamesTests extends ActivityInstrumentationTestCase2<AccountSetupNames> {
    private static final String EXTRA_ACCOUNT_ID = "accountId";
    private long mAccountId;
    private EmailContent.Account mAccount;
    private Context mContext;
    private AccountSetupNames mActivity;
    private Button mDoneButton;
    public AccountSetupNamesTests() {
        super(AccountSetupNames.class);
    }
    @Override
    protected void setUp() throws Exception {
        super.setUp();
        mContext = this.getInstrumentation().getTargetContext();
    }
    @Override
    protected void tearDown() throws Exception {
        if (mAccount != null) {
            Uri uri = ContentUris.withAppendedId(
                    EmailContent.Account.CONTENT_URI, mAccountId);
            mContext.getContentResolver().delete(uri, null, null);
        }
        super.tearDown();
    }
    public void testGoodAccountName() {
        Intent i = getTestIntent("imap", "GoodName");
        this.setActivityIntent(i);
        getActivityAndFields();
        assertTrue(mDoneButton.isEnabled());
    }
    public void testBadAccountName() {
        Intent i = getTestIntent("imap", "");
        this.setActivityIntent(i);
        getActivityAndFields();
        assertFalse(mDoneButton.isEnabled());
    }
    public void testEasAccountName() {
        Intent i = getTestIntent("eas", "");
        this.setActivityIntent(i);
        getActivityAndFields();
        assertTrue(mDoneButton.isEnabled());
    }
    private void getActivityAndFields() {
        mActivity = getActivity();
        mDoneButton = (Button) mActivity.findViewById(R.id.done);
    }
    private Intent getTestIntent(String protocol, String name) {
        mAccount = new EmailContent.Account();
        mAccount.setSenderName(name);
        HostAuth hostAuth = new HostAuth();
        hostAuth.mProtocol = protocol;
        mAccount.mHostAuthRecv = hostAuth;
        mAccount.save(mContext);
        mAccountId = mAccount.mId;
        Intent i = new Intent(Intent.ACTION_MAIN);
        i.putExtra(EXTRA_ACCOUNT_ID, mAccountId);
        return i;
    }
}
