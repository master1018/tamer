public class BluetoothService extends IBluetooth.Stub {
    private static final String TAG = "BluetoothService";
    private static final boolean DBG = true;
    private int mNativeData;
    private BluetoothEventLoop mEventLoop;
    private boolean mIsAirplaneSensitive;
    private boolean mIsAirplaneToggleable;
    private int mBluetoothState;
    private boolean mRestart = false;  
    private boolean mIsDiscovering;
    private BluetoothAdapter mAdapter;  
    private final BondState mBondState = new BondState();  
    private final IBatteryStats mBatteryStats;
    private final Context mContext;
    private static final String BLUETOOTH_ADMIN_PERM = android.Manifest.permission.BLUETOOTH_ADMIN;
    private static final String BLUETOOTH_PERM = android.Manifest.permission.BLUETOOTH;
    private static final String DOCK_ADDRESS_PATH = "/sys/class/switch/dock/bt_addr";
    private static final String DOCK_PIN_PATH = "/sys/class/switch/dock/bt_pin";
    private static final String SHARED_PREFERENCE_DOCK_ADDRESS = "dock_bluetooth_address";
    private static final String SHARED_PREFERENCES_NAME = "bluetooth_service_settings";
    private static final int MESSAGE_REGISTER_SDP_RECORDS = 1;
    private static final int MESSAGE_FINISH_DISABLE = 2;
    private static final int MESSAGE_UUID_INTENT = 3;
    private static final int MESSAGE_DISCOVERABLE_TIMEOUT = 4;
    private static final int UUID_INTENT_DELAY = 6000;
    private static final ParcelUuid[] RFCOMM_UUIDS = {
            BluetoothUuid.Handsfree,
            BluetoothUuid.HSP,
            BluetoothUuid.ObexObjectPush };
    private final Map<String, String> mAdapterProperties;
    private final HashMap<String, Map<String, String>> mDeviceProperties;
    private final HashMap<String, Map<ParcelUuid, Integer>> mDeviceServiceChannelCache;
    private final ArrayList<String> mUuidIntentTracker;
    private final HashMap<RemoteService, IBluetoothCallback> mUuidCallbackTracker;
    private final HashMap<Integer, Integer> mServiceRecordToPid;
    private static String mDockAddress;
    private String mDockPin;
    private static class RemoteService {
        public String address;
        public ParcelUuid uuid;
        public RemoteService(String address, ParcelUuid uuid) {
            this.address = address;
            this.uuid = uuid;
        }
        @Override
        public boolean equals(Object o) {
            if (o instanceof RemoteService) {
                RemoteService service = (RemoteService)o;
                return address.equals(service.address) && uuid.equals(service.uuid);
            }
            return false;
        }
        @Override
        public int hashCode() {
            int hash = 1;
            hash = hash * 31 + (address == null ? 0 : address.hashCode());
            hash = hash * 31 + (uuid == null ? 0 : uuid.hashCode());
            return hash;
        }
    }
    static {
        classInitNative();
    }
    public BluetoothService(Context context) {
        mContext = context;
        mBatteryStats = IBatteryStats.Stub.asInterface(ServiceManager.getService("batteryinfo"));
        initializeNativeDataNative();
        if (isEnabledNative() == 1) {
            Log.w(TAG, "Bluetooth daemons already running - runtime restart? ");
            disableNative();
        }
        mBluetoothState = BluetoothAdapter.STATE_OFF;
        mIsDiscovering = false;
        mAdapterProperties = new HashMap<String, String>();
        mDeviceProperties = new HashMap<String, Map<String,String>>();
        mDeviceServiceChannelCache = new HashMap<String, Map<ParcelUuid, Integer>>();
        mUuidIntentTracker = new ArrayList<String>();
        mUuidCallbackTracker = new HashMap<RemoteService, IBluetoothCallback>();
        mServiceRecordToPid = new HashMap<Integer, Integer>();
        IntentFilter filter = new IntentFilter();
        registerForAirplaneMode(filter);
        filter.addAction(Intent.ACTION_DOCK_EVENT);
        mContext.registerReceiver(mReceiver, filter);
    }
     public static synchronized String readDockBluetoothAddress() {
        if (mDockAddress != null) return mDockAddress;
        BufferedInputStream file = null;
        String dockAddress;
        try {
            file = new BufferedInputStream(new FileInputStream(DOCK_ADDRESS_PATH));
            byte[] address = new byte[17];
            file.read(address);
            dockAddress = new String(address);
            dockAddress = dockAddress.toUpperCase();
            if (BluetoothAdapter.checkBluetoothAddress(dockAddress)) {
                mDockAddress = dockAddress;
                return mDockAddress;
            } else {
                log("CheckBluetoothAddress failed for car dock address:" + dockAddress);
            }
        } catch (FileNotFoundException e) {
            log("FileNotFoundException while trying to read dock address");
        } catch (IOException e) {
            log("IOException while trying to read dock address");
        } finally {
            if (file != null) {
                try {
                    file.close();
                } catch (IOException e) {
                }
            }
        }
        mDockAddress = null;
        return null;
    }
    private synchronized boolean writeDockPin() {
        BufferedWriter out = null;
        try {
            out = new BufferedWriter(new FileWriter(DOCK_PIN_PATH));
            int pin = (int) Math.floor(Math.random() * 10000);
            mDockPin = String.format("%04d", pin);
            out.write(mDockPin);
            return true;
        } catch (FileNotFoundException e) {
            log("FileNotFoundException while trying to write dock pairing pin");
        } catch (IOException e) {
            log("IOException while while trying to write dock pairing pin");
        } finally {
            if (out != null) {
                try {
                    out.close();
                } catch (IOException e) {
                }
            }
        }
        mDockPin = null;
        return false;
    }
     synchronized String getDockPin() {
        return mDockPin;
    }
    public synchronized void initAfterRegistration() {
        mAdapter = BluetoothAdapter.getDefaultAdapter();
        mEventLoop = new BluetoothEventLoop(mContext, mAdapter, this);
    }
    @Override
    protected void finalize() throws Throwable {
        mContext.unregisterReceiver(mReceiver);
        try {
            cleanupNativeDataNative();
        } finally {
            super.finalize();
        }
    }
    public boolean isEnabled() {
        mContext.enforceCallingOrSelfPermission(BLUETOOTH_PERM, "Need BLUETOOTH permission");
        return isEnabledInternal();
    }
    private boolean isEnabledInternal() {
        return mBluetoothState == BluetoothAdapter.STATE_ON;
    }
    public int getBluetoothState() {
        mContext.enforceCallingOrSelfPermission(BLUETOOTH_PERM, "Need BLUETOOTH permission");
        return mBluetoothState;
    }
    public boolean disable() {
        return disable(true);
    }
    public synchronized boolean disable(boolean saveSetting) {
        mContext.enforceCallingOrSelfPermission(BLUETOOTH_ADMIN_PERM, "Need BLUETOOTH_ADMIN permission");
        switch (mBluetoothState) {
        case BluetoothAdapter.STATE_OFF:
            return true;
        case BluetoothAdapter.STATE_ON:
            break;
        default:
            return false;
        }
        if (mEnableThread != null && mEnableThread.isAlive()) {
            return false;
        }
        setBluetoothState(BluetoothAdapter.STATE_TURNING_OFF);
        mHandler.removeMessages(MESSAGE_REGISTER_SDP_RECORDS);
        mHandler.sendMessageDelayed(
                mHandler.obtainMessage(MESSAGE_FINISH_DISABLE, saveSetting ? 1 : 0, 0), 3000);
        return true;
    }
    private synchronized void finishDisable(boolean saveSetting) {
        if (mBluetoothState != BluetoothAdapter.STATE_TURNING_OFF) {
            return;
        }
        mEventLoop.stop();
        tearDownNativeDataNative();
        disableNative();
        for (String address : mBondState.listInState(BluetoothDevice.BOND_BONDING)) {
            mBondState.setBondState(address, BluetoothDevice.BOND_NONE,
                                    BluetoothDevice.UNBOND_REASON_AUTH_CANCELED);
        }
        Intent intent = new Intent(BluetoothAdapter.ACTION_SCAN_MODE_CHANGED);
        intent.putExtra(BluetoothAdapter.EXTRA_SCAN_MODE, BluetoothAdapter.SCAN_MODE_NONE);
        mContext.sendBroadcast(intent, BLUETOOTH_PERM);
        mIsDiscovering = false;
        mAdapterProperties.clear();
        mServiceRecordToPid.clear();
        if (saveSetting) {
            persistBluetoothOnSetting(false);
        }
        setBluetoothState(BluetoothAdapter.STATE_OFF);
        long ident = Binder.clearCallingIdentity();
        try {
            mBatteryStats.noteBluetoothOff();
        } catch (RemoteException e) {
        } finally {
            Binder.restoreCallingIdentity(ident);
        }
        if (mRestart) {
            mRestart = false;
            enable();
        }
    }
    public boolean enable() {
        return enable(true);
    }
    public synchronized boolean enable(boolean saveSetting) {
        mContext.enforceCallingOrSelfPermission(BLUETOOTH_ADMIN_PERM,
                                                "Need BLUETOOTH_ADMIN permission");
        if (mIsAirplaneSensitive && isAirplaneModeOn() && !mIsAirplaneToggleable) {
            return false;
        }
        if (mBluetoothState != BluetoothAdapter.STATE_OFF) {
            return false;
        }
        if (mEnableThread != null && mEnableThread.isAlive()) {
            return false;
        }
        setBluetoothState(BluetoothAdapter.STATE_TURNING_ON);
        mEnableThread = new EnableThread(saveSetting);
        mEnableThread.start();
        return true;
    }
     synchronized void restart() {
        if (mBluetoothState != BluetoothAdapter.STATE_ON) {
            return;
        }
        mRestart = true;
        if (!disable(false)) {
            mRestart = false;
        }
    }
    private synchronized void setBluetoothState(int state) {
        if (state == mBluetoothState) {
            return;
        }
        if (DBG) log("Bluetooth state " + mBluetoothState + " -> " + state);
        Intent intent = new Intent(BluetoothAdapter.ACTION_STATE_CHANGED);
        intent.putExtra(BluetoothAdapter.EXTRA_PREVIOUS_STATE, mBluetoothState);
        intent.putExtra(BluetoothAdapter.EXTRA_STATE, state);
        intent.addFlags(Intent.FLAG_RECEIVER_REGISTERED_ONLY_BEFORE_BOOT);
        mBluetoothState = state;
        mContext.sendBroadcast(intent, BLUETOOTH_PERM);
    }
    private final Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
            case MESSAGE_REGISTER_SDP_RECORDS:
                if (!isEnabledInternal()) {
                    return;
                }
                switch (msg.arg1) {
                case 1:
                    Log.d(TAG, "Registering hfag record");
                    SystemService.start("hfag");
                    mHandler.sendMessageDelayed(
                            mHandler.obtainMessage(MESSAGE_REGISTER_SDP_RECORDS, 2, -1), 500);
                    break;
                case 2:
                    Log.d(TAG, "Registering hsag record");
                    SystemService.start("hsag");
                    mHandler.sendMessageDelayed(
                            mHandler.obtainMessage(MESSAGE_REGISTER_SDP_RECORDS, 3, -1), 500);
                    break;
                case 3:
                    Log.d(TAG, "Registering opush record");
                    SystemService.start("opush");
                    mHandler.sendMessageDelayed(
                            mHandler.obtainMessage(MESSAGE_REGISTER_SDP_RECORDS, 4, -1), 500);
                    break;
                case 4:
                    Log.d(TAG, "Registering pbap record");
                    SystemService.start("pbap");
                    break;
                }
                break;
            case MESSAGE_FINISH_DISABLE:
                finishDisable(msg.arg1 != 0);
                break;
            case MESSAGE_UUID_INTENT:
                String address = (String)msg.obj;
                if (address != null) {
                    sendUuidIntent(address);
                    makeServiceChannelCallbacks(address);
                }
                break;
            case MESSAGE_DISCOVERABLE_TIMEOUT:
                int mode = msg.arg1;
                if (isEnabledInternal()) {
                    setScanMode(BluetoothAdapter.SCAN_MODE_CONNECTABLE, -1);
                }
                break;
            }
        }
    };
    private EnableThread mEnableThread;
    private class EnableThread extends Thread {
        private final boolean mSaveSetting;
        public EnableThread(boolean saveSetting) {
            mSaveSetting = saveSetting;
        }
        public void run() {
            boolean res = (enableNative() == 0);
            if (res) {
                int retryCount = 2;
                boolean running = false;
                while ((retryCount-- > 0) && !running) {
                    mEventLoop.start();
                    int pollCount = 5;
                    while ((pollCount-- > 0) && !running) {
                        if (mEventLoop.isEventLoopRunning()) {
                            running = true;
                            break;
                        }
                        try {
                            Thread.sleep(100);
                        } catch (InterruptedException e) {}
                    }
                }
                if (!running) {
                    log("bt EnableThread giving up");
                    res = false;
                    disableNative();
                }
            }
            if (res) {
                if (!setupNativeDataNative()) {
                    return;
                }
                if (mSaveSetting) {
                    persistBluetoothOnSetting(true);
                }
                mIsDiscovering = false;
                mBondState.readAutoPairingData();
                mBondState.loadBondState();
                mHandler.sendMessageDelayed(
                        mHandler.obtainMessage(MESSAGE_REGISTER_SDP_RECORDS, 1, -1), 3000);
                long ident = Binder.clearCallingIdentity();
                try {
                    mBatteryStats.noteBluetoothOn();
                } catch (RemoteException e) {
                } finally {
                    Binder.restoreCallingIdentity(ident);
                }
            }
            mEnableThread = null;
            setBluetoothState(res ?
                              BluetoothAdapter.STATE_ON :
                              BluetoothAdapter.STATE_OFF);
            if (res) {
                String[] propVal = {"Pairable", getProperty("Pairable")};
                mEventLoop.onPropertyChanged(propVal);
            }
            if (mIsAirplaneSensitive && isAirplaneModeOn() && !mIsAirplaneToggleable) {
                disable(false);
            }
        }
    }
    private void persistBluetoothOnSetting(boolean bluetoothOn) {
        long origCallerIdentityToken = Binder.clearCallingIdentity();
        Settings.Secure.putInt(mContext.getContentResolver(), Settings.Secure.BLUETOOTH_ON,
                bluetoothOn ? 1 : 0);
        Binder.restoreCallingIdentity(origCallerIdentityToken);
    }
     BondState getBondState() {
        return mBondState;
    }
    public class BondState {
        private final HashMap<String, Integer> mState = new HashMap<String, Integer>();
        private final HashMap<String, Integer> mPinAttempt = new HashMap<String, Integer>();
        private static final String AUTO_PAIRING_BLACKLIST =
            "/etc/bluetooth/auto_pairing.conf";
        private static final String DYNAMIC_AUTO_PAIRING_BLACKLIST =
            "/data/misc/bluetooth/dynamic_auto_pairing.conf";
        private ArrayList<String>  mAutoPairingAddressBlacklist;
        private ArrayList<String> mAutoPairingExactNameBlacklist;
        private ArrayList<String> mAutoPairingPartialNameBlacklist;
        private ArrayList<String> mAutoPairingDynamicAddressBlacklist;
        private String mPendingOutgoingBonding;
        private synchronized void setPendingOutgoingBonding(String address) {
            mPendingOutgoingBonding = address;
        }
        public synchronized String getPendingOutgoingBonding() {
            return mPendingOutgoingBonding;
        }
        public synchronized void loadBondState() {
            if (mBluetoothState != BluetoothAdapter.STATE_TURNING_ON) {
                return;
            }
            String []bonds = null;
            String val = getPropertyInternal("Devices");
            if (val != null) {
                bonds = val.split(",");
            }
            if (bonds == null) {
                return;
            }
            mState.clear();
            if (DBG) log("found " + bonds.length + " bonded devices");
            for (String device : bonds) {
                mState.put(getAddressFromObjectPath(device).toUpperCase(),
                        BluetoothDevice.BOND_BONDED);
            }
        }
        public synchronized void setBondState(String address, int state) {
            setBondState(address, state, 0);
        }
        public synchronized void setBondState(String address, int state, int reason) {
            int oldState = getBondState(address);
            if (oldState == state) {
                return;
            }
            if (oldState == BluetoothDevice.BOND_BONDING) {
                if (address.equals(mPendingOutgoingBonding)) {
                    mPendingOutgoingBonding = null;
                }
            }
            if (DBG) log(address + " bond state " + oldState + " -> " + state + " (" +
                         reason + ")");
            Intent intent = new Intent(BluetoothDevice.ACTION_BOND_STATE_CHANGED);
            intent.putExtra(BluetoothDevice.EXTRA_DEVICE, mAdapter.getRemoteDevice(address));
            intent.putExtra(BluetoothDevice.EXTRA_BOND_STATE, state);
            intent.putExtra(BluetoothDevice.EXTRA_PREVIOUS_BOND_STATE, oldState);
            if (state == BluetoothDevice.BOND_NONE) {
                if (reason <= 0) {
                    Log.w(TAG, "setBondState() called to unbond device, but reason code is " +
                          "invalid. Overriding reason code with BOND_RESULT_REMOVED");
                    reason = BluetoothDevice.UNBOND_REASON_REMOVED;
                }
                intent.putExtra(BluetoothDevice.EXTRA_REASON, reason);
                mState.remove(address);
            } else {
                mState.put(address, state);
            }
            mContext.sendBroadcast(intent, BLUETOOTH_PERM);
        }
        public boolean isAutoPairingBlacklisted(String address) {
            if (mAutoPairingAddressBlacklist != null) {
                for (String blacklistAddress : mAutoPairingAddressBlacklist) {
                    if (address.startsWith(blacklistAddress)) return true;
                }
            }
            if (mAutoPairingDynamicAddressBlacklist != null) {
                for (String blacklistAddress: mAutoPairingDynamicAddressBlacklist) {
                    if (address.equals(blacklistAddress)) return true;
                }
            }
            String name = getRemoteName(address);
            if (name != null) {
                if (mAutoPairingExactNameBlacklist != null) {
                    for (String blacklistName : mAutoPairingExactNameBlacklist) {
                        if (name.equals(blacklistName)) return true;
                    }
                }
                if (mAutoPairingPartialNameBlacklist != null) {
                    for (String blacklistName : mAutoPairingPartialNameBlacklist) {
                        if (name.startsWith(blacklistName)) return true;
                    }
                }
            }
            return false;
        }
        public synchronized int getBondState(String address) {
            Integer state = mState.get(address);
            if (state == null) {
                return BluetoothDevice.BOND_NONE;
            }
            return state.intValue();
        }
         synchronized String[] listInState(int state) {
            ArrayList<String> result = new ArrayList<String>(mState.size());
            for (Map.Entry<String, Integer> e : mState.entrySet()) {
                if (e.getValue().intValue() == state) {
                    result.add(e.getKey());
                }
            }
            return result.toArray(new String[result.size()]);
        }
        public synchronized void addAutoPairingFailure(String address) {
            if (mAutoPairingDynamicAddressBlacklist == null) {
                mAutoPairingDynamicAddressBlacklist = new ArrayList<String>();
            }
            updateAutoPairingData(address);
            mAutoPairingDynamicAddressBlacklist.add(address);
        }
        public synchronized boolean isAutoPairingAttemptsInProgress(String address) {
            return getAttempt(address) != 0;
        }
        public synchronized void clearPinAttempts(String address) {
            mPinAttempt.remove(address);
        }
        public synchronized boolean hasAutoPairingFailed(String address) {
            if (mAutoPairingDynamicAddressBlacklist == null) return false;
            return mAutoPairingDynamicAddressBlacklist.contains(address);
        }
        public synchronized int getAttempt(String address) {
            Integer attempt = mPinAttempt.get(address);
            if (attempt == null) {
                return 0;
            }
            return attempt.intValue();
        }
        public synchronized void attempt(String address) {
            Integer attempt = mPinAttempt.get(address);
            int newAttempt;
            if (attempt == null) {
                newAttempt = 1;
            } else {
                newAttempt = attempt.intValue() + 1;
            }
            mPinAttempt.put(address, new Integer(newAttempt));
        }
        private void copyAutoPairingData() {
            File file = null;
            FileInputStream in = null;
            FileOutputStream out = null;
            try {
                file = new File(DYNAMIC_AUTO_PAIRING_BLACKLIST);
                if (file.exists()) return;
                in = new FileInputStream(AUTO_PAIRING_BLACKLIST);
                out= new FileOutputStream(DYNAMIC_AUTO_PAIRING_BLACKLIST);
                byte[] buf = new byte[1024];
                int len;
                while ((len = in.read(buf)) > 0) {
                    out.write(buf, 0, len);
                }
            } catch (FileNotFoundException e) {
                log("FileNotFoundException: in copyAutoPairingData");
            } catch (IOException e) {
                log("IOException: in copyAutoPairingData");
            } finally {
                 try {
                     if (in != null) in.close();
                     if (out != null) out.close();
                 } catch (IOException e) {}
            }
        }
        public void readAutoPairingData() {
            if (mAutoPairingAddressBlacklist != null) return;
            copyAutoPairingData();
            FileInputStream fstream = null;
            try {
                fstream = new FileInputStream(DYNAMIC_AUTO_PAIRING_BLACKLIST);
                DataInputStream in = new DataInputStream(fstream);
                BufferedReader file = new BufferedReader(new InputStreamReader(in));
                String line;
                while((line = file.readLine()) != null) {
                    line = line.trim();
                    if (line.length() == 0 || line.startsWith("
                    String[] value = line.split("=");
                    if (value != null && value.length == 2) {
                        String[] val = value[1].split(",");
                        if (value[0].equalsIgnoreCase("AddressBlacklist")) {
                            mAutoPairingAddressBlacklist =
                                new ArrayList<String>(Arrays.asList(val));
                        } else if (value[0].equalsIgnoreCase("ExactNameBlacklist")) {
                            mAutoPairingExactNameBlacklist =
                                new ArrayList<String>(Arrays.asList(val));
                        } else if (value[0].equalsIgnoreCase("PartialNameBlacklist")) {
                            mAutoPairingPartialNameBlacklist =
                                new ArrayList<String>(Arrays.asList(val));
                        } else if (value[0].equalsIgnoreCase("DynamicAddressBlacklist")) {
                            mAutoPairingDynamicAddressBlacklist =
                                new ArrayList<String>(Arrays.asList(val));
                        } else {
                            Log.e(TAG, "Error parsing Auto pairing blacklist file");
                        }
                    }
                }
            } catch (FileNotFoundException e) {
                log("FileNotFoundException: readAutoPairingData" + e.toString());
            } catch (IOException e) {
                log("IOException: readAutoPairingData" + e.toString());
            } finally {
                if (fstream != null) {
                    try {
                        fstream.close();
                    } catch (IOException e) {
                    }
                }
            }
        }
        private void updateAutoPairingData(String address) {
            BufferedWriter out = null;
            try {
                out = new BufferedWriter(new FileWriter(DYNAMIC_AUTO_PAIRING_BLACKLIST, true));
                StringBuilder str = new StringBuilder();
                if (mAutoPairingDynamicAddressBlacklist.size() == 0) {
                    str.append("DynamicAddressBlacklist=");
                }
                str.append(address);
                str.append(",");
                out.write(str.toString());
            } catch (FileNotFoundException e) {
                log("FileNotFoundException: updateAutoPairingData" + e.toString());
            } catch (IOException e) {
                log("IOException: updateAutoPairingData" + e.toString());
            } finally {
                if (out != null) {
                    try {
                        out.close();
                    } catch (IOException e) {
                    }
                }
            }
        }
    }
    private static String toBondStateString(int bondState) {
        switch (bondState) {
        case BluetoothDevice.BOND_NONE:
            return "not bonded";
        case BluetoothDevice.BOND_BONDING:
            return "bonding";
        case BluetoothDevice.BOND_BONDED:
            return "bonded";
        default:
            return "??????";
        }
    }
     synchronized boolean isAdapterPropertiesEmpty() {
        return mAdapterProperties.isEmpty();
    }
    synchronized void getAllProperties() {
        mContext.enforceCallingOrSelfPermission(BLUETOOTH_PERM, "Need BLUETOOTH permission");
        mAdapterProperties.clear();
        String properties[] = (String [])getAdapterPropertiesNative();
        if (properties == null) {
            Log.e(TAG, "*Error*: GetAdapterProperties returned NULL");
            return;
        }
        for (int i = 0; i < properties.length; i++) {
            String name = properties[i];
            String newValue = null;
            int len;
            if (name == null) {
                Log.e(TAG, "Error:Adapter Property at index" + i + "is null");
                continue;
            }
            if (name.equals("Devices")) {
                StringBuilder str = new StringBuilder();
                len = Integer.valueOf(properties[++i]);
                for (int j = 0; j < len; j++) {
                    str.append(properties[++i]);
                    str.append(",");
                }
                if (len > 0) {
                    newValue = str.toString();
                }
            } else {
                newValue = properties[++i];
            }
            mAdapterProperties.put(name, newValue);
        }
        String adapterPath = getAdapterPathNative();
        if (adapterPath != null)
            mAdapterProperties.put("ObjectPath", adapterPath + "/dev_");
    }
     synchronized void setProperty(String name, String value) {
        mAdapterProperties.put(name, value);
    }
    public synchronized boolean setName(String name) {
        mContext.enforceCallingOrSelfPermission(BLUETOOTH_ADMIN_PERM,
                                                "Need BLUETOOTH_ADMIN permission");
        if (name == null) {
            return false;
        }
        return setPropertyString("Name", name);
    }
    private boolean setPropertyString(String key, String value) {
        mContext.enforceCallingOrSelfPermission(BLUETOOTH_PERM, "Need BLUETOOTH permission");
        if (!isEnabledInternal()) return false;
        return setAdapterPropertyStringNative(key, value);
    }
    private boolean setPropertyInteger(String key, int value) {
        mContext.enforceCallingOrSelfPermission(BLUETOOTH_PERM, "Need BLUETOOTH permission");
        if (!isEnabledInternal()) return false;
        return setAdapterPropertyIntegerNative(key, value);
    }
    private boolean setPropertyBoolean(String key, boolean value) {
        mContext.enforceCallingOrSelfPermission(BLUETOOTH_PERM, "Need BLUETOOTH permission");
        if (!isEnabledInternal()) return false;
        return setAdapterPropertyBooleanNative(key, value ? 1 : 0);
    }
    public synchronized boolean setDiscoverableTimeout(int timeout) {
        mContext.enforceCallingOrSelfPermission(BLUETOOTH_ADMIN_PERM,
                                                "Need BLUETOOTH_ADMIN permission");
        return setPropertyInteger("DiscoverableTimeout", timeout);
    }
    public synchronized boolean setScanMode(int mode, int duration) {
        mContext.enforceCallingOrSelfPermission(android.Manifest.permission.WRITE_SECURE_SETTINGS,
                                                "Need WRITE_SECURE_SETTINGS permission");
        boolean pairable = false;
        boolean discoverable = false;
        switch (mode) {
        case BluetoothAdapter.SCAN_MODE_NONE:
            mHandler.removeMessages(MESSAGE_DISCOVERABLE_TIMEOUT);
            pairable = false;
            discoverable = false;
            break;
        case BluetoothAdapter.SCAN_MODE_CONNECTABLE:
            mHandler.removeMessages(MESSAGE_DISCOVERABLE_TIMEOUT);
            pairable = true;
            discoverable = false;
            break;
        case BluetoothAdapter.SCAN_MODE_CONNECTABLE_DISCOVERABLE:
            mHandler.removeMessages(MESSAGE_DISCOVERABLE_TIMEOUT);
            pairable = true;
            discoverable = true;
            Message msg = mHandler.obtainMessage(MESSAGE_DISCOVERABLE_TIMEOUT);
            mHandler.sendMessageDelayed(msg, duration * 1000);
            if (DBG) Log.d(TAG, "BT Discoverable for " + duration + " seconds");
            break;
        default:
            Log.w(TAG, "Requested invalid scan mode " + mode);
            return false;
        }
        setPropertyBoolean("Pairable", pairable);
        setPropertyBoolean("Discoverable", discoverable);
        return true;
    }
     synchronized String getProperty(String name) {
        if (!isEnabledInternal()) return null;
        return getPropertyInternal(name);
    }
     synchronized String getPropertyInternal(String name) {
        if (!mAdapterProperties.isEmpty())
            return mAdapterProperties.get(name);
        getAllProperties();
        return mAdapterProperties.get(name);
    }
    public synchronized String getAddress() {
        mContext.enforceCallingOrSelfPermission(BLUETOOTH_PERM, "Need BLUETOOTH permission");
        return getProperty("Address");
    }
    public synchronized String getName() {
        mContext.enforceCallingOrSelfPermission(BLUETOOTH_PERM, "Need BLUETOOTH permission");
        return getProperty("Name");
    }
    public synchronized String getRemoteName(String address) {
        mContext.enforceCallingOrSelfPermission(BLUETOOTH_PERM, "Need BLUETOOTH permission");
        if (!BluetoothAdapter.checkBluetoothAddress(address)) {
            return null;
        }
        return getRemoteDeviceProperty(address, "Name");
    }
    public synchronized int getDiscoverableTimeout() {
        mContext.enforceCallingOrSelfPermission(BLUETOOTH_PERM, "Need BLUETOOTH permission");
        String timeout = getProperty("DiscoverableTimeout");
        if (timeout != null)
           return Integer.valueOf(timeout);
        else
            return -1;
    }
    public synchronized int getScanMode() {
        mContext.enforceCallingOrSelfPermission(BLUETOOTH_PERM, "Need BLUETOOTH permission");
        if (!isEnabledInternal())
            return BluetoothAdapter.SCAN_MODE_NONE;
        boolean pairable = getProperty("Pairable").equals("true");
        boolean discoverable = getProperty("Discoverable").equals("true");
        return bluezStringToScanMode (pairable, discoverable);
    }
    public synchronized boolean startDiscovery() {
        mContext.enforceCallingOrSelfPermission(BLUETOOTH_ADMIN_PERM,
                                                "Need BLUETOOTH_ADMIN permission");
        if (!isEnabledInternal()) return false;
        return startDiscoveryNative();
    }
    public synchronized boolean cancelDiscovery() {
        mContext.enforceCallingOrSelfPermission(BLUETOOTH_ADMIN_PERM,
                                                "Need BLUETOOTH_ADMIN permission");
        if (!isEnabledInternal()) return false;
        return stopDiscoveryNative();
    }
    public synchronized boolean isDiscovering() {
        mContext.enforceCallingOrSelfPermission(BLUETOOTH_PERM, "Need BLUETOOTH permission");
        return mIsDiscovering;
    }
     void setIsDiscovering(boolean isDiscovering) {
        mIsDiscovering = isDiscovering;
    }
    public synchronized boolean createBond(String address) {
        mContext.enforceCallingOrSelfPermission(BLUETOOTH_ADMIN_PERM,
                                                "Need BLUETOOTH_ADMIN permission");
        if (!isEnabledInternal()) return false;
        if (!BluetoothAdapter.checkBluetoothAddress(address)) {
            return false;
        }
        address = address.toUpperCase();
        if (mBondState.getPendingOutgoingBonding() != null) {
            log("Ignoring createBond(): another device is bonding");
            return false;
        }
        if (!mBondState.isAutoPairingAttemptsInProgress(address) &&
                mBondState.getBondState(address) != BluetoothDevice.BOND_NONE) {
            log("Ignoring createBond(): this device is already bonding or bonded");
            return false;
        }
        if (address.equals(mDockAddress)) {
            if (!writeDockPin()) {
                log("Error while writing Pin for the dock");
                return false;
            }
        }
        if (!createPairedDeviceNative(address, 60000 )) {
            return false;
        }
        mBondState.setPendingOutgoingBonding(address);
        mBondState.setBondState(address, BluetoothDevice.BOND_BONDING);
        return true;
    }
    public synchronized boolean cancelBondProcess(String address) {
        mContext.enforceCallingOrSelfPermission(BLUETOOTH_ADMIN_PERM,
                                                "Need BLUETOOTH_ADMIN permission");
        if (!isEnabledInternal()) return false;
        if (!BluetoothAdapter.checkBluetoothAddress(address)) {
            return false;
        }
        address = address.toUpperCase();
        if (mBondState.getBondState(address) != BluetoothDevice.BOND_BONDING) {
            return false;
        }
        mBondState.setBondState(address, BluetoothDevice.BOND_NONE,
                                BluetoothDevice.UNBOND_REASON_AUTH_CANCELED);
        cancelDeviceCreationNative(address);
        return true;
    }
    public synchronized boolean removeBond(String address) {
        mContext.enforceCallingOrSelfPermission(BLUETOOTH_ADMIN_PERM,
                                                "Need BLUETOOTH_ADMIN permission");
        if (!isEnabledInternal()) return false;
        if (!BluetoothAdapter.checkBluetoothAddress(address)) {
            return false;
        }
        return removeDeviceNative(getObjectPathFromAddress(address));
    }
    public synchronized String[] listBonds() {
        mContext.enforceCallingOrSelfPermission(BLUETOOTH_PERM, "Need BLUETOOTH permission");
        return mBondState.listInState(BluetoothDevice.BOND_BONDED);
    }
    public synchronized int getBondState(String address) {
        mContext.enforceCallingOrSelfPermission(BLUETOOTH_PERM, "Need BLUETOOTH permission");
        if (!BluetoothAdapter.checkBluetoothAddress(address)) {
            return BluetoothDevice.ERROR;
        }
        return mBondState.getBondState(address.toUpperCase());
    }
    public synchronized boolean isBluetoothDock(String address) {
        SharedPreferences sp = mContext.getSharedPreferences(SHARED_PREFERENCES_NAME,
                mContext.MODE_PRIVATE);
        return sp.contains(SHARED_PREFERENCE_DOCK_ADDRESS + address);
    }
     boolean isRemoteDeviceInCache(String address) {
        return (mDeviceProperties.get(address) != null);
    }
     String[] getRemoteDeviceProperties(String address) {
        if (!isEnabledInternal()) return null;
        String objectPath = getObjectPathFromAddress(address);
        return (String [])getDevicePropertiesNative(objectPath);
    }
     synchronized String getRemoteDeviceProperty(String address, String property) {
        Map<String, String> properties = mDeviceProperties.get(address);
        if (properties != null) {
            return properties.get(property);
        } else {
            if (updateRemoteDevicePropertiesCache(address))
                return getRemoteDeviceProperty(address, property);
        }
        Log.e(TAG, "getRemoteDeviceProperty: " + property + "not present:" + address);
        return null;
    }
     synchronized boolean updateRemoteDevicePropertiesCache(String address) {
        String[] propValues = getRemoteDeviceProperties(address);
        if (propValues != null) {
            addRemoteDeviceProperties(address, propValues);
            return true;
        }
        return false;
    }
     synchronized void addRemoteDeviceProperties(String address, String[] properties) {
        Map<String, String> propertyValues = mDeviceProperties.get(address);
        if (propertyValues == null) {
            propertyValues = new HashMap<String, String>();
        }
        for (int i = 0; i < properties.length; i++) {
            String name = properties[i];
            String newValue = null;
            int len;
            if (name == null) {
                Log.e(TAG, "Error: Remote Device Property at index" + i + "is null");
                continue;
            }
            if (name.equals("UUIDs") || name.equals("Nodes")) {
                StringBuilder str = new StringBuilder();
                len = Integer.valueOf(properties[++i]);
                for (int j = 0; j < len; j++) {
                    str.append(properties[++i]);
                    str.append(",");
                }
                if (len > 0) {
                    newValue = str.toString();
                }
            } else {
                newValue = properties[++i];
            }
            propertyValues.put(name, newValue);
        }
        mDeviceProperties.put(address, propertyValues);
        updateDeviceServiceChannelCache(address);
    }
     void removeRemoteDeviceProperties(String address) {
        mDeviceProperties.remove(address);
    }
     synchronized void setRemoteDeviceProperty(String address, String name,
                                                              String value) {
        Map <String, String> propVal = mDeviceProperties.get(address);
        if (propVal != null) {
            propVal.put(name, value);
            mDeviceProperties.put(address, propVal);
        } else {
            Log.e(TAG, "setRemoteDeviceProperty for a device not in cache:" + address);
        }
    }
    public synchronized boolean setTrust(String address, boolean value) {
        if (!BluetoothAdapter.checkBluetoothAddress(address)) {
            mContext.enforceCallingOrSelfPermission(BLUETOOTH_ADMIN_PERM,
                    "Need BLUETOOTH_ADMIN permission");
            return false;
        }
        if (!isEnabledInternal()) return false;
        return setDevicePropertyBooleanNative(getObjectPathFromAddress(address), "Trusted",
                value ? 1 : 0);
    }
    public synchronized boolean getTrustState(String address) {
        if (!BluetoothAdapter.checkBluetoothAddress(address)) {
            mContext.enforceCallingOrSelfPermission(BLUETOOTH_PERM, "Need BLUETOOTH permission");
            return false;
        }
        String val = getRemoteDeviceProperty(address, "Trusted");
        if (val == null) {
            return false;
        } else {
            return val.equals("true") ? true : false;
        }
    }
    public synchronized int getRemoteClass(String address) {
        if (!BluetoothAdapter.checkBluetoothAddress(address)) {
            mContext.enforceCallingOrSelfPermission(BLUETOOTH_PERM, "Need BLUETOOTH permission");
            return BluetoothClass.ERROR;
        }
        String val = getRemoteDeviceProperty(address, "Class");
        if (val == null)
            return BluetoothClass.ERROR;
        else {
            return Integer.valueOf(val);
        }
    }
    public synchronized ParcelUuid[] getRemoteUuids(String address) {
        mContext.enforceCallingOrSelfPermission(BLUETOOTH_PERM, "Need BLUETOOTH permission");
        if (!BluetoothAdapter.checkBluetoothAddress(address)) {
            return null;
        }
        return getUuidFromCache(address);
    }
    private ParcelUuid[] getUuidFromCache(String address) {
        String value = getRemoteDeviceProperty(address, "UUIDs");
        if (value == null) return null;
        String[] uuidStrings = null;
        uuidStrings = value.split(",");
        ParcelUuid[] uuids = new ParcelUuid[uuidStrings.length];
        for (int i = 0; i < uuidStrings.length; i++) {
            uuids[i] = ParcelUuid.fromString(uuidStrings[i]);
        }
        return uuids;
    }
    public synchronized boolean fetchRemoteUuids(String address, ParcelUuid uuid,
            IBluetoothCallback callback) {
        mContext.enforceCallingOrSelfPermission(BLUETOOTH_PERM, "Need BLUETOOTH permission");
        if (!isEnabledInternal()) return false;
        if (!BluetoothAdapter.checkBluetoothAddress(address)) {
            return false;
        }
        RemoteService service = new RemoteService(address, uuid);
        if (uuid != null && mUuidCallbackTracker.get(service) != null) {
            return false;
        }
        if (mUuidIntentTracker.contains(address)) {
            if (uuid != null) {
                mUuidCallbackTracker.put(new RemoteService(address, uuid), callback);
            }
            return true;
        }
        boolean ret;
        if (isRemoteDeviceInCache(address) && getRemoteUuids(address) != null) {
            String path = getObjectPathFromAddress(address);
            if (path == null) return false;
            ret = discoverServicesNative(path, "");
        } else {
            ret = createDeviceNative(address);
        }
        mUuidIntentTracker.add(address);
        if (uuid != null) {
            mUuidCallbackTracker.put(new RemoteService(address, uuid), callback);
        }
        Message message = mHandler.obtainMessage(MESSAGE_UUID_INTENT);
        message.obj = address;
        mHandler.sendMessageDelayed(message, UUID_INTENT_DELAY);
        return ret;
    }
    public int getRemoteServiceChannel(String address, ParcelUuid uuid) {
        mContext.enforceCallingOrSelfPermission(BLUETOOTH_PERM, "Need BLUETOOTH permission");
        if (!isEnabledInternal()) return -1;
        if (!BluetoothAdapter.checkBluetoothAddress(address)) {
            return BluetoothDevice.ERROR;
        }
        if (mDeviceProperties.isEmpty()) {
            if (!updateRemoteDevicePropertiesCache(address))
                return -1;
        }
        Map<ParcelUuid, Integer> value = mDeviceServiceChannelCache.get(address);
        if (value != null && value.containsKey(uuid))
            return value.get(uuid);
        return -1;
    }
    public synchronized boolean setPin(String address, byte[] pin) {
        mContext.enforceCallingOrSelfPermission(BLUETOOTH_ADMIN_PERM,
                                                "Need BLUETOOTH_ADMIN permission");
        if (!isEnabledInternal()) return false;
        if (pin == null || pin.length <= 0 || pin.length > 16 ||
            !BluetoothAdapter.checkBluetoothAddress(address)) {
            return false;
        }
        address = address.toUpperCase();
        Integer data = mEventLoop.getPasskeyAgentRequestData().remove(address);
        if (data == null) {
            Log.w(TAG, "setPin(" + address + ") called but no native data available, " +
                  "ignoring. Maybe the PasskeyAgent Request was cancelled by the remote device" +
                  " or by bluez.\n");
            return false;
        }
        String pinString;
        try {
            pinString = new String(pin, "UTF8");
        } catch (UnsupportedEncodingException uee) {
            Log.e(TAG, "UTF8 not supported?!?");
            return false;
        }
        return setPinNative(address, pinString, data.intValue());
    }
    public synchronized boolean setPasskey(String address, int passkey) {
        mContext.enforceCallingOrSelfPermission(BLUETOOTH_ADMIN_PERM,
                                                "Need BLUETOOTH_ADMIN permission");
        if (!isEnabledInternal()) return false;
        if (passkey < 0 || passkey > 999999 || !BluetoothAdapter.checkBluetoothAddress(address)) {
            return false;
        }
        address = address.toUpperCase();
        Integer data = mEventLoop.getPasskeyAgentRequestData().remove(address);
        if (data == null) {
            Log.w(TAG, "setPasskey(" + address + ") called but no native data available, " +
                  "ignoring. Maybe the PasskeyAgent Request was cancelled by the remote device" +
                  " or by bluez.\n");
            return false;
        }
        return setPasskeyNative(address, passkey, data.intValue());
    }
    public synchronized boolean setPairingConfirmation(String address, boolean confirm) {
        mContext.enforceCallingOrSelfPermission(BLUETOOTH_ADMIN_PERM,
                                                "Need BLUETOOTH_ADMIN permission");
        if (!isEnabledInternal()) return false;
        address = address.toUpperCase();
        Integer data = mEventLoop.getPasskeyAgentRequestData().remove(address);
        if (data == null) {
            Log.w(TAG, "setPasskey(" + address + ") called but no native data available, " +
                  "ignoring. Maybe the PasskeyAgent Request was cancelled by the remote device" +
                  " or by bluez.\n");
            return false;
        }
        return setPairingConfirmationNative(address, confirm, data.intValue());
    }
    public synchronized boolean cancelPairingUserInput(String address) {
        mContext.enforceCallingOrSelfPermission(BLUETOOTH_ADMIN_PERM,
                                                "Need BLUETOOTH_ADMIN permission");
        if (!isEnabledInternal()) return false;
        if (!BluetoothAdapter.checkBluetoothAddress(address)) {
            return false;
        }
        mBondState.setBondState(address, BluetoothDevice.BOND_NONE,
                BluetoothDevice.UNBOND_REASON_AUTH_CANCELED);
        address = address.toUpperCase();
        Integer data = mEventLoop.getPasskeyAgentRequestData().remove(address);
        if (data == null) {
            Log.w(TAG, "cancelUserInputNative(" + address + ") called but no native data " +
                "available, ignoring. Maybe the PasskeyAgent Request was already cancelled " +
                "by the remote or by bluez.\n");
            return false;
        }
        return cancelPairingUserInputNative(address, data.intValue());
    }
     void updateDeviceServiceChannelCache(String address) {
        ParcelUuid[] deviceUuids = getRemoteUuids(address);
        int channel;
        if (DBG) log("updateDeviceServiceChannelCache(" + address + ")");
        ArrayList<ParcelUuid> applicationUuids = new ArrayList();
        synchronized (this) {
            for (RemoteService service : mUuidCallbackTracker.keySet()) {
                if (service.address.equals(address)) {
                    applicationUuids.add(service.uuid);
                }
            }
        }
        Map <ParcelUuid, Integer> value = new HashMap<ParcelUuid, Integer>();
        for (ParcelUuid uuid : RFCOMM_UUIDS) {
            if (BluetoothUuid.isUuidPresent(deviceUuids, uuid)) {
                channel = getDeviceServiceChannelNative(getObjectPathFromAddress(address),
                        uuid.toString(), 0x0004);
                if (DBG) log("\tuuid(system): " + uuid + " " + channel);
                value.put(uuid, channel);
            }
        }
        for (ParcelUuid uuid : applicationUuids) {
            if (BluetoothUuid.isUuidPresent(deviceUuids, uuid)) {
                channel = getDeviceServiceChannelNative(getObjectPathFromAddress(address),
                        uuid.toString(), 0x0004);
                if (DBG) log("\tuuid(application): " + uuid + " " + channel);
                value.put(uuid, channel);
            }
        }
        synchronized (this) {
            for (Iterator<RemoteService> iter = mUuidCallbackTracker.keySet().iterator();
                    iter.hasNext();) {
                RemoteService service = iter.next();
                if (service.address.equals(address)) {
                    channel = -1;
                    if (value.get(service.uuid) != null) {
                        channel = value.get(service.uuid);
                    }
                    if (channel != -1) {
                        if (DBG) log("Making callback for " + service.uuid + " with result " +
                                channel);
                        IBluetoothCallback callback = mUuidCallbackTracker.get(service);
                        if (callback != null) {
                            try {
                                callback.onRfcommChannelFound(channel);
                            } catch (RemoteException e) {Log.e(TAG, "", e);}
                        }
                        iter.remove();
                    }
                }
            }
            mDeviceServiceChannelCache.put(address, value);
        }
    }
    public synchronized int addRfcommServiceRecord(String serviceName, ParcelUuid uuid,
            int channel, IBinder b) {
        mContext.enforceCallingOrSelfPermission(BLUETOOTH_PERM, "Need BLUETOOTH permission");
        if (!isEnabledInternal()) return -1;
        if (serviceName == null || uuid == null || channel < 1 ||
                channel > BluetoothSocket.MAX_RFCOMM_CHANNEL) {
            return -1;
        }
        if (BluetoothUuid.isUuidPresent(BluetoothUuid.RESERVED_UUIDS, uuid)) {
            Log.w(TAG, "Attempted to register a reserved UUID: " + uuid);
            return -1;
        }
        int handle = addRfcommServiceRecordNative(serviceName,
                uuid.getUuid().getMostSignificantBits(), uuid.getUuid().getLeastSignificantBits(),
                (short)channel);
        if (DBG) log("new handle " + Integer.toHexString(handle));
        if (handle == -1) {
            return -1;
        }
        int pid = Binder.getCallingPid();
        mServiceRecordToPid.put(new Integer(handle), new Integer(pid));
        try {
            b.linkToDeath(new Reaper(handle, pid), 0);
        } catch (RemoteException e) {}
        return handle;
    }
    public void removeServiceRecord(int handle) {
        mContext.enforceCallingOrSelfPermission(BLUETOOTH_PERM,
                                                "Need BLUETOOTH permission");
        checkAndRemoveRecord(handle, Binder.getCallingPid());
    }
    private synchronized void checkAndRemoveRecord(int handle, int pid) {
        Integer handleInt = new Integer(handle);
        Integer owner = mServiceRecordToPid.get(handleInt);
        if (owner != null && pid == owner.intValue()) {
            if (DBG) log("Removing service record " + Integer.toHexString(handle) + " for pid " +
                    pid);
            mServiceRecordToPid.remove(handleInt);
            removeServiceRecordNative(handle);
        }
    }
    private class Reaper implements IBinder.DeathRecipient {
        int pid;
        int handle;
        Reaper(int handle, int pid) {
            this.pid = pid;
            this.handle = handle;
        }
        public void binderDied() {
            synchronized (BluetoothService.this) {
                if (DBG) log("Tracked app " + pid + " died");
                checkAndRemoveRecord(handle, pid);
            }
        }
    }
    private final BroadcastReceiver mReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent == null) return;
            String action = intent.getAction();
            if (action.equals(Intent.ACTION_AIRPLANE_MODE_CHANGED)) {
                ContentResolver resolver = context.getContentResolver();
                boolean enabled = !isAirplaneModeOn();
                if (Settings.Secure.getInt(resolver, Settings.Secure.BLUETOOTH_ON, 0) > 0) {
                    if (enabled) {
                        enable(false);
                    } else {
                        disable(false);
                    }
                }
            } else if (Intent.ACTION_DOCK_EVENT.equals(action)) {
                int state = intent.getIntExtra(Intent.EXTRA_DOCK_STATE,
                        Intent.EXTRA_DOCK_STATE_UNDOCKED);
                if (DBG) Log.v(TAG, "Received ACTION_DOCK_EVENT with State:" + state);
                if (state == Intent.EXTRA_DOCK_STATE_UNDOCKED) {
                    mDockAddress = null;
                    mDockPin = null;
                } else {
                    SharedPreferences.Editor editor =
                        mContext.getSharedPreferences(SHARED_PREFERENCES_NAME,
                                mContext.MODE_PRIVATE).edit();
                    editor.putBoolean(SHARED_PREFERENCE_DOCK_ADDRESS + mDockAddress, true);
                    editor.commit();
                }
            }
        }
    };
    private void registerForAirplaneMode(IntentFilter filter) {
        final ContentResolver resolver = mContext.getContentResolver();
        final String airplaneModeRadios = Settings.System.getString(resolver,
                Settings.System.AIRPLANE_MODE_RADIOS);
        final String toggleableRadios = Settings.System.getString(resolver,
                Settings.System.AIRPLANE_MODE_TOGGLEABLE_RADIOS);
        mIsAirplaneSensitive = airplaneModeRadios == null ? true :
                airplaneModeRadios.contains(Settings.System.RADIO_BLUETOOTH);
        mIsAirplaneToggleable = toggleableRadios == null ? false :
                toggleableRadios.contains(Settings.System.RADIO_BLUETOOTH);
        if (mIsAirplaneSensitive) {
            filter.addAction(Intent.ACTION_AIRPLANE_MODE_CHANGED);
        }
    }
    private final boolean isAirplaneModeOn() {
        return Settings.System.getInt(mContext.getContentResolver(),
                Settings.System.AIRPLANE_MODE_ON, 0) == 1;
    }
     synchronized void sendUuidIntent(String address) {
        ParcelUuid[] uuid = getUuidFromCache(address);
        Intent intent = new Intent(BluetoothDevice.ACTION_UUID);
        intent.putExtra(BluetoothDevice.EXTRA_DEVICE, mAdapter.getRemoteDevice(address));
        intent.putExtra(BluetoothDevice.EXTRA_UUID, uuid);
        mContext.sendBroadcast(intent, BLUETOOTH_ADMIN_PERM);
        if (mUuidIntentTracker.contains(address))
            mUuidIntentTracker.remove(address);
    }
     synchronized void makeServiceChannelCallbacks(String address) {
        for (Iterator<RemoteService> iter = mUuidCallbackTracker.keySet().iterator();
                iter.hasNext();) {
            RemoteService service = iter.next();
            if (service.address.equals(address)) {
                if (DBG) log("Cleaning up failed UUID channel lookup: " + service.address +
                        " " + service.uuid);
                IBluetoothCallback callback = mUuidCallbackTracker.get(service);
                if (callback != null) {
                    try {
                        callback.onRfcommChannelFound(-1);
                    } catch (RemoteException e) {Log.e(TAG, "", e);}
                }
                iter.remove();
            }
        }
    }
    @Override
    protected void dump(FileDescriptor fd, PrintWriter pw, String[] args) {
        switch(mBluetoothState) {
        case BluetoothAdapter.STATE_OFF:
            pw.println("Bluetooth OFF\n");
            return;
        case BluetoothAdapter.STATE_TURNING_ON:
            pw.println("Bluetooth TURNING ON\n");
            return;
        case BluetoothAdapter.STATE_TURNING_OFF:
            pw.println("Bluetooth TURNING OFF\n");
            return;
        case BluetoothAdapter.STATE_ON:
            pw.println("Bluetooth ON\n");
        }
        pw.println("mIsAirplaneSensitive = " + mIsAirplaneSensitive);
        pw.println("mIsAirplaneToggleable = " + mIsAirplaneToggleable);
        pw.println("Local address = " + getAddress());
        pw.println("Local name = " + getName());
        pw.println("isDiscovering() = " + isDiscovering());
        BluetoothHeadset headset = new BluetoothHeadset(mContext, null);
        pw.println("\n--Known devices--");
        for (String address : mDeviceProperties.keySet()) {
            int bondState = mBondState.getBondState(address);
            pw.printf("%s %10s (%d) %s\n", address,
                       toBondStateString(bondState),
                       mBondState.getAttempt(address),
                       getRemoteName(address));
            Map<ParcelUuid, Integer> uuidChannels = mDeviceServiceChannelCache.get(address);
            if (uuidChannels == null) {
                pw.println("\tuuids = null");
            } else {
                for (ParcelUuid uuid : uuidChannels.keySet()) {
                    Integer channel = uuidChannels.get(uuid);
                    if (channel == null) {
                        pw.println("\t" + uuid);
                    } else {
                        pw.println("\t" + uuid + " RFCOMM channel = " + channel);
                    }
                }
            }
            for (RemoteService service : mUuidCallbackTracker.keySet()) {
                if (service.address.equals(address)) {
                    pw.println("\tPENDING CALLBACK: " + service.uuid);
                }
            }
        }
        String value = getProperty("Devices");
        String[] devicesObjectPath = null;
        if (value != null) {
            devicesObjectPath = value.split(",");
        }
        pw.println("\n--ACL connected devices--");
        if (devicesObjectPath != null) {
            for (String device : devicesObjectPath) {
                pw.println(getAddressFromObjectPath(device));
            }
        }
        pw.println("\n--Headset Service--");
        switch (headset.getState()) {
        case BluetoothHeadset.STATE_DISCONNECTED:
            pw.println("getState() = STATE_DISCONNECTED");
            break;
        case BluetoothHeadset.STATE_CONNECTING:
            pw.println("getState() = STATE_CONNECTING");
            break;
        case BluetoothHeadset.STATE_CONNECTED:
            pw.println("getState() = STATE_CONNECTED");
            break;
        case BluetoothHeadset.STATE_ERROR:
            pw.println("getState() = STATE_ERROR");
            break;
        }
        pw.println("\ngetCurrentHeadset() = " + headset.getCurrentHeadset());
        pw.println("getBatteryUsageHint() = " + headset.getBatteryUsageHint());
        headset.close();
        pw.println("\n--Application Service Records--");
        for (Integer handle : mServiceRecordToPid.keySet()) {
            Integer pid = mServiceRecordToPid.get(handle);
            pw.println("\tpid " + pid + " handle " + Integer.toHexString(handle));
        }
    }
     static int bluezStringToScanMode(boolean pairable, boolean discoverable) {
        if (pairable && discoverable)
            return BluetoothAdapter.SCAN_MODE_CONNECTABLE_DISCOVERABLE;
        else if (pairable && !discoverable)
            return BluetoothAdapter.SCAN_MODE_CONNECTABLE;
        else
            return BluetoothAdapter.SCAN_MODE_NONE;
    }
     static String scanModeToBluezString(int mode) {
        switch (mode) {
        case BluetoothAdapter.SCAN_MODE_NONE:
            return "off";
        case BluetoothAdapter.SCAN_MODE_CONNECTABLE:
            return "connectable";
        case BluetoothAdapter.SCAN_MODE_CONNECTABLE_DISCOVERABLE:
            return "discoverable";
        }
        return null;
    }
     String getAddressFromObjectPath(String objectPath) {
        String adapterObjectPath = getPropertyInternal("ObjectPath");
        if (adapterObjectPath == null || objectPath == null) {
            Log.e(TAG, "getAddressFromObjectPath: AdpaterObjectPath:" + adapterObjectPath +
                    "  or deviceObjectPath:" + objectPath + " is null");
            return null;
        }
        if (!objectPath.startsWith(adapterObjectPath)) {
            Log.e(TAG, "getAddressFromObjectPath: AdpaterObjectPath:" + adapterObjectPath +
                    "  is not a prefix of deviceObjectPath:" + objectPath +
                    "bluetoothd crashed ?");
            return null;
        }
        String address = objectPath.substring(adapterObjectPath.length());
        if (address != null) return address.replace('_', ':');
        Log.e(TAG, "getAddressFromObjectPath: Address being returned is null");
        return null;
    }
     String getObjectPathFromAddress(String address) {
        String path = getPropertyInternal("ObjectPath");
        if (path == null) {
            Log.e(TAG, "Error: Object Path is null");
            return null;
        }
        path = path + address.replace(":", "_");
        return path;
    }
     void setLinkTimeout(String address, int num_slots) {
        String path = getObjectPathFromAddress(address);
        boolean result = setLinkTimeoutNative(path, num_slots);
        if (!result) log("Set Link Timeout to:" + num_slots + " slots failed");
    }
    private static void log(String msg) {
        Log.d(TAG, msg);
    }
    private native static void classInitNative();
    private native void initializeNativeDataNative();
    private native boolean setupNativeDataNative();
    private native boolean tearDownNativeDataNative();
    private native void cleanupNativeDataNative();
    private native String getAdapterPathNative();
    private native int isEnabledNative();
    private native int enableNative();
    private native int disableNative();
    private native Object[] getAdapterPropertiesNative();
    private native Object[] getDevicePropertiesNative(String objectPath);
    private native boolean setAdapterPropertyStringNative(String key, String value);
    private native boolean setAdapterPropertyIntegerNative(String key, int value);
    private native boolean setAdapterPropertyBooleanNative(String key, int value);
    private native boolean startDiscoveryNative();
    private native boolean stopDiscoveryNative();
    private native boolean createPairedDeviceNative(String address, int timeout_ms);
    private native boolean cancelDeviceCreationNative(String address);
    private native boolean removeDeviceNative(String objectPath);
    private native int getDeviceServiceChannelNative(String objectPath, String uuid,
            int attributeId);
    private native boolean cancelPairingUserInputNative(String address, int nativeData);
    private native boolean setPinNative(String address, String pin, int nativeData);
    private native boolean setPasskeyNative(String address, int passkey, int nativeData);
    private native boolean setPairingConfirmationNative(String address, boolean confirm,
            int nativeData);
    private native boolean setDevicePropertyBooleanNative(String objectPath, String key,
            int value);
    private native boolean createDeviceNative(String address);
     native boolean discoverServicesNative(String objectPath, String pattern);
    private native int addRfcommServiceRecordNative(String name, long uuidMsb, long uuidLsb,
            short channel);
    private native boolean removeServiceRecordNative(int handle);
    private native boolean setLinkTimeoutNative(String path, int num_slots);
}
