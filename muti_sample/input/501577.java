public class MessagingController implements Runnable {
    private static final int MAX_SMALL_MESSAGE_SIZE = (25 * 1024);
    private static Flag[] FLAG_LIST_SEEN = new Flag[] { Flag.SEEN };
    private static Flag[] FLAG_LIST_FLAGGED = new Flag[] { Flag.FLAGGED };
    private static final String LOCAL_SERVERID_PREFIX = "Local-";
    private static String[] PRUNE_ATTACHMENT_PROJECTION = new String[] {
        AttachmentColumns.LOCATION
    };
    private static ContentValues PRUNE_ATTACHMENT_CV = new ContentValues();
    static {
        PRUNE_ATTACHMENT_CV.putNull(AttachmentColumns.CONTENT_URI);
    }
    private static MessagingController inst = null;
    private BlockingQueue<Command> mCommands = new LinkedBlockingQueue<Command>();
    private Thread mThread;
    private GroupMessagingListener mListeners = new GroupMessagingListener();
    private boolean mBusy;
    private Context mContext;
    protected MessagingController(Context _context) {
        mContext = _context;
        mThread = new Thread(this);
        mThread.start();
    }
    public synchronized static MessagingController getInstance(Context _context) {
        if (inst == null) {
            inst = new MessagingController(_context);
        }
        return inst;
    }
    public static void injectMockController(MessagingController mockController) {
        inst = mockController;
    }
    public boolean isBusy() {
        return mBusy;
    }
    public void run() {
        Process.setThreadPriority(Process.THREAD_PRIORITY_BACKGROUND);
        while (true) {
            Command command;
            try {
                command = mCommands.take();
            } catch (InterruptedException e) {
                continue; 
            }
            if (command.listener == null || isActiveListener(command.listener)) {
                mBusy = true;
                command.runnable.run();
                mListeners.controllerCommandCompleted(mCommands.size() > 0);
            }
            mBusy = false;
        }
    }
    private void put(String description, MessagingListener listener, Runnable runnable) {
        try {
            Command command = new Command();
            command.listener = listener;
            command.runnable = runnable;
            command.description = description;
            mCommands.add(command);
        }
        catch (IllegalStateException ie) {
            throw new Error(ie);
        }
    }
    public void addListener(MessagingListener listener) {
        mListeners.addListener(listener);
    }
    public void removeListener(MessagingListener listener) {
        mListeners.removeListener(listener);
    }
    private boolean isActiveListener(MessagingListener listener) {
        return mListeners.isActiveListener(listener);
    }
    private static class LocalMailboxInfo {
        private static final int COLUMN_ID = 0;
        private static final int COLUMN_DISPLAY_NAME = 1;
        private static final int COLUMN_ACCOUNT_KEY = 2;
        private static final int COLUMN_TYPE = 3;
        private static final String[] PROJECTION = new String[] {
            EmailContent.RECORD_ID,
            MailboxColumns.DISPLAY_NAME, MailboxColumns.ACCOUNT_KEY, MailboxColumns.TYPE,
        };
        long mId;
        String mDisplayName;
        long mAccountKey;
        int mType;
        public LocalMailboxInfo(Cursor c) {
            mId = c.getLong(COLUMN_ID);
            mDisplayName = c.getString(COLUMN_DISPLAY_NAME);
            mAccountKey = c.getLong(COLUMN_ACCOUNT_KEY);
            mType = c.getInt(COLUMN_TYPE);
        }
    }
    public void listFolders(final long accountId, MessagingListener listener) {
        final EmailContent.Account account =
                EmailContent.Account.restoreAccountWithId(mContext, accountId);
        if (account == null) {
            return;
        }
        mListeners.listFoldersStarted(accountId);
        put("listFolders", listener, new Runnable() {
            public void run() {
                Cursor localFolderCursor = null;
                try {
                    Store store = Store.getInstance(account.getStoreUri(mContext), mContext, null);
                    Folder[] remoteFolders = store.getPersonalNamespaces();
                    HashSet<String> remoteFolderNames = new HashSet<String>();
                    for (int i = 0, count = remoteFolders.length; i < count; i++) {
                        remoteFolderNames.add(remoteFolders[i].getName());
                    }
                    HashMap<String, LocalMailboxInfo> localFolders =
                        new HashMap<String, LocalMailboxInfo>();
                    HashSet<String> localFolderNames = new HashSet<String>();
                    localFolderCursor = mContext.getContentResolver().query(
                            EmailContent.Mailbox.CONTENT_URI,
                            LocalMailboxInfo.PROJECTION,
                            EmailContent.MailboxColumns.ACCOUNT_KEY + "=?",
                            new String[] { String.valueOf(account.mId) },
                            null);
                    while (localFolderCursor.moveToNext()) {
                        LocalMailboxInfo info = new LocalMailboxInfo(localFolderCursor);
                        localFolders.put(info.mDisplayName, info);
                        localFolderNames.add(info.mDisplayName);
                    }
                    if (!remoteFolderNames.equals(localFolderNames)) {
                        HashSet<String> localsToDrop = new HashSet<String>(localFolderNames);
                        localsToDrop.removeAll(remoteFolderNames);
                        for (String localNameToDrop : localsToDrop) {
                            LocalMailboxInfo localInfo = localFolders.get(localNameToDrop);
                            switch (localInfo.mType) {
                                case Mailbox.TYPE_INBOX:
                                case Mailbox.TYPE_DRAFTS:
                                case Mailbox.TYPE_OUTBOX:
                                case Mailbox.TYPE_SENT:
                                case Mailbox.TYPE_TRASH:
                                    break;
                                default:
                                    AttachmentProvider.deleteAllMailboxAttachmentFiles(
                                            mContext, accountId, localInfo.mId);
                                    Uri uri = ContentUris.withAppendedId(
                                            EmailContent.Mailbox.CONTENT_URI, localInfo.mId);
                                    mContext.getContentResolver().delete(uri, null, null);
                                    break;
                            }
                        }
                        remoteFolderNames.removeAll(localFolderNames);
                        for (String remoteNameToAdd : remoteFolderNames) {
                            EmailContent.Mailbox box = new EmailContent.Mailbox();
                            box.mDisplayName = remoteNameToAdd;
                            box.mAccountKey = account.mId;
                            box.mType = LegacyConversions.inferMailboxTypeFromName(
                                    mContext, remoteNameToAdd);
                            box.mFlagVisible = true;
                            box.mVisibleLimit = Email.VISIBLE_LIMIT_DEFAULT;
                            box.save(mContext);
                        }
                    }
                    mListeners.listFoldersFinished(accountId);
                } catch (Exception e) {
                    mListeners.listFoldersFailed(accountId, "");
                } finally {
                    if (localFolderCursor != null) {
                        localFolderCursor.close();
                    }
                }
            }
        });
    }
    public void synchronizeMailbox(final EmailContent.Account account,
            final EmailContent.Mailbox folder, MessagingListener listener) {
        if (folder.mType == EmailContent.Mailbox.TYPE_OUTBOX) {
            return;
        }
        mListeners.synchronizeMailboxStarted(account.mId, folder.mId);
        put("synchronizeMailbox", listener, new Runnable() {
            public void run() {
                synchronizeMailboxSynchronous(account, folder);
            }
        });
    }
    private void synchronizeMailboxSynchronous(final EmailContent.Account account,
            final EmailContent.Mailbox folder) {
        mListeners.synchronizeMailboxStarted(account.mId, folder.mId);
        try {
            processPendingActionsSynchronous(account);
            StoreSynchronizer.SyncResults results;
            Store remoteStore = Store.getInstance(account.getStoreUri(mContext), mContext, null);
            StoreSynchronizer customSync = remoteStore.getMessageSynchronizer();
            if (customSync == null) {
                results = synchronizeMailboxGeneric(account, folder);
            } else {
                results = customSync.SynchronizeMessagesSynchronous(
                        account, folder, mListeners, mContext);
            }
            mListeners.synchronizeMailboxFinished(account.mId, folder.mId,
                                                  results.mTotalMessages,
                                                  results.mNewMessages);
        } catch (MessagingException e) {
            if (Email.LOGD) {
                Log.v(Email.LOG_TAG, "synchronizeMailbox", e);
            }
            mListeners.synchronizeMailboxFailed(account.mId, folder.mId, e);
        }
    }
    private static class LocalMessageInfo {
        private static final int COLUMN_ID = 0;
        private static final int COLUMN_FLAG_READ = 1;
        private static final int COLUMN_FLAG_FAVORITE = 2;
        private static final int COLUMN_FLAG_LOADED = 3;
        private static final int COLUMN_SERVER_ID = 4;
        private static final String[] PROJECTION = new String[] {
            EmailContent.RECORD_ID,
            MessageColumns.FLAG_READ, MessageColumns.FLAG_FAVORITE, MessageColumns.FLAG_LOADED,
            SyncColumns.SERVER_ID, MessageColumns.MAILBOX_KEY, MessageColumns.ACCOUNT_KEY
        };
        int mCursorIndex;
        long mId;
        boolean mFlagRead;
        boolean mFlagFavorite;
        int mFlagLoaded;
        String mServerId;
        public LocalMessageInfo(Cursor c) {
            mCursorIndex = c.getPosition();
            mId = c.getLong(COLUMN_ID);
            mFlagRead = c.getInt(COLUMN_FLAG_READ) != 0;
            mFlagFavorite = c.getInt(COLUMN_FLAG_FAVORITE) != 0;
            mFlagLoaded = c.getInt(COLUMN_FLAG_LOADED);
            mServerId = c.getString(COLUMN_SERVER_ID);
        }
    }
    private void saveOrUpdate(EmailContent content) {
        if (content.isSaved()) {
            content.update(mContext, content.toContentValues());
        } else {
            content.save(mContext);
        }
    }
    private StoreSynchronizer.SyncResults synchronizeMailboxGeneric(
            final EmailContent.Account account, final EmailContent.Mailbox folder)
            throws MessagingException {
        Log.d(Email.LOG_TAG, "*** synchronizeMailboxGeneric ***");
        ContentResolver resolver = mContext.getContentResolver();
        if (folder.mType == Mailbox.TYPE_DRAFTS || folder.mType == Mailbox.TYPE_OUTBOX) {
            int totalMessages = EmailContent.count(mContext, folder.getUri(), null, null);
            return new StoreSynchronizer.SyncResults(totalMessages, 0);
        }
        Cursor localUidCursor = null;
        HashMap<String, LocalMessageInfo> localMessageMap = new HashMap<String, LocalMessageInfo>();
        try {
            localUidCursor = resolver.query(
                    EmailContent.Message.CONTENT_URI,
                    LocalMessageInfo.PROJECTION,
                    EmailContent.MessageColumns.ACCOUNT_KEY + "=?" +
                    " AND " + MessageColumns.MAILBOX_KEY + "=?",
                    new String[] {
                            String.valueOf(account.mId),
                            String.valueOf(folder.mId)
                    },
                    null);
            while (localUidCursor.moveToNext()) {
                LocalMessageInfo info = new LocalMessageInfo(localUidCursor);
                localMessageMap.put(info.mServerId, info);
            }
        } finally {
            if (localUidCursor != null) {
                localUidCursor.close();
            }
        }
        int localUnreadCount = EmailContent.count(mContext, EmailContent.Message.CONTENT_URI,
                EmailContent.MessageColumns.ACCOUNT_KEY + "=?" +
                " AND " + MessageColumns.MAILBOX_KEY + "=?" +
                " AND " + MessageColumns.FLAG_READ + "=0",
                new String[] {
                        String.valueOf(account.mId),
                        String.valueOf(folder.mId)
                });
        Store remoteStore = Store.getInstance(account.getStoreUri(mContext), mContext, null);
        Folder remoteFolder = remoteStore.getFolder(folder.mDisplayName);
        if (folder.mType == Mailbox.TYPE_TRASH || folder.mType == Mailbox.TYPE_SENT
                || folder.mType == Mailbox.TYPE_DRAFTS) {
            if (!remoteFolder.exists()) {
                if (!remoteFolder.create(FolderType.HOLDS_MESSAGES)) {
                    return new StoreSynchronizer.SyncResults(0, 0);
                }
            }
        }
        remoteFolder.open(OpenMode.READ_WRITE, null);
        int remoteMessageCount = remoteFolder.getMessageCount();
        int visibleLimit = folder.mVisibleLimit;
        if (visibleLimit <= 0) {
            Store.StoreInfo info = Store.StoreInfo.getStoreInfo(account.getStoreUri(mContext),
                    mContext);
            visibleLimit = info.mVisibleLimitDefault;
        }
        Message[] remoteMessages = new Message[0];
        final ArrayList<Message> unsyncedMessages = new ArrayList<Message>();
        HashMap<String, Message> remoteUidMap = new HashMap<String, Message>();
        int newMessageCount = 0;
        if (remoteMessageCount > 0) {
            int remoteStart = Math.max(0, remoteMessageCount - visibleLimit) + 1;
            int remoteEnd = remoteMessageCount;
            remoteMessages = remoteFolder.getMessages(remoteStart, remoteEnd, null);
            for (Message message : remoteMessages) {
                remoteUidMap.put(message.getUid(), message);
            }
            for (Message message : remoteMessages) {
                LocalMessageInfo localMessage = localMessageMap.get(message.getUid());
                if (localMessage == null) {
                    newMessageCount++;
                }
                if (localMessage == null
                        || (localMessage.mFlagLoaded == EmailContent.Message.FLAG_LOADED_UNLOADED)
                        || (localMessage.mFlagLoaded == EmailContent.Message.FLAG_LOADED_PARTIAL)) {
                    unsyncedMessages.add(message);
                }
            }
        }
        final ArrayList<Message> newMessages = new ArrayList<Message>();
        if (unsyncedMessages.size() > 0) {
            FetchProfile fp = new FetchProfile();
            fp.add(FetchProfile.Item.FLAGS);
            fp.add(FetchProfile.Item.ENVELOPE);
            final HashMap<String, LocalMessageInfo> localMapCopy =
                new HashMap<String, LocalMessageInfo>(localMessageMap);
            remoteFolder.fetch(unsyncedMessages.toArray(new Message[0]), fp,
                    new MessageRetrievalListener() {
                        public void messageFinished(Message message, int number, int ofTotal) {
                            try {
                                LocalMessageInfo localMessageInfo =
                                    localMapCopy.get(message.getUid());
                                EmailContent.Message localMessage = null;
                                if (localMessageInfo == null) {
                                    localMessage = new EmailContent.Message();
                                } else {
                                    localMessage = EmailContent.Message.restoreMessageWithId(
                                            mContext, localMessageInfo.mId);
                                }
                                if (localMessage != null) {
                                    try {
                                        LegacyConversions.updateMessageFields(localMessage,
                                                message, account.mId, folder.mId);
                                        saveOrUpdate(localMessage);
                                        if (!message.isSet(Flag.SEEN)) {
                                            newMessages.add(message);
                                        }
                                    } catch (MessagingException me) {
                                        Log.e(Email.LOG_TAG,
                                                "Error while copying downloaded message." + me);
                                    }
                                }
                            }
                            catch (Exception e) {
                                Log.e(Email.LOG_TAG,
                                        "Error while storing downloaded message." + e.toString());
                            }
                        }
                        public void messageStarted(String uid, int number, int ofTotal) {
                        }
                    });
        }
        FetchProfile fp = new FetchProfile();
        fp.add(FetchProfile.Item.FLAGS);
        remoteFolder.fetch(remoteMessages, fp, null);
        boolean remoteSupportsSeen = false;
        boolean remoteSupportsFlagged = false;
        for (Flag flag : remoteFolder.getPermanentFlags()) {
            if (flag == Flag.SEEN) {
                remoteSupportsSeen = true;
            }
            if (flag == Flag.FLAGGED) {
                remoteSupportsFlagged = true;
            }
        }
        if (remoteSupportsSeen || remoteSupportsFlagged) {
            for (Message remoteMessage : remoteMessages) {
                LocalMessageInfo localMessageInfo = localMessageMap.get(remoteMessage.getUid());
                if (localMessageInfo == null) {
                    continue;
                }
                boolean localSeen = localMessageInfo.mFlagRead;
                boolean remoteSeen = remoteMessage.isSet(Flag.SEEN);
                boolean newSeen = (remoteSupportsSeen && (remoteSeen != localSeen));
                boolean localFlagged = localMessageInfo.mFlagFavorite;
                boolean remoteFlagged = remoteMessage.isSet(Flag.FLAGGED);
                boolean newFlagged = (remoteSupportsFlagged && (localFlagged != remoteFlagged));
                if (newSeen || newFlagged) {
                    Uri uri = ContentUris.withAppendedId(
                            EmailContent.Message.CONTENT_URI, localMessageInfo.mId);
                    ContentValues updateValues = new ContentValues();
                    updateValues.put(EmailContent.Message.FLAG_READ, remoteSeen);
                    updateValues.put(EmailContent.Message.FLAG_FAVORITE, remoteFlagged);
                    resolver.update(uri, updateValues, null, null);
                }
            }
        }
        HashSet<String> localUidsToDelete = new HashSet<String>(localMessageMap.keySet());
        localUidsToDelete.removeAll(remoteUidMap.keySet());
        for (String uidToDelete : localUidsToDelete) {
            LocalMessageInfo infoToDelete = localMessageMap.get(uidToDelete);
            AttachmentProvider.deleteAllAttachmentFiles(mContext, account.mId, infoToDelete.mId);
            Uri uriToDelete = ContentUris.withAppendedId(
                    EmailContent.Message.CONTENT_URI, infoToDelete.mId);
            resolver.delete(uriToDelete, null, null);
            Uri syncRowToDelete = ContentUris.withAppendedId(
                    EmailContent.Message.UPDATED_CONTENT_URI, infoToDelete.mId);
            resolver.delete(syncRowToDelete, null, null);
            Uri deletERowToDelete = ContentUris.withAppendedId(
                    EmailContent.Message.UPDATED_CONTENT_URI, infoToDelete.mId);
            resolver.delete(deletERowToDelete, null, null);
        }
        ArrayList<Message> largeMessages = new ArrayList<Message>();
        ArrayList<Message> smallMessages = new ArrayList<Message>();
        for (Message message : unsyncedMessages) {
            if (message.getSize() > (MAX_SMALL_MESSAGE_SIZE)) {
                largeMessages.add(message);
            } else {
                smallMessages.add(message);
            }
        }
        fp = new FetchProfile();
        fp.add(FetchProfile.Item.BODY);
        remoteFolder.fetch(smallMessages.toArray(new Message[smallMessages.size()]), fp,
                new MessageRetrievalListener() {
                    public void messageFinished(Message message, int number, int ofTotal) {
                        copyOneMessageToProvider(message, account, folder,
                                EmailContent.Message.FLAG_LOADED_COMPLETE);
                    }
                    public void messageStarted(String uid, int number, int ofTotal) {
                    }
        });
        fp.clear();
        fp.add(FetchProfile.Item.STRUCTURE);
        remoteFolder.fetch(largeMessages.toArray(new Message[largeMessages.size()]), fp, null);
        for (Message message : largeMessages) {
            if (message.getBody() == null) {
                fp.clear();
                fp.add(FetchProfile.Item.BODY_SANE);
                remoteFolder.fetch(new Message[] { message }, fp, null);
                copyOneMessageToProvider(message, account, folder,
                        EmailContent.Message.FLAG_LOADED_PARTIAL);
            } else {
                ArrayList<Part> viewables = new ArrayList<Part>();
                ArrayList<Part> attachments = new ArrayList<Part>();
                MimeUtility.collectParts(message, viewables, attachments);
                for (Part part : viewables) {
                    fp.clear();
                    fp.add(part);
                    remoteFolder.fetch(new Message[] { message }, fp, null);
                }
                copyOneMessageToProvider(message, account, folder,
                        EmailContent.Message.FLAG_LOADED_COMPLETE);
            }
        }
        remoteFolder.close(false);
        if (false) {
        fp.clear();
        fp.add(FetchProfile.Item.STRUCTURE);
        remoteFolder.fetch(largeMessages.toArray(new Message[largeMessages.size()]),
                fp, null);
        for (Message message : largeMessages) {
            if (message.getBody() == null) {
                fp.clear();
                fp.add(FetchProfile.Item.BODY_SANE);
                remoteFolder.fetch(new Message[] { message }, fp, null);
            } else {
                ArrayList<Part> viewables = new ArrayList<Part>();
                ArrayList<Part> attachments = new ArrayList<Part>();
                MimeUtility.collectParts(message, viewables, attachments);
                for (Part part : viewables) {
                    fp.clear();
                    fp.add(part);
                    remoteFolder.fetch(new Message[] { message }, fp, null);
                }
            }
        }
        StoreSynchronizer.SyncResults results = new StoreSynchronizer.SyncResults(
                remoteFolder.getMessageCount(), newMessages.size());
        remoteFolder.close(false);
        return results;
        }
        return new StoreSynchronizer.SyncResults(remoteMessageCount, newMessages.size());
    }
    private void copyOneMessageToProvider(Message message, EmailContent.Account account,
            EmailContent.Mailbox folder, int loadStatus) {
        try {
            EmailContent.Message localMessage = null;
            Cursor c = null;
            try {
                c = mContext.getContentResolver().query(
                        EmailContent.Message.CONTENT_URI,
                        EmailContent.Message.CONTENT_PROJECTION,
                        EmailContent.MessageColumns.ACCOUNT_KEY + "=?" +
                        " AND " + MessageColumns.MAILBOX_KEY + "=?" +
                        " AND " + SyncColumns.SERVER_ID + "=?",
                        new String[] {
                                String.valueOf(account.mId),
                                String.valueOf(folder.mId),
                                String.valueOf(message.getUid())
                        },
                        null);
                if (c.moveToNext()) {
                    localMessage = EmailContent.getContent(c, EmailContent.Message.class);
                }
            } finally {
                if (c != null) {
                    c.close();
                }
            }
            if (localMessage == null) {
                Log.d(Email.LOG_TAG, "Could not retrieve message from db, UUID="
                        + message.getUid());
                return;
            }
            EmailContent.Body body = EmailContent.Body.restoreBodyWithMessageId(mContext,
                    localMessage.mId);
            if (body == null) {
                body = new EmailContent.Body();
            }
            try {
                LegacyConversions.updateMessageFields(localMessage, message, account.mId,
                        folder.mId);
                ArrayList<Part> viewables = new ArrayList<Part>();
                ArrayList<Part> attachments = new ArrayList<Part>();
                MimeUtility.collectParts(message, viewables, attachments);
                LegacyConversions.updateBodyFields(body, localMessage, viewables);
                saveOrUpdate(localMessage);
                saveOrUpdate(body);
                LegacyConversions.updateAttachments(mContext, localMessage,
                        attachments, false);
                localMessage.mFlagLoaded = loadStatus;
                ContentValues cv = new ContentValues();
                cv.put(EmailContent.MessageColumns.FLAG_ATTACHMENT, localMessage.mFlagAttachment);
                cv.put(EmailContent.MessageColumns.FLAG_LOADED, localMessage.mFlagLoaded);
                Uri uri = ContentUris.withAppendedId(EmailContent.Message.CONTENT_URI,
                        localMessage.mId);
                mContext.getContentResolver().update(uri, cv, null, null);
            } catch (MessagingException me) {
                Log.e(Email.LOG_TAG, "Error while copying downloaded message." + me);
            }
        } catch (RuntimeException rte) {
            Log.e(Email.LOG_TAG, "Error while storing downloaded message." + rte.toString());
        } catch (IOException ioe) {
            Log.e(Email.LOG_TAG, "Error while storing attachment." + ioe.toString());
        }
    }
    public void processPendingActions(final long accountId) {
        put("processPendingActions", null, new Runnable() {
            public void run() {
                try {
                    EmailContent.Account account =
                        EmailContent.Account.restoreAccountWithId(mContext, accountId);
                    if (account == null) {
                        return;
                    }
                    processPendingActionsSynchronous(account);
                }
                catch (MessagingException me) {
                    if (Email.LOGD) {
                        Log.v(Email.LOG_TAG, "processPendingActions", me);
                    }
                }
            }
        });
    }
    private void processPendingActionsSynchronous(EmailContent.Account account)
           throws MessagingException {
        ContentResolver resolver = mContext.getContentResolver();
        String[] accountIdArgs = new String[] { Long.toString(account.mId) };
        processPendingDeletesSynchronous(account, resolver, accountIdArgs);
        processPendingUploadsSynchronous(account, resolver, accountIdArgs);
        processPendingUpdatesSynchronous(account, resolver, accountIdArgs);
    }
    private void processPendingDeletesSynchronous(EmailContent.Account account,
            ContentResolver resolver, String[] accountIdArgs) {
        Cursor deletes = resolver.query(EmailContent.Message.DELETED_CONTENT_URI,
                EmailContent.Message.CONTENT_PROJECTION,
                EmailContent.MessageColumns.ACCOUNT_KEY + "=?", accountIdArgs,
                EmailContent.MessageColumns.MAILBOX_KEY);
        long lastMessageId = -1;
        try {
            Store remoteStore = null;
            Mailbox mailbox = null;
            while (deletes.moveToNext()) {
                boolean deleteFromTrash = false;
                EmailContent.Message oldMessage =
                    EmailContent.getContent(deletes, EmailContent.Message.class);
                lastMessageId = oldMessage.mId;
                if (oldMessage != null) {
                    if (mailbox == null || mailbox.mId != oldMessage.mMailboxKey) {
                        mailbox = Mailbox.restoreMailboxWithId(mContext, oldMessage.mMailboxKey);
                    }
                    deleteFromTrash = mailbox.mType == Mailbox.TYPE_TRASH;
                }
                if (remoteStore == null && deleteFromTrash) {
                    remoteStore = Store.getInstance(account.getStoreUri(mContext), mContext, null);
                }
                if (deleteFromTrash) {
                    processPendingDeleteFromTrash(remoteStore, account, mailbox, oldMessage);
                }
                Uri uri = ContentUris.withAppendedId(EmailContent.Message.DELETED_CONTENT_URI,
                        oldMessage.mId);
                resolver.delete(uri, null, null);
            }
        } catch (MessagingException me) {
            if (Email.DEBUG) {
                Log.d(Email.LOG_TAG, "Unable to process pending delete for id="
                            + lastMessageId + ": " + me);
            }
        } finally {
            deletes.close();
        }
    }
    private void processPendingUploadsSynchronous(EmailContent.Account account,
            ContentResolver resolver, String[] accountIdArgs) throws MessagingException {
        Cursor mailboxes = resolver.query(Mailbox.CONTENT_URI, Mailbox.ID_PROJECTION,
                MailboxColumns.ACCOUNT_KEY + "=?"
                + " and " + MailboxColumns.TYPE + "=" + Mailbox.TYPE_SENT,
                accountIdArgs, null);
        long lastMessageId = -1;
        try {
            Store remoteStore = null;
            while (mailboxes.moveToNext()) {
                long mailboxId = mailboxes.getLong(Mailbox.ID_PROJECTION_COLUMN);
                String[] mailboxKeyArgs = new String[] { Long.toString(mailboxId) };
                Mailbox mailbox = null;
                Cursor upsyncs1 = resolver.query(EmailContent.Message.CONTENT_URI,
                        EmailContent.Message.ID_PROJECTION,
                        EmailContent.Message.MAILBOX_KEY + "=?"
                        + " and (" + EmailContent.Message.SERVER_ID + " is null"
                        + " or " + EmailContent.Message.SERVER_ID + "=''" + ")",
                        mailboxKeyArgs,
                        null);
                try {
                    while (upsyncs1.moveToNext()) {
                        if (remoteStore == null) {
                            remoteStore =
                                Store.getInstance(account.getStoreUri(mContext), mContext, null);
                        }
                        if (mailbox == null) {
                            mailbox = Mailbox.restoreMailboxWithId(mContext, mailboxId);
                        }
                        long id = upsyncs1.getLong(EmailContent.Message.ID_PROJECTION_COLUMN);
                        lastMessageId = id;
                        processUploadMessage(resolver, remoteStore, account, mailbox, id);
                    }
                } finally {
                    if (upsyncs1 != null) {
                        upsyncs1.close();
                    }
                }
                Cursor upsyncs2 = resolver.query(EmailContent.Message.UPDATED_CONTENT_URI,
                        EmailContent.Message.ID_PROJECTION,
                        EmailContent.MessageColumns.MAILBOX_KEY + "=?", mailboxKeyArgs,
                        null);
                try {
                    while (upsyncs2.moveToNext()) {
                        if (remoteStore == null) {
                            remoteStore =
                                Store.getInstance(account.getStoreUri(mContext), mContext, null);
                        }
                        if (mailbox == null) {
                            mailbox = Mailbox.restoreMailboxWithId(mContext, mailboxId);
                        }
                        long id = upsyncs2.getLong(EmailContent.Message.ID_PROJECTION_COLUMN);
                        lastMessageId = id;
                        processUploadMessage(resolver, remoteStore, account, mailbox, id);
                    }
                } finally {
                    if (upsyncs2 != null) {
                        upsyncs2.close();
                    }
                }
            }
        } catch (MessagingException me) {
            if (Email.DEBUG) {
                Log.d(Email.LOG_TAG, "Unable to process pending upsync for id="
                        + lastMessageId + ": " + me);
            }
        } finally {
            if (mailboxes != null) {
                mailboxes.close();
            }
        }
    }
    private void processPendingUpdatesSynchronous(EmailContent.Account account,
            ContentResolver resolver, String[] accountIdArgs) {
        Cursor updates = resolver.query(EmailContent.Message.UPDATED_CONTENT_URI,
                EmailContent.Message.CONTENT_PROJECTION,
                EmailContent.MessageColumns.ACCOUNT_KEY + "=?", accountIdArgs,
                EmailContent.MessageColumns.MAILBOX_KEY);
        long lastMessageId = -1;
        try {
            Store remoteStore = null;
            Mailbox mailbox = null;
            while (updates.moveToNext()) {
                boolean changeMoveToTrash = false;
                boolean changeRead = false;
                boolean changeFlagged = false;
                EmailContent.Message oldMessage =
                    EmailContent.getContent(updates, EmailContent.Message.class);
                lastMessageId = oldMessage.mId;
                EmailContent.Message newMessage =
                    EmailContent.Message.restoreMessageWithId(mContext, oldMessage.mId);
                if (newMessage != null) {
                    if (mailbox == null || mailbox.mId != newMessage.mMailboxKey) {
                        mailbox = Mailbox.restoreMailboxWithId(mContext, newMessage.mMailboxKey);
                    }
                    changeMoveToTrash = (oldMessage.mMailboxKey != newMessage.mMailboxKey)
                            && (mailbox.mType == Mailbox.TYPE_TRASH);
                    changeRead = oldMessage.mFlagRead != newMessage.mFlagRead;
                    changeFlagged = oldMessage.mFlagFavorite != newMessage.mFlagFavorite;
                }
                if (remoteStore == null && (changeMoveToTrash || changeRead || changeFlagged)) {
                    remoteStore = Store.getInstance(account.getStoreUri(mContext), mContext, null);
                }
                if (changeMoveToTrash) {
                    processPendingMoveToTrash(remoteStore, account, mailbox, oldMessage,
                            newMessage);
                } else if (changeRead || changeFlagged) {
                    processPendingFlagChange(remoteStore, mailbox, changeRead, changeFlagged,
                            newMessage);
                }
                Uri uri = ContentUris.withAppendedId(EmailContent.Message.UPDATED_CONTENT_URI,
                        oldMessage.mId);
                resolver.delete(uri, null, null);
            }
        } catch (MessagingException me) {
            if (Email.DEBUG) {
                Log.d(Email.LOG_TAG, "Unable to process pending update for id="
                            + lastMessageId + ": " + me);
            }
        } finally {
            updates.close();
        }
    }
    private void processUploadMessage(ContentResolver resolver, Store remoteStore,
            EmailContent.Account account, Mailbox mailbox, long messageId)
            throws MessagingException {
        EmailContent.Message message =
            EmailContent.Message.restoreMessageWithId(mContext, messageId);
        boolean deleteUpdate = false;
        if (message == null) {
            deleteUpdate = true;
            Log.d(Email.LOG_TAG, "Upsync failed for null message, id=" + messageId);
        } else if (mailbox.mType == Mailbox.TYPE_DRAFTS) {
            deleteUpdate = false;
            Log.d(Email.LOG_TAG, "Upsync skipped for mailbox=drafts, id=" + messageId);
        } else if (mailbox.mType == Mailbox.TYPE_OUTBOX) {
            deleteUpdate = false;
            Log.d(Email.LOG_TAG, "Upsync skipped for mailbox=outbox, id=" + messageId);
        } else if (mailbox.mType == Mailbox.TYPE_TRASH) {
            deleteUpdate = false;
            Log.d(Email.LOG_TAG, "Upsync skipped for mailbox=trash, id=" + messageId);
        } else {
            Log.d(Email.LOG_TAG, "Upsyc triggered for message id=" + messageId);
            deleteUpdate = processPendingAppend(remoteStore, account, mailbox, message);
        }
        if (deleteUpdate) {
            Uri uri = ContentUris.withAppendedId(EmailContent.Message.UPDATED_CONTENT_URI, messageId);
            resolver.delete(uri, null, null);
        }
    }
    private void processPendingFlagChange(Store remoteStore, Mailbox mailbox, boolean changeRead,
            boolean changeFlagged, EmailContent.Message newMessage) throws MessagingException {
        if (newMessage.mServerId == null || newMessage.mServerId.equals("")
                || newMessage.mServerId.startsWith(LOCAL_SERVERID_PREFIX)) {
            return;
        }
        if (mailbox.mType == Mailbox.TYPE_DRAFTS || mailbox.mType == Mailbox.TYPE_OUTBOX) {
            return;
        }
        Folder remoteFolder = remoteStore.getFolder(mailbox.mDisplayName);
        if (!remoteFolder.exists()) {
            return;
        }
        remoteFolder.open(OpenMode.READ_WRITE, null);
        if (remoteFolder.getMode() != OpenMode.READ_WRITE) {
            return;
        }
        Message remoteMessage = remoteFolder.getMessage(newMessage.mServerId);
        if (remoteMessage == null) {
            return;
        }
        if (Email.DEBUG) {
            Log.d(Email.LOG_TAG,
                    "Update flags for msg id=" + newMessage.mId
                    + " read=" + newMessage.mFlagRead
                    + " flagged=" + newMessage.mFlagFavorite);
        }
        Message[] messages = new Message[] { remoteMessage };
        if (changeRead) {
            remoteFolder.setFlags(messages, FLAG_LIST_SEEN, newMessage.mFlagRead);
        }
        if (changeFlagged) {
            remoteFolder.setFlags(messages, FLAG_LIST_FLAGGED, newMessage.mFlagFavorite);
        }
    }
    private void processPendingMoveToTrash(Store remoteStore,
            EmailContent.Account account, Mailbox newMailbox, EmailContent.Message oldMessage,
            final EmailContent.Message newMessage) throws MessagingException {
        if (newMessage.mServerId == null || newMessage.mServerId.equals("")
                || newMessage.mServerId.startsWith(LOCAL_SERVERID_PREFIX)) {
            return;
        }
        Mailbox oldMailbox = Mailbox.restoreMailboxWithId(mContext, oldMessage.mMailboxKey);
        if (oldMailbox == null) {
            return;
        }
        if (oldMailbox.mType == Mailbox.TYPE_TRASH) {
            return;
        }
        if (account.getDeletePolicy() == Account.DELETE_POLICY_NEVER) {
            EmailContent.Message sentinel = new EmailContent.Message();
            sentinel.mAccountKey = oldMessage.mAccountKey;
            sentinel.mMailboxKey = oldMessage.mMailboxKey;
            sentinel.mFlagLoaded = EmailContent.Message.FLAG_LOADED_DELETED;
            sentinel.mFlagRead = true;
            sentinel.mServerId = oldMessage.mServerId;
            sentinel.save(mContext);
            return;
        }
        Folder remoteFolder = remoteStore.getFolder(oldMailbox.mDisplayName);
        if (!remoteFolder.exists()) {
            return;
        }
        remoteFolder.open(OpenMode.READ_WRITE, null);
        if (remoteFolder.getMode() != OpenMode.READ_WRITE) {
            remoteFolder.close(false);
            return;
        }
        Message remoteMessage = remoteFolder.getMessage(oldMessage.mServerId);
        if (remoteMessage == null) {
            remoteFolder.close(false);
            return;
        }
        Folder remoteTrashFolder = remoteStore.getFolder(newMailbox.mDisplayName);
        if (!remoteTrashFolder.exists()) {
            remoteTrashFolder.create(FolderType.HOLDS_MESSAGES);
        }
        if (remoteTrashFolder.exists()) {
            remoteTrashFolder.open(OpenMode.READ_WRITE, null);
            if (remoteTrashFolder.getMode() != OpenMode.READ_WRITE) {
                remoteFolder.close(false);
                remoteTrashFolder.close(false);
                return;
            }
            remoteFolder.copyMessages(new Message[] { remoteMessage }, remoteTrashFolder,
                    new Folder.MessageUpdateCallbacks() {
                public void onMessageUidChange(Message message, String newUid) {
                    ContentValues cv = new ContentValues();
                    cv.put(EmailContent.Message.SERVER_ID, newUid);
                    mContext.getContentResolver().update(newMessage.getUri(), cv, null, null);
                }
                public void onMessageNotFound(Message message) {
                    mContext.getContentResolver().delete(newMessage.getUri(), null, null);
                }
            }
            );
            remoteTrashFolder.close(false);
        }
        remoteMessage.setFlag(Flag.DELETED, true);
        remoteFolder.expunge();
        remoteFolder.close(false);
    }
    private void processPendingDeleteFromTrash(Store remoteStore,
            EmailContent.Account account, Mailbox oldMailbox, EmailContent.Message oldMessage)
            throws MessagingException {
        if (oldMailbox.mType != Mailbox.TYPE_TRASH) {
            return;
        }
        Folder remoteTrashFolder = remoteStore.getFolder(oldMailbox.mDisplayName);
        if (!remoteTrashFolder.exists()) {
            return;
        }
        remoteTrashFolder.open(OpenMode.READ_WRITE, null);
        if (remoteTrashFolder.getMode() != OpenMode.READ_WRITE) {
            remoteTrashFolder.close(false);
            return;
        }
        Message remoteMessage = remoteTrashFolder.getMessage(oldMessage.mServerId);
        if (remoteMessage == null) {
            remoteTrashFolder.close(false);
            return;
        }
        remoteMessage.setFlag(Flag.DELETED, true);
        remoteTrashFolder.expunge();
        remoteTrashFolder.close(false);
    }
    private boolean processPendingAppend(Store remoteStore, EmailContent.Account account,
            Mailbox newMailbox, EmailContent.Message message)
            throws MessagingException {
        boolean updateInternalDate = false;
        boolean updateMessage = false;
        boolean deleteMessage = false;
        Folder remoteFolder = remoteStore.getFolder(newMailbox.mDisplayName);
        if (!remoteFolder.exists()) {
            if (!remoteFolder.canCreate(FolderType.HOLDS_MESSAGES)) {
                if (message.mServerId == null || message.mServerId.length() == 0) {
                    message.mServerId = LOCAL_SERVERID_PREFIX + message.mId;
                    Uri uri =
                        ContentUris.withAppendedId(EmailContent.Message.CONTENT_URI, message.mId);
                    ContentValues cv = new ContentValues();
                    cv.put(EmailContent.Message.SERVER_ID, message.mServerId);
                    mContext.getContentResolver().update(uri, cv, null, null);
                }
                return true;
            }
            if (!remoteFolder.create(FolderType.HOLDS_MESSAGES)) {
                return false;
            }
        }
        remoteFolder.open(OpenMode.READ_WRITE, null);
        if (remoteFolder.getMode() != OpenMode.READ_WRITE) {
            return false;
        }
        Message remoteMessage = null;
        if (message.mServerId != null && message.mServerId.length() > 0) {
            remoteMessage = remoteFolder.getMessage(message.mServerId);
        }
        if (remoteMessage == null) {
            Message localMessage = LegacyConversions.makeMessage(mContext, message);
            FetchProfile fp = new FetchProfile();
            fp.add(FetchProfile.Item.BODY);
            remoteFolder.appendMessages(new Message[] { localMessage });
            message.mServerId = localMessage.getUid();
            updateInternalDate = true;
            updateMessage = true;
        } else {
            FetchProfile fp = new FetchProfile();
            fp.add(FetchProfile.Item.ENVELOPE);
            remoteFolder.fetch(new Message[] { remoteMessage }, fp, null);
            Date localDate = new Date(message.mServerTimeStamp);
            Date remoteDate = remoteMessage.getInternalDate();
            if (remoteDate.compareTo(localDate) > 0) {
                deleteMessage = true;
            } else {
                Message localMessage = LegacyConversions.makeMessage(mContext, message);
                fp.clear();
                fp = new FetchProfile();
                fp.add(FetchProfile.Item.BODY);
                remoteFolder.appendMessages(new Message[] { localMessage });
                message.mServerId = localMessage.getUid();
                updateInternalDate = true;
                updateMessage = true;
                remoteMessage.setFlag(Flag.DELETED, true);
            }
        }
        if (updateInternalDate && message.mServerId != null) {
            try {
                Message remoteMessage2 = remoteFolder.getMessage(message.mServerId);
                if (remoteMessage2 != null) {
                    FetchProfile fp2 = new FetchProfile();
                    fp2.add(FetchProfile.Item.ENVELOPE);
                    remoteFolder.fetch(new Message[] { remoteMessage2 }, fp2, null);
                    message.mServerTimeStamp = remoteMessage2.getInternalDate().getTime();
                    updateMessage = true;
                }
            } catch (MessagingException me) {
            }
        }
        if (deleteMessage || updateMessage) {
            Uri uri = ContentUris.withAppendedId(EmailContent.Message.CONTENT_URI, message.mId);
            ContentResolver resolver = mContext.getContentResolver();
            if (deleteMessage) {
                resolver.delete(uri, null, null);
            } else if (updateMessage) {
                ContentValues cv = new ContentValues();
                cv.put(EmailContent.Message.SERVER_ID, message.mServerId);
                cv.put(EmailContent.Message.SERVER_TIMESTAMP, message.mServerTimeStamp);
                resolver.update(uri, cv, null, null);
            }
        }
        return true;
    }
    public void loadMessageForView(final long messageId, MessagingListener listener) {
        mListeners.loadMessageForViewStarted(messageId);
        put("loadMessageForViewRemote", listener, new Runnable() {
            public void run() {
                try {
                    EmailContent.Message message =
                        EmailContent.Message.restoreMessageWithId(mContext, messageId);
                    if (message == null) {
                        mListeners.loadMessageForViewFailed(messageId, "Unknown message");
                        return;
                    }
                    if (message.mFlagLoaded == EmailContent.Message.FLAG_LOADED_COMPLETE) {
                        mListeners.loadMessageForViewFinished(messageId);
                        return;
                    }
                    EmailContent.Account account =
                        EmailContent.Account.restoreAccountWithId(mContext, message.mAccountKey);
                    EmailContent.Mailbox mailbox =
                        EmailContent.Mailbox.restoreMailboxWithId(mContext, message.mMailboxKey);
                    if (account == null || mailbox == null) {
                        mListeners.loadMessageForViewFailed(messageId, "null account or mailbox");
                        return;
                    }
                    Store remoteStore =
                        Store.getInstance(account.getStoreUri(mContext), mContext, null);
                    Folder remoteFolder = remoteStore.getFolder(mailbox.mDisplayName);
                    remoteFolder.open(OpenMode.READ_WRITE, null);
                    Message remoteMessage = remoteFolder.getMessage(message.mServerId);
                    FetchProfile fp = new FetchProfile();
                    fp.add(FetchProfile.Item.BODY);
                    remoteFolder.fetch(new Message[] { remoteMessage }, fp, null);
                    copyOneMessageToProvider(remoteMessage, account, mailbox,
                            EmailContent.Message.FLAG_LOADED_COMPLETE);
                    mListeners.loadMessageForViewFinished(messageId);
                } catch (MessagingException me) {
                    if (Email.LOGD) Log.v(Email.LOG_TAG, "", me);
                    mListeners.loadMessageForViewFailed(messageId, me.getMessage());
                } catch (RuntimeException rte) {
                    mListeners.loadMessageForViewFailed(messageId, rte.getMessage());
                }
            }
        });
    }
    public void loadAttachment(final long accountId, final long messageId, final long mailboxId,
            final long attachmentId, MessagingListener listener) {
        mListeners.loadAttachmentStarted(accountId, messageId, attachmentId, true);
        put("loadAttachment", listener, new Runnable() {
            public void run() {
                try {
                    File saveToFile = AttachmentProvider.getAttachmentFilename(mContext, accountId,
                            attachmentId);
                    Attachment attachment =
                        Attachment.restoreAttachmentWithId(mContext, attachmentId);
                    if (attachment == null) {
                        mListeners.loadAttachmentFailed(accountId, messageId, attachmentId,
                                "Attachment is null");
                        return;
                    }
                    if (saveToFile.exists() && attachment.mContentUri != null) {
                        mListeners.loadAttachmentFinished(accountId, messageId, attachmentId);
                        return;
                    }
                    EmailContent.Account account =
                        EmailContent.Account.restoreAccountWithId(mContext, accountId);
                    EmailContent.Mailbox mailbox =
                        EmailContent.Mailbox.restoreMailboxWithId(mContext, mailboxId);
                    EmailContent.Message message =
                        EmailContent.Message.restoreMessageWithId(mContext, messageId);
                    if (account == null || mailbox == null || message == null) {
                        mListeners.loadAttachmentFailed(accountId, messageId, attachmentId,
                                "Account, mailbox, message or attachment are null");
                        return;
                    }
                    pruneCachedAttachments(accountId);
                    Store remoteStore =
                        Store.getInstance(account.getStoreUri(mContext), mContext, null);
                    Folder remoteFolder = remoteStore.getFolder(mailbox.mDisplayName);
                    remoteFolder.open(OpenMode.READ_WRITE, null);
                    Message storeMessage = remoteFolder.createMessage(message.mServerId);
                    MimeBodyPart storePart = new MimeBodyPart();
                    storePart.setSize((int)attachment.mSize);
                    storePart.setHeader(MimeHeader.HEADER_ANDROID_ATTACHMENT_STORE_DATA,
                            attachment.mLocation);
                    storePart.setHeader(MimeHeader.HEADER_CONTENT_TYPE,
                            String.format("%s;\n name=\"%s\"",
                            attachment.mMimeType,
                            attachment.mFileName));
                    storePart.setHeader(MimeHeader.HEADER_CONTENT_TRANSFER_ENCODING, "base64");
                    MimeMultipart multipart = new MimeMultipart();
                    multipart.setSubType("mixed");
                    multipart.addBodyPart(storePart);
                    storeMessage.setHeader(MimeHeader.HEADER_CONTENT_TYPE, "multipart/mixed");
                    storeMessage.setBody(multipart);
                    FetchProfile fp = new FetchProfile();
                    fp.add(storePart);
                    remoteFolder.fetch(new Message[] { storeMessage }, fp, null);
                    LegacyConversions.saveAttachmentBody(mContext, storePart, attachment,
                            accountId);
                    mListeners.loadAttachmentFinished(accountId, messageId, attachmentId);
                }
                catch (MessagingException me) {
                    if (Email.LOGD) Log.v(Email.LOG_TAG, "", me);
                    mListeners.loadAttachmentFailed(accountId, messageId, attachmentId,
                            me.getMessage());
                } catch (IOException ioe) {
                    Log.e(Email.LOG_TAG, "Error while storing attachment." + ioe.toString());
                }
            }});
    }
     void pruneCachedAttachments(long accountId) {
        ContentResolver resolver = mContext.getContentResolver();
        File cacheDir = AttachmentProvider.getAttachmentDirectory(mContext, accountId);
        File[] fileList = cacheDir.listFiles();
        if (fileList == null) return;
        for (File file : fileList) {
            if (file.exists()) {
                long id;
                try {
                    id = Long.valueOf(file.getName());
                    Uri uri = ContentUris.withAppendedId(Attachment.CONTENT_URI, id);
                    Cursor c = resolver.query(uri, PRUNE_ATTACHMENT_PROJECTION, null, null, null);
                    try {
                        if (c.moveToNext()) {
                            if (c.getString(0) == null) {
                                continue;
                            }
                        }
                    } finally {
                        c.close();
                    }
                    resolver.update(uri, PRUNE_ATTACHMENT_CV, null, null);
                } catch (NumberFormatException nfe) {
                }
                if (!file.delete()) {
                    file.deleteOnExit();
                }
            }
        }
    }
    public void sendPendingMessages(final EmailContent.Account account, final long sentFolderId,
            MessagingListener listener) {
        put("sendPendingMessages", listener, new Runnable() {
            public void run() {
                sendPendingMessagesSynchronous(account, sentFolderId);
            }
        });
    }
    public void sendPendingMessagesSynchronous(final EmailContent.Account account,
            long sentFolderId) {
        long outboxId = Mailbox.findMailboxOfType(mContext, account.mId, Mailbox.TYPE_OUTBOX);
        if (outboxId == Mailbox.NO_MAILBOX) {
            return;
        }
        ContentResolver resolver = mContext.getContentResolver();
        Cursor c = resolver.query(EmailContent.Message.CONTENT_URI,
                EmailContent.Message.ID_COLUMN_PROJECTION,
                EmailContent.Message.MAILBOX_KEY + "=?", new String[] { Long.toString(outboxId) },
                null);
        try {
            if (c.getCount() <= 0) {
                return;
            }
            mListeners.sendPendingMessagesStarted(account.mId, -1);
            Sender sender = Sender.getInstance(mContext, account.getSenderUri(mContext));
            Store remoteStore = Store.getInstance(account.getStoreUri(mContext), mContext, null);
            boolean requireMoveMessageToSentFolder = remoteStore.requireCopyMessageToSentFolder();
            ContentValues moveToSentValues = null;
            if (requireMoveMessageToSentFolder) {
                moveToSentValues = new ContentValues();
                moveToSentValues.put(MessageColumns.MAILBOX_KEY, sentFolderId);
            }
            while (c.moveToNext()) {
                long messageId = -1;
                try {
                    messageId = c.getLong(0);
                    mListeners.sendPendingMessagesStarted(account.mId, messageId);
                    sender.sendMessage(messageId);
                } catch (MessagingException me) {
                    mListeners.sendPendingMessagesFailed(account.mId, messageId, me);
                    continue;
                }
                Uri syncedUri =
                    ContentUris.withAppendedId(EmailContent.Message.SYNCED_CONTENT_URI, messageId);
                if (requireMoveMessageToSentFolder) {
                    resolver.update(syncedUri, moveToSentValues, null, null);
                } else {
                    AttachmentProvider.deleteAllAttachmentFiles(mContext, account.mId, messageId);
                    Uri uri =
                        ContentUris.withAppendedId(EmailContent.Message.CONTENT_URI, messageId);
                    resolver.delete(uri, null, null);
                    resolver.delete(syncedUri, null, null);
                }
            }
            mListeners.sendPendingMessagesCompleted(account.mId);
        } catch (MessagingException me) {
            mListeners.sendPendingMessagesFailed(account.mId, -1, me);
        } finally {
            c.close();
        }
    }
    public void checkMail(final long accountId, final long tag, final MessagingListener listener) {
        mListeners.checkMailStarted(mContext, accountId, tag);
        listFolders(accountId, null);
        put("checkMail", listener, new Runnable() {
            public void run() {
                long inboxId = -1;
                EmailContent.Account account =
                    EmailContent.Account.restoreAccountWithId(mContext, accountId);
                if (account != null) {
                    long sentboxId = Mailbox.findMailboxOfType(mContext, accountId,
                            Mailbox.TYPE_SENT);
                    if (sentboxId != Mailbox.NO_MAILBOX) {
                        sendPendingMessagesSynchronous(account, sentboxId);
                    }
                    inboxId = Mailbox.findMailboxOfType(mContext, accountId, Mailbox.TYPE_INBOX);
                    if (inboxId != Mailbox.NO_MAILBOX) {
                        EmailContent.Mailbox mailbox =
                            EmailContent.Mailbox.restoreMailboxWithId(mContext, inboxId);
                        if (mailbox != null) {
                            synchronizeMailboxSynchronous(account, mailbox);
                        }
                    }
                }
                mListeners.checkMailFinished(mContext, accountId, inboxId, tag);
            }
        });
    }
    private static class Command {
        public Runnable runnable;
        public MessagingListener listener;
        public String description;
        @Override
        public String toString() {
            return description;
        }
    }
}
