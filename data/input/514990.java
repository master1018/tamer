public final class QuickContactActivity extends Activity implements
        QuickContactWindow.OnDismissListener {
    private static final String TAG = "QuickContactActivity";
    static final boolean LOGV = false;
    static final boolean FORCE_CREATE = false;
    private QuickContactWindow mQuickContact;
    @Override
    protected void onCreate(Bundle icicle) {
        super.onCreate(icicle);
        if (LOGV) Log.d(TAG, "onCreate");
        this.onNewIntent(getIntent());
    }
    @Override
    public void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        if (LOGV) Log.d(TAG, "onNewIntent");
        if (QuickContactWindow.TRACE_LAUNCH) {
            android.os.Debug.startMethodTracing(QuickContactWindow.TRACE_TAG);
        }
        if (mQuickContact == null || FORCE_CREATE) {
            if (LOGV) Log.d(TAG, "Preparing window");
            mQuickContact = new QuickContactWindow(this, this);
        }
        Uri lookupUri = intent.getData();
        if (android.provider.Contacts.AUTHORITY.equals(lookupUri.getAuthority())) {
            final long rawContactId = ContentUris.parseId(lookupUri);
            lookupUri = RawContacts.getContactLookupUri(getContentResolver(),
                    ContentUris.withAppendedId(RawContacts.CONTENT_URI, rawContactId));
        }
        final Bundle extras = intent.getExtras();
        final Rect target = intent.getSourceBounds();
        final int mode = extras.getInt(QuickContact.EXTRA_MODE, QuickContact.MODE_MEDIUM);
        final String[] excludeMimes = extras.getStringArray(QuickContact.EXTRA_EXCLUDE_MIMES);
        mQuickContact.show(lookupUri, target, mode, excludeMimes);
    }
    @Override
    public void onBackPressed() {
        if (LOGV) Log.w(TAG, "Unexpected back captured by stub activity");
        finish();
    }
    @Override
    protected void onPause() {
        super.onPause();
        if (LOGV) Log.d(TAG, "onPause");
        mQuickContact.dismiss();
    }
    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (LOGV) Log.d(TAG, "onDestroy");
    }
    public void onDismiss(QuickContactWindow dialog) {
        if (LOGV) Log.d(TAG, "onDismiss");
        if (isTaskRoot() && !FORCE_CREATE) {
            moveTaskToBack(false);
        } else {
            finish();
        }
    }
}
