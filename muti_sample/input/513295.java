public abstract class DataConnection extends HierarchicalStateMachine {
    protected static final boolean DBG = true;
    protected static Object mCountLock = new Object();
    protected static int mCount;
    protected enum SetupResult {
        ERR_BadCommand,
        ERR_BadDns,
        ERR_Other,
        ERR_Stale,
        SUCCESS;
        public FailCause mFailCause;
        @Override
        public String toString() {
            switch (this) {
                case ERR_BadCommand: return "Bad Command";
                case ERR_BadDns: return "Bad DNS";
                case ERR_Other: return "Other error";
                case ERR_Stale: return "Stale command";
                case SUCCESS: return "SUCCESS";
                default: return "unknown";
            }
        }
    }
    protected static class ConnectionParams {
        public ConnectionParams(ApnSetting apn, Message onCompletedMsg) {
            this.apn = apn;
            this.onCompletedMsg = onCompletedMsg;
        }
        public int tag;
        public ApnSetting apn;
        public Message onCompletedMsg;
    }
    class ResetSynchronouslyLock {
    }
    protected static class DisconnectParams {
        public DisconnectParams(Message onCompletedMsg) {
            this.onCompletedMsg = onCompletedMsg;
        }
        public DisconnectParams(ResetSynchronouslyLock lockObj) {
            this.lockObj = lockObj;
        }
        public int tag;
        public Message onCompletedMsg;
        public ResetSynchronouslyLock lockObj;
    }
    public enum FailCause {
        NONE,
        OPERATOR_BARRED,
        INSUFFICIENT_RESOURCES,
        MISSING_UKNOWN_APN,
        UNKNOWN_PDP_ADDRESS,
        USER_AUTHENTICATION,
        ACTIVATION_REJECT_GGSN,
        ACTIVATION_REJECT_UNSPECIFIED,
        SERVICE_OPTION_NOT_SUPPORTED,
        SERVICE_OPTION_NOT_SUBSCRIBED,
        SERVICE_OPTION_OUT_OF_ORDER,
        NSAPI_IN_USE,
        PROTOCOL_ERRORS,
        REGISTRATION_FAIL,
        GPRS_REGISTRATION_FAIL,
        UNKNOWN,
        RADIO_NOT_AVAILABLE;
        public boolean isPermanentFail() {
            return (this == OPERATOR_BARRED) || (this == MISSING_UKNOWN_APN) ||
                   (this == UNKNOWN_PDP_ADDRESS) || (this == USER_AUTHENTICATION) ||
                   (this == ACTIVATION_REJECT_GGSN) || (this == ACTIVATION_REJECT_UNSPECIFIED) ||
                   (this == SERVICE_OPTION_NOT_SUPPORTED) ||
                   (this == SERVICE_OPTION_NOT_SUBSCRIBED) || (this == NSAPI_IN_USE) ||
                   (this == PROTOCOL_ERRORS);
        }
        public boolean isEventLoggable() {
            return (this == OPERATOR_BARRED) || (this == INSUFFICIENT_RESOURCES) ||
                    (this == UNKNOWN_PDP_ADDRESS) || (this == USER_AUTHENTICATION) ||
                    (this == ACTIVATION_REJECT_GGSN) || (this == ACTIVATION_REJECT_UNSPECIFIED) ||
                    (this == SERVICE_OPTION_NOT_SUBSCRIBED) ||
                    (this == SERVICE_OPTION_NOT_SUPPORTED) ||
                    (this == SERVICE_OPTION_OUT_OF_ORDER) || (this == NSAPI_IN_USE) ||
                    (this == PROTOCOL_ERRORS);
        }
        @Override
        public String toString() {
            switch (this) {
            case NONE:
                return "No Error";
            case OPERATOR_BARRED:
                return "Operator Barred";
            case INSUFFICIENT_RESOURCES:
                return "Insufficient Resources";
            case MISSING_UKNOWN_APN:
                return "Missing / Unknown APN";
            case UNKNOWN_PDP_ADDRESS:
                return "Unknown PDP Address";
            case USER_AUTHENTICATION:
                return "Error User Autentication";
            case ACTIVATION_REJECT_GGSN:
                return "Activation Reject GGSN";
            case ACTIVATION_REJECT_UNSPECIFIED:
                return "Activation Reject unspecified";
            case SERVICE_OPTION_NOT_SUPPORTED:
                return "Data Not Supported";
            case SERVICE_OPTION_NOT_SUBSCRIBED:
                return "Data Not subscribed";
            case SERVICE_OPTION_OUT_OF_ORDER:
                return "Data Services Out of Order";
            case NSAPI_IN_USE:
                return "NSAPI in use";
            case PROTOCOL_ERRORS:
                return "Protocol Errors";
            case REGISTRATION_FAIL:
                return "Network Registration Failure";
            case GPRS_REGISTRATION_FAIL:
                return "Data Network Registration Failure";
            case RADIO_NOT_AVAILABLE:
                return "Radio Not Available";
            default:
                return "Unknown Data Error";
            }
        }
    }
    protected static final int EVENT_RESET = 1;
    protected static final int EVENT_CONNECT = 2;
    protected static final int EVENT_SETUP_DATA_CONNECTION_DONE = 3;
    protected static final int EVENT_GET_LAST_FAIL_DONE = 4;
    protected static final int EVENT_DEACTIVATE_DONE = 5;
    protected static final int EVENT_DISCONNECT = 6;
    protected static final int EVENT_LOG_BAD_DNS_ADDRESS = 50100;
    protected int mTag;
    protected PhoneBase phone;
    protected int cid;
    protected String interfaceName;
    protected String ipAddress;
    protected String gatewayAddress;
    protected String[] dnsServers;
    protected long createTime;
    protected long lastFailTime;
    protected FailCause lastFailCause;
    protected static final String NULL_IP = "0.0.0.0";
    Object userData;
    public abstract String toString();
    protected abstract void onConnect(ConnectionParams cp);
    protected abstract FailCause getFailCauseFromRequest(int rilCause);
    protected abstract boolean isDnsOk(String[] domainNameServers);
    protected abstract void log(String s);
    protected DataConnection(PhoneBase phone, String name) {
        super(name);
        if (DBG) log("DataConnection constructor E");
        this.phone = phone;
        this.cid = -1;
        this.dnsServers = new String[2];
        clearSettings();
        setDbg(false);
        addState(mDefaultState);
            addState(mInactiveState, mDefaultState);
            addState(mActivatingState, mDefaultState);
            addState(mActiveState, mDefaultState);
            addState(mDisconnectingState, mDefaultState);
            addState(mDisconnectingBadDnsState, mDefaultState);
        setInitialState(mInactiveState);
        if (DBG) log("DataConnection constructor X");
    }
    private void tearDownData(Object o) {
        if (phone.mCM.getRadioState().isOn()) {
            if (DBG) log("tearDownData radio is on, call deactivateDataCall");
            phone.mCM.deactivateDataCall(cid, obtainMessage(EVENT_DEACTIVATE_DONE, o));
        } else {
            if (DBG) log("tearDownData radio is off sendMessage EVENT_DEACTIVATE_DONE immediately");
            AsyncResult ar = new AsyncResult(o, null, null);
            sendMessage(obtainMessage(EVENT_DEACTIVATE_DONE, ar));
        }
    }
    private void notifyConnectCompleted(ConnectionParams cp, FailCause cause) {
        Message connectionCompletedMsg = cp.onCompletedMsg;
        if (connectionCompletedMsg == null) {
            return;
        }
        long timeStamp = System.currentTimeMillis();
        connectionCompletedMsg.arg1 = cid;
        if (cause == FailCause.NONE) {
            createTime = timeStamp;
            AsyncResult.forMessage(connectionCompletedMsg);
        } else {
            lastFailCause = cause;
            lastFailTime = timeStamp;
            AsyncResult.forMessage(connectionCompletedMsg, cause, new Exception());
        }
        if (DBG) log("notifyConnection at " + timeStamp + " cause=" + cause);
        connectionCompletedMsg.sendToTarget();
    }
    private void notifyDisconnectCompleted(DisconnectParams dp) {
        if (DBG) log("NotifyDisconnectCompleted");
        if (dp.onCompletedMsg != null) {
            Message msg = dp.onCompletedMsg;
            log(String.format("msg.what=%d msg.obj=%s",
                    msg.what, ((msg.obj instanceof String) ? (String) msg.obj : "<no-reason>")));
            AsyncResult.forMessage(msg);
            msg.sendToTarget();
        }
        if (dp.lockObj != null) {
            synchronized(dp.lockObj) {
                dp.lockObj.notify();
            }
        }
        clearSettings();
    }
    protected void clearSettings() {
        if (DBG) log("clearSettings");
        this.createTime = -1;
        this.lastFailTime = -1;
        this.lastFailCause = FailCause.NONE;
        interfaceName = null;
        ipAddress = null;
        gatewayAddress = null;
        dnsServers[0] = null;
        dnsServers[1] = null;
    }
    private SetupResult onSetupConnectionCompleted(AsyncResult ar) {
        SetupResult result;
        String[] response = ((String[]) ar.result);
        ConnectionParams cp = (ConnectionParams) ar.userObj;
        if (ar.exception != null) {
            if (DBG) log("DataConnection Init failed " + ar.exception);
            if (ar.exception instanceof CommandException
                    && ((CommandException) (ar.exception)).getCommandError()
                    == CommandException.Error.RADIO_NOT_AVAILABLE) {
                result = SetupResult.ERR_BadCommand;
                result.mFailCause = FailCause.RADIO_NOT_AVAILABLE;
            } else {
                result = SetupResult.ERR_Other;
            }
        } else if (cp.tag != mTag) {
            if (DBG) {
                log("BUG: onSetupConnectionCompleted is stale cp.tag=" + cp.tag + ", mtag=" + mTag);
            }
            result = SetupResult.ERR_Stale;
        } else {
            if (response.length >= 2) {
                cid = Integer.parseInt(response[0]);
                interfaceName = response[1];
                if (response.length > 2) {
                    ipAddress = response[2];
                    String prefix = "net." + interfaceName + ".";
                    gatewayAddress = SystemProperties.get(prefix + "gw");
                    dnsServers[0] = SystemProperties.get(prefix + "dns1");
                    dnsServers[1] = SystemProperties.get(prefix + "dns2");
                    if (DBG) {
                        log("interface=" + interfaceName + " ipAddress=" + ipAddress
                            + " gateway=" + gatewayAddress + " DNS1=" + dnsServers[0]
                            + " DNS2=" + dnsServers[1]);
                    }
                    if (isDnsOk(dnsServers)) {
                        result = SetupResult.SUCCESS;
                    } else {
                        result = SetupResult.ERR_BadDns;
                    }
                } else {
                    result = SetupResult.SUCCESS;
                }
            } else {
                result = SetupResult.ERR_Other;
            }
        }
        if (DBG) log("DataConnection setup result='" + result + "' on cid=" + cid);
        return result;
    }
    private class DcDefaultState extends HierarchicalState {
        @Override
        protected boolean processMessage(Message msg) {
            AsyncResult ar;
            switch (msg.what) {
                case EVENT_RESET:
                    if (DBG) log("DcDefaultState: msg.what=EVENT_RESET");
                    clearSettings();
                    if (msg.obj != null) {
                        notifyDisconnectCompleted((DisconnectParams) msg.obj);
                    }
                    transitionTo(mInactiveState);
                    break;
                case EVENT_CONNECT:
                    if (DBG) log("DcDefaultState: msg.what=EVENT_CONNECT, fail not expected");
                    ConnectionParams cp = (ConnectionParams) msg.obj;
                    notifyConnectCompleted(cp, FailCause.UNKNOWN);
                    break;
                case EVENT_DISCONNECT:
                    if (DBG) log("DcDefaultState: msg.what=EVENT_DISCONNECT");
                    notifyDisconnectCompleted((DisconnectParams) msg.obj);
                    break;
                default:
                    if (DBG) {
                        log("DcDefaultState: shouldn't happen but ignore msg.what=" + msg.what);
                    }
                    break;
            }
            return true;
        }
    }
    private DcDefaultState mDefaultState = new DcDefaultState();
    private class DcInactiveState extends HierarchicalState {
        private ConnectionParams mConnectionParams = null;
        private FailCause mFailCause = null;
        private DisconnectParams mDisconnectParams = null;
        public void setEnterNotificationParams(ConnectionParams cp, FailCause cause) {
            log("DcInactiveState: setEnterNoticationParams cp,cause");
            mConnectionParams = cp;
            mFailCause = cause;
        }
        public void setEnterNotificationParams(DisconnectParams dp) {
          log("DcInactiveState: setEnterNoticationParams dp");
            mDisconnectParams = dp;
        }
        @Override protected void enter() {
            mTag += 1;
            if ((mConnectionParams != null) && (mFailCause != null)) {
                log("DcInactiveState: enter notifyConnectCompleted");
                notifyConnectCompleted(mConnectionParams, mFailCause);
            }
            if (mDisconnectParams != null) {
              log("DcInactiveState: enter notifyDisconnectCompleted");
                notifyDisconnectCompleted(mDisconnectParams);
            }
        }
        @Override protected void exit() {
            mConnectionParams = null;
            mFailCause = null;
            mDisconnectParams = null;
        }
        @Override protected boolean processMessage(Message msg) {
            boolean retVal;
            switch (msg.what) {
                case EVENT_RESET:
                    if (DBG) {
                        log("DcInactiveState: msg.what=EVENT_RESET, ignore we're already reset");
                    }
                    if (msg.obj != null) {
                        notifyDisconnectCompleted((DisconnectParams) msg.obj);
                    }
                    retVal = true;
                    break;
                case EVENT_CONNECT:
                    if (DBG) log("DcInactiveState msg.what=EVENT_CONNECT");
                    ConnectionParams cp = (ConnectionParams) msg.obj;
                    cp.tag = mTag;
                    onConnect(cp);
                    transitionTo(mActivatingState);
                    retVal = true;
                    break;
                default:
                    if (DBG) log("DcInactiveState nothandled msg.what=" + msg.what);
                    retVal = false;
                    break;
            }
            return retVal;
        }
    }
    private DcInactiveState mInactiveState = new DcInactiveState();
    private class DcActivatingState extends HierarchicalState {
        @Override protected boolean processMessage(Message msg) {
            boolean retVal;
            AsyncResult ar;
            ConnectionParams cp;
            switch (msg.what) {
                case EVENT_DISCONNECT:
                    if (DBG) log("DcActivatingState deferring msg.what=EVENT_DISCONNECT");
                    deferMessage(msg);
                    retVal = true;
                    break;
                case EVENT_SETUP_DATA_CONNECTION_DONE:
                    if (DBG) log("DcActivatingState msg.what=EVENT_SETUP_DATA_CONNECTION_DONE");
                    ar = (AsyncResult) msg.obj;
                    cp = (ConnectionParams) ar.userObj;
                    SetupResult result = onSetupConnectionCompleted(ar);
                    switch (result) {
                        case SUCCESS:
                            mActiveState.setEnterNotificationParams(cp, FailCause.NONE);
                            transitionTo(mActiveState);
                            break;
                        case ERR_BadCommand:
                            mInactiveState.setEnterNotificationParams(cp, result.mFailCause);
                            transitionTo(mInactiveState);
                            break;
                        case ERR_BadDns:
                            EventLog.writeEvent(EventLogTags.PDP_BAD_DNS_ADDRESS, dnsServers[0]);
                            tearDownData(cp);
                            transitionTo(mDisconnectingBadDnsState);
                            break;
                        case ERR_Other:
                            phone.mCM.getLastDataCallFailCause(
                                    obtainMessage(EVENT_GET_LAST_FAIL_DONE, cp));
                            break;
                        case ERR_Stale:
                            break;
                        default:
                            throw new RuntimeException("Unkown SetupResult, should not happen");
                    }
                    retVal = true;
                    break;
                case EVENT_GET_LAST_FAIL_DONE:
                    ar = (AsyncResult) msg.obj;
                    cp = (ConnectionParams) ar.userObj;
                    FailCause cause = FailCause.UNKNOWN;
                    if (cp.tag == mTag) {
                        if (DBG) log("DcActivatingState msg.what=EVENT_GET_LAST_FAIL_DONE");
                        if (ar.exception == null) {
                            int rilFailCause = ((int[]) (ar.result))[0];
                            cause = getFailCauseFromRequest(rilFailCause);
                        }
                         mInactiveState.setEnterNotificationParams(cp, cause);
                         transitionTo(mInactiveState);
                    } else {
                        if (DBG) {
                            log("DcActivatingState EVENT_GET_LAST_FAIL_DONE is stale cp.tag="
                                + cp.tag + ", mTag=" + mTag);
                        }
                    }
                    retVal = true;
                    break;
                default:
                    if (DBG) log("DcActivatingState not handled msg.what=" + msg.what);
                    retVal = false;
                    break;
            }
            return retVal;
        }
    }
    private DcActivatingState mActivatingState = new DcActivatingState();
    private class DcActiveState extends HierarchicalState {
        private ConnectionParams mConnectionParams = null;
        private FailCause mFailCause = null;
        public void setEnterNotificationParams(ConnectionParams cp, FailCause cause) {
            log("DcInactiveState: setEnterNoticationParams cp,cause");
            mConnectionParams = cp;
            mFailCause = cause;
        }
        @Override public void enter() {
            if ((mConnectionParams != null) && (mFailCause != null)) {
                log("DcActiveState: enter notifyConnectCompleted");
                notifyConnectCompleted(mConnectionParams, mFailCause);
            }
        }
        @Override protected void exit() {
            mConnectionParams = null;
            mFailCause = null;
        }
        @Override protected boolean processMessage(Message msg) {
            boolean retVal;
            switch (msg.what) {
                case EVENT_DISCONNECT:
                    if (DBG) log("DcActiveState msg.what=EVENT_DISCONNECT");
                    DisconnectParams dp = (DisconnectParams) msg.obj;
                    dp.tag = mTag;
                    tearDownData(dp);
                    transitionTo(mDisconnectingState);
                    retVal = true;
                    break;
                default:
                    if (DBG) log("DcActiveState nothandled msg.what=" + msg.what);
                    retVal = false;
                    break;
            }
            return retVal;
        }
    }
    private DcActiveState mActiveState = new DcActiveState();
    private class DcDisconnectingState extends HierarchicalState {
        @Override protected boolean processMessage(Message msg) {
            boolean retVal;
            switch (msg.what) {
                case EVENT_DEACTIVATE_DONE:
                    if (DBG) log("DcDisconnectingState msg.what=EVENT_DEACTIVATE_DONE");
                    AsyncResult ar = (AsyncResult) msg.obj;
                    DisconnectParams dp = (DisconnectParams) ar.userObj;
                    if (dp.tag == mTag) {
                        mInactiveState.setEnterNotificationParams((DisconnectParams) ar.userObj);
                        transitionTo(mInactiveState);
                    } else {
                        if (DBG) log("DcDisconnectState EVENT_DEACTIVATE_DONE stale dp.tag="
                                + dp.tag + " mTag=" + mTag);
                    }
                    retVal = true;
                    break;
                default:
                    if (DBG) log("DcDisconnectingState not handled msg.what=" + msg.what);
                    retVal = false;
                    break;
            }
            return retVal;
        }
    }
    private DcDisconnectingState mDisconnectingState = new DcDisconnectingState();
    private class DcDisconnectingBadDnsState extends HierarchicalState {
        @Override protected boolean processMessage(Message msg) {
            boolean retVal;
            switch (msg.what) {
                case EVENT_DEACTIVATE_DONE:
                    AsyncResult ar = (AsyncResult) msg.obj;
                    ConnectionParams cp = (ConnectionParams) ar.userObj;
                    if (cp.tag == mTag) {
                        if (DBG) log("DcDisconnectingBadDnsState msg.what=EVENT_DEACTIVATE_DONE");
                        mInactiveState.setEnterNotificationParams(cp, FailCause.UNKNOWN);
                        transitionTo(mInactiveState);
                    } else {
                        if (DBG) log("DcDisconnectingBadDnsState EVENT_DEACTIVE_DONE stale dp.tag="
                                + cp.tag + ", mTag=" + mTag);
                    }
                    retVal = true;
                    break;
                default:
                    if (DBG) log("DcDisconnectingBadDnsState not handled msg.what=" + msg.what);
                    retVal = false;
                    break;
            }
            return retVal;
        }
    }
    private DcDisconnectingBadDnsState mDisconnectingBadDnsState = new DcDisconnectingBadDnsState();
    public void reset(Message onCompletedMsg) {
        sendMessage(obtainMessage(EVENT_RESET, new DisconnectParams(onCompletedMsg)));
    }
    public void resetSynchronously() {
        ResetSynchronouslyLock lockObj = new ResetSynchronouslyLock();
        synchronized(lockObj) {
            sendMessage(obtainMessage(EVENT_RESET, new DisconnectParams(lockObj)));
            try {
                lockObj.wait();
            } catch (InterruptedException e) {
                log("blockingReset: unexpected interrupted of wait()");
            }
        }
    }
    public void connect(Message onCompletedMsg, ApnSetting apn) {
        sendMessage(obtainMessage(EVENT_CONNECT, new ConnectionParams(apn, onCompletedMsg)));
    }
    public void connect(Message onCompletedMsg) {
        sendMessage(obtainMessage(EVENT_CONNECT, new ConnectionParams(null, onCompletedMsg)));
    }
    public void disconnect(Message onCompletedMsg) {
        sendMessage(obtainMessage(EVENT_DISCONNECT, new DisconnectParams(onCompletedMsg)));
    }
    public boolean isInactive() {
        boolean retVal = getCurrentState() == mInactiveState;
        return retVal;
    }
    public boolean isActive() {
        boolean retVal = getCurrentState() == mActiveState;
        return retVal;
    }
    public String getInterface() {
        return interfaceName;
    }
    public String getIpAddress() {
        return ipAddress;
    }
    public String getGatewayAddress() {
        return gatewayAddress;
    }
    public String[] getDnsServers() {
        return dnsServers;
    }
    public String getStateAsString() {
        String retVal = getCurrentState().getName();
        return retVal;
    }
    public long getConnectionTime() {
        return createTime;
    }
    public long getLastFailTime() {
        return lastFailTime;
    }
    public FailCause getLastFailCause() {
        return lastFailCause;
    }
}
