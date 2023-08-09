public abstract class BatteryStats implements Parcelable {
    private static final boolean LOCAL_LOGV = false;
    public static final int WAKE_TYPE_PARTIAL = 0;
    public static final int WAKE_TYPE_FULL = 1;
    public static final int WAKE_TYPE_WINDOW = 2;
    public static final int SENSOR = 3;
    public static final int WIFI_TURNED_ON = 4;
    public static final int FULL_WIFI_LOCK = 5;
    public static final int SCAN_WIFI_LOCK = 6;
     public static final int WIFI_MULTICAST_ENABLED = 7;
    public static final int AUDIO_TURNED_ON = 7;
    public static final int VIDEO_TURNED_ON = 8;
    public static final int STATS_TOTAL = 0;
    public static final int STATS_LAST = 1;
    public static final int STATS_CURRENT = 2;
    public static final int STATS_UNPLUGGED = 3;
    private static final String[] STAT_NAMES = { "t", "l", "c", "u" };
    private static final int BATTERY_STATS_CHECKIN_VERSION = 5;
    private static final long BYTES_PER_KB = 1024;
    private static final long BYTES_PER_MB = 1048576; 
    private static final long BYTES_PER_GB = 1073741824; 
    private static final String APK_DATA = "apk";
    private static final String PROCESS_DATA = "pr";
    private static final String SENSOR_DATA = "sr";
    private static final String WAKELOCK_DATA = "wl";
    private static final String KERNEL_WAKELOCK_DATA = "kwl";
    private static final String NETWORK_DATA = "nt";
    private static final String USER_ACTIVITY_DATA = "ua";
    private static final String BATTERY_DATA = "bt";
    private static final String BATTERY_LEVEL_DATA = "lv";
    private static final String WIFI_LOCK_DATA = "wfl";
    private static final String MISC_DATA = "m";
    private static final String SCREEN_BRIGHTNESS_DATA = "br";
    private static final String SIGNAL_STRENGTH_TIME_DATA = "sgt";
    private static final String SIGNAL_SCANNING_TIME_DATA = "sst";
    private static final String SIGNAL_STRENGTH_COUNT_DATA = "sgc";
    private static final String DATA_CONNECTION_TIME_DATA = "dct";
    private static final String DATA_CONNECTION_COUNT_DATA = "dcc";
    private final StringBuilder mFormatBuilder = new StringBuilder(32);
    private final Formatter mFormatter = new Formatter(mFormatBuilder);
    public static abstract class Counter {
        public abstract int getCountLocked(int which);
        public abstract void logState(Printer pw, String prefix);
    }
    public static abstract class Timer {
        public abstract int getCountLocked(int which);
        public abstract long getTotalTimeLocked(long batteryRealtime, int which);
        public abstract void logState(Printer pw, String prefix);
    }
    public static abstract class Uid {
        public abstract Map<String, ? extends Wakelock> getWakelockStats();
        public static abstract class Wakelock {
            public abstract Timer getWakeTime(int type);
        }
        public abstract Map<Integer, ? extends Sensor> getSensorStats();
        public abstract Map<String, ? extends Proc> getProcessStats();
        public abstract Map<String, ? extends Pkg> getPackageStats();
        public abstract int getUid();
        public abstract long getTcpBytesReceived(int which);
        public abstract long getTcpBytesSent(int which);
        public abstract void noteWifiTurnedOnLocked();
        public abstract void noteWifiTurnedOffLocked();
        public abstract void noteFullWifiLockAcquiredLocked();
        public abstract void noteFullWifiLockReleasedLocked();
        public abstract void noteScanWifiLockAcquiredLocked();
        public abstract void noteScanWifiLockReleasedLocked();
        public abstract void noteWifiMulticastEnabledLocked();
        public abstract void noteWifiMulticastDisabledLocked();
        public abstract void noteAudioTurnedOnLocked();
        public abstract void noteAudioTurnedOffLocked();
        public abstract void noteVideoTurnedOnLocked();
        public abstract void noteVideoTurnedOffLocked();
        public abstract long getWifiTurnedOnTime(long batteryRealtime, int which);
        public abstract long getFullWifiLockTime(long batteryRealtime, int which);
        public abstract long getScanWifiLockTime(long batteryRealtime, int which);
        public abstract long getWifiMulticastTime(long batteryRealtime,
                                                  int which);
        public abstract long getAudioTurnedOnTime(long batteryRealtime, int which);
        public abstract long getVideoTurnedOnTime(long batteryRealtime, int which);
        static final String[] USER_ACTIVITY_TYPES = {
            "other", "cheek", "touch", "long_touch", "touch_up", "button", "unknown"
        };
        public static final int NUM_USER_ACTIVITY_TYPES = 7;
        public abstract void noteUserActivityLocked(int type);
        public abstract boolean hasUserActivity();
        public abstract int getUserActivityCount(int type, int which);
        public static abstract class Sensor {
            public static final int GPS = -10000;
            public abstract int getHandle();
            public abstract Timer getSensorTime();
        }
        public static abstract class Proc {
            public abstract long getUserTime(int which);
            public abstract long getSystemTime(int which);
            public abstract int getStarts(int which);
            public abstract long getForegroundTime(int which);
            public abstract long getTimeAtCpuSpeedStep(int speedStep, int which);
        }
        public static abstract class Pkg {
            public abstract int getWakeups(int which);
            public abstract Map<String, ? extends Serv> getServiceStats();
            public abstract class Serv {
                public abstract long getStartTime(long batteryUptime, int which);
                public abstract int getStarts(int which);
                public abstract int getLaunches(int which);
            }
        }
    }
    public abstract int getStartCount();
    public abstract long getScreenOnTime(long batteryRealtime, int which);
    public static final int SCREEN_BRIGHTNESS_DARK = 0;
    public static final int SCREEN_BRIGHTNESS_DIM = 1;
    public static final int SCREEN_BRIGHTNESS_MEDIUM = 2;
    public static final int SCREEN_BRIGHTNESS_LIGHT = 3;
    public static final int SCREEN_BRIGHTNESS_BRIGHT = 4;
    static final String[] SCREEN_BRIGHTNESS_NAMES = {
        "dark", "dim", "medium", "light", "bright"
    };
    public static final int NUM_SCREEN_BRIGHTNESS_BINS = 5;
    public abstract long getScreenBrightnessTime(int brightnessBin,
            long batteryRealtime, int which);
    public abstract int getInputEventCount(int which);
    public abstract long getPhoneOnTime(long batteryRealtime, int which);
    public static final int SIGNAL_STRENGTH_NONE_OR_UNKNOWN = 0;
    public static final int SIGNAL_STRENGTH_POOR = 1;
    public static final int SIGNAL_STRENGTH_MODERATE = 2;
    public static final int SIGNAL_STRENGTH_GOOD = 3;
    public static final int SIGNAL_STRENGTH_GREAT = 4;
    static final String[] SIGNAL_STRENGTH_NAMES = {
        "none", "poor", "moderate", "good", "great"
    };
    public static final int NUM_SIGNAL_STRENGTH_BINS = 5;
    public abstract long getPhoneSignalStrengthTime(int strengthBin,
            long batteryRealtime, int which);
    public abstract long getPhoneSignalScanningTime(
            long batteryRealtime, int which);
    public abstract int getPhoneSignalStrengthCount(int strengthBin, int which);
    public static final int DATA_CONNECTION_NONE = 0;
    public static final int DATA_CONNECTION_GPRS = 1;
    public static final int DATA_CONNECTION_EDGE = 2;
    public static final int DATA_CONNECTION_UMTS = 3;
    public static final int DATA_CONNECTION_OTHER = 4;
    static final String[] DATA_CONNECTION_NAMES = {
        "none", "gprs", "edge", "umts", "other"
    };
    public static final int NUM_DATA_CONNECTION_TYPES = 5;
    public abstract long getPhoneDataConnectionTime(int dataType,
            long batteryRealtime, int which);
    public abstract int getPhoneDataConnectionCount(int dataType, int which);
    public abstract long getWifiOnTime(long batteryRealtime, int which);
    public abstract long getWifiRunningTime(long batteryRealtime, int which);
    public abstract long getBluetoothOnTime(long batteryRealtime, int which);
    public abstract boolean getIsOnBattery();
    public abstract SparseArray<? extends Uid> getUidStats();
    public abstract long getBatteryUptime(long curTime);
    public long getRadioDataUptimeMs() {
        return getRadioDataUptime() / 1000;
    }
    public abstract long getRadioDataUptime();
    public abstract long getBatteryRealtime(long curTime);
    public abstract int getDischargeStartLevel();
    public abstract int getDischargeCurrentLevel();
    public abstract long computeBatteryUptime(long curTime, int which);
    public abstract long computeBatteryRealtime(long curTime, int which);
    public abstract long computeUptime(long curTime, int which);
    public abstract long computeRealtime(long curTime, int which);
    public abstract Map<String, ? extends Timer> getKernelWakelockStats();
    public abstract int getCpuSpeedSteps();
    private final static void formatTimeRaw(StringBuilder out, long seconds) {
        long days = seconds / (60 * 60 * 24);
        if (days != 0) {
            out.append(days);
            out.append("d ");
        }
        long used = days * 60 * 60 * 24;
        long hours = (seconds - used) / (60 * 60);
        if (hours != 0 || used != 0) {
            out.append(hours);
            out.append("h ");
        }
        used += hours * 60 * 60;
        long mins = (seconds-used) / 60;
        if (mins != 0 || used != 0) {
            out.append(mins);
            out.append("m ");
        }
        used += mins * 60;
        if (seconds != 0 || used != 0) {
            out.append(seconds-used);
            out.append("s ");
        }
    }
    private final static void formatTime(StringBuilder sb, long time) {
        long sec = time / 100;
        formatTimeRaw(sb, sec);
        sb.append((time - (sec * 100)) * 10);
        sb.append("ms ");
    }
    private final static void formatTimeMs(StringBuilder sb, long time) {
        long sec = time / 1000;
        formatTimeRaw(sb, sec);
        sb.append(time - (sec * 1000));
        sb.append("ms ");
    }
    private final String formatRatioLocked(long num, long den) {
        if (den == 0L) {
            return "---%";
        }
        float perc = ((float)num) / ((float)den) * 100;
        mFormatBuilder.setLength(0);
        mFormatter.format("%.1f%%", perc);
        return mFormatBuilder.toString();
    }
    private final String formatBytesLocked(long bytes) {
        mFormatBuilder.setLength(0);
        if (bytes < BYTES_PER_KB) {
            return bytes + "B";
        } else if (bytes < BYTES_PER_MB) {
            mFormatter.format("%.2fKB", bytes / (double) BYTES_PER_KB);
            return mFormatBuilder.toString();
        } else if (bytes < BYTES_PER_GB){
            mFormatter.format("%.2fMB", bytes / (double) BYTES_PER_MB);
            return mFormatBuilder.toString();
        } else {
            mFormatter.format("%.2fGB", bytes / (double) BYTES_PER_GB);
            return mFormatBuilder.toString();
        }
    }
    private static final String printWakeLock(StringBuilder sb, Timer timer,
            long batteryRealtime, String name, int which, String linePrefix) {
        if (timer != null) {
            long totalTimeMicros = timer.getTotalTimeLocked(batteryRealtime, which);
            long totalTimeMillis = (totalTimeMicros + 500) / 1000;
            int count = timer.getCountLocked(which);
            if (totalTimeMillis != 0) {
                sb.append(linePrefix);
                formatTimeMs(sb, totalTimeMillis);
                if (name != null) sb.append(name);
                sb.append(' ');
                sb.append('(');
                sb.append(count);
                sb.append(" times)");
                return ", ";
            }
        }
        return linePrefix;
    }
    private static final String printWakeLockCheckin(StringBuilder sb, Timer timer, long now,
            String name, int which, String linePrefix) {
        long totalTimeMicros = 0;
        int count = 0;
        if (timer != null) {
            totalTimeMicros = timer.getTotalTimeLocked(now, which);
            count = timer.getCountLocked(which); 
        }
        sb.append(linePrefix);
        sb.append((totalTimeMicros + 500) / 1000); 
        sb.append(',');
        sb.append(name != null ? name + "," : "");
        sb.append(count);
        return ",";
    }
    private static final void dumpLine(PrintWriter pw, int uid, String category, String type, 
           Object... args ) {
        pw.print(BATTERY_STATS_CHECKIN_VERSION); pw.print(',');
        pw.print(uid); pw.print(',');
        pw.print(category); pw.print(',');
        pw.print(type); 
        for (Object arg : args) {  
            pw.print(','); 
            pw.print(arg); 
        }
        pw.print('\n');
    }
    public final void dumpCheckinLocked(PrintWriter pw, int which, int reqUid) {
        final long rawUptime = SystemClock.uptimeMillis() * 1000;
        final long rawRealtime = SystemClock.elapsedRealtime() * 1000;
        final long batteryUptime = getBatteryUptime(rawUptime);
        final long batteryRealtime = getBatteryRealtime(rawRealtime);
        final long whichBatteryUptime = computeBatteryUptime(rawUptime, which);
        final long whichBatteryRealtime = computeBatteryRealtime(rawRealtime, which);
        final long totalRealtime = computeRealtime(rawRealtime, which);
        final long totalUptime = computeUptime(rawUptime, which);
        final long screenOnTime = getScreenOnTime(batteryRealtime, which);
        final long phoneOnTime = getPhoneOnTime(batteryRealtime, which);
        final long wifiOnTime = getWifiOnTime(batteryRealtime, which);
        final long wifiRunningTime = getWifiRunningTime(batteryRealtime, which);
        final long bluetoothOnTime = getBluetoothOnTime(batteryRealtime, which);
        StringBuilder sb = new StringBuilder(128);
        SparseArray<? extends Uid> uidStats = getUidStats();
        final int NU = uidStats.size();
        String category = STAT_NAMES[which];
        dumpLine(pw, 0 , category, BATTERY_DATA, 
                which == STATS_TOTAL ? getStartCount() : "N/A",
                whichBatteryRealtime / 1000, whichBatteryUptime / 1000,
                totalRealtime / 1000, totalUptime / 1000); 
        long rxTotal = 0;
        long txTotal = 0;
        long fullWakeLockTimeTotal = 0;
        long partialWakeLockTimeTotal = 0;
        for (int iu = 0; iu < NU; iu++) {
            Uid u = uidStats.valueAt(iu);
            rxTotal += u.getTcpBytesReceived(which);
            txTotal += u.getTcpBytesSent(which);
            Map<String, ? extends BatteryStats.Uid.Wakelock> wakelocks = u.getWakelockStats();
            if (wakelocks.size() > 0) {
                for (Map.Entry<String, ? extends BatteryStats.Uid.Wakelock> ent 
                        : wakelocks.entrySet()) {
                    Uid.Wakelock wl = ent.getValue();
                    Timer fullWakeTimer = wl.getWakeTime(WAKE_TYPE_FULL);
                    if (fullWakeTimer != null) {
                        fullWakeLockTimeTotal += fullWakeTimer.getTotalTimeLocked(batteryRealtime, which);
                    }
                    Timer partialWakeTimer = wl.getWakeTime(WAKE_TYPE_PARTIAL);
                    if (partialWakeTimer != null) {
                        partialWakeLockTimeTotal += partialWakeTimer.getTotalTimeLocked(
                            batteryRealtime, which);
                    }
                }
            }
        }
        dumpLine(pw, 0 , category, MISC_DATA,
                screenOnTime / 1000, phoneOnTime / 1000, wifiOnTime / 1000,
                wifiRunningTime / 1000, bluetoothOnTime / 1000, rxTotal, txTotal, 
                fullWakeLockTimeTotal, partialWakeLockTimeTotal,
                getInputEventCount(which));
        Object[] args = new Object[NUM_SCREEN_BRIGHTNESS_BINS];
        for (int i=0; i<NUM_SCREEN_BRIGHTNESS_BINS; i++) {
            args[i] = getScreenBrightnessTime(i, batteryRealtime, which) / 1000;
        }
        dumpLine(pw, 0 , category, SCREEN_BRIGHTNESS_DATA, args);
        args = new Object[NUM_SIGNAL_STRENGTH_BINS];
        for (int i=0; i<NUM_SIGNAL_STRENGTH_BINS; i++) {
            args[i] = getPhoneSignalStrengthTime(i, batteryRealtime, which) / 1000;
        }
        dumpLine(pw, 0 , category, SIGNAL_STRENGTH_TIME_DATA, args);
        dumpLine(pw, 0 , category, SIGNAL_SCANNING_TIME_DATA,
                getPhoneSignalScanningTime(batteryRealtime, which) / 1000);
        for (int i=0; i<NUM_SIGNAL_STRENGTH_BINS; i++) {
            args[i] = getPhoneSignalStrengthCount(i, which);
        }
        dumpLine(pw, 0 , category, SIGNAL_STRENGTH_COUNT_DATA, args);
        args = new Object[NUM_DATA_CONNECTION_TYPES];
        for (int i=0; i<NUM_DATA_CONNECTION_TYPES; i++) {
            args[i] = getPhoneDataConnectionTime(i, batteryRealtime, which) / 1000;
        }
        dumpLine(pw, 0 , category, DATA_CONNECTION_TIME_DATA, args);
        for (int i=0; i<NUM_DATA_CONNECTION_TYPES; i++) {
            args[i] = getPhoneDataConnectionCount(i, which);
        }
        dumpLine(pw, 0 , category, DATA_CONNECTION_COUNT_DATA, args);
        if (which == STATS_UNPLUGGED) {
            dumpLine(pw, 0 , category, BATTERY_LEVEL_DATA, getDischargeStartLevel(), 
                    getDischargeCurrentLevel());
        }
        if (reqUid < 0) {
            Map<String, ? extends BatteryStats.Timer> kernelWakelocks = getKernelWakelockStats();
            if (kernelWakelocks.size() > 0) {
                for (Map.Entry<String, ? extends BatteryStats.Timer> ent : kernelWakelocks.entrySet()) {
                    sb.setLength(0);
                    printWakeLockCheckin(sb, ent.getValue(), batteryRealtime, null, which, "");
                    dumpLine(pw, 0 , category, KERNEL_WAKELOCK_DATA, ent.getKey(), 
                            sb.toString());
                }
            }
        }
        for (int iu = 0; iu < NU; iu++) {
            final int uid = uidStats.keyAt(iu);
            if (reqUid >= 0 && uid != reqUid) {
                continue;
            }
            Uid u = uidStats.valueAt(iu);
            long rx = u.getTcpBytesReceived(which);
            long tx = u.getTcpBytesSent(which);
            long fullWifiLockOnTime = u.getFullWifiLockTime(batteryRealtime, which);
            long scanWifiLockOnTime = u.getScanWifiLockTime(batteryRealtime, which);
            long wifiTurnedOnTime = u.getWifiTurnedOnTime(batteryRealtime, which);
            if (rx > 0 || tx > 0) dumpLine(pw, uid, category, NETWORK_DATA, rx, tx);
            if (fullWifiLockOnTime != 0 || scanWifiLockOnTime != 0
                    || wifiTurnedOnTime != 0) {
                dumpLine(pw, uid, category, WIFI_LOCK_DATA, 
                        fullWifiLockOnTime, scanWifiLockOnTime, wifiTurnedOnTime);
            }
            if (u.hasUserActivity()) {
                args = new Object[Uid.NUM_USER_ACTIVITY_TYPES];
                boolean hasData = false;
                for (int i=0; i<Uid.NUM_USER_ACTIVITY_TYPES; i++) {
                    int val = u.getUserActivityCount(i, which);
                    args[i] = val;
                    if (val != 0) hasData = true;
                }
                if (hasData) {
                    dumpLine(pw, 0 , category, USER_ACTIVITY_DATA, args);
                }
            }
            Map<String, ? extends BatteryStats.Uid.Wakelock> wakelocks = u.getWakelockStats();
            if (wakelocks.size() > 0) {
                for (Map.Entry<String, ? extends BatteryStats.Uid.Wakelock> ent
                        : wakelocks.entrySet()) {
                    Uid.Wakelock wl = ent.getValue();
                    String linePrefix = "";
                    sb.setLength(0);
                    linePrefix = printWakeLockCheckin(sb, wl.getWakeTime(WAKE_TYPE_FULL), 
                            batteryRealtime, "f", which, linePrefix);
                    linePrefix = printWakeLockCheckin(sb, wl.getWakeTime(WAKE_TYPE_PARTIAL), 
                            batteryRealtime, "p", which, linePrefix);
                    linePrefix = printWakeLockCheckin(sb, wl.getWakeTime(WAKE_TYPE_WINDOW), 
                            batteryRealtime, "w", which, linePrefix);
                    if (sb.length() > 0) {
                       dumpLine(pw, uid, category, WAKELOCK_DATA, ent.getKey(), sb.toString());
                    }
                }
            }
            Map<Integer, ? extends BatteryStats.Uid.Sensor> sensors = u.getSensorStats();
            if (sensors.size() > 0)  {
                for (Map.Entry<Integer, ? extends BatteryStats.Uid.Sensor> ent
                        : sensors.entrySet()) {
                    Uid.Sensor se = ent.getValue();
                    int sensorNumber = ent.getKey();
                    Timer timer = se.getSensorTime();
                    if (timer != null) {
                        long totalTime = (timer.getTotalTimeLocked(batteryRealtime, which) + 500) / 1000;
                        int count = timer.getCountLocked(which);
                        if (totalTime != 0) {
                            dumpLine(pw, uid, category, SENSOR_DATA, sensorNumber, totalTime, count);
                        }
                    } 
                }
            }
            Map<String, ? extends BatteryStats.Uid.Proc> processStats = u.getProcessStats();
            if (processStats.size() > 0) {
                for (Map.Entry<String, ? extends BatteryStats.Uid.Proc> ent
                        : processStats.entrySet()) {
                    Uid.Proc ps = ent.getValue();
                    long userTime = ps.getUserTime(which);
                    long systemTime = ps.getSystemTime(which);
                    int starts = ps.getStarts(which);
                    if (userTime != 0 || systemTime != 0 || starts != 0) {
                        dumpLine(pw, uid, category, PROCESS_DATA, 
                                ent.getKey(), 
                                userTime * 10, 
                                systemTime * 10, 
                                starts); 
                    }
                }
            }
            Map<String, ? extends BatteryStats.Uid.Pkg> packageStats = u.getPackageStats();
            if (packageStats.size() > 0) {
                for (Map.Entry<String, ? extends BatteryStats.Uid.Pkg> ent
                        : packageStats.entrySet()) {
                    Uid.Pkg ps = ent.getValue();
                    int wakeups = ps.getWakeups(which);
                    Map<String, ? extends  Uid.Pkg.Serv> serviceStats = ps.getServiceStats();
                    for (Map.Entry<String, ? extends BatteryStats.Uid.Pkg.Serv> sent
                            : serviceStats.entrySet()) {
                        BatteryStats.Uid.Pkg.Serv ss = sent.getValue();
                        long startTime = ss.getStartTime(batteryUptime, which);
                        int starts = ss.getStarts(which);
                        int launches = ss.getLaunches(which);
                        if (startTime != 0 || starts != 0 || launches != 0) {
                            dumpLine(pw, uid, category, APK_DATA, 
                                    wakeups, 
                                    ent.getKey(), 
                                    sent.getKey(), 
                                    startTime / 1000, 
                                    starts,
                                    launches);
                        }
                    }
                }
            }
        }
    }
    @SuppressWarnings("unused")
    public final void dumpLocked(PrintWriter pw, String prefix, int which, int reqUid) {
        final long rawUptime = SystemClock.uptimeMillis() * 1000;
        final long rawRealtime = SystemClock.elapsedRealtime() * 1000;
        final long batteryUptime = getBatteryUptime(rawUptime);
        final long batteryRealtime = getBatteryRealtime(rawRealtime);
        final long whichBatteryUptime = computeBatteryUptime(rawUptime, which);
        final long whichBatteryRealtime = computeBatteryRealtime(rawRealtime, which);
        final long totalRealtime = computeRealtime(rawRealtime, which);
        final long totalUptime = computeUptime(rawUptime, which);
        StringBuilder sb = new StringBuilder(128);
        SparseArray<? extends Uid> uidStats = getUidStats();
        final int NU = uidStats.size();
        sb.setLength(0);
        sb.append(prefix);
                sb.append("  Time on battery: ");
                formatTimeMs(sb, whichBatteryRealtime / 1000); sb.append("(");
                sb.append(formatRatioLocked(whichBatteryRealtime, totalRealtime));
                sb.append(") realtime, ");
                formatTimeMs(sb, whichBatteryUptime / 1000);
                sb.append("("); sb.append(formatRatioLocked(whichBatteryUptime, totalRealtime));
                sb.append(") uptime");
        pw.println(sb.toString());
        sb.setLength(0);
        sb.append(prefix);
                sb.append("  Total run time: ");
                formatTimeMs(sb, totalRealtime / 1000);
                sb.append("realtime, ");
                formatTimeMs(sb, totalUptime / 1000);
                sb.append("uptime, ");
        pw.println(sb.toString());
        final long screenOnTime = getScreenOnTime(batteryRealtime, which);
        final long phoneOnTime = getPhoneOnTime(batteryRealtime, which);
        final long wifiRunningTime = getWifiRunningTime(batteryRealtime, which);
        final long wifiOnTime = getWifiOnTime(batteryRealtime, which);
        final long bluetoothOnTime = getBluetoothOnTime(batteryRealtime, which);
        sb.setLength(0);
        sb.append(prefix);
                sb.append("  Screen on: "); formatTimeMs(sb, screenOnTime / 1000);
                sb.append("("); sb.append(formatRatioLocked(screenOnTime, whichBatteryRealtime));
                sb.append("), Input events: "); sb.append(getInputEventCount(which));
                sb.append(", Active phone call: "); formatTimeMs(sb, phoneOnTime / 1000);
                sb.append("("); sb.append(formatRatioLocked(phoneOnTime, whichBatteryRealtime));
                sb.append(")");
        pw.println(sb.toString());
        sb.setLength(0);
        sb.append(prefix);
        sb.append("  Screen brightnesses: ");
        boolean didOne = false;
        for (int i=0; i<NUM_SCREEN_BRIGHTNESS_BINS; i++) {
            final long time = getScreenBrightnessTime(i, batteryRealtime, which);
            if (time == 0) {
                continue;
            }
            if (didOne) sb.append(", ");
            didOne = true;
            sb.append(SCREEN_BRIGHTNESS_NAMES[i]);
            sb.append(" ");
            formatTimeMs(sb, time/1000);
            sb.append("(");
            sb.append(formatRatioLocked(time, screenOnTime));
            sb.append(")");
        }
        if (!didOne) sb.append("No activity");
        pw.println(sb.toString());
        long rxTotal = 0;
        long txTotal = 0;
        long fullWakeLockTimeTotalMicros = 0;
        long partialWakeLockTimeTotalMicros = 0;
        if (reqUid < 0) {
            Map<String, ? extends BatteryStats.Timer> kernelWakelocks = getKernelWakelockStats();
            if (kernelWakelocks.size() > 0) {
                for (Map.Entry<String, ? extends BatteryStats.Timer> ent : kernelWakelocks.entrySet()) {
                    String linePrefix = ": ";
                    sb.setLength(0);
                    sb.append(prefix);
                    sb.append("  Kernel Wake lock ");
                    sb.append(ent.getKey());
                    linePrefix = printWakeLock(sb, ent.getValue(), batteryRealtime, null, which, 
                            linePrefix);
                    if (!linePrefix.equals(": ")) {
                        sb.append(" realtime");
                    } else {
                        sb.append(": (nothing executed)");
                    }
                    pw.println(sb.toString());
                }
            }
        }
        for (int iu = 0; iu < NU; iu++) {
            Uid u = uidStats.valueAt(iu);
            rxTotal += u.getTcpBytesReceived(which);
            txTotal += u.getTcpBytesSent(which);
            Map<String, ? extends BatteryStats.Uid.Wakelock> wakelocks = u.getWakelockStats();
            if (wakelocks.size() > 0) {
                for (Map.Entry<String, ? extends BatteryStats.Uid.Wakelock> ent 
                        : wakelocks.entrySet()) {
                    Uid.Wakelock wl = ent.getValue();
                    Timer fullWakeTimer = wl.getWakeTime(WAKE_TYPE_FULL);
                    if (fullWakeTimer != null) {
                        fullWakeLockTimeTotalMicros += fullWakeTimer.getTotalTimeLocked(
                                batteryRealtime, which);
                    }
                    Timer partialWakeTimer = wl.getWakeTime(WAKE_TYPE_PARTIAL);
                    if (partialWakeTimer != null) {
                        partialWakeLockTimeTotalMicros += partialWakeTimer.getTotalTimeLocked(
                                batteryRealtime, which);
                    }
                }
            }
        }
        pw.print(prefix);
                pw.print("  Total received: "); pw.print(formatBytesLocked(rxTotal));
                pw.print(", Total sent: "); pw.println(formatBytesLocked(txTotal));
        sb.setLength(0);
        sb.append(prefix);
                sb.append("  Total full wakelock time: "); formatTimeMs(sb,
                        (fullWakeLockTimeTotalMicros + 500) / 1000);
                sb.append(", Total partial waklock time: "); formatTimeMs(sb,
                        (partialWakeLockTimeTotalMicros + 500) / 1000);
        pw.println(sb.toString());
        sb.setLength(0);
        sb.append(prefix);
        sb.append("  Signal levels: ");
        didOne = false;
        for (int i=0; i<NUM_SIGNAL_STRENGTH_BINS; i++) {
            final long time = getPhoneSignalStrengthTime(i, batteryRealtime, which);
            if (time == 0) {
                continue;
            }
            if (didOne) sb.append(", ");
            didOne = true;
            sb.append(SIGNAL_STRENGTH_NAMES[i]);
            sb.append(" ");
            formatTimeMs(sb, time/1000);
            sb.append("(");
            sb.append(formatRatioLocked(time, whichBatteryRealtime));
            sb.append(") ");
            sb.append(getPhoneSignalStrengthCount(i, which));
            sb.append("x");
        }
        if (!didOne) sb.append("No activity");
        pw.println(sb.toString());
        sb.setLength(0);
        sb.append(prefix);
        sb.append("  Signal scanning time: ");
        formatTimeMs(sb, getPhoneSignalScanningTime(batteryRealtime, which) / 1000);
        pw.println(sb.toString());
        sb.setLength(0);
        sb.append(prefix);
        sb.append("  Radio types: ");
        didOne = false;
        for (int i=0; i<NUM_DATA_CONNECTION_TYPES; i++) {
            final long time = getPhoneDataConnectionTime(i, batteryRealtime, which);
            if (time == 0) {
                continue;
            }
            if (didOne) sb.append(", ");
            didOne = true;
            sb.append(DATA_CONNECTION_NAMES[i]);
            sb.append(" ");
            formatTimeMs(sb, time/1000);
            sb.append("(");
            sb.append(formatRatioLocked(time, whichBatteryRealtime));
            sb.append(") ");
            sb.append(getPhoneDataConnectionCount(i, which));
            sb.append("x");
        }
        if (!didOne) sb.append("No activity");
        pw.println(sb.toString());
        sb.setLength(0);
        sb.append(prefix);
        sb.append("  Radio data uptime when unplugged: ");
        sb.append(getRadioDataUptime() / 1000);
        sb.append(" ms");
        pw.println(sb.toString());
        sb.setLength(0);
        sb.append(prefix);
                sb.append("  Wifi on: "); formatTimeMs(sb, wifiOnTime / 1000);
                sb.append("("); sb.append(formatRatioLocked(wifiOnTime, whichBatteryRealtime));
                sb.append("), Wifi running: "); formatTimeMs(sb, wifiRunningTime / 1000);
                sb.append("("); sb.append(formatRatioLocked(wifiRunningTime, whichBatteryRealtime));
                sb.append("), Bluetooth on: "); formatTimeMs(sb, bluetoothOnTime / 1000);
                sb.append("("); sb.append(formatRatioLocked(bluetoothOnTime, whichBatteryRealtime));
                sb.append(")");
        pw.println(sb.toString());
        pw.println(" ");
        if (which == STATS_UNPLUGGED) {
            if (getIsOnBattery()) {
                pw.print(prefix); pw.println("  Device is currently unplugged");
                pw.print(prefix); pw.print("    Discharge cycle start level: "); 
                        pw.println(getDischargeStartLevel());
                pw.print(prefix); pw.print("    Discharge cycle current level: ");
                        pw.println(getDischargeCurrentLevel());
            } else {
                pw.print(prefix); pw.println("  Device is currently plugged into power");
                pw.print(prefix); pw.print("    Last discharge cycle start level: "); 
                        pw.println(getDischargeStartLevel());
                pw.print(prefix); pw.print("    Last discharge cycle end level: "); 
                        pw.println(getDischargeCurrentLevel());
            }
            pw.println(" ");
        }
        for (int iu=0; iu<NU; iu++) {
            final int uid = uidStats.keyAt(iu);
            if (reqUid >= 0 && uid != reqUid) {
                continue;
            }
            Uid u = uidStats.valueAt(iu);
            pw.println(prefix + "  #" + uid + ":");
            boolean uidActivity = false;
            long tcpReceived = u.getTcpBytesReceived(which);
            long tcpSent = u.getTcpBytesSent(which);
            long fullWifiLockOnTime = u.getFullWifiLockTime(batteryRealtime, which);
            long scanWifiLockOnTime = u.getScanWifiLockTime(batteryRealtime, which);
            long wifiTurnedOnTime = u.getWifiTurnedOnTime(batteryRealtime, which);
            if (tcpReceived != 0 || tcpSent != 0) {
                pw.print(prefix); pw.print("    Network: ");
                        pw.print(formatBytesLocked(tcpReceived)); pw.print(" received, ");
                        pw.print(formatBytesLocked(tcpSent)); pw.println(" sent");
            }
            if (u.hasUserActivity()) {
                boolean hasData = false;
                for (int i=0; i<NUM_SCREEN_BRIGHTNESS_BINS; i++) {
                    int val = u.getUserActivityCount(i, which);
                    if (val != 0) {
                        if (!hasData) {
                            sb.setLength(0);
                            sb.append("    User activity: ");
                            hasData = true;
                        } else {
                            sb.append(", ");
                        }
                        sb.append(val);
                        sb.append(" ");
                        sb.append(Uid.USER_ACTIVITY_TYPES[i]);
                    }
                }
                if (hasData) {
                    pw.println(sb.toString());
                }
            }
            if (fullWifiLockOnTime != 0 || scanWifiLockOnTime != 0
                    || wifiTurnedOnTime != 0) {
                sb.setLength(0);
                sb.append(prefix); sb.append("    Turned Wifi On: "); 
                        formatTimeMs(sb, wifiTurnedOnTime / 1000); 
                        sb.append("("); sb.append(formatRatioLocked(wifiTurnedOnTime, 
                                whichBatteryRealtime)); sb.append(")\n");
                sb.append(prefix); sb.append("    Full Wifi Lock: "); 
                        formatTimeMs(sb, fullWifiLockOnTime / 1000); 
                        sb.append("("); sb.append(formatRatioLocked(fullWifiLockOnTime, 
                                whichBatteryRealtime)); sb.append(")\n");
                sb.append(prefix); sb.append("    Scan Wifi Lock: "); 
                        formatTimeMs(sb, scanWifiLockOnTime / 1000);
                        sb.append("("); sb.append(formatRatioLocked(scanWifiLockOnTime, 
                                whichBatteryRealtime)); sb.append(")");
                pw.println(sb.toString());
            }
            Map<String, ? extends BatteryStats.Uid.Wakelock> wakelocks = u.getWakelockStats();
            if (wakelocks.size() > 0) {
                for (Map.Entry<String, ? extends BatteryStats.Uid.Wakelock> ent
                    : wakelocks.entrySet()) {
                    Uid.Wakelock wl = ent.getValue();
                    String linePrefix = ": ";
                    sb.setLength(0);
                    sb.append(prefix);
                    sb.append("    Wake lock ");
                    sb.append(ent.getKey());
                    linePrefix = printWakeLock(sb, wl.getWakeTime(WAKE_TYPE_FULL), batteryRealtime,
                            "full", which, linePrefix);
                    linePrefix = printWakeLock(sb, wl.getWakeTime(WAKE_TYPE_PARTIAL), batteryRealtime,
                            "partial", which, linePrefix);
                    linePrefix = printWakeLock(sb, wl.getWakeTime(WAKE_TYPE_WINDOW), batteryRealtime,
                            "window", which, linePrefix);
                    if (!linePrefix.equals(": ")) {
                        sb.append(" realtime");
                    } else {
                        sb.append(": (nothing executed)");
                    }
                    pw.println(sb.toString());
                    uidActivity = true;
                }
            }
            Map<Integer, ? extends BatteryStats.Uid.Sensor> sensors = u.getSensorStats();
            if (sensors.size() > 0) {
                for (Map.Entry<Integer, ? extends BatteryStats.Uid.Sensor> ent
                    : sensors.entrySet()) {
                    Uid.Sensor se = ent.getValue();
                    int sensorNumber = ent.getKey();
                    sb.setLength(0);
                    sb.append(prefix);
                    sb.append("    Sensor ");
                    int handle = se.getHandle();
                    if (handle == Uid.Sensor.GPS) {
                        sb.append("GPS");
                    } else {
                        sb.append(handle);
                    }
                    sb.append(": ");
                    Timer timer = se.getSensorTime();
                    if (timer != null) {
                        long totalTime = (timer.getTotalTimeLocked(
                                batteryRealtime, which) + 500) / 1000;
                        int count = timer.getCountLocked(which);
                        if (totalTime != 0) {
                            formatTimeMs(sb, totalTime);
                            sb.append("realtime (");
                            sb.append(count);
                            sb.append(" times)");
                        } else {
                            sb.append("(not used)");
                        }
                    } else {
                        sb.append("(not used)");
                    }
                    pw.println(sb.toString());
                    uidActivity = true;
                }
            }
            Map<String, ? extends BatteryStats.Uid.Proc> processStats = u.getProcessStats();
            if (processStats.size() > 0) {
                for (Map.Entry<String, ? extends BatteryStats.Uid.Proc> ent
                    : processStats.entrySet()) {
                    Uid.Proc ps = ent.getValue();
                    long userTime;
                    long systemTime;
                    int starts;
                    userTime = ps.getUserTime(which);
                    systemTime = ps.getSystemTime(which);
                    starts = ps.getStarts(which);
                    if (userTime != 0 || systemTime != 0 || starts != 0) {
                        sb.setLength(0);
                        sb.append(prefix); sb.append("    Proc ");
                                sb.append(ent.getKey()); sb.append(":\n");
                        sb.append(prefix); sb.append("      CPU: ");
                                formatTime(sb, userTime); sb.append("usr + ");
                                formatTime(sb, systemTime); sb.append("krn\n");
                        sb.append(prefix); sb.append("      "); sb.append(starts);
                                sb.append(" proc starts");
                        pw.println(sb.toString());
                        uidActivity = true;
                    }
                }
            }
            Map<String, ? extends BatteryStats.Uid.Pkg> packageStats = u.getPackageStats();
            if (packageStats.size() > 0) {
                for (Map.Entry<String, ? extends BatteryStats.Uid.Pkg> ent
                    : packageStats.entrySet()) {
                    pw.print(prefix); pw.print("    Apk "); pw.print(ent.getKey()); pw.println(":");
                    boolean apkActivity = false;
                    Uid.Pkg ps = ent.getValue();
                    int wakeups = ps.getWakeups(which);
                    if (wakeups != 0) {
                        pw.print(prefix); pw.print("      ");
                                pw.print(wakeups); pw.println(" wakeup alarms");
                        apkActivity = true;
                    }
                    Map<String, ? extends  Uid.Pkg.Serv> serviceStats = ps.getServiceStats();
                    if (serviceStats.size() > 0) {
                        for (Map.Entry<String, ? extends BatteryStats.Uid.Pkg.Serv> sent
                                : serviceStats.entrySet()) {
                            BatteryStats.Uid.Pkg.Serv ss = sent.getValue();
                            long startTime = ss.getStartTime(batteryUptime, which);
                            int starts = ss.getStarts(which);
                            int launches = ss.getLaunches(which);
                            if (startTime != 0 || starts != 0 || launches != 0) {
                                sb.setLength(0);
                                sb.append(prefix); sb.append("      Service ");
                                        sb.append(sent.getKey()); sb.append(":\n");
                                sb.append(prefix); sb.append("        Created for: ");
                                        formatTimeMs(sb, startTime / 1000);
                                        sb.append(" uptime\n");
                                sb.append(prefix); sb.append("        Starts: ");
                                        sb.append(starts);
                                        sb.append(", launches: "); sb.append(launches);
                                pw.println(sb.toString());
                                apkActivity = true;
                            }
                        }
                    }
                    if (!apkActivity) {
                        pw.print(prefix); pw.println("      (nothing executed)");
                    }
                    uidActivity = true;
                }
            }
            if (!uidActivity) {
                pw.print(prefix); pw.println("    (nothing executed)");
            }
        }
    }
    @SuppressWarnings("unused")
    public void dumpLocked(PrintWriter pw) {
        pw.println("Total Statistics (Current and Historic):");
        pw.println("  System starts: " + getStartCount()
                + ", currently on battery: " + getIsOnBattery());
        dumpLocked(pw, "", STATS_TOTAL, -1);
        pw.println("");
        pw.println("Last Run Statistics (Previous run of system):");
        dumpLocked(pw, "", STATS_LAST, -1);
        pw.println("");
        pw.println("Current Battery Statistics (Currently running system):");
        dumpLocked(pw, "", STATS_CURRENT, -1);
        pw.println("");
        pw.println("Unplugged Statistics (Since last unplugged from power):");
        dumpLocked(pw, "", STATS_UNPLUGGED, -1);
    }
    @SuppressWarnings("unused")
    public void dumpCheckinLocked(PrintWriter pw, String[] args) {
        boolean isUnpluggedOnly = false;
        for (String arg : args) {
            if ("-u".equals(arg)) {
                if (LOCAL_LOGV) Log.v("BatteryStats", "Dumping unplugged data");
                isUnpluggedOnly = true;
            }
        }
        if (isUnpluggedOnly) {
            dumpCheckinLocked(pw, STATS_UNPLUGGED, -1);
        }
        else {
            dumpCheckinLocked(pw, STATS_TOTAL, -1);
            dumpCheckinLocked(pw, STATS_LAST, -1);
            dumpCheckinLocked(pw, STATS_UNPLUGGED, -1);
            dumpCheckinLocked(pw, STATS_CURRENT, -1);
        }
    }
}
