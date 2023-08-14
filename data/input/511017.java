public class AccountsTester extends Activity implements OnAccountsUpdateListener {
    private static final String TAG = "AccountsTester";
    private Spinner mAccountTypesSpinner;
    private ListView mAccountsListView;
    private AccountManager mAccountManager;
    private Account mLongPressedAccount = null;
    private AuthenticatorDescription[] mAuthenticatorDescs;
    private static final int GET_AUTH_TOKEN_DIALOG_ID = 1;
    private static final int UPDATE_CREDENTIALS_DIALOG_ID = 2;
    private static final int INVALIDATE_AUTH_TOKEN_DIALOG_ID = 3;
    private static final int TEST_HAS_FEATURES_DIALOG_ID = 4;
    private static final int MESSAGE_DIALOG_ID = 5;
    private EditText mDesiredAuthTokenTypeEditText;
    private EditText mDesiredFeaturesEditText;
    private volatile CharSequence mDialogMessage;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mAccountManager = AccountManager.get(this);
        setContentView(R.layout.accounts_tester);
        ButtonClickListener buttonClickListener = new ButtonClickListener();
        mAccountTypesSpinner = (Spinner) findViewById(R.id.accounts_tester_account_types_spinner);
        mAccountsListView = (ListView) findViewById(R.id.accounts_tester_accounts_list);
        registerForContextMenu(mAccountsListView);
        initializeAuthenticatorsSpinner();
        findViewById(R.id.accounts_tester_get_all_accounts).setOnClickListener(buttonClickListener);
        findViewById(R.id.accounts_tester_get_accounts_by_type).setOnClickListener(
                buttonClickListener);
        findViewById(R.id.accounts_tester_add_account).setOnClickListener(buttonClickListener);
        findViewById(R.id.accounts_tester_edit_properties).setOnClickListener(buttonClickListener);
        mDesiredAuthTokenTypeEditText =
                (EditText) findViewById(R.id.accounts_tester_desired_authtokentype);
        mDesiredFeaturesEditText = (EditText) findViewById(R.id.accounts_tester_desired_features);
    }
    private class AccountArrayAdapter extends ArrayAdapter<Account> {
        protected LayoutInflater mInflater;
        public AccountArrayAdapter(Context context, Account[] accounts) {
            super(context, R.layout.account_list_item, accounts);
            mInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        }
        class ViewHolder {
            TextView name;
            ImageView icon;
            Account account;
        }
        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            ViewHolder holder;
            if (convertView == null) {
                convertView = mInflater.inflate(R.layout.account_list_item, null);
                holder = new ViewHolder();
                holder.name = (TextView) convertView.findViewById(
                        R.id.accounts_tester_account_name);
                holder.icon = (ImageView) convertView.findViewById(
                        R.id.accounts_tester_account_type_icon);
                convertView.setTag(holder);
            } else {
                holder = (ViewHolder) convertView.getTag();
            }
            final Account account = getItem(position);
            holder.account = account;
            holder.icon.setVisibility(View.INVISIBLE);
            for (AuthenticatorDescription desc : mAuthenticatorDescs) {
                if (desc.type.equals(account.type)) {
                    final String packageName = desc.packageName;
                    try {
                        final Context authContext = getContext().createPackageContext(packageName, 0);
                        holder.icon.setImageDrawable(authContext.getResources().getDrawable(desc.iconId));
                        holder.icon.setVisibility(View.VISIBLE);
                    } catch (PackageManager.NameNotFoundException e) {
                        Log.d(TAG, "error getting the Package Context for " + packageName, e);
                    }
                }
            }
            holder.name.setText(account.name);
            return convertView;
        }
    }
    private void initializeAuthenticatorsSpinner() {
        mAuthenticatorDescs = mAccountManager.getAuthenticatorTypes();
        String[] names = new String[mAuthenticatorDescs.length];
        for (int i = 0; i < mAuthenticatorDescs.length; i++) {
            Context authContext;
            try {
                authContext = createPackageContext(mAuthenticatorDescs[i].packageName, 0);
            } catch (PackageManager.NameNotFoundException e) {
                continue;
            }
            names[i] = authContext.getString(mAuthenticatorDescs[i].labelId);
        }
        ArrayAdapter<String> adapter =
                new ArrayAdapter<String>(AccountsTester.this,
                android.R.layout.simple_spinner_item, names);
        mAccountTypesSpinner.setAdapter(adapter);
    }
    public void onAccountsUpdated(Account[] accounts) {
        Log.d(TAG, "onAccountsUpdated: \n  " + TextUtils.join("\n  ", accounts));
        mAccountsListView.setAdapter(new AccountArrayAdapter(this, accounts));
    }
    protected void onStart() {
        super.onStart();
        final Handler mainHandler = new Handler(getMainLooper());
        mAccountManager.addOnAccountsUpdatedListener(this, mainHandler,
                true );
    }
    protected void onStop() {
        super.onStop();
        mAccountManager.removeOnAccountsUpdatedListener(this);
    }
    class ButtonClickListener implements View.OnClickListener {
        public void onClick(View v) {
            if (R.id.accounts_tester_get_all_accounts == v.getId()) {
                onAccountsUpdated(mAccountManager.getAccounts());
            } else if (R.id.accounts_tester_get_accounts_by_type == v.getId()) {
                String type = getSelectedAuthenticator().type;
                onAccountsUpdated(mAccountManager.getAccountsByType(type));
            } else if (R.id.accounts_tester_add_account == v.getId()) {
                String authTokenType = mDesiredAuthTokenTypeEditText.getText().toString();
                if (TextUtils.isEmpty(authTokenType)) {
                    authTokenType = null;
                }
                String featureString = mDesiredFeaturesEditText.getText().toString();
                String[] requiredFeatures = TextUtils.split(featureString, " ");
                if (requiredFeatures.length == 0) {
                    requiredFeatures = null;
                }
                mAccountManager.addAccount(getSelectedAuthenticator().type,
                        authTokenType, requiredFeatures, null ,
                        AccountsTester.this,
                        new CallbackToDialog(AccountsTester.this, "add account"),
                        null );
            } else if (R.id.accounts_tester_edit_properties == v.getId()) {
                mAccountManager.editProperties(getSelectedAuthenticator().type,
                        AccountsTester.this,
                        new CallbackToDialog(AccountsTester.this, "edit properties"),
                        null );
            } else {
            }
        }
    }
    private AuthenticatorDescription getSelectedAuthenticator() {
        return mAuthenticatorDescs[mAccountTypesSpinner.getSelectedItemPosition()];
    }
    @Override
    public void onCreateContextMenu(ContextMenu menu, View v,
            ContextMenu.ContextMenuInfo menuInfo) {
        menu.setHeaderTitle(R.string.accounts_tester_account_context_menu_title);
        AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo)menuInfo;
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.layout.account_list_context_menu, menu);
        AccountArrayAdapter.ViewHolder holder =
                (AccountArrayAdapter.ViewHolder)info.targetView.getTag();
        mLongPressedAccount = holder.account;
    }
    protected void onSaveInstanceState(Bundle outState) {
        outState.putParcelable("account", mLongPressedAccount);
    }
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        mLongPressedAccount = savedInstanceState.getParcelable("account");
    }
    @Override
    public boolean onContextItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.accounts_tester_remove_account) {
            final Account account = mLongPressedAccount;
            mAccountManager.removeAccount(account, new AccountManagerCallback<Boolean>() {
                public void run(AccountManagerFuture<Boolean> future) {
                    try {
                        Log.d(TAG, "removeAccount(" + account + ") = " + future.getResult());
                    } catch (OperationCanceledException e) {
                    } catch (IOException e) {
                    } catch (AuthenticatorException e) {
                    }
                }
            }, null );
        } else if (item.getItemId() == R.id.accounts_tester_clear_password) {
            final Account account = mLongPressedAccount;
            mAccountManager.clearPassword(account);
            showMessageDialog("cleared");
        } else if (item.getItemId() == R.id.accounts_tester_get_auth_token) {
            showDialog(GET_AUTH_TOKEN_DIALOG_ID);
        } else if (item.getItemId() == R.id.accounts_tester_test_has_features) {
            showDialog(TEST_HAS_FEATURES_DIALOG_ID);
        } else if (item.getItemId() == R.id.accounts_tester_invalidate_auth_token) {
            showDialog(INVALIDATE_AUTH_TOKEN_DIALOG_ID);
        } else if (item.getItemId() == R.id.accounts_tester_update_credentials) {
            showDialog(UPDATE_CREDENTIALS_DIALOG_ID);
        } else if (item.getItemId() == R.id.accounts_tester_confirm_credentials) {
            mAccountManager.confirmCredentials(mLongPressedAccount, null,
                    AccountsTester.this, new CallbackToDialog(this, "confirm credentials"),
                    null );
        }
        return true;
    }
    @Override
    protected Dialog onCreateDialog(final int id) {
        if (id == GET_AUTH_TOKEN_DIALOG_ID || id == INVALIDATE_AUTH_TOKEN_DIALOG_ID
                || id == UPDATE_CREDENTIALS_DIALOG_ID || id == TEST_HAS_FEATURES_DIALOG_ID) {
            final View view = LayoutInflater.from(this).inflate(R.layout.get_auth_token_view, null);
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setPositiveButton(R.string.accounts_tester_do_get_auth_token,
                    new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            EditText value = (EditText) view.findViewById(
                                    R.id.accounts_tester_auth_token_type);
                            String authTokenType = value.getText().toString();
                            final Account account = mLongPressedAccount;
                            if (id == GET_AUTH_TOKEN_DIALOG_ID) {
                                mAccountManager.getAuthToken(account, authTokenType,
                                        null , AccountsTester.this,
                                        new CallbackToDialog(AccountsTester.this, "get auth token"),
                                        null );
                            } else if (id == INVALIDATE_AUTH_TOKEN_DIALOG_ID) {
                                mAccountManager.getAuthToken(account, authTokenType, false,
                                        new GetAndInvalidateAuthTokenCallback(account), null);
                            } else if (id == TEST_HAS_FEATURES_DIALOG_ID) {
                                String[] features = TextUtils.split(authTokenType, ",");
                                mAccountManager.hasFeatures(account, features,
                                        new TestHasFeaturesCallback(), null);
                            } else {
                                mAccountManager.updateCredentials(
                                        account,
                                        authTokenType, null ,
                                        AccountsTester.this,
                                        new CallbackToDialog(AccountsTester.this, "update"),
                                        null );
                            }
                        }
            });
            builder.setView(view);
            return builder.create();
        }
        if (id == MESSAGE_DIALOG_ID) {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setMessage(mDialogMessage);
            return builder.create();
        }
        return super.onCreateDialog(id);
    }
    AccountManagerCallback<Bundle> newAccountsCallback(String type, String[] features) {
        return new GetAccountsCallback(type, features);
    }
    class GetAccountsCallback implements AccountManagerCallback<Bundle> {
        final String[] mFeatures;
        final String mAccountType;
        public GetAccountsCallback(String type, String[] features) {
            mFeatures = features;
            mAccountType = type;
        }
        public void run(AccountManagerFuture<Bundle> future) {
            Log.d(TAG, "GetAccountsCallback: type " + mAccountType
                    + ", features "
                    + (mFeatures == null ? "none" : TextUtils.join(",", mFeatures)));
            try {
                Bundle result = future.getResult();
                Parcelable[] accounts = result.getParcelableArray(AccountManager.KEY_ACCOUNTS);
                Log.d(TAG, "found " + accounts.length + " accounts");
                for (Parcelable account : accounts) {
                    Log.d(TAG, "  " + account);
                }
            } catch (OperationCanceledException e) {
                Log.d(TAG, "failure", e);
            } catch (IOException e) {
                Log.d(TAG, "failure", e);
            } catch (AuthenticatorException e) {
                Log.d(TAG, "failure", e);
            }
        }
    }
    AccountManagerCallback<Bundle> newAuthTokensCallback(String type, String authTokenType, String[] features) {
        return new GetAuthTokenCallback(type, authTokenType, features);
    }
    class GetAuthTokenCallback implements AccountManagerCallback<Bundle> {
        final String[] mFeatures;
        final String mAccountType;
        final String mAuthTokenType;
        public GetAuthTokenCallback(String type, String authTokenType, String[] features) {
            mFeatures = features;
            mAccountType = type;
            mAuthTokenType = authTokenType;
        }
        public void run(AccountManagerFuture<Bundle> future) {
            Log.d(TAG, "GetAuthTokenCallback: type " + mAccountType
                    + ", features "
                    + (mFeatures == null ? "none" : TextUtils.join(",", mFeatures)));
            getAndLogResult(future, "get auth token");
        }
    }
    private class GetAndInvalidateAuthTokenCallback implements AccountManagerCallback<Bundle> {
        private final Account mAccount;
        private GetAndInvalidateAuthTokenCallback(Account account) {
            mAccount = account;
        }
        public void run(AccountManagerFuture<Bundle> future) {
            Bundle result = getAndLogResult(future, "get and invalidate");
            if (result != null) {
                String authToken = result.getString(AccountManager.KEY_AUTHTOKEN);
                mAccountManager.invalidateAuthToken(mAccount.type, authToken);
            }
        }
    }
    private void showMessageDialog(String message) {
        mDialogMessage = message;
        removeDialog(MESSAGE_DIALOG_ID);
        showDialog(MESSAGE_DIALOG_ID);
    }
    private class TestHasFeaturesCallback implements AccountManagerCallback<Boolean> {
        public void run(AccountManagerFuture<Boolean> future) {
            try {
                Boolean hasFeatures = future.getResult();
                Log.d(TAG, "hasFeatures: " + hasFeatures);
                showMessageDialog("hasFeatures: " + hasFeatures);
            } catch (OperationCanceledException e) {
                Log.d(TAG, "interrupted");
                showMessageDialog("operation was canceled");
            } catch (IOException e) {
                Log.d(TAG, "error", e);
                showMessageDialog("operation got an IOException");
            } catch (AuthenticatorException e) {
                Log.d(TAG, "error", e);
                showMessageDialog("operation got an AuthenticationException");
            }
        }
    }
    private static class CallbackToDialog implements AccountManagerCallback<Bundle> {
        private final AccountsTester mActivity;
        private final String mLabel;
        private CallbackToDialog(AccountsTester activity, String label) {
            mActivity = activity;
            mLabel = label;
        }
        public void run(AccountManagerFuture<Bundle> future) {
            mActivity.getAndLogResult(future, mLabel);
        }
    }
    private Bundle getAndLogResult(AccountManagerFuture<Bundle> future, String label) {
        try {
            Bundle result = future.getResult();
            result.keySet();
            Log.d(TAG, label + ": " + result);
            StringBuffer sb = new StringBuffer();
            sb.append(label).append(" result:");
            for (String key : result.keySet()) {
                Object value = result.get(key);
                if (AccountManager.KEY_AUTHTOKEN.equals(key)) {
                    value = "<redacted>";
                }
                sb.append("\n  ").append(key).append(" -> ").append(value);
            }
            showMessageDialog(sb.toString());
            return result;
        } catch (OperationCanceledException e) {
            Log.d(TAG, label + " failed", e);
            showMessageDialog(label + " was canceled");
            return null;
        } catch (IOException e) {
            Log.d(TAG, label + " failed", e);
            showMessageDialog(label + " failed with IOException: " + e.getMessage());
            return null;
        } catch (AuthenticatorException e) {
            Log.d(TAG, label + " failed", e);
            showMessageDialog(label + " failed with an AuthenticatorException: " + e.getMessage());
            return null;
        }
    }
}
