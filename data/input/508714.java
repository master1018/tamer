class PerfChecker {
    private long mTime;
    private static final long mResponseThreshold = 2000;    
    public PerfChecker() {
        if (false) {
            mTime = SystemClock.uptimeMillis();
        }
    }
    public void responseAlert(String what) {
        if (false) {
            long upTime = SystemClock.uptimeMillis();
            long time =  upTime - mTime;
            if (time > mResponseThreshold) {
                Log.w("webkit", what + " used " + time + " ms");
            }
            mTime = upTime;
        }
    }
}
