public class Imps {
    private Imps() {}
    public interface ProviderColumns {
        String NAME = "name";
        String FULLNAME = "fullname";
        String CATEGORY = "category";
        String SIGNUP_URL = "signup_url";
    }
    public interface ProviderNames {
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
    public static final class Provider implements BaseColumns, ProviderColumns {
        private Provider() {}
        public static final long getProviderIdForName(ContentResolver cr, String providerName) {
            String[] selectionArgs = new String[1];
            selectionArgs[0] = providerName;
            Cursor cursor = cr.query(CONTENT_URI,
                    PROVIDER_PROJECTION,
                    NAME+"=?",
                    selectionArgs, null);
            long retVal = 0;
            try {
                if (cursor.moveToFirst()) {
                    retVal = cursor.getLong(cursor.getColumnIndexOrThrow(_ID));
                }
            } finally {
                cursor.close();
            }
            return retVal;
        }
        public static final String getProviderNameForId(ContentResolver cr, long providerId) {
            Cursor cursor = cr.query(CONTENT_URI,
                    PROVIDER_PROJECTION,
                    _ID + "=" + providerId,
                    null, null);
            String retVal = null;
            try {
                if (cursor.moveToFirst()) {
                    retVal = cursor.getString(cursor.getColumnIndexOrThrow(NAME));
                }
            } finally {
                cursor.close();
            }
            return retVal;
        }
        private static final String[] PROVIDER_PROJECTION = new String[] {
                _ID,
                NAME
        };
        public static final String ACTIVE_ACCOUNT_ID = "account_id";
        public static final String ACTIVE_ACCOUNT_USERNAME = "account_username";
        public static final String ACTIVE_ACCOUNT_PW = "account_pw";
        public static final String ACTIVE_ACCOUNT_LOCKED = "account_locked";
        public static final String ACTIVE_ACCOUNT_KEEP_SIGNED_IN = "account_keepSignedIn";
        public static final String ACCOUNT_PRESENCE_STATUS = "account_presenceStatus";
        public static final String ACCOUNT_CONNECTION_STATUS = "account_connStatus";
        public static final Uri CONTENT_URI =
            Uri.parse("content:
        public static final Uri CONTENT_URI_WITH_ACCOUNT =
            Uri.parse("content:
        public static final String CONTENT_TYPE =
                "vnd.android.cursor.dir/imps-providers";
        public static final String CONTENT_ITEM_TYPE =
                "vnd.android.cursor.item/imps-providers";
        public static final String DEFAULT_SORT_ORDER = "name ASC";
    }
    public interface AccountColumns {
        String NAME = "name";
        String PROVIDER = "provider";
        String USERNAME = "username";
        String PASSWORD = "pw";
        String ACTIVE = "active";
        String LOCKED = "locked";
        String KEEP_SIGNED_IN = "keep_signed_in";
        String LAST_LOGIN_STATE = "last_login_state";
    }
    public static final class Account implements BaseColumns, AccountColumns {
        private Account() {}
        public static final long getProviderIdForAccount(ContentResolver cr, long accountId) {
            Cursor cursor = cr.query(CONTENT_URI,
                    PROVIDER_PROJECTION,
                    _ID + "=" + accountId,
                    null ,
                    null );
            long providerId = 0;
            try {
                if (cursor.moveToFirst()) {
                    providerId = cursor.getLong(PROVIDER_COLUMN);
                }
            } finally {
                cursor.close();
            }
            return providerId;
        }
        private static final String[] PROVIDER_PROJECTION = new String[] { PROVIDER };
        private static final int PROVIDER_COLUMN = 0;
        public static final Uri CONTENT_URI =
            Uri.parse("content:
        public static final String CONTENT_TYPE =
                "vnd.android.cursor.dir/imps-accounts";
        public static final String CONTENT_ITEM_TYPE =
                "vnd.android.cursor.item/imps-accounts";
        public static final String DEFAULT_SORT_ORDER = "name ASC";
    }
    public interface ConnectionStatus {
        int OFFLINE = 0;
        int CONNECTING = 1;
        int SUSPENDED = 2;
        int ONLINE = 3;
    }
    public interface AccountStatusColumns {
        String ACCOUNT = "account";
        String PRESENCE_STATUS = "presenceStatus";
        String CONNECTION_STATUS = "connStatus";
    }
    public static final class AccountStatus implements BaseColumns, AccountStatusColumns {
        public static final Uri CONTENT_URI =
            Uri.parse("content:
        public static final String CONTENT_TYPE =
                "vnd.android.cursor.dir/imps-account-status";
        public static final String CONTENT_ITEM_TYPE =
                "vnd.android.cursor.item/imps-account-status";
        public static final String DEFAULT_SORT_ORDER = "name ASC";
    }
    public interface ContactsColumns {
        String USERNAME = "username";
        String NICKNAME = "nickname";
        String PROVIDER = "provider";
        String ACCOUNT = "account";
        String CONTACTLIST = "contactList";
        String TYPE = "type";
        int TYPE_NORMAL = 0;
        int TYPE_TEMPORARY = 1;
        int TYPE_GROUP = 2;
        int TYPE_BLOCKED = 3;
        int TYPE_HIDDEN = 4;
        int TYPE_PINNED = 5;
        String SUBSCRIPTION_STATUS = "subscriptionStatus";
        int SUBSCRIPTION_STATUS_NONE = 0;
        int SUBSCRIPTION_STATUS_SUBSCRIBE_PENDING = 1;
        int SUBSCRIPTION_STATUS_UNSUBSCRIBE_PENDING = 2;
        String SUBSCRIPTION_TYPE = "subscriptionType";
        int SUBSCRIPTION_TYPE_NONE = 0;
        int SUBSCRIPTION_TYPE_REMOVE = 1;
        int SUBSCRIPTION_TYPE_TO = 2;
        int SUBSCRIPTION_TYPE_FROM = 3;
        int SUBSCRIPTION_TYPE_BOTH = 4;
        int SUBSCRIPTION_TYPE_INVITATIONS = 5;
        String QUICK_CONTACT = "qc";
        String REJECTED = "rejected";
        String OTR = "otr";
    }
    public interface OffTheRecordType {
        int DISABLED = 0;
        int ENABLED = 1;
        int ENABLED_BY_USER = 2;
        int ENABLED_BY_BUDDY = 3;
    };
    public static final class Contacts implements BaseColumns,
            ContactsColumns, PresenceColumns, ChatsColumns {
        private Contacts() {}
        public static final Uri CONTENT_URI =
            Uri.parse("content:
        public static final Uri CONTENT_URI_WITH_PRESENCE =
            Uri.parse("content:
        public static final Uri CONTENT_URI_CONTACTS_BAREBONE =
            Uri.parse("content:
        public static final Uri CONTENT_URI_CHAT_CONTACTS =
            Uri.parse("content:
        public static final Uri CONTENT_URI_BLOCKED_CONTACTS =
            Uri.parse("content:
        public static final Uri CONTENT_URI_CONTACTS_BY =
            Uri.parse("content:
        public static final Uri CONTENT_URI_CHAT_CONTACTS_BY =
            Uri.parse("content:
        public static final Uri CONTENT_URI_ONLINE_CONTACTS_BY =
            Uri.parse("content:
        public static final Uri CONTENT_URI_OFFLINE_CONTACTS_BY =
            Uri.parse("content:
        public static final Uri BULK_CONTENT_URI =
                Uri.parse("content:
        public static final Uri CONTENT_URI_ONLINE_COUNT =
            Uri.parse("content:
        public static final String CONTENT_TYPE = "vnd.android.cursor.dir/imps-contacts";
        public static final String CONTENT_ITEM_TYPE = "vnd.android.cursor.item/imps-contacts";
        public static final String DEFAULT_SORT_ORDER =
                "subscriptionType DESC, last_message_date DESC," +
                        " mode DESC, nickname COLLATE UNICODE ASC";
        public static final String CHATS_CONTACT = "chats_contact";
        public static final String AVATAR_HASH = "avatars_hash";
        public static final String AVATAR_DATA = "avatars_data";
    }
    public interface ContactListColumns {
        String NAME = "name";
        String PROVIDER = "provider";
        String ACCOUNT = "account";
    }
    public static final class ContactList implements BaseColumns,
            ContactListColumns {
        private ContactList() {}
        public static final Uri CONTENT_URI =
            Uri.parse("content:
        public static final String CONTENT_TYPE =
                "vnd.android.cursor.dir/imps-contactLists";
        public static final String CONTENT_ITEM_TYPE =
                "vnd.android.cursor.item/imps-contactLists";
        public static final String DEFAULT_SORT_ORDER = "name COLLATE UNICODE ASC";
        public static final String PROVIDER_NAME = "provider_name";
        public static final String ACCOUNT_NAME = "account_name";
    }
    public interface BlockedListColumns {
        String USERNAME = "username";
        String NICKNAME = "nickname";
        String PROVIDER = "provider";
        String ACCOUNT = "account";
    }
    public static final class BlockedList implements BaseColumns, BlockedListColumns {
        private BlockedList() {}
        public static final Uri CONTENT_URI =
            Uri.parse("content:
        public static final String CONTENT_TYPE =
                "vnd.android.cursor.dir/imps-blockedList";
        public static final String CONTENT_ITEM_TYPE =
                "vnd.android.cursor.item/imps-blockedList";
        public static final String DEFAULT_SORT_ORDER = "nickname ASC";
        public static final String PROVIDER_NAME = "provider_name";
        public static final String ACCOUNT_NAME = "account_name";
        public static final String AVATAR_DATA = "avatars_data";
    }
    public interface ContactsEtagColumns {
        String ETAG = "etag";
        String OTR_ETAG = "otr_etag";
        String ACCOUNT = "account";
    }
    public static final class ContactsEtag implements BaseColumns, ContactsEtagColumns {
        private ContactsEtag() {}
        public static final Cursor query(ContentResolver cr,
                String[] projection) {
            return cr.query(CONTENT_URI, projection, null, null, null);
        }
        public static final Cursor query(ContentResolver cr,
                String[] projection, String where, String orderBy) {
            return cr.query(CONTENT_URI, projection, where,
                    null, orderBy == null ? null : orderBy);
        }
        public static final String getRosterEtag(ContentResolver resolver, long accountId) {
            String retVal = null;
            Cursor c = resolver.query(CONTENT_URI,
                    CONTACT_ETAG_PROJECTION,
                    ACCOUNT + "=" + accountId,
                    null ,
                    null );
            try {
                if (c.moveToFirst()) {
                    retVal = c.getString(COLUMN_ETAG);
                }
            } finally {
                c.close();
            }
            return retVal;
        }
        public static final String getOtrEtag(ContentResolver resolver, long accountId) {
            String retVal = null;
            Cursor c = resolver.query(CONTENT_URI,
                    CONTACT_OTR_ETAG_PROJECTION,
                    ACCOUNT + "=" + accountId,
                    null ,
                    null );
            try {
                if (c.moveToFirst()) {
                    retVal = c.getString(COLUMN_OTR_ETAG);
                }
            } finally {
                c.close();
            }
            return retVal;
        }
        private static final String[] CONTACT_ETAG_PROJECTION = new String[] {
                Imps.ContactsEtag.ETAG    
        };
        private static int COLUMN_ETAG = 0;
        private static final String[] CONTACT_OTR_ETAG_PROJECTION = new String[] {
                Imps.ContactsEtag.OTR_ETAG    
        };
        private static int COLUMN_OTR_ETAG = 0;
        public static final Uri CONTENT_URI =
            Uri.parse("content:
        public static final String CONTENT_TYPE =
                "vnd.android.cursor.dir/imps-contactsEtag";
        public static final String CONTENT_ITEM_TYPE =
                "vnd.android.cursor.item/imps-contactsEtag";
    }
    public interface MessageType {
        int OUTGOING = 0;
        int INCOMING = 1;
        int PRESENCE_AVAILABLE = 2;
        int PRESENCE_AWAY = 3;
        int PRESENCE_DND = 4;
        int PRESENCE_UNAVAILABLE = 5;
        int CONVERT_TO_GROUPCHAT = 6;
        int STATUS = 7;
        int POSTPONED = 8;
        int OTR_IS_TURNED_OFF = 9;
        int OTR_IS_TURNED_ON = 10;
        int OTR_TURNED_ON_BY_USER = 11;
        int OTR_TURNED_ON_BY_BUDDY = 12;
    }
    public interface MessageColumns {
        String THREAD_ID = "thread_id";
        String NICKNAME = "nickname";
        String BODY = "body";
        String DATE = "date";
        String TYPE = "type";
        String ERROR_CODE = "err_code";
        String ERROR_MESSAGE = "err_msg";
        String PACKET_ID = "packet_id";
        String IS_GROUP_CHAT = "is_muc";
        String DISPLAY_SENT_TIME = "show_ts";
    }
    public static final class Messages implements BaseColumns, MessageColumns {
        private Messages() {}
        public static final Uri getContentUriByThreadId(long threadId) {
            Uri.Builder builder = CONTENT_URI_MESSAGES_BY_THREAD_ID.buildUpon();
            ContentUris.appendId(builder, threadId);
            return builder.build();
        }
        public static final Uri getContentUriByContact(long accountId, String username) {
            Uri.Builder builder = CONTENT_URI_MESSAGES_BY_ACCOUNT_AND_CONTACT.buildUpon();
            ContentUris.appendId(builder, accountId);
            builder.appendPath(username);
            return builder.build();
        }
        public static final Uri getContentUriByProvider(long providerId) {
            Uri.Builder builder = CONTENT_URI_MESSAGES_BY_PROVIDER.buildUpon();
            ContentUris.appendId(builder, providerId);
            return builder.build();
        }
        public static final Uri getContentUriByAccount(long accountId) {
            Uri.Builder builder = CONTENT_URI_BY_ACCOUNT.buildUpon();
            ContentUris.appendId(builder, accountId);
            return builder.build();
        }
        public static final Uri getOtrMessagesContentUriByThreadId(long threadId) {
            Uri.Builder builder = OTR_MESSAGES_CONTENT_URI_BY_THREAD_ID.buildUpon();
            ContentUris.appendId(builder, threadId);
            return builder.build();
        }
        public static final Uri getOtrMessagesContentUriByContact(long accountId, String username) {
            Uri.Builder builder = OTR_MESSAGES_CONTENT_URI_BY_ACCOUNT_AND_CONTACT.buildUpon();
            ContentUris.appendId(builder, accountId);
            builder.appendPath(username);
            return builder.build();
        }
        public static final Uri getOtrMessagesContentUriByProvider(long providerId) {
            Uri.Builder builder = OTR_MESSAGES_CONTENT_URI_BY_PROVIDER.buildUpon();
            ContentUris.appendId(builder, providerId);
            return builder.build();
        }
        public static final Uri getOtrMessagesContentUriByAccount(long accountId) {
            Uri.Builder builder = OTR_MESSAGES_CONTENT_URI_BY_ACCOUNT.buildUpon();
            ContentUris.appendId(builder, accountId);
            return builder.build();
        }
        public static final Uri CONTENT_URI =
                Uri.parse("content:
        public static final Uri CONTENT_URI_MESSAGES_BY_THREAD_ID =
                Uri.parse("content:
        public static final Uri CONTENT_URI_MESSAGES_BY_ACCOUNT_AND_CONTACT =
                Uri.parse("content:
        public static final Uri CONTENT_URI_MESSAGES_BY_PROVIDER =
                Uri.parse("content:
        public static final Uri CONTENT_URI_BY_ACCOUNT =
                Uri.parse("content:
        public static final Uri OTR_MESSAGES_CONTENT_URI =
                Uri.parse("content:
        public static final Uri OTR_MESSAGES_CONTENT_URI_BY_THREAD_ID =
                Uri.parse("content:
        public static final Uri OTR_MESSAGES_CONTENT_URI_BY_ACCOUNT_AND_CONTACT =
                Uri.parse("content:
        public static final Uri OTR_MESSAGES_CONTENT_URI_BY_PROVIDER =
                Uri.parse("content:
        public static final Uri OTR_MESSAGES_CONTENT_URI_BY_ACCOUNT =
                Uri.parse("content:
        public static final String CONTENT_TYPE = "vnd.android.cursor.dir/imps-messages";
        public static final String CONTENT_ITEM_TYPE =
                "vnd.android.cursor.item/imps-messages";
        public static final String DEFAULT_SORT_ORDER = "date ASC";
        public static final String CONTACT = "contact";
    }
    public interface GroupMemberColumns {
        String GROUP = "groupId";
        String USERNAME = "username";
        String NICKNAME = "nickname";
    }
    public final static class GroupMembers implements GroupMemberColumns {
        private GroupMembers(){}
        public static final Uri CONTENT_URI =
            Uri.parse("content:
        public static final String CONTENT_TYPE =
            "vnd.android.cursor.dir/imps-groupMembers";
        public static final String CONTENT_ITEM_TYPE =
                "vnd.android.cursor.item/imps-groupMembers";
    }
    public interface InvitationColumns {
        String PROVIDER = "providerId";
        String ACCOUNT = "accountId";
        String INVITE_ID = "inviteId";
        String SENDER = "sender";
        String GROUP_NAME = "groupName";
        String NOTE = "note";
        String STATUS = "status";
        int STATUS_PENDING = 0;
        int STATUS_ACCEPTED = 1;
        int STATUS_REJECTED = 2;
    }
    public final static class Invitation implements InvitationColumns,
            BaseColumns {
        private Invitation() {
        }
        public static final Uri CONTENT_URI =
            Uri.parse("content:
        public static final String CONTENT_TYPE =
            "vnd.android.cursor.dir/imps-invitations";
        public static final String CONTENT_ITEM_TYPE =
                "vnd.android.cursor.item/imps-invitations";
    }
    public interface AvatarsColumns {
        String CONTACT = "contact";
        String PROVIDER = "provider_id";
        String ACCOUNT = "account_id";
        String HASH = "hash";
        String DATA = "data";
    }
    public static final class Avatars implements BaseColumns, AvatarsColumns {
        private Avatars() {}
        public static final Uri CONTENT_URI = Uri.parse("content:
        public static final Uri CONTENT_URI_AVATARS_BY =
                Uri.parse("content:
        public static final String CONTENT_TYPE = "vnd.android.cursor.dir/imps-avatars";
        public static final String CONTENT_ITEM_TYPE =
                "vnd.android.cursor.item/imps-avatars";
        public static final String DEFAULT_SORT_ORDER = "contact ASC";
    }
    public interface CommonPresenceColumns {
        String PRIORITY = "priority";
        String PRESENCE_STATUS = "mode";
        int OFFLINE = 0;
        int INVISIBLE = 1;
        int AWAY = 2;
        int IDLE = 3;
        int DO_NOT_DISTURB = 4;
        int AVAILABLE = 5;
        String PRESENCE_CUSTOM_STATUS = "status";
    }
    public interface PresenceColumns extends CommonPresenceColumns {
        String CONTACT_ID = "contact_id";
        String JID_RESOURCE = "jid_resource";
        String CLIENT_TYPE = "client_type";
        int CLIENT_TYPE_DEFAULT = 0;
        int CLIENT_TYPE_MOBILE = 1;
        int CLIENT_TYPE_ANDROID = 2;
    }
    public static final class Presence implements BaseColumns, PresenceColumns {
        public static final Uri CONTENT_URI = Uri.parse("content:
        public static final Uri CONTENT_URI_BY_ACCOUNT = Uri.parse("content:
        public static final Uri BULK_CONTENT_URI = Uri.parse("content:
        public static final Uri SEED_PRESENCE_BY_ACCOUNT_CONTENT_URI =
                Uri.parse("content:
        public static final String CONTENT_TYPE = "vnd.android.cursor.dir/imps-presence";
        public static final String DEFAULT_SORT_ORDER = "mode DESC";
    }
    public interface ChatsColumns {
        String CONTACT_ID = "contact_id";
        String JID_RESOURCE = "jid_resource";
        String GROUP_CHAT = "groupchat";
        String LAST_UNREAD_MESSAGE = "last_unread_message";
        String LAST_MESSAGE_DATE = "last_message_date";
        String UNSENT_COMPOSED_MESSAGE = "unsent_composed_message";
        String SHORTCUT = "shortcut";
    }
    public static final class Chats implements BaseColumns, ChatsColumns {
        private Chats() {}
        public static final Uri CONTENT_URI =
            Uri.parse("content:
        public static final Uri CONTENT_URI_BY_ACCOUNT = Uri.parse("content:
        public static final String CONTENT_TYPE = "vnd.android.cursor.dir/imps-chats";
        public static final String CONTENT_ITEM_TYPE = "vnd.android.cursor.item/imps-chats";
        public static final String DEFAULT_SORT_ORDER = "last_message_date ASC";
    }
    public static interface SessionCookiesColumns {
        String NAME = "name";
        String VALUE = "value";
        String PROVIDER = "provider";
        String ACCOUNT = "account";
    }
    public static class SessionCookies implements SessionCookiesColumns, BaseColumns {
        private SessionCookies() {
        }
        public static final Uri CONTENT_URI = Uri.parse("content:
        public static final Uri CONTENT_URI_SESSION_COOKIES_BY =
            Uri.parse("content:
        public static final String CONTENT_TYPE = "vnd.android-dir/imps-sessionCookies";
    }
    public static interface ProviderSettingsColumns {
        String PROVIDER = "provider";
        String NAME = "name";
        String VALUE = "value";
    }
    public static class ProviderSettings implements ProviderSettingsColumns {
        private ProviderSettings() {
        }
        public static final Uri CONTENT_URI =
                Uri.parse("content:
        public static final String CONTENT_TYPE = "vnd.android-dir/imps-providerSettings";
        public static final String SHOW_OFFLINE_CONTACTS = "show_offline_contacts";
        public static final String SETTING_AUTOMATICALLY_CONNECT_GTALK = "gtalk_auto_connect";
        public static final String SETTING_AUTOMATICALLY_START_SERVICE = "auto_start_service";
        public static final String SETTING_HIDE_OFFLINE_CONTACTS = "hide_offline_contacts";
        public static final String SETTING_ENABLE_NOTIFICATION = "enable_notification";
        public static final String SETTING_VIBRATE = "vibrate";
        public static final String SETTING_RINGTONE = "ringtone";
        public static final String SETTING_RINGTONE_DEFAULT =
                "content:
        public static final String SETTING_SHOW_MOBILE_INDICATOR = "mobile_indicator";
        public static final String SETTING_SHOW_AWAY_ON_IDLE = "show_away_on_idle";
        public static final String SETTING_UPLOAD_HEARTBEAT_STAT = "upload_heartbeat_stat";
        public static final String SETTING_HEARTBEAT_INTERVAL = "heartbeat_interval";
        public static final String SETTING_JID_RESOURCE = "jid_resource";
        public static final String LAST_RMQ_RECEIVED = "last_rmq_rec";
        public static HashMap<String, String> queryProviderSettings(ContentResolver cr,
                long providerId) {
            HashMap<String, String> settings = new HashMap<String, String>();
            String[] projection = { NAME, VALUE };
            Cursor c = cr.query(ContentUris.withAppendedId(CONTENT_URI, providerId), projection, null, null, null);
            if (c == null) {
                return null;
            }
            while(c.moveToNext()) {
                settings.put(c.getString(0), c.getString(1));
            }
            c.close();
            return settings;
        }
        public static String getStringValue(ContentResolver cr, long providerId, String settingName) {
            String ret = null;
            Cursor c = getSettingValue(cr, providerId, settingName);
            if (c != null) {
                ret = c.getString(0);
                c.close();
            }
            return ret;
        }
        public static boolean getBooleanValue(ContentResolver cr, long providerId, String settingName) {
            boolean ret = false;
            Cursor c = getSettingValue(cr, providerId, settingName);
            if (c != null) {
                ret = c.getInt(0) != 0;
                c.close();
            }
            return ret;
        }
        private static Cursor getSettingValue(ContentResolver cr, long providerId, String settingName) {
            Cursor c = cr.query(ContentUris.withAppendedId(CONTENT_URI, providerId), new String[]{VALUE}, NAME + "=?",
                    new String[]{settingName}, null);
            if (c != null) {
                if (!c.moveToFirst()) {
                    c.close();
                    return null;
                }
            }
            return c;
        }
        public static void putLongValue(ContentResolver cr, long providerId, String name,
                long value) {
            ContentValues v = new ContentValues(3);
            v.put(PROVIDER, providerId);
            v.put(NAME, name);
            v.put(VALUE, value);
            cr.insert(CONTENT_URI, v);
        }
        public static void putBooleanValue(ContentResolver cr, long providerId, String name,
                boolean value) {
            ContentValues v = new ContentValues(3);
            v.put(PROVIDER, providerId);
            v.put(NAME, name);
            v.put(VALUE, Boolean.toString(value));
            cr.insert(CONTENT_URI, v);
        }
        public static void putStringValue(ContentResolver cr, long providerId, String name,
                String value) {
            ContentValues v = new ContentValues(3);
            v.put(PROVIDER, providerId);
            v.put(NAME, name);
            v.put(VALUE, value);
            cr.insert(CONTENT_URI, v);
        }
        public static void setAutomaticallyConnectGTalk(ContentResolver contentResolver,
                long providerId, boolean autoConnect) {
            putBooleanValue(contentResolver, providerId, SETTING_AUTOMATICALLY_CONNECT_GTALK,
                    autoConnect);
        }
        public static void setHideOfflineContacts(ContentResolver contentResolver,
                long providerId, boolean hideOfflineContacts) {
            putBooleanValue(contentResolver, providerId, SETTING_HIDE_OFFLINE_CONTACTS,
                    hideOfflineContacts);
        }
        public static void setEnableNotification(ContentResolver contentResolver, long providerId,
                boolean enable) {
            putBooleanValue(contentResolver, providerId, SETTING_ENABLE_NOTIFICATION, enable);
        }
        public static void setVibrate(ContentResolver contentResolver, long providerId,
                boolean vibrate) {
            putBooleanValue(contentResolver, providerId, SETTING_VIBRATE, vibrate);
        }
        public static void setRingtoneURI(ContentResolver contentResolver, long providerId,
                String ringtoneUri) {
            putStringValue(contentResolver, providerId, SETTING_RINGTONE, ringtoneUri);
        }
        public static void setShowMobileIndicator(ContentResolver contentResolver, long providerId,
                boolean showMobileIndicator) {
            putBooleanValue(contentResolver, providerId, SETTING_SHOW_MOBILE_INDICATOR,
                    showMobileIndicator);
        }
        public static void setShowAwayOnIdle(ContentResolver contentResolver,
                long providerId, boolean showAway) {
            putBooleanValue(contentResolver, providerId, SETTING_SHOW_AWAY_ON_IDLE, showAway);
        }
        public static void setUploadHeartbeatStat(ContentResolver contentResolver,
                long providerId, boolean uploadStat) {
            putBooleanValue(contentResolver, providerId, SETTING_UPLOAD_HEARTBEAT_STAT, uploadStat);
        }
        public static void setHeartbeatInterval(ContentResolver contentResolver,
                long providerId, long interval) {
            putLongValue(contentResolver, providerId, SETTING_HEARTBEAT_INTERVAL, interval);
        }
        public static void setJidResource(ContentResolver contentResolver,
                                          long providerId, String jidResource) {
            putStringValue(contentResolver, providerId, SETTING_JID_RESOURCE, jidResource);
        }
        public static class QueryMap extends ContentQueryMap {
            private ContentResolver mContentResolver;
            private long mProviderId;
            public QueryMap(ContentResolver contentResolver, long providerId, boolean keepUpdated,
                    Handler handlerForUpdateNotifications) {
                super(contentResolver.query(CONTENT_URI,
                            new String[] {NAME,VALUE},
                            PROVIDER + "=" + providerId,
                            null, 
                            null), 
                        NAME, keepUpdated, handlerForUpdateNotifications);
                mContentResolver = contentResolver;
                mProviderId = providerId;
            }
            public void setAutomaticallyConnectToGTalkServer(boolean autoConnect) {
                ProviderSettings.setAutomaticallyConnectGTalk(mContentResolver, mProviderId,
                        autoConnect);
            }
            public boolean getAutomaticallyConnectToGTalkServer() {
                return getBoolean(SETTING_AUTOMATICALLY_CONNECT_GTALK,
                        true );
            }
            public void setHideOfflineContacts(boolean hideOfflineContacts) {
                ProviderSettings.setHideOfflineContacts(mContentResolver, mProviderId,
                        hideOfflineContacts);
            }
            public boolean getHideOfflineContacts() {
                return getBoolean(SETTING_HIDE_OFFLINE_CONTACTS,
                        false);
            }
            public void setEnableNotification(boolean enable) {
                ProviderSettings.setEnableNotification(mContentResolver, mProviderId, enable);
            }
            public boolean getEnableNotification() {
                return getBoolean(SETTING_ENABLE_NOTIFICATION,
                        true);
            }
            public void setVibrate(boolean vibrate) {
                ProviderSettings.setVibrate(mContentResolver, mProviderId, vibrate);
            }
            public boolean getVibrate() {
                return getBoolean(SETTING_VIBRATE, false );
            }
            public void setRingtoneURI(String ringtoneUri) {
                ProviderSettings.setRingtoneURI(mContentResolver, mProviderId, ringtoneUri);
            }
            public String getRingtoneURI() {
                return getString(SETTING_RINGTONE, SETTING_RINGTONE_DEFAULT);
            }
            public void setShowMobileIndicator(boolean showMobile) {
                ProviderSettings.setShowMobileIndicator(mContentResolver, mProviderId, showMobile);
            }
            public boolean getShowMobileIndicator() {
                return getBoolean(SETTING_SHOW_MOBILE_INDICATOR,
                        true );
            }
            public void setShowAwayOnIdle(boolean showAway) {
                ProviderSettings.setShowAwayOnIdle(mContentResolver, mProviderId, showAway);
            }
            public boolean getShowAwayOnIdle() {
                return getBoolean(SETTING_SHOW_AWAY_ON_IDLE,
                        true );
            }
            public void setUploadHeartbeatStat(boolean uploadStat) {
                ProviderSettings.setUploadHeartbeatStat(mContentResolver, mProviderId, uploadStat);
            }
            public boolean getUploadHeartbeatStat() {
                return getBoolean(SETTING_UPLOAD_HEARTBEAT_STAT,
                        false );
            }
            public void setHeartbeatInterval(long interval) {
                ProviderSettings.setHeartbeatInterval(mContentResolver, mProviderId, interval);
            }
            public long getHeartbeatInterval() {
                return getLong(SETTING_HEARTBEAT_INTERVAL, 0L );
            }
            public void setJidResource(String jidResource) {
                ProviderSettings.setJidResource(mContentResolver, mProviderId, jidResource);
            }
            public String getJidResource() {
                return getString(SETTING_JID_RESOURCE, null);
            }
            private boolean getBoolean(String name, boolean def) {
                ContentValues values = getValues(name);
                return values != null ? values.getAsBoolean(VALUE) : def;
            }
            private String getString(String name, String def) {
                ContentValues values = getValues(name);
                return values != null ? values.getAsString(VALUE) : def;
            }
            private int getInteger(String name, int def) {
                ContentValues values = getValues(name);
                return values != null ? values.getAsInteger(VALUE) : def;
            }
            private long getLong(String name, long def) {
                ContentValues values = getValues(name);
                return values != null ? values.getAsLong(VALUE) : def;
            }
        }
    }
    public interface BrandingResourceMapCacheColumns {
        String PROVIDER_ID = "provider_id";
        String APP_RES_ID = "app_res_id";
        String PLUGIN_RES_ID = "plugin_res_id";
    }
    public static final class BrandingResourceMapCache
        implements BaseColumns, BrandingResourceMapCacheColumns {
        public static final Uri CONTENT_URI = Uri.parse("content:
    }
    public interface OutgoingRmqColumns {
        String RMQ_ID = "rmq_id";
        String TIMESTAMP = "ts";
        String DATA = "data";
        String PROTOBUF_TAG = "type";
    }
    public static final class OutgoingRmq implements BaseColumns, OutgoingRmqColumns {
        private static String[] RMQ_ID_PROJECTION = new String[] {
                RMQ_ID,
        };
        public static final long queryHighestRmqId(ContentResolver resolver) {
            Cursor cursor = resolver.query(Imps.OutgoingRmq.CONTENT_URI_FOR_HIGHEST_RMQ_ID,
                    RMQ_ID_PROJECTION,
                    null, 
                    null, 
                    null  
                    );
            long retVal = 0;
            try {
                if (cursor.moveToFirst()) {
                    retVal = cursor.getLong(cursor.getColumnIndexOrThrow(RMQ_ID));
                }
            } finally {
                cursor.close();
            }
            return retVal;
        }
        public static final Uri CONTENT_URI = Uri.parse("content:
        public static final Uri CONTENT_URI_FOR_HIGHEST_RMQ_ID =
                Uri.parse("content:
        public static final String DEFAULT_SORT_ORDER = "rmq_id ASC";
    }
    public interface LastRmqIdColumns {
        String RMQ_ID = "rmq_id";
    }
    public static final class LastRmqId implements BaseColumns, LastRmqIdColumns {
        private static String[] PROJECTION = new String[] {
                RMQ_ID,
        };
        public static final long queryLastRmqId(ContentResolver resolver) {
            Cursor cursor = resolver.query(Imps.LastRmqId.CONTENT_URI,
                    PROJECTION,
                    null, 
                    null, 
                    null  
                    );
            long retVal = 0;
            try {
                if (cursor.moveToFirst()) {
                    retVal = cursor.getLong(cursor.getColumnIndexOrThrow(RMQ_ID));
                }
            } finally {
                cursor.close();
            }
            return retVal;
        }
        public static final void saveLastRmqId(ContentResolver resolver, long rmqId) {
            ContentValues values = new ContentValues();
            values.put(_ID, 1);
            values.put(RMQ_ID, rmqId);
            resolver.insert(CONTENT_URI, values);
        }
        public static final Uri CONTENT_URI = Uri.parse("content:
    }
    public interface ServerToDeviceRmqIdsColumn {
        String RMQ_ID = "rmq_id";
    }
    public static final class ServerToDeviceRmqIds implements BaseColumns,
            ServerToDeviceRmqIdsColumn {
        public static final Uri CONTENT_URI = Uri.parse("content:
    }
}
