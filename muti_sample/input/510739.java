public class Browser {
    private static final String LOGTAG = "browser";
    public static final Uri BOOKMARKS_URI =
        Uri.parse("content:
    public static final String INITIAL_ZOOM_LEVEL = "browser.initialZoomLevel";
    public static final String EXTRA_APPLICATION_ID =
        "com.android.browser.application_id";
    public static final String EXTRA_HEADERS = "com.android.browser.headers";
    public static final String[] HISTORY_PROJECTION = new String[] {
        BookmarkColumns._ID, BookmarkColumns.URL, BookmarkColumns.VISITS,
        BookmarkColumns.DATE, BookmarkColumns.BOOKMARK, BookmarkColumns.TITLE,
        BookmarkColumns.FAVICON, BookmarkColumns.THUMBNAIL,
        BookmarkColumns.TOUCH_ICON, BookmarkColumns.USER_ENTERED };
    public static final int HISTORY_PROJECTION_ID_INDEX = 0;
    public static final int HISTORY_PROJECTION_URL_INDEX = 1;
    public static final int HISTORY_PROJECTION_VISITS_INDEX = 2;
    public static final int HISTORY_PROJECTION_DATE_INDEX = 3;
    public static final int HISTORY_PROJECTION_BOOKMARK_INDEX = 4;
    public static final int HISTORY_PROJECTION_TITLE_INDEX = 5;
    public static final int HISTORY_PROJECTION_FAVICON_INDEX = 6;
    public static final int HISTORY_PROJECTION_THUMBNAIL_INDEX = 7;
    public static final int HISTORY_PROJECTION_TOUCH_ICON_INDEX = 8;
    public static final String[] TRUNCATE_HISTORY_PROJECTION = new String[] {
        BookmarkColumns._ID, BookmarkColumns.DATE, };
    public static final int TRUNCATE_HISTORY_PROJECTION_ID_INDEX = 0;
    public static final int TRUNCATE_N_OLDEST = 5;
    public static final Uri SEARCHES_URI =
        Uri.parse("content:
    public static final String[] SEARCHES_PROJECTION = new String[] {
        SearchColumns._ID, SearchColumns.SEARCH, SearchColumns.DATE };
    public static final int SEARCHES_PROJECTION_SEARCH_INDEX = 1;
    public static final int SEARCHES_PROJECTION_DATE_INDEX = 2;
    private static final String SEARCHES_WHERE_CLAUSE = "search = ?";
    private static final int MAX_HISTORY_COUNT = 250;
    public static final void saveBookmark(Context c, 
                                          String title, 
                                          String url) {
        Intent i = new Intent(Intent.ACTION_INSERT, Browser.BOOKMARKS_URI);
        i.putExtra("title", title);
        i.putExtra("url", url);
        c.startActivity(i);
    }
    public final static String EXTRA_SHARE_SCREENSHOT = "share_screenshot";
    public final static String EXTRA_SHARE_FAVICON = "share_favicon";
    public static final void sendString(Context c, String s) {
        sendString(c, s, c.getString(com.android.internal.R.string.sendText));
    }
    public static final void sendString(Context c,
                                        String stringToSend,
                                        String chooserDialogTitle) {
        Intent send = new Intent(Intent.ACTION_SEND);
        send.setType("text/plain");
        send.putExtra(Intent.EXTRA_TEXT, stringToSend);
        try {
            c.startActivity(Intent.createChooser(send, chooserDialogTitle));
        } catch(android.content.ActivityNotFoundException ex) {
        }
    }
    public static final Cursor getAllBookmarks(ContentResolver cr) throws 
            IllegalStateException {
        return cr.query(BOOKMARKS_URI,
                new String[] { BookmarkColumns.URL }, 
                "bookmark = 1", null, null);
    }
    public static final Cursor getAllVisitedUrls(ContentResolver cr) throws
            IllegalStateException {
        return cr.query(BOOKMARKS_URI,
                new String[] { BookmarkColumns.URL }, null, null, null);
    }
    private static final void addOrUrlEquals(StringBuilder sb) {
        sb.append(" OR " + BookmarkColumns.URL + " = ");
    }
    public static final Cursor getVisitedLike(ContentResolver cr, String url) {
        boolean secure = false;
        String compareString = url;
        if (compareString.startsWith("http:
            compareString = compareString.substring(7);
        } else if (compareString.startsWith("https:
            compareString = compareString.substring(8);
            secure = true;
        }
        if (compareString.startsWith("www.")) {
            compareString = compareString.substring(4);
        }
        StringBuilder whereClause = null;
        if (secure) {
            whereClause = new StringBuilder(BookmarkColumns.URL + " = ");
            DatabaseUtils.appendEscapedSQLString(whereClause,
                    "https:
            addOrUrlEquals(whereClause);
            DatabaseUtils.appendEscapedSQLString(whereClause,
                    "https:
        } else {
            whereClause = new StringBuilder(BookmarkColumns.URL + " = ");
            DatabaseUtils.appendEscapedSQLString(whereClause,
                    compareString);
            addOrUrlEquals(whereClause);
            String wwwString = "www." + compareString;
            DatabaseUtils.appendEscapedSQLString(whereClause,
                    wwwString);
            addOrUrlEquals(whereClause);
            DatabaseUtils.appendEscapedSQLString(whereClause,
                    "http:
            addOrUrlEquals(whereClause);
            DatabaseUtils.appendEscapedSQLString(whereClause,
                    "http:
        }
        return cr.query(BOOKMARKS_URI, HISTORY_PROJECTION,
                whereClause.toString(), null, null);
    }
    public static final void updateVisitedHistory(ContentResolver cr,
                                                  String url, boolean real) {
        long now = new Date().getTime();
        Cursor c = null;
        try {
            c = getVisitedLike(cr, url);
            if (c.moveToFirst()) {
                ContentValues map = new ContentValues();
                if (real) {
                    map.put(BookmarkColumns.VISITS, c
                            .getInt(HISTORY_PROJECTION_VISITS_INDEX) + 1);
                } else {
                    map.put(BookmarkColumns.USER_ENTERED, 1);
                }
                map.put(BookmarkColumns.DATE, now);
                String[] projection = new String[]
                        { Integer.valueOf(c.getInt(0)).toString() };
                cr.update(BOOKMARKS_URI, map, "_id = ?", projection);
            } else {
                truncateHistory(cr);
                ContentValues map = new ContentValues();
                int visits;
                int user_entered;
                if (real) {
                    visits = 1;
                    user_entered = 0;
                } else {
                    visits = 0;
                    user_entered = 1;
                }
                map.put(BookmarkColumns.URL, url);
                map.put(BookmarkColumns.VISITS, visits);
                map.put(BookmarkColumns.DATE, now);
                map.put(BookmarkColumns.BOOKMARK, 0);
                map.put(BookmarkColumns.TITLE, url);
                map.put(BookmarkColumns.CREATED, 0);
                map.put(BookmarkColumns.USER_ENTERED, user_entered);
                cr.insert(BOOKMARKS_URI, map);
            }
        } catch (IllegalStateException e) {
            Log.e(LOGTAG, "updateVisitedHistory", e);
        } finally {
            if (c != null) c.close();
        }
    }
    public static final String[] getVisitedHistory(ContentResolver cr) {
        Cursor c = null;
        String[] str = null;
        try {
            String[] projection = new String[] {
                "url"
            };
            c = cr.query(BOOKMARKS_URI, projection, "visits > 0", null,
                    null);
            str = new String[c.getCount()];
            int i = 0;
            while (c.moveToNext()) {
                str[i] = c.getString(0);
                i++;
            }
        } catch (IllegalStateException e) {
            Log.e(LOGTAG, "getVisitedHistory", e);
            str = new String[0];
        } finally {
            if (c != null) c.close();
        }
        return str;
    }
    public static final void truncateHistory(ContentResolver cr) {
        Cursor c = null;
        try {
            c = cr.query(
                    BOOKMARKS_URI,
                    TRUNCATE_HISTORY_PROJECTION,
                    "bookmark = 0",
                    null,
                    BookmarkColumns.DATE);
            if (c.moveToFirst() && c.getCount() >= MAX_HISTORY_COUNT) {
                for (int i = 0; i < TRUNCATE_N_OLDEST; i++) {
                    cr.delete(BOOKMARKS_URI, "_id = " +
                            c.getInt(TRUNCATE_HISTORY_PROJECTION_ID_INDEX),
                            null);
                    if (!c.moveToNext()) break;
                }
            }
        } catch (IllegalStateException e) {
            Log.e(LOGTAG, "truncateHistory", e);
        } finally {
            if (c != null) c.close();
        }
    }
    public static final boolean canClearHistory(ContentResolver cr) {
        Cursor c = null;
        boolean ret = false;
        try {
            c = cr.query(
                BOOKMARKS_URI,
                new String [] { BookmarkColumns._ID, 
                                BookmarkColumns.BOOKMARK,
                                BookmarkColumns.VISITS },
                "bookmark = 0 OR visits > 0", 
                null,
                null
                );
            ret = c.moveToFirst();
        } catch (IllegalStateException e) {
            Log.e(LOGTAG, "canClearHistory", e);
        } finally {
            if (c != null) c.close();
        }
        return ret;
    }
    public static final void clearHistory(ContentResolver cr) {
        deleteHistoryWhere(cr, null);
    }
    private static final void deleteHistoryWhere(ContentResolver cr,
            String whereClause) {
        Cursor c = null;
        try {
            c = cr.query(BOOKMARKS_URI,
                HISTORY_PROJECTION,
                whereClause,
                null,
                null);
            if (c.moveToFirst()) {
                final WebIconDatabase iconDb = WebIconDatabase.getInstance();
                StringBuffer sb = new StringBuffer();
                boolean firstTime = true;
                do {
                    String url = c.getString(HISTORY_PROJECTION_URL_INDEX);
                    boolean isBookmark =
                        c.getInt(HISTORY_PROJECTION_BOOKMARK_INDEX) == 1;
                    if (isBookmark) {
                        if (firstTime) {
                            firstTime = false;
                        } else {
                            sb.append(" OR ");
                        }
                        sb.append("( _id = ");
                        sb.append(c.getInt(0));
                        sb.append(" )");
                    } else {
                        iconDb.releaseIconForPageUrl(url);
                    }
                } while (c.moveToNext());
                if (!firstTime) {
                    ContentValues map = new ContentValues();
                    map.put(BookmarkColumns.VISITS, 0);
                    map.put(BookmarkColumns.DATE, 0);
                    cr.update(BOOKMARKS_URI, map, sb.toString(), null);
                }
                String deleteWhereClause = BookmarkColumns.BOOKMARK + " = 0";
                if (whereClause != null) {
                    deleteWhereClause += " AND " + whereClause;
                }
                cr.delete(BOOKMARKS_URI, deleteWhereClause, null);
            }
        } catch (IllegalStateException e) {
            Log.e(LOGTAG, "deleteHistoryWhere", e);
            return;
        } finally {
            if (c != null) c.close();
        }
    }
    public static final void deleteHistoryTimeFrame(ContentResolver cr,
            long begin, long end) {
        String whereClause;
        String date = BookmarkColumns.DATE;
        if (-1 == begin) {
            if (-1 == end) {
                clearHistory(cr);
                return;
            }
            whereClause = date + " < " + Long.toString(end);
        } else if (-1 == end) {
            whereClause = date + " >= " + Long.toString(begin);
        } else {
            whereClause = date + " >= " + Long.toString(begin) + " AND " + date
                    + " < " + Long.toString(end);
        }
        deleteHistoryWhere(cr, whereClause);
    }
    public static final void deleteFromHistory(ContentResolver cr, 
                                               String url) {
        StringBuilder sb = new StringBuilder(BookmarkColumns.URL + " = ");
        DatabaseUtils.appendEscapedSQLString(sb, url);
        String matchesUrl = sb.toString();
        deleteHistoryWhere(cr, matchesUrl);
    }
    public static final void addSearchUrl(ContentResolver cr, String search) {
        long now = new Date().getTime();
        Cursor c = null;
        try {
            c = cr.query(
                SEARCHES_URI,
                SEARCHES_PROJECTION,
                SEARCHES_WHERE_CLAUSE,
                new String [] { search },
                null);
            ContentValues map = new ContentValues();
            map.put(SearchColumns.SEARCH, search);
            map.put(SearchColumns.DATE, now);
            if (c.moveToFirst()) {
                cr.update(SEARCHES_URI, map, "_id = " + c.getInt(0), null);
            } else {
                cr.insert(SEARCHES_URI, map);
            }
        } catch (IllegalStateException e) {
            Log.e(LOGTAG, "addSearchUrl", e);
        } finally {
            if (c != null) c.close();
        }
    }
    public static final void clearSearches(ContentResolver cr) {
        try {
            cr.delete(SEARCHES_URI, null, null);
        } catch (IllegalStateException e) {
            Log.e(LOGTAG, "clearSearches", e);
        }
    }
    public static final void requestAllIcons(ContentResolver cr, String where,
            WebIconDatabase.IconListener listener) {
        WebIconDatabase.getInstance()
                .bulkRequestIconForPageUrl(cr, where, listener);
    }
    public static class BookmarkColumns implements BaseColumns {
        public static final String URL = "url";
        public static final String VISITS = "visits";
        public static final String DATE = "date";
        public static final String BOOKMARK = "bookmark";
        public static final String TITLE = "title";
        public static final String CREATED = "created";
        public static final String FAVICON = "favicon";
        public static final String THUMBNAIL = "thumbnail";
        public static final String TOUCH_ICON = "touch_icon";
        public static final String USER_ENTERED = "user_entered";
    }
    public static class SearchColumns implements BaseColumns {
        public static final String URL = "url";
        public static final String SEARCH = "search";
        public static final String DATE = "date";
    }
}
