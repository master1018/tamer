class BluetoothOppNotification {
    private static final String TAG = "BluetoothOppNotification";
    private static final boolean D = Constants.DEBUG;
    private static final boolean V = Constants.VERBOSE;
    static final String status = "(" + BluetoothShare.STATUS + " == '192'" + ")";
    static final String visible = "(" + BluetoothShare.VISIBILITY + " IS NULL OR "
            + BluetoothShare.VISIBILITY + " == '" + BluetoothShare.VISIBILITY_VISIBLE + "'" + ")";
    static final String confirm = "(" + BluetoothShare.USER_CONFIRMATION + " == '"
            + BluetoothShare.USER_CONFIRMATION_CONFIRMED + "' OR "
            + BluetoothShare.USER_CONFIRMATION + " == '"
            + BluetoothShare.USER_CONFIRMATION_AUTO_CONFIRMED + "'" + ")";
    static final String WHERE_RUNNING = status + " AND " + visible + " AND " + confirm;
    static final String WHERE_COMPLETED = BluetoothShare.STATUS + " >= '200' AND " + visible;
    private static final String WHERE_COMPLETED_OUTBOUND = WHERE_COMPLETED + " AND " + "("
            + BluetoothShare.DIRECTION + " == " + BluetoothShare.DIRECTION_OUTBOUND + ")";
    private static final String WHERE_COMPLETED_INBOUND = WHERE_COMPLETED + " AND " + "("
            + BluetoothShare.DIRECTION + " == " + BluetoothShare.DIRECTION_INBOUND + ")";
    static final String WHERE_CONFIRM_PENDING = BluetoothShare.USER_CONFIRMATION + " == '"
            + BluetoothShare.USER_CONFIRMATION_PENDING + "'" + " AND " + visible;
    public NotificationManager mNotificationMgr;
    private Context mContext;
    private HashMap<String, NotificationItem> mNotifications;
    private NotificationUpdateThread mUpdateNotificationThread;
    private boolean mPendingUpdate = false;
    private boolean mFinised = false;
    private static final int NOTIFICATION_ID_OUTBOUND = -1000005;
    private static final int NOTIFICATION_ID_INBOUND = -1000006;
    private boolean mUpdateCompleteNotification = true;
    private int mActiveNotificationId = 0;
    static class NotificationItem {
        int id; 
        int direction; 
        int totalCurrent = 0; 
        int totalTotal = 0; 
        String description; 
    }
    BluetoothOppNotification(Context ctx) {
        mContext = ctx;
        mNotificationMgr = (NotificationManager)mContext
                .getSystemService(Context.NOTIFICATION_SERVICE);
        mNotifications = new HashMap<String, NotificationItem>();
    }
    public void finishNotification() {
        synchronized (BluetoothOppNotification.this) {
            mFinised = true;
        }
    }
    public void updateNotification() {
        synchronized (BluetoothOppNotification.this) {
            mPendingUpdate = true;
            if (mUpdateNotificationThread == null) {
                mUpdateNotificationThread = new NotificationUpdateThread();
                mUpdateNotificationThread.start();
                mFinised = false;
            }
        }
    }
    private class NotificationUpdateThread extends Thread {
        public NotificationUpdateThread() {
            super("Notification Update Thread");
        }
        @Override
        public void run() {
            Process.setThreadPriority(Process.THREAD_PRIORITY_BACKGROUND);
            for (;;) {
                synchronized (BluetoothOppNotification.this) {
                    if (mUpdateNotificationThread != this) {
                        throw new IllegalStateException(
                                "multiple UpdateThreads in BluetoothOppNotification");
                    }
                    if (!mPendingUpdate && mFinised) {
                        mUpdateNotificationThread = null;
                        return;
                    }
                    mPendingUpdate = false;
                }
                updateActiveNotification();
                updateCompletedNotification();
                updateIncomingFileConfirmNotification();
            }
        }
    }
    private void updateActiveNotification() {
        Cursor cursor = mContext.getContentResolver().query(BluetoothShare.CONTENT_URI, null,
                WHERE_RUNNING, null, BluetoothShare._ID);
        if (cursor == null) {
            return;
        }
        if (cursor.getCount() > 0) {
            mUpdateCompleteNotification = false;
        } else {
            mUpdateCompleteNotification = true;
        }
        if (V) Log.v(TAG, "mUpdateCompleteNotification = " + mUpdateCompleteNotification);
        final int timestampIndex = cursor.getColumnIndexOrThrow(BluetoothShare.TIMESTAMP);
        final int directionIndex = cursor.getColumnIndexOrThrow(BluetoothShare.DIRECTION);
        final int idIndex = cursor.getColumnIndexOrThrow(BluetoothShare._ID);
        final int totalBytesIndex = cursor.getColumnIndexOrThrow(BluetoothShare.TOTAL_BYTES);
        final int currentBytesIndex = cursor.getColumnIndexOrThrow(BluetoothShare.CURRENT_BYTES);
        final int dataIndex = cursor.getColumnIndexOrThrow(BluetoothShare._DATA);
        final int filenameHintIndex = cursor.getColumnIndexOrThrow(BluetoothShare.FILENAME_HINT);
        mNotifications.clear();
        for (cursor.moveToFirst(); !cursor.isAfterLast(); cursor.moveToNext()) {
            int timeStamp = cursor.getInt(timestampIndex);
            int dir = cursor.getInt(directionIndex);
            int id = cursor.getInt(idIndex);
            int total = cursor.getInt(totalBytesIndex);
            int current = cursor.getInt(currentBytesIndex);
            String fileName = cursor.getString(dataIndex);
            if (fileName == null) {
                fileName = cursor.getString(filenameHintIndex);
            }
            if (fileName == null) {
                fileName = mContext.getString(R.string.unknown_file);
            }
            String batchID = Long.toString(timeStamp);
            if (mNotifications.containsKey(batchID)) {
            } else {
                NotificationItem item = new NotificationItem();
                item.id = id;
                item.direction = dir;
                if (item.direction == BluetoothShare.DIRECTION_OUTBOUND) {
                    item.description = mContext.getString(R.string.notification_sending, fileName);
                } else if (item.direction == BluetoothShare.DIRECTION_INBOUND) {
                    item.description = mContext
                            .getString(R.string.notification_receiving, fileName);
                } else {
                    if (V) Log.v(TAG, "mDirection ERROR!");
                }
                item.totalCurrent = current;
                item.totalTotal = total;
                mNotifications.put(batchID, item);
                if (V) Log.v(TAG, "ID=" + item.id + "; batchID=" + batchID + "; totoalCurrent"
                            + item.totalCurrent + "; totalTotal=" + item.totalTotal);
            }
        }
        cursor.close();
        for (NotificationItem item : mNotifications.values()) {
            RemoteViews expandedView = new RemoteViews(Constants.THIS_PACKAGE_NAME,
                    R.layout.status_bar_ongoing_event_progress_bar);
            expandedView.setTextViewText(R.id.description, item.description);
            expandedView.setProgressBar(R.id.progress_bar, item.totalTotal, item.totalCurrent,
                    item.totalTotal == -1);
            expandedView.setTextViewText(R.id.progress_text, BluetoothOppUtility
                    .formatProgressText(item.totalTotal, item.totalCurrent));
            Notification n = new Notification();
            if (item.direction == BluetoothShare.DIRECTION_OUTBOUND) {
                n.icon = android.R.drawable.stat_sys_upload;
                expandedView.setImageViewResource(R.id.appIcon, android.R.drawable.stat_sys_upload);
            } else if (item.direction == BluetoothShare.DIRECTION_INBOUND) {
                n.icon = android.R.drawable.stat_sys_download;
                expandedView.setImageViewResource(R.id.appIcon,
                        android.R.drawable.stat_sys_download);
            } else {
                if (V) Log.v(TAG, "mDirection ERROR!");
            }
            n.flags |= Notification.FLAG_ONGOING_EVENT;
            n.contentView = expandedView;
            Intent intent = new Intent(Constants.ACTION_LIST);
            intent.setClassName(Constants.THIS_PACKAGE_NAME, BluetoothOppReceiver.class.getName());
            intent.setData(Uri.parse(BluetoothShare.CONTENT_URI + "/" + item.id));
            n.contentIntent = PendingIntent.getBroadcast(mContext, 0, intent, 0);
            mNotificationMgr.notify(item.id, n);
            mActiveNotificationId = item.id;
        }
    }
    private void updateCompletedNotification() {
        String title;
        String caption;
        long timeStamp = 0;
        int outboundSuccNumber = 0;
        int outboundFailNumber = 0;
        int outboundNum;
        int inboundNum;
        int inboundSuccNumber = 0;
        int inboundFailNumber = 0;
        Intent intent;
        if (!mUpdateCompleteNotification) {
            if (V) Log.v(TAG, "No need to update complete notification");
            return;
        }
        if (mNotificationMgr != null && mActiveNotificationId != 0) {
            mNotificationMgr.cancel(mActiveNotificationId);
            if (V) Log.v(TAG, "ongoing transfer notification was removed");
        }
        Cursor cursor = mContext.getContentResolver().query(BluetoothShare.CONTENT_URI, null,
                WHERE_COMPLETED_OUTBOUND, null, BluetoothShare.TIMESTAMP + " DESC");
        if (cursor == null) {
            return;
        }
        final int timestampIndex = cursor.getColumnIndexOrThrow(BluetoothShare.TIMESTAMP);
        final int statusIndex = cursor.getColumnIndexOrThrow(BluetoothShare.STATUS);
        for (cursor.moveToFirst(); !cursor.isAfterLast(); cursor.moveToNext()) {
            if (cursor.isFirst()) {
                timeStamp = cursor.getLong(timestampIndex);
            }
            int status = cursor.getInt(statusIndex);
            if (BluetoothShare.isStatusError(status)) {
                outboundFailNumber++;
            } else {
                outboundSuccNumber++;
            }
        }
        if (V) Log.v(TAG, "outbound: succ-" + outboundSuccNumber + "  fail-" + outboundFailNumber);
        cursor.close();
        outboundNum = outboundSuccNumber + outboundFailNumber;
        if (outboundNum > 0) {
            Notification outNoti = new Notification();
            outNoti.icon = android.R.drawable.stat_sys_upload_done;
            title = mContext.getString(R.string.outbound_noti_title);
            caption = mContext.getString(R.string.noti_caption, outboundSuccNumber,
                    outboundFailNumber);
            intent = new Intent(Constants.ACTION_OPEN_OUTBOUND_TRANSFER);
            intent.setClassName(Constants.THIS_PACKAGE_NAME, BluetoothOppReceiver.class.getName());
            outNoti.setLatestEventInfo(mContext, title, caption, PendingIntent.getBroadcast(
                    mContext, 0, intent, 0));
            intent = new Intent(Constants.ACTION_COMPLETE_HIDE);
            intent.setClassName(Constants.THIS_PACKAGE_NAME, BluetoothOppReceiver.class.getName());
            outNoti.deleteIntent = PendingIntent.getBroadcast(mContext, 0, intent, 0);
            outNoti.when = timeStamp;
            mNotificationMgr.notify(NOTIFICATION_ID_OUTBOUND, outNoti);
        } else {
            if (mNotificationMgr != null) {
                mNotificationMgr.cancel(NOTIFICATION_ID_OUTBOUND);
                if (V) Log.v(TAG, "outbound notification was removed.");
            }
        }
        cursor = mContext.getContentResolver().query(BluetoothShare.CONTENT_URI, null,
                WHERE_COMPLETED_INBOUND, null, BluetoothShare.TIMESTAMP + " DESC");
        if (cursor == null) {
            return;
        }
        for (cursor.moveToFirst(); !cursor.isAfterLast(); cursor.moveToNext()) {
            if (cursor.isFirst()) {
                timeStamp = cursor.getLong(timestampIndex);
            }
            int status = cursor.getInt(statusIndex);
            if (BluetoothShare.isStatusError(status)) {
                inboundFailNumber++;
            } else {
                inboundSuccNumber++;
            }
        }
        if (V) Log.v(TAG, "inbound: succ-" + inboundSuccNumber + "  fail-" + inboundFailNumber);
        cursor.close();
        inboundNum = inboundSuccNumber + inboundFailNumber;
        if (inboundNum > 0) {
            Notification inNoti = new Notification();
            inNoti.icon = android.R.drawable.stat_sys_download_done;
            title = mContext.getString(R.string.inbound_noti_title);
            caption = mContext.getString(R.string.noti_caption, inboundSuccNumber,
                    inboundFailNumber);
            intent = new Intent(Constants.ACTION_OPEN_INBOUND_TRANSFER);
            intent.setClassName(Constants.THIS_PACKAGE_NAME, BluetoothOppReceiver.class.getName());
            inNoti.setLatestEventInfo(mContext, title, caption, PendingIntent.getBroadcast(
                    mContext, 0, intent, 0));
            intent = new Intent(Constants.ACTION_COMPLETE_HIDE);
            intent.setClassName(Constants.THIS_PACKAGE_NAME, BluetoothOppReceiver.class.getName());
            inNoti.deleteIntent = PendingIntent.getBroadcast(mContext, 0, intent, 0);
            inNoti.when = timeStamp;
            mNotificationMgr.notify(NOTIFICATION_ID_INBOUND, inNoti);
        } else {
            if (mNotificationMgr != null) {
                mNotificationMgr.cancel(NOTIFICATION_ID_INBOUND);
                if (V) Log.v(TAG, "inbound notification was removed.");
            }
        }
    }
    private void updateIncomingFileConfirmNotification() {
        Cursor cursor = mContext.getContentResolver().query(BluetoothShare.CONTENT_URI, null,
                WHERE_CONFIRM_PENDING, null, BluetoothShare._ID);
        if (cursor == null) {
            return;
        }
        for (cursor.moveToFirst(); !cursor.isAfterLast(); cursor.moveToNext()) {
            String title = mContext.getString(R.string.incoming_file_confirm_Notification_title);
            String caption = mContext
                    .getString(R.string.incoming_file_confirm_Notification_caption);
            int id = cursor.getInt(cursor.getColumnIndexOrThrow(BluetoothShare._ID));
            long timeStamp = cursor.getLong(cursor.getColumnIndexOrThrow(BluetoothShare.TIMESTAMP));
            Uri contentUri = Uri.parse(BluetoothShare.CONTENT_URI + "/" + id);
            Notification n = new Notification();
            n.icon = R.drawable.bt_incomming_file_notification;
            n.flags |= Notification.FLAG_ONLY_ALERT_ONCE;
            n.defaults = Notification.DEFAULT_SOUND;
            n.tickerText = title;
            Intent intent = new Intent(Constants.ACTION_INCOMING_FILE_CONFIRM);
            intent.setClassName(Constants.THIS_PACKAGE_NAME, BluetoothOppReceiver.class.getName());
            intent.setData(contentUri);
            n.when = timeStamp;
            n.setLatestEventInfo(mContext, title, caption, PendingIntent.getBroadcast(mContext, 0,
                    intent, 0));
            intent = new Intent(Constants.ACTION_HIDE);
            intent.setClassName(Constants.THIS_PACKAGE_NAME, BluetoothOppReceiver.class.getName());
            intent.setData(contentUri);
            n.deleteIntent = PendingIntent.getBroadcast(mContext, 0, intent, 0);
            mNotificationMgr.notify(id, n);
        }
        cursor.close();
    }
}
