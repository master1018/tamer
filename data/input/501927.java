class AccessPoint extends Preference {
    private static final int[] STATE_SECURED = {R.attr.state_encrypted};
    private static final int[] STATE_NONE = {};
    static final int SECURITY_NONE = 0;
    static final int SECURITY_WEP = 1;
    static final int SECURITY_PSK = 2;
    static final int SECURITY_EAP = 3;
    final String ssid;
    final int security;
    final int networkId;
    private WifiConfiguration mConfig;
    private int mRssi;
    private WifiInfo mInfo;
    private DetailedState mState;
    private ImageView mSignal;
    static int getSecurity(WifiConfiguration config) {
        if (config.allowedKeyManagement.get(KeyMgmt.WPA_PSK)) {
            return SECURITY_PSK;
        }
        if (config.allowedKeyManagement.get(KeyMgmt.WPA_EAP) ||
                config.allowedKeyManagement.get(KeyMgmt.IEEE8021X)) {
            return SECURITY_EAP;
        }
        return (config.wepKeys[0] != null) ? SECURITY_WEP : SECURITY_NONE;
    }
    private static int getSecurity(ScanResult result) {
        if (result.capabilities.contains("WEP")) {
            return SECURITY_WEP;
        } else if (result.capabilities.contains("PSK")) {
            return SECURITY_PSK;
        } else if (result.capabilities.contains("EAP")) {
            return SECURITY_EAP;
        }
        return SECURITY_NONE;
    }
    AccessPoint(Context context, WifiConfiguration config) {
        super(context);
        setWidgetLayoutResource(R.layout.preference_widget_wifi_signal);
        ssid = (config.SSID == null ? "" : removeDoubleQuotes(config.SSID));
        security = getSecurity(config);
        networkId = config.networkId;
        mConfig = config;
        mRssi = Integer.MAX_VALUE;
    }
    AccessPoint(Context context, ScanResult result) {
        super(context);
        setWidgetLayoutResource(R.layout.preference_widget_wifi_signal);
        ssid = result.SSID;
        security = getSecurity(result);
        networkId = -1;
        mRssi = result.level;
    }
    @Override
    protected void onBindView(View view) {
        setTitle(ssid);
        mSignal = (ImageView) view.findViewById(R.id.signal);
        if (mRssi == Integer.MAX_VALUE) {
            mSignal.setImageDrawable(null);
        } else {
            mSignal.setImageResource(R.drawable.wifi_signal);
            mSignal.setImageState((security != SECURITY_NONE) ?
                    STATE_SECURED : STATE_NONE, true);
        }
        refresh();
        super.onBindView(view);
    }
    @Override
    public int compareTo(Preference preference) {
        if (!(preference instanceof AccessPoint)) {
            return 1;
        }
        AccessPoint other = (AccessPoint) preference;
        if (mInfo != other.mInfo) {
            return (mInfo != null) ? -1 : 1;
        }
        if ((mRssi ^ other.mRssi) < 0) {
            return (mRssi != Integer.MAX_VALUE) ? -1 : 1;
        }
        if ((networkId ^ other.networkId) < 0) {
            return (networkId != -1) ? -1 : 1;
        }
        int difference = WifiManager.compareSignalLevel(other.mRssi, mRssi);
        if (difference != 0) {
            return difference;
        }
        return ssid.compareToIgnoreCase(other.ssid);
    }
    boolean update(ScanResult result) {
        if (ssid.equals(result.SSID) && security == getSecurity(result)) {
            if (WifiManager.compareSignalLevel(result.level, mRssi) > 0) {
                mRssi = result.level;
            }
            return true;
        }
        return false;
    }
    void update(WifiInfo info, DetailedState state) {
        boolean reorder = false;
        if (info != null && networkId != -1 && networkId == info.getNetworkId()) {
            reorder = (mInfo == null);
            mRssi = info.getRssi();
            mInfo = info;
            mState = state;
            refresh();
        } else if (mInfo != null) {
            reorder = true;
            mInfo = null;
            mState = null;
            refresh();
        }
        if (reorder) {
            notifyHierarchyChanged();
        }
    }
    int getLevel() {
        if (mRssi == Integer.MAX_VALUE) {
            return -1;
        }
        return WifiManager.calculateSignalLevel(mRssi, 4);
    }
    WifiConfiguration getConfig() {
        return mConfig;
    }
    WifiInfo getInfo() {
        return mInfo;
    }
    DetailedState getState() {
        return mState;
    }
    static String removeDoubleQuotes(String string) {
        int length = string.length();
        if ((length > 1) && (string.charAt(0) == '"')
                && (string.charAt(length - 1) == '"')) {
            return string.substring(1, length - 1);
        }
        return string;
    }
    static String convertToQuotedString(String string) {
        return "\"" + string + "\"";
    }
    private void refresh() {
        if (mSignal == null) {
            return;
        }
        Context context = getContext();
        mSignal.setImageLevel(getLevel());
        if (mState != null) {
            setSummary(Summary.get(context, mState));
        } else {
            String status = null;
            if (mRssi == Integer.MAX_VALUE) {
                status = context.getString(R.string.wifi_not_in_range);
            } else if (mConfig != null) {
                status = context.getString((mConfig.status == WifiConfiguration.Status.DISABLED) ?
                        R.string.wifi_disabled : R.string.wifi_remembered);
            }
            if (security == SECURITY_NONE) {
                setSummary(status);
            } else {
                String format = context.getString((status == null) ?
                        R.string.wifi_secured : R.string.wifi_secured_with_status);
                String[] type = context.getResources().getStringArray(R.array.wifi_security);
                setSummary(String.format(format, type[security], status));
            }
        }
    }
}
