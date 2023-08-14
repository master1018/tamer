public class Profiler {
    private static final String LOG_TAG = PhoneApp.LOG_TAG;
    private static final boolean PROFILE = false;
    static long sTimeCallScreenRequested;
    static long sTimeCallScreenOnCreate;
    static long sTimeCallScreenCreated;
    static long sTimeIncomingCallPanelRequested;
    static long sTimeIncomingCallPanelOnCreate;
    static long sTimeIncomingCallPanelCreated;
    private Profiler() {
    }
    static void profileViewCreate(Window win, String tag) {
        if (false) {
            ViewParent p = (ViewParent) win.getDecorView();
            while (p instanceof View) {
                p = ((View) p).getParent();
            }
        }
    }
    static void callScreenRequested() {
        if (PROFILE) {
            sTimeCallScreenRequested = SystemClock.uptimeMillis();
        }
    }
    static void callScreenOnCreate() {
        if (PROFILE) {
            sTimeCallScreenOnCreate = SystemClock.uptimeMillis();
        }
    }
    static void callScreenCreated() {
        if (PROFILE) {
            sTimeCallScreenCreated = SystemClock.uptimeMillis();
            dumpCallScreenStat();
        }
    }
    private static void dumpCallScreenStat() {
        if (PROFILE) {
            log(">>> call screen perf stats <<<");
            log(">>> request -> onCreate = " +
                    (sTimeCallScreenOnCreate - sTimeCallScreenRequested));
            log(">>> onCreate -> created = " +
                    (sTimeCallScreenCreated - sTimeCallScreenOnCreate));
        }
    }
    static void incomingCallPanelRequested() {
        if (PROFILE) {
            sTimeIncomingCallPanelRequested = SystemClock.uptimeMillis();
        }
    }
    static void incomingCallPanelOnCreate() {
        if (PROFILE) {
            sTimeIncomingCallPanelOnCreate = SystemClock.uptimeMillis();
        }
    }
    static void incomingCallPanelCreated() {
        if (PROFILE) {
            sTimeIncomingCallPanelCreated = SystemClock.uptimeMillis();
            dumpIncomingCallPanelStat();
        }
    }
    private static void dumpIncomingCallPanelStat() {
        if (PROFILE) {
            log(">>> incoming call panel perf stats <<<");
            log(">>> request -> onCreate = " +
                    (sTimeIncomingCallPanelOnCreate - sTimeIncomingCallPanelRequested));
            log(">>> onCreate -> created = " +
                    (sTimeIncomingCallPanelCreated - sTimeIncomingCallPanelOnCreate));
        }
    }
    private static void log(String msg) {
        Log.d(LOG_TAG, "[Profiler] " + msg);
    }
}
