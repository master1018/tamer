@TestTargetClass(AsyncQueryHandler.class)
public class AsyncQueryHandlerTest extends AndroidTestCase {
    private static final long TEST_TIME_OUT = DummyProvider.MOCK_OPERATION_SLEEP_TIME + 5000;
    private static final int INSERT_TOKEN_1    = 100;
    private static final int INSERT_TOKEN_2    = 101;
    private static final int QUERY_TOKEN_1     = 200;
    private static final int QUERY_TOKEN_2     = 201;
    private static final int MOCK_QUERY_TOKEN  = 202;
    private static final int DELETE_TOKEN_1    = 300;
    private static final int DELETE_TOKEN_2    = 301;
    private static final int UPDATE_TOKEN_1    = 400;
    private static final int UPDATE_TOKEN_2    = 401;
    private static final Object INSERT_COOKIE = "insert cookie";
    private static final Object QUERY_COOKIE  = "query cookie";
    private static final Object DELETE_COOKIE = "delete cookie";
    private static final Object UPDATE_COOKIE = "update cookie";
    private static final String NAME0 = "name0";
    private static final String NAME1 = "name1";
    private static final String NAME2 = "name2";
    private static final String NAME3 = "name3";
    private static final String NAME4 = "name4";
    private static final int NAME_COLUMN_INDEX = 1;
    private static final boolean CANCELABLE = true;
    private static final boolean NO_CANCEL = false;
    private static final String[] PROJECTIONS = new String[] {
            DummyProvider._ID, DummyProvider.NAME};
    private static final String ORDER_BY = "_id ASC";
    private static final int ORIGINAL_ROW_COUNT = 3;
    private MockAsyncQueryHandler mAsyncHandler;
    private ContentResolver mResolver;
    @Override
    protected void setUp() throws Exception {
        super.setUp();
        mResolver = mContext.getContentResolver();
        ContentValues values0 = new ContentValues();
        values0.put(DummyProvider.NAME, NAME0);
        mResolver.insert(DummyProvider.CONTENT_URI, values0);
        ContentValues values1 = new ContentValues();
        values1.put(DummyProvider.NAME, NAME1);
        mResolver.insert(DummyProvider.CONTENT_URI, values1);
        ContentValues values2 = new ContentValues();
        values2.put(DummyProvider.NAME, NAME2);
        mResolver.insert(DummyProvider.CONTENT_URI, values2);
    }
    @Override
    protected void tearDown() throws Exception {
        mResolver.delete(DummyProvider.CONTENT_URI, null, null);
        super.tearDown();
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        method = "AsyncQueryHandler",
        args = {android.content.ContentResolver.class}
    )
    public void testConstructor() {
        new AsyncQueryHandler(mResolver) {};
        new AsyncQueryHandler(null) {};
    }
    @TestTargets({
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "startInsert",
            args = {int.class, java.lang.Object.class, android.net.Uri.class,
                    android.content.ContentValues.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "onInsertComplete",
            args = {int.class, java.lang.Object.class, android.net.Uri.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "cancelOperation",
            args = {int.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "handleMessage",
            args = {android.os.Message.class}
        )
    })
    public void testStartInsert() throws InterruptedException {
        final ContentValues values1 = new ContentValues();
        values1.put(DummyProvider.NAME, NAME3);
        final ContentValues values2 = new ContentValues();
        values2.put(DummyProvider.NAME, NAME4);
        mAsyncHandler = createAsyncQueryHandlerSync();
        assertFalse(mAsyncHandler.hadInserted(0));
        startInsert(INSERT_TOKEN_1, INSERT_COOKIE, DummyProvider.CONTENT_URI, values1, NO_CANCEL);
        assertTrue(mAsyncHandler.hadInserted(TEST_TIME_OUT));
        assertEquals(INSERT_TOKEN_1, mAsyncHandler.getToken());
        assertEquals(INSERT_COOKIE, mAsyncHandler.getCookie());
        assertEquals(DummyProvider.CONTENT_URI, (Uri) mAsyncHandler.getResult());
        mAsyncHandler.reset();
        startInsert(INSERT_TOKEN_2, INSERT_COOKIE, DummyProvider.CONTENT_URI, values2, CANCELABLE);
        mAsyncHandler.cancelOperation(INSERT_TOKEN_2);
        assertFalse(mAsyncHandler.hadInserted(TEST_TIME_OUT));
        Cursor cursor = null;
        try {
            cursor = mResolver.query(DummyProvider.CONTENT_URI, PROJECTIONS, null, null, ORDER_BY);
            assertEquals(ORIGINAL_ROW_COUNT + 1, cursor.getCount());
            cursor.moveToLast();
            assertEquals(NAME3, cursor.getString(NAME_COLUMN_INDEX));
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
    }
    @TestTargets({
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "startQuery",
            args = {int.class, java.lang.Object.class, android.net.Uri.class,
                    java.lang.String[].class, java.lang.String.class, java.lang.String[].class,
                    java.lang.String.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "onQueryComplete",
            args = {int.class, java.lang.Object.class, android.database.Cursor.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "cancelOperation",
            args = {int.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "handleMessage",
            args = {android.os.Message.class}
        )
    })
    public void testStartQuery() throws InterruptedException {
        mAsyncHandler = createAsyncQueryHandlerSync();
        assertFalse(mAsyncHandler.hadQueried(0));
        startQuery(QUERY_TOKEN_1, QUERY_COOKIE, DummyProvider.CONTENT_URI,
                PROJECTIONS, null, null, ORDER_BY, NO_CANCEL);
        assertTrue(mAsyncHandler.hadQueried(TEST_TIME_OUT));
        assertEquals(QUERY_TOKEN_1, mAsyncHandler.getToken());
        assertEquals(QUERY_COOKIE, mAsyncHandler.getCookie());
        Cursor cursor = (Cursor) mAsyncHandler.getResult();
        assertNotNull(cursor);
        try {
            assertEquals(ORIGINAL_ROW_COUNT, cursor.getCount());
            assertEquals(2, cursor.getColumnCount());
            cursor.moveToFirst();
            assertEquals(NAME0, cursor.getString(NAME_COLUMN_INDEX));
            cursor.moveToNext();
            assertEquals(NAME1, cursor.getString(NAME_COLUMN_INDEX));
            cursor.moveToNext();
            assertEquals(NAME2, cursor.getString(NAME_COLUMN_INDEX));
        } finally {
            cursor.close();
        }
        mAsyncHandler.reset();
        startQuery(QUERY_TOKEN_2, QUERY_COOKIE, DummyProvider.CONTENT_URI,
                PROJECTIONS, null, null, ORDER_BY, CANCELABLE);
        mAsyncHandler.cancelOperation(QUERY_TOKEN_2);
        assertFalse(mAsyncHandler.hadQueried(TEST_TIME_OUT));
    }
    @TestTargets({
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "startUpdate",
            args = {int.class, java.lang.Object.class, android.net.Uri.class,
                    android.content.ContentValues.class, java.lang.String.class,
                    java.lang.String[].class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "onUpdateComplete",
            args = {int.class, java.lang.Object.class, int.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "cancelOperation",
            args = {int.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "handleMessage",
            args = {android.os.Message.class}
        )
    })
    public void testStartUpdate() throws InterruptedException {
        final ContentValues values1 = new ContentValues();
        values1.put(DummyProvider.NAME, NAME3);
        final ContentValues values2 = new ContentValues();
        values2.put(DummyProvider.NAME, NAME4);
        mAsyncHandler = createAsyncQueryHandlerSync();
        assertFalse(mAsyncHandler.hadUpdated(0));
        startUpdate(UPDATE_TOKEN_1, UPDATE_COOKIE, DummyProvider.CONTENT_URI, values1,
                DummyProvider.NAME + "=?", new String[] { NAME0 }, NO_CANCEL);
        assertTrue(mAsyncHandler.hadUpdated(TEST_TIME_OUT));
        assertEquals(UPDATE_TOKEN_1, mAsyncHandler.getToken());
        assertEquals(UPDATE_COOKIE, mAsyncHandler.getCookie());
        assertEquals(1, ((Integer) mAsyncHandler.getResult()).intValue());
        mAsyncHandler.reset();
        startUpdate(UPDATE_TOKEN_2, UPDATE_COOKIE, DummyProvider.CONTENT_URI, values2,
                DummyProvider.NAME + "=?", new String[] { NAME1 }, CANCELABLE);
        mAsyncHandler.cancelOperation(UPDATE_TOKEN_2);
        assertFalse(mAsyncHandler.hadUpdated(TEST_TIME_OUT));
        Cursor cursor = null;
        try {
            cursor = mResolver.query(DummyProvider.CONTENT_URI, PROJECTIONS, null, null, ORDER_BY);
            assertEquals(ORIGINAL_ROW_COUNT, cursor.getCount());
            cursor.moveToFirst();
            assertEquals(NAME3, cursor.getString(NAME_COLUMN_INDEX));
            cursor.moveToNext();
            assertEquals(NAME1, cursor.getString(NAME_COLUMN_INDEX));
            cursor.moveToNext();
            assertEquals(NAME2, cursor.getString(NAME_COLUMN_INDEX));
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
    }
    @TestTargets({
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "startDelete",
            args = {int.class, java.lang.Object.class, android.net.Uri.class,
                    java.lang.String.class, java.lang.String[].class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "onDeleteComplete",
            args = {int.class, java.lang.Object.class, int.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "cancelOperation",
            args = {int.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "handleMessage",
            args = {android.os.Message.class}
        )
    })
    public void testStartDelete() throws InterruptedException {
        mAsyncHandler = createAsyncQueryHandlerSync();
        assertFalse(mAsyncHandler.hadDeleted(0));
        startDelete(DELETE_TOKEN_1, DELETE_COOKIE, DummyProvider.CONTENT_URI,
                DummyProvider.NAME + "=?", new String[] { NAME0 }, NO_CANCEL);
        assertTrue(mAsyncHandler.hadDeleted(TEST_TIME_OUT));
        assertEquals(DELETE_TOKEN_1, mAsyncHandler.getToken());
        assertEquals(DELETE_COOKIE, mAsyncHandler.getCookie());
        assertEquals(1, ((Integer) mAsyncHandler.getResult()).intValue());
        mAsyncHandler.reset();
        startDelete(DELETE_TOKEN_2, DELETE_COOKIE, DummyProvider.CONTENT_URI,
                DummyProvider.NAME + "=?", new String[] { NAME1 }, CANCELABLE);
        mAsyncHandler.cancelOperation(DELETE_TOKEN_2);
        assertFalse(mAsyncHandler.hadDeleted(TEST_TIME_OUT));
        Cursor cursor = null;
        try {
            cursor = mResolver.query(DummyProvider.CONTENT_URI, PROJECTIONS, null, null, ORDER_BY);
            assertEquals(ORIGINAL_ROW_COUNT - 1, cursor.getCount());
            cursor.moveToFirst();
            assertEquals(NAME1, cursor.getString(NAME_COLUMN_INDEX));
            cursor.moveToNext();
            assertEquals(NAME2, cursor.getString(NAME_COLUMN_INDEX));
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        method = "createHandler",
        args = {android.os.Looper.class}
    )
    @ToBeFixed(bug = "1695243", explanation = "should add @throws clause into javadoc of "
            + "AsyncQueryHandlerWrapper#createHandler(Looper) when param looper is null")
    public void testCreateHandler() {
        MockAsyncQueryHandler wrapper = new MockAsyncQueryHandler(mResolver);
        Handler result = wrapper.createHandler(Looper.myLooper());
        assertNotNull(result);
        assertSame(Looper.myLooper(), result.getLooper());
        try {
            wrapper.createHandler(null);
            fail("Should throw NullPointerException if param looper is null");
        } catch (NullPointerException e) {
        }
    }
    private void startQuery(int token, Object cookie, Uri uri, String[] projection,
            String selection, String[] selectionArgs, String orderBy, boolean cancelable) {
        if (cancelable) {
            mAsyncHandler.injectMockQuery();
        }
        mAsyncHandler.startQuery(token, cookie, uri, projection,
                selection, selectionArgs, orderBy);
    }
    private void startInsert(int token, Object cookie, Uri uri,
            ContentValues initialValues, boolean cancelable) {
        if (cancelable) {
            mAsyncHandler.injectMockQuery();
        }
        mAsyncHandler.startInsert(token, cookie, uri, initialValues);
    }
    private void startUpdate(int token, Object cookie, Uri uri, ContentValues values,
            String selection, String[] selectionArgs, boolean cancelable) {
        if (cancelable) {
            mAsyncHandler.injectMockQuery();
        }
        mAsyncHandler.startUpdate(token, cookie, uri, values, selection, selectionArgs);
    }
    private void startDelete(int token, Object cookie, Uri uri, String selection,
            String[] selectionArgs, boolean cancelable) {
        if (cancelable) {
            mAsyncHandler.injectMockQuery();
        }
        mAsyncHandler.startDelete(token, cookie, uri, selection, selectionArgs);
    }
    private static class MockAsyncQueryHandler extends AsyncQueryHandler {
        private boolean mHadInserted;
        private boolean mHadDeleted;
        private boolean mHadQueried;
        private boolean mHadUpdated;
        private int mToken;
        private Object mCookie;
        private Object mResult;
        public MockAsyncQueryHandler(ContentResolver cr) {
            super(cr);
        }
        @Override
        protected Handler createHandler(Looper looper) {
            return super.createHandler(looper);
        }
        private void injectMockQuery() {
            super.startQuery(MOCK_QUERY_TOKEN, QUERY_COOKIE,
                    DummyProvider.CONTENT_URI_MOCK_OPERATION, PROJECTIONS, null, null, ORDER_BY);
        }
        @Override
        protected void onDeleteComplete(int token, Object cookie, int result) {
            super.onDeleteComplete(token, cookie, result);
            mToken = token;
            mCookie = cookie;
            mResult = Integer.valueOf(result);
            synchronized (this) {
                mHadDeleted = true;
                notify();
            }
        }
        @Override
        protected void onInsertComplete(int token, Object cookie, Uri uri) {
            super.onInsertComplete(token, cookie, uri);
            mToken = token;
            mCookie = cookie;
            mResult = uri;
            synchronized (this) {
                mHadInserted = true;
                notify();
            }
        }
        @Override
        protected void onQueryComplete(int token, Object cookie, Cursor cursor) {
            super.onQueryComplete(token, cookie, cursor);
            if (token == MOCK_QUERY_TOKEN) {
                return;
            }
            mToken = token;
            mCookie = cookie;
            mResult = cursor;
            synchronized (this) {
                mHadQueried = true;
                notify();
            }
        }
        @Override
        protected void onUpdateComplete(int token, Object cookie, int result) {
            super.onUpdateComplete(token, cookie, result);
            mToken = token;
            mCookie = cookie;
            mResult = Integer.valueOf(result);
            synchronized (this) {
                mHadUpdated = true;
                notify();
            }
        }
        public synchronized boolean hadInserted(long timeout) throws InterruptedException {
            synchronized (this) {
                if (timeout > 0 && !mHadInserted) {
                    wait(timeout);
                }
            }
            return mHadInserted;
        }
        public synchronized boolean hadUpdated(long timeout) throws InterruptedException {
            if (timeout > 0 && !mHadUpdated) {
                wait(timeout);
            }
            return mHadUpdated;
        }
        public synchronized boolean hadDeleted(long timeout) throws InterruptedException {
            if (timeout > 0 && !mHadDeleted) {
                wait(timeout);
            }
            return mHadDeleted;
        }
        public synchronized boolean hadQueried(long timeout) throws InterruptedException {
            if (timeout > 0 && !mHadQueried) {
                wait(timeout);
            }
            return mHadQueried;
        }
        public int getToken() {
            return mToken;
        }
        public Object getCookie() {
            return mCookie;
        }
        public Object getResult() {
            return mResult;
        }
        public void reset() {
            mHadInserted = false;
            mHadDeleted = false;
            mHadQueried = false;
            mHadUpdated = false;
            mToken = 0;
            mCookie = null;
            mResult = null;
        }
    }
    private MockAsyncQueryHandler createAsyncQueryHandlerSync() throws InterruptedException {
        SyncRunnable sr = new SyncRunnable(new Runnable() {
            public void run() {
                mAsyncHandler = new MockAsyncQueryHandler(mResolver);
            }
        });
        TestHandlerThread t = new TestHandlerThread(sr);
        t.start();
        sr.waitForComplete();
        return mAsyncHandler;
    }
    private static class TestHandlerThread extends HandlerThread {
        private Runnable mTarget;
        public TestHandlerThread(Runnable target) {
            super("AsyncQueryHandlerTestHandlerThread");
            mTarget = target;
        }
        @Override
        protected void onLooperPrepared() {
            super.onLooperPrepared();
            if (mTarget != null) {
                mTarget.run();
            }
        }
    }
    private static final class SyncRunnable implements Runnable {
        private final Runnable mTarget;
        private boolean mHadCompleted;
        public SyncRunnable(Runnable target) {
            mTarget = target;
        }
        public void run() {
            if (mTarget != null) {
                mTarget.run();
            }
            synchronized (this) {
                mHadCompleted = true;
                notifyAll();
            }
        }
        public synchronized void waitForComplete() throws InterruptedException {
            if (!mHadCompleted) {
                wait(TEST_TIME_OUT);
            }
        }
    }
}
