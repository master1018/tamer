class DownloadNotification {
    Context mContext;
    public NotificationManager mNotificationMgr;
    HashMap <String, NotificationItem> mNotifications;
    static final String LOGTAG = "DownloadNotification";
    static final String WHERE_RUNNING = 
        "(" + Downloads.Impl.COLUMN_STATUS + " >= '100') AND (" +
        Downloads.Impl.COLUMN_STATUS + " <= '199') AND (" +
        Downloads.Impl.COLUMN_VISIBILITY + " IS NULL OR " +
        Downloads.Impl.COLUMN_VISIBILITY + " == '" + Downloads.Impl.VISIBILITY_VISIBLE + "' OR " +
        Downloads.Impl.COLUMN_VISIBILITY +
            " == '" + Downloads.Impl.VISIBILITY_VISIBLE_NOTIFY_COMPLETED + "')";
    static final String WHERE_COMPLETED =
        Downloads.Impl.COLUMN_STATUS + " >= '200' AND " +
        Downloads.Impl.COLUMN_VISIBILITY +
            " == '" + Downloads.Impl.VISIBILITY_VISIBLE_NOTIFY_COMPLETED + "'";
    static class NotificationItem {
        int mId;  
        int mTotalCurrent = 0;
        int mTotalTotal = 0;
        int mTitleCount = 0;
        String mPackageName;  
        String mDescription;
        String[] mTitles = new String[2]; 
        void addItem(String title, int currentBytes, int totalBytes) {
            mTotalCurrent += currentBytes;
            if (totalBytes <= 0 || mTotalTotal == -1) {
                mTotalTotal = -1;
            } else {
                mTotalTotal += totalBytes;
            }
            if (mTitleCount < 2) {
                mTitles[mTitleCount] = title;
            }
            mTitleCount++;
        }
    }
    DownloadNotification(Context ctx) {
        mContext = ctx;
        mNotificationMgr = (NotificationManager) mContext
                .getSystemService(Context.NOTIFICATION_SERVICE);
        mNotifications = new HashMap<String, NotificationItem>();
    }
    public void updateNotification() {
        updateActiveNotification();
        updateCompletedNotification();
    }
    private void updateActiveNotification() {
        Cursor c = mContext.getContentResolver().query(
                Downloads.Impl.CONTENT_URI, new String [] {
                        Downloads.Impl._ID,
                        Downloads.Impl.COLUMN_TITLE,
                        Downloads.Impl.COLUMN_DESCRIPTION,
                        Downloads.Impl.COLUMN_NOTIFICATION_PACKAGE,
                        Downloads.Impl.COLUMN_NOTIFICATION_CLASS,
                        Downloads.Impl.COLUMN_CURRENT_BYTES,
                        Downloads.Impl.COLUMN_TOTAL_BYTES,
                        Downloads.Impl.COLUMN_STATUS
                },
                WHERE_RUNNING, null, Downloads.Impl._ID);
        if (c == null) {
            return;
        }
        final int idColumn = 0;
        final int titleColumn = 1;
        final int descColumn = 2;
        final int ownerColumn = 3;
        final int classOwnerColumn = 4;
        final int currentBytesColumn = 5;
        final int totalBytesColumn = 6;
        final int statusColumn = 7;
        mNotifications.clear();
        for (c.moveToFirst(); !c.isAfterLast(); c.moveToNext()) {
            String packageName = c.getString(ownerColumn);
            int max = c.getInt(totalBytesColumn);
            int progress = c.getInt(currentBytesColumn);
            long id = c.getLong(idColumn);
            String title = c.getString(titleColumn);
            if (title == null || title.length() == 0) {
                title = mContext.getResources().getString(
                        R.string.download_unknown_title);
            }
            if (mNotifications.containsKey(packageName)) {
                mNotifications.get(packageName).addItem(title, progress, max);
            } else {
                NotificationItem item = new NotificationItem();
                item.mId = (int) id;
                item.mPackageName = packageName;
                item.mDescription = c.getString(descColumn);
                String className = c.getString(classOwnerColumn);
                item.addItem(title, progress, max);
                mNotifications.put(packageName, item);
            }
        }
        c.close();
        for (NotificationItem item : mNotifications.values()) {
            Notification n = new Notification();
            n.icon = android.R.drawable.stat_sys_download;
            n.flags |= Notification.FLAG_ONGOING_EVENT;
            RemoteViews expandedView = new RemoteViews(
                    "com.android.providers.downloads",
                    R.layout.status_bar_ongoing_event_progress_bar);
            StringBuilder title = new StringBuilder(item.mTitles[0]);
            if (item.mTitleCount > 1) {
                title.append(mContext.getString(R.string.notification_filename_separator));
                title.append(item.mTitles[1]);
                n.number = item.mTitleCount;
                if (item.mTitleCount > 2) {
                    title.append(mContext.getString(R.string.notification_filename_extras,
                            new Object[] { Integer.valueOf(item.mTitleCount - 2) }));
                }
            } else {
                expandedView.setTextViewText(R.id.description, 
                        item.mDescription);
            }
            expandedView.setTextViewText(R.id.title, title);
            expandedView.setProgressBar(R.id.progress_bar, 
                    item.mTotalTotal,
                    item.mTotalCurrent,
                    item.mTotalTotal == -1);
            expandedView.setTextViewText(R.id.progress_text, 
                    getDownloadingText(item.mTotalTotal, item.mTotalCurrent));
            expandedView.setImageViewResource(R.id.appIcon,
                    android.R.drawable.stat_sys_download);
            n.contentView = expandedView;
            Intent intent = new Intent(Constants.ACTION_LIST);
            intent.setClassName("com.android.providers.downloads",
                    DownloadReceiver.class.getName());
            intent.setData(Uri.parse(Downloads.Impl.CONTENT_URI + "/" + item.mId));
            intent.putExtra("multiple", item.mTitleCount > 1);
            n.contentIntent = PendingIntent.getBroadcast(mContext, 0, intent, 0);
            mNotificationMgr.notify(item.mId, n);
        }
    }
    private void updateCompletedNotification() {
        Cursor c = mContext.getContentResolver().query(
                Downloads.Impl.CONTENT_URI, new String [] {
                        Downloads.Impl._ID,
                        Downloads.Impl.COLUMN_TITLE,
                        Downloads.Impl.COLUMN_DESCRIPTION,
                        Downloads.Impl.COLUMN_NOTIFICATION_PACKAGE,
                        Downloads.Impl.COLUMN_NOTIFICATION_CLASS,
                        Downloads.Impl.COLUMN_CURRENT_BYTES,
                        Downloads.Impl.COLUMN_TOTAL_BYTES,
                        Downloads.Impl.COLUMN_STATUS,
                        Downloads.Impl.COLUMN_LAST_MODIFICATION,
                        Downloads.Impl.COLUMN_DESTINATION
                },
                WHERE_COMPLETED, null, Downloads.Impl._ID);
        if (c == null) {
            return;
        }
        final int idColumn = 0;
        final int titleColumn = 1;
        final int descColumn = 2;
        final int ownerColumn = 3;
        final int classOwnerColumn = 4;
        final int currentBytesColumn = 5;
        final int totalBytesColumn = 6;
        final int statusColumn = 7;
        final int lastModColumnId = 8;
        final int destinationColumnId = 9;
        for (c.moveToFirst(); !c.isAfterLast(); c.moveToNext()) {
            Notification n = new Notification();
            n.icon = android.R.drawable.stat_sys_download_done;
            long id = c.getLong(idColumn);
            String title = c.getString(titleColumn);
            if (title == null || title.length() == 0) {
                title = mContext.getResources().getString(
                        R.string.download_unknown_title);
            }
            Uri contentUri = Uri.parse(Downloads.Impl.CONTENT_URI + "/" + id);
            String caption;
            Intent intent;
            if (Downloads.Impl.isStatusError(c.getInt(statusColumn))) {
                caption = mContext.getResources()
                        .getString(R.string.notification_download_failed);
                intent = new Intent(Constants.ACTION_LIST);
            } else {
                caption = mContext.getResources()
                        .getString(R.string.notification_download_complete);
                if (c.getInt(destinationColumnId) == Downloads.Impl.DESTINATION_EXTERNAL) {
                    intent = new Intent(Constants.ACTION_OPEN);
                } else {
                    intent = new Intent(Constants.ACTION_LIST);
                }
            }
            intent.setClassName("com.android.providers.downloads",
                    DownloadReceiver.class.getName());
            intent.setData(contentUri);
            n.when = c.getLong(lastModColumnId);
            n.setLatestEventInfo(mContext, title, caption,
                    PendingIntent.getBroadcast(mContext, 0, intent, 0));
            intent = new Intent(Constants.ACTION_HIDE);
            intent.setClassName("com.android.providers.downloads",
                    DownloadReceiver.class.getName());
            intent.setData(contentUri);
            n.deleteIntent = PendingIntent.getBroadcast(mContext, 0, intent, 0);
            mNotificationMgr.notify(c.getInt(idColumn), n);
        }
        c.close();
    }
    private String getDownloadingText(long totalBytes, long currentBytes) {
        if (totalBytes <= 0) {
            return "";
        }
        long progress = currentBytes * 100 / totalBytes;
        StringBuilder sb = new StringBuilder();
        sb.append(progress);
        sb.append('%');
        return sb.toString();
    }
}
