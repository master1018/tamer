public final class BluetoothDevice implements Parcelable {
    private static final String TAG = "BluetoothDevice";
    public static final int ERROR = Integer.MIN_VALUE;
    @SdkConstant(SdkConstantType.BROADCAST_INTENT_ACTION)
    public static final String ACTION_FOUND =
            "android.bluetooth.device.action.FOUND";
    @SdkConstant(SdkConstantType.BROADCAST_INTENT_ACTION)
    public static final String ACTION_DISAPPEARED =
            "android.bluetooth.device.action.DISAPPEARED";
    @SdkConstant(SdkConstantType.BROADCAST_INTENT_ACTION)
    public static final String ACTION_CLASS_CHANGED =
            "android.bluetooth.device.action.CLASS_CHANGED";
    @SdkConstant(SdkConstantType.BROADCAST_INTENT_ACTION)
    public static final String ACTION_ACL_CONNECTED =
            "android.bluetooth.device.action.ACL_CONNECTED";
    @SdkConstant(SdkConstantType.BROADCAST_INTENT_ACTION)
    public static final String ACTION_ACL_DISCONNECT_REQUESTED =
            "android.bluetooth.device.action.ACL_DISCONNECT_REQUESTED";
    @SdkConstant(SdkConstantType.BROADCAST_INTENT_ACTION)
    public static final String ACTION_ACL_DISCONNECTED =
            "android.bluetooth.device.action.ACL_DISCONNECTED";
    @SdkConstant(SdkConstantType.BROADCAST_INTENT_ACTION)
    public static final String ACTION_NAME_CHANGED =
            "android.bluetooth.device.action.NAME_CHANGED";
    @SdkConstant(SdkConstantType.BROADCAST_INTENT_ACTION)
    public static final String ACTION_BOND_STATE_CHANGED =
            "android.bluetooth.device.action.BOND_STATE_CHANGED";
    public static final String EXTRA_DEVICE = "android.bluetooth.device.extra.DEVICE";
    public static final String EXTRA_NAME = "android.bluetooth.device.extra.NAME";
    public static final String EXTRA_RSSI = "android.bluetooth.device.extra.RSSI";
    public static final String EXTRA_CLASS = "android.bluetooth.device.extra.CLASS";
    public static final String EXTRA_BOND_STATE = "android.bluetooth.device.extra.BOND_STATE";
    public static final String EXTRA_PREVIOUS_BOND_STATE =
            "android.bluetooth.device.extra.PREVIOUS_BOND_STATE";
    public static final int BOND_NONE = 10;
    public static final int BOND_BONDING = 11;
    public static final int BOND_BONDED = 12;
    public static final String EXTRA_REASON = "android.bluetooth.device.extra.REASON";
    public static final String EXTRA_PAIRING_VARIANT =
            "android.bluetooth.device.extra.PAIRING_VARIANT";
    public static final String EXTRA_PASSKEY = "android.bluetooth.device.extra.PASSKEY";
    @SdkConstant(SdkConstantType.BROADCAST_INTENT_ACTION)
    public static final String ACTION_UUID =
            "android.bleutooth.device.action.UUID";
    @SdkConstant(SdkConstantType.BROADCAST_INTENT_ACTION)
    public static final String ACTION_NAME_FAILED =
            "android.bluetooth.device.action.NAME_FAILED";
    @SdkConstant(SdkConstantType.BROADCAST_INTENT_ACTION)
    public static final String ACTION_PAIRING_REQUEST =
            "android.bluetooth.device.action.PAIRING_REQUEST";
    @SdkConstant(SdkConstantType.BROADCAST_INTENT_ACTION)
    public static final String ACTION_PAIRING_CANCEL =
            "android.bluetooth.device.action.PAIRING_CANCEL";
    public static final int BOND_SUCCESS = 0;
    public static final int UNBOND_REASON_AUTH_FAILED = 1;
    public static final int UNBOND_REASON_AUTH_REJECTED = 2;
    public static final int UNBOND_REASON_AUTH_CANCELED = 3;
    public static final int UNBOND_REASON_REMOTE_DEVICE_DOWN = 4;
    public static final int UNBOND_REASON_DISCOVERY_IN_PROGRESS = 5;
    public static final int UNBOND_REASON_AUTH_TIMEOUT = 6;
    public static final int UNBOND_REASON_REPEATED_ATTEMPTS = 7;
    public static final int UNBOND_REASON_REMOTE_AUTH_CANCELED = 8;
    public static final int UNBOND_REASON_REMOVED = 9;
    public static final int PAIRING_VARIANT_PIN = 0;
    public static final int PAIRING_VARIANT_PASSKEY = 1;
    public static final int PAIRING_VARIANT_PASSKEY_CONFIRMATION = 2;
    public static final int PAIRING_VARIANT_CONSENT = 3;
    public static final int PAIRING_VARIANT_DISPLAY_PASSKEY = 4;
    public static final String EXTRA_UUID = "android.bluetooth.device.extra.UUID";
    private static IBluetooth sService;
    private final String mAddress;
     static IBluetooth getService() {
        synchronized (BluetoothDevice.class) {
            if (sService == null) {
                IBinder b = ServiceManager.getService(BluetoothAdapter.BLUETOOTH_SERVICE);
                if (b == null) {
                    throw new RuntimeException("Bluetooth service not available");
                }
                sService = IBluetooth.Stub.asInterface(b);
            }
        }
        return sService;
    }
     BluetoothDevice(String address) {
        getService();  
        if (!BluetoothAdapter.checkBluetoothAddress(address)) {
            throw new IllegalArgumentException(address + " is not a valid Bluetooth address");
        }
        mAddress = address;
    }
    @Override
    public boolean equals(Object o) {
        if (o instanceof BluetoothDevice) {
            return mAddress.equals(((BluetoothDevice)o).getAddress());
        }
        return false;
    }
    @Override
    public int hashCode() {
        return mAddress.hashCode();
    }
    @Override
    public String toString() {
        return mAddress;
    }
    public int describeContents() {
        return 0;
    }
    public static final Parcelable.Creator<BluetoothDevice> CREATOR =
            new Parcelable.Creator<BluetoothDevice>() {
        public BluetoothDevice createFromParcel(Parcel in) {
            return new BluetoothDevice(in.readString());
        }
        public BluetoothDevice[] newArray(int size) {
            return new BluetoothDevice[size];
        }
    };
    public void writeToParcel(Parcel out, int flags) {
        out.writeString(mAddress);
    }
    public String getAddress() {
        return mAddress;
    }
    public String getName() {
        try {
            return sService.getRemoteName(mAddress);
        } catch (RemoteException e) {Log.e(TAG, "", e);}
        return null;
    }
    public boolean createBond() {
        try {
            return sService.createBond(mAddress);
        } catch (RemoteException e) {Log.e(TAG, "", e);}
        return false;
    }
    public boolean cancelBondProcess() {
        try {
            return sService.cancelBondProcess(mAddress);
        } catch (RemoteException e) {Log.e(TAG, "", e);}
        return false;
    }
    public boolean removeBond() {
        try {
            return sService.removeBond(mAddress);
        } catch (RemoteException e) {Log.e(TAG, "", e);}
        return false;
    }
    public int getBondState() {
        try {
            return sService.getBondState(mAddress);
        } catch (RemoteException e) {Log.e(TAG, "", e);}
        return BOND_NONE;
    }
    public BluetoothClass getBluetoothClass() {
        try {
            int classInt = sService.getRemoteClass(mAddress);
            if (classInt == BluetoothClass.ERROR) return null;
            return new BluetoothClass(classInt);
        } catch (RemoteException e) {Log.e(TAG, "", e);}
        return null;
    }
    public boolean getTrustState() {
        try {
            return sService.getTrustState(mAddress);
        } catch (RemoteException e) {
            Log.e(TAG, "", e);
        }
        return false;
    }
    public boolean setTrust(boolean value) {
        try {
            return sService.setTrust(mAddress, value);
        } catch (RemoteException e) {
            Log.e(TAG, "", e);
        }
        return false;
    }
     public ParcelUuid[] getUuids() {
        try {
            return sService.getRemoteUuids(mAddress);
        } catch (RemoteException e) {Log.e(TAG, "", e);}
        return null;
    }
     public boolean fetchUuidsWithSdp() {
        try {
            return sService.fetchRemoteUuids(mAddress, null, null);
        } catch (RemoteException e) {Log.e(TAG, "", e);}
        return false;
    }
    public int getServiceChannel(ParcelUuid uuid) {
         try {
             return sService.getRemoteServiceChannel(mAddress, uuid);
         } catch (RemoteException e) {Log.e(TAG, "", e);}
         return BluetoothDevice.ERROR;
    }
    public boolean setPin(byte[] pin) {
        try {
            return sService.setPin(mAddress, pin);
        } catch (RemoteException e) {Log.e(TAG, "", e);}
        return false;
    }
    public boolean setPasskey(int passkey) {
        try {
            return sService.setPasskey(mAddress, passkey);
        } catch (RemoteException e) {Log.e(TAG, "", e);}
        return false;
    }
    public boolean setPairingConfirmation(boolean confirm) {
        try {
            return sService.setPairingConfirmation(mAddress, confirm);
        } catch (RemoteException e) {Log.e(TAG, "", e);}
        return false;
    }
    public boolean cancelPairingUserInput() {
        try {
            return sService.cancelPairingUserInput(mAddress);
        } catch (RemoteException e) {Log.e(TAG, "", e);}
        return false;
    }
    public boolean isBluetoothDock() {
        try {
            return sService.isBluetoothDock(mAddress);
        } catch (RemoteException e) {Log.e(TAG, "", e);}
        return false;
    }
    public BluetoothSocket createRfcommSocket(int channel) throws IOException {
        return new BluetoothSocket(BluetoothSocket.TYPE_RFCOMM, -1, true, true, this, channel,
                null);
    }
    public BluetoothSocket createRfcommSocketToServiceRecord(UUID uuid) throws IOException {
        return new BluetoothSocket(BluetoothSocket.TYPE_RFCOMM, -1, true, true, this, -1,
                new ParcelUuid(uuid));
    }
    public BluetoothSocket createInsecureRfcommSocket(int port) throws IOException {
        return new BluetoothSocket(BluetoothSocket.TYPE_RFCOMM, -1, false, false, this, port,
                null);
    }
    public BluetoothSocket createScoSocket() throws IOException {
        return new BluetoothSocket(BluetoothSocket.TYPE_SCO, -1, true, true, this, -1, null);
    }
    public static byte[] convertPinToBytes(String pin) {
        if (pin == null) {
            return null;
        }
        byte[] pinBytes;
        try {
            pinBytes = pin.getBytes("UTF8");
        } catch (UnsupportedEncodingException uee) {
            Log.e(TAG, "UTF8 not supported?!?");  
            return null;
        }
        if (pinBytes.length <= 0 || pinBytes.length > 16) {
            return null;
        }
        return pinBytes;
    }
}
