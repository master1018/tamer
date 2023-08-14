public class CallTime extends Handler {
    private static final String LOG_TAG = "PHONE/CallTime";
    private static final boolean DBG = false;
     static final boolean PROFILE = true;
    private static final int PROFILE_STATE_NONE = 0;
    private static final int PROFILE_STATE_READY = 1;
    private static final int PROFILE_STATE_RUNNING = 2;
    private static int sProfileState = PROFILE_STATE_NONE;
    private Call mCall;
    private long mLastReportedTime;
    private boolean mTimerRunning;
    private long mInterval;
    private PeriodicTimerCallback mTimerCallback;
    private OnTickListener mListener;
    interface OnTickListener {
        void onTickForCallTimeElapsed(long timeElapsed);
    }
    public CallTime(OnTickListener listener) {
        mListener = listener;
        mTimerCallback = new PeriodicTimerCallback();
    }
     void setActiveCallMode(Call call) {
        if (DBG) log("setActiveCallMode(" + call + ")...");
        mCall = call;
        mInterval = 1000;  
    }
     void reset() {
        if (DBG) log("reset()...");
        mLastReportedTime = SystemClock.uptimeMillis() - mInterval;
    }
     void periodicUpdateTimer() {
        if (!mTimerRunning) {
            mTimerRunning = true;
            long now = SystemClock.uptimeMillis();
            long nextReport = mLastReportedTime + mInterval;
            while (now >= nextReport) {
                nextReport += mInterval;
            }
            if (DBG) log("periodicUpdateTimer() @ " + nextReport);
            postAtTime(mTimerCallback, nextReport);
            mLastReportedTime = nextReport;
            if (mCall != null) {
                Call.State state = mCall.getState();
                if (state == Call.State.ACTIVE) {
                    updateElapsedTime(mCall);
                }
            }
            if (PROFILE && isTraceReady()) {
                startTrace();
            }
        } else {
            if (DBG) log("periodicUpdateTimer: timer already running, bail");
        }
    }
     void cancelTimer() {
        if (DBG) log("cancelTimer()...");
        removeCallbacks(mTimerCallback);
        mTimerRunning = false;
    }
    private void updateElapsedTime(Call call) {
        if (mListener != null) {
            long duration = getCallDuration(call);
            mListener.onTickForCallTimeElapsed(duration / 1000);
        }
    }
     static long getCallDuration(Call call) {
        long duration = 0;
        List connections = call.getConnections();
        int count = connections.size();
        Connection c;
        if (count == 1) {
            c = (Connection) connections.get(0);
            duration = c.getDurationMillis();
        } else {
            for (int i = 0; i < count; i++) {
                c = (Connection) connections.get(i);
                long t = c.getDurationMillis();
                if (t > duration) {
                    duration = t;
                }
            }
        }
        if (DBG) log("updateElapsedTime, count=" + count + ", duration=" + duration);
        return duration;
    }
    private static void log(String msg) {
        Log.d(LOG_TAG, "[CallTime] " + msg);
    }
    private class PeriodicTimerCallback implements Runnable {
        PeriodicTimerCallback() {
        }
        public void run() {
            if (PROFILE && isTraceRunning()) {
                stopTrace();
            }
            mTimerRunning = false;
            periodicUpdateTimer();
        }
    }
    static void setTraceReady() {
        if (sProfileState == PROFILE_STATE_NONE) {
            sProfileState = PROFILE_STATE_READY;
            log("trace ready...");
        } else {
            log("current trace state = " + sProfileState);
        }
    }
    boolean isTraceReady() {
        return sProfileState == PROFILE_STATE_READY;
    }
    boolean isTraceRunning() {
        return sProfileState == PROFILE_STATE_RUNNING;
    }
    void startTrace() {
        if (PROFILE & sProfileState == PROFILE_STATE_READY) {
            File file = PhoneApp.getInstance().getDir ("phoneTrace", Context.MODE_PRIVATE);
            if (file.exists() == false) {
                file.mkdirs();
            }
            String baseName = file.getPath() + File.separator + "callstate";
            String dataFile = baseName + ".data";
            String keyFile = baseName + ".key";
            file = new File(dataFile);
            if (file.exists() == true) {
                file.delete();
            }
            file = new File(keyFile);
            if (file.exists() == true) {
                file.delete();
            }
            sProfileState = PROFILE_STATE_RUNNING;
            log("startTrace");
            Debug.startMethodTracing(baseName, 8 * 1024 * 1024);
        }
    }
    void stopTrace() {
        if (PROFILE) {
            if (sProfileState == PROFILE_STATE_RUNNING) {
                sProfileState = PROFILE_STATE_NONE;
                log("stopTrace");
                Debug.stopMethodTracing();
            }
        }
    }
}
