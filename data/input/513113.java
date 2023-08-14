public final class BluetoothAdapter {
    private static final String TAG = "BluetoothAdapter";
    private static final boolean DBG = false;
    public static final int ERROR = Integer.MIN_VALUE;
    @SdkConstant(SdkConstantType.BROADCAST_INTENT_ACTION)
    public static final String ACTION_STATE_CHANGED =
            "android.bluetooth.adapter.action.STATE_CHANGED";
    public static final String EXTRA_STATE =
            "android.bluetooth.adapter.extra.STATE";
    public static final String EXTRA_PREVIOUS_STATE =
            "android.bluetooth.adapter.extra.PREVIOUS_STATE";
    public static final int STATE_OFF = 10;
    public static final int STATE_TURNING_ON = 11;
    public static final int STATE_ON = 12;
    public static final int STATE_TURNING_OFF = 13;
    @SdkConstant(SdkConstantType.ACTIVITY_INTENT_ACTION)
    public static final String ACTION_REQUEST_DISCOVERABLE =
            "android.bluetooth.adapter.action.REQUEST_DISCOVERABLE";
    public static final String EXTRA_DISCOVERABLE_DURATION =
            "android.bluetooth.adapter.extra.DISCOVERABLE_DURATION";
    @SdkConstant(SdkConstantType.ACTIVITY_INTENT_ACTION)
    public static final String ACTION_REQUEST_ENABLE =
            "android.bluetooth.adapter.action.REQUEST_ENABLE";
    @SdkConstant(SdkConstantType.BROADCAST_INTENT_ACTION)
    public static final String ACTION_SCAN_MODE_CHANGED =
            "android.bluetooth.adapter.action.SCAN_MODE_CHANGED";
    public static final String EXTRA_SCAN_MODE = "android.bluetooth.adapter.extra.SCAN_MODE";
    public static final String EXTRA_PREVIOUS_SCAN_MODE =
            "android.bluetooth.adapter.extra.PREVIOUS_SCAN_MODE";
    public static final int SCAN_MODE_NONE = 20;
    public static final int SCAN_MODE_CONNECTABLE = 21;
    public static final int SCAN_MODE_CONNECTABLE_DISCOVERABLE = 23;
    @SdkConstant(SdkConstantType.BROADCAST_INTENT_ACTION)
    public static final String ACTION_DISCOVERY_STARTED =
            "android.bluetooth.adapter.action.DISCOVERY_STARTED";
    @SdkConstant(SdkConstantType.BROADCAST_INTENT_ACTION)
    public static final String ACTION_DISCOVERY_FINISHED =
            "android.bluetooth.adapter.action.DISCOVERY_FINISHED";
    @SdkConstant(SdkConstantType.BROADCAST_INTENT_ACTION)
    public static final String ACTION_LOCAL_NAME_CHANGED =
            "android.bluetooth.adapter.action.LOCAL_NAME_CHANGED";
    public static final String EXTRA_LOCAL_NAME = "android.bluetooth.adapter.extra.LOCAL_NAME";
    public static final String BLUETOOTH_SERVICE = "bluetooth";
    private static final int ADDRESS_LENGTH = 17;
    private static BluetoothAdapter sAdapter;
    private final IBluetooth mService;
    public static synchronized BluetoothAdapter getDefaultAdapter() {
        if (sAdapter == null) {
            IBinder b = ServiceManager.getService(BluetoothAdapter.BLUETOOTH_SERVICE);
            if (b != null) {
                IBluetooth service = IBluetooth.Stub.asInterface(b);
                sAdapter = new BluetoothAdapter(service);
            }
        }
        return sAdapter;
    }
    public BluetoothAdapter(IBluetooth service) {
        if (service == null) {
            throw new IllegalArgumentException("service is null");
        }
        mService = service;
    }
    public BluetoothDevice getRemoteDevice(String address) {
        return new BluetoothDevice(address);
    }
    public boolean isEnabled() {
        try {
            return mService.isEnabled();
        } catch (RemoteException e) {Log.e(TAG, "", e);}
        return false;
    }
    public int getState() {
        try {
            return mService.getBluetoothState();
        } catch (RemoteException e) {Log.e(TAG, "", e);}
        return STATE_OFF;
    }
    public boolean enable() {
        try {
            return mService.enable();
        } catch (RemoteException e) {Log.e(TAG, "", e);}
        return false;
    }
    public boolean disable() {
        try {
            return mService.disable(true);
        } catch (RemoteException e) {Log.e(TAG, "", e);}
        return false;
    }
    public String getAddress() {
        try {
            return mService.getAddress();
        } catch (RemoteException e) {Log.e(TAG, "", e);}
        return null;
    }
    public String getName() {
        try {
            return mService.getName();
        } catch (RemoteException e) {Log.e(TAG, "", e);}
        return null;
    }
    public boolean setName(String name) {
        try {
            return mService.setName(name);
        } catch (RemoteException e) {Log.e(TAG, "", e);}
        return false;
    }
    public int getScanMode() {
        try {
            return mService.getScanMode();
        } catch (RemoteException e) {Log.e(TAG, "", e);}
        return SCAN_MODE_NONE;
    }
    public boolean setScanMode(int mode, int duration) {
        try {
            return mService.setScanMode(mode, duration);
        } catch (RemoteException e) {Log.e(TAG, "", e);}
        return false;
    }
    public boolean setScanMode(int mode) {
        return setScanMode(mode, 120);
    }
    public int getDiscoverableTimeout() {
        try {
            return mService.getDiscoverableTimeout();
        } catch (RemoteException e) {Log.e(TAG, "", e);}
        return -1;
    }
    public void setDiscoverableTimeout(int timeout) {
        try {
            mService.setDiscoverableTimeout(timeout);
        } catch (RemoteException e) {Log.e(TAG, "", e);}
    }
    public boolean startDiscovery() {
        try {
            return mService.startDiscovery();
        } catch (RemoteException e) {Log.e(TAG, "", e);}
        return false;
    }
    public boolean cancelDiscovery() {
        try {
            mService.cancelDiscovery();
        } catch (RemoteException e) {Log.e(TAG, "", e);}
        return false;
    }
    public boolean isDiscovering() {
        try {
            return mService.isDiscovering();
        } catch (RemoteException e) {Log.e(TAG, "", e);}
        return false;
    }
    public Set<BluetoothDevice> getBondedDevices() {
        try {
            return toDeviceSet(mService.listBonds());
        } catch (RemoteException e) {Log.e(TAG, "", e);}
        return null;
    }
    private static class RfcommChannelPicker {
        private static final int[] RESERVED_RFCOMM_CHANNELS =  new int[] {
            10,  
            11,  
            12,  
            19,  
        };
        private static LinkedList<Integer> sChannels;  
        private static Random sRandom;
        private final LinkedList<Integer> mChannels;  
        private final UUID mUuid;
        public RfcommChannelPicker(UUID uuid) {
            synchronized (RfcommChannelPicker.class) {
                if (sChannels == null) {
                    sChannels = new LinkedList<Integer>();
                    for (int i = 1; i <= BluetoothSocket.MAX_RFCOMM_CHANNEL; i++) {
                        sChannels.addLast(new Integer(i));
                    }
                    for (int reserved : RESERVED_RFCOMM_CHANNELS) {
                        sChannels.remove(new Integer(reserved));
                    }
                    sRandom = new Random();
                }
                mChannels = (LinkedList<Integer>)sChannels.clone();
            }
            mUuid = uuid;
        }
        public int nextChannel() {
            if (mChannels.size() == 0) {
                return -1;
            }
            return mChannels.remove(sRandom.nextInt(mChannels.size()));
        }
    }
    public BluetoothServerSocket listenUsingRfcommOn(int channel) throws IOException {
        BluetoothServerSocket socket = new BluetoothServerSocket(
                BluetoothSocket.TYPE_RFCOMM, true, true, channel);
        int errno = socket.mSocket.bindListen();
        if (errno != 0) {
            try {
                socket.close();
            } catch (IOException e) {}
            socket.mSocket.throwErrnoNative(errno);
        }
        return socket;
    }
    public BluetoothServerSocket listenUsingRfcommWithServiceRecord(String name, UUID uuid)
            throws IOException {
        RfcommChannelPicker picker = new RfcommChannelPicker(uuid);
        BluetoothServerSocket socket;
        int channel;
        int errno;
        while (true) {
            channel = picker.nextChannel();
            if (channel == -1) {
                throw new IOException("No available channels");
            }
            socket = new BluetoothServerSocket(
                    BluetoothSocket.TYPE_RFCOMM, true, true, channel);
            errno = socket.mSocket.bindListen();
            if (errno == 0) {
                if (DBG) Log.d(TAG, "listening on RFCOMM channel " + channel);
                break;  
            } else if (errno == BluetoothSocket.EADDRINUSE) {
                if (DBG) Log.d(TAG, "RFCOMM channel " + channel + " in use");
                try {
                    socket.close();
                } catch (IOException e) {}
                continue;  
            } else {
                try {
                    socket.close();
                } catch (IOException e) {}
                socket.mSocket.throwErrnoNative(errno);  
            }
        }
        int handle = -1;
        try {
            handle = mService.addRfcommServiceRecord(name, new ParcelUuid(uuid), channel,
                    new Binder());
        } catch (RemoteException e) {Log.e(TAG, "", e);}
        if (handle == -1) {
            try {
                socket.close();
            } catch (IOException e) {}
            throw new IOException("Not able to register SDP record for " + name);
        }
        socket.setCloseHandler(mHandler, handle);
        return socket;
    }
    public BluetoothServerSocket listenUsingInsecureRfcommOn(int port) throws IOException {
        BluetoothServerSocket socket = new BluetoothServerSocket(
                BluetoothSocket.TYPE_RFCOMM, false, false, port);
        int errno = socket.mSocket.bindListen();
        if (errno != 0) {
            try {
                socket.close();
            } catch (IOException e) {}
            socket.mSocket.throwErrnoNative(errno);
        }
        return socket;
    }
    public static BluetoothServerSocket listenUsingScoOn() throws IOException {
        BluetoothServerSocket socket = new BluetoothServerSocket(
                BluetoothSocket.TYPE_SCO, false, false, -1);
        int errno = socket.mSocket.bindListen();
        if (errno != 0) {
            try {
                socket.close();
            } catch (IOException e) {}
            socket.mSocket.throwErrnoNative(errno);
        }
        return socket;
    }
    private Set<BluetoothDevice> toDeviceSet(String[] addresses) {
        Set<BluetoothDevice> devices = new HashSet<BluetoothDevice>(addresses.length);
        for (int i = 0; i < addresses.length; i++) {
            devices.add(getRemoteDevice(addresses[i]));
        }
        return Collections.unmodifiableSet(devices);
    }
    private Handler mHandler = new Handler() {
        public void handleMessage(Message msg) {
            int handle = msg.what;
            try {
                if (DBG) Log.d(TAG, "Removing service record " + Integer.toHexString(handle));
                mService.removeServiceRecord(handle);
            } catch (RemoteException e) {Log.e(TAG, "", e);}
        }
    };
    public static boolean checkBluetoothAddress(String address) {
        if (address == null || address.length() != ADDRESS_LENGTH) {
            return false;
        }
        for (int i = 0; i < ADDRESS_LENGTH; i++) {
            char c = address.charAt(i);
            switch (i % 3) {
            case 0:
            case 1:
                if ((c >= '0' && c <= '9') || (c >= 'A' && c <= 'F')) {
                    break;
                }
                return false;
            case 2:
                if (c == ':') {
                    break;  
                }
                return false;
            }
        }
        return true;
    }
}
