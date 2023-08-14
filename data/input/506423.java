public class LegacyApiSupport {
    private static final String TAG = "ContactsProviderV1";
    private static final UriMatcher sUriMatcher = new UriMatcher(UriMatcher.NO_MATCH);
    private static final int PEOPLE = 1;
    private static final int PEOPLE_ID = 2;
    private static final int PEOPLE_UPDATE_CONTACT_TIME = 3;
    private static final int ORGANIZATIONS = 4;
    private static final int ORGANIZATIONS_ID = 5;
    private static final int PEOPLE_CONTACTMETHODS = 6;
    private static final int PEOPLE_CONTACTMETHODS_ID = 7;
    private static final int CONTACTMETHODS = 8;
    private static final int CONTACTMETHODS_ID = 9;
    private static final int PEOPLE_PHONES = 10;
    private static final int PEOPLE_PHONES_ID = 11;
    private static final int PHONES = 12;
    private static final int PHONES_ID = 13;
    private static final int EXTENSIONS = 14;
    private static final int EXTENSIONS_ID = 15;
    private static final int PEOPLE_EXTENSIONS = 16;
    private static final int PEOPLE_EXTENSIONS_ID = 17;
    private static final int GROUPS = 18;
    private static final int GROUPS_ID = 19;
    private static final int GROUPMEMBERSHIP = 20;
    private static final int GROUPMEMBERSHIP_ID = 21;
    private static final int PEOPLE_GROUPMEMBERSHIP = 22;
    private static final int PEOPLE_GROUPMEMBERSHIP_ID = 23;
    private static final int PEOPLE_PHOTO = 24;
    private static final int PHOTOS = 25;
    private static final int PHOTOS_ID = 26;
    private static final int PEOPLE_FILTER = 29;
    private static final int DELETED_PEOPLE = 30;
    private static final int DELETED_GROUPS = 31;
    private static final int SEARCH_SUGGESTIONS = 32;
    private static final int SEARCH_SHORTCUT = 33;
    private static final int PHONES_FILTER = 34;
    private static final int LIVE_FOLDERS_PEOPLE = 35;
    private static final int LIVE_FOLDERS_PEOPLE_GROUP_NAME = 36;
    private static final int LIVE_FOLDERS_PEOPLE_WITH_PHONES = 37;
    private static final int LIVE_FOLDERS_PEOPLE_FAVORITES = 38;
    private static final int CONTACTMETHODS_EMAIL = 39;
    private static final int GROUP_NAME_MEMBERS = 40;
    private static final int GROUP_SYSTEM_ID_MEMBERS = 41;
    private static final int PEOPLE_ORGANIZATIONS = 42;
    private static final int PEOPLE_ORGANIZATIONS_ID = 43;
    private static final int SETTINGS = 44;
    private static final String PEOPLE_JOINS =
            " LEFT OUTER JOIN data name ON (raw_contacts._id = name.raw_contact_id"
            + " AND (SELECT mimetype FROM mimetypes WHERE mimetypes._id = name.mimetype_id)"
                    + "='" + StructuredName.CONTENT_ITEM_TYPE + "')"
            + " LEFT OUTER JOIN data organization ON (raw_contacts._id = organization.raw_contact_id"
            + " AND (SELECT mimetype FROM mimetypes WHERE mimetypes._id = organization.mimetype_id)"
                    + "='" + Organization.CONTENT_ITEM_TYPE + "' AND organization.is_primary)"
            + " LEFT OUTER JOIN data email ON (raw_contacts._id = email.raw_contact_id"
            + " AND (SELECT mimetype FROM mimetypes WHERE mimetypes._id = email.mimetype_id)"
                    + "='" + Email.CONTENT_ITEM_TYPE + "' AND email.is_primary)"
            + " LEFT OUTER JOIN data note ON (raw_contacts._id = note.raw_contact_id"
            + " AND (SELECT mimetype FROM mimetypes WHERE mimetypes._id = note.mimetype_id)"
                    + "='" + Note.CONTENT_ITEM_TYPE + "')"
            + " LEFT OUTER JOIN data phone ON (raw_contacts._id = phone.raw_contact_id"
            + " AND (SELECT mimetype FROM mimetypes WHERE mimetypes._id = phone.mimetype_id)"
                    + "='" + Phone.CONTENT_ITEM_TYPE + "' AND phone.is_primary)";
    public static final String DATA_JOINS =
            " JOIN mimetypes ON (mimetypes._id = data.mimetype_id)"
            + " JOIN raw_contacts ON (raw_contacts._id = data.raw_contact_id)"
            + PEOPLE_JOINS;
    public static final String PRESENCE_JOINS =
            " LEFT OUTER JOIN " + Tables.PRESENCE +
            " ON (" + Tables.PRESENCE + "." + StatusUpdates.DATA_ID + "=" +
                    "(SELECT MAX(" + StatusUpdates.DATA_ID + ")" +
                    " FROM " + Tables.PRESENCE +
                    " WHERE people._id = " + PresenceColumns.RAW_CONTACT_ID + ")" +
            " )";
    private static final String PHONETIC_NAME_SQL = "trim(trim("
            + "ifnull(name." + StructuredName.PHONETIC_GIVEN_NAME + ",' ')||' '||"
            + "ifnull(name." + StructuredName.PHONETIC_MIDDLE_NAME + ",' '))||' '||"
            + "ifnull(name." + StructuredName.PHONETIC_FAMILY_NAME + ",' ')) ";
    private static final String CONTACT_METHOD_KIND_SQL =
            "CAST ((CASE WHEN mimetype='" + Email.CONTENT_ITEM_TYPE + "'"
                + " THEN " + android.provider.Contacts.KIND_EMAIL
                + " ELSE"
                    + " (CASE WHEN mimetype='" + Im.CONTENT_ITEM_TYPE +"'"
                        + " THEN " + android.provider.Contacts.KIND_IM
                        + " ELSE"
                        + " (CASE WHEN mimetype='" + StructuredPostal.CONTENT_ITEM_TYPE + "'"
                            + " THEN "  + android.provider.Contacts.KIND_POSTAL
                            + " ELSE"
                                + " NULL"
                            + " END)"
                        + " END)"
                + " END) AS INTEGER)";
    private static final String IM_PROTOCOL_SQL =
            "(CASE WHEN " + StatusUpdates.PROTOCOL + "=" + Im.PROTOCOL_CUSTOM
                + " THEN 'custom:'||" + StatusUpdates.CUSTOM_PROTOCOL
                + " ELSE 'pre:'||" + StatusUpdates.PROTOCOL
                + " END)";
    private static String CONTACT_METHOD_DATA_SQL =
            "(CASE WHEN " + Data.MIMETYPE + "='" + Im.CONTENT_ITEM_TYPE + "'"
                + " THEN (CASE WHEN " + Tables.DATA + "." + Im.PROTOCOL + "=" + Im.PROTOCOL_CUSTOM
                    + " THEN 'custom:'||" + Tables.DATA + "." + Im.CUSTOM_PROTOCOL
                    + " ELSE 'pre:'||" + Tables.DATA + "." + Im.PROTOCOL
                    + " END)"
                + " ELSE " + Tables.DATA + "." + Email.DATA
                + " END)";
    private static final Uri LIVE_FOLDERS_CONTACTS_URI = Uri.withAppendedPath(
            ContactsContract.AUTHORITY_URI, "live_folders/contacts");
    private static final Uri LIVE_FOLDERS_CONTACTS_WITH_PHONES_URI = Uri.withAppendedPath(
            ContactsContract.AUTHORITY_URI, "live_folders/contacts_with_phones");
    private static final Uri LIVE_FOLDERS_CONTACTS_FAVORITES_URI = Uri.withAppendedPath(
            ContactsContract.AUTHORITY_URI, "live_folders/favorites");
    private static final String CONTACTS_UPDATE_LASTTIMECONTACTED =
            "UPDATE " + Tables.CONTACTS +
            " SET " + Contacts.LAST_TIME_CONTACTED + "=? " +
            "WHERE " + Contacts._ID + "=?";
    private static final String RAWCONTACTS_UPDATE_LASTTIMECONTACTED =
            "UPDATE " + Tables.RAW_CONTACTS + " SET "
            + RawContacts.LAST_TIME_CONTACTED + "=? WHERE "
            + RawContacts._ID + "=?";
    private String[] mSelectionArgs1 = new String[1];
    private String[] mSelectionArgs2 = new String[2];
    public interface LegacyTables {
        public static final String PEOPLE = "view_v1_people";
        public static final String PEOPLE_JOIN_PRESENCE = "view_v1_people people " + PRESENCE_JOINS;
        public static final String GROUPS = "view_v1_groups";
        public static final String ORGANIZATIONS = "view_v1_organizations";
        public static final String CONTACT_METHODS = "view_v1_contact_methods";
        public static final String PHONES = "view_v1_phones";
        public static final String EXTENSIONS = "view_v1_extensions";
        public static final String GROUP_MEMBERSHIP = "view_v1_group_membership";
        public static final String PHOTOS = "view_v1_photos";
        public static final String SETTINGS = "v1_settings";
    }
    private static final String[] ORGANIZATION_MIME_TYPES = new String[] {
        Organization.CONTENT_ITEM_TYPE
    };
    private static final String[] CONTACT_METHOD_MIME_TYPES = new String[] {
        Email.CONTENT_ITEM_TYPE,
        Im.CONTENT_ITEM_TYPE,
        StructuredPostal.CONTENT_ITEM_TYPE,
    };
    private static final String[] PHONE_MIME_TYPES = new String[] {
        Phone.CONTENT_ITEM_TYPE
    };
    private static final String[] PHOTO_MIME_TYPES = new String[] {
        Photo.CONTENT_ITEM_TYPE
    };
    private static final String[] GROUP_MEMBERSHIP_MIME_TYPES = new String[] {
        GroupMembership.CONTENT_ITEM_TYPE
    };
    private static final String[] EXTENSION_MIME_TYPES = new String[] {
        android.provider.Contacts.Extensions.CONTENT_ITEM_TYPE
    };
    private interface IdQuery {
        String[] COLUMNS = { BaseColumns._ID };
        int _ID = 0;
    }
    private interface LegacyPhotoData {
        public static final String CONTENT_ITEM_TYPE = "vnd.android.cursor.item/photo_v1_extras";
        public static final String PHOTO_DATA_ID = Data.DATA1;
        public static final String LOCAL_VERSION = Data.DATA2;
        public static final String DOWNLOAD_REQUIRED = Data.DATA3;
        public static final String EXISTS_ON_SERVER = Data.DATA4;
        public static final String SYNC_ERROR = Data.DATA5;
    }
    public static final String LEGACY_PHOTO_JOIN =
            " LEFT OUTER JOIN data legacy_photo ON (raw_contacts._id = legacy_photo.raw_contact_id"
            + " AND (SELECT mimetype FROM mimetypes WHERE mimetypes._id = legacy_photo.mimetype_id)"
                + "='" + LegacyPhotoData.CONTENT_ITEM_TYPE + "'"
            + " AND " + DataColumns.CONCRETE_ID + " = legacy_photo." + LegacyPhotoData.PHOTO_DATA_ID
            + ")";
    private static final HashMap<String, String> sPeopleProjectionMap;
    private static final HashMap<String, String> sOrganizationProjectionMap;
    private static final HashMap<String, String> sContactMethodProjectionMap;
    private static final HashMap<String, String> sPhoneProjectionMap;
    private static final HashMap<String, String> sExtensionProjectionMap;
    private static final HashMap<String, String> sGroupProjectionMap;
    private static final HashMap<String, String> sGroupMembershipProjectionMap;
    private static final HashMap<String, String> sPhotoProjectionMap;
    static {
        UriMatcher matcher = sUriMatcher;
        String authority = android.provider.Contacts.AUTHORITY;
        matcher.addURI(authority, "extensions", EXTENSIONS);
        matcher.addURI(authority, "extensions/#", EXTENSIONS_ID);
        matcher.addURI(authority, "groups", GROUPS);
        matcher.addURI(authority, "groups/#", GROUPS_ID);
        matcher.addURI(authority, "groups/namemembers", GROUP_SYSTEM_ID_MEMBERS);
        matcher.addURI(authority, "groupmembership", GROUPMEMBERSHIP);
        matcher.addURI(authority, "groupmembership/#", GROUPMEMBERSHIP_ID);
        matcher.addURI(authority, "people", PEOPLE);
        matcher.addURI(authority, "people/filter
    public void copySettingsToLegacySettings() {
        SQLiteDatabase db = mDbHelper.getWritableDatabase();
        Cursor cursor = db.rawQuery(SettingsMatchQuery.SQL, null);
        try {
            while(cursor.moveToNext()) {
                String accountName = cursor.getString(SettingsMatchQuery.ACCOUNT_NAME);
                String accountType = cursor.getString(SettingsMatchQuery.ACCOUNT_TYPE);
                String value = cursor.getString(SettingsMatchQuery.SHOULD_SYNC);
                mValues.clear();
                mValues.put(android.provider.Contacts.Settings._SYNC_ACCOUNT, accountName);
                mValues.put(android.provider.Contacts.Settings._SYNC_ACCOUNT_TYPE, accountType);
                mValues.put(android.provider.Contacts.Settings.KEY,
                        android.provider.Contacts.Settings.SYNC_EVERYTHING);
                mValues.put(android.provider.Contacts.Settings.VALUE, value);
                updateSetting(db, accountName, accountType, mValues);
            }
        } finally {
            cursor.close();
        }
    }
    private void parsePeopleValues(ContentValues values) {
        mValues.clear();
        mValues2.clear();
        mValues3.clear();
        ContactsDatabaseHelper.copyStringValue(mValues, RawContacts.CUSTOM_RINGTONE,
                values, People.CUSTOM_RINGTONE);
        ContactsDatabaseHelper.copyLongValue(mValues, RawContacts.SEND_TO_VOICEMAIL,
                values, People.SEND_TO_VOICEMAIL);
        ContactsDatabaseHelper.copyLongValue(mValues, RawContacts.LAST_TIME_CONTACTED,
                values, People.LAST_TIME_CONTACTED);
        ContactsDatabaseHelper.copyLongValue(mValues, RawContacts.TIMES_CONTACTED,
                values, People.TIMES_CONTACTED);
        ContactsDatabaseHelper.copyLongValue(mValues, RawContacts.STARRED,
                values, People.STARRED);
        if (mAccount != null) {
            mValues.put(RawContacts.ACCOUNT_NAME, mAccount.name);
            mValues.put(RawContacts.ACCOUNT_TYPE, mAccount.type);
        }
        if (values.containsKey(People.NAME) || values.containsKey(People.PHONETIC_NAME)) {
            mValues2.put(Data.MIMETYPE, StructuredName.CONTENT_ITEM_TYPE);
            ContactsDatabaseHelper.copyStringValue(mValues2, StructuredName.DISPLAY_NAME,
                    values, People.NAME);
            if (values.containsKey(People.PHONETIC_NAME)) {
                String phoneticName = values.getAsString(People.PHONETIC_NAME);
                NameSplitter.Name parsedName = new NameSplitter.Name();
                mPhoneticNameSplitter.split(parsedName, phoneticName);
                mValues2.put(StructuredName.PHONETIC_GIVEN_NAME, parsedName.getGivenNames());
                mValues2.put(StructuredName.PHONETIC_MIDDLE_NAME, parsedName.getMiddleName());
                mValues2.put(StructuredName.PHONETIC_FAMILY_NAME, parsedName.getFamilyName());
            }
        }
        if (values.containsKey(People.NOTES)) {
            mValues3.put(Data.MIMETYPE, Note.CONTENT_ITEM_TYPE);
            ContactsDatabaseHelper.copyStringValue(mValues3, Note.NOTE, values, People.NOTES);
        }
    }
    private void parseOrganizationValues(ContentValues values) {
        mValues.clear();
        mValues.put(Data.MIMETYPE, Organization.CONTENT_ITEM_TYPE);
        ContactsDatabaseHelper.copyLongValue(mValues, Data.IS_PRIMARY,
                values, android.provider.Contacts.Organizations.ISPRIMARY);
        ContactsDatabaseHelper.copyStringValue(mValues, Organization.COMPANY,
                values, android.provider.Contacts.Organizations.COMPANY);
        ContactsDatabaseHelper.copyLongValue(mValues, Organization.TYPE,
                values, android.provider.Contacts.Organizations.TYPE);
        ContactsDatabaseHelper.copyStringValue(mValues, Organization.LABEL,
                values, android.provider.Contacts.Organizations.LABEL);
        ContactsDatabaseHelper.copyStringValue(mValues, Organization.TITLE,
                values, android.provider.Contacts.Organizations.TITLE);
    }
    private void parsePhoneValues(ContentValues values) {
        mValues.clear();
        mValues.put(Data.MIMETYPE, Phone.CONTENT_ITEM_TYPE);
        ContactsDatabaseHelper.copyLongValue(mValues, Data.IS_PRIMARY,
                values, android.provider.Contacts.Phones.ISPRIMARY);
        ContactsDatabaseHelper.copyStringValue(mValues, Phone.NUMBER,
                values, android.provider.Contacts.Phones.NUMBER);
        ContactsDatabaseHelper.copyLongValue(mValues, Phone.TYPE,
                values, android.provider.Contacts.Phones.TYPE);
        ContactsDatabaseHelper.copyStringValue(mValues, Phone.LABEL,
                values, android.provider.Contacts.Phones.LABEL);
    }
    private void parseContactMethodValues(int kind, ContentValues values) {
        mValues.clear();
        ContactsDatabaseHelper.copyLongValue(mValues, Data.IS_PRIMARY, values,
                ContactMethods.ISPRIMARY);
        switch (kind) {
            case android.provider.Contacts.KIND_EMAIL: {
                copyCommonFields(values, Email.CONTENT_ITEM_TYPE, Email.TYPE, Email.LABEL,
                        Data.DATA14);
                ContactsDatabaseHelper.copyStringValue(mValues, Email.DATA, values,
                        ContactMethods.DATA);
                break;
            }
            case android.provider.Contacts.KIND_IM: {
                String protocol = values.getAsString(ContactMethods.DATA);
                if (protocol.startsWith("pre:")) {
                    mValues.put(Im.PROTOCOL, Integer.parseInt(protocol.substring(4)));
                } else if (protocol.startsWith("custom:")) {
                    mValues.put(Im.PROTOCOL, Im.PROTOCOL_CUSTOM);
                    mValues.put(Im.CUSTOM_PROTOCOL, protocol.substring(7));
                }
                copyCommonFields(values, Im.CONTENT_ITEM_TYPE, Im.TYPE, Im.LABEL, Data.DATA14);
                break;
            }
            case android.provider.Contacts.KIND_POSTAL: {
                copyCommonFields(values, StructuredPostal.CONTENT_ITEM_TYPE, StructuredPostal.TYPE,
                        StructuredPostal.LABEL, Data.DATA14);
                ContactsDatabaseHelper.copyStringValue(mValues, StructuredPostal.FORMATTED_ADDRESS,
                        values, ContactMethods.DATA);
                break;
            }
        }
    }
    private void copyCommonFields(ContentValues values, String mimeType, String typeColumn,
            String labelColumn, String auxDataColumn) {
        mValues.put(Data.MIMETYPE, mimeType);
        ContactsDatabaseHelper.copyLongValue(mValues, typeColumn, values,
                ContactMethods.TYPE);
        ContactsDatabaseHelper.copyStringValue(mValues, labelColumn, values,
                ContactMethods.LABEL);
        ContactsDatabaseHelper.copyStringValue(mValues, auxDataColumn, values,
                ContactMethods.AUX_DATA);
    }
    private void parseGroupValues(ContentValues values) {
        mValues.clear();
        ContactsDatabaseHelper.copyStringValue(mValues, Groups.TITLE,
                values, android.provider.Contacts.Groups.NAME);
        ContactsDatabaseHelper.copyStringValue(mValues, Groups.NOTES,
                values, android.provider.Contacts.Groups.NOTES);
        ContactsDatabaseHelper.copyStringValue(mValues, Groups.SYSTEM_ID,
                values, android.provider.Contacts.Groups.SYSTEM_ID);
    }
    private void parseExtensionValues(ContentValues values) {
        ContactsDatabaseHelper.copyStringValue(mValues, ExtensionsColumns.NAME,
                values, android.provider.Contacts.People.Extensions.NAME);
        ContactsDatabaseHelper.copyStringValue(mValues, ExtensionsColumns.VALUE,
                values, android.provider.Contacts.People.Extensions.VALUE);
    }
    private Uri findFirstDataRow(long rawContactId, String contentItemType) {
        long dataId = findFirstDataId(rawContactId, contentItemType);
        if (dataId == -1) {
            return null;
        }
        return ContentUris.withAppendedId(Data.CONTENT_URI, dataId);
    }
    private long findFirstDataId(long rawContactId, String mimeType) {
        long dataId = -1;
        Cursor c = mContactsProvider.query(Data.CONTENT_URI, IdQuery.COLUMNS,
                Data.RAW_CONTACT_ID + "=" + rawContactId + " AND "
                        + Data.MIMETYPE + "='" + mimeType + "'",
                null, null);
        try {
            if (c.moveToFirst()) {
                dataId = c.getLong(IdQuery._ID);
            }
        } finally {
            c.close();
        }
        return dataId;
    }
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        final int match = sUriMatcher.match(uri);
        if (match == -1 || match == SETTINGS) {
            throw new UnsupportedOperationException(mDbHelper.exceptionMessage(uri));
        }
        Cursor c = query(uri, IdQuery.COLUMNS, selection, selectionArgs, null, null);
        if (c == null) {
            return 0;
        }
        int count = 0;
        try {
            while (c.moveToNext()) {
                long id = c.getLong(IdQuery._ID);
                count += delete(uri, match, id);
            }
        } finally {
            c.close();
        }
        return count;
    }
    public int delete(Uri uri, int match, long id) {
        int count = 0;
        switch (match) {
            case PEOPLE:
            case PEOPLE_ID:
                count = mContactsProvider.deleteRawContact(id, mDbHelper.getContactId(id), false);
                break;
            case PEOPLE_PHOTO:
                mValues.clear();
                mValues.putNull(android.provider.Contacts.Photos.DATA);
                updatePhoto(id, mValues);
                break;
            case ORGANIZATIONS:
            case ORGANIZATIONS_ID:
                count = mContactsProvider.deleteData(id, ORGANIZATION_MIME_TYPES);
                break;
            case CONTACTMETHODS:
            case CONTACTMETHODS_ID:
                count = mContactsProvider.deleteData(id, CONTACT_METHOD_MIME_TYPES);
                break;
            case PHONES:
            case PHONES_ID:
                count = mContactsProvider.deleteData(id, PHONE_MIME_TYPES);
                break;
            case EXTENSIONS:
            case EXTENSIONS_ID:
                count = mContactsProvider.deleteData(id, EXTENSION_MIME_TYPES);
                break;
            case PHOTOS:
            case PHOTOS_ID:
                count = mContactsProvider.deleteData(id, PHOTO_MIME_TYPES);
                break;
            case GROUPMEMBERSHIP:
            case GROUPMEMBERSHIP_ID:
                count = mContactsProvider.deleteData(id, GROUP_MEMBERSHIP_MIME_TYPES);
                break;
            case GROUPS:
            case GROUPS_ID:
                count = mContactsProvider.deleteGroup(uri, id, false);
                break;
            default:
                throw new UnsupportedOperationException(mDbHelper.exceptionMessage(uri));
        }
        return count;
    }
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs,
            String sortOrder, String limit) {
        ensureDefaultAccount();
        final SQLiteDatabase db = mDbHelper.getReadableDatabase();
        SQLiteQueryBuilder qb = new SQLiteQueryBuilder();
        String groupBy = null;
        final int match = sUriMatcher.match(uri);
        switch (match) {
            case PEOPLE: {
                qb.setTables(LegacyTables.PEOPLE_JOIN_PRESENCE);
                qb.setProjectionMap(sPeopleProjectionMap);
                applyRawContactsAccount(qb);
                break;
            }
            case PEOPLE_ID:
                qb.setTables(LegacyTables.PEOPLE_JOIN_PRESENCE);
                qb.setProjectionMap(sPeopleProjectionMap);
                applyRawContactsAccount(qb);
                qb.appendWhere(" AND " + People._ID + "=");
                qb.appendWhere(uri.getPathSegments().get(1));
                break;
            case PEOPLE_FILTER: {
                qb.setTables(LegacyTables.PEOPLE_JOIN_PRESENCE);
                qb.setProjectionMap(sPeopleProjectionMap);
                applyRawContactsAccount(qb);
                String filterParam = uri.getPathSegments().get(2);
                qb.appendWhere(" AND " + People._ID + " IN "
                        + mContactsProvider.getRawContactsByFilterAsNestedQuery(filterParam));
                break;
            }
            case GROUP_NAME_MEMBERS:
                qb.setTables(LegacyTables.PEOPLE_JOIN_PRESENCE);
                qb.setProjectionMap(sPeopleProjectionMap);
                applyRawContactsAccount(qb);
                String group = uri.getPathSegments().get(2);
                qb.appendWhere(" AND " + buildGroupNameMatchWhereClause(group));
                break;
            case GROUP_SYSTEM_ID_MEMBERS:
                qb.setTables(LegacyTables.PEOPLE_JOIN_PRESENCE);
                qb.setProjectionMap(sPeopleProjectionMap);
                applyRawContactsAccount(qb);
                String systemId = uri.getPathSegments().get(2);
                qb.appendWhere(" AND " + buildGroupSystemIdMatchWhereClause(systemId));
                break;
            case ORGANIZATIONS:
                qb.setTables(LegacyTables.ORGANIZATIONS + " organizations");
                qb.setProjectionMap(sOrganizationProjectionMap);
                applyRawContactsAccount(qb);
                break;
            case ORGANIZATIONS_ID:
                qb.setTables(LegacyTables.ORGANIZATIONS + " organizations");
                qb.setProjectionMap(sOrganizationProjectionMap);
                applyRawContactsAccount(qb);
                qb.appendWhere(" AND " + android.provider.Contacts.Organizations._ID + "=");
                qb.appendWhere(uri.getPathSegments().get(1));
                break;
            case PEOPLE_ORGANIZATIONS:
                qb.setTables(LegacyTables.ORGANIZATIONS + " organizations");
                qb.setProjectionMap(sOrganizationProjectionMap);
                applyRawContactsAccount(qb);
                qb.appendWhere(" AND " + android.provider.Contacts.Organizations.PERSON_ID + "=");
                qb.appendWhere(uri.getPathSegments().get(1));
                break;
            case PEOPLE_ORGANIZATIONS_ID:
                qb.setTables(LegacyTables.ORGANIZATIONS + " organizations");
                qb.setProjectionMap(sOrganizationProjectionMap);
                applyRawContactsAccount(qb);
                qb.appendWhere(" AND " + android.provider.Contacts.Organizations.PERSON_ID + "=");
                qb.appendWhere(uri.getPathSegments().get(1));
                qb.appendWhere(" AND " + android.provider.Contacts.Organizations._ID + "=");
                qb.appendWhere(uri.getPathSegments().get(3));
                break;
            case CONTACTMETHODS:
                qb.setTables(LegacyTables.CONTACT_METHODS + " contact_methods");
                qb.setProjectionMap(sContactMethodProjectionMap);
                applyRawContactsAccount(qb);
                break;
            case CONTACTMETHODS_ID:
                qb.setTables(LegacyTables.CONTACT_METHODS + " contact_methods");
                qb.setProjectionMap(sContactMethodProjectionMap);
                applyRawContactsAccount(qb);
                qb.appendWhere(" AND " + ContactMethods._ID + "=");
                qb.appendWhere(uri.getPathSegments().get(1));
                break;
            case CONTACTMETHODS_EMAIL:
                qb.setTables(LegacyTables.CONTACT_METHODS + " contact_methods");
                qb.setProjectionMap(sContactMethodProjectionMap);
                applyRawContactsAccount(qb);
                qb.appendWhere(" AND " + ContactMethods.KIND + "="
                        + android.provider.Contacts.KIND_EMAIL);
                break;
            case PEOPLE_CONTACTMETHODS:
                qb.setTables(LegacyTables.CONTACT_METHODS + " contact_methods");
                qb.setProjectionMap(sContactMethodProjectionMap);
                applyRawContactsAccount(qb);
                qb.appendWhere(" AND " + ContactMethods.PERSON_ID + "=");
                qb.appendWhere(uri.getPathSegments().get(1));
                qb.appendWhere(" AND " + ContactMethods.KIND + " IS NOT NULL");
                break;
            case PEOPLE_CONTACTMETHODS_ID:
                qb.setTables(LegacyTables.CONTACT_METHODS + " contact_methods");
                qb.setProjectionMap(sContactMethodProjectionMap);
                applyRawContactsAccount(qb);
                qb.appendWhere(" AND " + ContactMethods.PERSON_ID + "=");
                qb.appendWhere(uri.getPathSegments().get(1));
                qb.appendWhere(" AND " + ContactMethods._ID + "=");
                qb.appendWhere(uri.getPathSegments().get(3));
                qb.appendWhere(" AND " + ContactMethods.KIND + " IS NOT NULL");
                break;
            case PHONES:
                qb.setTables(LegacyTables.PHONES + " phones");
                qb.setProjectionMap(sPhoneProjectionMap);
                applyRawContactsAccount(qb);
                break;
            case PHONES_ID:
                qb.setTables(LegacyTables.PHONES + " phones");
                qb.setProjectionMap(sPhoneProjectionMap);
                applyRawContactsAccount(qb);
                qb.appendWhere(" AND " + android.provider.Contacts.Phones._ID + "=");
                qb.appendWhere(uri.getPathSegments().get(1));
                break;
            case PHONES_FILTER:
                qb.setTables(LegacyTables.PHONES + " phones");
                qb.setProjectionMap(sPhoneProjectionMap);
                applyRawContactsAccount(qb);
                if (uri.getPathSegments().size() > 2) {
                    String filterParam = uri.getLastPathSegment();
                    qb.appendWhere(" AND person =");
                    qb.appendWhere(mDbHelper.buildPhoneLookupAsNestedQuery(filterParam));
                }
                break;
            case PEOPLE_PHONES:
                qb.setTables(LegacyTables.PHONES + " phones");
                qb.setProjectionMap(sPhoneProjectionMap);
                applyRawContactsAccount(qb);
                qb.appendWhere(" AND " + android.provider.Contacts.Phones.PERSON_ID + "=");
                qb.appendWhere(uri.getPathSegments().get(1));
                break;
            case PEOPLE_PHONES_ID:
                qb.setTables(LegacyTables.PHONES + " phones");
                qb.setProjectionMap(sPhoneProjectionMap);
                applyRawContactsAccount(qb);
                qb.appendWhere(" AND " + android.provider.Contacts.Phones.PERSON_ID + "=");
                qb.appendWhere(uri.getPathSegments().get(1));
                qb.appendWhere(" AND " + android.provider.Contacts.Phones._ID + "=");
                qb.appendWhere(uri.getPathSegments().get(3));
                break;
            case EXTENSIONS:
                qb.setTables(LegacyTables.EXTENSIONS + " extensions");
                qb.setProjectionMap(sExtensionProjectionMap);
                applyRawContactsAccount(qb);
                break;
            case EXTENSIONS_ID:
                qb.setTables(LegacyTables.EXTENSIONS + " extensions");
                qb.setProjectionMap(sExtensionProjectionMap);
                applyRawContactsAccount(qb);
                qb.appendWhere(" AND " + android.provider.Contacts.Extensions._ID + "=");
                qb.appendWhere(uri.getPathSegments().get(1));
                break;
            case PEOPLE_EXTENSIONS:
                qb.setTables(LegacyTables.EXTENSIONS + " extensions");
                qb.setProjectionMap(sExtensionProjectionMap);
                applyRawContactsAccount(qb);
                qb.appendWhere(" AND " + android.provider.Contacts.Extensions.PERSON_ID + "=");
                qb.appendWhere(uri.getPathSegments().get(1));
                break;
            case PEOPLE_EXTENSIONS_ID:
                qb.setTables(LegacyTables.EXTENSIONS + " extensions");
                qb.setProjectionMap(sExtensionProjectionMap);
                applyRawContactsAccount(qb);
                qb.appendWhere(" AND " + android.provider.Contacts.Extensions.PERSON_ID + "=");
                qb.appendWhere(uri.getPathSegments().get(1));
                qb.appendWhere(" AND " + android.provider.Contacts.Extensions._ID + "=");
                qb.appendWhere(uri.getPathSegments().get(3));
                break;
            case GROUPS:
                qb.setTables(LegacyTables.GROUPS + " groups");
                qb.setProjectionMap(sGroupProjectionMap);
                applyGroupAccount(qb);
                break;
            case GROUPS_ID:
                qb.setTables(LegacyTables.GROUPS + " groups");
                qb.setProjectionMap(sGroupProjectionMap);
                applyGroupAccount(qb);
                qb.appendWhere(" AND " + android.provider.Contacts.Groups._ID + "=");
                qb.appendWhere(uri.getPathSegments().get(1));
                break;
            case GROUPMEMBERSHIP:
                qb.setTables(LegacyTables.GROUP_MEMBERSHIP + " groupmembership");
                qb.setProjectionMap(sGroupMembershipProjectionMap);
                applyRawContactsAccount(qb);
                break;
            case GROUPMEMBERSHIP_ID:
                qb.setTables(LegacyTables.GROUP_MEMBERSHIP + " groupmembership");
                qb.setProjectionMap(sGroupMembershipProjectionMap);
                applyRawContactsAccount(qb);
                qb.appendWhere(" AND " + android.provider.Contacts.GroupMembership._ID + "=");
                qb.appendWhere(uri.getPathSegments().get(1));
                break;
            case PEOPLE_GROUPMEMBERSHIP:
                qb.setTables(LegacyTables.GROUP_MEMBERSHIP + " groupmembership");
                qb.setProjectionMap(sGroupMembershipProjectionMap);
                applyRawContactsAccount(qb);
                qb.appendWhere(" AND " + android.provider.Contacts.GroupMembership.PERSON_ID + "=");
                qb.appendWhere(uri.getPathSegments().get(1));
                break;
            case PEOPLE_GROUPMEMBERSHIP_ID:
                qb.setTables(LegacyTables.GROUP_MEMBERSHIP + " groupmembership");
                qb.setProjectionMap(sGroupMembershipProjectionMap);
                applyRawContactsAccount(qb);
                qb.appendWhere(" AND " + android.provider.Contacts.GroupMembership.PERSON_ID + "=");
                qb.appendWhere(uri.getPathSegments().get(1));
                qb.appendWhere(" AND " + android.provider.Contacts.GroupMembership._ID + "=");
                qb.appendWhere(uri.getPathSegments().get(3));
                break;
            case PEOPLE_PHOTO:
                qb.setTables(LegacyTables.PHOTOS + " photos");
                qb.setProjectionMap(sPhotoProjectionMap);
                applyRawContactsAccount(qb);
                qb.appendWhere(" AND " + android.provider.Contacts.Photos.PERSON_ID + "=");
                qb.appendWhere(uri.getPathSegments().get(1));
                limit = "1";
                break;
            case PHOTOS:
                qb.setTables(LegacyTables.PHOTOS + " photos");
                qb.setProjectionMap(sPhotoProjectionMap);
                applyRawContactsAccount(qb);
                break;
            case PHOTOS_ID:
                qb.setTables(LegacyTables.PHOTOS + " photos");
                qb.setProjectionMap(sPhotoProjectionMap);
                applyRawContactsAccount(qb);
                qb.appendWhere(" AND " + android.provider.Contacts.Photos._ID + "=");
                qb.appendWhere(uri.getPathSegments().get(1));
                break;
            case SEARCH_SUGGESTIONS:
                return mGlobalSearchSupport.handleSearchSuggestionsQuery(db, uri, limit);
            case SEARCH_SHORTCUT: {
                String lookupKey = uri.getLastPathSegment();
                return mGlobalSearchSupport.handleSearchShortcutRefresh(db, lookupKey, projection);
            }
            case LIVE_FOLDERS_PEOPLE:
                return mContactsProvider.query(LIVE_FOLDERS_CONTACTS_URI,
                        projection, selection, selectionArgs, sortOrder);
            case LIVE_FOLDERS_PEOPLE_WITH_PHONES:
                return mContactsProvider.query(LIVE_FOLDERS_CONTACTS_WITH_PHONES_URI,
                        projection, selection, selectionArgs, sortOrder);
            case LIVE_FOLDERS_PEOPLE_FAVORITES:
                return mContactsProvider.query(LIVE_FOLDERS_CONTACTS_FAVORITES_URI,
                        projection, selection, selectionArgs, sortOrder);
            case LIVE_FOLDERS_PEOPLE_GROUP_NAME:
                return mContactsProvider.query(Uri.withAppendedPath(LIVE_FOLDERS_CONTACTS_URI,
                        Uri.encode(uri.getLastPathSegment())),
                        projection, selection, selectionArgs, sortOrder);
            case DELETED_PEOPLE:
            case DELETED_GROUPS:
                throw new UnsupportedOperationException(mDbHelper.exceptionMessage(uri));
            case SETTINGS:
                copySettingsToLegacySettings();
                qb.setTables(LegacyTables.SETTINGS);
                break;
            default:
                throw new IllegalArgumentException(mDbHelper.exceptionMessage(uri));
        }
        final Cursor c = qb.query(db, projection, selection, selectionArgs,
                groupBy, null, sortOrder, limit);
        if (c != null) {
            c.setNotificationUri(mContext.getContentResolver(),
                    android.provider.Contacts.CONTENT_URI);
        }
        return c;
    }
    private void applyRawContactsAccount(SQLiteQueryBuilder qb) {
        StringBuilder sb = new StringBuilder();
        appendRawContactsAccount(sb);
        qb.appendWhere(sb.toString());
    }
    private void appendRawContactsAccount(StringBuilder sb) {
        if (mAccount != null) {
            sb.append(RawContacts.ACCOUNT_NAME + "=");
            DatabaseUtils.appendEscapedSQLString(sb, mAccount.name);
            sb.append(" AND " + RawContacts.ACCOUNT_TYPE + "=");
            DatabaseUtils.appendEscapedSQLString(sb, mAccount.type);
        } else {
            sb.append(RawContacts.ACCOUNT_NAME + " IS NULL" +
                    " AND " + RawContacts.ACCOUNT_TYPE + " IS NULL");
        }
    }
    private void applyGroupAccount(SQLiteQueryBuilder qb) {
        StringBuilder sb = new StringBuilder();
        appendGroupAccount(sb);
        qb.appendWhere(sb.toString());
    }
    private void appendGroupAccount(StringBuilder sb) {
        if (mAccount != null) {
            sb.append(Groups.ACCOUNT_NAME + "=");
            DatabaseUtils.appendEscapedSQLString(sb, mAccount.name);
            sb.append(" AND " + Groups.ACCOUNT_TYPE + "=");
            DatabaseUtils.appendEscapedSQLString(sb, mAccount.type);
        } else {
            sb.append(Groups.ACCOUNT_NAME + " IS NULL" +
                    " AND " + Groups.ACCOUNT_TYPE + " IS NULL");
        }
    }
    private String buildGroupNameMatchWhereClause(String groupName) {
        return "people._id IN "
                + "(SELECT " + DataColumns.CONCRETE_RAW_CONTACT_ID
                + " FROM " + Tables.DATA_JOIN_MIMETYPES
                + " WHERE " + Data.MIMETYPE + "='" + GroupMembership.CONTENT_ITEM_TYPE
                        + "' AND " + GroupMembership.GROUP_ROW_ID + "="
                                + "(SELECT " + Tables.GROUPS + "." + Groups._ID
                                + " FROM " + Tables.GROUPS
                                + " WHERE " + Groups.TITLE + "="
                                        + DatabaseUtils.sqlEscapeString(groupName) + "))";
    }
    private String buildGroupSystemIdMatchWhereClause(String systemId) {
        return "people._id IN "
                + "(SELECT " + DataColumns.CONCRETE_RAW_CONTACT_ID
                + " FROM " + Tables.DATA_JOIN_MIMETYPES
                + " WHERE " + Data.MIMETYPE + "='" + GroupMembership.CONTENT_ITEM_TYPE
                        + "' AND " + GroupMembership.GROUP_ROW_ID + "="
                                + "(SELECT " + Tables.GROUPS + "." + Groups._ID
                                + " FROM " + Tables.GROUPS
                                + " WHERE " + Groups.SYSTEM_ID + "="
                                        + DatabaseUtils.sqlEscapeString(systemId) + "))";
    }
    private void onChange(Uri uri) {
        mContext.getContentResolver().notifyChange(android.provider.Contacts.CONTENT_URI, null);
    }
    public String getType(Uri uri) {
        int match = sUriMatcher.match(uri);
        switch (match) {
            case EXTENSIONS:
            case PEOPLE_EXTENSIONS:
                return Extensions.CONTENT_TYPE;
            case EXTENSIONS_ID:
            case PEOPLE_EXTENSIONS_ID:
                return Extensions.CONTENT_ITEM_TYPE;
            case PEOPLE:
                return "vnd.android.cursor.dir/person";
            case PEOPLE_ID:
                return "vnd.android.cursor.item/person";
            case PEOPLE_PHONES:
                return "vnd.android.cursor.dir/phone";
            case PEOPLE_PHONES_ID:
                return "vnd.android.cursor.item/phone";
            case PEOPLE_CONTACTMETHODS:
                return "vnd.android.cursor.dir/contact-methods";
            case PEOPLE_CONTACTMETHODS_ID:
                return getContactMethodType(uri);
            case PHONES:
                return "vnd.android.cursor.dir/phone";
            case PHONES_ID:
                return "vnd.android.cursor.item/phone";
            case PHONES_FILTER:
                return "vnd.android.cursor.dir/phone";
            case PHOTOS_ID:
                return "vnd.android.cursor.item/photo";
            case PHOTOS:
                return "vnd.android.cursor.dir/photo";
            case PEOPLE_PHOTO:
                return "vnd.android.cursor.item/photo";
            case CONTACTMETHODS:
                return "vnd.android.cursor.dir/contact-methods";
            case CONTACTMETHODS_ID:
                return getContactMethodType(uri);
            case ORGANIZATIONS:
                return "vnd.android.cursor.dir/organizations";
            case ORGANIZATIONS_ID:
                return "vnd.android.cursor.item/organization";
            case SEARCH_SUGGESTIONS:
                return SearchManager.SUGGEST_MIME_TYPE;
            case SEARCH_SHORTCUT:
                return SearchManager.SHORTCUT_MIME_TYPE;
            default:
                throw new IllegalArgumentException(mDbHelper.exceptionMessage(uri));
        }
    }
    private String getContactMethodType(Uri url) {
        String mime = null;
        Cursor c = query(url, new String[] {ContactMethods.KIND}, null, null, null, null);
        if (c != null) {
            try {
                if (c.moveToFirst()) {
                    int kind = c.getInt(0);
                    switch (kind) {
                    case android.provider.Contacts.KIND_EMAIL:
                        mime = "vnd.android.cursor.item/email";
                        break;
                    case android.provider.Contacts.KIND_IM:
                        mime = "vnd.android.cursor.item/jabber-im";
                        break;
                    case android.provider.Contacts.KIND_POSTAL:
                        mime = "vnd.android.cursor.item/postal-address";
                        break;
                    }
                }
            } finally {
                c.close();
            }
        }
        return mime;
    }
}
