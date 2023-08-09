public class SettingsBtStatus {
    private static final String TAG = "SettingsBtStatus";
    public static final int CONNECTION_STATUS_UNKNOWN = 0;
    public static final int CONNECTION_STATUS_ACTIVE = 1;
    public static final int CONNECTION_STATUS_CONNECTED = 2;
    public static final int CONNECTION_STATUS_CONNECTING = 3;
    public static final int CONNECTION_STATUS_DISCONNECTED = 4;
    public static final int CONNECTION_STATUS_DISCONNECTING = 5;
    public static final int getConnectionStatusSummary(int connectionStatus) {
        switch (connectionStatus) {
        case CONNECTION_STATUS_ACTIVE:
            return R.string.bluetooth_connected;
        case CONNECTION_STATUS_CONNECTED:
            return R.string.bluetooth_connected;
        case CONNECTION_STATUS_CONNECTING:
            return R.string.bluetooth_connecting;
        case CONNECTION_STATUS_DISCONNECTED:
            return R.string.bluetooth_disconnected;
        case CONNECTION_STATUS_DISCONNECTING:
            return R.string.bluetooth_disconnecting;
        case CONNECTION_STATUS_UNKNOWN:
            return R.string.bluetooth_unknown;
        default:
            return 0;
        }
    }
    public static final boolean isConnectionStatusConnected(int connectionStatus) {
        return connectionStatus == CONNECTION_STATUS_ACTIVE
                || connectionStatus == CONNECTION_STATUS_CONNECTED;
    }
    public static final boolean isConnectionStatusBusy(int connectionStatus) {
        return connectionStatus == CONNECTION_STATUS_CONNECTING
                || connectionStatus == CONNECTION_STATUS_DISCONNECTING;
    }
    public static final int getPairingStatusSummary(int bondState) {
        switch (bondState) {
        case BluetoothDevice.BOND_BONDED:
            return R.string.bluetooth_paired;
        case BluetoothDevice.BOND_BONDING:
            return R.string.bluetooth_pairing;
        case BluetoothDevice.BOND_NONE:
            return R.string.bluetooth_not_connected;
        default:
            return 0;
        }
    }
}
