final class CdmaServiceStateTracker extends ServiceStateTracker {
    static final String LOG_TAG = "CDMA";
    CDMAPhone phone;
    CdmaCellLocation cellLoc;
    CdmaCellLocation newCellLoc;
    private static final int NITZ_UPDATE_SPACING_DEFAULT = 1000 * 60 * 10;
    private int mNitzUpdateSpacing = SystemProperties.getInt("ro.nitz_update_spacing",
            NITZ_UPDATE_SPACING_DEFAULT);
    private static final int NITZ_UPDATE_DIFF_DEFAULT = 2000;
    private int mNitzUpdateDiff = SystemProperties.getInt("ro.nitz_update_diff",
            NITZ_UPDATE_DIFF_DEFAULT);
    private int networkType = 0;
    private int newNetworkType = 0;
    private boolean mCdmaRoaming = false;
    private int mRoamingIndicator;
    private boolean mIsInPrl;
    private int mDefaultRoamingIndicator;
    private int cdmaDataConnectionState = ServiceState.STATE_OUT_OF_SERVICE;
    private int newCdmaDataConnectionState = ServiceState.STATE_OUT_OF_SERVICE;
    private int mRegistrationState = -1;
    private RegistrantList cdmaDataConnectionAttachedRegistrants = new RegistrantList();
    private RegistrantList cdmaDataConnectionDetachedRegistrants = new RegistrantList();
    private RegistrantList cdmaForSubscriptionInfoReadyRegistrants = new RegistrantList();
    private boolean mNeedFixZone = false;
    private int mZoneOffset;
    private boolean mZoneDst;
    private long mZoneTime;
    private boolean mGotCountryCode = false;
    String mSavedTimeZone;
    long mSavedTime;
    long mSavedAtTime;
    private boolean mNeedToRegForRuimLoaded = false;
    private PowerManager.WakeLock mWakeLock;
    private static final String WAKELOCK_TAG = "ServiceStateTracker";
    private String curSpn = null;
    private int curSpnRule = 0;
    private String curPlmn = null;
    private String mMdn;
    private int mHomeSystemId[] = null;
    private int mHomeNetworkId[] = null;
    private String mMin;
    private String mPrlVersion;
    private boolean mIsMinInfoReady = false;
    private boolean isEriTextLoaded = false;
    private boolean isSubscriptionFromRuim = false;
    private boolean mPendingRadioPowerOffAfterDataOff = false;
    private String mRegistrationDeniedReason;
    private ContentResolver cr;
    private String currentCarrier = null;
    private ContentObserver mAutoTimeObserver = new ContentObserver(new Handler()) {
        @Override
        public void onChange(boolean selfChange) {
            Log.i("CdmaServiceStateTracker", "Auto time state called ...");
            revertToNitz();
        }
    };
    public CdmaServiceStateTracker(CDMAPhone phone) {
        super();
        this.phone = phone;
        cr = phone.getContext().getContentResolver();
        cm = phone.mCM;
        ss = new ServiceState();
        newSS = new ServiceState();
        cellLoc = new CdmaCellLocation();
        newCellLoc = new CdmaCellLocation();
        mSignalStrength = new SignalStrength();
        PowerManager powerManager =
                (PowerManager)phone.getContext().getSystemService(Context.POWER_SERVICE);
        mWakeLock = powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, WAKELOCK_TAG);
        cm.registerForAvailable(this, EVENT_RADIO_AVAILABLE, null);
        cm.registerForRadioStateChanged(this, EVENT_RADIO_STATE_CHANGED, null);
        cm.registerForNetworkStateChanged(this, EVENT_NETWORK_STATE_CHANGED_CDMA, null);
        cm.setOnNITZTime(this, EVENT_NITZ_TIME, null);
        cm.setOnSignalStrengthUpdate(this, EVENT_SIGNAL_STRENGTH_UPDATE, null);
        cm.registerForRUIMReady(this, EVENT_RUIM_READY, null);
        cm.registerForNVReady(this, EVENT_NV_READY, null);
        phone.registerForEriFileLoaded(this, EVENT_ERI_FILE_LOADED, null);
        cm.registerForCdmaOtaProvision(this,EVENT_OTA_PROVISION_STATUS_CHANGE, null);
        int airplaneMode = Settings.System.getInt(cr, Settings.System.AIRPLANE_MODE_ON, 0);
        mDesiredPowerState = ! (airplaneMode > 0);
        cr.registerContentObserver(
                Settings.System.getUriFor(Settings.System.AUTO_TIME), true,
                mAutoTimeObserver);
        setSignalStrengthDefaultValues();
        mNeedToRegForRuimLoaded = true;
    }
    public void dispose() {
        cm.unregisterForAvailable(this);
        cm.unregisterForRadioStateChanged(this);
        cm.unregisterForNetworkStateChanged(this);
        cm.unregisterForRUIMReady(this);
        cm.unregisterForNVReady(this);
        cm.unregisterForCdmaOtaProvision(this);
        phone.unregisterForEriFileLoaded(this);
        phone.mRuimRecords.unregisterForRecordsLoaded(this);
        cm.unSetOnSignalStrengthUpdate(this);
        cm.unSetOnNITZTime(this);
        cr.unregisterContentObserver(this.mAutoTimeObserver);
    }
    @Override
    protected void finalize() {
        if (DBG) log("CdmaServiceStateTracker finalized");
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
    void registerForCdmaDataConnectionAttached(Handler h, int what, Object obj) {
        Registrant r = new Registrant(h, what, obj);
        cdmaDataConnectionAttachedRegistrants.add(r);
        if (cdmaDataConnectionState == ServiceState.STATE_IN_SERVICE) {
            r.notifyRegistrant();
        }
    }
    void unregisterForCdmaDataConnectionAttached(Handler h) {
        cdmaDataConnectionAttachedRegistrants.remove(h);
    }
    void registerForCdmaDataConnectionDetached(Handler h, int what, Object obj) {
        Registrant r = new Registrant(h, what, obj);
        cdmaDataConnectionDetachedRegistrants.add(r);
        if (cdmaDataConnectionState != ServiceState.STATE_IN_SERVICE) {
            r.notifyRegistrant();
        }
    }
    void unregisterForCdmaDataConnectionDetached(Handler h) {
        cdmaDataConnectionDetachedRegistrants.remove(h);
    }
    public void registerForSubscriptionInfoReady(Handler h, int what, Object obj) {
        Registrant r = new Registrant(h, what, obj);
        cdmaForSubscriptionInfoReadyRegistrants.add(r);
        if (isMinInfoReady()) {
            r.notifyRegistrant();
        }
    }
    public void unregisterForSubscriptionInfoReady(Handler h) {
        cdmaForSubscriptionInfoReadyRegistrants.remove(h);
    }
    @Override
    public void handleMessage (Message msg) {
        AsyncResult ar;
        int[] ints;
        String[] strings;
        switch (msg.what) {
        case EVENT_RADIO_AVAILABLE:
            break;
        case EVENT_RUIM_READY:
            isSubscriptionFromRuim = true;
            if (mNeedToRegForRuimLoaded) {
                phone.mRuimRecords.registerForRecordsLoaded(this,
                        EVENT_RUIM_RECORDS_LOADED, null);
                mNeedToRegForRuimLoaded = false;
            }
            cm.getCDMASubscription(obtainMessage(EVENT_POLL_STATE_CDMA_SUBSCRIPTION));
            if (DBG) log("Receive EVENT_RUIM_READY and Send Request getCDMASubscription.");
            pollState();
            queueNextSignalStrengthPoll();
            break;
        case EVENT_NV_READY:
            isSubscriptionFromRuim = false;
            cm.getCDMASubscription( obtainMessage(EVENT_POLL_STATE_CDMA_SUBSCRIPTION));
            pollState();
            queueNextSignalStrengthPoll();
            break;
        case EVENT_RADIO_STATE_CHANGED:
            setPowerStateToDesired();
            pollState();
            break;
        case EVENT_NETWORK_STATE_CHANGED_CDMA:
            pollState();
            break;
        case EVENT_GET_SIGNAL_STRENGTH:
            if (!(cm.getRadioState().isOn()) || (cm.getRadioState().isGsm())) {
                return;
            }
            ar = (AsyncResult) msg.obj;
            onSignalStrengthResult(ar);
            queueNextSignalStrengthPoll();
            break;
        case EVENT_GET_LOC_DONE_CDMA:
            ar = (AsyncResult) msg.obj;
            if (ar.exception == null) {
                String states[] = (String[])ar.result;
                int baseStationId = -1;
                int baseStationLatitude = CdmaCellLocation.INVALID_LAT_LONG;
                int baseStationLongitude = CdmaCellLocation.INVALID_LAT_LONG;
                int systemId = -1;
                int networkId = -1;
                if (states.length > 9) {
                    try {
                        if (states[4] != null) {
                            baseStationId = Integer.parseInt(states[4]);
                        }
                        if (states[5] != null) {
                            baseStationLatitude = Integer.parseInt(states[5]);
                        }
                        if (states[6] != null) {
                            baseStationLongitude = Integer.parseInt(states[6]);
                        }
                        if (baseStationLatitude == 0 && baseStationLongitude == 0) {
                            baseStationLatitude  = CdmaCellLocation.INVALID_LAT_LONG;
                            baseStationLongitude = CdmaCellLocation.INVALID_LAT_LONG;
                        }
                        if (states[8] != null) {
                            systemId = Integer.parseInt(states[8]);
                        }
                        if (states[9] != null) {
                            networkId = Integer.parseInt(states[9]);
                        }
                    } catch (NumberFormatException ex) {
                        Log.w(LOG_TAG, "error parsing cell location data: " + ex);
                    }
                }
                cellLoc.setCellLocationData(baseStationId, baseStationLatitude,
                        baseStationLongitude, systemId, networkId);
                phone.notifyLocationChanged();
            }
            disableSingleLocationUpdate();
            break;
        case EVENT_POLL_STATE_REGISTRATION_CDMA:
        case EVENT_POLL_STATE_OPERATOR_CDMA:
            ar = (AsyncResult) msg.obj;
            handlePollStateResult(msg.what, ar);
            break;
        case EVENT_POLL_STATE_CDMA_SUBSCRIPTION: 
            ar = (AsyncResult) msg.obj;
            if (ar.exception == null) {
                String cdmaSubscription[] = (String[])ar.result;
                if (cdmaSubscription != null && cdmaSubscription.length >= 5) {
                    mMdn = cdmaSubscription[0];
                    if (cdmaSubscription[1] != null) {
                        String[] sid = cdmaSubscription[1].split(",");
                        mHomeSystemId = new int[sid.length];
                        for (int i = 0; i < sid.length; i++) {
                            try {
                                mHomeSystemId[i] = Integer.parseInt(sid[i]);
                            } catch (NumberFormatException ex) {
                                Log.e(LOG_TAG, "error parsing system id: ", ex);
                            }
                        }
                    }
                    Log.d(LOG_TAG,"GET_CDMA_SUBSCRIPTION SID=" + cdmaSubscription[1] );
                    if (cdmaSubscription[2] != null) {
                        String[] nid = cdmaSubscription[2].split(",");
                        mHomeNetworkId = new int[nid.length];
                        for (int i = 0; i < nid.length; i++) {
                            try {
                                mHomeNetworkId[i] = Integer.parseInt(nid[i]);
                            } catch (NumberFormatException ex) {
                                Log.e(LOG_TAG, "error parsing network id: ", ex);
                            }
                        }
                    }
                    Log.d(LOG_TAG,"GET_CDMA_SUBSCRIPTION NID=" + cdmaSubscription[2] );
                    mMin = cdmaSubscription[3];
                    mPrlVersion = cdmaSubscription[4];
                    Log.d(LOG_TAG,"GET_CDMA_SUBSCRIPTION MDN=" + mMdn);
                    if (cdmaForSubscriptionInfoReadyRegistrants != null) {
                        cdmaForSubscriptionInfoReadyRegistrants.notifyRegistrants();
                    }
                    if (!mIsMinInfoReady) {
                        mIsMinInfoReady = true;
                    }
                    phone.getIccCard().broadcastIccStateChangedIntent(IccCard.INTENT_VALUE_ICC_IMSI,
                            null);
                } else {
                    Log.w(LOG_TAG,"error parsing cdmaSubscription params num="
                            + cdmaSubscription.length);
                }
            }
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
        case EVENT_RUIM_RECORDS_LOADED:
            updateSpnDisplay();
            break;
        case EVENT_LOCATION_UPDATES_ENABLED:
            ar = (AsyncResult) msg.obj;
            if (ar.exception == null) {
                cm.getRegistrationState(obtainMessage(EVENT_GET_LOC_DONE_CDMA, null));
            }
            break;
        case EVENT_ERI_FILE_LOADED:
            if (DBG) log("[CdmaServiceStateTracker] ERI file has been loaded, repolling.");
            pollState();
            break;
        case EVENT_OTA_PROVISION_STATUS_CHANGE:
            ar = (AsyncResult)msg.obj;
            if (ar.exception == null) {
                ints = (int[]) ar.result;
                int otaStatus = ints[0];
                if (otaStatus == phone.CDMA_OTA_PROVISION_STATUS_COMMITTED
                    || otaStatus == phone.CDMA_OTA_PROVISION_STATUS_OTAPA_STOPPED) {
                    Log.d(LOG_TAG, "Received OTA_PROGRAMMING Complete,Reload MDN ");
                    cm.getCDMASubscription( obtainMessage(EVENT_POLL_STATE_CDMA_SUBSCRIPTION));
                }
            }
            break;
        case EVENT_SET_RADIO_POWER_OFF:
            synchronized(this) {
                if (mPendingRadioPowerOffAfterDataOff) {
                    if (DBG) log("EVENT_SET_RADIO_OFF, turn radio off now.");
                    hangupAndPowerOff();
                    mPendingRadioPowerOffAfterDataOff = false;
                }
            }
            break;
        default:
            Log.e(LOG_TAG, "Unhandled message with number: " + msg.what);
        break;
        }
    }
    @Override
    protected void setPowerStateToDesired() {
        if (mDesiredPowerState
            && cm.getRadioState() == CommandsInterface.RadioState.RADIO_OFF) {
            cm.setRadioPower(true, null);
        } else if (!mDesiredPowerState && cm.getRadioState().isOn()) {
            DataConnectionTracker dcTracker = phone.mDataConnection;
            if (! dcTracker.isDataConnectionAsDesired()) {
                EventLog.writeEvent(EventLogTags.DATA_NETWORK_STATUS_ON_RADIO_OFF,
                        dcTracker.getStateInString(),
                        dcTracker.getAnyDataEnabled() ? 1 : 0);
            }
            powerOffRadioSafely();
        } 
    }
    @Override
    protected void powerOffRadioSafely(){
        DataConnectionTracker dcTracker = phone.mDataConnection;
        Message msg = dcTracker.obtainMessage(DataConnectionTracker.EVENT_CLEAN_UP_CONNECTION);
        msg.arg1 = 1; 
        msg.obj = CDMAPhone.REASON_RADIO_TURNED_OFF;
        dcTracker.sendMessage(msg);
        synchronized(this) {
            if (!mPendingRadioPowerOffAfterDataOff) {
                DataConnectionTracker.State currentState = dcTracker.getState();
                if (currentState != DataConnectionTracker.State.CONNECTED
                        && currentState != DataConnectionTracker.State.DISCONNECTING
                        && currentState != DataConnectionTracker.State.INITING) {
                    if (DBG) log("Data disconnected, turn off radio right away.");
                    hangupAndPowerOff();
                }
                else if (sendEmptyMessageDelayed(EVENT_SET_RADIO_POWER_OFF, 30000)) {
                    if (DBG) {
                        log("Wait up to 30 sec for data to disconnect, then turn off radio.");
                    }
                    mPendingRadioPowerOffAfterDataOff = true;
                } else {
                    Log.w(LOG_TAG, "Cannot send delayed Msg, turn off radio right away.");
                    hangupAndPowerOff();
                }
            }
        }
    }
    @Override
    protected void updateSpnDisplay() {
        String spn = "";
        boolean showSpn = false;
        String plmn = "";
        boolean showPlmn = false;
        int rule = 0;
        if (cm.getRadioState().isRUIMReady()) {
            plmn = ss.getOperatorAlphaLong(); 
            showPlmn = true; 
        } else {
            plmn = ss.getOperatorAlphaLong(); 
            showPlmn = true;
        }
        if (rule != curSpnRule
                || !TextUtils.equals(spn, curSpn)
                || !TextUtils.equals(plmn, curPlmn)) {
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
    @Override
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
                        "RIL implementation has returned an error where it must succeed",
                        ar.exception);
            }
        } else try {
            switch (what) {
            case EVENT_POLL_STATE_REGISTRATION_CDMA: 
                states = (String[])ar.result;
                int registrationState = 4;     
                int radioTechnology = -1;      
                int baseStationId = -1;        
                int baseStationLatitude = CdmaCellLocation.INVALID_LAT_LONG;
                int baseStationLongitude = CdmaCellLocation.INVALID_LAT_LONG;
                int cssIndicator = 0;          
                int systemId = 0;              
                int networkId = 0;             
                int roamingIndicator = -1;     
                int systemIsInPrl = 0;         
                int defaultRoamingIndicator = 0;  
                int reasonForDenial = 0;       
                if (states.length == 14) {
                    try {
                        if (states[0] != null) {
                            registrationState = Integer.parseInt(states[0]);
                        }
                        if (states[3] != null) {
                            radioTechnology = Integer.parseInt(states[3]);
                        }
                        if (states[4] != null) {
                            baseStationId = Integer.parseInt(states[4]);
                        }
                        if (states[5] != null) {
                            baseStationLatitude = Integer.parseInt(states[5]);
                        }
                        if (states[6] != null) {
                            baseStationLongitude = Integer.parseInt(states[6]);
                        }
                        if (baseStationLatitude == 0 && baseStationLongitude == 0) {
                            baseStationLatitude  = CdmaCellLocation.INVALID_LAT_LONG;
                            baseStationLongitude = CdmaCellLocation.INVALID_LAT_LONG;
                        }
                        if (states[7] != null) {
                            cssIndicator = Integer.parseInt(states[7]);
                        }
                        if (states[8] != null) {
                            systemId = Integer.parseInt(states[8]);
                        }
                        if (states[9] != null) {
                            networkId = Integer.parseInt(states[9]);
                        }
                        if (states[10] != null) {
                            roamingIndicator = Integer.parseInt(states[10]);
                        }
                        if (states[11] != null) {
                            systemIsInPrl = Integer.parseInt(states[11]);
                        }
                        if (states[12] != null) {
                            defaultRoamingIndicator = Integer.parseInt(states[12]);
                        }
                        if (states[13] != null) {
                            reasonForDenial = Integer.parseInt(states[13]);
                        }
                    } catch (NumberFormatException ex) {
                        Log.w(LOG_TAG, "error parsing RegistrationState: " + ex);
                    }
                } else {
                    throw new RuntimeException("Warning! Wrong number of parameters returned from "
                                         + "RIL_REQUEST_REGISTRATION_STATE: expected 14 got "
                                         + states.length);
                }
                mRegistrationState = registrationState;
                mCdmaRoaming =
                        regCodeIsRoaming(registrationState) && !isRoamIndForHomeSystem(states[10]);
                newSS.setState (regCodeToServiceState(registrationState));
                this.newCdmaDataConnectionState =
                        radioTechnologyToDataServiceState(radioTechnology);
                newSS.setRadioTechnology(radioTechnology);
                newNetworkType = radioTechnology;
                newSS.setCssIndicator(cssIndicator);
                newSS.setSystemAndNetworkId(systemId, networkId);
                mRoamingIndicator = roamingIndicator;
                mIsInPrl = (systemIsInPrl == 0) ? false : true;
                mDefaultRoamingIndicator = defaultRoamingIndicator;
                newCellLoc.setCellLocationData(baseStationId, baseStationLatitude,
                        baseStationLongitude, systemId, networkId);
                if (reasonForDenial == 0) {
                    mRegistrationDeniedReason = ServiceStateTracker.REGISTRATION_DENIED_GEN;
                } else if (reasonForDenial == 1) {
                    mRegistrationDeniedReason = ServiceStateTracker.REGISTRATION_DENIED_AUTH;
                } else {
                    mRegistrationDeniedReason = "";
                }
                if (mRegistrationState == 3) {
                    if (DBG) log("Registration denied, " + mRegistrationDeniedReason);
                }
                break;
            case EVENT_POLL_STATE_OPERATOR_CDMA: 
                String opNames[] = (String[])ar.result;
                if (opNames != null && opNames.length >= 3) {
                    if (cm.getRadioState().isNVReady()) {
                        newSS.setOperatorName(null, opNames[1], opNames[2]);
                    } else {
                        newSS.setOperatorName(opNames[0], opNames[1], opNames[2]);
                    }
                } else {
                    Log.w(LOG_TAG, "error parsing opNames");
                }
                break;
            default:
                Log.e(LOG_TAG, "RIL response handle in wrong phone!"
                    + " Expected CDMA RIL request and get GSM RIL request.");
            break;
            }
        } catch (RuntimeException ex) {
            Log.e(LOG_TAG, "Exception while polling service state. "
                    + "Probably malformed RIL response.", ex);
        }
        pollingContext[0]--;
        if (pollingContext[0] == 0) {
            boolean namMatch = false;
            if (!isSidsAllZeros() && isHomeSid(newSS.getSystemId())) {
                namMatch = true;
            }
            if (isSubscriptionFromRuim) {
                newSS.setRoaming(isRoamingBetweenOperators(mCdmaRoaming, newSS));
            } else {
                newSS.setRoaming(mCdmaRoaming);
            }
            newSS.setCdmaDefaultRoamingIndicator(mDefaultRoamingIndicator);
            newSS.setCdmaRoamingIndicator(mRoamingIndicator);
            boolean isPrlLoaded = true;
            if (TextUtils.isEmpty(mPrlVersion)) {
                isPrlLoaded = false;
            }
            if (!isPrlLoaded) {
                newSS.setCdmaRoamingIndicator(EriInfo.ROAMING_INDICATOR_FLASH);
            } else if (!isSidsAllZeros()) {
                if (!namMatch && !mIsInPrl) {
                    newSS.setCdmaRoamingIndicator(mDefaultRoamingIndicator);
                } else if (namMatch && !mIsInPrl) {
                    newSS.setCdmaRoamingIndicator(EriInfo.ROAMING_INDICATOR_FLASH);
                } else if (!namMatch && mIsInPrl) {
                    newSS.setCdmaRoamingIndicator(mRoamingIndicator);
                } else {
                    if ((mRoamingIndicator <= 2)) {
                        newSS.setCdmaRoamingIndicator(EriInfo.ROAMING_INDICATOR_OFF);
                    } else {
                        newSS.setCdmaRoamingIndicator(mRoamingIndicator);
                    }
                }
            }
            int roamingIndicator = newSS.getCdmaRoamingIndicator();
            newSS.setCdmaEriIconIndex(phone.mEriManager.getCdmaEriIconIndex(roamingIndicator,
                    mDefaultRoamingIndicator));
            newSS.setCdmaEriIconMode(phone.mEriManager.getCdmaEriIconMode(roamingIndicator,
                    mDefaultRoamingIndicator));
            if (DBG) {
                log("Set CDMA Roaming Indicator to: " + newSS.getCdmaRoamingIndicator()
                    + ". mCdmaRoaming = " + mCdmaRoaming + ", isPrlLoaded = " + isPrlLoaded
                    + ". namMatch = " + namMatch + " , mIsInPrl = " + mIsInPrl
                    + ", mRoamingIndicator = " + mRoamingIndicator
                    + ", mDefaultRoamingIndicator= " + mDefaultRoamingIndicator);
            }
            pollStateDone();
        }
    }
    private void setSignalStrengthDefaultValues() {
        mSignalStrength = new SignalStrength(99, -1, -1, -1, -1, -1, -1, false);
    }
    private void
    pollState() {
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
        case SIM_NOT_READY:
        case SIM_LOCKED_OR_ABSENT:
        case SIM_READY:
            log("Radio Technology Change ongoing, setting SS to off");
            newSS.setStateOff();
            newCellLoc.setStateInvalid();
            setSignalStrengthDefaultValues();
            mGotCountryCode = false;
            break;
        default:
            pollingContext[0]++;
            cm.getOperator(
                    obtainMessage(EVENT_POLL_STATE_OPERATOR_CDMA, pollingContext));
            pollingContext[0]++;
            cm.getRegistrationState(
                    obtainMessage(EVENT_POLL_STATE_REGISTRATION_CDMA, pollingContext));
            break;
        }
    }
    private static String networkTypeToString(int type) {
        String ret = "unknown";
        switch (type) {
        case DATA_ACCESS_CDMA_IS95A:
        case DATA_ACCESS_CDMA_IS95B:
            ret = "CDMA";
            break;
        case DATA_ACCESS_CDMA_1xRTT:
            ret = "CDMA - 1xRTT";
            break;
        case DATA_ACCESS_CDMA_EvDo_0:
            ret = "CDMA - EvDo rev. 0";
            break;
        case DATA_ACCESS_CDMA_EvDo_A:
            ret = "CDMA - EvDo rev. A";
            break;
        default:
            if (DBG) {
                Log.e(LOG_TAG, "Wrong network. Can not return a string.");
            }
        break;
        }
        return ret;
    }
    private void fixTimeZone(String isoCountryCode) {
        TimeZone zone = null;
        String zoneName = SystemProperties.get(TIMEZONE_PROPERTY);
        if ((mZoneOffset == 0) && (mZoneDst == false) && (zoneName != null)
                && (zoneName.length() > 0)
                && (Arrays.binarySearch(GMT_COUNTRY_CODES, isoCountryCode) < 0)) {
            zone = TimeZone.getDefault();
            long tzOffset;
            tzOffset = zone.getOffset(System.currentTimeMillis());
            if (getAutoTime()) {
                setAndBroadcastNetworkSetTime(System.currentTimeMillis() - tzOffset);
            } else {
                mSavedTime = mSavedTime - tzOffset;
            }
        } else if (isoCountryCode.equals("")) {
            zone = getNitzTimeZone(mZoneOffset, mZoneDst, mZoneTime);
        } else {
            zone = TimeUtils.getTimeZone(mZoneOffset, mZoneDst, mZoneTime, isoCountryCode);
        }
        mNeedFixZone = false;
        if (zone != null) {
            if (getAutoTime()) {
                setAndBroadcastNetworkSetTimeZone(zone.getID());
            }
            saveNitzTimeZone(zone.getID());
        }
    }
    private void pollStateDone() {
        if (DBG) log("Poll ServiceState done: oldSS=[" + ss + "] newSS=[" + newSS + "]");
        boolean hasRegistered =
            ss.getState() != ServiceState.STATE_IN_SERVICE
            && newSS.getState() == ServiceState.STATE_IN_SERVICE;
        boolean hasDeregistered =
            ss.getState() == ServiceState.STATE_IN_SERVICE
            && newSS.getState() != ServiceState.STATE_IN_SERVICE;
        boolean hasCdmaDataConnectionAttached =
            this.cdmaDataConnectionState != ServiceState.STATE_IN_SERVICE
            && this.newCdmaDataConnectionState == ServiceState.STATE_IN_SERVICE;
        boolean hasCdmaDataConnectionDetached =
            this.cdmaDataConnectionState == ServiceState.STATE_IN_SERVICE
            && this.newCdmaDataConnectionState != ServiceState.STATE_IN_SERVICE;
        boolean hasCdmaDataConnectionChanged =
                       cdmaDataConnectionState != newCdmaDataConnectionState;
        boolean hasNetworkTypeChanged = networkType != newNetworkType;
        boolean hasChanged = !newSS.equals(ss);
        boolean hasRoamingOn = !ss.getRoaming() && newSS.getRoaming();
        boolean hasRoamingOff = ss.getRoaming() && !newSS.getRoaming();
        boolean hasLocationChanged = !newCellLoc.equals(cellLoc);
        if (ss.getState() != newSS.getState() ||
                cdmaDataConnectionState != newCdmaDataConnectionState) {
            EventLog.writeEvent(EventLogTags.CDMA_SERVICE_STATE_CHANGE,
                    ss.getState(), cdmaDataConnectionState,
                    newSS.getState(), newCdmaDataConnectionState);
        }
        ServiceState tss;
        tss = ss;
        ss = newSS;
        newSS = tss;
        newSS.setStateOutOfService();
        CdmaCellLocation tcl = cellLoc;
        cellLoc = newCellLoc;
        newCellLoc = tcl;
        cdmaDataConnectionState = newCdmaDataConnectionState;
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
            if (cm.getRadioState().isNVReady()) {
                String eriText;
                if (ss.getState() == ServiceState.STATE_IN_SERVICE) {
                    eriText = phone.getCdmaEriText();
                } else {
                    eriText = phone.getContext().getText(
                            com.android.internal.R.string.roamingTextSearching).toString();
                }
                ss.setCdmaEriText(eriText);
            }
            String operatorNumeric;
            phone.setSystemProperty(TelephonyProperties.PROPERTY_OPERATOR_ALPHA,
                    ss.getOperatorAlphaLong());
            operatorNumeric = ss.getOperatorNumeric();
            phone.setSystemProperty(TelephonyProperties.PROPERTY_OPERATOR_NUMERIC, operatorNumeric);
            if (operatorNumeric == null) {
                phone.setSystemProperty(TelephonyProperties.PROPERTY_OPERATOR_ISO_COUNTRY, "");
            } else {
                String isoCountryCode = "";
                try{
                    isoCountryCode = MccTable.countryCodeForMcc(Integer.parseInt(
                            operatorNumeric.substring(0,3)));
                } catch ( NumberFormatException ex){
                    Log.w(LOG_TAG, "countryCodeForMcc error" + ex);
                } catch ( StringIndexOutOfBoundsException ex) {
                    Log.w(LOG_TAG, "countryCodeForMcc error" + ex);
                }
                phone.setSystemProperty(TelephonyProperties.PROPERTY_OPERATOR_ISO_COUNTRY,
                        isoCountryCode);
                mGotCountryCode = true;
                if (mNeedFixZone) {
                    fixTimeZone(isoCountryCode);
                }
            }
            phone.setSystemProperty(TelephonyProperties.PROPERTY_OPERATOR_ISROAMING,
                    ss.getRoaming() ? "true" : "false");
            updateSpnDisplay();
            phone.notifyServiceStateChanged(ss);
        }
        if (hasCdmaDataConnectionAttached) {
            cdmaDataConnectionAttachedRegistrants.notifyRegistrants();
        }
        if (hasCdmaDataConnectionDetached) {
            cdmaDataConnectionDetachedRegistrants.notifyRegistrants();
        }
        if (hasCdmaDataConnectionChanged || hasNetworkTypeChanged) {
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
    }
    private TimeZone getNitzTimeZone(int offset, boolean dst, long when) {
        TimeZone guess = findTimeZone(offset, dst, when);
        if (guess == null) {
            guess = findTimeZone(offset, !dst, when);
        }
        if (DBG) log("getNitzTimeZone returning " + (guess == null ? guess : guess.getID()));
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
    private void
    queueNextSignalStrengthPoll() {
        if (dontPollSignalStrength || (cm.getRadioState().isGsm())) {
            return;
        }
        Message msg;
        msg = obtainMessage();
        msg.what = EVENT_POLL_SIGNAL_STRENGTH;
        sendMessageDelayed(msg, POLL_PERIOD_MILLIS);
    }
    private void
    onSignalStrengthResult(AsyncResult ar) {
        SignalStrength oldSignalStrength = mSignalStrength;
        if (ar.exception != null) {
            setSignalStrengthDefaultValues();
        } else {
            int[] ints = (int[])ar.result;
            int offset = 2;
            int cdmaDbm = (ints[offset] > 0) ? -ints[offset] : -120;
            int cdmaEcio = (ints[offset+1] > 0) ? -ints[offset+1] : -160;
            int evdoRssi = (ints[offset+2] > 0) ? -ints[offset+2] : -120;
            int evdoEcio = (ints[offset+3] > 0) ? -ints[offset+3] : -1;
            int evdoSnr  = ((ints[offset+4] > 0) && (ints[offset+4] <= 8)) ? ints[offset+4] : -1;
            mSignalStrength = new SignalStrength(99, -1, cdmaDbm, cdmaEcio,
                    evdoRssi, evdoEcio, evdoSnr, false);
        }
        try {
            phone.notifySignalStrength();
        } catch (NullPointerException ex) {
            log("onSignalStrengthResult() Phone already destroyed: " + ex
                    + "SignalStrength not notified");
        }
    }
    private int radioTechnologyToDataServiceState(int code) {
        int retVal = ServiceState.STATE_OUT_OF_SERVICE;
        switch(code) {
        case 0:
        case 1:
        case 2:
        case 3:
        case 4:
        case 5:
            break;
        case 6: 
        case 7: 
        case 8: 
            retVal = ServiceState.STATE_IN_SERVICE;
            break;
        default:
            Log.e(LOG_TAG, "Wrong radioTechnology code.");
        break;
        }
        return(retVal);
    }
    private int
    regCodeToServiceState(int code) {
        switch (code) {
        case 0: 
            return ServiceState.STATE_OUT_OF_SERVICE;
        case 1:
            return ServiceState.STATE_IN_SERVICE;
        case 2: 
        case 3: 
        case 4: 
            return ServiceState.STATE_OUT_OF_SERVICE;
        case 5:
            return ServiceState.STATE_IN_SERVICE;
        default:
            Log.w(LOG_TAG, "unexpected service state " + code);
        return ServiceState.STATE_OUT_OF_SERVICE;
        }
    }
     int getCurrentCdmaDataConnectionState() {
        return cdmaDataConnectionState;
    }
    private boolean
    regCodeIsRoaming (int code) {
        return 5 == code;
    }
    private boolean isRoamIndForHomeSystem(String roamInd) {
        String homeRoamIndcators = SystemProperties.get("ro.cdma.homesystem");
        if (!TextUtils.isEmpty(homeRoamIndcators)) {
            for (String homeRoamInd : homeRoamIndcators.split(",")) {
                if (homeRoamInd.equals(roamInd)) {
                    return true;
                }
            }
            return false;
        }
        return false;
    }
    private
    boolean isRoamingBetweenOperators(boolean cdmaRoaming, ServiceState s) {
        String spn = SystemProperties.get(TelephonyProperties.PROPERTY_ICC_OPERATOR_ALPHA, "empty");
        String onsl = s.getOperatorAlphaLong();
        String onss = s.getOperatorAlphaShort();
        boolean equalsOnsl = onsl != null && spn.equals(onsl);
        boolean equalsOnss = onss != null && spn.equals(onss);
        return cdmaRoaming && !(equalsOnsl || equalsOnss);
    }
    private
    void setTimeFromNITZString (String nitz, long nitzReceiveTime)
    {
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
                if (getAutoTime()) {
                    long gained = c.getTimeInMillis() - System.currentTimeMillis();
                    long timeSinceLastUpdate = SystemClock.elapsedRealtime() - mSavedAtTime;
                    int nitzUpdateSpacing = Settings.Secure.getInt(cr,
                            Settings.Secure.NITZ_UPDATE_SPACING, mNitzUpdateSpacing);
                    int nitzUpdateDiff = Settings.Secure.getInt(cr,
                            Settings.Secure.NITZ_UPDATE_DIFF, mNitzUpdateDiff);
                    if ((mSavedAtTime == 0) || (timeSinceLastUpdate > nitzUpdateSpacing)
                            || (Math.abs(gained) > nitzUpdateDiff)) {
                        Log.i(LOG_TAG, "NITZ: Auto updating time of day to " + c.getTime()
                                + " NITZ receive delay=" + millisSinceNitzReceived
                                + "ms gained=" + gained + "ms from " + nitz);
                        setAndBroadcastNetworkSetTime(c.getTimeInMillis());
                    } else {
                        Log.i(LOG_TAG, "NITZ: ignore, a previous update was "
                                + timeSinceLastUpdate + "ms ago and gained=" + gained + "ms");
                        return;
                    }
                }
                Log.i(LOG_TAG, "NITZ: update nitz time property");
                SystemProperties.set("gsm.nitz.time", String.valueOf(c.getTimeInMillis()));
                mSavedTime = c.getTimeInMillis();
                mSavedAtTime = SystemClock.elapsedRealtime();
            } finally {
                long end = SystemClock.elapsedRealtime();
                Log.i(LOG_TAG, "NITZ: end=" + end + " dur=" + (end - start));
                mWakeLock.release();
            }
        } catch (RuntimeException ex) {
            Log.e(LOG_TAG, "NITZ: Parsing NITZ time " + nitz, ex);
        }
    }
    private boolean getAutoTime() {
        try {
            return Settings.System.getInt(cr, Settings.System.AUTO_TIME) > 0;
        } catch (SettingNotFoundException snfe) {
            return true;
        }
    }
    private void saveNitzTimeZone(String zoneId) {
        mSavedTimeZone = zoneId;
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
        if (Settings.System.getInt(cr, Settings.System.AUTO_TIME, 0) == 0) {
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
    private boolean isSidsAllZeros() {
        if (mHomeSystemId != null) {
            for (int i=0; i < mHomeSystemId.length; i++) {
                if (mHomeSystemId[i] != 0) {
                    return false;
                }
            }
        }
        return true;
    }
    private boolean isHomeSid(int sid) {
        if (mHomeSystemId != null) {
            for (int i=0; i < mHomeSystemId.length; i++) {
                if (sid == mHomeSystemId[i]) {
                    return true;
                }
            }
        }
        return false;
    }
    boolean isConcurrentVoiceAndData() {
        return false;
    }
    protected void log(String s) {
        Log.d(LOG_TAG, "[CdmaServiceStateTracker] " + s);
    }
    public String getMdnNumber() {
        return mMdn;
    }
    public String getCdmaMin() {
         return mMin;
    }
    public String getPrlVersion() {
        return mPrlVersion;
    }
    String getImsi() {
        String operatorNumeric = SystemProperties.get(
                TelephonyProperties.PROPERTY_ICC_OPERATOR_NUMERIC, "");
        if (!TextUtils.isEmpty(operatorNumeric) && getCdmaMin() != null) {
            return (operatorNumeric + getCdmaMin());
        } else {
            return null;
        }
    }
    public boolean isMinInfoReady() {
        return mIsMinInfoReady;
    }
    public boolean processPendingRadioPowerOffAfterDataOff() {
        synchronized(this) {
            if (mPendingRadioPowerOffAfterDataOff) {
                if (DBG) log("Process pending request to turn radio off.");
                removeMessages(EVENT_SET_RADIO_POWER_OFF);
                hangupAndPowerOff();
                mPendingRadioPowerOffAfterDataOff = false;
                return true;
            }
            return false;
        }
    }
    private void hangupAndPowerOff() {
        phone.mCT.ringingCall.hangupIfAlive();
        phone.mCT.backgroundCall.hangupIfAlive();
        phone.mCT.foregroundCall.hangupIfAlive();
        cm.setRadioPower(false, null);
    }
}
