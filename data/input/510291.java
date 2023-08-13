public class LocalStore extends Store implements PersistentDataCallbacks {
    private static final int DB_VERSION = 24;
    private static final Flag[] PERMANENT_FLAGS = { Flag.DELETED, Flag.X_DESTROYED, Flag.SEEN };
    private String mPath;
    private SQLiteDatabase mDb;
    private File mAttachmentsDir;
    private Context mContext;
    private int mVisibleLimitDefault = -1;
    public static LocalStore newInstance(String uri, Context context,
            PersistentDataCallbacks callbacks) throws MessagingException {
        return new LocalStore(uri, context);
    }
    private LocalStore(String _uri, Context context) throws MessagingException {
        mContext = context;
        URI uri = null;
        try {
            uri = new URI(_uri);
        } catch (Exception e) {
            throw new MessagingException("Invalid uri for LocalStore");
        }
        if (!uri.getScheme().equals("local")) {
            throw new MessagingException("Invalid scheme");
        }
        mPath = uri.getPath();
        File parentDir = new File(mPath).getParentFile();
        if (!parentDir.exists()) {
            parentDir.mkdirs();
        }
        mDb = SQLiteDatabase.openOrCreateDatabase(mPath, null);
        int oldVersion = mDb.getVersion();
        if (oldVersion != DB_VERSION) {
            if (Email.LOGD) {
                Log.v(Email.LOG_TAG, String.format("Upgrading database from %d to %d",
                        oldVersion, DB_VERSION));
            }
            if (oldVersion < 18) {
                mDb.execSQL("DROP TABLE IF EXISTS folders");
                mDb.execSQL("CREATE TABLE folders (id INTEGER PRIMARY KEY, name TEXT, "
                        + "last_updated INTEGER, unread_count INTEGER, visible_limit INTEGER)");
                mDb.execSQL("DROP TABLE IF EXISTS messages");
                mDb.execSQL("CREATE TABLE messages (id INTEGER PRIMARY KEY, folder_id INTEGER, " +
                        "uid TEXT, subject TEXT, date INTEGER, flags TEXT, sender_list TEXT, " +
                        "to_list TEXT, cc_list TEXT, bcc_list TEXT, reply_to_list TEXT, " +
                        "html_content TEXT, text_content TEXT, attachment_count INTEGER, " +
                        "internal_date INTEGER, message_id TEXT, store_flag_1 INTEGER, " +
                        "store_flag_2 INTEGER, flag_downloaded_full INTEGER," +
                        "flag_downloaded_partial INTEGER, flag_deleted INTEGER, x_headers TEXT)");
                mDb.execSQL("DROP TABLE IF EXISTS attachments");
                mDb.execSQL("CREATE TABLE attachments (id INTEGER PRIMARY KEY, message_id INTEGER,"
                        + "store_data TEXT, content_uri TEXT, size INTEGER, name TEXT,"
                        + "mime_type TEXT, content_id TEXT)");
                mDb.execSQL("DROP TABLE IF EXISTS pending_commands");
                mDb.execSQL("CREATE TABLE pending_commands " +
                        "(id INTEGER PRIMARY KEY, command TEXT, arguments TEXT)");
                addRemoteStoreDataTable();
                addFolderDeleteTrigger();
                mDb.execSQL("DROP TRIGGER IF EXISTS delete_message");
                mDb.execSQL("CREATE TRIGGER delete_message BEFORE DELETE ON messages BEGIN DELETE FROM attachments WHERE old.id = message_id; END;");
                mDb.setVersion(DB_VERSION);
            }
            else {
                if (oldVersion < 19) {
                    mDb.execSQL("ALTER TABLE messages ADD COLUMN message_id TEXT;");
                    mDb.setVersion(19);
                }
                if (oldVersion < 20) {
                    mDb.execSQL("ALTER TABLE attachments ADD COLUMN content_id TEXT;");
                    mDb.setVersion(20);
                }
                if (oldVersion < 21) {
                    addRemoteStoreDataTable();
                    addFolderDeleteTrigger();
                    mDb.setVersion(21);
                }
                if (oldVersion < 22) {
                    mDb.execSQL("ALTER TABLE messages ADD COLUMN store_flag_1 INTEGER;");
                    mDb.execSQL("ALTER TABLE messages ADD COLUMN store_flag_2 INTEGER;");
                    mDb.setVersion(22);
                }
                if (oldVersion < 23) {
                    mDb.beginTransaction();
                    try {
                        mDb.execSQL(
                                "ALTER TABLE messages ADD COLUMN flag_downloaded_full INTEGER;");
                        mDb.execSQL(
                                "ALTER TABLE messages ADD COLUMN flag_downloaded_partial INTEGER;");
                        mDb.execSQL(
                                "ALTER TABLE messages ADD COLUMN flag_deleted INTEGER;");
                        migrateMessageFlags();
                        mDb.setVersion(23);
                        mDb.setTransactionSuccessful();
                    } finally {
                        mDb.endTransaction();
                    }
                }
                if (oldVersion < 24) {
                    mDb.execSQL("ALTER TABLE messages ADD COLUMN x_headers TEXT;");
                    mDb.setVersion(24);
                }
            }
            if (mDb.getVersion() != DB_VERSION) {
                throw new Error("Database upgrade failed!");
            }
        }
        mAttachmentsDir = new File(mPath + "_att");
        if (!mAttachmentsDir.exists()) {
            mAttachmentsDir.mkdirs();
        }
    }
    private void addRemoteStoreDataTable() {
        mDb.execSQL("DROP TABLE IF EXISTS remote_store_data");
        mDb.execSQL("CREATE TABLE remote_store_data (" +
        		"id INTEGER PRIMARY KEY, folder_id INTEGER, data_key TEXT, data TEXT, " +
                "UNIQUE (folder_id, data_key) ON CONFLICT REPLACE" +
                ")");
    }
    private void addFolderDeleteTrigger() {
        mDb.execSQL("DROP TRIGGER IF EXISTS delete_folder");
        mDb.execSQL("CREATE TRIGGER delete_folder "
                + "BEFORE DELETE ON folders "
                + "BEGIN "
                    + "DELETE FROM messages WHERE old.id = folder_id; "
                    + "DELETE FROM remote_store_data WHERE old.id = folder_id; "
                + "END;");
    }
    private void migrateMessageFlags() {
        Cursor cursor = mDb.query("messages",
                new String[] { "id", "flags" },
                null, null, null, null, null);
        try {
            int columnId = cursor.getColumnIndexOrThrow("id");
            int columnFlags = cursor.getColumnIndexOrThrow("flags");
            while (cursor.moveToNext()) {
                String oldFlags = cursor.getString(columnFlags);
                ContentValues values = new ContentValues();
                int newFlagDlFull = 0;
                int newFlagDlPartial = 0;
                int newFlagDeleted = 0;
                if (oldFlags != null) {
                    if (oldFlags.contains(Flag.X_DOWNLOADED_FULL.toString())) {
                        newFlagDlFull = 1;
                    }
                    if (oldFlags.contains(Flag.X_DOWNLOADED_PARTIAL.toString())) {
                        newFlagDlPartial = 1;
                    }
                    if (oldFlags.contains(Flag.DELETED.toString())) {
                        newFlagDeleted = 1;
                    }
                }
                values.put("flag_downloaded_full", newFlagDlFull);
                values.put("flag_downloaded_partial", newFlagDlPartial);
                values.put("flag_deleted", newFlagDeleted);
                int rowId = cursor.getInt(columnId);
                mDb.update("messages", values, "id=" + rowId, null);
            }
        } finally {
            cursor.close();
        }
    }
    @Override
    public Folder getFolder(String name) throws MessagingException {
        return new LocalFolder(name);
    }
    @Override
    public Folder[] getPersonalNamespaces() throws MessagingException {
        ArrayList<Folder> folders = new ArrayList<Folder>();
        Cursor cursor = null;
        try {
            cursor = mDb.rawQuery("SELECT name FROM folders", null);
            while (cursor.moveToNext()) {
                folders.add(new LocalFolder(cursor.getString(0)));
            }
        }
        finally {
            if (cursor != null) {
                cursor.close();
            }
        }
        return folders.toArray(new Folder[] {});
    }
    @Override
    public void checkSettings() throws MessagingException {
    }
    public void close() {
        try {
            mDb.close();
            mDb = null;
        } catch (Exception e) {
            Log.d(Email.LOG_TAG, "Caught exception while closing localstore db: " + e);
        }
    }
    @Override
    public void delete() {
        try {
            mDb.close();
        } catch (Exception e) {
        }
        try{
            File[] attachments = mAttachmentsDir.listFiles();
            for (File attachment : attachments) {
                if (attachment.exists()) {
                    attachment.delete();
                }
            }
            if (mAttachmentsDir.exists()) {
                mAttachmentsDir.delete();
            }
        }
        catch (Exception e) {
        }
        try {
            new File(mPath).delete();
        }
        catch (Exception e) {
        }
    }
    public int getStoredAttachmentCount() {
        try{
            File[] attachments = mAttachmentsDir.listFiles();
            return attachments.length;
        }
        catch (Exception e) {
            return 0;
        }
    }
    public int pruneCachedAttachments() throws MessagingException {
        int prunedCount = 0;
        File[] files = mAttachmentsDir.listFiles();
        for (File file : files) {
            if (file.exists()) {
                try {
                    Cursor cursor = null;
                    try {
                        cursor = mDb.query(
                            "attachments",
                            new String[] { "store_data" },
                            "id = ?",
                            new String[] { file.getName() },
                            null,
                            null,
                            null);
                        if (cursor.moveToNext()) {
                            if (cursor.getString(0) == null) {
                                continue;
                            }
                        }
                    }
                    finally {
                        if (cursor != null) {
                            cursor.close();
                        }
                    }
                    ContentValues cv = new ContentValues();
                    cv.putNull("content_uri");
                    mDb.update("attachments", cv, "id = ?", new String[] { file.getName() });
                }
                catch (Exception e) {
                }
                if (!file.delete()) {
                    file.deleteOnExit();
                }
                prunedCount++;
            }
        }
        return prunedCount;
    }
    public void resetVisibleLimits(int visibleLimit) {
        mVisibleLimitDefault = visibleLimit;            
        ContentValues cv = new ContentValues();
        cv.put("visible_limit", Integer.toString(visibleLimit));
        mDb.update("folders", cv, null, null);
    }
    public ArrayList<PendingCommand> getPendingCommands() {
        Cursor cursor = null;
        try {
            cursor = mDb.query("pending_commands",
                    new String[] { "id", "command", "arguments" },
                    null,
                    null,
                    null,
                    null,
                    "id ASC");
            ArrayList<PendingCommand> commands = new ArrayList<PendingCommand>();
            while (cursor.moveToNext()) {
                PendingCommand command = new PendingCommand();
                command.mId = cursor.getLong(0);
                command.command = cursor.getString(1);
                String arguments = cursor.getString(2);
                command.arguments = arguments.split(",");
                for (int i = 0; i < command.arguments.length; i++) {
                    command.arguments[i] = Utility.fastUrlDecode(command.arguments[i]);
                }
                commands.add(command);
            }
            return commands;
        }
        finally {
            if (cursor != null) {
                cursor.close();
            }
        }
    }
    public void addPendingCommand(PendingCommand command) {
        try {
            for (int i = 0; i < command.arguments.length; i++) {
                command.arguments[i] = URLEncoder.encode(command.arguments[i], "UTF-8");
            }
            ContentValues cv = new ContentValues();
            cv.put("command", command.command);
            cv.put("arguments", Utility.combine(command.arguments, ','));
            mDb.insert("pending_commands", "command", cv);
        }
        catch (UnsupportedEncodingException usee) {
            throw new Error("Aparently UTF-8 has been lost to the annals of history.");
        }
    }
    public void removePendingCommand(PendingCommand command) {
        mDb.delete("pending_commands", "id = ?", new String[] { Long.toString(command.mId) });
    }
    public static class PendingCommand {
        private long mId;
        public String command;
        public String[] arguments;
        @Override
        public String toString() {
            StringBuffer sb = new StringBuffer();
            sb.append(command);
            sb.append("\n");
            for (String argument : arguments) {
                sb.append("  ");
                sb.append(argument);
                sb.append("\n");
            }
            return sb.toString();
        }
    }
    public PersistentDataCallbacks getPersistentCallbacks() throws MessagingException {
        return this;
    }
    public String getPersistentString(String key, String defaultValue) {
        return getPersistentString(-1, key, defaultValue);
    }
    public void setPersistentString(String key, String value) {
        setPersistentString(-1, key, value);
    }
    private String getPersistentString(long folderId, String key, String defaultValue) {
        String result = defaultValue;
        Cursor cursor = null;
        try {
            cursor = mDb.query("remote_store_data",
                    new String[] { "data" },
                    "folder_id = ? AND data_key = ?",
                    new String[] { Long.toString(folderId), key },
                    null,
                    null,
                    null);
            if (cursor != null && cursor.moveToNext()) {
                result = cursor.getString(0);
            }
        }
        finally {
            if (cursor != null) {
                cursor.close();
            }
        }
        return result;
    }
    private void setPersistentString(long folderId, String key, String value) {
        ContentValues cv = new ContentValues();
        cv.put("folder_id", Long.toString(folderId));
        cv.put("data_key", key);
        cv.put("data", value);
        mDb.insert("remote_store_data", null, cv);
    }
    public class LocalFolder extends Folder implements Folder.PersistentDataCallbacks {
        private String mName;
        private long mFolderId = -1;
        private int mUnreadMessageCount = -1;
        private int mVisibleLimit = -1;
        public LocalFolder(String name) {
            this.mName = name;
        }
        public long getId() {
            return mFolderId;
        }
        private void open(OpenMode mode) throws MessagingException {
            open(mode, null);
        }
        @Override
        public void open(OpenMode mode, PersistentDataCallbacks callbacks)
                throws MessagingException {
            if (isOpen()) {
                return;
            }
            if (!exists()) {
                create(FolderType.HOLDS_MESSAGES);
            }
            Cursor cursor = null;
            try {
                cursor = mDb.rawQuery("SELECT id, unread_count, visible_limit FROM folders "
                        + "where folders.name = ?",
                    new String[] {
                        mName
                    });
                if (!cursor.moveToFirst()) {
                    throw new MessagingException("Nonexistent folder");
                }
                mFolderId = cursor.getInt(0);
                mUnreadMessageCount = cursor.getInt(1);
                mVisibleLimit = cursor.getInt(2);
            }
            finally {
                if (cursor != null) {
                    cursor.close();
                }
            }
        }
        @Override
        public boolean isOpen() {
            return mFolderId != -1;
        }
        @Override
        public OpenMode getMode() throws MessagingException {
            return OpenMode.READ_WRITE;
        }
        @Override
        public String getName() {
            return mName;
        }
        @Override
        public boolean exists() throws MessagingException {
            return Utility.arrayContains(getPersonalNamespaces(), this);
        }
        @Override
        public boolean canCreate(FolderType type) {
            return true;
        }
        @Override
        public boolean create(FolderType type) throws MessagingException {
            if (exists()) {
                throw new MessagingException("Folder " + mName + " already exists.");
            }
            mDb.execSQL("INSERT INTO folders (name, visible_limit) VALUES (?, ?)", new Object[] {
                mName,
                mVisibleLimitDefault
            });
            return true;
        }
        @Override
        public void close(boolean expunge) throws MessagingException {
            if (expunge) {
                expunge();
            }
            mFolderId = -1;
        }
        @Override
        public int getMessageCount() throws MessagingException {
            return getMessageCount(null, null);
        }
        public int getMessageCount(Flag[] setFlags, Flag[] clearFlags) throws MessagingException {
            StringBuilder sql = new StringBuilder("SELECT COUNT(*) FROM messages WHERE ");
            buildFlagPredicates(sql, setFlags, clearFlags);
            sql.append("messages.folder_id = ?");
            open(OpenMode.READ_WRITE);
            Cursor cursor = null;
            try {
                cursor = mDb.rawQuery(
                        sql.toString(),
                        new String[] {
                            Long.toString(mFolderId)
                        });
                cursor.moveToFirst();
                int messageCount = cursor.getInt(0);
                return messageCount;
            }
            finally {
                if (cursor != null) {
                    cursor.close();
                }
            }
        }
        @Override
        public int getUnreadMessageCount() throws MessagingException {
            if (!isOpen()) {
                open(OpenMode.READ_WRITE);
            } else {
                Cursor cursor = null;
                try {
                    cursor = mDb.rawQuery("SELECT unread_count FROM folders WHERE folders.name = ?",
                            new String[] { mName });
                    if (!cursor.moveToFirst()) {
                        throw new MessagingException("Nonexistent folder");
                    }
                    mUnreadMessageCount = cursor.getInt(0);
                } finally {
                    if (cursor != null) {
                        cursor.close();
                    }
                }
            }
            return mUnreadMessageCount;
        }
        public void setUnreadMessageCount(int unreadMessageCount) throws MessagingException {
            open(OpenMode.READ_WRITE);
            mUnreadMessageCount = Math.max(0, unreadMessageCount);
            mDb.execSQL("UPDATE folders SET unread_count = ? WHERE id = ?",
                    new Object[] { mUnreadMessageCount, mFolderId });
        }
        public int getVisibleLimit() throws MessagingException {
            if (!isOpen()) {
                open(OpenMode.READ_WRITE);
            } else {
                Cursor cursor = null;
                try {
                    cursor = mDb.rawQuery(
                            "SELECT visible_limit FROM folders WHERE folders.name = ?",
                            new String[] { mName });
                    if (!cursor.moveToFirst()) {
                        throw new MessagingException("Nonexistent folder");
                    }
                    mVisibleLimit = cursor.getInt(0);
                } finally {
                    if (cursor != null) {
                        cursor.close();
                    }
                }
            }
            return mVisibleLimit;
        }
        public void setVisibleLimit(int visibleLimit) throws MessagingException {
            open(OpenMode.READ_WRITE);
            mVisibleLimit = visibleLimit;
            mDb.execSQL("UPDATE folders SET visible_limit = ? WHERE id = ?",
                    new Object[] { mVisibleLimit, mFolderId });
        }
        @Override
        public void fetch(Message[] messages, FetchProfile fp, MessageRetrievalListener listener)
                throws MessagingException {
            open(OpenMode.READ_WRITE);
            if (fp.contains(FetchProfile.Item.BODY) || fp.contains(FetchProfile.Item.STRUCTURE)) {
                for (Message message : messages) {
                    LocalMessage localMessage = (LocalMessage)message;
                    Cursor cursor = null;
                    localMessage.setHeader(MimeHeader.HEADER_CONTENT_TYPE, "multipart/mixed");
                    MimeMultipart mp = new MimeMultipart();
                    mp.setSubType("mixed");
                    localMessage.setBody(mp);
                    if (fp.contains(FetchProfile.Item.BODY)) {
                        try {
                            cursor = mDb.rawQuery("SELECT html_content, text_content FROM messages "
                                    + "WHERE id = ?",
                                    new String[] { Long.toString(localMessage.mId) });
                            cursor.moveToNext();
                            String htmlContent = cursor.getString(0);
                            String textContent = cursor.getString(1);
                            if (htmlContent != null) {
                                TextBody body = new TextBody(htmlContent);
                                MimeBodyPart bp = new MimeBodyPart(body, "text/html");
                                mp.addBodyPart(bp);
                            }
                            if (textContent != null) {
                                TextBody body = new TextBody(textContent);
                                MimeBodyPart bp = new MimeBodyPart(body, "text/plain");
                                mp.addBodyPart(bp);
                            }
                        }
                        finally {
                            if (cursor != null) {
                                cursor.close();
                            }
                        }
                    } else {
                        MimeBodyPart bp = new MimeBodyPart();
                        bp.setHeader(MimeHeader.HEADER_CONTENT_TYPE,
                                "text/html;\n charset=\"UTF-8\"");
                        mp.addBodyPart(bp);
                        bp = new MimeBodyPart();
                        bp.setHeader(MimeHeader.HEADER_CONTENT_TYPE,
                                "text/plain;\n charset=\"UTF-8\"");
                        mp.addBodyPart(bp);
                    }
                    try {
                        cursor = mDb.query(
                                "attachments",
                                new String[] {
                                        "id",
                                        "size",
                                        "name",
                                        "mime_type",
                                        "store_data",
                                        "content_uri",
                                        "content_id" },
                                "message_id = ?",
                                new String[] { Long.toString(localMessage.mId) },
                                null,
                                null,
                                null);
                        while (cursor.moveToNext()) {
                            long id = cursor.getLong(0);
                            int size = cursor.getInt(1);
                            String name = cursor.getString(2);
                            String type = cursor.getString(3);
                            String storeData = cursor.getString(4);
                            String contentUri = cursor.getString(5);
                            String contentId = cursor.getString(6);
                            Body body = null;
                            if (contentUri != null) {
                                body = new LocalAttachmentBody(Uri.parse(contentUri), mContext);
                            }
                            MimeBodyPart bp = new LocalAttachmentBodyPart(body, id);
                            bp.setHeader(MimeHeader.HEADER_CONTENT_TYPE,
                                    String.format("%s;\n name=\"%s\"",
                                    type,
                                    name));
                            bp.setHeader(MimeHeader.HEADER_CONTENT_TRANSFER_ENCODING, "base64");
                            bp.setHeader(MimeHeader.HEADER_CONTENT_DISPOSITION,
                                    String.format("attachment;\n filename=\"%s\";\n size=%d",
                                    name,
                                    size));
                            bp.setHeader(MimeHeader.HEADER_CONTENT_ID, contentId);
                            bp.setHeader(MimeHeader.HEADER_ANDROID_ATTACHMENT_STORE_DATA, storeData);
                            mp.addBodyPart(bp);
                        }
                    }
                    finally {
                        if (cursor != null) {
                            cursor.close();
                        }
                    }
                }
            }
        }
        private final String POPULATE_MESSAGE_SELECT_COLUMNS =
            "subject, sender_list, date, uid, flags, id, to_list, cc_list, " +
            "bcc_list, reply_to_list, attachment_count, internal_date, message_id, " +
            "store_flag_1, store_flag_2, flag_downloaded_full, flag_downloaded_partial, " +
            "flag_deleted, x_headers";
        private void populateMessageFromGetMessageCursor(LocalMessage message, Cursor cursor)
                throws MessagingException{
            message.setSubject(cursor.getString(0) == null ? "" : cursor.getString(0));
            Address[] from = Address.legacyUnpack(cursor.getString(1));
            if (from.length > 0) {
                message.setFrom(from[0]);
            }
            message.setSentDate(new Date(cursor.getLong(2)));
            message.setUid(cursor.getString(3));
            String flagList = cursor.getString(4);
            if (flagList != null && flagList.length() > 0) {
                String[] flags = flagList.split(",");
                try {
                    for (String flag : flags) {
                        message.setFlagInternal(Flag.valueOf(flag.toUpperCase()), true);
                    }
                } catch (Exception e) {
                }
            }
            message.mId = cursor.getLong(5);
            message.setRecipients(RecipientType.TO, Address.legacyUnpack(cursor.getString(6)));
            message.setRecipients(RecipientType.CC, Address.legacyUnpack(cursor.getString(7)));
            message.setRecipients(RecipientType.BCC, Address.legacyUnpack(cursor.getString(8)));
            message.setReplyTo(Address.legacyUnpack(cursor.getString(9)));
            message.mAttachmentCount = cursor.getInt(10);
            message.setInternalDate(new Date(cursor.getLong(11)));
            message.setMessageId(cursor.getString(12));
            message.setFlagInternal(Flag.X_STORE_1, (0 != cursor.getInt(13)));
            message.setFlagInternal(Flag.X_STORE_2, (0 != cursor.getInt(14)));
            message.setFlagInternal(Flag.X_DOWNLOADED_FULL, (0 != cursor.getInt(15)));
            message.setFlagInternal(Flag.X_DOWNLOADED_PARTIAL, (0 != cursor.getInt(16)));
            message.setFlagInternal(Flag.DELETED, (0 != cursor.getInt(17)));
            message.setExtendedHeaders(cursor.getString(18));
        }
        @Override
        public Message[] getMessages(int start, int end, MessageRetrievalListener listener)
                throws MessagingException {
            open(OpenMode.READ_WRITE);
            throw new MessagingException(
                    "LocalStore.getMessages(int, int, MessageRetrievalListener) not yet implemented");
        }
        @Override
        public Message getMessage(String uid) throws MessagingException {
            open(OpenMode.READ_WRITE);
            LocalMessage message = new LocalMessage(uid, this);
            Cursor cursor = null;
            try {
                cursor = mDb.rawQuery(
                        "SELECT " + POPULATE_MESSAGE_SELECT_COLUMNS +
                        " FROM messages" +
                        " WHERE uid = ? AND folder_id = ?",
                        new String[] {
                                message.getUid(), Long.toString(mFolderId)
                        });
                if (!cursor.moveToNext()) {
                    return null;
                }
                populateMessageFromGetMessageCursor(message, cursor);
            }
            finally {
                if (cursor != null) {
                    cursor.close();
                }
            }
            return message;
        }
        @Override
        public Message[] getMessages(MessageRetrievalListener listener) throws MessagingException {
            open(OpenMode.READ_WRITE);
            ArrayList<Message> messages = new ArrayList<Message>();
            Cursor cursor = null;
            try {
                cursor = mDb.rawQuery(
                        "SELECT " + POPULATE_MESSAGE_SELECT_COLUMNS +
                        " FROM messages" +
                        " WHERE folder_id = ?",
                        new String[] {
                                Long.toString(mFolderId)
                        });
                while (cursor.moveToNext()) {
                    LocalMessage message = new LocalMessage(null, this);
                    populateMessageFromGetMessageCursor(message, cursor);
                    messages.add(message);
                }
            }
            finally {
                if (cursor != null) {
                    cursor.close();
                }
            }
            return messages.toArray(new Message[] {});
        }
        @Override
        public Message[] getMessages(String[] uids, MessageRetrievalListener listener)
                throws MessagingException {
            open(OpenMode.READ_WRITE);
            if (uids == null) {
                return getMessages(listener);
            }
            ArrayList<Message> messages = new ArrayList<Message>();
            for (String uid : uids) {
                messages.add(getMessage(uid));
            }
            return messages.toArray(new Message[] {});
        }
        @Override
        public Message[] getMessages(Flag[] setFlags, Flag[] clearFlags,
                MessageRetrievalListener listener) throws MessagingException {
            StringBuilder sql = new StringBuilder(
                    "SELECT " + POPULATE_MESSAGE_SELECT_COLUMNS +
                    " FROM messages" +
                    " WHERE ");
            buildFlagPredicates(sql, setFlags, clearFlags);
            sql.append("folder_id = ?");
            open(OpenMode.READ_WRITE);
            ArrayList<Message> messages = new ArrayList<Message>();
            Cursor cursor = null;
            try {
                cursor = mDb.rawQuery(
                        sql.toString(),
                        new String[] {
                                Long.toString(mFolderId)
                        });
                while (cursor.moveToNext()) {
                    LocalMessage message = new LocalMessage(null, this);
                    populateMessageFromGetMessageCursor(message, cursor);
                    messages.add(message);
                }
            } finally {
                if (cursor != null) {
                    cursor.close();
                }
            }
            return messages.toArray(new Message[] {});
        }
        private void buildFlagPredicates(StringBuilder sql, Flag[] setFlags, Flag[] clearFlags)
                throws MessagingException {
            if (setFlags != null) {
                for (Flag flag : setFlags) {
                    if (flag == Flag.X_STORE_1) {
                        sql.append("store_flag_1 = 1 AND ");
                    } else if (flag == Flag.X_STORE_2) {
                        sql.append("store_flag_2 = 1 AND ");
                    } else if (flag == Flag.X_DOWNLOADED_FULL) {
                        sql.append("flag_downloaded_full = 1 AND ");
                    } else if (flag == Flag.X_DOWNLOADED_PARTIAL) {
                        sql.append("flag_downloaded_partial = 1 AND ");
                    } else if (flag == Flag.DELETED) {
                        sql.append("flag_deleted = 1 AND ");
                    } else {
                        throw new MessagingException("Unsupported flag " + flag);
                    }
                }
            }
            if (clearFlags != null) {
                for (Flag flag : clearFlags) {
                    if (flag == Flag.X_STORE_1) {
                        sql.append("store_flag_1 = 0 AND ");
                    } else if (flag == Flag.X_STORE_2) {
                        sql.append("store_flag_2 = 0 AND ");
                    } else if (flag == Flag.X_DOWNLOADED_FULL) {
                        sql.append("flag_downloaded_full = 0 AND ");
                    } else if (flag == Flag.X_DOWNLOADED_PARTIAL) {
                        sql.append("flag_downloaded_partial = 0 AND ");
                    } else if (flag == Flag.DELETED) {
                        sql.append("flag_deleted = 0 AND ");
                    } else {
                        throw new MessagingException("Unsupported flag " + flag);
                    }
                }
            }
        }
        @Override
        public void copyMessages(Message[] msgs, Folder folder, MessageUpdateCallbacks callbacks)
                throws MessagingException {
            if (!(folder instanceof LocalFolder)) {
                throw new MessagingException("copyMessages called with incorrect Folder");
            }
            ((LocalFolder) folder).appendMessages(msgs, true);
        }
        @Override
        public void appendMessages(Message[] messages) throws MessagingException {
            appendMessages(messages, false);
        }
        public void appendMessages(Message[] messages, boolean copy) throws MessagingException {
            open(OpenMode.READ_WRITE);
            for (Message message : messages) {
                if (!(message instanceof MimeMessage)) {
                    throw new Error("LocalStore can only store Messages that extend MimeMessage");
                }
                String uid = message.getUid();
                if (uid == null) {
                    message.setUid("Local" + UUID.randomUUID().toString());
                }
                else {
                    deleteAttachments(message.getUid());
                    mDb.execSQL("DELETE FROM messages WHERE folder_id = ? AND uid = ?",
                            new Object[] { mFolderId, message.getUid() });
                }
                ArrayList<Part> viewables = new ArrayList<Part>();
                ArrayList<Part> attachments = new ArrayList<Part>();
                MimeUtility.collectParts(message, viewables, attachments);
                StringBuffer sbHtml = new StringBuffer();
                StringBuffer sbText = new StringBuffer();
                for (Part viewable : viewables) {
                    try {
                        String text = MimeUtility.getTextFromPart(viewable);
                        if (viewable.getMimeType().equalsIgnoreCase("text/html")) {
                            sbHtml.append(text);
                        }
                        else {
                            sbText.append(text);
                        }
                    } catch (Exception e) {
                        throw new MessagingException("Unable to get text for message part", e);
                    }
                }
                try {
                    ContentValues cv = new ContentValues();
                    cv.put("uid", message.getUid());
                    cv.put("subject", message.getSubject());
                    cv.put("sender_list", Address.legacyPack(message.getFrom()));
                    cv.put("date", message.getSentDate() == null
                            ? System.currentTimeMillis() : message.getSentDate().getTime());
                    cv.put("flags", makeFlagsString(message));
                    cv.put("folder_id", mFolderId);
                    cv.put("to_list", Address.legacyPack(message.getRecipients(RecipientType.TO)));
                    cv.put("cc_list", Address.legacyPack(message.getRecipients(RecipientType.CC)));
                    cv.put("bcc_list", Address.legacyPack(
                            message.getRecipients(RecipientType.BCC)));
                    cv.put("html_content", sbHtml.length() > 0 ? sbHtml.toString() : null);
                    cv.put("text_content", sbText.length() > 0 ? sbText.toString() : null);
                    cv.put("reply_to_list", Address.legacyPack(message.getReplyTo()));
                    cv.put("attachment_count", attachments.size());
                    cv.put("internal_date",  message.getInternalDate() == null
                            ? System.currentTimeMillis() : message.getInternalDate().getTime());
                    cv.put("message_id", ((MimeMessage)message).getMessageId());
                    cv.put("store_flag_1", makeFlagNumeric(message, Flag.X_STORE_1));
                    cv.put("store_flag_2", makeFlagNumeric(message, Flag.X_STORE_2));
                    cv.put("flag_downloaded_full",
                            makeFlagNumeric(message, Flag.X_DOWNLOADED_FULL));
                    cv.put("flag_downloaded_partial",
                            makeFlagNumeric(message, Flag.X_DOWNLOADED_PARTIAL));
                    cv.put("flag_deleted", makeFlagNumeric(message, Flag.DELETED));
                    cv.put("x_headers", ((MimeMessage) message).getExtendedHeaders());
                    long messageId = mDb.insert("messages", "uid", cv);
                    for (Part attachment : attachments) {
                        saveAttachment(messageId, attachment, copy);
                    }
                } catch (Exception e) {
                    throw new MessagingException("Error appending message", e);
                }
            }
        }
        public void updateMessage(LocalMessage message) throws MessagingException {
            open(OpenMode.READ_WRITE);
            ArrayList<Part> viewables = new ArrayList<Part>();
            ArrayList<Part> attachments = new ArrayList<Part>();
            MimeUtility.collectParts(message, viewables, attachments);
            StringBuffer sbHtml = new StringBuffer();
            StringBuffer sbText = new StringBuffer();
            for (int i = 0, count = viewables.size(); i < count; i++)  {
                Part viewable = viewables.get(i);
                try {
                    String text = MimeUtility.getTextFromPart(viewable);
                    if (viewable.getMimeType().equalsIgnoreCase("text/html")) {
                        sbHtml.append(text);
                    }
                    else {
                        sbText.append(text);
                    }
                } catch (Exception e) {
                    throw new MessagingException("Unable to get text for message part", e);
                }
            }
            try {
                mDb.execSQL("UPDATE messages SET "
                        + "uid = ?, subject = ?, sender_list = ?, date = ?, flags = ?, "
                        + "folder_id = ?, to_list = ?, cc_list = ?, bcc_list = ?, "
                        + "html_content = ?, text_content = ?, reply_to_list = ?, "
                        + "attachment_count = ?, message_id = ?, store_flag_1 = ?, "
                        + "store_flag_2 = ?, flag_downloaded_full = ?, "
                        + "flag_downloaded_partial = ?, flag_deleted = ?, x_headers = ? "
                        + "WHERE id = ?",
                        new Object[] {
                                message.getUid(),
                                message.getSubject(),
                                Address.legacyPack(message.getFrom()),
                                message.getSentDate() == null ? System
                                        .currentTimeMillis() : message.getSentDate()
                                        .getTime(),
                                makeFlagsString(message),
                                mFolderId,
                                Address.legacyPack(message
                                        .getRecipients(RecipientType.TO)),
                                Address.legacyPack(message
                                        .getRecipients(RecipientType.CC)),
                                Address.legacyPack(message
                                        .getRecipients(RecipientType.BCC)),
                                sbHtml.length() > 0 ? sbHtml.toString() : null,
                                sbText.length() > 0 ? sbText.toString() : null,
                                Address.legacyPack(message.getReplyTo()),
                                attachments.size(),
                                message.getMessageId(),
                                makeFlagNumeric(message, Flag.X_STORE_1),
                                makeFlagNumeric(message, Flag.X_STORE_2),
                                makeFlagNumeric(message, Flag.X_DOWNLOADED_FULL),
                                makeFlagNumeric(message, Flag.X_DOWNLOADED_PARTIAL),
                                makeFlagNumeric(message, Flag.DELETED),
                                message.getExtendedHeaders(),
                                message.mId
                                });
                for (int i = 0, count = attachments.size(); i < count; i++) {
                    Part attachment = attachments.get(i);
                    saveAttachment(message.mId, attachment, false);
                }
            } catch (Exception e) {
                throw new MessagingException("Error appending message", e);
            }
        }
        private void saveAttachment(long messageId, Part attachment, boolean saveAsNew)
                throws IOException, MessagingException {
            long attachmentId = -1;
            Uri contentUri = null;
            int size = -1;
            File tempAttachmentFile = null;
            if ((!saveAsNew) && (attachment instanceof LocalAttachmentBodyPart)) {
                attachmentId = ((LocalAttachmentBodyPart) attachment).getAttachmentId();
            }
            if (attachment.getBody() != null) {
                Body body = attachment.getBody();
                if (body instanceof LocalAttachmentBody) {
                    contentUri = ((LocalAttachmentBody) body).getContentUri();
                }
                else {
                    InputStream in = attachment.getBody().getInputStream();
                    tempAttachmentFile = File.createTempFile("att", null, mAttachmentsDir);
                    FileOutputStream out = new FileOutputStream(tempAttachmentFile);
                    size = IOUtils.copy(in, out);
                    in.close();
                    out.close();
                }
            }
            if (size == -1) {
                String disposition = attachment.getDisposition();
                if (disposition != null) {
                    String s = MimeUtility.getHeaderParameter(disposition, "size");
                    if (s != null) {
                        size = Integer.parseInt(s);
                    }
                }
            }
            if (size == -1) {
                size = 0;
            }
            String storeData =
                Utility.combine(attachment.getHeader(
                        MimeHeader.HEADER_ANDROID_ATTACHMENT_STORE_DATA), ',');
            String name = MimeUtility.getHeaderParameter(attachment.getContentType(), "name");
            String contentId = attachment.getContentId();
            if (attachmentId == -1) {
                ContentValues cv = new ContentValues();
                cv.put("message_id", messageId);
                cv.put("content_uri", contentUri != null ? contentUri.toString() : null);
                cv.put("store_data", storeData);
                cv.put("size", size);
                cv.put("name", name);
                cv.put("mime_type", attachment.getMimeType());
                cv.put("content_id", contentId);
                attachmentId = mDb.insert("attachments", "message_id", cv);
            }
            else {
                ContentValues cv = new ContentValues();
                cv.put("content_uri", contentUri != null ? contentUri.toString() : null);
                cv.put("size", size);
                cv.put("content_id", contentId);
                cv.put("message_id", messageId);
                mDb.update(
                        "attachments",
                        cv,
                        "id = ?",
                        new String[] { Long.toString(attachmentId) });
            }
            if (tempAttachmentFile != null) {
                File attachmentFile = new File(mAttachmentsDir, Long.toString(attachmentId));
                tempAttachmentFile.renameTo(attachmentFile);
                attachment.setBody(new LocalAttachmentBody(contentUri, mContext));
                ContentValues cv = new ContentValues();
                cv.put("content_uri", contentUri != null ? contentUri.toString() : null);
                mDb.update(
                        "attachments",
                        cv,
                        "id = ?",
                        new String[] { Long.toString(attachmentId) });
            }
            if (attachment instanceof LocalAttachmentBodyPart) {
                ((LocalAttachmentBodyPart) attachment).setAttachmentId(attachmentId);
            }
        }
        public void changeUid(LocalMessage message) throws MessagingException {
            open(OpenMode.READ_WRITE);
            ContentValues cv = new ContentValues();
            cv.put("uid", message.getUid());
            mDb.update("messages", cv, "id = ?", new String[] { Long.toString(message.mId) });
        }
        @Override
        public void setFlags(Message[] messages, Flag[] flags, boolean value)
                throws MessagingException {
            open(OpenMode.READ_WRITE);
            for (Message message : messages) {
                message.setFlags(flags, value);
            }
        }
        @Override
        public Message[] expunge() throws MessagingException {
            open(OpenMode.READ_WRITE);
            ArrayList<Message> expungedMessages = new ArrayList<Message>();
            return expungedMessages.toArray(new Message[] {});
        }
        @Override
        public void delete(boolean recurse) throws MessagingException {
            open(OpenMode.READ_ONLY);
            Message[] messages = getMessages(null);
            for (Message message : messages) {
                deleteAttachments(message.getUid());
            }
            mDb.execSQL("DELETE FROM folders WHERE id = ?", new Object[] {
                Long.toString(mFolderId),
            });
        }
        @Override
        public boolean equals(Object o) {
            if (o instanceof LocalFolder) {
                return ((LocalFolder)o).mName.equals(mName);
            }
            return super.equals(o);
        }
        @Override
        public Flag[] getPermanentFlags() throws MessagingException {
            return PERMANENT_FLAGS;
        }
        private void deleteAttachments(String uid) throws MessagingException {
            open(OpenMode.READ_WRITE);
            Cursor messagesCursor = null;
            try {
                messagesCursor = mDb.query(
                        "messages",
                        new String[] { "id" },
                        "folder_id = ? AND uid = ?",
                        new String[] { Long.toString(mFolderId), uid },
                        null,
                        null,
                        null);
                while (messagesCursor.moveToNext()) {
                    long messageId = messagesCursor.getLong(0);
                    Cursor attachmentsCursor = null;
                    try {
                        attachmentsCursor = mDb.query(
                                "attachments",
                                new String[] { "id" },
                                "message_id = ?",
                                new String[] { Long.toString(messageId) },
                                null,
                                null,
                                null);
                        while (attachmentsCursor.moveToNext()) {
                            long attachmentId = attachmentsCursor.getLong(0);
                            try{
                                File file = new File(mAttachmentsDir, Long.toString(attachmentId));
                                if (file.exists()) {
                                    file.delete();
                                }
                            }
                            catch (Exception e) {
                            }
                        }
                    }
                    finally {
                        if (attachmentsCursor != null) {
                            attachmentsCursor.close();
                        }
                    }
                }
            }
            finally {
                if (messagesCursor != null) {
                    messagesCursor.close();
                }
            }
        }
        public Folder.PersistentDataCallbacks getPersistentCallbacks() throws MessagingException {
            open(OpenMode.READ_WRITE);
            return this;
        }
        public String getPersistentString(String key, String defaultValue) {
            return LocalStore.this.getPersistentString(mFolderId, key, defaultValue);
        }
        public void setPersistentString(String key, String value) {
            LocalStore.this.setPersistentString(mFolderId, key, value);
        }
        public void setPersistentStringAndMessageFlags(String key, String value,
                Flag[] setFlags, Flag[] clearFlags) throws MessagingException {
            mDb.beginTransaction();
            try {
                if (key != null) {
                    setPersistentString(key, value);
                }
                ContentValues cv = new ContentValues();
                if (setFlags != null) {
                    for (Flag flag : setFlags) {
                        if (flag == Flag.X_STORE_1) {
                            cv.put("store_flag_1", 1);
                        } else if (flag == Flag.X_STORE_2) {
                            cv.put("store_flag_2", 1);
                        } else if (flag == Flag.X_DOWNLOADED_FULL) {
                            cv.put("flag_downloaded_full", 1);
                        } else if (flag == Flag.X_DOWNLOADED_PARTIAL) {
                            cv.put("flag_downloaded_partial", 1);
                        } else {
                            throw new MessagingException("Unsupported flag " + flag);
                        }
                    }
                }
                if (clearFlags != null) {
                    for (Flag flag : clearFlags) {
                        if (flag == Flag.X_STORE_1) {
                            cv.put("store_flag_1", 0);
                        } else if (flag == Flag.X_STORE_2) {
                            cv.put("store_flag_2", 0);
                        } else if (flag == Flag.X_DOWNLOADED_FULL) {
                            cv.put("flag_downloaded_full", 0);
                        } else if (flag == Flag.X_DOWNLOADED_PARTIAL) {
                            cv.put("flag_downloaded_partial", 0);
                        } else {
                            throw new MessagingException("Unsupported flag " + flag);
                        }
                    }
                }
                mDb.update("messages", cv,
                        "folder_id = ?", new String[] { Long.toString(mFolderId) });
                mDb.setTransactionSuccessful();
            } finally {
                mDb.endTransaction();
            }
        }
        @Override
        public Message createMessage(String uid) throws MessagingException {
            return new LocalMessage(uid, this);
        }
    }
    public class LocalMessage extends MimeMessage {
        private long mId;
        private int mAttachmentCount;
        LocalMessage(String uid, Folder folder) throws MessagingException {
            this.mUid = uid;
            this.mFolder = folder;
        }
        public int getAttachmentCount() {
            return mAttachmentCount;
        }
        public void parse(InputStream in) throws IOException, MessagingException {
            super.parse(in);
        }
        public void setFlagInternal(Flag flag, boolean set) throws MessagingException {
            super.setFlag(flag, set);
        }
        public long getId() {
            return mId;
        }
        @Override
        public void setFlag(Flag flag, boolean set) throws MessagingException {
            if (flag == Flag.DELETED && set) {
                mDb.execSQL(
                        "UPDATE messages SET " +
                        "subject = NULL, " +
                        "sender_list = NULL, " +
                        "date = NULL, " +
                        "to_list = NULL, " +
                        "cc_list = NULL, " +
                        "bcc_list = NULL, " +
                        "html_content = NULL, " +
                        "text_content = NULL, " +
                        "reply_to_list = NULL " +
                        "WHERE id = ?",
                        new Object[] {
                                mId
                        });
                ((LocalFolder) mFolder).deleteAttachments(getUid());
                mDb.execSQL("DELETE FROM attachments WHERE id = ?",
                        new Object[] {
                                mId
                        });
            }
            else if (flag == Flag.X_DESTROYED && set) {
                ((LocalFolder) mFolder).deleteAttachments(getUid());
                mDb.execSQL("DELETE FROM messages WHERE id = ?",
                        new Object[] { mId });
            }
            try {
                if (flag == Flag.DELETED || flag == Flag.X_DESTROYED || flag == Flag.SEEN) {
                    LocalFolder folder = (LocalFolder)mFolder;
                    if (set && !isSet(Flag.SEEN)) {
                        folder.setUnreadMessageCount(folder.getUnreadMessageCount() - 1);
                    }
                    else if (!set && isSet(Flag.SEEN)) {
                        folder.setUnreadMessageCount(folder.getUnreadMessageCount() + 1);
                    }
                }
            }
            catch (MessagingException me) {
                Log.e(Email.LOG_TAG, "Unable to update LocalStore unread message count",
                        me);
                throw new RuntimeException(me);
            }
            super.setFlag(flag, set);
            mDb.execSQL("UPDATE messages "
                    + "SET flags = ?, store_flag_1 = ?, store_flag_2 = ?, "
                    + "flag_downloaded_full = ?, flag_downloaded_partial = ?, flag_deleted = ? "
                    + "WHERE id = ?",
                    new Object[] {
                            makeFlagsString(this),
                            makeFlagNumeric(this, Flag.X_STORE_1),
                            makeFlagNumeric(this, Flag.X_STORE_2),
                            makeFlagNumeric(this, Flag.X_DOWNLOADED_FULL),
                            makeFlagNumeric(this, Flag.X_DOWNLOADED_PARTIAL),
                            makeFlagNumeric(this, Flag.DELETED),
                            mId
            });
        }
    }
     String makeFlagsString(Message message) {
        StringBuilder sb = null;
        boolean nonEmpty = false;
        for (Flag flag : Flag.values()) {
            if (flag != Flag.X_STORE_1 && flag != Flag.X_STORE_2 &&
                    flag != Flag.X_DOWNLOADED_FULL && flag != Flag.X_DOWNLOADED_PARTIAL &&
                    flag != Flag.DELETED &&
                    message.isSet(flag)) {
                if (sb == null) {
                    sb = new StringBuilder();
                }
                if (nonEmpty) {
                    sb.append(',');
                }
                sb.append(flag.toString());
                nonEmpty = true;
            }
        }
        return (sb == null) ? null : sb.toString();
    }
     int makeFlagNumeric(Message message, Flag flag) {
        return message.isSet(flag) ? 1 : 0;
    }
    public class LocalAttachmentBodyPart extends MimeBodyPart {
        private long mAttachmentId = -1;
        public LocalAttachmentBodyPart(Body body, long attachmentId) throws MessagingException {
            super(body);
            mAttachmentId = attachmentId;
        }
        public long getAttachmentId() {
            return mAttachmentId;
        }
        public void setAttachmentId(long attachmentId) {
            mAttachmentId = attachmentId;
        }
        public String toString() {
            return "" + mAttachmentId;
        }
    }
    public static class LocalAttachmentBody implements Body {
        private Context mContext;
        private Uri mUri;
        public LocalAttachmentBody(Uri uri, Context context) {
            mContext = context;
            mUri = uri;
        }
        public InputStream getInputStream() throws MessagingException {
            try {
                return mContext.getContentResolver().openInputStream(mUri);
            }
            catch (FileNotFoundException fnfe) {
                return new ByteArrayInputStream(new byte[0]);
            }
            catch (IOException ioe) {
                throw new MessagingException("Invalid attachment.", ioe);
            }
        }
        public void writeTo(OutputStream out) throws IOException, MessagingException {
            InputStream in = getInputStream();
            Base64OutputStream base64Out = new Base64OutputStream(
                out, Base64.CRLF | Base64.NO_CLOSE);
            IOUtils.copy(in, base64Out);
            base64Out.close();
        }
        public Uri getContentUri() {
            return mUri;
        }
    }
    @Override
    public Class<? extends android.app.Activity> getSettingActivityClass() {
        return null;
    }
}
