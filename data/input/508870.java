public class NotifyingService extends Service {
    private static int MOOD_NOTIFICATIONS = R.layout.status_bar_notifications;
    private ConditionVariable mCondition;
    @Override
    public void onCreate() {
        mNM = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        Thread notifyingThread = new Thread(null, mTask, "NotifyingService");
        mCondition = new ConditionVariable(false);
        notifyingThread.start();
    }
    @Override
    public void onDestroy() {
        mNM.cancel(MOOD_NOTIFICATIONS);
        mCondition.open();
    }
    private Runnable mTask = new Runnable() {
        public void run() {
            for (int i = 0; i < 4; ++i) {
                showNotification(R.drawable.stat_happy,
                        R.string.status_bar_notifications_happy_message);
                if (mCondition.block(5 * 1000)) 
                    break;
                showNotification(R.drawable.stat_neutral,
                        R.string.status_bar_notifications_ok_message);
                if (mCondition.block(5 * 1000)) 
                    break;
                showNotification(R.drawable.stat_sad,
                        R.string.status_bar_notifications_sad_message);
                if (mCondition.block(5 * 1000)) 
                    break;
            }
            NotifyingService.this.stopSelf();
        }
    };
    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }
    private void showNotification(int moodId, int textId) {
        CharSequence text = getText(textId);
        Notification notification = new Notification(moodId, null, System.currentTimeMillis());
        PendingIntent contentIntent = PendingIntent.getActivity(this, 0,
                new Intent(this, NotifyingController.class), 0);
        notification.setLatestEventInfo(this, getText(R.string.status_bar_notifications_mood_title),
                       text, contentIntent);
        mNM.notify(MOOD_NOTIFICATIONS, notification);
    }
    private final IBinder mBinder = new Binder() {
        @Override
        protected boolean onTransact(int code, Parcel data, Parcel reply,
                int flags) throws RemoteException {
            return super.onTransact(code, data, reply, flags);
        }
    };
    private NotificationManager mNM;
}
