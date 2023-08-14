 class Bookmarks {
    private static final String acceptableBookmarkSchemes[] = {
            "http:",
            "https:",
            "about:",
            "data:",
            "javascript:",
            "file:",
            "content:"
    };
    private final static String LOGTAG = "Bookmarks";
     static void addBookmark(Context context,
            ContentResolver cr, String url, String name,
            Bitmap thumbnail, boolean retainIcon) {
        long creationTime = new Date().getTime();
        ContentValues map = new ContentValues();
        Cursor cursor = null;
        try {
            cursor = Browser.getVisitedLike(cr, url);
            if (cursor.moveToFirst() && cursor.getInt(
                    Browser.HISTORY_PROJECTION_BOOKMARK_INDEX) == 0) {
                map.put(Browser.BookmarkColumns.CREATED, creationTime);
                map.put(Browser.BookmarkColumns.TITLE, name);
                map.put(Browser.BookmarkColumns.BOOKMARK, 1);
                map.put(Browser.BookmarkColumns.THUMBNAIL,
                        bitmapToBytes(thumbnail));
                cr.update(Browser.BOOKMARKS_URI, map,
                        "_id = " + cursor.getInt(0), null);
            } else {
                int count = cursor.getCount();
                boolean matchedTitle = false;
                for (int i = 0; i < count; i++) {
                    cursor.moveToPosition(i);
                    if (cursor.getString(Browser.HISTORY_PROJECTION_TITLE_INDEX)
                            .equals(name)) {
                        map.put(Browser.BookmarkColumns.CREATED,
                                creationTime);
                        cr.update(Browser.BOOKMARKS_URI, map,
                                "_id = " + cursor.getInt(0), null);
                        matchedTitle = true;
                        break;
                    }
                }
                if (!matchedTitle) {
                    map.put(Browser.BookmarkColumns.TITLE, name);
                    map.put(Browser.BookmarkColumns.URL, url);
                    map.put(Browser.BookmarkColumns.CREATED, creationTime);
                    map.put(Browser.BookmarkColumns.BOOKMARK, 1);
                    map.put(Browser.BookmarkColumns.DATE, 0);
                    map.put(Browser.BookmarkColumns.THUMBNAIL,
                            bitmapToBytes(thumbnail));
                    int visits = 0;
                    if (count > 0) {
                        visits = cursor.getInt(
                                Browser.HISTORY_PROJECTION_VISITS_INDEX);
                    }
                    map.put(Browser.BookmarkColumns.VISITS, visits + 3);
                    cr.insert(Browser.BOOKMARKS_URI, map);
                }
            }
        } catch (IllegalStateException e) {
            Log.e(LOGTAG, "addBookmark", e);
        } finally {
            if (cursor != null) cursor.close();
        }
        if (retainIcon) {
            WebIconDatabase.getInstance().retainIconForPageUrl(url);
        }
        if (context != null) {
            Toast.makeText(context, R.string.added_to_bookmarks,
                    Toast.LENGTH_LONG).show();
        }
    }
     static void removeFromBookmarks(Context context,
            ContentResolver cr, String url, String title) {
        Cursor cursor = null;
        try {
            cursor = cr.query(
                    Browser.BOOKMARKS_URI,
                    Browser.HISTORY_PROJECTION,
                    "url = ? AND title = ?",
                    new String[] { url, title },
                    null);
            boolean first = cursor.moveToFirst();
            if (!first) {
                throw new AssertionError("URL is not in the database! " + url
                        + " " + title);
            }
            WebIconDatabase.getInstance().releaseIconForPageUrl(url);
            Uri uri = ContentUris.withAppendedId(Browser.BOOKMARKS_URI,
                    cursor.getInt(Browser.HISTORY_PROJECTION_ID_INDEX));
            int numVisits = cursor.getInt(
                    Browser.HISTORY_PROJECTION_VISITS_INDEX);
            if (0 == numVisits) {
                cr.delete(uri, null, null);
            } else {
                ContentValues values = new ContentValues();
                values.put(Browser.BookmarkColumns.BOOKMARK, 0);
                try {
                    cr.update(uri, values, null, null);
                } catch (IllegalStateException e) {
                    Log.e("removeFromBookmarks", "no database!");
                }
            }
            if (context != null) {
                Toast.makeText(context, R.string.removed_from_bookmarks,
                        Toast.LENGTH_LONG).show();
            }
        } catch (IllegalStateException e) {
            Log.e(LOGTAG, "removeFromBookmarks", e);
        } finally {
            if (cursor != null) cursor.close();
        }
    }
    private static byte[] bitmapToBytes(Bitmap bm) {
        if (bm == null) {
            return null;
        }
        final ByteArrayOutputStream os = new ByteArrayOutputStream();
        bm.compress(Bitmap.CompressFormat.PNG, 100, os);
        return os.toByteArray();
    }
     static boolean urlHasAcceptableScheme(String url) {
        if (url == null) {
            return false;
        }
        for (int i = 0; i < acceptableBookmarkSchemes.length; i++) {
            if (url.startsWith(acceptableBookmarkSchemes[i])) {
                return true;
            }
        }
        return false;
    }
}
