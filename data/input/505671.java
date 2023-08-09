public class BluetoothPbapReceiver extends BroadcastReceiver {
    private static final String TAG = "BluetoothPbapReceiver";
    private static final boolean V = BluetoothPbapService.VERBOSE;
    @Override
    public void onReceive(Context context, Intent intent) {
        if (V) Log.v(TAG, "PbapReceiver onReceive: " + intent.getAction());
        Intent in = new Intent();
        in.putExtras(intent);
        in.setClass(context, BluetoothPbapService.class);
        String action = intent.getAction();
        in.putExtra("action", action);
        boolean startService = true;
        if (action.equals(BluetoothAdapter.ACTION_STATE_CHANGED)) {
            int state = intent.getIntExtra(BluetoothAdapter.EXTRA_STATE, BluetoothAdapter.ERROR);
            in.putExtra(BluetoothAdapter.EXTRA_STATE, state);
            if ((state == BluetoothAdapter.STATE_TURNING_ON)
                    || (state == BluetoothAdapter.STATE_TURNING_OFF)) {
                startService = false;
            }
        }
        if (startService) {
            context.startService(in);
        }
    }
}
