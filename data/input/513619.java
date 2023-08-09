public final class DdmPreferences {
    public final static boolean DEFAULT_INITIAL_THREAD_UPDATE = false;
    public final static boolean DEFAULT_INITIAL_HEAP_UPDATE = false;
    public final static int DEFAULT_SELECTED_DEBUG_PORT = 8700;
    public final static int DEFAULT_DEBUG_PORT_BASE = 8600;
    public final static LogLevel DEFAULT_LOG_LEVEL = LogLevel.ERROR;
    public static final int DEFAULT_TIMEOUT = 5000; 
    private static boolean sThreadUpdate = DEFAULT_INITIAL_THREAD_UPDATE;
    private static boolean sInitialHeapUpdate = DEFAULT_INITIAL_HEAP_UPDATE;
    private static int sSelectedDebugPort = DEFAULT_SELECTED_DEBUG_PORT;
    private static int sDebugPortBase = DEFAULT_DEBUG_PORT_BASE;
    private static LogLevel sLogLevel = DEFAULT_LOG_LEVEL;
    private static int sTimeOut = DEFAULT_TIMEOUT;
    public static boolean getInitialThreadUpdate() {
        return sThreadUpdate;
    }
    public static void setInitialThreadUpdate(boolean state) {
        sThreadUpdate = state;
    }
    public static boolean getInitialHeapUpdate() {
        return sInitialHeapUpdate;
    }
    public static void setInitialHeapUpdate(boolean state) {
        sInitialHeapUpdate = state;
    }
    public static int getSelectedDebugPort() {
        return sSelectedDebugPort;
    }
    public static void setSelectedDebugPort(int port) {
        sSelectedDebugPort = port;
        MonitorThread monitorThread = MonitorThread.getInstance();
        if (monitorThread != null) {
            monitorThread.setDebugSelectedPort(port);
        }
    }
    public static int getDebugPortBase() {
        return sDebugPortBase;
    }
    public static void setDebugPortBase(int port) {
        sDebugPortBase = port;
    }
    public static LogLevel getLogLevel() {
        return sLogLevel;
    }
    public static void setLogLevel(String value) {
        sLogLevel = LogLevel.getByString(value);
        Log.setLevel(sLogLevel);
    }
    public static int getTimeOut() {
        return sTimeOut;
    }
    public static void setTimeOut(int timeOut) {
        sTimeOut = timeOut;
    }
    private DdmPreferences() {
    }
}
