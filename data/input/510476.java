public class AccountSetupAccountTypeUnitTests
        extends ActivityUnitTestCase<AccountSetupAccountType> {
    private static final String EXTRA_ACCOUNT = "account";
    Context mContext;
    private HashSet<Account> mAccounts = new HashSet<Account>();
    public AccountSetupAccountTypeUnitTests() {
        super(AccountSetupAccountType.class);
      }
    @Override
    protected void setUp() throws Exception {
        super.setUp();
        mContext = this.getInstrumentation().getTargetContext();
    }
    @Override
    protected void tearDown() throws Exception {
        for (Account account : mAccounts) {
            Uri uri = ContentUris.withAppendedId(Account.CONTENT_URI, account.mId);
            mContext.getContentResolver().delete(uri, null, null);
        }
        super.tearDown();
    }
    public void testStoreTypeLimits() {
        EmailContent.Account acct1 = createTestAccount("scheme1");
        EmailContent.Account acct2 = createTestAccount("scheme1");
        EmailContent.Account acct3 = createTestAccount("scheme2");
        AccountSetupAccountType activity = startActivity(getTestIntent(acct1), null, null);
        Store.StoreInfo info = new Store.StoreInfo();
        info.mAccountInstanceLimit = -1;
        info.mScheme = "scheme1";
        assertTrue("no limit", activity.checkAccountInstanceLimit(info));
        info.mAccountInstanceLimit = 3;
        assertTrue("limit, but not reached", activity.checkAccountInstanceLimit(info));
        info.mAccountInstanceLimit = 2;
        assertFalse("limit, reached", activity.checkAccountInstanceLimit(info));
    }
    public void testEasOffered() {
        Account acct1 = createTestAccount("scheme1");
        AccountSetupAccountType activity = startActivity(getTestIntent(acct1), null, null);
        View exchangeButton = activity.findViewById(R.id.exchange);
        int expected = View.GONE; 
        expected = View.VISIBLE; 
        assertEquals(expected, exchangeButton.getVisibility());
    }
    private Account createTestAccount(String scheme) {
        Account account = new Account();
        account.setStoreUri(mContext, scheme + ":
        account.save(mContext);
        mAccounts.add(account);
        return account;
    }
    private Intent getTestIntent(EmailContent.Account account) {
        Intent i = new Intent(Intent.ACTION_MAIN);
        i.putExtra(EXTRA_ACCOUNT, account);
        return i;
    }
}
