class BrowserBookmarksAdapter extends BaseAdapter {
    private String                  mCurrentPage;
    private String                  mCurrentTitle;
    private Bitmap                  mCurrentThumbnail;
    private Cursor                  mCursor;
    private int                     mCount;
    private BrowserBookmarksPage    mBookmarksPage;
    private ContentResolver         mContentResolver;
    private boolean                 mDataValid;
    private BookmarkViewMode        mViewMode;
    private boolean                 mMostVisited;
    private boolean                 mNeedsOffset;
    private int                     mExtraOffset;
    public BrowserBookmarksAdapter(BrowserBookmarksPage b, String curPage,
            String curTitle, Bitmap curThumbnail, boolean createShortcut,
            boolean mostVisited) {
        mNeedsOffset = !(createShortcut || mostVisited);
        mMostVisited = mostVisited;
        mExtraOffset = mNeedsOffset ? 1 : 0;
        mBookmarksPage = b;
        mCurrentPage = b.getResources().getString(R.string.current_page)
                + curPage;
        mCurrentTitle = curTitle;
        mCurrentThumbnail = curThumbnail;
        mContentResolver = b.getContentResolver();
        mViewMode = BookmarkViewMode.LIST;
        String whereClause;
        String orderBy = Browser.BookmarkColumns.VISITS + " DESC";
        if (mostVisited) {
            whereClause = Browser.BookmarkColumns.VISITS + " != 0";
        } else {
            whereClause = Browser.BookmarkColumns.BOOKMARK + " = 1";
        }
        mCursor = b.managedQuery(Browser.BOOKMARKS_URI,
                Browser.HISTORY_PROJECTION, whereClause, null, orderBy);
        mCursor.registerContentObserver(new ChangeObserver());
        mCursor.registerDataSetObserver(new MyDataSetObserver());
        mDataValid = true;
        notifyDataSetChanged();
        mCount = mCursor.getCount() + mExtraOffset;
    }
    public Bundle getRow(int position) {
        Bundle map = new Bundle();
        if (position < mExtraOffset || position >= mCount) {
            return map;
        }
        mCursor.moveToPosition(position- mExtraOffset);
        String url = mCursor.getString(Browser.HISTORY_PROJECTION_URL_INDEX);
        map.putString(Browser.BookmarkColumns.TITLE, 
                mCursor.getString(Browser.HISTORY_PROJECTION_TITLE_INDEX));
        map.putString(Browser.BookmarkColumns.URL, url);
        byte[] data = mCursor.getBlob(Browser.HISTORY_PROJECTION_FAVICON_INDEX);
        if (data != null) {
            map.putParcelable(Browser.BookmarkColumns.FAVICON,
                    BitmapFactory.decodeByteArray(data, 0, data.length));
        }
        map.putInt("id", mCursor.getInt(Browser.HISTORY_PROJECTION_ID_INDEX));
        return map;
    }
    public void updateRow(Bundle map) {
        int id = map.getInt("id");
        int position = -1;
        for (mCursor.moveToFirst(); !mCursor.isAfterLast(); mCursor.moveToNext()) {
            if (mCursor.getInt(Browser.HISTORY_PROJECTION_ID_INDEX) == id) {
                position = mCursor.getPosition();
                break;
            }
        }
        if (position < 0) {
            return;
        }
        mCursor.moveToPosition(position);
        ContentValues values = new ContentValues();
        String title = map.getString(Browser.BookmarkColumns.TITLE);
        if (!title.equals(mCursor
                .getString(Browser.HISTORY_PROJECTION_TITLE_INDEX))) {
            values.put(Browser.BookmarkColumns.TITLE, title);
        }
        String url = map.getString(Browser.BookmarkColumns.URL);
        if (!url.equals(mCursor.
                getString(Browser.HISTORY_PROJECTION_URL_INDEX))) {
            values.put(Browser.BookmarkColumns.URL, url);
        }
        if (map.getBoolean("invalidateThumbnail") == true) {
            values.put(Browser.BookmarkColumns.THUMBNAIL, new byte[0]);
        }
        if (values.size() > 0
                && mContentResolver.update(Browser.BOOKMARKS_URI, values,
                        "_id = " + id, null) != -1) {
            refreshList();
        }
    }
    public void deleteRow(int position) {
        if (position < mExtraOffset || position >= getCount()) {
            return;
        }
        mCursor.moveToPosition(position- mExtraOffset);
        String url = mCursor.getString(Browser.HISTORY_PROJECTION_URL_INDEX);
        String title = mCursor.getString(Browser.HISTORY_PROJECTION_TITLE_INDEX);
        Bookmarks.removeFromBookmarks(null, mContentResolver, url, title);
        refreshList();
    }
    public void deleteAllRows() {
        StringBuilder deleteIds = null;
        StringBuilder convertIds = null;
        for (mCursor.moveToFirst(); !mCursor.isAfterLast(); mCursor.moveToNext()) {
            String url = mCursor.getString(Browser.HISTORY_PROJECTION_URL_INDEX);
            WebIconDatabase.getInstance().releaseIconForPageUrl(url);
            int id = mCursor.getInt(Browser.HISTORY_PROJECTION_ID_INDEX);
            int numVisits = mCursor.getInt(Browser.HISTORY_PROJECTION_VISITS_INDEX);
            if (0 == numVisits) {
                if (deleteIds == null) {
                    deleteIds = new StringBuilder();
                    deleteIds.append("( ");
                } else {
                    deleteIds.append(" OR ( ");
                }
                deleteIds.append(BookmarkColumns._ID);
                deleteIds.append(" = ");
                deleteIds.append(id);
                deleteIds.append(" )");
            } else {
                if (convertIds == null) {
                    convertIds = new StringBuilder();
                    convertIds.append("( ");
                } else {
                    convertIds.append(" OR ( ");
                }
                convertIds.append(BookmarkColumns._ID);
                convertIds.append(" = ");
                convertIds.append(id);
                convertIds.append(" )");
            }
        }
        if (deleteIds != null) {
            mContentResolver.delete(Browser.BOOKMARKS_URI, deleteIds.toString(), 
                null);
        }
        if (convertIds != null) {
            ContentValues values = new ContentValues();
            values.put(Browser.BookmarkColumns.BOOKMARK, 0);
            mContentResolver.update(Browser.BOOKMARKS_URI, values, 
                    convertIds.toString(), null);
        }
        refreshList();
    }
    public void refreshList() {
        mCursor.requery();
        mCount = mCursor.getCount() + mExtraOffset;
        notifyDataSetChanged();
    }
     static void updateBookmarkFavicon(final ContentResolver cr,
            final String originalUrl, final String url, final Bitmap favicon) {
        new AsyncTask<Void, Void, Void>() {
            protected Void doInBackground(Void... unused) {
                final Cursor c =
                        queryBookmarksForUrl(cr, originalUrl, url, true);
                if (c == null) {
                    return null;
                }
                if (c.moveToFirst()) {
                    ContentValues values = new ContentValues();
                    final ByteArrayOutputStream os =
                            new ByteArrayOutputStream();
                    favicon.compress(Bitmap.CompressFormat.PNG, 100, os);
                    values.put(Browser.BookmarkColumns.FAVICON,
                            os.toByteArray());
                    do {
                        cr.update(ContentUris.withAppendedId(
                                Browser.BOOKMARKS_URI, c.getInt(0)),
                                values, null, null);
                    } while (c.moveToNext());
                }
                c.close();
                return null;
            }
        }.execute();
    }
     static Cursor queryBookmarksForUrl(ContentResolver cr,
            String originalUrl, String url, boolean onlyBookmarks) {
        if (cr == null || url == null) {
            return null;
        }
        if (originalUrl == null) {
            originalUrl = url;
        }
        String originalUrlNoQuery = removeQuery(originalUrl);
        String urlNoQuery = removeQuery(url);
        originalUrl = originalUrlNoQuery + '?';
        url = urlNoQuery + '?';
        final String[] selArgs = new String[] {
            originalUrlNoQuery, urlNoQuery, originalUrl, url };
        String where = BookmarkColumns.URL + " == ? OR "
                + BookmarkColumns.URL + " == ? OR "
                + BookmarkColumns.URL + " LIKE ? || '%' OR "
                + BookmarkColumns.URL + " LIKE ? || '%'";
        if (onlyBookmarks) {
            where = "(" + where + ") AND " + BookmarkColumns.BOOKMARK + " == 1";
        }
        final String[] projection =
                new String[] { Browser.BookmarkColumns._ID };
        return cr.query(Browser.BOOKMARKS_URI, projection, where, selArgs,
                null);
    }
    private static String removeQuery(String url) {
        if (url == null) {
            return null;
        }
        int query = url.indexOf('?');
        String noQuery = url;
        if (query != -1) {
            noQuery = url.substring(0, query);
        }
        return noQuery;
    }
    public int getCount() {
        if (mDataValid) {
            return mCount;
        } else {
            return 0;
        }
    }
    public boolean areAllItemsEnabled() {
        return true;
    }
    public boolean isEnabled(int position) {
        return true;
    }
    public Object getItem(int position) {
        return null;
    }
    public long getItemId(int position) {
        return position;
    }
     void switchViewMode(BookmarkViewMode viewMode) {
        mViewMode = viewMode;
    }
     void populateBookmarkItem(BookmarkItem b, int position) {
        mCursor.moveToPosition(position - mExtraOffset);
        String url = mCursor.getString(Browser.HISTORY_PROJECTION_URL_INDEX);
        b.setUrl(url);
        b.setName(mCursor.getString(Browser.HISTORY_PROJECTION_TITLE_INDEX));
        byte[] data = mCursor.getBlob(Browser.HISTORY_PROJECTION_FAVICON_INDEX);
        Bitmap bitmap = null;
        if (data == null) {
            bitmap = CombinedBookmarkHistoryActivity.getIconListenerSet()
                    .getFavicon(url);
        } else {
            bitmap = BitmapFactory.decodeByteArray(data, 0, data.length);
        }
        b.setFavicon(bitmap);
    }
    public View getView(int position, View convertView, ViewGroup parent) {
        if (!mDataValid) {
            throw new IllegalStateException(
                    "this should only be called when the cursor is valid");
        }
        if (position < 0 || position > mCount) {
            throw new AssertionError(
                    "BrowserBookmarksAdapter tried to get a view out of range");
        }
        if (mViewMode == BookmarkViewMode.GRID) {
            if (convertView == null || convertView instanceof AddNewBookmark
                    || convertView instanceof BookmarkItem) {
                LayoutInflater factory = LayoutInflater.from(mBookmarksPage);
                convertView
                        = factory.inflate(R.layout.bookmark_thumbnail, null);
            }
            View holder = convertView.findViewById(R.id.holder);
            ImageView thumb = (ImageView) convertView.findViewById(R.id.thumb);
            TextView tv = (TextView) convertView.findViewById(R.id.label);
            if (0 == position && mNeedsOffset) {
                holder.setVisibility(View.VISIBLE);
                tv.setText(mCurrentTitle);
                if (mCurrentThumbnail != null) {
                    thumb.setImageBitmap(mCurrentThumbnail);
                } else {
                    thumb.setImageResource(
                            R.drawable.browser_thumbnail);
                }
                return convertView;
            }
            holder.setVisibility(View.GONE);
            mCursor.moveToPosition(position - mExtraOffset);
            tv.setText(mCursor.getString(
                    Browser.HISTORY_PROJECTION_TITLE_INDEX));
            Bitmap thumbnail = getScreenshot(position);
            if (thumbnail == null) {
                thumb.setImageResource(R.drawable.browser_thumbnail);
            } else {
                thumb.setImageBitmap(thumbnail);
            }
            return convertView;
        }
        if (position == 0 && mNeedsOffset) {
            AddNewBookmark b;
            if (convertView instanceof AddNewBookmark) {
                b = (AddNewBookmark) convertView;
            } else {
                b = new AddNewBookmark(mBookmarksPage);
            }
            b.setUrl(mCurrentPage);
            return b;
        }
        if (mMostVisited) {
            if (convertView == null || !(convertView instanceof HistoryItem)) {
                convertView = new HistoryItem(mBookmarksPage);
            }
        } else {
            if (convertView == null || !(convertView instanceof BookmarkItem)) {
                convertView = new BookmarkItem(mBookmarksPage);
            }
        }
        bind((BookmarkItem) convertView, position);
        if (mMostVisited) {
            ((HistoryItem) convertView).setIsBookmark(
                    getIsBookmark(position));
        }
        return convertView;
    }
    public String getTitle(int position) {
        return getString(Browser.HISTORY_PROJECTION_TITLE_INDEX, position);
    }
    public String getUrl(int position) {
        return getString(Browser.HISTORY_PROJECTION_URL_INDEX, position);
    }
    public Bitmap getScreenshot(int position) {
        return getBitmap(Browser.HISTORY_PROJECTION_THUMBNAIL_INDEX, position);
    }
    public Bitmap getFavicon(int position) {
        return getBitmap(Browser.HISTORY_PROJECTION_FAVICON_INDEX, position);
    }
    public Bitmap getTouchIcon(int position) {
        return getBitmap(Browser.HISTORY_PROJECTION_TOUCH_ICON_INDEX, position);
    }
    private Bitmap getBitmap(int cursorIndex, int position) {
        if (position < mExtraOffset || position > mCount) {
            return null;
        }
        mCursor.moveToPosition(position - mExtraOffset);
        byte[] data = mCursor.getBlob(cursorIndex);
        if (data == null) {
            return null;
        }
        return BitmapFactory.decodeByteArray(data, 0, data.length);
    }
    public boolean getIsBookmark(int position) {
        if (position < mExtraOffset || position > mCount) {
            return false;
        }
        mCursor.moveToPosition(position - mExtraOffset);
        return (1 == mCursor.getInt(Browser.HISTORY_PROJECTION_BOOKMARK_INDEX));
    }
    private String getString(int cursorIndex, int position) {
        if (position < mExtraOffset || position > mCount) {
            return "";
        }
        mCursor.moveToPosition(position- mExtraOffset);
        return mCursor.getString(cursorIndex);
    }
    private void bind(BookmarkItem b, int position) {
        mCursor.moveToPosition(position- mExtraOffset);
        b.setName(mCursor.getString(Browser.HISTORY_PROJECTION_TITLE_INDEX));
        String url = mCursor.getString(Browser.HISTORY_PROJECTION_URL_INDEX);
        b.setUrl(url);
        byte[] data = mCursor.getBlob(Browser.HISTORY_PROJECTION_FAVICON_INDEX);
        if (data != null) {
            b.setFavicon(BitmapFactory.decodeByteArray(data, 0, data.length));
        } else {
            b.setFavicon(CombinedBookmarkHistoryActivity.getIconListenerSet()
                    .getFavicon(url));
        }
    }
    private class ChangeObserver extends ContentObserver {
        public ChangeObserver() {
            super(new Handler(Looper.getMainLooper()));
        }
        @Override
        public boolean deliverSelfNotifications() {
            return true;
        }
        @Override
        public void onChange(boolean selfChange) {
            refreshList();
        }
    }
    private class MyDataSetObserver extends DataSetObserver {
        @Override
        public void onChanged() {
            mDataValid = true;
            notifyDataSetChanged();
        }
        @Override
        public void onInvalidated() {
            mDataValid = false;
            notifyDataSetInvalidated();
        }
    }
}
