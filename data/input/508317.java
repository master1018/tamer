class DownloadTouchIcon extends AsyncTask<String, Void, Void> {
    private final ContentResolver mContentResolver;
    private Cursor mCursor;
    private final String mOriginalUrl;
    private final String mUrl;
    private final String mUserAgent;
     Tab mTab;
    public DownloadTouchIcon(Tab tab, ContentResolver cr, WebView view) {
        mTab = tab;
        mContentResolver = cr;
        mOriginalUrl = view.getOriginalUrl();
        mUrl = view.getUrl();
        mUserAgent = view.getSettings().getUserAgentString();
    }
    public DownloadTouchIcon(ContentResolver cr, String url) {
        mTab = null;
        mContentResolver = cr;
        mOriginalUrl = null;
        mUrl = url;
        mUserAgent = null;
    }
    @Override
    public Void doInBackground(String... values) {
        mCursor = BrowserBookmarksAdapter.queryBookmarksForUrl(mContentResolver,
                mOriginalUrl, mUrl, true);
        if (mCursor != null && mCursor.getCount() > 0) {
            String url = values[0];
            AndroidHttpClient client = AndroidHttpClient.newInstance(
                    mUserAgent);
            HttpGet request = new HttpGet(url);
            HttpClientParams.setRedirecting(client.getParams(), true);
            try {
                HttpResponse response = client.execute(request);
                if (response.getStatusLine().getStatusCode() == 200) {
                    HttpEntity entity = response.getEntity();
                    if (entity != null) {
                        InputStream content = entity.getContent();
                        if (content != null) {
                            Bitmap icon = BitmapFactory.decodeStream(
                                    content, null, null);
                            storeIcon(icon);
                        }
                    }
                }
            } catch (IllegalArgumentException ex) {
                request.abort();
            } catch (IOException ex) {
                request.abort();
            } finally {
                client.close();
            }
        }
        if (mCursor != null) {
            mCursor.close();
        }
        return null;
    }
    @Override
    protected void onCancelled() {
        if (mCursor != null) {
            mCursor.close();
        }
    }
    private void storeIcon(Bitmap icon) {
        if (mTab != null) {
            mTab.mTouchIconLoader = null;
        }
        if (icon == null || mCursor == null || isCancelled()) {
            return;
        }
        final ByteArrayOutputStream os = new ByteArrayOutputStream();
        icon.compress(Bitmap.CompressFormat.PNG, 100, os);
        ContentValues values = new ContentValues();
        values.put(Browser.BookmarkColumns.TOUCH_ICON,
                os.toByteArray());
        if (mCursor.moveToFirst()) {
            do {
                mContentResolver.update(ContentUris.withAppendedId(
                        Browser.BOOKMARKS_URI, mCursor.getInt(0)),
                        values, null, null);
            } while (mCursor.moveToNext());
        }
    }
}
