public abstract class DataConnectionTracker extends Handler {
    protected static final boolean DBG = true;
    protected final String LOG_TAG = "DataConnectionTracker";
    public enum State {
        IDLE,
        INITING,
        CONNECTING,
        SCANNING,
        CONNECTED,
        DISCONNECTING,
        FAILED
    }
    public enum Activity {
        NONE,
        DATAIN,
        DATAOUT,
        DATAINANDOUT,
        DORMANT
    }
    protected static final int EVENT_DATA_SETUP_COMPLETE = 1;
    protected static final int EVENT_RADIO_AVAILABLE = 3;
    protected static final int EVENT_RECORDS_LOADED = 4;
    protected static final int EVENT_TRY_SETUP_DATA = 5;
    protected static final int EVENT_DATA_STATE_CHANGED = 6;
    protected static final int EVENT_POLL_PDP = 7;
    protected static final int EVENT_GET_PDP_LIST_COMPLETE = 11;
    protected static final int EVENT_RADIO_OFF_OR_NOT_AVAILABLE = 12;
    protected static final int EVENT_VOICE_CALL_STARTED = 14;
    protected static final int EVENT_VOICE_CALL_ENDED = 15;
    protected static final int EVENT_GPRS_DETACHED = 19;
    protected static final int EVENT_LINK_STATE_CHANGED = 20;
    protected static final int EVENT_ROAMING_ON = 21;
    protected static final int EVENT_ROAMING_OFF = 22;
    protected static final int EVENT_ENABLE_NEW_APN = 23;
    protected static final int EVENT_RESTORE_DEFAULT_APN = 24;
    protected static final int EVENT_DISCONNECT_DONE = 25;
    protected static final int EVENT_GPRS_ATTACHED = 26;
    protected static final int EVENT_START_NETSTAT_POLL = 27;
    protected static final int EVENT_START_RECOVERY = 28;
    protected static final int EVENT_APN_CHANGED = 29;
    protected static final int EVENT_CDMA_DATA_DETACHED = 30;
    protected static final int EVENT_NV_READY = 31;
    protected static final int EVENT_PS_RESTRICT_ENABLED = 32;
    protected static final int EVENT_PS_RESTRICT_DISABLED = 33;
    public static final int EVENT_CLEAN_UP_CONNECTION = 34;
    protected static final int EVENT_CDMA_OTA_PROVISION = 35;
    protected static final int EVENT_RESTART_RADIO = 36;
    protected static final int EVENT_SET_MASTER_DATA_ENABLE = 37;
    protected static final int EVENT_RESET_DONE = 38;
    protected static final int APN_INVALID_ID = -1;
    protected static final int APN_DEFAULT_ID = 0;
    protected static final int APN_MMS_ID = 1;
    protected static final int APN_SUPL_ID = 2;
    protected static final int APN_DUN_ID = 3;
    protected static final int APN_HIPRI_ID = 4;
    protected static final int APN_NUM_TYPES = 5;
    protected static final int DISABLED = 0;
    protected static final int ENABLED = 1;
    protected boolean mMasterDataEnabled = true;
    protected boolean[] dataEnabled = new boolean[APN_NUM_TYPES];
    protected int enabledCount = 0;
    protected String mRequestedApnType = Phone.APN_TYPE_DEFAULT;
    protected static final String DEFAULT_DATA_RETRY_CONFIG = "default_randomization=2000,"
        + "5000,10000,20000,40000,80000:5000,160000:5000,"
        + "320000:5000,640000:5000,1280000:5000,1800000:5000";
    protected static final String SECONDARY_DATA_RETRY_CONFIG =
            "max_retries=3, 5000, 5000, 5000";
    protected static final int POLL_NETSTAT_SLOW_MILLIS = 5000;
    protected static final int DEFAULT_PING_DEADLINE = 5;
    protected static final int DEFAULT_MAX_PDP_RESET_FAIL = 3;
    protected static final int NO_RECV_POLL_LIMIT = 24;
    protected static final int POLL_NETSTAT_MILLIS = 1000;
    protected static final int POLL_NETSTAT_SCREEN_OFF_MILLIS = 1000*60*10;
    protected static final int POLL_LONGEST_RTT = 120 * 1000;
    protected static final int NUMBER_SENT_PACKETS_OF_HANG = 10;
    protected static final int RESTORE_DEFAULT_APN_DELAY = 1 * 60 * 1000;
    protected static final String APN_RESTORE_DELAY_PROP_NAME = "android.telephony.apn-restore";
    protected static final String NULL_IP = "0.0.0.0";
    protected PhoneBase phone;
    protected Activity activity = Activity.NONE;
    protected State state = State.IDLE;
    protected Handler mDataConnectionTracker = null;
    protected long txPkts, rxPkts, sentSinceLastRecv;
    protected int netStatPollPeriod;
    protected int mNoRecvPollCount = 0;
    protected boolean netStatPollEnabled = false;
    protected RetryManager mRetryMgr = new RetryManager();
    protected boolean mIsWifiConnected = false;
    protected PendingIntent mReconnectIntent = null;
    protected int cidActive;
    protected DataConnectionTracker(PhoneBase phone) {
        super();
        this.phone = phone;
    }
    public abstract void dispose();
    public Activity getActivity() {
        return activity;
    }
    public State getState() {
        return state;
    }
    public String getStateInString() {
        switch (state) {
            case IDLE:          return "IDLE";
            case INITING:       return "INIT";
            case CONNECTING:    return "CING";
            case SCANNING:      return "SCAN";
            case CONNECTED:     return "CNTD";
            case DISCONNECTING: return "DING";
            case FAILED:        return "FAIL";
            default:            return "ERRO";
        }
    }
    public abstract boolean isDataConnectionAsDesired();
    public void setDataOnRoamingEnabled(boolean enabled) {
        if (getDataOnRoamingEnabled() != enabled) {
            Settings.Secure.putInt(phone.getContext().getContentResolver(),
                Settings.Secure.DATA_ROAMING, enabled ? 1 : 0);
            if (phone.getServiceState().getRoaming()) {
                if (enabled) {
                    mRetryMgr.resetRetryCount();
                }
                sendMessage(obtainMessage(EVENT_ROAMING_ON));
            }
        }
    }
    public boolean getDataOnRoamingEnabled() {
        try {
            return Settings.Secure.getInt(phone.getContext().getContentResolver(),
                Settings.Secure.DATA_ROAMING) > 0;
        } catch (SettingNotFoundException snfe) {
            return false;
        }
    }
    protected abstract boolean onTrySetupData(String reason);
    protected abstract void onRoamingOff();
    protected abstract void onRoamingOn();
    protected abstract void onRadioAvailable();
    protected abstract void onRadioOffOrNotAvailable();
    protected abstract void onDataSetupComplete(AsyncResult ar);
    protected abstract void onDisconnectDone(AsyncResult ar);
    protected abstract void onResetDone(AsyncResult ar);
    protected abstract void onVoiceCallStarted();
    protected abstract void onVoiceCallEnded();
    protected abstract void onCleanUpConnection(boolean tearDown, String reason);
    @Override
    public void handleMessage (Message msg) {
        switch (msg.what) {
            case EVENT_ENABLE_NEW_APN:
                onEnableApn(msg.arg1, msg.arg2);
                break;
            case EVENT_TRY_SETUP_DATA:
                String reason = null;
                if (msg.obj instanceof String) {
                    reason = (String)msg.obj;
                }
                onTrySetupData(reason);
                break;
            case EVENT_ROAMING_OFF:
                if (getDataOnRoamingEnabled() == false) {
                    mRetryMgr.resetRetryCount();
                }
                onRoamingOff();
                break;
            case EVENT_ROAMING_ON:
                onRoamingOn();
                break;
            case EVENT_RADIO_AVAILABLE:
                onRadioAvailable();
                break;
            case EVENT_RADIO_OFF_OR_NOT_AVAILABLE:
                onRadioOffOrNotAvailable();
                break;
            case EVENT_DATA_SETUP_COMPLETE:
                cidActive = msg.arg1;
                onDataSetupComplete((AsyncResult) msg.obj);
                break;
            case EVENT_DISCONNECT_DONE:
                onDisconnectDone((AsyncResult) msg.obj);
                break;
            case EVENT_VOICE_CALL_STARTED:
                onVoiceCallStarted();
                break;
            case EVENT_VOICE_CALL_ENDED:
                onVoiceCallEnded();
                break;
            case EVENT_CLEAN_UP_CONNECTION:
                boolean tearDown = (msg.arg1 == 0) ? false : true;
                onCleanUpConnection(tearDown, (String)msg.obj);
                break;
            case EVENT_SET_MASTER_DATA_ENABLE:
                boolean enabled = (msg.arg1 == ENABLED) ? true : false;
                onSetDataEnabled(enabled);
                break;
            case EVENT_RESET_DONE:
                onResetDone((AsyncResult) msg.obj);
                break;
            default:
                Log.e("DATA", "Unidentified event = " + msg.what);
                break;
        }
    }
    public synchronized boolean getDataEnabled() {
        return dataEnabled[APN_DEFAULT_ID];
    }
    public boolean getAnyDataEnabled() {
        return (enabledCount != 0);
    }
    protected abstract void startNetStatPoll();
    protected abstract void stopNetStatPoll();
    protected abstract void restartRadio();
    protected abstract void log(String s);
    protected int apnTypeToId(String type) {
        if (TextUtils.equals(type, Phone.APN_TYPE_DEFAULT)) {
            return APN_DEFAULT_ID;
        } else if (TextUtils.equals(type, Phone.APN_TYPE_MMS)) {
            return APN_MMS_ID;
        } else if (TextUtils.equals(type, Phone.APN_TYPE_SUPL)) {
            return APN_SUPL_ID;
        } else if (TextUtils.equals(type, Phone.APN_TYPE_DUN)) {
            return APN_DUN_ID;
        } else if (TextUtils.equals(type, Phone.APN_TYPE_HIPRI)) {
            return APN_HIPRI_ID;
        } else {
            return APN_INVALID_ID;
        }
    }
    protected String apnIdToType(int id) {
        switch (id) {
        case APN_DEFAULT_ID:
            return Phone.APN_TYPE_DEFAULT;
        case APN_MMS_ID:
            return Phone.APN_TYPE_MMS;
        case APN_SUPL_ID:
            return Phone.APN_TYPE_SUPL;
        case APN_DUN_ID:
            return Phone.APN_TYPE_DUN;
        case APN_HIPRI_ID:
            return Phone.APN_TYPE_HIPRI;
        default:
            Log.e(LOG_TAG, "Unknown id (" + id + ") in apnIdToType");
            return Phone.APN_TYPE_DEFAULT;
        }
    }
    protected abstract boolean isApnTypeActive(String type);
    protected abstract boolean isApnTypeAvailable(String type);
    protected abstract String[] getActiveApnTypes();
    protected abstract String getActiveApnString();
    public abstract ArrayList<DataConnection> getAllDataConnections();
    protected abstract String getInterfaceName(String apnType);
    protected abstract String getIpAddress(String apnType);
    protected abstract String getGateway(String apnType);
    protected abstract String[] getDnsServers(String apnType);
    protected abstract void setState(State s);
    protected synchronized boolean isEnabled(int id) {
        if (id != APN_INVALID_ID) {
            return dataEnabled[id];
        }
        return false;
    }
    public synchronized int enableApnType(String type) {
        int id = apnTypeToId(type);
        if (id == APN_INVALID_ID) {
            return Phone.APN_REQUEST_FAILED;
        }
        if (DBG) Log.d(LOG_TAG, "enableApnType("+type+"), isApnTypeActive = "
                + isApnTypeActive(type) + " and state = " + state);
        if (!isApnTypeAvailable(type)) {
            if (DBG) Log.d(LOG_TAG, "type not available");
            return Phone.APN_TYPE_NOT_AVAILABLE;
        }
        setEnabled(id, true);
        if (isApnTypeActive(type)) {
            if (state == State.INITING) return Phone.APN_REQUEST_STARTED;
            else if (state == State.CONNECTED) return Phone.APN_ALREADY_ACTIVE;
        }
        return Phone.APN_REQUEST_STARTED;
    }
    public synchronized int disableApnType(String type) {
        if (DBG) Log.d(LOG_TAG, "disableApnType("+type+")");
        int id = apnTypeToId(type);
        if (id == APN_INVALID_ID) {
            return Phone.APN_REQUEST_FAILED;
        }
        if (isEnabled(id)) {
            setEnabled(id, false);
            if (isApnTypeActive(Phone.APN_TYPE_DEFAULT)) {
                if (dataEnabled[APN_DEFAULT_ID]) {
                    return Phone.APN_ALREADY_ACTIVE;
                } else {
                    return Phone.APN_REQUEST_STARTED;
                }
            } else {
                return Phone.APN_REQUEST_STARTED;
            }
        } else {
            return Phone.APN_REQUEST_FAILED;
        }
    }
    private void setEnabled(int id, boolean enable) {
        if (DBG) Log.d(LOG_TAG, "setEnabled(" + id + ", " + enable + ") with old state = " +
                dataEnabled[id] + " and enabledCount = " + enabledCount);
        Message msg = obtainMessage(EVENT_ENABLE_NEW_APN);
        msg.arg1 = id;
        msg.arg2 = (enable ? ENABLED : DISABLED);
        sendMessage(msg);
    }
    protected synchronized void onEnableApn(int apnId, int enabled) {
        if (DBG) {
            Log.d(LOG_TAG, "EVENT_APN_ENABLE_REQUEST " + apnId + ", " + enabled);
            Log.d(LOG_TAG, " dataEnabled = " + dataEnabled[apnId] +
                    ", enabledCount = " + enabledCount +
                    ", isApnTypeActive = " + isApnTypeActive(apnIdToType(apnId)));
        }
        if (enabled == ENABLED) {
            if (!dataEnabled[apnId]) {
                dataEnabled[apnId] = true;
                enabledCount++;
            }
            String type = apnIdToType(apnId);
            if (!isApnTypeActive(type)) {
                mRequestedApnType = type;
                onEnableNewApn();
            }
        } else {
            if (dataEnabled[apnId]) {
                dataEnabled[apnId] = false;
                enabledCount--;
                if (enabledCount == 0) {
                    onCleanUpConnection(true, Phone.REASON_DATA_DISABLED);
                } else if (dataEnabled[APN_DEFAULT_ID] == true &&
                        !isApnTypeActive(Phone.APN_TYPE_DEFAULT)) {
                    mRequestedApnType = Phone.APN_TYPE_DEFAULT;
                    onEnableNewApn();
                }
            }
        }
    }
    protected void onEnableNewApn() {
    }
    public boolean setDataEnabled(boolean enable) {
        if (DBG) Log.d(LOG_TAG, "setDataEnabled(" + enable + ")");
        Message msg = obtainMessage(EVENT_SET_MASTER_DATA_ENABLE);
        msg.arg1 = (enable ? ENABLED : DISABLED);
        sendMessage(msg);
        return true;
    }
    protected void onSetDataEnabled(boolean enable) {
        if (mMasterDataEnabled != enable) {
            mMasterDataEnabled = enable;
            if (enable) {
                mRetryMgr.resetRetryCount();
                onTrySetupData(Phone.REASON_DATA_ENABLED);
            } else {
                onCleanUpConnection(true, Phone.REASON_DATA_DISABLED);
           }
        }
    }
}
