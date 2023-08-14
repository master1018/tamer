public class VCardComposer {
    private static final String LOG_TAG = "VCardComposer";
    public static final int DEFAULT_PHONE_TYPE = Phone.TYPE_HOME;
    public static final int DEFAULT_POSTAL_TYPE = StructuredPostal.TYPE_HOME;
    public static final int DEFAULT_EMAIL_TYPE = Email.TYPE_OTHER;
    public static final String FAILURE_REASON_FAILED_TO_GET_DATABASE_INFO =
        "Failed to get database information";
    public static final String FAILURE_REASON_NO_ENTRY =
        "There's no exportable in the database";
    public static final String FAILURE_REASON_NOT_INITIALIZED =
        "The vCard composer object is not correctly initialized";
    public static final String FAILURE_REASON_UNSUPPORTED_URI =
        "The Uri vCard composer received is not supported by the composer.";
    public static final String NO_ERROR = "No error";
    public static final String VCARD_TYPE_STRING_DOCOMO = "docomo";
    private static final String SHIFT_JIS = "SHIFT_JIS";
    private static final String UTF_8 = "UTF-8";
    public static final String VCARD_TEST_AUTHORITY = "com.android.unit_tests.vcard";
    public static final Uri VCARD_TEST_AUTHORITY_URI =
        Uri.parse("content:
    public static final Uri CONTACTS_TEST_CONTENT_URI =
        Uri.withAppendedPath(VCARD_TEST_AUTHORITY_URI, "contacts");
    private static final Map<Integer, String> sImMap;
    static {
        sImMap = new HashMap<Integer, String>();
        sImMap.put(Im.PROTOCOL_AIM, VCardConstants.PROPERTY_X_AIM);
        sImMap.put(Im.PROTOCOL_MSN, VCardConstants.PROPERTY_X_MSN);
        sImMap.put(Im.PROTOCOL_YAHOO, VCardConstants.PROPERTY_X_YAHOO);
        sImMap.put(Im.PROTOCOL_ICQ, VCardConstants.PROPERTY_X_ICQ);
        sImMap.put(Im.PROTOCOL_JABBER, VCardConstants.PROPERTY_X_JABBER);
        sImMap.put(Im.PROTOCOL_SKYPE, VCardConstants.PROPERTY_X_SKYPE_USERNAME);
    }
    public static interface OneEntryHandler {
        public boolean onInit(Context context);
        public boolean onEntryCreated(String vcard);
        public void onTerminate();
    }
    public class HandlerForOutputStream implements OneEntryHandler {
        @SuppressWarnings("hiding")
        private static final String LOG_TAG = "vcard.VCardComposer.HandlerForOutputStream";
        final private OutputStream mOutputStream; 
        private Writer mWriter;
        private boolean mOnTerminateIsCalled = false;
        public HandlerForOutputStream(OutputStream outputStream) {
            mOutputStream = outputStream;
        }
        public boolean onInit(Context context) {
            try {
                mWriter = new BufferedWriter(new OutputStreamWriter(
                        mOutputStream, mCharsetString));
            } catch (UnsupportedEncodingException e1) {
                Log.e(LOG_TAG, "Unsupported charset: " + mCharsetString);
                mErrorReason = "Encoding is not supported (usually this does not happen!): "
                        + mCharsetString;
                return false;
            }
            if (mIsDoCoMo) {
                try {
                    mWriter.write(createOneEntryInternal("-1", null));
                } catch (VCardException e) {
                    Log.e(LOG_TAG, "VCardException has been thrown during on Init(): " +
                            e.getMessage());
                    return false;
                } catch (IOException e) {
                    Log.e(LOG_TAG,
                            "IOException occurred during exportOneContactData: "
                                    + e.getMessage());
                    mErrorReason = "IOException occurred: " + e.getMessage();
                    return false;
                }
            }
            return true;
        }
        public boolean onEntryCreated(String vcard) {
            try {
                mWriter.write(vcard);
            } catch (IOException e) {
                Log.e(LOG_TAG,
                        "IOException occurred during exportOneContactData: "
                                + e.getMessage());
                mErrorReason = "IOException occurred: " + e.getMessage();
                return false;
            }
            return true;
        }
        public void onTerminate() {
            mOnTerminateIsCalled = true;
            if (mWriter != null) {
                try {
                    mWriter.flush();
                    if (mOutputStream != null
                            && mOutputStream instanceof FileOutputStream) {
                            ((FileOutputStream) mOutputStream).getFD().sync();
                    }
                } catch (IOException e) {
                    Log.d(LOG_TAG,
                            "IOException during closing the output stream: "
                                    + e.getMessage());
                } finally {
                    try {
                        mWriter.close();
                    } catch (IOException e) {
                    }
                }
            }
        }
        @Override
        public void finalize() {
            if (!mOnTerminateIsCalled) {
                onTerminate();
            }
        }
    }
    private final Context mContext;
    private final int mVCardType;
    private final boolean mCareHandlerErrors;
    private final ContentResolver mContentResolver;
    private final boolean mIsDoCoMo;
    private final boolean mUsesShiftJis;
    private Cursor mCursor;
    private int mIdColumn;
    private final String mCharsetString;
    private boolean mTerminateIsCalled;
    private final List<OneEntryHandler> mHandlerList;
    private String mErrorReason = NO_ERROR;
    private static final String[] sContactsProjection = new String[] {
        Contacts._ID,
    };
    public VCardComposer(Context context) {
        this(context, VCardConfig.VCARD_TYPE_DEFAULT, true);
    }
    public VCardComposer(Context context, int vcardType) {
        this(context, vcardType, true);
    }
    public VCardComposer(Context context, String vcardTypeStr, boolean careHandlerErrors) {
        this(context, VCardConfig.getVCardTypeFromString(vcardTypeStr), careHandlerErrors);
    }
    public VCardComposer(final Context context, final int vcardType,
            final boolean careHandlerErrors) {
        mContext = context;
        mVCardType = vcardType;
        mCareHandlerErrors = careHandlerErrors;
        mContentResolver = context.getContentResolver();
        mIsDoCoMo = VCardConfig.isDoCoMo(vcardType);
        mUsesShiftJis = VCardConfig.usesShiftJis(vcardType);
        mHandlerList = new ArrayList<OneEntryHandler>();
        if (mIsDoCoMo) {
            String charset;
            try {
                charset = CharsetUtils.charsetForVendor(SHIFT_JIS, "docomo").name();
            } catch (UnsupportedCharsetException e) {
                Log.e(LOG_TAG, "DoCoMo-specific SHIFT_JIS was not found. Use SHIFT_JIS as is.");
                charset = SHIFT_JIS;
            }
            mCharsetString = charset;
        } else if (mUsesShiftJis) {
            String charset;
            try {
                charset = CharsetUtils.charsetForVendor(SHIFT_JIS).name();
            } catch (UnsupportedCharsetException e) {
                Log.e(LOG_TAG, "Vendor-specific SHIFT_JIS was not found. Use SHIFT_JIS as is.");
                charset = SHIFT_JIS;
            }
            mCharsetString = charset;
        } else {
            mCharsetString = UTF_8;
        }
    }
    public void addHandler(OneEntryHandler handler) {
        if (handler != null) {
            mHandlerList.add(handler);
        }
    }
    public boolean init() {
        return init(null, null);
    }
    public boolean init(final String selection, final String[] selectionArgs) {
        return init(Contacts.CONTENT_URI, selection, selectionArgs, null);
    }
    public boolean init(final Uri contentUri, final String selection,
            final String[] selectionArgs, final String sortOrder) {
        if (contentUri == null) {
            return false;
        }
        if (mCareHandlerErrors) {
            List<OneEntryHandler> finishedList = new ArrayList<OneEntryHandler>(
                    mHandlerList.size());
            for (OneEntryHandler handler : mHandlerList) {
                if (!handler.onInit(mContext)) {
                    for (OneEntryHandler finished : finishedList) {
                        finished.onTerminate();
                    }
                    return false;
                }
            }
        } else {
            for (OneEntryHandler handler : mHandlerList) {
                handler.onInit(mContext);
            }
        }
        final String[] projection;
        if (Contacts.CONTENT_URI.equals(contentUri) ||
                CONTACTS_TEST_CONTENT_URI.equals(contentUri)) {
            projection = sContactsProjection;
        } else {
            mErrorReason = FAILURE_REASON_UNSUPPORTED_URI;
            return false;
        }
        mCursor = mContentResolver.query(
                contentUri, projection, selection, selectionArgs, sortOrder);
        if (mCursor == null) {
            mErrorReason = FAILURE_REASON_FAILED_TO_GET_DATABASE_INFO;
            return false;
        }
        if (getCount() == 0 || !mCursor.moveToFirst()) {
            try {
                mCursor.close();
            } catch (SQLiteException e) {
                Log.e(LOG_TAG, "SQLiteException on Cursor#close(): " + e.getMessage());
            } finally {
                mCursor = null;
                mErrorReason = FAILURE_REASON_NO_ENTRY;
            }
            return false;
        }
        mIdColumn = mCursor.getColumnIndex(Contacts._ID);
        return true;
    }
    public boolean createOneEntry() {
        return createOneEntry(null);
    }
    public boolean createOneEntry(Method getEntityIteratorMethod) {
        if (mCursor == null || mCursor.isAfterLast()) {
            mErrorReason = FAILURE_REASON_NOT_INITIALIZED;
            return false;
        }
        String vcard;
        try {
            if (mIdColumn >= 0) {
                vcard = createOneEntryInternal(mCursor.getString(mIdColumn),
                        getEntityIteratorMethod);
            } else {
                Log.e(LOG_TAG, "Incorrect mIdColumn: " + mIdColumn);
                return true;
            }
        } catch (VCardException e) {
            Log.e(LOG_TAG, "VCardException has been thrown: " + e.getMessage());
            return false;
        } catch (OutOfMemoryError error) {
            Log.e(LOG_TAG, "OutOfMemoryError occured. Ignore the entry.");
            System.gc();
            return true;
        } finally {
            mCursor.moveToNext();
        }
        if (mCareHandlerErrors) {
            List<OneEntryHandler> finishedList = new ArrayList<OneEntryHandler>(
                    mHandlerList.size());
            for (OneEntryHandler handler : mHandlerList) {
                if (!handler.onEntryCreated(vcard)) {
                    return false;
                }
            }
        } else {
            for (OneEntryHandler handler : mHandlerList) {
                handler.onEntryCreated(vcard);
            }
        }
        return true;
    }
    private String createOneEntryInternal(final String contactId,
            Method getEntityIteratorMethod) throws VCardException {
        final Map<String, List<ContentValues>> contentValuesListMap =
                new HashMap<String, List<ContentValues>>();
        EntityIterator entityIterator = null;
        try {
            final Uri uri = RawContactsEntity.CONTENT_URI.buildUpon()
                    .appendQueryParameter(Data.FOR_EXPORT_ONLY, "1")
                    .build();
            final String selection = Data.CONTACT_ID + "=?";
            final String[] selectionArgs = new String[] {contactId};
            if (getEntityIteratorMethod != null) {
                try {
                    entityIterator = (EntityIterator)getEntityIteratorMethod.invoke(null,
                            mContentResolver, uri, selection, selectionArgs, null);
                } catch (IllegalArgumentException e) {
                    Log.e(LOG_TAG, "IllegalArgumentException has been thrown: " +
                            e.getMessage());
                } catch (IllegalAccessException e) {
                    Log.e(LOG_TAG, "IllegalAccessException has been thrown: " +
                            e.getMessage());
                } catch (InvocationTargetException e) {
                    Log.e(LOG_TAG, "InvocationTargetException has been thrown: ");
                    StackTraceElement[] stackTraceElements = e.getCause().getStackTrace();
                    for (StackTraceElement element : stackTraceElements) {
                        Log.e(LOG_TAG, "    at " + element.toString());
                    }
                    throw new VCardException("InvocationTargetException has been thrown: " +
                            e.getCause().getMessage());
                }
            } else {
                entityIterator = RawContacts.newEntityIterator(mContentResolver.query(
                        uri, null, selection, selectionArgs, null));
            }
            if (entityIterator == null) {
                Log.e(LOG_TAG, "EntityIterator is null");
                return "";
            }
            if (!entityIterator.hasNext()) {
                Log.w(LOG_TAG, "Data does not exist. contactId: " + contactId);
                return "";
            }
            while (entityIterator.hasNext()) {
                Entity entity = entityIterator.next();
                for (NamedContentValues namedContentValues : entity.getSubValues()) {
                    ContentValues contentValues = namedContentValues.values;
                    String key = contentValues.getAsString(Data.MIMETYPE);
                    if (key != null) {
                        List<ContentValues> contentValuesList =
                                contentValuesListMap.get(key);
                        if (contentValuesList == null) {
                            contentValuesList = new ArrayList<ContentValues>();
                            contentValuesListMap.put(key, contentValuesList);
                        }
                        contentValuesList.add(contentValues);
                    }
                }
            }
        } finally {
            if (entityIterator != null) {
                entityIterator.close();
            }
        }
        final VCardBuilder builder = new VCardBuilder(mVCardType);
        builder.appendNameProperties(contentValuesListMap.get(StructuredName.CONTENT_ITEM_TYPE))
                .appendNickNames(contentValuesListMap.get(Nickname.CONTENT_ITEM_TYPE))
                .appendPhones(contentValuesListMap.get(Phone.CONTENT_ITEM_TYPE))
                .appendEmails(contentValuesListMap.get(Email.CONTENT_ITEM_TYPE))
                .appendPostals(contentValuesListMap.get(StructuredPostal.CONTENT_ITEM_TYPE))
                .appendOrganizations(contentValuesListMap.get(Organization.CONTENT_ITEM_TYPE))
                .appendWebsites(contentValuesListMap.get(Website.CONTENT_ITEM_TYPE));
        if ((mVCardType & VCardConfig.FLAG_REFRAIN_IMAGE_EXPORT) == 0) {
            builder.appendPhotos(contentValuesListMap.get(Photo.CONTENT_ITEM_TYPE));
        }
        builder.appendNotes(contentValuesListMap.get(Note.CONTENT_ITEM_TYPE))
                .appendEvents(contentValuesListMap.get(Event.CONTENT_ITEM_TYPE))
                .appendIms(contentValuesListMap.get(Im.CONTENT_ITEM_TYPE))
                .appendRelation(contentValuesListMap.get(Relation.CONTENT_ITEM_TYPE));
        return builder.toString();
    }
    public void terminate() {
        for (OneEntryHandler handler : mHandlerList) {
            handler.onTerminate();
        }
        if (mCursor != null) {
            try {
                mCursor.close();
            } catch (SQLiteException e) {
                Log.e(LOG_TAG, "SQLiteException on Cursor#close(): " + e.getMessage());
            }
            mCursor = null;
        }
        mTerminateIsCalled = true;
    }
    @Override
    public void finalize() {
        if (!mTerminateIsCalled) {
            terminate();
        }
    }
    public int getCount() {
        if (mCursor == null) {
            return 0;
        }
        return mCursor.getCount();
    }
    public boolean isAfterLast() {
        if (mCursor == null) {
            return false;
        }
        return mCursor.isAfterLast();
    }
    public String getErrorReason() {
        return mErrorReason;
    }
}
