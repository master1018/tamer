public class DockEventReceiver extends BroadcastReceiver {
    private static final boolean DEBUG = DockService.DEBUG;
    private static final String TAG = "DockEventReceiver";
    public static final String ACTION_DOCK_SHOW_UI =
        "com.android.settings.bluetooth.action.DOCK_SHOW_UI";
    private static final int EXTRA_INVALID = -1234;
    private static final Object mStartingServiceSync = new Object();
    private static final long WAKELOCK_TIMEOUT = 5000;
    private static PowerManager.WakeLock mStartingService;
    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent == null)
            return;
        int state = intent.getIntExtra(Intent.EXTRA_DOCK_STATE, intent.getIntExtra(
                BluetoothAdapter.EXTRA_STATE, EXTRA_INVALID));
        BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
        if (DEBUG) {
            Log.d(TAG, "Action: " + intent.getAction() + " State:" + state + " Device: "
                    + (device == null ? "null" : device.getName()));
        }
        if (Intent.ACTION_DOCK_EVENT.equals(intent.getAction())
                || ACTION_DOCK_SHOW_UI.endsWith(intent.getAction())) {
            if (device == null) {
                if (DEBUG) Log.d(TAG, "Device is missing");
                return;
            }
            switch (state) {
                case Intent.EXTRA_DOCK_STATE_UNDOCKED:
                case Intent.EXTRA_DOCK_STATE_CAR:
                case Intent.EXTRA_DOCK_STATE_DESK:
                    Intent i = new Intent(intent);
                    i.setClass(context, DockService.class);
                    beginStartingService(context, i);
                    break;
                default:
                    if (DEBUG) Log.e(TAG, "Unknown state");
                    break;
            }
        } else if (BluetoothHeadset.ACTION_STATE_CHANGED.equals(intent.getAction())) {
            if (device == null) {
                if (DEBUG) Log.d(TAG, "Device is missing");
                return;
            }
            int newState = intent.getIntExtra(BluetoothHeadset.EXTRA_STATE,
                    BluetoothHeadset.STATE_CONNECTED);
            if (newState != BluetoothHeadset.STATE_DISCONNECTED) return;
            int source = intent.getIntExtra(BluetoothHeadset.EXTRA_DISCONNECT_INITIATOR,
                    BluetoothHeadset.LOCAL_DISCONNECT);
            if (source != BluetoothHeadset.REMOTE_DISCONNECT) return;
            Intent i = new Intent(intent);
            i.setClass(context, DockService.class);
            beginStartingService(context, i);
        } else if (BluetoothA2dp.ACTION_SINK_STATE_CHANGED.equals(intent.getAction())) {
            if (device == null) {
                if (DEBUG) Log.d(TAG, "Device is missing");
                return;
            }
            int newState = intent.getIntExtra(BluetoothA2dp.EXTRA_SINK_STATE, 0);
            int oldState = intent.getIntExtra(BluetoothA2dp.EXTRA_PREVIOUS_SINK_STATE, 0);
            if (newState == BluetoothA2dp.STATE_DISCONNECTED &&
                    oldState != BluetoothA2dp.STATE_DISCONNECTING) {
                Intent i = new Intent(intent);
                i.setClass(context, DockService.class);
                beginStartingService(context, i);
            }
        } else if (BluetoothAdapter.ACTION_STATE_CHANGED.equals(intent.getAction())) {
            int btState = intent.getIntExtra(BluetoothAdapter.EXTRA_STATE, BluetoothAdapter.ERROR);
            if (btState != BluetoothAdapter.STATE_TURNING_ON) {
                Intent i = new Intent(intent);
                i.setClass(context, DockService.class);
                beginStartingService(context, i);
            }
        }
    }
    public static void beginStartingService(Context context, Intent intent) {
        synchronized (mStartingServiceSync) {
            if (mStartingService == null) {
                PowerManager pm = (PowerManager) context.getSystemService(Context.POWER_SERVICE);
                mStartingService = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK,
                        "StartingDockService");
            }
            mStartingService.acquire(WAKELOCK_TIMEOUT);
            if (context.startService(intent) == null) {
                Log.e(TAG, "Can't start DockService");
            }
        }
    }
    public static void finishStartingService(Service service, int startId) {
        synchronized (mStartingServiceSync) {
            if (mStartingService != null) {
                if (DEBUG) Log.d(TAG, "stopSelf id = "+ startId);
                service.stopSelfResult(startId);
            }
        }
    }
}
