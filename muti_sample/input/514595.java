public class SigningInActivity extends Activity {
    private static final String SYNC_SETTINGS_ACTION = "android.settings.SYNC_SETTINGS";
    private static final String SYNC_SETTINGS_CATEGORY = "android.intent.category.DEFAULT";
    private IImConnection mConn;
    private IConnectionListener mListener;
    private SimpleAlertHandler mHandler;
    private ImApp mApp;
    private long mProviderId;
    private long mAccountId;
    private String mProviderName;
    private String mUserName;
    private String mPassword;
    private String mToAddress;
    protected static final int ID_CANCEL_SIGNIN = Menu.FIRST + 1;
    @Override
    protected void onCreate(Bundle icicle) {
        super.onCreate(icicle);
        getWindow().requestFeature(Window.FEATURE_LEFT_ICON);
        setContentView(R.layout.signing_in_activity);
        Intent intent = getIntent();
        mToAddress = intent.getStringExtra(ImApp.EXTRA_INTENT_SEND_TO_USER);
        Uri data = intent.getData();
        if (data == null) {
            if(Log.isLoggable(ImApp.LOG_TAG, Log.DEBUG)) {
                log("Need account data to sign in");
            }
            finish();
            return;
        }
        ContentResolver cr = getContentResolver();
        Cursor c = cr.query(data, null, null, null, null);
        if (c == null) {
            if (Log.isLoggable(ImApp.LOG_TAG, Log.DEBUG)) {
                log("Query fail:" + data);
            }
            finish();
            return;
        }
        if (!c.moveToFirst()) {
            if (Log.isLoggable(ImApp.LOG_TAG, Log.DEBUG)) {
                log("No data for " + data);
            }
            c.close();
            finish();
            return;
        }
        mProviderId = c.getLong(c.getColumnIndexOrThrow(Imps.Account.PROVIDER));
        mAccountId = c.getLong(c.getColumnIndexOrThrow(Imps.Account._ID));
        mUserName = c.getString(c.getColumnIndexOrThrow(Imps.Account.USERNAME));
        String pwExtra = intent.getStringExtra(ImApp.EXTRA_INTENT_PASSWORD);
        mPassword = pwExtra != null ? pwExtra
                : c.getString(c.getColumnIndexOrThrow(Imps.Account.PASSWORD));
        final boolean isActive = c.getInt(c.getColumnIndexOrThrow(Imps.Account.ACTIVE)) == 1;
        c.close();
        mApp = ImApp.getApplication(this);
        final ProviderDef provider = mApp.getProvider(mProviderId);
        mProviderName = provider.mName;
        BrandingResources brandingRes = mApp.getBrandingResource(mProviderId);
        getWindow().setFeatureDrawable(Window.FEATURE_LEFT_ICON,
                brandingRes.getDrawable(BrandingResourceIDs.DRAWABLE_LOGO));
        setTitle(getResources().getString(R.string.signing_in_to,
                provider.mFullName));
        ImageView splash = (ImageView)findViewById(R.id.splashscr);
        splash.setImageDrawable(brandingRes.getDrawable(
                BrandingResourceIDs.DRAWABLE_SPLASH_SCREEN));
        mHandler = new SimpleAlertHandler(this);
        mListener = new MyConnectionListener(mHandler);
        mApp.callWhenServiceConnected(mHandler, new Runnable() {
            public void run() {
                if (mApp.serviceConnected()) {
                    if (!isActive) {
                        activateAccount(mProviderId, mAccountId);
                    }
                    signInAccount();
                }
            }
        });
        setResult(RESULT_OK);
    }
    @Override
    protected void onRestart() {
        super.onRestart();
        if (mApp.serviceConnected() && mApp.isBackgroundDataEnabled()) {
            signInAccount();
        } else {
            if(Log.isLoggable(ImApp.LOG_TAG, Log.DEBUG)) {
                log("onRestart: service disconnected or background data disabled...");
            }
            setResult(RESULT_CANCELED);
            finish();
        }
    }
    void signInAccount() {
        try {
            IImConnection conn = mApp.getConnection(mProviderId);
            if (conn != null) {
                mConn = conn;
                conn.registerConnectionListener(mListener);
                int state = conn.getState();
                if (state != ImConnection.LOGGING_IN) {
                    conn.unregisterConnectionListener(mListener);
                    handleConnectionEvent(state, null);
                }
            } else {
                if (mApp.isBackgroundDataEnabled()) {
                    mConn = mApp.createConnection(mProviderId);
                    mConn.registerConnectionListener(mListener);
                    mConn.login(mAccountId, mUserName, mPassword, true);
                } else {
                    promptForBackgroundDataSetting();
                    return;
                }
            }
        } catch (RemoteException e) {
            mHandler.showServiceErrorAlert();
            finish();
        }
    }
    private void activateAccount(long providerId, long accountId) {
        ContentValues values = new ContentValues(1);
        values.put(Imps.Account.ACTIVE, 0);
        ContentResolver cr = getContentResolver();
        cr.update(Imps.Account.CONTENT_URI, values,
                Imps.Account.PROVIDER + "=" + providerId, null);
        values.put(Imps.Account.ACTIVE, 1);
        cr.update(ContentUris.withAppendedId(Imps.Account.CONTENT_URI, accountId),
                values, null, null);
    }
    @Override
    protected void onStop() {
        super.onStop();
        if (mApp != null) {
            mApp.removePendingCall(mHandler);
        }
        if (mConn != null) {
            try {
                if (Log.isLoggable(ImApp.LOG_TAG, Log.DEBUG)) {
                    log("unregisterConnectonListener");
                }
                mConn.unregisterConnectionListener(mListener);
            } catch (RemoteException e) {
                Log.w(ImApp.LOG_TAG, "<SigningInActivity> Connection disappeared!");
            }
        }
        if (mApp.isBackgroundDataEnabled()) {
            finish();
        }
    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        menu.add(0, ID_CANCEL_SIGNIN, 0, R.string.menu_cancel_signin)
            .setIcon(android.R.drawable.ic_menu_close_clear_cancel);
        return true;
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == ID_CANCEL_SIGNIN) {
            if (mConn != null) {
                try {
                    if (mConn.getState() == ImConnection.LOGGING_IN) {
                        if (Log.isLoggable(ImApp.LOG_TAG, Log.DEBUG)) {
                            log("Cancelling sign in");
                        }
                        mConn.logout();
                        finish();
                    }
                } catch (RemoteException e) {
                    Log.w(ImApp.LOG_TAG, "<SigningInActivity> Connection disappeared!");
                }
            }
            return true;
        } else {
            return super.onOptionsItemSelected(item);
        }
    }
    private void promptForBackgroundDataSetting() {
        new AlertDialog.Builder(SigningInActivity.this)
            .setTitle(R.string.bg_data_prompt_title)
            .setIcon(android.R.drawable.ic_dialog_alert)
            .setMessage(getString(R.string.bg_data_prompt_message, mProviderName))
            .setPositiveButton(R.string.bg_data_prompt_ok, new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int whichButton) {
                    Intent intent = new Intent(SYNC_SETTINGS_ACTION);
                    intent.addCategory(SYNC_SETTINGS_CATEGORY);
                    startActivity(intent);
                }
             })
            .setNegativeButton(R.string.bg_data_prompt_cancel,
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                        setResult(RESULT_CANCELED);
                        finish();
                    }
             })
            .show();
    }
    void handleConnectionEvent(int state, ImErrorInfo error) {
        if (isFinishing()) {
            return;
        }
        if (state == ImConnection.LOGGED_IN) {
            finish();
            try {
                Intent intent;
                long accountId = mConn.getAccountId();
                if (mToAddress != null) {
                    IChatSessionManager manager = mConn.getChatSessionManager();
                    IChatSession session = manager.getChatSession(mToAddress);
                    if(session == null) {
                        session = manager.createChatSession(mToAddress);
                    }
                    Uri data = ContentUris.withAppendedId(Imps.Chats.CONTENT_URI, session.getId());
                    intent = new Intent(Intent.ACTION_VIEW, data);
                    intent.putExtra("from", mToAddress);
                    intent.putExtra("providerId", mProviderId);
                    intent.putExtra("accountId", accountId);
                    intent.addCategory(ImApp.IMPS_CATEGORY);
                } else {
                    intent = new Intent(this, ContactListActivity.class);
                    intent.putExtra(ImServiceConstants.EXTRA_INTENT_ACCOUNT_ID, accountId);
                }
                startActivity(intent);
            } catch (RemoteException e) {
                Log.w(ImApp.LOG_TAG, "<SigningInActivity> Connection disappeared while signing in!");
            }
        } else if (state == ImConnection.DISCONNECTED) {
            Resources r = getResources();
            new AlertDialog.Builder(this)
                .setTitle(R.string.error)
                .setMessage(r.getString(R.string.login_service_failed, mProviderName,
                            error == null? "": ErrorResUtils.getErrorRes(r, error.getCode())))
                .setPositiveButton(R.string.ok,
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int whichButton) {
                                setResult(RESULT_CANCELED);
                                finish();
                            }
                        })
                .setCancelable(false)
                .show();
        }
    }
    private static final void log(String msg) {
        Log.d(ImApp.LOG_TAG, "<SigningInActivity>" + msg);
    }
    private final class MyConnectionListener extends ConnectionListenerAdapter {
        MyConnectionListener(Handler handler) {
            super(handler);
        }
        @Override
        public void onConnectionStateChange(IImConnection connection,
                int state, ImErrorInfo error) {
            handleConnectionEvent(state, error);
        }
    }
}
