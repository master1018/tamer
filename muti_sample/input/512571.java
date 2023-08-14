public final class BatteryStatsImpl extends BatteryStats {
    private static final String TAG = "BatteryStatsImpl";
    private static final boolean DEBUG = false;
    private static final int MAGIC = 0xBA757475; 
    private static final int VERSION = 43;
    private static final int MAX_WAKELOCKS_PER_UID = 20;
    private static final String BATCHED_WAKELOCK_NAME = "*overflow*";
    private static int sNumSpeedSteps;
    private final JournaledFile mFile;
    final SparseArray<BatteryStatsImpl.Uid> mUidStats =
        new SparseArray<BatteryStatsImpl.Uid>();
    final ArrayList<StopwatchTimer> mPartialTimers = new ArrayList<StopwatchTimer>();
    final ArrayList<StopwatchTimer> mFullTimers = new ArrayList<StopwatchTimer>();
    final ArrayList<StopwatchTimer> mWindowTimers = new ArrayList<StopwatchTimer>();
    final SparseArray<ArrayList<StopwatchTimer>> mSensorTimers
            = new SparseArray<ArrayList<StopwatchTimer>>();
    final ArrayList<Unpluggable> mUnpluggables = new ArrayList<Unpluggable>();
    int mStartCount;
    long mBatteryUptime;
    long mBatteryLastUptime;
    long mBatteryRealtime;
    long mBatteryLastRealtime;
    long mUptime;
    long mUptimeStart;
    long mLastUptime;
    long mRealtime;
    long mRealtimeStart;
    long mLastRealtime;
    boolean mScreenOn;
    StopwatchTimer mScreenOnTimer;
    int mScreenBrightnessBin = -1;
    final StopwatchTimer[] mScreenBrightnessTimer = new StopwatchTimer[NUM_SCREEN_BRIGHTNESS_BINS];
    Counter mInputEventCounter;
    boolean mPhoneOn;
    StopwatchTimer mPhoneOnTimer;
    boolean mAudioOn;
    StopwatchTimer mAudioOnTimer;
    boolean mVideoOn;
    StopwatchTimer mVideoOnTimer;
    int mPhoneSignalStrengthBin = -1;
    final StopwatchTimer[] mPhoneSignalStrengthsTimer = 
            new StopwatchTimer[NUM_SIGNAL_STRENGTH_BINS];
    StopwatchTimer mPhoneSignalScanningTimer;
    int mPhoneDataConnectionType = -1;
    final StopwatchTimer[] mPhoneDataConnectionsTimer = 
            new StopwatchTimer[NUM_DATA_CONNECTION_TYPES];
    boolean mWifiOn;
    StopwatchTimer mWifiOnTimer;
    int mWifiOnUid = -1;
    boolean mWifiRunning;
    StopwatchTimer mWifiRunningTimer;
    boolean mBluetoothOn;
    StopwatchTimer mBluetoothOnTimer;
    BluetoothHeadset mBtHeadset;
    boolean mOnBattery;
    boolean mOnBatteryInternal;
    long mTrackBatteryPastUptime;
    long mTrackBatteryUptimeStart;
    long mTrackBatteryPastRealtime;
    long mTrackBatteryRealtimeStart;
    long mUnpluggedBatteryUptime;
    long mUnpluggedBatteryRealtime;
    int mDischargeStartLevel;
    int mDischargeCurrentLevel;
    long mLastWriteTime = 0; 
    private long[] mMobileDataTx = new long[4];
    private long[] mMobileDataRx = new long[4];
    private long[] mTotalDataTx = new long[4];
    private long[] mTotalDataRx = new long[4];
    private long mRadioDataUptime;
    private long mRadioDataStart;
    private int mBluetoothPingCount;
    private int mBluetoothPingStart = -1;
    private int mPhoneServiceState = -1;
    private final HashMap<String, SamplingTimer> mKernelWakelockStats = 
            new HashMap<String, SamplingTimer>();
    public Map<String, ? extends SamplingTimer> getKernelWakelockStats() {
        return mKernelWakelockStats;
    }
    private static int sKernelWakelockUpdateVersion = 0;
    private static final int[] PROC_WAKELOCKS_FORMAT = new int[] {
        Process.PROC_TAB_TERM|Process.PROC_OUT_STRING,                
        Process.PROC_TAB_TERM|Process.PROC_OUT_LONG,                  
        Process.PROC_TAB_TERM,
        Process.PROC_TAB_TERM,
        Process.PROC_TAB_TERM,
        Process.PROC_TAB_TERM|Process.PROC_OUT_LONG,                  
    };
    private final String[] mProcWakelocksName = new String[3];
    private final long[] mProcWakelocksData = new long[3];
    private final Map<String, KernelWakelockStats> mProcWakelockFileStats = 
            new HashMap<String, KernelWakelockStats>();
    private HashMap<String, Integer> mUidCache = new HashMap<String, Integer>();
    public BatteryStatsImpl() {
        mFile = null;
    }
    public static interface Unpluggable {
        void unplug(long batteryUptime, long batteryRealtime);
        void plug(long batteryUptime, long batteryRealtime);
    }
    public static class Counter extends BatteryStats.Counter implements Unpluggable {
        final AtomicInteger mCount = new AtomicInteger();
        int mLoadedCount;
        int mLastCount;
        int mUnpluggedCount;
        int mPluggedCount;
        Counter(ArrayList<Unpluggable> unpluggables, Parcel in) {
            mPluggedCount = in.readInt();
            mCount.set(mPluggedCount);
            mLoadedCount = in.readInt();
            mLastCount = in.readInt();
            mUnpluggedCount = in.readInt();
            unpluggables.add(this);
        }
        Counter(ArrayList<Unpluggable> unpluggables) {
            unpluggables.add(this);
        }
        public void writeToParcel(Parcel out) {
            out.writeInt(mCount.get());
            out.writeInt(mLoadedCount);
            out.writeInt(mLastCount);
            out.writeInt(mUnpluggedCount);
        }
        public void unplug(long batteryUptime, long batteryRealtime) {
            mUnpluggedCount = mPluggedCount;
            mCount.set(mPluggedCount);
        }
        public void plug(long batteryUptime, long batteryRealtime) {
            mPluggedCount = mCount.get();
        }
        public static void writeCounterToParcel(Parcel out, Counter counter) {
            if (counter == null) {
                out.writeInt(0); 
                return;
            }
            out.writeInt(1); 
            counter.writeToParcel(out);
        }
        @Override
        public int getCountLocked(int which) {
            int val;
            if (which == STATS_LAST) {
                val = mLastCount;
            } else {
                val = mCount.get();
                if (which == STATS_UNPLUGGED) {
                    val -= mUnpluggedCount;
                } else if (which != STATS_TOTAL) {
                    val -= mLoadedCount;
                }
            }
            return val;
        }
        public void logState(Printer pw, String prefix) {
            pw.println(prefix + "mCount=" + mCount.get()
                    + " mLoadedCount=" + mLoadedCount + " mLastCount=" + mLastCount
                    + " mUnpluggedCount=" + mUnpluggedCount
                    + " mPluggedCount=" + mPluggedCount);
        }
        void stepAtomic() {
            mCount.incrementAndGet();
        }
        void writeSummaryFromParcelLocked(Parcel out) {
            int count = mCount.get();
            out.writeInt(count);
            out.writeInt(count - mLoadedCount);
        }
        void readSummaryFromParcelLocked(Parcel in) {
            mLoadedCount = in.readInt();
            mCount.set(mLoadedCount);
            mLastCount = in.readInt();
            mUnpluggedCount = mPluggedCount = mLoadedCount;
        }
    }
    public static class SamplingCounter extends Counter {
        SamplingCounter(ArrayList<Unpluggable> unpluggables, Parcel in) {
            super(unpluggables, in);
        }
        SamplingCounter(ArrayList<Unpluggable> unpluggables) {
            super(unpluggables);
        }
        public void addCountAtomic(long count) {
            mCount.addAndGet((int)count);
        }
    }
    public static abstract class Timer extends BatteryStats.Timer implements Unpluggable {
        final int mType;
        int mCount;
        int mLoadedCount;
        int mLastCount;
        int mUnpluggedCount;
        long mTotalTime;
        long mLoadedTime;
        long mLastTime;
        long mUnpluggedTime;
        Timer(int type, ArrayList<Unpluggable> unpluggables, Parcel in) {
            mType = type;
            mCount = in.readInt();
            mLoadedCount = in.readInt();
            mLastCount = in.readInt();
            mUnpluggedCount = in.readInt();
            mTotalTime = in.readLong();
            mLoadedTime = in.readLong();
            mLastTime = in.readLong();
            mUnpluggedTime = in.readLong();
            unpluggables.add(this);
        }
        Timer(int type, ArrayList<Unpluggable> unpluggables) {
            mType = type;
            unpluggables.add(this);
        }
        protected abstract long computeRunTimeLocked(long curBatteryRealtime);
        protected abstract int computeCurrentCountLocked();
        public void writeToParcel(Parcel out, long batteryRealtime) {
            out.writeInt(mCount);
            out.writeInt(mLoadedCount);
            out.writeInt(mLastCount);
            out.writeInt(mUnpluggedCount);
            out.writeLong(computeRunTimeLocked(batteryRealtime));
            out.writeLong(mLoadedTime);
            out.writeLong(mLastTime);
            out.writeLong(mUnpluggedTime);
        }
        public void unplug(long batteryUptime, long batteryRealtime) {
            if (DEBUG && mType < 0) {
                Log.v(TAG, "unplug #" + mType + ": realtime=" + batteryRealtime
                        + " old mUnpluggedTime=" + mUnpluggedTime
                        + " old mUnpluggedCount=" + mUnpluggedCount);
            }
            mUnpluggedTime = computeRunTimeLocked(batteryRealtime);
            mUnpluggedCount = mCount;
            if (DEBUG && mType < 0) {
                Log.v(TAG, "unplug #" + mType
                        + ": new mUnpluggedTime=" + mUnpluggedTime
                        + " new mUnpluggedCount=" + mUnpluggedCount);
            }
        }
        public void plug(long batteryUptime, long batteryRealtime) {
            if (DEBUG && mType < 0) {
                Log.v(TAG, "plug #" + mType + ": realtime=" + batteryRealtime
                        + " old mTotalTime=" + mTotalTime);
            }
            mTotalTime = computeRunTimeLocked(batteryRealtime);
            mCount = computeCurrentCountLocked();
            if (DEBUG && mType < 0) {
                Log.v(TAG, "plug #" + mType
                        + ": new mTotalTime=" + mTotalTime);
            }
        }
        public static void writeTimerToParcel(Parcel out, Timer timer,
                long batteryRealtime) {
            if (timer == null) {
                out.writeInt(0); 
                return;
            }
            out.writeInt(1); 
            timer.writeToParcel(out, batteryRealtime);
        }
        @Override
        public long getTotalTimeLocked(long batteryRealtime, int which) {
            long val;
            if (which == STATS_LAST) {
                val = mLastTime;
            } else {
                val = computeRunTimeLocked(batteryRealtime);
                if (which == STATS_UNPLUGGED) {
                    val -= mUnpluggedTime;
                } else if (which != STATS_TOTAL) {
                    val -= mLoadedTime;
                }
            }
            return val;
        }
        @Override
        public int getCountLocked(int which) {
            int val;
            if (which == STATS_LAST) {
                val = mLastCount;
            } else {
                val = computeCurrentCountLocked();
                if (which == STATS_UNPLUGGED) {
                    val -= mUnpluggedCount;
                } else if (which != STATS_TOTAL) {
                    val -= mLoadedCount;
                }
            }
            return val;
        }
        public void logState(Printer pw, String prefix) {
            pw.println(prefix + " mCount=" + mCount
                    + " mLoadedCount=" + mLoadedCount + " mLastCount=" + mLastCount
                    + " mUnpluggedCount=" + mUnpluggedCount);
            pw.println(prefix + "mTotalTime=" + mTotalTime
                    + " mLoadedTime=" + mLoadedTime);
            pw.println(prefix + "mLastTime=" + mLastTime
                    + " mUnpluggedTime=" + mUnpluggedTime);
        }
        void writeSummaryFromParcelLocked(Parcel out, long batteryRealtime) {
            long runTime = computeRunTimeLocked(batteryRealtime);
            out.writeLong((runTime + 500) / 1000);
            out.writeLong(((runTime - mLoadedTime) + 500) / 1000);
            out.writeInt(mCount);
            out.writeInt(mCount - mLoadedCount);
        }
        void readSummaryFromParcelLocked(Parcel in) {
            mTotalTime = mLoadedTime = in.readLong() * 1000;
            mLastTime = in.readLong() * 1000;
            mUnpluggedTime = mTotalTime;
            mCount = mLoadedCount = in.readInt();
            mLastCount = in.readInt();
            mUnpluggedCount = mCount;
        }
    }
    public static final class SamplingTimer extends Timer {
        int mCurrentReportedCount;
        int mUnpluggedReportedCount;
        long mCurrentReportedTotalTime;
        long mUnpluggedReportedTotalTime;
        boolean mInDischarge;
        boolean mTrackingReportedValues;
        int mUpdateVersion;
        SamplingTimer(ArrayList<Unpluggable> unpluggables, boolean inDischarge, Parcel in) {
            super(0, unpluggables, in);
            mCurrentReportedCount = in.readInt();
            mUnpluggedReportedCount = in.readInt();
            mCurrentReportedTotalTime = in.readLong();
            mUnpluggedReportedTotalTime = in.readLong();
            mTrackingReportedValues = in.readInt() == 1;
            mInDischarge = inDischarge;
        }
        SamplingTimer(ArrayList<Unpluggable> unpluggables, boolean inDischarge, 
                boolean trackReportedValues) {
            super(0, unpluggables);
            mTrackingReportedValues = trackReportedValues;
            mInDischarge = inDischarge;
        }
        public void setStale() {
            mTrackingReportedValues = false;
            mUnpluggedReportedTotalTime = 0;
            mUnpluggedReportedCount = 0;
        }
        public void setUpdateVersion(int version) {
            mUpdateVersion = version;
        }
        public int getUpdateVersion() {
            return mUpdateVersion;
        }
        public void updateCurrentReportedCount(int count) {
            if (mInDischarge && mUnpluggedReportedCount == 0) {
                mUnpluggedReportedCount = count;
                mTrackingReportedValues = true;
            }
            mCurrentReportedCount = count;
        }
        public void updateCurrentReportedTotalTime(long totalTime) {
            if (mInDischarge && mUnpluggedReportedTotalTime == 0) {
                mUnpluggedReportedTotalTime = totalTime;
                mTrackingReportedValues = true;
            }
            mCurrentReportedTotalTime = totalTime;
        }
        public void unplug(long batteryUptime, long batteryRealtime) {
            super.unplug(batteryUptime, batteryRealtime);
            if (mTrackingReportedValues) {
                mUnpluggedReportedTotalTime = mCurrentReportedTotalTime;
                mUnpluggedReportedCount = mCurrentReportedCount;
            }
            mInDischarge = true;
        }
        public void plug(long batteryUptime, long batteryRealtime) {
            super.plug(batteryUptime, batteryRealtime);
            mInDischarge = false;
        }
        public void logState(Printer pw, String prefix) {
            super.logState(pw, prefix);
            pw.println(prefix + "mCurrentReportedCount=" + mCurrentReportedCount 
                    + " mUnpluggedReportedCount=" + mUnpluggedReportedCount
                    + " mCurrentReportedTotalTime=" + mCurrentReportedTotalTime
                    + " mUnpluggedReportedTotalTime=" + mUnpluggedReportedTotalTime);
        }
        protected long computeRunTimeLocked(long curBatteryRealtime) {
            return mTotalTime + (mInDischarge && mTrackingReportedValues 
                    ? mCurrentReportedTotalTime - mUnpluggedReportedTotalTime : 0);
        }
        protected int computeCurrentCountLocked() {
            return mCount + (mInDischarge && mTrackingReportedValues
                    ? mCurrentReportedCount - mUnpluggedReportedCount : 0);
        }
        public void writeToParcel(Parcel out, long batteryRealtime) {
            super.writeToParcel(out, batteryRealtime);
            out.writeInt(mCurrentReportedCount);
            out.writeInt(mUnpluggedReportedCount);
            out.writeLong(mCurrentReportedTotalTime);
            out.writeLong(mUnpluggedReportedTotalTime);
            out.writeInt(mTrackingReportedValues ? 1 : 0);
        }
        void writeSummaryFromParcelLocked(Parcel out, long batteryRealtime) {
            super.writeSummaryFromParcelLocked(out, batteryRealtime);
            out.writeLong(mCurrentReportedTotalTime);
            out.writeInt(mCurrentReportedCount);
            out.writeInt(mTrackingReportedValues ? 1 : 0);
        }
        void readSummaryFromParcelLocked(Parcel in) {
            super.readSummaryFromParcelLocked(in);
            mUnpluggedReportedTotalTime = mCurrentReportedTotalTime = in.readLong();
            mUnpluggedReportedCount = mCurrentReportedCount = in.readInt();
            mTrackingReportedValues = in.readInt() == 1;
        }
    }
    public static final class StopwatchTimer extends Timer {
        final ArrayList<StopwatchTimer> mTimerPool;
        int mNesting;
        long mUpdateTime;
        long mAcquireTime;
        long mTimeout;
        StopwatchTimer(int type, ArrayList<StopwatchTimer> timerPool,
                ArrayList<Unpluggable> unpluggables, Parcel in) {
            super(type, unpluggables, in);
            mTimerPool = timerPool;
            mUpdateTime = in.readLong();
        }
        StopwatchTimer(int type, ArrayList<StopwatchTimer> timerPool,
                ArrayList<Unpluggable> unpluggables) {
            super(type, unpluggables);
            mTimerPool = timerPool;
        }
        void setTimeout(long timeout) {
            mTimeout = timeout;
        }
        public void writeToParcel(Parcel out, long batteryRealtime) {
            super.writeToParcel(out, batteryRealtime);
            out.writeLong(mUpdateTime);
        }
        public void plug(long batteryUptime, long batteryRealtime) {
            if (mNesting > 0) {
                if (DEBUG && mType < 0) {
                    Log.v(TAG, "old mUpdateTime=" + mUpdateTime);
                }
                super.plug(batteryUptime, batteryRealtime);
                mUpdateTime = batteryRealtime;
                if (DEBUG && mType < 0) {
                    Log.v(TAG, "new mUpdateTime=" + mUpdateTime);
                }
            }
        }
        public void logState(Printer pw, String prefix) {
            super.logState(pw, prefix);
            pw.println(prefix + "mNesting=" + mNesting + "mUpdateTime=" + mUpdateTime
                    + " mAcquireTime=" + mAcquireTime);
        }
        void startRunningLocked(BatteryStatsImpl stats) {
            if (mNesting++ == 0) {
                mUpdateTime = stats.getBatteryRealtimeLocked(
                        SystemClock.elapsedRealtime() * 1000);
                if (mTimerPool != null) {
                    refreshTimersLocked(stats, mTimerPool);
                    mTimerPool.add(this);
                }
                mCount++;
                mAcquireTime = mTotalTime;
                if (DEBUG && mType < 0) {
                    Log.v(TAG, "start #" + mType + ": mUpdateTime=" + mUpdateTime
                            + " mTotalTime=" + mTotalTime + " mCount=" + mCount
                            + " mAcquireTime=" + mAcquireTime);
                }
            }
        }
        boolean isRunningLocked() {
            return mNesting > 0;
        }
        void stopRunningLocked(BatteryStatsImpl stats) {
            if (mNesting == 0) {
                return;
            }
            if (--mNesting == 0) {
                if (mTimerPool != null) {
                    refreshTimersLocked(stats, mTimerPool);
                    mTimerPool.remove(this);
                } else {
                    final long realtime = SystemClock.elapsedRealtime() * 1000; 
                    final long batteryRealtime = stats.getBatteryRealtimeLocked(realtime);
                    mNesting = 1;
                    mTotalTime = computeRunTimeLocked(batteryRealtime);
                    mNesting = 0;
                }
                if (DEBUG && mType < 0) {
                    Log.v(TAG, "stop #" + mType + ": mUpdateTime=" + mUpdateTime
                            + " mTotalTime=" + mTotalTime + " mCount=" + mCount
                            + " mAcquireTime=" + mAcquireTime);
                }
                if (mTotalTime == mAcquireTime) {
                    mCount--;
                }
            }
        }
        private static void refreshTimersLocked(final BatteryStatsImpl stats,
                final ArrayList<StopwatchTimer> pool) {
            final long realtime = SystemClock.elapsedRealtime() * 1000; 
            final long batteryRealtime = stats.getBatteryRealtimeLocked(realtime);
            final int N = pool.size();
            for (int i=N-1; i>= 0; i--) {
                final StopwatchTimer t = pool.get(i);
                long heldTime = batteryRealtime - t.mUpdateTime;
                if (heldTime > 0) {
                    t.mTotalTime += heldTime / N;
                }
                t.mUpdateTime = batteryRealtime;
            }
        }
        @Override
        protected long computeRunTimeLocked(long curBatteryRealtime) {
            if (mTimeout > 0 && curBatteryRealtime > mUpdateTime + mTimeout) {
                curBatteryRealtime = mUpdateTime + mTimeout;
            }
            return mTotalTime + (mNesting > 0
                    ? (curBatteryRealtime - mUpdateTime)
                            / (mTimerPool != null ? mTimerPool.size() : 1)
                    : 0);
        }
        @Override
        protected int computeCurrentCountLocked() {
            return mCount;
        }
        void readSummaryFromParcelLocked(Parcel in) {
            super.readSummaryFromParcelLocked(in);
            mNesting = 0;
        }
    }
    private final Map<String, KernelWakelockStats> readKernelWakelockStats() {
        byte[] buffer = new byte[4096];
        int len;
        try {
            FileInputStream is = new FileInputStream("/proc/wakelocks");
            len = is.read(buffer);
            is.close();
            if (len > 0) {
                int i;
                for (i=0; i<len; i++) {
                    if (buffer[i] == '\0') {
                        len = i;
                        break;
                    }
                }
            }
        } catch (java.io.FileNotFoundException e) {
            return null;
        } catch (java.io.IOException e) {
            return null;
        }
        return parseProcWakelocks(buffer, len);
    }
    private final Map<String, KernelWakelockStats> parseProcWakelocks(
            byte[] wlBuffer, int len) {
        String name;
        int count;
        long totalTime;
        int startIndex, endIndex;
        int numUpdatedWlNames = 0;
        int i;
        for (i = 0; i < len && wlBuffer[i] != '\n' && wlBuffer[i] != '\0'; i++);
        startIndex = endIndex = i + 1;
        synchronized(this) {
            Map<String, KernelWakelockStats> m = mProcWakelockFileStats;
            sKernelWakelockUpdateVersion++;
            while (endIndex < len) {
                for (endIndex=startIndex; 
                        endIndex < len && wlBuffer[endIndex] != '\n' && wlBuffer[endIndex] != '\0'; 
                        endIndex++);
                if (endIndex < len) {
                    endIndex++; 
                }
                String[] nameStringArray = mProcWakelocksName;
                long[] wlData = mProcWakelocksData;
                for (int j = startIndex; j < endIndex; j++) {
                    if ((wlBuffer[j] & 0x80) != 0) wlBuffer[j] = (byte) '?';
                }
                boolean parsed = Process.parseProcLine(wlBuffer, startIndex, endIndex,
                        PROC_WAKELOCKS_FORMAT, nameStringArray, wlData, null);
                name = nameStringArray[0];
                count = (int) wlData[1];
                totalTime = (wlData[2] + 500) / 1000;
                if (parsed && name.length() > 0) {
                    if (!m.containsKey(name)) {
                        m.put(name, new KernelWakelockStats(count, totalTime, 
                                sKernelWakelockUpdateVersion));
                        numUpdatedWlNames++;
                    } else {
                        KernelWakelockStats kwlStats = m.get(name);
                        if (kwlStats.mVersion == sKernelWakelockUpdateVersion) {
                            kwlStats.mCount += count;
                            kwlStats.mTotalTime += totalTime;
                        } else {
                            kwlStats.mCount = count;
                            kwlStats.mTotalTime = totalTime;
                            kwlStats.mVersion = sKernelWakelockUpdateVersion;
                            numUpdatedWlNames++;
                        }
                    }
                }
                startIndex = endIndex;
            }
            if (m.size() != numUpdatedWlNames) {
                Iterator<KernelWakelockStats> itr = m.values().iterator();
                while (itr.hasNext()) {
                    if (itr.next().mVersion != sKernelWakelockUpdateVersion) {
                        itr.remove();
                    }
                }
            }
            return m;
        }
    }
    private class KernelWakelockStats {
        public int mCount;
        public long mTotalTime;
        public int mVersion;
        KernelWakelockStats(int count, long totalTime, int version) {
            mCount = count;
            mTotalTime = totalTime;
            mVersion = version;
        }
    }
    public SamplingTimer getKernelWakelockTimerLocked(String name) {
        SamplingTimer kwlt = mKernelWakelockStats.get(name);
        if (kwlt == null) {
            kwlt = new SamplingTimer(mUnpluggables, mOnBatteryInternal, 
                    true );
            mKernelWakelockStats.put(name, kwlt);
        }
        return kwlt;
    }
    private void doDataPlug(long[] dataTransfer, long currentBytes) {
        dataTransfer[STATS_LAST] = dataTransfer[STATS_UNPLUGGED];
        dataTransfer[STATS_UNPLUGGED] = -1;
    }
    private void doDataUnplug(long[] dataTransfer, long currentBytes) {
        dataTransfer[STATS_UNPLUGGED] = currentBytes;
    }
    private long getCurrentRadioDataUptime() {
        try {
            File awakeTimeFile = new File("/sys/devices/virtual/net/rmnet0/awake_time_ms");
            if (!awakeTimeFile.exists()) return 0;
            BufferedReader br = new BufferedReader(new FileReader(awakeTimeFile));
            String line = br.readLine();
            br.close();
            return Long.parseLong(line) * 1000;
        } catch (NumberFormatException nfe) {
        } catch (IOException ioe) {
        }
        return 0;
    }
    public long getRadioDataUptimeMs() {
        return getRadioDataUptime() / 1000;
    }
    public long getRadioDataUptime() {
        if (mRadioDataStart == -1) {
            return mRadioDataUptime;
        } else {
            return getCurrentRadioDataUptime() - mRadioDataStart;
        }
    }
    private int getCurrentBluetoothPingCount() {
        if (mBtHeadset != null) {
            return mBtHeadset.getBatteryUsageHint();
        }
        return -1;
    }
    public int getBluetoothPingCount() {
        if (mBluetoothPingStart == -1) {
            return mBluetoothPingCount;
        } else if (mBtHeadset != null) {
            return getCurrentBluetoothPingCount() - mBluetoothPingStart;
        }
        return 0;
    }
    public void setBtHeadset(BluetoothHeadset headset) {
        if (headset != null && mBtHeadset == null && isOnBattery() && mBluetoothPingStart == -1) {
            mBluetoothPingStart = getCurrentBluetoothPingCount();
        }
        mBtHeadset = headset;
    }
    public void doUnplug(long batteryUptime, long batteryRealtime) {
        for (int iu = mUidStats.size() - 1; iu >= 0; iu--) {
            Uid u = mUidStats.valueAt(iu);
            u.mStartedTcpBytesReceived = TrafficStats.getUidRxBytes(u.mUid);
            u.mStartedTcpBytesSent = TrafficStats.getUidTxBytes(u.mUid);
            u.mTcpBytesReceivedAtLastUnplug = u.mCurrentTcpBytesReceived;
            u.mTcpBytesSentAtLastUnplug = u.mCurrentTcpBytesSent;
        }
        for (int i = mUnpluggables.size() - 1; i >= 0; i--) {
            mUnpluggables.get(i).unplug(batteryUptime, batteryRealtime);
        }
        doDataUnplug(mMobileDataRx, TrafficStats.getMobileRxBytes());
        doDataUnplug(mMobileDataTx, TrafficStats.getMobileTxBytes());
        doDataUnplug(mTotalDataRx, TrafficStats.getTotalRxBytes());
        doDataUnplug(mTotalDataTx, TrafficStats.getTotalTxBytes());
        mRadioDataStart = getCurrentRadioDataUptime();
        mRadioDataUptime = 0;
        mBluetoothPingStart = getCurrentBluetoothPingCount();
        mBluetoothPingCount = 0;
    }
    public void doPlug(long batteryUptime, long batteryRealtime) {
        for (int iu = mUidStats.size() - 1; iu >= 0; iu--) {
            Uid u = mUidStats.valueAt(iu);
            if (u.mStartedTcpBytesReceived >= 0) {
                u.mCurrentTcpBytesReceived = u.computeCurrentTcpBytesReceived();
                u.mStartedTcpBytesReceived = -1;
            }
            if (u.mStartedTcpBytesSent >= 0) {
                u.mCurrentTcpBytesSent = u.computeCurrentTcpBytesSent();
                u.mStartedTcpBytesSent = -1;
            }
        }
        for (int i = mUnpluggables.size() - 1; i >= 0; i--) {
            mUnpluggables.get(i).plug(batteryUptime, batteryRealtime);
        }
        doDataPlug(mMobileDataRx, TrafficStats.getMobileRxBytes());
        doDataPlug(mMobileDataTx, TrafficStats.getMobileTxBytes());
        doDataPlug(mTotalDataRx, TrafficStats.getTotalRxBytes());
        doDataPlug(mTotalDataTx, TrafficStats.getTotalTxBytes());
        mRadioDataUptime = getRadioDataUptime();
        mRadioDataStart = -1;
        mBluetoothPingCount = getBluetoothPingCount();
        mBluetoothPingStart = -1;
    }
    public void noteStartGps(int uid) {
        getUidStatsLocked(uid).noteStartGps();
    }
    public void noteStopGps(int uid) {
        getUidStatsLocked(uid).noteStopGps();
    }
    public void noteScreenOnLocked() {
        if (!mScreenOn) {
            mScreenOn = true;
            mScreenOnTimer.startRunningLocked(this);
            if (mScreenBrightnessBin >= 0) {
                mScreenBrightnessTimer[mScreenBrightnessBin].startRunningLocked(this);
            }
        }
    }
    public void noteScreenOffLocked() {
        if (mScreenOn) {
            mScreenOn = false;
            mScreenOnTimer.stopRunningLocked(this);
            if (mScreenBrightnessBin >= 0) {
                mScreenBrightnessTimer[mScreenBrightnessBin].stopRunningLocked(this);
            }
        }
    }
    public void noteScreenBrightnessLocked(int brightness) {
        int bin = brightness / (256/NUM_SCREEN_BRIGHTNESS_BINS);
        if (bin < 0) bin = 0;
        else if (bin >= NUM_SCREEN_BRIGHTNESS_BINS) bin = NUM_SCREEN_BRIGHTNESS_BINS-1;
        if (mScreenBrightnessBin != bin) {
            if (mScreenOn) {
                if (mScreenBrightnessBin >= 0) {
                    mScreenBrightnessTimer[mScreenBrightnessBin].stopRunningLocked(this);
                }
                mScreenBrightnessTimer[bin].startRunningLocked(this);
            }
            mScreenBrightnessBin = bin;
        }
    }
    public void noteInputEventAtomic() {
        mInputEventCounter.stepAtomic();
    }
    public void noteUserActivityLocked(int uid, int event) {
        getUidStatsLocked(uid).noteUserActivityLocked(event);
    }
    public void notePhoneOnLocked() {
        if (!mPhoneOn) {
            mPhoneOn = true;
            mPhoneOnTimer.startRunningLocked(this);
        }
    }
    public void notePhoneOffLocked() {
        if (mPhoneOn) {
            mPhoneOn = false;
            mPhoneOnTimer.stopRunningLocked(this);
        }
    }
    public void notePhoneStateLocked(int state) {
        int bin = mPhoneSignalStrengthBin;
        boolean isAirplaneMode = state == ServiceState.STATE_POWER_OFF;
        if (isAirplaneMode || state == ServiceState.STATE_OUT_OF_SERVICE) {
            for (int i = 0; i < NUM_SIGNAL_STRENGTH_BINS; i++) {
                while (mPhoneSignalStrengthsTimer[i].isRunningLocked()) {
                    mPhoneSignalStrengthsTimer[i].stopRunningLocked(this);
                }
            }
        }
        while (mPhoneSignalScanningTimer.isRunningLocked()) {
            mPhoneSignalScanningTimer.stopRunningLocked(this);
        }
        if (state == ServiceState.STATE_IN_SERVICE) {
            if (bin == -1) bin = SIGNAL_STRENGTH_NONE_OR_UNKNOWN;
            if (!mPhoneSignalStrengthsTimer[bin].isRunningLocked()) {
                mPhoneSignalStrengthsTimer[bin].startRunningLocked(this);
            }
        } else if (state == ServiceState.STATE_OUT_OF_SERVICE) {
            mPhoneSignalStrengthBin = SIGNAL_STRENGTH_NONE_OR_UNKNOWN;
            if (!mPhoneSignalStrengthsTimer[mPhoneSignalStrengthBin].isRunningLocked()) {
                mPhoneSignalStrengthsTimer[mPhoneSignalStrengthBin].startRunningLocked(this);
            }
            if (!mPhoneSignalScanningTimer.isRunningLocked()) {
                mPhoneSignalScanningTimer.startRunningLocked(this);
            }
        }
        mPhoneServiceState = state;
    }
    public void notePhoneSignalStrengthLocked(SignalStrength signalStrength) {
        int bin;
        if (mPhoneServiceState == ServiceState.STATE_POWER_OFF
                || mPhoneServiceState == ServiceState.STATE_OUT_OF_SERVICE) {
            return;
        }
        if (!signalStrength.isGsm()) {
            int dBm = signalStrength.getCdmaDbm();
            if (dBm >= -75) bin = SIGNAL_STRENGTH_GREAT;
            else if (dBm >= -85) bin = SIGNAL_STRENGTH_GOOD;
            else if (dBm >= -95)  bin = SIGNAL_STRENGTH_MODERATE;
            else if (dBm >= -100)  bin = SIGNAL_STRENGTH_POOR;
            else bin = SIGNAL_STRENGTH_NONE_OR_UNKNOWN;
        } else {
            int asu = signalStrength.getGsmSignalStrength();
            if (asu < 0 || asu >= 99) bin = SIGNAL_STRENGTH_NONE_OR_UNKNOWN;
            else if (asu >= 16) bin = SIGNAL_STRENGTH_GREAT;
            else if (asu >= 8)  bin = SIGNAL_STRENGTH_GOOD;
            else if (asu >= 4)  bin = SIGNAL_STRENGTH_MODERATE;
            else bin = SIGNAL_STRENGTH_POOR;
        }
        if (mPhoneSignalStrengthBin != bin) {
            if (mPhoneSignalStrengthBin >= 0) {
                mPhoneSignalStrengthsTimer[mPhoneSignalStrengthBin].stopRunningLocked(this);
            }
            mPhoneSignalStrengthBin = bin;
            mPhoneSignalStrengthsTimer[bin].startRunningLocked(this);
        }
    }
    public void notePhoneDataConnectionStateLocked(int dataType, boolean hasData) {
        int bin = DATA_CONNECTION_NONE;
        if (hasData) {
            switch (dataType) {
                case TelephonyManager.NETWORK_TYPE_EDGE:
                    bin = DATA_CONNECTION_EDGE;
                    break;
                case TelephonyManager.NETWORK_TYPE_GPRS:
                    bin = DATA_CONNECTION_GPRS;
                    break;
                case TelephonyManager.NETWORK_TYPE_UMTS:
                    bin = DATA_CONNECTION_UMTS;
                    break;
                default:
                    bin = DATA_CONNECTION_OTHER;
                    break;
            }
        }
        if (DEBUG) Log.i(TAG, "Phone Data Connection -> " + dataType + " = " + hasData);
        if (mPhoneDataConnectionType != bin) {
            if (mPhoneDataConnectionType >= 0) {
                mPhoneDataConnectionsTimer[mPhoneDataConnectionType].stopRunningLocked(this);
            }
            mPhoneDataConnectionType = bin;
            mPhoneDataConnectionsTimer[bin].startRunningLocked(this);
        }
    }
    public void noteWifiOnLocked(int uid) {
        if (!mWifiOn) {
            mWifiOn = true;
            mWifiOnTimer.startRunningLocked(this);
        }
        if (mWifiOnUid != uid) {
            if (mWifiOnUid >= 0) {
                getUidStatsLocked(mWifiOnUid).noteWifiTurnedOffLocked();
            }
            mWifiOnUid = uid;
            getUidStatsLocked(uid).noteWifiTurnedOnLocked();
        }
    }
    public void noteWifiOffLocked(int uid) {
        if (mWifiOn) {
            mWifiOn = false;
            mWifiOnTimer.stopRunningLocked(this);
        }
        if (mWifiOnUid >= 0) {
            getUidStatsLocked(mWifiOnUid).noteWifiTurnedOffLocked();
            mWifiOnUid = -1;
        }
    }
    public void noteAudioOnLocked(int uid) {
        if (!mAudioOn) {
            mAudioOn = true;
            mAudioOnTimer.startRunningLocked(this);
        }
        getUidStatsLocked(uid).noteAudioTurnedOnLocked();
    }
    public void noteAudioOffLocked(int uid) {
        if (mAudioOn) {
            mAudioOn = false;
            mAudioOnTimer.stopRunningLocked(this);
        }
        getUidStatsLocked(uid).noteAudioTurnedOffLocked();
    }
    public void noteVideoOnLocked(int uid) {
        if (!mVideoOn) {
            mVideoOn = true;
            mVideoOnTimer.startRunningLocked(this);
        }
        getUidStatsLocked(uid).noteVideoTurnedOnLocked();
    }
    public void noteVideoOffLocked(int uid) {
        if (mVideoOn) {
            mVideoOn = false;
            mVideoOnTimer.stopRunningLocked(this);
        }
        getUidStatsLocked(uid).noteVideoTurnedOffLocked();
    }
    public void noteWifiRunningLocked() {
        if (!mWifiRunning) {
            mWifiRunning = true;
            mWifiRunningTimer.startRunningLocked(this);
        }
    }
    public void noteWifiStoppedLocked() {
        if (mWifiRunning) {
            mWifiRunning = false;
            mWifiRunningTimer.stopRunningLocked(this);
        }
    }
    public void noteBluetoothOnLocked() {
        if (!mBluetoothOn) {
            mBluetoothOn = true;
            mBluetoothOnTimer.startRunningLocked(this);
        }
    }
    public void noteBluetoothOffLocked() {
        if (mBluetoothOn) {
            mBluetoothOn = false;
            mBluetoothOnTimer.stopRunningLocked(this);
        }
    }
    public void noteFullWifiLockAcquiredLocked(int uid) {
        getUidStatsLocked(uid).noteFullWifiLockAcquiredLocked();
    }
    public void noteFullWifiLockReleasedLocked(int uid) {
        getUidStatsLocked(uid).noteFullWifiLockReleasedLocked();
    }
    public void noteScanWifiLockAcquiredLocked(int uid) {
        getUidStatsLocked(uid).noteScanWifiLockAcquiredLocked();
    }
    public void noteScanWifiLockReleasedLocked(int uid) {
        getUidStatsLocked(uid).noteScanWifiLockReleasedLocked();
    }
    public void noteWifiMulticastEnabledLocked(int uid) {
        getUidStatsLocked(uid).noteWifiMulticastEnabledLocked();
    }
    public void noteWifiMulticastDisabledLocked(int uid) {
        getUidStatsLocked(uid).noteWifiMulticastDisabledLocked();
    }
    @Override public long getScreenOnTime(long batteryRealtime, int which) {
        return mScreenOnTimer.getTotalTimeLocked(batteryRealtime, which);
    }
    @Override public long getScreenBrightnessTime(int brightnessBin,
            long batteryRealtime, int which) {
        return mScreenBrightnessTimer[brightnessBin].getTotalTimeLocked(
                batteryRealtime, which);
    }
    @Override public int getInputEventCount(int which) {
        return mInputEventCounter.getCountLocked(which);
    }
    @Override public long getPhoneOnTime(long batteryRealtime, int which) {
        return mPhoneOnTimer.getTotalTimeLocked(batteryRealtime, which);
    }
    @Override public long getPhoneSignalStrengthTime(int strengthBin,
            long batteryRealtime, int which) {
        return mPhoneSignalStrengthsTimer[strengthBin].getTotalTimeLocked(
                batteryRealtime, which);
    }
    @Override public long getPhoneSignalScanningTime(
            long batteryRealtime, int which) {
        return mPhoneSignalScanningTimer.getTotalTimeLocked(
                batteryRealtime, which);
    }
    @Override public int getPhoneSignalStrengthCount(int dataType, int which) {
        return mPhoneDataConnectionsTimer[dataType].getCountLocked(which);
    }
    @Override public long getPhoneDataConnectionTime(int dataType,
            long batteryRealtime, int which) {
        return mPhoneDataConnectionsTimer[dataType].getTotalTimeLocked(
                batteryRealtime, which);
    }
    @Override public int getPhoneDataConnectionCount(int dataType, int which) {
        return mPhoneDataConnectionsTimer[dataType].getCountLocked(which);
    }
    @Override public long getWifiOnTime(long batteryRealtime, int which) {
        return mWifiOnTimer.getTotalTimeLocked(batteryRealtime, which);
    }
    @Override public long getWifiRunningTime(long batteryRealtime, int which) {
        return mWifiRunningTimer.getTotalTimeLocked(batteryRealtime, which);
    }
    @Override public long getBluetoothOnTime(long batteryRealtime, int which) {
        return mBluetoothOnTimer.getTotalTimeLocked(batteryRealtime, which);
    }
    @Override public boolean getIsOnBattery() {
        return mOnBattery;
    }
    @Override public SparseArray<? extends BatteryStats.Uid> getUidStats() {
        return mUidStats;
    }
    public final class Uid extends BatteryStats.Uid {
        final int mUid;
        long mLoadedTcpBytesReceived;
        long mLoadedTcpBytesSent;
        long mCurrentTcpBytesReceived;
        long mCurrentTcpBytesSent;
        long mTcpBytesReceivedAtLastUnplug;
        long mTcpBytesSentAtLastUnplug;
        long mStartedTcpBytesReceived = -1;
        long mStartedTcpBytesSent = -1;
        boolean mWifiTurnedOn;
        StopwatchTimer mWifiTurnedOnTimer;
        boolean mFullWifiLockOut;
        StopwatchTimer mFullWifiLockTimer;
        boolean mScanWifiLockOut;
        StopwatchTimer mScanWifiLockTimer;
        boolean mWifiMulticastEnabled;
        StopwatchTimer mWifiMulticastTimer;
        boolean mAudioTurnedOn;
        StopwatchTimer mAudioTurnedOnTimer;
        boolean mVideoTurnedOn;
        StopwatchTimer mVideoTurnedOnTimer;
        Counter[] mUserActivityCounters;
        final HashMap<String, Wakelock> mWakelockStats = new HashMap<String, Wakelock>();
        final HashMap<Integer, Sensor> mSensorStats = new HashMap<Integer, Sensor>();
        final HashMap<String, Proc> mProcessStats = new HashMap<String, Proc>();
        final HashMap<String, Pkg> mPackageStats = new HashMap<String, Pkg>();
        public Uid(int uid) {
            mUid = uid;
            mWifiTurnedOnTimer = new StopwatchTimer(WIFI_TURNED_ON, null, mUnpluggables);
            mFullWifiLockTimer = new StopwatchTimer(FULL_WIFI_LOCK, null, mUnpluggables);
            mScanWifiLockTimer = new StopwatchTimer(SCAN_WIFI_LOCK, null, mUnpluggables);
            mWifiMulticastTimer = new StopwatchTimer(WIFI_MULTICAST_ENABLED,
                    null, mUnpluggables);
            mAudioTurnedOnTimer = new StopwatchTimer(AUDIO_TURNED_ON, null, mUnpluggables);
            mVideoTurnedOnTimer = new StopwatchTimer(VIDEO_TURNED_ON, null, mUnpluggables);
        }
        @Override
        public Map<String, ? extends BatteryStats.Uid.Wakelock> getWakelockStats() {
            return mWakelockStats;
        }
        @Override
        public Map<Integer, ? extends BatteryStats.Uid.Sensor> getSensorStats() {
            return mSensorStats;
        }
        @Override
        public Map<String, ? extends BatteryStats.Uid.Proc> getProcessStats() {
            return mProcessStats;
        }
        @Override
        public Map<String, ? extends BatteryStats.Uid.Pkg> getPackageStats() {
            return mPackageStats;
        }
        @Override
        public int getUid() {
            return mUid;
        }
        @Override
        public long getTcpBytesReceived(int which) {
            if (which == STATS_LAST) {
                return mLoadedTcpBytesReceived;
            } else {
                long current = computeCurrentTcpBytesReceived();
                if (which == STATS_UNPLUGGED) {
                    current -= mTcpBytesReceivedAtLastUnplug;
                } else if (which == STATS_TOTAL) {
                    current += mLoadedTcpBytesReceived;
                }
                return current;
            }
        }
        public long computeCurrentTcpBytesReceived() {
            return mCurrentTcpBytesReceived + (mStartedTcpBytesReceived >= 0
                    ? (TrafficStats.getUidRxBytes(mUid) - mStartedTcpBytesReceived) : 0);
        }
        @Override
        public long getTcpBytesSent(int which) {
            if (which == STATS_LAST) {
                return mLoadedTcpBytesSent;
            } else {
                long current = computeCurrentTcpBytesSent();
                if (which == STATS_UNPLUGGED) {
                    current -= mTcpBytesSentAtLastUnplug;
                } else if (which == STATS_TOTAL) {
                    current += mLoadedTcpBytesSent;
                }
                return current;
            }
        }
        @Override
        public void noteWifiTurnedOnLocked() {
            if (!mWifiTurnedOn) {
                mWifiTurnedOn = true;
                mWifiTurnedOnTimer.startRunningLocked(BatteryStatsImpl.this);
            }
        }
        @Override
        public void noteWifiTurnedOffLocked() {
            if (mWifiTurnedOn) {
                mWifiTurnedOn = false;
                mWifiTurnedOnTimer.stopRunningLocked(BatteryStatsImpl.this);
            }
        }
        @Override
        public void noteFullWifiLockAcquiredLocked() {
            if (!mFullWifiLockOut) {
                mFullWifiLockOut = true;
                mFullWifiLockTimer.startRunningLocked(BatteryStatsImpl.this);
            }
        }
        @Override
        public void noteVideoTurnedOnLocked() {
            if (!mVideoTurnedOn) {
                mVideoTurnedOn = true;
                mVideoTurnedOnTimer.startRunningLocked(BatteryStatsImpl.this);
            }
        }
        @Override
        public void noteVideoTurnedOffLocked() {
            if (mVideoTurnedOn) {
                mVideoTurnedOn = false;
                mVideoTurnedOnTimer.stopRunningLocked(BatteryStatsImpl.this);
            }
        }
        @Override
        public void noteAudioTurnedOnLocked() {
            if (!mAudioTurnedOn) {
                mAudioTurnedOn = true;
                mAudioTurnedOnTimer.startRunningLocked(BatteryStatsImpl.this);
            }
        }
        @Override
        public void noteAudioTurnedOffLocked() {
            if (mAudioTurnedOn) {
                mAudioTurnedOn = false;
                mAudioTurnedOnTimer.stopRunningLocked(BatteryStatsImpl.this);
            }
        }
        @Override
        public void noteFullWifiLockReleasedLocked() {
            if (mFullWifiLockOut) {
                mFullWifiLockOut = false;
                mFullWifiLockTimer.stopRunningLocked(BatteryStatsImpl.this);
            }
        }
        @Override
        public void noteScanWifiLockAcquiredLocked() {
            if (!mScanWifiLockOut) {
                mScanWifiLockOut = true;
                mScanWifiLockTimer.startRunningLocked(BatteryStatsImpl.this);
            }
        }
        @Override
        public void noteScanWifiLockReleasedLocked() {
            if (mScanWifiLockOut) {
                mScanWifiLockOut = false;
                mScanWifiLockTimer.stopRunningLocked(BatteryStatsImpl.this);
            }
        }
        @Override
        public void noteWifiMulticastEnabledLocked() {
            if (!mWifiMulticastEnabled) {
                mWifiMulticastEnabled = true;
                mWifiMulticastTimer.startRunningLocked(BatteryStatsImpl.this);
            }
        }
        @Override
        public void noteWifiMulticastDisabledLocked() {
            if (mWifiMulticastEnabled) {
                mWifiMulticastEnabled = false;
                mWifiMulticastTimer.stopRunningLocked(BatteryStatsImpl.this);
            }
        }
        @Override 
        public long getWifiTurnedOnTime(long batteryRealtime, int which) {
            return mWifiTurnedOnTimer.getTotalTimeLocked(batteryRealtime, which);
        }
        @Override 
        public long getAudioTurnedOnTime(long batteryRealtime, int which) {
            return mAudioTurnedOnTimer.getTotalTimeLocked(batteryRealtime, which);
        }
        @Override 
        public long getVideoTurnedOnTime(long batteryRealtime, int which) {
            return mVideoTurnedOnTimer.getTotalTimeLocked(batteryRealtime, which);
        }
        @Override 
        public long getFullWifiLockTime(long batteryRealtime, int which) {
            return mFullWifiLockTimer.getTotalTimeLocked(batteryRealtime, which);
        }
        @Override 
        public long getScanWifiLockTime(long batteryRealtime, int which) {
            return mScanWifiLockTimer.getTotalTimeLocked(batteryRealtime, which);
        }
        @Override
        public long getWifiMulticastTime(long batteryRealtime, int which) {
            return mWifiMulticastTimer.getTotalTimeLocked(batteryRealtime,
                                                          which);
        }
        @Override
        public void noteUserActivityLocked(int type) {
            if (mUserActivityCounters == null) {
                initUserActivityLocked();
            }
            if (type < 0) type = 0;
            else if (type >= NUM_USER_ACTIVITY_TYPES) type = NUM_USER_ACTIVITY_TYPES-1;
            mUserActivityCounters[type].stepAtomic();
        }
        @Override
        public boolean hasUserActivity() {
            return mUserActivityCounters != null;
        }
        @Override
        public int getUserActivityCount(int type, int which) {
            if (mUserActivityCounters == null) {
                return 0;
            }
            return mUserActivityCounters[type].getCountLocked(which);
        }
        void initUserActivityLocked() {
            mUserActivityCounters = new Counter[NUM_USER_ACTIVITY_TYPES];
            for (int i=0; i<NUM_USER_ACTIVITY_TYPES; i++) {
                mUserActivityCounters[i] = new Counter(mUnpluggables);
            }
        }
        public long computeCurrentTcpBytesSent() {
            return mCurrentTcpBytesSent + (mStartedTcpBytesSent >= 0
                    ? (TrafficStats.getUidTxBytes(mUid) - mStartedTcpBytesSent) : 0);
        }
        void writeToParcelLocked(Parcel out, long batteryRealtime) {
            out.writeInt(mWakelockStats.size());
            for (Map.Entry<String, Uid.Wakelock> wakelockEntry : mWakelockStats.entrySet()) {
                out.writeString(wakelockEntry.getKey());
                Uid.Wakelock wakelock = wakelockEntry.getValue();
                wakelock.writeToParcelLocked(out, batteryRealtime);
            }
            out.writeInt(mSensorStats.size());
            for (Map.Entry<Integer, Uid.Sensor> sensorEntry : mSensorStats.entrySet()) {
                out.writeInt(sensorEntry.getKey());
                Uid.Sensor sensor = sensorEntry.getValue();
                sensor.writeToParcelLocked(out, batteryRealtime);
            }
            out.writeInt(mProcessStats.size());
            for (Map.Entry<String, Uid.Proc> procEntry : mProcessStats.entrySet()) {
                out.writeString(procEntry.getKey());
                Uid.Proc proc = procEntry.getValue();
                proc.writeToParcelLocked(out);
            }
            out.writeInt(mPackageStats.size());
            for (Map.Entry<String, Uid.Pkg> pkgEntry : mPackageStats.entrySet()) {
                out.writeString(pkgEntry.getKey());
                Uid.Pkg pkg = pkgEntry.getValue();
                pkg.writeToParcelLocked(out);
            }
            out.writeLong(mLoadedTcpBytesReceived);
            out.writeLong(mLoadedTcpBytesSent);
            out.writeLong(computeCurrentTcpBytesReceived());
            out.writeLong(computeCurrentTcpBytesSent());
            out.writeLong(mTcpBytesReceivedAtLastUnplug);
            out.writeLong(mTcpBytesSentAtLastUnplug);
            mWifiTurnedOnTimer.writeToParcel(out, batteryRealtime);
            mFullWifiLockTimer.writeToParcel(out, batteryRealtime);
            mAudioTurnedOnTimer.writeToParcel(out, batteryRealtime);
            mVideoTurnedOnTimer.writeToParcel(out, batteryRealtime);
            mScanWifiLockTimer.writeToParcel(out, batteryRealtime);
            mWifiMulticastTimer.writeToParcel(out, batteryRealtime);
            if (mUserActivityCounters == null) {
                out.writeInt(0);
            } else {
                out.writeInt(1);
                for (int i=0; i<NUM_USER_ACTIVITY_TYPES; i++) {
                    mUserActivityCounters[i].writeToParcel(out);
                }
            }
        }
        void readFromParcelLocked(ArrayList<Unpluggable> unpluggables, Parcel in) {
            int numWakelocks = in.readInt();
            mWakelockStats.clear();
            for (int j = 0; j < numWakelocks; j++) {
                String wakelockName = in.readString();
                Uid.Wakelock wakelock = new Wakelock();
                wakelock.readFromParcelLocked(unpluggables, in);
                if (mWakelockStats.size() < MAX_WAKELOCKS_PER_UID) {
                    mWakelockStats.put(wakelockName, wakelock);
                }
            }
            int numSensors = in.readInt();
            mSensorStats.clear();
            for (int k = 0; k < numSensors; k++) {
                int sensorNumber = in.readInt();
                Uid.Sensor sensor = new Sensor(sensorNumber);
                sensor.readFromParcelLocked(mUnpluggables, in);
                mSensorStats.put(sensorNumber, sensor);
            }
            int numProcs = in.readInt();
            mProcessStats.clear();
            for (int k = 0; k < numProcs; k++) {
                String processName = in.readString();
                Uid.Proc proc = new Proc();
                proc.readFromParcelLocked(in);
                mProcessStats.put(processName, proc);
            }
            int numPkgs = in.readInt();
            mPackageStats.clear();
            for (int l = 0; l < numPkgs; l++) {
                String packageName = in.readString();
                Uid.Pkg pkg = new Pkg();
                pkg.readFromParcelLocked(in);
                mPackageStats.put(packageName, pkg);
            }
            mLoadedTcpBytesReceived = in.readLong();
            mLoadedTcpBytesSent = in.readLong();
            mCurrentTcpBytesReceived = in.readLong();
            mCurrentTcpBytesSent = in.readLong();
            mTcpBytesReceivedAtLastUnplug = in.readLong();
            mTcpBytesSentAtLastUnplug = in.readLong();
            mWifiTurnedOn = false;
            mWifiTurnedOnTimer = new StopwatchTimer(WIFI_TURNED_ON, null, mUnpluggables, in);
            mFullWifiLockOut = false;
            mFullWifiLockTimer = new StopwatchTimer(FULL_WIFI_LOCK, null, mUnpluggables, in);
            mAudioTurnedOn = false;
            mAudioTurnedOnTimer = new StopwatchTimer(AUDIO_TURNED_ON, null, mUnpluggables, in);
            mVideoTurnedOn = false;
            mVideoTurnedOnTimer = new StopwatchTimer(VIDEO_TURNED_ON, null, mUnpluggables, in);
            mScanWifiLockOut = false;
            mScanWifiLockTimer = new StopwatchTimer(SCAN_WIFI_LOCK, null, mUnpluggables, in);
            mWifiMulticastEnabled = false;
            mWifiMulticastTimer = new StopwatchTimer(WIFI_MULTICAST_ENABLED,
                    null, mUnpluggables, in);
            if (in.readInt() == 0) {
                mUserActivityCounters = null;
            } else {
                mUserActivityCounters = new Counter[NUM_USER_ACTIVITY_TYPES];
                for (int i=0; i<NUM_USER_ACTIVITY_TYPES; i++) {
                    mUserActivityCounters[i] = new Counter(mUnpluggables, in);
                }
            }
        }
        public final class Wakelock extends BatteryStats.Uid.Wakelock {
            StopwatchTimer mTimerPartial;
            StopwatchTimer mTimerFull;
            StopwatchTimer mTimerWindow;
            private StopwatchTimer readTimerFromParcel(int type, ArrayList<StopwatchTimer> pool,
                    ArrayList<Unpluggable> unpluggables, Parcel in) {
                if (in.readInt() == 0) {
                    return null;
                }
                return new StopwatchTimer(type, pool, unpluggables, in);
            }
            void readFromParcelLocked(ArrayList<Unpluggable> unpluggables, Parcel in) {
                mTimerPartial = readTimerFromParcel(WAKE_TYPE_PARTIAL,
                        mPartialTimers, unpluggables, in);
                mTimerFull = readTimerFromParcel(WAKE_TYPE_FULL,
                        mFullTimers, unpluggables, in);
                mTimerWindow = readTimerFromParcel(WAKE_TYPE_WINDOW,
                        mWindowTimers, unpluggables, in);
            }
            void writeToParcelLocked(Parcel out, long batteryRealtime) {
                Timer.writeTimerToParcel(out, mTimerPartial, batteryRealtime);
                Timer.writeTimerToParcel(out, mTimerFull, batteryRealtime);
                Timer.writeTimerToParcel(out, mTimerWindow, batteryRealtime);
            }
            @Override
            public Timer getWakeTime(int type) {
                switch (type) {
                case WAKE_TYPE_FULL: return mTimerFull;
                case WAKE_TYPE_PARTIAL: return mTimerPartial;
                case WAKE_TYPE_WINDOW: return mTimerWindow;
                default: throw new IllegalArgumentException("type = " + type);
                }
            }
        }
        public final class Sensor extends BatteryStats.Uid.Sensor {
            final int mHandle;
            StopwatchTimer mTimer;
            public Sensor(int handle) {
                mHandle = handle;
            }
            private StopwatchTimer readTimerFromParcel(ArrayList<Unpluggable> unpluggables,
                    Parcel in) {
                if (in.readInt() == 0) {
                    return null;
                }
                ArrayList<StopwatchTimer> pool = mSensorTimers.get(mHandle);
                if (pool == null) {
                    pool = new ArrayList<StopwatchTimer>();
                    mSensorTimers.put(mHandle, pool);
                }
                return new StopwatchTimer(0, pool, unpluggables, in);
            }
            void readFromParcelLocked(ArrayList<Unpluggable> unpluggables, Parcel in) {
                mTimer = readTimerFromParcel(unpluggables, in);
            }
            void writeToParcelLocked(Parcel out, long batteryRealtime) {
                Timer.writeTimerToParcel(out, mTimer, batteryRealtime);
            }
            @Override
            public Timer getSensorTime() {
                return mTimer;
            }
            @Override
            public int getHandle() {
                return mHandle;
            }
        }
        public final class Proc extends BatteryStats.Uid.Proc implements Unpluggable {
            long mUserTime;
            long mSystemTime;
            int mStarts;
            long mForegroundTime;
            long mLoadedUserTime;
            long mLoadedSystemTime;
            int mLoadedStarts;
            long mLoadedForegroundTime;
            long mLastUserTime;
            long mLastSystemTime;
            int mLastStarts;
            long mLastForegroundTime;
            long mUnpluggedUserTime;
            long mUnpluggedSystemTime;
            int mUnpluggedStarts;
            long mUnpluggedForegroundTime;
            SamplingCounter[] mSpeedBins;
            Proc() {
                mUnpluggables.add(this);
                mSpeedBins = new SamplingCounter[getCpuSpeedSteps()];
                for (int i = 0; i < mSpeedBins.length; i++) {
                    mSpeedBins[i] = new SamplingCounter(mUnpluggables);
                }
            }
            public void unplug(long batteryUptime, long batteryRealtime) {
                mUnpluggedUserTime = mUserTime;
                mUnpluggedSystemTime = mSystemTime;
                mUnpluggedStarts = mStarts;
                mUnpluggedForegroundTime = mForegroundTime;
            }
            public void plug(long batteryUptime, long batteryRealtime) {
            }
            void writeToParcelLocked(Parcel out) {
                final long uSecRealtime = SystemClock.elapsedRealtime() * 1000;
                final long batteryRealtime = getBatteryRealtimeLocked(uSecRealtime);
                out.writeLong(mUserTime);
                out.writeLong(mSystemTime);
                out.writeLong(mForegroundTime);
                out.writeInt(mStarts);
                out.writeLong(mLoadedUserTime);
                out.writeLong(mLoadedSystemTime);
                out.writeLong(mLoadedForegroundTime);
                out.writeInt(mLoadedStarts);
                out.writeLong(mLastUserTime);
                out.writeLong(mLastSystemTime);
                out.writeLong(mLastForegroundTime);
                out.writeInt(mLastStarts);
                out.writeLong(mUnpluggedUserTime);
                out.writeLong(mUnpluggedSystemTime);
                out.writeLong(mUnpluggedForegroundTime);
                out.writeInt(mUnpluggedStarts);
                out.writeInt(mSpeedBins.length);
                for (int i = 0; i < mSpeedBins.length; i++) {
                    mSpeedBins[i].writeToParcel(out);
                }
            }
            void readFromParcelLocked(Parcel in) {
                mUserTime = in.readLong();
                mSystemTime = in.readLong();
                mForegroundTime = in.readLong();
                mStarts = in.readInt();
                mLoadedUserTime = in.readLong();
                mLoadedSystemTime = in.readLong();
                mLoadedForegroundTime = in.readLong();
                mLoadedStarts = in.readInt();
                mLastUserTime = in.readLong();
                mLastSystemTime = in.readLong();
                mLastForegroundTime = in.readLong();
                mLastStarts = in.readInt();
                mUnpluggedUserTime = in.readLong();
                mUnpluggedSystemTime = in.readLong();
                mUnpluggedForegroundTime = in.readLong();
                mUnpluggedStarts = in.readInt();
                int bins = in.readInt();
                mSpeedBins = new SamplingCounter[bins];
                for (int i = 0; i < bins; i++) {
                    mSpeedBins[i] = new SamplingCounter(mUnpluggables, in);
                }
            }
            public BatteryStatsImpl getBatteryStats() {
                return BatteryStatsImpl.this;
            }
            public void addCpuTimeLocked(int utime, int stime) {
                mUserTime += utime;
                mSystemTime += stime;
            }
            public void addForegroundTimeLocked(long ttime) {
                mForegroundTime += ttime;
            }
            public void incStartsLocked() {
                mStarts++;
            }
            @Override
            public long getUserTime(int which) {
                long val;
                if (which == STATS_LAST) {
                    val = mLastUserTime;
                } else {
                    val = mUserTime;
                    if (which == STATS_CURRENT) {
                        val -= mLoadedUserTime;
                    } else if (which == STATS_UNPLUGGED) {
                        val -= mUnpluggedUserTime;
                    }
                }
                return val;
            }
            @Override
            public long getSystemTime(int which) {
                long val;
                if (which == STATS_LAST) {
                    val = mLastSystemTime;
                } else {
                    val = mSystemTime;
                    if (which == STATS_CURRENT) {
                        val -= mLoadedSystemTime;
                    } else if (which == STATS_UNPLUGGED) {
                        val -= mUnpluggedSystemTime;
                    }
                }
                return val;
            }
            @Override
            public long getForegroundTime(int which) {
                long val;
                if (which == STATS_LAST) {
                    val = mLastForegroundTime;
                } else {
                    val = mForegroundTime;
                    if (which == STATS_CURRENT) {
                        val -= mLoadedForegroundTime;
                    } else if (which == STATS_UNPLUGGED) {
                        val -= mUnpluggedForegroundTime;
                    }
                }
                return val;
            }
            @Override
            public int getStarts(int which) {
                int val;
                if (which == STATS_LAST) {
                    val = mLastStarts;
                } else {
                    val = mStarts;
                    if (which == STATS_CURRENT) {
                        val -= mLoadedStarts;
                    } else if (which == STATS_UNPLUGGED) {
                        val -= mUnpluggedStarts;
                    }
                }
                return val;
            }
            public void addSpeedStepTimes(long[] values) {
                for (int i = 0; i < mSpeedBins.length && i < values.length; i++) {
                    mSpeedBins[i].addCountAtomic(values[i]);
                }
            }
            @Override
            public long getTimeAtCpuSpeedStep(int speedStep, int which) {
                if (speedStep < mSpeedBins.length) {
                    return mSpeedBins[speedStep].getCountLocked(which);
                } else {
                    return 0;
                }
            }
        }
        public final class Pkg extends BatteryStats.Uid.Pkg implements Unpluggable {
            int mWakeups;
            int mLoadedWakeups;
            int mLastWakeups;
            int mUnpluggedWakeups;
            final HashMap<String, Serv> mServiceStats = new HashMap<String, Serv>();
            Pkg() {
                mUnpluggables.add(this);
            }
            public void unplug(long batteryUptime, long batteryRealtime) {
                mUnpluggedWakeups = mWakeups;
            }
            public void plug(long batteryUptime, long batteryRealtime) {
            }
            void readFromParcelLocked(Parcel in) {
                mWakeups = in.readInt();
                mLoadedWakeups = in.readInt();
                mLastWakeups = in.readInt();
                mUnpluggedWakeups = in.readInt();
                int numServs = in.readInt();
                mServiceStats.clear();
                for (int m = 0; m < numServs; m++) {
                    String serviceName = in.readString();
                    Uid.Pkg.Serv serv = new Serv();
                    mServiceStats.put(serviceName, serv);
                    serv.readFromParcelLocked(in);
                }
            }
            void writeToParcelLocked(Parcel out) {
                out.writeInt(mWakeups);
                out.writeInt(mLoadedWakeups);
                out.writeInt(mLastWakeups);
                out.writeInt(mUnpluggedWakeups);
                out.writeInt(mServiceStats.size());
                for (Map.Entry<String, Uid.Pkg.Serv> servEntry : mServiceStats.entrySet()) {
                    out.writeString(servEntry.getKey());
                    Uid.Pkg.Serv serv = servEntry.getValue();
                    serv.writeToParcelLocked(out);
                }
            }
            @Override
            public Map<String, ? extends BatteryStats.Uid.Pkg.Serv> getServiceStats() {
                return mServiceStats;
            }
            @Override
            public int getWakeups(int which) {
                int val;
                if (which == STATS_LAST) {
                    val = mLastWakeups;
                } else {
                    val = mWakeups;
                    if (which == STATS_CURRENT) {
                        val -= mLoadedWakeups;
                    } else if (which == STATS_UNPLUGGED) {
                        val -= mUnpluggedWakeups;
                    }
                }
                return val;
            }
            public final class Serv extends BatteryStats.Uid.Pkg.Serv implements Unpluggable {
                long mStartTime;
                long mRunningSince;
                boolean mRunning;
                int mStarts;
                long mLaunchedTime;
                long mLaunchedSince;
                boolean mLaunched;
                int mLaunches;
                long mLoadedStartTime;
                int mLoadedStarts;
                int mLoadedLaunches;
                long mLastStartTime;
                int mLastStarts;
                int mLastLaunches;
                long mUnpluggedStartTime;
                int mUnpluggedStarts;
                int mUnpluggedLaunches;
                Serv() {
                    mUnpluggables.add(this);
                }
                public void unplug(long batteryUptime, long batteryRealtime) {
                    mUnpluggedStartTime = getStartTimeToNowLocked(batteryUptime);
                    mUnpluggedStarts = mStarts;
                    mUnpluggedLaunches = mLaunches;
                }
                public void plug(long batteryUptime, long batteryRealtime) {
                }
                void readFromParcelLocked(Parcel in) {
                    mStartTime = in.readLong();
                    mRunningSince = in.readLong();
                    mRunning = in.readInt() != 0;
                    mStarts = in.readInt();
                    mLaunchedTime = in.readLong();
                    mLaunchedSince = in.readLong();
                    mLaunched = in.readInt() != 0;
                    mLaunches = in.readInt();
                    mLoadedStartTime = in.readLong();
                    mLoadedStarts = in.readInt();
                    mLoadedLaunches = in.readInt();
                    mLastStartTime = in.readLong();
                    mLastStarts = in.readInt();
                    mLastLaunches = in.readInt();
                    mUnpluggedStartTime = in.readLong();
                    mUnpluggedStarts = in.readInt();
                    mUnpluggedLaunches = in.readInt();
                }
                void writeToParcelLocked(Parcel out) {
                    out.writeLong(mStartTime);
                    out.writeLong(mRunningSince);
                    out.writeInt(mRunning ? 1 : 0);
                    out.writeInt(mStarts);
                    out.writeLong(mLaunchedTime);
                    out.writeLong(mLaunchedSince);
                    out.writeInt(mLaunched ? 1 : 0);
                    out.writeInt(mLaunches);
                    out.writeLong(mLoadedStartTime);
                    out.writeInt(mLoadedStarts);
                    out.writeInt(mLoadedLaunches);
                    out.writeLong(mLastStartTime);
                    out.writeInt(mLastStarts);
                    out.writeInt(mLastLaunches);
                    out.writeLong(mUnpluggedStartTime);
                    out.writeInt(mUnpluggedStarts);
                    out.writeInt(mUnpluggedLaunches);
                }
                long getLaunchTimeToNowLocked(long batteryUptime) {
                    if (!mLaunched) return mLaunchedTime;
                    return mLaunchedTime + batteryUptime - mLaunchedSince;
                }
                long getStartTimeToNowLocked(long batteryUptime) {
                    if (!mRunning) return mStartTime;
                    return mStartTime + batteryUptime - mRunningSince;
                }
                public void startLaunchedLocked() {
                    if (!mLaunched) {
                        mLaunches++;
                        mLaunchedSince = getBatteryUptimeLocked();
                        mLaunched = true;
                    }
                }
                public void stopLaunchedLocked() {
                    if (mLaunched) {
                        long time = getBatteryUptimeLocked() - mLaunchedSince;
                        if (time > 0) {
                            mLaunchedTime += time;
                        } else {
                            mLaunches--;
                        }
                        mLaunched = false;
                    }
                }
                public void startRunningLocked() {
                    if (!mRunning) {
                        mStarts++;
                        mRunningSince = getBatteryUptimeLocked();
                        mRunning = true;
                    }
                }
                public void stopRunningLocked() {
                    if (mRunning) {
                        long time = getBatteryUptimeLocked() - mRunningSince;
                        if (time > 0) {
                            mStartTime += time;
                        } else {
                            mStarts--;
                        }
                        mRunning = false;
                    }
                }
                public BatteryStatsImpl getBatteryStats() {
                    return BatteryStatsImpl.this;
                }
                @Override
                public int getLaunches(int which) {
                    int val;
                    if (which == STATS_LAST) {
                        val = mLastLaunches;
                    } else {
                        val = mLaunches;
                        if (which == STATS_CURRENT) {
                            val -= mLoadedLaunches;
                        } else if (which == STATS_UNPLUGGED) {
                            val -= mUnpluggedLaunches;
                        }
                    }
                    return val;
                }
                @Override
                public long getStartTime(long now, int which) {
                    long val;
                    if (which == STATS_LAST) {
                        val = mLastStartTime;
                    } else {
                        val = getStartTimeToNowLocked(now);
                        if (which == STATS_CURRENT) {
                            val -= mLoadedStartTime;
                        } else if (which == STATS_UNPLUGGED) {
                            val -= mUnpluggedStartTime;
                        }
                    }
                    return val;
                }
                @Override
                public int getStarts(int which) {
                    int val;
                    if (which == STATS_LAST) {
                        val = mLastStarts;
                    } else {
                        val = mStarts;
                        if (which == STATS_CURRENT) {
                            val -= mLoadedStarts;
                        } else if (which == STATS_UNPLUGGED) {
                            val -= mUnpluggedStarts;
                        }
                    }
                    return val;
                }
            }
            public BatteryStatsImpl getBatteryStats() {
                return BatteryStatsImpl.this;
            }
            public void incWakeupsLocked() {
                mWakeups++;
            }
            final Serv newServiceStatsLocked() {
                return new Serv();
            }
        }
        public Proc getProcessStatsLocked(String name) {
            Proc ps = mProcessStats.get(name);
            if (ps == null) {
                ps = new Proc();
                mProcessStats.put(name, ps);
            }
            return ps;
        }
        public Pkg getPackageStatsLocked(String name) {
            Pkg ps = mPackageStats.get(name);
            if (ps == null) {
                ps = new Pkg();
                mPackageStats.put(name, ps);
            }
            return ps;
        }
        public Pkg.Serv getServiceStatsLocked(String pkg, String serv) {
            Pkg ps = getPackageStatsLocked(pkg);
            Pkg.Serv ss = ps.mServiceStats.get(serv);
            if (ss == null) {
                ss = ps.newServiceStatsLocked();
                ps.mServiceStats.put(serv, ss);
            }
            return ss;
        }
        public StopwatchTimer getWakeTimerLocked(String name, int type) {
            Wakelock wl = mWakelockStats.get(name);
            if (wl == null) {
                if (mWakelockStats.size() > MAX_WAKELOCKS_PER_UID) {
                    name = BATCHED_WAKELOCK_NAME;
                    wl = mWakelockStats.get(name);
                }
                if (wl == null) {
                    wl = new Wakelock();
                    mWakelockStats.put(name, wl);
                }
            }
            StopwatchTimer t = null;
            switch (type) {
                case WAKE_TYPE_PARTIAL:
                    t = wl.mTimerPartial;
                    if (t == null) {
                        t = new StopwatchTimer(WAKE_TYPE_PARTIAL, mPartialTimers, mUnpluggables);
                        wl.mTimerPartial = t;
                    }
                    return t;
                case WAKE_TYPE_FULL:
                    t = wl.mTimerFull;
                    if (t == null) {
                        t = new StopwatchTimer(WAKE_TYPE_FULL, mFullTimers, mUnpluggables);
                        wl.mTimerFull = t;
                    }
                    return t;
                case WAKE_TYPE_WINDOW:
                    t = wl.mTimerWindow;
                    if (t == null) {
                        t = new StopwatchTimer(WAKE_TYPE_WINDOW, mWindowTimers, mUnpluggables);
                        wl.mTimerWindow = t;
                    }
                    return t;
                default:
                    throw new IllegalArgumentException("type=" + type);
            }
        }
        public StopwatchTimer getSensorTimerLocked(int sensor, boolean create) {
            Sensor se = mSensorStats.get(sensor);
            if (se == null) {
                if (!create) {
                    return null;
                }
                se = new Sensor(sensor);
                mSensorStats.put(sensor, se);
            }
            StopwatchTimer t = se.mTimer;
            if (t != null) {
                return t;
            }
            ArrayList<StopwatchTimer> timers = mSensorTimers.get(sensor);
            if (timers == null) {
                timers = new ArrayList<StopwatchTimer>();
                mSensorTimers.put(sensor, timers);
            }
            t = new StopwatchTimer(BatteryStats.SENSOR, timers, mUnpluggables);
            se.mTimer = t;
            return t;
        }
        public void noteStartWakeLocked(String name, int type) {
            StopwatchTimer t = getWakeTimerLocked(name, type);
            if (t != null) {
                t.startRunningLocked(BatteryStatsImpl.this);
            }
        }
        public void noteStopWakeLocked(String name, int type) {
            StopwatchTimer t = getWakeTimerLocked(name, type);
            if (t != null) {
                t.stopRunningLocked(BatteryStatsImpl.this);
            }
        }
        public void noteStartSensor(int sensor) {
            StopwatchTimer t = getSensorTimerLocked(sensor, true);
            if (t != null) {
                t.startRunningLocked(BatteryStatsImpl.this);
            }            
        }
        public void noteStopSensor(int sensor) {
            StopwatchTimer t = getSensorTimerLocked(sensor, false);
            if (t != null) {
                t.stopRunningLocked(BatteryStatsImpl.this);
            }            
        }
        public void noteStartGps() {
            StopwatchTimer t = getSensorTimerLocked(Sensor.GPS, true);
            if (t != null) {
                t.startRunningLocked(BatteryStatsImpl.this);
            }  
        }
        public void noteStopGps() {
            StopwatchTimer t = getSensorTimerLocked(Sensor.GPS, false);
            if (t != null) {
                t.stopRunningLocked(BatteryStatsImpl.this);
            }
        }
        public BatteryStatsImpl getBatteryStats() {
            return BatteryStatsImpl.this;
        }
    }
    public BatteryStatsImpl(String filename) {
        mFile = new JournaledFile(new File(filename), new File(filename + ".tmp"));
        mStartCount++;
        mScreenOnTimer = new StopwatchTimer(-1, null, mUnpluggables);
        for (int i=0; i<NUM_SCREEN_BRIGHTNESS_BINS; i++) {
            mScreenBrightnessTimer[i] = new StopwatchTimer(-100-i, null, mUnpluggables);
        }
        mInputEventCounter = new Counter(mUnpluggables);
        mPhoneOnTimer = new StopwatchTimer(-2, null, mUnpluggables);
        for (int i=0; i<NUM_SIGNAL_STRENGTH_BINS; i++) {
            mPhoneSignalStrengthsTimer[i] = new StopwatchTimer(-200-i, null, mUnpluggables);
        }
        mPhoneSignalScanningTimer = new StopwatchTimer(-200+1, null, mUnpluggables);
        for (int i=0; i<NUM_DATA_CONNECTION_TYPES; i++) {
            mPhoneDataConnectionsTimer[i] = new StopwatchTimer(-300-i, null, mUnpluggables);
        }
        mWifiOnTimer = new StopwatchTimer(-3, null, mUnpluggables);
        mWifiRunningTimer = new StopwatchTimer(-4, null, mUnpluggables);
        mBluetoothOnTimer = new StopwatchTimer(-5, null, mUnpluggables);
        mAudioOnTimer = new StopwatchTimer(-6, null, mUnpluggables);
        mOnBattery = mOnBatteryInternal = false;
        mTrackBatteryPastUptime = 0;
        mTrackBatteryPastRealtime = 0;
        mUptimeStart = mTrackBatteryUptimeStart = SystemClock.uptimeMillis() * 1000;
        mRealtimeStart = mTrackBatteryRealtimeStart = SystemClock.elapsedRealtime() * 1000;
        mUnpluggedBatteryUptime = getBatteryUptimeLocked(mUptimeStart);
        mUnpluggedBatteryRealtime = getBatteryRealtimeLocked(mRealtimeStart);
        mDischargeStartLevel = 0;
        mDischargeCurrentLevel = 0;
    }
    public BatteryStatsImpl(Parcel p) {
        mFile = null;
        readFromParcel(p);
    }
    public void setNumSpeedSteps(int steps) {
        if (sNumSpeedSteps == 0) sNumSpeedSteps = steps;
    }
    public void setRadioScanningTimeout(long timeout) {
        if (mPhoneSignalScanningTimer != null) {
            mPhoneSignalScanningTimer.setTimeout(timeout);
        }
    }
    @Override
    public int getStartCount() {
        return mStartCount;
    }
    public boolean isOnBattery() {
        return mOnBattery;
    }
    public void setOnBattery(boolean onBattery, int level) {
        synchronized(this) {
            updateKernelWakelocksLocked();
            if (mOnBattery != onBattery) {
                mOnBattery = mOnBatteryInternal = onBattery;
                long uptime = SystemClock.uptimeMillis() * 1000;
                long mSecRealtime = SystemClock.elapsedRealtime();
                long realtime = mSecRealtime * 1000;
                if (onBattery) {
                    mTrackBatteryUptimeStart = uptime;
                    mTrackBatteryRealtimeStart = realtime;
                    mUnpluggedBatteryUptime = getBatteryUptimeLocked(uptime);
                    mUnpluggedBatteryRealtime = getBatteryRealtimeLocked(realtime);
                    mDischargeCurrentLevel = mDischargeStartLevel = level;
                    doUnplug(mUnpluggedBatteryUptime, mUnpluggedBatteryRealtime);
                } else {
                    mTrackBatteryPastUptime += uptime - mTrackBatteryUptimeStart;
                    mTrackBatteryPastRealtime += realtime - mTrackBatteryRealtimeStart;
                    mDischargeCurrentLevel = level;
                    doPlug(getBatteryUptimeLocked(uptime), getBatteryRealtimeLocked(realtime));
                }
                if ((mLastWriteTime + (60 * 1000)) < mSecRealtime) {
                    if (mFile != null) {
                        writeLocked();
                    }
                }
            }
        }
    }
    public void recordCurrentLevel(int level) {
        mDischargeCurrentLevel = level;
    }
    public void updateKernelWakelocksLocked() {
        Map<String, KernelWakelockStats> m = readKernelWakelockStats();
        if (m == null) {
            Slog.w(TAG, "Couldn't get kernel wake lock stats");
            return;
        }
        for (Map.Entry<String, KernelWakelockStats> ent : m.entrySet()) {
            String name = ent.getKey();
            KernelWakelockStats kws = ent.getValue();
            SamplingTimer kwlt = mKernelWakelockStats.get(name);
            if (kwlt == null) {
                kwlt = new SamplingTimer(mUnpluggables, mOnBatteryInternal, 
                        true );
                mKernelWakelockStats.put(name, kwlt);
            }
            kwlt.updateCurrentReportedCount(kws.mCount);
            kwlt.updateCurrentReportedTotalTime(kws.mTotalTime);
            kwlt.setUpdateVersion(sKernelWakelockUpdateVersion);
        }
        if (m.size() != mKernelWakelockStats.size()) {
            for (Map.Entry<String, SamplingTimer> ent : mKernelWakelockStats.entrySet()) {
                SamplingTimer st = ent.getValue();
                if (st.getUpdateVersion() != sKernelWakelockUpdateVersion) {
                    st.setStale();
                }
            }
        }
    }
    public long getAwakeTimeBattery() {
        return computeBatteryUptime(getBatteryUptimeLocked(), STATS_CURRENT);
    }
    public long getAwakeTimePlugged() {
        return (SystemClock.uptimeMillis() * 1000) - getAwakeTimeBattery();
    }
    @Override
    public long computeUptime(long curTime, int which) {
        switch (which) {
            case STATS_TOTAL: return mUptime + (curTime-mUptimeStart);
            case STATS_LAST: return mLastUptime;
            case STATS_CURRENT: return (curTime-mUptimeStart);
            case STATS_UNPLUGGED: return (curTime-mTrackBatteryUptimeStart);
        }
        return 0;
    }
    @Override
    public long computeRealtime(long curTime, int which) {
        switch (which) {
            case STATS_TOTAL: return mRealtime + (curTime-mRealtimeStart);
            case STATS_LAST: return mLastRealtime;
            case STATS_CURRENT: return (curTime-mRealtimeStart);
            case STATS_UNPLUGGED: return (curTime-mTrackBatteryRealtimeStart);
        }
        return 0;
    }
    @Override
    public long computeBatteryUptime(long curTime, int which) {
        switch (which) {
            case STATS_TOTAL:
                return mBatteryUptime + getBatteryUptime(curTime);
            case STATS_LAST:
                return mBatteryLastUptime;
            case STATS_CURRENT:
                return getBatteryUptime(curTime);
            case STATS_UNPLUGGED:
                return getBatteryUptimeLocked(curTime) - mUnpluggedBatteryUptime;
        }
        return 0;
    }
    @Override
    public long computeBatteryRealtime(long curTime, int which) {
        switch (which) {
            case STATS_TOTAL:
                return mBatteryRealtime + getBatteryRealtimeLocked(curTime);
            case STATS_LAST:
                return mBatteryLastRealtime;
            case STATS_CURRENT:
                return getBatteryRealtimeLocked(curTime);
            case STATS_UNPLUGGED:
                return getBatteryRealtimeLocked(curTime) - mUnpluggedBatteryRealtime;
        }
        return 0;
    }
    long getBatteryUptimeLocked(long curTime) {
        long time = mTrackBatteryPastUptime;
        if (mOnBatteryInternal) {
            time += curTime - mTrackBatteryUptimeStart;
        }
        return time;
    }
    long getBatteryUptimeLocked() {
        return getBatteryUptime(SystemClock.uptimeMillis() * 1000);
    }
    @Override
    public long getBatteryUptime(long curTime) {
        return getBatteryUptimeLocked(curTime);
    }
    long getBatteryRealtimeLocked(long curTime) {
        long time = mTrackBatteryPastRealtime;
        if (mOnBatteryInternal) {
            time += curTime - mTrackBatteryRealtimeStart;
        }
        return time;
    }
    @Override
    public long getBatteryRealtime(long curTime) {
        return getBatteryRealtimeLocked(curTime);
    }
    private long getTcpBytes(long current, long[] dataBytes, int which) {
        if (which == STATS_LAST) {
            return dataBytes[STATS_LAST];
        } else {
            if (which == STATS_UNPLUGGED) {
                if (dataBytes[STATS_UNPLUGGED] < 0) {
                    return dataBytes[STATS_LAST];
                } else {
                    return current - dataBytes[STATS_UNPLUGGED];
                }
            } else if (which == STATS_TOTAL) {
                return (current - dataBytes[STATS_CURRENT]) + dataBytes[STATS_TOTAL];
            }
            return current - dataBytes[STATS_CURRENT];
        }
    }
    public long getMobileTcpBytesSent(int which) {
        return getTcpBytes(TrafficStats.getMobileTxBytes(), mMobileDataTx, which);
    }
    public long getMobileTcpBytesReceived(int which) {
        return getTcpBytes(TrafficStats.getMobileRxBytes(), mMobileDataRx, which);
    }
    public long getTotalTcpBytesSent(int which) {
        return getTcpBytes(TrafficStats.getTotalTxBytes(), mTotalDataTx, which);
    }
    public long getTotalTcpBytesReceived(int which) {
        return getTcpBytes(TrafficStats.getTotalRxBytes(), mTotalDataRx, which);
    }
    @Override
    public int getDischargeStartLevel() {
        synchronized(this) {
            return getDischargeStartLevelLocked();
        }
    }
    public int getDischargeStartLevelLocked() {
            return mDischargeStartLevel;
    }
    @Override
    public int getDischargeCurrentLevel() {
        synchronized(this) {
            return getDischargeCurrentLevelLocked();
        }
    }
    public int getDischargeCurrentLevelLocked() {
            return mDischargeCurrentLevel;
    }
    @Override
    public int getCpuSpeedSteps() {
        return sNumSpeedSteps;
    }
    public Uid getUidStatsLocked(int uid) {
        Uid u = mUidStats.get(uid);
        if (u == null) {
            u = new Uid(uid);
            mUidStats.put(uid, u);
        }
        return u;
    }
    public void removeUidStatsLocked(int uid) {
        mUidStats.remove(uid);
    }
    public Uid.Proc getProcessStatsLocked(int uid, String name) {
        Uid u = getUidStatsLocked(uid);
        return u.getProcessStatsLocked(name);
    }
    public Uid.Proc getProcessStatsLocked(String name, int pid) {
        int uid;
        if (mUidCache.containsKey(name)) {
            uid = mUidCache.get(name);
        } else {
            uid = Process.getUidForPid(pid);
            mUidCache.put(name, uid);
        }
        Uid u = getUidStatsLocked(uid);
        return u.getProcessStatsLocked(name);
    }
    public Uid.Pkg getPackageStatsLocked(int uid, String pkg) {
        Uid u = getUidStatsLocked(uid);
        return u.getPackageStatsLocked(pkg);
    }
    public Uid.Pkg.Serv getServiceStatsLocked(int uid, String pkg, String name) {
        Uid u = getUidStatsLocked(uid);
        return u.getServiceStatsLocked(pkg, name);
    }
    private static JournaledFile makeJournaledFile() {
        final String base = "/data/system/device_policies.xml";
        return new JournaledFile(new File(base), new File(base + ".tmp"));
    }
    public void writeLocked() {
        if (mFile == null) {
            Slog.w("BatteryStats", "writeLocked: no file associated with this instance");
            return;
        }
        try {
            FileOutputStream stream = new FileOutputStream(mFile.chooseForWrite());
            Parcel out = Parcel.obtain();
            writeSummaryToParcel(out);
            stream.write(out.marshall());
            out.recycle();
            stream.flush();
            stream.close();
            mFile.commit();
            mLastWriteTime = SystemClock.elapsedRealtime();
            return;
        } catch (IOException e) {
            Slog.w("BatteryStats", "Error writing battery statistics", e);
        }
        mFile.rollback();
    }
    static byte[] readFully(FileInputStream stream) throws java.io.IOException {
        int pos = 0;
        int avail = stream.available();
        byte[] data = new byte[avail];
        while (true) {
            int amt = stream.read(data, pos, data.length-pos);
            if (amt <= 0) {
                return data;
            }
            pos += amt;
            avail = stream.available();
            if (avail > data.length-pos) {
                byte[] newData = new byte[pos+avail];
                System.arraycopy(data, 0, newData, 0, pos);
                data = newData;
            }
        }
    }
    public void readLocked() {
        if (mFile == null) {
            Slog.w("BatteryStats", "readLocked: no file associated with this instance");
            return;
        }
        mUidStats.clear();
        try {
            File file = mFile.chooseForRead();
            if (!file.exists()) {
                return;
            }
            FileInputStream stream = new FileInputStream(file);
            byte[] raw = readFully(stream);
            Parcel in = Parcel.obtain();
            in.unmarshall(raw, 0, raw.length);
            in.setDataPosition(0);
            stream.close();
            readSummaryFromParcel(in);
        } catch(java.io.IOException e) {
            Slog.e("BatteryStats", "Error reading battery statistics", e);
        }
    }
    public int describeContents() {
        return 0;
    }
    private void readSummaryFromParcel(Parcel in) {
        final int version = in.readInt();
        if (version != VERSION) {
            Slog.w("BatteryStats", "readFromParcel: version got " + version
                + ", expected " + VERSION + "; erasing old stats");
            return;
        }
        mStartCount = in.readInt();
        mBatteryUptime = in.readLong();
        mBatteryLastUptime = in.readLong();
        mBatteryRealtime = in.readLong();
        mBatteryLastRealtime = in.readLong();
        mUptime = in.readLong();
        mLastUptime = in.readLong();
        mRealtime = in.readLong();
        mLastRealtime = in.readLong();
        mDischargeStartLevel = in.readInt();
        mDischargeCurrentLevel = in.readInt();
        mStartCount++;
        mScreenOn = false;
        mScreenOnTimer.readSummaryFromParcelLocked(in);
        for (int i=0; i<NUM_SCREEN_BRIGHTNESS_BINS; i++) {
            mScreenBrightnessTimer[i].readSummaryFromParcelLocked(in);
        }
        mInputEventCounter.readSummaryFromParcelLocked(in);
        mPhoneOn = false;
        mPhoneOnTimer.readSummaryFromParcelLocked(in);
        for (int i=0; i<NUM_SIGNAL_STRENGTH_BINS; i++) {
            mPhoneSignalStrengthsTimer[i].readSummaryFromParcelLocked(in);
        }
        mPhoneSignalScanningTimer.readSummaryFromParcelLocked(in);
        for (int i=0; i<NUM_DATA_CONNECTION_TYPES; i++) {
            mPhoneDataConnectionsTimer[i].readSummaryFromParcelLocked(in);
        }
        mWifiOn = false;
        mWifiOnTimer.readSummaryFromParcelLocked(in);
        mWifiRunning = false;
        mWifiRunningTimer.readSummaryFromParcelLocked(in);
        mBluetoothOn = false;
        mBluetoothOnTimer.readSummaryFromParcelLocked(in);
        int NKW = in.readInt();
        if (NKW > 10000) {
            Slog.w(TAG, "File corrupt: too many kernel wake locks " + NKW);
            return;
        }
        for (int ikw = 0; ikw < NKW; ikw++) {
            if (in.readInt() != 0) {
                String kwltName = in.readString();
                getKernelWakelockTimerLocked(kwltName).readSummaryFromParcelLocked(in);
            }
        }
        sNumSpeedSteps = in.readInt();
        final int NU = in.readInt();
        if (NU > 10000) {
            Slog.w(TAG, "File corrupt: too many uids " + NU);
            return;
        }
        for (int iu = 0; iu < NU; iu++) {
            int uid = in.readInt();
            Uid u = new Uid(uid);
            mUidStats.put(uid, u);
            u.mWifiTurnedOn = false;
            u.mWifiTurnedOnTimer.readSummaryFromParcelLocked(in);
            u.mFullWifiLockOut = false;
            u.mFullWifiLockTimer.readSummaryFromParcelLocked(in);
            u.mAudioTurnedOn = false;
            u.mAudioTurnedOnTimer.readSummaryFromParcelLocked(in);
            u.mVideoTurnedOn = false;
            u.mVideoTurnedOnTimer.readSummaryFromParcelLocked(in);
            u.mScanWifiLockOut = false;
            u.mScanWifiLockTimer.readSummaryFromParcelLocked(in);
            u.mWifiMulticastEnabled = false;
            u.mWifiMulticastTimer.readSummaryFromParcelLocked(in);
            if (in.readInt() != 0) {
                if (u.mUserActivityCounters == null) {
                    u.initUserActivityLocked();
                }
                for (int i=0; i<Uid.NUM_USER_ACTIVITY_TYPES; i++) {
                    u.mUserActivityCounters[i].readSummaryFromParcelLocked(in);
                }
            }
            int NW = in.readInt();
            if (NW > 10000) {
                Slog.w(TAG, "File corrupt: too many wake locks " + NW);
                return;
            }
            for (int iw = 0; iw < NW; iw++) {
                String wlName = in.readString();
                if (in.readInt() != 0) {
                    u.getWakeTimerLocked(wlName, WAKE_TYPE_FULL).readSummaryFromParcelLocked(in);
                }
                if (in.readInt() != 0) {
                    u.getWakeTimerLocked(wlName, WAKE_TYPE_PARTIAL).readSummaryFromParcelLocked(in);
                }
                if (in.readInt() != 0) {
                    u.getWakeTimerLocked(wlName, WAKE_TYPE_WINDOW).readSummaryFromParcelLocked(in);
                }
            }
            int NP = in.readInt();
            if (NP > 10000) {
                Slog.w(TAG, "File corrupt: too many sensors " + NP);
                return;
            }
            for (int is = 0; is < NP; is++) {
                int seNumber = in.readInt();
                if (in.readInt() != 0) {
                    u.getSensorTimerLocked(seNumber, true)
                            .readSummaryFromParcelLocked(in);
                }
            }
            NP = in.readInt();
            if (NP > 10000) {
                Slog.w(TAG, "File corrupt: too many processes " + NP);
                return;
            }
            for (int ip = 0; ip < NP; ip++) {
                String procName = in.readString();
                Uid.Proc p = u.getProcessStatsLocked(procName);
                p.mUserTime = p.mLoadedUserTime = in.readLong();
                p.mLastUserTime = in.readLong();
                p.mSystemTime = p.mLoadedSystemTime = in.readLong();
                p.mLastSystemTime = in.readLong();
                p.mStarts = p.mLoadedStarts = in.readInt();
                p.mLastStarts = in.readInt();
            }
            NP = in.readInt();
            if (NP > 10000) {
                Slog.w(TAG, "File corrupt: too many packages " + NP);
                return;
            }
            for (int ip = 0; ip < NP; ip++) {
                String pkgName = in.readString();
                Uid.Pkg p = u.getPackageStatsLocked(pkgName);
                p.mWakeups = p.mLoadedWakeups = in.readInt();
                p.mLastWakeups = in.readInt();
                final int NS = in.readInt();
                for (int is = 0; is < NS; is++) {
                    String servName = in.readString();
                    Uid.Pkg.Serv s = u.getServiceStatsLocked(pkgName, servName);
                    s.mStartTime = s.mLoadedStartTime = in.readLong();
                    s.mLastStartTime = in.readLong();
                    s.mStarts = s.mLoadedStarts = in.readInt();
                    s.mLastStarts = in.readInt();
                    s.mLaunches = s.mLoadedLaunches = in.readInt();
                    s.mLastLaunches = in.readInt();
                }
            }
            u.mLoadedTcpBytesReceived = in.readLong();
            u.mLoadedTcpBytesSent = in.readLong();
        }
    }
    public void writeSummaryToParcel(Parcel out) {
        final long NOW_SYS = SystemClock.uptimeMillis() * 1000;
        final long NOWREAL_SYS = SystemClock.elapsedRealtime() * 1000;
        final long NOW = getBatteryUptimeLocked(NOW_SYS);
        final long NOWREAL = getBatteryRealtimeLocked(NOWREAL_SYS);
        out.writeInt(VERSION);
        out.writeInt(mStartCount);
        out.writeLong(computeBatteryUptime(NOW_SYS, STATS_TOTAL));
        out.writeLong(computeBatteryUptime(NOW_SYS, STATS_CURRENT));
        out.writeLong(computeBatteryRealtime(NOWREAL_SYS, STATS_TOTAL));
        out.writeLong(computeBatteryRealtime(NOWREAL_SYS, STATS_CURRENT));
        out.writeLong(computeUptime(NOW_SYS, STATS_TOTAL));
        out.writeLong(computeUptime(NOW_SYS, STATS_CURRENT));
        out.writeLong(computeRealtime(NOWREAL_SYS, STATS_TOTAL));
        out.writeLong(computeRealtime(NOWREAL_SYS, STATS_CURRENT));
        out.writeInt(mDischargeStartLevel);
        out.writeInt(mDischargeCurrentLevel);
        mScreenOnTimer.writeSummaryFromParcelLocked(out, NOWREAL);
        for (int i=0; i<NUM_SCREEN_BRIGHTNESS_BINS; i++) {
            mScreenBrightnessTimer[i].writeSummaryFromParcelLocked(out, NOWREAL);
        }
        mInputEventCounter.writeSummaryFromParcelLocked(out);
        mPhoneOnTimer.writeSummaryFromParcelLocked(out, NOWREAL);
        for (int i=0; i<NUM_SIGNAL_STRENGTH_BINS; i++) {
            mPhoneSignalStrengthsTimer[i].writeSummaryFromParcelLocked(out, NOWREAL);
        }
        mPhoneSignalScanningTimer.writeSummaryFromParcelLocked(out, NOWREAL);
        for (int i=0; i<NUM_DATA_CONNECTION_TYPES; i++) {
            mPhoneDataConnectionsTimer[i].writeSummaryFromParcelLocked(out, NOWREAL);
        }
        mWifiOnTimer.writeSummaryFromParcelLocked(out, NOWREAL);
        mWifiRunningTimer.writeSummaryFromParcelLocked(out, NOWREAL);
        mBluetoothOnTimer.writeSummaryFromParcelLocked(out, NOWREAL);
        out.writeInt(mKernelWakelockStats.size());
        for (Map.Entry<String, SamplingTimer> ent : mKernelWakelockStats.entrySet()) {
            Timer kwlt = ent.getValue();
            if (kwlt != null) {
                out.writeInt(1);
                out.writeString(ent.getKey());
                ent.getValue().writeSummaryFromParcelLocked(out, NOWREAL);
            } else {
                out.writeInt(0);
            }
        }
        out.writeInt(sNumSpeedSteps);
        final int NU = mUidStats.size();
        out.writeInt(NU);
        for (int iu = 0; iu < NU; iu++) {
            out.writeInt(mUidStats.keyAt(iu));
            Uid u = mUidStats.valueAt(iu);
            u.mWifiTurnedOnTimer.writeSummaryFromParcelLocked(out, NOWREAL);
            u.mFullWifiLockTimer.writeSummaryFromParcelLocked(out, NOWREAL);
            u.mAudioTurnedOnTimer.writeSummaryFromParcelLocked(out, NOWREAL);
            u.mVideoTurnedOnTimer.writeSummaryFromParcelLocked(out, NOWREAL);
            u.mScanWifiLockTimer.writeSummaryFromParcelLocked(out, NOWREAL);
            u.mWifiMulticastTimer.writeSummaryFromParcelLocked(out, NOWREAL);
            if (u.mUserActivityCounters == null) {
                out.writeInt(0);
            } else {
                out.writeInt(1);
                for (int i=0; i<Uid.NUM_USER_ACTIVITY_TYPES; i++) {
                    u.mUserActivityCounters[i].writeSummaryFromParcelLocked(out);
                }
            }
            int NW = u.mWakelockStats.size();
            out.writeInt(NW);
            if (NW > 0) {
                for (Map.Entry<String, BatteryStatsImpl.Uid.Wakelock> ent
                        : u.mWakelockStats.entrySet()) {
                    out.writeString(ent.getKey());
                    Uid.Wakelock wl = ent.getValue();
                    if (wl.mTimerFull != null) {
                        out.writeInt(1);
                        wl.mTimerFull.writeSummaryFromParcelLocked(out, NOWREAL);
                    } else {
                        out.writeInt(0);
                    }
                    if (wl.mTimerPartial != null) {
                        out.writeInt(1);
                        wl.mTimerPartial.writeSummaryFromParcelLocked(out, NOWREAL);
                    } else {
                        out.writeInt(0);
                    }
                    if (wl.mTimerWindow != null) {
                        out.writeInt(1);
                        wl.mTimerWindow.writeSummaryFromParcelLocked(out, NOWREAL);
                    } else {
                        out.writeInt(0);
                    }
                }
            }
            int NSE = u.mSensorStats.size();
            out.writeInt(NSE);
            if (NSE > 0) {
                for (Map.Entry<Integer, BatteryStatsImpl.Uid.Sensor> ent
                        : u.mSensorStats.entrySet()) {
                    out.writeInt(ent.getKey());
                    Uid.Sensor se = ent.getValue();
                    if (se.mTimer != null) {
                        out.writeInt(1);
                        se.mTimer.writeSummaryFromParcelLocked(out, NOWREAL);
                    } else {
                        out.writeInt(0);
                    }
                }
            }
            int NP = u.mProcessStats.size();
            out.writeInt(NP);
            if (NP > 0) {
                for (Map.Entry<String, BatteryStatsImpl.Uid.Proc> ent
                    : u.mProcessStats.entrySet()) {
                    out.writeString(ent.getKey());
                    Uid.Proc ps = ent.getValue();
                    out.writeLong(ps.mUserTime);
                    out.writeLong(ps.mUserTime - ps.mLoadedUserTime);
                    out.writeLong(ps.mSystemTime);
                    out.writeLong(ps.mSystemTime - ps.mLoadedSystemTime);
                    out.writeInt(ps.mStarts);
                    out.writeInt(ps.mStarts - ps.mLoadedStarts);
                }
            }
            NP = u.mPackageStats.size();
            out.writeInt(NP);
            if (NP > 0) {
                for (Map.Entry<String, BatteryStatsImpl.Uid.Pkg> ent
                    : u.mPackageStats.entrySet()) {
                    out.writeString(ent.getKey());
                    Uid.Pkg ps = ent.getValue();
                    out.writeInt(ps.mWakeups);
                    out.writeInt(ps.mWakeups - ps.mLoadedWakeups);
                    final int NS = ps.mServiceStats.size();
                    out.writeInt(NS);
                    if (NS > 0) {
                        for (Map.Entry<String, BatteryStatsImpl.Uid.Pkg.Serv> sent
                                : ps.mServiceStats.entrySet()) {
                            out.writeString(sent.getKey());
                            BatteryStatsImpl.Uid.Pkg.Serv ss = sent.getValue();
                            long time = ss.getStartTimeToNowLocked(NOW);
                            out.writeLong(time);
                            out.writeLong(time - ss.mLoadedStartTime);
                            out.writeInt(ss.mStarts);
                            out.writeInt(ss.mStarts - ss.mLoadedStarts);
                            out.writeInt(ss.mLaunches);
                            out.writeInt(ss.mLaunches - ss.mLoadedLaunches);
                        }
                    }
                }
            }
            out.writeLong(u.getTcpBytesReceived(STATS_TOTAL));
            out.writeLong(u.getTcpBytesSent(STATS_TOTAL));
        }
    }
    public void readFromParcel(Parcel in) {
        readFromParcelLocked(in);
    }
    void readFromParcelLocked(Parcel in) {
        int magic = in.readInt();
        if (magic != MAGIC) {
            throw new ParcelFormatException("Bad magic number");
        }
        mStartCount = in.readInt();
        mBatteryUptime = in.readLong();
        mBatteryLastUptime = in.readLong();
        mBatteryRealtime = in.readLong();
        mBatteryLastRealtime = in.readLong();
        mScreenOn = false;
        mScreenOnTimer = new StopwatchTimer(-1, null, mUnpluggables, in);
        for (int i=0; i<NUM_SCREEN_BRIGHTNESS_BINS; i++) {
            mScreenBrightnessTimer[i] = new StopwatchTimer(-100-i, null, mUnpluggables, in);
        }
        mInputEventCounter = new Counter(mUnpluggables, in);
        mPhoneOn = false;
        mPhoneOnTimer = new StopwatchTimer(-2, null, mUnpluggables, in);
        for (int i=0; i<NUM_SIGNAL_STRENGTH_BINS; i++) {
            mPhoneSignalStrengthsTimer[i] = new StopwatchTimer(-200-i, null, mUnpluggables, in);
        }
        mPhoneSignalScanningTimer = new StopwatchTimer(-200+1, null, mUnpluggables, in);
        for (int i=0; i<NUM_DATA_CONNECTION_TYPES; i++) {
            mPhoneDataConnectionsTimer[i] = new StopwatchTimer(-300-i, null, mUnpluggables, in);
        }
        mWifiOn = false;
        mWifiOnTimer = new StopwatchTimer(-2, null, mUnpluggables, in);
        mWifiRunning = false;
        mWifiRunningTimer = new StopwatchTimer(-2, null, mUnpluggables, in);
        mBluetoothOn = false;
        mBluetoothOnTimer = new StopwatchTimer(-2, null, mUnpluggables, in);
        mUptime = in.readLong();
        mUptimeStart = in.readLong();
        mLastUptime = in.readLong();
        mRealtime = in.readLong();
        mRealtimeStart = in.readLong();
        mLastRealtime = in.readLong();
        mOnBattery = in.readInt() != 0;
        mOnBatteryInternal = false; 
        mTrackBatteryPastUptime = in.readLong();
        mTrackBatteryUptimeStart = in.readLong();
        mTrackBatteryPastRealtime = in.readLong();
        mTrackBatteryRealtimeStart = in.readLong();
        mUnpluggedBatteryUptime = in.readLong();
        mUnpluggedBatteryRealtime = in.readLong();
        mDischargeStartLevel = in.readInt();
        mDischargeCurrentLevel = in.readInt();
        mLastWriteTime = in.readLong();
        mMobileDataRx[STATS_LAST] = in.readLong();
        mMobileDataRx[STATS_UNPLUGGED] = -1;
        mMobileDataTx[STATS_LAST] = in.readLong();
        mMobileDataTx[STATS_UNPLUGGED] = -1;
        mTotalDataRx[STATS_LAST] = in.readLong();
        mTotalDataRx[STATS_UNPLUGGED] = -1;
        mTotalDataTx[STATS_LAST] = in.readLong();
        mTotalDataTx[STATS_UNPLUGGED] = -1;
        mRadioDataUptime = in.readLong();
        mRadioDataStart = -1;
        mBluetoothPingCount = in.readInt();
        mBluetoothPingStart = -1;
        mKernelWakelockStats.clear();
        int NKW = in.readInt();
        for (int ikw = 0; ikw < NKW; ikw++) {
            if (in.readInt() != 0) {
                String wakelockName = in.readString();
                in.readInt(); 
                SamplingTimer kwlt = new SamplingTimer(mUnpluggables, mOnBattery, in);
                mKernelWakelockStats.put(wakelockName, kwlt);
            }
        }
        mPartialTimers.clear();
        mFullTimers.clear();
        mWindowTimers.clear();
        sNumSpeedSteps = in.readInt();
        int numUids = in.readInt();
        mUidStats.clear();
        for (int i = 0; i < numUids; i++) {
            int uid = in.readInt();
            Uid u = new Uid(uid);
            u.readFromParcelLocked(mUnpluggables, in);
            mUidStats.append(uid, u);
        }
    }
    public void writeToParcel(Parcel out, int flags) {
        writeToParcelLocked(out, flags);
    }
    @SuppressWarnings("unused") 
    void writeToParcelLocked(Parcel out, int flags) {
        final long uSecUptime = SystemClock.uptimeMillis() * 1000;
        final long uSecRealtime = SystemClock.elapsedRealtime() * 1000;
        final long batteryUptime = getBatteryUptimeLocked(uSecUptime);
        final long batteryRealtime = getBatteryRealtimeLocked(uSecRealtime);
        out.writeInt(MAGIC);
        out.writeInt(mStartCount);
        out.writeLong(mBatteryUptime);
        out.writeLong(mBatteryLastUptime);
        out.writeLong(mBatteryRealtime);
        out.writeLong(mBatteryLastRealtime);
        mScreenOnTimer.writeToParcel(out, batteryRealtime);
        for (int i=0; i<NUM_SCREEN_BRIGHTNESS_BINS; i++) {
            mScreenBrightnessTimer[i].writeToParcel(out, batteryRealtime);
        }
        mInputEventCounter.writeToParcel(out);
        mPhoneOnTimer.writeToParcel(out, batteryRealtime);
        for (int i=0; i<NUM_SIGNAL_STRENGTH_BINS; i++) {
            mPhoneSignalStrengthsTimer[i].writeToParcel(out, batteryRealtime);
        }
        mPhoneSignalScanningTimer.writeToParcel(out, batteryRealtime);
        for (int i=0; i<NUM_DATA_CONNECTION_TYPES; i++) {
            mPhoneDataConnectionsTimer[i].writeToParcel(out, batteryRealtime);
        }
        mWifiOnTimer.writeToParcel(out, batteryRealtime);
        mWifiRunningTimer.writeToParcel(out, batteryRealtime);
        mBluetoothOnTimer.writeToParcel(out, batteryRealtime);
        out.writeLong(mUptime);
        out.writeLong(mUptimeStart);
        out.writeLong(mLastUptime);
        out.writeLong(mRealtime);
        out.writeLong(mRealtimeStart);
        out.writeLong(mLastRealtime);
        out.writeInt(mOnBattery ? 1 : 0);
        out.writeLong(batteryUptime);
        out.writeLong(mTrackBatteryUptimeStart);
        out.writeLong(batteryRealtime);
        out.writeLong(mTrackBatteryRealtimeStart);
        out.writeLong(mUnpluggedBatteryUptime);
        out.writeLong(mUnpluggedBatteryRealtime);
        out.writeInt(mDischargeStartLevel);
        out.writeInt(mDischargeCurrentLevel);
        out.writeLong(mLastWriteTime);
        out.writeLong(getMobileTcpBytesReceived(STATS_UNPLUGGED));
        out.writeLong(getMobileTcpBytesSent(STATS_UNPLUGGED));
        out.writeLong(getTotalTcpBytesReceived(STATS_UNPLUGGED));
        out.writeLong(getTotalTcpBytesSent(STATS_UNPLUGGED));
        out.writeLong(getRadioDataUptime());
        out.writeInt(getBluetoothPingCount());
        out.writeInt(mKernelWakelockStats.size());
        for (Map.Entry<String, SamplingTimer> ent : mKernelWakelockStats.entrySet()) {
            SamplingTimer kwlt = ent.getValue();
            if (kwlt != null) {
                out.writeInt(1);
                out.writeString(ent.getKey());
                Timer.writeTimerToParcel(out, kwlt, batteryRealtime);
            } else {
                out.writeInt(0);
            }
        }
        out.writeInt(sNumSpeedSteps);
        int size = mUidStats.size();
        out.writeInt(size);
        for (int i = 0; i < size; i++) {
            out.writeInt(mUidStats.keyAt(i));
            Uid uid = mUidStats.valueAt(i);
            uid.writeToParcelLocked(out, batteryRealtime);
        }
    }
    public static final Parcelable.Creator<BatteryStatsImpl> CREATOR =
        new Parcelable.Creator<BatteryStatsImpl>() {
        public BatteryStatsImpl createFromParcel(Parcel in) {
            return new BatteryStatsImpl(in);
        }
        public BatteryStatsImpl[] newArray(int size) {
            return new BatteryStatsImpl[size];
        }
    };
    public void dumpLocked(PrintWriter pw) {
        if (DEBUG) {
            Printer pr = new PrintWriterPrinter(pw);
            pr.println("*** Screen timer:");
            mScreenOnTimer.logState(pr, "  ");
            for (int i=0; i<NUM_SCREEN_BRIGHTNESS_BINS; i++) {
                pr.println("*** Screen brightness #" + i + ":");
                mScreenBrightnessTimer[i].logState(pr, "  ");
            }
            pr.println("*** Input event counter:");
            mInputEventCounter.logState(pr, "  ");
            pr.println("*** Phone timer:");
            mPhoneOnTimer.logState(pr, "  ");
            for (int i=0; i<NUM_SIGNAL_STRENGTH_BINS; i++) {
                pr.println("*** Signal strength #" + i + ":");
                mPhoneSignalStrengthsTimer[i].logState(pr, "  ");
            }
            pr.println("*** Signal scanning :");
            mPhoneSignalScanningTimer.logState(pr, "  ");
            for (int i=0; i<NUM_DATA_CONNECTION_TYPES; i++) {
                pr.println("*** Data connection type #" + i + ":");
                mPhoneDataConnectionsTimer[i].logState(pr, "  ");
            }
            pr.println("*** Wifi timer:");
            mWifiOnTimer.logState(pr, "  ");
            pr.println("*** WifiRunning timer:");
            mWifiRunningTimer.logState(pr, "  ");
            pr.println("*** Bluetooth timer:");
            mBluetoothOnTimer.logState(pr, "  ");
        }
        super.dumpLocked(pw);
    }
}
