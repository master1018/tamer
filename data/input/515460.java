public class AndroidHeartBeatService extends BroadcastReceiver
        implements HeartbeatService {
    private static final String WAKELOCK_TAG = "IM_HEARTBEAT";
    private static final String HEARTBEAT_INTENT_ACTION
            = "com.android.im.intent.action.HEARTBEAT";
    private static final Uri HEARTBEAT_CONTENT_URI
            = Uri.parse("content:
    private static final String HEARTBEAT_CONTENT_TYPE
            = "vnd.android.im/heartbeat";
    private static final ExecutorService sExecutor = Executors.newSingleThreadExecutor();
    private final Context mContext;
    private final AlarmManager mAlarmManager;
     PowerManager.WakeLock mWakeLock;
    static class Alarm {
        public PendingIntent mAlaramSender;
        public Callback mCallback;
    }
    private final SparseArray<Alarm> mAlarms;
    public AndroidHeartBeatService(Context context) {
        mContext = context;
        mAlarmManager = (AlarmManager)context.getSystemService(
                Context.ALARM_SERVICE);
        PowerManager powerManager = (PowerManager)context.getSystemService(
                Context.POWER_SERVICE);
        mWakeLock = powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK,
                WAKELOCK_TAG);
        mAlarms = new SparseArray<Alarm>();
    }
    public synchronized void startHeartbeat(Callback callback, long triggerTime) {
        Alarm alarm = findAlarm(callback);
        if (alarm == null) {
            alarm = new Alarm();
            int id = nextId();
            alarm.mCallback = callback;
            Uri data = ContentUris.withAppendedId(HEARTBEAT_CONTENT_URI, id);
            Intent i = new Intent(HEARTBEAT_INTENT_ACTION)
                            .setDataAndType(data, HEARTBEAT_CONTENT_TYPE);
            alarm.mAlaramSender = PendingIntent.getBroadcast(mContext, 0, i, 0);
            if (mAlarms.size() == 0) {
                mContext.registerReceiver(this, IntentFilter.create(
                        HEARTBEAT_INTENT_ACTION, HEARTBEAT_CONTENT_TYPE));
            }
            mAlarms.append(id, alarm);
        }
        setAlarm(alarm, triggerTime);
    }
    public synchronized void stopHeartbeat(Callback callback) {
        Alarm alarm = findAlarm(callback);
        if (alarm != null) {
            cancelAlarm(alarm);
        }
    }
    public synchronized void stopAll() {
        for (int i = 0; i < mAlarms.size(); i++) {
            Alarm alarm = mAlarms.valueAt(i);
            cancelAlarm(alarm);
        }
    }
    @Override
    public void onReceive(Context context, Intent intent) {
        int id = (int)ContentUris.parseId(intent.getData());
        Alarm alarm = mAlarms.get(id);
        if (alarm == null) {
            return;
        }
        sExecutor.execute(new Worker(alarm));
    }
    private class Worker implements Runnable {
        private final Alarm mAlarm;
        public Worker(Alarm alarm) {
            mAlarm = alarm;
        }
        public void run() {
            mWakeLock.acquire();
            try {
                Callback callback = mAlarm.mCallback;
                long nextSchedule = callback.sendHeartbeat();
                if (nextSchedule <= 0) {
                    cancelAlarm(mAlarm);
                } else {
                    setAlarm(mAlarm, nextSchedule);
                }
            } finally {
                mWakeLock.release();
            }
        }
    }
    private Alarm findAlarm(Callback callback) {
        for (int i = 0; i < mAlarms.size(); i++) {
            Alarm alarm = mAlarms.valueAt(i);
            if (alarm.mCallback == callback) {
                return alarm;
            }
        }
        return null;
    }
     synchronized void setAlarm(Alarm alarm, long offset) {
        long triggerAtTime = SystemClock.elapsedRealtime() + offset;
        mAlarmManager.set(AlarmManager.ELAPSED_REALTIME_WAKEUP, triggerAtTime,
                alarm.mAlaramSender);
    }
      synchronized void cancelAlarm(Alarm alarm) {
        mAlarmManager.cancel(alarm.mAlaramSender);
        int index = mAlarms.indexOfValue(alarm);
        if (index >= 0) {
            mAlarms.delete(mAlarms.keyAt(index));
        }
        if (mAlarms.size() == 0) {
            mContext.unregisterReceiver(this);
        }
    }
    private static int sNextId = 0;
    private static synchronized int nextId() {
        return sNextId++;
    }
}
