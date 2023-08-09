public class StorageNotification extends StorageEventListener {
    private static final String TAG = "StorageNotification";
    private static final boolean POP_UMS_ACTIVITY_ON_CONNECT = true;
    private Context mContext;
    private Notification mUsbStorageNotification;
    private Notification   mMediaStorageNotification;
    private boolean        mUmsAvailable;
    private StorageManager mStorageManager;
    public StorageNotification(Context context) {
        mContext = context;
        mStorageManager = (StorageManager) context.getSystemService(Context.STORAGE_SERVICE);
        mUmsAvailable = mStorageManager.isUsbMassStorageConnected();
        Slog.d(TAG, String.format( "Startup with UMS connection %s (media state %s)", mUmsAvailable,
                Environment.getExternalStorageState()));
    }
    @Override
    public void onUsbMassStorageConnectionChanged(boolean connected) {
        mUmsAvailable = connected;
        String st = Environment.getExternalStorageState();
        Slog.i(TAG, String.format("UMS connection changed to %s (media state %s)", connected, st));
        if (connected && (st.equals(
                Environment.MEDIA_REMOVED) || st.equals(Environment.MEDIA_CHECKING))) {
            connected = false;
        }
        updateUsbMassStorageNotification(connected);
    }
    @Override
    public void onStorageStateChanged(String path, String oldState, String newState) {
        Slog.i(TAG, String.format(
                "Media {%s} state changed from {%s} -> {%s}", path, oldState, newState));
        if (newState.equals(Environment.MEDIA_SHARED)) {
            Intent intent = new Intent();
            intent.setClass(mContext, com.android.server.status.UsbStorageActivity.class);
            PendingIntent pi = PendingIntent.getActivity(mContext, 0, intent, 0);
            setUsbStorageNotification(
                    com.android.internal.R.string.usb_storage_stop_notification_title,
                    com.android.internal.R.string.usb_storage_stop_notification_message,
                    com.android.internal.R.drawable.stat_sys_warning, false, true, pi);
        } else if (newState.equals(Environment.MEDIA_CHECKING)) {
            setMediaStorageNotification(
                    com.android.internal.R.string.ext_media_checking_notification_title,
                    com.android.internal.R.string.ext_media_checking_notification_message,
                    com.android.internal.R.drawable.stat_notify_sdcard_prepare, true, false, null);
            updateUsbMassStorageNotification(false);
        } else if (newState.equals(Environment.MEDIA_MOUNTED)) {
            setMediaStorageNotification(0, 0, 0, false, false, null);
            updateUsbMassStorageNotification(mUmsAvailable);
        } else if (newState.equals(Environment.MEDIA_UNMOUNTED)) {
            if (!mStorageManager.isUsbMassStorageEnabled()) {
                if (oldState.equals(Environment.MEDIA_SHARED)) {
                    setMediaStorageNotification(0, 0, 0, false, false, null);
                    updateUsbMassStorageNotification(mUmsAvailable);
                } else {
                    setMediaStorageNotification(
                            com.android.internal.R.string.ext_media_safe_unmount_notification_title,
                            com.android.internal.R.string.ext_media_safe_unmount_notification_message,
                            com.android.internal.R.drawable.stat_notify_sdcard, true, true, null);
                    updateUsbMassStorageNotification(mUmsAvailable);
                }
            } else {
                setMediaStorageNotification(0, 0, 0, false, false, null);
                updateUsbMassStorageNotification(false);
            }
        } else if (newState.equals(Environment.MEDIA_NOFS)) {
            Intent intent = new Intent();
            intent.setClass(mContext, com.android.internal.app.ExternalMediaFormatActivity.class);
            PendingIntent pi = PendingIntent.getActivity(mContext, 0, intent, 0);
            setMediaStorageNotification(
                    com.android.internal.R.string.ext_media_nofs_notification_title,
                    com.android.internal.R.string.ext_media_nofs_notification_message,
                    com.android.internal.R.drawable.stat_notify_sdcard_usb, true, false, pi);
            updateUsbMassStorageNotification(mUmsAvailable);
        } else if (newState.equals(Environment.MEDIA_UNMOUNTABLE)) {
            Intent intent = new Intent();
            intent.setClass(mContext, com.android.internal.app.ExternalMediaFormatActivity.class);
            PendingIntent pi = PendingIntent.getActivity(mContext, 0, intent, 0);
            setMediaStorageNotification(
                    com.android.internal.R.string.ext_media_unmountable_notification_title,
                    com.android.internal.R.string.ext_media_unmountable_notification_message,
                    com.android.internal.R.drawable.stat_notify_sdcard_usb, true, false, pi); 
            updateUsbMassStorageNotification(mUmsAvailable);
        } else if (newState.equals(Environment.MEDIA_REMOVED)) {
            setMediaStorageNotification(
                    com.android.internal.R.string.ext_media_nomedia_notification_title,
                    com.android.internal.R.string.ext_media_nomedia_notification_message,
                    com.android.internal.R.drawable.stat_notify_sdcard_usb,
                    true, false, null);
            updateUsbMassStorageNotification(false);
        } else if (newState.equals(Environment.MEDIA_BAD_REMOVAL)) {
            setMediaStorageNotification(
                    com.android.internal.R.string.ext_media_badremoval_notification_title,
                    com.android.internal.R.string.ext_media_badremoval_notification_message,
                    com.android.internal.R.drawable.stat_sys_warning,
                    true, true, null);
            updateUsbMassStorageNotification(false);
        } else {
            Slog.w(TAG, String.format("Ignoring unknown state {%s}", newState));
        }
    }
    void updateUsbMassStorageNotification(boolean available) {
        if (available) {
            Intent intent = new Intent();
            intent.setClass(mContext, com.android.server.status.UsbStorageActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            final boolean adbOn = 1 == Settings.Secure.getInt(
                mContext.getContentResolver(),
                Settings.Secure.ADB_ENABLED,
                0);
            PendingIntent pi = PendingIntent.getActivity(mContext, 0, intent, 0);
            setUsbStorageNotification(
                    com.android.internal.R.string.usb_storage_notification_title,
                    com.android.internal.R.string.usb_storage_notification_message,
                    com.android.internal.R.drawable.stat_sys_data_usb,
                    false, true, pi);
            if (POP_UMS_ACTIVITY_ON_CONNECT && !adbOn) {
                mContext.startActivity(intent);
            }
        } else {
            setUsbStorageNotification(0, 0, 0, false, false, null);
        }
    }
    private synchronized void setUsbStorageNotification(int titleId, int messageId, int icon,
            boolean sound, boolean visible, PendingIntent pi) {
        if (!visible && mUsbStorageNotification == null) {
            return;
        }
        NotificationManager notificationManager = (NotificationManager) mContext
                .getSystemService(Context.NOTIFICATION_SERVICE);
        if (notificationManager == null) {
            return;
        }
        if (visible) {
            Resources r = Resources.getSystem();
            CharSequence title = r.getText(titleId);
            CharSequence message = r.getText(messageId);
            if (mUsbStorageNotification == null) {
                mUsbStorageNotification = new Notification();
                mUsbStorageNotification.icon = icon;
                mUsbStorageNotification.when = 0;
            }
            if (sound) {
                mUsbStorageNotification.defaults |= Notification.DEFAULT_SOUND;
            } else {
                mUsbStorageNotification.defaults &= ~Notification.DEFAULT_SOUND;
            }
            mUsbStorageNotification.flags = Notification.FLAG_ONGOING_EVENT;
            mUsbStorageNotification.tickerText = title;
            if (pi == null) {
                Intent intent = new Intent();
                pi = PendingIntent.getBroadcast(mContext, 0, intent, 0);
            }
            mUsbStorageNotification.setLatestEventInfo(mContext, title, message, pi);
        }
        final int notificationId = mUsbStorageNotification.icon;
        if (visible) {
            notificationManager.notify(notificationId, mUsbStorageNotification);
        } else {
            notificationManager.cancel(notificationId);
        }
    }
    private synchronized boolean getMediaStorageNotificationDismissable() {
        if ((mMediaStorageNotification != null) &&
            ((mMediaStorageNotification.flags & Notification.FLAG_AUTO_CANCEL) ==
                    Notification.FLAG_AUTO_CANCEL))
            return true;
        return false;
    }
    private synchronized void setMediaStorageNotification(int titleId, int messageId, int icon, boolean visible,
                                                          boolean dismissable, PendingIntent pi) {
        if (!visible && mMediaStorageNotification == null) {
            return;
        }
        NotificationManager notificationManager = (NotificationManager) mContext
                .getSystemService(Context.NOTIFICATION_SERVICE);
        if (notificationManager == null) {
            return;
        }
        if (mMediaStorageNotification != null && visible) {
            final int notificationId = mMediaStorageNotification.icon;
            notificationManager.cancel(notificationId);
        }
        if (visible) {
            Resources r = Resources.getSystem();
            CharSequence title = r.getText(titleId);
            CharSequence message = r.getText(messageId);
            if (mMediaStorageNotification == null) {
                mMediaStorageNotification = new Notification();
                mMediaStorageNotification.when = 0;
            }
            mMediaStorageNotification.defaults &= ~Notification.DEFAULT_SOUND;
            if (dismissable) {
                mMediaStorageNotification.flags = Notification.FLAG_AUTO_CANCEL;
            } else {
                mMediaStorageNotification.flags = Notification.FLAG_ONGOING_EVENT;
            }
            mMediaStorageNotification.tickerText = title;
            if (pi == null) {
                Intent intent = new Intent();
                pi = PendingIntent.getBroadcast(mContext, 0, intent, 0);
            }
            mMediaStorageNotification.icon = icon;
            mMediaStorageNotification.setLatestEventInfo(mContext, title, message, pi);
        }
        final int notificationId = mMediaStorageNotification.icon;
        if (visible) {
            notificationManager.notify(notificationId, mMediaStorageNotification);
        } else {
            notificationManager.cancel(notificationId);
        }
    }
}
