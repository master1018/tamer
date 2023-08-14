public abstract class SourceLatency extends Activity {
    private static final String TAG = "SourceLatency";
    private SearchManager mSearchManager;
    private ExecutorService mExecutorService;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mSearchManager = (SearchManager) getSystemService(SEARCH_SERVICE);
        mExecutorService = Executors.newSingleThreadExecutor();
    }
    @Override
    protected void onResume() {
        super.onResume();
    }
    private SearchableInfo getSearchable(ComponentName componentName) {
        SearchableInfo searchable = mSearchManager.getSearchableInfo(componentName);
        if (searchable == null || searchable.getSuggestAuthority() == null) {
            throw new RuntimeException("Component is not searchable: "
                    + componentName.flattenToShortString());
        }
        return searchable;
    }
    private static class ElapsedTime {
        private long mTotal = 0;
        private int mCount = 0;
        public synchronized void addTime(long time) {
            mTotal += time;
            mCount++;
        }
        public synchronized long getTotal() {
            return mTotal;
        }
        public synchronized long getAverage() {
            return mTotal / mCount;
        }
        public synchronized int getCount() {
            return mCount;
        }
    }
    public void checkSourceConcurrent(final String src, final ComponentName componentName,
            String query, long delay) {
        final ElapsedTime time = new ElapsedTime();
        final SearchableInfo searchable = getSearchable(componentName);
        int length = query.length();
        for (int end = 0; end <= length; end++) {
            final String prefix = query.substring(0, end);
            (new Thread() {
                @Override
                public void run() {
                    long t = checkSourceInternal(src, searchable, prefix);
                    time.addTime(t);
                }
            }).start();
            try {
                Thread.sleep(delay);
            } catch (InterruptedException ex) {
                Log.e(TAG, "sleep() in checkSourceConcurrent() interrupted.");
            }
        }
        int count = length + 1;
        while (time.getCount() < count) {
            try {
                Thread.sleep(1000);
            } catch (InterruptedException ex) {
                Log.e(TAG, "sleep() in checkSourceConcurrent() interrupted.");
            }
        }
        Log.d(TAG, src + "[DONE]: " + length + " queries in " + formatTime(time.getAverage())
                + " (average), " + formatTime(time.getTotal()) + " (total)");
    }
    public void checkSource(String src, ComponentName componentName, String[] queries) {
        ElapsedTime time = new ElapsedTime();
        int count = queries.length;
        for (int i = 0; i < queries.length; i++) {
            long t = checkSource(src, componentName, queries[i]);
            time.addTime(t);
        }
        Log.d(TAG, src + "[DONE]: " + count + " queries in " + formatTime(time.getAverage())
                + " (average), " + formatTime(time.getTotal()) + " (total)");
    }
    public long checkSource(String src, ComponentName componentName, String query) {
        SearchableInfo searchable = getSearchable(componentName);
        return checkSourceInternal(src, searchable, query);
    }
    private long checkSourceInternal(String src, SearchableInfo searchable, String query) {
        Cursor cursor = null;
        try {
            final long start = System.nanoTime();
            cursor = getSuggestions(searchable, query);
            long end = System.nanoTime();
            long elapsed = end - start;
            if (cursor == null) {
                Log.d(TAG, src + ": null cursor in " + formatTime(elapsed)
                        + " for '" + query + "'");
            } else {
                Log.d(TAG, src + ": " + cursor.getCount() + " rows in " + formatTime(elapsed)
                        + " for '" + query + "'");
            }
            return elapsed;
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
    }
    public Cursor getSuggestions(SearchableInfo searchable, String query) {
        return getSuggestions(searchable, query, -1);
    }
    public Cursor getSuggestions(SearchableInfo searchable, String query, int limit) {
        if (searchable == null) {
            return null;
        }
        String authority = searchable.getSuggestAuthority();
        if (authority == null) {
            return null;
        }
        Uri.Builder uriBuilder = new Uri.Builder()
                .scheme(ContentResolver.SCHEME_CONTENT)
                .authority(authority)
                .query("")  
                .fragment("");  
        final String contentPath = searchable.getSuggestPath();
        if (contentPath != null) {
            uriBuilder.appendEncodedPath(contentPath);
        }
        uriBuilder.appendPath(SearchManager.SUGGEST_URI_PATH_QUERY);
        String selection = searchable.getSuggestSelection();
        String[] selArgs = null;
        if (selection != null) {    
            selArgs = new String[] { query };
        } else {                    
            uriBuilder.appendPath(query);
        }
        if (limit > 0) {
            uriBuilder.appendQueryParameter(SearchManager.SUGGEST_PARAMETER_LIMIT,
                    String.valueOf(limit));
        }
        Uri uri = uriBuilder.build();
        return getContentResolver().query(uri, null, selection, selArgs, null);
    }
    private static String formatTime(long ns) {
        return (ns / 1000000.0d) + " ms";
    }
    public void checkLiveSource(String src, ComponentName componentName, String query) {
        mExecutorService.submit(new LiveSourceCheck(src, componentName, query));
    }
    private class LiveSourceCheck implements Runnable {
        private String mSrc;
        private SearchableInfo mSearchable;
        private String mQuery;
        private Handler mHandler = new Handler(Looper.getMainLooper());
        public LiveSourceCheck(String src, ComponentName componentName, String query) {
            mSrc = src;
            mSearchable = mSearchManager.getSearchableInfo(componentName);
            assert(mSearchable != null);
            assert(mSearchable.getSuggestAuthority() != null);
            mQuery = query;
        }
        public void run() {
            Cursor cursor = null;
            try {
                final long start = System.nanoTime();
                cursor = getSuggestions(mSearchable, mQuery);
                long end = System.nanoTime();
                long elapsed = (end - start);
                if (cursor == null) {
                    Log.d(TAG, mSrc + ": null cursor in " + formatTime(elapsed)
                            + " for '" + mQuery + "'");
                } else {
                    Log.d(TAG, mSrc + ": " + cursor.getCount() + " rows in " + formatTime(elapsed)
                            + " for '" + mQuery + "'");
                    cursor.registerContentObserver(new ChangeObserver(cursor));
                    cursor.registerDataSetObserver(new MyDataSetObserver(mSrc, start, cursor));
                    try {
                        Thread.sleep(2000);
                    } catch (InterruptedException ex) {
                        Log.d(TAG, mSrc + ": interrupted");
                    }
                }
            } finally {
                if (cursor != null) {
                    cursor.close();
                }
            }
        }
        private class ChangeObserver extends ContentObserver {
            private Cursor mCursor;
            public ChangeObserver(Cursor cursor) {
                super(mHandler);
                mCursor = cursor;
            }
            @Override
            public boolean deliverSelfNotifications() {
                return true;
            }
            @Override
            public void onChange(boolean selfChange) {
                mCursor.requery();
            }
        }
        private class MyDataSetObserver extends DataSetObserver {
            private long mStart;
            private Cursor mCursor;
            private int mUpdateCount = 0;
            public MyDataSetObserver(String src, long start, Cursor cursor) {
                mSrc = src;
                mStart = start;
                mCursor = cursor;
            }
            @Override
            public void onChanged() {
                long end = System.nanoTime();
                long elapsed = end - mStart;
                mUpdateCount++;
                Log.d(TAG, mSrc + ", update " + mUpdateCount + ": " + mCursor.getCount()
                        + " rows in " + formatTime(elapsed));
            }
            @Override
            public void onInvalidated() {
                Log.d(TAG, mSrc + ": invalidated");
            }
        }
    }
}
