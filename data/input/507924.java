public class DebugReceiver extends BroadcastReceiver {
    private static final String TAG = "BTDEBUG";
    public void onReceive(Context context, Intent intent) {
        Log.d(TAG, shorten(intent.getAction()));
        Bundle bundle = intent.getExtras();
        if (bundle == null) return;
        for (String extra : bundle.keySet()) {
            Log.d(TAG, "\t" + shorten(extra) + " = " + bundle.get(extra));
        }
    }
    private static String shorten(String action) {
        return action.replace("android", "a")
                     .replace("bluetooth", "b")
                     .replace("extra", "e")
                     .replace("action", "a");
    }
}
