public class BinderInternal {
    static WeakReference<GcWatcher> mGcWatcher
            = new WeakReference<GcWatcher>(new GcWatcher());
    static long mLastGcTime;
    static final class GcWatcher {
        @Override
        protected void finalize() throws Throwable {
            handleGc();
            mLastGcTime = SystemClock.uptimeMillis();
            mGcWatcher = new WeakReference<GcWatcher>(new GcWatcher());
        }
    }
    public static final native void joinThreadPool();
    public static long getLastGcTime() {
        return mLastGcTime;
    }
    public static final native IBinder getContextObject();
    public static final native void disableBackgroundScheduling(boolean disable);
    static native final void handleGc();
    public static void forceGc(String reason) {
        EventLog.writeEvent(2741, reason);
        Runtime.getRuntime().gc();
    }
    static void forceBinderGc() {
        forceGc("Binder");
    }
}
