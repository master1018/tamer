public class Contacts {
    private static final String TAG = "Contacts";
    @Deprecated
    public static final String AUTHORITY = "contacts";
    @Deprecated
    public static final Uri CONTENT_URI = Uri.parse("content:
    @Deprecated
    public static final int KIND_EMAIL = 1;
    @Deprecated
    public static final int KIND_POSTAL = 2;
    @Deprecated
    public static final int KIND_IM = 3;
    @Deprecated
    public static final int KIND_ORGANIZATION = 4;
    @Deprecated
    public static final int KIND_PHONE = 5;
    private Contacts() {}
    @Deprecated
    public interface SettingsColumns {
        @Deprecated
        public static final String _SYNC_ACCOUNT = "_sync_account";
        @Deprecated
        public static final String _SYNC_ACCOUNT_TYPE = "_sync_account_type";
        @Deprecated
        public static final String KEY = "key";
        @Deprecated
        public static final String VALUE = "value";
    }
    @Deprecated
    public static final class Settings implements BaseColumns, SettingsColumns {
        private Settings() {}
        @Deprecated
        public static final Uri CONTENT_URI =
            Uri.parse("content:
        @Deprecated
        public static final String CONTENT_DIRECTORY = "settings";
        @Deprecated
        public static final String DEFAULT_SORT_ORDER = "key ASC";
        @Deprecated
        public static final String SYNC_EVERYTHING = "syncEverything";
        @Deprecated
        public static String getSetting(ContentResolver cr, String account, String key) {
            String selectString;
            String[] selectArgs;
            if (false) {
                selectString = (account == null)
                        ? "_sync_account is null AND key=?"
                        : "_sync_account=? AND key=?";
                selectArgs = (account == null)
                ? new String[]{key}
                : new String[]{account, key};
            } else {
                selectString = "key=?";
                selectArgs = new String[] {key};
            }
            Cursor cursor = cr.query(Settings.CONTENT_URI, new String[]{VALUE},
                    selectString, selectArgs, null);
            try {
                if (!cursor.moveToNext()) return null;
                return cursor.getString(0);
            } finally {
                cursor.close();
            }
        }
        @Deprecated
        public static void setSetting(ContentResolver cr, String account, String key,
                String value) {
            ContentValues values = new ContentValues();
            values.put(KEY, key);
            values.put(VALUE, value);
            cr.update(Settings.CONTENT_URI, values, null, null);
        }
    }
    @Deprecated
    public interface PeopleColumns {
        @Deprecated
        public static final String NAME = "name";
        @Deprecated
        public static final String PHONETIC_NAME = "phonetic_name";
        @Deprecated
        public static final String DISPLAY_NAME = "display_name";
        @Deprecated
        public static final String SORT_STRING = "sort_string";
        @Deprecated
        public static final String NOTES = "notes";
        @Deprecated
        public static final String TIMES_CONTACTED = "times_contacted";
        @Deprecated
        public static final String LAST_TIME_CONTACTED = "last_time_contacted";
        @Deprecated
        public static final String CUSTOM_RINGTONE = "custom_ringtone";
        @Deprecated
        public static final String SEND_TO_VOICEMAIL = "send_to_voicemail";
        @Deprecated
        public static final String STARRED = "starred";
        @Deprecated
        public static final String PHOTO_VERSION = "photo_version";
    }
    @Deprecated
    public static final class People implements BaseColumns, SyncConstValue, PeopleColumns,
            PhonesColumns, PresenceColumns {
        private People() {}
        @Deprecated
        public static final Uri CONTENT_URI =
            Uri.parse("content:
        @Deprecated
        public static final Uri CONTENT_FILTER_URI =
            Uri.parse("content:
        @Deprecated
        public static final Uri DELETED_CONTENT_URI =
            Uri.parse("content:
        @Deprecated
        public static final Uri WITH_EMAIL_OR_IM_FILTER_URI =
            Uri.parse("content:
        @Deprecated
        public static final String CONTENT_TYPE = "vnd.android.cursor.dir/person";
        @Deprecated
        public static final String CONTENT_ITEM_TYPE = "vnd.android.cursor.item/person";
        @Deprecated
        public static final String DEFAULT_SORT_ORDER = People.NAME + " ASC";
        @Deprecated
        public static final String PRIMARY_PHONE_ID = "primary_phone";
        @Deprecated
        public static final String PRIMARY_EMAIL_ID = "primary_email";
        @Deprecated
        public static final String PRIMARY_ORGANIZATION_ID = "primary_organization";
        @Deprecated
        public static void markAsContacted(ContentResolver resolver, long personId) {
            Uri uri = ContentUris.withAppendedId(CONTENT_URI, personId);
            uri = Uri.withAppendedPath(uri, "update_contact_time");
            ContentValues values = new ContentValues();
            values.put(LAST_TIME_CONTACTED, System.currentTimeMillis());
            resolver.update(uri, values, null, null);
        }
        @Deprecated
        public static long tryGetMyContactsGroupId(ContentResolver resolver) {
            Cursor groupsCursor = resolver.query(Groups.CONTENT_URI, GROUPS_PROJECTION,
                    Groups.SYSTEM_ID + "='" + Groups.GROUP_MY_CONTACTS + "'", null, null);
            if (groupsCursor != null) {
                try {
                    if (groupsCursor.moveToFirst()) {
                        return groupsCursor.getLong(0);
                    }
                } finally {
                    groupsCursor.close();
                }
            }
            return 0;
        }
        @Deprecated
        public static Uri addToMyContactsGroup(ContentResolver resolver, long personId) {
            long groupId = tryGetMyContactsGroupId(resolver);
            if (groupId == 0) {
                throw new IllegalStateException("Failed to find the My Contacts group");
            }
            return addToGroup(resolver, personId, groupId);
        }
        @Deprecated
        public static Uri addToGroup(ContentResolver resolver, long personId, String groupName) {
            long groupId = 0;
            Cursor groupsCursor = resolver.query(Groups.CONTENT_URI, GROUPS_PROJECTION,
                    Groups.NAME + "=?", new String[] { groupName }, null);
            if (groupsCursor != null) {
                try {
                    if (groupsCursor.moveToFirst()) {
                        groupId = groupsCursor.getLong(0);
                    }
                } finally {
                    groupsCursor.close();
                }
            }
            if (groupId == 0) {
                throw new IllegalStateException("Failed to find the My Contacts group");
            }
            return addToGroup(resolver, personId, groupId);
        }
        @Deprecated
        public static Uri addToGroup(ContentResolver resolver, long personId, long groupId) {
            ContentValues values = new ContentValues();
            values.put(GroupMembership.PERSON_ID, personId);
            values.put(GroupMembership.GROUP_ID, groupId);
            return resolver.insert(GroupMembership.CONTENT_URI, values);
        }
        private static final String[] GROUPS_PROJECTION = new String[] {
            Groups._ID,
        };
        @Deprecated
        public static Uri createPersonInMyContactsGroup(ContentResolver resolver,
                ContentValues values) {
            Uri contactUri = resolver.insert(People.CONTENT_URI, values);
            if (contactUri == null) {
                Log.e(TAG, "Failed to create the contact");
                return null;
            }
            if (addToMyContactsGroup(resolver, ContentUris.parseId(contactUri)) == null) {
                resolver.delete(contactUri, null, null);
                return null;
            }
            return contactUri;
        }
        @Deprecated
        public static Cursor queryGroups(ContentResolver resolver, long person) {
            return resolver.query(GroupMembership.CONTENT_URI, null, "person=?",
                    new String[]{String.valueOf(person)}, Groups.DEFAULT_SORT_ORDER);
        }
        @Deprecated
        public static void setPhotoData(ContentResolver cr, Uri person, byte[] data) {
            Uri photoUri = Uri.withAppendedPath(person, Contacts.Photos.CONTENT_DIRECTORY);
            ContentValues values = new ContentValues();
            values.put(Photos.DATA, data);
            cr.update(photoUri, values, null, null);
        }
        @Deprecated
        public static InputStream openContactPhotoInputStream(ContentResolver cr, Uri person) {
            Uri photoUri = Uri.withAppendedPath(person, Contacts.Photos.CONTENT_DIRECTORY);
            Cursor cursor = cr.query(photoUri, new String[]{Photos.DATA}, null, null, null);
            try {
                if (cursor == null || !cursor.moveToNext()) {
                    return null;
                }
                byte[] data = cursor.getBlob(0);
                if (data == null) {
                    return null;
                }
                return new ByteArrayInputStream(data);
            } finally {
                if (cursor != null) cursor.close();
            }
        }
        @Deprecated
        public static Bitmap loadContactPhoto(Context context, Uri person,
                int placeholderImageResource, BitmapFactory.Options options) {
            if (person == null) {
                return loadPlaceholderPhoto(placeholderImageResource, context, options);
            }
            InputStream stream = openContactPhotoInputStream(context.getContentResolver(), person);
            Bitmap bm = stream != null ? BitmapFactory.decodeStream(stream, null, options) : null;
            if (bm == null) {
                bm = loadPlaceholderPhoto(placeholderImageResource, context, options);
            }
            return bm;
        }
        private static Bitmap loadPlaceholderPhoto(int placeholderImageResource, Context context,
                BitmapFactory.Options options) {
            if (placeholderImageResource == 0) {
                return null;
            }
            return BitmapFactory.decodeResource(context.getResources(),
                    placeholderImageResource, options);
        }
        @Deprecated
        public static final class Phones implements BaseColumns, PhonesColumns,
                PeopleColumns {
            private Phones() {}
            @Deprecated
            public static final String CONTENT_DIRECTORY = "phones";
            @Deprecated
            public static final String DEFAULT_SORT_ORDER = "number ASC";
        }
        @Deprecated
        public static final class ContactMethods
                implements BaseColumns, ContactMethodsColumns, PeopleColumns {
            private ContactMethods() {}
            @Deprecated
            public static final String CONTENT_DIRECTORY = "contact_methods";
            @Deprecated
            public static final String DEFAULT_SORT_ORDER = "data ASC";
        }
        @Deprecated
        public static class Extensions implements BaseColumns, ExtensionsColumns {
            private Extensions() {}
            @Deprecated
            public static final String CONTENT_DIRECTORY = "extensions";
            @Deprecated
            public static final String DEFAULT_SORT_ORDER = "name ASC";
            @Deprecated
            public static final String PERSON_ID = "person";
        }
    }
    @Deprecated
    public interface GroupsColumns {
        @Deprecated
        public static final String NAME = "name";
        @Deprecated
        public static final String NOTES = "notes";
        @Deprecated
        public static final String SHOULD_SYNC = "should_sync";
        @Deprecated
        public static final String SYSTEM_ID = "system_id";
    }
    @Deprecated
    public static final class Groups
            implements BaseColumns, SyncConstValue, GroupsColumns {
        private Groups() {}
        @Deprecated
        public static final Uri CONTENT_URI =
            Uri.parse("content:
        @Deprecated
        public static final Uri DELETED_CONTENT_URI =
            Uri.parse("content:
        @Deprecated
        public static final String CONTENT_TYPE = "vnd.android.cursor.dir/contactsgroup";
        @Deprecated
        public static final String CONTENT_ITEM_TYPE = "vnd.android.cursor.item/contactsgroup";
        @Deprecated
        public static final String DEFAULT_SORT_ORDER = NAME + " ASC";
        @Deprecated
        public static final String GROUP_ANDROID_STARRED = "Starred in Android";
        @Deprecated
        public static final String GROUP_MY_CONTACTS = "Contacts";
    }
    @Deprecated
    public interface PhonesColumns {
        @Deprecated
        public static final String TYPE = "type";
        @Deprecated
        public static final int TYPE_CUSTOM = 0;
        @Deprecated
        public static final int TYPE_HOME = 1;
        @Deprecated
        public static final int TYPE_MOBILE = 2;
        @Deprecated
        public static final int TYPE_WORK = 3;
        @Deprecated
        public static final int TYPE_FAX_WORK = 4;
        @Deprecated
        public static final int TYPE_FAX_HOME = 5;
        @Deprecated
        public static final int TYPE_PAGER = 6;
        @Deprecated
        public static final int TYPE_OTHER = 7;
        @Deprecated
        public static final String LABEL = "label";
        @Deprecated
        public static final String NUMBER = "number";
        @Deprecated
        public static final String NUMBER_KEY = "number_key";
        @Deprecated
        public static final String ISPRIMARY = "isprimary";
    }
    @Deprecated
    public static final class Phones
            implements BaseColumns, PhonesColumns, PeopleColumns {
        private Phones() {}
        @Deprecated
        public static final CharSequence getDisplayLabel(Context context, int type,
                CharSequence label, CharSequence[] labelArray) {
            CharSequence display = "";
            if (type != People.Phones.TYPE_CUSTOM) {
                CharSequence[] labels = labelArray != null? labelArray
                        : context.getResources().getTextArray(
                                com.android.internal.R.array.phoneTypes);
                try {
                    display = labels[type - 1];
                } catch (ArrayIndexOutOfBoundsException e) {
                    display = labels[People.Phones.TYPE_HOME - 1];
                }
            } else {
                if (!TextUtils.isEmpty(label)) {
                    display = label;
                }
            }
            return display;
        }
        @Deprecated
        public static final CharSequence getDisplayLabel(Context context, int type,
                CharSequence label) {
            return getDisplayLabel(context, type, label, null);
        }
        @Deprecated
        public static final Uri CONTENT_URI =
            Uri.parse("content:
        @Deprecated
        public static final Uri CONTENT_FILTER_URL =
            Uri.parse("content:
        @Deprecated
        public static final String CONTENT_TYPE = "vnd.android.cursor.dir/phone";
        @Deprecated
        public static final String CONTENT_ITEM_TYPE = "vnd.android.cursor.item/phone";
        @Deprecated
        public static final String DEFAULT_SORT_ORDER = "name ASC";
        @Deprecated
        public static final String PERSON_ID = "person";
    }
    @Deprecated
    public static final class GroupMembership implements BaseColumns, GroupsColumns {
        private GroupMembership() {}
        @Deprecated
        public static final Uri CONTENT_URI =
            Uri.parse("content:
        @Deprecated
        public static final Uri RAW_CONTENT_URI =
            Uri.parse("content:
        @Deprecated
        public static final String CONTENT_DIRECTORY = "groupmembership";
        @Deprecated
        public static final String CONTENT_TYPE = "vnd.android.cursor.dir/contactsgroupmembership";
        @Deprecated
        public static final String CONTENT_ITEM_TYPE =
                "vnd.android.cursor.item/contactsgroupmembership";
        @Deprecated
        public static final String DEFAULT_SORT_ORDER = "group_id ASC";
        @Deprecated
        public static final String GROUP_ID = "group_id";
        @Deprecated
        public static final String GROUP_SYNC_ID = "group_sync_id";
        @Deprecated
        public static final String GROUP_SYNC_ACCOUNT = "group_sync_account";
        @Deprecated
        public static final String GROUP_SYNC_ACCOUNT_TYPE = "group_sync_account_type";
        @Deprecated
        public static final String PERSON_ID = "person";
    }
    @Deprecated
    public interface ContactMethodsColumns {
        @Deprecated
        public static final String KIND = "kind";
        @Deprecated
        public static final String TYPE = "type";
        @Deprecated
        public static final int TYPE_CUSTOM = 0;
        @Deprecated
        public static final int TYPE_HOME = 1;
        @Deprecated
        public static final int TYPE_WORK = 2;
        @Deprecated
        public static final int TYPE_OTHER = 3;
        @Deprecated
        public static final int MOBILE_EMAIL_TYPE_INDEX = 2;
        @Deprecated
        public static final String MOBILE_EMAIL_TYPE_NAME = "_AUTO_CELL";
        @Deprecated
        public static final String LABEL = "label";
        @Deprecated
        public static final String DATA = "data";
        @Deprecated
        public static final String AUX_DATA = "aux_data";
        @Deprecated
        public static final String ISPRIMARY = "isprimary";
    }
    @Deprecated
    public static final class ContactMethods
            implements BaseColumns, ContactMethodsColumns, PeopleColumns {
        @Deprecated
        public static final String POSTAL_LOCATION_LATITUDE = DATA;
        @Deprecated
        public static final String POSTAL_LOCATION_LONGITUDE = AUX_DATA;
        @Deprecated
        public static final int PROTOCOL_AIM = 0;
        @Deprecated
        public static final int PROTOCOL_MSN = 1;
        @Deprecated
        public static final int PROTOCOL_YAHOO = 2;
        @Deprecated
        public static final int PROTOCOL_SKYPE = 3;
        @Deprecated
        public static final int PROTOCOL_QQ = 4;
        @Deprecated
        public static final int PROTOCOL_GOOGLE_TALK = 5;
        @Deprecated
        public static final int PROTOCOL_ICQ = 6;
        @Deprecated
        public static final int PROTOCOL_JABBER = 7;
        @Deprecated
        public static String encodePredefinedImProtocol(int protocol) {
            return "pre:" + protocol;
        }
        @Deprecated
        public static String encodeCustomImProtocol(String protocolString) {
            return "custom:" + protocolString;
        }
        @Deprecated
        public static Object decodeImProtocol(String encodedString) {
            if (encodedString == null) {
                return null;
            }
            if (encodedString.startsWith("pre:")) {
                return Integer.parseInt(encodedString.substring(4));
            }
            if (encodedString.startsWith("custom:")) {
                return encodedString.substring(7);
            }
            throw new IllegalArgumentException(
                    "the value is not a valid encoded protocol, " + encodedString);
        }
        interface ProviderNames {
            String YAHOO = "Yahoo";
            String GTALK = "GTalk";
            String MSN = "MSN";
            String ICQ = "ICQ";
            String AIM = "AIM";
            String XMPP = "XMPP";
            String JABBER = "JABBER";
            String SKYPE = "SKYPE";
            String QQ = "QQ";
        }
        @Deprecated
        public static String lookupProviderNameFromId(int protocol) {
            switch (protocol) {
                case PROTOCOL_GOOGLE_TALK:
                    return ProviderNames.GTALK;
                case PROTOCOL_AIM:
                    return ProviderNames.AIM;
                case PROTOCOL_MSN:
                    return ProviderNames.MSN;
                case PROTOCOL_YAHOO:
                    return ProviderNames.YAHOO;
                case PROTOCOL_ICQ:
                    return ProviderNames.ICQ;
                case PROTOCOL_JABBER:
                    return ProviderNames.JABBER;
                case PROTOCOL_SKYPE:
                    return ProviderNames.SKYPE;
                case PROTOCOL_QQ:
                    return ProviderNames.QQ;
            }
            return null;
        }
        private ContactMethods() {}
        @Deprecated
        public static final CharSequence getDisplayLabel(Context context, int kind,
                int type, CharSequence label) {
            CharSequence display = "";
            switch (kind) {
                case KIND_EMAIL: {
                    if (type != People.ContactMethods.TYPE_CUSTOM) {
                        CharSequence[] labels = context.getResources().getTextArray(
                                com.android.internal.R.array.emailAddressTypes);
                        try {
                            display = labels[type - 1];
                        } catch (ArrayIndexOutOfBoundsException e) {
                            display = labels[ContactMethods.TYPE_HOME - 1];
                        }
                    } else {
                        if (!TextUtils.isEmpty(label)) {
                            display = label;
                        }
                    }
                    break;
                }
                case KIND_POSTAL: {
                    if (type != People.ContactMethods.TYPE_CUSTOM) {
                        CharSequence[] labels = context.getResources().getTextArray(
                                com.android.internal.R.array.postalAddressTypes);
                        try {
                            display = labels[type - 1];
                        } catch (ArrayIndexOutOfBoundsException e) {
                            display = labels[ContactMethods.TYPE_HOME - 1];
                        }
                    } else {
                        if (!TextUtils.isEmpty(label)) {
                            display = label;
                        }
                    }
                    break;
                }
                default:
                    display = context.getString(R.string.untitled);
            }
            return display;
        }
        @Deprecated
        public void addPostalLocation(Context context, long postalId,
                double latitude, double longitude) {
            final ContentResolver resolver = context.getContentResolver();
            ContentValues values = new ContentValues(2);
            values.put(POSTAL_LOCATION_LATITUDE, latitude);
            values.put(POSTAL_LOCATION_LONGITUDE, longitude);
            Uri loc = resolver.insert(CONTENT_URI, values);
            long locId = ContentUris.parseId(loc);
            values.clear();
            values.put(AUX_DATA, locId);
            resolver.update(ContentUris.withAppendedId(CONTENT_URI, postalId), values, null, null);
        }
        @Deprecated
        public static final Uri CONTENT_URI =
            Uri.parse("content:
        @Deprecated
        public static final Uri CONTENT_EMAIL_URI =
            Uri.parse("content:
        @Deprecated
        public static final String CONTENT_TYPE = "vnd.android.cursor.dir/contact-methods";
        @Deprecated
        public static final String CONTENT_EMAIL_TYPE = "vnd.android.cursor.dir/email";
        @Deprecated
        public static final String CONTENT_POSTAL_TYPE = "vnd.android.cursor.dir/postal-address";
        @Deprecated
        public static final String CONTENT_EMAIL_ITEM_TYPE = "vnd.android.cursor.item/email";
        @Deprecated
        public static final String CONTENT_POSTAL_ITEM_TYPE
                = "vnd.android.cursor.item/postal-address";
        @Deprecated
        public static final String CONTENT_IM_ITEM_TYPE = "vnd.android.cursor.item/jabber-im";
        @Deprecated
        public static final String DEFAULT_SORT_ORDER = "name ASC";
        @Deprecated
        public static final String PERSON_ID = "person";
    }
    @Deprecated
    public interface PresenceColumns {
        String PRIORITY = "priority";
        String PRESENCE_STATUS = ContactsContract.StatusUpdates.PRESENCE;
        int OFFLINE = ContactsContract.StatusUpdates.OFFLINE;
        int INVISIBLE = ContactsContract.StatusUpdates.INVISIBLE;
        int AWAY = ContactsContract.StatusUpdates.AWAY;
        int IDLE = ContactsContract.StatusUpdates.IDLE;
        int DO_NOT_DISTURB = ContactsContract.StatusUpdates.DO_NOT_DISTURB;
        int AVAILABLE = ContactsContract.StatusUpdates.AVAILABLE;
        String PRESENCE_CUSTOM_STATUS = ContactsContract.StatusUpdates.STATUS;
        @Deprecated
        public static final String IM_PROTOCOL = "im_protocol";
        @Deprecated
        public static final String IM_HANDLE = "im_handle";
        @Deprecated
        public static final String IM_ACCOUNT = "im_account";
    }
    @Deprecated
    public static final class Presence
            implements BaseColumns, PresenceColumns, PeopleColumns {
        @Deprecated
        public static final Uri CONTENT_URI =
            Uri.parse("content:
        @Deprecated
        public static final String PERSON_ID = "person";
        @Deprecated
        public static final int getPresenceIconResourceId(int status) {
            switch (status) {
                case Contacts.People.AVAILABLE:
                    return com.android.internal.R.drawable.presence_online;
                case Contacts.People.IDLE:
                case Contacts.People.AWAY:
                    return com.android.internal.R.drawable.presence_away;
                case Contacts.People.DO_NOT_DISTURB:
                    return com.android.internal.R.drawable.presence_busy;
                case Contacts.People.INVISIBLE:
                    return com.android.internal.R.drawable.presence_invisible;
                case Contacts.People.OFFLINE:
                default:
                    return com.android.internal.R.drawable.presence_offline;
            }
        }
        @Deprecated
        public static final void setPresenceIcon(ImageView icon, int serverStatus) {
            icon.setImageResource(getPresenceIconResourceId(serverStatus));
        }
    }
    @Deprecated
    public interface OrganizationColumns {
        @Deprecated
        public static final String TYPE = "type";
        @Deprecated
        public static final int TYPE_CUSTOM = 0;
        @Deprecated
        public static final int TYPE_WORK = 1;
        @Deprecated
        public static final int TYPE_OTHER = 2;
        @Deprecated
        public static final String LABEL = "label";
        @Deprecated
        public static final String COMPANY = "company";
        @Deprecated
        public static final String TITLE = "title";
        @Deprecated
        public static final String PERSON_ID = "person";
        @Deprecated
        public static final String ISPRIMARY = "isprimary";
    }
    @Deprecated
    public static final class Organizations implements BaseColumns, OrganizationColumns {
        private Organizations() {}
        @Deprecated
        public static final CharSequence getDisplayLabel(Context context, int type,
                CharSequence label) {
            CharSequence display = "";
            if (type != TYPE_CUSTOM) {
                CharSequence[] labels = context.getResources().getTextArray(
                        com.android.internal.R.array.organizationTypes);
                try {
                    display = labels[type - 1];
                } catch (ArrayIndexOutOfBoundsException e) {
                    display = labels[Organizations.TYPE_WORK - 1];
                }
            } else {
                if (!TextUtils.isEmpty(label)) {
                    display = label;
                }
            }
            return display;
        }
        @Deprecated
        public static final Uri CONTENT_URI =
            Uri.parse("content:
        @Deprecated
        public static final String CONTENT_DIRECTORY = "organizations";
        @Deprecated
        public static final String DEFAULT_SORT_ORDER = "company, title, isprimary ASC";
    }
    @Deprecated
    public interface PhotosColumns {
        @Deprecated
        public static final String LOCAL_VERSION = "local_version";
        @Deprecated
        public static final String PERSON_ID = "person";
        @Deprecated
        public static final String DOWNLOAD_REQUIRED = "download_required";
        @Deprecated
        public static final String EXISTS_ON_SERVER = "exists_on_server";
        @Deprecated
        public static final String SYNC_ERROR = "sync_error";
        @Deprecated
        public static final String DATA = "data";
    }
    @Deprecated
    public static final class Photos implements BaseColumns, PhotosColumns, SyncConstValue {
        private Photos() {}
        @Deprecated
        public static final Uri CONTENT_URI = Uri.parse("content:
        @Deprecated
        public static final String CONTENT_DIRECTORY = "photo";
        @Deprecated
        public static final String DEFAULT_SORT_ORDER = "person ASC";
    }
    @Deprecated
    public interface ExtensionsColumns {
        @Deprecated
        public static final String NAME = "name";
        @Deprecated
        public static final String VALUE = "value";
    }
    @Deprecated
    public static final class Extensions implements BaseColumns, ExtensionsColumns {
        private Extensions() {}
        @Deprecated
        public static final Uri CONTENT_URI =
            Uri.parse("content:
        @Deprecated
        public static final String CONTENT_TYPE = "vnd.android.cursor.dir/contact_extensions";
        @Deprecated
        public static final String CONTENT_ITEM_TYPE = "vnd.android.cursor.item/contact_extensions";
        @Deprecated
        public static final String DEFAULT_SORT_ORDER = "person, name ASC";
        @Deprecated
        public static final String PERSON_ID = "person";
    }
    @Deprecated
    public static final class Intents {
        @Deprecated
        public Intents() {
        }
        @Deprecated
        public static final String SEARCH_SUGGESTION_CLICKED =
                ContactsContract.Intents.SEARCH_SUGGESTION_CLICKED;
        @Deprecated
        public static final String SEARCH_SUGGESTION_DIAL_NUMBER_CLICKED =
                ContactsContract.Intents.SEARCH_SUGGESTION_DIAL_NUMBER_CLICKED;
        @Deprecated
        public static final String SEARCH_SUGGESTION_CREATE_CONTACT_CLICKED =
                ContactsContract.Intents.SEARCH_SUGGESTION_CREATE_CONTACT_CLICKED;
        @Deprecated
        public static final String ATTACH_IMAGE = ContactsContract.Intents.ATTACH_IMAGE;
        @Deprecated
        public static final String SHOW_OR_CREATE_CONTACT =
                ContactsContract.Intents.SHOW_OR_CREATE_CONTACT;
        @Deprecated
        public static final String EXTRA_FORCE_CREATE = ContactsContract.Intents.EXTRA_FORCE_CREATE;
        @Deprecated
        public static final String EXTRA_CREATE_DESCRIPTION =
                ContactsContract.Intents.EXTRA_CREATE_DESCRIPTION;
        @Deprecated
        public static final String EXTRA_TARGET_RECT = ContactsContract.Intents.EXTRA_TARGET_RECT;
        @Deprecated
        public static final class UI {
            @Deprecated
            public UI() {
            }
            @Deprecated
            public static final String LIST_DEFAULT = ContactsContract.Intents.UI.LIST_DEFAULT;
            @Deprecated
            public static final String LIST_GROUP_ACTION =
                    ContactsContract.Intents.UI.LIST_GROUP_ACTION;
            @Deprecated
            public static final String GROUP_NAME_EXTRA_KEY =
                    ContactsContract.Intents.UI.GROUP_NAME_EXTRA_KEY;
            @Deprecated
            public static final String LIST_ALL_CONTACTS_ACTION =
                    ContactsContract.Intents.UI.LIST_ALL_CONTACTS_ACTION;
            @Deprecated
            public static final String LIST_CONTACTS_WITH_PHONES_ACTION =
                    ContactsContract.Intents.UI.LIST_CONTACTS_WITH_PHONES_ACTION;
            @Deprecated
            public static final String LIST_STARRED_ACTION =
                    ContactsContract.Intents.UI.LIST_STARRED_ACTION;
            @Deprecated
            public static final String LIST_FREQUENT_ACTION =
                    ContactsContract.Intents.UI.LIST_FREQUENT_ACTION;
            @Deprecated
            public static final String LIST_STREQUENT_ACTION =
                    ContactsContract.Intents.UI.LIST_STREQUENT_ACTION;
            @Deprecated
            public static final String TITLE_EXTRA_KEY =
                    ContactsContract.Intents.UI.TITLE_EXTRA_KEY;
            @Deprecated
            public static final String FILTER_CONTACTS_ACTION =
                    ContactsContract.Intents.UI.FILTER_CONTACTS_ACTION;
            @Deprecated
            public static final String FILTER_TEXT_EXTRA_KEY =
                    ContactsContract.Intents.UI.FILTER_TEXT_EXTRA_KEY;
        }
        @Deprecated
        public static final class Insert {
            @Deprecated
            public Insert() {
            }
            @Deprecated
            public static final String ACTION = ContactsContract.Intents.Insert.ACTION;
            @Deprecated
            public static final String FULL_MODE = ContactsContract.Intents.Insert.FULL_MODE;
            @Deprecated
            public static final String NAME = ContactsContract.Intents.Insert.NAME;
            @Deprecated
            public static final String PHONETIC_NAME =
                    ContactsContract.Intents.Insert.PHONETIC_NAME;
            @Deprecated
            public static final String COMPANY = ContactsContract.Intents.Insert.COMPANY;
            @Deprecated
            public static final String JOB_TITLE = ContactsContract.Intents.Insert.JOB_TITLE;
            @Deprecated
            public static final String NOTES = ContactsContract.Intents.Insert.NOTES;
            @Deprecated
            public static final String PHONE = ContactsContract.Intents.Insert.PHONE;
            @Deprecated
            public static final String PHONE_TYPE = ContactsContract.Intents.Insert.PHONE_TYPE;
            @Deprecated
            public static final String PHONE_ISPRIMARY =
                    ContactsContract.Intents.Insert.PHONE_ISPRIMARY;
            @Deprecated
            public static final String SECONDARY_PHONE =
                    ContactsContract.Intents.Insert.SECONDARY_PHONE;
            @Deprecated
            public static final String SECONDARY_PHONE_TYPE =
                    ContactsContract.Intents.Insert.SECONDARY_PHONE_TYPE;
            @Deprecated
            public static final String TERTIARY_PHONE =
                    ContactsContract.Intents.Insert.TERTIARY_PHONE;
            @Deprecated
            public static final String TERTIARY_PHONE_TYPE =
                    ContactsContract.Intents.Insert.TERTIARY_PHONE_TYPE;
            @Deprecated
            public static final String EMAIL = ContactsContract.Intents.Insert.EMAIL;
            @Deprecated
            public static final String EMAIL_TYPE = ContactsContract.Intents.Insert.EMAIL_TYPE;
            @Deprecated
            public static final String EMAIL_ISPRIMARY =
                    ContactsContract.Intents.Insert.EMAIL_ISPRIMARY;
            @Deprecated
            public static final String SECONDARY_EMAIL =
                    ContactsContract.Intents.Insert.SECONDARY_EMAIL;
            @Deprecated
            public static final String SECONDARY_EMAIL_TYPE =
                    ContactsContract.Intents.Insert.SECONDARY_EMAIL_TYPE;
            @Deprecated
            public static final String TERTIARY_EMAIL =
                    ContactsContract.Intents.Insert.TERTIARY_EMAIL;
            @Deprecated
            public static final String TERTIARY_EMAIL_TYPE =
                    ContactsContract.Intents.Insert.TERTIARY_EMAIL_TYPE;
            @Deprecated
            public static final String POSTAL = ContactsContract.Intents.Insert.POSTAL;
            @Deprecated
            public static final String POSTAL_TYPE = ContactsContract.Intents.Insert.POSTAL_TYPE;
            @Deprecated
            public static final String POSTAL_ISPRIMARY = ContactsContract.Intents.Insert.POSTAL_ISPRIMARY;
            @Deprecated
            public static final String IM_HANDLE = ContactsContract.Intents.Insert.IM_HANDLE;
            @Deprecated
            public static final String IM_PROTOCOL = ContactsContract.Intents.Insert.IM_PROTOCOL;
            @Deprecated
            public static final String IM_ISPRIMARY = ContactsContract.Intents.Insert.IM_ISPRIMARY;
        }
    }
}
