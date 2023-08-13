public class AccountSetupExchangeTests extends
        ActivityInstrumentationTestCase2<AccountSetupExchange> {
    private AccountSetupExchange mActivity;
    private EditText mServerView;
    private Button mNextButton;
    private CheckBox mSslRequiredCheckbox;
    private CheckBox mTrustAllCertificatesCheckbox;
    public AccountSetupExchangeTests() {
        super(AccountSetupExchange.class);
    }
    @Override
    protected void setUp() throws Exception {
        super.setUp();
        Intent i = getTestIntent("eas:
        setActivityIntent(i);
    }
    public void testGoodUri() {
        Intent i = getTestIntent("eas:
        setActivityIntent(i);
        getActivityAndFields();
        assertTrue(mNextButton.isEnabled());
    }
    public void testBadUriNoUser() {
        Intent i = getTestIntent("eas:
        setActivityIntent(i);
        getActivityAndFields();
        assertFalse(mNextButton.isEnabled());
    }
    public void testBadUriNoPassword() {
        Intent i = getTestIntent("eas:
        setActivityIntent(i);
        getActivityAndFields();
        assertFalse(mNextButton.isEnabled());
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
    @UiThreadTest
    public void testLoadFields() {
        getActivityAndFields();
        assertFalse(mSslRequiredCheckbox.isChecked());
        assertFalse(mTrustAllCertificatesCheckbox.isChecked());
        assertFalse(mTrustAllCertificatesCheckbox.getVisibility() == View.VISIBLE);
        Account account =
            ProviderTestUtils.setupAccount("account", false, mActivity.getBaseContext());
        account.mHostAuthRecv = ProviderTestUtils.setupHostAuth(
                "eas", "hostauth", 1, false, mActivity.getBaseContext());
        account.mHostAuthRecv.mFlags |= HostAuth.FLAG_SSL;
        account.mHostAuthRecv.mFlags &= ~HostAuth.FLAG_TRUST_ALL_CERTIFICATES;
        mActivity.loadFields(account);
        assertTrue(mSslRequiredCheckbox.isChecked());
        assertFalse(mTrustAllCertificatesCheckbox.isChecked());
        assertTrue(mTrustAllCertificatesCheckbox.getVisibility() == View.VISIBLE);
        account.mHostAuthRecv.mFlags |= HostAuth.FLAG_TRUST_ALL_CERTIFICATES;
        mActivity.loadFields(account);
        assertTrue(mSslRequiredCheckbox.isChecked());
        assertTrue(mTrustAllCertificatesCheckbox.isChecked());
        assertTrue(mTrustAllCertificatesCheckbox.getVisibility() == View.VISIBLE);
    }
    private void getActivityAndFields() {
        mActivity = getActivity();
        mServerView = (EditText) mActivity.findViewById(R.id.account_server);
        mNextButton = (Button) mActivity.findViewById(R.id.next);
        mSslRequiredCheckbox = (CheckBox) mActivity.findViewById(R.id.account_ssl);
        mTrustAllCertificatesCheckbox =
            (CheckBox) mActivity.findViewById(R.id.account_trust_certificates);
    }
    private Intent getTestIntent(String storeUriString) {
        EmailContent.Account account = new EmailContent.Account();
        account.setStoreUri(getInstrumentation().getTargetContext(), storeUriString);
        Intent i = new Intent(Intent.ACTION_MAIN);
        i.putExtra(AccountSetupExchange.EXTRA_ACCOUNT, account);
        i.putExtra(AccountSetupExchange.EXTRA_DISABLE_AUTO_DISCOVER, true);
        return i;
    }
}
