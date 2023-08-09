public abstract class NetworkStateTracker extends Handler {
    protected NetworkInfo mNetworkInfo;
    protected Context mContext;
    protected Handler mTarget;
    protected String mInterfaceName;
    protected String[] mDnsPropNames;
    private boolean mPrivateDnsRouteSet;
    protected int mDefaultGatewayAddr;
    private boolean mDefaultRouteSet;
    private boolean mTeardownRequested;
    private static boolean DBG = true;
    private static final String TAG = "NetworkStateTracker";
    public static final int EVENT_STATE_CHANGED = 1;
    public static final int EVENT_SCAN_RESULTS_AVAILABLE = 2;
    public static final int EVENT_NOTIFICATION_CHANGED = 3;
    public static final int EVENT_CONFIGURATION_CHANGED = 4;
    public static final int EVENT_ROAMING_CHANGED = 5;
    public static final int EVENT_NETWORK_SUBTYPE_CHANGED = 6;
    public static final int EVENT_RESTORE_DEFAULT_NETWORK = 7;
    public NetworkStateTracker(Context context,
            Handler target,
            int networkType,
            int subType,
            String typeName,
            String subtypeName) {
        super();
        mContext = context;
        mTarget = target;
        mTeardownRequested = false;
        this.mNetworkInfo = new NetworkInfo(networkType, subType, typeName, subtypeName);
    }
    public NetworkInfo getNetworkInfo() {
        return mNetworkInfo;
    }
    public abstract String getTcpBufferSizesPropName();
    public String[] getNameServers() {
        return getNameServerList(mDnsPropNames);
    }
    static protected String[] getNameServerList(String[] propertyNames) {
        String[] dnsAddresses = new String[propertyNames.length];
        int i, j;
        for (i = 0, j = 0; i < propertyNames.length; i++) {
            String value = SystemProperties.get(propertyNames[i]);
            if (!TextUtils.isEmpty(value) && !TextUtils.equals(value, "0.0.0.0")) {
                dnsAddresses[j++] = value;
            }
        }
        return dnsAddresses;
    }
    public void addPrivateDnsRoutes() {
        if (DBG) {
            Log.d(TAG, "addPrivateDnsRoutes for " + this +
                    "(" + mInterfaceName + ") - mPrivateDnsRouteSet = "+mPrivateDnsRouteSet);
        }
        if (mInterfaceName != null && !mPrivateDnsRouteSet) {
            for (String addrString : getNameServers()) {
                int addr = NetworkUtils.lookupHost(addrString);
                if (addr != -1 && addr != 0) {
                    if (DBG) Log.d(TAG, "  adding "+addrString+" ("+addr+")");
                    NetworkUtils.addHostRoute(mInterfaceName, addr);
                }
            }
            mPrivateDnsRouteSet = true;
        }
    }
    public void removePrivateDnsRoutes() {
        if (mInterfaceName != null && mPrivateDnsRouteSet) {
            if (DBG) {
                Log.d(TAG, "removePrivateDnsRoutes for " + mNetworkInfo.getTypeName() +
                        " (" + mInterfaceName + ")");
            }
            NetworkUtils.removeHostRoutes(mInterfaceName);
            mPrivateDnsRouteSet = false;
        }
    }
    public void addDefaultRoute() {
        if ((mInterfaceName != null) && (mDefaultGatewayAddr != 0) &&
                mDefaultRouteSet == false) {
            if (DBG) {
                Log.d(TAG, "addDefaultRoute for " + mNetworkInfo.getTypeName() +
                        " (" + mInterfaceName + "), GatewayAddr=" + mDefaultGatewayAddr);
            }
            NetworkUtils.setDefaultRoute(mInterfaceName, mDefaultGatewayAddr);
            mDefaultRouteSet = true;
        }
    }
    public void removeDefaultRoute() {
        if (mInterfaceName != null && mDefaultRouteSet == true) {
            if (DBG) {
                Log.d(TAG, "removeDefaultRoute for " + mNetworkInfo.getTypeName() + " (" +
                        mInterfaceName + ")");
            }
            NetworkUtils.removeDefaultRoute(mInterfaceName);
            mDefaultRouteSet = false;
        }
    }
   public void updateNetworkSettings() {
        String key = getTcpBufferSizesPropName();
        String bufferSizes = SystemProperties.get(key);
        if (bufferSizes.length() == 0) {
            Log.e(TAG, key + " not found in system properties. Using defaults");
            key = "net.tcp.buffersize.default";
            bufferSizes = SystemProperties.get(key);
        }
        if (bufferSizes.length() != 0) {
            if (DBG) {
                Log.v(TAG, "Setting TCP values: [" + bufferSizes
                        + "] which comes from [" + key + "]");
            }
            setBufferSize(bufferSizes);
        }
    }
    public void releaseWakeLock() {
    }
    private void setBufferSize(String bufferSizes) {
        try {
            String[] values = bufferSizes.split(",");
            if (values.length == 6) {
              final String prefix = "/sys/kernel/ipv4/tcp_";
                stringToFile(prefix + "rmem_min", values[0]);
                stringToFile(prefix + "rmem_def", values[1]);
                stringToFile(prefix + "rmem_max", values[2]);
                stringToFile(prefix + "wmem_min", values[3]);
                stringToFile(prefix + "wmem_def", values[4]);
                stringToFile(prefix + "wmem_max", values[5]);
            } else {
                Log.e(TAG, "Invalid buffersize string: " + bufferSizes);
            }
        } catch (IOException e) {
            Log.e(TAG, "Can't set tcp buffer sizes:" + e);
        }
    }
    private void stringToFile(String filename, String string) throws IOException {
        FileWriter out = new FileWriter(filename);
        try {
            out.write(string);
        } finally {
            out.close();
        }
    }
    public void setDetailedState(NetworkInfo.DetailedState state) {
        setDetailedState(state, null, null);
    }
    public void setDetailedState(NetworkInfo.DetailedState state, String reason, String extraInfo) {
        if (DBG) Log.d(TAG, "setDetailed state, old ="+mNetworkInfo.getDetailedState()+" and new state="+state);
        if (state != mNetworkInfo.getDetailedState()) {
            boolean wasConnecting = (mNetworkInfo.getState() == NetworkInfo.State.CONNECTING);
            String lastReason = mNetworkInfo.getReason();
            if (wasConnecting && state == NetworkInfo.DetailedState.CONNECTED && reason == null
                    && lastReason != null)
                reason = lastReason;
            mNetworkInfo.setDetailedState(state, reason, extraInfo);
            Message msg = mTarget.obtainMessage(EVENT_STATE_CHANGED, mNetworkInfo);
            msg.sendToTarget();
        }
    }
    protected void setDetailedStateInternal(NetworkInfo.DetailedState state) {
        mNetworkInfo.setDetailedState(state, null, null);
    }
    public void setTeardownRequested(boolean isRequested) {
        mTeardownRequested = isRequested;
    }
    public boolean isTeardownRequested() {
        return mTeardownRequested;
    }
    protected void sendScanResultsAvailable() {
        Message msg = mTarget.obtainMessage(EVENT_SCAN_RESULTS_AVAILABLE, mNetworkInfo);
        msg.sendToTarget();
    }
    protected void setRoamingStatus(boolean isRoaming) {
        if (isRoaming != mNetworkInfo.isRoaming()) {
            mNetworkInfo.setRoaming(isRoaming);
            Message msg = mTarget.obtainMessage(EVENT_ROAMING_CHANGED, mNetworkInfo);
            msg.sendToTarget();
        }
    }
    protected void setSubtype(int subtype, String subtypeName) {
        if (mNetworkInfo.isConnected()) {
            int oldSubtype = mNetworkInfo.getSubtype();
            if (subtype != oldSubtype) {
                mNetworkInfo.setSubtype(subtype, subtypeName);
                Message msg = mTarget.obtainMessage(
                        EVENT_NETWORK_SUBTYPE_CHANGED, oldSubtype, 0, mNetworkInfo);
                msg.sendToTarget();
            }
        }
    }
    public abstract void startMonitoring();
    public abstract boolean teardown();
    public abstract boolean reconnect();
    public abstract boolean setRadio(boolean turnOn);
    public abstract boolean isAvailable();
    public abstract int startUsingNetworkFeature(String feature, int callingPid, int callingUid);
    public abstract int stopUsingNetworkFeature(String feature, int callingPid, int callingUid);
    public boolean requestRouteToHost(int hostAddress) {
        return false;
    }
    public void interpretScanResultsAvailable() {
    }
}
