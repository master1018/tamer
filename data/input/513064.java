public class QuickContactBadge extends ImageView implements OnClickListener {
    private Uri mContactUri;
    private String mContactEmail;
    private String mContactPhone;
    private int mMode;
    private QueryHandler mQueryHandler;
    private Drawable mBadgeBackground;
    private Drawable mNoBadgeBackground;
    protected String[] mExcludeMimes = null;
    static final private int TOKEN_EMAIL_LOOKUP = 0;
    static final private int TOKEN_PHONE_LOOKUP = 1;
    static final private int TOKEN_EMAIL_LOOKUP_AND_TRIGGER = 2;
    static final private int TOKEN_PHONE_LOOKUP_AND_TRIGGER = 3;
    static final private int TOKEN_CONTACT_LOOKUP_AND_TRIGGER = 4;
    static final String[] EMAIL_LOOKUP_PROJECTION = new String[] {
        RawContacts.CONTACT_ID,
        Contacts.LOOKUP_KEY,
    };
    static final int EMAIL_ID_COLUMN_INDEX = 0;
    static final int EMAIL_LOOKUP_STRING_COLUMN_INDEX = 1;
    static final String[] PHONE_LOOKUP_PROJECTION = new String[] {
        PhoneLookup._ID,
        PhoneLookup.LOOKUP_KEY,
    };
    static final int PHONE_ID_COLUMN_INDEX = 0;
    static final int PHONE_LOOKUP_STRING_COLUMN_INDEX = 1;
    static final String[] CONTACT_LOOKUP_PROJECTION = new String[] {
        Contacts._ID,
        Contacts.LOOKUP_KEY,
    };
    static final int CONTACT_ID_COLUMN_INDEX = 0;
    static final int CONTACT_LOOKUPKEY_COLUMN_INDEX = 1;
    public QuickContactBadge(Context context) {
        this(context, null);
    }
    public QuickContactBadge(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }
    public QuickContactBadge(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        TypedArray a =
            context.obtainStyledAttributes(attrs,
                    com.android.internal.R.styleable.QuickContactBadge, defStyle, 0);
        mMode = a.getInt(com.android.internal.R.styleable.QuickContactBadge_quickContactWindowSize,
                QuickContact.MODE_MEDIUM);
        a.recycle();
        init();
        mBadgeBackground = getBackground();
    }
    private void init() {
        mQueryHandler = new QueryHandler(mContext.getContentResolver());
        setOnClickListener(this);
    }
    public void setMode(int size) {
        mMode = size;
    }
    public void assignContactUri(Uri contactUri) {
        mContactUri = contactUri;
        mContactEmail = null;
        mContactPhone = null;
        onContactUriChanged();
    }
    private void onContactUriChanged() {
        if (mContactUri == null && mContactEmail == null && mContactPhone == null) {
            if (mNoBadgeBackground == null) {
                mNoBadgeBackground = getResources().getDrawable(R.drawable.quickcontact_nobadge);
            }
            setBackgroundDrawable(mNoBadgeBackground);
        } else {
            setBackgroundDrawable(mBadgeBackground);
        }
    }
    public void assignContactFromEmail(String emailAddress, boolean lazyLookup) {
        mContactEmail = emailAddress;
        if (!lazyLookup) {
            mQueryHandler.startQuery(TOKEN_EMAIL_LOOKUP, null,
                    Uri.withAppendedPath(Email.CONTENT_LOOKUP_URI, Uri.encode(mContactEmail)),
                    EMAIL_LOOKUP_PROJECTION, null, null, null);
        } else {
            mContactUri = null;
            onContactUriChanged();
        }
    }
    public void assignContactFromPhone(String phoneNumber, boolean lazyLookup) {
        mContactPhone = phoneNumber;
        if (!lazyLookup) {
            mQueryHandler.startQuery(TOKEN_PHONE_LOOKUP, null,
                    Uri.withAppendedPath(PhoneLookup.CONTENT_FILTER_URI, mContactPhone),
                    PHONE_LOOKUP_PROJECTION, null, null, null);
        } else {
            mContactUri = null;
            onContactUriChanged();
        }
    }
    public void onClick(View v) {
        if (mContactUri != null) {
            mQueryHandler.startQuery(TOKEN_CONTACT_LOOKUP_AND_TRIGGER, null,
                    mContactUri,
                    CONTACT_LOOKUP_PROJECTION, null, null, null);
        } else if (mContactEmail != null) {
            mQueryHandler.startQuery(TOKEN_EMAIL_LOOKUP_AND_TRIGGER, mContactEmail,
                    Uri.withAppendedPath(Email.CONTENT_LOOKUP_URI, Uri.encode(mContactEmail)),
                    EMAIL_LOOKUP_PROJECTION, null, null, null);
        } else if (mContactPhone != null) {
            mQueryHandler.startQuery(TOKEN_PHONE_LOOKUP_AND_TRIGGER, mContactPhone,
                    Uri.withAppendedPath(PhoneLookup.CONTENT_FILTER_URI, mContactPhone),
                    PHONE_LOOKUP_PROJECTION, null, null, null);
        } else {
            return;
        }
    }
    public void setExcludeMimes(String[] excludeMimes) {
        mExcludeMimes = excludeMimes;
    }
    private void trigger(Uri lookupUri) {
        QuickContact.showQuickContact(getContext(), this, lookupUri, mMode, mExcludeMimes);
    }
    private class QueryHandler extends AsyncQueryHandler {
        public QueryHandler(ContentResolver cr) {
            super(cr);
        }
        @Override
        protected void onQueryComplete(int token, Object cookie, Cursor cursor) {
            Uri lookupUri = null;
            Uri createUri = null;
            boolean trigger = false;
            try {
                switch(token) {
                    case TOKEN_PHONE_LOOKUP_AND_TRIGGER:
                        trigger = true;
                        createUri = Uri.fromParts("tel", (String)cookie, null);
                    case TOKEN_PHONE_LOOKUP: {
                        if (cursor != null && cursor.moveToFirst()) {
                            long contactId = cursor.getLong(PHONE_ID_COLUMN_INDEX);
                            String lookupKey = cursor.getString(PHONE_LOOKUP_STRING_COLUMN_INDEX);
                            lookupUri = Contacts.getLookupUri(contactId, lookupKey);
                        }
                        break;
                    }
                    case TOKEN_EMAIL_LOOKUP_AND_TRIGGER:
                        trigger = true;
                        createUri = Uri.fromParts("mailto", (String)cookie, null);
                    case TOKEN_EMAIL_LOOKUP: {
                        if (cursor != null && cursor.moveToFirst()) {
                            long contactId = cursor.getLong(EMAIL_ID_COLUMN_INDEX);
                            String lookupKey = cursor.getString(EMAIL_LOOKUP_STRING_COLUMN_INDEX);
                            lookupUri = Contacts.getLookupUri(contactId, lookupKey);
                        }
                    }
                    case TOKEN_CONTACT_LOOKUP_AND_TRIGGER: {
                        if (cursor != null && cursor.moveToFirst()) {
                            long contactId = cursor.getLong(CONTACT_ID_COLUMN_INDEX);
                            String lookupKey = cursor.getString(CONTACT_LOOKUPKEY_COLUMN_INDEX);
                            lookupUri = Contacts.getLookupUri(contactId, lookupKey);
                            trigger = true;
                        }
                        break;
                    }
                }
            } finally {
                if (cursor != null) {
                    cursor.close();
                }
            }
            mContactUri = lookupUri;
            onContactUriChanged();
            if (trigger && lookupUri != null) {
                trigger(lookupUri);
            } else if (createUri != null) {
                final Intent intent = new Intent(Intents.SHOW_OR_CREATE_CONTACT, createUri);
                getContext().startActivity(intent);
            }
        }
    }
}
