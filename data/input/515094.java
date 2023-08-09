public class MobileDataStateTracker extends NetworkStateTracker {
    private static final String TAG = "MobileDataStateTracker";
    private static final boolean DBG = true;
    private Phone.DataState mMobileDataState;
    private ITelephony mPhoneService;
    private String mApnType;
    private String mApnTypeToWatchFor;
    private String mApnName;
    private boolean mEnabled;
    private BroadcastReceiver mStateReceiver;
    public MobileDataStateTracker(Context context, Handler target, int netType, String tag) {
        super(context, target, netType,
                TelephonyManager.getDefault().getNetworkType(), tag,
                TelephonyManager.getDefault().getNetworkTypeName());
        mApnType = networkTypeToApnType(netType);
        if (TextUtils.equals(mApnType, Phone.APN_TYPE_HIPRI)) {
            mApnTypeToWatchFor = Phone.APN_TYPE_DEFAULT;
        } else {
            mApnTypeToWatchFor = mApnType;
        }
        mPhoneService = null;
        if(netType == ConnectivityManager.TYPE_MOBILE) {
            mEnabled = true;
        } else {
            mEnabled = false;
        }
        mDnsPropNames = new String[] {
                "net.rmnet0.dns1",
                "net.rmnet0.dns2",
                "net.eth0.dns1",
                "net.eth0.dns2",
                "net.eth0.dns3",
                "net.eth0.dns4",
                "net.gprs.dns1",
                "net.gprs.dns2",
                "net.ppp0.dns1",
                "net.ppp0.dns2"};
    }
    public void startMonitoring() {
        IntentFilter filter =
                new IntentFilter(TelephonyIntents.ACTION_ANY_DATA_CONNECTION_STATE_CHANGED);
        filter.addAction(TelephonyIntents.ACTION_DATA_CONNECTION_FAILED);
        filter.addAction(TelephonyIntents.ACTION_SERVICE_STATE_CHANGED);
        mStateReceiver = new MobileDataStateReceiver();
        Intent intent = mContext.registerReceiver(mStateReceiver, filter);
        if (intent != null)
            mMobileDataState = getMobileDataState(intent);
        else
            mMobileDataState = Phone.DataState.DISCONNECTED;
    }
    private Phone.DataState getMobileDataState(Intent intent) {
        String str = intent.getStringExtra(Phone.STATE_KEY);
        if (str != null) {
            String apnTypeList =
                    intent.getStringExtra(Phone.DATA_APN_TYPES_KEY);
            if (isApnTypeIncluded(apnTypeList)) {
                return Enum.valueOf(Phone.DataState.class, str);
            }
        }
        return Phone.DataState.DISCONNECTED;
    }
    private boolean isApnTypeIncluded(String typeList) {
        if (typeList == null)
            return false;
        String[] list = typeList.split(",");
        for(int i=0; i< list.length; i++) {
            if (TextUtils.equals(list[i], mApnTypeToWatchFor) ||
                TextUtils.equals(list[i], Phone.APN_TYPE_ALL)) {
                return true;
            }
        }
        return false;
    }
    private class MobileDataStateReceiver extends BroadcastReceiver {
        public void onReceive(Context context, Intent intent) {
            synchronized(this) {
                if (intent.getAction().equals(TelephonyIntents.
                        ACTION_ANY_DATA_CONNECTION_STATE_CHANGED)) {
                    Phone.DataState state = getMobileDataState(intent);
                    String reason = intent.getStringExtra(Phone.STATE_CHANGE_REASON_KEY);
                    String apnName = intent.getStringExtra(Phone.DATA_APN_KEY);
                    String apnTypeList = intent.getStringExtra(Phone.DATA_APN_TYPES_KEY);
                    mApnName = apnName;
                    boolean unavailable = intent.getBooleanExtra(Phone.NETWORK_UNAVAILABLE_KEY,
                            false);
                    mNetworkInfo.setIsAvailable(!unavailable);
                    if (isApnTypeIncluded(apnTypeList)) {
                        if (mEnabled == false) {
                            if (state == Phone.DataState.CONNECTED) {
                                if (DBG) Log.d(TAG, "replacing old mInterfaceName (" +
                                        mInterfaceName + ") with " +
                                        intent.getStringExtra(Phone.DATA_IFACE_NAME_KEY) +
                                        " for " + mApnType);
                                mInterfaceName = intent.getStringExtra(Phone.DATA_IFACE_NAME_KEY);
                            }
                            return;
                        }
                    } else {
                        return;
                    }
                    if (DBG) Log.d(TAG, mApnType + " Received state= " + state + ", old= " +
                            mMobileDataState + ", reason= " +
                            (reason == null ? "(unspecified)" : reason) +
                            ", apnTypeList= " + apnTypeList);
                    if (mMobileDataState != state) {
                        mMobileDataState = state;
                        switch (state) {
                            case DISCONNECTED:
                                if(isTeardownRequested()) {
                                    mEnabled = false;
                                    setTeardownRequested(false);
                                }
                                setDetailedState(DetailedState.DISCONNECTED, reason, apnName);
                                if (mInterfaceName != null) {
                                    NetworkUtils.resetConnections(mInterfaceName);
                                }
                                break;
                            case CONNECTING:
                                setDetailedState(DetailedState.CONNECTING, reason, apnName);
                                break;
                            case SUSPENDED:
                                setDetailedState(DetailedState.SUSPENDED, reason, apnName);
                                break;
                            case CONNECTED:
                                mInterfaceName = intent.getStringExtra(Phone.DATA_IFACE_NAME_KEY);
                                if (mInterfaceName == null) {
                                    Log.d(TAG, "CONNECTED event did not supply interface name.");
                                }
                                setDetailedState(DetailedState.CONNECTED, reason, apnName);
                                break;
                        }
                    }
                } else if (intent.getAction().
                        equals(TelephonyIntents.ACTION_DATA_CONNECTION_FAILED)) {
                    mEnabled = false;
                    String reason = intent.getStringExtra(Phone.FAILURE_REASON_KEY);
                    String apnName = intent.getStringExtra(Phone.DATA_APN_KEY);
                    if (DBG) Log.d(TAG, "Received " + intent.getAction() + " broadcast" +
                            reason == null ? "" : "(" + reason + ")");
                    setDetailedState(DetailedState.FAILED, reason, apnName);
                }
                TelephonyManager tm = TelephonyManager.getDefault();
                setRoamingStatus(tm.isNetworkRoaming());
                setSubtype(tm.getNetworkType(), tm.getNetworkTypeName());
            }
        }
    }
    private void getPhoneService(boolean forceRefresh) {
        if ((mPhoneService == null) || forceRefresh) {
            mPhoneService = ITelephony.Stub.asInterface(ServiceManager.getService("phone"));
        }
    }
    public boolean isAvailable() {
        getPhoneService(false);
        for (int retry = 0; retry < 2; retry++) {
            if (mPhoneService == null) break;
            try {
                return mPhoneService.isDataConnectivityPossible();
            } catch (RemoteException e) {
                if (retry == 0) getPhoneService(true);
            }
        }
        return false;
    }
    public int getNetworkSubtype() {
        return TelephonyManager.getDefault().getNetworkType();
    }
    public String getTcpBufferSizesPropName() {
        String networkTypeStr = "unknown";
        TelephonyManager tm = new TelephonyManager(mContext);
        switch(tm.getNetworkType()) {
        case TelephonyManager.NETWORK_TYPE_GPRS:
            networkTypeStr = "gprs";
            break;
        case TelephonyManager.NETWORK_TYPE_EDGE:
            networkTypeStr = "edge";
            break;
        case TelephonyManager.NETWORK_TYPE_UMTS:
            networkTypeStr = "umts";
            break;
        case TelephonyManager.NETWORK_TYPE_HSDPA:
            networkTypeStr = "hsdpa";
            break;
        case TelephonyManager.NETWORK_TYPE_HSUPA:
            networkTypeStr = "hsupa";
            break;
        case TelephonyManager.NETWORK_TYPE_HSPA:
            networkTypeStr = "hspa";
            break;
        case TelephonyManager.NETWORK_TYPE_CDMA:
            networkTypeStr = "cdma";
            break;
        case TelephonyManager.NETWORK_TYPE_1xRTT:
            networkTypeStr = "1xrtt";
            break;
        case TelephonyManager.NETWORK_TYPE_EVDO_0:
            networkTypeStr = "evdo";
            break;
        case TelephonyManager.NETWORK_TYPE_EVDO_A:
            networkTypeStr = "evdo";
            break;
        }
        return "net.tcp.buffersize." + networkTypeStr;
    }
    @Override
    public boolean teardown() {
        setTeardownRequested(true);
        return (setEnableApn(mApnType, false) != Phone.APN_REQUEST_FAILED);
    }
    public boolean reconnect() {
        setTeardownRequested(false);
        switch (setEnableApn(mApnType, true)) {
            case Phone.APN_ALREADY_ACTIVE:
                mEnabled = true;
                mMobileDataState = Phone.DataState.CONNECTING;
                setDetailedState(DetailedState.CONNECTING, Phone.REASON_APN_CHANGED, null);
                Intent intent = new Intent(TelephonyIntents.
                        ACTION_ANY_DATA_CONNECTION_STATE_CHANGED);
                intent.putExtra(Phone.STATE_KEY, Phone.DataState.CONNECTED.toString());
                intent.putExtra(Phone.STATE_CHANGE_REASON_KEY, Phone.REASON_APN_CHANGED);
                intent.putExtra(Phone.DATA_APN_TYPES_KEY, mApnTypeToWatchFor);
                intent.putExtra(Phone.DATA_APN_KEY, mApnName);
                intent.putExtra(Phone.DATA_IFACE_NAME_KEY, mInterfaceName);
                intent.putExtra(Phone.NETWORK_UNAVAILABLE_KEY, false);
                if (mStateReceiver != null) mStateReceiver.onReceive(mContext, intent);
                break;
            case Phone.APN_REQUEST_STARTED:
                mEnabled = true;
                break;
            case Phone.APN_REQUEST_FAILED:
                if (mPhoneService == null && mApnType == Phone.APN_TYPE_DEFAULT) {
                    mEnabled = true;
                    return false;
                }
            case Phone.APN_TYPE_NOT_AVAILABLE:
                if (mApnType != Phone.APN_TYPE_DEFAULT) {
                    mEnabled = false;
                }
                break;
            default:
                Log.e(TAG, "Error in reconnect - unexpected response.");
                mEnabled = false;
                break;
        }
        return mEnabled;
    }
    public boolean setRadio(boolean turnOn) {
        getPhoneService(false);
        for (int retry = 0; retry < 2; retry++) {
            if (mPhoneService == null) {
                Log.w(TAG,
                    "Ignoring mobile radio request because could not acquire PhoneService");
                break;
            }
            try {
                return mPhoneService.setRadio(turnOn);
            } catch (RemoteException e) {
                if (retry == 0) getPhoneService(true);
            }
        }
        Log.w(TAG, "Could not set radio power to " + (turnOn ? "on" : "off"));
        return false;
    }
    public int startUsingNetworkFeature(String feature, int callingPid, int callingUid) {
        return -1;
    }
    public int stopUsingNetworkFeature(String feature, int callingPid, int callingUid) {
        return -1;
    }
    @Override
    public boolean requestRouteToHost(int hostAddress) {
        if (DBG) {
            Log.d(TAG, "Requested host route to " + Integer.toHexString(hostAddress) +
                    " for " + mApnType + "(" + mInterfaceName + ")");
        }
        if (mInterfaceName != null && hostAddress != -1) {
            return NetworkUtils.addHostRoute(mInterfaceName, hostAddress) == 0;
        } else {
            return false;
        }
    }
    @Override
    public String toString() {
        StringBuffer sb = new StringBuffer("Mobile data state: ");
        sb.append(mMobileDataState);
        return sb.toString();
    }
    private int setEnableApn(String apnType, boolean enable) {
        getPhoneService(false);
        for (int retry = 0; retry < 2; retry++) {
            if (mPhoneService == null) {
                Log.w(TAG,
                    "Ignoring feature request because could not acquire PhoneService");
                break;
            }
            try {
                if (enable) {
                    return mPhoneService.enableApnType(apnType);
                } else {
                    return mPhoneService.disableApnType(apnType);
                }
            } catch (RemoteException e) {
                if (retry == 0) getPhoneService(true);
            }
        }
        Log.w(TAG, "Could not " + (enable ? "enable" : "disable")
                + " APN type \"" + apnType + "\"");
        return Phone.APN_REQUEST_FAILED;
    }
    public static String networkTypeToApnType(int netType) {
        switch(netType) {
            case ConnectivityManager.TYPE_MOBILE:
                return Phone.APN_TYPE_DEFAULT;  
            case ConnectivityManager.TYPE_MOBILE_MMS:
                return Phone.APN_TYPE_MMS;
            case ConnectivityManager.TYPE_MOBILE_SUPL:
                return Phone.APN_TYPE_SUPL;
            case ConnectivityManager.TYPE_MOBILE_DUN:
                return Phone.APN_TYPE_DUN;
            case ConnectivityManager.TYPE_MOBILE_HIPRI:
                return Phone.APN_TYPE_HIPRI;
            default:
                Log.e(TAG, "Error mapping networkType " + netType + " to apnType.");
                return null;
        }
    }
}
