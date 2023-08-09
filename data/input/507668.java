public class GsmDataConnection extends DataConnection {
    private static final String LOG_TAG = "GSM";
    private static final int PDP_FAIL_OPERATOR_BARRED = 0x08;
    private static final int PDP_FAIL_INSUFFICIENT_RESOURCES = 0x1A;
    private static final int PDP_FAIL_MISSING_UKNOWN_APN = 0x1B;
    private static final int PDP_FAIL_UNKNOWN_PDP_ADDRESS_TYPE = 0x1C;
    private static final int PDP_FAIL_USER_AUTHENTICATION = 0x1D;
    private static final int PDP_FAIL_ACTIVATION_REJECT_GGSN = 0x1E;
    private static final int PDP_FAIL_ACTIVATION_REJECT_UNSPECIFIED = 0x1F;
    private static final int PDP_FAIL_SERVICE_OPTION_NOT_SUPPORTED = 0x20;
    private static final int PDP_FAIL_SERVICE_OPTION_NOT_SUBSCRIBED = 0x21;
    private static final int PDP_FAIL_SERVICE_OPTION_OUT_OF_ORDER = 0x22;
    private static final int PDP_FAIL_NSAPI_IN_USE      = 0x23;
    private static final int PDP_FAIL_PROTOCOL_ERRORS   = 0x6F;
    private static final int PDP_FAIL_ERROR_UNSPECIFIED = 0xffff;
    private static final int PDP_FAIL_REGISTRATION_FAIL = -1;
    private static final int PDP_FAIL_GPRS_REGISTRATION_FAIL = -2;
    private ApnSetting apn;
    private GsmDataConnection(GSMPhone phone, String name) {
        super(phone, name);
    }
    static GsmDataConnection makeDataConnection(GSMPhone phone) {
        synchronized (mCountLock) {
            mCount += 1;
        }
        GsmDataConnection gsmDc = new GsmDataConnection(phone, "GsmDataConnection-" + mCount);
        gsmDc.start();
        if (DBG) gsmDc.log("Made " + gsmDc.getName());
        return gsmDc;
    }
    @Override
    protected
    void onConnect(ConnectionParams cp) {
        apn = cp.apn;
        if (DBG) log("Connecting to carrier: '" + apn.carrier
                + "' APN: '" + apn.apn
                + "' proxy: '" + apn.proxy + "' port: '" + apn.port);
        setHttpProxy (apn.proxy, apn.port);
        createTime = -1;
        lastFailTime = -1;
        lastFailCause = FailCause.NONE;
        Message msg = obtainMessage(EVENT_SETUP_DATA_CONNECTION_DONE, cp);
        msg.obj = cp;
        int authType = apn.authType;
        if (authType == -1) {
            authType = (apn.user != null) ? RILConstants.SETUP_DATA_AUTH_PAP_CHAP :
                RILConstants.SETUP_DATA_AUTH_NONE;
        }
        phone.mCM.setupDataCall(Integer.toString(RILConstants.SETUP_DATA_TECH_GSM),
                Integer.toString(RILConstants.DATA_PROFILE_DEFAULT), apn.apn, apn.user,
                apn.password, Integer.toString(authType), msg);
    }
    @Override
    protected void clearSettings() {
        super.clearSettings();
        apn = null;
    }
    @Override
    public String toString() {
        return "State=" + getCurrentState().getName() + " Apn=" + apn +
               " create=" + createTime + " lastFail=" + lastFailTime +
               " lastFailCause=" + lastFailCause;
    }
    @Override
    protected FailCause getFailCauseFromRequest(int rilCause) {
        FailCause cause;
        switch (rilCause) {
            case PDP_FAIL_OPERATOR_BARRED:
                cause = FailCause.OPERATOR_BARRED;
                break;
            case PDP_FAIL_INSUFFICIENT_RESOURCES:
                cause = FailCause.INSUFFICIENT_RESOURCES;
                break;
            case PDP_FAIL_MISSING_UKNOWN_APN:
                cause = FailCause.MISSING_UKNOWN_APN;
                break;
            case PDP_FAIL_UNKNOWN_PDP_ADDRESS_TYPE:
                cause = FailCause.UNKNOWN_PDP_ADDRESS;
                break;
            case PDP_FAIL_USER_AUTHENTICATION:
                cause = FailCause.USER_AUTHENTICATION;
                break;
            case PDP_FAIL_ACTIVATION_REJECT_GGSN:
                cause = FailCause.ACTIVATION_REJECT_GGSN;
                break;
            case PDP_FAIL_ACTIVATION_REJECT_UNSPECIFIED:
                cause = FailCause.ACTIVATION_REJECT_UNSPECIFIED;
                break;
            case PDP_FAIL_SERVICE_OPTION_OUT_OF_ORDER:
                cause = FailCause.SERVICE_OPTION_OUT_OF_ORDER;
                break;
            case PDP_FAIL_SERVICE_OPTION_NOT_SUPPORTED:
                cause = FailCause.SERVICE_OPTION_NOT_SUPPORTED;
                break;
            case PDP_FAIL_SERVICE_OPTION_NOT_SUBSCRIBED:
                cause = FailCause.SERVICE_OPTION_NOT_SUBSCRIBED;
                break;
            case PDP_FAIL_NSAPI_IN_USE:
                cause = FailCause.NSAPI_IN_USE;
                break;
            case PDP_FAIL_PROTOCOL_ERRORS:
                cause = FailCause.PROTOCOL_ERRORS;
                break;
            case PDP_FAIL_ERROR_UNSPECIFIED:
                cause = FailCause.UNKNOWN;
                break;
            case PDP_FAIL_REGISTRATION_FAIL:
                cause = FailCause.REGISTRATION_FAIL;
                break;
            case PDP_FAIL_GPRS_REGISTRATION_FAIL:
                cause = FailCause.GPRS_REGISTRATION_FAIL;
                break;
            default:
                cause = FailCause.UNKNOWN;
        }
        return cause;
    }
    @Override
    protected boolean isDnsOk(String[] domainNameServers) {
        if (NULL_IP.equals(dnsServers[0]) && NULL_IP.equals(dnsServers[1])
                    && !((GSMPhone) phone).isDnsCheckDisabled()) {
            if (!apn.types[0].equals(Phone.APN_TYPE_MMS)
                || !isIpAddress(apn.mmsProxy)) {
                return false;
            }
        }
        return true;
    }
    @Override
    protected void log(String s) {
        Log.d(LOG_TAG, "[" + getName() + "] " + s);
    }
    public ApnSetting getApn() {
        return this.apn;
    }
    private void setHttpProxy(String httpProxy, String httpPort) {
        if (httpProxy == null || httpProxy.length() == 0) {
            phone.setSystemProperty("net.gprs.http-proxy", null);
            return;
        }
        if (httpPort == null || httpPort.length() == 0) {
            httpPort = "8080";     
        }
        phone.setSystemProperty("net.gprs.http-proxy",
                "http:
    }
    private boolean isIpAddress(String address) {
        if (address == null) return false;
        return Patterns.IP_ADDRESS.matcher(apn.mmsProxy).matches();
    }
}
