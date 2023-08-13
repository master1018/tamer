public class L2tpIpsecPskProfile extends L2tpProfile {
    private static final long serialVersionUID = 1L;
    private String mPresharedKey;
    @Override
    public VpnType getType() {
        return VpnType.L2TP_IPSEC_PSK;
    }
    public void setPresharedKey(String key) {
        mPresharedKey = key;
    }
    public String getPresharedKey() {
        return mPresharedKey;
    }
    @Override
    protected void readFromParcel(Parcel in) {
        super.readFromParcel(in);
        mPresharedKey = in.readString();
    }
    @Override
    public void writeToParcel(Parcel parcel, int flags) {
        super.writeToParcel(parcel, flags);
        parcel.writeString(mPresharedKey);
    }
}
