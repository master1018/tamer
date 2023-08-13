public class AlarmService_Service extends Service {
    NotificationManager mNM;
    @Override
    public void onCreate() {
        mNM = (NotificationManager)getSystemService(NOTIFICATION_SERVICE);
        showNotification();
        Thread thr = new Thread(null, mTask, "AlarmService_Service");
        thr.start();
    }
    @Override
    public void onDestroy() {
        mNM.cancel(R.string.alarm_service_started);
        Toast.makeText(this, R.string.alarm_service_finished, Toast.LENGTH_SHORT).show();
    }
    Runnable mTask = new Runnable() {
        public void run() {
            long endTime = System.currentTimeMillis() + 15*1000;
            while (System.currentTimeMillis() < endTime) {
                synchronized (mBinder) {
                    try {
                        mBinder.wait(endTime - System.currentTimeMillis());
                    } catch (Exception e) {
                    }
                }
            }
            AlarmService_Service.this.stopSelf();
        }
    };
    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }
    private void showNotification() {
        CharSequence text = getText(R.string.alarm_service_started);
        Notification notification = new Notification(R.drawable.stat_sample, text,
                System.currentTimeMillis());
        PendingIntent contentIntent = PendingIntent.getActivity(this, 0,
                new Intent(this, AlarmService.class), 0);
        notification.setLatestEventInfo(this, getText(R.string.alarm_service_label),
                       text, contentIntent);
        mNM.notify(R.string.alarm_service_started, notification);
    }
    private final IBinder mBinder = new Binder() {
        @Override
		protected boolean onTransact(int code, Parcel data, Parcel reply,
		        int flags) throws RemoteException {
            return super.onTransact(code, data, reply, flags);
        }
    };
}
