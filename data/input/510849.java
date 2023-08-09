public class WifiInfo implements Parcelable {
    private static final EnumMap<SupplicantState, DetailedState> stateMap =
        new EnumMap<SupplicantState, DetailedState>(SupplicantState.class);
    static {
        stateMap.put(SupplicantState.DISCONNECTED, DetailedState.DISCONNECTED);
        stateMap.put(SupplicantState.INACTIVE, DetailedState.IDLE);
        stateMap.put(SupplicantState.SCANNING, DetailedState.SCANNING);
        stateMap.put(SupplicantState.ASSOCIATING, DetailedState.CONNECTING);
        stateMap.put(SupplicantState.ASSOCIATED, DetailedState.CONNECTING);
        stateMap.put(SupplicantState.FOUR_WAY_HANDSHAKE, DetailedState.AUTHENTICATING);
        stateMap.put(SupplicantState.GROUP_HANDSHAKE, DetailedState.AUTHENTICATING);
        stateMap.put(SupplicantState.COMPLETED, DetailedState.OBTAINING_IPADDR);
        stateMap.put(SupplicantState.DORMANT, DetailedState.DISCONNECTED);
        stateMap.put(SupplicantState.UNINITIALIZED, DetailedState.IDLE);
        stateMap.put(SupplicantState.INVALID, DetailedState.FAILED);
    }
    private SupplicantState mSupplicantState;
    private String mBSSID;
    private String mSSID;
    private int mNetworkId;
    private boolean mHiddenSSID;
    private int mRssi;
    public static final String LINK_SPEED_UNITS = "Mbps";
    private int mLinkSpeed;
    private int mIpAddress;
    private String mMacAddress;
    WifiInfo() {
        mSSID = null;
        mBSSID = null;
        mNetworkId = -1;
        mSupplicantState = SupplicantState.UNINITIALIZED;
        mRssi = -9999;
        mLinkSpeed = -1;
        mIpAddress = 0;
        mHiddenSSID = false;
    }
    void setSSID(String SSID) {
        mSSID = SSID;
        mHiddenSSID = false;
    }
    public String getSSID() {
        return mSSID;
    }
    void setBSSID(String BSSID) {
        mBSSID = BSSID;
    }
    public String getBSSID() {
        return mBSSID;
    }
    public int getRssi() {
        return mRssi;
    }
    void setRssi(int rssi) {
        mRssi = rssi;
    }
    public int getLinkSpeed() {
        return mLinkSpeed;
    }
    void setLinkSpeed(int linkSpeed) {
        this.mLinkSpeed = linkSpeed;
    }
    void setMacAddress(String macAddress) {
        this.mMacAddress = macAddress;
    }
    public String getMacAddress() {
        return mMacAddress;
    }
    void setNetworkId(int id) {
        mNetworkId = id;
    }
    public int getNetworkId() {
        return mNetworkId;
    }
    public SupplicantState getSupplicantState() {
        return mSupplicantState;
    }
    void setSupplicantState(SupplicantState state) {
        mSupplicantState = state;
    }
    void setIpAddress(int address) {
        mIpAddress = address;
    }
    public int getIpAddress() {
        return mIpAddress;
    }
    public boolean getHiddenSSID() {
        return mHiddenSSID;
    }
    public void setHiddenSSID(boolean hiddenSSID) {
        mHiddenSSID = hiddenSSID;
    }
    public static DetailedState getDetailedStateOf(SupplicantState suppState) {
        return stateMap.get(suppState);
    }
    void setSupplicantState(String stateName) {
        mSupplicantState = valueOf(stateName);
    }
    static SupplicantState valueOf(String stateName) {
        if ("4WAY_HANDSHAKE".equalsIgnoreCase(stateName))
            return SupplicantState.FOUR_WAY_HANDSHAKE;
        else {
            try {
                return SupplicantState.valueOf(stateName.toUpperCase());
            } catch (IllegalArgumentException e) {
                return SupplicantState.INVALID;
            }
        }
    }
    @Override
    public String toString() {
        StringBuffer sb = new StringBuffer();
        String none = "<none>";
        sb.append("SSID: ").append(mSSID == null ? none : mSSID).
            append(", BSSID: ").append(mBSSID == null ? none : mBSSID).
            append(", MAC: ").append(mMacAddress == null ? none : mMacAddress).
            append(", Supplicant state: ").
            append(mSupplicantState == null ? none : mSupplicantState).
            append(", RSSI: ").append(mRssi).
            append(", Link speed: ").append(mLinkSpeed).
            append(", Net ID: ").append(mNetworkId);
        return sb.toString();
    }
    public int describeContents() {
        return 0;
    }
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(mNetworkId);
        dest.writeInt(mRssi);
        dest.writeInt(mLinkSpeed);
        dest.writeInt(mIpAddress);
        dest.writeString(getSSID());
        dest.writeString(mBSSID);
        dest.writeString(mMacAddress);
        mSupplicantState.writeToParcel(dest, flags);
    }
    public static final Creator<WifiInfo> CREATOR =
        new Creator<WifiInfo>() {
            public WifiInfo createFromParcel(Parcel in) {
                WifiInfo info = new WifiInfo();
                info.setNetworkId(in.readInt());
                info.setRssi(in.readInt());
                info.setLinkSpeed(in.readInt());
                info.setIpAddress(in.readInt());
                info.setSSID(in.readString());
                info.mBSSID = in.readString();
                info.mMacAddress = in.readString();
                info.mSupplicantState = SupplicantState.CREATOR.createFromParcel(in);
                return info;
            }
            public WifiInfo[] newArray(int size) {
                return new WifiInfo[size];
            }
        };
}
