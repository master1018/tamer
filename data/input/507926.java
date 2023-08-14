public class DataUsageListener {
    private ThrottleManager mThrottleManager;
    private Preference mCurrentUsagePref = null;
    private Preference mTimeFramePref = null;
    private Preference mThrottleRatePref = null;
    private Preference mSummaryPref = null;
    private PreferenceScreen mPrefScreen = null;
    private boolean mSummaryPrefEnabled = false;
    private final Context mContext;
    private IntentFilter mFilter;
    private BroadcastReceiver mReceiver;
    private int mPolicyThrottleValue;  
    private long mPolicyThreshold;
    private int mCurrentThrottleRate;
    private long mDataUsed;
    private Calendar mStart;
    private Calendar mEnd;
    public DataUsageListener(Context context, Preference summary, PreferenceScreen prefScreen) {
        mContext = context;
        mSummaryPref = summary;
        mPrefScreen = prefScreen;
        mSummaryPrefEnabled = true;
        initialize();
    }
    public DataUsageListener(Context context, Preference currentUsage,
            Preference timeFrame, Preference throttleRate) {
        mContext = context;
        mCurrentUsagePref = currentUsage;
        mTimeFramePref = timeFrame;
        mThrottleRatePref = throttleRate;
        initialize();
    }
    private void initialize() {
        mThrottleManager = (ThrottleManager) mContext.getSystemService(Context.THROTTLE_SERVICE);
        mStart = GregorianCalendar.getInstance();
        mEnd = GregorianCalendar.getInstance();
        mFilter = new IntentFilter();
        mFilter.addAction(ThrottleManager.THROTTLE_POLL_ACTION);
        mFilter.addAction(ThrottleManager.THROTTLE_ACTION);
        mFilter.addAction(ThrottleManager.POLICY_CHANGED_ACTION);
        mReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                String action = intent.getAction();
                if (ThrottleManager.THROTTLE_POLL_ACTION.equals(action)) {
                    updateUsageStats(intent.getLongExtra(ThrottleManager.EXTRA_CYCLE_READ, 0),
                        intent.getLongExtra(ThrottleManager.EXTRA_CYCLE_WRITE, 0),
                        intent.getLongExtra(ThrottleManager.EXTRA_CYCLE_START, 0),
                        intent.getLongExtra(ThrottleManager.EXTRA_CYCLE_END, 0));
                } else if (ThrottleManager.POLICY_CHANGED_ACTION.equals(action)) {
                    updatePolicy();
                } else if (ThrottleManager.THROTTLE_ACTION.equals(action)) {
                    updateThrottleRate(intent.getIntExtra(ThrottleManager.EXTRA_THROTTLE_LEVEL, -1));
                }
            }
        };
    }
    void resume() {
        mContext.registerReceiver(mReceiver, mFilter);
        updatePolicy();
    }
    void pause() {
        mContext.unregisterReceiver(mReceiver);
    }
    private void updatePolicy() {
        mPolicyThrottleValue = mThrottleManager.getCliffLevel(null, 1);
        mPolicyThreshold = mThrottleManager.getCliffThreshold(null, 1);
        if (mSummaryPref != null) { 
            if (mPolicyThreshold == 0) {
                if (mSummaryPrefEnabled) {
                    mPrefScreen.removePreference(mSummaryPref);
                    mSummaryPrefEnabled = false;
                }
            } else {
                if (!mSummaryPrefEnabled) {
                    mSummaryPrefEnabled = true;
                    mPrefScreen.addPreference(mSummaryPref);
                }
            }
        }
        updateUI();
    }
    private void updateThrottleRate(int throttleRate) {
        mCurrentThrottleRate = throttleRate;
        updateUI();
    }
    private void updateUsageStats(long readByteCount, long writeByteCount,
            long startTime, long endTime) {
        mDataUsed = readByteCount + writeByteCount;
        mStart.setTimeInMillis(startTime);
        mEnd.setTimeInMillis(endTime);
        updateUI();
    }
    private void updateUI() {
        if (mPolicyThreshold == 0)
            return;
        int dataUsedPercent = (int) ((mDataUsed * 100) / mPolicyThreshold);
        long cycleTime = mEnd.getTimeInMillis() - mStart.getTimeInMillis();
        long currentTime = GregorianCalendar.getInstance().getTimeInMillis()
                            - mStart.getTimeInMillis();
        int cycleThroughPercent = (cycleTime == 0) ? 0 : (int) ((currentTime * 100) / cycleTime);
        Calendar cal = Calendar.getInstance();
        cal.setTimeInMillis(cycleTime - currentTime);
        int daysLeft = cal.get(Calendar.DAY_OF_YEAR);
        if (daysLeft >= 365) daysLeft = 0;
        if (mCurrentUsagePref != null) {
            if (mCurrentThrottleRate > 0) {
                mCurrentUsagePref.setSummary(mContext.getString(
                        R.string.throttle_data_rate_reduced_subtext,
                        toReadable(mPolicyThreshold),
                        mCurrentThrottleRate));
            } else {
                mCurrentUsagePref.setSummary(mContext.getString(
                        R.string.throttle_data_usage_subtext,
                        toReadable(mDataUsed), dataUsedPercent, toReadable(mPolicyThreshold)));
            }
        }
        if (mTimeFramePref != null) {
            mTimeFramePref.setSummary(mContext.getString(R.string.throttle_time_frame_subtext,
                        cycleThroughPercent, daysLeft,
                        DateFormat.getDateInstance(DateFormat.SHORT).format(mEnd.getTime())));
        }
        if (mThrottleRatePref != null) {
            mThrottleRatePref.setSummary(mContext.getString(R.string.throttle_rate_subtext,
                    mPolicyThrottleValue));
        }
        if (mSummaryPref != null && mSummaryPrefEnabled) {
            if (mCurrentThrottleRate > 0) {
                mSummaryPref.setSummary(mContext.getString(
                        R.string.throttle_data_rate_reduced_subtext,
                        toReadable(mPolicyThreshold),
                        mCurrentThrottleRate));
            } else {
                mSummaryPref.setSummary(mContext.getString(R.string.throttle_status_subtext,
                            toReadable(mDataUsed),
                            dataUsedPercent,
                            toReadable(mPolicyThreshold),
                            daysLeft,
                            DateFormat.getDateInstance(DateFormat.SHORT).format(mEnd.getTime())));
            }
        }
    }
    private String toReadable (long data) {
        long KB = 1024;
        long MB = 1024 * KB;
        long GB = 1024 * MB;
        long TB = 1024 * GB;
        String ret;
        if (data < KB) {
            ret = data + " bytes";
        } else if (data < MB) {
            ret = (data / KB) + " KB";
        } else if (data < GB) {
            ret = (data / MB) + " MB";
        } else if (data < TB) {
            ret = (data / GB) + " GB";
        } else {
            ret = (data / TB) + " TB";
        }
        return ret;
    }
}
