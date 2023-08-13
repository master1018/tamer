public class AppWidgetShared {
    static final String TAG = "AppWidgetShared";
    static final boolean LOGD = false;
    static Object sLock = new Object();
    static WakeLock sWakeLock;
    static boolean sUpdateRequested = false;
    static boolean sUpdateRunning = false;
    static long sLastRequest = -1;
    private static HashSet<Integer> sAppWidgetIds = new HashSet<Integer>();
    private static HashSet<Long> sChangedEventIds = new HashSet<Long>();
    static void mergeAppWidgetIdsLocked(int[] appWidgetIds) {
        if (appWidgetIds != null) {
            for (int appWidgetId : appWidgetIds) {
                sAppWidgetIds.add(appWidgetId);
            }
        } else {
            sAppWidgetIds.clear();
        }
    }
    static void mergeChangedEventIdsLocked(long[] changedEventIds) {
        if (changedEventIds != null) {
            for (long changedEventId : changedEventIds) {
                sChangedEventIds.add(changedEventId);
            }
        } else {
            sChangedEventIds.clear();
        }
    }
    static int[] collectAppWidgetIdsLocked() {
        final int size = sAppWidgetIds.size();
        int[] array = new int[size];
        Iterator<Integer> iterator = sAppWidgetIds.iterator();
        for (int i = 0; i < size; i++) {
            array[i] = iterator.next();
        }
        sAppWidgetIds.clear();
        return array;
    }
    static Set<Long> collectChangedEventIdsLocked() {
        Set<Long> set = new HashSet<Long>();
        for (Long value : sChangedEventIds) {
            set.add(value);
        }
        sChangedEventIds.clear();
        return set;
    }
    static void clearLocked() {
        if (sWakeLock != null && sWakeLock.isHeld()) {
            if (LOGD) Log.d(TAG, "found held wakelock, so releasing");
            sWakeLock.release();
        }
        sWakeLock = null;
        sUpdateRequested = false;
        sUpdateRunning = false;
        sAppWidgetIds.clear();
        sChangedEventIds.clear();
    }
}
