public class ContactHeaderWidget extends FrameLayout implements View.OnClickListener {
    private static final String TAG = "ContactHeaderWidget";
    private TextView mDisplayNameView;
    private View mAggregateBadge;
    private TextView mPhoneticNameView;
    private CheckBox mStarredView;
    private QuickContactBadge mPhotoView;
    private ImageView mPresenceView;
    private TextView mStatusView;
    private TextView mStatusAttributionView;
    private int mNoPhotoResource;
    private QueryHandler mQueryHandler;
    protected Uri mContactUri;
    protected String[] mExcludeMimes = null;
    protected ContentResolver mContentResolver;
    public interface ContactHeaderListener {
        public void onPhotoClick(View view);
        public void onDisplayNameClick(View view);
    }
    private ContactHeaderListener mListener;
    private interface ContactQuery {
        String[] COLUMNS = new String[] {
            Contacts._ID,
            Contacts.LOOKUP_KEY,
            Contacts.PHOTO_ID,
            Contacts.DISPLAY_NAME,
            Contacts.PHONETIC_NAME,
            Contacts.STARRED,
            Contacts.CONTACT_PRESENCE,
            Contacts.CONTACT_STATUS,
            Contacts.CONTACT_STATUS_TIMESTAMP,
            Contacts.CONTACT_STATUS_RES_PACKAGE,
            Contacts.CONTACT_STATUS_LABEL,
        };
        int _ID = 0;
        int LOOKUP_KEY = 1;
        int PHOTO_ID = 2;
        int DISPLAY_NAME = 3;
        int PHONETIC_NAME = 4;
        int STARRED = 5;
        int CONTACT_PRESENCE_STATUS = 6;
        int CONTACT_STATUS = 7;
        int CONTACT_STATUS_TIMESTAMP = 8;
        int CONTACT_STATUS_RES_PACKAGE = 9;
        int CONTACT_STATUS_LABEL = 10;
    }
    private interface PhotoQuery {
        String[] COLUMNS = new String[] {
            Photo.PHOTO
        };
        int PHOTO = 0;
    }
    protected static final String[] PHONE_LOOKUP_PROJECTION = new String[] {
        PhoneLookup._ID,
        PhoneLookup.LOOKUP_KEY,
    };
    protected static final int PHONE_LOOKUP_CONTACT_ID_COLUMN_INDEX = 0;
    protected static final int PHONE_LOOKUP_CONTACT_LOOKUP_KEY_COLUMN_INDEX = 1;
    protected static final String[] EMAIL_LOOKUP_PROJECTION = new String[] {
        RawContacts.CONTACT_ID,
        Contacts.LOOKUP_KEY,
    };
    protected static final int EMAIL_LOOKUP_CONTACT_ID_COLUMN_INDEX = 0;
    protected static final int EMAIL_LOOKUP_CONTACT_LOOKUP_KEY_COLUMN_INDEX = 1;
    protected static final String[] CONTACT_LOOKUP_PROJECTION = new String[] {
        Contacts._ID,
    };
    protected static final int CONTACT_LOOKUP_ID_COLUMN_INDEX = 0;
    private static final int TOKEN_CONTACT_INFO = 0;
    private static final int TOKEN_PHONE_LOOKUP = 1;
    private static final int TOKEN_EMAIL_LOOKUP = 2;
    private static final int TOKEN_PHOTO_QUERY = 3;
    public ContactHeaderWidget(Context context) {
        this(context, null);
    }
    public ContactHeaderWidget(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }
    public ContactHeaderWidget(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        mContentResolver = mContext.getContentResolver();
        LayoutInflater inflater =
            (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        inflater.inflate(R.layout.contact_header, this);
        mDisplayNameView = (TextView) findViewById(R.id.name);
        mAggregateBadge = findViewById(R.id.aggregate_badge);
        mPhoneticNameView = (TextView) findViewById(R.id.phonetic_name);
        mStarredView = (CheckBox)findViewById(R.id.star);
        mStarredView.setOnClickListener(this);
        mPhotoView = (QuickContactBadge) findViewById(R.id.photo);
        mPresenceView = (ImageView) findViewById(R.id.presence);
        mStatusView = (TextView)findViewById(R.id.status);
        mStatusAttributionView = (TextView)findViewById(R.id.status_date);
        long now = SystemClock.elapsedRealtime();
        int num = (int) now & 0xf;
        if (num < 9) {
            mNoPhotoResource = R.drawable.ic_contact_picture;
        } else if (num < 14) {
            mNoPhotoResource = R.drawable.ic_contact_picture_2;
        } else {
            mNoPhotoResource = R.drawable.ic_contact_picture_3;
        }
        resetAsyncQueryHandler();
    }
    public void enableClickListeners() {
        mDisplayNameView.setOnClickListener(this);
        mPhotoView.setOnClickListener(this);
    }
    public void setContactHeaderListener(ContactHeaderListener listener) {
        mListener = listener;
    }
    private void performPhotoClick() {
        if (mListener != null) {
            mListener.onPhotoClick(mPhotoView);
        }
    }
    private void performDisplayNameClick() {
        if (mListener != null) {
            mListener.onDisplayNameClick(mDisplayNameView);
        }
    }
    private class QueryHandler extends AsyncQueryHandler {
        public QueryHandler(ContentResolver cr) {
            super(cr);
        }
        @Override
        protected void onQueryComplete(int token, Object cookie, Cursor cursor) {
            try{
                if (this != mQueryHandler) {
                    Log.d(TAG, "onQueryComplete: discard result, the query handler is reset!");
                    return;
                }
                switch (token) {
                    case TOKEN_PHOTO_QUERY: {
                        Bitmap photoBitmap = null;
                        if (cursor != null && cursor.moveToFirst()
                                && !cursor.isNull(PhotoQuery.PHOTO)) {
                            byte[] photoData = cursor.getBlob(PhotoQuery.PHOTO);
                            photoBitmap = BitmapFactory.decodeByteArray(photoData, 0,
                                    photoData.length, null);
                        }
                        if (photoBitmap == null) {
                            photoBitmap = loadPlaceholderPhoto(null);
                        }
                        mPhotoView.setImageBitmap(photoBitmap);
                        if (cookie != null && cookie instanceof Uri) {
                            mPhotoView.assignContactUri((Uri) cookie);
                        }
                        invalidate();
                        break;
                    }
                    case TOKEN_CONTACT_INFO: {
                        if (cursor != null && cursor.moveToFirst()) {
                            bindContactInfo(cursor);
                            Uri lookupUri = Contacts.getLookupUri(cursor.getLong(ContactQuery._ID),
                                    cursor.getString(ContactQuery.LOOKUP_KEY));
                            final long photoId = cursor.getLong(ContactQuery.PHOTO_ID);
                            if (photoId == 0) {
                                mPhotoView.setImageBitmap(loadPlaceholderPhoto(null));
                                if (cookie != null && cookie instanceof Uri) {
                                    mPhotoView.assignContactUri((Uri) cookie);
                                }
                                invalidate();
                            } else {
                                startPhotoQuery(photoId, lookupUri,
                                        false );
                            }
                        } else {
                            setDisplayName(null, null);
                            setSocialSnippet(null);
                            setPhoto(loadPlaceholderPhoto(null));
                        }
                        break;
                    }
                    case TOKEN_PHONE_LOOKUP: {
                        if (cursor != null && cursor.moveToFirst()) {
                            long contactId = cursor.getLong(PHONE_LOOKUP_CONTACT_ID_COLUMN_INDEX);
                            String lookupKey = cursor.getString(
                                    PHONE_LOOKUP_CONTACT_LOOKUP_KEY_COLUMN_INDEX);
                            bindFromContactUriInternal(Contacts.getLookupUri(contactId, lookupKey),
                                    false );
                        } else {
                            String phoneNumber = (String) cookie;
                            setDisplayName(phoneNumber, null);
                            setSocialSnippet(null);
                            setPhoto(loadPlaceholderPhoto(null));
                            mPhotoView.assignContactFromPhone(phoneNumber, true);
                        }
                        break;
                    }
                    case TOKEN_EMAIL_LOOKUP: {
                        if (cursor != null && cursor.moveToFirst()) {
                            long contactId = cursor.getLong(EMAIL_LOOKUP_CONTACT_ID_COLUMN_INDEX);
                            String lookupKey = cursor.getString(
                                    EMAIL_LOOKUP_CONTACT_LOOKUP_KEY_COLUMN_INDEX);
                            bindFromContactUriInternal(Contacts.getLookupUri(contactId, lookupKey),
                                    false );
                        } else {
                            String emailAddress = (String) cookie;
                            setDisplayName(emailAddress, null);
                            setSocialSnippet(null);
                            setPhoto(loadPlaceholderPhoto(null));
                            mPhotoView.assignContactFromEmail(emailAddress, true);
                        }
                        break;
                    }
                }
            } finally {
                if (cursor != null) {
                    cursor.close();
                }
            }
        }
    }
    public void showAggregateBadge(boolean showBagde) {
        mAggregateBadge.setVisibility(showBagde ? View.VISIBLE : View.GONE);
    }
    public void showStar(boolean showStar) {
        mStarredView.setVisibility(showStar ? View.VISIBLE : View.GONE);
    }
    public void setStared(boolean starred) {
        mStarredView.setChecked(starred);
    }
    public void setPresence(int presence) {
        mPresenceView.setImageResource(StatusUpdates.getPresenceIconResourceId(presence));
    }
    public void setContactUri(Uri uri) {
        setContactUri(uri, true);
    }
    public void setContactUri(Uri uri, boolean sendToFastrack) {
        mContactUri = uri;
        if (sendToFastrack) {
            mPhotoView.assignContactUri(uri);
        }
    }
    public void setPhoto(Bitmap bitmap) {
        mPhotoView.setImageBitmap(bitmap);
    }
    public void setDisplayName(CharSequence displayName, CharSequence phoneticName) {
        mDisplayNameView.setText(displayName);
        if (!TextUtils.isEmpty(phoneticName)) {
            mPhoneticNameView.setText(phoneticName);
            mPhoneticNameView.setVisibility(View.VISIBLE);
        } else {
            mPhoneticNameView.setVisibility(View.GONE);
        }
    }
    public void setSocialSnippet(CharSequence snippet) {
        if (snippet == null) {
            mStatusView.setVisibility(View.GONE);
            mStatusAttributionView.setVisibility(View.GONE);
        } else {
            mStatusView.setText(snippet);
            mStatusView.setVisibility(View.VISIBLE);
        }
    }
    public void setExcludeMimes(String[] excludeMimes) {
        mExcludeMimes = excludeMimes;
        mPhotoView.setExcludeMimes(excludeMimes);
    }
    public void bindFromContactLookupUri(Uri contactLookupUri) {
        bindFromContactUriInternal(contactLookupUri, true );
    }
    private void bindFromContactUriInternal(Uri contactUri, boolean resetQueryHandler) {
        mContactUri = contactUri;
        startContactQuery(contactUri, resetQueryHandler);
    }
    public void bindFromEmail(String emailAddress) {
        resetAsyncQueryHandler();
        mQueryHandler.startQuery(TOKEN_EMAIL_LOOKUP, emailAddress,
                Uri.withAppendedPath(Email.CONTENT_LOOKUP_URI, Uri.encode(emailAddress)),
                EMAIL_LOOKUP_PROJECTION, null, null, null);
    }
    public void bindFromPhoneNumber(String number) {
        resetAsyncQueryHandler();
        mQueryHandler.startQuery(TOKEN_PHONE_LOOKUP, number,
                Uri.withAppendedPath(PhoneLookup.CONTENT_FILTER_URI, Uri.encode(number)),
                PHONE_LOOKUP_PROJECTION, null, null, null);
    }
    private void startContactQuery(Uri contactUri, boolean resetQueryHandler) {
        if (resetQueryHandler) {
            resetAsyncQueryHandler();
        }
        mQueryHandler.startQuery(TOKEN_CONTACT_INFO, contactUri, contactUri, ContactQuery.COLUMNS,
                null, null, null);
    }
    protected void startPhotoQuery(long photoId, Uri lookupKey, boolean resetQueryHandler) {
        if (resetQueryHandler) {
            resetAsyncQueryHandler();
        }
        mQueryHandler.startQuery(TOKEN_PHOTO_QUERY, lookupKey,
                ContentUris.withAppendedId(Data.CONTENT_URI, photoId), PhotoQuery.COLUMNS,
                null, null, null);
    }
    public void wipeClean() {
        resetAsyncQueryHandler();
        setDisplayName(null, null);
        setPhoto(loadPlaceholderPhoto(null));
        setSocialSnippet(null);
        setPresence(0);
        mContactUri = null;
        mExcludeMimes = null;
    }
    private void resetAsyncQueryHandler() {
        mQueryHandler = new QueryHandler(mContentResolver);
    }
    protected void bindContactInfo(Cursor c) {
        final String displayName = c.getString(ContactQuery.DISPLAY_NAME);
        final String phoneticName = c.getString(ContactQuery.PHONETIC_NAME);
        this.setDisplayName(displayName, phoneticName);
        final boolean starred = c.getInt(ContactQuery.STARRED) != 0;
        mStarredView.setChecked(starred);
        if (!c.isNull(ContactQuery.CONTACT_PRESENCE_STATUS)) {
            int presence = c.getInt(ContactQuery.CONTACT_PRESENCE_STATUS);
            mPresenceView.setImageResource(StatusUpdates.getPresenceIconResourceId(presence));
            mPresenceView.setVisibility(View.VISIBLE);
        } else {
            mPresenceView.setVisibility(View.GONE);
        }
        String status = c.getString(ContactQuery.CONTACT_STATUS);
        if (!TextUtils.isEmpty(status)) {
            mStatusView.setText(status);
            mStatusView.setVisibility(View.VISIBLE);
            CharSequence timestamp = null;
            if (!c.isNull(ContactQuery.CONTACT_STATUS_TIMESTAMP)) {
                long date = c.getLong(ContactQuery.CONTACT_STATUS_TIMESTAMP);
                int flags = DateUtils.FORMAT_ABBREV_RELATIVE;
                timestamp = DateUtils.getRelativeTimeSpanString(date,
                        System.currentTimeMillis(), DateUtils.MINUTE_IN_MILLIS, flags);
            }
            String label = null;
            if (!c.isNull(ContactQuery.CONTACT_STATUS_LABEL)) {
                String resPackage = c.getString(ContactQuery.CONTACT_STATUS_RES_PACKAGE);
                int labelResource = c.getInt(ContactQuery.CONTACT_STATUS_LABEL);
                Resources resources;
                if (TextUtils.isEmpty(resPackage)) {
                    resources = getResources();
                } else {
                    PackageManager pm = getContext().getPackageManager();
                    try {
                        resources = pm.getResourcesForApplication(resPackage);
                    } catch (NameNotFoundException e) {
                        Log.w(TAG, "Contact status update resource package not found: "
                                + resPackage);
                        resources = null;
                    }
                }
                if (resources != null) {
                    try {
                        label = resources.getString(labelResource);
                    } catch (NotFoundException e) {
                        Log.w(TAG, "Contact status update resource not found: " + resPackage + "@"
                                + labelResource);
                    }
                }
            }
            CharSequence attribution;
            if (timestamp != null && label != null) {
                attribution = getContext().getString(
                        R.string.contact_status_update_attribution_with_date,
                        timestamp, label);
            } else if (timestamp == null && label != null) {
                attribution = getContext().getString(
                        R.string.contact_status_update_attribution,
                        label);
            } else if (timestamp != null) {
                attribution = timestamp;
            } else {
                attribution = null;
            }
            if (attribution != null) {
                mStatusAttributionView.setText(attribution);
                mStatusAttributionView.setVisibility(View.VISIBLE);
            } else {
                mStatusAttributionView.setVisibility(View.GONE);
            }
        } else {
            mStatusView.setVisibility(View.GONE);
            mStatusAttributionView.setVisibility(View.GONE);
        }
    }
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.star: {
                if (mContactUri != null) {
                    final ContentValues values = new ContentValues(1);
                    values.put(Contacts.STARRED, mStarredView.isChecked());
                    mContentResolver.update(mContactUri, values, null, null);
                }
                break;
            }
            case R.id.photo: {
                performPhotoClick();
                break;
            }
            case R.id.name: {
                performDisplayNameClick();
                break;
            }
        }
    }
    private Bitmap loadPlaceholderPhoto(BitmapFactory.Options options) {
        if (mNoPhotoResource == 0) {
            return null;
        }
        return BitmapFactory.decodeResource(mContext.getResources(),
                mNoPhotoResource, options);
    }
}
