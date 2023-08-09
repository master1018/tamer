public class EmailProvider extends ContentProvider {
    private static final String TAG = "EmailProvider";
    protected static final String DATABASE_NAME = "EmailProvider.db";
    protected static final String BODY_DATABASE_NAME = "EmailProviderBody.db";
    public static final Uri INTEGRITY_CHECK_URI =
        Uri.parse("content:
    private static final String[] ORPHANS_PROJECTION
        = new String[] {MessageColumns.ID, MessageColumns.MAILBOX_KEY};
    private static final int ORPHANS_ID = 0;
    private static final int ORPHANS_MAILBOX_KEY = 1;
    private static final String WHERE_ID = EmailContent.RECORD_ID + "=?";
    public static final int DATABASE_VERSION = 12;
    public static final int BODY_DATABASE_VERSION = 6;
    public static final String EMAIL_AUTHORITY = "com.android.email.provider";
    private static final int ACCOUNT_BASE = 0;
    private static final int ACCOUNT = ACCOUNT_BASE;
    private static final int ACCOUNT_MAILBOXES = ACCOUNT_BASE + 1;
    private static final int ACCOUNT_ID = ACCOUNT_BASE + 2;
    private static final int ACCOUNT_ID_ADD_TO_FIELD = ACCOUNT_BASE + 3;
    private static final int MAILBOX_BASE = 0x1000;
    private static final int MAILBOX = MAILBOX_BASE;
    private static final int MAILBOX_MESSAGES = MAILBOX_BASE + 1;
    private static final int MAILBOX_ID = MAILBOX_BASE + 2;
    private static final int MAILBOX_ID_ADD_TO_FIELD = MAILBOX_BASE + 3;
    private static final int MESSAGE_BASE = 0x2000;
    private static final int MESSAGE = MESSAGE_BASE;
    private static final int MESSAGE_ID = MESSAGE_BASE + 1;
    private static final int SYNCED_MESSAGE_ID = MESSAGE_BASE + 2;
    private static final int ATTACHMENT_BASE = 0x3000;
    private static final int ATTACHMENT = ATTACHMENT_BASE;
    private static final int ATTACHMENT_CONTENT = ATTACHMENT_BASE + 1;
    private static final int ATTACHMENT_ID = ATTACHMENT_BASE + 2;
    private static final int ATTACHMENTS_MESSAGE_ID = ATTACHMENT_BASE + 3;
    private static final int HOSTAUTH_BASE = 0x4000;
    private static final int HOSTAUTH = HOSTAUTH_BASE;
    private static final int HOSTAUTH_ID = HOSTAUTH_BASE + 1;
    private static final int UPDATED_MESSAGE_BASE = 0x5000;
    private static final int UPDATED_MESSAGE = UPDATED_MESSAGE_BASE;
    private static final int UPDATED_MESSAGE_ID = UPDATED_MESSAGE_BASE + 1;
    private static final int DELETED_MESSAGE_BASE = 0x6000;
    private static final int DELETED_MESSAGE = DELETED_MESSAGE_BASE;
    private static final int DELETED_MESSAGE_ID = DELETED_MESSAGE_BASE + 1;
    private static final int DELETED_MESSAGE_MAILBOX = DELETED_MESSAGE_BASE + 2;
    private static final int LAST_EMAIL_PROVIDER_DB_BASE = DELETED_MESSAGE_BASE;
    private static final int BODY_BASE = LAST_EMAIL_PROVIDER_DB_BASE + 0x1000;
    private static final int BODY = BODY_BASE;
    private static final int BODY_ID = BODY_BASE + 1;
    private static final int BODY_MESSAGE_ID = BODY_BASE + 2;
    private static final int BODY_HTML = BODY_BASE + 3;
    private static final int BODY_TEXT = BODY_BASE + 4;
    private static final int BASE_SHIFT = 12;  
    private static final String[] TABLE_NAMES = {
        EmailContent.Account.TABLE_NAME,
        EmailContent.Mailbox.TABLE_NAME,
        EmailContent.Message.TABLE_NAME,
        EmailContent.Attachment.TABLE_NAME,
        EmailContent.HostAuth.TABLE_NAME,
        EmailContent.Message.UPDATED_TABLE_NAME,
        EmailContent.Message.DELETED_TABLE_NAME,
        EmailContent.Body.TABLE_NAME
    };
    private static final UriMatcher sURIMatcher = new UriMatcher(UriMatcher.NO_MATCH);
    private static final String UPDATED_MESSAGE_INSERT = "insert or ignore into " +
        Message.UPDATED_TABLE_NAME + " select * from " + Message.TABLE_NAME + " where " +
        EmailContent.RECORD_ID + '=';
    private static final String UPDATED_MESSAGE_DELETE = "delete from " +
        Message.UPDATED_TABLE_NAME + " where " + EmailContent.RECORD_ID + '=';
    private static final String DELETED_MESSAGE_INSERT = "insert or replace into " +
        Message.DELETED_TABLE_NAME + " select * from " + Message.TABLE_NAME + " where " +
        EmailContent.RECORD_ID + '=';
    private static final String DELETE_ORPHAN_BODIES = "delete from " + Body.TABLE_NAME +
        " where " + BodyColumns.MESSAGE_KEY + " in " + "(select " + BodyColumns.MESSAGE_KEY +
        " from " + Body.TABLE_NAME + " except select " + EmailContent.RECORD_ID + " from " +
        Message.TABLE_NAME + ')';
    private static final String DELETE_BODY = "delete from " + Body.TABLE_NAME +
        " where " + BodyColumns.MESSAGE_KEY + '=';
    private static final String ID_EQUALS = EmailContent.RECORD_ID + "=?";
    private static final String TRIGGER_MAILBOX_DELETE =
        "create trigger mailbox_delete before delete on " + Mailbox.TABLE_NAME +
        " begin" +
        " delete from " + Message.TABLE_NAME +
        "  where " + MessageColumns.MAILBOX_KEY + "=old." + EmailContent.RECORD_ID +
        "; delete from " + Message.UPDATED_TABLE_NAME +
        "  where " + MessageColumns.MAILBOX_KEY + "=old." + EmailContent.RECORD_ID +
        "; delete from " + Message.DELETED_TABLE_NAME +
        "  where " + MessageColumns.MAILBOX_KEY + "=old." + EmailContent.RECORD_ID +
        "; end";
    static {
        UriMatcher matcher = sURIMatcher;
        matcher.addURI(EMAIL_AUTHORITY, "account", ACCOUNT);
        matcher.addURI(EMAIL_AUTHORITY, "account/#", ACCOUNT_ID);
        matcher.addURI(EMAIL_AUTHORITY, "account/#/mailbox", ACCOUNT_MAILBOXES);
        matcher.addURI(EMAIL_AUTHORITY, "mailbox", MAILBOX);
        matcher.addURI(EMAIL_AUTHORITY, "mailbox/#", MAILBOX_ID);
        matcher.addURI(EMAIL_AUTHORITY, "mailbox/#/message", MAILBOX_MESSAGES);
        matcher.addURI(EMAIL_AUTHORITY, "message", MESSAGE);
        matcher.addURI(EMAIL_AUTHORITY, "message/#", MESSAGE_ID);
        matcher.addURI(EMAIL_AUTHORITY, "attachment", ATTACHMENT);
        matcher.addURI(EMAIL_AUTHORITY, "attachment/#", ATTACHMENT_ID);
        matcher.addURI(EMAIL_AUTHORITY, "attachment/content
        matcher.addURI(EMAIL_AUTHORITY, "syncedMessage/#", SYNCED_MESSAGE_ID);
        matcher.addURI(EMAIL_AUTHORITY, "deletedMessage", DELETED_MESSAGE);
        matcher.addURI(EMAIL_AUTHORITY, "deletedMessage/#", DELETED_MESSAGE_ID);
        matcher.addURI(EMAIL_AUTHORITY, "deletedMessage/mailbox/#", DELETED_MESSAGE_MAILBOX);
        matcher.addURI(EMAIL_AUTHORITY, "updatedMessage", UPDATED_MESSAGE);
        matcher.addURI(EMAIL_AUTHORITY, "updatedMessage/#", UPDATED_MESSAGE_ID);
    }
    static String createIndex(String tableName, String columnName) {
        return "create index " + tableName.toLowerCase() + '_' + columnName
            + " on " + tableName + " (" + columnName + ");";
    }
    static void createMessageTable(SQLiteDatabase db) {
        String messageColumns = MessageColumns.DISPLAY_NAME + " text, "
            + MessageColumns.TIMESTAMP + " integer, "
            + MessageColumns.SUBJECT + " text, "
            + MessageColumns.FLAG_READ + " integer, "
            + MessageColumns.FLAG_LOADED + " integer, "
            + MessageColumns.FLAG_FAVORITE + " integer, "
            + MessageColumns.FLAG_ATTACHMENT + " integer, "
            + MessageColumns.FLAGS + " integer, "
            + MessageColumns.CLIENT_ID + " integer, "
            + MessageColumns.MESSAGE_ID + " text, "
            + MessageColumns.MAILBOX_KEY + " integer, "
            + MessageColumns.ACCOUNT_KEY + " integer, "
            + MessageColumns.FROM_LIST + " text, "
            + MessageColumns.TO_LIST + " text, "
            + MessageColumns.CC_LIST + " text, "
            + MessageColumns.BCC_LIST + " text, "
            + MessageColumns.REPLY_TO_LIST + " text, "
            + MessageColumns.MEETING_INFO + " text"
            + ");";
        String createString = " (" + EmailContent.RECORD_ID + " integer primary key autoincrement, "
            + SyncColumns.SERVER_ID + " text, "
            + SyncColumns.SERVER_TIMESTAMP + " integer, "
            + messageColumns;
        String altCreateString = " (" + EmailContent.RECORD_ID + " integer unique, "
            + SyncColumns.SERVER_ID + " text, "
            + SyncColumns.SERVER_TIMESTAMP + " integer, "
            + messageColumns;
        db.execSQL("create table " + Message.TABLE_NAME + createString);
        db.execSQL("create table " + Message.UPDATED_TABLE_NAME + altCreateString);
        db.execSQL("create table " + Message.DELETED_TABLE_NAME + altCreateString);
        String indexColumns[] = {
            MessageColumns.TIMESTAMP,
            MessageColumns.FLAG_READ,
            MessageColumns.FLAG_LOADED,
            MessageColumns.MAILBOX_KEY,
            SyncColumns.SERVER_ID
        };
        for (String columnName : indexColumns) {
            db.execSQL(createIndex(Message.TABLE_NAME, columnName));
        }
        db.execSQL("create trigger message_delete before delete on " + Message.TABLE_NAME +
                " begin delete from " + Attachment.TABLE_NAME +
                "  where " + AttachmentColumns.MESSAGE_KEY + "=old." + EmailContent.RECORD_ID +
                "; end");
        db.execSQL("create trigger unread_message_insert before insert on " + Message.TABLE_NAME +
                " when NEW." + MessageColumns.FLAG_READ + "=0" +
                " begin update " + Mailbox.TABLE_NAME + " set " + MailboxColumns.UNREAD_COUNT +
                '=' + MailboxColumns.UNREAD_COUNT + "+1" +
                "  where " + EmailContent.RECORD_ID + "=NEW." + MessageColumns.MAILBOX_KEY +
                "; end");
        db.execSQL("create trigger unread_message_delete before delete on " + Message.TABLE_NAME +
                " when OLD." + MessageColumns.FLAG_READ + "=0" +
                " begin update " + Mailbox.TABLE_NAME + " set " + MailboxColumns.UNREAD_COUNT +
                '=' + MailboxColumns.UNREAD_COUNT + "-1" +
                "  where " + EmailContent.RECORD_ID + "=OLD." + MessageColumns.MAILBOX_KEY +
                "; end");
        db.execSQL("create trigger unread_message_move before update of " +
                MessageColumns.MAILBOX_KEY + " on " + Message.TABLE_NAME +
                " when OLD." + MessageColumns.FLAG_READ + "=0" +
                " begin update " + Mailbox.TABLE_NAME + " set " + MailboxColumns.UNREAD_COUNT +
                '=' + MailboxColumns.UNREAD_COUNT + "-1" +
                "  where " + EmailContent.RECORD_ID + "=OLD." + MessageColumns.MAILBOX_KEY +
                "; update " + Mailbox.TABLE_NAME + " set " + MailboxColumns.UNREAD_COUNT +
                '=' + MailboxColumns.UNREAD_COUNT + "+1" +
                " where " + EmailContent.RECORD_ID + "=NEW." + MessageColumns.MAILBOX_KEY +
                "; end");
        db.execSQL("create trigger unread_message_read before update of " +
                MessageColumns.FLAG_READ + " on " + Message.TABLE_NAME +
                " when OLD." + MessageColumns.FLAG_READ + "!=NEW." + MessageColumns.FLAG_READ +
                " begin update " + Mailbox.TABLE_NAME + " set " + MailboxColumns.UNREAD_COUNT +
                '=' + MailboxColumns.UNREAD_COUNT + "+ case OLD." + MessageColumns.FLAG_READ +
                " when 0 then -1 else 1 end" +
                "  where " + EmailContent.RECORD_ID + "=OLD." + MessageColumns.MAILBOX_KEY +
                "; end");
   }
    static void resetMessageTable(SQLiteDatabase db, int oldVersion, int newVersion) {
        try {
            db.execSQL("drop table " + Message.TABLE_NAME);
            db.execSQL("drop table " + Message.UPDATED_TABLE_NAME);
            db.execSQL("drop table " + Message.DELETED_TABLE_NAME);
        } catch (SQLException e) {
        }
        createMessageTable(db);
    }
    static void createAccountTable(SQLiteDatabase db) {
        String s = " (" + EmailContent.RECORD_ID + " integer primary key autoincrement, "
            + AccountColumns.DISPLAY_NAME + " text, "
            + AccountColumns.EMAIL_ADDRESS + " text, "
            + AccountColumns.SYNC_KEY + " text, "
            + AccountColumns.SYNC_LOOKBACK + " integer, "
            + AccountColumns.SYNC_INTERVAL + " text, "
            + AccountColumns.HOST_AUTH_KEY_RECV + " integer, "
            + AccountColumns.HOST_AUTH_KEY_SEND + " integer, "
            + AccountColumns.FLAGS + " integer, "
            + AccountColumns.IS_DEFAULT + " integer, "
            + AccountColumns.COMPATIBILITY_UUID + " text, "
            + AccountColumns.SENDER_NAME + " text, "
            + AccountColumns.RINGTONE_URI + " text, "
            + AccountColumns.PROTOCOL_VERSION + " text, "
            + AccountColumns.NEW_MESSAGE_COUNT + " integer, "
            + AccountColumns.SECURITY_FLAGS + " integer, "
            + AccountColumns.SECURITY_SYNC_KEY + " text, "
            + AccountColumns.SIGNATURE + " text "
            + ");";
        db.execSQL("create table " + Account.TABLE_NAME + s);
        db.execSQL("create trigger account_delete before delete on " + Account.TABLE_NAME +
                " begin delete from " + Mailbox.TABLE_NAME +
                " where " + MailboxColumns.ACCOUNT_KEY + "=old." + EmailContent.RECORD_ID +
                "; delete from " + HostAuth.TABLE_NAME +
                " where " + EmailContent.RECORD_ID + "=old." + AccountColumns.HOST_AUTH_KEY_RECV +
                "; delete from " + HostAuth.TABLE_NAME +
                " where " + EmailContent.RECORD_ID + "=old." + AccountColumns.HOST_AUTH_KEY_SEND +
        "; end");
    }
    static void resetAccountTable(SQLiteDatabase db, int oldVersion, int newVersion) {
        try {
            db.execSQL("drop table " +  Account.TABLE_NAME);
        } catch (SQLException e) {
        }
        createAccountTable(db);
    }
    static void createHostAuthTable(SQLiteDatabase db) {
        String s = " (" + EmailContent.RECORD_ID + " integer primary key autoincrement, "
            + HostAuthColumns.PROTOCOL + " text, "
            + HostAuthColumns.ADDRESS + " text, "
            + HostAuthColumns.PORT + " integer, "
            + HostAuthColumns.FLAGS + " integer, "
            + HostAuthColumns.LOGIN + " text, "
            + HostAuthColumns.PASSWORD + " text, "
            + HostAuthColumns.DOMAIN + " text, "
            + HostAuthColumns.ACCOUNT_KEY + " integer"
            + ");";
        db.execSQL("create table " + HostAuth.TABLE_NAME + s);
    }
    static void resetHostAuthTable(SQLiteDatabase db, int oldVersion, int newVersion) {
        try {
            db.execSQL("drop table " + HostAuth.TABLE_NAME);
        } catch (SQLException e) {
        }
        createHostAuthTable(db);
    }
    static void createMailboxTable(SQLiteDatabase db) {
        String s = " (" + EmailContent.RECORD_ID + " integer primary key autoincrement, "
            + MailboxColumns.DISPLAY_NAME + " text, "
            + MailboxColumns.SERVER_ID + " text, "
            + MailboxColumns.PARENT_SERVER_ID + " text, "
            + MailboxColumns.ACCOUNT_KEY + " integer, "
            + MailboxColumns.TYPE + " integer, "
            + MailboxColumns.DELIMITER + " integer, "
            + MailboxColumns.SYNC_KEY + " text, "
            + MailboxColumns.SYNC_LOOKBACK + " integer, "
            + MailboxColumns.SYNC_INTERVAL + " integer, "
            + MailboxColumns.SYNC_TIME + " integer, "
            + MailboxColumns.UNREAD_COUNT + " integer, "
            + MailboxColumns.FLAG_VISIBLE + " integer, "
            + MailboxColumns.FLAGS + " integer, "
            + MailboxColumns.VISIBLE_LIMIT + " integer, "
            + MailboxColumns.SYNC_STATUS + " text"
            + ");";
        db.execSQL("create table " + Mailbox.TABLE_NAME + s);
        db.execSQL("create index mailbox_" + MailboxColumns.SERVER_ID
                + " on " + Mailbox.TABLE_NAME + " (" + MailboxColumns.SERVER_ID + ")");
        db.execSQL("create index mailbox_" + MailboxColumns.ACCOUNT_KEY
                + " on " + Mailbox.TABLE_NAME + " (" + MailboxColumns.ACCOUNT_KEY + ")");
        db.execSQL(TRIGGER_MAILBOX_DELETE);
    }
    static void resetMailboxTable(SQLiteDatabase db, int oldVersion, int newVersion) {
        try {
            db.execSQL("drop table " + Mailbox.TABLE_NAME);
        } catch (SQLException e) {
        }
        createMailboxTable(db);
    }
    static void createAttachmentTable(SQLiteDatabase db) {
        String s = " (" + EmailContent.RECORD_ID + " integer primary key autoincrement, "
            + AttachmentColumns.FILENAME + " text, "
            + AttachmentColumns.MIME_TYPE + " text, "
            + AttachmentColumns.SIZE + " integer, "
            + AttachmentColumns.CONTENT_ID + " text, "
            + AttachmentColumns.CONTENT_URI + " text, "
            + AttachmentColumns.MESSAGE_KEY + " integer, "
            + AttachmentColumns.LOCATION + " text, "
            + AttachmentColumns.ENCODING + " text, "
            + AttachmentColumns.CONTENT + " text, "
            + AttachmentColumns.FLAGS + " integer, "
            + AttachmentColumns.CONTENT_BYTES + " blob"
            + ");";
        db.execSQL("create table " + Attachment.TABLE_NAME + s);
        db.execSQL(createIndex(Attachment.TABLE_NAME, AttachmentColumns.MESSAGE_KEY));
    }
    static void resetAttachmentTable(SQLiteDatabase db, int oldVersion, int newVersion) {
        try {
            db.execSQL("drop table " + Attachment.TABLE_NAME);
        } catch (SQLException e) {
        }
        createAttachmentTable(db);
    }
    static void createBodyTable(SQLiteDatabase db) {
        String s = " (" + EmailContent.RECORD_ID + " integer primary key autoincrement, "
            + BodyColumns.MESSAGE_KEY + " integer, "
            + BodyColumns.HTML_CONTENT + " text, "
            + BodyColumns.TEXT_CONTENT + " text, "
            + BodyColumns.HTML_REPLY + " text, "
            + BodyColumns.TEXT_REPLY + " text, "
            + BodyColumns.SOURCE_MESSAGE_KEY + " text, "
            + BodyColumns.INTRO_TEXT + " text"
            + ");";
        db.execSQL("create table " + Body.TABLE_NAME + s);
        db.execSQL(createIndex(Body.TABLE_NAME, BodyColumns.MESSAGE_KEY));
    }
    static void upgradeBodyTable(SQLiteDatabase db, int oldVersion, int newVersion) {
        if (oldVersion < 5) {
            try {
                db.execSQL("drop table " + Body.TABLE_NAME);
                createBodyTable(db);
            } catch (SQLException e) {
            }
        } else if (oldVersion == 5) {
            try {
                db.execSQL("alter table " + Body.TABLE_NAME
                        + " add " + BodyColumns.INTRO_TEXT + " text");
            } catch (SQLException e) {
                Log.w(TAG, "Exception upgrading EmailProviderBody.db from v5 to v6", e);
            }
            oldVersion = 6;
        }
    }
    private SQLiteDatabase mDatabase;
    private SQLiteDatabase mBodyDatabase;
    public synchronized SQLiteDatabase getDatabase(Context context) {
        if (mDatabase != null) {
            return mDatabase;
        }
        checkDatabases();
        DatabaseHelper helper = new DatabaseHelper(context, DATABASE_NAME);
        mDatabase = helper.getWritableDatabase();
        if (mDatabase != null) {
            mDatabase.setLockingEnabled(true);
            BodyDatabaseHelper bodyHelper = new BodyDatabaseHelper(context, BODY_DATABASE_NAME);
            mBodyDatabase = bodyHelper.getWritableDatabase();
            if (mBodyDatabase != null) {
                mBodyDatabase.setLockingEnabled(true);
                String bodyFileName = mBodyDatabase.getPath();
                mDatabase.execSQL("attach \"" + bodyFileName + "\" as BodyDatabase");
            }
        }
        deleteOrphans(mDatabase, Message.UPDATED_TABLE_NAME);
        deleteOrphans(mDatabase, Message.DELETED_TABLE_NAME);
        return mDatabase;
    }
     static SQLiteDatabase getReadableDatabase(Context context) {
        DatabaseHelper helper = new EmailProvider().new DatabaseHelper(context, DATABASE_NAME);
        return helper.getReadableDatabase();
    }
     static void deleteOrphans(SQLiteDatabase database, String tableName) {
        if (database != null) {
            Cursor c = database.query(tableName, ORPHANS_PROJECTION, null, null, null, null, null);
            try {
                if (c.getCount() == 0) return;
                ArrayList<Long> foundMailboxes = new ArrayList<Long>();
                ArrayList<Long> notFoundMailboxes = new ArrayList<Long>();
                ArrayList<Long> deleteList = new ArrayList<Long>();
                String[] bindArray = new String[1];
                while (c.moveToNext()) {
                    long mailboxId = c.getLong(ORPHANS_MAILBOX_KEY);
                    if (notFoundMailboxes.contains(mailboxId)) {
                        deleteList.add(c.getLong(ORPHANS_ID));
                    } else if (!foundMailboxes.contains(mailboxId)) {
                        bindArray[0] = Long.toString(mailboxId);
                        Cursor boxCursor = database.query(Mailbox.TABLE_NAME,
                                Mailbox.ID_PROJECTION, WHERE_ID, bindArray, null, null, null);
                        try {
                            if (boxCursor.moveToFirst()) {
                                foundMailboxes.add(mailboxId);
                            } else {
                                notFoundMailboxes.add(mailboxId);
                                deleteList.add(c.getLong(ORPHANS_ID));
                            }
                        } finally {
                            boxCursor.close();
                        }
                    }
                }
                for (long messageId: deleteList) {
                    bindArray[0] = Long.toString(messageId);
                    database.delete(tableName, WHERE_ID, bindArray);
                }
            } finally {
                c.close();
            }
        }
    }
    private class BodyDatabaseHelper extends SQLiteOpenHelper {
        BodyDatabaseHelper(Context context, String name) {
            super(context, name, null, BODY_DATABASE_VERSION);
        }
        @Override
        public void onCreate(SQLiteDatabase db) {
            Log.d(TAG, "Creating EmailProviderBody database");
            createBodyTable(db);
        }
        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
            upgradeBodyTable(db, oldVersion, newVersion);
        }
        @Override
        public void onOpen(SQLiteDatabase db) {
        }
    }
    private class DatabaseHelper extends SQLiteOpenHelper {
        Context mContext;
        DatabaseHelper(Context context, String name) {
            super(context, name, null, DATABASE_VERSION);
            mContext = context;
        }
        @Override
        public void onCreate(SQLiteDatabase db) {
            Log.d(TAG, "Creating EmailProvider database");
            createMessageTable(db);
            createAttachmentTable(db);
            createMailboxTable(db);
            createHostAuthTable(db);
            createAccountTable(db);
        }
        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
            if (oldVersion < 5) {
                android.accounts.Account[] accounts = AccountManager.get(mContext)
                        .getAccountsByType(Email.EXCHANGE_ACCOUNT_MANAGER_TYPE);
                for (android.accounts.Account account: accounts) {
                    AccountManager.get(mContext).removeAccount(account, null, null);
                }
                resetMessageTable(db, oldVersion, newVersion);
                resetAttachmentTable(db, oldVersion, newVersion);
                resetMailboxTable(db, oldVersion, newVersion);
                resetHostAuthTable(db, oldVersion, newVersion);
                resetAccountTable(db, oldVersion, newVersion);
                return;
            }
            if (oldVersion == 5) {
                try {
                    db.execSQL("alter table " + Message.TABLE_NAME
                            + " add column " + SyncColumns.SERVER_TIMESTAMP + " integer" + ";");
                    db.execSQL("alter table " + Message.UPDATED_TABLE_NAME
                            + " add column " + SyncColumns.SERVER_TIMESTAMP + " integer" + ";");
                    db.execSQL("alter table " + Message.DELETED_TABLE_NAME
                            + " add column " + SyncColumns.SERVER_TIMESTAMP + " integer" + ";");
                } catch (SQLException e) {
                    Log.w(TAG, "Exception upgrading EmailProvider.db from v5 to v6", e);
                }
                oldVersion = 6;
            }
            if (oldVersion == 6) {
                db.execSQL("drop trigger mailbox_delete;");
                db.execSQL(TRIGGER_MAILBOX_DELETE);
                oldVersion = 7;
            }
            if (oldVersion == 7) {
                try {
                    db.execSQL("alter table " + Account.TABLE_NAME
                            + " add column " + AccountColumns.SECURITY_FLAGS + " integer" + ";");
                } catch (SQLException e) {
                    Log.w(TAG, "Exception upgrading EmailProvider.db from 7 to 8 " + e);
                }
                oldVersion = 8;
            }
            if (oldVersion == 8) {
                try {
                    db.execSQL("alter table " + Account.TABLE_NAME
                            + " add column " + AccountColumns.SECURITY_SYNC_KEY + " text" + ";");
                    db.execSQL("alter table " + Account.TABLE_NAME
                            + " add column " + AccountColumns.SIGNATURE + " text" + ";");
                } catch (SQLException e) {
                    Log.w(TAG, "Exception upgrading EmailProvider.db from 8 to 9 " + e);
                }
                oldVersion = 9;
            }
            if (oldVersion == 9) {
                try {
                    db.execSQL("alter table " + Message.TABLE_NAME
                            + " add column " + MessageColumns.MEETING_INFO + " text" + ";");
                    db.execSQL("alter table " + Message.UPDATED_TABLE_NAME
                            + " add column " + MessageColumns.MEETING_INFO + " text" + ";");
                    db.execSQL("alter table " + Message.DELETED_TABLE_NAME
                            + " add column " + MessageColumns.MEETING_INFO + " text" + ";");
                } catch (SQLException e) {
                    Log.w(TAG, "Exception upgrading EmailProvider.db from 9 to 10 " + e);
                }
                oldVersion = 10;
            }
            if (oldVersion == 10) {
                try {
                    db.execSQL("alter table " + Attachment.TABLE_NAME
                            + " add column " + AttachmentColumns.CONTENT + " text" + ";");
                    db.execSQL("alter table " + Attachment.TABLE_NAME
                            + " add column " + AttachmentColumns.FLAGS + " integer" + ";");
                } catch (SQLException e) {
                    Log.w(TAG, "Exception upgrading EmailProvider.db from 10 to 11 " + e);
                }
                oldVersion = 11;
            }
            if (oldVersion == 11) {
                try {
                    db.execSQL("alter table " + Attachment.TABLE_NAME
                            + " add column " + AttachmentColumns.CONTENT_BYTES + " blob" + ";");
                } catch (SQLException e) {
                    Log.w(TAG, "Exception upgrading EmailProvider.db from 11 to 12 " + e);
                }
                oldVersion = 12;
            }
        }
        @Override
        public void onOpen(SQLiteDatabase db) {
        }
    }
    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        final int match = sURIMatcher.match(uri);
        Context context = getContext();
        SQLiteDatabase db = getDatabase(context);
        int table = match >> BASE_SHIFT;
        String id = "0";
        boolean messageDeletion = false;
        if (Email.LOGD) {
            Log.v(TAG, "EmailProvider.delete: uri=" + uri + ", match is " + match);
        }
        int result = -1;
        try {
            switch (match) {
                case MAILBOX_ID:
                case MAILBOX:
                case ACCOUNT_ID:
                case ACCOUNT:
                case MESSAGE:
                case SYNCED_MESSAGE_ID:
                case MESSAGE_ID:
                    messageDeletion = true;
                    db.beginTransaction();
                    break;
            }
            switch (match) {
                case BODY_ID:
                case DELETED_MESSAGE_ID:
                case SYNCED_MESSAGE_ID:
                case MESSAGE_ID:
                case UPDATED_MESSAGE_ID:
                case ATTACHMENT_ID:
                case MAILBOX_ID:
                case ACCOUNT_ID:
                case HOSTAUTH_ID:
                    id = uri.getPathSegments().get(1);
                    if (match == SYNCED_MESSAGE_ID) {
                        db.execSQL(DELETED_MESSAGE_INSERT + id);
                        db.execSQL(UPDATED_MESSAGE_DELETE + id);
                    }
                    result = db.delete(TABLE_NAMES[table], whereWithId(id, selection),
                            selectionArgs);
                    break;
                case ATTACHMENTS_MESSAGE_ID:
                    id = uri.getPathSegments().get(2);
                    result = db.delete(TABLE_NAMES[table],
                            whereWith(Attachment.MESSAGE_KEY + "=" + id, selection), selectionArgs);
                    break;
                case BODY:
                case MESSAGE:
                case DELETED_MESSAGE:
                case UPDATED_MESSAGE:
                case ATTACHMENT:
                case MAILBOX:
                case ACCOUNT:
                case HOSTAUTH:
                    result = db.delete(TABLE_NAMES[table], selection, selectionArgs);
                    break;
                default:
                    throw new IllegalArgumentException("Unknown URI " + uri);
            }
            if (messageDeletion) {
                if (match == MESSAGE_ID) {
                    db.execSQL(DELETE_BODY + id);
                } else {
                    db.execSQL(DELETE_ORPHAN_BODIES);
                }
                db.setTransactionSuccessful();
            }
        } catch (SQLiteException e) {
            checkDatabases();
            throw e;
        } finally {
            if (messageDeletion) {
                db.endTransaction();
            }
        }
        getContext().getContentResolver().notifyChange(uri, null);
        return result;
    }
    @Override
    public String getType(Uri uri) {
        int match = sURIMatcher.match(uri);
        switch (match) {
            case BODY_ID:
                return "vnd.android.cursor.item/email-body";
            case BODY:
                return "vnd.android.cursor.dir/email-message";
            case UPDATED_MESSAGE_ID:
            case MESSAGE_ID:
                return "vnd.android.cursor.item/email-message";
            case MAILBOX_MESSAGES:
            case UPDATED_MESSAGE:
            case MESSAGE:
                return "vnd.android.cursor.dir/email-message";
            case ACCOUNT_MAILBOXES:
            case MAILBOX:
                return "vnd.android.cursor.dir/email-mailbox";
            case MAILBOX_ID:
                return "vnd.android.cursor.item/email-mailbox";
            case ACCOUNT:
                return "vnd.android.cursor.dir/email-account";
            case ACCOUNT_ID:
                return "vnd.android.cursor.item/email-account";
            case ATTACHMENTS_MESSAGE_ID:
            case ATTACHMENT:
                return "vnd.android.cursor.dir/email-attachment";
            case ATTACHMENT_ID:
                return "vnd.android.cursor.item/email-attachment";
            case HOSTAUTH:
                return "vnd.android.cursor.dir/email-hostauth";
            case HOSTAUTH_ID:
                return "vnd.android.cursor.item/email-hostauth";
            default:
                throw new IllegalArgumentException("Unknown URI " + uri);
        }
    }
    @Override
    public Uri insert(Uri uri, ContentValues values) {
        int match = sURIMatcher.match(uri);
        Context context = getContext();
        SQLiteDatabase db = getDatabase(context);
        int table = match >> BASE_SHIFT;
        long id;
        if (Email.LOGD) {
            Log.v(TAG, "EmailProvider.insert: uri=" + uri + ", match is " + match);
        }
        Uri resultUri = null;
        try {
            switch (match) {
                case UPDATED_MESSAGE:
                case DELETED_MESSAGE:
                case BODY:
                case MESSAGE:
                case ATTACHMENT:
                case MAILBOX:
                case ACCOUNT:
                case HOSTAUTH:
                    id = db.insert(TABLE_NAMES[table], "foo", values);
                    resultUri = ContentUris.withAppendedId(uri, id);
                    if (match == UPDATED_MESSAGE || match == DELETED_MESSAGE) {
                        throw new IllegalArgumentException("Unknown URL " + uri);
                    }
                    break;
                case MAILBOX_ID:
                    id = Long.parseLong(uri.getPathSegments().get(1));
                    values.put(MessageColumns.MAILBOX_KEY, id);
                    resultUri = insert(Message.CONTENT_URI, values);
                    break;
                case MESSAGE_ID:
                    id = Long.parseLong(uri.getPathSegments().get(1));
                    values.put(AttachmentColumns.MESSAGE_KEY, id);
                    resultUri = insert(Attachment.CONTENT_URI, values);
                    break;
                case ACCOUNT_ID:
                    id = Long.parseLong(uri.getPathSegments().get(1));
                    values.put(MailboxColumns.ACCOUNT_KEY, id);
                    resultUri = insert(Mailbox.CONTENT_URI, values);
                    break;
                case ATTACHMENTS_MESSAGE_ID:
                    id = db.insert(TABLE_NAMES[table], "foo", values);
                    resultUri = ContentUris.withAppendedId(Attachment.CONTENT_URI, id);
                    break;
                default:
                    throw new IllegalArgumentException("Unknown URL " + uri);
            }
        } catch (SQLiteException e) {
            checkDatabases();
            throw e;
        }
        getContext().getContentResolver().notifyChange(uri, null);
        return resultUri;
    }
    @Override
    public boolean onCreate() {
        checkDatabases();
        return false;
    }
    public void checkDatabases() {
        if (mDatabase != null) {
            mDatabase = null;
        }
        if (mBodyDatabase != null) {
            mBodyDatabase = null;
        }
        File databaseFile = getContext().getDatabasePath(DATABASE_NAME);
        File bodyFile = getContext().getDatabasePath(BODY_DATABASE_NAME);
        if (databaseFile.exists() && !bodyFile.exists()) {
            Log.w(TAG, "Deleting orphaned EmailProvider database...");
            databaseFile.delete();
        } else if (bodyFile.exists() && !databaseFile.exists()) {
            Log.w(TAG, "Deleting orphaned EmailProviderBody database...");
            bodyFile.delete();
        }
    }
    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs,
            String sortOrder) {
        Cursor c = null;
        Uri notificationUri = EmailContent.CONTENT_URI;
        int match = sURIMatcher.match(uri);
        Context context = getContext();
        SQLiteDatabase db = getDatabase(context);
        int table = match >> BASE_SHIFT;
        String id;
        if (Email.LOGD) {
            Log.v(TAG, "EmailProvider.query: uri=" + uri + ", match is " + match);
        }
        try {
            switch (match) {
                case BODY:
                case MESSAGE:
                case UPDATED_MESSAGE:
                case DELETED_MESSAGE:
                case ATTACHMENT:
                case MAILBOX:
                case ACCOUNT:
                case HOSTAUTH:
                    c = db.query(TABLE_NAMES[table], projection,
                            selection, selectionArgs, null, null, sortOrder);
                    break;
                case BODY_ID:
                case MESSAGE_ID:
                case DELETED_MESSAGE_ID:
                case UPDATED_MESSAGE_ID:
                case ATTACHMENT_ID:
                case MAILBOX_ID:
                case ACCOUNT_ID:
                case HOSTAUTH_ID:
                    id = uri.getPathSegments().get(1);
                    c = db.query(TABLE_NAMES[table], projection,
                            whereWithId(id, selection), selectionArgs, null, null, sortOrder);
                    break;
                case ATTACHMENTS_MESSAGE_ID:
                    id = uri.getPathSegments().get(2);
                    c = db.query(Attachment.TABLE_NAME, projection,
                            whereWith(Attachment.MESSAGE_KEY + "=" + id, selection),
                            selectionArgs, null, null, sortOrder);
                    break;
                default:
                    throw new IllegalArgumentException("Unknown URI " + uri);
            }
        } catch (SQLiteException e) {
            checkDatabases();
            throw e;
        }
        if ((c != null) && !isTemporary()) {
            c.setNotificationUri(getContext().getContentResolver(), notificationUri);
        }
        return c;
    }
    private String whereWithId(String id, String selection) {
        StringBuilder sb = new StringBuilder(256);
        sb.append("_id=");
        sb.append(id);
        if (selection != null) {
            sb.append(" AND (");
            sb.append(selection);
            sb.append(')');
        }
        return sb.toString();
    }
    private String whereWith(String where, String selection) {
        if (selection == null) {
            return where;
        }
        StringBuilder sb = new StringBuilder(where);
        sb.append(" AND (");
        sb.append(selection);
        sb.append(')');
        return sb.toString();
    }
    @Override
    public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
        int match = sURIMatcher.match(uri);
        Context context = getContext();
        SQLiteDatabase db = getDatabase(context);
        int table = match >> BASE_SHIFT;
        int result;
        if (Email.LOGD) {
            Log.v(TAG, "EmailProvider.update: uri=" + uri + ", match is " + match);
        }
        if (match == MAILBOX_ID || match == MAILBOX) {
            values.remove(MailboxColumns.UNREAD_COUNT);
        }
        if (uri == INTEGRITY_CHECK_URI) {
            checkDatabases();
            return 0;
        }
        String id;
        try {
            switch (match) {
                case MAILBOX_ID_ADD_TO_FIELD:
                case ACCOUNT_ID_ADD_TO_FIELD:
                    db.beginTransaction();
                    id = uri.getPathSegments().get(1);
                    String field = values.getAsString(EmailContent.FIELD_COLUMN_NAME);
                    Long add = values.getAsLong(EmailContent.ADD_COLUMN_NAME);
                    if (field == null || add == null) {
                        throw new IllegalArgumentException("No field/add specified " + uri);
                    }
                    Cursor c = db.query(TABLE_NAMES[table],
                            new String[] {EmailContent.RECORD_ID, field},
                            whereWithId(id, selection),
                            selectionArgs, null, null, null);
                    try {
                        result = 0;
                        ContentValues cv = new ContentValues();
                        String[] bind = new String[1];
                        while (c.moveToNext()) {
                            bind[0] = c.getString(0);
                            long value = c.getLong(1) + add;
                            cv.put(field, value);
                            result = db.update(TABLE_NAMES[table], cv, ID_EQUALS, bind);
                        }
                    } finally {
                        c.close();
                    }
                    db.setTransactionSuccessful();
                    db.endTransaction();
                    break;
                case BODY_ID:
                case MESSAGE_ID:
                case SYNCED_MESSAGE_ID:
                case UPDATED_MESSAGE_ID:
                case ATTACHMENT_ID:
                case MAILBOX_ID:
                case ACCOUNT_ID:
                case HOSTAUTH_ID:
                    id = uri.getPathSegments().get(1);
                    if (match == SYNCED_MESSAGE_ID) {
                        db.execSQL(UPDATED_MESSAGE_INSERT + id);
                    } else if (match == MESSAGE_ID) {
                        db.execSQL(UPDATED_MESSAGE_DELETE + id);
                    }
                    result = db.update(TABLE_NAMES[table], values, whereWithId(id, selection),
                            selectionArgs);
                    break;
                case BODY:
                case MESSAGE:
                case UPDATED_MESSAGE:
                case ATTACHMENT:
                case MAILBOX:
                case ACCOUNT:
                case HOSTAUTH:
                    result = db.update(TABLE_NAMES[table], values, selection, selectionArgs);
                    break;
                default:
                    throw new IllegalArgumentException("Unknown URI " + uri);
            }
        } catch (SQLiteException e) {
            checkDatabases();
            throw e;
        }
        getContext().getContentResolver().notifyChange(uri, null);
        return result;
    }
    @Override
    public ContentProviderResult[] applyBatch(ArrayList<ContentProviderOperation> operations)
            throws OperationApplicationException {
        Context context = getContext();
        SQLiteDatabase db = getDatabase(context);
        db.beginTransaction();
        try {
            ContentProviderResult[] results = super.applyBatch(operations);
            db.setTransactionSuccessful();
            return results;
        } finally {
            db.endTransaction();
        }
    }
}
