public class ChooseAccountActivity extends ListActivity {
    private static final String TAG = "AccountManager";
    private Parcelable[] mAccounts = null;
    private AccountManagerResponse mAccountManagerResponse = null;
    private Bundle mResult;
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mAccounts = getIntent().getParcelableArrayExtra(AccountManager.KEY_ACCOUNTS);
        mAccountManagerResponse =
                getIntent().getParcelableExtra(AccountManager.KEY_ACCOUNT_MANAGER_RESPONSE);
        if (mAccounts == null) {
            setResult(RESULT_CANCELED);
            finish();
            return;
        }
        String[] mAccountNames = new String[mAccounts.length];
        for (int i = 0; i < mAccounts.length; i++) {
            mAccountNames[i] = ((Account) mAccounts[i]).name;
        }
        setListAdapter(new ArrayAdapter<String>(this,
                android.R.layout.simple_list_item_1, mAccountNames));
        getListView().setTextFilterEnabled(true);
    }
    protected void onListItemClick(ListView l, View v, int position, long id) {
        Account account = (Account) mAccounts[position];
        Log.d(TAG, "selected account " + account);
        Bundle bundle = new Bundle();
        bundle.putString(AccountManager.KEY_ACCOUNT_NAME, account.name);
        bundle.putString(AccountManager.KEY_ACCOUNT_TYPE, account.type);
        mResult = bundle;
        finish();
    }
    public void finish() {
        if (mAccountManagerResponse != null) {
            if (mResult != null) {
                mAccountManagerResponse.onResult(mResult);
            } else {
                mAccountManagerResponse.onError(AccountManager.ERROR_CODE_CANCELED, "canceled");
            }
        }
        super.finish();
    }
}
