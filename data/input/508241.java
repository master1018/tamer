public final class WebIconDatabase {
    private static final String LOGTAG = "WebIconDatabase";
    private static WebIconDatabase sIconDatabase;
    private final EventHandler mEventHandler = new EventHandler();
    private static class EventHandler extends Handler {
        static final int OPEN         = 0;
        static final int CLOSE        = 1;
        static final int REMOVE_ALL   = 2;
        static final int REQUEST_ICON = 3;
        static final int RETAIN_ICON  = 4;
        static final int RELEASE_ICON = 5;
        static final int BULK_REQUEST_ICON = 6;
        private static final int ICON_RESULT = 10;
        private Handler mHandler;
        private Vector<Message> mMessages = new Vector<Message>();
        private class IconResult {
            private final String mUrl;
            private final Bitmap mIcon;
            private final IconListener mListener;
            IconResult(String url, Bitmap icon, IconListener l) {
                mUrl = url;
                mIcon = icon;
                mListener = l;
            }
            void dispatch() {
                mListener.onReceivedIcon(mUrl, mIcon);
            }
        }
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case ICON_RESULT:
                    ((IconResult) msg.obj).dispatch();
                    break;
            }
        }
        private synchronized void createHandler() {
            if (mHandler == null) {
                mHandler = new Handler() {
                    @Override
                    public void handleMessage(Message msg) {
                        switch (msg.what) {
                            case OPEN:
                                nativeOpen((String) msg.obj);
                                break;
                            case CLOSE:
                                nativeClose();
                                break;
                            case REMOVE_ALL:
                                nativeRemoveAllIcons();
                                break;
                            case REQUEST_ICON:
                                IconListener l = (IconListener) msg.obj;
                                String url = msg.getData().getString("url");
                                requestIconAndSendResult(url, l);
                                break;
                            case BULK_REQUEST_ICON:
                                bulkRequestIcons(msg);
                                break;
                            case RETAIN_ICON:
                                nativeRetainIconForPageUrl((String) msg.obj);
                                break;
                            case RELEASE_ICON:
                                nativeReleaseIconForPageUrl((String) msg.obj);
                                break;
                        }
                    }
                };
                for (int size = mMessages.size(); size > 0; size--) {
                    mHandler.sendMessage(mMessages.remove(0));
                }
                mMessages = null;
            }
        }
        private synchronized boolean hasHandler() {
            return mHandler != null;
        }
        private synchronized void postMessage(Message msg) {
            if (mMessages != null) {
                mMessages.add(msg);
            } else {
                mHandler.sendMessage(msg);
            }
        }
        private void bulkRequestIcons(Message msg) {
            HashMap map = (HashMap) msg.obj;
            IconListener listener = (IconListener) map.get("listener");
            ContentResolver cr = (ContentResolver) map.get("contentResolver");
            String where = (String) map.get("where");
            Cursor c = null;
            try {
                c = cr.query(
                        Browser.BOOKMARKS_URI,
                        new String[] { Browser.BookmarkColumns.URL },
                        where, null, null);
                if (c.moveToFirst()) {
                    do {
                        String url = c.getString(0);
                        requestIconAndSendResult(url, listener);
                    } while (c.moveToNext());
                }
            } catch (IllegalStateException e) {
                Log.e(LOGTAG, "BulkRequestIcons", e);
            } finally {
                if (c != null) c.close();
            }
        }
        private void requestIconAndSendResult(String url, IconListener listener) {
            Bitmap icon = nativeIconForPageUrl(url);
            if (icon != null) {
                sendMessage(obtainMessage(ICON_RESULT,
                            new IconResult(url, icon, listener)));
            }
        }
    }
    public interface IconListener {
        public void onReceivedIcon(String url, Bitmap icon);
    }
    public void open(String path) {
        if (path != null) {
            mEventHandler.postMessage(
                    Message.obtain(null, EventHandler.OPEN, path));
        }
    }
    public void close() {
        mEventHandler.postMessage(
                Message.obtain(null, EventHandler.CLOSE));
    }
    public void removeAllIcons() {
        mEventHandler.postMessage(
                Message.obtain(null, EventHandler.REMOVE_ALL));
    }
    public void requestIconForPageUrl(String url, IconListener listener) {
        if (listener == null || url == null) {
            return;
        }
        Message msg = Message.obtain(null, EventHandler.REQUEST_ICON, listener);
        msg.getData().putString("url", url);
        mEventHandler.postMessage(msg);
    }
    public void bulkRequestIconForPageUrl(ContentResolver cr, String where,
            IconListener listener) {
        if (listener == null) {
            return;
        }
        if (mEventHandler.hasHandler()) {
            HashMap<String, Object> map = new HashMap<String, Object>();
            map.put("contentResolver", cr);
            map.put("where", where);
            map.put("listener", listener);
            Message msg =
                    Message.obtain(null, EventHandler.BULK_REQUEST_ICON, map);
            mEventHandler.postMessage(msg);
        }
    }
    public void retainIconForPageUrl(String url) {
        if (url != null) {
            mEventHandler.postMessage(
                    Message.obtain(null, EventHandler.RETAIN_ICON, url));
        }
    }
    public void releaseIconForPageUrl(String url) {
        if (url != null) {
            mEventHandler.postMessage(
                    Message.obtain(null, EventHandler.RELEASE_ICON, url));
        }
    }
    public static WebIconDatabase getInstance() {
        if (sIconDatabase == null) {
            sIconDatabase = new WebIconDatabase();
        }
        return sIconDatabase;
    }
     void createHandler() {
        mEventHandler.createHandler();
    }
    private WebIconDatabase() {}
    private static native void nativeOpen(String path);
    private static native void nativeClose();
    private static native void nativeRemoveAllIcons();
    private static native Bitmap nativeIconForPageUrl(String url);
    private static native void nativeRetainIconForPageUrl(String url);
    private static native void nativeReleaseIconForPageUrl(String url);
}
