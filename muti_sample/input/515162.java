public class CachedBluetoothDevice implements Comparable<CachedBluetoothDevice> {
    private static final String TAG = "CachedBluetoothDevice";
    private static final boolean D = LocalBluetoothManager.D;
    private static final boolean V = LocalBluetoothManager.V;
    private static final boolean DEBUG = false;
    private static final int CONTEXT_ITEM_CONNECT = Menu.FIRST + 1;
    private static final int CONTEXT_ITEM_DISCONNECT = Menu.FIRST + 2;
    private static final int CONTEXT_ITEM_UNPAIR = Menu.FIRST + 3;
    private static final int CONTEXT_ITEM_CONNECT_ADVANCED = Menu.FIRST + 4;
    private final BluetoothDevice mDevice;
    private String mName;
    private short mRssi;
    private BluetoothClass mBtClass;
    private List<Profile> mProfiles = new ArrayList<Profile>();
    private boolean mVisible;
    private final LocalBluetoothManager mLocalManager;
    private List<Callback> mCallbacks = new ArrayList<Callback>();
    private boolean mIsConnectingErrorPossible;
    private long mConnectAttempted;
    private static final long MAX_UUID_DELAY_FOR_AUTO_CONNECT = 5000;
    private static final long MAX_WAIT_TIME_FOR_FRAMEWORK = 25 * 1000;
    private enum BluetoothCommand {
        CONNECT, DISCONNECT, REMOVE_BOND,
    }
    static class BluetoothJob {
        final BluetoothCommand command; 
        final CachedBluetoothDevice cachedDevice;
        final Profile profile; 
        long timeSent;
        public BluetoothJob(BluetoothCommand command,
                CachedBluetoothDevice cachedDevice, Profile profile) {
            this.command = command;
            this.cachedDevice = cachedDevice;
            this.profile = profile;
            this.timeSent = 0;
        }
        @Override
        public String toString() {
            StringBuilder sb = new StringBuilder();
            sb.append(command.name());
            sb.append(" Address:").append(cachedDevice.mDevice);
            if (profile != null) {
                sb.append(" Profile:").append(profile.name());
            }
            sb.append(" TimeSent:");
            if (timeSent == 0) {
                sb.append("not yet");
            } else {
                sb.append(DateFormat.getTimeInstance().format(new Date(timeSent)));
            }
            return sb.toString();
        }
    }
    private static LinkedList<BluetoothJob> workQueue = new LinkedList<BluetoothJob>();
    private void queueCommand(BluetoothJob job) {
        synchronized (workQueue) {
            if (D) {
                Log.d(TAG, workQueue.toString());
            }
            boolean processNow = pruneQueue(job);
            if (D) {
                Log.d(TAG, "Adding: " + job.toString());
            }
            workQueue.add(job);
            if (workQueue.size() == 1 || processNow) {
                processCommands();
            }
        }
    }
    private boolean pruneQueue(BluetoothJob job) {
        boolean removedStaleItems = false;
        long now = System.currentTimeMillis();
        Iterator<BluetoothJob> it = workQueue.iterator();
        while (it.hasNext()) {
            BluetoothJob existingJob = it.next();
            if (job != null && job.command == BluetoothCommand.DISCONNECT) {
                if (existingJob.timeSent == 0
                        && existingJob.command == BluetoothCommand.CONNECT
                        && existingJob.cachedDevice.mDevice.equals(job.cachedDevice.mDevice)
                        && existingJob.profile == job.profile) {
                    if (D) {
                        Log.d(TAG, "Removed because of a pending disconnect. " + existingJob);
                    }
                    it.remove();
                    continue;
                }
            }
            if (existingJob.timeSent != 0
                    && (now - existingJob.timeSent) >= MAX_WAIT_TIME_FOR_FRAMEWORK) {
                Log.w(TAG, "Timeout. Removing Job:" + existingJob.toString());
                it.remove();
                removedStaleItems = true;
                continue;
            }
        }
        return removedStaleItems;
    }
    private boolean processCommand(BluetoothJob job) {
        boolean successful = false;
        if (job.timeSent == 0) {
            job.timeSent = System.currentTimeMillis();
            switch (job.command) {
            case CONNECT:
                successful = connectInt(job.cachedDevice, job.profile);
                break;
            case DISCONNECT:
                successful = disconnectInt(job.cachedDevice, job.profile);
                break;
            case REMOVE_BOND:
                BluetoothDevice dev = job.cachedDevice.getDevice();
                if (dev != null) {
                    successful = dev.removeBond();
                }
                break;
            }
            if (successful) {
                if (D) {
                    Log.d(TAG, "Command sent successfully:" + job.toString());
                }
            } else if (V) {
                Log.v(TAG, "Framework rejected command immediately:" + job.toString());
            }
        } else if (D) {
            Log.d(TAG, "Job already has a sent time. Skip. " + job.toString());
        }
        return successful;
    }
    public void onProfileStateChanged(Profile profile, int newProfileState) {
        synchronized (workQueue) {
            if (D) {
                Log.d(TAG, "onProfileStateChanged:" + workQueue.toString());
            }
            int newState = LocalBluetoothProfileManager.getProfileManager(mLocalManager,
                    profile).convertState(newProfileState);
            if (newState == SettingsBtStatus.CONNECTION_STATUS_CONNECTED) {
                if (!mProfiles.contains(profile)) {
                    mProfiles.add(profile);
                }
            }
            if (newState == SettingsBtStatus.CONNECTION_STATUS_CONNECTED ||
                    newState == SettingsBtStatus.CONNECTION_STATUS_DISCONNECTED) {
                BluetoothJob job = workQueue.peek();
                if (job == null) {
                    return;
                } else if (!job.cachedDevice.mDevice.equals(mDevice)) {
                    if (D) {
                        Log.d(TAG, "mDevice:" + mDevice + " != head:" + job.toString());
                    }
                    if (!pruneQueue(null)) {
                        return;
                    }
                } else {
                    workQueue.poll();
                }
                processCommands();
            }
        }
    }
    private void processCommands() {
        if (D) {
            Log.d(TAG, "processCommands:" + workQueue.toString());
        }
        Iterator<BluetoothJob> it = workQueue.iterator();
        while (it.hasNext()) {
            BluetoothJob job = it.next();
            if (processCommand(job)) {
                return;
            } else {
                it.remove();
            }
        }
    }
    CachedBluetoothDevice(Context context, BluetoothDevice device) {
        mLocalManager = LocalBluetoothManager.getInstance(context);
        if (mLocalManager == null) {
            throw new IllegalStateException(
                    "Cannot use CachedBluetoothDevice without Bluetooth hardware");
        }
        mDevice = device;
        fillData();
    }
    public void onClicked() {
        int bondState = getBondState();
        if (isConnected()) {
            askDisconnect();
        } else if (bondState == BluetoothDevice.BOND_BONDED) {
            connect();
        } else if (bondState == BluetoothDevice.BOND_NONE) {
            pair();
        }
    }
    public void disconnect() {
        for (Profile profile : mProfiles) {
            disconnect(profile);
        }
    }
    public void disconnect(Profile profile) {
        queueCommand(new BluetoothJob(BluetoothCommand.DISCONNECT, this, profile));
    }
    private boolean disconnectInt(CachedBluetoothDevice cachedDevice, Profile profile) {
        LocalBluetoothProfileManager profileManager =
                LocalBluetoothProfileManager.getProfileManager(mLocalManager, profile);
        int status = profileManager.getConnectionStatus(cachedDevice.mDevice);
        if (SettingsBtStatus.isConnectionStatusConnected(status)) {
            if (profileManager.disconnect(cachedDevice.mDevice)) {
                return true;
            }
        }
        return false;
    }
    public void askDisconnect() {
        Context context = mLocalManager.getForegroundActivity();
        if (context == null) {
            disconnect();
            return;
        }
        Resources res = context.getResources();
        String name = getName();
        if (TextUtils.isEmpty(name)) {
            name = res.getString(R.string.bluetooth_device);
        }
        String message = res.getString(R.string.bluetooth_disconnect_blank, name);
        DialogInterface.OnClickListener disconnectListener = new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                disconnect();
            }
        };
        new AlertDialog.Builder(context)
                .setTitle(getName())
                .setMessage(message)
                .setPositiveButton(android.R.string.ok, disconnectListener)
                .setNegativeButton(android.R.string.cancel, null)
                .show();
    }
    public void connect() {
        if (!ensurePaired()) return;
        mConnectAttempted = SystemClock.elapsedRealtime();
        connectWithoutResettingTimer();
    }
     void onBondingDockConnect() {
        mConnectAttempted = SystemClock.elapsedRealtime();
    }
    private void connectWithoutResettingTimer() {
        if (mProfiles.size() == 0) {
            if (!updateProfiles()) {
                if (DEBUG) Log.d(TAG, "No profiles. Maybe we will connect later");
                return;
            }
        }
        mIsConnectingErrorPossible = true;
        int preferredProfiles = 0;
        for (Profile profile : mProfiles) {
            if (isConnectableProfile(profile)) {
                LocalBluetoothProfileManager profileManager = LocalBluetoothProfileManager
                        .getProfileManager(mLocalManager, profile);
                if (profileManager.isPreferred(mDevice)) {
                    ++preferredProfiles;
                    disconnectConnected(profile);
                    queueCommand(new BluetoothJob(BluetoothCommand.CONNECT, this, profile));
                }
            }
        }
        if (DEBUG) Log.d(TAG, "Preferred profiles = " + preferredProfiles);
        if (preferredProfiles == 0) {
            connectAllProfiles();
        }
    }
    private void connectAllProfiles() {
        if (!ensurePaired()) return;
        mIsConnectingErrorPossible = true;
        for (Profile profile : mProfiles) {
            if (isConnectableProfile(profile)) {
                LocalBluetoothProfileManager profileManager = LocalBluetoothProfileManager
                        .getProfileManager(mLocalManager, profile);
                profileManager.setPreferred(mDevice, false);
                disconnectConnected(profile);
                queueCommand(new BluetoothJob(BluetoothCommand.CONNECT, this, profile));
            }
        }
    }
    public void connect(Profile profile) {
        mConnectAttempted = SystemClock.elapsedRealtime();
        mIsConnectingErrorPossible = true;
        disconnectConnected(profile);
        queueCommand(new BluetoothJob(BluetoothCommand.CONNECT, this, profile));
    }
    private void disconnectConnected(Profile profile) {
        LocalBluetoothProfileManager profileManager =
            LocalBluetoothProfileManager.getProfileManager(mLocalManager, profile);
        CachedBluetoothDeviceManager cachedDeviceManager = mLocalManager.getCachedDeviceManager();
        Set<BluetoothDevice> devices = profileManager.getConnectedDevices();
        if (devices == null) return;
        for (BluetoothDevice device : devices) {
            CachedBluetoothDevice cachedDevice = cachedDeviceManager.findDevice(device);
            if (cachedDevice != null) {
                queueCommand(new BluetoothJob(BluetoothCommand.DISCONNECT, cachedDevice, profile));
            }
        }
    }
    private boolean connectInt(CachedBluetoothDevice cachedDevice, Profile profile) {
        if (!cachedDevice.ensurePaired()) return false;
        LocalBluetoothProfileManager profileManager =
                LocalBluetoothProfileManager.getProfileManager(mLocalManager, profile);
        int status = profileManager.getConnectionStatus(cachedDevice.mDevice);
        if (!SettingsBtStatus.isConnectionStatusConnected(status)) {
            if (profileManager.connect(cachedDevice.mDevice)) {
                return true;
            }
            Log.i(TAG, "Failed to connect " + profile.toString() + " to " + cachedDevice.mName);
        } else {
            Log.i(TAG, "Already connected");
        }
        return false;
    }
    public void showConnectingError() {
        if (!mIsConnectingErrorPossible) return;
        mIsConnectingErrorPossible = false;
        mLocalManager.showError(mDevice, R.string.bluetooth_error_title,
                R.string.bluetooth_connecting_error_message);
    }
    private boolean ensurePaired() {
        if (getBondState() == BluetoothDevice.BOND_NONE) {
            pair();
            return false;
        } else {
            return true;
        }
    }
    public void pair() {
        BluetoothAdapter adapter = mLocalManager.getBluetoothAdapter();
        if (adapter.isDiscovering()) {
            adapter.cancelDiscovery();
        }
        if (!mDevice.createBond()) {
            mLocalManager.showError(mDevice, R.string.bluetooth_error_title,
                    R.string.bluetooth_pairing_error_message);
        }
    }
    public void unpair() {
        disconnect();
        int state = getBondState();
        if (state == BluetoothDevice.BOND_BONDING) {
            mDevice.cancelBondProcess();
        }
        if (state != BluetoothDevice.BOND_NONE) {
            queueCommand(new BluetoothJob(BluetoothCommand.REMOVE_BOND, this, null));
        }
    }
    private void fillData() {
        fetchName();
        fetchBtClass();
        updateProfiles();
        mVisible = false;
        dispatchAttributesChanged();
    }
    public BluetoothDevice getDevice() {
        return mDevice;
    }
    public String getName() {
        return mName;
    }
    public void setName(String name) {
        if (!mName.equals(name)) {
            if (TextUtils.isEmpty(name)) {
                mName = mDevice.getAddress();
            } else {
                mName = name;
            }
            dispatchAttributesChanged();
        }
    }
    public void refreshName() {
        fetchName();
        dispatchAttributesChanged();
    }
    private void fetchName() {
        mName = mDevice.getName();
        if (TextUtils.isEmpty(mName)) {
            mName = mDevice.getAddress();
            if (DEBUG) Log.d(TAG, "Default to address. Device has no name (yet) " + mName);
        }
    }
    public void refresh() {
        dispatchAttributesChanged();
    }
    public boolean isVisible() {
        return mVisible;
    }
    void setVisible(boolean visible) {
        if (mVisible != visible) {
            mVisible = visible;
            dispatchAttributesChanged();
        }
    }
    public int getBondState() {
        return mDevice.getBondState();
    }
    void setRssi(short rssi) {
        if (mRssi != rssi) {
            mRssi = rssi;
            dispatchAttributesChanged();
        }
    }
    public boolean isConnected() {
        for (Profile profile : mProfiles) {
            int status = LocalBluetoothProfileManager.getProfileManager(mLocalManager, profile)
                    .getConnectionStatus(mDevice);
            if (SettingsBtStatus.isConnectionStatusConnected(status)) {
                return true;
            }
        }
        return false;
    }
    public boolean isBusy() {
        for (Profile profile : mProfiles) {
            int status = LocalBluetoothProfileManager.getProfileManager(mLocalManager, profile)
                    .getConnectionStatus(mDevice);
            if (SettingsBtStatus.isConnectionStatusBusy(status)) {
                return true;
            }
        }
        if (getBondState() == BluetoothDevice.BOND_BONDING) {
            return true;
        }
        return false;
    }
    public int getBtClassDrawable() {
        if (mBtClass != null) {
            switch (mBtClass.getMajorDeviceClass()) {
            case BluetoothClass.Device.Major.COMPUTER:
                return R.drawable.ic_bt_laptop;
            case BluetoothClass.Device.Major.PHONE:
                return R.drawable.ic_bt_cellphone;
            }
        } else {
            Log.w(TAG, "mBtClass is null");
        }
        if (mProfiles.size() > 0) {
            if (mProfiles.contains(Profile.A2DP)) {
                return R.drawable.ic_bt_headphones_a2dp;
            } else if (mProfiles.contains(Profile.HEADSET)) {
                return R.drawable.ic_bt_headset_hfp;
            }
        } else if (mBtClass != null) {
            if (mBtClass.doesClassMatch(BluetoothClass.PROFILE_A2DP)) {
                return R.drawable.ic_bt_headphones_a2dp;
            }
            if (mBtClass.doesClassMatch(BluetoothClass.PROFILE_HEADSET)) {
                return R.drawable.ic_bt_headset_hfp;
            }
        }
        return 0;
    }
    private void fetchBtClass() {
        mBtClass = mDevice.getBluetoothClass();
    }
    private boolean updateProfiles() {
        ParcelUuid[] uuids = mDevice.getUuids();
        if (uuids == null) return false;
        LocalBluetoothProfileManager.updateProfiles(uuids, mProfiles);
        if (DEBUG) {
            Log.e(TAG, "updating profiles for " + mDevice.getName());
            boolean printUuids = true;
            BluetoothClass bluetoothClass = mDevice.getBluetoothClass();
            if (bluetoothClass != null) {
                if (bluetoothClass.doesClassMatch(BluetoothClass.PROFILE_HEADSET) !=
                    mProfiles.contains(Profile.HEADSET)) {
                    Log.v(TAG, "headset classbits != uuid");
                    printUuids = true;
                }
                if (bluetoothClass.doesClassMatch(BluetoothClass.PROFILE_A2DP) !=
                    mProfiles.contains(Profile.A2DP)) {
                    Log.v(TAG, "a2dp classbits != uuid");
                    printUuids = true;
                }
                if (bluetoothClass.doesClassMatch(BluetoothClass.PROFILE_OPP) !=
                    mProfiles.contains(Profile.OPP)) {
                    Log.v(TAG, "opp classbits != uuid");
                    printUuids = true;
                }
            }
            if (printUuids) {
                if (bluetoothClass != null) Log.v(TAG, "Class: " + bluetoothClass.toString());
                Log.v(TAG, "UUID:");
                for (int i = 0; i < uuids.length; i++) {
                    Log.v(TAG, "  " + uuids[i]);
                }
            }
        }
        return true;
    }
    public void refreshBtClass() {
        fetchBtClass();
        dispatchAttributesChanged();
    }
    public void onUuidChanged() {
        updateProfiles();
        if (DEBUG) {
            Log.e(TAG, "onUuidChanged: Time since last connect"
                    + (SystemClock.elapsedRealtime() - mConnectAttempted));
        }
        if (mProfiles.size() > 0
                && (mConnectAttempted + MAX_UUID_DELAY_FOR_AUTO_CONNECT) > SystemClock
                        .elapsedRealtime()) {
            connectWithoutResettingTimer();
        }
        dispatchAttributesChanged();
    }
    public void onBondingStateChanged(int bondState) {
        if (bondState == BluetoothDevice.BOND_NONE) {
            mProfiles.clear();
            BluetoothJob job = workQueue.peek();
            if (job != null) {
                if (job.command == BluetoothCommand.REMOVE_BOND
                        && job.cachedDevice.mDevice.equals(mDevice)) {
                    workQueue.poll(); 
                } else {
                    if (D) {
                        Log.d(TAG, "job.command = " + job.command);
                        Log.d(TAG, "mDevice:" + mDevice + " != head:" + job.toString());
                    }
                    if (!pruneQueue(null)) {
                        refresh();
                        return;
                    }
                }
                processCommands();
            }
        }
        refresh();
    }
    public void setBtClass(BluetoothClass btClass) {
        if (btClass != null && mBtClass != btClass) {
            mBtClass = btClass;
            dispatchAttributesChanged();
        }
    }
    public int getSummary() {
        int oneOffSummary = getOneOffSummary();
        if (oneOffSummary != 0) {
            return oneOffSummary;
        }
        for (Profile profile : mProfiles) {
            LocalBluetoothProfileManager profileManager = LocalBluetoothProfileManager
                    .getProfileManager(mLocalManager, profile);
            int connectionStatus = profileManager.getConnectionStatus(mDevice);
            if (SettingsBtStatus.isConnectionStatusConnected(connectionStatus) ||
                    connectionStatus == SettingsBtStatus.CONNECTION_STATUS_CONNECTING ||
                    connectionStatus == SettingsBtStatus.CONNECTION_STATUS_DISCONNECTING) {
                return SettingsBtStatus.getConnectionStatusSummary(connectionStatus);
            }
        }
        return SettingsBtStatus.getPairingStatusSummary(getBondState());
    }
    private int getOneOffSummary() {
        boolean isA2dpConnected = false, isHeadsetConnected = false, isConnecting = false;
        if (mProfiles.contains(Profile.A2DP)) {
            LocalBluetoothProfileManager profileManager = LocalBluetoothProfileManager
                    .getProfileManager(mLocalManager, Profile.A2DP);
            isConnecting = profileManager.getConnectionStatus(mDevice) ==
                    SettingsBtStatus.CONNECTION_STATUS_CONNECTING;
            isA2dpConnected = profileManager.isConnected(mDevice);
        }
        if (mProfiles.contains(Profile.HEADSET)) {
            LocalBluetoothProfileManager profileManager = LocalBluetoothProfileManager
                    .getProfileManager(mLocalManager, Profile.HEADSET);
            isConnecting |= profileManager.getConnectionStatus(mDevice) ==
                    SettingsBtStatus.CONNECTION_STATUS_CONNECTING;
            isHeadsetConnected = profileManager.isConnected(mDevice);
        }
        if (isConnecting) {
            return SettingsBtStatus.getConnectionStatusSummary(
                    SettingsBtStatus.CONNECTION_STATUS_CONNECTING);
        } else if (isA2dpConnected && isHeadsetConnected) {
            return R.string.bluetooth_summary_connected_to_a2dp_headset;
        } else if (isA2dpConnected) {
            return R.string.bluetooth_summary_connected_to_a2dp;
        } else if (isHeadsetConnected) {
            return R.string.bluetooth_summary_connected_to_headset;
        } else {
            return 0;
        }
    }
    public List<Profile> getConnectableProfiles() {
        ArrayList<Profile> connectableProfiles = new ArrayList<Profile>();
        for (Profile profile : mProfiles) {
            if (isConnectableProfile(profile)) {
                connectableProfiles.add(profile);
            }
        }
        return connectableProfiles;
    }
    private boolean isConnectableProfile(Profile profile) {
        return profile.equals(Profile.HEADSET) || profile.equals(Profile.A2DP);
    }
    public void onCreateContextMenu(ContextMenu menu) {
        if (mLocalManager.getBluetoothState() != BluetoothAdapter.STATE_ON || isBusy()) {
            return;
        }
        int bondState = getBondState();
        boolean isConnected = isConnected();
        boolean hasConnectableProfiles = false;
        for (Profile profile : mProfiles) {
            if (isConnectableProfile(profile)) {
                hasConnectableProfiles = true;
                break;
            }
        }
        menu.setHeaderTitle(getName());
        if (bondState == BluetoothDevice.BOND_NONE) { 
            menu.add(0, CONTEXT_ITEM_CONNECT, 0, R.string.bluetooth_device_context_pair_connect);
        } else { 
            if (isConnected) { 
                menu.add(0, CONTEXT_ITEM_DISCONNECT, 0,
                        R.string.bluetooth_device_context_disconnect);
                menu.add(0, CONTEXT_ITEM_UNPAIR, 0,
                        R.string.bluetooth_device_context_disconnect_unpair);
            } else { 
                if (hasConnectableProfiles) {
                    menu.add(0, CONTEXT_ITEM_CONNECT, 0, R.string.bluetooth_device_context_connect);
                }
                menu.add(0, CONTEXT_ITEM_UNPAIR, 0, R.string.bluetooth_device_context_unpair);
            }
            if (hasConnectableProfiles) {
                menu.add(0, CONTEXT_ITEM_CONNECT_ADVANCED, 0,
                        R.string.bluetooth_device_context_connect_advanced);
            }
        }
    }
    public void onContextItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case CONTEXT_ITEM_DISCONNECT:
                disconnect();
                break;
            case CONTEXT_ITEM_CONNECT:
                connect();
                break;
            case CONTEXT_ITEM_UNPAIR:
                unpair();
                break;
            case CONTEXT_ITEM_CONNECT_ADVANCED:
                Intent intent = new Intent();
                Context context = mLocalManager.getForegroundActivity();
                if (context == null) {
                    context = mLocalManager.getContext();
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                }
                intent.setClass(context, ConnectSpecificProfilesActivity.class);
                intent.putExtra(ConnectSpecificProfilesActivity.EXTRA_DEVICE, mDevice);
                context.startActivity(intent);
                break;
        }
    }
    public void registerCallback(Callback callback) {
        synchronized (mCallbacks) {
            mCallbacks.add(callback);
        }
    }
    public void unregisterCallback(Callback callback) {
        synchronized (mCallbacks) {
            mCallbacks.remove(callback);
        }
    }
    private void dispatchAttributesChanged() {
        synchronized (mCallbacks) {
            for (Callback callback : mCallbacks) {
                callback.onDeviceAttributesChanged(this);
            }
        }
    }
    @Override
    public String toString() {
        return mDevice.toString();
    }
    @Override
    public boolean equals(Object o) {
        if ((o == null) || !(o instanceof CachedBluetoothDevice)) {
            throw new ClassCastException();
        }
        return mDevice.equals(((CachedBluetoothDevice) o).mDevice);
    }
    @Override
    public int hashCode() {
        return mDevice.getAddress().hashCode();
    }
    public int compareTo(CachedBluetoothDevice another) {
        int comparison;
        comparison = (another.isConnected() ? 1 : 0) - (isConnected() ? 1 : 0);
        if (comparison != 0) return comparison;
        comparison = (another.getBondState() == BluetoothDevice.BOND_BONDED ? 1 : 0) -
            (getBondState() == BluetoothDevice.BOND_BONDED ? 1 : 0);
        if (comparison != 0) return comparison;
        comparison = (another.mVisible ? 1 : 0) - (mVisible ? 1 : 0);
        if (comparison != 0) return comparison;
        comparison = another.mRssi - mRssi;
        if (comparison != 0) return comparison;
        return getName().compareTo(another.getName());
    }
    public interface Callback {
        void onDeviceAttributesChanged(CachedBluetoothDevice cachedDevice);
    }
}
