public class AccountSetupCheckSettings extends Activity implements OnClickListener {
    private static final boolean DBG_SKIP_CHECK_OK = false;     
    private static final boolean DBG_SKIP_CHECK_ERR = false;    
    private static final boolean DBG_FORCE_RESULT_OK = false;   
    private static final String EXTRA_ACCOUNT = "account";
    private static final String EXTRA_CHECK_INCOMING = "checkIncoming";
    private static final String EXTRA_CHECK_OUTGOING = "checkOutgoing";
    private static final String EXTRA_AUTO_DISCOVER = "autoDiscover";
    private static final String EXTRA_AUTO_DISCOVER_USERNAME = "userName";
    private static final String EXTRA_AUTO_DISCOVER_PASSWORD = "password";
    public static final int REQUEST_CODE_VALIDATE = 1;
    public static final int REQUEST_CODE_AUTO_DISCOVER = 2;
    public static final int RESULT_AUTO_DISCOVER_AUTH_FAILED = Activity.RESULT_FIRST_USER;
    public static final int RESULT_SECURITY_REQUIRED_USER_CANCEL = Activity.RESULT_FIRST_USER + 1;
    private Handler mHandler = new Handler();
    private ProgressBar mProgressBar;
    private TextView mMessageView;
    private EmailContent.Account mAccount;
    private boolean mCheckIncoming;
    private boolean mCheckOutgoing;
    private boolean mAutoDiscover;
    private boolean mCanceled;
    private boolean mDestroyed;
    public static void actionValidateSettings(Activity fromActivity, EmailContent.Account account,
            boolean checkIncoming, boolean checkOutgoing) {
        Intent i = new Intent(fromActivity, AccountSetupCheckSettings.class);
        i.putExtra(EXTRA_ACCOUNT, account);
        i.putExtra(EXTRA_CHECK_INCOMING, checkIncoming);
        i.putExtra(EXTRA_CHECK_OUTGOING, checkOutgoing);
        fromActivity.startActivityForResult(i, REQUEST_CODE_VALIDATE);
    }
    public static void actionAutoDiscover(Activity fromActivity, EmailContent.Account account,
            String userName, String password) {
        Intent i = new Intent(fromActivity, AccountSetupCheckSettings.class);
        i.putExtra(EXTRA_ACCOUNT, account);
        i.putExtra(EXTRA_AUTO_DISCOVER, true);
        i.putExtra(EXTRA_AUTO_DISCOVER_USERNAME, userName);
        i.putExtra(EXTRA_AUTO_DISCOVER_PASSWORD, password);
        fromActivity.startActivityForResult(i, REQUEST_CODE_AUTO_DISCOVER);
    }
    private class AutoDiscoverAuthenticationException extends AuthenticationFailedException {
        private static final long serialVersionUID = 1L;
        public AutoDiscoverAuthenticationException(String message) {
            super(message);
        }
    }
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.account_setup_check_settings);
        mMessageView = (TextView)findViewById(R.id.message);
        mProgressBar = (ProgressBar)findViewById(R.id.progress);
        ((Button)findViewById(R.id.cancel)).setOnClickListener(this);
        setMessage(R.string.account_setup_check_settings_retr_info_msg);
        mProgressBar.setIndeterminate(true);
        if (DBG_SKIP_CHECK_OK || DBG_SKIP_CHECK_ERR) {
            setResult(DBG_SKIP_CHECK_OK ? RESULT_OK : RESULT_CANCELED);
            finish();
            return;
        }
        final Intent intent = getIntent();
        mAccount = (EmailContent.Account)intent.getParcelableExtra(EXTRA_ACCOUNT);
        mCheckIncoming = intent.getBooleanExtra(EXTRA_CHECK_INCOMING, false);
        mCheckOutgoing = intent.getBooleanExtra(EXTRA_CHECK_OUTGOING, false);
        mAutoDiscover = intent.getBooleanExtra(EXTRA_AUTO_DISCOVER, false);
        new Thread() {
            @Override
            public void run() {
                Process.setThreadPriority(Process.THREAD_PRIORITY_BACKGROUND);
                try {
                    if (mDestroyed) {
                        return;
                    }
                    if (mCanceled) {
                        finish();
                        return;
                    }
                    if (mAutoDiscover) {
                        String userName = intent.getStringExtra(EXTRA_AUTO_DISCOVER_USERNAME);
                        String password = intent.getStringExtra(EXTRA_AUTO_DISCOVER_PASSWORD);
                        Log.d(Email.LOG_TAG, "Begin auto-discover for " + userName);
                        Store store = Store.getInstance(
                                mAccount.getStoreUri(AccountSetupCheckSettings.this),
                                getApplication(), null);
                        Bundle result = store.autoDiscover(AccountSetupCheckSettings.this,
                                userName, password);
                        if (result != null) {
                            int errorCode =
                                result.getInt(EmailServiceProxy.AUTO_DISCOVER_BUNDLE_ERROR_CODE);
                            if (errorCode == MessagingException.AUTHENTICATION_FAILED) {
                                throw new AutoDiscoverAuthenticationException(null);
                            } else if (errorCode != MessagingException.NO_ERROR) {
                                setResult(RESULT_OK);
                                finish();
                            }
                            Intent resultIntent = new Intent();
                            resultIntent.putExtra("HostAuth", result.getParcelable(
                                    EmailServiceProxy.AUTO_DISCOVER_BUNDLE_HOST_AUTH));
                            setResult(RESULT_OK, resultIntent);
                            finish();
                            return;
                       }
                    }
                    if (mDestroyed) {
                        return;
                    }
                    if (mCanceled) {
                        finish();
                        return;
                    }
                    if (mCheckIncoming) {
                        Log.d(Email.LOG_TAG, "Begin check of incoming email settings");
                        setMessage(R.string.account_setup_check_settings_check_incoming_msg);
                        Store store = Store.getInstance(
                                mAccount.getStoreUri(AccountSetupCheckSettings.this),
                                getApplication(), null);
                        store.checkSettings();
                    }
                    if (mDestroyed) {
                        return;
                    }
                    if (mCanceled) {
                        finish();
                        return;
                    }
                    if (mCheckOutgoing) {
                        Log.d(Email.LOG_TAG, "Begin check of outgoing email settings");
                        setMessage(R.string.account_setup_check_settings_check_outgoing_msg);
                        Sender sender = Sender.getInstance(getApplication(),
                                mAccount.getSenderUri(AccountSetupCheckSettings.this));
                        sender.close();
                        sender.open();
                        sender.close();
                    }
                    if (mDestroyed) {
                        return;
                    }
                    setResult(RESULT_OK);
                    finish();
                } catch (final AuthenticationFailedException afe) {
                    String message = afe.getMessage();
                    int id = (message == null)
                            ? R.string.account_setup_failed_dlg_auth_message
                            : R.string.account_setup_failed_dlg_auth_message_fmt;
                    showErrorDialog(afe instanceof AutoDiscoverAuthenticationException,
                            id, message);
                } catch (final CertificateValidationException cve) {
                    String message = cve.getMessage();
                    int id = (message == null)
                        ? R.string.account_setup_failed_dlg_certificate_message
                        : R.string.account_setup_failed_dlg_certificate_message_fmt;
                    showErrorDialog(false, id, message);
                } catch (final MessagingException me) {
                    int exceptionType = me.getExceptionType();
                    if (exceptionType == MessagingException.SECURITY_POLICIES_REQUIRED) {
                        showSecurityRequiredDialog();
                        return;
                    }
                    int id;
                    String message = me.getMessage();
                    switch (exceptionType) {
                        case MessagingException.IOERROR:
                            id = R.string.account_setup_failed_ioerror;
                            break;
                        case MessagingException.TLS_REQUIRED:
                            id = R.string.account_setup_failed_tls_required;
                            break;
                        case MessagingException.AUTH_REQUIRED:
                            id = R.string.account_setup_failed_auth_required;
                            break;
                        case MessagingException.SECURITY_POLICIES_UNSUPPORTED:
                            id = R.string.account_setup_failed_security_policies_unsupported;
                            break;
                        case MessagingException.GENERAL_SECURITY:
                            id = R.string.account_setup_failed_security;
                            break;
                        default:
                            id = (message == null)
                                    ? R.string.account_setup_failed_dlg_server_message
                                    : R.string.account_setup_failed_dlg_server_message_fmt;
                            break;
                    }
                    showErrorDialog(false, id, message);
                }
            }
        }.start();
    }
    @Override
    public void onDestroy() {
        super.onDestroy();
        mDestroyed = true;
        mCanceled = true;
    }
    private void setMessage(final int resId) {
        mHandler.post(new Runnable() {
            public void run() {
                if (mDestroyed) {
                    return;
                }
                mMessageView.setText(getString(resId));
            }
        });
    }
    private void showErrorDialog(final boolean autoDiscoverAuthException, final int msgResId,
            final Object... args) {
        mHandler.post(new Runnable() {
            public void run() {
                if (mDestroyed) {
                    return;
                }
                mProgressBar.setIndeterminate(false);
                new AlertDialog.Builder(AccountSetupCheckSettings.this)
                        .setIcon(android.R.drawable.ic_dialog_alert)
                        .setTitle(getString(R.string.account_setup_failed_dlg_title))
                        .setMessage(getString(msgResId, args))
                        .setCancelable(true)
                        .setPositiveButton(
                                getString(R.string.account_setup_failed_dlg_edit_details_action),
                                new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int which) {
                                        if (autoDiscoverAuthException) {
                                            setResult(RESULT_AUTO_DISCOVER_AUTH_FAILED);
                                        } else if (DBG_FORCE_RESULT_OK) {
                                            setResult(RESULT_OK);
                                        }
                                        finish();
                                    }
                                })
                        .show();
            }
        });
    }
    private void showSecurityRequiredDialog() {
        mHandler.post(new Runnable() {
            public void run() {
                if (mDestroyed) {
                    return;
                }
                mProgressBar.setIndeterminate(false);
                String host = mAccount.mHostAuthRecv.mAddress;
                Object[] args = new String[] { host };
                new AlertDialog.Builder(AccountSetupCheckSettings.this)
                        .setIcon(android.R.drawable.ic_dialog_alert)
                        .setTitle(getString(R.string.account_setup_security_required_title))
                        .setMessage(getString(
                                R.string.account_setup_security_policies_required_fmt, args))
                        .setCancelable(true)
                        .setPositiveButton(
                                getString(R.string.okay_action),
                                new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int which) {
                                        setResult(RESULT_OK);
                                        finish();
                                    }
                                })
                        .setNegativeButton(
                                getString(R.string.cancel_action),
                                new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int which) {
                                        setResult(RESULT_SECURITY_REQUIRED_USER_CANCEL);
                                        finish();
                                    }
                                })
                        .show();
            }
        });
    }
    private void onCancel() {
        mCanceled = true;
        setMessage(R.string.account_setup_check_settings_canceling_msg);
    }
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.cancel:
                onCancel();
                break;
        }
    }
}
