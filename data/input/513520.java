public class AccountSetupExchange extends Activity implements OnClickListener,
        OnCheckedChangeListener {
     static final String EXTRA_ACCOUNT = "account";
    private static final String EXTRA_MAKE_DEFAULT = "makeDefault";
    private static final String EXTRA_EAS_FLOW = "easFlow";
     static final String EXTRA_DISABLE_AUTO_DISCOVER = "disableAutoDiscover";
    private final static int DIALOG_DUPLICATE_ACCOUNT = 1;
    private EditText mUsernameView;
    private EditText mPasswordView;
    private EditText mServerView;
    private CheckBox mSslSecurityView;
    private CheckBox mTrustCertificatesView;
    private Button mNextButton;
    private Account mAccount;
    private boolean mMakeDefault;
    private String mCacheLoginCredential;
    private String mDuplicateAccountName;
    public static void actionIncomingSettings(Activity fromActivity, Account account,
            boolean makeDefault, boolean easFlowMode, boolean allowAutoDiscover) {
        Intent i = new Intent(fromActivity, AccountSetupExchange.class);
        i.putExtra(EXTRA_ACCOUNT, account);
        i.putExtra(EXTRA_MAKE_DEFAULT, makeDefault);
        i.putExtra(EXTRA_EAS_FLOW, easFlowMode);
        if (!allowAutoDiscover) {
            i.putExtra(EXTRA_DISABLE_AUTO_DISCOVER, true);
        }
        fromActivity.startActivity(i);
    }
    public static void actionEditIncomingSettings(Activity fromActivity, Account account)
            {
        Intent i = new Intent(fromActivity, AccountSetupExchange.class);
        i.setAction(Intent.ACTION_EDIT);
        i.putExtra(EXTRA_ACCOUNT, account);
        fromActivity.startActivity(i);
    }
    public static void actionEditOutgoingSettings(Activity fromActivity, Account account)
            {
        Intent i = new Intent(fromActivity, AccountSetupExchange.class);
        i.setAction(Intent.ACTION_EDIT);
        i.putExtra(EXTRA_ACCOUNT, account);
        fromActivity.startActivity(i);
    }
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.account_setup_exchange);
        mUsernameView = (EditText) findViewById(R.id.account_username);
        mPasswordView = (EditText) findViewById(R.id.account_password);
        mServerView = (EditText) findViewById(R.id.account_server);
        mSslSecurityView = (CheckBox) findViewById(R.id.account_ssl);
        mSslSecurityView.setOnCheckedChangeListener(this);
        mTrustCertificatesView = (CheckBox) findViewById(R.id.account_trust_certificates);
        mNextButton = (Button)findViewById(R.id.next);
        mNextButton.setOnClickListener(this);
        TextWatcher validationTextWatcher = new TextWatcher() {
            public void afterTextChanged(Editable s) {
                validateFields();
            }
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }
        };
        mUsernameView.addTextChangedListener(validationTextWatcher);
        mPasswordView.addTextChangedListener(validationTextWatcher);
        mServerView.addTextChangedListener(validationTextWatcher);
        Intent intent = getIntent();
        mAccount = (EmailContent.Account) intent.getParcelableExtra(EXTRA_ACCOUNT);
        mMakeDefault = intent.getBooleanExtra(EXTRA_MAKE_DEFAULT, false);
        if (savedInstanceState != null && savedInstanceState.containsKey(EXTRA_ACCOUNT)) {
            mAccount = (EmailContent.Account) savedInstanceState.getParcelable(EXTRA_ACCOUNT);
        }
        loadFields(mAccount);
        validateFields();
        String username = mAccount.mHostAuthRecv.mLogin;
        String password = mAccount.mHostAuthRecv.mPassword;
        if (username != null && password != null &&
                !Intent.ACTION_EDIT.equals(intent.getAction())) {
            boolean disableAutoDiscover =
                intent.getBooleanExtra(EXTRA_DISABLE_AUTO_DISCOVER, false);
            if (!disableAutoDiscover) {
                AccountSetupCheckSettings
                    .actionAutoDiscover(this, mAccount, mAccount.mEmailAddress, password);
            }
        }
        try {
            ((TextView) findViewById(R.id.device_id)).setText(SyncManager.getDeviceId(this));
        } catch (IOException ignore) {
        }
    }
    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putParcelable(EXTRA_ACCOUNT, mAccount);
    }
    private boolean usernameFieldValid(EditText usernameView) {
        return Utility.requiredFieldValid(usernameView) &&
            !usernameView.getText().toString().equals("\\");
    }
    @Override
    public Dialog onCreateDialog(int id) {
        switch (id) {
            case DIALOG_DUPLICATE_ACCOUNT:
                return new AlertDialog.Builder(this)
                    .setIcon(android.R.drawable.ic_dialog_alert)
                    .setTitle(R.string.account_duplicate_dlg_title)
                    .setMessage(getString(R.string.account_duplicate_dlg_message_fmt,
                            mDuplicateAccountName))
                    .setPositiveButton(R.string.okay_action,
                            new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            dismissDialog(DIALOG_DUPLICATE_ACCOUNT);
                        }
                    })
                    .create();
        }
        return null;
    }
    @Override
    public void onPrepareDialog(int id, Dialog dialog) {
        switch (id) {
            case DIALOG_DUPLICATE_ACCOUNT:
                if (mDuplicateAccountName != null) {
                    AlertDialog alert = (AlertDialog) dialog;
                    alert.setMessage(getString(R.string.account_duplicate_dlg_message_fmt,
                            mDuplicateAccountName));
                }
                break;
        }
    }
     void loadFields(Account account) {
        HostAuth hostAuth = account.mHostAuthRecv;
        String userName = hostAuth.mLogin;
        if (userName != null) {
            if (userName.indexOf('\\') < 0) {
                userName = "\\" + userName;
            }
            mUsernameView.setText(userName);
        }
        if (hostAuth.mPassword != null) {
            mPasswordView.setText(hostAuth.mPassword);
        }
        String protocol = hostAuth.mProtocol;
        if (protocol == null || !protocol.startsWith("eas")) {
            throw new Error("Unknown account type: " + account.getStoreUri(this));
        }
        if (hostAuth.mAddress != null) {
            mServerView.setText(hostAuth.mAddress);
        }
        boolean ssl = 0 != (hostAuth.mFlags & HostAuth.FLAG_SSL);
        boolean trustCertificates = 0 != (hostAuth.mFlags & HostAuth.FLAG_TRUST_ALL_CERTIFICATES);
        mSslSecurityView.setChecked(ssl);
        mTrustCertificatesView.setChecked(trustCertificates);
        mTrustCertificatesView.setVisibility(ssl ? View.VISIBLE : View.GONE);
    }
    private boolean validateFields() {
        boolean enabled = usernameFieldValid(mUsernameView)
                && Utility.requiredFieldValid(mPasswordView)
                && Utility.requiredFieldValid(mServerView);
        if (enabled) {
            try {
                URI uri = getUri();
            } catch (URISyntaxException use) {
                enabled = false;
            }
        }
        mNextButton.setEnabled(enabled);
        Utility.setCompoundDrawablesAlpha(mNextButton, enabled ? 255 : 128);
        return enabled;
    }
    private void doOptions() {
        boolean easFlowMode = getIntent().getBooleanExtra(EXTRA_EAS_FLOW, false);
        AccountSetupOptions.actionOptions(this, mAccount, mMakeDefault, easFlowMode);
        finish();
    }
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == AccountSetupCheckSettings.REQUEST_CODE_VALIDATE) {
            if (Intent.ACTION_EDIT.equals(getIntent().getAction())) {
                doActivityResultValidateExistingAccount(resultCode, data);
            } else {
                doActivityResultValidateNewAccount(resultCode, data);
            }
        } else if (requestCode == AccountSetupCheckSettings.REQUEST_CODE_AUTO_DISCOVER) {
            doActivityResultAutoDiscoverNewAccount(resultCode, data);
        }
    }
    private void doActivityResultValidateExistingAccount(int resultCode, Intent data) {
        if (resultCode == RESULT_OK) {
            if (mAccount.isSaved()) {
                mAccount.update(this, mAccount.toContentValues());
                mAccount.mHostAuthRecv.update(this,
                        mAccount.mHostAuthRecv.toContentValues());
                mAccount.mHostAuthSend.update(this,
                        mAccount.mHostAuthSend.toContentValues());
                if (mAccount.mHostAuthRecv.mProtocol.equals("eas")) {
                    try {
                        ExchangeUtils.getExchangeEmailService(this, null)
                        .hostChanged(mAccount.mId);
                    } catch (RemoteException e) {
                    }
                }
            } else {
                mAccount.save(this);
            }
            AccountBackupRestore.backupAccounts(this);
            finish();
        }
    }
    private void doActivityResultValidateNewAccount(int resultCode, Intent data) {
        if (resultCode == RESULT_OK) {
            doOptions();
        } else if (resultCode == AccountSetupCheckSettings.RESULT_SECURITY_REQUIRED_USER_CANCEL) {
            finish();
        }
    }
    private void doActivityResultAutoDiscoverNewAccount(int resultCode, Intent data) {
        if (resultCode == AccountSetupCheckSettings.RESULT_AUTO_DISCOVER_AUTH_FAILED) {
            finish();
            return;
        }
        if (data != null) {
            Parcelable p = data.getParcelableExtra("HostAuth");
            if (p != null) {
                HostAuth hostAuth = (HostAuth)p;
                mAccount.mHostAuthSend = hostAuth;
                mAccount.mHostAuthRecv = hostAuth;
                loadFields(mAccount);
                if (validateFields()) {
                    onNext();
                }
            }
        }
    }
    private URI getUri() throws URISyntaxException {
        boolean sslRequired = mSslSecurityView.isChecked();
        boolean trustCertificates = mTrustCertificatesView.isChecked();
        String scheme = (sslRequired)
                        ? (trustCertificates ? "eas+ssl+trustallcerts" : "eas+ssl+")
                        : "eas";
        String userName = mUsernameView.getText().toString().trim();
        if (userName.startsWith("\\")) {
            userName = userName.substring(1);
        }
        mCacheLoginCredential = userName;
        String userInfo = userName + ":" + mPasswordView.getText().toString().trim();
        String host = mServerView.getText().toString().trim();
        String path = null;
        URI uri = new URI(
                scheme,
                userInfo,
                host,
                0,
                path,
                null,
                null);
        return uri;
    }
    private void onNext() {
        try {
            URI uri = getUri();
            mAccount.setStoreUri(this, uri.toString());
            mAccount.setSenderUri(this, uri.toString());
            mDuplicateAccountName = Utility.findDuplicateAccount(this, mAccount.mId,
                    uri.getHost(), mCacheLoginCredential);
            if (mDuplicateAccountName != null) {
                this.showDialog(DIALOG_DUPLICATE_ACCOUNT);
                return;
            }
        } catch (URISyntaxException use) {
            throw new Error(use);
        }
        AccountSetupCheckSettings.actionValidateSettings(this, mAccount, true, false);
    }
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.next:
                onNext();
                break;
        }
    }
    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        if (buttonView.getId() == R.id.account_ssl) {
            mTrustCertificatesView.setVisibility(isChecked ? View.VISIBLE : View.GONE);
        }
    }
}
