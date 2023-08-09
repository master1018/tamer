public class AccountSetupOptionsTests 
        extends ActivityInstrumentationTestCase2<AccountSetupOptions> {
    private static final String EXTRA_ACCOUNT = "account";
    private AccountSetupOptions mActivity;
    private Spinner mCheckFrequencyView;
    public AccountSetupOptionsTests() {
        super(AccountSetupOptions.class);
    }
    public void testPushOptionPOP() {
        Intent i = getTestIntent("Name", "pop3:
        this.setActivityIntent(i);
        getActivityAndFields();
        boolean hasPush = frequencySpinnerHasValue(EmailContent.Account.CHECK_INTERVAL_PUSH);
        assertFalse(hasPush);
    }
    public void testPushOptionIMAP() {
        Intent i = getTestIntent("Name", "imap:
        this.setActivityIntent(i);
        getActivityAndFields();
        boolean hasPush = frequencySpinnerHasValue(EmailContent.Account.CHECK_INTERVAL_PUSH);
        assertFalse(hasPush);
    }
    public void testPushOptionEAS() {
        if (Store.StoreInfo.getStoreInfo("eas", this.getInstrumentation().getTargetContext()) 
                == null) {
            return;
        }
        Intent i = getTestIntent("Name", "eas:
        this.setActivityIntent(i);
        getActivityAndFields();
        boolean hasPush = frequencySpinnerHasValue(EmailContent.Account.CHECK_INTERVAL_PUSH);
        assertTrue(hasPush);
    }
    private void getActivityAndFields() {
        mActivity = getActivity();
        mCheckFrequencyView = (Spinner) mActivity.findViewById(R.id.account_check_frequency);
    }
    private boolean frequencySpinnerHasValue(int value) {
        SpinnerAdapter sa = mCheckFrequencyView.getAdapter();
        for (int i = 0; i < sa.getCount(); ++i) {
            SpinnerOption so = (SpinnerOption) sa.getItem(i);
            if (so != null && ((Integer)so.value).intValue() == value) {
                return true;
            }
        }
        return false;
    }
    private Intent getTestIntent(String name, String storeUri) {
        EmailContent.Account account = new EmailContent.Account();
        account.setSenderName(name);
        account.setStoreUri(getInstrumentation().getTargetContext(), storeUri);
        Intent i = new Intent(Intent.ACTION_MAIN);
        i.putExtra(EXTRA_ACCOUNT, account);
        return i;
    }
}
