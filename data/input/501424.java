public class BookmarkWidgetService extends Service
        implements UrlRenderer.Callback {
    private static final String TAG = "BookmarkWidgetService";
    public static final String UPDATE = "com.android.browser.widget.UPDATE";
    private static final String NEXT = "com.android.browser.widget.NEXT";
    private static final String PREV = "com.android.browser.widget.PREV";
    private static final String EXTRA_ID =
            "com.android.browser.widget.extra.ID";
    private static final int WIDTH = 306;
    private static final int HEIGHT = 386;
    private static final int MAX_SERVICE_RETRY_COUNT = 5;
    private static final int NO_ID = -1;
    private static final int MSG_UPDATE = 0;
    private final Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case MSG_UPDATE:
                    if (mRenderer != null) {
                        queryCursorAndRender();
                    } else {
                        if (++mServiceRetryCount <= MAX_SERVICE_RETRY_COUNT) {
                            mHandler.sendEmptyMessageDelayed(MSG_UPDATE, 1000);
                        }
                    }
                    break;
                default:
                    break;
            }
        }
    };
    private final ServiceConnection mConnection = new ServiceConnection() {
        public void onServiceConnected(ComponentName className,
                IBinder service) {
            mRenderer = new UrlRenderer(service);
        }
        public void onServiceDisconnected(ComponentName className) {
            mRenderer = null;
        }
    };
    private final HashMap<Integer, RenderResult> mIdsToResults =
            new HashMap<Integer, RenderResult>();
    private final ArrayList<Integer> mIdList = new ArrayList<Integer>();
    private final HashMap<String, Integer> mUrlsToIds =
            new HashMap<String, Integer>();
    private int mCurrentId = NO_ID;
    private UrlRenderer mRenderer;
    private int mServiceRetryCount;
    @Override
    public void onCreate() {
        bindService(new Intent(UrlRendererService.SERVICE_INTERFACE),
                mConnection, Context.BIND_AUTO_CREATE);
    }
    @Override
    public void onDestroy() {
        unbindService(mConnection);
    }
    @Override
    public android.os.IBinder onBind(Intent intent) {
        return null;
    }
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        final String action = intent.getAction();
        if (UPDATE.equals(action)) {
            mHandler.sendEmptyMessage(MSG_UPDATE);
        } else if (PREV.equals(action) && mIdList.size() > 1) {
            int prev = getPreviousId(intent);
            if (prev == NO_ID) {
                Log.d(TAG, "Could not determine previous id");
                return START_NOT_STICKY;
            }
            RenderResult res = mIdsToResults.get(prev);
            if (res != null) {
                updateWidget(res);
            }
        } else if (NEXT.equals(action) && mIdList.size() > 1) {
            int next = getNextId(intent);
            if (next == NO_ID) {
                Log.d(TAG, "Could not determine next id");
                return START_NOT_STICKY;
            }
            RenderResult res = mIdsToResults.get(next);
            if (res != null) {
                updateWidget(res);
            }
        }
        return START_STICKY;
    }
    private int getPreviousId(Intent intent) {
        int listSize = mIdList.size();
        if (listSize <= 1) {
            return NO_ID;
        }
        int curr = intent.getIntExtra(EXTRA_ID, NO_ID);
        if (curr == NO_ID) {
            return NO_ID;
        }
        if (mIdList.get(0) == curr) {
            return mIdList.get(listSize - 1);
        }
        int prev = NO_ID;
        for (int id : mIdList) {
            if (id == curr) {
                break;
            }
            prev = id;
        }
        return prev;
    }
    private int getNextId(Intent intent) {
        int listSize = mIdList.size();
        if (listSize <= 1) {
            return NO_ID;
        }
        int curr = intent.getIntExtra(EXTRA_ID, NO_ID);
        if (curr == NO_ID) {
            return NO_ID;
        }
        if (mIdList.get(listSize - 1) == curr) {
            return mIdList.get(0);
        }
        int i = 1;
        for (int id : mIdList) {
            if (id == curr) {
                break;
            }
            i++;
        }
        return mIdList.get(i);
    }
    private void updateWidget(RenderResult res) {
        RemoteViews views = new RemoteViews(getPackageName(),
                R.layout.bookmarkwidget);
        Intent prev = new Intent(PREV, null, this, BookmarkWidgetService.class);
        prev.putExtra(EXTRA_ID, res.mId);
        views.setOnClickPendingIntent(R.id.previous,
                PendingIntent.getService(this, 0, prev,
                    PendingIntent.FLAG_CANCEL_CURRENT));
        Intent next = new Intent(NEXT, null, this, BookmarkWidgetService.class);
        next.putExtra(EXTRA_ID, res.mId);
        views.setOnClickPendingIntent(R.id.next,
                PendingIntent.getService(this, 0, next,
                    PendingIntent.FLAG_CANCEL_CURRENT));
        String displayTitle = res.mTitle;
        if (displayTitle == null) {
            displayTitle = res.mUrl;
        }
        views.setTextViewText(R.id.title, displayTitle);
        if (res.mBitmap != null) {
            views.setImageViewBitmap(R.id.image, res.mBitmap);
            views.setViewVisibility(R.id.image, View.VISIBLE);
            views.setViewVisibility(R.id.progress, View.GONE);
        } else {
            views.setViewVisibility(R.id.progress, View.VISIBLE);
            views.setViewVisibility(R.id.image, View.GONE);
        }
        mCurrentId = res.mId;
        AppWidgetManager.getInstance(this).updateAppWidget(
                new ComponentName(this, BookmarkWidgetProvider.class),
                views);
    }
    private static final String QUERY_WHERE =
            BookmarkColumns.BOOKMARK + " == 1";
    private static final String[] PROJECTION = new String[] {
            BookmarkColumns._ID, BookmarkColumns.TITLE, BookmarkColumns.URL };
    private static class RenderResult {
        final int    mId;
        final String mTitle;
        final String mUrl;
        Bitmap       mBitmap;
        RenderResult(int id, String title, String url) {
            mId = id;
            mTitle = title;
            mUrl = url;
        }
    }
    private void queryCursorAndRender() {
        mIdList.clear();
        mIdsToResults.clear();
        Cursor c = getContentResolver().query(Browser.BOOKMARKS_URI, PROJECTION,
                QUERY_WHERE, null, null);
        if (c != null) {
            if (c.moveToFirst()) {
                ArrayList<String> urls = new ArrayList<String>(c.getCount());
                boolean sawCurrentId = false;
                do {
                    int id = c.getInt(0);
                    String title = c.getString(1);
                    String url = c.getString(2);
                    mIdList.add(id);
                    mUrlsToIds.put(url, id);
                    if (mCurrentId == id) {
                        sawCurrentId = true;
                    }
                    RenderResult res = new RenderResult(id, title, url);
                    mIdsToResults.put(id, res);
                    urls.add(url);
                } while (c.moveToNext());
                mRenderer.render(urls, WIDTH, HEIGHT, this);
                if (!sawCurrentId) {
                    mCurrentId = mIdList.get(0);
                }
            }
            c.close();
        }
    }
    public void complete(String url, ParcelFileDescriptor result) {
        int id = mUrlsToIds.get(url);
        if (id == NO_ID) {
            Log.d(TAG, "No matching id found during completion of "
                    + url);
            return;
        }
        RenderResult res = mIdsToResults.get(id);
        if (res == null) {
            Log.d(TAG, "No result found during completion of "
                    + url);
            return;
        }
        if (result != null) {
            InputStream input =
                    new ParcelFileDescriptor.AutoCloseInputStream(result);
            Bitmap orig = BitmapFactory.decodeStream(input, null, null);
            res.mBitmap = Bitmap.createScaledBitmap(orig, WIDTH, HEIGHT, true);
            try {
                input.close();
            } catch (IOException e) {
            }
        }
        if (mCurrentId == id) {
            updateWidget(res);
        }
    }
}
