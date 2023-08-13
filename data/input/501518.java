public class ImpsProvider extends ContentProvider {
    private static final String LOG_TAG = "imProvider";
    private static final boolean DBG = false;
    private static final String AUTHORITY = "imps";
    private static final String TABLE_ACCOUNTS = "accounts";
    private static final String TABLE_PROVIDERS = "providers";
    private static final String TABLE_PROVIDER_SETTINGS = "providerSettings";
    private static final String TABLE_CONTACTS = "contacts";
    private static final String TABLE_CONTACTS_ETAG = "contactsEtag";
    private static final String TABLE_BLOCKED_LIST = "blockedList";
    private static final String TABLE_CONTACT_LIST = "contactList";
    private static final String TABLE_INVITATIONS = "invitations";
    private static final String TABLE_GROUP_MEMBERS = "groupMembers";
    private static final String TABLE_PRESENCE = "presence";
    private static final String USERNAME = "username";
    private static final String TABLE_CHATS = "chats";
    private static final String TABLE_AVATARS = "avatars";
    private static final String TABLE_SESSION_COOKIES = "sessionCookies";
    private static final String TABLE_MESSAGES = "messages";
    private static final String TABLE_IN_MEMORY_MESSAGES = "inMemoryMessages";
    private static final String TABLE_ACCOUNT_STATUS = "accountStatus";
    private static final String TABLE_BRANDING_RESOURCE_MAP_CACHE = "brandingResMapCache";
    private static final String TABLE_OUTGOING_RMQ_MESSAGES = "outgoingRmqMessages";
    private static final String TABLE_LAST_RMQ_ID = "lastrmqid";
    private static final String TABLE_S2D_RMQ_IDS = "s2dRmqIds";
    private static final String DATABASE_NAME = "imps.db";
    private static final int DATABASE_VERSION = 1;
    protected static final int MATCH_PROVIDERS = 1;
    protected static final int MATCH_PROVIDERS_BY_ID = 2;
    protected static final int MATCH_PROVIDERS_WITH_ACCOUNT = 3;
    protected static final int MATCH_ACCOUNTS = 10;
    protected static final int MATCH_ACCOUNTS_BY_ID = 11;
    protected static final int MATCH_CONTACTS = 18;
    protected static final int MATCH_CONTACTS_JOIN_PRESENCE = 19;
    protected static final int MATCH_CONTACTS_BAREBONE = 20;
    protected static final int MATCH_CHATTING_CONTACTS = 21;
    protected static final int MATCH_CONTACTS_BY_PROVIDER = 22;
    protected static final int MATCH_CHATTING_CONTACTS_BY_PROVIDER = 23;
    protected static final int MATCH_NO_CHATTING_CONTACTS_BY_PROVIDER = 24;
    protected static final int MATCH_ONLINE_CONTACTS_BY_PROVIDER = 25;
    protected static final int MATCH_OFFLINE_CONTACTS_BY_PROVIDER = 26;
    protected static final int MATCH_CONTACT = 27;
    protected static final int MATCH_CONTACTS_BULK = 28;
    protected static final int MATCH_ONLINE_CONTACT_COUNT = 30;
    protected static final int MATCH_BLOCKED_CONTACTS = 31;
    protected static final int MATCH_CONTACTLISTS = 32;
    protected static final int MATCH_CONTACTLISTS_BY_PROVIDER = 33;
    protected static final int MATCH_CONTACTLIST = 34;
    protected static final int MATCH_BLOCKEDLIST = 35;
    protected static final int MATCH_BLOCKEDLIST_BY_PROVIDER = 36;
    protected static final int MATCH_CONTACTS_ETAGS = 37;
    protected static final int MATCH_CONTACTS_ETAG = 38;
    protected static final int MATCH_PRESENCE = 40;
    protected static final int MATCH_PRESENCE_ID = 41;
    protected static final int MATCH_PRESENCE_BY_ACCOUNT = 42;
    protected static final int MATCH_PRESENCE_SEED_BY_ACCOUNT = 43;
    protected static final int MATCH_PRESENCE_BULK = 44;
    protected static final int MATCH_MESSAGES = 50;
    protected static final int MATCH_MESSAGES_BY_CONTACT = 51;
    protected static final int MATCH_MESSAGES_BY_THREAD_ID = 52;
    protected static final int MATCH_MESSAGES_BY_PROVIDER = 53;
    protected static final int MATCH_MESSAGES_BY_ACCOUNT = 54;
    protected static final int MATCH_MESSAGE = 55;
    protected static final int MATCH_OTR_MESSAGES = 56;
    protected static final int MATCH_OTR_MESSAGES_BY_CONTACT = 57;
    protected static final int MATCH_OTR_MESSAGES_BY_THREAD_ID = 58;
    protected static final int MATCH_OTR_MESSAGES_BY_PROVIDER = 59;
    protected static final int MATCH_OTR_MESSAGES_BY_ACCOUNT = 60;
    protected static final int MATCH_OTR_MESSAGE = 61;
    protected static final int MATCH_GROUP_MEMBERS = 65;
    protected static final int MATCH_GROUP_MEMBERS_BY_GROUP = 66;
    protected static final int MATCH_AVATARS = 70;
    protected static final int MATCH_AVATAR = 71;
    protected static final int MATCH_AVATAR_BY_PROVIDER = 72;
    protected static final int MATCH_CHATS = 80;
    protected static final int MATCH_CHATS_BY_ACCOUNT = 81;
    protected static final int MATCH_CHATS_ID = 82;
    protected static final int MATCH_SESSIONS = 83;
    protected static final int MATCH_SESSIONS_BY_PROVIDER = 84;
    protected static final int MATCH_PROVIDER_SETTINGS = 90;
    protected static final int MATCH_PROVIDER_SETTINGS_BY_ID = 91;
    protected static final int MATCH_PROVIDER_SETTINGS_BY_ID_AND_NAME = 92;
    protected static final int MATCH_INVITATIONS = 100;
    protected static final int MATCH_INVITATION  = 101;
    protected static final int MATCH_ACCOUNTS_STATUS = 104;
    protected static final int MATCH_ACCOUNT_STATUS = 105;
    protected static final int MATCH_BRANDING_RESOURCE_MAP_CACHE = 106;
    protected static final int MATCH_OUTGOING_RMQ_MESSAGES = 200;
    protected static final int MATCH_OUTGOING_RMQ_MESSAGE = 201;
    protected static final int MATCH_OUTGOING_HIGHEST_RMQ_ID = 202;
    protected static final int MATCH_LAST_RMQ_ID = 203;
    protected static final int MATCH_S2D_RMQ_IDS = 204;
    protected final UriMatcher mUrlMatcher = new UriMatcher(UriMatcher.NO_MATCH);
    private final String mTransientDbName;
    private static final HashMap<String, String> sProviderAccountsProjectionMap;
    private static final HashMap<String, String> sContactsProjectionMap;
    private static final HashMap<String, String> sContactListProjectionMap;
    private static final HashMap<String, String> sBlockedListProjectionMap;
    private static final HashMap<String, String> sMessagesProjectionMap;
    private static final HashMap<String, String> sInMemoryMessagesProjectionMap;
    private static final String PROVIDER_JOIN_ACCOUNT_TABLE =
            "providers LEFT OUTER JOIN accounts ON " +
                    "(providers._id = accounts.provider AND accounts.active = 1) " +
                    "LEFT OUTER JOIN accountStatus ON (accounts._id = accountStatus.account)";
    private static final String CONTACT_JOIN_PRESENCE_TABLE =
            "contacts LEFT OUTER JOIN presence ON (contacts._id = presence.contact_id)";
    private static final String CONTACT_JOIN_PRESENCE_CHAT_TABLE =
            CONTACT_JOIN_PRESENCE_TABLE +
                    " LEFT OUTER JOIN chats ON (contacts._id = chats.contact_id)";
    private static final String CONTACT_JOIN_PRESENCE_CHAT_AVATAR_TABLE =
            CONTACT_JOIN_PRESENCE_CHAT_TABLE +
                    " LEFT OUTER JOIN avatars ON (contacts.username = avatars.contact" +
                    " AND contacts.account = avatars.account_id)";
    private static final String BLOCKEDLIST_JOIN_AVATAR_TABLE =
            "blockedList LEFT OUTER JOIN avatars ON (blockedList.username = avatars.contact" +
                    " AND blockedList.account = avatars.account_id)";
    private static final String MESSAGE_JOIN_CONTACT_TABLE =
            "messages LEFT OUTER JOIN contacts ON (contacts._id = messages.thread_id)";
    private static final String IN_MEMORY_MESSAGES_JOIN_CONTACT_TABLE =
            "inMemoryMessages LEFT OUTER JOIN contacts ON " +
                "(contacts._id = inMemoryMessages.thread_id)";
    private static final String NON_BLOCKED_CONTACTS_WHERE_CLAUSE = "("
        + Imps.Contacts.TYPE + " IS NULL OR "
        + Imps.Contacts.TYPE + "!="
        + String.valueOf(Imps.Contacts.TYPE_BLOCKED)
        + ")";
    private static final String BLOCKED_CONTACTS_WHERE_CLAUSE =
        "(contacts." + Imps.Contacts.TYPE + "=" + Imps.Contacts.TYPE_BLOCKED + ")";
    private static final String CONTACT_ID = TABLE_CONTACTS + '.' + Imps.Contacts._ID;
    private static final String PRESENCE_CONTACT_ID = TABLE_PRESENCE + '.' + Imps.Presence.CONTACT_ID;
    protected SQLiteOpenHelper mOpenHelper;
    private final String mDatabaseName;
    private final int mDatabaseVersion;
    private final String[] BACKFILL_PROJECTION = {
        Imps.Chats._ID, Imps.Chats.SHORTCUT, Imps.Chats.LAST_MESSAGE_DATE
    };
    private final String[] FIND_SHORTCUT_PROJECTION = {
        Imps.Chats._ID, Imps.Chats.SHORTCUT
    };
    private static final String[] CONTACT_ID_PROJECTION = new String[] {
            Imps.Contacts._ID,    
    };
    private static final int CONTACT_ID_COLUMN = 0;
    private static final String CONTACTS_WITH_NO_PRESENCE_SELECTION =
            Imps.Contacts.ACCOUNT + "=?" + " AND " + Imps.Contacts._ID +
                    " in (select " + CONTACT_ID + " from " + TABLE_CONTACTS +
                    " left outer join " + TABLE_PRESENCE + " on " + CONTACT_ID + '=' +
                    PRESENCE_CONTACT_ID + " where " + PRESENCE_CONTACT_ID + " IS NULL)";
    private String[] mQueryContactIdSelectionArgs1 = new String[1];
    private static final String CONTACT_ID_QUERY_SELECTION =
            Imps.Contacts.ACCOUNT + "=? AND " + Imps.Contacts.USERNAME + "=?";
    private String[] mQueryContactIdSelectionArgs2 = new String[2];
    private class DatabaseHelper extends SQLiteOpenHelper {
        DatabaseHelper(Context context) {
            super(context, mDatabaseName, null, mDatabaseVersion);
        }
        @Override
        public void onCreate(SQLiteDatabase db) {
            if (DBG) log("DatabaseHelper.onCreate");
            db.execSQL("CREATE TABLE " + TABLE_PROVIDERS + " (" +
                    "_id INTEGER PRIMARY KEY," +
                    "name TEXT," +       
                    "fullname TEXT," +   
                    "category TEXT," +   
                    "signup_url TEXT" +  
                    ");");
            db.execSQL("CREATE TABLE " + TABLE_ACCOUNTS + " (" +
                    "_id INTEGER PRIMARY KEY," +
                    "name TEXT," +
                    "provider INTEGER," +
                    "username TEXT," +
                    "pw TEXT," +
                    "active INTEGER NOT NULL DEFAULT 0," +
                    "locked INTEGER NOT NULL DEFAULT 0," +
                    "keep_signed_in INTEGER NOT NULL DEFAULT 0," +
                    "last_login_state INTEGER NOT NULL DEFAULT 0," +
                    "UNIQUE (provider, username)" +
                    ");");
            createContactsTables(db);
            createMessageChatTables(db, true );
            db.execSQL("CREATE TABLE " + TABLE_AVATARS + " (" +
                    "_id INTEGER PRIMARY KEY," +
                    "contact TEXT," +
                    "provider_id INTEGER," +
                    "account_id INTEGER," +
                    "hash TEXT," +
                    "data BLOB," +     
                    "UNIQUE (account_id, contact)" +
                    ");");
            db.execSQL("CREATE TABLE " + TABLE_PROVIDER_SETTINGS + " (" +
                    "_id INTEGER PRIMARY KEY," +
                    "provider INTEGER," +
                    "name TEXT," +
                    "value TEXT," +
                    "UNIQUE (provider, name)" +
                    ");");
            db.execSQL("create TABLE " + TABLE_BRANDING_RESOURCE_MAP_CACHE + " (" +
                    "_id INTEGER PRIMARY KEY," +
                    "provider_id INTEGER," +
                    "app_res_id INTEGER," +
                    "plugin_res_id INTEGER" +
                    ");");
            db.execSQL("CREATE TRIGGER account_cleanup " +
                    "DELETE ON " + TABLE_ACCOUNTS +
                    " BEGIN " +
                        "DELETE FROM " + TABLE_AVATARS + " WHERE account_id= OLD._id;" +
                    "END");
            db.execSQL("CREATE TRIGGER provider_cleanup " +
                    "DELETE ON " + TABLE_PROVIDERS +
                    " BEGIN " +
                        "DELETE FROM " + TABLE_PROVIDER_SETTINGS + " WHERE provider= OLD._id;" +
                    "END");
            db.execSQL("create TABLE " + TABLE_OUTGOING_RMQ_MESSAGES + " (" +
                    "_id INTEGER PRIMARY KEY," +
                    "rmq_id INTEGER," +
                    "type INTEGER," +
                    "ts INTEGER," +
                    "data TEXT" +
                    ");");
            db.execSQL("create TABLE " + TABLE_LAST_RMQ_ID + " (" +
                    "_id INTEGER PRIMARY KEY," +
                    "rmq_id INTEGER" +
                    ");");
            db.execSQL("create TABLE " + TABLE_S2D_RMQ_IDS + " (" +
                    "_id INTEGER PRIMARY KEY," +
                    "rmq_id INTEGER" +
                    ");");
        }
        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
            Log.d(LOG_TAG, "Upgrading database from version " + oldVersion + " to " + newVersion);
            switch (oldVersion) {
                case 43:    
                case 44:
                    if (newVersion <= 44) {
                        return;
                    }
                    db.beginTransaction();
                    try {
                        db.execSQL("ALTER TABLE " + TABLE_PROVIDERS + " ADD COLUMN category TEXT;");
                        db.execSQL("ALTER TABLE " + TABLE_CONTACTS + " ADD COLUMN otr INTEGER;");
                        db.setTransactionSuccessful();
                    } catch (Throwable ex) {
                        Log.e(LOG_TAG, ex.getMessage(), ex);
                        break; 
                    } finally {
                        db.endTransaction();
                    }
                case 45:
                    if (newVersion <= 45) {
                        return;
                    }
                    db.beginTransaction();
                    try {
                        db.execSQL(
                                "ALTER TABLE " + TABLE_CONTACTS_ETAG + " ADD COLUMN otr_etag TEXT;");
                        db.setTransactionSuccessful();
                    } catch (Throwable ex) {
                        Log.e(LOG_TAG, ex.getMessage(), ex);
                        break; 
                    } finally {
                        db.endTransaction();
                    }
                case 46:
                    if (newVersion <= 46) {
                        return;
                    }
                    db.beginTransaction();
                    try {
                        db.execSQL("create TABLE " + TABLE_BRANDING_RESOURCE_MAP_CACHE + " (" +
                                "_id INTEGER PRIMARY KEY," +
                                "provider_id INTEGER," +
                                "app_res_id INTEGER," +
                                "plugin_res_id INTEGER" +
                                ");");
                        db.setTransactionSuccessful();
                    } catch (Throwable ex) {
                        Log.e(LOG_TAG, ex.getMessage(), ex);
                        break; 
                    } finally {
                        db.endTransaction();
                    }
                case 47:
                    if (newVersion <= 47) {
                        return;
                    }
                    db.beginTransaction();
                    try {
                        createMessageChatTables(db, false );
                        db.setTransactionSuccessful();
                    } catch (Throwable ex) {
                        Log.e(LOG_TAG, ex.getMessage(), ex);
                        break; 
                    } finally {
                        db.endTransaction();
                    }
                case 48:
                case 49:
                case 50:
                    if (newVersion <= 50) {
                        return;
                    }
                    db.beginTransaction();
                    try {
                        db.execSQL("create TABLE " + TABLE_S2D_RMQ_IDS + " (" +
                                "_id INTEGER PRIMARY KEY," +
                                "rmq_id INTEGER" +
                                ");");
                        db.setTransactionSuccessful();
                    } catch (Throwable ex) {
                        Log.e(LOG_TAG, ex.getMessage(), ex);
                        break; 
                    } finally {
                        db.endTransaction();
                    }
                case 51:
                    if (newVersion <= 51) {
                        return;
                    }
                    db.beginTransaction();
                    try {
                        db.execSQL(
                                "ALTER TABLE " + TABLE_MESSAGES + " ADD COLUMN show_ts INTEGER;");
                        db.setTransactionSuccessful();
                    } catch (Throwable ex) {
                        Log.e(LOG_TAG, ex.getMessage(), ex);
                        break; 
                    } finally {
                        db.endTransaction();
                    }
                    return;
            }
            Log.w(LOG_TAG, "Couldn't upgrade db to " + newVersion + ". Destroying old data.");
            destroyOldTables(db);
            onCreate(db);
        }
        private void destroyOldTables(SQLiteDatabase db) {
            db.execSQL("DROP TABLE IF EXISTS " + TABLE_PROVIDERS);
            db.execSQL("DROP TABLE IF EXISTS " + TABLE_ACCOUNTS);
            db.execSQL("DROP TABLE IF EXISTS " + TABLE_CONTACT_LIST);
            db.execSQL("DROP TABLE IF EXISTS " + TABLE_BLOCKED_LIST);
            db.execSQL("DROP TABLE IF EXISTS " + TABLE_CONTACTS);
            db.execSQL("DROP TABLE IF EXISTS " + TABLE_CONTACTS_ETAG);
            db.execSQL("DROP TABLE IF EXISTS " + TABLE_AVATARS);
            db.execSQL("DROP TABLE IF EXISTS " + TABLE_PROVIDER_SETTINGS);
            db.execSQL("DROP TABLE IF EXISTS " + TABLE_BRANDING_RESOURCE_MAP_CACHE);
            db.execSQL("DROP TABLE IF EXISTS " + TABLE_MESSAGES);
            db.execSQL("DROP TABLE IF EXISTS " + TABLE_CHATS);
            db.execSQL("DROP TABLE IF EXISTS " + TABLE_OUTGOING_RMQ_MESSAGES);
            db.execSQL("DROP TABLE IF EXISTS " + TABLE_LAST_RMQ_ID);
            db.execSQL("DROP TABLE IF EXISTS " + TABLE_S2D_RMQ_IDS);
        }
        private void createContactsTables(SQLiteDatabase db) {
            if (DBG) log("createContactsTables");
            StringBuilder buf = new StringBuilder();
            String contactsTableName = TABLE_CONTACTS;
            buf.append("CREATE TABLE IF NOT EXISTS ");
            buf.append(contactsTableName);
            buf.append(" (");
            buf.append("_id INTEGER PRIMARY KEY,");
            buf.append("username TEXT,");
            buf.append("nickname TEXT,");
            buf.append("provider INTEGER,");
            buf.append("account INTEGER,");
            buf.append("contactList INTEGER,");
            buf.append("type INTEGER,");
            buf.append("subscriptionStatus INTEGER,");
            buf.append("subscriptionType INTEGER,");
            buf.append("qc INTEGER,");
            buf.append("rejected INTEGER,");
            buf.append("otr INTEGER");
            buf.append(");");
            db.execSQL(buf.toString());
            buf.delete(0, buf.length());
            buf.append("CREATE TABLE IF NOT EXISTS ");
            buf.append(TABLE_CONTACTS_ETAG);
            buf.append(" (");
            buf.append("_id INTEGER PRIMARY KEY,");
            buf.append("etag TEXT,");
            buf.append("otr_etag TEXT,");
            buf.append("account INTEGER UNIQUE");
            buf.append(");");
            db.execSQL(buf.toString());
            buf.delete(0, buf.length());
            buf.append("CREATE TABLE IF NOT EXISTS ");
            buf.append(TABLE_CONTACT_LIST);
            buf.append(" (");
            buf.append("_id INTEGER PRIMARY KEY,");
            buf.append("name TEXT,");
            buf.append("provider INTEGER,");
            buf.append("account INTEGER");
            buf.append(");");
            db.execSQL(buf.toString());
            buf.delete(0, buf.length());
            buf.append("CREATE TABLE IF NOT EXISTS ");
            buf.append(TABLE_BLOCKED_LIST);
            buf.append(" (");
            buf.append("_id INTEGER PRIMARY KEY,");
            buf.append("username TEXT,");
            buf.append("nickname TEXT,");
            buf.append("provider INTEGER,");
            buf.append("account INTEGER");
            buf.append(");");
            db.execSQL(buf.toString());
        }
        private void createMessageChatTables(SQLiteDatabase db,
                                             boolean addShowTsColumnForMessagesTable) {
            if (DBG) log("createMessageChatTables");
            StringBuilder buf = new StringBuilder();
            buf.append("CREATE TABLE IF NOT EXISTS ");
            buf.append(TABLE_MESSAGES);
            buf.append(" (_id INTEGER PRIMARY KEY,");
            buf.append("thread_id INTEGER,");
            buf.append("nickname TEXT,");
            buf.append("body TEXT,");
            buf.append("date INTEGER,");
            buf.append("type INTEGER,");
            buf.append("packet_id TEXT UNIQUE,");
            buf.append("err_code INTEGER NOT NULL DEFAULT 0,");
            buf.append("err_msg TEXT,");
            buf.append("is_muc INTEGER");
            if (addShowTsColumnForMessagesTable) {
                buf.append(",show_ts INTEGER");
            }
            buf.append(");");
            String sqlStatement = buf.toString();
            if (DBG) log("create message table: " + sqlStatement);
            db.execSQL(sqlStatement);
            buf.delete(0, buf.length());
            buf.append("CREATE TABLE IF NOT EXISTS ");
            buf.append(TABLE_CHATS);
            buf.append(" (_id INTEGER PRIMARY KEY,");
            buf.append("contact_id INTEGER UNIQUE,");
            buf.append("jid_resource TEXT,"); 
            buf.append("groupchat INTEGER,"); 
            buf.append("last_unread_message TEXT,"); 
            buf.append("last_message_date INTEGER,"); 
            buf.append("unsent_composed_message TEXT,"); 
            buf.append("shortcut INTEGER);"); 
            sqlStatement = buf.toString();
            if (DBG) log("create chat table: " + sqlStatement);
            db.execSQL(sqlStatement);
            buf.delete(0, buf.length());
            buf.append("CREATE TRIGGER IF NOT EXISTS contact_cleanup ");
            buf.append("DELETE ON contacts ");
            buf.append("BEGIN ");
            buf.append("DELETE FROM ").append(TABLE_CHATS).append(" WHERE contact_id = OLD._id;");
            buf.append("DELETE FROM ").append(TABLE_MESSAGES).append(" WHERE thread_id = OLD._id;");
            buf.append("END");
            sqlStatement = buf.toString();
            if (DBG) log("create trigger: " + sqlStatement);
            db.execSQL(sqlStatement);
        }
        private void createInMemoryMessageTables(SQLiteDatabase db, String tablePrefix) {
            String tableName = (tablePrefix != null) ?
                    tablePrefix+TABLE_IN_MEMORY_MESSAGES : TABLE_IN_MEMORY_MESSAGES;
            db.execSQL("CREATE TABLE IF NOT EXISTS " + tableName + " (" +
                    "_id INTEGER PRIMARY KEY," +
                    "thread_id INTEGER," +
                    "nickname TEXT," +
                    "body TEXT," +
                    "date INTEGER," +    
                    "type INTEGER," +
                    "packet_id TEXT UNIQUE," +
                    "err_code INTEGER NOT NULL DEFAULT 0," +
                    "err_msg TEXT," +
                    "is_muc INTEGER," +
                    "show_ts INTEGER" +
                    ");");
        }
        @Override
        public void onOpen(SQLiteDatabase db) {
            if (db.isReadOnly()) {
                Log.w(LOG_TAG, "ImProvider database opened in read only mode.");
                Log.w(LOG_TAG, "Transient tables not created.");
                return;
            }
            if (DBG) log("##### createTransientTables");
            String cpDbName;
            db.execSQL("ATTACH DATABASE ':memory:' AS " + mTransientDbName + ";");
            cpDbName = mTransientDbName + ".";
            createInMemoryMessageTables(db, cpDbName);
            db.execSQL("CREATE TABLE IF NOT EXISTS " + cpDbName + TABLE_PRESENCE + " ("+
                    "_id INTEGER PRIMARY KEY," +
                    "contact_id INTEGER UNIQUE," +
                    "jid_resource TEXT," +  
                    "client_type INTEGER," + 
                    "priority INTEGER," +   
                    "mode INTEGER," +       
                    "status TEXT" +         
                    ");");
            db.execSQL("CREATE TABLE IF NOT EXISTS " + cpDbName + TABLE_INVITATIONS + " (" +
                    "_id INTEGER PRIMARY KEY," +
                    "providerId INTEGER," +
                    "accountId INTEGER," +
                    "inviteId TEXT," +
                    "sender TEXT," +
                    "groupName TEXT," +
                    "note TEXT," +
                    "status INTEGER" +
                    ");");
            db.execSQL("CREATE TABLE IF NOT EXISTS " + cpDbName + TABLE_GROUP_MEMBERS + " (" +
                    "_id INTEGER PRIMARY KEY," +
                    "groupId INTEGER," +
                    "username TEXT," +
                    "nickname TEXT" +
                    ");");
            db.execSQL("CREATE TABLE IF NOT EXISTS " + cpDbName + TABLE_ACCOUNT_STATUS + " (" +
                    "_id INTEGER PRIMARY KEY," +
                    "account INTEGER UNIQUE," +
                    "presenceStatus INTEGER," +
                    "connStatus INTEGER" +
                    ");"
            );
            db.execSQL("CREATE TABLE IF NOT EXISTS " + cpDbName + TABLE_SESSION_COOKIES + " ("+
                    "_id INTEGER PRIMARY KEY," +
                    "provider INTEGER," +
                    "account INTEGER," +
                    "name TEXT," +
                    "value TEXT" +
                    ");");
        }
    }
    static {
        sProviderAccountsProjectionMap = new HashMap<String, String>();
        sProviderAccountsProjectionMap.put(Imps.Provider._ID,
                "providers._id AS _id");
        sProviderAccountsProjectionMap.put(Imps.Provider._COUNT,
                "COUNT(*) AS _account");
        sProviderAccountsProjectionMap.put(Imps.Provider.NAME,
                "providers.name AS name");
        sProviderAccountsProjectionMap.put(Imps.Provider.FULLNAME,
                "providers.fullname AS fullname");
        sProviderAccountsProjectionMap.put(Imps.Provider.CATEGORY,
                "providers.category AS category");
        sProviderAccountsProjectionMap.put(Imps.Provider.ACTIVE_ACCOUNT_ID,
                "accounts._id AS account_id");
        sProviderAccountsProjectionMap.put(Imps.Provider.ACTIVE_ACCOUNT_USERNAME,
                "accounts.username AS account_username");
        sProviderAccountsProjectionMap.put(Imps.Provider.ACTIVE_ACCOUNT_PW,
                "accounts.pw AS account_pw");
        sProviderAccountsProjectionMap.put(Imps.Provider.ACTIVE_ACCOUNT_LOCKED,
                "accounts.locked AS account_locked");
        sProviderAccountsProjectionMap.put(Imps.Provider.ACTIVE_ACCOUNT_KEEP_SIGNED_IN,
                "accounts.keep_signed_in AS account_keepSignedIn");
        sProviderAccountsProjectionMap.put(Imps.Provider.ACCOUNT_PRESENCE_STATUS,
                "accountStatus.presenceStatus AS account_presenceStatus");
        sProviderAccountsProjectionMap.put(Imps.Provider.ACCOUNT_CONNECTION_STATUS,
                "accountStatus.connStatus AS account_connStatus");
        sContactsProjectionMap = new HashMap<String, String>();
        sContactsProjectionMap.put(Imps.Contacts._ID, "contacts._id AS _id");
        sContactsProjectionMap.put(Imps.Contacts._COUNT, "COUNT(*) AS _count");
        sContactsProjectionMap.put(Imps.Contacts._ID, "contacts._id as _id");
        sContactsProjectionMap.put(Imps.Contacts.USERNAME, "contacts.username as username");
        sContactsProjectionMap.put(Imps.Contacts.NICKNAME, "contacts.nickname as nickname");
        sContactsProjectionMap.put(Imps.Contacts.PROVIDER, "contacts.provider as provider");
        sContactsProjectionMap.put(Imps.Contacts.ACCOUNT, "contacts.account as account");
        sContactsProjectionMap.put(Imps.Contacts.CONTACTLIST, "contacts.contactList as contactList");
        sContactsProjectionMap.put(Imps.Contacts.TYPE, "contacts.type as type");
        sContactsProjectionMap.put(Imps.Contacts.SUBSCRIPTION_STATUS,
                "contacts.subscriptionStatus as subscriptionStatus");
        sContactsProjectionMap.put(Imps.Contacts.SUBSCRIPTION_TYPE,
                "contacts.subscriptionType as subscriptionType");
        sContactsProjectionMap.put(Imps.Contacts.QUICK_CONTACT, "contacts.qc as qc");
        sContactsProjectionMap.put(Imps.Contacts.REJECTED, "contacts.rejected as rejected");
        sContactsProjectionMap.put(Imps.Presence.CONTACT_ID,
                "presence.contact_id AS contact_id");
        sContactsProjectionMap.put(Imps.Contacts.PRESENCE_STATUS,
                "presence.mode AS mode");
        sContactsProjectionMap.put(Imps.Contacts.PRESENCE_CUSTOM_STATUS,
                "presence.status AS status");
        sContactsProjectionMap.put(Imps.Contacts.CLIENT_TYPE,
                "presence.client_type AS client_type");
        sContactsProjectionMap.put(Imps.Contacts.CHATS_CONTACT,
                "chats.contact_id AS chats_contact_id");
        sContactsProjectionMap.put(Imps.Chats.JID_RESOURCE,
                "chats.jid_resource AS jid_resource");
        sContactsProjectionMap.put(Imps.Chats.GROUP_CHAT,
                "chats.groupchat AS groupchat");
        sContactsProjectionMap.put(Imps.Contacts.LAST_UNREAD_MESSAGE,
                "chats.last_unread_message AS last_unread_message");
        sContactsProjectionMap.put(Imps.Contacts.LAST_MESSAGE_DATE,
                "chats.last_message_date AS last_message_date");
        sContactsProjectionMap.put(Imps.Contacts.UNSENT_COMPOSED_MESSAGE,
                "chats.unsent_composed_message AS unsent_composed_message");
        sContactsProjectionMap.put(Imps.Contacts.SHORTCUT, "chats.SHORTCUT AS shortcut");
        sContactsProjectionMap.put(Imps.Contacts.AVATAR_HASH, "avatars.hash AS avatars_hash");
        sContactsProjectionMap.put(Imps.Contacts.AVATAR_DATA, "avatars.data AS avatars_data");
        sContactListProjectionMap = new HashMap<String, String>();
        sContactListProjectionMap.put(Imps.ContactList._ID, "contactList._id AS _id");
        sContactListProjectionMap.put(Imps.ContactList._COUNT, "COUNT(*) AS _count");
        sContactListProjectionMap.put(Imps.ContactList.NAME, "name");
        sContactListProjectionMap.put(Imps.ContactList.PROVIDER, "provider");
        sContactListProjectionMap.put(Imps.ContactList.ACCOUNT, "account");
        sBlockedListProjectionMap = new HashMap<String, String>();
        sBlockedListProjectionMap.put(Imps.BlockedList._ID, "blockedList._id AS _id");
        sBlockedListProjectionMap.put(Imps.BlockedList._COUNT, "COUNT(*) AS _count");
        sBlockedListProjectionMap.put(Imps.BlockedList.USERNAME, "username");
        sBlockedListProjectionMap.put(Imps.BlockedList.NICKNAME, "nickname");
        sBlockedListProjectionMap.put(Imps.BlockedList.PROVIDER, "provider");
        sBlockedListProjectionMap.put(Imps.BlockedList.ACCOUNT, "account");
        sBlockedListProjectionMap.put(Imps.BlockedList.AVATAR_DATA,
                "avatars.data AS avatars_data");
        sMessagesProjectionMap = new HashMap<String, String>();
        sMessagesProjectionMap.put(Imps.Messages._ID, "messages._id AS _id");
        sMessagesProjectionMap.put(Imps.Messages._COUNT, "COUNT(*) AS _count");
        sMessagesProjectionMap.put(Imps.Messages.THREAD_ID, "messages.thread_id AS thread_id");
        sMessagesProjectionMap.put(Imps.Messages.PACKET_ID, "messages.packet_id AS packet_id");
        sMessagesProjectionMap.put(Imps.Messages.NICKNAME, "messages.nickname AS nickname");
        sMessagesProjectionMap.put(Imps.Messages.BODY, "messages.body AS body");
        sMessagesProjectionMap.put(Imps.Messages.DATE, "messages.date AS date");
        sMessagesProjectionMap.put(Imps.Messages.TYPE, "messages.type AS type");
        sMessagesProjectionMap.put(Imps.Messages.ERROR_CODE, "messages.err_code AS err_code");
        sMessagesProjectionMap.put(Imps.Messages.ERROR_MESSAGE, "messages.err_msg AS err_msg");
        sMessagesProjectionMap.put(Imps.Messages.IS_GROUP_CHAT, "messages.is_muc AS is_muc");
        sMessagesProjectionMap.put(Imps.Messages.DISPLAY_SENT_TIME, "messages.show_ts AS show_ts");
        sMessagesProjectionMap.put(Imps.Messages.CONTACT, "contacts.username AS contact");
        sMessagesProjectionMap.put(Imps.Contacts.PROVIDER, "contacts.provider AS provider");
        sMessagesProjectionMap.put(Imps.Contacts.ACCOUNT, "contacts.account AS account");
        sMessagesProjectionMap.put("contact_type", "contacts.type AS contact_type");
        sInMemoryMessagesProjectionMap = new HashMap<String, String>();
        sInMemoryMessagesProjectionMap.put(Imps.Messages._ID,
                "inMemoryMessages._id AS _id");
        sInMemoryMessagesProjectionMap.put(Imps.Messages._COUNT,
                "COUNT(*) AS _count");
        sInMemoryMessagesProjectionMap.put(Imps.Messages.THREAD_ID,
                "inMemoryMessages.thread_id AS thread_id");
        sInMemoryMessagesProjectionMap.put(Imps.Messages.PACKET_ID,
                "inMemoryMessages.packet_id AS packet_id");
        sInMemoryMessagesProjectionMap.put(Imps.Messages.NICKNAME,
                "inMemoryMessages.nickname AS nickname");
        sInMemoryMessagesProjectionMap.put(Imps.Messages.BODY,
                "inMemoryMessages.body AS body");
        sInMemoryMessagesProjectionMap.put(Imps.Messages.DATE,
                "inMemoryMessages.date AS date");
        sInMemoryMessagesProjectionMap.put(Imps.Messages.TYPE,
                "inMemoryMessages.type AS type");
        sInMemoryMessagesProjectionMap.put(Imps.Messages.ERROR_CODE,
                "inMemoryMessages.err_code AS err_code");
        sInMemoryMessagesProjectionMap.put(Imps.Messages.ERROR_MESSAGE,
                "inMemoryMessages.err_msg AS err_msg");
        sInMemoryMessagesProjectionMap.put(Imps.Messages.IS_GROUP_CHAT,
                "inMemoryMessages.is_muc AS is_muc");
        sInMemoryMessagesProjectionMap.put(Imps.Messages.DISPLAY_SENT_TIME,
                "inMemoryMessages.show_ts AS show_ts");
        sInMemoryMessagesProjectionMap.put(Imps.Messages.CONTACT, "contacts.username AS contact");
        sInMemoryMessagesProjectionMap.put(Imps.Contacts.PROVIDER, "contacts.provider AS provider");
        sInMemoryMessagesProjectionMap.put(Imps.Contacts.ACCOUNT, "contacts.account AS account");
        sInMemoryMessagesProjectionMap.put("contact_type", "contacts.type AS contact_type");
    }
    public ImpsProvider() {
        this(DATABASE_NAME, DATABASE_VERSION);
        setupImUrlMatchers(AUTHORITY);
        setupMcsUrlMatchers(AUTHORITY);
    }
    protected ImpsProvider(String dbName, int dbVersion) {
        mDatabaseName = dbName;
        mDatabaseVersion = dbVersion;
        mTransientDbName = "transient_" + dbName.replace(".", "_");
    }
    private void setupImUrlMatchers(String authority) {
        mUrlMatcher.addURI(authority, "providers", MATCH_PROVIDERS);
        mUrlMatcher.addURI(authority, "providers/#", MATCH_PROVIDERS_BY_ID);
        mUrlMatcher.addURI(authority, "providers/account", MATCH_PROVIDERS_WITH_ACCOUNT);
        mUrlMatcher.addURI(authority, "accounts", MATCH_ACCOUNTS);
        mUrlMatcher.addURI(authority, "accounts/#", MATCH_ACCOUNTS_BY_ID);
        mUrlMatcher.addURI(authority, "contacts", MATCH_CONTACTS);
        mUrlMatcher.addURI(authority, "contactsWithPresence", MATCH_CONTACTS_JOIN_PRESENCE);
        mUrlMatcher.addURI(authority, "contactsBarebone", MATCH_CONTACTS_BAREBONE);
        mUrlMatcher.addURI(authority, "contacts/#/#", MATCH_CONTACTS_BY_PROVIDER);
        mUrlMatcher.addURI(authority, "contacts/chatting", MATCH_CHATTING_CONTACTS);
        mUrlMatcher.addURI(authority, "contacts/chatting/#/#", MATCH_CHATTING_CONTACTS_BY_PROVIDER);
        mUrlMatcher.addURI(authority, "contacts/online/#/#", MATCH_ONLINE_CONTACTS_BY_PROVIDER);
        mUrlMatcher.addURI(authority, "contacts/offline/#/#", MATCH_OFFLINE_CONTACTS_BY_PROVIDER);
        mUrlMatcher.addURI(authority, "contacts/#", MATCH_CONTACT);
        mUrlMatcher.addURI(authority, "contacts/blocked", MATCH_BLOCKED_CONTACTS);
        mUrlMatcher.addURI(authority, "bulk_contacts", MATCH_CONTACTS_BULK);
        mUrlMatcher.addURI(authority, "contacts/onlineCount", MATCH_ONLINE_CONTACT_COUNT);
        mUrlMatcher.addURI(authority, "contactLists", MATCH_CONTACTLISTS);
        mUrlMatcher.addURI(authority, "contactLists/#/#", MATCH_CONTACTLISTS_BY_PROVIDER);
        mUrlMatcher.addURI(authority, "contactLists/#", MATCH_CONTACTLIST);
        mUrlMatcher.addURI(authority, "blockedList", MATCH_BLOCKEDLIST);
        mUrlMatcher.addURI(authority, "blockedList/#/#", MATCH_BLOCKEDLIST_BY_PROVIDER);
        mUrlMatcher.addURI(authority, "contactsEtag", MATCH_CONTACTS_ETAGS);
        mUrlMatcher.addURI(authority, "contactsEtag/#", MATCH_CONTACTS_ETAG);
        mUrlMatcher.addURI(authority, "presence", MATCH_PRESENCE);
        mUrlMatcher.addURI(authority, "presence/#", MATCH_PRESENCE_ID);
        mUrlMatcher.addURI(authority, "presence/account/#", MATCH_PRESENCE_BY_ACCOUNT);
        mUrlMatcher.addURI(authority, "seed_presence/account/#", MATCH_PRESENCE_SEED_BY_ACCOUNT);
        mUrlMatcher.addURI(authority, "bulk_presence", MATCH_PRESENCE_BULK);
        mUrlMatcher.addURI(authority, "messages", MATCH_MESSAGES);
        mUrlMatcher.addURI(authority, "messagesByAcctAndContact/#, false );
        }
        return result;
    }
    @Override
    public final int delete(final Uri url, final String selection,
            final String[] selectionArgs) {
        int result;
        SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        db.beginTransaction();
        try {
            result = deleteInternal(url, selection, selectionArgs);
            db.setTransactionSuccessful();
        } finally {
            db.endTransaction();
        }
        if (result > 0) {
            getContext().getContentResolver()
                    .notifyChange(url, null , false );
        }
        return result;
    }
    @Override
    public final Uri insert(final Uri url, final ContentValues values) {
        Uri result;
        SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        db.beginTransaction();
        try {
            result = insertInternal(url, values);
            db.setTransactionSuccessful();
        } finally {
            db.endTransaction();
        }
        if (result != null) {
            getContext().getContentResolver()
                    .notifyChange(url, null , false );
        }
        return result;
    }
    @Override
    public final Cursor query(final Uri url, final String[] projection,
            final String selection, final String[] selectionArgs,
            final String sortOrder) {
        return queryInternal(url, projection, selection, selectionArgs, sortOrder);
    }
    public Cursor queryInternal(Uri url, String[] projectionIn,
            String selection, String[] selectionArgs, String sort) {
        SQLiteQueryBuilder qb = new SQLiteQueryBuilder();
        StringBuilder whereClause = new StringBuilder();
        if(selection != null) {
            whereClause.append(selection);
        }
        String groupBy = null;
        String limit = null;
        int match = mUrlMatcher.match(url);
        if (DBG) {
            log("query " + url + ", match " + match + ", where " + selection);
            if (selectionArgs != null) {
                for (String selectionArg : selectionArgs) {
                    log("     selectionArg: " + selectionArg);
                }
            }
        }
        switch (match) {
            case MATCH_PROVIDERS_BY_ID:
                appendWhere(whereClause, Imps.Provider._ID, "=", url.getPathSegments().get(1));
            case MATCH_PROVIDERS:
                qb.setTables(TABLE_PROVIDERS);
                break;
            case MATCH_PROVIDERS_WITH_ACCOUNT:
                qb.setTables(PROVIDER_JOIN_ACCOUNT_TABLE);
                qb.setProjectionMap(sProviderAccountsProjectionMap);
                break;
            case MATCH_ACCOUNTS_BY_ID:
                appendWhere(whereClause, Imps.Account._ID, "=", url.getPathSegments().get(1));
            case MATCH_ACCOUNTS:
                qb.setTables(TABLE_ACCOUNTS);
                break;
            case MATCH_CONTACTS:
                qb.setTables(CONTACT_JOIN_PRESENCE_CHAT_AVATAR_TABLE);
                qb.setProjectionMap(sContactsProjectionMap);
                break;
            case MATCH_CONTACTS_JOIN_PRESENCE:
                qb.setTables(CONTACT_JOIN_PRESENCE_TABLE);
                qb.setProjectionMap(sContactsProjectionMap);
                break;
            case MATCH_CONTACTS_BAREBONE:
                qb.setTables(TABLE_CONTACTS);
                break;
            case MATCH_CHATTING_CONTACTS:
                qb.setTables(CONTACT_JOIN_PRESENCE_CHAT_AVATAR_TABLE);
                qb.setProjectionMap(sContactsProjectionMap);
                appendWhere(whereClause, "chats.last_message_date IS NOT NULL");
                break;
            case MATCH_CONTACTS_BY_PROVIDER:
                buildQueryContactsByProvider(qb, whereClause, url);
                appendWhere(whereClause, NON_BLOCKED_CONTACTS_WHERE_CLAUSE);
                break;
            case MATCH_CHATTING_CONTACTS_BY_PROVIDER:
                buildQueryContactsByProvider(qb, whereClause, url);
                appendWhere(whereClause, "chats.last_message_date IS NOT NULL");
                break;
            case MATCH_NO_CHATTING_CONTACTS_BY_PROVIDER:
                buildQueryContactsByProvider(qb, whereClause, url);
                appendWhere(whereClause, "chats.last_message_date IS NULL");
                appendWhere(whereClause, NON_BLOCKED_CONTACTS_WHERE_CLAUSE);
                break;
            case MATCH_ONLINE_CONTACTS_BY_PROVIDER:
                buildQueryContactsByProvider(qb, whereClause, url);
                appendWhere(whereClause, Imps.Contacts.PRESENCE_STATUS, "!=", Imps.Presence.OFFLINE);
                appendWhere(whereClause, NON_BLOCKED_CONTACTS_WHERE_CLAUSE);
                break;
            case MATCH_OFFLINE_CONTACTS_BY_PROVIDER:
                buildQueryContactsByProvider(qb, whereClause, url);
                appendWhere(whereClause, Imps.Contacts.PRESENCE_STATUS, "=", Imps.Presence.OFFLINE);
                appendWhere(whereClause, NON_BLOCKED_CONTACTS_WHERE_CLAUSE);
                break;
            case MATCH_BLOCKED_CONTACTS:
                qb.setTables(CONTACT_JOIN_PRESENCE_CHAT_AVATAR_TABLE);
                qb.setProjectionMap(sContactsProjectionMap);
                appendWhere(whereClause, BLOCKED_CONTACTS_WHERE_CLAUSE);
                break;
            case MATCH_CONTACT:
                qb.setTables(CONTACT_JOIN_PRESENCE_CHAT_AVATAR_TABLE);
                qb.setProjectionMap(sContactsProjectionMap);
                appendWhere(whereClause, "contacts._id", "=", url.getPathSegments().get(1));
                break;
            case MATCH_ONLINE_CONTACT_COUNT:
                qb.setTables(CONTACT_JOIN_PRESENCE_CHAT_TABLE);
                qb.setProjectionMap(sContactsProjectionMap);
                appendWhere(whereClause, Imps.Contacts.PRESENCE_STATUS, "!=", Imps.Presence.OFFLINE);
                appendWhere(whereClause, "chats.last_message_date IS NULL");
                appendWhere(whereClause, NON_BLOCKED_CONTACTS_WHERE_CLAUSE);
                groupBy = Imps.Contacts.CONTACTLIST;
                break;
            case MATCH_CONTACTLISTS_BY_PROVIDER:
                appendWhere(whereClause, Imps.ContactList.ACCOUNT, "=",
                        url.getPathSegments().get(2));
            case MATCH_CONTACTLISTS:
                qb.setTables(TABLE_CONTACT_LIST);
                qb.setProjectionMap(sContactListProjectionMap);
                break;
            case MATCH_CONTACTLIST:
                qb.setTables(TABLE_CONTACT_LIST);
                appendWhere(whereClause, Imps.ContactList._ID, "=", url.getPathSegments().get(1));
                break;
            case MATCH_BLOCKEDLIST:
                qb.setTables(BLOCKEDLIST_JOIN_AVATAR_TABLE);
                qb.setProjectionMap(sBlockedListProjectionMap);
                break;
            case MATCH_BLOCKEDLIST_BY_PROVIDER:
                qb.setTables(BLOCKEDLIST_JOIN_AVATAR_TABLE);
                qb.setProjectionMap(sBlockedListProjectionMap);
                appendWhere(whereClause, Imps.BlockedList.ACCOUNT, "=",
                        url.getPathSegments().get(2));
                break;
            case MATCH_CONTACTS_ETAGS:
                qb.setTables(TABLE_CONTACTS_ETAG);
                break;
            case MATCH_CONTACTS_ETAG:
                qb.setTables(TABLE_CONTACTS_ETAG);
                appendWhere(whereClause, "_id", "=", url.getPathSegments().get(1));
                break;
            case MATCH_MESSAGES_BY_THREAD_ID:
                appendWhere(whereClause, Imps.Messages.THREAD_ID, "=", url.getPathSegments().get(1));
            case MATCH_MESSAGES:
                qb.setTables(TABLE_MESSAGES);
                final String selectionClause = whereClause.toString();
                final String query1 = qb.buildQuery(projectionIn, selectionClause,
                        null, null, null, null, null );
                qb = new SQLiteQueryBuilder();
                qb.setTables(TABLE_IN_MEMORY_MESSAGES);
                final String query2 = qb.buildQuery(projectionIn,
                        selectionClause, null, null, null, null, null );
                final String query = qb.buildUnionQuery(new String[] {query1, query2}, sort, null);
                final SQLiteDatabase db = mOpenHelper.getWritableDatabase();
                Cursor c = db.rawQueryWithFactory(null, query, null, TABLE_MESSAGES);
                if ((c != null) && !isTemporary()) {
                    c.setNotificationUri(getContext().getContentResolver(), url);
                }
                return c;
            case MATCH_MESSAGE:
                qb.setTables(TABLE_MESSAGES);
                appendWhere(whereClause, Imps.Messages._ID, "=", url.getPathSegments().get(1));
                break;
            case MATCH_MESSAGES_BY_CONTACT:
                qb.setTables(MESSAGE_JOIN_CONTACT_TABLE);
                qb.setProjectionMap(sMessagesProjectionMap);
                appendWhere(whereClause, Imps.Contacts.ACCOUNT, "=", url.getPathSegments().get(1));
                appendWhere(whereClause, "contacts.username", "=",
                        decodeURLSegment(url.getPathSegments().get(2)));
                final String sel = whereClause.toString();
                final String q1 = qb.buildQuery(projectionIn, sel, null, null, null, null, null);
                qb = new SQLiteQueryBuilder();
                qb.setTables(IN_MEMORY_MESSAGES_JOIN_CONTACT_TABLE);
                qb.setProjectionMap(sInMemoryMessagesProjectionMap);
                final String q2 = qb.buildQuery(projectionIn, sel, null, null, null, null, null);
                final String q3 = qb.buildUnionQuery(new String[] {q1, q2}, sort, null);
                final SQLiteDatabase db2 = mOpenHelper.getWritableDatabase();
                Cursor c2 = db2.rawQueryWithFactory(null, q3, null, MESSAGE_JOIN_CONTACT_TABLE);
                if ((c2 != null) && !isTemporary()) {
                    c2.setNotificationUri(getContext().getContentResolver(), url);
                }
                return c2;
            case MATCH_INVITATIONS:
                qb.setTables(TABLE_INVITATIONS);
                break;
            case MATCH_INVITATION:
                qb.setTables(TABLE_INVITATIONS);
                appendWhere(whereClause, Imps.Invitation._ID, "=", url.getPathSegments().get(1));
                break;
            case MATCH_GROUP_MEMBERS:
                qb.setTables(TABLE_GROUP_MEMBERS);
                break;
            case MATCH_GROUP_MEMBERS_BY_GROUP:
                qb.setTables(TABLE_GROUP_MEMBERS);
                appendWhere(whereClause, Imps.GroupMembers.GROUP, "=", url.getPathSegments().get(1));
                break;
            case MATCH_AVATARS:
                qb.setTables(TABLE_AVATARS);
                break;
            case MATCH_AVATAR_BY_PROVIDER:
                qb.setTables(TABLE_AVATARS);
                appendWhere(whereClause, Imps.Avatars.ACCOUNT, "=", url.getPathSegments().get(2));
                break;
            case MATCH_CHATS:
                qb.setTables(TABLE_CHATS);
                break;
            case MATCH_CHATS_ID:
                qb.setTables(TABLE_CHATS);
                appendWhere(whereClause, Imps.Chats.CONTACT_ID, "=", url.getPathSegments().get(1));
                break;
            case MATCH_CHATS_BY_ACCOUNT:
                qb.setTables(TABLE_CHATS);
                String accountStr = decodeURLSegment(url.getLastPathSegment());
                appendWhere(whereClause, buildContactIdSelection(Imps.Chats.CONTACT_ID,
                        Imps.Contacts.ACCOUNT + "='" + accountStr + "'"));
                break;
            case MATCH_PRESENCE:
                qb.setTables(TABLE_PRESENCE);
                break;
            case MATCH_PRESENCE_ID:
                qb.setTables(TABLE_PRESENCE);
                appendWhere(whereClause, Imps.Presence.CONTACT_ID, "=", url.getPathSegments().get(1));
                break;
            case MATCH_SESSIONS:
                qb.setTables(TABLE_SESSION_COOKIES);
                break;
            case MATCH_SESSIONS_BY_PROVIDER:
                qb.setTables(TABLE_SESSION_COOKIES);
                appendWhere(whereClause, Imps.SessionCookies.ACCOUNT, "=", url.getPathSegments().get(2));
                break;
            case MATCH_PROVIDER_SETTINGS_BY_ID_AND_NAME:
                appendWhere(whereClause, Imps.ProviderSettings.NAME, "=", url.getPathSegments().get(2));
            case MATCH_PROVIDER_SETTINGS_BY_ID:
                appendWhere(whereClause, Imps.ProviderSettings.PROVIDER, "=", url.getPathSegments().get(1));
            case MATCH_PROVIDER_SETTINGS:
                qb.setTables(TABLE_PROVIDER_SETTINGS);
                break;
            case MATCH_ACCOUNTS_STATUS:
                qb.setTables(TABLE_ACCOUNT_STATUS);
                break;
            case MATCH_ACCOUNT_STATUS:
                qb.setTables(TABLE_ACCOUNT_STATUS);
                appendWhere(whereClause, Imps.AccountStatus.ACCOUNT, "=",
                        url.getPathSegments().get(1));
                break;
            case MATCH_BRANDING_RESOURCE_MAP_CACHE:
                qb.setTables(TABLE_BRANDING_RESOURCE_MAP_CACHE);
                break;
            case MATCH_OUTGOING_RMQ_MESSAGES:
                qb.setTables(TABLE_OUTGOING_RMQ_MESSAGES);
                break;
            case MATCH_OUTGOING_HIGHEST_RMQ_ID:
                qb.setTables(TABLE_OUTGOING_RMQ_MESSAGES);
                sort = "rmq_id DESC";
                limit = "1";
                break;
            case MATCH_LAST_RMQ_ID:
                qb.setTables(TABLE_LAST_RMQ_ID);
                limit = "1";
                break;
            case MATCH_S2D_RMQ_IDS:
                qb.setTables(TABLE_S2D_RMQ_IDS);
                break;
            default:
                throw new IllegalArgumentException("Unknown URL " + url);
        }
        final SQLiteDatabase db = mOpenHelper.getReadableDatabase();
        Cursor c = null;
        try {
            c = qb.query(db, projectionIn, whereClause.toString(), selectionArgs,
                    groupBy, null, sort, limit);
            if (c != null) {
                switch(match) {
                case MATCH_CHATTING_CONTACTS:
                case MATCH_CONTACTS_BY_PROVIDER:
                case MATCH_CHATTING_CONTACTS_BY_PROVIDER:
                case MATCH_ONLINE_CONTACTS_BY_PROVIDER:
                case MATCH_OFFLINE_CONTACTS_BY_PROVIDER:
                case MATCH_CONTACTS_BAREBONE:
                case MATCH_CONTACTS_JOIN_PRESENCE:
                case MATCH_ONLINE_CONTACT_COUNT:
                    url = Imps.Contacts.CONTENT_URI;
                    break;
                }
                if (DBG) log("set notify url " + url);
                c.setNotificationUri(getContext().getContentResolver(), url);
            }
        } catch (Exception ex) {
            Log.e(LOG_TAG, "query db caught ", ex);
        }
        return c;
    }
    private void buildQueryContactsByProvider(SQLiteQueryBuilder qb,
            StringBuilder whereClause, Uri url) {
        qb.setTables(CONTACT_JOIN_PRESENCE_CHAT_AVATAR_TABLE);
        qb.setProjectionMap(sContactsProjectionMap);
        appendWhere(whereClause, Imps.Contacts.ACCOUNT, "=", url.getLastPathSegment());
    }
    @Override
    public String getType(Uri url) {
        int match = mUrlMatcher.match(url);
        switch (match) {
            case MATCH_PROVIDERS:
                return Imps.Provider.CONTENT_TYPE;
            case MATCH_PROVIDERS_BY_ID:
                return Imps.Provider.CONTENT_ITEM_TYPE;
            case MATCH_ACCOUNTS:
                return Imps.Account.CONTENT_TYPE;
            case MATCH_ACCOUNTS_BY_ID:
                return Imps.Account.CONTENT_ITEM_TYPE;
            case MATCH_CONTACTS:
            case MATCH_CONTACTS_BY_PROVIDER:
            case MATCH_ONLINE_CONTACTS_BY_PROVIDER:
            case MATCH_OFFLINE_CONTACTS_BY_PROVIDER:
            case MATCH_CONTACTS_BULK:
            case MATCH_CONTACTS_BAREBONE:
            case MATCH_CONTACTS_JOIN_PRESENCE:
                return Imps.Contacts.CONTENT_TYPE;
            case MATCH_CONTACT:
                return Imps.Contacts.CONTENT_ITEM_TYPE;
            case MATCH_CONTACTLISTS:
            case MATCH_CONTACTLISTS_BY_PROVIDER:
                return Imps.ContactList.CONTENT_TYPE;
            case MATCH_CONTACTLIST:
                return Imps.ContactList.CONTENT_ITEM_TYPE;
            case MATCH_BLOCKEDLIST:
            case MATCH_BLOCKEDLIST_BY_PROVIDER:
                return Imps.BlockedList.CONTENT_TYPE;
            case MATCH_CONTACTS_ETAGS:
            case MATCH_CONTACTS_ETAG:
                return Imps.ContactsEtag.CONTENT_TYPE;
            case MATCH_MESSAGES:
            case MATCH_MESSAGES_BY_CONTACT:
            case MATCH_MESSAGES_BY_THREAD_ID:
            case MATCH_MESSAGES_BY_PROVIDER:
            case MATCH_MESSAGES_BY_ACCOUNT:
            case MATCH_OTR_MESSAGES:
            case MATCH_OTR_MESSAGES_BY_CONTACT:
            case MATCH_OTR_MESSAGES_BY_THREAD_ID:
            case MATCH_OTR_MESSAGES_BY_PROVIDER:
            case MATCH_OTR_MESSAGES_BY_ACCOUNT:
                return Imps.Messages.CONTENT_TYPE;
            case MATCH_MESSAGE:
            case MATCH_OTR_MESSAGE:
                return Imps.Messages.CONTENT_ITEM_TYPE;
            case MATCH_PRESENCE:
            case MATCH_PRESENCE_BULK:
                return Imps.Presence.CONTENT_TYPE;
            case MATCH_AVATARS:
                return Imps.Avatars.CONTENT_TYPE;
            case MATCH_AVATAR:
                return Imps.Avatars.CONTENT_ITEM_TYPE;
            case MATCH_CHATS:
                return Imps.Chats.CONTENT_TYPE;
            case MATCH_CHATS_ID:
                return Imps.Chats.CONTENT_ITEM_TYPE;
            case MATCH_INVITATIONS:
                return Imps.Invitation.CONTENT_TYPE;
            case MATCH_INVITATION:
                return Imps.Invitation.CONTENT_ITEM_TYPE;
            case MATCH_GROUP_MEMBERS:
            case MATCH_GROUP_MEMBERS_BY_GROUP:
                return Imps.GroupMembers.CONTENT_TYPE;
            case MATCH_SESSIONS:
            case MATCH_SESSIONS_BY_PROVIDER:
                return Imps.SessionCookies.CONTENT_TYPE;
            case MATCH_PROVIDER_SETTINGS:
                return Imps.ProviderSettings.CONTENT_TYPE;
            case MATCH_ACCOUNTS_STATUS:
                return Imps.AccountStatus.CONTENT_TYPE;
            case MATCH_ACCOUNT_STATUS:
                return Imps.AccountStatus.CONTENT_ITEM_TYPE;
            default:
                throw new IllegalArgumentException("Unknown URL");
        }
    }
    boolean insertBulkContacts(ContentValues values) {
        ArrayList<String> usernames = values.getStringArrayList(Imps.Contacts.USERNAME);
        ArrayList<String> nicknames = values.getStringArrayList(Imps.Contacts.NICKNAME);
        int usernameCount = usernames.size();
        int nicknameCount = nicknames.size();
        if (usernameCount != nicknameCount) {
            Log.e(LOG_TAG, "[ImProvider] insertBulkContacts: input bundle " +
                    "username & nickname lists have diff. length!");
            return false;
        }
        ArrayList<String> contactTypeArray = values.getStringArrayList(Imps.Contacts.TYPE);
        ArrayList<String> subscriptionStatusArray =
                values.getStringArrayList(Imps.Contacts.SUBSCRIPTION_STATUS);
        ArrayList<String> subscriptionTypeArray =
                values.getStringArrayList(Imps.Contacts.SUBSCRIPTION_TYPE);
        ArrayList<String> quickContactArray = values.getStringArrayList(Imps.Contacts.QUICK_CONTACT);
        ArrayList<String> rejectedArray = values.getStringArrayList(Imps.Contacts.REJECTED);
        int sum = 0;
        final SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        db.beginTransaction();
        try {
            Long provider = values.getAsLong(Imps.Contacts.PROVIDER);
            Long account = values.getAsLong(Imps.Contacts.ACCOUNT);
            Long listId = values.getAsLong(Imps.Contacts.CONTACTLIST);
            ContentValues contactValues = new ContentValues();
            contactValues.put(Imps.Contacts.PROVIDER, provider);
            contactValues.put(Imps.Contacts.ACCOUNT, account);
            contactValues.put(Imps.Contacts.CONTACTLIST, listId);
            ContentValues presenceValues = new ContentValues();
            presenceValues.put(Imps.Presence.PRESENCE_STATUS,
                    Imps.Presence.OFFLINE);
            for (int i=0; i<usernameCount; i++) {
                String username = usernames.get(i);
                String nickname = nicknames.get(i);
                int type = 0;
                int subscriptionStatus = 0;
                int subscriptionType = 0;
                int quickContact = 0;
                int rejected = 0;
                try {
                    type = Integer.parseInt(contactTypeArray.get(i));
                    if (subscriptionStatusArray != null) {
                        subscriptionStatus = Integer.parseInt(subscriptionStatusArray.get(i));
                    }
                    if (subscriptionTypeArray != null) {
                        subscriptionType = Integer.parseInt(subscriptionTypeArray.get(i));
                    }
                    if (quickContactArray != null) {
                        quickContact = Integer.parseInt(quickContactArray.get(i));
                    }
                    if (rejectedArray != null) {
                        rejected = Integer.parseInt(rejectedArray.get(i));
                    }
                } catch (NumberFormatException ex) {
                    Log.e(LOG_TAG, "insertBulkContacts: caught " + ex);
                }
                contactValues.put(Imps.Contacts.USERNAME, username);
                contactValues.put(Imps.Contacts.NICKNAME, nickname);
                contactValues.put(Imps.Contacts.TYPE, type);
                if (subscriptionStatusArray != null) {
                    contactValues.put(Imps.Contacts.SUBSCRIPTION_STATUS, subscriptionStatus);
                }
                if (subscriptionTypeArray != null) {
                    contactValues.put(Imps.Contacts.SUBSCRIPTION_TYPE, subscriptionType);
                }
                if (quickContactArray != null) {
                    contactValues.put(Imps.Contacts.QUICK_CONTACT, quickContact);
                }
                if (rejectedArray != null) {
                    contactValues.put(Imps.Contacts.REJECTED, rejected);
                }
                long rowId;
                rowId = db.insert(TABLE_CONTACTS, USERNAME, contactValues);
                if (rowId > 0) {
                    sum++;
                    if (DBG) log("### seedPresence for contact id " + rowId);
                    presenceValues.put(Imps.Presence.CONTACT_ID, rowId);
                    try {
                        db.insert(TABLE_PRESENCE, null, presenceValues);
                    } catch (android.database.sqlite.SQLiteConstraintException ex) {
                        Log.w(LOG_TAG, "insertBulkContacts: seeding presence caught " + ex);
                    }
                }
                db.yieldIfContended();
            }
            db.setTransactionSuccessful();
        } finally {
            db.endTransaction();
        }
        if (DBG) log("insertBulkContacts: added " + sum + " contacts!");
        return true;
    }
    int updateBulkContacts(ContentValues values, String userWhere) {
        ArrayList<String> usernames = values.getStringArrayList(Imps.Contacts.USERNAME);
        ArrayList<String> nicknames = values.getStringArrayList(Imps.Contacts.NICKNAME);
        int usernameCount = usernames.size();
        int nicknameCount = nicknames.size();
        if (usernameCount != nicknameCount) {
            Log.e(LOG_TAG, "[ImProvider] updateBulkContacts: input bundle " +
                    "username & nickname lists have diff. length!");
            return 0;
        }
        ArrayList<String> contactTypeArray = values.getStringArrayList(Imps.Contacts.TYPE);
        ArrayList<String> subscriptionStatusArray =
                values.getStringArrayList(Imps.Contacts.SUBSCRIPTION_STATUS);
        ArrayList<String> subscriptionTypeArray =
                values.getStringArrayList(Imps.Contacts.SUBSCRIPTION_TYPE);
        ArrayList<String> quickContactArray = values.getStringArrayList(Imps.Contacts.QUICK_CONTACT);
        ArrayList<String> rejectedArray = values.getStringArrayList(Imps.Contacts.REJECTED);
        final SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        db.beginTransaction();
        int sum = 0;
        try {
            Long provider = values.getAsLong(Imps.Contacts.PROVIDER);
            Long account = values.getAsLong(Imps.Contacts.ACCOUNT);
            ContentValues contactValues = new ContentValues();
            contactValues.put(Imps.Contacts.PROVIDER, provider);
            contactValues.put(Imps.Contacts.ACCOUNT, account);
            StringBuilder updateSelection = new StringBuilder();
            String[] updateSelectionArgs = new String[1];
            for (int i=0; i<usernameCount; i++) {
                String username = usernames.get(i);
                String nickname = nicknames.get(i);
                int type = 0;
                int subscriptionStatus = 0;
                int subscriptionType = 0;
                int quickContact = 0;
                int rejected = 0;
                try {
                    type = Integer.parseInt(contactTypeArray.get(i));
                    subscriptionStatus = Integer.parseInt(subscriptionStatusArray.get(i));
                    subscriptionType = Integer.parseInt(subscriptionTypeArray.get(i));
                    quickContact = Integer.parseInt(quickContactArray.get(i));
                    rejected = Integer.parseInt(rejectedArray.get(i));
                } catch (NumberFormatException ex) {
                    Log.e(LOG_TAG, "insertBulkContacts: caught " + ex);
                }
                if (DBG) log("updateBulkContacts[" + i + "] username=" +
                        username + ", nickname=" + nickname + ", type=" + type +
                        ", subscriptionStatus=" + subscriptionStatus + ", subscriptionType=" +
                        subscriptionType + ", qc=" + quickContact);
                contactValues.put(Imps.Contacts.USERNAME, username);
                contactValues.put(Imps.Contacts.NICKNAME, nickname);
                contactValues.put(Imps.Contacts.TYPE, type);
                contactValues.put(Imps.Contacts.SUBSCRIPTION_STATUS, subscriptionStatus);
                contactValues.put(Imps.Contacts.SUBSCRIPTION_TYPE, subscriptionType);
                contactValues.put(Imps.Contacts.QUICK_CONTACT, quickContact);
                contactValues.put(Imps.Contacts.REJECTED, rejected);
                updateSelection.delete(0, updateSelection.length());
                updateSelection.append(userWhere);
                updateSelection.append(" AND ");
                updateSelection.append(Imps.Contacts.USERNAME);
                updateSelection.append("=?");
                updateSelectionArgs[0] = username;
                int numUpdated = db.update(TABLE_CONTACTS, contactValues,
                        updateSelection.toString(), updateSelectionArgs);
                if (numUpdated == 0) {
                    Log.e(LOG_TAG, "[ImProvider] updateBulkContacts: " +
                            " update failed for selection = " + updateSelection);
                } else {
                    sum += numUpdated;
                }
                db.yieldIfContended();
            }
            db.setTransactionSuccessful();
        } finally {
            db.endTransaction();
        }
        if (DBG) log("updateBulkContacts: " + sum + " entries updated");
        return sum;
    }
    private void seedInitialPresenceByAccount(long account) {
        SQLiteQueryBuilder qb = new SQLiteQueryBuilder();
        qb.setTables(TABLE_CONTACTS);
        qb.setProjectionMap(sContactsProjectionMap);
        mQueryContactIdSelectionArgs1[0] = String.valueOf(account);
        final SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        db.beginTransaction();
        Cursor c = null;
        try {
            ContentValues presenceValues = new ContentValues();
            presenceValues.put(Imps.Presence.PRESENCE_STATUS, Imps.Presence.OFFLINE);
            presenceValues.put(Imps.Presence.PRESENCE_CUSTOM_STATUS, "");
            StringBuilder buf = new StringBuilder();
            buf.append(Imps.Presence.CONTACT_ID);
            buf.append(" in (select ");
            buf.append(Imps.Contacts._ID);
            buf.append(" from ");
            buf.append(TABLE_CONTACTS);
            buf.append(" where ");
            buf.append(Imps.Contacts.ACCOUNT);
            buf.append("=?) ");
            String selection = buf.toString();
            if (DBG) log("seedInitialPresence: reset presence selection=" + selection);
            int count = db.update(TABLE_PRESENCE, presenceValues, selection,
                    mQueryContactIdSelectionArgs1);
            if (DBG) log("seedInitialPresence: reset " + count + " presence rows to OFFLINE");
            if (DBG) {
                log("seedInitialPresence: contacts_with_no_presence_selection => " +
                        CONTACTS_WITH_NO_PRESENCE_SELECTION);
            }
            c = qb.query(db,
                    CONTACT_ID_PROJECTION,
                    CONTACTS_WITH_NO_PRESENCE_SELECTION,
                    mQueryContactIdSelectionArgs1,
                    null, null, null, null);
            if (DBG) log("seedInitialPresence: found " + c.getCount() + " contacts w/o presence");
            count = 0;
            while (c.moveToNext()) {
                long id = c.getLong(CONTACT_ID_COLUMN);
                presenceValues.put(Imps.Presence.CONTACT_ID, id);
                try {
                    if (db.insert(TABLE_PRESENCE, null, presenceValues) > 0) {
                        count++;
                    }
                } catch (SQLiteConstraintException ex) {
                    if (DBG) log("seedInitialPresence: insert presence for contact_id " + id +
                            " failed, caught " + ex);
                }
            }
            if (DBG) log("seedInitialPresence: added " + count + " new presence rows");
            db.setTransactionSuccessful();
        } finally {
            if (c != null) {
                c.close();
            }
            db.endTransaction();
        }
    }
    private int updateBulkPresence(ContentValues values, String userWhere, String[] whereArgs) {
        ArrayList<String> usernames = values.getStringArrayList(Imps.Contacts.USERNAME);
        int count = usernames.size();
        Long account = values.getAsLong(Imps.Contacts.ACCOUNT);
        ArrayList<String> priorityArray = values.getStringArrayList(Imps.Presence.PRIORITY);
        ArrayList<String> modeArray = values.getStringArrayList(Imps.Presence.PRESENCE_STATUS);
        ArrayList<String> statusArray = values.getStringArrayList(
                Imps.Presence.PRESENCE_CUSTOM_STATUS);
        ArrayList<String> clientTypeArray = values.getStringArrayList(Imps.Presence.CLIENT_TYPE);
        ArrayList<String> resourceArray = values.getStringArrayList(Imps.Presence.JID_RESOURCE);
        StringBuilder buf = new StringBuilder();
        if (!TextUtils.isEmpty(userWhere)) {
            buf.append(userWhere);
            buf.append(" AND ");
        }
        buf.append(Imps.Presence.CONTACT_ID);
        buf.append(" in (select ");
        buf.append(Imps.Contacts._ID);
        buf.append(" from ");
        buf.append(TABLE_CONTACTS);
        buf.append(" where ");
        buf.append(Imps.Contacts.ACCOUNT);
        buf.append("=? AND ");
        buf.append(Imps.Contacts.USERNAME);
        buf.append(" LIKE ?) AND (");
        buf.append(Imps.Presence.PRIORITY);
        buf.append("<=? OR ");
        buf.append(Imps.Presence.PRIORITY);
        buf.append(" IS NULL OR ");
        buf.append(Imps.Presence.JID_RESOURCE);
        buf.append("=?)");
        String selection = buf.toString();
        if (DBG) log("updateBulkPresence: selection => " + selection);
        int numArgs = (whereArgs != null ? whereArgs.length + 4 : 4);
        String[] selectionArgs = new String[numArgs];
        int selArgsIndex = 0;
        if (whereArgs != null) {
            for (selArgsIndex=0; selArgsIndex<numArgs-1; selArgsIndex++) {
                selectionArgs[selArgsIndex] = whereArgs[selArgsIndex];
            }
        }
        final SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        db.beginTransaction();
        int sum = 0;
        try {
            ContentValues presenceValues = new ContentValues();
            for (int i=0; i<count; i++) {
                String username = usernames.get(i);
                int priority = 0;
                int mode = 0;
                String status = statusArray.get(i);
                String jidResource = resourceArray == null ? "" : resourceArray.get(i);
                int clientType = Imps.Presence.CLIENT_TYPE_DEFAULT;
                try {
                    if (priorityArray != null) {
                        priority = Integer.parseInt(priorityArray.get(i));
                    }
                    if (modeArray != null) {
                        mode = Integer.parseInt(modeArray.get(i));
                    }
                    if (clientTypeArray != null) {
                        clientType = Integer.parseInt(clientTypeArray.get(i));
                    }
                } catch (NumberFormatException ex) {
                    Log.e(LOG_TAG, "[ImProvider] updateBulkPresence: caught " + ex);
                }
                if (modeArray != null) {
                    presenceValues.put(Imps.Presence.PRESENCE_STATUS, mode);
                }
                if (priorityArray != null) {
                    presenceValues.put(Imps.Presence.PRIORITY, priority);
                }
                presenceValues.put(Imps.Presence.PRESENCE_CUSTOM_STATUS, status);
                if (clientTypeArray != null) {
                    presenceValues.put(Imps.Presence.CLIENT_TYPE, clientType);
                }
                if (!TextUtils.isEmpty(jidResource)) {
                    presenceValues.put(Imps.Presence.JID_RESOURCE, jidResource);
                }
                int idx = selArgsIndex;
                selectionArgs[idx++] = String.valueOf(account);
                selectionArgs[idx++] = username;
                selectionArgs[idx++] = String.valueOf(priority);
                selectionArgs[idx] = jidResource;
                int numUpdated = db.update(TABLE_PRESENCE,
                        presenceValues, selection, selectionArgs);
                if (numUpdated == 0) {
                    Log.e(LOG_TAG, "[ImProvider] updateBulkPresence: failed for " + username);
                } else {
                    sum += numUpdated;
                }
                db.yieldIfContended();
            }
            db.setTransactionSuccessful();
        } finally {
            db.endTransaction();
        }
        if (DBG) log("updateBulkPresence: " + sum + " entries updated");
        return sum;
    }
    private Uri insertInternal(Uri url, ContentValues initialValues) {
        Uri resultUri = null;
        long rowID = 0;
        long account = 0;
        String contact = null;
        long threadId = 0;
        boolean notifyContactListContentUri = false;
        boolean notifyContactContentUri = false;
        boolean notifyMessagesContentUri = false;
        boolean notifyMessagesByContactContentUri = false;
        boolean notifyMessagesByThreadIdContentUri = false;
        boolean notifyProviderAccountContentUri = false;
        final SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        int match = mUrlMatcher.match(url);
        if (DBG) log("insert to " + url + ", match " + match);
        switch (match) {
            case MATCH_PROVIDERS:
                rowID = db.insert(TABLE_PROVIDERS, "name", initialValues);
                if (rowID > 0) {
                    resultUri = Uri.parse(Imps.Provider.CONTENT_URI + "/" + rowID);
                }
                notifyProviderAccountContentUri = true;
                break;
            case MATCH_ACCOUNTS:
                rowID = db.insert(TABLE_ACCOUNTS, "name", initialValues);
                if (rowID > 0) {
                    resultUri = Uri.parse(Imps.Account.CONTENT_URI + "/" + rowID);
                }
                notifyProviderAccountContentUri = true;
                break;
            case MATCH_CONTACTS_BY_PROVIDER:
                appendValuesFromUrl(initialValues, url, Imps.Contacts.PROVIDER,
                    Imps.Contacts.ACCOUNT);
            case MATCH_CONTACTS:
            case MATCH_CONTACTS_BAREBONE:
                rowID = db.insert(TABLE_CONTACTS, "username", initialValues);
                if (rowID > 0) {
                    resultUri = Uri.parse(Imps.Contacts.CONTENT_URI + "/" + rowID);
                }
                notifyContactContentUri = true;
                break;
            case MATCH_CONTACTS_BULK:
                if (insertBulkContacts(initialValues)) {
                    resultUri = Imps.Contacts.CONTENT_URI;
                }
                notifyContactContentUri = true;
                break;
            case MATCH_CONTACTLISTS_BY_PROVIDER:
                appendValuesFromUrl(initialValues, url, Imps.ContactList.PROVIDER,
                        Imps.ContactList.ACCOUNT);
            case MATCH_CONTACTLISTS:
                rowID = db.insert(TABLE_CONTACT_LIST, "name", initialValues);
                if (rowID > 0) {
                    resultUri = Uri.parse(Imps.ContactList.CONTENT_URI + "/" + rowID);
                }
                notifyContactListContentUri = true;
                break;
            case MATCH_BLOCKEDLIST_BY_PROVIDER:
                appendValuesFromUrl(initialValues, url, Imps.BlockedList.PROVIDER,
                    Imps.BlockedList.ACCOUNT);
            case MATCH_BLOCKEDLIST:
                rowID = db.insert(TABLE_BLOCKED_LIST, "username", initialValues);
                if (rowID > 0) {
                    resultUri = Uri.parse(Imps.BlockedList.CONTENT_URI + "/" + rowID);
                }
                break;
            case MATCH_CONTACTS_ETAGS:
                rowID = db.replace(TABLE_CONTACTS_ETAG, Imps.ContactsEtag.ETAG, initialValues);
                if (rowID > 0) {
                    resultUri = Uri.parse(Imps.ContactsEtag.CONTENT_URI + "/" + rowID);
                }
                break;
            case MATCH_MESSAGES_BY_CONTACT:
                String accountStr = decodeURLSegment(url.getPathSegments().get(1));
                try {
                    account = Long.parseLong(accountStr);
                } catch (NumberFormatException ex) {
                    throw new IllegalArgumentException();
                }
                contact = decodeURLSegment(url.getPathSegments().get(2));
                initialValues.put(Imps.Messages.THREAD_ID, getContactId(db, accountStr, contact));
                notifyMessagesContentUri = true;
                rowID = db.insert(TABLE_MESSAGES, "thread_id", initialValues);
                if (rowID > 0) {
                    resultUri = Uri.parse(Imps.Messages.CONTENT_URI + "/" + rowID);
                }
                break;
            case MATCH_MESSAGES_BY_THREAD_ID:
                appendValuesFromUrl(initialValues, url, Imps.Messages.THREAD_ID);
            case MATCH_MESSAGES:
                notifyMessagesContentUri = true;
                rowID = db.insert(TABLE_MESSAGES, "thread_id", initialValues);
                if (rowID > 0) {
                    resultUri = Uri.parse(Imps.Messages.CONTENT_URI + "/" + rowID);
                }
                break;
            case MATCH_OTR_MESSAGES_BY_CONTACT:
                String accountStr2 = decodeURLSegment(url.getPathSegments().get(1));
                try {
                    account = Long.parseLong(accountStr2);
                } catch (NumberFormatException ex) {
                    throw new IllegalArgumentException();
                }
                contact = decodeURLSegment(url.getPathSegments().get(2));
                initialValues.put(Imps.Messages.THREAD_ID, getContactId(db, accountStr2, contact));
                notifyMessagesByContactContentUri = true;
                rowID = db.insert(TABLE_IN_MEMORY_MESSAGES, "thread_id", initialValues);
                if (rowID > 0) {
                    resultUri = Uri.parse(Imps.Messages.OTR_MESSAGES_CONTENT_URI + "/" + rowID);
                }
                break;
            case MATCH_OTR_MESSAGES_BY_THREAD_ID:
                try {
                    threadId = Long.parseLong(decodeURLSegment(url.getPathSegments().get(1)));
                } catch (NumberFormatException ex) {
                    throw new IllegalArgumentException();
                }
                initialValues.put(Imps.Messages.THREAD_ID, threadId);
                notifyMessagesByThreadIdContentUri = true;
            case MATCH_OTR_MESSAGES:
                rowID = db.insert(TABLE_IN_MEMORY_MESSAGES, "thread_id", initialValues);
                if (rowID > 0) {
                    resultUri = Uri.parse(Imps.Messages.OTR_MESSAGES_CONTENT_URI + "/" + rowID);
                }
                break;
            case MATCH_INVITATIONS:
                rowID = db.insert(TABLE_INVITATIONS, null, initialValues);
                if (rowID > 0) {
                    resultUri = Uri.parse(Imps.Invitation.CONTENT_URI + "/" + rowID);
                }
                break;
            case MATCH_GROUP_MEMBERS:
                rowID = db.insert(TABLE_GROUP_MEMBERS, "nickname", initialValues);
                if (rowID > 0) {
                    resultUri = Uri.parse(Imps.GroupMembers.CONTENT_URI + "/" + rowID);
                }
                break;
            case MATCH_GROUP_MEMBERS_BY_GROUP:
                appendValuesFromUrl(initialValues, url, Imps.GroupMembers.GROUP);
                rowID = db.insert(TABLE_GROUP_MEMBERS, "nickname", initialValues);
                if (rowID > 0) {
                    resultUri = Uri.parse(Imps.GroupMembers.CONTENT_URI + "/" + rowID);
                }
                break;
            case MATCH_AVATAR_BY_PROVIDER:
                appendValuesFromUrl(initialValues, url, Imps.Avatars.PROVIDER, Imps.Avatars.ACCOUNT);
            case MATCH_AVATARS:
                rowID = db.replace(TABLE_AVATARS, "contact", initialValues);
                if (rowID > 0) {
                    resultUri = Uri.parse(Imps.Avatars.CONTENT_URI + "/" + rowID);
                }
                break;
            case MATCH_CHATS_ID:
                appendValuesFromUrl(initialValues, url, Imps.Chats.CONTACT_ID);
            case MATCH_CHATS:
                initialValues.put(Imps.Chats.SHORTCUT, -1);
                rowID = db.replace(TABLE_CHATS, Imps.Chats.CONTACT_ID, initialValues);
                if (rowID > 0) {
                    resultUri = Uri.parse(Imps.Chats.CONTENT_URI + "/" + rowID);
                    addToQuickSwitch(rowID);
                }
                notifyContactContentUri = true;
                break;
            case MATCH_PRESENCE:
                rowID = db.replace(TABLE_PRESENCE, null, initialValues);
                if (rowID > 0) {
                    resultUri = Uri.parse(Imps.Presence.CONTENT_URI + "/" + rowID);
                }
                notifyContactContentUri = true;
                break;
            case MATCH_PRESENCE_SEED_BY_ACCOUNT:
                try {
                    seedInitialPresenceByAccount(Long.parseLong(url.getLastPathSegment()));
                    resultUri = Imps.Presence.CONTENT_URI;
                } catch (NumberFormatException ex) {
                    throw new IllegalArgumentException();
                }
                break;
            case MATCH_SESSIONS_BY_PROVIDER:
                appendValuesFromUrl(initialValues, url, Imps.SessionCookies.PROVIDER,
                        Imps.SessionCookies.ACCOUNT);
            case MATCH_SESSIONS:
                rowID = db.insert(TABLE_SESSION_COOKIES, null, initialValues);
                if(rowID > 0) {
                    resultUri = Uri.parse(Imps.SessionCookies.CONTENT_URI + "/" + rowID);
                }
                break;
            case MATCH_PROVIDER_SETTINGS:
                rowID = db.replace(TABLE_PROVIDER_SETTINGS, null, initialValues);
                if (rowID > 0) {
                    resultUri = Uri.parse(Imps.ProviderSettings.CONTENT_URI + "/" + rowID);
                }
                break;
            case MATCH_ACCOUNTS_STATUS:
                rowID = db.replace(TABLE_ACCOUNT_STATUS, null, initialValues);
                if (rowID > 0) {
                    resultUri = Uri.parse(Imps.AccountStatus.CONTENT_URI + "/" + rowID);
                }
                notifyProviderAccountContentUri = true;
                break;
            case MATCH_BRANDING_RESOURCE_MAP_CACHE:
                rowID = db.insert(TABLE_BRANDING_RESOURCE_MAP_CACHE, null, initialValues);
                if (rowID > 0) {
                    resultUri = Uri.parse(Imps.BrandingResourceMapCache.CONTENT_URI + "/" + rowID);
                }
                break;
            case MATCH_OUTGOING_RMQ_MESSAGES:
                rowID = db.insert(TABLE_OUTGOING_RMQ_MESSAGES, null, initialValues);
                if (rowID > 0) {
                    resultUri = Uri.parse(Imps.OutgoingRmq.CONTENT_URI + "/" + rowID);
                }
                break;
            case MATCH_LAST_RMQ_ID:
                rowID = db.replace(TABLE_LAST_RMQ_ID, null, initialValues);
                if (rowID > 0) {
                    resultUri = Uri.parse(Imps.LastRmqId.CONTENT_URI + "/" + rowID);
                }
                break;
            case MATCH_S2D_RMQ_IDS:
                rowID = db.insert(TABLE_S2D_RMQ_IDS, null, initialValues);
                if (rowID > 0) {
                    resultUri = Uri.parse(Imps.ServerToDeviceRmqIds.CONTENT_URI + "/" + rowID);
                }
                break;
            default:
                throw new UnsupportedOperationException("Cannot insert into URL: " + url);
        }
        if (resultUri != null) {
            ContentResolver resolver = getContext().getContentResolver();
            if (notifyContactContentUri) {
                resolver.notifyChange(Imps.Contacts.CONTENT_URI, null);
            }
            if (notifyContactListContentUri) {
                resolver.notifyChange(Imps.ContactList.CONTENT_URI, null);
            }
            if (notifyMessagesContentUri) {
                resolver.notifyChange(Imps.Messages.CONTENT_URI, null);
            }
            if (notifyMessagesByContactContentUri) {
                resolver.notifyChange(Imps.Messages.CONTENT_URI, null);
                resolver.notifyChange(Imps.Messages.getContentUriByContact(account, contact), null);
            }
            if (notifyMessagesByThreadIdContentUri) {
                resolver.notifyChange(Imps.Messages.CONTENT_URI, null);
                resolver.notifyChange(Imps.Messages.getContentUriByThreadId(threadId), null);
            }
            if (notifyProviderAccountContentUri) {
                if (DBG) log("notify insert for " + Imps.Provider.CONTENT_URI_WITH_ACCOUNT);
                resolver.notifyChange(Imps.Provider.CONTENT_URI_WITH_ACCOUNT, null);
            }
        }
        return resultUri;
    }
    private void appendValuesFromUrl(ContentValues values, Uri url, String...columns){
        if(url.getPathSegments().size() <= columns.length) {
            throw new IllegalArgumentException("Not enough values in url");
        }
        for(int i = 0; i < columns.length; i++){
            if(values.containsKey(columns[i])){
                throw new UnsupportedOperationException("Cannot override the value for " + columns[i]);
            }
            values.put(columns[i], decodeURLSegment(url.getPathSegments().get(i + 1)));
        }
    }
    private long getContactId(final SQLiteDatabase db,
                              final String accountId, final String contact) {
        SQLiteQueryBuilder qb = new SQLiteQueryBuilder();
        qb.setTables(TABLE_CONTACTS);
        qb.setProjectionMap(sContactsProjectionMap);
        mQueryContactIdSelectionArgs2[0] = accountId;
        mQueryContactIdSelectionArgs2[1] = contact;
        Cursor c = qb.query(db,
                CONTACT_ID_PROJECTION,
                CONTACT_ID_QUERY_SELECTION,
                mQueryContactIdSelectionArgs2,
                null, null, null, null);
        long contactId = 0;
        try {
            if (c.moveToFirst()) {
                contactId = c.getLong(CONTACT_ID_COLUMN);
            }
        } finally {
            c.close();
        }
        return contactId;
    }
    private void addToQuickSwitch(long newRow) {
        int slot = findEmptyQuickSwitchSlot();
        if (slot == -1) {
            return;
        }
        updateSlotForChat(newRow, slot);
    }
    private void backfillQuickSwitchSlots() {
        Cursor c = query(Imps.Chats.CONTENT_URI,
            BACKFILL_PROJECTION,
            Imps.Chats.SHORTCUT + "=-1", null, Imps.Chats.LAST_MESSAGE_DATE + " DESC");
        try {
            if (c.getCount() < 1) {
                return;
            }
            int slot = findEmptyQuickSwitchSlot();
            if (slot != -1) {
                c.moveToFirst();
                long id = c.getLong(c.getColumnIndex(Imps.Chats._ID));
                updateSlotForChat(id, slot);
            }
        } finally {
            c.close();
        }
    }
    private int updateSlotForChat(long chatId, int slot) {
        ContentValues values = new ContentValues();
        values.put(Imps.Chats.SHORTCUT, slot);
        return update(Imps.Chats.CONTENT_URI, values, Imps.Chats._ID + "=?",
            new String[] { Long.toString(chatId) });
    }
    private int findEmptyQuickSwitchSlot() {
        Cursor c = queryInternal(Imps.Chats.CONTENT_URI, FIND_SHORTCUT_PROJECTION, null, null, null);
        final int N = c.getCount();
        try {
            if (N >= 10) {
                return -1;
            }
            int slots = 0;
            int column = c.getColumnIndex(Imps.Chats.SHORTCUT);
            int[] map = new int[] { 0, 9, 8, 7, 6, 5, 4, 3, 2, 1 };
            for (c.moveToFirst(); ! c.isAfterLast(); c.moveToNext()) {
                int slot = c.getInt(column);
                if (slot != -1) {
                    slots |= (1 << map[slot]);
                }
            }
            for (int i = 0; i < 10; i++) {
                if ((slots & (1 << i)) == 0) {
                    return map[i];
                }
            }
            return -1;
        } finally {
            c.close();
        }
    }
    private static final String DELETE_PRESENCE_SELECTION =
            Imps.Presence.CONTACT_ID + " in (select " +
            PRESENCE_CONTACT_ID + " from " + TABLE_PRESENCE + " left outer join " + TABLE_CONTACTS +
            " on " + PRESENCE_CONTACT_ID + '=' + CONTACT_ID + " where " + CONTACT_ID + " IS NULL)";
    private static final String CHATS_CONTACT_ID = TABLE_CHATS + '.' + Imps.Chats.CONTACT_ID;
    private static final String DELETE_CHATS_SELECTION = Imps.Chats.CONTACT_ID + " in (select "+
            CHATS_CONTACT_ID + " from " + TABLE_CHATS + " left outer join " + TABLE_CONTACTS +
            " on " + CHATS_CONTACT_ID + '=' + CONTACT_ID + " where " + CONTACT_ID + " IS NULL)";
    private static final String GROUP_MEMBER_ID = TABLE_GROUP_MEMBERS + '.' + Imps.GroupMembers.GROUP;
    private static final String DELETE_GROUP_MEMBER_SELECTION =
            Imps.GroupMembers.GROUP + " in (select "+
            GROUP_MEMBER_ID + " from " + TABLE_GROUP_MEMBERS + " left outer join " + TABLE_CONTACTS +
            " on " + GROUP_MEMBER_ID + '=' + CONTACT_ID + " where " + CONTACT_ID + " IS NULL)";
    private static final String GROUP_MESSAGES_ID = TABLE_MESSAGES + '.' + Imps.Messages.THREAD_ID;
    private static final String DELETE_GROUP_MESSAGES_SELECTION =
            Imps.Messages.THREAD_ID + " in (select "+ GROUP_MESSAGES_ID + " from " +
                    TABLE_MESSAGES + " left outer join " + TABLE_CONTACTS + " on " +
                    GROUP_MESSAGES_ID + '=' + CONTACT_ID + " where " + CONTACT_ID + " IS NULL)";
    private void performContactRemovalCleanup(long contactId) {
        final SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        if (contactId > 0) {
            StringBuilder buf = new StringBuilder();
            buf.append(Imps.Presence.CONTACT_ID).append('=').append(contactId);
            deleteWithSelection(db, TABLE_PRESENCE, buf.toString(), null);
            buf.delete(0, buf.length());
            buf.append(Imps.GroupMembers.GROUP).append('=').append(contactId);
            deleteWithSelection(db, TABLE_GROUP_MEMBERS, buf.toString(), null);
        } else {
            deleteWithSelection(db, TABLE_PRESENCE, DELETE_PRESENCE_SELECTION, null);
            deleteWithSelection(db, TABLE_GROUP_MEMBERS, DELETE_GROUP_MEMBER_SELECTION, null);
        }
    }
    private void deleteWithSelection(SQLiteDatabase db, String tableName,
            String selection, String[] selectionArgs) {
        if (DBG) log("deleteWithSelection: table " + tableName + ", selection => " + selection);
        int count = db.delete(tableName, selection, selectionArgs);
        if (DBG) log("deleteWithSelection: deleted " + count + " rows");
    }
    private String buildContactIdSelection(String columnName, String contactSelection) {
        StringBuilder buf = new StringBuilder();
        buf.append(columnName);
        buf.append(" in (select ");
        buf.append(Imps.Contacts._ID);
        buf.append(" from ");
        buf.append(TABLE_CONTACTS);
        buf.append(" where ");
        buf.append(contactSelection);
        buf.append(")");
        return buf.toString();
    }
     private int deleteInternal(Uri url, String userWhere, String[] whereArgs) {
        String tableToChange;
        String tableToChange2 = null;
        String idColumnName = null;
        String changedItemId = null;
        String provider = null;
        String accountStr = null;
        long account = 0;
        String contact = null;
        long threadId = 0;
        StringBuilder whereClause = new StringBuilder();
        if(userWhere != null) {
            whereClause.append(userWhere);
        }
        boolean notifyMessagesContentUri = false;
        boolean notifyMessagesByContactContentUri = false;
        boolean notifyMessagesByThreadIdContentUri = false;
        boolean notifyContactListContentUri = false;
        boolean notifyProviderAccountContentUri = false;
        int match = mUrlMatcher.match(url);
        boolean contactDeleted = false;
        long deletedContactId = 0;
        boolean backfillQuickSwitchSlots = false;
        final SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        switch (match) {
            case MATCH_PROVIDERS:
                tableToChange = TABLE_PROVIDERS;
                notifyProviderAccountContentUri = true;
                break;
            case MATCH_ACCOUNTS_BY_ID:
                changedItemId = url.getPathSegments().get(1);
            case MATCH_ACCOUNTS:
                tableToChange = TABLE_ACCOUNTS;
                notifyProviderAccountContentUri = true;
                break;
            case MATCH_ACCOUNT_STATUS:
                changedItemId = url.getPathSegments().get(1);
            case MATCH_ACCOUNTS_STATUS:
                tableToChange = TABLE_ACCOUNT_STATUS;
                notifyProviderAccountContentUri = true;
                break;
            case MATCH_CONTACTS:
            case MATCH_CONTACTS_BAREBONE:
                tableToChange = TABLE_CONTACTS;
                contactDeleted = true;
                break;
            case MATCH_CONTACT:
                tableToChange = TABLE_CONTACTS;
                changedItemId = url.getPathSegments().get(1);
                try {
                    deletedContactId = Long.parseLong(changedItemId);
                } catch (NumberFormatException ex) {
                    throw new IllegalArgumentException();
                }
                contactDeleted = true;
                break;
            case MATCH_CONTACTS_BY_PROVIDER:
                tableToChange = TABLE_CONTACTS;
                appendWhere(whereClause, Imps.Contacts.ACCOUNT, "=", url.getPathSegments().get(2));
                contactDeleted = true;
                break;
            case MATCH_CONTACTLISTS_BY_PROVIDER:
                appendWhere(whereClause, Imps.ContactList.ACCOUNT, "=",
                        url.getPathSegments().get(2));
            case MATCH_CONTACTLISTS:
                tableToChange = TABLE_CONTACT_LIST;
                notifyContactListContentUri = true;
                break;
            case MATCH_CONTACTLIST:
                tableToChange = TABLE_CONTACT_LIST;
                changedItemId = url.getPathSegments().get(1);
                break;
            case MATCH_BLOCKEDLIST:
                tableToChange = TABLE_BLOCKED_LIST;
                break;
            case MATCH_BLOCKEDLIST_BY_PROVIDER:
                tableToChange = TABLE_BLOCKED_LIST;
                appendWhere(whereClause, Imps.BlockedList.ACCOUNT, "=", url.getPathSegments().get(2));
                break;
            case MATCH_CONTACTS_ETAGS:
                tableToChange = TABLE_CONTACTS_ETAG;
                break;
            case MATCH_CONTACTS_ETAG:
                tableToChange = TABLE_CONTACTS_ETAG;
                changedItemId = url.getPathSegments().get(1);
                break;
            case MATCH_MESSAGES:
                tableToChange = TABLE_MESSAGES;
                break;
            case MATCH_MESSAGES_BY_CONTACT:
                tableToChange = TABLE_MESSAGES;
                tableToChange2 = TABLE_IN_MEMORY_MESSAGES;
                accountStr = decodeURLSegment(url.getPathSegments().get(1));
                try {
                    account = Long.parseLong(accountStr);
                } catch (NumberFormatException ex) {
                    throw new IllegalArgumentException();
                }
                contact = decodeURLSegment(url.getPathSegments().get(2));
                appendWhere(whereClause, Imps.Messages.THREAD_ID, "=",
                        getContactId(db, accountStr, contact));
                notifyMessagesContentUri = true;
                break;
            case MATCH_MESSAGES_BY_THREAD_ID:
                tableToChange = TABLE_MESSAGES;
                tableToChange2 = TABLE_IN_MEMORY_MESSAGES;
                try {
                    threadId = Long.parseLong(decodeURLSegment(url.getPathSegments().get(1)));
                } catch (NumberFormatException ex) {
                    throw new IllegalArgumentException();
                }
                appendWhere(whereClause, Imps.Messages.THREAD_ID, "=", threadId);
                notifyMessagesContentUri = true;
                break;
            case MATCH_MESSAGES_BY_PROVIDER:
                tableToChange = TABLE_MESSAGES;
                provider = decodeURLSegment(url.getPathSegments().get(1));
                appendWhere(whereClause, buildContactIdSelection(Imps.Messages.THREAD_ID,
                        Imps.Contacts.PROVIDER + "='" + provider + "'"));
                notifyMessagesContentUri = true;
                break;
            case MATCH_MESSAGES_BY_ACCOUNT:
                tableToChange = TABLE_MESSAGES;
                accountStr = decodeURLSegment(url.getPathSegments().get(1));
                appendWhere(whereClause, buildContactIdSelection(Imps.Messages.THREAD_ID,
                        Imps.Contacts.ACCOUNT + "='" + accountStr + "'"));
                notifyMessagesContentUri = true;
                break;
            case MATCH_MESSAGE:
                tableToChange = TABLE_MESSAGES;
                changedItemId = url.getPathSegments().get(1);
                notifyMessagesContentUri = true;
                break;
            case MATCH_OTR_MESSAGES:
                tableToChange = TABLE_IN_MEMORY_MESSAGES;
                break;
            case MATCH_OTR_MESSAGES_BY_CONTACT:
                tableToChange = TABLE_IN_MEMORY_MESSAGES;
                accountStr = decodeURLSegment(url.getPathSegments().get(1));
                try {
                    account = Long.parseLong(accountStr);
                } catch (NumberFormatException ex) {
                    throw new IllegalArgumentException();
                }
                contact = decodeURLSegment(url.getPathSegments().get(2));
                appendWhere(whereClause, Imps.Messages.THREAD_ID, "=",
                        getContactId(db, accountStr, contact));
                notifyMessagesByContactContentUri = true;
                break;
            case MATCH_OTR_MESSAGES_BY_THREAD_ID:
                tableToChange = TABLE_IN_MEMORY_MESSAGES;
                try {
                    threadId = Long.parseLong(decodeURLSegment(url.getPathSegments().get(1)));
                } catch (NumberFormatException ex) {
                    throw new IllegalArgumentException();
                }
                appendWhere(whereClause, Imps.Messages.THREAD_ID, "=", threadId);
                notifyMessagesByThreadIdContentUri = true;
                break;
            case MATCH_OTR_MESSAGES_BY_PROVIDER:
                tableToChange = TABLE_IN_MEMORY_MESSAGES;
                provider = decodeURLSegment(url.getPathSegments().get(1));
                appendWhere(whereClause, buildContactIdSelection(Imps.Messages.THREAD_ID,
                        Imps.Contacts.PROVIDER + "='" + provider + "'"));
                if (DBG) log("delete (MATCH_OTR_MESSAGES_BY_PROVIDER) sel => " + whereClause);
                notifyMessagesContentUri = true;
                break;
            case MATCH_OTR_MESSAGES_BY_ACCOUNT:
                tableToChange = TABLE_IN_MEMORY_MESSAGES;
                accountStr = decodeURLSegment(url.getPathSegments().get(1));
                appendWhere(whereClause, buildContactIdSelection(Imps.Messages.THREAD_ID,
                        Imps.Contacts.ACCOUNT + "='" + accountStr + "'"));
                if (DBG) log("delete (MATCH_OTR_MESSAGES_BY_ACCOUNT) sel => " + whereClause);
                notifyMessagesContentUri = true;
                break;
            case MATCH_OTR_MESSAGE:
                tableToChange = TABLE_IN_MEMORY_MESSAGES;
                changedItemId = url.getPathSegments().get(1);
                notifyMessagesContentUri = true;
                break;
            case MATCH_GROUP_MEMBERS:
                tableToChange = TABLE_GROUP_MEMBERS;
                break;
            case MATCH_GROUP_MEMBERS_BY_GROUP:
                tableToChange = TABLE_GROUP_MEMBERS;
                appendWhere(whereClause, Imps.GroupMembers.GROUP, "=", url.getPathSegments().get(1));
                break;
            case MATCH_INVITATIONS:
                tableToChange = TABLE_INVITATIONS;
                break;
            case MATCH_INVITATION:
                tableToChange = TABLE_INVITATIONS;
                changedItemId = url.getPathSegments().get(1);
                break;
            case MATCH_AVATARS:
                tableToChange = TABLE_AVATARS;
                break;
            case MATCH_AVATAR:
                tableToChange = TABLE_AVATARS;
                changedItemId = url.getPathSegments().get(1);
                break;
            case MATCH_AVATAR_BY_PROVIDER:
                tableToChange = TABLE_AVATARS;
                changedItemId = url.getPathSegments().get(2);
                idColumnName = Imps.Avatars.ACCOUNT;
                break;
            case MATCH_CHATS:
                tableToChange = TABLE_CHATS;
                backfillQuickSwitchSlots = true;
                break;
            case MATCH_CHATS_BY_ACCOUNT:
                tableToChange = TABLE_CHATS;
                accountStr = decodeURLSegment(url.getLastPathSegment());
                appendWhere(whereClause, buildContactIdSelection(Imps.Chats.CONTACT_ID,
                        Imps.Contacts.ACCOUNT + "='" + accountStr + "'"));
                if (DBG) log("delete (MATCH_CHATS_BY_ACCOUNT) sel => " + whereClause);
                changedItemId = null;
                break;
            case MATCH_CHATS_ID:
                tableToChange = TABLE_CHATS;
                changedItemId = url.getPathSegments().get(1);
                idColumnName = Imps.Chats.CONTACT_ID;
                break;
            case MATCH_PRESENCE:
                tableToChange = TABLE_PRESENCE;
                break;
            case MATCH_PRESENCE_ID:
                tableToChange = TABLE_PRESENCE;
                changedItemId = url.getPathSegments().get(1);
                idColumnName = Imps.Presence.CONTACT_ID;
                break;
            case MATCH_PRESENCE_BY_ACCOUNT:
                tableToChange = TABLE_PRESENCE;
                accountStr = decodeURLSegment(url.getLastPathSegment());
                appendWhere(whereClause, buildContactIdSelection(Imps.Presence.CONTACT_ID,
                        Imps.Contacts.ACCOUNT + "='" + accountStr + "'"));
                if (DBG) log("delete (MATCH_PRESENCE_BY_ACCOUNT): sel => " + whereClause);
                changedItemId = null;
                break;
            case MATCH_SESSIONS:
                tableToChange = TABLE_SESSION_COOKIES;
                break;
            case MATCH_SESSIONS_BY_PROVIDER:
                tableToChange = TABLE_SESSION_COOKIES;
                changedItemId = url.getPathSegments().get(2);
                idColumnName = Imps.SessionCookies.ACCOUNT;
                break;
            case MATCH_PROVIDER_SETTINGS_BY_ID:
                tableToChange = TABLE_PROVIDER_SETTINGS;
                changedItemId = url.getPathSegments().get(1);
                idColumnName = Imps.ProviderSettings.PROVIDER;
                break;
            case MATCH_PROVIDER_SETTINGS_BY_ID_AND_NAME:
                tableToChange = TABLE_PROVIDER_SETTINGS;
                String providerId = url.getPathSegments().get(1);
                String name = url.getPathSegments().get(2);
                appendWhere(whereClause, Imps.ProviderSettings.PROVIDER, "=", providerId);
                appendWhere(whereClause, Imps.ProviderSettings.NAME, "=", name);
                break;
            case MATCH_BRANDING_RESOURCE_MAP_CACHE:
                tableToChange = TABLE_BRANDING_RESOURCE_MAP_CACHE;
                break;
            case MATCH_OUTGOING_RMQ_MESSAGES:
                tableToChange = TABLE_OUTGOING_RMQ_MESSAGES;
                break;
            case MATCH_LAST_RMQ_ID:
                tableToChange = TABLE_LAST_RMQ_ID;
                break;
            case MATCH_S2D_RMQ_IDS:
                tableToChange = TABLE_S2D_RMQ_IDS;
                break;
            default:
                throw new UnsupportedOperationException("Cannot delete that URL: " + url);
        }
        if (idColumnName == null) {
            idColumnName = "_id";
        }
        if (changedItemId != null) {
            appendWhere(whereClause, idColumnName, "=", changedItemId);
        }
        if (DBG) log("delete from " + url + " WHERE  " + whereClause);
        int count = db.delete(tableToChange, whereClause.toString(), whereArgs);
        if (tableToChange2 != null){
            count += db.delete(tableToChange2, whereClause.toString(), whereArgs);
        }
        if (contactDeleted && count > 0) {
            performContactRemovalCleanup(deletedContactId);
        }
        if (count > 0) {
            ContentResolver resolver = getContext().getContentResolver();
            if (match == MATCH_CHATS || match == MATCH_CHATS_ID
                    || match == MATCH_PRESENCE || match == MATCH_PRESENCE_ID
                    || match == MATCH_CONTACTS_BAREBONE) {
                resolver.notifyChange(Imps.Contacts.CONTENT_URI, null);
            }
            if (notifyMessagesContentUri) {
                resolver.notifyChange(Imps.Messages.CONTENT_URI, null);
            }
            if (notifyMessagesByContactContentUri) {
                resolver.notifyChange(Imps.Messages.CONTENT_URI, null);
                resolver.notifyChange(Imps.Messages.getContentUriByContact(account, contact), null);
            }
            if (notifyMessagesByThreadIdContentUri) {
                resolver.notifyChange(Imps.Messages.CONTENT_URI, null);
                resolver.notifyChange(Imps.Messages.getContentUriByThreadId(threadId), null);
            }
            if (notifyContactListContentUri) {
                resolver.notifyChange(Imps.ContactList.CONTENT_URI, null);
            }
            if (notifyProviderAccountContentUri) {
                if (DBG) log("notify delete for " + Imps.Provider.CONTENT_URI_WITH_ACCOUNT);
                resolver.notifyChange(Imps.Provider.CONTENT_URI_WITH_ACCOUNT, null);
            }
            if (backfillQuickSwitchSlots) {
                backfillQuickSwitchSlots();
            }
        }
        return count;
    }
    private int updateInternal(Uri url, ContentValues values, String userWhere,
            String[] whereArgs) {
        String tableToChange;
        String idColumnName = null;
        String changedItemId = null;
        String accountStr = null;
        long account = 0;
        String contact = null;
        long threadId = 0;
        int count;
        StringBuilder whereClause = new StringBuilder();
        if(userWhere != null) {
            whereClause.append(userWhere);
        }
        boolean notifyMessagesContentUri = false;
        boolean notifyMessagesByContactContentUri = false;
        boolean notifyMessagesByThreadIdContentUri = false;
        boolean notifyContactListContentUri = false;
        boolean notifyProviderAccountContentUri = false;
        int match = mUrlMatcher.match(url);
        final SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        switch (match) {
            case MATCH_PROVIDERS_BY_ID:
                changedItemId = url.getPathSegments().get(1);
            case MATCH_PROVIDERS:
                tableToChange = TABLE_PROVIDERS;
                break;
            case MATCH_ACCOUNTS_BY_ID:
                changedItemId = url.getPathSegments().get(1);
            case MATCH_ACCOUNTS:
                tableToChange = TABLE_ACCOUNTS;
                notifyProviderAccountContentUri = true;
                break;
            case MATCH_ACCOUNT_STATUS:
                changedItemId = url.getPathSegments().get(1);
            case MATCH_ACCOUNTS_STATUS:
                tableToChange = TABLE_ACCOUNT_STATUS;
                notifyProviderAccountContentUri = true;
                break;
            case MATCH_CONTACTS:
            case MATCH_CONTACTS_BAREBONE:
                tableToChange = TABLE_CONTACTS;
                break;
            case MATCH_CONTACTS_BY_PROVIDER:
                tableToChange = TABLE_CONTACTS;
                changedItemId = url.getPathSegments().get(2);
                idColumnName = Imps.Contacts.ACCOUNT;
                break;
            case MATCH_CONTACT:
                tableToChange = TABLE_CONTACTS;
                changedItemId = url.getPathSegments().get(1);
                break;
            case MATCH_CONTACTS_BULK:
                count = updateBulkContacts(values, userWhere);
                if (count > 0) {
                    getContext().getContentResolver().notifyChange(
                            Imps.Contacts.CONTENT_URI, null);
                }
                return count;
            case MATCH_CONTACTLIST:
                tableToChange = TABLE_CONTACT_LIST;
                changedItemId = url.getPathSegments().get(1);
                notifyContactListContentUri = true;
                break;
            case MATCH_CONTACTS_ETAGS:
                tableToChange = TABLE_CONTACTS_ETAG;
                break;
            case MATCH_CONTACTS_ETAG:
                tableToChange = TABLE_CONTACTS_ETAG;
                changedItemId = url.getPathSegments().get(1);
                break;
            case MATCH_MESSAGES:
                tableToChange = TABLE_MESSAGES;
                break;
            case MATCH_MESSAGES_BY_CONTACT:
                tableToChange = TABLE_MESSAGES;
                accountStr = decodeURLSegment(url.getPathSegments().get(1));
                try {
                    account = Long.parseLong(accountStr);
                } catch (NumberFormatException ex) {
                    throw new IllegalArgumentException();
                }
                contact = decodeURLSegment(url.getPathSegments().get(2));
                appendWhere(whereClause, Imps.Messages.THREAD_ID, "=",
                        getContactId(db, accountStr, contact));
                notifyMessagesContentUri = true;
                break;
            case MATCH_MESSAGES_BY_THREAD_ID:
                tableToChange = TABLE_MESSAGES;
                try {
                    threadId = Long.parseLong(decodeURLSegment(url.getPathSegments().get(1)));
                } catch (NumberFormatException ex) {
                    throw new IllegalArgumentException();
                }
                appendWhere(whereClause, Imps.Messages.THREAD_ID, "=", threadId);
                notifyMessagesContentUri = true;
                break;
            case MATCH_MESSAGE:
                tableToChange = TABLE_MESSAGES;
                changedItemId = url.getPathSegments().get(1);
                notifyMessagesContentUri = true;
                break;
            case MATCH_OTR_MESSAGES:
                tableToChange = TABLE_IN_MEMORY_MESSAGES;
                break;
            case MATCH_OTR_MESSAGES_BY_CONTACT:
                tableToChange = TABLE_IN_MEMORY_MESSAGES;
                accountStr = decodeURLSegment(url.getPathSegments().get(1));
                try {
                    account = Long.parseLong(accountStr);
                } catch (NumberFormatException ex) {
                    throw new IllegalArgumentException();
                }
                contact = decodeURLSegment(url.getPathSegments().get(2));
                appendWhere(whereClause, Imps.Messages.THREAD_ID, "=",
                        getContactId(db, accountStr, contact));
                notifyMessagesByContactContentUri = true;
                break;
            case MATCH_OTR_MESSAGES_BY_THREAD_ID:
                tableToChange = TABLE_IN_MEMORY_MESSAGES;
                try {
                    threadId = Long.parseLong(decodeURLSegment(url.getPathSegments().get(1)));
                } catch (NumberFormatException ex) {
                    throw new IllegalArgumentException();
                }
                appendWhere(whereClause, Imps.Messages.THREAD_ID, "=", threadId);
                notifyMessagesByThreadIdContentUri = true;
                break;
            case MATCH_OTR_MESSAGE:
                tableToChange = TABLE_IN_MEMORY_MESSAGES;
                changedItemId = url.getPathSegments().get(1);
                notifyMessagesContentUri = true;
                break;
            case MATCH_AVATARS:
                tableToChange = TABLE_AVATARS;
                break;
            case MATCH_AVATAR:
                tableToChange = TABLE_AVATARS;
                changedItemId = url.getPathSegments().get(1);
                break;
            case MATCH_AVATAR_BY_PROVIDER:
                tableToChange = TABLE_AVATARS;
                changedItemId = url.getPathSegments().get(2);
                idColumnName = Imps.Avatars.ACCOUNT;
                break;
            case MATCH_CHATS:
                tableToChange = TABLE_CHATS;
                break;
            case MATCH_CHATS_ID:
                tableToChange = TABLE_CHATS;
                changedItemId = url.getPathSegments().get(1);
                idColumnName = Imps.Chats.CONTACT_ID;
                break;
            case MATCH_PRESENCE:
                tableToChange = TABLE_PRESENCE;
                break;
            case MATCH_PRESENCE_ID:
                tableToChange = TABLE_PRESENCE;
                changedItemId = url.getPathSegments().get(1);
                idColumnName = Imps.Presence.CONTACT_ID;
                break;
            case MATCH_PRESENCE_BULK:
                count = updateBulkPresence(values, userWhere, whereArgs);
                if (count > 0) {
                     getContext().getContentResolver().notifyChange(Imps.Contacts.CONTENT_URI, null);
                }
                return count;
            case MATCH_INVITATION:
                tableToChange = TABLE_INVITATIONS;
                changedItemId = url.getPathSegments().get(1);
                break;
            case MATCH_SESSIONS:
                tableToChange = TABLE_SESSION_COOKIES;
                break;
            case MATCH_PROVIDER_SETTINGS_BY_ID_AND_NAME:
                tableToChange = TABLE_PROVIDER_SETTINGS;
                String providerId = url.getPathSegments().get(1);
                String name = url.getPathSegments().get(2);
                if (values.containsKey(Imps.ProviderSettings.PROVIDER) ||
                        values.containsKey(Imps.ProviderSettings.NAME)) {
                    throw new SecurityException("Cannot override the value for provider|name");
                }
                appendWhere(whereClause, Imps.ProviderSettings.PROVIDER, "=", providerId);
                appendWhere(whereClause, Imps.ProviderSettings.NAME, "=", name);
                break;
            case MATCH_OUTGOING_RMQ_MESSAGES:
                tableToChange = TABLE_OUTGOING_RMQ_MESSAGES;
                break;
            case MATCH_LAST_RMQ_ID:
                tableToChange = TABLE_LAST_RMQ_ID;
                break;
            case MATCH_S2D_RMQ_IDS:
                tableToChange = TABLE_S2D_RMQ_IDS;
                break;
            default:
                throw new UnsupportedOperationException("Cannot update URL: " + url);
        }
        if (idColumnName == null) {
            idColumnName = "_id";
        }
        if(changedItemId != null) {
            appendWhere(whereClause, idColumnName, "=", changedItemId);
        }
        if (DBG) log("update " + url + " WHERE " + whereClause);
        count = db.update(tableToChange, values, whereClause.toString(), whereArgs);
        if (count > 0) {
            ContentResolver resolver = getContext().getContentResolver();
            if (match == MATCH_CHATS || match == MATCH_CHATS_ID
                    || match == MATCH_PRESENCE || match == MATCH_PRESENCE_ID
                    || match == MATCH_CONTACTS_BAREBONE) {
                resolver.notifyChange(Imps.Contacts.CONTENT_URI, null);
            }
            if (notifyMessagesContentUri) {
                if (DBG) log("notify change for " + Imps.Messages.CONTENT_URI);
                resolver.notifyChange(Imps.Messages.CONTENT_URI, null);
            }
            if (notifyMessagesByContactContentUri) {
                resolver.notifyChange(Imps.Messages.CONTENT_URI, null);
                resolver.notifyChange(Imps.Messages.getContentUriByContact(account, contact), null);
            }
            if (notifyMessagesByThreadIdContentUri) {
                resolver.notifyChange(Imps.Messages.CONTENT_URI, null);
                resolver.notifyChange(Imps.Messages.getContentUriByThreadId(threadId), null);
            }
            if (notifyContactListContentUri) {
                resolver.notifyChange(Imps.ContactList.CONTENT_URI, null);
            }
            if (notifyProviderAccountContentUri) {
                if (DBG) log("notify change for " + Imps.Provider.CONTENT_URI_WITH_ACCOUNT);
                resolver.notifyChange(Imps.Provider.CONTENT_URI_WITH_ACCOUNT, null);
            }
        }
        return count;
    }
    @Override
    public ParcelFileDescriptor openFile(Uri uri, String mode)
            throws FileNotFoundException {
        return openFileHelper(uri, mode);
    }
    private static void appendWhere(StringBuilder where, String columnName,
            String condition, Object value) {
        if (where.length() > 0) {
            where.append(" AND ");
        }
        where.append(columnName).append(condition);
        if(value != null) {
            DatabaseUtils.appendValueToSql(where, value);
        }
    }
    private static void appendWhere(StringBuilder where, String clause) {
        if (where.length() > 0) {
            where.append(" AND ");
        }
        where.append(clause);
    }
    private static String decodeURLSegment(String segment) {
        try {
            return URLDecoder.decode(segment, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            return segment;
        }
    }
    static void log(String message) {
        Log.d(LOG_TAG, message);
    }
}
