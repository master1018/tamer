public class ServiceStartArguments extends Service {
    private NotificationManager mNM;
    private Intent mInvokeIntent;
    private volatile Looper mServiceLooper;
    private volatile ServiceHandler mServiceHandler;
    private final class ServiceHandler extends Handler {
        public ServiceHandler(Looper looper) {
            super(looper);
        }
        @Override
        public void handleMessage(Message msg) {
            Bundle arguments = (Bundle)msg.obj;
            String txt = arguments.getString("name");
            Log.i("ServiceStartArguments", "Message: " + msg + ", "
                    + arguments.getString("name"));
            if ((msg.arg2&Service.START_FLAG_REDELIVERY) == 0) {
                txt = "New cmd #" + msg.arg1 + ": " + txt;
            } else {
                txt = "Re-delivered #" + msg.arg1 + ": " + txt;
            }
            showNotification(txt);
            long endTime = System.currentTimeMillis() + 5*1000;
            while (System.currentTimeMillis() < endTime) {
                synchronized (this) {
                    try {
                        wait(endTime - System.currentTimeMillis());
                    } catch (Exception e) {
                    }
                }
            }
            hideNotification();
            Log.i("ServiceStartArguments", "Done with #" + msg.arg1);
            stopSelf(msg.arg1);
        }
    };
    @Override
    public void onCreate() {
        mNM = (NotificationManager)getSystemService(NOTIFICATION_SERVICE);
        Toast.makeText(this, R.string.service_created,
                Toast.LENGTH_SHORT).show();
        mInvokeIntent = new Intent(this, Controller.class);
        HandlerThread thread = new HandlerThread("ServiceStartArguments",
                Process.THREAD_PRIORITY_BACKGROUND);
        thread.start();
        mServiceLooper = thread.getLooper();
        mServiceHandler = new ServiceHandler(mServiceLooper);
    }
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.i("ServiceStartArguments",
                "Starting #" + startId + ": " + intent.getExtras());
        Message msg = mServiceHandler.obtainMessage();
        msg.arg1 = startId;
        msg.arg2 = flags;
        msg.obj = intent.getExtras();
        mServiceHandler.sendMessage(msg);
        Log.i("ServiceStartArguments", "Sending: " + msg);
        if (intent.getBooleanExtra("fail", false)) {
            if ((flags&START_FLAG_RETRY) == 0) {
                Process.killProcess(Process.myPid());
            }
        }
        return intent.getBooleanExtra("redeliver", false)
                ? START_REDELIVER_INTENT : START_NOT_STICKY;
    }
    @Override
    public void onDestroy() {
        mServiceLooper.quit();
        hideNotification();
        Toast.makeText(ServiceStartArguments.this, R.string.service_destroyed,
                Toast.LENGTH_SHORT).show();
    }
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
    private void showNotification(String text) {
        Notification notification = new Notification(R.drawable.stat_sample, text,
                System.currentTimeMillis());
        PendingIntent contentIntent = PendingIntent.getActivity(this, 0,
                new Intent(this, Controller.class), 0);
        notification.setLatestEventInfo(this, getText(R.string.service_start_arguments_label),
                       text, contentIntent);
        notification.flags |= Notification.FLAG_ONGOING_EVENT;
        mNM.notify(R.string.service_created, notification);
    }
    private void hideNotification() {
        mNM.cancel(R.string.service_created);
    }
    public static class Controller extends Activity {
        @Override
        protected void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            setContentView(R.layout.service_start_arguments_controller);
            Button button = (Button)findViewById(R.id.start1);
            button.setOnClickListener(mStart1Listener);
            button = (Button)findViewById(R.id.start2);
            button.setOnClickListener(mStart2Listener);
            button = (Button)findViewById(R.id.start3);
            button.setOnClickListener(mStart3Listener);
            button = (Button)findViewById(R.id.startfail);
            button.setOnClickListener(mStartFailListener);
            button = (Button)findViewById(R.id.kill);
            button.setOnClickListener(mKillListener);
        }
        private OnClickListener mStart1Listener = new OnClickListener() {
            public void onClick(View v) {
                startService(new Intent(Controller.this,
                        ServiceStartArguments.class)
                                .putExtra("name", "One"));
            }
        };
        private OnClickListener mStart2Listener = new OnClickListener() {
            public void onClick(View v) {
                startService(new Intent(Controller.this,
                        ServiceStartArguments.class)
                                .putExtra("name", "Two"));
            }
        };
        private OnClickListener mStart3Listener = new OnClickListener() {
            public void onClick(View v) {
                startService(new Intent(Controller.this,
                        ServiceStartArguments.class)
                                .putExtra("name", "Three")
                                .putExtra("redeliver", true));
            }
        };
        private OnClickListener mStartFailListener = new OnClickListener() {
            public void onClick(View v) {
                startService(new Intent(Controller.this,
                        ServiceStartArguments.class)
                                .putExtra("name", "Failure")
                                .putExtra("fail", true));
            }
        };
        private OnClickListener mKillListener = new OnClickListener() {
            public void onClick(View v) {
                Process.killProcess(Process.myPid());
            }
        };
    }
}
