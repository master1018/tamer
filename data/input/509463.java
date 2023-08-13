public class BatteryWaster extends Activity {
    TextView mLog;
    DateFormat mDateFormat;
    IntentFilter mFilter;
    PowerManager.WakeLock mWakeLock;
    SpinThread mThread;
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.battery_waster);
        findViewById(R.id.checkbox).setOnClickListener(mClickListener);
        mLog = (TextView)findViewById(R.id.log);
        mDateFormat = DateFormat.getInstance();
        mFilter = new IntentFilter();
        mFilter.addAction(Intent.ACTION_BATTERY_CHANGED);
        mFilter.addAction(Intent.ACTION_BATTERY_LOW);
        mFilter.addAction(Intent.ACTION_BATTERY_OKAY);
        mFilter.addAction(Intent.ACTION_POWER_CONNECTED);
        PowerManager pm = (PowerManager)getSystemService(POWER_SERVICE);
        mWakeLock = pm.newWakeLock(PowerManager.FULL_WAKE_LOCK, "BatteryWaster");
        mWakeLock.setReferenceCounted(false);
    }
    @Override
    public void onPause() {
        stopRunning();
    }
    View.OnClickListener mClickListener = new View.OnClickListener() {
        public void onClick(View v) {
            CheckBox checkbox = (CheckBox)v;
            if (checkbox.isChecked()) {
                startRunning();
            } else {
                stopRunning();
            }
        }
    };
    void startRunning() {
        log("Start");
        registerReceiver(mReceiver, mFilter);
        mWakeLock.acquire();
        if (mThread == null) {
            mThread = new SpinThread();
            mThread.start();
        }
    }
    void stopRunning() {
        log("Stop");
        unregisterReceiver(mReceiver);
        mWakeLock.release();
        if (mThread != null) {
            mThread.quit();
            mThread = null;
        }
    }
    void log(String s) {
        mLog.setText(mLog.getText() + "\n" + mDateFormat.format(new Date()) + ": " + s);
    }
    BroadcastReceiver mReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            String title = action;
            int index = title.lastIndexOf('.');
            if (index >= 0) {
                title = title.substring(index + 1);
            }
            if (Intent.ACTION_BATTERY_CHANGED.equals(action)) {
                int level = intent.getIntExtra(BatteryManager.EXTRA_LEVEL, -1);
                log(title + ": level=" + level);
            } else {
                log(title);
            }
        }
    };
    class SpinThread extends Thread {
        private boolean mStop;
        public void quit() {
            synchronized (this) {
                mStop = true;
            }
        }
        public void run() {
            while (true) {
                synchronized (this) {
                    if (mStop) {
                        return;
                    }
                }
            }
        }
    }
}
