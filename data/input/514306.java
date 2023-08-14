public class SignoutActivity extends Activity {
    private String[] ACCOUNT_SELECTION = new String[] {
            Imps.Account._ID,
            Imps.Account.PROVIDER,
    };
    private ImApp mApp;
    private Handler mHandler = new Handler();
    @Override
    protected void onCreate(Bundle icicle) {
        super.onCreate(icicle);
        Intent intent = getIntent();
        Uri data = intent.getData();
        if (data == null) {
            Log.e(ImApp.LOG_TAG, "Need account data to sign in");
            finish();
            return;
        }
        ContentResolver cr = getContentResolver();
        Cursor c = cr.query(data,
                ACCOUNT_SELECTION,
                null ,
                null ,
                null );
        final long providerId;
        final long accountId;
        try {
            if (!c.moveToFirst()) {
                Log.w(ImApp.LOG_TAG, "[SignoutActivity] No data for " + data);
                finish();
                return;
            }
            providerId = c.getLong(c.getColumnIndexOrThrow(Imps.Account.PROVIDER));
            accountId = c.getLong(c.getColumnIndexOrThrow(Imps.Account._ID));
        } finally {
            c.close();
        }
        mApp = ImApp.getApplication(this);
        mApp.callWhenServiceConnected(mHandler, new Runnable() {
            public void run() {
                signOut(providerId, accountId);
            }
        });
    }
    private void signOut(long providerId, long accountId) {
        try {
            IImConnection conn = mApp.getConnection(providerId);
            if (conn != null) {
                conn.logout();
            } else {
                ContentValues values = new ContentValues(2);
                values.put(Imps.AccountStatus.PRESENCE_STATUS,
                        Imps.Presence.OFFLINE);
                values.put(Imps.AccountStatus.CONNECTION_STATUS,
                        Imps.ConnectionStatus.OFFLINE);
                String where = Imps.AccountStatus.ACCOUNT + "=?";
                getContentResolver().update(Imps.AccountStatus.CONTENT_URI,
                        values, where,
                        new String[] { Long.toString(accountId) });
            }
        } catch (RemoteException ex) {
            Log.e(ImApp.LOG_TAG, "signout: caught ", ex);
        } finally {
            finish();
        }
    }
    @Override
    protected void onStop() {
        super.onStop();
        finish();
    }
    static void log(String msg) {
        Log.d(ImApp.LOG_TAG, "[Signout] " + msg);
    }
}
