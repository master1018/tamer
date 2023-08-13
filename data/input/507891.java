public class Conversation {
    private static final String TAG = "Mms/conv";
    private static final boolean DEBUG = false;
    private static final Uri sAllThreadsUri =
        Threads.CONTENT_URI.buildUpon().appendQueryParameter("simple", "true").build();
    private static final String[] ALL_THREADS_PROJECTION = {
        Threads._ID, Threads.DATE, Threads.MESSAGE_COUNT, Threads.RECIPIENT_IDS,
        Threads.SNIPPET, Threads.SNIPPET_CHARSET, Threads.READ, Threads.ERROR,
        Threads.HAS_ATTACHMENT
    };
    private static final String[] UNREAD_PROJECTION = {
        Threads._ID,
        Threads.READ
    };
    private static final String UNREAD_SELECTION = "(read=0 OR seen=0)";
    private static final String[] SEEN_PROJECTION = new String[] {
        "seen"
    };
    private static final int ID             = 0;
    private static final int DATE           = 1;
    private static final int MESSAGE_COUNT  = 2;
    private static final int RECIPIENT_IDS  = 3;
    private static final int SNIPPET        = 4;
    private static final int SNIPPET_CS     = 5;
    private static final int READ           = 6;
    private static final int ERROR          = 7;
    private static final int HAS_ATTACHMENT = 8;
    private final Context mContext;
    private long mThreadId;
    private ContactList mRecipients;    
    private long mDate;                 
    private int mMessageCount;          
    private String mSnippet;            
    private boolean mHasUnreadMessages; 
    private boolean mHasAttachment;     
    private boolean mHasError;          
    private static ContentValues mReadContentValues;
    private static boolean mLoadingThreads;
    private boolean mMarkAsReadBlocked;
    private Object mMarkAsBlockedSyncer = new Object();
    private Conversation(Context context) {
        mContext = context;
        mRecipients = new ContactList();
        mThreadId = 0;
    }
    private Conversation(Context context, long threadId, boolean allowQuery) {
        mContext = context;
        if (!loadFromThreadId(threadId, allowQuery)) {
            mRecipients = new ContactList();
            mThreadId = 0;
        }
    }
    private Conversation(Context context, Cursor cursor, boolean allowQuery) {
        mContext = context;
        fillFromCursor(context, this, cursor, allowQuery);
    }
    public static Conversation createNew(Context context) {
        return new Conversation(context);
    }
    public static Conversation get(Context context, long threadId, boolean allowQuery) {
        Conversation conv = Cache.get(threadId);
        if (conv != null)
            return conv;
        conv = new Conversation(context, threadId, allowQuery);
        try {
            Cache.put(conv);
        } catch (IllegalStateException e) {
            LogTag.error("Tried to add duplicate Conversation to Cache");
        }
        return conv;
    }
    public static Conversation get(Context context, ContactList recipients, boolean allowQuery) {
        if (recipients.size() < 1) {
            return createNew(context);
        }
        Conversation conv = Cache.get(recipients);
        if (conv != null)
            return conv;
        long threadId = getOrCreateThreadId(context, recipients);
        conv = new Conversation(context, threadId, allowQuery);
        Log.d(TAG, "Conversation.get: created new conversation " + conv.toString());
        if (!conv.getRecipients().equals(recipients)) {
            Log.e(TAG, "Conversation.get: new conv's recipients don't match input recpients "
                    + recipients);
        }
        try {
            Cache.put(conv);
        } catch (IllegalStateException e) {
            LogTag.error("Tried to add duplicate Conversation to Cache");
        }
        return conv;
    }
    public static Conversation get(Context context, Uri uri, boolean allowQuery) {
        if (uri == null) {
            return createNew(context);
        }
        if (DEBUG) Log.v(TAG, "Conversation get URI: " + uri);
        if (uri.getPathSegments().size() >= 2) {
            try {
                long threadId = Long.parseLong(uri.getPathSegments().get(1));
                if (DEBUG) {
                    Log.v(TAG, "Conversation get threadId: " + threadId);
                }
                return get(context, threadId, allowQuery);
            } catch (NumberFormatException exception) {
                LogTag.error("Invalid URI: " + uri);
            }
        }
        String recipient = uri.getSchemeSpecificPart();
        return get(context, ContactList.getByNumbers(recipient,
                allowQuery , true ), allowQuery);
    }
    public boolean sameRecipient(Uri uri) {
        int size = mRecipients.size();
        if (size > 1) {
            return false;
        }
        if (uri == null) {
            return size == 0;
        }
        if (uri.getPathSegments().size() >= 2) {
            return false;       
        }
        String recipient = uri.getSchemeSpecificPart();
        ContactList incomingRecipient = ContactList.getByNumbers(recipient,
                false , false );
        return mRecipients.equals(incomingRecipient);
    }
    public static Conversation from(Context context, Cursor cursor) {
        long threadId = cursor.getLong(ID);
        if (threadId > 0) {
            Conversation conv = Cache.get(threadId);
            if (conv != null) {
                fillFromCursor(context, conv, cursor, false);   
                return conv;
            }
        }
        Conversation conv = new Conversation(context, cursor, false);
        try {
            Cache.put(conv);
        } catch (IllegalStateException e) {
            LogTag.error("Tried to add duplicate Conversation to Cache");
        }
        return conv;
    }
    private void buildReadContentValues() {
        if (mReadContentValues == null) {
            mReadContentValues = new ContentValues(2);
            mReadContentValues.put("read", 1);
            mReadContentValues.put("seen", 1);
        }
    }
    public void markAsRead() {
        final Uri threadUri = getUri();
        new Thread(new Runnable() {
            public void run() {
                synchronized(mMarkAsBlockedSyncer) {
                    if (mMarkAsReadBlocked) {
                        try {
                            mMarkAsBlockedSyncer.wait();
                        } catch (InterruptedException e) {
                        }
                    }
                    if (threadUri != null) {
                        buildReadContentValues();
                        boolean needUpdate = true;
                        Cursor c = mContext.getContentResolver().query(threadUri,
                                UNREAD_PROJECTION, UNREAD_SELECTION, null, null);
                        if (c != null) {
                            try {
                                needUpdate = c.getCount() > 0;
                            } finally {
                                c.close();
                            }
                        }
                        if (needUpdate) {
                            LogTag.debug("markAsRead: update read/seen for thread uri: " +
                                    threadUri);
                            mContext.getContentResolver().update(threadUri, mReadContentValues,
                                    UNREAD_SELECTION, null);
                        }
                        setHasUnreadMessages(false);
                    }
                }
                MessagingNotification.blockingUpdateAllNotifications(mContext);
            }
        }).start();
    }
    public void blockMarkAsRead(boolean block) {
        if (Log.isLoggable(LogTag.APP, Log.VERBOSE)) {
            LogTag.debug("blockMarkAsRead: " + block);
        }
        synchronized(mMarkAsBlockedSyncer) {
            if (block != mMarkAsReadBlocked) {
                mMarkAsReadBlocked = block;
                if (!mMarkAsReadBlocked) {
                    mMarkAsBlockedSyncer.notifyAll();
                }
            }
        }
    }
    public synchronized Uri getUri() {
        if (mThreadId <= 0)
            return null;
        return ContentUris.withAppendedId(Threads.CONTENT_URI, mThreadId);
    }
    public static Uri getUri(long threadId) {
        return ContentUris.withAppendedId(Threads.CONTENT_URI, threadId);
    }
    public synchronized long getThreadId() {
        return mThreadId;
    }
    public synchronized long ensureThreadId() {
        if (DEBUG) {
            LogTag.debug("ensureThreadId before: " + mThreadId);
        }
        if (mThreadId <= 0) {
            mThreadId = getOrCreateThreadId(mContext, mRecipients);
        }
        if (DEBUG) {
            LogTag.debug("ensureThreadId after: " + mThreadId);
        }
        return mThreadId;
    }
    public synchronized void clearThreadId() {
        if (Log.isLoggable(LogTag.APP, Log.VERBOSE)) {
            LogTag.debug("clearThreadId old threadId was: " + mThreadId + " now zero");
        }
        Cache.remove(mThreadId);
        mThreadId = 0;
    }
    public synchronized void setRecipients(ContactList list) {
        mRecipients = list;
        mThreadId = 0;
    }
    public synchronized ContactList getRecipients() {
        return mRecipients;
    }
    public synchronized boolean hasDraft() {
        if (mThreadId <= 0)
            return false;
        return DraftCache.getInstance().hasDraft(mThreadId);
    }
    public synchronized void setDraftState(boolean hasDraft) {
        if (mThreadId <= 0)
            return;
        DraftCache.getInstance().setDraftState(mThreadId, hasDraft);
    }
    public synchronized long getDate() {
        return mDate;
    }
    public synchronized int getMessageCount() {
        return mMessageCount;
    }
    public synchronized String getSnippet() {
        return mSnippet;
    }
    public boolean hasUnreadMessages() {
        synchronized (this) {
            return mHasUnreadMessages;
        }
    }
    private void setHasUnreadMessages(boolean flag) {
        synchronized (this) {
            mHasUnreadMessages = flag;
        }
    }
    public synchronized boolean hasAttachment() {
        return mHasAttachment;
    }
    public synchronized boolean hasError() {
        return mHasError;
    }
    private static long getOrCreateThreadId(Context context, ContactList list) {
        HashSet<String> recipients = new HashSet<String>();
        Contact cacheContact = null;
        for (Contact c : list) {
            cacheContact = Contact.get(c.getNumber(), false);
            if (cacheContact != null) {
                recipients.add(cacheContact.getNumber());
            } else {
                recipients.add(c.getNumber());
            }
        }
        long retVal = Threads.getOrCreateThreadId(context, recipients);
        if (Log.isLoggable(LogTag.APP, Log.VERBOSE)) {
            LogTag.debug("[Conversation] getOrCreateThreadId for (%s) returned %d",
                    recipients, retVal);
        }
        return retVal;
    }
    @Override
    public synchronized boolean equals(Object obj) {
        try {
            Conversation other = (Conversation)obj;
            return (mRecipients.equals(other.mRecipients));
        } catch (ClassCastException e) {
            return false;
        }
    }
    @Override
    public synchronized int hashCode() {
        return mRecipients.hashCode();
    }
    @Override
    public synchronized String toString() {
        return String.format("[%s] (tid %d)", mRecipients.serialize(), mThreadId);
    }
    public static void asyncDeleteObsoleteThreads(AsyncQueryHandler handler, int token) {
        handler.startDelete(token, null, Threads.OBSOLETE_THREADS_URI, null, null);
    }
    public static void startQueryForAll(AsyncQueryHandler handler, int token) {
        handler.cancelOperation(token);
        handler.startQuery(token, null, sAllThreadsUri,
                ALL_THREADS_PROJECTION, null, null, Conversations.DEFAULT_SORT_ORDER);
    }
    public static void startDelete(AsyncQueryHandler handler, int token, boolean deleteAll,
            long threadId) {
        Uri uri = ContentUris.withAppendedId(Threads.CONTENT_URI, threadId);
        String selection = deleteAll ? null : "locked=0";
        handler.startDelete(token, null, uri, selection, null);
    }
    public static void startDeleteAll(AsyncQueryHandler handler, int token, boolean deleteAll) {
        String selection = deleteAll ? null : "locked=0";
        handler.startDelete(token, null, Threads.CONTENT_URI, selection, null);
    }
    public static void startQueryHaveLockedMessages(AsyncQueryHandler handler, long threadId,
            int token) {
        handler.cancelOperation(token);
        Uri uri = MmsSms.CONTENT_LOCKED_URI;
        if (threadId != -1) {
            uri = ContentUris.withAppendedId(uri, threadId);
        }
        handler.startQuery(token, new Long(threadId), uri,
                ALL_THREADS_PROJECTION, null, null, Conversations.DEFAULT_SORT_ORDER);
    }
    private static void fillFromCursor(Context context, Conversation conv,
                                       Cursor c, boolean allowQuery) {
        synchronized (conv) {
            conv.mThreadId = c.getLong(ID);
            conv.mDate = c.getLong(DATE);
            conv.mMessageCount = c.getInt(MESSAGE_COUNT);
            String snippet = MessageUtils.extractEncStrFromCursor(c, SNIPPET, SNIPPET_CS);
            if (TextUtils.isEmpty(snippet)) {
                snippet = context.getString(R.string.no_subject_view);
            }
            conv.mSnippet = snippet;
            conv.setHasUnreadMessages(c.getInt(READ) == 0);
            conv.mHasError = (c.getInt(ERROR) != 0);
            conv.mHasAttachment = (c.getInt(HAS_ATTACHMENT) != 0);
        }
        String recipientIds = c.getString(RECIPIENT_IDS);
        ContactList recipients = ContactList.getByIds(recipientIds, allowQuery);
        synchronized (conv) {
            conv.mRecipients = recipients;
        }
        if (Log.isLoggable(LogTag.THREAD_CACHE, Log.VERBOSE)) {
            LogTag.debug("fillFromCursor: conv=" + conv + ", recipientIds=" + recipientIds);
        }
    }
    private static class Cache {
        private static Cache sInstance = new Cache();
        static Cache getInstance() { return sInstance; }
        private final HashSet<Conversation> mCache;
        private Cache() {
            mCache = new HashSet<Conversation>(10);
        }
        static Conversation get(long threadId) {
            synchronized (sInstance) {
                if (Log.isLoggable(LogTag.THREAD_CACHE, Log.VERBOSE)) {
                    LogTag.debug("Conversation get with threadId: " + threadId);
                }
                for (Conversation c : sInstance.mCache) {
                    if (DEBUG) {
                        LogTag.debug("Conversation get() threadId: " + threadId +
                                " c.getThreadId(): " + c.getThreadId());
                    }
                    if (c.getThreadId() == threadId) {
                        return c;
                    }
                }
            }
            return null;
        }
        static Conversation get(ContactList list) {
            synchronized (sInstance) {
                if (Log.isLoggable(LogTag.THREAD_CACHE, Log.VERBOSE)) {
                    LogTag.debug("Conversation get with ContactList: " + list);
                }
                for (Conversation c : sInstance.mCache) {
                    if (c.getRecipients().equals(list)) {
                        return c;
                    }
                }
            }
            return null;
        }
        static void put(Conversation c) {
            synchronized (sInstance) {
                if (Log.isLoggable(LogTag.THREAD_CACHE, Log.VERBOSE)) {
                    LogTag.debug("Conversation.Cache.put: conv= " + c + ", hash: " + c.hashCode());
                }
                if (sInstance.mCache.contains(c)) {
                    throw new IllegalStateException("cache already contains " + c +
                            " threadId: " + c.mThreadId);
                }
                sInstance.mCache.add(c);
            }
        }
        static void remove(long threadId) {
            if (DEBUG) {
                LogTag.debug("remove threadid: " + threadId);
                dumpCache();
            }
            for (Conversation c : sInstance.mCache) {
                if (c.getThreadId() == threadId) {
                    sInstance.mCache.remove(c);
                    return;
                }
            }
        }
        static void dumpCache() {
            synchronized (sInstance) {
                LogTag.debug("Conversation dumpCache: ");
                for (Conversation c : sInstance.mCache) {
                    LogTag.debug("   conv: " + c.toString() + " hash: " + c.hashCode());
                }
            }
        }
        static void keepOnly(Set<Long> threads) {
            synchronized (sInstance) {
                Iterator<Conversation> iter = sInstance.mCache.iterator();
                while (iter.hasNext()) {
                    Conversation c = iter.next();
                    if (!threads.contains(c.getThreadId())) {
                        iter.remove();
                    }
                }
            }
            if (DEBUG) {
                LogTag.debug("after keepOnly");
                dumpCache();
            }
        }
    }
    public static void init(final Context context) {
        new Thread(new Runnable() {
            public void run() {
                cacheAllThreads(context);
            }
        }).start();
    }
    public static void markAllConversationsAsSeen(final Context context) {
        if (DEBUG) {
            LogTag.debug("Conversation.markAllConversationsAsSeen");
        }
        new Thread(new Runnable() {
            public void run() {
                blockingMarkAllSmsMessagesAsSeen(context);
                blockingMarkAllMmsMessagesAsSeen(context);
                MessagingNotification.blockingUpdateAllNotifications(context);
            }
        }).start();
    }
    private static void blockingMarkAllSmsMessagesAsSeen(final Context context) {
        ContentResolver resolver = context.getContentResolver();
        Cursor cursor = resolver.query(Sms.Inbox.CONTENT_URI,
                SEEN_PROJECTION,
                "seen=0",
                null,
                null);
        int count = 0;
        if (cursor != null) {
            try {
                count = cursor.getCount();
            } finally {
                cursor.close();
            }
        }
        if (count == 0) {
            return;
        }
        if (Log.isLoggable(LogTag.APP, Log.VERBOSE)) {
            Log.d(TAG, "mark " + count + " SMS msgs as seen");
        }
        ContentValues values = new ContentValues(1);
        values.put("seen", 1);
        resolver.update(Sms.Inbox.CONTENT_URI,
                values,
                "seen=0",
                null);
    }
    private static void blockingMarkAllMmsMessagesAsSeen(final Context context) {
        ContentResolver resolver = context.getContentResolver();
        Cursor cursor = resolver.query(Mms.Inbox.CONTENT_URI,
                SEEN_PROJECTION,
                "seen=0",
                null,
                null);
        int count = 0;
        if (cursor != null) {
            try {
                count = cursor.getCount();
            } finally {
                cursor.close();
            }
        }
        if (count == 0) {
            return;
        }
        if (Log.isLoggable(LogTag.APP, Log.VERBOSE)) {
            Log.d(TAG, "mark " + count + " MMS msgs as seen");
        }
        ContentValues values = new ContentValues(1);
        values.put("seen", 1);
        resolver.update(Mms.Inbox.CONTENT_URI,
                values,
                "seen=0",
                null);
    }
    public static boolean loadingThreads() {
        synchronized (Cache.getInstance()) {
            return mLoadingThreads;
        }
    }
    private static void cacheAllThreads(Context context) {
        if (Log.isLoggable(LogTag.THREAD_CACHE, Log.VERBOSE)) {
            LogTag.debug("[Conversation] cacheAllThreads: begin");
        }
        synchronized (Cache.getInstance()) {
            if (mLoadingThreads) {
                return;
                }
            mLoadingThreads = true;
        }
        HashSet<Long> threadsOnDisk = new HashSet<Long>();
        Cursor c = context.getContentResolver().query(sAllThreadsUri,
                ALL_THREADS_PROJECTION, null, null, null);
        try {
            if (c != null) {
                while (c.moveToNext()) {
                    long threadId = c.getLong(ID);
                    threadsOnDisk.add(threadId);
                    Conversation conv;
                    synchronized (Cache.getInstance()) {
                        conv = Cache.get(threadId);
                    }
                    if (conv == null) {
                        conv = new Conversation(context, c, true);
                        try {
                            synchronized (Cache.getInstance()) {
                                Cache.put(conv);
                            }
                        } catch (IllegalStateException e) {
                            LogTag.error("Tried to add duplicate Conversation to Cache");
                        }
                    } else {
                        fillFromCursor(context, conv, c, true);
                    }
                }
            }
        } finally {
            if (c != null) {
                c.close();
            }
            synchronized (Cache.getInstance()) {
                mLoadingThreads = false;
            }
        }
        Cache.keepOnly(threadsOnDisk);
        if (Log.isLoggable(LogTag.THREAD_CACHE, Log.VERBOSE)) {
            LogTag.debug("[Conversation] cacheAllThreads: finished");
            Cache.dumpCache();
        }
    }
    private boolean loadFromThreadId(long threadId, boolean allowQuery) {
        Cursor c = mContext.getContentResolver().query(sAllThreadsUri, ALL_THREADS_PROJECTION,
                "_id=" + Long.toString(threadId), null, null);
        try {
            if (c.moveToFirst()) {
                fillFromCursor(mContext, this, c, allowQuery);
                if (threadId != mThreadId) {
                    LogTag.error("loadFromThreadId: fillFromCursor returned differnt thread_id!" +
                            " threadId=" + threadId + ", mThreadId=" + mThreadId);
                }
            } else {
                LogTag.error("loadFromThreadId: Can't find thread ID " + threadId);
                return false;
            }
        } finally {
            c.close();
        }
        return true;
    }
}
