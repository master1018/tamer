public class VpnManager {
    private static final String ACTION_VPN_CONNECTIVITY = "vpn.connectivity";
    public static final String BROADCAST_PROFILE_NAME = "profile_name";
    public static final String BROADCAST_CONNECTION_STATE = "connection_state";
    public static final String BROADCAST_ERROR_CODE = "err";
    public static final int VPN_ERROR_AUTH = 51;
    public static final int VPN_ERROR_CONNECTION_FAILED = 101;
    public static final int VPN_ERROR_UNKNOWN_SERVER = 102;
    public static final int VPN_ERROR_CHALLENGE = 5;
    public static final int VPN_ERROR_REMOTE_HUNG_UP = 7;
    public static final int VPN_ERROR_REMOTE_PPP_HUNG_UP = 48;
    public static final int VPN_ERROR_PPP_NEGOTIATION_FAILED = 42;
    public static final int VPN_ERROR_CONNECTION_LOST = 103;
    public static final int VPN_ERROR_LARGEST = 200;
    public static final int VPN_ERROR_NO_ERROR = 0;
    public static final String PROFILES_PATH = "/misc/vpn/profiles";
    private static final String PACKAGE_PREFIX =
            VpnManager.class.getPackage().getName() + ".";
    private static final String ACTION_VPN_SERVICE = PACKAGE_PREFIX + "SERVICE";
    private static final String ACTION_VPN_SETTINGS =
            PACKAGE_PREFIX + "SETTINGS";
    public static final String TAG = VpnManager.class.getSimpleName();
    public static String getProfilePath() {
        return Environment.getDataDirectory().getPath() + PROFILES_PATH;
    }
    public static VpnType[] getSupportedVpnTypes() {
        return VpnType.values();
    }
    private Context mContext;
    public VpnManager(Context c) {
        mContext = c;
    }
    public VpnProfile createVpnProfile(VpnType type) {
        return createVpnProfile(type, false);
    }
    public VpnProfile createVpnProfile(VpnType type, boolean customized) {
        try {
            VpnProfile p = (VpnProfile) type.getProfileClass().newInstance();
            p.setCustomized(customized);
            return p;
        } catch (InstantiationException e) {
            return null;
        } catch (IllegalAccessException e) {
            return null;
        }
    }
    public void startVpnService() {
        mContext.startService(new Intent(ACTION_VPN_SERVICE));
    }
    public void stopVpnService() {
        mContext.stopService(new Intent(ACTION_VPN_SERVICE));
    }
    public boolean bindVpnService(ServiceConnection c) {
        if (!mContext.bindService(new Intent(ACTION_VPN_SERVICE), c, 0)) {
            Log.w(TAG, "failed to connect to VPN service");
            return false;
        } else {
            Log.d(TAG, "succeeded to connect to VPN service");
            return true;
        }
    }
    public void broadcastConnectivity(String profileName, VpnState s) {
        broadcastConnectivity(profileName, s, VPN_ERROR_NO_ERROR);
    }
    public void broadcastConnectivity(String profileName, VpnState s,
            int error) {
        Intent intent = new Intent(ACTION_VPN_CONNECTIVITY);
        intent.putExtra(BROADCAST_PROFILE_NAME, profileName);
        intent.putExtra(BROADCAST_CONNECTION_STATE, s);
        if (error != VPN_ERROR_NO_ERROR) {
            intent.putExtra(BROADCAST_ERROR_CODE, error);
        }
        mContext.sendBroadcast(intent);
    }
    public void registerConnectivityReceiver(BroadcastReceiver r) {
        IntentFilter filter = new IntentFilter();
        filter.addAction(VpnManager.ACTION_VPN_CONNECTIVITY);
        mContext.registerReceiver(r, filter);
    }
    public void unregisterConnectivityReceiver(BroadcastReceiver r) {
        mContext.unregisterReceiver(r);
    }
    public void startSettingsActivity() {
        Intent intent = new Intent(ACTION_VPN_SETTINGS);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        mContext.startActivity(intent);
    }
    public Intent createSettingsActivityIntent() {
        Intent intent = new Intent(ACTION_VPN_SETTINGS);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        return intent;
    }
}
