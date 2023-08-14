public class CDMAPhone extends PhoneBase {
    static final String LOG_TAG = "CDMA";
    private static final boolean DBG = true;
    private static final String UNACTIVATED_MIN2_VALUE = "000000";
    private static final String UNACTIVATED_MIN_VALUE = "1111110111";
    private static final int DEFAULT_ECM_EXIT_TIMER_VALUE = 300000;
    static final String VM_COUNT_CDMA = "vm_count_key_cdma";
    private static final String VM_NUMBER_CDMA = "vm_number_key_cdma";
    private String mVmNumber = null;
    static final int RESTART_ECM_TIMER = 0; 
    static final int CANCEL_ECM_TIMER = 1; 
    CdmaCallTracker mCT;
    CdmaSMSDispatcher mSMS;
    CdmaServiceStateTracker mSST;
    RuimFileHandler mRuimFileHandler;
    RuimRecords mRuimRecords;
    RuimCard mRuimCard;
    RuimPhoneBookInterfaceManager mRuimPhoneBookInterfaceManager;
    RuimSmsInterfaceManager mRuimSmsInterfaceManager;
    PhoneSubInfo mSubInfo;
    EriManager mEriManager;
    WakeLock mWakeLock;
    private RegistrantList mNvLoadedRegistrants = new RegistrantList();
    private RegistrantList mEriFileLoadedRegistrants = new RegistrantList();
    private RegistrantList mEcmTimerResetRegistrants = new RegistrantList();
    private boolean mIsPhoneInEcmState;
    private Registrant mEcmExitRespRegistrant;
    private String mEsn;
    private String mMeid;
    private String mCarrierOtaSpNumSchema;
    private Runnable mExitEcmRunnable = new Runnable() {
        public void run() {
            exitEmergencyCallbackMode();
        }
    };
    Registrant mPostDialHandler;
    public CDMAPhone(Context context, CommandsInterface ci, PhoneNotifier notifier) {
        this(context,ci,notifier, false);
    }
    public CDMAPhone(Context context, CommandsInterface ci, PhoneNotifier notifier,
            boolean unitTestMode) {
        super(notifier, context, ci, unitTestMode);
        mCM.setPhoneType(Phone.PHONE_TYPE_CDMA);
        mCT = new CdmaCallTracker(this);
        mSST = new CdmaServiceStateTracker (this);
        mSMS = new CdmaSMSDispatcher(this);
        mIccFileHandler = new RuimFileHandler(this);
        mRuimRecords = new RuimRecords(this);
        mDataConnection = new CdmaDataConnectionTracker (this);
        mRuimCard = new RuimCard(this);
        mRuimPhoneBookInterfaceManager = new RuimPhoneBookInterfaceManager(this);
        mRuimSmsInterfaceManager = new RuimSmsInterfaceManager(this);
        mSubInfo = new PhoneSubInfo(this);
        mEriManager = new EriManager(this, context, EriManager.ERI_FROM_XML);
        mCM.registerForAvailable(this, EVENT_RADIO_AVAILABLE, null);
        mRuimRecords.registerForRecordsLoaded(this, EVENT_RUIM_RECORDS_LOADED, null);
        mCM.registerForOffOrNotAvailable(this, EVENT_RADIO_OFF_OR_NOT_AVAILABLE, null);
        mCM.registerForOn(this, EVENT_RADIO_ON, null);
        mCM.setOnSuppServiceNotification(this, EVENT_SSN, null);
        mSST.registerForNetworkAttach(this, EVENT_REGISTERED_TO_NETWORK, null);
        mCM.registerForNVReady(this, EVENT_NV_READY, null);
        mCM.setEmergencyCallbackMode(this, EVENT_EMERGENCY_CALLBACK_MODE_ENTER, null);
        PowerManager pm
            = (PowerManager) context.getSystemService(Context.POWER_SERVICE);
        mWakeLock = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK,LOG_TAG);
        SystemProperties.set(TelephonyProperties.CURRENT_ACTIVE_PHONE,
                new Integer(Phone.PHONE_TYPE_CDMA).toString());
        String inEcm=SystemProperties.get(TelephonyProperties.PROPERTY_INECM_MODE, "false");
        mIsPhoneInEcmState = inEcm.equals("true");
        if (mIsPhoneInEcmState) {
            mCM.exitEmergencyCallbackMode(obtainMessage(EVENT_EXIT_EMERGENCY_CALLBACK_RESPONSE));
        }
        mCarrierOtaSpNumSchema = SystemProperties.get(
                TelephonyProperties.PROPERTY_OTASP_NUM_SCHEMA,"");
        String operatorAlpha = SystemProperties.get("ro.cdma.home.operator.alpha");
        setSystemProperty(PROPERTY_ICC_OPERATOR_ALPHA, operatorAlpha);
        String operatorNumeric = SystemProperties.get("ro.cdma.home.operator.numeric");
        setSystemProperty(PROPERTY_ICC_OPERATOR_NUMERIC, operatorNumeric);
        setIsoCountryProperty(operatorNumeric);
        updateCurrentCarrierInProvider(operatorNumeric);
        notifier.notifyMessageWaitingChanged(this);
    }
    public void dispose() {
        synchronized(PhoneProxy.lockForRadioTechnologyChange) {
            super.dispose();
            mRuimRecords.unregisterForRecordsLoaded(this); 
            mCM.unregisterForAvailable(this); 
            mCM.unregisterForOffOrNotAvailable(this); 
            mCM.unregisterForOn(this); 
            mCM.unregisterForNVReady(this); 
            mSST.unregisterForNetworkAttach(this); 
            mCM.unSetOnSuppServiceNotification(this);
            mCT.dispose();
            mDataConnection.dispose();
            mSST.dispose();
            mSMS.dispose();
            mIccFileHandler.dispose(); 
            mRuimRecords.dispose();
            mRuimCard.dispose();
            mRuimPhoneBookInterfaceManager.dispose();
            mRuimSmsInterfaceManager.dispose();
            mSubInfo.dispose();
            mEriManager.dispose();
        }
    }
    public void removeReferences() {
            this.mRuimPhoneBookInterfaceManager = null;
            this.mRuimSmsInterfaceManager = null;
            this.mSMS = null;
            this.mSubInfo = null;
            this.mRuimRecords = null;
            this.mIccFileHandler = null;
            this.mRuimCard = null;
            this.mDataConnection = null;
            this.mCT = null;
            this.mSST = null;
            this.mEriManager = null;
    }
    protected void finalize() {
        if(DBG) Log.d(LOG_TAG, "CDMAPhone finalized");
        if (mWakeLock.isHeld()) {
            Log.e(LOG_TAG, "UNEXPECTED; mWakeLock is held when finalizing.");
            mWakeLock.release();
        }
    }
    public ServiceState getServiceState() {
        return mSST.ss;
    }
    public Phone.State getState() {
        return mCT.state;
    }
    public String getPhoneName() {
        return "CDMA";
    }
    public int getPhoneType() {
        return Phone.PHONE_TYPE_CDMA;
    }
    public boolean canTransfer() {
        Log.e(LOG_TAG, "canTransfer: not possible in CDMA");
        return false;
    }
    public CdmaCall getRingingCall() {
        return mCT.ringingCall;
    }
    public void setMute(boolean muted) {
        mCT.setMute(muted);
    }
    public boolean getMute() {
        return mCT.getMute();
    }
    public void conference() throws CallStateException {
        Log.e(LOG_TAG, "conference: not possible in CDMA");
    }
    public void enableEnhancedVoicePrivacy(boolean enable, Message onComplete) {
        this.mCM.setPreferredVoicePrivacy(enable, onComplete);
    }
    public void getEnhancedVoicePrivacy(Message onComplete) {
        this.mCM.getPreferredVoicePrivacy(onComplete);
    }
    public void clearDisconnected() {
        mCT.clearDisconnected();
    }
    public DataActivityState getDataActivityState() {
        DataActivityState ret = DataActivityState.NONE;
        if (mSST.getCurrentCdmaDataConnectionState() == ServiceState.STATE_IN_SERVICE) {
            switch (mDataConnection.getActivity()) {
                case DATAIN:
                    ret = DataActivityState.DATAIN;
                break;
                case DATAOUT:
                    ret = DataActivityState.DATAOUT;
                break;
                case DATAINANDOUT:
                    ret = DataActivityState.DATAINANDOUT;
                break;
                case DORMANT:
                    ret = DataActivityState.DORMANT;
                break;
            }
        }
        return ret;
    }
     void
    notifySignalStrength() {
        mNotifier.notifySignalStrength(this);
    }
    public Connection
    dial (String dialString) throws CallStateException {
        String newDialString = PhoneNumberUtils.stripSeparators(dialString);
        return mCT.dial(newDialString);
    }
    public SignalStrength getSignalStrength() {
        return mSST.mSignalStrength;
    }
    public boolean
    getMessageWaitingIndicator() {
        return (getVoiceMessageCount() > 0);
    }
    public List<? extends MmiCode>
    getPendingMmiCodes() {
        Log.e(LOG_TAG, "method getPendingMmiCodes is NOT supported in CDMA!");
        return null;
    }
    public void registerForSuppServiceNotification(
            Handler h, int what, Object obj) {
        Log.e(LOG_TAG, "method registerForSuppServiceNotification is NOT supported in CDMA!");
    }
    public CdmaCall getBackgroundCall() {
        return mCT.backgroundCall;
    }
    public boolean handleInCallMmiCommands(String dialString) {
        Log.e(LOG_TAG, "method handleInCallMmiCommands is NOT supported in CDMA!");
        return false;
    }
    public void
    setNetworkSelectionModeAutomatic(Message response) {
        Log.e(LOG_TAG, "method setNetworkSelectionModeAutomatic is NOT supported in CDMA!");
    }
    public void unregisterForSuppServiceNotification(Handler h) {
        Log.e(LOG_TAG, "method unregisterForSuppServiceNotification is NOT supported in CDMA!");
    }
    public void
    acceptCall() throws CallStateException {
        mCT.acceptCall();
    }
    public void
    rejectCall() throws CallStateException {
        mCT.rejectCall();
    }
    public void
    switchHoldingAndActive() throws CallStateException {
        mCT.switchWaitingOrHoldingAndActive();
    }
    public String getLine1Number() {
        return mSST.getMdnNumber();
    }
    public String getCdmaPrlVersion(){
        return mSST.getPrlVersion();
    }
    public String getCdmaMin() {
        return mSST.getCdmaMin();
    }
    public boolean isMinInfoReady() {
        return mSST.isMinInfoReady();
    }
    public void getCallWaiting(Message onComplete) {
        mCM.queryCallWaiting(CommandsInterface.SERVICE_CLASS_VOICE, onComplete);
    }
    public void
    setRadioPower(boolean power) {
        mSST.setRadioPower(power);
    }
    public String getEsn() {
        return mEsn;
    }
    public String getMeid() {
        return mMeid;
    }
    public String getDeviceId() {
        String id = getMeid();
        if ((id == null) || id.matches("^0*$")) {
            Log.d(LOG_TAG, "getDeviceId(): MEID is not initialized use ESN");
            id = getEsn();
        }
        return id;
    }
    public String getDeviceSvn() {
        Log.d(LOG_TAG, "getDeviceSvn(): return 0");
        return "0";
    }
    public String getSubscriberId() {
        return mSST.getImsi();
    }
    public boolean canConference() {
        Log.e(LOG_TAG, "canConference: not possible in CDMA");
        return false;
    }
    public CellLocation getCellLocation() {
        return mSST.cellLoc;
    }
    public boolean disableDataConnectivity() {
        return mDataConnection.setDataEnabled(false);
    }
    public CdmaCall getForegroundCall() {
        return mCT.foregroundCall;
    }
    public void
    selectNetworkManually(com.android.internal.telephony.gsm.NetworkInfo network,
            Message response) {
        Log.e(LOG_TAG, "selectNetworkManually: not possible in CDMA");
    }
    public void setOnPostDialCharacter(Handler h, int what, Object obj) {
        mPostDialHandler = new Registrant(h, what, obj);
    }
    public boolean handlePinMmi(String dialString) {
        Log.e(LOG_TAG, "method handlePinMmi is NOT supported in CDMA!");
        return false;
    }
    public boolean isDataConnectivityPossible() {
        boolean noData = mDataConnection.getDataEnabled() &&
                getDataConnectionState() == DataState.DISCONNECTED;
        return !noData && getIccCard().getState() == IccCard.State.READY &&
                getServiceState().getState() == ServiceState.STATE_IN_SERVICE &&
                (mDataConnection.getDataOnRoamingEnabled() || !getServiceState().getRoaming());
    }
    public void setLine1Number(String alphaTag, String number, Message onComplete) {
        Log.e(LOG_TAG, "setLine1Number: not possible in CDMA");
    }
    public IccCard getIccCard() {
        return mRuimCard;
    }
    public String getIccSerialNumber() {
        return mRuimRecords.iccid;
    }
    public void setCallWaiting(boolean enable, Message onComplete) {
        Log.e(LOG_TAG, "method setCallWaiting is NOT supported in CDMA!");
    }
    public void updateServiceLocation() {
        mSST.enableSingleLocationUpdate();
    }
    public void setDataRoamingEnabled(boolean enable) {
        mDataConnection.setDataOnRoamingEnabled(enable);
    }
    public void registerForCdmaOtaStatusChange(Handler h, int what, Object obj) {
        mCM.registerForCdmaOtaProvision(h, what, obj);
    }
    public void unregisterForCdmaOtaStatusChange(Handler h) {
        mCM.unregisterForCdmaOtaProvision(h);
    }
    public void registerForSubscriptionInfoReady(Handler h, int what, Object obj) {
        mSST.registerForSubscriptionInfoReady(h, what, obj);
    }
    public void unregisterForSubscriptionInfoReady(Handler h) {
        mSST.unregisterForSubscriptionInfoReady(h);
    }
    public void setOnEcbModeExitResponse(Handler h, int what, Object obj) {
        mEcmExitRespRegistrant = new Registrant (h, what, obj);
    }
    public void unsetOnEcbModeExitResponse(Handler h) {
        mEcmExitRespRegistrant.clear();
    }
    public void registerForCallWaiting(Handler h, int what, Object obj) {
        mCT.registerForCallWaiting(h, what, obj);
    }
    public void unregisterForCallWaiting(Handler h) {
        mCT.unregisterForCallWaiting(h);
    }
    public void
    getNeighboringCids(Message response) {
        if (response != null) {
            CommandException ce = new CommandException(
                    CommandException.Error.REQUEST_NOT_SUPPORTED);
            AsyncResult.forMessage(response).exception = ce;
            response.sendToTarget();
        }
    }
    public DataState getDataConnectionState() {
        DataState ret = DataState.DISCONNECTED;
        if (mSST == null) {
             ret = DataState.DISCONNECTED;
        } else if (mSST.getCurrentCdmaDataConnectionState() != ServiceState.STATE_IN_SERVICE) {
            ret = DataState.DISCONNECTED;
        } else {
            switch (mDataConnection.getState()) {
                case FAILED:
                case IDLE:
                    ret = DataState.DISCONNECTED;
                break;
                case CONNECTED:
                case DISCONNECTING:
                    if ( mCT.state != Phone.State.IDLE
                            && !mSST.isConcurrentVoiceAndData()) {
                        ret = DataState.SUSPENDED;
                    } else {
                        ret = DataState.CONNECTED;
                    }
                break;
                case INITING:
                case CONNECTING:
                case SCANNING:
                    ret = DataState.CONNECTING;
                break;
            }
        }
        return ret;
    }
    public void sendUssdResponse(String ussdMessge) {
        Log.e(LOG_TAG, "sendUssdResponse: not possible in CDMA");
    }
    public void sendDtmf(char c) {
        if (!PhoneNumberUtils.is12Key(c)) {
            Log.e(LOG_TAG,
                    "sendDtmf called with invalid character '" + c + "'");
        } else {
            if (mCT.state ==  Phone.State.OFFHOOK) {
                mCM.sendDtmf(c, null);
            }
        }
    }
    public void startDtmf(char c) {
        if (!PhoneNumberUtils.is12Key(c)) {
            Log.e(LOG_TAG,
                    "startDtmf called with invalid character '" + c + "'");
        } else {
            mCM.startDtmf(c, null);
        }
    }
    public void stopDtmf() {
        mCM.stopDtmf(null);
    }
    public void sendBurstDtmf(String dtmfString, int on, int off, Message onComplete) {
        boolean check = true;
        for (int itr = 0;itr < dtmfString.length(); itr++) {
            if (!PhoneNumberUtils.is12Key(dtmfString.charAt(itr))) {
                Log.e(LOG_TAG,
                        "sendDtmf called with invalid character '" + dtmfString.charAt(itr)+ "'");
                check = false;
                break;
            }
        }
        if ((mCT.state ==  Phone.State.OFFHOOK)&&(check)) {
            mCM.sendBurstDtmf(dtmfString, on, off, onComplete);
        }
     }
    public void getAvailableNetworks(Message response) {
        Log.e(LOG_TAG, "getAvailableNetworks: not possible in CDMA");
    }
    public void setOutgoingCallerIdDisplay(int commandInterfaceCLIRMode, Message onComplete) {
        Log.e(LOG_TAG, "setOutgoingCallerIdDisplay: not possible in CDMA");
    }
    public void enableLocationUpdates() {
        mSST.enableLocationUpdates();
    }
    public void disableLocationUpdates() {
        mSST.disableLocationUpdates();
    }
    public void getDataCallList(Message response) {
        mCM.getDataCallList(response);
    }
    public boolean getDataRoamingEnabled() {
        return mDataConnection.getDataOnRoamingEnabled();
    }
    public List<DataConnection> getCurrentDataConnectionList () {
        return mDataConnection.getAllDataConnections();
    }
    public void setVoiceMailNumber(String alphaTag,
                                   String voiceMailNumber,
                                   Message onComplete) {
        Message resp;
        mVmNumber = voiceMailNumber;
        resp = obtainMessage(EVENT_SET_VM_NUMBER_DONE, 0, 0, onComplete);
        mRuimRecords.setVoiceMailNumber(alphaTag, mVmNumber, resp);
    }
    public String getVoiceMailNumber() {
        String number = null;
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(getContext());
        number = sp.getString(VM_NUMBER_CDMA, "*86");
        return number;
    }
    public int getVoiceMessageCount() {
        int voicemailCount =  mRuimRecords.getVoiceMessageCount();
        if (voicemailCount == 0) {
            SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(getContext());
            voicemailCount = sp.getInt(VM_COUNT_CDMA, 0);
        }
        return voicemailCount;
    }
    public String getVoiceMailAlphaTag() {
        String ret = "";
        if (ret == null || ret.length() == 0) {
            return mContext.getText(
                com.android.internal.R.string.defaultVoiceMailAlphaTag).toString();
        }
        return ret;
    }
    public boolean enableDataConnectivity() {
        if (mIsPhoneInEcmState) {
            Intent intent = new Intent(TelephonyIntents.ACTION_SHOW_NOTICE_ECM_BLOCK_OTHERS);
            ActivityManagerNative.broadcastStickyIntent(intent, null);
            return false;
        } else if ((mCT.state == Phone.State.OFFHOOK) && mCT.isInEmergencyCall()) {
            return false;
        } else {
            return mDataConnection.setDataEnabled(true);
        }
    }
    public boolean getIccRecordsLoaded() {
        return mRuimRecords.getRecordsLoaded();
    }
    public void getCallForwardingOption(int commandInterfaceCFReason, Message onComplete) {
        Log.e(LOG_TAG, "getCallForwardingOption: not possible in CDMA");
    }
    public void setCallForwardingOption(int commandInterfaceCFAction,
            int commandInterfaceCFReason,
            String dialingNumber,
            int timerSeconds,
            Message onComplete) {
        Log.e(LOG_TAG, "setCallForwardingOption: not possible in CDMA");
    }
    public void
    getOutgoingCallerIdDisplay(Message onComplete) {
        Log.e(LOG_TAG, "getOutgoingCallerIdDisplay: not possible in CDMA");
    }
    public boolean
    getCallForwardingIndicator() {
        Log.e(LOG_TAG, "getCallForwardingIndicator: not possible in CDMA");
        return false;
    }
    public void explicitCallTransfer() {
        Log.e(LOG_TAG, "explicitCallTransfer: not possible in CDMA");
    }
    public String getLine1AlphaTag() {
        Log.e(LOG_TAG, "getLine1AlphaTag: not possible in CDMA");
        return null;
    }
     void notifyPhoneStateChanged() {
        mNotifier.notifyPhoneState(this);
    }
     void notifyPreciseCallStateChanged() {
        super.notifyPreciseCallStateChangedP();
    }
     void notifyServiceStateChanged(ServiceState ss) {
         super.notifyServiceStateChangedP(ss);
     }
     void notifyLocationChanged() {
         mNotifier.notifyCellLocation(this);
     }
     void notifyNewRingingConnection(Connection c) {
        super.notifyNewRingingConnectionP(c);
    }
     void notifyDisconnect(Connection cn) {
        mDisconnectRegistrants.notifyResult(cn);
    }
    void notifyUnknownConnection() {
        mUnknownConnectionRegistrants.notifyResult(this);
    }
    void sendEmergencyCallbackModeChange(){
        Intent intent = new Intent(TelephonyIntents.ACTION_EMERGENCY_CALLBACK_MODE_CHANGED);
        intent.putExtra(PHONE_IN_ECM_STATE, mIsPhoneInEcmState);
        ActivityManagerNative.broadcastStickyIntent(intent,null);
        if (DBG) Log.d(LOG_TAG, "sendEmergencyCallbackModeChange");
    }
     void
    updateMessageWaitingIndicator(boolean mwi) {
        mRuimRecords.setVoiceMessageWaiting(1, mwi ? -1 : 0);
    }
     void
    updateMessageWaitingIndicator(int mwi) {
        mRuimRecords.setVoiceMessageWaiting(1, mwi);
    }
     boolean
    needsOtaServiceProvisioning() {
        String cdmaMin = getCdmaMin();
        boolean needsProvisioning;
        if (cdmaMin == null || (cdmaMin.length() < 6)) {
            if (DBG) Log.d(LOG_TAG, "needsOtaServiceProvisioning: illegal cdmaMin='"
                                    + cdmaMin + "' assume provisioning needed.");
            needsProvisioning = true;
        } else {
            needsProvisioning = (cdmaMin.equals(UNACTIVATED_MIN_VALUE)
                    || cdmaMin.substring(0,6).equals(UNACTIVATED_MIN2_VALUE))
                    || SystemProperties.getBoolean("test_cdma_setup", false);
        }
        if (DBG) Log.d(LOG_TAG, "needsOtaServiceProvisioning: ret=" + needsProvisioning);
        return needsProvisioning;
    }
    @Override
    public void exitEmergencyCallbackMode() {
        if (mWakeLock.isHeld()) {
            mWakeLock.release();
        }
        mCM.exitEmergencyCallbackMode(obtainMessage(EVENT_EXIT_EMERGENCY_CALLBACK_RESPONSE));
    }
    private void handleEnterEmergencyCallbackMode(Message msg) {
        if (DBG) {
            Log.d(LOG_TAG, "handleEnterEmergencyCallbackMode,mIsPhoneInEcmState= "
                    + mIsPhoneInEcmState);
        }
        if (mIsPhoneInEcmState == false) {
            mIsPhoneInEcmState = true;
            sendEmergencyCallbackModeChange();
            setSystemProperty(TelephonyProperties.PROPERTY_INECM_MODE, "true");
            long delayInMillis = SystemProperties.getLong(
                    TelephonyProperties.PROPERTY_ECM_EXIT_TIMER, DEFAULT_ECM_EXIT_TIMER_VALUE);
            postDelayed(mExitEcmRunnable, delayInMillis);
            mWakeLock.acquire();
        }
    }
    private void handleExitEmergencyCallbackMode(Message msg) {
        AsyncResult ar = (AsyncResult)msg.obj;
        if (DBG) {
            Log.d(LOG_TAG, "handleExitEmergencyCallbackMode,ar.exception , mIsPhoneInEcmState "
                    + ar.exception + mIsPhoneInEcmState);
        }
        removeCallbacks(mExitEcmRunnable);
        if (mEcmExitRespRegistrant != null) {
            mEcmExitRespRegistrant.notifyRegistrant(ar);
        }
        if (ar.exception == null) {
            if (mIsPhoneInEcmState) {
                mIsPhoneInEcmState = false;
                setSystemProperty(TelephonyProperties.PROPERTY_INECM_MODE, "false");
            }
            sendEmergencyCallbackModeChange();
            mDataConnection.setDataEnabled(true);
        }
    }
    void handleTimerInEmergencyCallbackMode(int action) {
        switch(action) {
        case CANCEL_ECM_TIMER:
            removeCallbacks(mExitEcmRunnable);
            mEcmTimerResetRegistrants.notifyResult(new Boolean(true));
            break;
        case RESTART_ECM_TIMER:
            long delayInMillis = SystemProperties.getLong(
                    TelephonyProperties.PROPERTY_ECM_EXIT_TIMER, DEFAULT_ECM_EXIT_TIMER_VALUE);
            postDelayed(mExitEcmRunnable, delayInMillis);
            mEcmTimerResetRegistrants.notifyResult(new Boolean(false));
            break;
        default:
            Log.e(LOG_TAG, "handleTimerInEmergencyCallbackMode, unsupported action " + action);
        }
    }
    public void registerForEcmTimerReset(Handler h, int what, Object obj) {
        mEcmTimerResetRegistrants.addUnique(h, what, obj);
    }
    public void unregisterForEcmTimerReset(Handler h) {
        mEcmTimerResetRegistrants.remove(h);
    }
    @Override
    public void handleMessage(Message msg) {
        AsyncResult ar;
        Message     onComplete;
        switch(msg.what) {
            case EVENT_RADIO_AVAILABLE: {
                mCM.getBasebandVersion(obtainMessage(EVENT_GET_BASEBAND_VERSION_DONE));
                mCM.getDeviceIdentity(obtainMessage(EVENT_GET_DEVICE_IDENTITY_DONE));
            }
            break;
            case EVENT_GET_BASEBAND_VERSION_DONE:{
                ar = (AsyncResult)msg.obj;
                if (ar.exception != null) {
                    break;
                }
                if (DBG) Log.d(LOG_TAG, "Baseband version: " + ar.result);
                setSystemProperty(TelephonyProperties.PROPERTY_BASEBAND_VERSION, (String)ar.result);
            }
            break;
            case EVENT_GET_DEVICE_IDENTITY_DONE:{
                ar = (AsyncResult)msg.obj;
                if (ar.exception != null) {
                    break;
                }
                String[] respId = (String[])ar.result;
                mEsn  =  respId[2];
                mMeid =  respId[3];
            }
            break;
            case EVENT_EMERGENCY_CALLBACK_MODE_ENTER:{
                handleEnterEmergencyCallbackMode(msg);
            }
            break;
            case  EVENT_EXIT_EMERGENCY_CALLBACK_RESPONSE:{
                handleExitEmergencyCallbackMode(msg);
            }
            break;
            case EVENT_RUIM_RECORDS_LOADED:{
                Log.d(LOG_TAG, "Event EVENT_RUIM_RECORDS_LOADED Received");
            }
            break;
            case EVENT_RADIO_OFF_OR_NOT_AVAILABLE:{
                Log.d(LOG_TAG, "Event EVENT_RADIO_OFF_OR_NOT_AVAILABLE Received");
            }
            break;
            case EVENT_RADIO_ON:{
                Log.d(LOG_TAG, "Event EVENT_RADIO_ON Received");
            }
            break;
            case EVENT_SSN:{
                Log.d(LOG_TAG, "Event EVENT_SSN Received");
            }
            break;
            case EVENT_REGISTERED_TO_NETWORK:{
                Log.d(LOG_TAG, "Event EVENT_REGISTERED_TO_NETWORK Received");
            }
            break;
            case EVENT_NV_READY:{
                Log.d(LOG_TAG, "Event EVENT_NV_READY Received");
                mEriManager.loadEriFile();
                mNvLoadedRegistrants.notifyRegistrants();
                if(mEriManager.isEriFileLoaded()) {
                    Log.d(LOG_TAG, "ERI read, notify registrants");
                    mEriFileLoadedRegistrants.notifyRegistrants();
                }
            }
            break;
            case EVENT_SET_VM_NUMBER_DONE:{
                ar = (AsyncResult)msg.obj;
                if (IccException.class.isInstance(ar.exception)) {
                    storeVoiceMailNumber(mVmNumber);
                    ar.exception = null;
                }
                onComplete = (Message) ar.userObj;
                if (onComplete != null) {
                    AsyncResult.forMessage(onComplete, ar.result, ar.exception);
                    onComplete.sendToTarget();
                }
            }
            break;
            default:{
                super.handleMessage(msg);
            }
        }
    }
    public PhoneSubInfo getPhoneSubInfo() {
        return mSubInfo;
    }
    public IccSmsInterfaceManager getIccSmsInterfaceManager() {
        return mRuimSmsInterfaceManager;
    }
    public IccPhoneBookInterfaceManager getIccPhoneBookInterfaceManager() {
        return mRuimPhoneBookInterfaceManager;
    }
    public void registerForNvLoaded(Handler h, int what, Object obj) {
        Registrant r = new Registrant (h, what, obj);
        mNvLoadedRegistrants.add(r);
    }
    public void unregisterForNvLoaded(Handler h) {
        mNvLoadedRegistrants.remove(h);
    }
    public void registerForEriFileLoaded(Handler h, int what, Object obj) {
        Registrant r = new Registrant (h, what, obj);
        mEriFileLoadedRegistrants.add(r);
    }
    public void unregisterForEriFileLoaded(Handler h) {
        mEriFileLoadedRegistrants.remove(h);
    }
    public final void setSystemProperty(String property, String value) {
        super.setSystemProperty(property, value);
    }
    public IccFileHandler getIccFileHandler() {
        return this.mIccFileHandler;
    }
    public void setTTYMode(int ttyMode, Message onComplete) {
        this.mCM.setTTYMode(ttyMode, onComplete);
    }
    public void queryTTYMode(Message onComplete) {
        this.mCM.queryTTYMode(onComplete);
    }
    public void activateCellBroadcastSms(int activate, Message response) {
        mSMS.activateCellBroadcastSms(activate, response);
    }
    public void getCellBroadcastSmsConfig(Message response) {
        mSMS.getCellBroadcastSmsConfig(response);
    }
    public void setCellBroadcastSmsConfig(int[] configValuesArray, Message response) {
        mSMS.setCellBroadcastConfig(configValuesArray, response);
    }
    private static final String IS683A_FEATURE_CODE = "*228";
    private static final int IS683A_FEATURE_CODE_NUM_DIGITS = 4;
    private static final int IS683A_SYS_SEL_CODE_NUM_DIGITS = 2;
    private static final int IS683A_SYS_SEL_CODE_OFFSET = 4;
    private static final int IS683_CONST_800MHZ_A_BAND = 0;
    private static final int IS683_CONST_800MHZ_B_BAND = 1;
    private static final int IS683_CONST_1900MHZ_A_BLOCK = 2;
    private static final int IS683_CONST_1900MHZ_B_BLOCK = 3;
    private static final int IS683_CONST_1900MHZ_C_BLOCK = 4;
    private static final int IS683_CONST_1900MHZ_D_BLOCK = 5;
    private static final int IS683_CONST_1900MHZ_E_BLOCK = 6;
    private static final int IS683_CONST_1900MHZ_F_BLOCK = 7;
    private static final int INVALID_SYSTEM_SELECTION_CODE = -1;
    private boolean isIs683OtaSpDialStr(String dialStr) {
        int sysSelCodeInt;
        boolean isOtaspDialString = false;
        int dialStrLen = dialStr.length();
        if (dialStrLen == IS683A_FEATURE_CODE_NUM_DIGITS) {
            if (dialStr.equals(IS683A_FEATURE_CODE)) {
                isOtaspDialString = true;
            }
        } else {
            sysSelCodeInt = extractSelCodeFromOtaSpNum(dialStr);
            switch (sysSelCodeInt) {
                case IS683_CONST_800MHZ_A_BAND:
                case IS683_CONST_800MHZ_B_BAND:
                case IS683_CONST_1900MHZ_A_BLOCK:
                case IS683_CONST_1900MHZ_B_BLOCK:
                case IS683_CONST_1900MHZ_C_BLOCK:
                case IS683_CONST_1900MHZ_D_BLOCK:
                case IS683_CONST_1900MHZ_E_BLOCK:
                case IS683_CONST_1900MHZ_F_BLOCK:
                    isOtaspDialString = true;
                    break;
                default:
                    break;
            }
        }
        return isOtaspDialString;
    }
    private int extractSelCodeFromOtaSpNum(String dialStr) {
        int dialStrLen = dialStr.length();
        int sysSelCodeInt = INVALID_SYSTEM_SELECTION_CODE;
        if ((dialStr.regionMatches(0, IS683A_FEATURE_CODE,
                                   0, IS683A_FEATURE_CODE_NUM_DIGITS)) &&
            (dialStrLen >= (IS683A_FEATURE_CODE_NUM_DIGITS +
                            IS683A_SYS_SEL_CODE_NUM_DIGITS))) {
                sysSelCodeInt = Integer.parseInt (
                                dialStr.substring (IS683A_FEATURE_CODE_NUM_DIGITS,
                                IS683A_FEATURE_CODE_NUM_DIGITS + IS683A_SYS_SEL_CODE_NUM_DIGITS));
        }
        if (DBG) Log.d(LOG_TAG, "extractSelCodeFromOtaSpNum " + sysSelCodeInt);
        return sysSelCodeInt;
    }
    private boolean
    checkOtaSpNumBasedOnSysSelCode (int sysSelCodeInt, String sch[]) {
        boolean isOtaSpNum = false;
        try {
            int selRc = Integer.parseInt((String)sch[1]);
            for (int i = 0; i < selRc; i++) {
                if (!TextUtils.isEmpty(sch[i+2]) && !TextUtils.isEmpty(sch[i+3])) {
                    int selMin = Integer.parseInt((String)sch[i+2]);
                    int selMax = Integer.parseInt((String)sch[i+3]);
                    if ((sysSelCodeInt >= selMin) && (sysSelCodeInt <= selMax)) {
                        isOtaSpNum = true;
                        break;
                    }
                }
            }
        } catch (NumberFormatException ex) {
            Log.e(LOG_TAG, "checkOtaSpNumBasedOnSysSelCode, error", ex);
        }
        return isOtaSpNum;
    }
    private static Pattern pOtaSpNumSchema = Pattern.compile("[,\\s]+");
    private boolean isCarrierOtaSpNum(String dialStr) {
        boolean isOtaSpNum = false;
        int sysSelCodeInt = extractSelCodeFromOtaSpNum(dialStr);
        if (sysSelCodeInt == INVALID_SYSTEM_SELECTION_CODE) {
            return isOtaSpNum;
        }
        if (!TextUtils.isEmpty(mCarrierOtaSpNumSchema)) {
            Matcher m = pOtaSpNumSchema.matcher(mCarrierOtaSpNumSchema);
            if (DBG) {
                Log.d(LOG_TAG, "isCarrierOtaSpNum,schema" + mCarrierOtaSpNumSchema);
            }
            if (m.find()) {
                String sch[] = pOtaSpNumSchema.split(mCarrierOtaSpNumSchema);
                if (!TextUtils.isEmpty(sch[0]) && sch[0].equals("SELC")) {
                    if (sysSelCodeInt!=INVALID_SYSTEM_SELECTION_CODE) {
                        isOtaSpNum=checkOtaSpNumBasedOnSysSelCode(sysSelCodeInt,sch);
                    } else {
                        if (DBG) {
                            Log.d(LOG_TAG, "isCarrierOtaSpNum,sysSelCodeInt is invalid");
                        }
                    }
                } else if (!TextUtils.isEmpty(sch[0]) && sch[0].equals("FC")) {
                    int fcLen =  Integer.parseInt((String)sch[1]);
                    String fc = (String)sch[2];
                    if (dialStr.regionMatches(0,fc,0,fcLen)) {
                        isOtaSpNum = true;
                    } else {
                        if (DBG) Log.d(LOG_TAG, "isCarrierOtaSpNum,not otasp number");
                    }
                } else {
                    if (DBG) {
                        Log.d(LOG_TAG, "isCarrierOtaSpNum,ota schema not supported" + sch[0]);
                    }
                }
            } else {
                if (DBG) {
                    Log.d(LOG_TAG, "isCarrierOtaSpNum,ota schema pattern not right" +
                          mCarrierOtaSpNumSchema);
                }
            }
        } else {
            if (DBG) Log.d(LOG_TAG, "isCarrierOtaSpNum,ota schema pattern empty");
        }
        return isOtaSpNum;
    }
    @Override
    public  boolean isOtaSpNumber(String dialStr){
        boolean isOtaSpNum = false;
        String dialableStr = PhoneNumberUtils.extractNetworkPortionAlt(dialStr);
        if (dialableStr != null) {
            isOtaSpNum = isIs683OtaSpDialStr(dialableStr);
            if (isOtaSpNum == false) {
                isOtaSpNum = isCarrierOtaSpNum(dialableStr);
            }
        }
        if (DBG) Log.d(LOG_TAG, "isOtaSpNumber " + isOtaSpNum);
        return isOtaSpNum;
    }
    @Override
    public int getCdmaEriIconIndex() {
        return getServiceState().getCdmaEriIconIndex();
    }
    @Override
    public int getCdmaEriIconMode() {
        return getServiceState().getCdmaEriIconMode();
    }
    @Override
    public String getCdmaEriText() {
        int roamInd = getServiceState().getCdmaRoamingIndicator();
        int defRoamInd = getServiceState().getCdmaDefaultRoamingIndicator();
        return mEriManager.getCdmaEriText(roamInd, defRoamInd);
    }
    private void storeVoiceMailNumber(String number) {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(getContext());
        SharedPreferences.Editor editor = sp.edit();
        editor.putString(VM_NUMBER_CDMA, number);
        editor.commit();
    }
    private void setIsoCountryProperty(String operatorNumeric) {
        if (TextUtils.isEmpty(operatorNumeric)) {
            setSystemProperty(PROPERTY_ICC_OPERATOR_ISO_COUNTRY, "");
        } else {
            String iso = "";
            try {
                iso = MccTable.countryCodeForMcc(Integer.parseInt(
                        operatorNumeric.substring(0,3)));
            } catch (NumberFormatException ex) {
                Log.w(LOG_TAG, "countryCodeForMcc error" + ex);
            } catch (StringIndexOutOfBoundsException ex) {
                Log.w(LOG_TAG, "countryCodeForMcc error" + ex);
            }
            setSystemProperty(PROPERTY_ICC_OPERATOR_ISO_COUNTRY, iso);
        }
    }
    boolean updateCurrentCarrierInProvider(String operatorNumeric) {
        if (!TextUtils.isEmpty(operatorNumeric)) {
            try {
                Uri uri = Uri.withAppendedPath(Telephony.Carriers.CONTENT_URI, "current");
                ContentValues map = new ContentValues();
                map.put(Telephony.Carriers.NUMERIC, operatorNumeric);
                getContext().getContentResolver().insert(uri, map);
                MccTable.updateMccMncConfiguration(this, operatorNumeric);
                return true;
            } catch (SQLException e) {
                Log.e(LOG_TAG, "Can't store current operator", e);
            }
        }
        return false;
    }
}
