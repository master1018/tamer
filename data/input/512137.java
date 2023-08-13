public class AccountSetupIncoming extends Activity implements OnClickListener {
    private static final String EXTRA_ACCOUNT = "account";
    private static final String EXTRA_MAKE_DEFAULT = "makeDefault";
    private static final int popPorts[] = {
            110, 995, 995, 110, 110
    };
    private static final String popSchemes[] = {
            "pop3", "pop3+ssl+", "pop3+ssl+trustallcerts", "pop3+tls+", "pop3+tls+trustallcerts"
    };
    private static final int imapPorts[] = {
            143, 993, 993, 143, 143
    };
    private static final String imapSchemes[] = {
            "imap", "imap+ssl+", "imap+ssl+trustallcerts", "imap+tls+", "imap+tls+trustallcerts"
    };
    private final static int DIALOG_DUPLICATE_ACCOUNT = 1;
    private int mAccountPorts[];
    private String mAccountSchemes[];
    private EditText mUsernameView;
    private EditText mPasswordView;
    private EditText mServerView;
    private EditText mPortView;
    private Spinner mSecurityTypeView;
    private Spinner mDeletePolicyView;
    private EditText mImapPathPrefixView;
    private Button mNextButton;
    private EmailContent.Account mAccount;
    private boolean mMakeDefault;
    private String mCacheLoginCredential;
    private String mDuplicateAccountName;
    public static void actionIncomingSettings(Activity fromActivity, EmailContent.Account account,
            boolean makeDefault) {
        Intent i = new Intent(fromActivity, AccountSetupIncoming.class);
        i.putExtra(EXTRA_ACCOUNT, account);
        i.putExtra(EXTRA_MAKE_DEFAULT, makeDefault);
        fromActivity.startActivity(i);
    }
    public static void actionEditIncomingSettings(Activity fromActivity, EmailContent.Account account)
            {
        Intent i = new Intent(fromActivity, AccountSetupIncoming.class);
        i.setAction(Intent.ACTION_EDIT);
        i.putExtra(EXTRA_ACCOUNT, account);
        fromActivity.startActivity(i);
    }
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.account_setup_incoming);
        mUsernameView = (EditText)findViewById(R.id.account_username);
        mPasswordView = (EditText)findViewById(R.id.account_password);
        TextView serverLabelView = (TextView) findViewById(R.id.account_server_label);
        mServerView = (EditText)findViewById(R.id.account_server);
        mPortView = (EditText)findViewById(R.id.account_port);
        mSecurityTypeView = (Spinner)findViewById(R.id.account_security_type);
        mDeletePolicyView = (Spinner)findViewById(R.id.account_delete_policy);
        mImapPathPrefixView = (EditText)findViewById(R.id.imap_path_prefix);
        mNextButton = (Button)findViewById(R.id.next);
        mNextButton.setOnClickListener(this);
        SpinnerOption securityTypes[] = {
            new SpinnerOption(0, getString(R.string.account_setup_incoming_security_none_label)),
            new SpinnerOption(1, getString(R.string.account_setup_incoming_security_ssl_label)),
            new SpinnerOption(2, getString(
                    R.string.account_setup_incoming_security_ssl_trust_certificates_label)),
            new SpinnerOption(3, getString(R.string.account_setup_incoming_security_tls_label)),
            new SpinnerOption(4, getString(
                    R.string.account_setup_incoming_security_tls_trust_certificates_label)),
        };
        SpinnerOption deletePolicies[] = {
                new SpinnerOption(Account.DELETE_POLICY_NEVER,
                        getString(R.string.account_setup_incoming_delete_policy_never_label)),
                new SpinnerOption(Account.DELETE_POLICY_ON_DELETE,
                        getString(R.string.account_setup_incoming_delete_policy_delete_label)),
        };
        ArrayAdapter<SpinnerOption> securityTypesAdapter = new ArrayAdapter<SpinnerOption>(this,
                android.R.layout.simple_spinner_item, securityTypes);
        securityTypesAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        mSecurityTypeView.setAdapter(securityTypesAdapter);
        ArrayAdapter<SpinnerOption> deletePoliciesAdapter = new ArrayAdapter<SpinnerOption>(this,
                android.R.layout.simple_spinner_item, deletePolicies);
        deletePoliciesAdapter
                .setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        mDeletePolicyView.setAdapter(deletePoliciesAdapter);
        mSecurityTypeView.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            public void onItemSelected(AdapterView arg0, View arg1, int arg2, long arg3) {
                updatePortFromSecurityType();
            }
            public void onNothingSelected(AdapterView<?> arg0) {
            }
        });
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
        mPortView.addTextChangedListener(validationTextWatcher);
        mPortView.setKeyListener(DigitsKeyListener.getInstance("0123456789"));
        mAccount = (EmailContent.Account)getIntent().getParcelableExtra(EXTRA_ACCOUNT);
        mMakeDefault = getIntent().getBooleanExtra(EXTRA_MAKE_DEFAULT, false);
        if (savedInstanceState != null && savedInstanceState.containsKey(EXTRA_ACCOUNT)) {
            mAccount = (EmailContent.Account)savedInstanceState.getParcelable(EXTRA_ACCOUNT);
        }
        try {
            URI uri = new URI(mAccount.getStoreUri(this));
            String username = null;
            String password = null;
            if (uri.getUserInfo() != null) {
                String[] userInfoParts = uri.getUserInfo().split(":", 2);
                username = userInfoParts[0];
                if (userInfoParts.length > 1) {
                    password = userInfoParts[1];
                }
            }
            if (username != null) {
                mUsernameView.setText(username);
            }
            if (password != null) {
                mPasswordView.setText(password);
            }
            if (uri.getScheme().startsWith("pop3")) {
                serverLabelView.setText(R.string.account_setup_incoming_pop_server_label);
                mAccountPorts = popPorts;
                mAccountSchemes = popSchemes;
                findViewById(R.id.imap_path_prefix_section).setVisibility(View.GONE);
            } else if (uri.getScheme().startsWith("imap")) {
                serverLabelView.setText(R.string.account_setup_incoming_imap_server_label);
                mAccountPorts = imapPorts;
                mAccountSchemes = imapSchemes;
                findViewById(R.id.account_delete_policy_label).setVisibility(View.GONE);
                mDeletePolicyView.setVisibility(View.GONE);
                if (uri.getPath() != null && uri.getPath().length() > 0) {
                    mImapPathPrefixView.setText(uri.getPath().substring(1));
                }
            } else {
                throw new Error("Unknown account type: " + mAccount.getStoreUri(this));
            }
            for (int i = 0; i < mAccountSchemes.length; i++) {
                if (mAccountSchemes[i].equals(uri.getScheme())) {
                    SpinnerOption.setSpinnerOptionValue(mSecurityTypeView, i);
                }
            }
            SpinnerOption.setSpinnerOptionValue(mDeletePolicyView, mAccount.getDeletePolicy());
            if (uri.getHost() != null) {
                mServerView.setText(uri.getHost());
            }
            if (uri.getPort() != -1) {
                mPortView.setText(Integer.toString(uri.getPort()));
            } else {
                updatePortFromSecurityType();
            }
        } catch (URISyntaxException use) {
            throw new Error(use);
        }
        validateFields();
    }
    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putParcelable(EXTRA_ACCOUNT, mAccount);
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
    private void validateFields() {
        boolean enabled = Utility.requiredFieldValid(mUsernameView)
                && Utility.requiredFieldValid(mPasswordView)
                && Utility.requiredFieldValid(mServerView)
                && Utility.requiredFieldValid(mPortView);
        if (enabled) {
            try {
                URI uri = getUri();
            } catch (URISyntaxException use) {
                enabled = false;
            }
        }
        mNextButton.setEnabled(enabled);
        Utility.setCompoundDrawablesAlpha(mNextButton, enabled ? 255 : 128);
    }
    private void updatePortFromSecurityType() {
        int securityType = (Integer)((SpinnerOption)mSecurityTypeView.getSelectedItem()).value;
        mPortView.setText(Integer.toString(mAccountPorts[securityType]));
    }
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == RESULT_OK) {
            if (Intent.ACTION_EDIT.equals(getIntent().getAction())) {
                if (mAccount.isSaved()) {
                    mAccount.update(this, mAccount.toContentValues());
                    mAccount.mHostAuthRecv.update(this, mAccount.mHostAuthRecv.toContentValues());
                } else {
                    mAccount.save(this);
                }
                AccountBackupRestore.backupAccounts(this);
                finish();
            } else {
                try {
                    URI oldUri = new URI(mAccount.getSenderUri(this));
                    URI uri = new URI(
                            oldUri.getScheme(),
                            mUsernameView.getText().toString().trim() + ":" 
                                    + mPasswordView.getText().toString().trim(),
                            oldUri.getHost(),
                            oldUri.getPort(),
                            null,
                            null,
                            null);
                    mAccount.setSenderUri(this, uri.toString());
                } catch (URISyntaxException use) {
                }
                AccountSetupOutgoing.actionOutgoingSettings(this, mAccount, mMakeDefault);
                finish();
            }
        }
    }
    private URI getUri() throws URISyntaxException {
        int securityType = (Integer)((SpinnerOption)mSecurityTypeView.getSelectedItem()).value;
        String path = null;
        if (mAccountSchemes[securityType].startsWith("imap")) {
            path = "/" + mImapPathPrefixView.getText().toString().trim();
        }
        String userName = mUsernameView.getText().toString().trim();
        mCacheLoginCredential = userName;
        URI uri = new URI(
                mAccountSchemes[securityType],
                userName + ":" + mPasswordView.getText().toString().trim(),
                mServerView.getText().toString().trim(),
                Integer.parseInt(mPortView.getText().toString().trim()),
                path, 
                null, 
                null);
        return uri;
    }
    private void onNext() {
        try {
            URI uri = getUri();
            mAccount.setStoreUri(this, uri.toString());
            mDuplicateAccountName = Utility.findDuplicateAccount(this, mAccount.mId,
                    uri.getHost(), mCacheLoginCredential);
            if (mDuplicateAccountName != null) {
                this.showDialog(DIALOG_DUPLICATE_ACCOUNT);
                return;
            }
        } catch (URISyntaxException use) {
            throw new Error(use);
        }
        mAccount.setDeletePolicy((Integer)((SpinnerOption)mDeletePolicyView.getSelectedItem()).value);
        AccountSetupCheckSettings.actionValidateSettings(this, mAccount, true, false);
    }
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.next:
                onNext();
                break;
        }
    }
}
