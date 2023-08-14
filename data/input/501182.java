public class RecyclerTest extends AndroidTestCase {
    static final String TAG = "RecyclerTest";
    private ArrayList<String> mWords;
    private ArrayList<String> mRecipients;
    private int mWordCount;
    private Random mRandom = new Random();
    private int mRecipientCnt;
    private static final Uri sAllThreadsUri =
        Threads.CONTENT_URI.buildUpon().appendQueryParameter("simple", "true").build();
    private static final String[] ALL_THREADS_PROJECTION = {
        Threads._ID, Threads.DATE, Threads.MESSAGE_COUNT, Threads.RECIPIENT_IDS,
        Threads.SNIPPET, Threads.SNIPPET_CHARSET, Threads.READ, Threads.ERROR,
        Threads.HAS_ATTACHMENT
    };
    @Override
    protected void setUp() throws Exception {
        super.setUp();
        Context context = getContext();
        mWords = new ArrayList<String>(98568);      
        StringBuilder sb = new StringBuilder();
        try {
            Log.v(TAG, "Loading dictionary of words");
            FileInputStream words = context.openFileInput("words");
            int c;
            while ((c = words.read()) != -1) {
                if (c == '\r' || c == '\n') {
                    String word = sb.toString().trim();
                    if (word.length() > 0) {
                        mWords.add(word);
                    }
                    sb.setLength(0);
                } else {
                    sb.append((char)c);
                }
            }
            words.close();
            mWordCount = mWords.size();
            Log.v(TAG, "Loaded dictionary word count: " + mWordCount);
        } catch (Exception e) {
            Log.e(TAG, "can't open words file at /data/data/com.android.mms/files/words");
            return;
        }
        mRecipients = new ArrayList<String>();
        try {
            Log.v(TAG, "Loading recipients");
            FileInputStream recipients = context.openFileInput("recipients");
            int c;
            while ((c = recipients.read()) != -1) {
                if (c == '\r' || c == '\n' || c == ',') {
                    String recipient = sb.toString().trim();
                    if (recipient.length() > 0) {
                        mRecipients.add(recipient);
                    }
                    sb.setLength(0);
                } else {
                    sb.append((char)c);
                }
            }
            recipients.close();
            Log.v(TAG, "Loaded recipients: " + mRecipients.size());
        } catch (Exception e) {
            Log.e(TAG, "can't open recipients file at /data/data/com.android.mms/files/recipients");
            return;
        }
        mRecipientCnt = mRecipients.size();
    }
    private String generateMessage() {
        int wordsInMessage = mRandom.nextInt(9) + 1;   
        StringBuilder msg = new StringBuilder();
        for (int i = 0; i < wordsInMessage; i++) {
            msg.append(mWords.get(mRandom.nextInt(mWordCount)) + " ");
        }
        return msg.toString();
    }
    private Uri storeMessage(Context context, String address, String message) {
        ContentValues values = new ContentValues();
        values.put(Inbox.ADDRESS, address);
        values.put(Inbox.DATE, new Long(System.currentTimeMillis()));
        values.put(Inbox.PROTOCOL, 0);
        values.put(Inbox.READ, Integer.valueOf(0));
        values.put(Inbox.REPLY_PATH_PRESENT, 0);
        values.put(Inbox.SERVICE_CENTER, 0);
        values.put(Inbox.BODY, message);
        Long threadId = 0L;
        Contact cacheContact = Contact.get(address,true);
        if (cacheContact != null) {
            address = cacheContact.getNumber();
        }
        if (((threadId == null) || (threadId == 0)) && (address != null)) {
            values.put(Sms.THREAD_ID, Threads.getOrCreateThreadId(
                               context, address));
        }
        ContentResolver resolver = context.getContentResolver();
        Uri insertedUri = SqliteWrapper.insert(context, resolver, Inbox.CONTENT_URI, values);
        threadId = values.getAsLong(Sms.THREAD_ID);
        Recycler.getSmsRecycler().deleteOldMessagesByThreadId(context, threadId);
        return insertedUri;
    }
    Runnable mRecyclerBang = new Runnable() {
        public void run() {
            final int MAXSEND = Integer.MAX_VALUE;
            for (int i = 0; i < MAXSEND; i++) {
                Uri uri = storeMessage(getContext(),
                        mRecipients.get(mRandom.nextInt(mRecipientCnt)),
                        generateMessage());
                Log.v(TAG, "Generating msg uri: " + uri);
                if (i > 100) {
                    Cursor cursor = null;
                    try {
                        cursor = SqliteWrapper.query(getContext(),
                                getContext().getContentResolver(), sAllThreadsUri,
                                ALL_THREADS_PROJECTION, null, null,
                                Conversations.DEFAULT_SORT_ORDER);
                        assertNotNull("Cursor from thread query is null!", cursor);
                        int cnt = cursor.getCount();
                        assertTrue("The threads appeared to have been wiped out",
                            cursor.getCount() >= mRecipientCnt);
                    } catch (SQLiteException e) {
                        Log.v(TAG, "query for threads failed with exception: " + e);
                        fail("query for threads failed with exception: " + e);
                    } finally {
                        if (cursor != null) {
                            cursor.close();
                        }
                    }
                }
            }
        }
    };
    Runnable mSQLMemoryReleaser = new Runnable() {
        public void run() {
            while (true) {
                SQLiteDatabase.releaseMemory();
                try {
                    Thread.sleep(5000);
                } catch (Exception e) {
                }
            }
        }
    };
    @LargeTest
    public void testRecycler() throws Throwable {
        final int THREAD_COUNT = 3;
        ArrayList<Thread> threads = new ArrayList<Thread>(THREAD_COUNT);
        for (int i = 0; i < THREAD_COUNT; i++) {
            threads.add(i, new Thread(mRecyclerBang));
            threads.get(i).start();
        }
        Thread memoryBanger = new Thread(mSQLMemoryReleaser);
        memoryBanger.start();
        for (int i = 0; i < THREAD_COUNT; i++) {
            threads.get(i).join();
        }
        assertTrue(true);
    }
}
