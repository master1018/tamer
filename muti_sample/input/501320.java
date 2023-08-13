public class AccountSetupNames extends Activity implements OnClickListener {
    private static final String EXTRA_ACCOUNT_ID = "accountId";
    private static final String EXTRA_EAS_FLOW = "easFlow";
    private static final int REQUEST_SECURITY = 0;
    private EditText mDescription;
    private EditText mName;
    private Account mAccount;
    private Button mDoneButton;
    private boolean mEasAccount = false;
    private CheckAccountStateTask mCheckAccountStateTask;
    private static final int ACCOUNT_INFO_COLUMN_FLAGS = 0;
    private static final int ACCOUNT_INFO_COLUMN_SECURITY_FLAGS = 1;
    private static final String[] ACCOUNT_INFO_PROJECTION = new String[] {
            AccountColumns.FLAGS, AccountColumns.SECURITY_FLAGS };
    public static void actionSetNames(Activity fromActivity, long accountId, boolean easFlowMode) {
        Intent i = new Intent(fromActivity, AccountSetupNames.class);
        i.putExtra(EXTRA_ACCOUNT_ID, accountId);
        i.putExtra(EXTRA_EAS_FLOW, easFlowMode);
        fromActivity.startActivity(i);
    }
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.account_setup_names);
        mDescription = (EditText)findViewById(R.id.account_description);
        mName = (EditText)findViewById(R.id.account_name);
        mDoneButton = (Button)findViewById(R.id.done);
        mDoneButton.setOnClickListener(this);
        TextWatcher validationTextWatcher = new TextWatcher() {
            public void afterTextChanged(Editable s) {
                validateFields();
            }
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }
        };
        mName.addTextChangedListener(validationTextWatcher);
        mName.setKeyListener(TextKeyListener.getInstance(false, Capitalize.WORDS));
        long accountId = getIntent().getLongExtra(EXTRA_ACCOUNT_ID, -1);
        mAccount = EmailContent.Account.restoreAccountWithId(this, accountId);
        if (mAccount == null) {
            onBackPressed();
            return;
        }
        HostAuth hostAuth = HostAuth.restoreHostAuthWithId(this, mAccount.mHostAuthKeyRecv);
        if (hostAuth == null) {
            onBackPressed();
        }
        mEasAccount = hostAuth.mProtocol.equals("eas");
        if (mEasAccount) {
            mName.setVisibility(View.GONE);
            findViewById(R.id.account_name_label).setVisibility(View.GONE);
        }
        if (mAccount != null && mAccount.getSenderName() != null) {
            mName.setText(mAccount.getSenderName());
        }
        validateFields();
    }
    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mCheckAccountStateTask != null &&
                mCheckAccountStateTask.getStatus() != CheckAccountStateTask.Status.FINISHED) {
            mCheckAccountStateTask.cancel(true);
            mCheckAccountStateTask = null;
        }
    }
    private void validateFields() {
        if (!mEasAccount) {
            mDoneButton.setEnabled(Utility.requiredFieldValid(mName));
        }
        Utility.setCompoundDrawablesAlpha(mDoneButton, mDoneButton.isEnabled() ? 255 : 128);
    }
    @Override
    public void onBackPressed() {
        boolean easFlowMode = getIntent().getBooleanExtra(EXTRA_EAS_FLOW, false);
        if (easFlowMode) {
            AccountSetupBasics.actionAccountCreateFinishedEas(this);
        } else {
            if (mAccount != null) {
                AccountSetupBasics.actionAccountCreateFinished(this, mAccount.mId);
            } else {
                Welcome.actionStart(this);
            }
        }
        finish();
    }
    private void onNext() {
        if (Utility.requiredFieldValid(mDescription)) {
            mAccount.setDisplayName(mDescription.getText().toString());
        }
        String name = mName.getText().toString();
        mAccount.setSenderName(name);
        ContentValues cv = new ContentValues();
        cv.put(AccountColumns.DISPLAY_NAME, mAccount.getDisplayName());
        cv.put(AccountColumns.SENDER_NAME, name);
        mAccount.update(this, cv);
        AccountBackupRestore.backupAccounts(this);
        mCheckAccountStateTask = new CheckAccountStateTask(mAccount.mId);
        mCheckAccountStateTask.execute();
    }
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.done:
                onNext();
                break;
        }
    }
    private class CheckAccountStateTask extends AsyncTask<Void, Void, Boolean> {
        private long mAccountId;
        public CheckAccountStateTask(long accountId) {
            mAccountId = accountId;
        }
        @Override
        protected Boolean doInBackground(Void... params) {
            Cursor c = AccountSetupNames.this.getContentResolver().query(
                    ContentUris.withAppendedId(Account.CONTENT_URI, mAccountId),
                    ACCOUNT_INFO_PROJECTION, null, null, null);
            try {
                if (c.moveToFirst()) {
                    int flags = c.getInt(ACCOUNT_INFO_COLUMN_FLAGS);
                    int securityFlags = c.getInt(ACCOUNT_INFO_COLUMN_SECURITY_FLAGS);
                    if ((flags & Account.FLAGS_SECURITY_HOLD) != 0) {
                        return Boolean.TRUE;
                    }
                }
            } finally {
                c.close();
            }
            return Boolean.FALSE;
        }
        @Override
        protected void onPostExecute(Boolean isSecurityHold) {
            if (!isCancelled()) {
                if (isSecurityHold) {
                    Intent i = AccountSecurity.actionUpdateSecurityIntent(
                            AccountSetupNames.this, mAccountId);
                    AccountSetupNames.this.startActivityForResult(i, REQUEST_SECURITY);
                } else {
                    onBackPressed();
                }
            }
        }
    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case REQUEST_SECURITY:
                onBackPressed();
        }
        super.onActivityResult(requestCode, resultCode, data);
    }
}
