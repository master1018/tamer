public final class ShowOrCreateActivity extends Activity implements
        NotifyingAsyncQueryHandler.AsyncQueryListener {
    static final String TAG = "ShowOrCreateActivity";
    static final boolean LOGD = false;
    static final String[] PHONES_PROJECTION = new String[] {
        PhoneLookup._ID,
    };
    static final String[] CONTACTS_PROJECTION = new String[] {
        RawContacts.CONTACT_ID,
    };
    static final int CONTACT_ID_INDEX = 0;
    static final int CREATE_CONTACT_DIALOG = 1;
    static final int QUERY_TOKEN = 42;
    private NotifyingAsyncQueryHandler mQueryHandler;
    private Bundle mCreateExtras;
    private String mCreateDescrip;
    private boolean mCreateForce;
    @Override
    protected void onCreate(Bundle icicle) {
        super.onCreate(icicle);
        if (mQueryHandler == null) {
            mQueryHandler = new NotifyingAsyncQueryHandler(this, this);
        } else {
            mQueryHandler.cancelOperation(QUERY_TOKEN);
        }
        final Intent intent = getIntent();
        final Uri data = intent.getData();
        String scheme = null;
        String ssp = null;
        if (data != null) {
            scheme = data.getScheme();
            ssp = data.getSchemeSpecificPart();
        }
        mCreateExtras = new Bundle();
        Bundle originalExtras = intent.getExtras();
        if (originalExtras != null) {
            mCreateExtras.putAll(originalExtras);
        }
        mCreateDescrip = intent.getStringExtra(Intents.EXTRA_CREATE_DESCRIPTION);
        if (mCreateDescrip == null) {
            mCreateDescrip = ssp;
        }
        mCreateForce = intent.getBooleanExtra(Intents.EXTRA_FORCE_CREATE, false);
        if (Constants.SCHEME_MAILTO.equals(scheme)) {
            mCreateExtras.putString(Intents.Insert.EMAIL, ssp);
            Uri uri = Uri.withAppendedPath(Email.CONTENT_FILTER_URI, Uri.encode(ssp));
            mQueryHandler.startQuery(QUERY_TOKEN, null, uri, CONTACTS_PROJECTION, null, null, null);
        } else if (Constants.SCHEME_TEL.equals(scheme)) {
            mCreateExtras.putString(Intents.Insert.PHONE, ssp);
            Uri uri = Uri.withAppendedPath(PhoneLookup.CONTENT_FILTER_URI, ssp);
            mQueryHandler.startQuery(QUERY_TOKEN, null, uri, PHONES_PROJECTION, null, null, null);
        } else {
            Log.w(TAG, "Invalid intent:" + getIntent());
            finish();
        }
    }
    @Override
    protected void onStop() {
        super.onStop();
        if (mQueryHandler != null) {
            mQueryHandler.cancelOperation(QUERY_TOKEN);
        }
    }
    public void onQueryComplete(int token, Object cookie, Cursor cursor) {
        if (cursor == null) {
            finish();
            return;
        }
        int count = 0;
        long contactId = -1;
        try {
            count = cursor.getCount();
            if (count == 1 && cursor.moveToFirst()) {
                contactId = cursor.getLong(CONTACT_ID_INDEX);
            }
        } finally {
            cursor.close();
        }
        if (count == 1 && contactId != -1) {
            final Uri contactUri = ContentUris.withAppendedId(Contacts.CONTENT_URI, contactId);
            final Intent viewIntent = new Intent(Intent.ACTION_VIEW, contactUri);
            startActivity(viewIntent);
            finish();
        } else if (count > 1) {
            Intent listIntent = new Intent(Intent.ACTION_SEARCH);
            listIntent.setComponent(new ComponentName(this, ContactsListActivity.class));
            listIntent.putExtras(mCreateExtras);
            startActivity(listIntent);
            finish();
        } else {
            if (mCreateForce) {
                Intent createIntent = new Intent(Intent.ACTION_INSERT, RawContacts.CONTENT_URI);
                createIntent.putExtras(mCreateExtras);
                createIntent.setType(RawContacts.CONTENT_TYPE);
                startActivity(createIntent);
                finish();
            } else {
	        showDialog(CREATE_CONTACT_DIALOG);
           }
        }
    }
    @Override
    protected Dialog onCreateDialog(int id) {
        switch(id) {
	    case CREATE_CONTACT_DIALOG:
                final Intent createIntent = new Intent(Intent.ACTION_INSERT_OR_EDIT);
                createIntent.putExtras(mCreateExtras);
                createIntent.setType(RawContacts.CONTENT_ITEM_TYPE);
                final CharSequence message = getResources().getString(
                        R.string.add_contact_dlg_message_fmt, mCreateDescrip);
                return new AlertDialog.Builder(this)
                        .setTitle(R.string.add_contact_dlg_title)
                        .setMessage(message)
                        .setPositiveButton(android.R.string.ok,
                                new IntentClickListener(this, createIntent))
                        .setNegativeButton(android.R.string.cancel,
                                new IntentClickListener(this, null))
                        .create();
        }
	return super.onCreateDialog(id);
    }
    private static class IntentClickListener implements DialogInterface.OnClickListener {
        private Activity mParent;
        private Intent mIntent;
        public IntentClickListener(Activity parent, Intent intent) {
            mParent = parent;
            mIntent = intent;
        }
        public void onClick(DialogInterface dialog, int which) {
            if (mIntent != null) {
                mParent.startActivity(mIntent);
            }
            mParent.finish();
        }
    }
    @Override
    public void startSearch(String initialQuery, boolean selectInitialQuery, Bundle appSearchData,
            boolean globalSearch) {
        if (globalSearch) {
            super.startSearch(initialQuery, selectInitialQuery, appSearchData, globalSearch);
        } else {
            ContactsSearchManager.startSearch(this, initialQuery);
        }
    }
}
