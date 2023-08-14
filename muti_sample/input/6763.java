public class StringIdCallbackReporter {
    static StringIdCallback callback = null;
    public static void tracker(String name, int id) {
        if (callback != null) {
            callback.tracker(name, id);
        }
    }
    public static void registerCallback(StringIdCallback aCallback) {
        callback = aCallback;
    }
}
