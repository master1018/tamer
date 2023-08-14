public abstract class StkLog {
    static final boolean DEBUG = true;
    public static void d(Object caller, String msg) {
        if (!DEBUG) {
            return;
        }
        String className = caller.getClass().getName();
        Log.d("STK", className.substring(className.lastIndexOf('.') + 1) + ": "
                + msg);
    }
    public static void d(String caller, String msg) {
        if (!DEBUG) {
            return;
        }
        Log.d("STK", caller + ": " + msg);
    }
}
