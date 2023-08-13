public abstract class ServiceStateTracker extends Handler {
    protected static final int DATA_ACCESS_UNKNOWN = 0;
    protected static final int DATA_ACCESS_GPRS = 1;
    protected static final int DATA_ACCESS_EDGE = 2;
    protected static final int DATA_ACCESS_UMTS = 3;
    protected static final int DATA_ACCESS_CDMA_IS95A = 4;
    protected static final int DATA_ACCESS_CDMA_IS95B = 5;
    protected static final int DATA_ACCESS_CDMA_1xRTT = 6;
    protected static final int DATA_ACCESS_CDMA_EvDo_0 = 7;
    protected static final int DATA_ACCESS_CDMA_EvDo_A = 8;
    protected static final int DATA_ACCESS_HSDPA = 9;
    protected static final int DATA_ACCESS_HSUPA = 10;
    protected static final int DATA_ACCESS_HSPA = 11;
    protected CommandsInterface cm;
    public ServiceState ss;
    protected ServiceState newSS;
    public SignalStrength mSignalStrength;
    protected int[] pollingContext;
    protected boolean mDesiredPowerState;
    protected boolean dontPollSignalStrength = false;
    protected RegistrantList networkAttachedRegistrants = new RegistrantList();
    protected RegistrantList roamingOnRegistrants = new RegistrantList();
    protected RegistrantList roamingOffRegistrants = new RegistrantList();
    protected  static final boolean DBG = true;
    protected static final int POLL_PERIOD_MILLIS = 20 * 1000;
    public static final int DEFAULT_GPRS_CHECK_PERIOD_MILLIS = 60 * 1000;
    public static final int DATA_STATE_POLL_SLEEP_MS = 100;
    protected static final int EVENT_RADIO_STATE_CHANGED               = 1;
    protected static final int EVENT_NETWORK_STATE_CHANGED             = 2;
    protected static final int EVENT_GET_SIGNAL_STRENGTH               = 3;
    protected static final int EVENT_POLL_STATE_REGISTRATION           = 4;
    protected static final int EVENT_POLL_STATE_GPRS                   = 5;
    protected static final int EVENT_POLL_STATE_OPERATOR               = 6;
    protected static final int EVENT_POLL_SIGNAL_STRENGTH              = 10;
    protected static final int EVENT_NITZ_TIME                         = 11;
    protected static final int EVENT_SIGNAL_STRENGTH_UPDATE            = 12;
    protected static final int EVENT_RADIO_AVAILABLE                   = 13;
    protected static final int EVENT_POLL_STATE_NETWORK_SELECTION_MODE = 14;
    protected static final int EVENT_GET_LOC_DONE                      = 15;
    protected static final int EVENT_SIM_RECORDS_LOADED                = 16;
    protected static final int EVENT_SIM_READY                         = 17;
    protected static final int EVENT_LOCATION_UPDATES_ENABLED          = 18;
    protected static final int EVENT_GET_PREFERRED_NETWORK_TYPE        = 19;
    protected static final int EVENT_SET_PREFERRED_NETWORK_TYPE        = 20;
    protected static final int EVENT_RESET_PREFERRED_NETWORK_TYPE      = 21;
    protected static final int EVENT_CHECK_REPORT_GPRS                 = 22;
    protected static final int EVENT_RESTRICTED_STATE_CHANGED          = 23;
    protected static final int EVENT_POLL_STATE_REGISTRATION_CDMA      = 24;
    protected static final int EVENT_POLL_STATE_OPERATOR_CDMA          = 25;
    protected static final int EVENT_RUIM_READY                        = 26;
    protected static final int EVENT_RUIM_RECORDS_LOADED               = 27;
    protected static final int EVENT_POLL_SIGNAL_STRENGTH_CDMA         = 28;
    protected static final int EVENT_GET_SIGNAL_STRENGTH_CDMA          = 29;
    protected static final int EVENT_NETWORK_STATE_CHANGED_CDMA        = 30;
    protected static final int EVENT_GET_LOC_DONE_CDMA                 = 31;
    protected static final int EVENT_SIGNAL_STRENGTH_UPDATE_CDMA       = 32;
    protected static final int EVENT_NV_LOADED                         = 33;
    protected static final int EVENT_POLL_STATE_CDMA_SUBSCRIPTION      = 34;
    protected static final int EVENT_NV_READY                          = 35;
    protected static final int EVENT_ERI_FILE_LOADED                   = 36;
    protected static final int EVENT_OTA_PROVISION_STATUS_CHANGE       = 37;
    protected static final int EVENT_SET_RADIO_POWER_OFF               = 38;
    protected static final String TIMEZONE_PROPERTY = "persist.sys.timezone";
    protected static final String[] GMT_COUNTRY_CODES = {
        "bf", 
        "ci", 
        "eh", 
        "fo", 
        "gh", 
        "gm", 
        "gn", 
        "gw", 
        "ie", 
        "lr", 
        "is", 
        "ma", 
        "ml", 
        "mr", 
        "pt", 
        "sl", 
        "sn", 
        "st", 
        "tg", 
        "uk", 
    };
    protected static final String REGISTRATION_DENIED_GEN  = "General";
    protected static final String REGISTRATION_DENIED_AUTH = "Authentication Failure";
    public ServiceStateTracker() {
    }
    public boolean getDesiredPowerState() {
        return mDesiredPowerState;
    }
    public  void registerForRoamingOn(Handler h, int what, Object obj) {
        Registrant r = new Registrant(h, what, obj);
        roamingOnRegistrants.add(r);
        if (ss.getRoaming()) {
            r.notifyRegistrant();
        }
    }
    public  void unregisterForRoamingOn(Handler h) {
        roamingOnRegistrants.remove(h);
    }
    public  void registerForRoamingOff(Handler h, int what, Object obj) {
        Registrant r = new Registrant(h, what, obj);
        roamingOffRegistrants.add(r);
        if (!ss.getRoaming()) {
            r.notifyRegistrant();
        }
    }
    public  void unregisterForRoamingOff(Handler h) {
        roamingOffRegistrants.remove(h);
    }
    public void reRegisterNetwork(Message onComplete) {
        cm.getPreferredNetworkType(
                obtainMessage(EVENT_GET_PREFERRED_NETWORK_TYPE, onComplete));
    }
    public void
    setRadioPower(boolean power) {
        mDesiredPowerState = power;
        setPowerStateToDesired();
    }
    private boolean mWantContinuousLocationUpdates;
    private boolean mWantSingleLocationUpdate;
    public void enableSingleLocationUpdate() {
        if (mWantSingleLocationUpdate || mWantContinuousLocationUpdates) return;
        mWantSingleLocationUpdate = true;
        cm.setLocationUpdates(true, obtainMessage(EVENT_LOCATION_UPDATES_ENABLED));
    }
    public void enableLocationUpdates() {
        if (mWantSingleLocationUpdate || mWantContinuousLocationUpdates) return;
        mWantContinuousLocationUpdates = true;
        cm.setLocationUpdates(true, obtainMessage(EVENT_LOCATION_UPDATES_ENABLED));
    }
    protected void disableSingleLocationUpdate() {
        mWantSingleLocationUpdate = false;
        if (!mWantSingleLocationUpdate && !mWantContinuousLocationUpdates) {
            cm.setLocationUpdates(false, null);
        }
    }
    public void disableLocationUpdates() {
        mWantContinuousLocationUpdates = false;
        if (!mWantSingleLocationUpdate && !mWantContinuousLocationUpdates) {
            cm.setLocationUpdates(false, null);
        }
    }
    public abstract void handleMessage(Message msg);
    protected abstract void handlePollStateResult(int what, AsyncResult ar);
    protected abstract void updateSpnDisplay();
    protected abstract void setPowerStateToDesired();
    protected abstract void powerOffRadioSafely();
    protected void cancelPollState() {
        pollingContext = new int[1];
    }
}
