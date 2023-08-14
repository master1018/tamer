public class MasterClearReceiver extends BroadcastReceiver {
    private static final String TAG = "MasterClear";
    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent.getAction().equals(Intent.ACTION_REMOTE_INTENT)) {
            if (!"google.com".equals(intent.getStringExtra("from"))) {
                Slog.w(TAG, "Ignoring master clear request -- not from trusted server.");
                return;
            }
        }
        try {
            Slog.w(TAG, "!!! FACTORY RESET !!!");
            RecoverySystem.rebootWipeUserData(context);
            Log.wtf(TAG, "Still running after master clear?!");
        } catch (IOException e) {
            Slog.e(TAG, "Can't perform master clear/factory reset", e);
        }
    }
}
