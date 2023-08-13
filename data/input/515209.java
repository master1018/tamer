public class MockAlarmReceiver extends BroadcastReceiver {
    public boolean alarmed = false;
    private Object mSync = new Object();
    public static final String MOCKACTION = "android.app.AlarmManagerTest.TEST_ALARMRECEIVER";
    public long elapsedTime;
    public long rtcTime;
    @Override
    public void onReceive(Context context, Intent intent) {
        final String action = intent.getAction();
        if (action.equals(MOCKACTION)) {
            synchronized (mSync) {
                alarmed = true;
                elapsedTime = SystemClock.elapsedRealtime();
                rtcTime = System.currentTimeMillis();
            }
        }
    }
    public void setAlarmedFalse() {
        synchronized (mSync) {
            alarmed = false;
        }
    }
}
