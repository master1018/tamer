public class L2tpIpsecProfile extends L2tpProfile {
    private static final long serialVersionUID = 1L;
    private String mUserCertificate;
    private String mCaCertificate;
    @Override
    public VpnType getType() {
        return VpnType.L2TP_IPSEC;
    }
    public void setCaCertificate(String name) {
        mCaCertificate = name;
    }
    public String getCaCertificate() {
        return mCaCertificate;
    }
    public void setUserCertificate(String name) {
        mUserCertificate = name;
    }
    public String getUserCertificate() {
        return mUserCertificate;
    }
    @Override
    protected void readFromParcel(Parcel in) {
        super.readFromParcel(in);
        mCaCertificate = in.readString();
        mUserCertificate = in.readString();
    }
    @Override
    public void writeToParcel(Parcel parcel, int flags) {
        super.writeToParcel(parcel, flags);
        parcel.writeString(mCaCertificate);
        parcel.writeString(mUserCertificate);
    }
}
