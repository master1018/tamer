public class ContactsSyncAdapterService extends Service {
    private static final String TAG = "EAS ContactsSyncAdapterService";
    private static SyncAdapterImpl sSyncAdapter = null;
    private static final Object sSyncAdapterLock = new Object();
    private static final String[] ID_PROJECTION = new String[] {EmailContent.RECORD_ID};
    private static final String ACCOUNT_AND_TYPE_CONTACTS =
        MailboxColumns.ACCOUNT_KEY + "=? AND " + MailboxColumns.TYPE + '=' + Mailbox.TYPE_CONTACTS;
    public ContactsSyncAdapterService() {
        super();
    }
    private static class SyncAdapterImpl extends AbstractThreadedSyncAdapter {
        private Context mContext;
        public SyncAdapterImpl(Context context) {
            super(context, true );
            mContext = context;
        }
        @Override
        public void onPerformSync(Account account, Bundle extras,
                String authority, ContentProviderClient provider, SyncResult syncResult) {
            try {
                ContactsSyncAdapterService.performSync(mContext, account, extras,
                        authority, provider, syncResult);
            } catch (OperationCanceledException e) {
            }
        }
    }
    @Override
    public void onCreate() {
        super.onCreate();
        synchronized (sSyncAdapterLock) {
            if (sSyncAdapter == null) {
                sSyncAdapter = new SyncAdapterImpl(getApplicationContext());
            }
        }
    }
    @Override
    public IBinder onBind(Intent intent) {
        return sSyncAdapter.getSyncAdapterBinder();
    }
    private static void performSync(Context context, Account account, Bundle extras,
            String authority, ContentProviderClient provider, SyncResult syncResult)
            throws OperationCanceledException {
        ContentResolver cr = context.getContentResolver();
        Log.i(TAG, "performSync");
        if (extras.getBoolean(ContentResolver.SYNC_EXTRAS_UPLOAD)) {
            Uri uri = RawContacts.CONTENT_URI.buildUpon()
                .appendQueryParameter(RawContacts.ACCOUNT_NAME, account.name)
                .appendQueryParameter(RawContacts.ACCOUNT_TYPE, Email.EXCHANGE_ACCOUNT_MANAGER_TYPE)
                .build();
            Cursor c = cr.query(uri,
                    new String[] {RawContacts._ID}, RawContacts.DIRTY + "=1", null, null);
            try {
                if (!c.moveToFirst()) {
                    Log.i(TAG, "Upload sync; no changes");
                    return;
                }
            } finally {
                c.close();
            }
        }
        Cursor accountCursor =
            cr.query(com.android.email.provider.EmailContent.Account.CONTENT_URI, ID_PROJECTION,
                AccountColumns.EMAIL_ADDRESS + "=?", new String[] {account.name}, null);
        try {
            if (accountCursor.moveToFirst()) {
                long accountId = accountCursor.getLong(0);
                Cursor mailboxCursor = cr.query(Mailbox.CONTENT_URI, ID_PROJECTION,
                        ACCOUNT_AND_TYPE_CONTACTS, new String[] {Long.toString(accountId)}, null);
                try {
                     if (mailboxCursor.moveToFirst()) {
                        Log.i(TAG, "Contact sync requested for " + account.name);
                        SyncManager.serviceRequest(mailboxCursor.getLong(0),
                                SyncManager.SYNC_UPSYNC);
                    }
                } finally {
                    mailboxCursor.close();
                }
            }
        } finally {
            accountCursor.close();
        }
    }
}