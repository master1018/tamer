public class ContactsProvider2 extends SQLiteContentProvider implements OnAccountsUpdateListener {
    private static final String TAG = "ContactsProvider";
    private static final boolean VERBOSE_LOGGING = Log.isLoggable(TAG, Log.VERBOSE);
    private static final int DEFAULT_MAX_SUGGESTIONS = 5;
    private static final String GOOGLE_MY_CONTACTS_GROUP_TITLE = "System Group: My Contacts";
    private static final String PROPERTY_CONTACTS_IMPORTED = "contacts_imported_v1";
    private static final int PROPERTY_CONTACTS_IMPORT_VERSION = 1;
    private static final String PREF_LOCALE = "locale";
    private static final String AGGREGATE_CONTACTS = "sync.contacts.aggregate";
    private static final UriMatcher sUriMatcher = new UriMatcher(UriMatcher.NO_MATCH);
    private static final String TIMES_CONTACED_SORT_COLUMN = "times_contacted_sort";
    private static final String STREQUENT_ORDER_BY = Contacts.STARRED + " DESC, "
            + TIMES_CONTACED_SORT_COLUMN + " DESC, "
            + Contacts.DISPLAY_NAME + " COLLATE LOCALIZED ASC";
    private static final String STREQUENT_LIMIT =
            "(SELECT COUNT(1) FROM " + Tables.CONTACTS + " WHERE "
            + Contacts.STARRED + "=1) + 25";
     static final String UPDATE_TIMES_CONTACTED_CONTACTS_TABLE =
            "UPDATE " + Tables.CONTACTS + " SET " + Contacts.TIMES_CONTACTED + "=" +
            " CASE WHEN " + Contacts.TIMES_CONTACTED + " IS NULL THEN 1 ELSE " +
            " (" + Contacts.TIMES_CONTACTED + " + 1) END WHERE " + Contacts._ID + "=?";
     static final String UPDATE_TIMES_CONTACTED_RAWCONTACTS_TABLE =
            "UPDATE " + Tables.RAW_CONTACTS + " SET " + RawContacts.TIMES_CONTACTED + "=" +
            " CASE WHEN " + RawContacts.TIMES_CONTACTED + " IS NULL THEN 1 ELSE " +
            " (" + RawContacts.TIMES_CONTACTED + " + 1) END WHERE " + RawContacts.CONTACT_ID + "=?";
     static final String PHONEBOOK_COLLATOR_NAME = "PHONEBOOK";
    private static final int CONTACTS = 1000;
    private static final int CONTACTS_ID = 1001;
    private static final int CONTACTS_LOOKUP = 1002;
    private static final int CONTACTS_LOOKUP_ID = 1003;
    private static final int CONTACTS_DATA = 1004;
    private static final int CONTACTS_FILTER = 1005;
    private static final int CONTACTS_STREQUENT = 1006;
    private static final int CONTACTS_STREQUENT_FILTER = 1007;
    private static final int CONTACTS_GROUP = 1008;
    private static final int CONTACTS_PHOTO = 1009;
    private static final int CONTACTS_AS_VCARD = 1010;
    private static final int CONTACTS_AS_MULTI_VCARD = 1011;
    private static final int RAW_CONTACTS = 2002;
    private static final int RAW_CONTACTS_ID = 2003;
    private static final int RAW_CONTACTS_DATA = 2004;
    private static final int RAW_CONTACT_ENTITY_ID = 2005;
    private static final int DATA = 3000;
    private static final int DATA_ID = 3001;
    private static final int PHONES = 3002;
    private static final int PHONES_ID = 3003;
    private static final int PHONES_FILTER = 3004;
    private static final int EMAILS = 3005;
    private static final int EMAILS_ID = 3006;
    private static final int EMAILS_LOOKUP = 3007;
    private static final int EMAILS_FILTER = 3008;
    private static final int POSTALS = 3009;
    private static final int POSTALS_ID = 3010;
    private static final int PHONE_LOOKUP = 4000;
    private static final int AGGREGATION_EXCEPTIONS = 6000;
    private static final int AGGREGATION_EXCEPTION_ID = 6001;
    private static final int STATUS_UPDATES = 7000;
    private static final int STATUS_UPDATES_ID = 7001;
    private static final int AGGREGATION_SUGGESTIONS = 8000;
    private static final int SETTINGS = 9000;
    private static final int GROUPS = 10000;
    private static final int GROUPS_ID = 10001;
    private static final int GROUPS_SUMMARY = 10003;
    private static final int SYNCSTATE = 11000;
    private static final int SYNCSTATE_ID = 11001;
    private static final int SEARCH_SUGGESTIONS = 12001;
    private static final int SEARCH_SHORTCUT = 12002;
    private static final int LIVE_FOLDERS_CONTACTS = 14000;
    private static final int LIVE_FOLDERS_CONTACTS_WITH_PHONES = 14001;
    private static final int LIVE_FOLDERS_CONTACTS_FAVORITES = 14002;
    private static final int LIVE_FOLDERS_CONTACTS_GROUP_NAME = 14003;
    private static final int RAW_CONTACT_ENTITIES = 15001;
    private static final int PROVIDER_STATUS = 16001;
    private interface DataContactsQuery {
        public static final String TABLE = "data "
                + "JOIN raw_contacts ON (data.raw_contact_id = raw_contacts._id) "
                + "JOIN contacts ON (raw_contacts.contact_id = contacts._id)";
        public static final String[] PROJECTION = new String[] {
            RawContactsColumns.CONCRETE_ID,
            DataColumns.CONCRETE_ID,
            ContactsColumns.CONCRETE_ID
        };
        public static final int RAW_CONTACT_ID = 0;
        public static final int DATA_ID = 1;
        public static final int CONTACT_ID = 2;
    }
    private interface DataDeleteQuery {
        public static final String TABLE = Tables.DATA_JOIN_MIMETYPES;
        public static final String[] CONCRETE_COLUMNS = new String[] {
            DataColumns.CONCRETE_ID,
            MimetypesColumns.MIMETYPE,
            Data.RAW_CONTACT_ID,
            Data.IS_PRIMARY,
            Data.DATA1,
        };
        public static final String[] COLUMNS = new String[] {
            Data._ID,
            MimetypesColumns.MIMETYPE,
            Data.RAW_CONTACT_ID,
            Data.IS_PRIMARY,
            Data.DATA1,
        };
        public static final int _ID = 0;
        public static final int MIMETYPE = 1;
        public static final int RAW_CONTACT_ID = 2;
        public static final int IS_PRIMARY = 3;
        public static final int DATA1 = 4;
    }
    private interface DataUpdateQuery {
        String[] COLUMNS = { Data._ID, Data.RAW_CONTACT_ID, Data.MIMETYPE };
        int _ID = 0;
        int RAW_CONTACT_ID = 1;
        int MIMETYPE = 2;
    }
    private interface RawContactsQuery {
        String TABLE = Tables.RAW_CONTACTS;
        String[] COLUMNS = new String[] {
                RawContacts.DELETED,
                RawContacts.ACCOUNT_TYPE,
                RawContacts.ACCOUNT_NAME,
        };
        int DELETED = 0;
        int ACCOUNT_TYPE = 1;
        int ACCOUNT_NAME = 2;
    }
    public static final String DEFAULT_ACCOUNT_TYPE = "com.google";
    public static final String FEATURE_LEGACY_HOSTED_OR_GOOGLE = "legacy_hosted_or_google";
    private static final String CONTACTS_IN_GROUP_SELECT =
            Contacts._ID + " IN "
                    + "(SELECT " + RawContacts.CONTACT_ID
                    + " FROM " + Tables.RAW_CONTACTS
                    + " WHERE " + RawContactsColumns.CONCRETE_ID + " IN "
                            + "(SELECT " + DataColumns.CONCRETE_RAW_CONTACT_ID
                            + " FROM " + Tables.DATA_JOIN_MIMETYPES
                            + " WHERE " + Data.MIMETYPE + "='" + GroupMembership.CONTENT_ITEM_TYPE
                                    + "' AND " + GroupMembership.GROUP_ROW_ID + "="
                                    + "(SELECT " + Tables.GROUPS + "." + Groups._ID
                                    + " FROM " + Tables.GROUPS
                                    + " WHERE " + Groups.TITLE + "=?)))";
    private static final String UPDATE_RAW_CONTACT_SET_DIRTY_SQL =
            "UPDATE " + Tables.RAW_CONTACTS +
            " SET " + RawContacts.DIRTY + "=1" +
            " WHERE " + RawContacts._ID + " IN (";
    private static final String UPDATE_RAW_CONTACT_SET_VERSION_SQL =
            "UPDATE " + Tables.RAW_CONTACTS +
            " SET " + RawContacts.VERSION + " = " + RawContacts.VERSION + " + 1" +
            " WHERE " + RawContacts._ID + " IN (";
    private static final String CONTACT_LOOKUP_NAME_TYPES =
            NameLookupType.NAME_COLLATION_KEY + "," +
            NameLookupType.EMAIL_BASED_NICKNAME + "," +
            NameLookupType.NICKNAME + "," +
            NameLookupType.NAME_SHORTHAND + "," +
            NameLookupType.ORGANIZATION + "," +
            NameLookupType.NAME_CONSONANTS;
    private static final HashMap<String, String> sCountProjectionMap;
    private static final HashMap<String, String> sContactsProjectionMap;
    private static final HashMap<String, String> sContactsProjectionWithSnippetMap;
    private static final HashMap<String, String> sStrequentStarredProjectionMap;
    private static final HashMap<String, String> sStrequentFrequentProjectionMap;
    private static final HashMap<String, String> sContactsVCardProjectionMap;
    private static final HashMap<String, String> sRawContactsProjectionMap;
    private static final HashMap<String, String> sRawContactsEntityProjectionMap;
    private static final HashMap<String, String> sDataProjectionMap;
    private static final HashMap<String, String> sDistinctDataProjectionMap;
    private static final HashMap<String, String> sPhoneLookupProjectionMap;
    private static final HashMap<String, String> sGroupsProjectionMap;
    private static final HashMap<String, String> sGroupsSummaryProjectionMap;
    private static final HashMap<String, String> sAggregationExceptionsProjectionMap;
    private static final HashMap<String, String> sSettingsProjectionMap;
    private static final HashMap<String, String> sStatusUpdatesProjectionMap;
    private static final HashMap<String, String> sLiveFoldersProjectionMap;
    private static final String WHERE_CLAUSE_FOR_STATUS_UPDATES_TABLE =
            StatusUpdatesColumns.DATA_ID + " IN (SELECT Distinct " + StatusUpdates.DATA_ID +
            " FROM " + Tables.STATUS_UPDATES + " LEFT OUTER JOIN " + Tables.PRESENCE +
            " ON " + StatusUpdatesColumns.DATA_ID + " = " + StatusUpdates.DATA_ID + " WHERE ";
    private static final String[] EMPTY_STRING_ARRAY = new String[0];
    private static final int LEGACY_IMPORT_FAILED_NOTIFICATION = 1;
    private SQLiteStatement mSetPrimaryStatement;
    private SQLiteStatement mSetSuperPrimaryStatement;
    private SQLiteStatement mRawContactDisplayNameUpdate;
    private SQLiteStatement mLastStatusUpdate;
    private SQLiteStatement mNameLookupInsert;
    private SQLiteStatement mNameLookupDelete;
    private SQLiteStatement mStatusUpdateAutoTimestamp;
    private SQLiteStatement mStatusUpdateInsert;
    private SQLiteStatement mStatusUpdateReplace;
    private SQLiteStatement mStatusAttributionUpdate;
    private SQLiteStatement mStatusUpdateDelete;
    private SQLiteStatement mResetNameVerifiedForOtherRawContacts;
    private long mMimeTypeIdEmail;
    private long mMimeTypeIdIm;
    private long mMimeTypeIdStructuredName;
    private long mMimeTypeIdOrganization;
    private long mMimeTypeIdNickname;
    private long mMimeTypeIdPhone;
    private StringBuilder mSb = new StringBuilder();
    private String[] mSelectionArgs1 = new String[1];
    private String[] mSelectionArgs2 = new String[2];
    private ArrayList<String> mSelectionArgs = Lists.newArrayList();
    private Account mAccount;
    static {
        final UriMatcher matcher = sUriMatcher;
        matcher.addURI(ContactsContract.AUTHORITY, "contacts", CONTACTS);
        matcher.addURI(ContactsContract.AUTHORITY, "contacts/#", CONTACTS_ID);
        matcher.addURI(ContactsContract.AUTHORITY, "contacts/#/data", CONTACTS_DATA);
        matcher.addURI(ContactsContract.AUTHORITY, "contacts/#/suggestions",
                AGGREGATION_SUGGESTIONS);
        matcher.addURI(ContactsContract.AUTHORITY, "contacts/#/suggestions#", CONTACTS_LOOKUP_ID);
        matcher.addURI(ContactsContract.AUTHORITY, "contacts/as_vcard
    private abstract class DataRowHandler {
        protected final String mMimetype;
        protected long mMimetypeId;
        @SuppressWarnings("all")
        public DataRowHandler(String mimetype) {
            mMimetype = mimetype;
            if (StructuredName.DISPLAY_NAME != Data.DATA1 || Nickname.NAME != Data.DATA1
                    || Organization.COMPANY != Data.DATA1 || Phone.NUMBER != Data.DATA1
                    || Email.DATA != Data.DATA1) {
                throw new AssertionError("Some of ContactsContract.CommonDataKinds class primary"
                        + " data is not in DATA1 column");
            }
        }
        protected long getMimeTypeId() {
            if (mMimetypeId == 0) {
                mMimetypeId = mDbHelper.getMimeTypeId(mMimetype);
            }
            return mMimetypeId;
        }
        public long insert(SQLiteDatabase db, long rawContactId, ContentValues values) {
            final long dataId = db.insert(Tables.DATA, null, values);
            Integer primary = values.getAsInteger(Data.IS_PRIMARY);
            if (primary != null && primary != 0) {
                setIsPrimary(rawContactId, dataId, getMimeTypeId());
            }
            return dataId;
        }
        public boolean update(SQLiteDatabase db, ContentValues values, Cursor c,
                boolean callerIsSyncAdapter) {
            long dataId = c.getLong(DataUpdateQuery._ID);
            long rawContactId = c.getLong(DataUpdateQuery.RAW_CONTACT_ID);
            if (values.containsKey(Data.IS_SUPER_PRIMARY)) {
                long mimeTypeId = getMimeTypeId();
                setIsSuperPrimary(rawContactId, dataId, mimeTypeId);
                setIsPrimary(rawContactId, dataId, mimeTypeId);
                values.remove(Data.IS_SUPER_PRIMARY);
                values.remove(Data.IS_PRIMARY);
            } else if (values.containsKey(Data.IS_PRIMARY)) {
                setIsPrimary(rawContactId, dataId, getMimeTypeId());
                values.remove(Data.IS_PRIMARY);
            }
            if (values.size() > 0) {
                mSelectionArgs1[0] = String.valueOf(dataId);
                mDb.update(Tables.DATA, values, Data._ID + " =?", mSelectionArgs1);
            }
            if (!callerIsSyncAdapter) {
                setRawContactDirty(rawContactId);
            }
            return true;
        }
        public int delete(SQLiteDatabase db, Cursor c) {
            long dataId = c.getLong(DataDeleteQuery._ID);
            long rawContactId = c.getLong(DataDeleteQuery.RAW_CONTACT_ID);
            boolean primary = c.getInt(DataDeleteQuery.IS_PRIMARY) != 0;
            mSelectionArgs1[0] = String.valueOf(dataId);
            int count = db.delete(Tables.DATA, Data._ID + "=?", mSelectionArgs1);
            mSelectionArgs1[0] = String.valueOf(rawContactId);
            db.delete(Tables.PRESENCE, PresenceColumns.RAW_CONTACT_ID + "=?", mSelectionArgs1);
            if (count != 0 && primary) {
                fixPrimary(db, rawContactId);
            }
            return count;
        }
        private void fixPrimary(SQLiteDatabase db, long rawContactId) {
            long mimeTypeId = getMimeTypeId();
            long primaryId = -1;
            int primaryType = -1;
            mSelectionArgs1[0] = String.valueOf(rawContactId);
            Cursor c = db.query(DataDeleteQuery.TABLE,
                    DataDeleteQuery.CONCRETE_COLUMNS,
                    Data.RAW_CONTACT_ID + "=?" +
                        " AND " + DataColumns.MIMETYPE_ID + "=" + mimeTypeId,
                    mSelectionArgs1, null, null, null);
            try {
                while (c.moveToNext()) {
                    long dataId = c.getLong(DataDeleteQuery._ID);
                    int type = c.getInt(DataDeleteQuery.DATA1);
                    if (primaryType == -1 || getTypeRank(type) < getTypeRank(primaryType)) {
                        primaryId = dataId;
                        primaryType = type;
                    }
                }
            } finally {
                c.close();
            }
            if (primaryId != -1) {
                setIsPrimary(rawContactId, primaryId, mimeTypeId);
            }
        }
        protected int getTypeRank(int type) {
            return 0;
        }
        protected void fixRawContactDisplayName(SQLiteDatabase db, long rawContactId) {
            if (!isNewRawContact(rawContactId)) {
                updateRawContactDisplayName(db, rawContactId);
                mContactAggregator.updateDisplayNameForRawContact(db, rawContactId);
            }
        }
        public ContentValues getAugmentedValues(SQLiteDatabase db, long dataId,
                ContentValues update) {
            boolean changing = false;
            final ContentValues values = new ContentValues();
            mSelectionArgs1[0] = String.valueOf(dataId);
            final Cursor cursor = db.query(Tables.DATA, null, Data._ID + "=?",
                    mSelectionArgs1, null, null, null);
            try {
                if (cursor.moveToFirst()) {
                    for (int i = 0; i < cursor.getColumnCount(); i++) {
                        final String key = cursor.getColumnName(i);
                        final String value = cursor.getString(i);
                        if (!changing && update.containsKey(key)) {
                            Object newValue = update.get(key);
                            String newString = newValue == null ? null : newValue.toString();
                            changing |= !TextUtils.equals(newString, value);
                        }
                        values.put(key, value);
                    }
                }
            } finally {
                cursor.close();
            }
            if (!changing) {
                return null;
            }
            values.putAll(update);
            return values;
        }
    }
    public class CustomDataRowHandler extends DataRowHandler {
        public CustomDataRowHandler(String mimetype) {
            super(mimetype);
        }
    }
    public class StructuredNameRowHandler extends DataRowHandler {
        private final NameSplitter mSplitter;
        public StructuredNameRowHandler(NameSplitter splitter) {
            super(StructuredName.CONTENT_ITEM_TYPE);
            mSplitter = splitter;
        }
        @Override
        public long insert(SQLiteDatabase db, long rawContactId, ContentValues values) {
            fixStructuredNameComponents(values, values);
            long dataId = super.insert(db, rawContactId, values);
            String name = values.getAsString(StructuredName.DISPLAY_NAME);
            Integer fullNameStyle = values.getAsInteger(StructuredName.FULL_NAME_STYLE);
            insertNameLookupForStructuredName(rawContactId, dataId, name,
                    fullNameStyle != null
                            ? mNameSplitter.getAdjustedFullNameStyle(fullNameStyle)
                            : FullNameStyle.UNDEFINED);
            insertNameLookupForPhoneticName(rawContactId, dataId, values);
            fixRawContactDisplayName(db, rawContactId);
            triggerAggregation(rawContactId);
            return dataId;
        }
        @Override
        public boolean update(SQLiteDatabase db, ContentValues values, Cursor c,
                boolean callerIsSyncAdapter) {
            final long dataId = c.getLong(DataUpdateQuery._ID);
            final long rawContactId = c.getLong(DataUpdateQuery.RAW_CONTACT_ID);
            final ContentValues augmented = getAugmentedValues(db, dataId, values);
            if (augmented == null) {  
                return false;
            }
            fixStructuredNameComponents(augmented, values);
            super.update(db, values, c, callerIsSyncAdapter);
            if (values.containsKey(StructuredName.DISPLAY_NAME) ||
                    values.containsKey(StructuredName.PHONETIC_FAMILY_NAME) ||
                    values.containsKey(StructuredName.PHONETIC_MIDDLE_NAME) ||
                    values.containsKey(StructuredName.PHONETIC_GIVEN_NAME)) {
                augmented.putAll(values);
                String name = augmented.getAsString(StructuredName.DISPLAY_NAME);
                deleteNameLookup(dataId);
                Integer fullNameStyle = augmented.getAsInteger(StructuredName.FULL_NAME_STYLE);
                insertNameLookupForStructuredName(rawContactId, dataId, name,
                        fullNameStyle != null
                                ? mNameSplitter.getAdjustedFullNameStyle(fullNameStyle)
                                : FullNameStyle.UNDEFINED);
                insertNameLookupForPhoneticName(rawContactId, dataId, augmented);
            }
            fixRawContactDisplayName(db, rawContactId);
            triggerAggregation(rawContactId);
            return true;
        }
        @Override
        public int delete(SQLiteDatabase db, Cursor c) {
            long dataId = c.getLong(DataDeleteQuery._ID);
            long rawContactId = c.getLong(DataDeleteQuery.RAW_CONTACT_ID);
            int count = super.delete(db, c);
            deleteNameLookup(dataId);
            fixRawContactDisplayName(db, rawContactId);
            triggerAggregation(rawContactId);
            return count;
        }
        private final String[] STRUCTURED_FIELDS = new String[] {
                StructuredName.PREFIX, StructuredName.GIVEN_NAME, StructuredName.MIDDLE_NAME,
                StructuredName.FAMILY_NAME, StructuredName.SUFFIX
        };
        private void fixStructuredNameComponents(ContentValues augmented, ContentValues update) {
            final String unstruct = update.getAsString(StructuredName.DISPLAY_NAME);
            final boolean touchedUnstruct = !TextUtils.isEmpty(unstruct);
            final boolean touchedStruct = !areAllEmpty(update, STRUCTURED_FIELDS);
            if (touchedUnstruct && !touchedStruct) {
                NameSplitter.Name name = new NameSplitter.Name();
                mSplitter.split(name, unstruct);
                name.toValues(update);
            } else if (!touchedUnstruct
                    && (touchedStruct || areAnySpecified(update, STRUCTURED_FIELDS))) {
                NameSplitter.Name name = new NameSplitter.Name();
                name.fromValues(augmented);
                name.fullNameStyle = FullNameStyle.UNDEFINED;
                mSplitter.guessNameStyle(name);
                int unadjustedFullNameStyle = name.fullNameStyle;
                name.fullNameStyle = mSplitter.getAdjustedFullNameStyle(name.fullNameStyle);
                final String joined = mSplitter.join(name, true);
                update.put(StructuredName.DISPLAY_NAME, joined);
                update.put(StructuredName.FULL_NAME_STYLE, unadjustedFullNameStyle);
                update.put(StructuredName.PHONETIC_NAME_STYLE, name.phoneticNameStyle);
            } else if (touchedUnstruct && touchedStruct){
                if (!update.containsKey(StructuredName.FULL_NAME_STYLE)) {
                    update.put(StructuredName.FULL_NAME_STYLE,
                            mSplitter.guessFullNameStyle(unstruct));
                }
                if (!update.containsKey(StructuredName.PHONETIC_NAME_STYLE)) {
                    update.put(StructuredName.PHONETIC_NAME_STYLE,
                            mSplitter.guessPhoneticNameStyle(unstruct));
                }
            }
        }
    }
    public class StructuredPostalRowHandler extends DataRowHandler {
        private PostalSplitter mSplitter;
        public StructuredPostalRowHandler(PostalSplitter splitter) {
            super(StructuredPostal.CONTENT_ITEM_TYPE);
            mSplitter = splitter;
        }
        @Override
        public long insert(SQLiteDatabase db, long rawContactId, ContentValues values) {
            fixStructuredPostalComponents(values, values);
            return super.insert(db, rawContactId, values);
        }
        @Override
        public boolean update(SQLiteDatabase db, ContentValues values, Cursor c,
                boolean callerIsSyncAdapter) {
            final long dataId = c.getLong(DataUpdateQuery._ID);
            final ContentValues augmented = getAugmentedValues(db, dataId, values);
            if (augmented == null) {    
                return false;
            }
            fixStructuredPostalComponents(augmented, values);
            super.update(db, values, c, callerIsSyncAdapter);
            return true;
        }
        private final String[] STRUCTURED_FIELDS = new String[] {
                StructuredPostal.STREET, StructuredPostal.POBOX, StructuredPostal.NEIGHBORHOOD,
                StructuredPostal.CITY, StructuredPostal.REGION, StructuredPostal.POSTCODE,
                StructuredPostal.COUNTRY,
        };
        private void fixStructuredPostalComponents(ContentValues augmented, ContentValues update) {
            final String unstruct = update.getAsString(StructuredPostal.FORMATTED_ADDRESS);
            final boolean touchedUnstruct = !TextUtils.isEmpty(unstruct);
            final boolean touchedStruct = !areAllEmpty(update, STRUCTURED_FIELDS);
            final PostalSplitter.Postal postal = new PostalSplitter.Postal();
            if (touchedUnstruct && !touchedStruct) {
                mSplitter.split(postal, unstruct);
                postal.toValues(update);
            } else if (!touchedUnstruct
                    && (touchedStruct || areAnySpecified(update, STRUCTURED_FIELDS))) {
                postal.fromValues(augmented);
                final String joined = mSplitter.join(postal);
                update.put(StructuredPostal.FORMATTED_ADDRESS, joined);
            }
        }
    }
    public class CommonDataRowHandler extends DataRowHandler {
        private final String mTypeColumn;
        private final String mLabelColumn;
        public CommonDataRowHandler(String mimetype, String typeColumn, String labelColumn) {
            super(mimetype);
            mTypeColumn = typeColumn;
            mLabelColumn = labelColumn;
        }
        @Override
        public long insert(SQLiteDatabase db, long rawContactId, ContentValues values) {
            enforceTypeAndLabel(values, values);
            return super.insert(db, rawContactId, values);
        }
        @Override
        public boolean update(SQLiteDatabase db, ContentValues values, Cursor c,
                boolean callerIsSyncAdapter) {
            final long dataId = c.getLong(DataUpdateQuery._ID);
            final ContentValues augmented = getAugmentedValues(db, dataId, values);
            if (augmented == null) {        
                return false;
            }
            enforceTypeAndLabel(augmented, values);
            return super.update(db, values, c, callerIsSyncAdapter);
        }
        private void enforceTypeAndLabel(ContentValues augmented, ContentValues update) {
            final boolean hasType = !TextUtils.isEmpty(augmented.getAsString(mTypeColumn));
            final boolean hasLabel = !TextUtils.isEmpty(augmented.getAsString(mLabelColumn));
            if (hasLabel && !hasType) {
                throw new IllegalArgumentException(mTypeColumn + " must be specified when "
                        + mLabelColumn + " is defined.");
            }
        }
    }
    public class OrganizationDataRowHandler extends CommonDataRowHandler {
        public OrganizationDataRowHandler() {
            super(Organization.CONTENT_ITEM_TYPE, Organization.TYPE, Organization.LABEL);
        }
        @Override
        public long insert(SQLiteDatabase db, long rawContactId, ContentValues values) {
            String company = values.getAsString(Organization.COMPANY);
            String title = values.getAsString(Organization.TITLE);
            long dataId = super.insert(db, rawContactId, values);
            fixRawContactDisplayName(db, rawContactId);
            insertNameLookupForOrganization(rawContactId, dataId, company, title);
            return dataId;
        }
        @Override
        public boolean update(SQLiteDatabase db, ContentValues values, Cursor c,
                boolean callerIsSyncAdapter) {
            if (!super.update(db, values, c, callerIsSyncAdapter)) {
                return false;
            }
            boolean containsCompany = values.containsKey(Organization.COMPANY);
            boolean containsTitle = values.containsKey(Organization.TITLE);
            if (containsCompany || containsTitle) {
                long dataId = c.getLong(DataUpdateQuery._ID);
                long rawContactId = c.getLong(DataUpdateQuery.RAW_CONTACT_ID);
                String company;
                if (containsCompany) {
                    company = values.getAsString(Organization.COMPANY);
                } else {
                    mSelectionArgs1[0] = String.valueOf(dataId);
                    company = DatabaseUtils.stringForQuery(db,
                            "SELECT " + Organization.COMPANY +
                            " FROM " + Tables.DATA +
                            " WHERE " + Data._ID + "=?", mSelectionArgs1);
                }
                String title;
                if (containsTitle) {
                    title = values.getAsString(Organization.TITLE);
                } else {
                    mSelectionArgs1[0] = String.valueOf(dataId);
                    title = DatabaseUtils.stringForQuery(db,
                            "SELECT " + Organization.TITLE +
                            " FROM " + Tables.DATA +
                            " WHERE " + Data._ID + "=?", mSelectionArgs1);
                }
                deleteNameLookup(dataId);
                insertNameLookupForOrganization(rawContactId, dataId, company, title);
                fixRawContactDisplayName(db, rawContactId);
            }
            return true;
        }
        @Override
        public int delete(SQLiteDatabase db, Cursor c) {
            long dataId = c.getLong(DataUpdateQuery._ID);
            long rawContactId = c.getLong(DataDeleteQuery.RAW_CONTACT_ID);
            int count = super.delete(db, c);
            fixRawContactDisplayName(db, rawContactId);
            deleteNameLookup(dataId);
            return count;
        }
        @Override
        protected int getTypeRank(int type) {
            switch (type) {
                case Organization.TYPE_WORK: return 0;
                case Organization.TYPE_CUSTOM: return 1;
                case Organization.TYPE_OTHER: return 2;
                default: return 1000;
            }
        }
    }
    public class EmailDataRowHandler extends CommonDataRowHandler {
        public EmailDataRowHandler() {
            super(Email.CONTENT_ITEM_TYPE, Email.TYPE, Email.LABEL);
        }
        @Override
        public long insert(SQLiteDatabase db, long rawContactId, ContentValues values) {
            String email = values.getAsString(Email.DATA);
            long dataId = super.insert(db, rawContactId, values);
            fixRawContactDisplayName(db, rawContactId);
            String address = insertNameLookupForEmail(rawContactId, dataId, email);
            if (address != null) {
                triggerAggregation(rawContactId);
            }
            return dataId;
        }
        @Override
        public boolean update(SQLiteDatabase db, ContentValues values, Cursor c,
                boolean callerIsSyncAdapter) {
            if (!super.update(db, values, c, callerIsSyncAdapter)) {
                return false;
            }
            if (values.containsKey(Email.DATA)) {
                long dataId = c.getLong(DataUpdateQuery._ID);
                long rawContactId = c.getLong(DataUpdateQuery.RAW_CONTACT_ID);
                String address = values.getAsString(Email.DATA);
                deleteNameLookup(dataId);
                insertNameLookupForEmail(rawContactId, dataId, address);
                fixRawContactDisplayName(db, rawContactId);
                triggerAggregation(rawContactId);
            }
            return true;
        }
        @Override
        public int delete(SQLiteDatabase db, Cursor c) {
            long dataId = c.getLong(DataDeleteQuery._ID);
            long rawContactId = c.getLong(DataDeleteQuery.RAW_CONTACT_ID);
            int count = super.delete(db, c);
            deleteNameLookup(dataId);
            fixRawContactDisplayName(db, rawContactId);
            triggerAggregation(rawContactId);
            return count;
        }
        @Override
        protected int getTypeRank(int type) {
            switch (type) {
                case Email.TYPE_HOME: return 0;
                case Email.TYPE_WORK: return 1;
                case Email.TYPE_CUSTOM: return 2;
                case Email.TYPE_OTHER: return 3;
                default: return 1000;
            }
        }
    }
    public class NicknameDataRowHandler extends CommonDataRowHandler {
        public NicknameDataRowHandler() {
            super(Nickname.CONTENT_ITEM_TYPE, Nickname.TYPE, Nickname.LABEL);
        }
        @Override
        public long insert(SQLiteDatabase db, long rawContactId, ContentValues values) {
            String nickname = values.getAsString(Nickname.NAME);
            long dataId = super.insert(db, rawContactId, values);
            if (!TextUtils.isEmpty(nickname)) {
                fixRawContactDisplayName(db, rawContactId);
                insertNameLookupForNickname(rawContactId, dataId, nickname);
                triggerAggregation(rawContactId);
            }
            return dataId;
        }
        @Override
        public boolean update(SQLiteDatabase db, ContentValues values, Cursor c,
                boolean callerIsSyncAdapter) {
            long dataId = c.getLong(DataUpdateQuery._ID);
            long rawContactId = c.getLong(DataUpdateQuery.RAW_CONTACT_ID);
            if (!super.update(db, values, c, callerIsSyncAdapter)) {
                return false;
            }
            if (values.containsKey(Nickname.NAME)) {
                String nickname = values.getAsString(Nickname.NAME);
                deleteNameLookup(dataId);
                insertNameLookupForNickname(rawContactId, dataId, nickname);
                fixRawContactDisplayName(db, rawContactId);
                triggerAggregation(rawContactId);
            }
            return true;
        }
        @Override
        public int delete(SQLiteDatabase db, Cursor c) {
            long dataId = c.getLong(DataDeleteQuery._ID);
            long rawContactId = c.getLong(DataDeleteQuery.RAW_CONTACT_ID);
            int count = super.delete(db, c);
            deleteNameLookup(dataId);
            fixRawContactDisplayName(db, rawContactId);
            triggerAggregation(rawContactId);
            return count;
        }
    }
    public class PhoneDataRowHandler extends CommonDataRowHandler {
        public PhoneDataRowHandler() {
            super(Phone.CONTENT_ITEM_TYPE, Phone.TYPE, Phone.LABEL);
        }
        @Override
        public long insert(SQLiteDatabase db, long rawContactId, ContentValues values) {
            long dataId;
            if (values.containsKey(Phone.NUMBER)) {
                String number = values.getAsString(Phone.NUMBER);
                String normalizedNumber = computeNormalizedNumber(number);
                values.put(PhoneColumns.NORMALIZED_NUMBER, normalizedNumber);
                dataId = super.insert(db, rawContactId, values);
                updatePhoneLookup(db, rawContactId, dataId, number, normalizedNumber);
                mContactAggregator.updateHasPhoneNumber(db, rawContactId);
                fixRawContactDisplayName(db, rawContactId);
                if (normalizedNumber != null) {
                    triggerAggregation(rawContactId);
                }
            } else {
                dataId = super.insert(db, rawContactId, values);
            }
            return dataId;
        }
        @Override
        public boolean update(SQLiteDatabase db, ContentValues values, Cursor c,
                boolean callerIsSyncAdapter) {
            String number = null;
            String normalizedNumber = null;
            if (values.containsKey(Phone.NUMBER)) {
                number = values.getAsString(Phone.NUMBER);
                normalizedNumber = computeNormalizedNumber(number);
                values.put(PhoneColumns.NORMALIZED_NUMBER, normalizedNumber);
            }
            if (!super.update(db, values, c, callerIsSyncAdapter)) {
                return false;
            }
            if (values.containsKey(Phone.NUMBER)) {
                long dataId = c.getLong(DataUpdateQuery._ID);
                long rawContactId = c.getLong(DataUpdateQuery.RAW_CONTACT_ID);
                updatePhoneLookup(db, rawContactId, dataId, number, normalizedNumber);
                mContactAggregator.updateHasPhoneNumber(db, rawContactId);
                fixRawContactDisplayName(db, rawContactId);
                triggerAggregation(rawContactId);
            }
            return true;
        }
        @Override
        public int delete(SQLiteDatabase db, Cursor c) {
            long dataId = c.getLong(DataDeleteQuery._ID);
            long rawContactId = c.getLong(DataDeleteQuery.RAW_CONTACT_ID);
            int count = super.delete(db, c);
            updatePhoneLookup(db, rawContactId, dataId, null, null);
            mContactAggregator.updateHasPhoneNumber(db, rawContactId);
            fixRawContactDisplayName(db, rawContactId);
            triggerAggregation(rawContactId);
            return count;
        }
        private String computeNormalizedNumber(String number) {
            String normalizedNumber = null;
            if (number != null) {
                normalizedNumber = PhoneNumberUtils.getStrippedReversed(number);
            }
            return normalizedNumber;
        }
        private void updatePhoneLookup(SQLiteDatabase db, long rawContactId, long dataId,
                String number, String normalizedNumber) {
            if (number != null) {
                ContentValues phoneValues = new ContentValues();
                phoneValues.put(PhoneLookupColumns.RAW_CONTACT_ID, rawContactId);
                phoneValues.put(PhoneLookupColumns.DATA_ID, dataId);
                phoneValues.put(PhoneLookupColumns.NORMALIZED_NUMBER, normalizedNumber);
                phoneValues.put(PhoneLookupColumns.MIN_MATCH,
                        PhoneNumberUtils.toCallerIDMinMatch(number));
                db.replace(Tables.PHONE_LOOKUP, null, phoneValues);
            } else {
                mSelectionArgs1[0] = String.valueOf(dataId);
                db.delete(Tables.PHONE_LOOKUP, PhoneLookupColumns.DATA_ID + "=?", mSelectionArgs1);
            }
        }
        @Override
        protected int getTypeRank(int type) {
            switch (type) {
                case Phone.TYPE_MOBILE: return 0;
                case Phone.TYPE_WORK: return 1;
                case Phone.TYPE_HOME: return 2;
                case Phone.TYPE_PAGER: return 3;
                case Phone.TYPE_CUSTOM: return 4;
                case Phone.TYPE_OTHER: return 5;
                case Phone.TYPE_FAX_WORK: return 6;
                case Phone.TYPE_FAX_HOME: return 7;
                default: return 1000;
            }
        }
    }
    public class GroupMembershipRowHandler extends DataRowHandler {
        public GroupMembershipRowHandler() {
            super(GroupMembership.CONTENT_ITEM_TYPE);
        }
        @Override
        public long insert(SQLiteDatabase db, long rawContactId, ContentValues values) {
            resolveGroupSourceIdInValues(rawContactId, db, values, true);
            long dataId = super.insert(db, rawContactId, values);
            updateVisibility(rawContactId);
            return dataId;
        }
        @Override
        public boolean update(SQLiteDatabase db, ContentValues values, Cursor c,
                boolean callerIsSyncAdapter) {
            long rawContactId = c.getLong(DataUpdateQuery.RAW_CONTACT_ID);
            resolveGroupSourceIdInValues(rawContactId, db, values, false);
            if (!super.update(db, values, c, callerIsSyncAdapter)) {
                return false;
            }
            updateVisibility(rawContactId);
            return true;
        }
        @Override
        public int delete(SQLiteDatabase db, Cursor c) {
            long rawContactId = c.getLong(DataDeleteQuery.RAW_CONTACT_ID);
            int count = super.delete(db, c);
            updateVisibility(rawContactId);
            return count;
        }
        private void updateVisibility(long rawContactId) {
            long contactId = mDbHelper.getContactId(rawContactId);
            if (contactId != 0) {
                mDbHelper.updateContactVisible(contactId);
            }
        }
        private void resolveGroupSourceIdInValues(long rawContactId, SQLiteDatabase db,
                ContentValues values, boolean isInsert) {
            boolean containsGroupSourceId = values.containsKey(GroupMembership.GROUP_SOURCE_ID);
            boolean containsGroupId = values.containsKey(GroupMembership.GROUP_ROW_ID);
            if (containsGroupSourceId && containsGroupId) {
                throw new IllegalArgumentException(
                        "you are not allowed to set both the GroupMembership.GROUP_SOURCE_ID "
                                + "and GroupMembership.GROUP_ROW_ID");
            }
            if (!containsGroupSourceId && !containsGroupId) {
                if (isInsert) {
                    throw new IllegalArgumentException(
                            "you must set exactly one of GroupMembership.GROUP_SOURCE_ID "
                                    + "and GroupMembership.GROUP_ROW_ID");
                } else {
                    return;
                }
            }
            if (containsGroupSourceId) {
                final String sourceId = values.getAsString(GroupMembership.GROUP_SOURCE_ID);
                final long groupId = getOrMakeGroup(db, rawContactId, sourceId,
                        mInsertedRawContacts.get(rawContactId));
                values.remove(GroupMembership.GROUP_SOURCE_ID);
                values.put(GroupMembership.GROUP_ROW_ID, groupId);
            }
        }
    }
    public class PhotoDataRowHandler extends DataRowHandler {
        public PhotoDataRowHandler() {
            super(Photo.CONTENT_ITEM_TYPE);
        }
        @Override
        public long insert(SQLiteDatabase db, long rawContactId, ContentValues values) {
            long dataId = super.insert(db, rawContactId, values);
            if (!isNewRawContact(rawContactId)) {
                mContactAggregator.updatePhotoId(db, rawContactId);
            }
            return dataId;
        }
        @Override
        public boolean update(SQLiteDatabase db, ContentValues values, Cursor c,
                boolean callerIsSyncAdapter) {
            long rawContactId = c.getLong(DataUpdateQuery.RAW_CONTACT_ID);
            if (!super.update(db, values, c, callerIsSyncAdapter)) {
                return false;
            }
            mContactAggregator.updatePhotoId(db, rawContactId);
            return true;
        }
        @Override
        public int delete(SQLiteDatabase db, Cursor c) {
            long rawContactId = c.getLong(DataDeleteQuery.RAW_CONTACT_ID);
            int count = super.delete(db, c);
            mContactAggregator.updatePhotoId(db, rawContactId);
            return count;
        }
    }
    public class GroupIdCacheEntry {
        String accountType;
        String accountName;
        String sourceId;
        long groupId;
    }
    private HashMap<String, DataRowHandler> mDataRowHandlers;
    private ContactsDatabaseHelper mDbHelper;
    private NameSplitter mNameSplitter;
    private NameLookupBuilder mNameLookupBuilder;
    private PostalSplitter mPostalSplitter;
    private HashMap<String, ArrayList<GroupIdCacheEntry>> mGroupIdCache = Maps.newHashMap();
    private ContactAggregator mContactAggregator;
    private LegacyApiSupport mLegacyApiSupport;
    private GlobalSearchSupport mGlobalSearchSupport;
    private CommonNicknameCache mCommonNicknameCache;
    private ContentValues mValues = new ContentValues();
    private CharArrayBuffer mCharArrayBuffer = new CharArrayBuffer(128);
    private NameSplitter.Name mName = new NameSplitter.Name();
    private HashMap<String, Boolean> mAccountWritability = Maps.newHashMap();
    private int mProviderStatus = ProviderStatus.STATUS_NORMAL;
    private long mEstimatedStorageRequirement = 0;
    private volatile CountDownLatch mAccessLatch;
    private HashMap<Long, Account> mInsertedRawContacts = Maps.newHashMap();
    private HashSet<Long> mUpdatedRawContacts = Sets.newHashSet();
    private HashSet<Long> mDirtyRawContacts = Sets.newHashSet();
    private HashMap<Long, Object> mUpdatedSyncStates = Maps.newHashMap();
    private boolean mVisibleTouched = false;
    private boolean mSyncToNetwork;
    private Locale mCurrentLocale;
    @Override
    public boolean onCreate() {
        super.onCreate();
        try {
            return initialize();
        } catch (RuntimeException e) {
            Log.e(TAG, "Cannot start provider", e);
            return false;
        }
    }
    private boolean initialize() {
        final Context context = getContext();
        mDbHelper = (ContactsDatabaseHelper)getDatabaseHelper();
        mGlobalSearchSupport = new GlobalSearchSupport(this);
        mLegacyApiSupport = new LegacyApiSupport(context, mDbHelper, this, mGlobalSearchSupport);
        mContactAggregator = new ContactAggregator(this, mDbHelper,
                createPhotoPriorityResolver(context));
        mContactAggregator.setEnabled(SystemProperties.getBoolean(AGGREGATE_CONTACTS, true));
        mDb = mDbHelper.getWritableDatabase();
        initForDefaultLocale();
        mSetPrimaryStatement = mDb.compileStatement(
                "UPDATE " + Tables.DATA +
                " SET " + Data.IS_PRIMARY + "=(_id=?)" +
                " WHERE " + DataColumns.MIMETYPE_ID + "=?" +
                "   AND " + Data.RAW_CONTACT_ID + "=?");
        mSetSuperPrimaryStatement = mDb.compileStatement(
                "UPDATE " + Tables.DATA +
                " SET " + Data.IS_SUPER_PRIMARY + "=(" + Data._ID + "=?)" +
                " WHERE " + DataColumns.MIMETYPE_ID + "=?" +
                "   AND " + Data.RAW_CONTACT_ID + " IN (" +
                        "SELECT " + RawContacts._ID +
                        " FROM " + Tables.RAW_CONTACTS +
                        " WHERE " + RawContacts.CONTACT_ID + " =(" +
                                "SELECT " + RawContacts.CONTACT_ID +
                                " FROM " + Tables.RAW_CONTACTS +
                                " WHERE " + RawContacts._ID + "=?))");
        mRawContactDisplayNameUpdate = mDb.compileStatement(
                "UPDATE " + Tables.RAW_CONTACTS +
                " SET " +
                        RawContacts.DISPLAY_NAME_SOURCE + "=?," +
                        RawContacts.DISPLAY_NAME_PRIMARY + "=?," +
                        RawContacts.DISPLAY_NAME_ALTERNATIVE + "=?," +
                        RawContacts.PHONETIC_NAME + "=?," +
                        RawContacts.PHONETIC_NAME_STYLE + "=?," +
                        RawContacts.SORT_KEY_PRIMARY + "=?," +
                        RawContacts.SORT_KEY_ALTERNATIVE + "=?" +
                " WHERE " + RawContacts._ID + "=?");
        mLastStatusUpdate = mDb.compileStatement(
                "UPDATE " + Tables.CONTACTS +
                " SET " + ContactsColumns.LAST_STATUS_UPDATE_ID + "=" +
                        "(SELECT " + DataColumns.CONCRETE_ID +
                        " FROM " + Tables.STATUS_UPDATES +
                        " JOIN " + Tables.DATA +
                        "   ON (" + StatusUpdatesColumns.DATA_ID + "="
                                + DataColumns.CONCRETE_ID + ")" +
                        " JOIN " + Tables.RAW_CONTACTS +
                        "   ON (" + DataColumns.CONCRETE_RAW_CONTACT_ID + "="
                                + RawContactsColumns.CONCRETE_ID + ")" +
                        " WHERE " + RawContacts.CONTACT_ID + "=?" +
                        " ORDER BY " + StatusUpdates.STATUS_TIMESTAMP + " DESC,"
                                + StatusUpdates.STATUS +
                        " LIMIT 1)" +
                " WHERE " + ContactsColumns.CONCRETE_ID + "=?");
        mNameLookupInsert = mDb.compileStatement("INSERT OR IGNORE INTO " + Tables.NAME_LOOKUP + "("
                + NameLookupColumns.RAW_CONTACT_ID + "," + NameLookupColumns.DATA_ID + ","
                + NameLookupColumns.NAME_TYPE + "," + NameLookupColumns.NORMALIZED_NAME
                + ") VALUES (?,?,?,?)");
        mNameLookupDelete = mDb.compileStatement("DELETE FROM " + Tables.NAME_LOOKUP + " WHERE "
                + NameLookupColumns.DATA_ID + "=?");
        mStatusUpdateInsert = mDb.compileStatement(
                "INSERT INTO " + Tables.STATUS_UPDATES + "("
                        + StatusUpdatesColumns.DATA_ID + ", "
                        + StatusUpdates.STATUS + ","
                        + StatusUpdates.STATUS_RES_PACKAGE + ","
                        + StatusUpdates.STATUS_ICON + ","
                        + StatusUpdates.STATUS_LABEL + ")" +
                " VALUES (?,?,?,?,?)");
        mStatusUpdateReplace = mDb.compileStatement(
                "INSERT OR REPLACE INTO " + Tables.STATUS_UPDATES + "("
                        + StatusUpdatesColumns.DATA_ID + ", "
                        + StatusUpdates.STATUS_TIMESTAMP + ","
                        + StatusUpdates.STATUS + ","
                        + StatusUpdates.STATUS_RES_PACKAGE + ","
                        + StatusUpdates.STATUS_ICON + ","
                        + StatusUpdates.STATUS_LABEL + ")" +
                " VALUES (?,?,?,?,?,?)");
        mStatusUpdateAutoTimestamp = mDb.compileStatement(
                "UPDATE " + Tables.STATUS_UPDATES +
                " SET " + StatusUpdates.STATUS_TIMESTAMP + "=?,"
                        + StatusUpdates.STATUS + "=?" +
                " WHERE " + StatusUpdatesColumns.DATA_ID + "=?"
                        + " AND " + StatusUpdates.STATUS + "!=?");
        mStatusAttributionUpdate = mDb.compileStatement(
                "UPDATE " + Tables.STATUS_UPDATES +
                " SET " + StatusUpdates.STATUS_RES_PACKAGE + "=?,"
                        + StatusUpdates.STATUS_ICON + "=?,"
                        + StatusUpdates.STATUS_LABEL + "=?" +
                " WHERE " + StatusUpdatesColumns.DATA_ID + "=?");
        mStatusUpdateDelete = mDb.compileStatement(
                "DELETE FROM " + Tables.STATUS_UPDATES +
                " WHERE " + StatusUpdatesColumns.DATA_ID + "=?");
        mResetNameVerifiedForOtherRawContacts = mDb.compileStatement(
                "UPDATE " + Tables.RAW_CONTACTS +
                " SET " + RawContacts.NAME_VERIFIED + "=0" +
                " WHERE " + RawContacts.CONTACT_ID + "=(" +
                        "SELECT " + RawContacts.CONTACT_ID +
                        " FROM " + Tables.RAW_CONTACTS +
                        " WHERE " + RawContacts._ID + "=?)" +
                " AND " + RawContacts._ID + "!=?");
        mMimeTypeIdEmail = mDbHelper.getMimeTypeId(Email.CONTENT_ITEM_TYPE);
        mMimeTypeIdIm = mDbHelper.getMimeTypeId(Im.CONTENT_ITEM_TYPE);
        mMimeTypeIdStructuredName = mDbHelper.getMimeTypeId(StructuredName.CONTENT_ITEM_TYPE);
        mMimeTypeIdOrganization = mDbHelper.getMimeTypeId(Organization.CONTENT_ITEM_TYPE);
        mMimeTypeIdNickname = mDbHelper.getMimeTypeId(Nickname.CONTENT_ITEM_TYPE);
        mMimeTypeIdPhone = mDbHelper.getMimeTypeId(Phone.CONTENT_ITEM_TYPE);
        verifyAccounts();
        if (isLegacyContactImportNeeded()) {
            importLegacyContactsAsync();
        } else {
            verifyLocale();
        }
        return (mDb != null);
    }
    private void initDataRowHandlers() {
      mDataRowHandlers = new HashMap<String, DataRowHandler>();
      mDataRowHandlers.put(Email.CONTENT_ITEM_TYPE, new EmailDataRowHandler());
      mDataRowHandlers.put(Im.CONTENT_ITEM_TYPE,
              new CommonDataRowHandler(Im.CONTENT_ITEM_TYPE, Im.TYPE, Im.LABEL));
      mDataRowHandlers.put(Nickname.CONTENT_ITEM_TYPE, new CommonDataRowHandler(
              StructuredPostal.CONTENT_ITEM_TYPE, StructuredPostal.TYPE, StructuredPostal.LABEL));
      mDataRowHandlers.put(Organization.CONTENT_ITEM_TYPE, new OrganizationDataRowHandler());
      mDataRowHandlers.put(Phone.CONTENT_ITEM_TYPE, new PhoneDataRowHandler());
      mDataRowHandlers.put(Nickname.CONTENT_ITEM_TYPE, new NicknameDataRowHandler());
      mDataRowHandlers.put(StructuredName.CONTENT_ITEM_TYPE,
              new StructuredNameRowHandler(mNameSplitter));
      mDataRowHandlers.put(StructuredPostal.CONTENT_ITEM_TYPE,
              new StructuredPostalRowHandler(mPostalSplitter));
      mDataRowHandlers.put(GroupMembership.CONTENT_ITEM_TYPE, new GroupMembershipRowHandler());
      mDataRowHandlers.put(Photo.CONTENT_ITEM_TYPE, new PhotoDataRowHandler());
    }
     PhotoPriorityResolver createPhotoPriorityResolver(Context context) {
        return new PhotoPriorityResolver(context);
    }
    private void initForDefaultLocale() {
        mCurrentLocale = getLocale();
        mNameSplitter = mDbHelper.createNameSplitter();
        mNameLookupBuilder = new StructuredNameLookupBuilder(mNameSplitter);
        mPostalSplitter = new PostalSplitter(mCurrentLocale);
        mCommonNicknameCache = new CommonNicknameCache(mDbHelper.getReadableDatabase());
        ContactLocaleUtils.getIntance().setLocale(mCurrentLocale);
        initDataRowHandlers();
    }
    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        if (mProviderStatus != ProviderStatus.STATUS_NORMAL) {
            return;
        }
        initForDefaultLocale();
        verifyLocale();
    }
    protected void verifyAccounts() {
        AccountManager.get(getContext()).addOnAccountsUpdatedListener(this, null, false);
        onAccountsUpdated(AccountManager.get(getContext()).getAccounts());
    }
    protected void verifyLocale() {
        if (mProviderStatus == ProviderStatus.STATUS_CHANGING_LOCALE) {
            return;
        }
        final SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getContext());
        final String providerLocale = prefs.getString(PREF_LOCALE, null);
        final Locale currentLocale = mCurrentLocale;
        if (currentLocale.toString().equals(providerLocale)) {
            return;
        }
        int providerStatus = mProviderStatus;
        setProviderStatus(ProviderStatus.STATUS_CHANGING_LOCALE);
        AsyncTask<Integer, Void, Void> task = new AsyncTask<Integer, Void, Void>() {
            int savedProviderStatus;
            @Override
            protected Void doInBackground(Integer... params) {
                savedProviderStatus = params[0];
                mDbHelper.setLocale(ContactsProvider2.this, currentLocale);
                return null;
            }
            @Override
            protected void onPostExecute(Void result) {
                prefs.edit().putString(PREF_LOCALE, currentLocale.toString()).commit();
                setProviderStatus(savedProviderStatus);
                verifyLocale();
            }
        };
        task.execute(providerStatus);
    }
    @Override
    protected ContactsDatabaseHelper getDatabaseHelper(final Context context) {
        return ContactsDatabaseHelper.getInstance(context);
    }
     NameSplitter getNameSplitter() {
        return mNameSplitter;
    }
    protected Locale getLocale() {
        return Locale.getDefault();
    }
    protected boolean isLegacyContactImportNeeded() {
        int version = Integer.parseInt(mDbHelper.getProperty(PROPERTY_CONTACTS_IMPORTED, "0"));
        return version < PROPERTY_CONTACTS_IMPORT_VERSION;
    }
    protected LegacyContactImporter getLegacyContactImporter() {
        return new LegacyContactImporter(getContext(), this);
    }
    private void importLegacyContactsAsync() {
        Log.v(TAG, "Importing legacy contacts");
        setProviderStatus(ProviderStatus.STATUS_UPGRADING);
        if (mAccessLatch == null) {
            mAccessLatch = new CountDownLatch(1);
        }
        Thread importThread = new Thread("LegacyContactImport") {
            @Override
            public void run() {
                final SharedPreferences prefs =
                    PreferenceManager.getDefaultSharedPreferences(getContext());
                mDbHelper.setLocale(ContactsProvider2.this, mCurrentLocale);
                prefs.edit().putString(PREF_LOCALE, mCurrentLocale.toString()).commit();
                LegacyContactImporter importer = getLegacyContactImporter();
                if (importLegacyContacts(importer)) {
                    onLegacyContactImportSuccess();
                } else {
                    onLegacyContactImportFailure();
                }
            }
        };
        importThread.start();
    }
    private void onLegacyContactImportSuccess() {
        NotificationManager nm =
            (NotificationManager)getContext().getSystemService(Context.NOTIFICATION_SERVICE);
        nm.cancel(LEGACY_IMPORT_FAILED_NOTIFICATION);
        mDbHelper.setProperty(PROPERTY_CONTACTS_IMPORTED,
                String.valueOf(PROPERTY_CONTACTS_IMPORT_VERSION));
        setProviderStatus(ProviderStatus.STATUS_NORMAL);
        mAccessLatch.countDown();
        mAccessLatch = null;
        Log.v(TAG, "Completed import of legacy contacts");
    }
    private void onLegacyContactImportFailure() {
        Context context = getContext();
        NotificationManager nm =
            (NotificationManager)context.getSystemService(Context.NOTIFICATION_SERVICE);
        Notification n = new Notification(android.R.drawable.stat_notify_error,
                context.getString(R.string.upgrade_out_of_memory_notification_ticker),
                System.currentTimeMillis());
        n.setLatestEventInfo(context,
                context.getString(R.string.upgrade_out_of_memory_notification_title),
                context.getString(R.string.upgrade_out_of_memory_notification_text),
                PendingIntent.getActivity(context, 0, new Intent(Intents.UI.LIST_DEFAULT), 0));
        n.flags |= Notification.FLAG_NO_CLEAR | Notification.FLAG_ONGOING_EVENT;
        nm.notify(LEGACY_IMPORT_FAILED_NOTIFICATION, n);
        setProviderStatus(ProviderStatus.STATUS_UPGRADE_OUT_OF_MEMORY);
        Log.v(TAG, "Failed to import legacy contacts");
    }
     boolean importLegacyContacts(LegacyContactImporter importer) {
        boolean aggregatorEnabled = mContactAggregator.isEnabled();
        mContactAggregator.setEnabled(false);
        try {
            if (importer.importContacts()) {
                mContactAggregator.setEnabled(aggregatorEnabled);
                return true;
            }
        } catch (Throwable e) {
           Log.e(TAG, "Legacy contact import failed", e);
        }
        mEstimatedStorageRequirement = importer.getEstimatedStorageRequirement();
        return false;
    }
     void wipeData() {
        mDbHelper.wipeData();
    }
    private void waitForAccess() {
        CountDownLatch latch = mAccessLatch;
        if (latch != null) {
            while (true) {
                try {
                    latch.await();
                    mAccessLatch = null;
                    return;
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }
        }
    }
    @Override
    public Uri insert(Uri uri, ContentValues values) {
        waitForAccess();
        return super.insert(uri, values);
    }
    @Override
    public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
        if (mAccessLatch != null) {
            int match = sUriMatcher.match(uri);
            if (match == PROVIDER_STATUS && isLegacyContactImportNeeded()) {
                Integer newStatus = values.getAsInteger(ProviderStatus.STATUS);
                if (newStatus != null && newStatus == ProviderStatus.STATUS_UPGRADING) {
                    importLegacyContactsAsync();
                    return 1;
                } else {
                    return 0;
                }
            }
        }
        waitForAccess();
        return super.update(uri, values, selection, selectionArgs);
    }
    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        waitForAccess();
        return super.delete(uri, selection, selectionArgs);
    }
    @Override
    public ContentProviderResult[] applyBatch(ArrayList<ContentProviderOperation> operations)
            throws OperationApplicationException {
        waitForAccess();
        return super.applyBatch(operations);
    }
    @Override
    protected void onBeginTransaction() {
        if (VERBOSE_LOGGING) {
            Log.v(TAG, "onBeginTransaction");
        }
        super.onBeginTransaction();
        mContactAggregator.clearPendingAggregations();
        clearTransactionalChanges();
    }
    private void clearTransactionalChanges() {
        mInsertedRawContacts.clear();
        mUpdatedRawContacts.clear();
        mUpdatedSyncStates.clear();
        mDirtyRawContacts.clear();
    }
    @Override
    protected void beforeTransactionCommit() {
        if (VERBOSE_LOGGING) {
            Log.v(TAG, "beforeTransactionCommit");
        }
        super.beforeTransactionCommit();
        flushTransactionalChanges();
        mContactAggregator.aggregateInTransaction(mDb);
        if (mVisibleTouched) {
            mVisibleTouched = false;
            mDbHelper.updateAllVisible();
        }
    }
    private void flushTransactionalChanges() {
        if (VERBOSE_LOGGING) {
            Log.v(TAG, "flushTransactionChanges");
        }
        for (long rawContactId : mInsertedRawContacts.keySet()) {
            updateRawContactDisplayName(mDb, rawContactId);
            mContactAggregator.onRawContactInsert(mDb, rawContactId);
        }
        if (!mDirtyRawContacts.isEmpty()) {
            mSb.setLength(0);
            mSb.append(UPDATE_RAW_CONTACT_SET_DIRTY_SQL);
            appendIds(mSb, mDirtyRawContacts);
            mSb.append(")");
            mDb.execSQL(mSb.toString());
        }
        if (!mUpdatedRawContacts.isEmpty()) {
            mSb.setLength(0);
            mSb.append(UPDATE_RAW_CONTACT_SET_VERSION_SQL);
            appendIds(mSb, mUpdatedRawContacts);
            mSb.append(")");
            mDb.execSQL(mSb.toString());
        }
        for (Map.Entry<Long, Object> entry : mUpdatedSyncStates.entrySet()) {
            long id = entry.getKey();
            if (mDbHelper.getSyncState().update(mDb, id, entry.getValue()) <= 0) {
                throw new IllegalStateException(
                        "unable to update sync state, does it still exist?");
            }
        }
        clearTransactionalChanges();
    }
    private void appendIds(StringBuilder sb, HashSet<Long> ids) {
        for (long id : ids) {
            sb.append(id).append(',');
        }
        sb.setLength(sb.length() - 1); 
    }
    @Override
    protected void notifyChange() {
        notifyChange(mSyncToNetwork);
        mSyncToNetwork = false;
    }
    protected void notifyChange(boolean syncToNetwork) {
        getContext().getContentResolver().notifyChange(ContactsContract.AUTHORITY_URI, null,
                syncToNetwork);
    }
    protected void setProviderStatus(int status) {
        mProviderStatus = status;
        getContext().getContentResolver().notifyChange(ContactsContract.ProviderStatus.CONTENT_URI,
                null, false);
    }
    private boolean isNewRawContact(long rawContactId) {
        return mInsertedRawContacts.containsKey(rawContactId);
    }
    private DataRowHandler getDataRowHandler(final String mimeType) {
        DataRowHandler handler = mDataRowHandlers.get(mimeType);
        if (handler == null) {
            handler = new CustomDataRowHandler(mimeType);
            mDataRowHandlers.put(mimeType, handler);
        }
        return handler;
    }
    @Override
    protected Uri insertInTransaction(Uri uri, ContentValues values) {
        if (VERBOSE_LOGGING) {
            Log.v(TAG, "insertInTransaction: " + uri + " " + values);
        }
        final boolean callerIsSyncAdapter =
                readBooleanQueryParameter(uri, ContactsContract.CALLER_IS_SYNCADAPTER, false);
        final int match = sUriMatcher.match(uri);
        long id = 0;
        switch (match) {
            case SYNCSTATE:
                id = mDbHelper.getSyncState().insert(mDb, values);
                break;
            case CONTACTS: {
                insertContact(values);
                break;
            }
            case RAW_CONTACTS: {
                id = insertRawContact(uri, values);
                mSyncToNetwork |= !callerIsSyncAdapter;
                break;
            }
            case RAW_CONTACTS_DATA: {
                values.put(Data.RAW_CONTACT_ID, uri.getPathSegments().get(1));
                id = insertData(values, callerIsSyncAdapter);
                mSyncToNetwork |= !callerIsSyncAdapter;
                break;
            }
            case DATA: {
                id = insertData(values, callerIsSyncAdapter);
                mSyncToNetwork |= !callerIsSyncAdapter;
                break;
            }
            case GROUPS: {
                id = insertGroup(uri, values, callerIsSyncAdapter);
                mSyncToNetwork |= !callerIsSyncAdapter;
                break;
            }
            case SETTINGS: {
                id = insertSettings(uri, values);
                mSyncToNetwork |= !callerIsSyncAdapter;
                break;
            }
            case STATUS_UPDATES: {
                id = insertStatusUpdate(values);
                break;
            }
            default:
                mSyncToNetwork = true;
                return mLegacyApiSupport.insert(uri, values);
        }
        if (id < 0) {
            return null;
        }
        return ContentUris.withAppendedId(uri, id);
    }
    private Account resolveAccount(Uri uri, ContentValues values) throws IllegalArgumentException {
        String accountName = getQueryParameter(uri, RawContacts.ACCOUNT_NAME);
        String accountType = getQueryParameter(uri, RawContacts.ACCOUNT_TYPE);
        final boolean partialUri = TextUtils.isEmpty(accountName) ^ TextUtils.isEmpty(accountType);
        String valueAccountName = values.getAsString(RawContacts.ACCOUNT_NAME);
        String valueAccountType = values.getAsString(RawContacts.ACCOUNT_TYPE);
        final boolean partialValues = TextUtils.isEmpty(valueAccountName)
                ^ TextUtils.isEmpty(valueAccountType);
        if (partialUri || partialValues) {
            throw new IllegalArgumentException(mDbHelper.exceptionMessage(
                    "Must specify both or neither of ACCOUNT_NAME and ACCOUNT_TYPE", uri));
        }
        final boolean validUri = !TextUtils.isEmpty(accountName);
        final boolean validValues = !TextUtils.isEmpty(valueAccountName);
        if (validValues && validUri) {
            final boolean accountMatch = TextUtils.equals(accountName, valueAccountName)
                    && TextUtils.equals(accountType, valueAccountType);
            if (!accountMatch) {
                throw new IllegalArgumentException(mDbHelper.exceptionMessage(
                        "When both specified, ACCOUNT_NAME and ACCOUNT_TYPE must match", uri));
            }
        } else if (validUri) {
            values.put(RawContacts.ACCOUNT_NAME, accountName);
            values.put(RawContacts.ACCOUNT_TYPE, accountType);
        } else if (validValues) {
            accountName = valueAccountName;
            accountType = valueAccountType;
        } else {
            return null;
        }
        if (mAccount == null
                || !mAccount.name.equals(accountName)
                || !mAccount.type.equals(accountType)) {
            mAccount = new Account(accountName, accountType);
        }
        return mAccount;
    }
    private long insertContact(ContentValues values) {
        throw new UnsupportedOperationException("Aggregate contacts are created automatically");
    }
    private long insertRawContact(Uri uri, ContentValues values) {
        mValues.clear();
        mValues.putAll(values);
        mValues.putNull(RawContacts.CONTACT_ID);
        final Account account = resolveAccount(uri, mValues);
        if (values.containsKey(RawContacts.DELETED)
                && values.getAsInteger(RawContacts.DELETED) != 0) {
            mValues.put(RawContacts.AGGREGATION_MODE, RawContacts.AGGREGATION_MODE_DISABLED);
        }
        long rawContactId = mDb.insert(Tables.RAW_CONTACTS, RawContacts.CONTACT_ID, mValues);
        int aggregationMode = RawContacts.AGGREGATION_MODE_DEFAULT;
        if (mValues.containsKey(RawContacts.AGGREGATION_MODE)) {
            aggregationMode = mValues.getAsInteger(RawContacts.AGGREGATION_MODE);
        }
        mContactAggregator.markNewForAggregation(rawContactId, aggregationMode);
        mInsertedRawContacts.put(rawContactId, account);
        return rawContactId;
    }
    private long insertData(ContentValues values, boolean callerIsSyncAdapter) {
        long id = 0;
        mValues.clear();
        mValues.putAll(values);
        long rawContactId = mValues.getAsLong(Data.RAW_CONTACT_ID);
        final String packageName = mValues.getAsString(Data.RES_PACKAGE);
        if (packageName != null) {
            mValues.put(DataColumns.PACKAGE_ID, mDbHelper.getPackageId(packageName));
        }
        mValues.remove(Data.RES_PACKAGE);
        final String mimeType = mValues.getAsString(Data.MIMETYPE);
        if (TextUtils.isEmpty(mimeType)) {
            throw new IllegalArgumentException(Data.MIMETYPE + " is required");
        }
        mValues.put(DataColumns.MIMETYPE_ID, mDbHelper.getMimeTypeId(mimeType));
        mValues.remove(Data.MIMETYPE);
        DataRowHandler rowHandler = getDataRowHandler(mimeType);
        id = rowHandler.insert(mDb, rawContactId, mValues);
        if (!callerIsSyncAdapter) {
            setRawContactDirty(rawContactId);
        }
        mUpdatedRawContacts.add(rawContactId);
        return id;
    }
    private void triggerAggregation(long rawContactId) {
        if (!mContactAggregator.isEnabled()) {
            return;
        }
        int aggregationMode = mDbHelper.getAggregationMode(rawContactId);
        switch (aggregationMode) {
            case RawContacts.AGGREGATION_MODE_DISABLED:
                break;
            case RawContacts.AGGREGATION_MODE_DEFAULT: {
                mContactAggregator.markForAggregation(rawContactId, aggregationMode, false);
                break;
            }
            case RawContacts.AGGREGATION_MODE_SUSPENDED: {
                long contactId = mDbHelper.getContactId(rawContactId);
                if (contactId != 0) {
                    mContactAggregator.updateAggregateData(contactId);
                }
                break;
            }
            case RawContacts.AGGREGATION_MODE_IMMEDIATE: {
                long contactId = mDbHelper.getContactId(rawContactId);
                mContactAggregator.aggregateContact(mDb, rawContactId, contactId);
                break;
            }
        }
    }
    private long getOrMakeGroup(SQLiteDatabase db, long rawContactId, String sourceId,
            Account account) {
        if (account == null) {
            mSelectionArgs1[0] = String.valueOf(rawContactId);
            Cursor c = db.query(RawContactsQuery.TABLE, RawContactsQuery.COLUMNS,
                    RawContacts._ID + "=?", mSelectionArgs1, null, null, null);
            try {
                if (c.moveToFirst()) {
                    String accountName = c.getString(RawContactsQuery.ACCOUNT_NAME);
                    String accountType = c.getString(RawContactsQuery.ACCOUNT_TYPE);
                    if (!TextUtils.isEmpty(accountName) && !TextUtils.isEmpty(accountType)) {
                        account = new Account(accountName, accountType);
                    }
                }
            } finally {
                c.close();
            }
        }
        if (account == null) {
            throw new IllegalArgumentException("if the groupmembership only "
                    + "has a sourceid the the contact must be associated with "
                    + "an account");
        }
        ArrayList<GroupIdCacheEntry> entries = mGroupIdCache.get(sourceId);
        if (entries == null) {
            entries = new ArrayList<GroupIdCacheEntry>(1);
            mGroupIdCache.put(sourceId, entries);
        }
        int count = entries.size();
        for (int i = 0; i < count; i++) {
            GroupIdCacheEntry entry = entries.get(i);
            if (entry.accountName.equals(account.name) && entry.accountType.equals(account.type)) {
                return entry.groupId;
            }
        }
        GroupIdCacheEntry entry = new GroupIdCacheEntry();
        entry.accountName = account.name;
        entry.accountType = account.type;
        entry.sourceId = sourceId;
        entries.add(0, entry);
        Cursor c = db.query(Tables.GROUPS, new String[]{RawContacts._ID},
                Clauses.GROUP_HAS_ACCOUNT_AND_SOURCE_ID,
                new String[]{sourceId, account.name, account.type}, null, null, null);
        try {
            if (c.moveToFirst()) {
                entry.groupId = c.getLong(0);
            } else {
                ContentValues groupValues = new ContentValues();
                groupValues.put(Groups.ACCOUNT_NAME, account.name);
                groupValues.put(Groups.ACCOUNT_TYPE, account.type);
                groupValues.put(Groups.SOURCE_ID, sourceId);
                long groupId = db.insert(Tables.GROUPS, Groups.ACCOUNT_NAME, groupValues);
                if (groupId < 0) {
                    throw new IllegalStateException("unable to create a new group with "
                            + "this sourceid: " + groupValues);
                }
                entry.groupId = groupId;
            }
        } finally {
            c.close();
        }
        return entry.groupId;
    }
    private interface DisplayNameQuery {
        public static final String RAW_SQL =
                "SELECT "
                        + DataColumns.MIMETYPE_ID + ","
                        + Data.IS_PRIMARY + ","
                        + Data.DATA1 + ","
                        + Data.DATA2 + ","
                        + Data.DATA3 + ","
                        + Data.DATA4 + ","
                        + Data.DATA5 + ","
                        + Data.DATA6 + ","
                        + Data.DATA7 + ","
                        + Data.DATA8 + ","
                        + Data.DATA9 + ","
                        + Data.DATA10 + ","
                        + Data.DATA11 +
                " FROM " + Tables.DATA +
                " WHERE " + Data.RAW_CONTACT_ID + "=?" +
                        " AND (" + Data.DATA1 + " NOT NULL OR " +
                                Organization.TITLE + " NOT NULL)";
        public static final int MIMETYPE = 0;
        public static final int IS_PRIMARY = 1;
        public static final int DATA1 = 2;
        public static final int GIVEN_NAME = 3;                         
        public static final int FAMILY_NAME = 4;                        
        public static final int PREFIX = 5;                             
        public static final int TITLE = 5;                              
        public static final int MIDDLE_NAME = 6;                        
        public static final int SUFFIX = 7;                             
        public static final int PHONETIC_GIVEN_NAME = 8;                
        public static final int PHONETIC_MIDDLE_NAME = 9;               
        public static final int ORGANIZATION_PHONETIC_NAME = 9;         
        public static final int PHONETIC_FAMILY_NAME = 10;              
        public static final int FULL_NAME_STYLE = 11;                   
        public static final int ORGANIZATION_PHONETIC_NAME_STYLE = 11;  
        public static final int PHONETIC_NAME_STYLE = 12;               
    }
    public void updateRawContactDisplayName(SQLiteDatabase db, long rawContactId) {
        int bestDisplayNameSource = DisplayNameSources.UNDEFINED;
        NameSplitter.Name bestName = null;
        String bestDisplayName = null;
        String bestPhoneticName = null;
        int bestPhoneticNameStyle = PhoneticNameStyle.UNDEFINED;
        mSelectionArgs1[0] = String.valueOf(rawContactId);
        Cursor c = db.rawQuery(DisplayNameQuery.RAW_SQL, mSelectionArgs1);
        try {
            while (c.moveToNext()) {
                int mimeType = c.getInt(DisplayNameQuery.MIMETYPE);
                int source = getDisplayNameSource(mimeType);
                if (source < bestDisplayNameSource || source == DisplayNameSources.UNDEFINED) {
                    continue;
                }
                if (source == bestDisplayNameSource && c.getInt(DisplayNameQuery.IS_PRIMARY) == 0) {
                    continue;
                }
                if (mimeType == mMimeTypeIdStructuredName) {
                    NameSplitter.Name name;
                    if (bestName != null) {
                        name = new NameSplitter.Name();
                    } else {
                        name = mName;
                        name.clear();
                    }
                    name.prefix = c.getString(DisplayNameQuery.PREFIX);
                    name.givenNames = c.getString(DisplayNameQuery.GIVEN_NAME);
                    name.middleName = c.getString(DisplayNameQuery.MIDDLE_NAME);
                    name.familyName = c.getString(DisplayNameQuery.FAMILY_NAME);
                    name.suffix = c.getString(DisplayNameQuery.SUFFIX);
                    name.fullNameStyle = c.isNull(DisplayNameQuery.FULL_NAME_STYLE)
                            ? FullNameStyle.UNDEFINED
                            : c.getInt(DisplayNameQuery.FULL_NAME_STYLE);
                    name.phoneticFamilyName = c.getString(DisplayNameQuery.PHONETIC_FAMILY_NAME);
                    name.phoneticMiddleName = c.getString(DisplayNameQuery.PHONETIC_MIDDLE_NAME);
                    name.phoneticGivenName = c.getString(DisplayNameQuery.PHONETIC_GIVEN_NAME);
                    name.phoneticNameStyle = c.isNull(DisplayNameQuery.PHONETIC_NAME_STYLE)
                            ? PhoneticNameStyle.UNDEFINED
                            : c.getInt(DisplayNameQuery.PHONETIC_NAME_STYLE);
                    if (!name.isEmpty()) {
                        bestDisplayNameSource = source;
                        bestName = name;
                    }
                } else if (mimeType == mMimeTypeIdOrganization) {
                    mCharArrayBuffer.sizeCopied = 0;
                    c.copyStringToBuffer(DisplayNameQuery.DATA1, mCharArrayBuffer);
                    if (mCharArrayBuffer.sizeCopied != 0) {
                        bestDisplayNameSource = source;
                        bestDisplayName = new String(mCharArrayBuffer.data, 0,
                                mCharArrayBuffer.sizeCopied);
                        bestPhoneticName = c.getString(DisplayNameQuery.ORGANIZATION_PHONETIC_NAME);
                        bestPhoneticNameStyle =
                                c.isNull(DisplayNameQuery.ORGANIZATION_PHONETIC_NAME_STYLE)
                                    ? PhoneticNameStyle.UNDEFINED
                                    : c.getInt(DisplayNameQuery.ORGANIZATION_PHONETIC_NAME_STYLE);
                    } else {
                        c.copyStringToBuffer(DisplayNameQuery.TITLE, mCharArrayBuffer);
                        if (mCharArrayBuffer.sizeCopied != 0) {
                            bestDisplayNameSource = source;
                            bestDisplayName = new String(mCharArrayBuffer.data, 0,
                                    mCharArrayBuffer.sizeCopied);
                            bestPhoneticName = null;
                            bestPhoneticNameStyle = PhoneticNameStyle.UNDEFINED;
                        }
                    }
                } else {
                    mCharArrayBuffer.sizeCopied = 0;
                    c.copyStringToBuffer(DisplayNameQuery.DATA1, mCharArrayBuffer);
                    if (mCharArrayBuffer.sizeCopied != 0) {
                        bestDisplayNameSource = source;
                        bestDisplayName = new String(mCharArrayBuffer.data, 0,
                                mCharArrayBuffer.sizeCopied);
                        bestPhoneticName = null;
                        bestPhoneticNameStyle = PhoneticNameStyle.UNDEFINED;
                    }
                }
            }
        } finally {
            c.close();
        }
        String displayNamePrimary;
        String displayNameAlternative;
        String sortKeyPrimary = null;
        String sortKeyAlternative = null;
        int displayNameStyle = FullNameStyle.UNDEFINED;
        if (bestDisplayNameSource == DisplayNameSources.STRUCTURED_NAME) {
            displayNameStyle = bestName.fullNameStyle;
            if (displayNameStyle == FullNameStyle.CJK
                    || displayNameStyle == FullNameStyle.UNDEFINED) {
                displayNameStyle = mNameSplitter.getAdjustedFullNameStyle(displayNameStyle);
                bestName.fullNameStyle = displayNameStyle;
            }
            displayNamePrimary = mNameSplitter.join(bestName, true);
            displayNameAlternative = mNameSplitter.join(bestName, false);
            bestPhoneticName = mNameSplitter.joinPhoneticName(bestName);
            bestPhoneticNameStyle = bestName.phoneticNameStyle;
        } else {
            displayNamePrimary = displayNameAlternative = bestDisplayName;
        }
        if (bestPhoneticName != null) {
            sortKeyPrimary = sortKeyAlternative = bestPhoneticName;
            if (bestPhoneticNameStyle == PhoneticNameStyle.UNDEFINED) {
                bestPhoneticNameStyle = mNameSplitter.guessPhoneticNameStyle(bestPhoneticName);
            }
        } else {
            if (displayNameStyle == FullNameStyle.UNDEFINED) {
                displayNameStyle = mNameSplitter.guessFullNameStyle(bestDisplayName);
                if (displayNameStyle == FullNameStyle.UNDEFINED
                        || displayNameStyle == FullNameStyle.CJK) {
                    displayNameStyle = mNameSplitter.getAdjustedNameStyleBasedOnPhoneticNameStyle(
                            displayNameStyle, bestPhoneticNameStyle);
                }
                displayNameStyle = mNameSplitter.getAdjustedFullNameStyle(displayNameStyle);
            }
            if (displayNameStyle == FullNameStyle.CHINESE ||
                    displayNameStyle == FullNameStyle.CJK) {
                sortKeyPrimary = sortKeyAlternative =
                        ContactLocaleUtils.getIntance().getSortKey(
                                displayNamePrimary, displayNameStyle);
            }
        }
        if (sortKeyPrimary == null) {
            sortKeyPrimary = displayNamePrimary;
            sortKeyAlternative = displayNameAlternative;
        }
        setDisplayName(rawContactId, bestDisplayNameSource, displayNamePrimary,
                displayNameAlternative, bestPhoneticName, bestPhoneticNameStyle,
                sortKeyPrimary, sortKeyAlternative);
    }
    private int getDisplayNameSource(int mimeTypeId) {
        if (mimeTypeId == mMimeTypeIdStructuredName) {
            return DisplayNameSources.STRUCTURED_NAME;
        } else if (mimeTypeId == mMimeTypeIdEmail) {
            return DisplayNameSources.EMAIL;
        } else if (mimeTypeId == mMimeTypeIdPhone) {
            return DisplayNameSources.PHONE;
        } else if (mimeTypeId == mMimeTypeIdOrganization) {
            return DisplayNameSources.ORGANIZATION;
        } else if (mimeTypeId == mMimeTypeIdNickname) {
            return DisplayNameSources.NICKNAME;
        } else {
            return DisplayNameSources.UNDEFINED;
        }
    }
    private int deleteData(String selection, String[] selectionArgs, boolean callerIsSyncAdapter) {
        int count = 0;
        Cursor c = query(Data.CONTENT_URI, DataDeleteQuery.COLUMNS, selection, selectionArgs, null);
        try {
            while(c.moveToNext()) {
                long rawContactId = c.getLong(DataDeleteQuery.RAW_CONTACT_ID);
                String mimeType = c.getString(DataDeleteQuery.MIMETYPE);
                DataRowHandler rowHandler = getDataRowHandler(mimeType);
                count += rowHandler.delete(mDb, c);
                if (!callerIsSyncAdapter) {
                    setRawContactDirty(rawContactId);
                }
            }
        } finally {
            c.close();
        }
        return count;
    }
    public int deleteData(long dataId, String[] allowedMimeTypes) {
        mSelectionArgs1[0] = String.valueOf(dataId);
        Cursor c = query(Data.CONTENT_URI, DataDeleteQuery.COLUMNS, Data._ID + "=?",
                mSelectionArgs1, null);
        try {
            if (!c.moveToFirst()) {
                return 0;
            }
            String mimeType = c.getString(DataDeleteQuery.MIMETYPE);
            boolean valid = false;
            for (int i = 0; i < allowedMimeTypes.length; i++) {
                if (TextUtils.equals(mimeType, allowedMimeTypes[i])) {
                    valid = true;
                    break;
                }
            }
            if (!valid) {
                throw new IllegalArgumentException("Data type mismatch: expected "
                        + Lists.newArrayList(allowedMimeTypes));
            }
            DataRowHandler rowHandler = getDataRowHandler(mimeType);
            return rowHandler.delete(mDb, c);
        } finally {
            c.close();
        }
    }
    private long insertGroup(Uri uri, ContentValues values, boolean callerIsSyncAdapter) {
        mValues.clear();
        mValues.putAll(values);
        final Account account = resolveAccount(uri, mValues);
        final String packageName = mValues.getAsString(Groups.RES_PACKAGE);
        if (packageName != null) {
            mValues.put(GroupsColumns.PACKAGE_ID, mDbHelper.getPackageId(packageName));
        }
        mValues.remove(Groups.RES_PACKAGE);
        if (!callerIsSyncAdapter) {
            mValues.put(Groups.DIRTY, 1);
        }
        long result = mDb.insert(Tables.GROUPS, Groups.TITLE, mValues);
        if (mValues.containsKey(Groups.GROUP_VISIBLE)) {
            mVisibleTouched = true;
        }
        return result;
    }
    private long insertSettings(Uri uri, ContentValues values) {
        final long id = mDb.insert(Tables.SETTINGS, null, values);
        if (values.containsKey(Settings.UNGROUPED_VISIBLE)) {
            mVisibleTouched = true;
        }
        return id;
    }
    public long insertStatusUpdate(ContentValues values) {
        final String handle = values.getAsString(StatusUpdates.IM_HANDLE);
        final Integer protocol = values.getAsInteger(StatusUpdates.PROTOCOL);
        String customProtocol = null;
        if (protocol != null && protocol == Im.PROTOCOL_CUSTOM) {
            customProtocol = values.getAsString(StatusUpdates.CUSTOM_PROTOCOL);
            if (TextUtils.isEmpty(customProtocol)) {
                throw new IllegalArgumentException(
                        "CUSTOM_PROTOCOL is required when PROTOCOL=PROTOCOL_CUSTOM");
            }
        }
        long rawContactId = -1;
        long contactId = -1;
        Long dataId = values.getAsLong(StatusUpdates.DATA_ID);
        mSb.setLength(0);
        mSelectionArgs.clear();
        if (dataId != null) {
            mSb.append(Tables.DATA + "." + Data._ID + "=?");
            mSelectionArgs.add(String.valueOf(dataId));
        } else {
            if (TextUtils.isEmpty(handle) || protocol == null) {
                throw new IllegalArgumentException("PROTOCOL and IM_HANDLE are required");
            }
            boolean matchEmail = Im.PROTOCOL_GOOGLE_TALK == protocol;
            String mimeTypeIdIm = String.valueOf(mMimeTypeIdIm);
            if (matchEmail) {
                String mimeTypeIdEmail = String.valueOf(mMimeTypeIdEmail);
                mSb.append(DataColumns.MIMETYPE_ID + " IN (?,?)" +
                        " AND " + Data.DATA1 + "=?" +
                        " AND ((" + DataColumns.MIMETYPE_ID + "=? AND " + Im.PROTOCOL + "=?");
                mSelectionArgs.add(mimeTypeIdEmail);
                mSelectionArgs.add(mimeTypeIdIm);
                mSelectionArgs.add(handle);
                mSelectionArgs.add(mimeTypeIdIm);
                mSelectionArgs.add(String.valueOf(protocol));
                if (customProtocol != null) {
                    mSb.append(" AND " + Im.CUSTOM_PROTOCOL + "=?");
                    mSelectionArgs.add(customProtocol);
                }
                mSb.append(") OR (" + DataColumns.MIMETYPE_ID + "=?))");
                mSelectionArgs.add(mimeTypeIdEmail);
            } else {
                mSb.append(DataColumns.MIMETYPE_ID + "=?" +
                        " AND " + Im.PROTOCOL + "=?" +
                        " AND " + Im.DATA + "=?");
                mSelectionArgs.add(mimeTypeIdIm);
                mSelectionArgs.add(String.valueOf(protocol));
                mSelectionArgs.add(handle);
                if (customProtocol != null) {
                    mSb.append(" AND " + Im.CUSTOM_PROTOCOL + "=?");
                    mSelectionArgs.add(customProtocol);
                }
            }
            if (values.containsKey(StatusUpdates.DATA_ID)) {
                mSb.append(" AND " + DataColumns.CONCRETE_ID + "=?");
                mSelectionArgs.add(values.getAsString(StatusUpdates.DATA_ID));
            }
        }
        mSb.append(" AND ").append(getContactsRestrictions());
        Cursor cursor = null;
        try {
            cursor = mDb.query(DataContactsQuery.TABLE, DataContactsQuery.PROJECTION,
                    mSb.toString(), mSelectionArgs.toArray(EMPTY_STRING_ARRAY), null, null,
                    Contacts.IN_VISIBLE_GROUP + " DESC, " + Data.RAW_CONTACT_ID);
            if (cursor.moveToFirst()) {
                dataId = cursor.getLong(DataContactsQuery.DATA_ID);
                rawContactId = cursor.getLong(DataContactsQuery.RAW_CONTACT_ID);
                contactId = cursor.getLong(DataContactsQuery.CONTACT_ID);
            } else {
                return -1;
            }
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
        if (values.containsKey(StatusUpdates.PRESENCE)) {
            if (customProtocol == null) {
                customProtocol = "";
            }
            mValues.clear();
            mValues.put(StatusUpdates.DATA_ID, dataId);
            mValues.put(PresenceColumns.RAW_CONTACT_ID, rawContactId);
            mValues.put(PresenceColumns.CONTACT_ID, contactId);
            mValues.put(StatusUpdates.PROTOCOL, protocol);
            mValues.put(StatusUpdates.CUSTOM_PROTOCOL, customProtocol);
            mValues.put(StatusUpdates.IM_HANDLE, handle);
            if (values.containsKey(StatusUpdates.IM_ACCOUNT)) {
                mValues.put(StatusUpdates.IM_ACCOUNT, values.getAsString(StatusUpdates.IM_ACCOUNT));
            }
            mValues.put(StatusUpdates.PRESENCE,
                    values.getAsString(StatusUpdates.PRESENCE));
            mDb.replace(Tables.PRESENCE, null, mValues);
        }
        if (values.containsKey(StatusUpdates.STATUS)) {
            String status = values.getAsString(StatusUpdates.STATUS);
            String resPackage = values.getAsString(StatusUpdates.STATUS_RES_PACKAGE);
            Integer labelResource = values.getAsInteger(StatusUpdates.STATUS_LABEL);
            if (TextUtils.isEmpty(resPackage)
                    && (labelResource == null || labelResource == 0)
                    && protocol != null) {
                labelResource = Im.getProtocolLabelResource(protocol);
            }
            Long iconResource = values.getAsLong(StatusUpdates.STATUS_ICON);
            if (TextUtils.isEmpty(status)) {
                mStatusUpdateDelete.bindLong(1, dataId);
                mStatusUpdateDelete.execute();
            } else if (values.containsKey(StatusUpdates.STATUS_TIMESTAMP)) {
                long timestamp = values.getAsLong(StatusUpdates.STATUS_TIMESTAMP);
                mStatusUpdateReplace.bindLong(1, dataId);
                mStatusUpdateReplace.bindLong(2, timestamp);
                bindString(mStatusUpdateReplace, 3, status);
                bindString(mStatusUpdateReplace, 4, resPackage);
                bindLong(mStatusUpdateReplace, 5, iconResource);
                bindLong(mStatusUpdateReplace, 6, labelResource);
                mStatusUpdateReplace.execute();
            } else {
                try {
                    mStatusUpdateInsert.bindLong(1, dataId);
                    bindString(mStatusUpdateInsert, 2, status);
                    bindString(mStatusUpdateInsert, 3, resPackage);
                    bindLong(mStatusUpdateInsert, 4, iconResource);
                    bindLong(mStatusUpdateInsert, 5, labelResource);
                    mStatusUpdateInsert.executeInsert();
                } catch (SQLiteConstraintException e) {
                    long timestamp = System.currentTimeMillis();
                    mStatusUpdateAutoTimestamp.bindLong(1, timestamp);
                    bindString(mStatusUpdateAutoTimestamp, 2, status);
                    mStatusUpdateAutoTimestamp.bindLong(3, dataId);
                    bindString(mStatusUpdateAutoTimestamp, 4, status);
                    mStatusUpdateAutoTimestamp.execute();
                    bindString(mStatusAttributionUpdate, 1, resPackage);
                    bindLong(mStatusAttributionUpdate, 2, iconResource);
                    bindLong(mStatusAttributionUpdate, 3, labelResource);
                    mStatusAttributionUpdate.bindLong(4, dataId);
                    mStatusAttributionUpdate.execute();
                }
            }
        }
        if (contactId != -1) {
            mLastStatusUpdate.bindLong(1, contactId);
            mLastStatusUpdate.bindLong(2, contactId);
            mLastStatusUpdate.execute();
        }
        return dataId;
    }
    @Override
    protected int deleteInTransaction(Uri uri, String selection, String[] selectionArgs) {
        if (VERBOSE_LOGGING) {
            Log.v(TAG, "deleteInTransaction: " + uri);
        }
        flushTransactionalChanges();
        final boolean callerIsSyncAdapter =
                readBooleanQueryParameter(uri, ContactsContract.CALLER_IS_SYNCADAPTER, false);
        final int match = sUriMatcher.match(uri);
        switch (match) {
            case SYNCSTATE:
                return mDbHelper.getSyncState().delete(mDb, selection, selectionArgs);
            case SYNCSTATE_ID:
                String selectionWithId =
                        (SyncStateContract.Columns._ID + "=" + ContentUris.parseId(uri) + " ")
                        + (selection == null ? "" : " AND (" + selection + ")");
                return mDbHelper.getSyncState().delete(mDb, selectionWithId, selectionArgs);
            case CONTACTS: {
                return 0;
            }
            case CONTACTS_ID: {
                long contactId = ContentUris.parseId(uri);
                return deleteContact(contactId);
            }
            case CONTACTS_LOOKUP: {
                final List<String> pathSegments = uri.getPathSegments();
                final int segmentCount = pathSegments.size();
                if (segmentCount < 3) {
                    throw new IllegalArgumentException(mDbHelper.exceptionMessage(
                            "Missing a lookup key", uri));
                }
                final String lookupKey = pathSegments.get(2);
                final long contactId = lookupContactIdByLookupKey(mDb, lookupKey);
                return deleteContact(contactId);
            }
            case CONTACTS_LOOKUP_ID: {
                long contactId = ContentUris.parseId(uri);
                final List<String> pathSegments = uri.getPathSegments();
                final String lookupKey = pathSegments.get(2);
                SQLiteQueryBuilder lookupQb = new SQLiteQueryBuilder();
                setTablesAndProjectionMapForContacts(lookupQb, uri, null);
                String[] args;
                if (selectionArgs == null) {
                    args = new String[2];
                } else {
                    args = new String[selectionArgs.length + 2];
                    System.arraycopy(selectionArgs, 0, args, 2, selectionArgs.length);
                }
                args[0] = String.valueOf(contactId);
                args[1] = Uri.encode(lookupKey);
                lookupQb.appendWhere(Contacts._ID + "=? AND " + Contacts.LOOKUP_KEY + "=?");
                final SQLiteDatabase db = mDbHelper.getReadableDatabase();
                Cursor c = query(db, lookupQb, null, selection, args, null, null, null);
                try {
                    if (c.getCount() == 1) {
                        return deleteContact(contactId);
                    } else {
                        return 0;
                    }
                } finally {
                    c.close();
                }
            }
            case RAW_CONTACTS: {
                int numDeletes = 0;
                Cursor c = mDb.query(Tables.RAW_CONTACTS,
                        new String[]{RawContacts._ID, RawContacts.CONTACT_ID},
                        appendAccountToSelection(uri, selection), selectionArgs, null, null, null);
                try {
                    while (c.moveToNext()) {
                        final long rawContactId = c.getLong(0);
                        long contactId = c.getLong(1);
                        numDeletes += deleteRawContact(rawContactId, contactId,
                                callerIsSyncAdapter);
                    }
                } finally {
                    c.close();
                }
                return numDeletes;
            }
            case RAW_CONTACTS_ID: {
                final long rawContactId = ContentUris.parseId(uri);
                return deleteRawContact(rawContactId, mDbHelper.getContactId(rawContactId),
                        callerIsSyncAdapter);
            }
            case DATA: {
                mSyncToNetwork |= !callerIsSyncAdapter;
                return deleteData(appendAccountToSelection(uri, selection), selectionArgs,
                        callerIsSyncAdapter);
            }
            case DATA_ID:
            case PHONES_ID:
            case EMAILS_ID:
            case POSTALS_ID: {
                long dataId = ContentUris.parseId(uri);
                mSyncToNetwork |= !callerIsSyncAdapter;
                mSelectionArgs1[0] = String.valueOf(dataId);
                return deleteData(Data._ID + "=?", mSelectionArgs1, callerIsSyncAdapter);
            }
            case GROUPS_ID: {
                mSyncToNetwork |= !callerIsSyncAdapter;
                return deleteGroup(uri, ContentUris.parseId(uri), callerIsSyncAdapter);
            }
            case GROUPS: {
                int numDeletes = 0;
                Cursor c = mDb.query(Tables.GROUPS, new String[]{Groups._ID},
                        appendAccountToSelection(uri, selection), selectionArgs, null, null, null);
                try {
                    while (c.moveToNext()) {
                        numDeletes += deleteGroup(uri, c.getLong(0), callerIsSyncAdapter);
                    }
                } finally {
                    c.close();
                }
                if (numDeletes > 0) {
                    mSyncToNetwork |= !callerIsSyncAdapter;
                }
                return numDeletes;
            }
            case SETTINGS: {
                mSyncToNetwork |= !callerIsSyncAdapter;
                return deleteSettings(uri, appendAccountToSelection(uri, selection), selectionArgs);
            }
            case STATUS_UPDATES: {
                return deleteStatusUpdates(selection, selectionArgs);
            }
            default: {
                mSyncToNetwork = true;
                return mLegacyApiSupport.delete(uri, selection, selectionArgs);
            }
        }
    }
    public int deleteGroup(Uri uri, long groupId, boolean callerIsSyncAdapter) {
        mGroupIdCache.clear();
        final long groupMembershipMimetypeId = mDbHelper
                .getMimeTypeId(GroupMembership.CONTENT_ITEM_TYPE);
        mDb.delete(Tables.DATA, DataColumns.MIMETYPE_ID + "="
                + groupMembershipMimetypeId + " AND " + GroupMembership.GROUP_ROW_ID + "="
                + groupId, null);
        try {
            if (callerIsSyncAdapter) {
                return mDb.delete(Tables.GROUPS, Groups._ID + "=" + groupId, null);
            } else {
                mValues.clear();
                mValues.put(Groups.DELETED, 1);
                mValues.put(Groups.DIRTY, 1);
                return mDb.update(Tables.GROUPS, mValues, Groups._ID + "=" + groupId, null);
            }
        } finally {
            mVisibleTouched = true;
        }
    }
    private int deleteSettings(Uri uri, String selection, String[] selectionArgs) {
        final int count = mDb.delete(Tables.SETTINGS, selection, selectionArgs);
        mVisibleTouched = true;
        return count;
    }
    private int deleteContact(long contactId) {
        mSelectionArgs1[0] = Long.toString(contactId);
        Cursor c = mDb.query(Tables.RAW_CONTACTS, new String[]{RawContacts._ID},
                RawContacts.CONTACT_ID + "=?", mSelectionArgs1,
                null, null, null);
        try {
            while (c.moveToNext()) {
                long rawContactId = c.getLong(0);
                markRawContactAsDeleted(rawContactId);
            }
        } finally {
            c.close();
        }
        return mDb.delete(Tables.CONTACTS, Contacts._ID + "=" + contactId, null);
    }
    public int deleteRawContact(long rawContactId, long contactId, boolean callerIsSyncAdapter) {
        mContactAggregator.invalidateAggregationExceptionCache();
        if (callerIsSyncAdapter) {
            mDb.delete(Tables.PRESENCE, PresenceColumns.RAW_CONTACT_ID + "=" + rawContactId, null);
            int count = mDb.delete(Tables.RAW_CONTACTS, RawContacts._ID + "=" + rawContactId, null);
            mContactAggregator.updateDisplayNameForContact(mDb, contactId);
            return count;
        } else {
            mDbHelper.removeContactIfSingleton(rawContactId);
            return markRawContactAsDeleted(rawContactId);
        }
    }
    private int deleteStatusUpdates(String selection, String[] selectionArgs) {
      if (VERBOSE_LOGGING) {
          Log.v(TAG, "deleting data from status_updates for " + selection);
      }
      mDb.delete(Tables.STATUS_UPDATES, getWhereClauseForStatusUpdatesTable(selection),
          selectionArgs);
      return mDb.delete(Tables.PRESENCE, selection, selectionArgs);
    }
    private int markRawContactAsDeleted(long rawContactId) {
        mSyncToNetwork = true;
        mValues.clear();
        mValues.put(RawContacts.DELETED, 1);
        mValues.put(RawContacts.AGGREGATION_MODE, RawContacts.AGGREGATION_MODE_DISABLED);
        mValues.put(RawContactsColumns.AGGREGATION_NEEDED, 1);
        mValues.putNull(RawContacts.CONTACT_ID);
        mValues.put(RawContacts.DIRTY, 1);
        return updateRawContact(rawContactId, mValues);
    }
    @Override
    protected int updateInTransaction(Uri uri, ContentValues values, String selection,
            String[] selectionArgs) {
        if (VERBOSE_LOGGING) {
            Log.v(TAG, "updateInTransaction: " + uri);
        }
        int count = 0;
        final int match = sUriMatcher.match(uri);
        if (match == SYNCSTATE_ID && selection == null) {
            long rowId = ContentUris.parseId(uri);
            Object data = values.get(ContactsContract.SyncState.DATA);
            mUpdatedSyncStates.put(rowId, data);
            return 1;
        }
        flushTransactionalChanges();
        final boolean callerIsSyncAdapter =
                readBooleanQueryParameter(uri, ContactsContract.CALLER_IS_SYNCADAPTER, false);
        switch(match) {
            case SYNCSTATE:
                return mDbHelper.getSyncState().update(mDb, values,
                        appendAccountToSelection(uri, selection), selectionArgs);
            case SYNCSTATE_ID: {
                selection = appendAccountToSelection(uri, selection);
                String selectionWithId =
                        (SyncStateContract.Columns._ID + "=" + ContentUris.parseId(uri) + " ")
                        + (selection == null ? "" : " AND (" + selection + ")");
                return mDbHelper.getSyncState().update(mDb, values,
                        selectionWithId, selectionArgs);
            }
            case CONTACTS: {
                count = updateContactOptions(values, selection, selectionArgs);
                break;
            }
            case CONTACTS_ID: {
                count = updateContactOptions(ContentUris.parseId(uri), values);
                break;
            }
            case CONTACTS_LOOKUP:
            case CONTACTS_LOOKUP_ID: {
                final List<String> pathSegments = uri.getPathSegments();
                final int segmentCount = pathSegments.size();
                if (segmentCount < 3) {
                    throw new IllegalArgumentException(mDbHelper.exceptionMessage(
                            "Missing a lookup key", uri));
                }
                final String lookupKey = pathSegments.get(2);
                final long contactId = lookupContactIdByLookupKey(mDb, lookupKey);
                count = updateContactOptions(contactId, values);
                break;
            }
            case RAW_CONTACTS_DATA: {
                final String rawContactId = uri.getPathSegments().get(1);
                String selectionWithId = (Data.RAW_CONTACT_ID + "=" + rawContactId + " ")
                    + (selection == null ? "" : " AND " + selection);
                count = updateData(uri, values, selectionWithId, selectionArgs, callerIsSyncAdapter);
                break;
            }
            case DATA: {
                count = updateData(uri, values, appendAccountToSelection(uri, selection),
                        selectionArgs, callerIsSyncAdapter);
                if (count > 0) {
                    mSyncToNetwork |= !callerIsSyncAdapter;
                }
                break;
            }
            case DATA_ID:
            case PHONES_ID:
            case EMAILS_ID:
            case POSTALS_ID: {
                count = updateData(uri, values, selection, selectionArgs, callerIsSyncAdapter);
                if (count > 0) {
                    mSyncToNetwork |= !callerIsSyncAdapter;
                }
                break;
            }
            case RAW_CONTACTS: {
                selection = appendAccountToSelection(uri, selection);
                count = updateRawContacts(values, selection, selectionArgs);
                break;
            }
            case RAW_CONTACTS_ID: {
                long rawContactId = ContentUris.parseId(uri);
                if (selection != null) {
                    selectionArgs = insertSelectionArg(selectionArgs, String.valueOf(rawContactId));
                    count = updateRawContacts(values, RawContacts._ID + "=?"
                                    + " AND(" + selection + ")", selectionArgs);
                } else {
                    mSelectionArgs1[0] = String.valueOf(rawContactId);
                    count = updateRawContacts(values, RawContacts._ID + "=?", mSelectionArgs1);
                }
                break;
            }
            case GROUPS: {
                count = updateGroups(uri, values, appendAccountToSelection(uri, selection),
                        selectionArgs, callerIsSyncAdapter);
                if (count > 0) {
                    mSyncToNetwork |= !callerIsSyncAdapter;
                }
                break;
            }
            case GROUPS_ID: {
                long groupId = ContentUris.parseId(uri);
                selectionArgs = insertSelectionArg(selectionArgs, String.valueOf(groupId));
                String selectionWithId = Groups._ID + "=? "
                        + (selection == null ? "" : " AND " + selection);
                count = updateGroups(uri, values, selectionWithId, selectionArgs,
                        callerIsSyncAdapter);
                if (count > 0) {
                    mSyncToNetwork |= !callerIsSyncAdapter;
                }
                break;
            }
            case AGGREGATION_EXCEPTIONS: {
                count = updateAggregationException(mDb, values);
                break;
            }
            case SETTINGS: {
                count = updateSettings(uri, values, appendAccountToSelection(uri, selection),
                        selectionArgs);
                mSyncToNetwork |= !callerIsSyncAdapter;
                break;
            }
            case STATUS_UPDATES: {
                count = updateStatusUpdate(uri, values, selection, selectionArgs);
                break;
            }
            default: {
                mSyncToNetwork = true;
                return mLegacyApiSupport.update(uri, values, selection, selectionArgs);
            }
        }
        return count;
    }
    private int updateStatusUpdate(Uri uri, ContentValues values, String selection,
        String[] selectionArgs) {
        int updateCount = 0;
        ContentValues settableValues = getSettableColumnsForStatusUpdatesTable(values);
        if (settableValues.size() > 0) {
          updateCount = mDb.update(Tables.STATUS_UPDATES,
                    settableValues,
                    getWhereClauseForStatusUpdatesTable(selection),
                    selectionArgs);
        }
        settableValues = getSettableColumnsForPresenceTable(values);
        if (settableValues.size() > 0) {
          updateCount = mDb.update(Tables.PRESENCE, settableValues,
                    selection, selectionArgs);
        }
        return updateCount;
    }
    private String getWhereClauseForStatusUpdatesTable(String selection) {
        mSb.setLength(0);
        mSb.append(WHERE_CLAUSE_FOR_STATUS_UPDATES_TABLE);
        mSb.append(selection);
        mSb.append(")");
        return mSb.toString();
    }
    private ContentValues getSettableColumnsForStatusUpdatesTable(ContentValues values) {
        mValues.clear();
        ContactsDatabaseHelper.copyStringValue(mValues, StatusUpdates.STATUS, values,
            StatusUpdates.STATUS);
        ContactsDatabaseHelper.copyStringValue(mValues, StatusUpdates.STATUS_TIMESTAMP, values,
            StatusUpdates.STATUS_TIMESTAMP);
        ContactsDatabaseHelper.copyStringValue(mValues, StatusUpdates.STATUS_RES_PACKAGE, values,
            StatusUpdates.STATUS_RES_PACKAGE);
        ContactsDatabaseHelper.copyStringValue(mValues, StatusUpdates.STATUS_LABEL, values,
            StatusUpdates.STATUS_LABEL);
        ContactsDatabaseHelper.copyStringValue(mValues, StatusUpdates.STATUS_ICON, values,
            StatusUpdates.STATUS_ICON);
        return mValues;
    }
    private ContentValues getSettableColumnsForPresenceTable(ContentValues values) {
        mValues.clear();
        ContactsDatabaseHelper.copyStringValue(mValues, StatusUpdates.PRESENCE, values,
            StatusUpdates.PRESENCE);
        return mValues;
    }
    private int updateGroups(Uri uri, ContentValues values, String selectionWithId,
            String[] selectionArgs, boolean callerIsSyncAdapter) {
        mGroupIdCache.clear();
        ContentValues updatedValues;
        if (!callerIsSyncAdapter && !values.containsKey(Groups.DIRTY)) {
            updatedValues = mValues;
            updatedValues.clear();
            updatedValues.putAll(values);
            updatedValues.put(Groups.DIRTY, 1);
        } else {
            updatedValues = values;
        }
        int count = mDb.update(Tables.GROUPS, updatedValues, selectionWithId, selectionArgs);
        if (updatedValues.containsKey(Groups.GROUP_VISIBLE)) {
            mVisibleTouched = true;
        }
        if (updatedValues.containsKey(Groups.SHOULD_SYNC)
                && updatedValues.getAsInteger(Groups.SHOULD_SYNC) != 0) {
            Cursor c = mDb.query(Tables.GROUPS, new String[]{Groups.ACCOUNT_NAME,
                    Groups.ACCOUNT_TYPE}, selectionWithId, selectionArgs, null,
                    null, null);
            String accountName;
            String accountType;
            try {
                while (c.moveToNext()) {
                    accountName = c.getString(0);
                    accountType = c.getString(1);
                    if(!TextUtils.isEmpty(accountName) && !TextUtils.isEmpty(accountType)) {
                        Account account = new Account(accountName, accountType);
                        ContentResolver.requestSync(account, ContactsContract.AUTHORITY,
                                new Bundle());
                        break;
                    }
                }
            } finally {
                c.close();
            }
        }
        return count;
    }
    private int updateSettings(Uri uri, ContentValues values, String selection,
            String[] selectionArgs) {
        final int count = mDb.update(Tables.SETTINGS, values, selection, selectionArgs);
        if (values.containsKey(Settings.UNGROUPED_VISIBLE)) {
            mVisibleTouched = true;
        }
        return count;
    }
    private int updateRawContacts(ContentValues values, String selection, String[] selectionArgs) {
        if (values.containsKey(RawContacts.CONTACT_ID)) {
            throw new IllegalArgumentException(RawContacts.CONTACT_ID + " should not be included " +
                    "in content values. Contact IDs are assigned automatically");
        }
        int count = 0;
        Cursor cursor = mDb.query(mDbHelper.getRawContactView(),
                new String[] { RawContacts._ID }, selection,
                selectionArgs, null, null, null);
        try {
            while (cursor.moveToNext()) {
                long rawContactId = cursor.getLong(0);
                updateRawContact(rawContactId, values);
                count++;
            }
        } finally {
            cursor.close();
        }
        return count;
    }
    private int updateRawContact(long rawContactId, ContentValues values) {
        final String selection = RawContacts._ID + " = ?";
        mSelectionArgs1[0] = Long.toString(rawContactId);
        final boolean requestUndoDelete = (values.containsKey(RawContacts.DELETED)
                && values.getAsInteger(RawContacts.DELETED) == 0);
        int previousDeleted = 0;
        String accountType = null;
        String accountName = null;
        if (requestUndoDelete) {
            Cursor cursor = mDb.query(RawContactsQuery.TABLE, RawContactsQuery.COLUMNS, selection,
                    mSelectionArgs1, null, null, null);
            try {
                if (cursor.moveToFirst()) {
                    previousDeleted = cursor.getInt(RawContactsQuery.DELETED);
                    accountType = cursor.getString(RawContactsQuery.ACCOUNT_TYPE);
                    accountName = cursor.getString(RawContactsQuery.ACCOUNT_NAME);
                }
            } finally {
                cursor.close();
            }
            values.put(ContactsContract.RawContacts.AGGREGATION_MODE,
                    ContactsContract.RawContacts.AGGREGATION_MODE_DEFAULT);
        }
        int count = mDb.update(Tables.RAW_CONTACTS, values, selection, mSelectionArgs1);
        if (count != 0) {
            if (values.containsKey(RawContacts.AGGREGATION_MODE)) {
                int aggregationMode = values.getAsInteger(RawContacts.AGGREGATION_MODE);
                if (aggregationMode != RawContacts.AGGREGATION_MODE_DEFAULT) {
                    mContactAggregator.markForAggregation(rawContactId, aggregationMode, false);
                }
            }
            if (values.containsKey(RawContacts.STARRED)) {
                mContactAggregator.updateStarred(rawContactId);
            }
            if (values.containsKey(RawContacts.SOURCE_ID)) {
                mContactAggregator.updateLookupKeyForRawContact(mDb, rawContactId);
            }
            if (values.containsKey(RawContacts.NAME_VERIFIED)) {
                if (values.getAsInteger(RawContacts.NAME_VERIFIED) != 0) {
                    mResetNameVerifiedForOtherRawContacts.bindLong(1, rawContactId);
                    mResetNameVerifiedForOtherRawContacts.bindLong(2, rawContactId);
                    mResetNameVerifiedForOtherRawContacts.execute();
                }
                mContactAggregator.updateDisplayNameForRawContact(mDb, rawContactId);
            }
            if (requestUndoDelete && previousDeleted == 1) {
                mInsertedRawContacts.put(rawContactId, new Account(accountName, accountType));
            }
        }
        return count;
    }
    private int updateData(Uri uri, ContentValues values, String selection,
            String[] selectionArgs, boolean callerIsSyncAdapter) {
        mValues.clear();
        mValues.putAll(values);
        mValues.remove(Data._ID);
        mValues.remove(Data.RAW_CONTACT_ID);
        mValues.remove(Data.MIMETYPE);
        String packageName = values.getAsString(Data.RES_PACKAGE);
        if (packageName != null) {
            mValues.remove(Data.RES_PACKAGE);
            mValues.put(DataColumns.PACKAGE_ID, mDbHelper.getPackageId(packageName));
        }
        boolean containsIsSuperPrimary = mValues.containsKey(Data.IS_SUPER_PRIMARY);
        boolean containsIsPrimary = mValues.containsKey(Data.IS_PRIMARY);
        if (containsIsSuperPrimary && mValues.getAsInteger(Data.IS_SUPER_PRIMARY) == 0) {
            containsIsSuperPrimary = false;
            mValues.remove(Data.IS_SUPER_PRIMARY);
        }
        if (containsIsPrimary && mValues.getAsInteger(Data.IS_PRIMARY) == 0) {
            containsIsPrimary = false;
            mValues.remove(Data.IS_PRIMARY);
        }
        int count = 0;
        Cursor c = query(uri, DataUpdateQuery.COLUMNS, selection, selectionArgs, null);
        try {
            while(c.moveToNext()) {
                count += updateData(mValues, c, callerIsSyncAdapter);
            }
        } finally {
            c.close();
        }
        return count;
    }
    private int updateData(ContentValues values, Cursor c, boolean callerIsSyncAdapter) {
        if (values.size() == 0) {
            return 0;
        }
        final String mimeType = c.getString(DataUpdateQuery.MIMETYPE);
        DataRowHandler rowHandler = getDataRowHandler(mimeType);
        if (rowHandler.update(mDb, values, c, callerIsSyncAdapter)) {
            return 1;
        } else {
            return 0;
        }
    }
    private int updateContactOptions(ContentValues values, String selection,
            String[] selectionArgs) {
        int count = 0;
        Cursor cursor = mDb.query(mDbHelper.getContactView(),
                new String[] { Contacts._ID }, selection,
                selectionArgs, null, null, null);
        try {
            while (cursor.moveToNext()) {
                long contactId = cursor.getLong(0);
                updateContactOptions(contactId, values);
                count++;
            }
        } finally {
            cursor.close();
        }
        return count;
    }
    private int updateContactOptions(long contactId, ContentValues values) {
        mValues.clear();
        ContactsDatabaseHelper.copyStringValue(mValues, RawContacts.CUSTOM_RINGTONE,
                values, Contacts.CUSTOM_RINGTONE);
        ContactsDatabaseHelper.copyLongValue(mValues, RawContacts.SEND_TO_VOICEMAIL,
                values, Contacts.SEND_TO_VOICEMAIL);
        ContactsDatabaseHelper.copyLongValue(mValues, RawContacts.LAST_TIME_CONTACTED,
                values, Contacts.LAST_TIME_CONTACTED);
        ContactsDatabaseHelper.copyLongValue(mValues, RawContacts.TIMES_CONTACTED,
                values, Contacts.TIMES_CONTACTED);
        ContactsDatabaseHelper.copyLongValue(mValues, RawContacts.STARRED,
                values, Contacts.STARRED);
        if (mValues.size() == 0) {
            return 0;
        }
        if (mValues.containsKey(RawContacts.STARRED)) {
            mValues.put(RawContacts.DIRTY, 1);
        }
        mSelectionArgs1[0] = String.valueOf(contactId);
        mDb.update(Tables.RAW_CONTACTS, mValues, RawContacts.CONTACT_ID + "=?", mSelectionArgs1);
        mValues.clear();
        ContactsDatabaseHelper.copyStringValue(mValues, RawContacts.CUSTOM_RINGTONE,
                values, Contacts.CUSTOM_RINGTONE);
        ContactsDatabaseHelper.copyLongValue(mValues, RawContacts.SEND_TO_VOICEMAIL,
                values, Contacts.SEND_TO_VOICEMAIL);
        ContactsDatabaseHelper.copyLongValue(mValues, RawContacts.LAST_TIME_CONTACTED,
                values, Contacts.LAST_TIME_CONTACTED);
        ContactsDatabaseHelper.copyLongValue(mValues, RawContacts.TIMES_CONTACTED,
                values, Contacts.TIMES_CONTACTED);
        ContactsDatabaseHelper.copyLongValue(mValues, RawContacts.STARRED,
                values, Contacts.STARRED);
        int rslt = mDb.update(Tables.CONTACTS, mValues, Contacts._ID + "=?", mSelectionArgs1);
        if (values.containsKey(Contacts.LAST_TIME_CONTACTED) &&
                !values.containsKey(Contacts.TIMES_CONTACTED)) {
            mDb.execSQL(UPDATE_TIMES_CONTACTED_CONTACTS_TABLE, mSelectionArgs1);
            mDb.execSQL(UPDATE_TIMES_CONTACTED_RAWCONTACTS_TABLE, mSelectionArgs1);
        }
        return rslt;
    }
    private int updateAggregationException(SQLiteDatabase db, ContentValues values) {
        int exceptionType = values.getAsInteger(AggregationExceptions.TYPE);
        long rcId1 = values.getAsInteger(AggregationExceptions.RAW_CONTACT_ID1);
        long rcId2 = values.getAsInteger(AggregationExceptions.RAW_CONTACT_ID2);
        long rawContactId1, rawContactId2;
        if (rcId1 < rcId2) {
            rawContactId1 = rcId1;
            rawContactId2 = rcId2;
        } else {
            rawContactId2 = rcId1;
            rawContactId1 = rcId2;
        }
        if (exceptionType == AggregationExceptions.TYPE_AUTOMATIC) {
            mSelectionArgs2[0] = String.valueOf(rawContactId1);
            mSelectionArgs2[1] = String.valueOf(rawContactId2);
            db.delete(Tables.AGGREGATION_EXCEPTIONS,
                    AggregationExceptions.RAW_CONTACT_ID1 + "=? AND "
                    + AggregationExceptions.RAW_CONTACT_ID2 + "=?", mSelectionArgs2);
        } else {
            ContentValues exceptionValues = new ContentValues(3);
            exceptionValues.put(AggregationExceptions.TYPE, exceptionType);
            exceptionValues.put(AggregationExceptions.RAW_CONTACT_ID1, rawContactId1);
            exceptionValues.put(AggregationExceptions.RAW_CONTACT_ID2, rawContactId2);
            db.replace(Tables.AGGREGATION_EXCEPTIONS, AggregationExceptions._ID,
                    exceptionValues);
        }
        mContactAggregator.invalidateAggregationExceptionCache();
        mContactAggregator.markForAggregation(rawContactId1,
                RawContacts.AGGREGATION_MODE_DEFAULT, true);
        mContactAggregator.markForAggregation(rawContactId2,
                RawContacts.AGGREGATION_MODE_DEFAULT, true);
        long contactId1 = mDbHelper.getContactId(rawContactId1);
        mContactAggregator.aggregateContact(db, rawContactId1, contactId1);
        long contactId2 = mDbHelper.getContactId(rawContactId2);
        mContactAggregator.aggregateContact(db, rawContactId2, contactId2);
        return 1;
    }
    private long getOrCreateMyContactsGroupInTransaction(String accountName, String accountType) {
        Cursor cursor = mDb.query(Tables.GROUPS, new String[] {"_id"},
                Groups.ACCOUNT_NAME + " =? AND " + Groups.ACCOUNT_TYPE + " =? AND "
                    + Groups.TITLE + " =?",
                new String[] {accountName, accountType, GOOGLE_MY_CONTACTS_GROUP_TITLE},
                null, null, null);
        try {
            if(cursor.moveToNext()) {
                return cursor.getLong(0);
            }
        } finally {
            cursor.close();
        }
        ContentValues values = new ContentValues();
        values.put(Groups.TITLE, GOOGLE_MY_CONTACTS_GROUP_TITLE);
        values.put(Groups.ACCOUNT_NAME, accountName);
        values.put(Groups.ACCOUNT_TYPE, accountType);
        values.put(Groups.GROUP_VISIBLE, "1");
        return mDb.insert(Tables.GROUPS, null, values);
    }
    public void onAccountsUpdated(Account[] accounts) {
        HashSet<Account> existingAccounts = new HashSet<Account>();
        boolean hasUnassignedContacts[] = new boolean[]{false};
        mDb.beginTransaction();
        try {
            findValidAccounts(existingAccounts, hasUnassignedContacts);
            for (Account account : accounts) {
                if (!existingAccounts.contains(account)) {
                    mDb.execSQL("INSERT INTO " + Tables.ACCOUNTS + " (" + RawContacts.ACCOUNT_NAME
                            + ", " + RawContacts.ACCOUNT_TYPE + ") VALUES (?, ?)",
                            new String[] {account.name, account.type});
                }
            }
            HashSet<Account> accountsToDelete = new HashSet<Account>(existingAccounts);
            for (Account account : accounts) {
                accountsToDelete.remove(account);
            }
            for (Account account : accountsToDelete) {
                Log.d(TAG, "removing data for removed account " + account);
                String[] params = new String[] {account.name, account.type};
                mDb.execSQL(
                        "DELETE FROM " + Tables.GROUPS +
                        " WHERE " + Groups.ACCOUNT_NAME + " = ?" +
                                " AND " + Groups.ACCOUNT_TYPE + " = ?", params);
                mDb.execSQL(
                        "DELETE FROM " + Tables.PRESENCE +
                        " WHERE " + PresenceColumns.RAW_CONTACT_ID + " IN (" +
                                "SELECT " + RawContacts._ID +
                                " FROM " + Tables.RAW_CONTACTS +
                                " WHERE " + RawContacts.ACCOUNT_NAME + " = ?" +
                                " AND " + RawContacts.ACCOUNT_TYPE + " = ?)", params);
                mDb.execSQL(
                        "DELETE FROM " + Tables.RAW_CONTACTS +
                        " WHERE " + RawContacts.ACCOUNT_NAME + " = ?" +
                        " AND " + RawContacts.ACCOUNT_TYPE + " = ?", params);
                mDb.execSQL(
                        "DELETE FROM " + Tables.SETTINGS +
                        " WHERE " + Settings.ACCOUNT_NAME + " = ?" +
                        " AND " + Settings.ACCOUNT_TYPE + " = ?", params);
                mDb.execSQL(
                        "DELETE FROM " + Tables.ACCOUNTS +
                        " WHERE " + RawContacts.ACCOUNT_NAME + "=?" +
                        " AND " + RawContacts.ACCOUNT_TYPE + "=?", params);
            }
            if (!accountsToDelete.isEmpty()) {
                HashSet<Long> orphanContactIds = Sets.newHashSet();
                Cursor cursor = mDb.rawQuery("SELECT " + Contacts._ID +
                        " FROM " + Tables.CONTACTS +
                        " WHERE (" + Contacts.NAME_RAW_CONTACT_ID + " NOT NULL AND " +
                                Contacts.NAME_RAW_CONTACT_ID + " NOT IN " +
                                        "(SELECT " + RawContacts._ID +
                                        " FROM " + Tables.RAW_CONTACTS + "))" +
                        " OR (" + Contacts.PHOTO_ID + " NOT NULL AND " +
                                Contacts.PHOTO_ID + " NOT IN " +
                                        "(SELECT " + Data._ID +
                                        " FROM " + Tables.DATA + "))", null);
                try {
                    while (cursor.moveToNext()) {
                        orphanContactIds.add(cursor.getLong(0));
                    }
                } finally {
                    cursor.close();
                }
                for (Long contactId : orphanContactIds) {
                    mContactAggregator.updateAggregateData(contactId);
                }
            }
            if (hasUnassignedContacts[0]) {
                Account primaryAccount = null;
                for (Account account : accounts) {
                    if (isWritableAccount(account.type)) {
                        primaryAccount = account;
                        break;
                    }
                }
                if (primaryAccount != null) {
                    String[] params = new String[] {primaryAccount.name, primaryAccount.type};
                    if (primaryAccount.type.equals(DEFAULT_ACCOUNT_TYPE)) {
                        long groupId = getOrCreateMyContactsGroupInTransaction(
                                primaryAccount.name, primaryAccount.type);
                        if (groupId != -1) {
                            long mimeTypeId = mDbHelper.getMimeTypeId(
                                    GroupMembership.CONTENT_ITEM_TYPE);
                            mDb.execSQL(
                                    "INSERT INTO " + Tables.DATA + "(" + DataColumns.MIMETYPE_ID +
                                        ", " + Data.RAW_CONTACT_ID + ", "
                                        + GroupMembership.GROUP_ROW_ID + ") " +
                                    "SELECT " + mimeTypeId + ", "
                                            + RawContacts._ID + ", " + groupId +
                                    " FROM " + Tables.RAW_CONTACTS +
                                    " WHERE " + RawContacts.ACCOUNT_NAME + " IS NULL" +
                                    " AND " + RawContacts.ACCOUNT_TYPE + " IS NULL"
                            );
                        }
                    }
                    mDb.execSQL(
                            "UPDATE " + Tables.RAW_CONTACTS +
                            " SET " + RawContacts.ACCOUNT_NAME + "=?,"
                                    + RawContacts.ACCOUNT_TYPE + "=?" +
                            " WHERE " + RawContacts.ACCOUNT_NAME + " IS NULL" +
                            " AND " + RawContacts.ACCOUNT_TYPE + " IS NULL", params);
                    mDb.execSQL(
                            "UPDATE " + Tables.GROUPS +
                            " SET " + Groups.ACCOUNT_NAME + "=?,"
                                    + Groups.ACCOUNT_TYPE + "=?" +
                            " WHERE " + Groups.ACCOUNT_NAME + " IS NULL" +
                            " AND " + Groups.ACCOUNT_TYPE + " IS NULL", params);
                    mDb.execSQL(
                            "DELETE FROM " + Tables.ACCOUNTS +
                            " WHERE " + RawContacts.ACCOUNT_NAME + " IS NULL" +
                            " AND " + RawContacts.ACCOUNT_TYPE + " IS NULL");
                }
            }
            mDbHelper.updateAllVisible();
            mDbHelper.getSyncState().onAccountsChanged(mDb, accounts);
            mDb.setTransactionSuccessful();
        } finally {
            mDb.endTransaction();
        }
        mAccountWritability.clear();
    }
    private void findValidAccounts(Set<Account> validAccounts, boolean[] hasUnassignedContacts) {
        Cursor c = mDb.rawQuery(
                "SELECT " + RawContacts.ACCOUNT_NAME + "," + RawContacts.ACCOUNT_TYPE +
                " FROM " + Tables.ACCOUNTS, null);
        try {
            while (c.moveToNext()) {
                if (c.isNull(0) && c.isNull(1)) {
                    hasUnassignedContacts[0] = true;
                } else {
                    validAccounts.add(new Account(c.getString(0), c.getString(1)));
                }
            }
        } finally {
            c.close();
        }
    }
    private static boolean areAllEmpty(ContentValues values, String[] keys) {
        for (String key : keys) {
            if (!TextUtils.isEmpty(values.getAsString(key))) {
                return false;
            }
        }
        return true;
    }
    private static boolean areAnySpecified(ContentValues values, String[] keys) {
        for (String key : keys) {
            if (values.containsKey(key)) {
                return true;
            }
        }
        return false;
    }
    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs,
            String sortOrder) {
        if (VERBOSE_LOGGING) {
            Log.v(TAG, "query: " + uri);
        }
        final SQLiteDatabase db = mDbHelper.getReadableDatabase();
        SQLiteQueryBuilder qb = new SQLiteQueryBuilder();
        String groupBy = null;
        String limit = getLimit(uri);
        final int match = sUriMatcher.match(uri);
        switch (match) {
            case SYNCSTATE:
                return mDbHelper.getSyncState().query(db, projection, selection,  selectionArgs,
                        sortOrder);
            case CONTACTS: {
                setTablesAndProjectionMapForContacts(qb, uri, projection);
                break;
            }
            case CONTACTS_ID: {
                long contactId = ContentUris.parseId(uri);
                setTablesAndProjectionMapForContacts(qb, uri, projection);
                selectionArgs = insertSelectionArg(selectionArgs, String.valueOf(contactId));
                qb.appendWhere(Contacts._ID + "=?");
                break;
            }
            case CONTACTS_LOOKUP:
            case CONTACTS_LOOKUP_ID: {
                List<String> pathSegments = uri.getPathSegments();
                int segmentCount = pathSegments.size();
                if (segmentCount < 3) {
                    throw new IllegalArgumentException(mDbHelper.exceptionMessage(
                            "Missing a lookup key", uri));
                }
                String lookupKey = pathSegments.get(2);
                if (segmentCount == 4) {
                    long contactId = Long.parseLong(pathSegments.get(3));
                    SQLiteQueryBuilder lookupQb = new SQLiteQueryBuilder();
                    setTablesAndProjectionMapForContacts(lookupQb, uri, projection);
                    String[] args;
                    if (selectionArgs == null) {
                        args = new String[2];
                    } else {
                        args = new String[selectionArgs.length + 2];
                        System.arraycopy(selectionArgs, 0, args, 2, selectionArgs.length);
                    }
                    args[0] = String.valueOf(contactId);
                    args[1] = Uri.encode(lookupKey);
                    lookupQb.appendWhere(Contacts._ID + "=? AND " + Contacts.LOOKUP_KEY + "=?");
                    Cursor c = query(db, lookupQb, projection, selection, args, sortOrder,
                            groupBy, limit);
                    if (c.getCount() != 0) {
                        return c;
                    }
                    c.close();
                }
                setTablesAndProjectionMapForContacts(qb, uri, projection);
                selectionArgs = insertSelectionArg(selectionArgs,
                        String.valueOf(lookupContactIdByLookupKey(db, lookupKey)));
                qb.appendWhere(Contacts._ID + "=?");
                break;
            }
            case CONTACTS_AS_VCARD: {
                final String lookupKey = Uri.encode(uri.getPathSegments().get(2));
                qb.setTables(mDbHelper.getContactView(true ));
                qb.setProjectionMap(sContactsVCardProjectionMap);
                selectionArgs = insertSelectionArg(selectionArgs,
                        String.valueOf(lookupContactIdByLookupKey(db, lookupKey)));
                qb.appendWhere(Contacts._ID + "=?");
                break;
            }
            case CONTACTS_AS_MULTI_VCARD: {
                SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMdd_HHmmss");
                String currentDateString = dateFormat.format(new Date()).toString();
                return db.rawQuery(
                    "SELECT" +
                    " 'vcards_' || ? || '.vcf' AS " + OpenableColumns.DISPLAY_NAME + "," +
                    " NULL AS " + OpenableColumns.SIZE,
                    new String[] { currentDateString });
            }
            case CONTACTS_FILTER: {
                String filterParam = "";
                if (uri.getPathSegments().size() > 2) {
                    filterParam = uri.getLastPathSegment();
                }
                setTablesAndProjectionMapForContactsWithSnippet(qb, uri, projection, filterParam);
                break;
            }
            case CONTACTS_STREQUENT_FILTER:
            case CONTACTS_STREQUENT: {
                String filterSql = null;
                if (match == CONTACTS_STREQUENT_FILTER
                        && uri.getPathSegments().size() > 3) {
                    String filterParam = uri.getLastPathSegment();
                    StringBuilder sb = new StringBuilder();
                    sb.append(Contacts._ID + " IN ");
                    appendContactFilterAsNestedQuery(sb, filterParam);
                    filterSql = sb.toString();
                }
                setTablesAndProjectionMapForContacts(qb, uri, projection);
                String[] starredProjection = null;
                String[] frequentProjection = null;
                if (projection != null) {
                    starredProjection = appendProjectionArg(projection, TIMES_CONTACED_SORT_COLUMN);
                    frequentProjection = appendProjectionArg(projection, TIMES_CONTACED_SORT_COLUMN);
                }
                if (filterSql != null) {
                    qb.appendWhere(filterSql);
                }
                qb.setProjectionMap(sStrequentStarredProjectionMap);
                final String starredQuery = qb.buildQuery(starredProjection, Contacts.STARRED + "=1",
                        null, Contacts._ID, null, null, null);
                qb = new SQLiteQueryBuilder();
                setTablesAndProjectionMapForContacts(qb, uri, projection);
                if (filterSql != null) {
                    qb.appendWhere(filterSql);
                }
                qb.setProjectionMap(sStrequentFrequentProjectionMap);
                final String frequentQuery = qb.buildQuery(frequentProjection,
                        Contacts.TIMES_CONTACTED + " > 0 AND (" + Contacts.STARRED
                        + " = 0 OR " + Contacts.STARRED + " IS NULL)",
                        null, Contacts._ID, null, null, null);
                final String query = qb.buildUnionQuery(new String[] {starredQuery, frequentQuery},
                        STREQUENT_ORDER_BY, STREQUENT_LIMIT);
                Cursor c = db.rawQuery(query, null);
                if (c != null) {
                    c.setNotificationUri(getContext().getContentResolver(),
                            ContactsContract.AUTHORITY_URI);
                }
                return c;
            }
            case CONTACTS_GROUP: {
                setTablesAndProjectionMapForContacts(qb, uri, projection);
                if (uri.getPathSegments().size() > 2) {
                    qb.appendWhere(CONTACTS_IN_GROUP_SELECT);
                    selectionArgs = insertSelectionArg(selectionArgs, uri.getLastPathSegment());
                }
                break;
            }
            case CONTACTS_DATA: {
                long contactId = Long.parseLong(uri.getPathSegments().get(1));
                setTablesAndProjectionMapForData(qb, uri, projection, false);
                selectionArgs = insertSelectionArg(selectionArgs, String.valueOf(contactId));
                qb.appendWhere(" AND " + RawContacts.CONTACT_ID + "=?");
                break;
            }
            case CONTACTS_PHOTO: {
                long contactId = Long.parseLong(uri.getPathSegments().get(1));
                setTablesAndProjectionMapForData(qb, uri, projection, false);
                selectionArgs = insertSelectionArg(selectionArgs, String.valueOf(contactId));
                qb.appendWhere(" AND " + RawContacts.CONTACT_ID + "=?");
                qb.appendWhere(" AND " + Data._ID + "=" + Contacts.PHOTO_ID);
                break;
            }
            case PHONES: {
                setTablesAndProjectionMapForData(qb, uri, projection, false);
                qb.appendWhere(" AND " + Data.MIMETYPE + " = '" + Phone.CONTENT_ITEM_TYPE + "'");
                break;
            }
            case PHONES_ID: {
                setTablesAndProjectionMapForData(qb, uri, projection, false);
                selectionArgs = insertSelectionArg(selectionArgs, uri.getLastPathSegment());
                qb.appendWhere(" AND " + Data.MIMETYPE + " = '" + Phone.CONTENT_ITEM_TYPE + "'");
                qb.appendWhere(" AND " + Data._ID + "=?");
                break;
            }
            case PHONES_FILTER: {
                setTablesAndProjectionMapForData(qb, uri, projection, true);
                qb.appendWhere(" AND " + Data.MIMETYPE + " = '" + Phone.CONTENT_ITEM_TYPE + "'");
                if (uri.getPathSegments().size() > 2) {
                    String filterParam = uri.getLastPathSegment();
                    StringBuilder sb = new StringBuilder();
                    sb.append(" AND (");
                    boolean hasCondition = false;
                    boolean orNeeded = false;
                    String normalizedName = NameNormalizer.normalize(filterParam);
                    if (normalizedName.length() > 0) {
                        sb.append(Data.RAW_CONTACT_ID + " IN ");
                        appendRawContactsByNormalizedNameFilter(sb, normalizedName, false);
                        orNeeded = true;
                        hasCondition = true;
                    }
                    if (isPhoneNumber(filterParam)) {
                        if (orNeeded) {
                            sb.append(" OR ");
                        }
                        String number = PhoneNumberUtils.convertKeypadLettersToDigits(filterParam);
                        String reversed = PhoneNumberUtils.getStrippedReversed(number);
                        sb.append(Data._ID +
                                " IN (SELECT " + PhoneLookupColumns.DATA_ID
                                  + " FROM " + Tables.PHONE_LOOKUP
                                  + " WHERE " + PhoneLookupColumns.NORMALIZED_NUMBER + " LIKE '%");
                        sb.append(reversed);
                        sb.append("')");
                        hasCondition = true;
                    }
                    if (!hasCondition) {
                        sb.append("0");
                    }
                    sb.append(")");
                    qb.appendWhere(sb);
                }
                groupBy = PhoneColumns.NORMALIZED_NUMBER + "," + RawContacts.CONTACT_ID;
                if (sortOrder == null) {
                    sortOrder = Contacts.IN_VISIBLE_GROUP + " DESC, " + RawContacts.CONTACT_ID;
                }
                break;
            }
            case EMAILS: {
                setTablesAndProjectionMapForData(qb, uri, projection, false);
                qb.appendWhere(" AND " + Data.MIMETYPE + " = '" + Email.CONTENT_ITEM_TYPE + "'");
                break;
            }
            case EMAILS_ID: {
                setTablesAndProjectionMapForData(qb, uri, projection, false);
                selectionArgs = insertSelectionArg(selectionArgs, uri.getLastPathSegment());
                qb.appendWhere(" AND " + Data.MIMETYPE + " = '" + Email.CONTENT_ITEM_TYPE + "'"
                        + " AND " + Data._ID + "=?");
                break;
            }
            case EMAILS_LOOKUP: {
                setTablesAndProjectionMapForData(qb, uri, projection, false);
                qb.appendWhere(" AND " + Data.MIMETYPE + " = '" + Email.CONTENT_ITEM_TYPE + "'");
                if (uri.getPathSegments().size() > 2) {
                    String email = uri.getLastPathSegment();
                    String address = mDbHelper.extractAddressFromEmailAddress(email);
                    selectionArgs = insertSelectionArg(selectionArgs, address);
                    qb.appendWhere(" AND UPPER(" + Email.DATA + ")=UPPER(?)");
                }
                break;
            }
            case EMAILS_FILTER: {
                setTablesAndProjectionMapForData(qb, uri, projection, true);
                String filterParam = null;
                if (uri.getPathSegments().size() > 3) {
                    filterParam = uri.getLastPathSegment();
                    if (TextUtils.isEmpty(filterParam)) {
                        filterParam = null;
                    }
                }
                if (filterParam == null) {
                    qb.appendWhere(" AND 0");
                } else {
                    StringBuilder sb = new StringBuilder();
                    sb.append(" AND " + Data._ID + " IN (");
                    sb.append(
                            "SELECT " + Data._ID +
                            " FROM " + Tables.DATA +
                            " WHERE " + DataColumns.MIMETYPE_ID + "=" + mMimeTypeIdEmail +
                            " AND " + Data.DATA1 + " LIKE ");
                    DatabaseUtils.appendEscapedSQLString(sb, filterParam + '%');
                    if (!filterParam.contains("@")) {
                        String normalizedName = NameNormalizer.normalize(filterParam);
                        if (normalizedName.length() > 0) {
                            sb.append(
                                    " UNION SELECT " + Data._ID +
                                    " FROM " + Tables.DATA +
                                    " WHERE +" + DataColumns.MIMETYPE_ID + "=" + mMimeTypeIdEmail +
                                    " AND " + Data.RAW_CONTACT_ID + " IN ");
                            appendRawContactsByNormalizedNameFilter(sb, normalizedName, false);
                        }
                    }
                    sb.append(")");
                    qb.appendWhere(sb);
                }
                groupBy = Email.DATA + "," + RawContacts.CONTACT_ID;
                if (sortOrder == null) {
                    sortOrder = Contacts.IN_VISIBLE_GROUP + " DESC, " + RawContacts.CONTACT_ID;
                }
                break;
            }
            case POSTALS: {
                setTablesAndProjectionMapForData(qb, uri, projection, false);
                qb.appendWhere(" AND " + Data.MIMETYPE + " = '"
                        + StructuredPostal.CONTENT_ITEM_TYPE + "'");
                break;
            }
            case POSTALS_ID: {
                setTablesAndProjectionMapForData(qb, uri, projection, false);
                selectionArgs = insertSelectionArg(selectionArgs, uri.getLastPathSegment());
                qb.appendWhere(" AND " + Data.MIMETYPE + " = '"
                        + StructuredPostal.CONTENT_ITEM_TYPE + "'");
                qb.appendWhere(" AND " + Data._ID + "=?");
                break;
            }
            case RAW_CONTACTS: {
                setTablesAndProjectionMapForRawContacts(qb, uri);
                break;
            }
            case RAW_CONTACTS_ID: {
                long rawContactId = ContentUris.parseId(uri);
                setTablesAndProjectionMapForRawContacts(qb, uri);
                selectionArgs = insertSelectionArg(selectionArgs, String.valueOf(rawContactId));
                qb.appendWhere(" AND " + RawContacts._ID + "=?");
                break;
            }
            case RAW_CONTACTS_DATA: {
                long rawContactId = Long.parseLong(uri.getPathSegments().get(1));
                setTablesAndProjectionMapForData(qb, uri, projection, false);
                selectionArgs = insertSelectionArg(selectionArgs, String.valueOf(rawContactId));
                qb.appendWhere(" AND " + Data.RAW_CONTACT_ID + "=?");
                break;
            }
            case DATA: {
                setTablesAndProjectionMapForData(qb, uri, projection, false);
                break;
            }
            case DATA_ID: {
                setTablesAndProjectionMapForData(qb, uri, projection, false);
                selectionArgs = insertSelectionArg(selectionArgs, uri.getLastPathSegment());
                qb.appendWhere(" AND " + Data._ID + "=?");
                break;
            }
            case PHONE_LOOKUP: {
                if (TextUtils.isEmpty(sortOrder)) {
                    sortOrder = RawContactsColumns.CONCRETE_ID;
                }
                String number = uri.getPathSegments().size() > 1 ? uri.getLastPathSegment() : "";
                mDbHelper.buildPhoneLookupAndContactQuery(qb, number);
                qb.setProjectionMap(sPhoneLookupProjectionMap);
                selection = null;
                selectionArgs = null;
                break;
            }
            case GROUPS: {
                qb.setTables(mDbHelper.getGroupView());
                qb.setProjectionMap(sGroupsProjectionMap);
                appendAccountFromParameter(qb, uri);
                break;
            }
            case GROUPS_ID: {
                qb.setTables(mDbHelper.getGroupView());
                qb.setProjectionMap(sGroupsProjectionMap);
                selectionArgs = insertSelectionArg(selectionArgs, uri.getLastPathSegment());
                qb.appendWhere(Groups._ID + "=?");
                break;
            }
            case GROUPS_SUMMARY: {
                qb.setTables(mDbHelper.getGroupView() + " AS groups");
                qb.setProjectionMap(sGroupsSummaryProjectionMap);
                appendAccountFromParameter(qb, uri);
                groupBy = Groups._ID;
                break;
            }
            case AGGREGATION_EXCEPTIONS: {
                qb.setTables(Tables.AGGREGATION_EXCEPTIONS);
                qb.setProjectionMap(sAggregationExceptionsProjectionMap);
                break;
            }
            case AGGREGATION_SUGGESTIONS: {
                long contactId = Long.parseLong(uri.getPathSegments().get(1));
                String filter = null;
                if (uri.getPathSegments().size() > 3) {
                    filter = uri.getPathSegments().get(3);
                }
                final int maxSuggestions;
                if (limit != null) {
                    maxSuggestions = Integer.parseInt(limit);
                } else {
                    maxSuggestions = DEFAULT_MAX_SUGGESTIONS;
                }
                setTablesAndProjectionMapForContacts(qb, uri, projection);
                return mContactAggregator.queryAggregationSuggestions(qb, projection, contactId,
                        maxSuggestions, filter);
            }
            case SETTINGS: {
                qb.setTables(Tables.SETTINGS);
                qb.setProjectionMap(sSettingsProjectionMap);
                appendAccountFromParameter(qb, uri);
                final String groupMembershipMimetypeId = Long.toString(mDbHelper
                        .getMimeTypeId(GroupMembership.CONTENT_ITEM_TYPE));
                if (projection != null && projection.length != 0 &&
                        mDbHelper.isInProjection(projection, Settings.UNGROUPED_COUNT)) {
                    selectionArgs = insertSelectionArg(selectionArgs, groupMembershipMimetypeId);
                }
                if (projection != null && projection.length != 0 &&
                        mDbHelper.isInProjection(projection, Settings.UNGROUPED_WITH_PHONES)) {
                    selectionArgs = insertSelectionArg(selectionArgs, groupMembershipMimetypeId);
                }
                break;
            }
            case STATUS_UPDATES: {
                setTableAndProjectionMapForStatusUpdates(qb, projection);
                break;
            }
            case STATUS_UPDATES_ID: {
                setTableAndProjectionMapForStatusUpdates(qb, projection);
                selectionArgs = insertSelectionArg(selectionArgs, uri.getLastPathSegment());
                qb.appendWhere(DataColumns.CONCRETE_ID + "=?");
                break;
            }
            case SEARCH_SUGGESTIONS: {
                return mGlobalSearchSupport.handleSearchSuggestionsQuery(db, uri, limit);
            }
            case SEARCH_SHORTCUT: {
                String lookupKey = uri.getLastPathSegment();
                return mGlobalSearchSupport.handleSearchShortcutRefresh(db, lookupKey, projection);
            }
            case LIVE_FOLDERS_CONTACTS:
                qb.setTables(mDbHelper.getContactView());
                qb.setProjectionMap(sLiveFoldersProjectionMap);
                break;
            case LIVE_FOLDERS_CONTACTS_WITH_PHONES:
                qb.setTables(mDbHelper.getContactView());
                qb.setProjectionMap(sLiveFoldersProjectionMap);
                qb.appendWhere(Contacts.HAS_PHONE_NUMBER + "=1");
                break;
            case LIVE_FOLDERS_CONTACTS_FAVORITES:
                qb.setTables(mDbHelper.getContactView());
                qb.setProjectionMap(sLiveFoldersProjectionMap);
                qb.appendWhere(Contacts.STARRED + "=1");
                break;
            case LIVE_FOLDERS_CONTACTS_GROUP_NAME:
                qb.setTables(mDbHelper.getContactView());
                qb.setProjectionMap(sLiveFoldersProjectionMap);
                qb.appendWhere(CONTACTS_IN_GROUP_SELECT);
                selectionArgs = insertSelectionArg(selectionArgs, uri.getLastPathSegment());
                break;
            case RAW_CONTACT_ENTITIES: {
                setTablesAndProjectionMapForRawContactsEntities(qb, uri);
                break;
            }
            case RAW_CONTACT_ENTITY_ID: {
                long rawContactId = Long.parseLong(uri.getPathSegments().get(1));
                setTablesAndProjectionMapForRawContactsEntities(qb, uri);
                selectionArgs = insertSelectionArg(selectionArgs, String.valueOf(rawContactId));
                qb.appendWhere(" AND " + RawContacts._ID + "=?");
                break;
            }
            case PROVIDER_STATUS: {
                return queryProviderStatus(uri, projection);
            }
            default:
                return mLegacyApiSupport.query(uri, projection, selection, selectionArgs,
                        sortOrder, limit);
        }
        qb.setStrictProjectionMap(true);
        Cursor cursor =
                query(db, qb, projection, selection, selectionArgs, sortOrder, groupBy, limit);
        if (readBooleanQueryParameter(uri, ContactCounts.ADDRESS_BOOK_INDEX_EXTRAS, false)) {
            cursor = bundleLetterCountExtras(cursor, db, qb, selection, selectionArgs, sortOrder);
        }
        return cursor;
    }
    private Cursor query(final SQLiteDatabase db, SQLiteQueryBuilder qb, String[] projection,
            String selection, String[] selectionArgs, String sortOrder, String groupBy,
            String limit) {
        if (projection != null && projection.length == 1
                && BaseColumns._COUNT.equals(projection[0])) {
            qb.setProjectionMap(sCountProjectionMap);
        }
        final Cursor c = qb.query(db, projection, selection, selectionArgs, groupBy, null,
                sortOrder, limit);
        if (c != null) {
            c.setNotificationUri(getContext().getContentResolver(), ContactsContract.AUTHORITY_URI);
        }
        return c;
    }
    private Cursor queryProviderStatus(Uri uri, String[] projection) {
        MatrixCursor cursor = new MatrixCursor(projection);
        RowBuilder row = cursor.newRow();
        for (int i = 0; i < projection.length; i++) {
            if (ProviderStatus.STATUS.equals(projection[i])) {
                row.add(mProviderStatus);
            } else if (ProviderStatus.DATA1.equals(projection[i])) {
                row.add(mEstimatedStorageRequirement);
            }
        }
        return cursor;
    }
    private static final class AddressBookIndexQuery {
        public static final String LETTER = "letter";
        public static final String TITLE = "title";
        public static final String COUNT = "count";
        public static final String[] COLUMNS = new String[] {
                LETTER, TITLE, COUNT
        };
        public static final int COLUMN_LETTER = 0;
        public static final int COLUMN_TITLE = 1;
        public static final int COLUMN_COUNT = 2;
        public static final String ORDER_BY = LETTER + " COLLATE " + PHONEBOOK_COLLATOR_NAME;
    }
    private Cursor bundleLetterCountExtras(Cursor cursor, final SQLiteDatabase db,
            SQLiteQueryBuilder qb, String selection, String[] selectionArgs, String sortOrder) {
        String sortKey;
        String sortOrderSuffix = "";
        if (sortOrder != null) {
            int spaceIndex = sortOrder.indexOf(' ');
            if (spaceIndex != -1) {
                sortKey = sortOrder.substring(0, spaceIndex);
                sortOrderSuffix = sortOrder.substring(spaceIndex);
            } else {
                sortKey = sortOrder;
            }
        } else {
            sortKey = Contacts.SORT_KEY_PRIMARY;
        }
        String locale = getLocale().toString();
        HashMap<String, String> projectionMap = Maps.newHashMap();
        projectionMap.put(AddressBookIndexQuery.LETTER,
                "SUBSTR(" + sortKey + ",1,1) AS " + AddressBookIndexQuery.LETTER);
        projectionMap.put(AddressBookIndexQuery.TITLE,
                "GET_PHONEBOOK_INDEX(SUBSTR(" + sortKey + ",1,1),'" + locale + "')"
                        + " AS " + AddressBookIndexQuery.TITLE);
        projectionMap.put(AddressBookIndexQuery.COUNT,
                "COUNT(" + Contacts._ID + ") AS " + AddressBookIndexQuery.COUNT);
        qb.setProjectionMap(projectionMap);
        Cursor indexCursor = qb.query(db, AddressBookIndexQuery.COLUMNS, selection, selectionArgs,
                AddressBookIndexQuery.ORDER_BY, null ,
                AddressBookIndexQuery.ORDER_BY + sortOrderSuffix);
        try {
            int groupCount = indexCursor.getCount();
            String titles[] = new String[groupCount];
            int counts[] = new int[groupCount];
            int indexCount = 0;
            String currentTitle = null;
            for (int i = 0; i < groupCount; i++) {
                indexCursor.moveToNext();
                String title = indexCursor.getString(AddressBookIndexQuery.COLUMN_TITLE);
                int count = indexCursor.getInt(AddressBookIndexQuery.COLUMN_COUNT);
                if (indexCount == 0 || !TextUtils.equals(title, currentTitle)) {
                    titles[indexCount] = currentTitle = title;
                    counts[indexCount] = count;
                    indexCount++;
                } else {
                    counts[indexCount - 1] += count;
                }
            }
            if (indexCount < groupCount) {
                String[] newTitles = new String[indexCount];
                System.arraycopy(titles, 0, newTitles, 0, indexCount);
                titles = newTitles;
                int[] newCounts = new int[indexCount];
                System.arraycopy(counts, 0, newCounts, 0, indexCount);
                counts = newCounts;
            }
            final Bundle bundle = new Bundle();
            bundle.putStringArray(ContactCounts.EXTRA_ADDRESS_BOOK_INDEX_TITLES, titles);
            bundle.putIntArray(ContactCounts.EXTRA_ADDRESS_BOOK_INDEX_COUNTS, counts);
            return new CursorWrapper(cursor) {
                @Override
                public Bundle getExtras() {
                    return bundle;
                }
            };
        } finally {
            indexCursor.close();
        }
    }
    public long lookupContactIdByLookupKey(SQLiteDatabase db, String lookupKey) {
        ContactLookupKey key = new ContactLookupKey();
        ArrayList<LookupKeySegment> segments = key.parse(lookupKey);
        long contactId = -1;
        if (lookupKeyContainsType(segments, ContactLookupKey.LOOKUP_TYPE_SOURCE_ID)) {
            contactId = lookupContactIdBySourceIds(db, segments);
            if (contactId != -1) {
                return contactId;
            }
        }
        boolean hasRawContactIds =
                lookupKeyContainsType(segments, ContactLookupKey.LOOKUP_TYPE_RAW_CONTACT_ID);
        if (hasRawContactIds) {
            contactId = lookupContactIdByRawContactIds(db, segments);
            if (contactId != -1) {
                return contactId;
            }
        }
        if (hasRawContactIds
                || lookupKeyContainsType(segments, ContactLookupKey.LOOKUP_TYPE_DISPLAY_NAME)) {
            contactId = lookupContactIdByDisplayNames(db, segments);
        }
        return contactId;
    }
    private interface LookupBySourceIdQuery {
        String TABLE = Tables.RAW_CONTACTS;
        String COLUMNS[] = {
                RawContacts.CONTACT_ID,
                RawContacts.ACCOUNT_TYPE,
                RawContacts.ACCOUNT_NAME,
                RawContacts.SOURCE_ID
        };
        int CONTACT_ID = 0;
        int ACCOUNT_TYPE = 1;
        int ACCOUNT_NAME = 2;
        int SOURCE_ID = 3;
    }
    private long lookupContactIdBySourceIds(SQLiteDatabase db,
                ArrayList<LookupKeySegment> segments) {
        StringBuilder sb = new StringBuilder();
        sb.append(RawContacts.SOURCE_ID + " IN (");
        for (int i = 0; i < segments.size(); i++) {
            LookupKeySegment segment = segments.get(i);
            if (segment.lookupType == ContactLookupKey.LOOKUP_TYPE_SOURCE_ID) {
                DatabaseUtils.appendEscapedSQLString(sb, segment.key);
                sb.append(",");
            }
        }
        sb.setLength(sb.length() - 1);      
        sb.append(") AND " + RawContacts.CONTACT_ID + " NOT NULL");
        Cursor c = db.query(LookupBySourceIdQuery.TABLE, LookupBySourceIdQuery.COLUMNS,
                 sb.toString(), null, null, null, null);
        try {
            while (c.moveToNext()) {
                String accountType = c.getString(LookupBySourceIdQuery.ACCOUNT_TYPE);
                String accountName = c.getString(LookupBySourceIdQuery.ACCOUNT_NAME);
                int accountHashCode =
                        ContactLookupKey.getAccountHashCode(accountType, accountName);
                String sourceId = c.getString(LookupBySourceIdQuery.SOURCE_ID);
                for (int i = 0; i < segments.size(); i++) {
                    LookupKeySegment segment = segments.get(i);
                    if (segment.lookupType == ContactLookupKey.LOOKUP_TYPE_SOURCE_ID
                            && accountHashCode == segment.accountHashCode
                            && segment.key.equals(sourceId)) {
                        segment.contactId = c.getLong(LookupBySourceIdQuery.CONTACT_ID);
                        break;
                    }
                }
            }
        } finally {
            c.close();
        }
        return getMostReferencedContactId(segments);
    }
    private interface LookupByRawContactIdQuery {
        String TABLE = Tables.RAW_CONTACTS;
        String COLUMNS[] = {
                RawContacts.CONTACT_ID,
                RawContacts.ACCOUNT_TYPE,
                RawContacts.ACCOUNT_NAME,
                RawContacts._ID,
        };
        int CONTACT_ID = 0;
        int ACCOUNT_TYPE = 1;
        int ACCOUNT_NAME = 2;
        int ID = 3;
    }
    private long lookupContactIdByRawContactIds(SQLiteDatabase db,
            ArrayList<LookupKeySegment> segments) {
        StringBuilder sb = new StringBuilder();
        sb.append(RawContacts._ID + " IN (");
        for (int i = 0; i < segments.size(); i++) {
            LookupKeySegment segment = segments.get(i);
            if (segment.lookupType == ContactLookupKey.LOOKUP_TYPE_RAW_CONTACT_ID) {
                sb.append(segment.rawContactId);
                sb.append(",");
            }
        }
        sb.setLength(sb.length() - 1);      
        sb.append(") AND " + RawContacts.CONTACT_ID + " NOT NULL");
        Cursor c = db.query(LookupByRawContactIdQuery.TABLE, LookupByRawContactIdQuery.COLUMNS,
                 sb.toString(), null, null, null, null);
        try {
            while (c.moveToNext()) {
                String accountType = c.getString(LookupByRawContactIdQuery.ACCOUNT_TYPE);
                String accountName = c.getString(LookupByRawContactIdQuery.ACCOUNT_NAME);
                int accountHashCode =
                        ContactLookupKey.getAccountHashCode(accountType, accountName);
                String rawContactId = c.getString(LookupByRawContactIdQuery.ID);
                for (int i = 0; i < segments.size(); i++) {
                    LookupKeySegment segment = segments.get(i);
                    if (segment.lookupType == ContactLookupKey.LOOKUP_TYPE_RAW_CONTACT_ID
                            && accountHashCode == segment.accountHashCode
                            && segment.rawContactId.equals(rawContactId)) {
                        segment.contactId = c.getLong(LookupByRawContactIdQuery.CONTACT_ID);
                        break;
                    }
                }
            }
        } finally {
            c.close();
        }
        return getMostReferencedContactId(segments);
    }
    private interface LookupByDisplayNameQuery {
        String TABLE = Tables.NAME_LOOKUP_JOIN_RAW_CONTACTS;
        String COLUMNS[] = {
                RawContacts.CONTACT_ID,
                RawContacts.ACCOUNT_TYPE,
                RawContacts.ACCOUNT_NAME,
                NameLookupColumns.NORMALIZED_NAME
        };
        int CONTACT_ID = 0;
        int ACCOUNT_TYPE = 1;
        int ACCOUNT_NAME = 2;
        int NORMALIZED_NAME = 3;
    }
    private long lookupContactIdByDisplayNames(SQLiteDatabase db,
                ArrayList<LookupKeySegment> segments) {
        StringBuilder sb = new StringBuilder();
        sb.append(NameLookupColumns.NORMALIZED_NAME + " IN (");
        for (int i = 0; i < segments.size(); i++) {
            LookupKeySegment segment = segments.get(i);
            if (segment.lookupType == ContactLookupKey.LOOKUP_TYPE_DISPLAY_NAME
                    || segment.lookupType == ContactLookupKey.LOOKUP_TYPE_RAW_CONTACT_ID) {
                DatabaseUtils.appendEscapedSQLString(sb, segment.key);
                sb.append(",");
            }
        }
        sb.setLength(sb.length() - 1);      
        sb.append(") AND " + NameLookupColumns.NAME_TYPE + "=" + NameLookupType.NAME_COLLATION_KEY
                + " AND " + RawContacts.CONTACT_ID + " NOT NULL");
        Cursor c = db.query(LookupByDisplayNameQuery.TABLE, LookupByDisplayNameQuery.COLUMNS,
                 sb.toString(), null, null, null, null);
        try {
            while (c.moveToNext()) {
                String accountType = c.getString(LookupByDisplayNameQuery.ACCOUNT_TYPE);
                String accountName = c.getString(LookupByDisplayNameQuery.ACCOUNT_NAME);
                int accountHashCode =
                        ContactLookupKey.getAccountHashCode(accountType, accountName);
                String name = c.getString(LookupByDisplayNameQuery.NORMALIZED_NAME);
                for (int i = 0; i < segments.size(); i++) {
                    LookupKeySegment segment = segments.get(i);
                    if ((segment.lookupType == ContactLookupKey.LOOKUP_TYPE_DISPLAY_NAME
                            || segment.lookupType == ContactLookupKey.LOOKUP_TYPE_RAW_CONTACT_ID)
                            && accountHashCode == segment.accountHashCode
                            && segment.key.equals(name)) {
                        segment.contactId = c.getLong(LookupByDisplayNameQuery.CONTACT_ID);
                        break;
                    }
                }
            }
        } finally {
            c.close();
        }
        return getMostReferencedContactId(segments);
    }
    private boolean lookupKeyContainsType(ArrayList<LookupKeySegment> segments, int lookupType) {
        for (int i = 0; i < segments.size(); i++) {
            LookupKeySegment segment = segments.get(i);
            if (segment.lookupType == lookupType) {
                return true;
            }
        }
        return false;
    }
    public void updateLookupKeyForRawContact(SQLiteDatabase db, long rawContactId) {
        mContactAggregator.updateLookupKeyForRawContact(db, rawContactId);
    }
    private long getMostReferencedContactId(ArrayList<LookupKeySegment> segments) {
        Collections.sort(segments);
        long bestContactId = -1;
        int bestRefCount = 0;
        long contactId = -1;
        int count = 0;
        int segmentCount = segments.size();
        for (int i = 0; i < segmentCount; i++) {
            LookupKeySegment segment = segments.get(i);
            if (segment.contactId != -1) {
                if (segment.contactId == contactId) {
                    count++;
                } else {
                    if (count > bestRefCount) {
                        bestContactId = contactId;
                        bestRefCount = count;
                    }
                    contactId = segment.contactId;
                    count = 1;
                }
            }
        }
        if (count > bestRefCount) {
            return contactId;
        } else {
            return bestContactId;
        }
    }
    private void setTablesAndProjectionMapForContacts(SQLiteQueryBuilder qb, Uri uri,
            String[] projection) {
        StringBuilder sb = new StringBuilder();
        appendContactsTables(sb, uri, projection);
        qb.setTables(sb.toString());
        qb.setProjectionMap(sContactsProjectionMap);
    }
    private void setTablesAndProjectionMapForContactsWithSnippet(SQLiteQueryBuilder qb, Uri uri,
            String[] projection, String filter) {
        StringBuilder sb = new StringBuilder();
        appendContactsTables(sb, uri, projection);
        sb.append(" JOIN (SELECT " +
                RawContacts.CONTACT_ID + " AS snippet_contact_id");
        if (mDbHelper.isInProjection(projection, SearchSnippetColumns.SNIPPET_DATA_ID)) {
            sb.append(", " + DataColumns.CONCRETE_ID + " AS "
                    + SearchSnippetColumns.SNIPPET_DATA_ID);
        }
        if (mDbHelper.isInProjection(projection, SearchSnippetColumns.SNIPPET_DATA1)) {
            sb.append(", " + Data.DATA1 + " AS " + SearchSnippetColumns.SNIPPET_DATA1);
        }
        if (mDbHelper.isInProjection(projection, SearchSnippetColumns.SNIPPET_DATA2)) {
            sb.append(", " + Data.DATA2 + " AS " + SearchSnippetColumns.SNIPPET_DATA2);
        }
        if (mDbHelper.isInProjection(projection, SearchSnippetColumns.SNIPPET_DATA3)) {
            sb.append(", " + Data.DATA3 + " AS " + SearchSnippetColumns.SNIPPET_DATA3);
        }
        if (mDbHelper.isInProjection(projection, SearchSnippetColumns.SNIPPET_DATA4)) {
            sb.append(", " + Data.DATA4 + " AS " + SearchSnippetColumns.SNIPPET_DATA4);
        }
        if (mDbHelper.isInProjection(projection, SearchSnippetColumns.SNIPPET_MIMETYPE)) {
            sb.append(", (" +
                    "SELECT " + MimetypesColumns.MIMETYPE +
                    " FROM " + Tables.MIMETYPES +
                    " WHERE " + MimetypesColumns._ID + "=" + DataColumns.MIMETYPE_ID +
                    ") AS " + SearchSnippetColumns.SNIPPET_MIMETYPE);
        }
        sb.append(" FROM " + Tables.DATA_JOIN_RAW_CONTACTS +
                " WHERE " + DataColumns.CONCRETE_ID +
                " IN (");
        sb.append(
                "SELECT MIN(" + Tables.NAME_LOOKUP + "." + NameLookupColumns.DATA_ID + ")" +
                " FROM " + Tables.NAME_LOOKUP +
                " JOIN " + Tables.RAW_CONTACTS +
                " ON (" + RawContactsColumns.CONCRETE_ID
                        + "=" + Tables.NAME_LOOKUP + "." + NameLookupColumns.RAW_CONTACT_ID + ")" +
                " WHERE " + NameLookupColumns.NORMALIZED_NAME + " GLOB '");
        sb.append(NameNormalizer.normalize(filter));
        sb.append("*' AND " + NameLookupColumns.NAME_TYPE +
                    " IN(" + CONTACT_LOOKUP_NAME_TYPES + ")" +
                " GROUP BY " + RawContactsColumns.CONCRETE_CONTACT_ID);
        sb.append(")) ON (" + Contacts._ID + "=snippet_contact_id)");
        qb.setTables(sb.toString());
        qb.setProjectionMap(sContactsProjectionWithSnippetMap);
    }
    private void appendContactsTables(StringBuilder sb, Uri uri, String[] projection) {
        boolean excludeRestrictedData = false;
        String requestingPackage = getQueryParameter(uri,
                ContactsContract.REQUESTING_PACKAGE_PARAM_KEY);
        if (requestingPackage != null) {
            excludeRestrictedData = !mDbHelper.hasAccessToRestrictedData(requestingPackage);
        }
        sb.append(mDbHelper.getContactView(excludeRestrictedData));
        if (mDbHelper.isInProjection(projection,
                Contacts.CONTACT_PRESENCE)) {
            sb.append(" LEFT OUTER JOIN " + Tables.AGGREGATED_PRESENCE +
                    " ON (" + Contacts._ID + " = " + AggregatedPresenceColumns.CONTACT_ID + ")");
        }
        if (mDbHelper.isInProjection(projection,
                Contacts.CONTACT_STATUS,
                Contacts.CONTACT_STATUS_RES_PACKAGE,
                Contacts.CONTACT_STATUS_ICON,
                Contacts.CONTACT_STATUS_LABEL,
                Contacts.CONTACT_STATUS_TIMESTAMP)) {
            sb.append(" LEFT OUTER JOIN " + Tables.STATUS_UPDATES + " "
                    + ContactsStatusUpdatesColumns.ALIAS +
                    " ON (" + ContactsColumns.LAST_STATUS_UPDATE_ID + "="
                            + ContactsStatusUpdatesColumns.CONCRETE_DATA_ID + ")");
        }
    }
    private void setTablesAndProjectionMapForRawContacts(SQLiteQueryBuilder qb, Uri uri) {
        StringBuilder sb = new StringBuilder();
        boolean excludeRestrictedData = false;
        String requestingPackage = getQueryParameter(uri,
                ContactsContract.REQUESTING_PACKAGE_PARAM_KEY);
        if (requestingPackage != null) {
            excludeRestrictedData = !mDbHelper.hasAccessToRestrictedData(requestingPackage);
        }
        sb.append(mDbHelper.getRawContactView(excludeRestrictedData));
        qb.setTables(sb.toString());
        qb.setProjectionMap(sRawContactsProjectionMap);
        appendAccountFromParameter(qb, uri);
    }
    private void setTablesAndProjectionMapForRawContactsEntities(SQLiteQueryBuilder qb, Uri uri) {
        boolean excludeRestrictedData = readBooleanQueryParameter(uri,
                Data.FOR_EXPORT_ONLY, false);
        String requestingPackage = getQueryParameter(uri,
                ContactsContract.REQUESTING_PACKAGE_PARAM_KEY);
        if (requestingPackage != null) {
            excludeRestrictedData = excludeRestrictedData
                    || !mDbHelper.hasAccessToRestrictedData(requestingPackage);
        }
        qb.setTables(mDbHelper.getContactEntitiesView(excludeRestrictedData));
        qb.setProjectionMap(sRawContactsEntityProjectionMap);
        appendAccountFromParameter(qb, uri);
    }
    private void setTablesAndProjectionMapForData(SQLiteQueryBuilder qb, Uri uri,
            String[] projection, boolean distinct) {
        StringBuilder sb = new StringBuilder();
        boolean excludeRestrictedData = readBooleanQueryParameter(uri,
                Data.FOR_EXPORT_ONLY, false);
        String requestingPackage = getQueryParameter(uri,
                ContactsContract.REQUESTING_PACKAGE_PARAM_KEY);
        if (requestingPackage != null) {
            excludeRestrictedData = excludeRestrictedData
                    || !mDbHelper.hasAccessToRestrictedData(requestingPackage);
        }
        sb.append(mDbHelper.getDataView(excludeRestrictedData));
        sb.append(" data");
        if (mDbHelper.isInProjection(projection, Data.CONTACT_PRESENCE)) {
            sb.append(" LEFT OUTER JOIN " + Tables.AGGREGATED_PRESENCE +
                    " ON (" + AggregatedPresenceColumns.CONCRETE_CONTACT_ID + "="
                    + RawContacts.CONTACT_ID + ")");
        }
        if (mDbHelper.isInProjection(projection,
                Data.CONTACT_STATUS,
                Data.CONTACT_STATUS_RES_PACKAGE,
                Data.CONTACT_STATUS_ICON,
                Data.CONTACT_STATUS_LABEL,
                Data.CONTACT_STATUS_TIMESTAMP)) {
            sb.append(" LEFT OUTER JOIN " + Tables.STATUS_UPDATES + " "
                    + ContactsStatusUpdatesColumns.ALIAS +
                    " ON (" + ContactsColumns.LAST_STATUS_UPDATE_ID + "="
                            + ContactsStatusUpdatesColumns.CONCRETE_DATA_ID + ")");
        }
        if (mDbHelper.isInProjection(projection, Data.PRESENCE)) {
            sb.append(" LEFT OUTER JOIN " + Tables.PRESENCE +
                    " ON (" + StatusUpdates.DATA_ID + "="
                    + DataColumns.CONCRETE_ID + ")");
        }
        if (mDbHelper.isInProjection(projection,
                Data.STATUS,
                Data.STATUS_RES_PACKAGE,
                Data.STATUS_ICON,
                Data.STATUS_LABEL,
                Data.STATUS_TIMESTAMP)) {
            sb.append(" LEFT OUTER JOIN " + Tables.STATUS_UPDATES +
                    " ON (" + StatusUpdatesColumns.CONCRETE_DATA_ID + "="
                            + DataColumns.CONCRETE_ID + ")");
        }
        qb.setTables(sb.toString());
        qb.setProjectionMap(distinct ? sDistinctDataProjectionMap : sDataProjectionMap);
        appendAccountFromParameter(qb, uri);
    }
    private void setTableAndProjectionMapForStatusUpdates(SQLiteQueryBuilder qb,
            String[] projection) {
        StringBuilder sb = new StringBuilder();
        sb.append(mDbHelper.getDataView());
        sb.append(" data");
        if (mDbHelper.isInProjection(projection, StatusUpdates.PRESENCE)) {
            sb.append(" LEFT OUTER JOIN " + Tables.PRESENCE +
                    " ON(" + Tables.PRESENCE + "." + StatusUpdates.DATA_ID
                    + "=" + DataColumns.CONCRETE_ID + ")");
        }
        if (mDbHelper.isInProjection(projection,
                StatusUpdates.STATUS,
                StatusUpdates.STATUS_RES_PACKAGE,
                StatusUpdates.STATUS_ICON,
                StatusUpdates.STATUS_LABEL,
                StatusUpdates.STATUS_TIMESTAMP)) {
            sb.append(" LEFT OUTER JOIN " + Tables.STATUS_UPDATES +
                    " ON(" + Tables.STATUS_UPDATES + "." + StatusUpdatesColumns.DATA_ID
                    + "=" + DataColumns.CONCRETE_ID + ")");
        }
        qb.setTables(sb.toString());
        qb.setProjectionMap(sStatusUpdatesProjectionMap);
    }
    private void appendAccountFromParameter(SQLiteQueryBuilder qb, Uri uri) {
        final String accountName = getQueryParameter(uri, RawContacts.ACCOUNT_NAME);
        final String accountType = getQueryParameter(uri, RawContacts.ACCOUNT_TYPE);
        final boolean partialUri = TextUtils.isEmpty(accountName) ^ TextUtils.isEmpty(accountType);
        if (partialUri) {
            throw new IllegalArgumentException(mDbHelper.exceptionMessage(
                    "Must specify both or neither of ACCOUNT_NAME and ACCOUNT_TYPE", uri));
        }
        final boolean validAccount = !TextUtils.isEmpty(accountName);
        if (validAccount) {
            qb.appendWhere(RawContacts.ACCOUNT_NAME + "="
                    + DatabaseUtils.sqlEscapeString(accountName) + " AND "
                    + RawContacts.ACCOUNT_TYPE + "="
                    + DatabaseUtils.sqlEscapeString(accountType));
        } else {
            qb.appendWhere("1");
        }
    }
    private String appendAccountToSelection(Uri uri, String selection) {
        final String accountName = getQueryParameter(uri, RawContacts.ACCOUNT_NAME);
        final String accountType = getQueryParameter(uri, RawContacts.ACCOUNT_TYPE);
        final boolean partialUri = TextUtils.isEmpty(accountName) ^ TextUtils.isEmpty(accountType);
        if (partialUri) {
            throw new IllegalArgumentException(mDbHelper.exceptionMessage(
                    "Must specify both or neither of ACCOUNT_NAME and ACCOUNT_TYPE", uri));
        }
        final boolean validAccount = !TextUtils.isEmpty(accountName);
        if (validAccount) {
            StringBuilder selectionSb = new StringBuilder(RawContacts.ACCOUNT_NAME + "="
                    + DatabaseUtils.sqlEscapeString(accountName) + " AND "
                    + RawContacts.ACCOUNT_TYPE + "="
                    + DatabaseUtils.sqlEscapeString(accountType));
            if (!TextUtils.isEmpty(selection)) {
                selectionSb.append(" AND (");
                selectionSb.append(selection);
                selectionSb.append(')');
            }
            return selectionSb.toString();
        } else {
            return selection;
        }
    }
    private String getLimit(Uri uri) {
        String limitParam = getQueryParameter(uri, "limit");
        if (limitParam == null) {
            return null;
        }
        try {
            int l = Integer.parseInt(limitParam);
            if (l < 0) {
                Log.w(TAG, "Invalid limit parameter: " + limitParam);
                return null;
            }
            return String.valueOf(l);
        } catch (NumberFormatException ex) {
            Log.w(TAG, "Invalid limit parameter: " + limitParam);
            return null;
        }
    }
    private boolean isPhoneNumber(CharSequence cons) {
        int len = cons.length();
        for (int i = 0; i < len; i++) {
            char c = cons.charAt(i);
            if ((c >= '0') && (c <= '9')) {
                continue;
            }
            if ((c == ' ') || (c == '-') || (c == '(') || (c == ')') || (c == '.') || (c == '+')
                    || (c == '#') || (c == '*')) {
                continue;
            }
            if ((c >= 'A') && (c <= 'Z')) {
                continue;
            }
            if ((c >= 'a') && (c <= 'z')) {
                continue;
            }
            return false;
        }
        return true;
    }
    String getContactsRestrictions() {
        if (mDbHelper.hasAccessToRestrictedData()) {
            return "1";
        } else {
            return RawContactsColumns.CONCRETE_IS_RESTRICTED + "=0";
        }
    }
    public String getContactsRestrictionExceptionAsNestedQuery(String contactIdColumn) {
        if (mDbHelper.hasAccessToRestrictedData()) {
            return "1";
        } else {
            return "(SELECT " + RawContacts.IS_RESTRICTED + " FROM " + Tables.RAW_CONTACTS
                    + " WHERE " + RawContactsColumns.CONCRETE_ID + "=" + contactIdColumn + ")=0";
        }
    }
    @Override
    public AssetFileDescriptor openAssetFile(Uri uri, String mode) throws FileNotFoundException {
        int match = sUriMatcher.match(uri);
        switch (match) {
            case CONTACTS_PHOTO: {
                return openPhotoAssetFile(uri, mode,
                        Data._ID + "=" + Contacts.PHOTO_ID + " AND " + RawContacts.CONTACT_ID + "=?",
                        new String[]{uri.getPathSegments().get(1)});
            }
            case DATA_ID: {
                return openPhotoAssetFile(uri, mode,
                        Data._ID + "=? AND " + Data.MIMETYPE + "='" + Photo.CONTENT_ITEM_TYPE + "'",
                        new String[]{uri.getPathSegments().get(1)});
            }
            case CONTACTS_AS_VCARD: {
                final String lookupKey = Uri.encode(uri.getPathSegments().get(2));
                mSelectionArgs1[0] = String.valueOf(lookupContactIdByLookupKey(mDb, lookupKey));
                final String selection = Contacts._ID + "=?";
                final ByteArrayOutputStream localStream = new ByteArrayOutputStream();
                outputRawContactsAsVCard(localStream, selection, mSelectionArgs1);
                return buildAssetFileDescriptor(localStream);
            }
            case CONTACTS_AS_MULTI_VCARD: {
                final String lookupKeys = uri.getPathSegments().get(2);
                final String[] loopupKeyList = lookupKeys.split(":");
                final StringBuilder inBuilder = new StringBuilder();
                int index = 0;
                for (String lookupKey : loopupKeyList) {
                    if (index == 0) {
                        inBuilder.append("(");
                    } else {
                        inBuilder.append(",");
                    }
                    inBuilder.append(lookupContactIdByLookupKey(mDb, lookupKey));
                    index++;
                }
                inBuilder.append(')');
                final String selection = Contacts._ID + " IN " + inBuilder.toString();
                final ByteArrayOutputStream localStream = new ByteArrayOutputStream();
                outputRawContactsAsVCard(localStream, selection, null);
                return buildAssetFileDescriptor(localStream);
            }
            default:
                throw new FileNotFoundException(mDbHelper.exceptionMessage("File does not exist",
                        uri));
        }
    }
    private AssetFileDescriptor openPhotoAssetFile(Uri uri, String mode, String selection,
            String[] selectionArgs)
            throws FileNotFoundException {
        if (!"r".equals(mode)) {
            throw new FileNotFoundException(mDbHelper.exceptionMessage("Mode " + mode
                    + " not supported.", uri));
        }
        String sql =
                "SELECT " + Photo.PHOTO + " FROM " + mDbHelper.getDataView() +
                " WHERE " + selection;
        SQLiteDatabase db = mDbHelper.getReadableDatabase();
        return SQLiteContentHelper.getBlobColumnAsAssetFile(db, sql,
                selectionArgs);
    }
    private static final String CONTACT_MEMORY_FILE_NAME = "contactAssetFile";
    private AssetFileDescriptor buildAssetFileDescriptor(ByteArrayOutputStream stream) {
        AssetFileDescriptor fd = null;
        try {
            stream.flush();
            final byte[] byteData = stream.toByteArray();
            final int size = byteData.length;
            final MemoryFile memoryFile = new MemoryFile(CONTACT_MEMORY_FILE_NAME, size);
            memoryFile.writeBytes(byteData, 0, 0, size);
            memoryFile.deactivate();
            fd = AssetFileDescriptor.fromMemoryFile(memoryFile);
        } catch (IOException e) {
            Log.w(TAG, "Problem writing stream into an AssetFileDescriptor: " + e.toString());
        }
        return fd;
    }
    private void outputRawContactsAsVCard(OutputStream stream, String selection,
            String[] selectionArgs) {
        final Context context = this.getContext();
        final VCardComposer composer =
                new VCardComposer(context, VCardConfig.VCARD_TYPE_DEFAULT, false);
        composer.addHandler(composer.new HandlerForOutputStream(stream));
        if (!composer.init(selection, selectionArgs)) {
            Log.w(TAG, "Failed to init VCardComposer");
            return;
        }
        while (!composer.isAfterLast()) {
            if (!composer.createOneEntry()) {
                Log.w(TAG, "Failed to output a contact.");
            }
        }
        composer.terminate();
    }
    @Override
    public String getType(Uri uri) {
        final int match = sUriMatcher.match(uri);
        switch (match) {
            case CONTACTS:
                return Contacts.CONTENT_TYPE;
            case CONTACTS_LOOKUP:
            case CONTACTS_ID:
            case CONTACTS_LOOKUP_ID:
                return Contacts.CONTENT_ITEM_TYPE;
            case CONTACTS_AS_VCARD:
            case CONTACTS_AS_MULTI_VCARD:
                return Contacts.CONTENT_VCARD_TYPE;
            case RAW_CONTACTS:
                return RawContacts.CONTENT_TYPE;
            case RAW_CONTACTS_ID:
                return RawContacts.CONTENT_ITEM_TYPE;
            case DATA_ID:
                return mDbHelper.getDataMimeType(ContentUris.parseId(uri));
            case PHONES:
                return Phone.CONTENT_TYPE;
            case PHONES_ID:
                return Phone.CONTENT_ITEM_TYPE;
            case PHONE_LOOKUP:
                return PhoneLookup.CONTENT_TYPE;
            case EMAILS:
                return Email.CONTENT_TYPE;
            case EMAILS_ID:
                return Email.CONTENT_ITEM_TYPE;
            case POSTALS:
                return StructuredPostal.CONTENT_TYPE;
            case POSTALS_ID:
                return StructuredPostal.CONTENT_ITEM_TYPE;
            case AGGREGATION_EXCEPTIONS:
                return AggregationExceptions.CONTENT_TYPE;
            case AGGREGATION_EXCEPTION_ID:
                return AggregationExceptions.CONTENT_ITEM_TYPE;
            case SETTINGS:
                return Settings.CONTENT_TYPE;
            case AGGREGATION_SUGGESTIONS:
                return Contacts.CONTENT_TYPE;
            case SEARCH_SUGGESTIONS:
                return SearchManager.SUGGEST_MIME_TYPE;
            case SEARCH_SHORTCUT:
                return SearchManager.SHORTCUT_MIME_TYPE;
            default:
                return mLegacyApiSupport.getType(uri);
        }
    }
    private void setDisplayName(long rawContactId, int displayNameSource,
            String displayNamePrimary, String displayNameAlternative, String phoneticName,
            int phoneticNameStyle, String sortKeyPrimary, String sortKeyAlternative) {
        mRawContactDisplayNameUpdate.bindLong(1, displayNameSource);
        bindString(mRawContactDisplayNameUpdate, 2, displayNamePrimary);
        bindString(mRawContactDisplayNameUpdate, 3, displayNameAlternative);
        bindString(mRawContactDisplayNameUpdate, 4, phoneticName);
        mRawContactDisplayNameUpdate.bindLong(5, phoneticNameStyle);
        bindString(mRawContactDisplayNameUpdate, 6, sortKeyPrimary);
        bindString(mRawContactDisplayNameUpdate, 7, sortKeyAlternative);
        mRawContactDisplayNameUpdate.bindLong(8, rawContactId);
        mRawContactDisplayNameUpdate.execute();
    }
    private void setRawContactDirty(long rawContactId) {
        mDirtyRawContacts.add(rawContactId);
    }
    private void setIsPrimary(long rawContactId, long dataId, long mimeTypeId) {
        mSetPrimaryStatement.bindLong(1, dataId);
        mSetPrimaryStatement.bindLong(2, mimeTypeId);
        mSetPrimaryStatement.bindLong(3, rawContactId);
        mSetPrimaryStatement.execute();
    }
    private void setIsSuperPrimary(long rawContactId, long dataId, long mimeTypeId) {
        mSetSuperPrimaryStatement.bindLong(1, dataId);
        mSetSuperPrimaryStatement.bindLong(2, mimeTypeId);
        mSetSuperPrimaryStatement.bindLong(3, rawContactId);
        mSetSuperPrimaryStatement.execute();
    }
    public String insertNameLookupForEmail(long rawContactId, long dataId, String email) {
        if (TextUtils.isEmpty(email)) {
            return null;
        }
        String address = mDbHelper.extractHandleFromEmailAddress(email);
        if (address == null) {
            return null;
        }
        insertNameLookup(rawContactId, dataId,
                NameLookupType.EMAIL_BASED_NICKNAME, NameNormalizer.normalize(address));
        return address;
    }
    public void insertNameLookupForNickname(long rawContactId, long dataId, String nickname) {
        if (TextUtils.isEmpty(nickname)) {
            return;
        }
        insertNameLookup(rawContactId, dataId,
                NameLookupType.NICKNAME, NameNormalizer.normalize(nickname));
    }
    public void insertNameLookupForOrganization(long rawContactId, long dataId, String company,
            String title) {
        if (!TextUtils.isEmpty(company)) {
            insertNameLookup(rawContactId, dataId,
                    NameLookupType.ORGANIZATION, NameNormalizer.normalize(company));
        }
        if (!TextUtils.isEmpty(title)) {
            insertNameLookup(rawContactId, dataId,
                    NameLookupType.ORGANIZATION, NameNormalizer.normalize(title));
        }
    }
    public void insertNameLookupForStructuredName(long rawContactId, long dataId, String name,
            int fullNameStyle) {
        mNameLookupBuilder.insertNameLookup(rawContactId, dataId, name, fullNameStyle);
    }
    private class StructuredNameLookupBuilder extends NameLookupBuilder {
        public StructuredNameLookupBuilder(NameSplitter splitter) {
            super(splitter);
        }
        @Override
        protected void insertNameLookup(long rawContactId, long dataId, int lookupType,
                String name) {
            ContactsProvider2.this.insertNameLookup(rawContactId, dataId, lookupType, name);
        }
        @Override
        protected String[] getCommonNicknameClusters(String normalizedName) {
            return mCommonNicknameCache.getCommonNicknameClusters(normalizedName);
        }
    }
    public void insertNameLookupForPhoneticName(long rawContactId, long dataId,
            ContentValues values) {
        if (values.containsKey(StructuredName.PHONETIC_FAMILY_NAME)
                || values.containsKey(StructuredName.PHONETIC_GIVEN_NAME)
                || values.containsKey(StructuredName.PHONETIC_MIDDLE_NAME)) {
            insertNameLookupForPhoneticName(rawContactId, dataId,
                    values.getAsString(StructuredName.PHONETIC_FAMILY_NAME),
                    values.getAsString(StructuredName.PHONETIC_MIDDLE_NAME),
                    values.getAsString(StructuredName.PHONETIC_GIVEN_NAME));
        }
    }
    public void insertNameLookupForPhoneticName(long rawContactId, long dataId, String familyName,
            String middleName, String givenName) {
        mSb.setLength(0);
        if (familyName != null) {
            mSb.append(familyName.trim());
        }
        if (middleName != null) {
            mSb.append(middleName.trim());
        }
        if (givenName != null) {
            mSb.append(givenName.trim());
        }
        if (mSb.length() > 0) {
            insertNameLookup(rawContactId, dataId, NameLookupType.NAME_COLLATION_KEY,
                    NameNormalizer.normalize(mSb.toString()));
        }
        if (givenName != null) {
            insertNameLookup(rawContactId, dataId, NameLookupType.NAME_SHORTHAND,
                    NameNormalizer.normalize(givenName.trim()));
        }
    }
    public void insertNameLookup(long rawContactId, long dataId, int lookupType, String name) {
        mNameLookupInsert.bindLong(1, rawContactId);
        mNameLookupInsert.bindLong(2, dataId);
        mNameLookupInsert.bindLong(3, lookupType);
        bindString(mNameLookupInsert, 4, name);
        mNameLookupInsert.executeInsert();
    }
    public void deleteNameLookup(long dataId) {
        mNameLookupDelete.bindLong(1, dataId);
        mNameLookupDelete.execute();
    }
    public void appendContactFilterAsNestedQuery(StringBuilder sb, String filterParam) {
        sb.append("(" +
                "SELECT DISTINCT " + RawContacts.CONTACT_ID +
                " FROM " + Tables.RAW_CONTACTS +
                " JOIN " + Tables.NAME_LOOKUP +
                " ON(" + RawContactsColumns.CONCRETE_ID + "="
                        + NameLookupColumns.RAW_CONTACT_ID + ")" +
                " WHERE normalized_name GLOB '");
        sb.append(NameNormalizer.normalize(filterParam));
        sb.append("*' AND " + NameLookupColumns.NAME_TYPE +
                    " IN(" + CONTACT_LOOKUP_NAME_TYPES + "))");
    }
    public String getRawContactsByFilterAsNestedQuery(String filterParam) {
        StringBuilder sb = new StringBuilder();
        appendRawContactsByFilterAsNestedQuery(sb, filterParam);
        return sb.toString();
    }
    public void appendRawContactsByFilterAsNestedQuery(StringBuilder sb, String filterParam) {
        appendRawContactsByNormalizedNameFilter(sb, NameNormalizer.normalize(filterParam), true);
    }
    private void appendRawContactsByNormalizedNameFilter(StringBuilder sb, String normalizedName,
            boolean allowEmailMatch) {
        sb.append("(" +
                "SELECT " + NameLookupColumns.RAW_CONTACT_ID +
                " FROM " + Tables.NAME_LOOKUP +
                " WHERE " + NameLookupColumns.NORMALIZED_NAME +
                " GLOB '");
        sb.append(normalizedName);
        sb.append("*' AND " + NameLookupColumns.NAME_TYPE + " IN ("
                + NameLookupType.NAME_COLLATION_KEY + ","
                + NameLookupType.NICKNAME + ","
                + NameLookupType.NAME_SHORTHAND + ","
                + NameLookupType.ORGANIZATION + ","
                + NameLookupType.NAME_CONSONANTS);
        if (allowEmailMatch) {
            sb.append("," + NameLookupType.EMAIL_BASED_NICKNAME);
        }
        sb.append("))");
    }
    private String[] insertSelectionArg(String[] selectionArgs, String arg) {
        if (selectionArgs == null) {
            return new String[] {arg};
        } else {
            int newLength = selectionArgs.length + 1;
            String[] newSelectionArgs = new String[newLength];
            newSelectionArgs[0] = arg;
            System.arraycopy(selectionArgs, 0, newSelectionArgs, 1, selectionArgs.length);
            return newSelectionArgs;
        }
    }
    private String[] appendProjectionArg(String[] projection, String arg) {
        if (projection == null) {
            return null;
        }
        final int length = projection.length;
        String[] newProjection = new String[length + 1];
        System.arraycopy(projection, 0, newProjection, 0, length);
        newProjection[length] = arg;
        return newProjection;
    }
    protected Account getDefaultAccount() {
        AccountManager accountManager = AccountManager.get(getContext());
        try {
            Account[] accounts = accountManager.getAccountsByTypeAndFeatures(DEFAULT_ACCOUNT_TYPE,
                    new String[] {FEATURE_LEGACY_HOSTED_OR_GOOGLE}, null, null).getResult();
            if (accounts != null && accounts.length > 0) {
                return accounts[0];
            }
        } catch (Throwable e) {
            Log.e(TAG, "Cannot determine the default account for contacts compatibility", e);
        }
        return null;
    }
    protected boolean isWritableAccount(String accountType) {
        if (accountType == null) {
            return true;
        }
        Boolean writable = mAccountWritability.get(accountType);
        if (writable != null) {
            return writable;
        }
        IContentService contentService = ContentResolver.getContentService();
        try {
            for (SyncAdapterType sync : contentService.getSyncAdapterTypes()) {
                if (ContactsContract.AUTHORITY.equals(sync.authority) &&
                        accountType.equals(sync.accountType)) {
                    writable = sync.supportsUploading();
                    break;
                }
            }
        } catch (RemoteException e) {
            Log.e(TAG, "Could not acquire sync adapter types");
        }
        if (writable == null) {
            writable = false;
        }
        mAccountWritability.put(accountType, writable);
        return writable;
    }
     static boolean readBooleanQueryParameter(Uri uri, String parameter,
            boolean defaultValue) {
        String query = uri.getEncodedQuery();
        if (query == null) {
            return defaultValue;
        }
        int index = query.indexOf(parameter);
        if (index == -1) {
            return defaultValue;
        }
        index += parameter.length();
        return !matchQueryParameter(query, index, "=0", false)
                && !matchQueryParameter(query, index, "=false", true);
    }
    private static boolean matchQueryParameter(String query, int index, String value,
            boolean ignoreCase) {
        int length = value.length();
        return query.regionMatches(ignoreCase, index, value, 0, length)
                && (query.length() == index + length || query.charAt(index + length) == '&');
    }
     static String getQueryParameter(Uri uri, String parameter) {
        String query = uri.getEncodedQuery();
        if (query == null) {
            return null;
        }
        int queryLength = query.length();
        int parameterLength = parameter.length();
        String value;
        int index = 0;
        while (true) {
            index = query.indexOf(parameter, index);
            if (index == -1) {
                return null;
            }
            index += parameterLength;
            if (queryLength == index) {
                return null;
            }
            if (query.charAt(index) == '=') {
                index++;
                break;
            }
        }
        int ampIndex = query.indexOf('&', index);
        if (ampIndex == -1) {
            value = query.substring(index);
        } else {
            value = query.substring(index, ampIndex);
        }
        return Uri.decode(value);
    }
    private void bindString(SQLiteStatement stmt, int index, String value) {
        if (value == null) {
            stmt.bindNull(index);
        } else {
            stmt.bindString(index, value);
        }
    }
    private void bindLong(SQLiteStatement stmt, int index, Number value) {
        if (value == null) {
            stmt.bindNull(index);
        } else {
            stmt.bindLong(index, value.longValue());
        }
    }
}
