public class AccountSetupAccountType extends Activity implements OnClickListener {
    private static final String EXTRA_ACCOUNT = "account";
    private static final String EXTRA_MAKE_DEFAULT = "makeDefault";
    private static final String EXTRA_EAS_FLOW = "easFlow";
    private static final String EXTRA_ALLOW_AUTODISCOVER = "allowAutoDiscover";
    private Account mAccount;
    private boolean mMakeDefault;
    private boolean mAllowAutoDiscover;
    public static void actionSelectAccountType(Activity fromActivity, Account account,
            boolean makeDefault, boolean easFlowMode, boolean allowAutoDiscover) {
        Intent i = new Intent(fromActivity, AccountSetupAccountType.class);
        i.putExtra(EXTRA_ACCOUNT, account);
        i.putExtra(EXTRA_MAKE_DEFAULT, makeDefault);
        i.putExtra(EXTRA_EAS_FLOW, easFlowMode);
        i.putExtra(EXTRA_ALLOW_AUTODISCOVER, allowAutoDiscover);
        fromActivity.startActivity(i);
    }
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Intent intent = getIntent();
        mAccount = (Account) intent.getParcelableExtra(EXTRA_ACCOUNT);
        mMakeDefault = intent.getBooleanExtra(EXTRA_MAKE_DEFAULT, false);
        boolean easFlowMode = intent.getBooleanExtra(EXTRA_EAS_FLOW, false);
        mAllowAutoDiscover = intent.getBooleanExtra(EXTRA_ALLOW_AUTODISCOVER, true);
        if (easFlowMode) {
            onExchange(true);
            return;
        }
        setContentView(R.layout.account_setup_account_type);
        ((Button)findViewById(R.id.pop)).setOnClickListener(this);
        ((Button)findViewById(R.id.imap)).setOnClickListener(this);
        final Button exchangeButton = ((Button)findViewById(R.id.exchange));
        exchangeButton.setOnClickListener(this);
        if (isExchangeAvailable()) {
            exchangeButton.setVisibility(View.VISIBLE);
            if (VendorPolicyLoader.getInstance(this).useAlternateExchangeStrings()) {
                exchangeButton.setText(
                        R.string.account_setup_account_type_exchange_action_alternate);
            }
        }
    }
    private void onPop() {
        try {
            URI uri = new URI(mAccount.getStoreUri(this));
            uri = new URI("pop3", uri.getUserInfo(), uri.getHost(), uri.getPort(), null, null, null);
            mAccount.setStoreUri(this, uri.toString());
        } catch (URISyntaxException use) {
            throw new Error(use);
        }
        AccountSetupIncoming.actionIncomingSettings(this, mAccount, mMakeDefault);
        finish();
    }
    private void onImap() {
        try {
            URI uri = new URI(mAccount.getStoreUri(this));
            uri = new URI("imap", uri.getUserInfo(), uri.getHost(), uri.getPort(), null, null, null);
            mAccount.setStoreUri(this, uri.toString());
        } catch (URISyntaxException use) {
            throw new Error(use);
        }
        mAccount.setDeletePolicy(Account.DELETE_POLICY_ON_DELETE);
        AccountSetupIncoming.actionIncomingSettings(this, mAccount, mMakeDefault);
        finish();
    }
    private void onExchange(boolean easFlowMode) {
        try {
            URI uri = new URI(mAccount.getStoreUri(this));
            uri = new URI("eas+ssl+", uri.getUserInfo(), uri.getHost(), uri.getPort(),
                    null, null, null);
            mAccount.setStoreUri(this, uri.toString());
            mAccount.setSenderUri(this, uri.toString());
        } catch (URISyntaxException use) {
            throw new Error(use);
        }
        mAccount.setDeletePolicy(Account.DELETE_POLICY_ON_DELETE);
        mAccount.setSyncInterval(Account.CHECK_INTERVAL_PUSH);
        mAccount.setSyncLookback(1);
        AccountSetupExchange.actionIncomingSettings(this, mAccount, mMakeDefault, easFlowMode,
                mAllowAutoDiscover);
        finish();
    }
    private boolean isExchangeAvailable() {
        try {
            URI uri = new URI(mAccount.getStoreUri(this));
            uri = new URI("eas", uri.getUserInfo(), uri.getHost(), uri.getPort(), null, null, null);
            Store.StoreInfo storeInfo = Store.StoreInfo.getStoreInfo(uri.toString(), this);
            return (storeInfo != null && checkAccountInstanceLimit(storeInfo));
        } catch (URISyntaxException e) {
        }
        return false;
    }
     boolean checkAccountInstanceLimit(Store.StoreInfo storeInfo) {
        if (storeInfo.mAccountInstanceLimit < 0) {
            return true;
        }
        int currentAccountsCount = 0;
        Cursor c = null;
        try {
            c = this.getContentResolver().query(
                    Account.CONTENT_URI,
                    Account.CONTENT_PROJECTION,
                    null, null, null);
            while (c.moveToNext()) {
                Account account = EmailContent.getContent(c, Account.class);
                String storeUri = account.getStoreUri(this);
                if (storeUri != null && storeUri.startsWith(storeInfo.mScheme)) {
                    currentAccountsCount++;
                }
            }
        } finally {
            if (c != null) {
                c.close();
            }
        }
        return (currentAccountsCount < storeInfo.mAccountInstanceLimit);
    }
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.pop:
                onPop();
                break;
            case R.id.imap:
                onImap();
                break;
            case R.id.exchange:
                onExchange(false);
                break;
        }
    }
}
