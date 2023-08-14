public class BluetoothPairingRequest extends BroadcastReceiver {
    public static final int NOTIFICATION_ID = android.R.drawable.stat_sys_data_bluetooth;
    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();
        if (action.equals(BluetoothDevice.ACTION_PAIRING_REQUEST)) {
            LocalBluetoothManager localManager = LocalBluetoothManager.getInstance(context);
            BluetoothDevice device =
                    intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
            int type = intent.getIntExtra(BluetoothDevice.EXTRA_PAIRING_VARIANT,
                    BluetoothDevice.ERROR);
            Intent pairingIntent = new Intent();
            pairingIntent.setClass(context, BluetoothPairingDialog.class);
            pairingIntent.putExtra(BluetoothDevice.EXTRA_DEVICE, device);
            pairingIntent.putExtra(BluetoothDevice.EXTRA_PAIRING_VARIANT, type);
            if (type == BluetoothDevice.PAIRING_VARIANT_PASSKEY_CONFIRMATION ||
                    type == BluetoothDevice.PAIRING_VARIANT_DISPLAY_PASSKEY) {
                int passkey = intent.getIntExtra(BluetoothDevice.EXTRA_PASSKEY, BluetoothDevice.ERROR);
                pairingIntent.putExtra(BluetoothDevice.EXTRA_PASSKEY, passkey);
            }
            pairingIntent.setAction(BluetoothDevice.ACTION_PAIRING_REQUEST);
            pairingIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            String deviceAddress = device != null ? device.getAddress() : null;
            if (localManager.shouldShowDialogInForeground(deviceAddress)) {
                context.startActivity(pairingIntent);
            } else {
                Resources res = context.getResources();
                Notification notification = new Notification(
                        android.R.drawable.stat_sys_data_bluetooth,
                        res.getString(R.string.bluetooth_notif_ticker),
                        System.currentTimeMillis());
                PendingIntent pending = PendingIntent.getActivity(context, 0,
                        pairingIntent, PendingIntent.FLAG_ONE_SHOT);
                String name = intent.getStringExtra(BluetoothDevice.EXTRA_NAME);
                if (TextUtils.isEmpty(name)) {
                    name = device.getName();
                }
                notification.setLatestEventInfo(context,
                        res.getString(R.string.bluetooth_notif_title),
                        res.getString(R.string.bluetooth_notif_message) + name,
                        pending);
                notification.flags |= Notification.FLAG_AUTO_CANCEL;
                NotificationManager manager = (NotificationManager)
                        context.getSystemService(Context.NOTIFICATION_SERVICE);
                manager.notify(NOTIFICATION_ID, notification);
            }
        } else if (action.equals(BluetoothDevice.ACTION_PAIRING_CANCEL)) {
            NotificationManager manager = (NotificationManager) context
                    .getSystemService(Context.NOTIFICATION_SERVICE);
            manager.cancel(NOTIFICATION_ID);
        }
    }
}
