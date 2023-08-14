final class GsmServiceStateTracker extends ServiceStateTracker {
    static final String LOG_TAG = "GSM";
    static final boolean DBG = true;
    GSMPhone phone;
    GsmCellLocation cellLoc;
    GsmCellLocation newCellLoc;
    int mPreferredNetworkType;
    RestrictedState rs;
    private int gprsState = ServiceState.STATE_OUT_OF_SERVICE;
    private int newGPRSState = ServiceState.STATE_OUT_OF_SERVICE;
    private int networkType = 0;
    private int newNetworkType = 0;
    private boolean mGsmRoaming = false;
    private boolean mDataRoaming = false;
    private boolean mEmergencyOnly = false;
    private RegistrantList gprsAttachedRegistrants = new RegistrantList();
    private RegistrantList gprsDetachedRegistrants = new RegistrantList();
    private RegistrantList psRestrictEnabledRegistrants = new RegistrantList();
    private RegistrantList psRestrictDisabledRegistrants = new RegistrantList();
    private boolean mNeedFixZone = false;
    private int mZoneOffset;
    private boolean mZoneDst;
    private long mZoneTime;
    private boolean mGotCountryCode = false;
    private ContentResolver cr;
    String mSavedTimeZone;
    long mSavedTime;
    long mSavedAtTime;
    private boolean mNeedToRegForSimLoaded;
    private boolean mStartedGprsRegCheck = false;
    private boolean mReportedGprsNoReg = false;
    private Notification mNotification;
    private PowerManager.WakeLock mWakeLock;
    private static final String WAKELOCK_TAG = "ServiceStateTracker";
    private String curSpn = null;
    private String curPlmn = null;
    private int curSpnRule = 0;
    static final int DEFAULT_GPRS_CHECK_PERIOD_MILLIS = 60 * 1000;
    static final int PS_ENABLED = 1001;            
    static final int PS_DISABLED = 1002;           
    static final int CS_ENABLED = 1003;            
    static final int CS_DISABLED = 1004;           
    static final int CS_NORMAL_ENABLED = 1005;     
    static final int CS_EMERGENCY_ENABLED = 1006;  
    static final int PS_NOTIFICATION = 888;  
    static final int CS_NOTIFICATION = 999;  
    static final int MAX_NUM_DATA_STATE_READS = 15;
    private BroadcastReceiver mIntentReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().equals(Intent.ACTION_LOCALE_CHANGED)) {
                updateSpnDisplay();
            }
        }
    };
    private ContentObserver mAutoTimeObserver = new ContentObserver(new Handler()) {
        @Override
        public void onChange(boolean selfChange) {
            Log.i("GsmServiceStateTracker", "Auto time state changed");
            revertToNitz();
        }
    };
    public GsmServiceStateTracker(GSMPhone phone) {
        super();
        this.phone = phone;
        cm = phone.mCM;
        ss = new ServiceState();
        newSS = new ServiceState();
        cellLoc = new GsmCellLocation();
        newCellLoc = new GsmCellLocation();
        rs = new RestrictedState();
        mSignalStrength = new SignalStrength();
        PowerManager powerManager =
                (PowerManager)phone.getContext().getSystemService(Context.POWER_SERVICE);
        mWakeLock = powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, WAKELOCK_TAG);
        cm.registerForAvailable(this, EVENT_RADIO_AVAILABLE, null);
        cm.registerForRadioStateChanged(this, EVENT_RADIO_STATE_CHANGED, null);
        cm.registerForNetworkStateChanged(this, EVENT_NETWORK_STATE_CHANGED, null);
        cm.setOnNITZTime(this, EVENT_NITZ_TIME, null);
        cm.setOnSignalStrengthUpdate(this, EVENT_SIGNAL_STRENGTH_UPDATE, null);
        cm.setOnRestrictedStateChanged(this, EVENT_RESTRICTED_STATE_CHANGED, null);
        cm.registerForSIMReady(this, EVENT_SIM_READY, null);
        int airplaneMode = Settings.System.getInt(
                phone.getContext().getContentResolver(),
                Settings.System.AIRPLANE_MODE_ON, 0);
        mDesiredPowerState = ! (airplaneMode > 0);
        cr = phone.getContext().getContentResolver();
        cr.registerContentObserver(
                Settings.System.getUriFor(Settings.System.AUTO_TIME), true,
                mAutoTimeObserver);
        setSignalStrengthDefaultValues();
        mNeedToRegForSimLoaded = true;
        IntentFilter filter = new IntentFilter();
        filter.addAction(Intent.ACTION_LOCALE_CHANGED);
        phone.getContext().registerReceiver(mIntentReceiver, filter);
    }
    public void dispose() {
        cm.unregisterForAvailable(this);
        cm.unregisterForRadioStateChanged(this);
        cm.unregisterForNetworkStateChanged(this);
        cm.unregisterForSIMReady(this);
        phone.mSIMRecords.unregisterForRecordsLoaded(this);
        cm.unSetOnSignalStrengthUpdate(this);
        cm.unSetOnRestrictedStateChanged(this);
        cm.unSetOnNITZTime(this);
        cr.unregisterContentObserver(this.mAutoTimeObserver);
    }
    protected void finalize() {
        if(DBG) Log.d(LOG_TAG, "GsmServiceStateTracker finalized");
    }
    void registerForGprsAttached(Handler h, int what, Object obj) {
        Registrant r = new Registrant(h, what, obj);
        gprsAttachedRegistrants.add(r);
        if (gprsState == ServiceState.STATE_IN_SERVICE) {
            r.notifyRegistrant();
        }
    }
    void unregisterForGprsAttached(Handler h) {
        gprsAttachedRegistrants.remove(h);
    }
    void registerForNetworkAttach(Handler h, int what, Object obj) {
        Registrant r = new Registrant(h, what, obj);
        networkAttachedRegistrants.add(r);
        if (ss.getState() == ServiceState.STATE_IN_SERVICE) {
            r.notifyRegistrant();
        }
    }
    void unregisterForNetworkAttach(Handler h) {
        networkAttachedRegistrants.remove(h);
    }
    void registerForGprsDetached(Handler h, int what, Object obj) {
        Registrant r = new Registrant(h, what, obj);
        gprsDetachedRegistrants.add(r);
        if (gprsState == ServiceState.STATE_OUT_OF_SERVICE) {
            r.notifyRegistrant();
        }
    }
    void unregisterForGprsDetached(Handler h) {
        gprsDetachedRegistrants.remove(h);
    }
    void registerForPsRestrictedEnabled(Handler h, int what, Object obj) {
        Log.d(LOG_TAG, "[DSAC DEB] " + "registerForPsRestrictedEnabled ");
        Registrant r = new Registrant(h, what, obj);
        psRestrictEnabledRegistrants.add(r);
        if (rs.isPsRestricted()) {
            r.notifyRegistrant();
        }
    }
    void unregisterForPsRestrictedEnabled(Handler h) {
        psRestrictEnabledRegistrants.remove(h);
    }
    void registerForPsRestrictedDisabled(Handler h, int what, Object obj) {
        Log.d(LOG_TAG, "[DSAC DEB] " + "registerForPsRestrictedDisabled ");
        Registrant r = new Registrant(h, what, obj);
        psRestrictDisabledRegistrants.add(r);
        if (rs.isPsRestricted()) {
            r.notifyRegistrant();
        }
    }
    void unregisterForPsRestrictedDisabled(Handler h) {
        psRestrictDisabledRegistrants.remove(h);
    }
    public void handleMessage (Message msg) {
        AsyncResult ar;
        int[] ints;
        String[] strings;
        Message message;
        switch (msg.what) {
            case EVENT_RADIO_AVAILABLE:
                break;
            case EVENT_SIM_READY:
                if (mNeedToRegForSimLoaded) {
                    phone.mSIMRecords.registerForRecordsLoaded(this,
                            EVENT_SIM_RECORDS_LOADED, null);
                    mNeedToRegForSimLoaded = false;
                }
                phone.restoreSavedNetworkSelection(null);
                pollState();
                queueNextSignalStrengthPoll();
                break;
            case EVENT_RADIO_STATE_CHANGED:
                setPowerStateToDesired();
                pollState();
                break;
            case EVENT_NETWORK_STATE_CHANGED:
                pollState();
                break;
            case EVENT_GET_SIGNAL_STRENGTH:
                if (!(cm.getRadioState().isOn()) || (cm.getRadioState().isCdma())) {
                    return;
                }
                ar = (AsyncResult) msg.obj;
                onSignalStrengthResult(ar);
                queueNextSignalStrengthPoll();
                break;
            case EVENT_GET_LOC_DONE:
                ar = (AsyncResult) msg.obj;
                if (ar.exception == null) {
                    String states[] = (String[])ar.result;
                    int lac = -1;
                    int cid = -1;
                    if (states.length >= 3) {
                        try {
                            if (states[1] != null && states[1].length() > 0) {
                                lac = Integer.parseInt(states[1], 16);
                            }
                            if (states[2] != null && states[2].length() > 0) {
                                cid = Integer.parseInt(states[2], 16);
                            }
                        } catch (NumberFormatException ex) {
                            Log.w(LOG_TAG, "error parsing location: " + ex);
                        }
                    }
                    cellLoc.setLacAndCid(lac, cid);
                    phone.notifyLocationChanged();
                }
                disableSingleLocationUpdate();
                break;
            case EVENT_POLL_STATE_REGISTRATION:
            case EVENT_POLL_STATE_GPRS:
            case EVENT_POLL_STATE_OPERATOR:
            case EVENT_POLL_STATE_NETWORK_SELECTION_MODE:
                ar = (AsyncResult) msg.obj;
                handlePollStateResult(msg.what, ar);
                break;
            case EVENT_POLL_SIGNAL_STRENGTH:
                cm.getSignalStrength(obtainMessage(EVENT_GET_SIGNAL_STRENGTH));
                break;
            case EVENT_NITZ_TIME:
                ar = (AsyncResult) msg.obj;
                String nitzString = (String)((Object[])ar.result)[0];
                long nitzReceiveTime = ((Long)((Object[])ar.result)[1]).longValue();
                setTimeFromNITZString(nitzString, nitzReceiveTime);
                break;
            case EVENT_SIGNAL_STRENGTH_UPDATE:
                ar = (AsyncResult) msg.obj;
                dontPollSignalStrength = true;
                onSignalStrengthResult(ar);
                break;
            case EVENT_SIM_RECORDS_LOADED:
                updateSpnDisplay();
                break;
            case EVENT_LOCATION_UPDATES_ENABLED:
                ar = (AsyncResult) msg.obj;
                if (ar.exception == null) {
                    cm.getRegistrationState(obtainMessage(EVENT_GET_LOC_DONE, null));
                }
                break;
            case EVENT_SET_PREFERRED_NETWORK_TYPE:
                ar = (AsyncResult) msg.obj;
                message = obtainMessage(EVENT_RESET_PREFERRED_NETWORK_TYPE, ar.userObj);
                cm.setPreferredNetworkType(mPreferredNetworkType, message);
                break;
            case EVENT_RESET_PREFERRED_NETWORK_TYPE:
                ar = (AsyncResult) msg.obj;
                if (ar.userObj != null) {
                    AsyncResult.forMessage(((Message) ar.userObj)).exception
                            = ar.exception;
                    ((Message) ar.userObj).sendToTarget();
                }
                break;
            case EVENT_GET_PREFERRED_NETWORK_TYPE:
                ar = (AsyncResult) msg.obj;
                if (ar.exception == null) {
                    mPreferredNetworkType = ((int[])ar.result)[0];
                } else {
                    mPreferredNetworkType = RILConstants.NETWORK_MODE_GLOBAL;
                }
                message = obtainMessage(EVENT_SET_PREFERRED_NETWORK_TYPE, ar.userObj);
                int toggledNetworkType = RILConstants.NETWORK_MODE_GLOBAL;
                cm.setPreferredNetworkType(toggledNetworkType, message);
                break;
            case EVENT_CHECK_REPORT_GPRS:
                if (ss != null && !isGprsConsistant(gprsState, ss.getState())) {
                    GsmCellLocation loc = ((GsmCellLocation)phone.getCellLocation());
                    EventLog.writeEvent(EventLogTags.DATA_NETWORK_REGISTRATION_FAIL,
                            ss.getOperatorNumeric(), loc != null ? loc.getCid() : -1);
                    mReportedGprsNoReg = true;
                }
                mStartedGprsRegCheck = false;
                break;
            case EVENT_RESTRICTED_STATE_CHANGED:
                Log.d(LOG_TAG, "[DSAC DEB] " + "EVENT_RESTRICTED_STATE_CHANGED");
                ar = (AsyncResult) msg.obj;
                onRestrictedStateChanged(ar);
                break;
            default:
                Log.e(LOG_TAG, "Unhandled message with number: " + msg.what);
            break;
        }
    }
    protected void setPowerStateToDesired() {
        if (mDesiredPowerState
            && cm.getRadioState() == CommandsInterface.RadioState.RADIO_OFF) {
            cm.setRadioPower(true, null);
        } else if (!mDesiredPowerState && cm.getRadioState().isOn()) {
            DataConnectionTracker dcTracker = phone.mDataConnection;
            if (! dcTracker.isDataConnectionAsDesired()) {
                EventLog.writeEvent(EventLogTags.DATA_NETWORK_STATUS_ON_RADIO_OFF,
                        dcTracker.getStateInString(), dcTracker.getAnyDataEnabled() ? 1 : 0);
            }
            powerOffRadioSafely();
        } 
    }
    @Override
    protected void powerOffRadioSafely() {
        DataConnectionTracker dcTracker = phone.mDataConnection;
        Message msg = dcTracker.obtainMessage(DataConnectionTracker.EVENT_CLEAN_UP_CONNECTION);
        msg.arg1 = 1; 
        msg.obj = GSMPhone.REASON_RADIO_TURNED_OFF;
        dcTracker.sendMessage(msg);
        for (int i = 0; i < MAX_NUM_DATA_STATE_READS; i++) {
            if (dcTracker.getState() != DataConnectionTracker.State.CONNECTED
                    && dcTracker.getState() != DataConnectionTracker.State.DISCONNECTING) {
                Log.d(LOG_TAG, "Data shutdown complete.");
                break;
            }
            SystemClock.sleep(DATA_STATE_POLL_SLEEP_MS);
        }
        if (phone.isInCall()) {
            phone.mCT.ringingCall.hangupIfAlive();
            phone.mCT.backgroundCall.hangupIfAlive();
            phone.mCT.foregroundCall.hangupIfAlive();
        }
        cm.setRadioPower(false, null);
    }
    protected void updateSpnDisplay() {
        int rule = phone.mSIMRecords.getDisplayRule(ss.getOperatorNumeric());
        String spn = phone.mSIMRecords.getServiceProviderName();
        String plmn = ss.getOperatorAlphaLong();
        if (mEmergencyOnly && cm.getRadioState().isOn()) {
            plmn = Resources.getSystem().
                getText(com.android.internal.R.string.emergency_calls_only).toString();
        }
        if (rule != curSpnRule
                || !TextUtils.equals(spn, curSpn)
                || !TextUtils.equals(plmn, curPlmn)) {
            boolean showSpn = !mEmergencyOnly
                && (rule & SIMRecords.SPN_RULE_SHOW_SPN) == SIMRecords.SPN_RULE_SHOW_SPN;
            boolean showPlmn =
                (rule & SIMRecords.SPN_RULE_SHOW_PLMN) == SIMRecords.SPN_RULE_SHOW_PLMN;
            Intent intent = new Intent(Intents.SPN_STRINGS_UPDATED_ACTION);
            intent.addFlags(Intent.FLAG_RECEIVER_REPLACE_PENDING);
            intent.putExtra(Intents.EXTRA_SHOW_SPN, showSpn);
            intent.putExtra(Intents.EXTRA_SPN, spn);
            intent.putExtra(Intents.EXTRA_SHOW_PLMN, showPlmn);
            intent.putExtra(Intents.EXTRA_PLMN, plmn);
            phone.getContext().sendStickyBroadcast(intent);
        }
        curSpnRule = rule;
        curSpn = spn;
        curPlmn = plmn;
    }
    protected void handlePollStateResult (int what, AsyncResult ar) {
        int ints[];
        String states[];
        if (ar.userObj != pollingContext) return;
        if (ar.exception != null) {
            CommandException.Error err=null;
            if (ar.exception instanceof CommandException) {
                err = ((CommandException)(ar.exception)).getCommandError();
            }
            if (err == CommandException.Error.RADIO_NOT_AVAILABLE) {
                cancelPollState();
                return;
            }
            if (!cm.getRadioState().isOn()) {
                cancelPollState();
                return;
            }
            if (err != CommandException.Error.OP_NOT_ALLOWED_BEFORE_REG_NW &&
                    err != CommandException.Error.OP_NOT_ALLOWED_BEFORE_REG_NW) {
                Log.e(LOG_TAG,
                        "RIL implementation has returned an error where it must succeed" +
                        ar.exception);
            }
        } else try {
            switch (what) {
                case EVENT_POLL_STATE_REGISTRATION:
                    states = (String[])ar.result;
                    int lac = -1;
                    int cid = -1;
                    int regState = -1;
                    if (states.length > 0) {
                        try {
                            regState = Integer.parseInt(states[0]);
                            if (states.length >= 3) {
                                if (states[1] != null && states[1].length() > 0) {
                                    lac = Integer.parseInt(states[1], 16);
                                }
                                if (states[2] != null && states[2].length() > 0) {
                                    cid = Integer.parseInt(states[2], 16);
                                }
                            }
                        } catch (NumberFormatException ex) {
                            Log.w(LOG_TAG, "error parsing RegistrationState: " + ex);
                        }
                    }
                    mGsmRoaming = regCodeIsRoaming(regState);
                    newSS.setState (regCodeToServiceState(regState));
                    if (regState == 10 || regState == 12 || regState == 13 || regState == 14) {
                        mEmergencyOnly = true;
                    } else {
                        mEmergencyOnly = false;
                    }
                    newCellLoc.setLacAndCid(lac, cid);
                break;
                case EVENT_POLL_STATE_GPRS:
                    states = (String[])ar.result;
                    int type = 0;
                    regState = -1;
                    if (states.length > 0) {
                        try {
                            regState = Integer.parseInt(states[0]);
                            if (states.length >= 4 && states[3] != null) {
                                type = Integer.parseInt(states[3]);
                            }
                        } catch (NumberFormatException ex) {
                            Log.w(LOG_TAG, "error parsing GprsRegistrationState: " + ex);
                        }
                    }
                    newGPRSState = regCodeToServiceState(regState);
                    mDataRoaming = regCodeIsRoaming(regState);
                    newNetworkType = type;
                    newSS.setRadioTechnology(type);
                break;
                case EVENT_POLL_STATE_OPERATOR:
                    String opNames[] = (String[])ar.result;
                    if (opNames != null && opNames.length >= 3) {
                        newSS.setOperatorName (
                                opNames[0], opNames[1], opNames[2]);
                    }
                break;
                case EVENT_POLL_STATE_NETWORK_SELECTION_MODE:
                    ints = (int[])ar.result;
                    newSS.setIsManualSelection(ints[0] == 1);
                break;
            }
        } catch (RuntimeException ex) {
            Log.e(LOG_TAG, "Exception while polling service state. "
                            + "Probably malformed RIL response.", ex);
        }
        pollingContext[0]--;
        if (pollingContext[0] == 0) {
            boolean roaming = (mGsmRoaming || mDataRoaming);
            if (mGsmRoaming && !isRoamingBetweenOperators(mGsmRoaming, newSS)) {
                roaming = false;
            }
            newSS.setRoaming(roaming);
            newSS.setEmergencyOnly(mEmergencyOnly);
            pollStateDone();
        }
    }
    private void setSignalStrengthDefaultValues() {
        mSignalStrength = new SignalStrength(99, -1, -1, -1, -1, -1, -1, true);
    }
    private void pollState() {
        pollingContext = new int[1];
        pollingContext[0] = 0;
        switch (cm.getRadioState()) {
            case RADIO_UNAVAILABLE:
                newSS.setStateOutOfService();
                newCellLoc.setStateInvalid();
                setSignalStrengthDefaultValues();
                mGotCountryCode = false;
                pollStateDone();
            break;
            case RADIO_OFF:
                newSS.setStateOff();
                newCellLoc.setStateInvalid();
                setSignalStrengthDefaultValues();
                mGotCountryCode = false;
                pollStateDone();
            break;
            case RUIM_NOT_READY:
            case RUIM_READY:
            case RUIM_LOCKED_OR_ABSENT:
            case NV_NOT_READY:
            case NV_READY:
                Log.d(LOG_TAG, "Radio Technology Change ongoing, setting SS to off");
                newSS.setStateOff();
                newCellLoc.setStateInvalid();
                setSignalStrengthDefaultValues();
                mGotCountryCode = false;
                break;
            default:
                pollingContext[0]++;
                cm.getOperator(
                    obtainMessage(
                        EVENT_POLL_STATE_OPERATOR, pollingContext));
                pollingContext[0]++;
                cm.getGPRSRegistrationState(
                    obtainMessage(
                        EVENT_POLL_STATE_GPRS, pollingContext));
                pollingContext[0]++;
                cm.getRegistrationState(
                    obtainMessage(
                        EVENT_POLL_STATE_REGISTRATION, pollingContext));
                pollingContext[0]++;
                cm.getNetworkSelectionMode(
                    obtainMessage(
                        EVENT_POLL_STATE_NETWORK_SELECTION_MODE, pollingContext));
            break;
        }
    }
    private static String networkTypeToString(int type) {
        String ret = "unknown";
        switch (type) {
            case DATA_ACCESS_GPRS:
                ret = "GPRS";
                break;
            case DATA_ACCESS_EDGE:
                ret = "EDGE";
                break;
            case DATA_ACCESS_UMTS:
                ret = "UMTS";
                break;
            case DATA_ACCESS_HSDPA:
                ret = "HSDPA";
                break;
            case DATA_ACCESS_HSUPA:
                ret = "HSUPA";
                break;
            case DATA_ACCESS_HSPA:
                ret = "HSPA";
                break;
            default:
                Log.e(LOG_TAG, "Wrong network type: " + Integer.toString(type));
                break;
        }
        return ret;
    }
    private void pollStateDone() {
        if (DBG) {
            Log.d(LOG_TAG, "Poll ServiceState done: " +
                " oldSS=[" + ss + "] newSS=[" + newSS +
                "] oldGprs=" + gprsState + " newGprs=" + newGPRSState +
                " oldType=" + networkTypeToString(networkType) +
                " newType=" + networkTypeToString(newNetworkType));
        }
        boolean hasRegistered =
            ss.getState() != ServiceState.STATE_IN_SERVICE
            && newSS.getState() == ServiceState.STATE_IN_SERVICE;
        boolean hasDeregistered =
            ss.getState() == ServiceState.STATE_IN_SERVICE
            && newSS.getState() != ServiceState.STATE_IN_SERVICE;
        boolean hasGprsAttached =
                gprsState != ServiceState.STATE_IN_SERVICE
                && newGPRSState == ServiceState.STATE_IN_SERVICE;
        boolean hasGprsDetached =
                gprsState == ServiceState.STATE_IN_SERVICE
                && newGPRSState != ServiceState.STATE_IN_SERVICE;
        boolean hasNetworkTypeChanged = networkType != newNetworkType;
        boolean hasChanged = !newSS.equals(ss);
        boolean hasRoamingOn = !ss.getRoaming() && newSS.getRoaming();
        boolean hasRoamingOff = ss.getRoaming() && !newSS.getRoaming();
        boolean hasLocationChanged = !newCellLoc.equals(cellLoc);
        if (ss.getState() != newSS.getState() || gprsState != newGPRSState) {
            EventLog.writeEvent(EventLogTags.GSM_SERVICE_STATE_CHANGE,
                ss.getState(), gprsState, newSS.getState(), newGPRSState);
        }
        ServiceState tss;
        tss = ss;
        ss = newSS;
        newSS = tss;
        newSS.setStateOutOfService();
        GsmCellLocation tcl = cellLoc;
        cellLoc = newCellLoc;
        newCellLoc = tcl;
        if (hasNetworkTypeChanged) {
            int cid = -1;
            GsmCellLocation loc = ((GsmCellLocation)phone.getCellLocation());
            if (loc != null) cid = loc.getCid();
            EventLog.writeEvent(EventLogTags.GSM_RAT_SWITCHED, cid, networkType, newNetworkType);
            Log.d(LOG_TAG,
                    "RAT switched " + networkTypeToString(networkType) + " -> "
                    + networkTypeToString(newNetworkType) + " at cell " + cid);
        }
        gprsState = newGPRSState;
        networkType = newNetworkType;
        newSS.setStateOutOfService(); 
        if (hasNetworkTypeChanged) {
            phone.setSystemProperty(TelephonyProperties.PROPERTY_DATA_NETWORK_TYPE,
                    networkTypeToString(networkType));
        }
        if (hasRegistered) {
            networkAttachedRegistrants.notifyRegistrants();
        }
        if (hasChanged) {
            String operatorNumeric;
            updateSpnDisplay();
            phone.setSystemProperty(TelephonyProperties.PROPERTY_OPERATOR_ALPHA,
                ss.getOperatorAlphaLong());
            operatorNumeric = ss.getOperatorNumeric();
            phone.setSystemProperty(TelephonyProperties.PROPERTY_OPERATOR_NUMERIC, operatorNumeric);
            if (operatorNumeric == null) {
                phone.setSystemProperty(TelephonyProperties.PROPERTY_OPERATOR_ISO_COUNTRY, "");
            } else {
                String iso = "";
                try{
                    iso = MccTable.countryCodeForMcc(Integer.parseInt(
                            operatorNumeric.substring(0,3)));
                } catch ( NumberFormatException ex){
                    Log.w(LOG_TAG, "countryCodeForMcc error" + ex);
                } catch ( StringIndexOutOfBoundsException ex) {
                    Log.w(LOG_TAG, "countryCodeForMcc error" + ex);
                }
                phone.setSystemProperty(TelephonyProperties.PROPERTY_OPERATOR_ISO_COUNTRY, iso);
                mGotCountryCode = true;
                if (mNeedFixZone) {
                    TimeZone zone = null;
                    String zoneName = SystemProperties.get(TIMEZONE_PROPERTY);
                    if ((mZoneOffset == 0) && (mZoneDst == false) &&
                        (zoneName != null) && (zoneName.length() > 0) &&
                        (Arrays.binarySearch(GMT_COUNTRY_CODES, iso) < 0)) {
                        zone = TimeZone.getDefault();
                        long tzOffset;
                        tzOffset = zone.getOffset(System.currentTimeMillis());
                        if (getAutoTime()) {
                            setAndBroadcastNetworkSetTime(System.currentTimeMillis() - tzOffset);
                        } else {
                            mSavedTime = mSavedTime - tzOffset;
                        }
                    } else if (iso.equals("")){
                        zone = getNitzTimeZone(mZoneOffset, mZoneDst, mZoneTime);
                    } else {
                        zone = TimeUtils.getTimeZone(mZoneOffset,
                            mZoneDst, mZoneTime, iso);
                    }
                    mNeedFixZone = false;
                    if (zone != null) {
                        if (getAutoTime()) {
                            setAndBroadcastNetworkSetTimeZone(zone.getID());
                        }
                        saveNitzTimeZone(zone.getID());
                    }
                }
            }
            phone.setSystemProperty(TelephonyProperties.PROPERTY_OPERATOR_ISROAMING,
                ss.getRoaming() ? "true" : "false");
            phone.notifyServiceStateChanged(ss);
        }
        if (hasGprsAttached) {
            gprsAttachedRegistrants.notifyRegistrants();
        }
        if (hasGprsDetached) {
            gprsDetachedRegistrants.notifyRegistrants();
        }
        if (hasNetworkTypeChanged) {
            phone.notifyDataConnection(null);
        }
        if (hasRoamingOn) {
            roamingOnRegistrants.notifyRegistrants();
        }
        if (hasRoamingOff) {
            roamingOffRegistrants.notifyRegistrants();
        }
        if (hasLocationChanged) {
            phone.notifyLocationChanged();
        }
        if (! isGprsConsistant(gprsState, ss.getState())) {
            if (!mStartedGprsRegCheck && !mReportedGprsNoReg) {
                mStartedGprsRegCheck = true;
                int check_period = Settings.Secure.getInt(
                        phone.getContext().getContentResolver(),
                        Settings.Secure.GPRS_REGISTER_CHECK_PERIOD_MS,
                        DEFAULT_GPRS_CHECK_PERIOD_MILLIS);
                sendMessageDelayed(obtainMessage(EVENT_CHECK_REPORT_GPRS),
                        check_period);
            }
        } else {
            mReportedGprsNoReg = false;
        }
    }
    private boolean isGprsConsistant (int gprsState, int serviceState) {
        return !((serviceState == ServiceState.STATE_IN_SERVICE) &&
                (gprsState != ServiceState.STATE_IN_SERVICE));
    }
    private TimeZone getNitzTimeZone(int offset, boolean dst, long when) {
        TimeZone guess = findTimeZone(offset, dst, when);
        if (guess == null) {
            guess = findTimeZone(offset, !dst, when);
        }
        if (DBG) {
            Log.d(LOG_TAG, "getNitzTimeZone returning "
                    + (guess == null ? guess : guess.getID()));
        }
        return guess;
    }
    private TimeZone findTimeZone(int offset, boolean dst, long when) {
        int rawOffset = offset;
        if (dst) {
            rawOffset -= 3600000;
        }
        String[] zones = TimeZone.getAvailableIDs(rawOffset);
        TimeZone guess = null;
        Date d = new Date(when);
        for (String zone : zones) {
            TimeZone tz = TimeZone.getTimeZone(zone);
            if (tz.getOffset(when) == offset &&
                tz.inDaylightTime(d) == dst) {
                guess = tz;
                break;
            }
        }
        return guess;
    }
    private void queueNextSignalStrengthPoll() {
        if (dontPollSignalStrength || (cm.getRadioState().isCdma())) {
            return;
        }
        Message msg;
        msg = obtainMessage();
        msg.what = EVENT_POLL_SIGNAL_STRENGTH;
        long nextTime;
        sendMessageDelayed(msg, POLL_PERIOD_MILLIS);
    }
    private void onSignalStrengthResult(AsyncResult ar) {
        SignalStrength oldSignalStrength = mSignalStrength;
        int rssi = 99;
        if (ar.exception != null) {
            setSignalStrengthDefaultValues();
        } else {
            int[] ints = (int[])ar.result;
            if (ints.length != 0) {
                rssi = ints[0];
            } else {
                Log.e(LOG_TAG, "Bogus signal strength response");
                rssi = 99;
            }
        }
        mSignalStrength = new SignalStrength(rssi, -1, -1, -1,
                -1, -1, -1, true);
        if (!mSignalStrength.equals(oldSignalStrength)) {
            try { 
                phone.notifySignalStrength();
           } catch (NullPointerException ex) {
                log("onSignalStrengthResult() Phone already destroyed: " + ex
                        + "SignalStrength not notified");
           }
        }
    }
    private void onRestrictedStateChanged(AsyncResult ar) {
        Log.d(LOG_TAG, "[DSAC DEB] " + "onRestrictedStateChanged");
        RestrictedState newRs = new RestrictedState();
        Log.d(LOG_TAG, "[DSAC DEB] " + "current rs at enter "+ rs);
        if (ar.exception == null) {
            int[] ints = (int[])ar.result;
            int state = ints[0];
            newRs.setCsEmergencyRestricted(
                    ((state & RILConstants.RIL_RESTRICTED_STATE_CS_EMERGENCY) != 0) ||
                    ((state & RILConstants.RIL_RESTRICTED_STATE_CS_ALL) != 0) );
            if (phone.getIccCard().getState() == IccCard.State.READY) {
                newRs.setCsNormalRestricted(
                        ((state & RILConstants.RIL_RESTRICTED_STATE_CS_NORMAL) != 0) ||
                        ((state & RILConstants.RIL_RESTRICTED_STATE_CS_ALL) != 0) );
                newRs.setPsRestricted(
                        (state & RILConstants.RIL_RESTRICTED_STATE_PS_ALL)!= 0);
            }
            Log.d(LOG_TAG, "[DSAC DEB] " + "new rs "+ newRs);
            if (!rs.isPsRestricted() && newRs.isPsRestricted()) {
                psRestrictEnabledRegistrants.notifyRegistrants();
                setNotification(PS_ENABLED);
            } else if (rs.isPsRestricted() && !newRs.isPsRestricted()) {
                psRestrictDisabledRegistrants.notifyRegistrants();
                setNotification(PS_DISABLED);
            }
            if (rs.isCsRestricted()) {
                if (!newRs.isCsRestricted()) {
                    setNotification(CS_DISABLED);
                } else if (!newRs.isCsNormalRestricted()) {
                    setNotification(CS_EMERGENCY_ENABLED);
                } else if (!newRs.isCsEmergencyRestricted()) {
                    setNotification(CS_NORMAL_ENABLED);
                }
            } else if (rs.isCsEmergencyRestricted() && !rs.isCsNormalRestricted()) {
                if (!newRs.isCsRestricted()) {
                    setNotification(CS_DISABLED);
                } else if (newRs.isCsRestricted()) {
                    setNotification(CS_ENABLED);
                } else if (newRs.isCsNormalRestricted()) {
                    setNotification(CS_NORMAL_ENABLED);
                }
            } else if (!rs.isCsEmergencyRestricted() && rs.isCsNormalRestricted()) {
                if (!newRs.isCsRestricted()) {
                    setNotification(CS_DISABLED);
                } else if (newRs.isCsRestricted()) {
                    setNotification(CS_ENABLED);
                } else if (newRs.isCsEmergencyRestricted()) {
                    setNotification(CS_EMERGENCY_ENABLED);
                }
            } else {
                if (newRs.isCsRestricted()) {
                    setNotification(CS_ENABLED);
                } else if (newRs.isCsEmergencyRestricted()) {
                    setNotification(CS_EMERGENCY_ENABLED);
                } else if (newRs.isCsNormalRestricted()) {
                    setNotification(CS_NORMAL_ENABLED);
                }
            }
            rs = newRs;
        }
        Log.d(LOG_TAG, "[DSAC DEB] " + "current rs at return "+ rs);
    }
    private int regCodeToServiceState(int code) {
        switch (code) {
            case 0:
            case 2: 
            case 3: 
            case 4: 
            case 10:
            case 12:
            case 13:
            case 14:
                return ServiceState.STATE_OUT_OF_SERVICE;
            case 1:
                return ServiceState.STATE_IN_SERVICE;
            case 5:
                return ServiceState.STATE_IN_SERVICE;
            default:
                Log.w(LOG_TAG, "unexpected service state " + code);
                return ServiceState.STATE_OUT_OF_SERVICE;
        }
    }
    private boolean regCodeIsRoaming (int code) {
        return 5 == code;
    }
    private boolean isRoamingBetweenOperators(boolean gsmRoaming, ServiceState s) {
        String spn = SystemProperties.get(TelephonyProperties.PROPERTY_ICC_OPERATOR_ALPHA, "empty");
        String onsl = s.getOperatorAlphaLong();
        String onss = s.getOperatorAlphaShort();
        boolean equalsOnsl = onsl != null && spn.equals(onsl);
        boolean equalsOnss = onss != null && spn.equals(onss);
        String simNumeric = SystemProperties.get(
                TelephonyProperties.PROPERTY_ICC_OPERATOR_NUMERIC, "");
        String  operatorNumeric = s.getOperatorNumeric();
        boolean equalsMcc = true;
        try {
            equalsMcc = simNumeric.substring(0, 3).
                    equals(operatorNumeric.substring(0, 3));
        } catch (Exception e){
        }
        return gsmRoaming && !(equalsMcc && (equalsOnsl || equalsOnss));
    }
    private static int twoDigitsAt(String s, int offset) {
        int a, b;
        a = Character.digit(s.charAt(offset), 10);
        b = Character.digit(s.charAt(offset+1), 10);
        if (a < 0 || b < 0) {
            throw new RuntimeException("invalid format");
        }
        return a*10 + b;
    }
    int getCurrentGprsState() {
        return gprsState;
    }
    boolean isConcurrentVoiceAndData() {
        return (networkType >= DATA_ACCESS_UMTS);
    }
    private static String displayNameFor(int off) {
        off = off / 1000 / 60;
        char[] buf = new char[9];
        buf[0] = 'G';
        buf[1] = 'M';
        buf[2] = 'T';
        if (off < 0) {
            buf[3] = '-';
            off = -off;
        } else {
            buf[3] = '+';
        }
        int hours = off / 60;
        int minutes = off % 60;
        buf[4] = (char) ('0' + hours / 10);
        buf[5] = (char) ('0' + hours % 10);
        buf[6] = ':';
        buf[7] = (char) ('0' + minutes / 10);
        buf[8] = (char) ('0' + minutes % 10);
        return new String(buf);
    }
    private void setTimeFromNITZString (String nitz, long nitzReceiveTime) {
        long start = SystemClock.elapsedRealtime();
        Log.i(LOG_TAG, "NITZ: " + nitz + "," + nitzReceiveTime +
                        " start=" + start + " delay=" + (start - nitzReceiveTime));
        try {
            Calendar c = Calendar.getInstance(TimeZone.getTimeZone("GMT"));
            c.clear();
            c.set(Calendar.DST_OFFSET, 0);
            String[] nitzSubs = nitz.split("[/:,+-]");
            int year = 2000 + Integer.parseInt(nitzSubs[0]);
            c.set(Calendar.YEAR, year);
            int month = Integer.parseInt(nitzSubs[1]) - 1;
            c.set(Calendar.MONTH, month);
            int date = Integer.parseInt(nitzSubs[2]);
            c.set(Calendar.DATE, date);
            int hour = Integer.parseInt(nitzSubs[3]);
            c.set(Calendar.HOUR, hour);
            int minute = Integer.parseInt(nitzSubs[4]);
            c.set(Calendar.MINUTE, minute);
            int second = Integer.parseInt(nitzSubs[5]);
            c.set(Calendar.SECOND, second);
            boolean sign = (nitz.indexOf('-') == -1);
            int tzOffset = Integer.parseInt(nitzSubs[6]);
            int dst = (nitzSubs.length >= 8 ) ? Integer.parseInt(nitzSubs[7])
                                              : 0;
            tzOffset = (sign ? 1 : -1) * tzOffset * 15 * 60 * 1000;
            TimeZone    zone = null;
            if (nitzSubs.length >= 9) {
                String  tzname = nitzSubs[8].replace('!','/');
                zone = TimeZone.getTimeZone( tzname );
            }
            String iso = SystemProperties.get(TelephonyProperties.PROPERTY_OPERATOR_ISO_COUNTRY);
            if (zone == null) {
                if (mGotCountryCode) {
                    if (iso != null && iso.length() > 0) {
                        zone = TimeUtils.getTimeZone(tzOffset, dst != 0,
                                c.getTimeInMillis(),
                                iso);
                    } else {
                        zone = getNitzTimeZone(tzOffset, (dst != 0), c.getTimeInMillis());
                    }
                }
            }
            if (zone == null) {
                mNeedFixZone = true;
                mZoneOffset  = tzOffset;
                mZoneDst     = dst != 0;
                mZoneTime    = c.getTimeInMillis();
            }
            if (zone != null) {
                if (getAutoTime()) {
                    setAndBroadcastNetworkSetTimeZone(zone.getID());
                }
                saveNitzTimeZone(zone.getID());
            }
            String ignore = SystemProperties.get("gsm.ignore-nitz");
            if (ignore != null && ignore.equals("yes")) {
                Log.i(LOG_TAG, "NITZ: Not setting clock because gsm.ignore-nitz is set");
                return;
            }
            try {
                mWakeLock.acquire();
                if (getAutoTime()) {
                    long millisSinceNitzReceived
                            = SystemClock.elapsedRealtime() - nitzReceiveTime;
                    if (millisSinceNitzReceived < 0) {
                        Log.i(LOG_TAG, "NITZ: not setting time, clock has rolled "
                                            + "backwards since NITZ time was received, "
                                            + nitz);
                        return;
                    }
                    if (millisSinceNitzReceived > Integer.MAX_VALUE) {
                        Log.i(LOG_TAG, "NITZ: not setting time, processing has taken "
                                        + (millisSinceNitzReceived / (1000 * 60 * 60 * 24))
                                        + " days");
                        return;
                    }
                    c.add(Calendar.MILLISECOND, (int)millisSinceNitzReceived);
                    Log.i(LOG_TAG, "NITZ: Setting time of day to " + c.getTime()
                        + " NITZ receive delay(ms): " + millisSinceNitzReceived
                        + " gained(ms): "
                        + (c.getTimeInMillis() - System.currentTimeMillis())
                        + " from " + nitz);
                    setAndBroadcastNetworkSetTime(c.getTimeInMillis());
                    Log.i(LOG_TAG, "NITZ: after Setting time of day");
                }
                SystemProperties.set("gsm.nitz.time", String.valueOf(c.getTimeInMillis()));
                saveNitzTime(c.getTimeInMillis());
                if (Config.LOGV) {
                    long end = SystemClock.elapsedRealtime();
                    Log.v(LOG_TAG, "NITZ: end=" + end + " dur=" + (end - start));
                }
            } finally {
                mWakeLock.release();
            }
        } catch (RuntimeException ex) {
            Log.e(LOG_TAG, "NITZ: Parsing NITZ time " + nitz, ex);
        }
    }
    private boolean getAutoTime() {
        try {
            return Settings.System.getInt(phone.getContext().getContentResolver(),
                    Settings.System.AUTO_TIME) > 0;
        } catch (SettingNotFoundException snfe) {
            return true;
        }
    }
    private void saveNitzTimeZone(String zoneId) {
        mSavedTimeZone = zoneId;
    }
    private void saveNitzTime(long time) {
        mSavedTime = time;
        mSavedAtTime = SystemClock.elapsedRealtime();
    }
    private void setAndBroadcastNetworkSetTimeZone(String zoneId) {
        AlarmManager alarm =
            (AlarmManager) phone.getContext().getSystemService(Context.ALARM_SERVICE);
        alarm.setTimeZone(zoneId);
        Intent intent = new Intent(TelephonyIntents.ACTION_NETWORK_SET_TIMEZONE);
        intent.addFlags(Intent.FLAG_RECEIVER_REPLACE_PENDING);
        intent.putExtra("time-zone", zoneId);
        phone.getContext().sendStickyBroadcast(intent);
    }
    private void setAndBroadcastNetworkSetTime(long time) {
        SystemClock.setCurrentTimeMillis(time);
        Intent intent = new Intent(TelephonyIntents.ACTION_NETWORK_SET_TIME);
        intent.addFlags(Intent.FLAG_RECEIVER_REPLACE_PENDING);
        intent.putExtra("time", time);
        phone.getContext().sendStickyBroadcast(intent);
    }
    private void revertToNitz() {
        if (Settings.System.getInt(phone.getContext().getContentResolver(),
                Settings.System.AUTO_TIME, 0) == 0) {
            return;
        }
        Log.d(LOG_TAG, "Reverting to NITZ: tz='" + mSavedTimeZone
                + "' mSavedTime=" + mSavedTime
                + " mSavedAtTime=" + mSavedAtTime);
        if (mSavedTimeZone != null && mSavedTime != 0 && mSavedAtTime != 0) {
            setAndBroadcastNetworkSetTimeZone(mSavedTimeZone);
            setAndBroadcastNetworkSetTime(mSavedTime
                    + (SystemClock.elapsedRealtime() - mSavedAtTime));
        }
    }
    private void setNotification(int notifyType) {
        Log.d(LOG_TAG, "[DSAC DEB] " + "create notification " + notifyType);
        Context context = phone.getContext();
        mNotification = new Notification();
        mNotification.when = System.currentTimeMillis();
        mNotification.flags = Notification.FLAG_AUTO_CANCEL;
        mNotification.icon = com.android.internal.R.drawable.stat_sys_warning;
        Intent intent = new Intent();
        mNotification.contentIntent = PendingIntent
        .getActivity(context, 0, intent, PendingIntent.FLAG_CANCEL_CURRENT);
        CharSequence details = "";
        CharSequence title = context.getText(com.android.internal.R.string.RestrictedChangedTitle);
        int notificationId = CS_NOTIFICATION;
        switch (notifyType) {
        case PS_ENABLED:
            notificationId = PS_NOTIFICATION;
            details = context.getText(com.android.internal.R.string.RestrictedOnData);;
            break;
        case PS_DISABLED:
            notificationId = PS_NOTIFICATION;
            break;
        case CS_ENABLED:
            details = context.getText(com.android.internal.R.string.RestrictedOnAllVoice);;
            break;
        case CS_NORMAL_ENABLED:
            details = context.getText(com.android.internal.R.string.RestrictedOnNormal);;
            break;
        case CS_EMERGENCY_ENABLED:
            details = context.getText(com.android.internal.R.string.RestrictedOnEmergency);;
            break;
        case CS_DISABLED:
            break;
        }
        Log.d(LOG_TAG, "[DSAC DEB] " + "put notification " + title + " / " +details);
        mNotification.tickerText = title;
        mNotification.setLatestEventInfo(context, title, details,
                mNotification.contentIntent);
        NotificationManager notificationManager = (NotificationManager)
            context.getSystemService(Context.NOTIFICATION_SERVICE);
        if (notifyType == PS_DISABLED || notifyType == CS_DISABLED) {
            notificationManager.cancel(notificationId);
        } else {
            notificationManager.notify(notificationId, mNotification);
        }
    }
    private void log(String s) {
        Log.d(LOG_TAG, "[GsmServiceStateTracker] " + s);
    }
}
