public class ForegroundService extends Service {
    static final String ACTION_FOREGROUND = "com.example.android.apis.FOREGROUND";
    static final String ACTION_BACKGROUND = "com.example.android.apis.BACKGROUND";
    private static final Class[] mStartForegroundSignature = new Class[] {
        int.class, Notification.class};
    private static final Class[] mStopForegroundSignature = new Class[] {
        boolean.class};
    private NotificationManager mNM;
    private Method mStartForeground;
    private Method mStopForeground;
    private Object[] mStartForegroundArgs = new Object[2];
    private Object[] mStopForegroundArgs = new Object[1];
    void startForegroundCompat(int id, Notification notification) {
        if (mStartForeground != null) {
            mStartForegroundArgs[0] = Integer.valueOf(id);
            mStartForegroundArgs[1] = notification;
            try {
                mStartForeground.invoke(this, mStartForegroundArgs);
            } catch (InvocationTargetException e) {
                Log.w("ApiDemos", "Unable to invoke startForeground", e);
            } catch (IllegalAccessException e) {
                Log.w("ApiDemos", "Unable to invoke startForeground", e);
            }
            return;
        }
        setForeground(true);
        mNM.notify(id, notification);
    }
    void stopForegroundCompat(int id) {
        if (mStopForeground != null) {
            mStopForegroundArgs[0] = Boolean.TRUE;
            try {
                mStopForeground.invoke(this, mStopForegroundArgs);
            } catch (InvocationTargetException e) {
                Log.w("ApiDemos", "Unable to invoke stopForeground", e);
            } catch (IllegalAccessException e) {
                Log.w("ApiDemos", "Unable to invoke stopForeground", e);
            }
            return;
        }
        mNM.cancel(id);
        setForeground(false);
    }
    @Override
    public void onCreate() {
        mNM = (NotificationManager)getSystemService(NOTIFICATION_SERVICE);
        try {
            mStartForeground = getClass().getMethod("startForeground",
                    mStartForegroundSignature);
            mStopForeground = getClass().getMethod("stopForeground",
                    mStopForegroundSignature);
        } catch (NoSuchMethodException e) {
            mStartForeground = mStopForeground = null;
        }
    }
    @Override
    public void onDestroy() {
        stopForegroundCompat(R.string.foreground_service_started);
    }
    @Override
    public void onStart(Intent intent, int startId) {
        handleCommand(intent);
    }
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        handleCommand(intent);
        return START_STICKY;
    }
    void handleCommand(Intent intent) {
        if (ACTION_FOREGROUND.equals(intent.getAction())) {
            CharSequence text = getText(R.string.foreground_service_started);
            Notification notification = new Notification(R.drawable.stat_sample, text,
                    System.currentTimeMillis());
            PendingIntent contentIntent = PendingIntent.getActivity(this, 0,
                    new Intent(this, Controller.class), 0);
            notification.setLatestEventInfo(this, getText(R.string.local_service_label),
                           text, contentIntent);
            startForegroundCompat(R.string.foreground_service_started, notification);
        } else if (ACTION_BACKGROUND.equals(intent.getAction())) {
            stopForegroundCompat(R.string.foreground_service_started);
        }
    }
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
    public static class Controller extends Activity {
        @Override
        protected void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            setContentView(R.layout.foreground_service_controller);
            Button button = (Button)findViewById(R.id.start_foreground);
            button.setOnClickListener(mForegroundListener);
            button = (Button)findViewById(R.id.start_background);
            button.setOnClickListener(mBackgroundListener);
            button = (Button)findViewById(R.id.stop);
            button.setOnClickListener(mStopListener);
        }
        private OnClickListener mForegroundListener = new OnClickListener() {
            public void onClick(View v) {
                Intent intent = new Intent(ForegroundService.ACTION_FOREGROUND);
                intent.setClass(Controller.this, ForegroundService.class);
                startService(intent);
            }
        };
        private OnClickListener mBackgroundListener = new OnClickListener() {
            public void onClick(View v) {
                Intent intent = new Intent(ForegroundService.ACTION_BACKGROUND);
                intent.setClass(Controller.this, ForegroundService.class);
                startService(intent);
            }
        };
        private OnClickListener mStopListener = new OnClickListener() {
            public void onClick(View v) {
                stopService(new Intent(Controller.this,
                        ForegroundService.class));
            }
        };
    }
}
