public class AccountSettingsTests extends ActivityInstrumentationTestCase2<AccountSettings> {
    private static final String EXTRA_ACCOUNT_ID = "account_id";
    private long mAccountId;
    private Account mAccount;
    private Context mContext;
    private AccountSettings mActivity;
    private ListPreference mCheckFrequency;
    private static final String PREFERENCE_FREQUENCY = "account_check_frequency";
    public AccountSettingsTests() {
        super(AccountSettings.class);
    }
    @Override
    protected void setUp() throws Exception {
        super.setUp();
        mContext = this.getInstrumentation().getTargetContext();
    }
    @Override
    protected void tearDown() throws Exception {
        if (mAccount != null) {
            Uri uri = ContentUris.withAppendedId(Account.CONTENT_URI, mAccountId);
            mContext.getContentResolver().delete(uri, null, null);
        }
        super.tearDown();
    }
    public void testPushOptionPOP() {
        Intent i = getTestIntent("Name", "pop3:
                "smtp:
        this.setActivityIntent(i);
        getActivityAndFields();
        boolean hasPush = frequencySpinnerHasValue(Account.CHECK_INTERVAL_PUSH);
        assertFalse(hasPush);
    }
    public void testPushOptionIMAP() {
        Intent i = getTestIntent("Name", "imap:
                "smtp:
        this.setActivityIntent(i);
        getActivityAndFields();
        boolean hasPush = frequencySpinnerHasValue(Account.CHECK_INTERVAL_PUSH);
        assertFalse(hasPush);
    }
    public void testPushOptionEAS() {
        if (Store.StoreInfo.getStoreInfo("eas", this.getInstrumentation().getTargetContext())
                == null) {
            return;
        }
        Intent i = getTestIntent("Name", "eas:
                "eas:
        this.setActivityIntent(i);
        getActivityAndFields();
        boolean hasPush = frequencySpinnerHasValue(Account.CHECK_INTERVAL_PUSH);
        assertTrue(hasPush);
    }
    private void getActivityAndFields() {
        mActivity = getActivity();
        mCheckFrequency = (ListPreference) mActivity.findPreference(PREFERENCE_FREQUENCY);
    }
    private boolean frequencySpinnerHasValue(int value) {
        CharSequence[] values = mCheckFrequency.getEntryValues();
        for (CharSequence listValue : values) {
            if (listValue != null && Integer.parseInt(listValue.toString()) == value) {
                return true;
            }
        }
        return false;
    }
    private Intent getTestIntent(String name, String storeUri, String senderUri) {
        mAccount = new Account();
        mAccount.setSenderName(name);
        mAccount.mEmailAddress = "user@server.com";
        mAccount.setStoreUri(mContext, storeUri);
        mAccount.setSenderUri(mContext, senderUri);
        mAccount.save(mContext);
        mAccountId = mAccount.mId;
        Intent i = new Intent(Intent.ACTION_MAIN);
        i.putExtra(EXTRA_ACCOUNT_ID, mAccountId);
        return i;
    }
}
