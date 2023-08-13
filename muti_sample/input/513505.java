public class ConnectivityService extends IConnectivityManager.Stub {
    private static final boolean DBG = true;
    private static final String TAG = "ConnectivityService";
    private static final int RESTORE_DEFAULT_NETWORK_DELAY = 1 * 60 * 1000;
    private static final String NETWORK_RESTORE_DELAY_PROP_NAME =
            "android.telephony.apn-restore";
    private Tethering mTethering;
    private boolean mTetheringConfigValid = false;
    private NetworkStateTracker mNetTrackers[];
    private List mNetRequestersPids[];
    private int[] mPriorityList;
    private Context mContext;
    private int mNetworkPreference;
    private int mActiveDefaultNetwork = -1;
    private int mNumDnsEntries;
    private boolean mTestMode;
    private static ConnectivityService sServiceInstance;
    private Handler mHandler;
    private List mFeatureUsers;
    private boolean mSystemReady;
    private Intent mInitialBroadcast;
    private static class NetworkAttributes {
        public String mName;
        public int mType;
        public int mRadio;
        public int mPriority;
        public NetworkInfo.State mLastState;
        public NetworkAttributes(String init) {
            String fragments[] = init.split(",");
            mName = fragments[0].toLowerCase();
            mType = Integer.parseInt(fragments[1]);
            mRadio = Integer.parseInt(fragments[2]);
            mPriority = Integer.parseInt(fragments[3]);
            mLastState = NetworkInfo.State.UNKNOWN;
        }
        public boolean isDefault() {
            return (mType == mRadio);
        }
    }
    NetworkAttributes[] mNetAttributes;
    int mNetworksDefined;
    private static class RadioAttributes {
        public int mSimultaneity;
        public int mType;
        public RadioAttributes(String init) {
            String fragments[] = init.split(",");
            mType = Integer.parseInt(fragments[0]);
            mSimultaneity = Integer.parseInt(fragments[1]);
        }
    }
    RadioAttributes[] mRadioAttributes;
    private static class ConnectivityThread extends Thread {
        private Context mContext;
        private ConnectivityThread(Context context) {
            super("ConnectivityThread");
            mContext = context;
        }
        @Override
        public void run() {
            Looper.prepare();
            synchronized (this) {
                sServiceInstance = new ConnectivityService(mContext);
                notifyAll();
            }
            Looper.loop();
        }
        public static ConnectivityService getServiceInstance(Context context) {
            ConnectivityThread thread = new ConnectivityThread(context);
            thread.start();
            synchronized (thread) {
                while (sServiceInstance == null) {
                    try {
                        thread.wait();
                    } catch (InterruptedException ignore) {
                        Slog.e(TAG,
                            "Unexpected InterruptedException while waiting"+
                            " for ConnectivityService thread");
                    }
                }
            }
            return sServiceInstance;
        }
    }
    public static ConnectivityService getInstance(Context context) {
        return ConnectivityThread.getServiceInstance(context);
    }
    private ConnectivityService(Context context) {
        if (DBG) Slog.v(TAG, "ConnectivityService starting up");
        String id = Settings.Secure.getString(context.getContentResolver(),
                Settings.Secure.ANDROID_ID);
        if (id != null && id.length() > 0) {
            String name = new String("android_").concat(id);
            SystemProperties.set("net.hostname", name);
        }
        mContext = context;
        mNetTrackers = new NetworkStateTracker[
                ConnectivityManager.MAX_NETWORK_TYPE+1];
        mHandler = new MyHandler();
        mNetworkPreference = getPersistedNetworkPreference();
        mRadioAttributes = new RadioAttributes[ConnectivityManager.MAX_RADIO_TYPE+1];
        mNetAttributes = new NetworkAttributes[ConnectivityManager.MAX_NETWORK_TYPE+1];
        String[] raStrings = context.getResources().getStringArray(
                com.android.internal.R.array.radioAttributes);
        for (String raString : raStrings) {
            RadioAttributes r = new RadioAttributes(raString);
            if (r.mType > ConnectivityManager.MAX_RADIO_TYPE) {
                Slog.e(TAG, "Error in radioAttributes - ignoring attempt to define type " + r.mType);
                continue;
            }
            if (mRadioAttributes[r.mType] != null) {
                Slog.e(TAG, "Error in radioAttributes - ignoring attempt to redefine type " +
                        r.mType);
                continue;
            }
            mRadioAttributes[r.mType] = r;
        }
        String[] naStrings = context.getResources().getStringArray(
                com.android.internal.R.array.networkAttributes);
        for (String naString : naStrings) {
            try {
                NetworkAttributes n = new NetworkAttributes(naString);
                if (n.mType > ConnectivityManager.MAX_NETWORK_TYPE) {
                    Slog.e(TAG, "Error in networkAttributes - ignoring attempt to define type " +
                            n.mType);
                    continue;
                }
                if (mNetAttributes[n.mType] != null) {
                    Slog.e(TAG, "Error in networkAttributes - ignoring attempt to redefine type " +
                            n.mType);
                    continue;
                }
                if (mRadioAttributes[n.mRadio] == null) {
                    Slog.e(TAG, "Error in networkAttributes - ignoring attempt to use undefined " +
                            "radio " + n.mRadio + " in network type " + n.mType);
                    continue;
                }
                mNetAttributes[n.mType] = n;
                mNetworksDefined++;
            } catch(Exception e) {
            }
        }
        mPriorityList = new int[mNetworksDefined];
        {
            int insertionPoint = mNetworksDefined-1;
            int currentLowest = 0;
            int nextLowest = 0;
            while (insertionPoint > -1) {
                for (NetworkAttributes na : mNetAttributes) {
                    if (na == null) continue;
                    if (na.mPriority < currentLowest) continue;
                    if (na.mPriority > currentLowest) {
                        if (na.mPriority < nextLowest || nextLowest == 0) {
                            nextLowest = na.mPriority;
                        }
                        continue;
                    }
                    mPriorityList[insertionPoint--] = na.mType;
                }
                currentLowest = nextLowest;
                nextLowest = 0;
            }
        }
        mNetRequestersPids = new ArrayList[ConnectivityManager.MAX_NETWORK_TYPE+1];
        for (int i : mPriorityList) {
            mNetRequestersPids[i] = new ArrayList();
        }
        mFeatureUsers = new ArrayList();
        mNumDnsEntries = 0;
        mTestMode = SystemProperties.get("cm.test.mode").equals("true")
                && SystemProperties.get("ro.build.type").equals("eng");
        boolean noMobileData = !getMobileDataEnabled();
        for (int netType : mPriorityList) {
            switch (mNetAttributes[netType].mRadio) {
            case ConnectivityManager.TYPE_WIFI:
                if (DBG) Slog.v(TAG, "Starting Wifi Service.");
                WifiStateTracker wst = new WifiStateTracker(context, mHandler);
                WifiService wifiService = new WifiService(context, wst);
                ServiceManager.addService(Context.WIFI_SERVICE, wifiService);
                wifiService.startWifi();
                mNetTrackers[ConnectivityManager.TYPE_WIFI] = wst;
                wst.startMonitoring();
                break;
            case ConnectivityManager.TYPE_MOBILE:
                mNetTrackers[netType] = new MobileDataStateTracker(context, mHandler,
                    netType, mNetAttributes[netType].mName);
                mNetTrackers[netType].startMonitoring();
                if (noMobileData) {
                    if (DBG) Slog.d(TAG, "tearing down Mobile networks due to setting");
                    mNetTrackers[netType].teardown();
                }
                break;
            default:
                Slog.e(TAG, "Trying to create a DataStateTracker for an unknown radio type " +
                        mNetAttributes[netType].mRadio);
                continue;
            }
        }
        mTethering = new Tethering(mContext, mHandler.getLooper());
        mTetheringConfigValid = (((mNetTrackers[ConnectivityManager.TYPE_MOBILE_DUN] != null) ||
                                  !mTethering.isDunRequired()) &&
                                 (mTethering.getTetherableUsbRegexs().length != 0 ||
                                  mTethering.getTetherableWifiRegexs().length != 0) &&
                                 mTethering.getUpstreamIfaceRegexs().length != 0);
    }
    public synchronized void setNetworkPreference(int preference) {
        enforceChangePermission();
        if (ConnectivityManager.isNetworkTypeValid(preference) &&
                mNetAttributes[preference] != null &&
                mNetAttributes[preference].isDefault()) {
            if (mNetworkPreference != preference) {
                persistNetworkPreference(preference);
                mNetworkPreference = preference;
                enforcePreference();
            }
        }
    }
    public int getNetworkPreference() {
        enforceAccessPermission();
        return mNetworkPreference;
    }
    private void persistNetworkPreference(int networkPreference) {
        final ContentResolver cr = mContext.getContentResolver();
        Settings.Secure.putInt(cr, Settings.Secure.NETWORK_PREFERENCE,
                networkPreference);
    }
    private int getPersistedNetworkPreference() {
        final ContentResolver cr = mContext.getContentResolver();
        final int networkPrefSetting = Settings.Secure
                .getInt(cr, Settings.Secure.NETWORK_PREFERENCE, -1);
        if (networkPrefSetting != -1) {
            return networkPrefSetting;
        }
        return ConnectivityManager.DEFAULT_NETWORK_PREFERENCE;
    }
    private void enforcePreference() {
        if (mNetTrackers[mNetworkPreference].getNetworkInfo().isConnected())
            return;
        if (!mNetTrackers[mNetworkPreference].isAvailable())
            return;
        for (int t=0; t <= ConnectivityManager.MAX_RADIO_TYPE; t++) {
            if (t != mNetworkPreference && mNetTrackers[t] != null &&
                    mNetTrackers[t].getNetworkInfo().isConnected()) {
                if (DBG) {
                    Slog.d(TAG, "tearing down " +
                            mNetTrackers[t].getNetworkInfo() +
                            " in enforcePreference");
                }
                teardown(mNetTrackers[t]);
            }
        }
    }
    private boolean teardown(NetworkStateTracker netTracker) {
        if (netTracker.teardown()) {
            netTracker.setTeardownRequested(true);
            return true;
        } else {
            return false;
        }
    }
    public NetworkInfo getActiveNetworkInfo() {
        enforceAccessPermission();
        for (int type=0; type <= ConnectivityManager.MAX_NETWORK_TYPE; type++) {
            if (mNetAttributes[type] == null || !mNetAttributes[type].isDefault()) {
                continue;
            }
            NetworkStateTracker t = mNetTrackers[type];
            NetworkInfo info = t.getNetworkInfo();
            if (info.isConnected()) {
                if (DBG && type != mActiveDefaultNetwork) Slog.e(TAG,
                        "connected default network is not " +
                        "mActiveDefaultNetwork!");
                return info;
            }
        }
        return null;
    }
    public NetworkInfo getNetworkInfo(int networkType) {
        enforceAccessPermission();
        if (ConnectivityManager.isNetworkTypeValid(networkType)) {
            NetworkStateTracker t = mNetTrackers[networkType];
            if (t != null)
                return t.getNetworkInfo();
        }
        return null;
    }
    public NetworkInfo[] getAllNetworkInfo() {
        enforceAccessPermission();
        NetworkInfo[] result = new NetworkInfo[mNetworksDefined];
        int i = 0;
        for (NetworkStateTracker t : mNetTrackers) {
            if(t != null) result[i++] = t.getNetworkInfo();
        }
        return result;
    }
    public boolean setRadios(boolean turnOn) {
        boolean result = true;
        enforceChangePermission();
        for (NetworkStateTracker t : mNetTrackers) {
            if (t != null) result = t.setRadio(turnOn) && result;
        }
        return result;
    }
    public boolean setRadio(int netType, boolean turnOn) {
        enforceChangePermission();
        if (!ConnectivityManager.isNetworkTypeValid(netType)) {
            return false;
        }
        NetworkStateTracker tracker = mNetTrackers[netType];
        return tracker != null && tracker.setRadio(turnOn);
    }
    private class FeatureUser implements IBinder.DeathRecipient {
        int mNetworkType;
        String mFeature;
        IBinder mBinder;
        int mPid;
        int mUid;
        long mCreateTime;
        FeatureUser(int type, String feature, IBinder binder) {
            super();
            mNetworkType = type;
            mFeature = feature;
            mBinder = binder;
            mPid = getCallingPid();
            mUid = getCallingUid();
            mCreateTime = System.currentTimeMillis();
            try {
                mBinder.linkToDeath(this, 0);
            } catch (RemoteException e) {
                binderDied();
            }
        }
        void unlinkDeathRecipient() {
            mBinder.unlinkToDeath(this, 0);
        }
        public void binderDied() {
            Slog.d(TAG, "ConnectivityService FeatureUser binderDied(" +
                    mNetworkType + ", " + mFeature + ", " + mBinder + "), created " +
                    (System.currentTimeMillis() - mCreateTime) + " mSec ago");
            stopUsingNetworkFeature(this, false);
        }
        public void expire() {
            Slog.d(TAG, "ConnectivityService FeatureUser expire(" +
                    mNetworkType + ", " + mFeature + ", " + mBinder +"), created " +
                    (System.currentTimeMillis() - mCreateTime) + " mSec ago");
            stopUsingNetworkFeature(this, false);
        }
        public String toString() {
            return "FeatureUser("+mNetworkType+","+mFeature+","+mPid+","+mUid+"), created " +
                    (System.currentTimeMillis() - mCreateTime) + " mSec ago";
        }
    }
    public int startUsingNetworkFeature(int networkType, String feature,
            IBinder binder) {
        if (DBG) {
            Slog.d(TAG, "startUsingNetworkFeature for net " + networkType +
                    ": " + feature);
        }
        enforceChangePermission();
        if (!ConnectivityManager.isNetworkTypeValid(networkType) ||
                mNetAttributes[networkType] == null) {
            return Phone.APN_REQUEST_FAILED;
        }
        FeatureUser f = new FeatureUser(networkType, feature, binder);
        int usedNetworkType = networkType;
        if(networkType == ConnectivityManager.TYPE_MOBILE) {
            if (!getMobileDataEnabled()) {
                if (DBG) Slog.d(TAG, "requested special network with data disabled - rejected");
                return Phone.APN_TYPE_NOT_AVAILABLE;
            }
            if (TextUtils.equals(feature, Phone.FEATURE_ENABLE_MMS)) {
                usedNetworkType = ConnectivityManager.TYPE_MOBILE_MMS;
            } else if (TextUtils.equals(feature, Phone.FEATURE_ENABLE_SUPL)) {
                usedNetworkType = ConnectivityManager.TYPE_MOBILE_SUPL;
            } else if (TextUtils.equals(feature, Phone.FEATURE_ENABLE_DUN)) {
                usedNetworkType = ConnectivityManager.TYPE_MOBILE_DUN;
            } else if (TextUtils.equals(feature, Phone.FEATURE_ENABLE_HIPRI)) {
                usedNetworkType = ConnectivityManager.TYPE_MOBILE_HIPRI;
            }
        }
        NetworkStateTracker network = mNetTrackers[usedNetworkType];
        if (network != null) {
            if (usedNetworkType != networkType) {
                Integer currentPid = new Integer(getCallingPid());
                NetworkStateTracker radio = mNetTrackers[networkType];
                NetworkInfo ni = network.getNetworkInfo();
                if (ni.isAvailable() == false) {
                    if (DBG) Slog.d(TAG, "special network not available");
                    return Phone.APN_TYPE_NOT_AVAILABLE;
                }
                synchronized(this) {
                    mFeatureUsers.add(f);
                    if (!mNetRequestersPids[usedNetworkType].contains(currentPid)) {
                        mNetRequestersPids[usedNetworkType].add(currentPid);
                    }
                }
                mHandler.sendMessageDelayed(mHandler.obtainMessage(
                        NetworkStateTracker.EVENT_RESTORE_DEFAULT_NETWORK,
                        f), getRestoreDefaultNetworkDelay());
                if ((ni.isConnectedOrConnecting() == true) &&
                        !network.isTeardownRequested()) {
                    if (ni.isConnected() == true) {
                        handleDnsConfigurationChange();
                        if (DBG) Slog.d(TAG, "special network already active");
                        return Phone.APN_ALREADY_ACTIVE;
                    }
                    if (DBG) Slog.d(TAG, "special network already connecting");
                    return Phone.APN_REQUEST_STARTED;
                }
                if (DBG) Slog.d(TAG, "reconnecting to special network");
                network.reconnect();
                return Phone.APN_REQUEST_STARTED;
            } else {
                synchronized(this) {
                    mFeatureUsers.add(f);
                }
                mHandler.sendMessageDelayed(mHandler.obtainMessage(
                        NetworkStateTracker.EVENT_RESTORE_DEFAULT_NETWORK,
                        f), getRestoreDefaultNetworkDelay());
                return network.startUsingNetworkFeature(feature,
                        getCallingPid(), getCallingUid());
            }
        }
        return Phone.APN_TYPE_NOT_AVAILABLE;
    }
    public int stopUsingNetworkFeature(int networkType, String feature) {
        enforceChangePermission();
        int pid = getCallingPid();
        int uid = getCallingUid();
        FeatureUser u = null;
        boolean found = false;
        synchronized(this) {
            for (int i = 0; i < mFeatureUsers.size() ; i++) {
                u = (FeatureUser)mFeatureUsers.get(i);
                if (uid == u.mUid && pid == u.mPid &&
                        networkType == u.mNetworkType &&
                        TextUtils.equals(feature, u.mFeature)) {
                    found = true;
                    break;
                }
            }
        }
        if (found && u != null) {
            return stopUsingNetworkFeature(u, true);
        } else {
            if (DBG) Slog.d(TAG, "ignoring stopUsingNetworkFeature - not a live request");
            return 1;
        }
    }
    private int stopUsingNetworkFeature(FeatureUser u, boolean ignoreDups) {
        int networkType = u.mNetworkType;
        String feature = u.mFeature;
        int pid = u.mPid;
        int uid = u.mUid;
        NetworkStateTracker tracker = null;
        boolean callTeardown = false;  
        if (DBG) {
            Slog.d(TAG, "stopUsingNetworkFeature for net " + networkType +
                    ": " + feature);
        }
        if (!ConnectivityManager.isNetworkTypeValid(networkType)) {
            return -1;
        }
        synchronized(this) {
            if (!mFeatureUsers.contains(u)) {
                if (DBG) Slog.d(TAG, "ignoring - this process has no outstanding requests");
                return 1;
            }
            u.unlinkDeathRecipient();
            mFeatureUsers.remove(mFeatureUsers.indexOf(u));
            if (ignoreDups == false) {
                for (int i = 0; i < mFeatureUsers.size() ; i++) {
                    FeatureUser x = (FeatureUser)mFeatureUsers.get(i);
                    if (x.mUid == u.mUid && x.mPid == u.mPid &&
                            x.mNetworkType == u.mNetworkType &&
                            TextUtils.equals(x.mFeature, u.mFeature)) {
                        if (DBG) Slog.d(TAG, "ignoring stopUsingNetworkFeature as dup is found");
                        return 1;
                    }
                }
            }
            int usedNetworkType = networkType;
            if (networkType == ConnectivityManager.TYPE_MOBILE) {
                if (TextUtils.equals(feature, Phone.FEATURE_ENABLE_MMS)) {
                    usedNetworkType = ConnectivityManager.TYPE_MOBILE_MMS;
                } else if (TextUtils.equals(feature, Phone.FEATURE_ENABLE_SUPL)) {
                    usedNetworkType = ConnectivityManager.TYPE_MOBILE_SUPL;
                } else if (TextUtils.equals(feature, Phone.FEATURE_ENABLE_DUN)) {
                    usedNetworkType = ConnectivityManager.TYPE_MOBILE_DUN;
                } else if (TextUtils.equals(feature, Phone.FEATURE_ENABLE_HIPRI)) {
                    usedNetworkType = ConnectivityManager.TYPE_MOBILE_HIPRI;
                }
            }
            tracker =  mNetTrackers[usedNetworkType];
            if (tracker == null) {
                if (DBG) Slog.d(TAG, "ignoring - no known tracker for net type " + usedNetworkType);
                return -1;
            }
            if (usedNetworkType != networkType) {
                Integer currentPid = new Integer(pid);
                mNetRequestersPids[usedNetworkType].remove(currentPid);
                reassessPidDns(pid, true);
                if (mNetRequestersPids[usedNetworkType].size() != 0) {
                    if (DBG) Slog.d(TAG, "not tearing down special network - " +
                           "others still using it");
                    return 1;
                }
                callTeardown = true;
            }
        }
        if (DBG) Slog.d(TAG, "Doing network teardown");
        if (callTeardown) {
            tracker.teardown();
            return 1;
        } else {
            return tracker.stopUsingNetworkFeature(feature, pid, uid);
        }
    }
    public boolean requestRouteToHost(int networkType, int hostAddress) {
        enforceChangePermission();
        if (!ConnectivityManager.isNetworkTypeValid(networkType)) {
            return false;
        }
        NetworkStateTracker tracker = mNetTrackers[networkType];
        if (tracker == null || !tracker.getNetworkInfo().isConnected() ||
                tracker.isTeardownRequested()) {
            if (DBG) {
                Slog.d(TAG, "requestRouteToHost on down network (" + networkType + ") - dropped");
            }
            return false;
        }
        return tracker.requestRouteToHost(hostAddress);
    }
    public boolean getBackgroundDataSetting() {
        return Settings.Secure.getInt(mContext.getContentResolver(),
                Settings.Secure.BACKGROUND_DATA, 1) == 1;
    }
    public void setBackgroundDataSetting(boolean allowBackgroundDataUsage) {
        mContext.enforceCallingOrSelfPermission(
                android.Manifest.permission.CHANGE_BACKGROUND_DATA_SETTING,
                "ConnectivityService");
        if (getBackgroundDataSetting() == allowBackgroundDataUsage) return;
        Settings.Secure.putInt(mContext.getContentResolver(),
                Settings.Secure.BACKGROUND_DATA,
                allowBackgroundDataUsage ? 1 : 0);
        Intent broadcast = new Intent(
                ConnectivityManager.ACTION_BACKGROUND_DATA_SETTING_CHANGED);
        mContext.sendBroadcast(broadcast);
    }
    public boolean getMobileDataEnabled() {
        enforceAccessPermission();
        boolean retVal = Settings.Secure.getInt(mContext.getContentResolver(),
                Settings.Secure.MOBILE_DATA, 1) == 1;
        if (DBG) Slog.d(TAG, "getMobileDataEnabled returning " + retVal);
        return retVal;
    }
    public synchronized void setMobileDataEnabled(boolean enabled) {
        enforceChangePermission();
        if (DBG) Slog.d(TAG, "setMobileDataEnabled(" + enabled + ")");
        if (getMobileDataEnabled() == enabled) return;
        Settings.Secure.putInt(mContext.getContentResolver(),
                Settings.Secure.MOBILE_DATA, enabled ? 1 : 0);
        if (enabled) {
            if (mNetTrackers[ConnectivityManager.TYPE_MOBILE] != null) {
                if (DBG) Slog.d(TAG, "starting up " + mNetTrackers[ConnectivityManager.TYPE_MOBILE]);
                mNetTrackers[ConnectivityManager.TYPE_MOBILE].reconnect();
            }
        } else {
            for (NetworkStateTracker nt : mNetTrackers) {
                if (nt == null) continue;
                int netType = nt.getNetworkInfo().getType();
                if (mNetAttributes[netType].mRadio == ConnectivityManager.TYPE_MOBILE) {
                    if (DBG) Slog.d(TAG, "tearing down " + nt);
                    nt.teardown();
                }
            }
        }
    }
    private int getNumConnectedNetworks() {
        int numConnectedNets = 0;
        for (NetworkStateTracker nt : mNetTrackers) {
            if (nt != null && nt.getNetworkInfo().isConnected() &&
                    !nt.isTeardownRequested()) {
                ++numConnectedNets;
            }
        }
        return numConnectedNets;
    }
    private void enforceAccessPermission() {
        mContext.enforceCallingOrSelfPermission(
                android.Manifest.permission.ACCESS_NETWORK_STATE,
                "ConnectivityService");
    }
    private void enforceChangePermission() {
        mContext.enforceCallingOrSelfPermission(
                android.Manifest.permission.CHANGE_NETWORK_STATE,
                "ConnectivityService");
    }
    private void enforceTetherChangePermission() {
        mContext.enforceCallingOrSelfPermission(
                android.Manifest.permission.CHANGE_NETWORK_STATE,
                "ConnectivityService");
    }
    private void enforceTetherAccessPermission() {
        mContext.enforceCallingOrSelfPermission(
                android.Manifest.permission.ACCESS_NETWORK_STATE,
                "ConnectivityService");
    }
    private void handleDisconnect(NetworkInfo info) {
        int prevNetType = info.getType();
        mNetTrackers[prevNetType].setTeardownRequested(false);
        if (!mNetAttributes[prevNetType].isDefault()) {
            List pids = mNetRequestersPids[prevNetType];
            for (int i = 0; i<pids.size(); i++) {
                Integer pid = (Integer)pids.get(i);
                reassessPidDns(pid.intValue(), false);
            }
        }
        Intent intent = new Intent(ConnectivityManager.CONNECTIVITY_ACTION);
        intent.addFlags(Intent.FLAG_RECEIVER_REPLACE_PENDING);
        intent.putExtra(ConnectivityManager.EXTRA_NETWORK_INFO, info);
        if (info.isFailover()) {
            intent.putExtra(ConnectivityManager.EXTRA_IS_FAILOVER, true);
            info.setFailover(false);
        }
        if (info.getReason() != null) {
            intent.putExtra(ConnectivityManager.EXTRA_REASON, info.getReason());
        }
        if (info.getExtraInfo() != null) {
            intent.putExtra(ConnectivityManager.EXTRA_EXTRA_INFO,
                    info.getExtraInfo());
        }
        NetworkStateTracker newNet = null;
        if (mNetAttributes[prevNetType].isDefault()) {
            newNet = tryFailover(prevNetType);
            if (newNet != null) {
                NetworkInfo switchTo = newNet.getNetworkInfo();
                intent.putExtra(ConnectivityManager.EXTRA_OTHER_NETWORK_INFO, switchTo);
            } else {
                intent.putExtra(ConnectivityManager.EXTRA_NO_CONNECTIVITY, true);
            }
        }
        handleConnectivityChange();
        sendStickyBroadcast(intent);
        if (newNet != null && newNet.getNetworkInfo().isConnected()) {
            sendConnectedBroadcast(newNet.getNetworkInfo());
        }
    }
    private NetworkStateTracker tryFailover(int prevNetType) {
        NetworkStateTracker newNet = null;
        if (mNetAttributes[prevNetType].isDefault()) {
            if (mActiveDefaultNetwork == prevNetType) {
                mActiveDefaultNetwork = -1;
            }
            int newType = -1;
            int newPriority = -1;
            boolean noMobileData = !getMobileDataEnabled();
            for (int checkType=0; checkType <= ConnectivityManager.MAX_NETWORK_TYPE; checkType++) {
                if (checkType == prevNetType) continue;
                if (mNetAttributes[checkType] == null) continue;
                if (mNetAttributes[checkType].mRadio == ConnectivityManager.TYPE_MOBILE &&
                        noMobileData) {
                    if (DBG) {
                        Slog.d(TAG, "not failing over to mobile type " + checkType +
                                " because Mobile Data Disabled");
                    }
                    continue;
                }
                if (mNetAttributes[checkType].isDefault()) {
                    if (checkType == mNetworkPreference) {
                        newType = checkType;
                        break;
                    }
                    if (mNetAttributes[checkType].mPriority > newPriority) {
                        newType = checkType;
                        newPriority = mNetAttributes[newType].mPriority;
                    }
                }
            }
            if (newType != -1) {
                newNet = mNetTrackers[newType];
                if (newNet.isAvailable()) {
                    NetworkInfo switchTo = newNet.getNetworkInfo();
                    switchTo.setFailover(true);
                    if (!switchTo.isConnectedOrConnecting() ||
                            newNet.isTeardownRequested()) {
                        newNet.reconnect();
                    }
                    if (DBG) {
                        if (switchTo.isConnected()) {
                            Slog.v(TAG, "Switching to already connected " +
                                    switchTo.getTypeName());
                        } else {
                            Slog.v(TAG, "Attempting to switch to " +
                                    switchTo.getTypeName());
                        }
                    }
                } else {
                    newNet.reconnect();
                    newNet = null; 
                }
            }
        }
        return newNet;
    }
    private void sendConnectedBroadcast(NetworkInfo info) {
        Intent intent = new Intent(ConnectivityManager.CONNECTIVITY_ACTION);
        intent.addFlags(Intent.FLAG_RECEIVER_REPLACE_PENDING);
        intent.putExtra(ConnectivityManager.EXTRA_NETWORK_INFO, info);
        if (info.isFailover()) {
            intent.putExtra(ConnectivityManager.EXTRA_IS_FAILOVER, true);
            info.setFailover(false);
        }
        if (info.getReason() != null) {
            intent.putExtra(ConnectivityManager.EXTRA_REASON, info.getReason());
        }
        if (info.getExtraInfo() != null) {
            intent.putExtra(ConnectivityManager.EXTRA_EXTRA_INFO,
                    info.getExtraInfo());
        }
        sendStickyBroadcast(intent);
    }
    private void handleConnectionFailure(NetworkInfo info) {
        mNetTrackers[info.getType()].setTeardownRequested(false);
        String reason = info.getReason();
        String extraInfo = info.getExtraInfo();
        if (DBG) {
            String reasonText;
            if (reason == null) {
                reasonText = ".";
            } else {
                reasonText = " (" + reason + ").";
            }
            Slog.v(TAG, "Attempt to connect to " + info.getTypeName() +
                    " failed" + reasonText);
        }
        Intent intent = new Intent(ConnectivityManager.CONNECTIVITY_ACTION);
        intent.addFlags(Intent.FLAG_RECEIVER_REPLACE_PENDING);
        intent.putExtra(ConnectivityManager.EXTRA_NETWORK_INFO, info);
        if (getActiveNetworkInfo() == null) {
            intent.putExtra(ConnectivityManager.EXTRA_NO_CONNECTIVITY, true);
        }
        if (reason != null) {
            intent.putExtra(ConnectivityManager.EXTRA_REASON, reason);
        }
        if (extraInfo != null) {
            intent.putExtra(ConnectivityManager.EXTRA_EXTRA_INFO, extraInfo);
        }
        if (info.isFailover()) {
            intent.putExtra(ConnectivityManager.EXTRA_IS_FAILOVER, true);
            info.setFailover(false);
        }
        NetworkStateTracker newNet = null;
        if (mNetAttributes[info.getType()].isDefault()) {
            newNet = tryFailover(info.getType());
            if (newNet != null) {
                NetworkInfo switchTo = newNet.getNetworkInfo();
                intent.putExtra(ConnectivityManager.EXTRA_OTHER_NETWORK_INFO, switchTo);
            } else {
                intent.putExtra(ConnectivityManager.EXTRA_NO_CONNECTIVITY, true);
            }
        }
        handleConnectivityChange();
        sendStickyBroadcast(intent);
        if (newNet != null && newNet.getNetworkInfo().isConnected()) {
            sendConnectedBroadcast(newNet.getNetworkInfo());
        }
    }
    private void sendStickyBroadcast(Intent intent) {
        synchronized(this) {
            if (!mSystemReady) {
                mInitialBroadcast = new Intent(intent);
            }
            intent.addFlags(Intent.FLAG_RECEIVER_REGISTERED_ONLY_BEFORE_BOOT);
            mContext.sendStickyBroadcast(intent);
        }
    }
    void systemReady() {
        synchronized(this) {
            mSystemReady = true;
            if (mInitialBroadcast != null) {
                mContext.sendStickyBroadcast(mInitialBroadcast);
                mInitialBroadcast = null;
            }
        }
    }
    private void handleConnect(NetworkInfo info) {
        int type = info.getType();
        boolean isFailover = info.isFailover();
        NetworkStateTracker thisNet = mNetTrackers[type];
        if (mNetAttributes[type].isDefault()) {
            if (mActiveDefaultNetwork != -1 && mActiveDefaultNetwork != type) {
                if ((type != mNetworkPreference &&
                        mNetAttributes[mActiveDefaultNetwork].mPriority >
                        mNetAttributes[type].mPriority) ||
                        mNetworkPreference == mActiveDefaultNetwork) {
                        if (DBG) Slog.v(TAG, "Not broadcasting CONNECT_ACTION " +
                                "to torn down network " + info.getTypeName());
                        teardown(thisNet);
                        return;
                } else {
                    NetworkStateTracker otherNet =
                            mNetTrackers[mActiveDefaultNetwork];
                    if (DBG) Slog.v(TAG, "Policy requires " +
                            otherNet.getNetworkInfo().getTypeName() +
                            " teardown");
                    if (!teardown(otherNet)) {
                        Slog.e(TAG, "Network declined teardown request");
                        return;
                    }
                    if (isFailover) {
                        otherNet.releaseWakeLock();
                    }
                }
            }
            mActiveDefaultNetwork = type;
        }
        thisNet.setTeardownRequested(false);
        thisNet.updateNetworkSettings();
        handleConnectivityChange();
        sendConnectedBroadcast(info);
    }
    private void handleScanResultsAvailable(NetworkInfo info) {
        int networkType = info.getType();
        if (networkType != ConnectivityManager.TYPE_WIFI) {
            if (DBG) Slog.v(TAG, "Got ScanResultsAvailable for " +
                    info.getTypeName() + " network. Don't know how to handle.");
        }
        mNetTrackers[networkType].interpretScanResultsAvailable();
    }
    private void handleNotificationChange(boolean visible, int id,
            Notification notification) {
        NotificationManager notificationManager = (NotificationManager) mContext
                .getSystemService(Context.NOTIFICATION_SERVICE);
        if (visible) {
            notificationManager.notify(id, notification);
        } else {
            notificationManager.cancel(id);
        }
    }
    private void handleConnectivityChange() {
        handleDnsConfigurationChange();
        for (int netType : mPriorityList) {
            if (mNetTrackers[netType].getNetworkInfo().isConnected()) {
                if (mNetAttributes[netType].isDefault()) {
                    mNetTrackers[netType].addDefaultRoute();
                } else {
                    mNetTrackers[netType].addPrivateDnsRoutes();
                }
            } else {
                if (mNetAttributes[netType].isDefault()) {
                    mNetTrackers[netType].removeDefaultRoute();
                } else {
                    mNetTrackers[netType].removePrivateDnsRoutes();
                }
            }
        }
    }
    private void reassessPidDns(int myPid, boolean doBump)
    {
        if (DBG) Slog.d(TAG, "reassessPidDns for pid " + myPid);
        for(int i : mPriorityList) {
            if (mNetAttributes[i].isDefault()) {
                continue;
            }
            NetworkStateTracker nt = mNetTrackers[i];
            if (nt.getNetworkInfo().isConnected() &&
                    !nt.isTeardownRequested()) {
                List pids = mNetRequestersPids[i];
                for (int j=0; j<pids.size(); j++) {
                    Integer pid = (Integer)pids.get(j);
                    if (pid.intValue() == myPid) {
                        String[] dnsList = nt.getNameServers();
                        writePidDns(dnsList, myPid);
                        if (doBump) {
                            bumpDns();
                        }
                        return;
                    }
                }
           }
        }
        for (int i = 1; ; i++) {
            String prop = "net.dns" + i + "." + myPid;
            if (SystemProperties.get(prop).length() == 0) {
                if (doBump) {
                    bumpDns();
                }
                return;
            }
            SystemProperties.set(prop, "");
        }
    }
    private void writePidDns(String[] dnsList, int pid) {
        int j = 1;
        for (String dns : dnsList) {
            if (dns != null && !TextUtils.equals(dns, "0.0.0.0")) {
                SystemProperties.set("net.dns" + j++ + "." + pid, dns);
            }
        }
    }
    private void bumpDns() {
        String propVal = SystemProperties.get("net.dnschange");
        int n = 0;
        if (propVal.length() != 0) {
            try {
                n = Integer.parseInt(propVal);
            } catch (NumberFormatException e) {}
        }
        SystemProperties.set("net.dnschange", "" + (n+1));
    }
    private void handleDnsConfigurationChange() {
        for (int x = mPriorityList.length-1; x>= 0; x--) {
            int netType = mPriorityList[x];
            NetworkStateTracker nt = mNetTrackers[netType];
            if (nt != null && nt.getNetworkInfo().isConnected() &&
                    !nt.isTeardownRequested()) {
                String[] dnsList = nt.getNameServers();
                if (mNetAttributes[netType].isDefault()) {
                    int j = 1;
                    for (String dns : dnsList) {
                        if (dns != null && !TextUtils.equals(dns, "0.0.0.0")) {
                            if (DBG) {
                                Slog.d(TAG, "adding dns " + dns + " for " +
                                        nt.getNetworkInfo().getTypeName());
                            }
                            SystemProperties.set("net.dns" + j++, dns);
                        }
                    }
                    for (int k=j ; k<mNumDnsEntries; k++) {
                        if (DBG) Slog.d(TAG, "erasing net.dns" + k);
                        SystemProperties.set("net.dns" + k, "");
                    }
                    mNumDnsEntries = j;
                } else {
                    List pids = mNetRequestersPids[netType];
                    for (int y=0; y< pids.size(); y++) {
                        Integer pid = (Integer)pids.get(y);
                        writePidDns(dnsList, pid.intValue());
                    }
                }
            }
        }
        bumpDns();
    }
    private int getRestoreDefaultNetworkDelay() {
        String restoreDefaultNetworkDelayStr = SystemProperties.get(
                NETWORK_RESTORE_DELAY_PROP_NAME);
        if(restoreDefaultNetworkDelayStr != null &&
                restoreDefaultNetworkDelayStr.length() != 0) {
            try {
                return Integer.valueOf(restoreDefaultNetworkDelayStr);
            } catch (NumberFormatException e) {
            }
        }
        return RESTORE_DEFAULT_NETWORK_DELAY;
    }
    @Override
    protected void dump(FileDescriptor fd, PrintWriter pw, String[] args) {
        if (mContext.checkCallingOrSelfPermission(
                android.Manifest.permission.DUMP)
                != PackageManager.PERMISSION_GRANTED) {
            pw.println("Permission Denial: can't dump ConnectivityService " +
                    "from from pid=" + Binder.getCallingPid() + ", uid=" +
                    Binder.getCallingUid());
            return;
        }
        pw.println();
        for (NetworkStateTracker nst : mNetTrackers) {
            if (nst != null) {
                if (nst.getNetworkInfo().isConnected()) {
                    pw.println("Active network: " + nst.getNetworkInfo().
                            getTypeName());
                }
                pw.println(nst.getNetworkInfo());
                pw.println(nst);
                pw.println();
            }
        }
        pw.println("Network Requester Pids:");
        for (int net : mPriorityList) {
            String pidString = net + ": ";
            for (Object pid : mNetRequestersPids[net]) {
                pidString = pidString + pid.toString() + ", ";
            }
            pw.println(pidString);
        }
        pw.println();
        pw.println("FeatureUsers:");
        for (Object requester : mFeatureUsers) {
            pw.println(requester.toString());
        }
        pw.println();
        mTethering.dump(fd, pw, args);
    }
    private class MyHandler extends Handler {
        @Override
        public void handleMessage(Message msg) {
            NetworkInfo info;
            switch (msg.what) {
                case NetworkStateTracker.EVENT_STATE_CHANGED:
                    info = (NetworkInfo) msg.obj;
                    int type = info.getType();
                    NetworkInfo.State state = info.getState();
                    if (mNetAttributes[type].mLastState == state &&
                            mNetAttributes[type].mRadio == ConnectivityManager.TYPE_WIFI) {
                        if (DBG) {
                            Slog.d(TAG, "Dropping ConnectivityChange for " +
                                    info.getTypeName() + ": " +
                                    state + "/" + info.getDetailedState());
                        }
                        return;
                    }
                    mNetAttributes[type].mLastState = state;
                    if (DBG) Slog.d(TAG, "ConnectivityChange for " +
                            info.getTypeName() + ": " +
                            state + "/" + info.getDetailedState());
                    int eventLogParam = (info.getType() & 0x7) |
                            ((info.getDetailedState().ordinal() & 0x3f) << 3) |
                            (info.getSubtype() << 9);
                    EventLog.writeEvent(EventLogTags.CONNECTIVITY_STATE_CHANGED,
                            eventLogParam);
                    if (info.getDetailedState() ==
                            NetworkInfo.DetailedState.FAILED) {
                        handleConnectionFailure(info);
                    } else if (state == NetworkInfo.State.DISCONNECTED) {
                        handleDisconnect(info);
                    } else if (state == NetworkInfo.State.SUSPENDED) {
                        handleDisconnect(info);
                    } else if (state == NetworkInfo.State.CONNECTED) {
                        handleConnect(info);
                    }
                    break;
                case NetworkStateTracker.EVENT_SCAN_RESULTS_AVAILABLE:
                    info = (NetworkInfo) msg.obj;
                    handleScanResultsAvailable(info);
                    break;
                case NetworkStateTracker.EVENT_NOTIFICATION_CHANGED:
                    handleNotificationChange(msg.arg1 == 1, msg.arg2,
                            (Notification) msg.obj);
                case NetworkStateTracker.EVENT_CONFIGURATION_CHANGED:
                    handleDnsConfigurationChange();
                    break;
                case NetworkStateTracker.EVENT_ROAMING_CHANGED:
                    break;
                case NetworkStateTracker.EVENT_NETWORK_SUBTYPE_CHANGED:
                    break;
                case NetworkStateTracker.EVENT_RESTORE_DEFAULT_NETWORK:
                    FeatureUser u = (FeatureUser)msg.obj;
                    u.expire();
                    break;
            }
        }
    }
    public int tether(String iface) {
        enforceTetherChangePermission();
        if (isTetheringSupported()) {
            return mTethering.tether(iface);
        } else {
            return ConnectivityManager.TETHER_ERROR_UNSUPPORTED;
        }
    }
    public int untether(String iface) {
        enforceTetherChangePermission();
        if (isTetheringSupported()) {
            return mTethering.untether(iface);
        } else {
            return ConnectivityManager.TETHER_ERROR_UNSUPPORTED;
        }
    }
    public int getLastTetherError(String iface) {
        enforceTetherAccessPermission();
        if (isTetheringSupported()) {
            return mTethering.getLastTetherError(iface);
        } else {
            return ConnectivityManager.TETHER_ERROR_UNSUPPORTED;
        }
    }
    public String[] getTetherableUsbRegexs() {
        enforceTetherAccessPermission();
        if (isTetheringSupported()) {
            return mTethering.getTetherableUsbRegexs();
        } else {
            return new String[0];
        }
    }
    public String[] getTetherableWifiRegexs() {
        enforceTetherAccessPermission();
        if (isTetheringSupported()) {
            return mTethering.getTetherableWifiRegexs();
        } else {
            return new String[0];
        }
    }
    public String[] getTetherableIfaces() {
        enforceTetherAccessPermission();
        return mTethering.getTetherableIfaces();
    }
    public String[] getTetheredIfaces() {
        enforceTetherAccessPermission();
        return mTethering.getTetheredIfaces();
    }
    public String[] getTetheringErroredIfaces() {
        enforceTetherAccessPermission();
        return mTethering.getErroredIfaces();
    }
    public boolean isTetheringSupported() {
        enforceTetherAccessPermission();
        int defaultVal = (SystemProperties.get("ro.tether.denied").equals("true") ? 0 : 1);
        boolean tetherEnabledInSettings = (Settings.Secure.getInt(mContext.getContentResolver(),
                Settings.Secure.TETHER_SUPPORTED, defaultVal) != 0);
        return tetherEnabledInSettings && mTetheringConfigValid;
    }
}
