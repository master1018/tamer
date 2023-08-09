public class Controller {
    static Controller sInstance;
    private Context mContext;
    private Context mProviderContext;
    private MessagingController mLegacyController;
    private LegacyListener mLegacyListener = new LegacyListener();
    private ServiceCallback mServiceCallback = new ServiceCallback();
    private HashSet<Result> mListeners = new HashSet<Result>();
    private static String[] MESSAGEID_TO_ACCOUNTID_PROJECTION = new String[] {
        EmailContent.RECORD_ID,
        EmailContent.MessageColumns.ACCOUNT_KEY
    };
    private static int MESSAGEID_TO_ACCOUNTID_COLUMN_ACCOUNTID = 1;
    private static String[] MESSAGEID_TO_MAILBOXID_PROJECTION = new String[] {
        EmailContent.RECORD_ID,
        EmailContent.MessageColumns.MAILBOX_KEY
    };
    private static int MESSAGEID_TO_MAILBOXID_COLUMN_MAILBOXID = 1;
    protected Controller(Context _context) {
        mContext = _context;
        mProviderContext = _context;
        mLegacyController = MessagingController.getInstance(mContext);
        mLegacyController.addListener(mLegacyListener);
    }
    public synchronized static Controller getInstance(Context _context) {
        if (sInstance == null) {
            sInstance = new Controller(_context);
        }
        return sInstance;
    }
    public void setProviderContext(Context providerContext) {
        mProviderContext = providerContext;
    }
    public void addResultCallback(Result listener) {
        synchronized (mListeners) {
            mListeners.add(listener);
        }
    }
    public void removeResultCallback(Result listener) {
        synchronized (mListeners) {
            mListeners.remove(listener);
        }
    }
    private boolean isActiveResultCallback(Result listener) {
        synchronized (mListeners) {
            return mListeners.contains(listener);
        }
    }
    public void serviceLogging(int debugEnabled) {
        IEmailService service = ExchangeUtils.getExchangeEmailService(mContext, mServiceCallback);
        try {
            service.setLogging(debugEnabled);
        } catch (RemoteException e) {
            Log.d("updateMailboxList", "RemoteException" + e);
        }
    }
    public void updateMailboxList(final long accountId, final Result callback) {
        IEmailService service = getServiceForAccount(accountId);
        if (service != null) {
            try {
                service.updateFolderList(accountId);
            } catch (RemoteException e) {
                Log.d("updateMailboxList", "RemoteException" + e);
            }
        } else {
            new Thread() {
                @Override
                public void run() {
                    mLegacyController.listFolders(accountId, mLegacyListener);
                }
            }.start();
        }
    }
    public void serviceCheckMail(final long accountId, final long mailboxId, final long tag,
            final Result callback) {
        IEmailService service = getServiceForAccount(accountId);
        if (service != null) {
                callback.serviceCheckMailCallback(null, accountId, mailboxId, 100, tag);
        } else {
            new Thread() {
                @Override
                public void run() {
                    mLegacyController.checkMail(accountId, tag, mLegacyListener);
                }
            }.start();
        }
    }
    public void updateMailbox(final long accountId, final long mailboxId, final Result callback) {
        IEmailService service = getServiceForAccount(accountId);
        if (service != null) {
            try {
                service.startSync(mailboxId);
            } catch (RemoteException e) {
                Log.d("updateMailbox", "RemoteException" + e);
            }
        } else {
            new Thread() {
                @Override
                public void run() {
                    Account account =
                        EmailContent.Account.restoreAccountWithId(mProviderContext, accountId);
                    Mailbox mailbox =
                        EmailContent.Mailbox.restoreMailboxWithId(mProviderContext, mailboxId);
                    if (account == null || mailbox == null) {
                        return;
                    }
                    mLegacyController.synchronizeMailbox(account, mailbox, mLegacyListener);
                }
            }.start();
        }
    }
    public void loadMessageForView(final long messageId, final Result callback) {
        IEmailService service = getServiceForMessage(messageId);
        if (service != null) {
            Uri uri = ContentUris.withAppendedId(Message.CONTENT_URI, messageId);
            ContentValues cv = new ContentValues();
            cv.put(MessageColumns.FLAG_LOADED, Message.FLAG_LOADED_COMPLETE);
            mProviderContext.getContentResolver().update(uri, cv, null, null);
            Log.d(Email.LOG_TAG, "Unexpected loadMessageForView() for service-based message.");
            synchronized (mListeners) {
                for (Result listener : mListeners) {
                    listener.loadMessageForViewCallback(null, messageId, 100);
                }
            }
        } else {
            new Thread() {
                @Override
                public void run() {
                    mLegacyController.loadMessageForView(messageId, mLegacyListener);
                }
            }.start();
        }
    }
    public void saveToMailbox(final EmailContent.Message message, final int mailboxType) {
        long accountId = message.mAccountKey;
        long mailboxId = findOrCreateMailboxOfType(accountId, mailboxType);
        message.mMailboxKey = mailboxId;
        message.save(mProviderContext);
    }
    public long findOrCreateMailboxOfType(long accountId, int mailboxType) {
        if (accountId < 0 || mailboxType < 0) {
            return Mailbox.NO_MAILBOX;
        }
        long mailboxId =
            Mailbox.findMailboxOfType(mProviderContext, accountId, mailboxType);
        return mailboxId == Mailbox.NO_MAILBOX ? createMailbox(accountId, mailboxType) : mailboxId;
    }
     String getMailboxServerName(int mailboxType) {
        int resId = -1;
        switch (mailboxType) {
            case Mailbox.TYPE_INBOX:
                resId = R.string.mailbox_name_server_inbox;
                break;
            case Mailbox.TYPE_OUTBOX:
                resId = R.string.mailbox_name_server_outbox;
                break;
            case Mailbox.TYPE_DRAFTS:
                resId = R.string.mailbox_name_server_drafts;
                break;
            case Mailbox.TYPE_TRASH:
                resId = R.string.mailbox_name_server_trash;
                break;
            case Mailbox.TYPE_SENT:
                resId = R.string.mailbox_name_server_sent;
                break;
            case Mailbox.TYPE_JUNK:
                resId = R.string.mailbox_name_server_junk;
                break;
        }
        return resId != -1 ? mContext.getString(resId) : "";
    }
     long createMailbox(long accountId, int mailboxType) {
        if (accountId < 0 || mailboxType < 0) {
            String mes = "Invalid arguments " + accountId + ' ' + mailboxType;
            Log.e(Email.LOG_TAG, mes);
            throw new RuntimeException(mes);
        }
        Mailbox box = new Mailbox();
        box.mAccountKey = accountId;
        box.mType = mailboxType;
        box.mSyncInterval = EmailContent.Account.CHECK_INTERVAL_NEVER;
        box.mFlagVisible = true;
        box.mDisplayName = getMailboxServerName(mailboxType);
        box.save(mProviderContext);
        return box.mId;
    }
    public void sendMessage(long messageId, long accountId) {
        ContentResolver resolver = mProviderContext.getContentResolver();
        if (accountId == -1) {
            accountId = lookupAccountForMessage(messageId);
        }
        if (accountId == -1) {
            if (Email.LOGD) {
                Email.log("no account found for message " + messageId);
            }
            return;
        }
        long outboxId = findOrCreateMailboxOfType(accountId, Mailbox.TYPE_OUTBOX);
        ContentValues cv = new ContentValues();
        cv.put(EmailContent.MessageColumns.MAILBOX_KEY, outboxId);
        Uri uri = ContentUris.withAppendedId(EmailContent.Message.CONTENT_URI, messageId);
        resolver.update(uri, cv, null, null);
        IEmailService service = getServiceForMessage(messageId);
        if (service != null) {
            try {
                service.setCallback(mServiceCallback);
            } catch (RemoteException re) {
            }
        } else {
            final EmailContent.Account account =
                    EmailContent.Account.restoreAccountWithId(mProviderContext, accountId);
            final long sentboxId = findOrCreateMailboxOfType(accountId, Mailbox.TYPE_SENT);
            new Thread() {
                @Override
                public void run() {
                    mLegacyController.sendPendingMessages(account, sentboxId, mLegacyListener);
                }
            }.start();
        }
    }
    public void sendPendingMessages(long accountId, Result callback) {
        final long outboxId =
            Mailbox.findMailboxOfType(mProviderContext, accountId, Mailbox.TYPE_OUTBOX);
        if (outboxId == Mailbox.NO_MAILBOX) {
            return;
        }
        IEmailService service = getServiceForAccount(accountId);
        if (service != null) {
            try {
                service.startSync(outboxId);
            } catch (RemoteException e) {
                Log.d("updateMailbox", "RemoteException" + e);
            }
        } else {
            final EmailContent.Account account =
                EmailContent.Account.restoreAccountWithId(mProviderContext, accountId);
            final long sentboxId = findOrCreateMailboxOfType(accountId, Mailbox.TYPE_SENT);
            new Thread() {
                @Override
                public void run() {
                    mLegacyController.sendPendingMessages(account, sentboxId, mLegacyListener);
                }
            }.start();
        }
    }
    public void resetVisibleLimits() {
        new Thread() {
            @Override
            public void run() {
                ContentResolver resolver = mProviderContext.getContentResolver();
                Cursor c = null;
                try {
                    c = resolver.query(
                            Account.CONTENT_URI,
                            Account.ID_PROJECTION,
                            null, null, null);
                    while (c.moveToNext()) {
                        long accountId = c.getLong(Account.ID_PROJECTION_COLUMN);
                        Account account = Account.restoreAccountWithId(mProviderContext, accountId);
                        if (account != null) {
                            Store.StoreInfo info = Store.StoreInfo.getStoreInfo(
                                    account.getStoreUri(mProviderContext), mContext);
                            if (info != null && info.mVisibleLimitDefault > 0) {
                                int limit = info.mVisibleLimitDefault;
                                ContentValues cv = new ContentValues();
                                cv.put(MailboxColumns.VISIBLE_LIMIT, limit);
                                resolver.update(Mailbox.CONTENT_URI, cv,
                                        MailboxColumns.ACCOUNT_KEY + "=?",
                                        new String[] { Long.toString(accountId) });
                            }
                        }
                    }
                } finally {
                    if (c != null) {
                        c.close();
                    }
                }
            }
        }.start();
    }
    public void loadMoreMessages(final long mailboxId, Result callback) {
        new Thread() {
            @Override
            public void run() {
                Mailbox mailbox = Mailbox.restoreMailboxWithId(mProviderContext, mailboxId);
                if (mailbox == null) {
                    return;
                }
                Account account = Account.restoreAccountWithId(mProviderContext,
                        mailbox.mAccountKey);
                if (account == null) {
                    return;
                }
                Store.StoreInfo info = Store.StoreInfo.getStoreInfo(
                        account.getStoreUri(mProviderContext), mContext);
                if (info != null && info.mVisibleLimitIncrement > 0) {
                    ContentValues cv = new ContentValues();;
                    cv.put(EmailContent.FIELD_COLUMN_NAME, MailboxColumns.VISIBLE_LIMIT);
                    cv.put(EmailContent.ADD_COLUMN_NAME, info.mVisibleLimitIncrement);
                    Uri uri = ContentUris.withAppendedId(Mailbox.ADD_TO_FIELD_URI, mailboxId);
                    mProviderContext.getContentResolver().update(uri, cv, null, null);
                    mailbox.mVisibleLimit += info.mVisibleLimitIncrement;
                    mLegacyController.synchronizeMailbox(account, mailbox, mLegacyListener);
                }
            }
        }.start();
    }
    private long lookupAccountForMessage(long messageId) {
        ContentResolver resolver = mProviderContext.getContentResolver();
        Cursor c = resolver.query(EmailContent.Message.CONTENT_URI,
                                  MESSAGEID_TO_ACCOUNTID_PROJECTION, EmailContent.RECORD_ID + "=?",
                                  new String[] { Long.toString(messageId) }, null);
        try {
            return c.moveToFirst()
                ? c.getLong(MESSAGEID_TO_ACCOUNTID_COLUMN_ACCOUNTID)
                : -1;
        } finally {
            c.close();
        }
    }
    public void deleteAttachment(long attachmentId) {
        ContentResolver resolver = mProviderContext.getContentResolver();
        Uri uri = ContentUris.withAppendedId(Attachment.CONTENT_URI, attachmentId);
        resolver.delete(uri, null, null);
    }
    public void deleteMessage(long messageId, long accountId) {
        ContentResolver resolver = mProviderContext.getContentResolver();
        if (accountId == -1) {
            accountId = lookupAccountForMessage(messageId);
        }
        if (accountId == -1) {
            return;
        }
        long trashMailboxId = findOrCreateMailboxOfType(accountId, Mailbox.TYPE_TRASH);
        long sourceMailboxId = -1;
        Cursor c = resolver.query(EmailContent.Message.CONTENT_URI,
                MESSAGEID_TO_MAILBOXID_PROJECTION, EmailContent.RECORD_ID + "=?",
                new String[] { Long.toString(messageId) }, null);
        try {
            sourceMailboxId = c.moveToFirst()
                ? c.getLong(MESSAGEID_TO_MAILBOXID_COLUMN_MAILBOXID)
                : -1;
        } finally {
            c.close();
        }
        AttachmentProvider.deleteAllAttachmentFiles(mProviderContext, accountId, messageId);
        Uri uri = ContentUris.withAppendedId(EmailContent.Message.SYNCED_CONTENT_URI, messageId);
        if (sourceMailboxId == trashMailboxId) {
            resolver.delete(uri, null, null);
        } else {
            ContentValues cv = new ContentValues();
            cv.put(EmailContent.MessageColumns.MAILBOX_KEY, trashMailboxId);
            resolver.update(uri, cv, null, null);
        }
        Account account = Account.restoreAccountWithId(mProviderContext, accountId);
        if (isMessagingController(account)) {
            final long syncAccountId = accountId;
            new Thread() {
                @Override
                public void run() {
                    mLegacyController.processPendingActions(syncAccountId);
                }
            }.start();
        }
    }
    public void setMessageRead(final long messageId, boolean isRead) {
        ContentValues cv = new ContentValues();
        cv.put(EmailContent.MessageColumns.FLAG_READ, isRead);
        Uri uri = ContentUris.withAppendedId(
                EmailContent.Message.SYNCED_CONTENT_URI, messageId);
        mProviderContext.getContentResolver().update(uri, cv, null, null);
        final Message message = Message.restoreMessageWithId(mProviderContext, messageId);
        Account account = Account.restoreAccountWithId(mProviderContext, message.mAccountKey);
        if (isMessagingController(account)) {
            new Thread() {
                @Override
                public void run() {
                    mLegacyController.processPendingActions(message.mAccountKey);
                }
            }.start();
        }
    }
    public void setMessageFavorite(final long messageId, boolean isFavorite) {
        ContentValues cv = new ContentValues();
        cv.put(EmailContent.MessageColumns.FLAG_FAVORITE, isFavorite);
        Uri uri = ContentUris.withAppendedId(
                EmailContent.Message.SYNCED_CONTENT_URI, messageId);
        mProviderContext.getContentResolver().update(uri, cv, null, null);
        final Message message = Message.restoreMessageWithId(mProviderContext, messageId);
        Account account = Account.restoreAccountWithId(mProviderContext, message.mAccountKey);
        if (isMessagingController(account)) {
            new Thread() {
                @Override
                public void run() {
                    mLegacyController.processPendingActions(message.mAccountKey);
                }
            }.start();
        }
    }
    public void sendMeetingResponse(final long messageId, final int response,
            final Result callback) {
        IEmailService service = getServiceForMessage(messageId);
        if (service != null) {
            try {
                service.sendMeetingResponse(messageId, response);
            } catch (RemoteException e) {
                Log.e("onDownloadAttachment", "RemoteException", e);
            }
        }
    }
    public void loadAttachment(final long attachmentId, final long messageId, final long mailboxId,
            final long accountId, final Result callback) {
        File saveToFile = AttachmentProvider.getAttachmentFilename(mProviderContext,
                accountId, attachmentId);
        Attachment attachInfo = Attachment.restoreAttachmentWithId(mProviderContext, attachmentId);
        if (saveToFile.exists() && attachInfo.mContentUri != null) {
            synchronized (mListeners) {
                for (Result listener : mListeners) {
                    listener.loadAttachmentCallback(null, messageId, attachmentId, 0);
                }
                for (Result listener : mListeners) {
                    listener.loadAttachmentCallback(null, messageId, attachmentId, 100);
                }
            }
            return;
        }
        IEmailService service = getServiceForMessage(messageId);
        if (service != null) {
            try {
                service.loadAttachment(attachInfo.mId, saveToFile.getAbsolutePath(),
                        AttachmentProvider.getAttachmentUri(accountId, attachmentId).toString());
            } catch (RemoteException e) {
                Log.e("onDownloadAttachment", "RemoteException", e);
            }
        } else {
            new Thread() {
                @Override
                public void run() {
                    mLegacyController.loadAttachment(accountId, messageId, mailboxId, attachmentId,
                            mLegacyListener);
                }
            }.start();
        }
    }
    private IEmailService getServiceForMessage(long messageId) {
        Message message = Message.restoreMessageWithId(mProviderContext, messageId);
        return getServiceForAccount(message.mAccountKey);
    }
    private IEmailService getServiceForAccount(long accountId) {
        Account account = EmailContent.Account.restoreAccountWithId(mProviderContext, accountId);
        if (account == null || isMessagingController(account)) {
            return null;
        } else {
            return ExchangeUtils.getExchangeEmailService(mContext, mServiceCallback);
        }
    }
    public boolean isMessagingController(EmailContent.Account account) {
        if (account == null) return false;
        Store.StoreInfo info =
            Store.StoreInfo.getStoreInfo(account.getStoreUri(mProviderContext), mContext);
        if (info == null) {
            return false;
        }
        String scheme = info.mScheme;
        return ("pop3".equals(scheme) || "imap".equals(scheme));
    }
    public interface Result {
        public void updateMailboxListCallback(MessagingException result, long accountId,
                int progress);
        public void updateMailboxCallback(MessagingException result, long accountId,
                long mailboxId, int progress, int numNewMessages);
        public void loadMessageForViewCallback(MessagingException result, long messageId,
                int progress);
        public void loadAttachmentCallback(MessagingException result, long messageId,
                long attachmentId, int progress);
        public void serviceCheckMailCallback(MessagingException result, long accountId,
                long mailboxId, int progress, long tag);
        public void sendMailCallback(MessagingException result, long accountId,
                long messageId, int progress);
    }
    private class LegacyListener extends MessagingListener {
        @Override
        public void listFoldersStarted(long accountId) {
            synchronized (mListeners) {
                for (Result l : mListeners) {
                    l.updateMailboxListCallback(null, accountId, 0);
                }
            }
        }
        @Override
        public void listFoldersFailed(long accountId, String message) {
            synchronized (mListeners) {
                for (Result l : mListeners) {
                    l.updateMailboxListCallback(new MessagingException(message), accountId, 0);
                }
            }
        }
        @Override
        public void listFoldersFinished(long accountId) {
            synchronized (mListeners) {
                for (Result l : mListeners) {
                    l.updateMailboxListCallback(null, accountId, 100);
                }
            }
        }
        @Override
        public void synchronizeMailboxStarted(long accountId, long mailboxId) {
            synchronized (mListeners) {
                for (Result l : mListeners) {
                    l.updateMailboxCallback(null, accountId, mailboxId, 0, 0);
                }
            }
        }
        @Override
        public void synchronizeMailboxFinished(long accountId, long mailboxId,
                int totalMessagesInMailbox, int numNewMessages) {
            synchronized (mListeners) {
                for (Result l : mListeners) {
                    l.updateMailboxCallback(null, accountId, mailboxId, 100, numNewMessages);
                }
            }
        }
        @Override
        public void synchronizeMailboxFailed(long accountId, long mailboxId, Exception e) {
            MessagingException me;
            if (e instanceof MessagingException) {
                me = (MessagingException) e;
            } else {
                me = new MessagingException(e.toString());
            }
            synchronized (mListeners) {
                for (Result l : mListeners) {
                    l.updateMailboxCallback(me, accountId, mailboxId, 0, 0);
                }
            }
        }
        @Override
        public void checkMailStarted(Context context, long accountId, long tag) {
            synchronized (mListeners) {
                for (Result l : mListeners) {
                    l.serviceCheckMailCallback(null, accountId, -1, 0, tag);
                }
            }
        }
        @Override
        public void checkMailFinished(Context context, long accountId, long folderId, long tag) {
            synchronized (mListeners) {
                for (Result l : mListeners) {
                    l.serviceCheckMailCallback(null, accountId, folderId, 100, tag);
                }
            }
        }
        @Override
        public void loadMessageForViewStarted(long messageId) {
            synchronized (mListeners) {
                for (Result listener : mListeners) {
                    listener.loadMessageForViewCallback(null, messageId, 0);
                }
            }
        }
        @Override
        public void loadMessageForViewFinished(long messageId) {
            synchronized (mListeners) {
                for (Result listener : mListeners) {
                    listener.loadMessageForViewCallback(null, messageId, 100);
                }
            }
        }
        @Override
        public void loadMessageForViewFailed(long messageId, String message) {
            synchronized (mListeners) {
                for (Result listener : mListeners) {
                    listener.loadMessageForViewCallback(new MessagingException(message),
                            messageId, 0);
                }
            }
        }
        @Override
        public void loadAttachmentStarted(long accountId, long messageId, long attachmentId,
                boolean requiresDownload) {
            synchronized (mListeners) {
                for (Result listener : mListeners) {
                    listener.loadAttachmentCallback(null, messageId, attachmentId, 0);
                }
            }
        }
        @Override
        public void loadAttachmentFinished(long accountId, long messageId, long attachmentId) {
            synchronized (mListeners) {
                for (Result listener : mListeners) {
                    listener.loadAttachmentCallback(null, messageId, attachmentId, 100);
                }
            }
        }
        @Override
        public void loadAttachmentFailed(long accountId, long messageId, long attachmentId,
                String reason) {
            synchronized (mListeners) {
                for (Result listener : mListeners) {
                    listener.loadAttachmentCallback(new MessagingException(reason),
                            messageId, attachmentId, 0);
                }
            }
        }
        @Override
        synchronized public void sendPendingMessagesStarted(long accountId, long messageId) {
            synchronized (mListeners) {
                for (Result listener : mListeners) {
                    listener.sendMailCallback(null, accountId, messageId, 0);
                }
            }
        }
        @Override
        synchronized public void sendPendingMessagesCompleted(long accountId) {
            synchronized (mListeners) {
                for (Result listener : mListeners) {
                    listener.sendMailCallback(null, accountId, -1, 100);
                }
            }
        }
        @Override
        synchronized public void sendPendingMessagesFailed(long accountId, long messageId,
                Exception reason) {
            MessagingException me;
            if (reason instanceof MessagingException) {
                me = (MessagingException) reason;
            } else {
                me = new MessagingException(reason.toString());
            }
            synchronized (mListeners) {
                for (Result listener : mListeners) {
                    listener.sendMailCallback(me, accountId, messageId, 0);
                }
            }
        }
    }
    private class ServiceCallback extends IEmailServiceCallback.Stub {
        private final static boolean DEBUG_FAIL_DOWNLOADS = false;       
        public void loadAttachmentStatus(long messageId, long attachmentId, int statusCode,
                int progress) {
            MessagingException result = mapStatusToException(statusCode);
            switch (statusCode) {
                case EmailServiceStatus.SUCCESS:
                    progress = 100;
                    break;
                case EmailServiceStatus.IN_PROGRESS:
                    if (DEBUG_FAIL_DOWNLOADS && progress > 75) {
                        result = new MessagingException(
                                String.valueOf(EmailServiceStatus.CONNECTION_ERROR));
                    }
                    if (progress < 0 || progress >= 100) {
                        return;
                    }
                    break;
            }
            synchronized (mListeners) {
                for (Result listener : mListeners) {
                    listener.loadAttachmentCallback(result, messageId, attachmentId, progress);
                }
            }
        }
        public void sendMessageStatus(long messageId, String subject, int statusCode,
                int progress) {
            long accountId = -1;        
            MessagingException result = mapStatusToException(statusCode);
            switch (statusCode) {
                case EmailServiceStatus.SUCCESS:
                    progress = 100;
                    break;
                case EmailServiceStatus.IN_PROGRESS:
                    if (progress < 0 || progress >= 100) {
                        return;
                    }
                    break;
            }
            synchronized(mListeners) {
                for (Result listener : mListeners) {
                    listener.sendMailCallback(result, accountId, messageId, progress);
                }
            }
        }
        public void syncMailboxListStatus(long accountId, int statusCode, int progress) {
            MessagingException result = mapStatusToException(statusCode);
            switch (statusCode) {
                case EmailServiceStatus.SUCCESS:
                    progress = 100;
                    break;
                case EmailServiceStatus.IN_PROGRESS:
                    if (progress < 0 || progress >= 100) {
                        return;
                    }
                    break;
            }
            synchronized(mListeners) {
                for (Result listener : mListeners) {
                    listener.updateMailboxListCallback(result, accountId, progress);
                }
            }
        }
        public void syncMailboxStatus(long mailboxId, int statusCode, int progress) {
            MessagingException result = mapStatusToException(statusCode);
            switch (statusCode) {
                case EmailServiceStatus.SUCCESS:
                    progress = 100;
                    break;
                case EmailServiceStatus.IN_PROGRESS:
                    if (progress < 0 || progress >= 100) {
                        return;
                    }
                    break;
            }
            Mailbox mbx = Mailbox.restoreMailboxWithId(mProviderContext, mailboxId);
            if (mbx == null) return;
            long accountId = mbx.mAccountKey;
            synchronized(mListeners) {
                for (Result listener : mListeners) {
                    listener.updateMailboxCallback(result, accountId, mailboxId, progress, 0);
                }
            }
        }
        private MessagingException mapStatusToException(int statusCode) {
            switch (statusCode) {
                case EmailServiceStatus.SUCCESS:
                case EmailServiceStatus.IN_PROGRESS:
                    return null;
                case EmailServiceStatus.LOGIN_FAILED:
                    return new AuthenticationFailedException("");
                case EmailServiceStatus.CONNECTION_ERROR:
                    return new MessagingException(MessagingException.IOERROR);
                case EmailServiceStatus.SECURITY_FAILURE:
                    return new MessagingException(MessagingException.SECURITY_POLICIES_REQUIRED);
                case EmailServiceStatus.MESSAGE_NOT_FOUND:
                case EmailServiceStatus.ATTACHMENT_NOT_FOUND:
                case EmailServiceStatus.FOLDER_NOT_DELETED:
                case EmailServiceStatus.FOLDER_NOT_RENAMED:
                case EmailServiceStatus.FOLDER_NOT_CREATED:
                case EmailServiceStatus.REMOTE_EXCEPTION:
                default:
                    return new MessagingException(String.valueOf(statusCode));
            }
        }
    }
}
