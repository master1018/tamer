public class PicasaReceiver extends BroadcastReceiver {
    private static final String TAG = "PicasaRecevier";
    @Override
    public void onReceive(Context context, Intent intent) {
        Log.e(TAG, "Accounts changed: " + intent);
    }
}
