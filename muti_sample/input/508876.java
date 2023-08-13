public class Watchdog extends Thread {
    static final String TAG = "Watchdog";
    static final boolean localLOGV = false || Config.LOGV;
    static final boolean DB = false;
    static final boolean RECORD_KERNEL_THREADS = true;
    static final int MONITOR = 2718;
    static final int GLOBAL_PSS = 2719;
    static final int TIME_TO_RESTART = DB ? 15*1000 : 60*1000;
    static final int TIME_TO_WAIT = TIME_TO_RESTART / 2;
    static final int MEMCHECK_DEFAULT_INTERVAL = DB ? 30 : 30*60; 
    static final int MEMCHECK_DEFAULT_LOG_REALTIME_INTERVAL = DB ? 60 : 2*60*60;      
    static final int MEMCHECK_DEFAULT_SYSTEM_SOFT_THRESHOLD = (DB ? 10:16)*1024*1024; 
    static final int MEMCHECK_DEFAULT_SYSTEM_HARD_THRESHOLD = (DB ? 14:20)*1024*1024; 
    static final int MEMCHECK_DEFAULT_PHONE_SOFT_THRESHOLD = (DB ? 4:8)*1024*1024;    
    static final int MEMCHECK_DEFAULT_PHONE_HARD_THRESHOLD = (DB ? 8:12)*1024*1024;   
    static final int MEMCHECK_DEFAULT_EXEC_START_TIME = 1*60*60;           
    static final int MEMCHECK_DEFAULT_EXEC_END_TIME = 5*60*60;             
    static final int MEMCHECK_DEFAULT_MIN_SCREEN_OFF = DB ? 1*60 : 5*60;   
    static final int MEMCHECK_DEFAULT_MIN_ALARM = DB ? 1*60 : 3*60;        
    static final int MEMCHECK_DEFAULT_RECHECK_INTERVAL = DB ? 1*60 : 5*60; 
    static final int REBOOT_DEFAULT_INTERVAL = DB ? 1 : 0;                 
    static final int REBOOT_DEFAULT_START_TIME = 3*60*60;                  
    static final int REBOOT_DEFAULT_WINDOW = 60*60;                        
    static final String CHECKUP_ACTION = "com.android.service.Watchdog.CHECKUP";
    static final String REBOOT_ACTION = "com.android.service.Watchdog.REBOOT";
    static Watchdog sWatchdog;
    final Handler mHandler;
    final Runnable mGlobalPssCollected;
    final ArrayList<Monitor> mMonitors = new ArrayList<Monitor>();
    ContentResolver mResolver;
    BatteryService mBattery;
    PowerManagerService mPower;
    AlarmManagerService mAlarm;
    ActivityManagerService mActivity;
    boolean mCompleted;
    boolean mForceKillSystem;
    Monitor mCurrentMonitor;
    PssRequestor mPhoneReq;
    int mPhonePid;
    int mPhonePss;
    long mLastMemCheckTime = -(MEMCHECK_DEFAULT_INTERVAL*1000);
    boolean mHavePss;
    long mLastMemCheckRealtime = -(MEMCHECK_DEFAULT_LOG_REALTIME_INTERVAL*1000);
    boolean mHaveGlobalPss;
    final MemMonitor mSystemMemMonitor = new MemMonitor("system",
            Settings.Secure.MEMCHECK_SYSTEM_ENABLED,
            Settings.Secure.MEMCHECK_SYSTEM_SOFT_THRESHOLD,
            MEMCHECK_DEFAULT_SYSTEM_SOFT_THRESHOLD,
            Settings.Secure.MEMCHECK_SYSTEM_HARD_THRESHOLD,
            MEMCHECK_DEFAULT_SYSTEM_HARD_THRESHOLD);
    final MemMonitor mPhoneMemMonitor = new MemMonitor("com.android.phone",
            Settings.Secure.MEMCHECK_PHONE_ENABLED,
            Settings.Secure.MEMCHECK_PHONE_SOFT_THRESHOLD,
            MEMCHECK_DEFAULT_PHONE_SOFT_THRESHOLD,
            Settings.Secure.MEMCHECK_PHONE_HARD_THRESHOLD,
            MEMCHECK_DEFAULT_PHONE_HARD_THRESHOLD);
    final Calendar mCalendar = Calendar.getInstance();
    long mMemcheckLastTime;
    long mMemcheckExecStartTime;
    long mMemcheckExecEndTime;
    int mMinScreenOff = MEMCHECK_DEFAULT_MIN_SCREEN_OFF;
    int mMinAlarm = MEMCHECK_DEFAULT_MIN_ALARM;
    boolean mNeedScheduledCheck;
    PendingIntent mCheckupIntent;
    PendingIntent mRebootIntent;
    long mBootTime;
    int mRebootInterval;
    boolean mReqRebootNoWait;     
    int mReqRebootInterval = -1;  
    int mReqRebootStartTime = -1; 
    int mReqRebootWindow = -1;    
    int mReqMinScreenOff = -1;    
    int mReqMinNextAlarm = -1;    
    int mReqRecheckInterval= -1;  
    final class MemMonitor {
        final String mProcessName;
        final String mEnabledSetting;
        final String mSoftSetting;
        final String mHardSetting;
        int mSoftThreshold;
        int mHardThreshold;
        boolean mEnabled;
        long mLastPss;
        static final int STATE_OK = 0;
        static final int STATE_SOFT = 1;
        static final int STATE_HARD = 2;
        int mState;
        MemMonitor(String processName, String enabledSetting,
                String softSetting, int defSoftThreshold,
                String hardSetting, int defHardThreshold) {
            mProcessName = processName;
            mEnabledSetting = enabledSetting;
            mSoftSetting = softSetting;
            mHardSetting = hardSetting;
            mSoftThreshold = defSoftThreshold;
            mHardThreshold = defHardThreshold;
        }
        void retrieveSettings(ContentResolver resolver) {
            mSoftThreshold = Settings.Secure.getInt(
                    resolver, mSoftSetting, mSoftThreshold);
            mHardThreshold = Settings.Secure.getInt(
                    resolver, mHardSetting, mHardThreshold);
            mEnabled = Settings.Secure.getInt(
                    resolver, mEnabledSetting, 0) != 0;
        }
        boolean checkLocked(long curTime, int pid, int pss) {
            mLastPss = pss;
            if (mLastPss < mSoftThreshold) {
                mState = STATE_OK;
            } else if (mLastPss < mHardThreshold) {
                mState = STATE_SOFT;
            } else {
                mState = STATE_HARD;
            }
            EventLog.writeEvent(EventLogTags.WATCHDOG_PROC_PSS, mProcessName, pid, mLastPss);
            if (mState == STATE_OK) {
                return false;
            }
            if (mState == STATE_HARD) {
                EventLog.writeEvent(EventLogTags.WATCHDOG_HARD_RESET, mProcessName, pid,
                        mHardThreshold, mLastPss);
                return mEnabled;
            }
            computeMemcheckTimesLocked(curTime);
            String skipReason = null;
            if (curTime < mMemcheckExecStartTime || curTime > mMemcheckExecEndTime) {
                skipReason = "time";
            } else {
                skipReason = shouldWeBeBrutalLocked(curTime);
            }
            EventLog.writeEvent(EventLogTags.WATCHDOG_SOFT_RESET, mProcessName, pid,
                    mSoftThreshold, mLastPss, skipReason != null ? skipReason : "");
            if (skipReason != null) {
                mNeedScheduledCheck = true;
                return false;
            }
            return mEnabled;
        }
        void clear() {
            mLastPss = 0;
            mState = STATE_OK;
        }
    }
    final class HeartbeatHandler extends Handler {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case GLOBAL_PSS: {
                    if (mHaveGlobalPss) {
                        mHaveGlobalPss = false;
                        if (localLOGV) Slog.v(TAG, "Received global pss, logging.");
                        logGlobalMemory();
                    }
                } break;
                case MONITOR: {
                    if (mHavePss) {
                        mHavePss = false;
                        if (localLOGV) Slog.v(TAG, "Have pss, checking memory.");
                        checkMemory();
                    }
                    if (mHaveGlobalPss) {
                        mHaveGlobalPss = false;
                        if (localLOGV) Slog.v(TAG, "Have global pss, logging.");
                        logGlobalMemory();
                    }
                    long now = SystemClock.uptimeMillis();
                    int rebootInterval = mReqRebootInterval >= 0
                            ? mReqRebootInterval : Settings.Secure.getInt(
                            mResolver, Settings.Secure.REBOOT_INTERVAL,
                            REBOOT_DEFAULT_INTERVAL);
                    if (mRebootInterval != rebootInterval) {
                        mRebootInterval = rebootInterval;
                        checkReboot(false);
                    }
                    long memCheckInterval = Settings.Secure.getLong(
                            mResolver, Settings.Secure.MEMCHECK_INTERVAL,
                            MEMCHECK_DEFAULT_INTERVAL) * 1000;
                    if ((mLastMemCheckTime+memCheckInterval) < now) {
                        mLastMemCheckTime = now;
                        if (localLOGV) Slog.v(TAG, "Collecting memory usage.");
                        collectMemory();
                        mHavePss = true;
                        long memCheckRealtimeInterval = Settings.Secure.getLong(
                                mResolver, Settings.Secure.MEMCHECK_LOG_REALTIME_INTERVAL,
                                MEMCHECK_DEFAULT_LOG_REALTIME_INTERVAL) * 1000;
                        long realtimeNow = SystemClock.elapsedRealtime();
                        if ((mLastMemCheckRealtime+memCheckRealtimeInterval) < realtimeNow) {
                            mLastMemCheckRealtime = realtimeNow;
                            if (localLOGV) Slog.v(TAG, "Collecting global memory usage.");
                            collectGlobalMemory();
                            mHaveGlobalPss = true;
                        }
                    }
                    final int size = mMonitors.size();
                    for (int i = 0 ; i < size ; i++) {
                        mCurrentMonitor = mMonitors.get(i);
                        mCurrentMonitor.monitor();
                    }
                    synchronized (Watchdog.this) {
                        mCompleted = true;
                        mCurrentMonitor = null;
                    }
                } break;
            }
        }
    }
    final class GlobalPssCollected implements Runnable {
        public void run() {
            mHandler.sendEmptyMessage(GLOBAL_PSS);
        }
    }
    final class CheckupReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context c, Intent intent) {
            if (localLOGV) Slog.v(TAG, "Alarm went off, checking memory.");
            checkMemory();
        }
    }
    final class RebootReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context c, Intent intent) {
            if (localLOGV) Slog.v(TAG, "Alarm went off, checking reboot.");
            checkReboot(true);
        }
    }
    final class RebootRequestReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context c, Intent intent) {
            mReqRebootNoWait = intent.getIntExtra("nowait", 0) != 0;
            mReqRebootInterval = intent.getIntExtra("interval", -1);
            mReqRebootStartTime = intent.getIntExtra("startTime", -1);
            mReqRebootWindow = intent.getIntExtra("window", -1);
            mReqMinScreenOff = intent.getIntExtra("minScreenOff", -1);
            mReqMinNextAlarm = intent.getIntExtra("minNextAlarm", -1);
            mReqRecheckInterval = intent.getIntExtra("recheckInterval", -1);
            EventLog.writeEvent(EventLogTags.WATCHDOG_REQUESTED_REBOOT,
                    mReqRebootNoWait ? 1 : 0, mReqRebootInterval,
                            mReqRecheckInterval, mReqRebootStartTime,
                    mReqRebootWindow, mReqMinScreenOff, mReqMinNextAlarm);
            checkReboot(true);
        }
    }
    public interface Monitor {
        void monitor();
    }
    public interface PssRequestor {
        void requestPss();
    }
    public class PssStats {
        public int mEmptyPss;
        public int mEmptyCount;
        public int mBackgroundPss;
        public int mBackgroundCount;
        public int mServicePss;
        public int mServiceCount;
        public int mVisiblePss;
        public int mVisibleCount;
        public int mForegroundPss;
        public int mForegroundCount;
        public int mNoPssCount;
        public int mProcDeaths[] = new int[10];
    }
    public static Watchdog getInstance() {
        if (sWatchdog == null) {
            sWatchdog = new Watchdog();
        }
        return sWatchdog;
    }
    private Watchdog() {
        super("watchdog");
        mHandler = new HeartbeatHandler();
        mGlobalPssCollected = new GlobalPssCollected();
    }
    public void init(Context context, BatteryService battery,
            PowerManagerService power, AlarmManagerService alarm,
            ActivityManagerService activity) {
        mResolver = context.getContentResolver();
        mBattery = battery;
        mPower = power;
        mAlarm = alarm;
        mActivity = activity;
        context.registerReceiver(new CheckupReceiver(),
                new IntentFilter(CHECKUP_ACTION));
        mCheckupIntent = PendingIntent.getBroadcast(context,
                0, new Intent(CHECKUP_ACTION), 0);
        context.registerReceiver(new RebootReceiver(),
                new IntentFilter(REBOOT_ACTION));
        mRebootIntent = PendingIntent.getBroadcast(context,
                0, new Intent(REBOOT_ACTION), 0);
        context.registerReceiver(new RebootRequestReceiver(),
                new IntentFilter(Intent.ACTION_REBOOT),
                android.Manifest.permission.REBOOT, null);
        mBootTime = System.currentTimeMillis();
    }
    public void processStarted(PssRequestor req, String name, int pid) {
        synchronized (this) {
            if ("com.android.phone".equals(name)) {
                mPhoneReq = req;
                mPhonePid = pid;
                mPhonePss = 0;
            }
        }
    }
    public void reportPss(PssRequestor req, String name, int pss) {
        synchronized (this) {
            if (mPhoneReq == req) {
                mPhonePss = pss;
            }
        }
    }
    public void addMonitor(Monitor monitor) {
        synchronized (this) {
            if (isAlive()) {
                throw new RuntimeException("Monitors can't be added while the Watchdog is running");
            }
            mMonitors.add(monitor);
        }
    }
    void collectMemory() {
        synchronized (this) {
            if (mPhoneReq != null) {
                mPhoneReq.requestPss();
            }
        }
    }
    void collectGlobalMemory() {
        mActivity.requestPss(mGlobalPssCollected);
    }
    void checkMemory() {
        boolean needScheduledCheck;
        long curTime;
        long nextTime = 0;
        long recheckInterval = Settings.Secure.getLong(
                mResolver, Settings.Secure.MEMCHECK_RECHECK_INTERVAL,
                MEMCHECK_DEFAULT_RECHECK_INTERVAL) * 1000;
        mSystemMemMonitor.retrieveSettings(mResolver);
        mPhoneMemMonitor.retrieveSettings(mResolver);
        retrieveBrutalityAmount();
        synchronized (this) {
            curTime = System.currentTimeMillis();
            mNeedScheduledCheck = false;
            if (mSystemMemMonitor.checkLocked(curTime, Process.myPid(),
                    (int)Process.getPss(Process.myPid()))) {
                mForceKillSystem = true;
                notifyAll();
                return;
            }
            if (mPhoneReq != null) {
                if (mPhoneMemMonitor.checkLocked(curTime, mPhonePid,
                        mPhonePss)) {
                    Slog.i(TAG, "Watchdog is killing the phone process");
                    Process.killProcess(mPhonePid);
                }
            } else {
                mPhoneMemMonitor.clear();
            }
            needScheduledCheck = mNeedScheduledCheck;
            if (needScheduledCheck) {
                nextTime = curTime + recheckInterval;
                if (nextTime < mMemcheckExecStartTime) {
                    nextTime = mMemcheckExecStartTime;
                } else if (nextTime >= mMemcheckExecEndTime){
                    if (localLOGV) Slog.v(TAG, "Computing next time range");
                    computeMemcheckTimesLocked(nextTime);
                    nextTime = mMemcheckExecStartTime;
                }
                if (localLOGV) {
                    mCalendar.setTimeInMillis(nextTime);
                    Slog.v(TAG, "Next Alarm Time: " + mCalendar);
                }
            }
        }
        if (needScheduledCheck) {
            if (localLOGV) Slog.v(TAG, "Scheduling next memcheck alarm for "
                    + ((nextTime-curTime)/1000/60) + "m from now");
            mAlarm.remove(mCheckupIntent);
            mAlarm.set(AlarmManager.RTC_WAKEUP, nextTime, mCheckupIntent);
        } else {
            if (localLOGV) Slog.v(TAG, "No need to schedule a memcheck alarm!");
            mAlarm.remove(mCheckupIntent);
        }
    }
    final PssStats mPssStats = new PssStats();
    final String[] mMemInfoFields = new String[] {
            "MemFree:", "Buffers:", "Cached:",
            "Active:", "Inactive:",
            "AnonPages:", "Mapped:", "Slab:",
            "SReclaimable:", "SUnreclaim:", "PageTables:" };
    final long[] mMemInfoSizes = new long[mMemInfoFields.length];
    final String[] mVMStatFields = new String[] {
            "pgfree ", "pgactivate ", "pgdeactivate ",
            "pgfault ", "pgmajfault " };
    final long[] mVMStatSizes = new long[mVMStatFields.length];
    final long[] mPrevVMStatSizes = new long[mVMStatFields.length];
    long mLastLogGlobalMemoryTime;
    void logGlobalMemory() {
        PssStats stats = mPssStats;
        mActivity.collectPss(stats);
        EventLog.writeEvent(EventLogTags.WATCHDOG_PSS_STATS,
                stats.mEmptyPss, stats.mEmptyCount,
                stats.mBackgroundPss, stats.mBackgroundCount,
                stats.mServicePss, stats.mServiceCount,
                stats.mVisiblePss, stats.mVisibleCount,
                stats.mForegroundPss, stats.mForegroundCount,
                stats.mNoPssCount);
        EventLog.writeEvent(EventLogTags.WATCHDOG_PROC_STATS,
                stats.mProcDeaths[0], stats.mProcDeaths[1], stats.mProcDeaths[2],
                stats.mProcDeaths[3], stats.mProcDeaths[4]);
        Process.readProcLines("/proc/meminfo", mMemInfoFields, mMemInfoSizes);
        for (int i=0; i<mMemInfoSizes.length; i++) {
            mMemInfoSizes[i] *= 1024;
        }
        EventLog.writeEvent(EventLogTags.WATCHDOG_MEMINFO,
                (int)mMemInfoSizes[0], (int)mMemInfoSizes[1], (int)mMemInfoSizes[2],
                (int)mMemInfoSizes[3], (int)mMemInfoSizes[4],
                (int)mMemInfoSizes[5], (int)mMemInfoSizes[6], (int)mMemInfoSizes[7],
                (int)mMemInfoSizes[8], (int)mMemInfoSizes[9], (int)mMemInfoSizes[10]);
        long now = SystemClock.uptimeMillis();
        long dur = now - mLastLogGlobalMemoryTime;
        mLastLogGlobalMemoryTime = now;
        Process.readProcLines("/proc/vmstat", mVMStatFields, mVMStatSizes);
        for (int i=0; i<mVMStatSizes.length; i++) {
            long v = mVMStatSizes[i];
            mVMStatSizes[i] -= mPrevVMStatSizes[i];
            mPrevVMStatSizes[i] = v;
        }
        EventLog.writeEvent(EventLogTags.WATCHDOG_VMSTAT, dur,
                (int)mVMStatSizes[0], (int)mVMStatSizes[1], (int)mVMStatSizes[2],
                (int)mVMStatSizes[3], (int)mVMStatSizes[4]);
    }
    void checkReboot(boolean fromAlarm) {
        int rebootInterval = mReqRebootInterval >= 0 ? mReqRebootInterval
                : Settings.Secure.getInt(
                mResolver, Settings.Secure.REBOOT_INTERVAL,
                REBOOT_DEFAULT_INTERVAL);
        mRebootInterval = rebootInterval;
        if (rebootInterval <= 0) {
            if (localLOGV) Slog.v(TAG, "No need to schedule a reboot alarm!");
            mAlarm.remove(mRebootIntent);
            return;
        }
        long rebootStartTime = mReqRebootStartTime >= 0 ? mReqRebootStartTime
                : Settings.Secure.getLong(
                mResolver, Settings.Secure.REBOOT_START_TIME,
                REBOOT_DEFAULT_START_TIME);
        long rebootWindowMillis = (mReqRebootWindow >= 0 ? mReqRebootWindow
                : Settings.Secure.getLong(
                mResolver, Settings.Secure.REBOOT_WINDOW,
                REBOOT_DEFAULT_WINDOW)) * 1000;
        long recheckInterval = (mReqRecheckInterval >= 0 ? mReqRecheckInterval
                : Settings.Secure.getLong(
                mResolver, Settings.Secure.MEMCHECK_RECHECK_INTERVAL,
                MEMCHECK_DEFAULT_RECHECK_INTERVAL)) * 1000;
        retrieveBrutalityAmount();
        long realStartTime;
        long now;
        synchronized (this) {
            now = System.currentTimeMillis();
            realStartTime = computeCalendarTime(mCalendar, now,
                    rebootStartTime);
            long rebootIntervalMillis = rebootInterval*24*60*60*1000;
            if (DB || mReqRebootNoWait ||
                    (now-mBootTime) >= (rebootIntervalMillis-rebootWindowMillis)) {
                if (fromAlarm && rebootWindowMillis <= 0) {
                    EventLog.writeEvent(EventLogTags.WATCHDOG_SCHEDULED_REBOOT, now,
                            (int)rebootIntervalMillis, (int)rebootStartTime*1000,
                            (int)rebootWindowMillis, "");
                    rebootSystem("Checkin scheduled forced");
                    return;
                }
                if (now < realStartTime) {
                    realStartTime = computeCalendarTime(mCalendar,
                            now, rebootStartTime);
                } else if (now < (realStartTime+rebootWindowMillis)) {
                    String doit = shouldWeBeBrutalLocked(now);
                    EventLog.writeEvent(EventLogTags.WATCHDOG_SCHEDULED_REBOOT, now,
                            (int)rebootInterval, (int)rebootStartTime*1000,
                            (int)rebootWindowMillis, doit != null ? doit : "");
                    if (doit == null) {
                        rebootSystem("Checked scheduled range");
                        return;
                    }
                    if ((now+recheckInterval) >= (realStartTime+rebootWindowMillis)) {
                        realStartTime = computeCalendarTime(mCalendar,
                                now + rebootIntervalMillis, rebootStartTime);
                    } else {
                        realStartTime = now + recheckInterval;
                    }
                } else {
                    realStartTime = computeCalendarTime(mCalendar,
                            now + rebootIntervalMillis, rebootStartTime);
                }
            }
        }
        if (localLOGV) Slog.v(TAG, "Scheduling next reboot alarm for "
                + ((realStartTime-now)/1000/60) + "m from now");
        mAlarm.remove(mRebootIntent);
        mAlarm.set(AlarmManager.RTC_WAKEUP, realStartTime, mRebootIntent);
    }
    void rebootSystem(String reason) {
        Slog.i(TAG, "Rebooting system because: " + reason);
        PowerManagerService pms = (PowerManagerService) ServiceManager.getService("power");
        pms.reboot(reason);
    }
    void retrieveBrutalityAmount() {
        mMinScreenOff = (mReqMinScreenOff >= 0 ? mReqMinScreenOff
                : Settings.Secure.getInt(
                mResolver, Settings.Secure.MEMCHECK_MIN_SCREEN_OFF,
                MEMCHECK_DEFAULT_MIN_SCREEN_OFF)) * 1000;
        mMinAlarm = (mReqMinNextAlarm >= 0 ? mReqMinNextAlarm
                : Settings.Secure.getInt(
                mResolver, Settings.Secure.MEMCHECK_MIN_ALARM,
                MEMCHECK_DEFAULT_MIN_ALARM)) * 1000;
    }
    String shouldWeBeBrutalLocked(long curTime) {
        if (mBattery == null || !mBattery.isPowered()) {
            return "battery";
        }
        if (mMinScreenOff >= 0 && (mPower == null ||
                mPower.timeSinceScreenOn() < mMinScreenOff)) {
            return "screen";
        }
        if (mMinAlarm >= 0 && (mAlarm == null ||
                mAlarm.timeToNextAlarm() < mMinAlarm)) {
            return "alarm";
        }
        return null;
    }
    void computeMemcheckTimesLocked(long curTime) {
        if (mMemcheckLastTime == curTime) {
            return;
        }
        mMemcheckLastTime = curTime;
        long memcheckExecStartTime = Settings.Secure.getLong(
                mResolver, Settings.Secure.MEMCHECK_EXEC_START_TIME,
                MEMCHECK_DEFAULT_EXEC_START_TIME);
        long memcheckExecEndTime = Settings.Secure.getLong(
                mResolver, Settings.Secure.MEMCHECK_EXEC_END_TIME,
                MEMCHECK_DEFAULT_EXEC_END_TIME);
        mMemcheckExecEndTime = computeCalendarTime(mCalendar, curTime,
                memcheckExecEndTime);
        if (mMemcheckExecEndTime < curTime) {
            memcheckExecStartTime += 24*60*60;
            memcheckExecEndTime += 24*60*60;
            mMemcheckExecEndTime = computeCalendarTime(mCalendar, curTime,
                    memcheckExecEndTime);
        }
        mMemcheckExecStartTime = computeCalendarTime(mCalendar, curTime,
                memcheckExecStartTime);
        if (localLOGV) {
            mCalendar.setTimeInMillis(curTime);
            Slog.v(TAG, "Current Time: " + mCalendar);
            mCalendar.setTimeInMillis(mMemcheckExecStartTime);
            Slog.v(TAG, "Start Check Time: " + mCalendar);
            mCalendar.setTimeInMillis(mMemcheckExecEndTime);
            Slog.v(TAG, "End Check Time: " + mCalendar);
        }
    }
    static long computeCalendarTime(Calendar c, long curTime,
            long secondsSinceMidnight) {
        c.setTimeInMillis(curTime);
        int val = (int)secondsSinceMidnight / (60*60);
        c.set(Calendar.HOUR_OF_DAY, val);
        secondsSinceMidnight -= val * (60*60);
        val = (int)secondsSinceMidnight / 60;
        c.set(Calendar.MINUTE, val);
        c.set(Calendar.SECOND, (int)secondsSinceMidnight - (val*60));
        c.set(Calendar.MILLISECOND, 0);
        long newTime = c.getTimeInMillis();
        if (newTime < curTime) {
            c.add(Calendar.DAY_OF_MONTH, 1);
            newTime = c.getTimeInMillis();
        }
        return newTime;
    }
    @Override
    public void run() {
        boolean waitedHalf = false;
        while (true) {
            mCompleted = false;
            mHandler.sendEmptyMessage(MONITOR);
            synchronized (this) {
                long timeout = TIME_TO_WAIT;
                long start = SystemClock.uptimeMillis();
                while (timeout > 0 && !mForceKillSystem) {
                    try {
                        wait(timeout);  
                    } catch (InterruptedException e) {
                        Log.wtf(TAG, e);
                    }
                    timeout = TIME_TO_WAIT - (SystemClock.uptimeMillis() - start);
                }
                if (mCompleted && !mForceKillSystem) {
                    waitedHalf = false;
                    continue;
                }
                if (!waitedHalf) {
                    ArrayList pids = new ArrayList();
                    pids.add(Process.myPid());
                    File stack = ActivityManagerService.dumpStackTraces(true, pids);
                    waitedHalf = true;
                    continue;
                }
            }
            String name = (mCurrentMonitor != null) ? mCurrentMonitor.getClass().getName() : "null";
            EventLog.writeEvent(EventLogTags.WATCHDOG, name);
            ArrayList pids = new ArrayList();
            pids.add(Process.myPid());
            if (mPhonePid > 0) pids.add(mPhonePid);
            File stack = ActivityManagerService.dumpStackTraces(!waitedHalf, pids);
            SystemClock.sleep(2000);
            if (RECORD_KERNEL_THREADS) {
                dumpKernelStackTraces();
            }
            mActivity.addErrorToDropBox("watchdog", null, null, null, name, null, stack, null);
            if (!Debug.isDebuggerConnected()) {
                Slog.w(TAG, "*** WATCHDOG KILLING SYSTEM PROCESS: " + name);
                Process.killProcess(Process.myPid());
                System.exit(10);
            } else {
                Slog.w(TAG, "Debugger connected: Watchdog is *not* killing the system process");
            }
            waitedHalf = false;
        }
    }
    private File dumpKernelStackTraces() {
        String tracesPath = SystemProperties.get("dalvik.vm.stack-trace-file", null);
        if (tracesPath == null || tracesPath.length() == 0) {
            return null;
        }
        native_dumpKernelStacks(tracesPath);
        return new File(tracesPath);
    }
    private native void native_dumpKernelStacks(String tracesPath);
}
