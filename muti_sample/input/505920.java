public class ImServiceAutoStarter extends BroadcastReceiver {
    static final String TAG = "ImServiceAutoStarter";
    @Override
    public void onReceive(Context context, Intent intent) {
        Log.d(TAG, "onReceiveIntent");
        String selection = Imps.Account.KEEP_SIGNED_IN + "=1 AND "
                + Imps.Account.ACTIVE + "=1";
        Cursor cursor = context.getContentResolver().query(Imps.Account.CONTENT_URI,
                new String[]{Imps.Account._ID}, selection, null, null);
        if (cursor != null) {
            if (cursor.getCount() > 0) {
                Log.d(TAG, "start service");
                Intent serviceIntent = new Intent();
                serviceIntent.setComponent(ImServiceConstants.IM_SERVICE_COMPONENT);
                serviceIntent.putExtra(ImServiceConstants.EXTRA_CHECK_AUTO_LOGIN, true);
                context.startService(serviceIntent);
            }
            cursor.close();
        }
    }
}
