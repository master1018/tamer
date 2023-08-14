    PPTP("PPTP", R.string.pptp_vpn_description, PptpProfile.class),
    L2TP("L2TP", R.string.l2tp_vpn_description, L2tpProfile.class),
    L2TP_IPSEC_PSK("L2TP/IPSec PSK", R.string.l2tp_ipsec_psk_vpn_description,
            L2tpIpsecPskProfile.class),
    L2TP_IPSEC("L2TP/IPSec CRT", R.string.l2tp_ipsec_crt_vpn_description,
            L2tpIpsecProfile.class);
    private String mDisplayName;
    private int mDescriptionId;
    private Class<? extends VpnProfile> mClass;
    VpnType(String displayName, int descriptionId,
            Class<? extends VpnProfile> klass) {
        mDisplayName = displayName;
        mDescriptionId = descriptionId;
        mClass = klass;
    }
    public String getDisplayName() {
        return mDisplayName;
    }
    public int getDescriptionId() {
        return mDescriptionId;
    }
    public Class<? extends VpnProfile> getProfileClass() {
        return mClass;
    }
}
