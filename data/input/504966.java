public class CdmaDataConnection extends DataConnection {
    private static final String LOG_TAG = "CDMA";
    private final static int PS_NET_DOWN_REASON_OPERATOR_DETERMINED_BARRING         = 8;
    private final static int PS_NET_DOWN_REASON_AUTH_FAILED                         = 29;
    private final static int PS_NET_DOWN_REASON_OPTION_NOT_SUPPORTED                = 32;
    private final static int PS_NET_DOWN_REASON_OPTION_UNSUBSCRIBED                 = 33;
    private CdmaDataConnection(CDMAPhone phone, String name) {
        super(phone, name);
    }
    static CdmaDataConnection makeDataConnection(CDMAPhone phone) {
        synchronized (mCountLock) {
            mCount += 1;
        }
        CdmaDataConnection cdmaDc = new CdmaDataConnection(phone, "CdmaDataConnection-" + mCount);
        cdmaDc.start();
        if (DBG) cdmaDc.log("Made " + cdmaDc.getName());
        return cdmaDc;
    }
    @Override
    protected void onConnect(ConnectionParams cp) {
        if (DBG) log("CdmaDataConnection Connecting...");
        createTime = -1;
        lastFailTime = -1;
        lastFailCause = FailCause.NONE;
        int dataProfile;
        if ((cp.apn != null) && (cp.apn.types.length > 0) && (cp.apn.types[0] != null) &&
                (cp.apn.types[0].equals(Phone.APN_TYPE_DUN))) {
            if (DBG) log("CdmaDataConnection using DUN");
            dataProfile = RILConstants.DATA_PROFILE_TETHERED;
        } else {
            dataProfile = RILConstants.DATA_PROFILE_DEFAULT;
        }
        Message msg = obtainMessage(EVENT_SETUP_DATA_CONNECTION_DONE, cp);
        msg.obj = cp;
        phone.mCM.setupDataCall(Integer.toString(RILConstants.SETUP_DATA_TECH_CDMA),
                Integer.toString(dataProfile), null, null,
                null, Integer.toString(RILConstants.SETUP_DATA_AUTH_PAP_CHAP), msg);
    }
    @Override
    public String toString() {
        return "State=" + getCurrentState().getName() + " create=" + createTime + " lastFail="
                + lastFailTime + " lastFasilCause=" + lastFailCause;
    }
    @Override
    protected FailCause getFailCauseFromRequest(int rilCause) {
        FailCause cause;
        switch (rilCause) {
            case PS_NET_DOWN_REASON_OPERATOR_DETERMINED_BARRING:
                cause = FailCause.OPERATOR_BARRED;
                break;
            case PS_NET_DOWN_REASON_AUTH_FAILED:
                cause = FailCause.USER_AUTHENTICATION;
                break;
            case PS_NET_DOWN_REASON_OPTION_NOT_SUPPORTED:
                cause = FailCause.SERVICE_OPTION_NOT_SUPPORTED;
                break;
            case PS_NET_DOWN_REASON_OPTION_UNSUBSCRIBED:
                cause = FailCause.SERVICE_OPTION_NOT_SUBSCRIBED;
                break;
            default:
                cause = FailCause.UNKNOWN;
        }
        return cause;
    }
    @Override
    protected boolean isDnsOk(String[] domainNameServers) {
        if ((NULL_IP.equals(domainNameServers[0])
                && NULL_IP.equals(domainNameServers[1])
                && !((CDMAPhone) phone).isDnsCheckDisabled())) {
            return false;
        } else {
            return true;
        }
    }
    @Override
    protected void log(String s) {
        Log.d(LOG_TAG, "[" + getName() + "] " + s);
    }
}
