@TestTargetClass(AlarmManager.class)
public class AlarmManagerTest extends AndroidTestCase {
    private AlarmManager mAlarmManager;
    private Intent mIntent;
    private PendingIntent mSender;
    private Intent mServiceIntent;
    private final long SNOOZE_DELAY = 5 * 1000L;
    private long mWakeupTime;
    private MockAlarmReceiver mMockAlarmReceiver;
    private Sync mSync;
    private final int TIME_DELTA = 200;
    private final int TIME_DELAY = 2000;
    private ISecondary mSecondaryService = null;
    class Sync {
        public boolean mIsConnected;
        public boolean mIsDisConnected;
    }
    @Override
    protected void setUp() throws Exception {
        super.setUp();
        mAlarmManager = (AlarmManager) mContext.getSystemService(Context.ALARM_SERVICE);
        mIntent = new Intent(MockAlarmReceiver.MOCKACTION);
        mSender = PendingIntent.getBroadcast(mContext, 0, mIntent, 0);
        mMockAlarmReceiver = new MockAlarmReceiver();
        IntentFilter filter = new IntentFilter(MockAlarmReceiver.MOCKACTION);
        mContext.registerReceiver(mMockAlarmReceiver, filter);
    }
    @Override
    protected void tearDown() throws Exception {
        super.tearDown();
        if (mServiceIntent != null) {
            mContext.stopService(mServiceIntent);
        }
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        method = "setTimeZone",
        args = {java.lang.String.class}
    )
    @BrokenTest("Broken by CL148448. Default timezone of the test and the service differ.")
    public void testSetTimeZone() throws Exception {
        mSync = new Sync();
        final String ACTION = "android.app.REMOTESERVICE";
        mServiceIntent = new Intent(ACTION);
        mContext.startService(mServiceIntent);
        mContext.bindService(new Intent(ISecondary.class.getName()), mSecondaryConnection,
                Context.BIND_AUTO_CREATE);
        synchronized (mSync) {
            if (!mSync.mIsConnected) {
                mSync.wait();
            }
        }
        final TimeZone currentZone = TimeZone.getDefault();
        String timeZone = null;
        mAlarmManager.setTimeZone(timeZone);
        TimeZone values = TimeZone.getDefault();
        assertEquals(currentZone.getID(), values.getID());
        assertEquals(currentZone.getID(), mSecondaryService.getTimeZoneID());
        timeZone = "";
        mAlarmManager.setTimeZone(timeZone);
        values = TimeZone.getDefault();
        assertEquals(currentZone.getID(), values.getID());
        String[] timeZones = TimeZone.getAvailableIDs();
        timeZone = currentZone.getID().equals(timeZones[0]) ? timeZones[1] : timeZones[0];
        mAlarmManager.setTimeZone(timeZone);
        Thread.sleep(TIME_DELAY);
        values = TimeZone.getDefault();
        TimeZone zone = TimeZone.getTimeZone(timeZone);
        assertEquals(zone.getID(), values.getID());
        assertEquals(zone.getID(), mSecondaryService.getTimeZoneID());
        TimeZone.setDefault(currentZone);
        mContext.stopService(mServiceIntent);
        mServiceIntent = null;
    }
    private ServiceConnection mSecondaryConnection = new ServiceConnection() {
        public void onServiceConnected(ComponentName className, IBinder service) {
            mSecondaryService = ISecondary.Stub.asInterface(service);
            synchronized (mSync) {
                mSync.mIsConnected = true;
                mSync.notify();
            }
        }
        public void onServiceDisconnected(ComponentName className) {
            mSecondaryService = null;
            synchronized (mSync) {
                mSync.mIsDisConnected = true;
                mSync.notify();
            }
        }
    };
    @TestTargetNew(
        level = TestLevel.PARTIAL,
        method = "set",
        args = {int.class, long.class, android.app.PendingIntent.class}
    )
    @ToBeFixed(bug = "1475410", explanation = "currently if make a device sleep android"
            + "framework will throw out exception")
    public void testSetTypes() throws Exception {
        mMockAlarmReceiver.setAlarmedFalse();
        mWakeupTime = System.currentTimeMillis() + SNOOZE_DELAY;
        mAlarmManager.set(AlarmManager.RTC_WAKEUP, mWakeupTime, mSender);
        Thread.sleep(SNOOZE_DELAY + TIME_DELAY);
        assertTrue(mMockAlarmReceiver.alarmed);
        assertEquals(mMockAlarmReceiver.rtcTime, mWakeupTime, TIME_DELTA);
        mMockAlarmReceiver.setAlarmedFalse();
        mWakeupTime = System.currentTimeMillis() + SNOOZE_DELAY;
        mAlarmManager.set(AlarmManager.RTC, mWakeupTime, mSender);
        Thread.sleep(SNOOZE_DELAY + TIME_DELAY);
        assertTrue(mMockAlarmReceiver.alarmed);
        assertEquals(mMockAlarmReceiver.rtcTime, mWakeupTime, TIME_DELTA);
        mMockAlarmReceiver.setAlarmedFalse();
        mWakeupTime = SystemClock.elapsedRealtime() + SNOOZE_DELAY;
        mAlarmManager.set(AlarmManager.ELAPSED_REALTIME, mWakeupTime, mSender);
        Thread.sleep(SNOOZE_DELAY + TIME_DELAY);
        assertTrue(mMockAlarmReceiver.alarmed);
        assertEquals(mMockAlarmReceiver.elapsedTime, mWakeupTime, TIME_DELTA);
        mMockAlarmReceiver.setAlarmedFalse();
        mWakeupTime = SystemClock.elapsedRealtime() + SNOOZE_DELAY;
        mAlarmManager.set(AlarmManager.ELAPSED_REALTIME_WAKEUP, mWakeupTime, mSender);
        Thread.sleep(SNOOZE_DELAY + TIME_DELAY);
        assertTrue(mMockAlarmReceiver.alarmed);
        assertEquals(mMockAlarmReceiver.elapsedTime, mWakeupTime, TIME_DELTA);
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL,
        method = "set",
        args = {int.class, long.class, android.app.PendingIntent.class}
    )
    @ToBeFixed(bug = "1475410", explanation = "currently if make a device sleep android"
            + "framework will throw out exception")
    public void testAlarmTriggersImmediatelyIfSetTimeIsNegative() throws Exception {
        mMockAlarmReceiver.setAlarmedFalse();
        mWakeupTime = -1000;
        mAlarmManager.set(AlarmManager.RTC, mWakeupTime, mSender);
        Thread.sleep(TIME_DELAY);
        assertTrue(mMockAlarmReceiver.alarmed);
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL,
        method = "setRepeating",
        args = {int.class, long.class, long.class, android.app.PendingIntent.class}
    )
    @ToBeFixed(bug = "1475410", explanation = "currently if make a device sleep android"
            + "framework will throw out exception")
    public void testSetRepeating() throws Exception {
        mMockAlarmReceiver.setAlarmedFalse();
        mWakeupTime = System.currentTimeMillis() + SNOOZE_DELAY;
        mAlarmManager.setRepeating(AlarmManager.RTC_WAKEUP, mWakeupTime, TIME_DELAY / 2, mSender);
        Thread.sleep(SNOOZE_DELAY + TIME_DELAY);
        assertTrue(mMockAlarmReceiver.alarmed);
        mMockAlarmReceiver.setAlarmedFalse();
        Thread.sleep(TIME_DELAY);
        assertTrue(mMockAlarmReceiver.alarmed);
        mAlarmManager.cancel(mSender);
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        method = "cancel",
        args = {android.app.PendingIntent.class}
    )
    public void testCancel() throws Exception {
        mMockAlarmReceiver.setAlarmedFalse();
        mWakeupTime = System.currentTimeMillis() + SNOOZE_DELAY;
        mAlarmManager.setRepeating(AlarmManager.RTC_WAKEUP, mWakeupTime, 1000, mSender);
        Thread.sleep(SNOOZE_DELAY + TIME_DELAY);
        assertTrue(mMockAlarmReceiver.alarmed);
        mMockAlarmReceiver.setAlarmedFalse();
        Thread.sleep(TIME_DELAY);
        assertTrue(mMockAlarmReceiver.alarmed);
        mAlarmManager.cancel(mSender);
        Thread.sleep(TIME_DELAY);
        mMockAlarmReceiver.setAlarmedFalse();
        Thread.sleep(TIME_DELAY * 5);
        assertFalse(mMockAlarmReceiver.alarmed);
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL,
        method = "setInexactRepeating",
        args = {int.class, long.class, long.class, android.app.PendingIntent.class}
    )
    @ToBeFixed(bug="1556930", explanation="no way to set the system clock")
    public void testSetInexactRepeating() throws Exception {
        mAlarmManager.setInexactRepeating(AlarmManager.RTC_WAKEUP, System.currentTimeMillis(),
                AlarmManager.INTERVAL_FIFTEEN_MINUTES, mSender);
        SystemClock.setCurrentTimeMillis(System.currentTimeMillis()
                + AlarmManager.INTERVAL_FIFTEEN_MINUTES);
    }
}
