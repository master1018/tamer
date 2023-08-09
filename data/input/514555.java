public final class DdmUiPreferences {
    public static final int DEFAULT_THREAD_REFRESH_INTERVAL = 4;  
    private static int sThreadRefreshInterval = DEFAULT_THREAD_REFRESH_INTERVAL;
    private static IPreferenceStore mStore;
    private static String sSymbolLocation =""; 
    private static String sAddr2LineLocation =""; 
    private static String sTraceviewLocation =""; 
    public static void setStore(IPreferenceStore store) {
        mStore = store;
    }
    public static IPreferenceStore getStore() {
        return mStore;
    }
    public static int getThreadRefreshInterval() {
        return sThreadRefreshInterval;
    }
    public static void setThreadRefreshInterval(int port) {
        sThreadRefreshInterval = port;
    }
    static String getSymbolDirectory() {
        return sSymbolLocation;
    }
    public static void setSymbolsLocation(String location) {
        sSymbolLocation = location;
    }
    static String getAddr2Line() {
        return sAddr2LineLocation;
    }
    public static void setAddr2LineLocation(String location) {
        sAddr2LineLocation = location;
    }
    public static String getTraceview() {
        return sTraceviewLocation;
    }
    public static void setTraceviewLocation(String location) {
        sTraceviewLocation = location;
    }
}
