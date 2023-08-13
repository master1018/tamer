public abstract class PhoneBase extends Handler implements Phone {
    private static final String LOG_TAG = "PHONE";
    private static final boolean LOCAL_DEBUG = true;
    public static final String NETWORK_SELECTION_KEY = "network_selection_key";
    public static final String NETWORK_SELECTION_NAME_KEY = "network_selection_name_key";
    public static final String DATA_DISABLED_ON_BOOT_KEY = "disabled_on_boot_key";
    protected static final int EVENT_RADIO_AVAILABLE             = 1;
    protected static final int EVENT_SSN                         = 2;
    protected static final int EVENT_SIM_RECORDS_LOADED          = 3;
    protected static final int EVENT_MMI_DONE                    = 4;
    protected static final int EVENT_RADIO_ON                    = 5;
    protected static final int EVENT_GET_BASEBAND_VERSION_DONE   = 6;
    protected static final int EVENT_USSD                        = 7;
    protected static final int EVENT_RADIO_OFF_OR_NOT_AVAILABLE  = 8;
    protected static final int EVENT_GET_IMEI_DONE               = 9;
    protected static final int EVENT_GET_IMEISV_DONE             = 10;
    protected static final int EVENT_GET_SIM_STATUS_DONE         = 11;
    protected static final int EVENT_SET_CALL_FORWARD_DONE       = 12;
    protected static final int EVENT_GET_CALL_FORWARD_DONE       = 13;
    protected static final int EVENT_CALL_RING                   = 14;
    protected static final int EVENT_CALL_RING_CONTINUE          = 15;
    protected static final int EVENT_SET_NETWORK_MANUAL_COMPLETE    = 16;
    protected static final int EVENT_SET_NETWORK_AUTOMATIC_COMPLETE = 17;
    protected static final int EVENT_SET_CLIR_COMPLETE              = 18;
    protected static final int EVENT_REGISTERED_TO_NETWORK          = 19;
    protected static final int EVENT_SET_VM_NUMBER_DONE             = 20;
    protected static final int EVENT_GET_DEVICE_IDENTITY_DONE       = 21;
    protected static final int EVENT_RUIM_RECORDS_LOADED            = 22;
    protected static final int EVENT_NV_READY                       = 23;
    protected static final int EVENT_SET_ENHANCED_VP                = 24;
    protected static final int EVENT_EMERGENCY_CALLBACK_MODE_ENTER  = 25;
    protected static final int EVENT_EXIT_EMERGENCY_CALLBACK_RESPONSE = 26;
    public static final String CLIR_KEY = "clir_key";
    public static final String DNS_SERVER_CHECK_DISABLED_KEY = "dns_server_check_disabled_key";
    public CommandsInterface mCM;
    protected IccFileHandler mIccFileHandler;
    boolean mDnsCheckDisabled = false;
    public DataConnectionTracker mDataConnection;
    boolean mDoesRilSendMultipleCallRing;
    int mCallRingContinueToken = 0;
    int mCallRingDelay;
    public boolean mIsTheCurrentActivePhone = true;
    public void
    setSystemProperty(String property, String value) {
        if(getUnitTestMode()) {
            return;
        }
        SystemProperties.set(property, value);
    }
    protected final RegistrantList mPreciseCallStateRegistrants
            = new RegistrantList();
    protected final RegistrantList mNewRingingConnectionRegistrants
            = new RegistrantList();
    protected final RegistrantList mIncomingRingRegistrants
            = new RegistrantList();
    protected final RegistrantList mDisconnectRegistrants
            = new RegistrantList();
    protected final RegistrantList mServiceStateRegistrants
            = new RegistrantList();
    protected final RegistrantList mMmiCompleteRegistrants
            = new RegistrantList();
    protected final RegistrantList mMmiRegistrants
            = new RegistrantList();
    protected final RegistrantList mUnknownConnectionRegistrants
            = new RegistrantList();
    protected final RegistrantList mSuppServiceFailedRegistrants
            = new RegistrantList();
    protected Looper mLooper; 
    protected Context mContext;
    protected PhoneNotifier mNotifier;
    protected SimulatedRadioControl mSimulatedRadioControl;
    boolean mUnitTestMode;
    protected PhoneBase(PhoneNotifier notifier, Context context, CommandsInterface ci) {
        this(notifier, context, ci, false);
    }
    protected PhoneBase(PhoneNotifier notifier, Context context, CommandsInterface ci,
            boolean unitTestMode) {
        this.mNotifier = notifier;
        this.mContext = context;
        mLooper = Looper.myLooper();
        mCM = ci;
        setPropertiesByCarrier();
        setUnitTestMode(unitTestMode);
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
        mDnsCheckDisabled = sp.getBoolean(DNS_SERVER_CHECK_DISABLED_KEY, false);
        mCM.setOnCallRing(this, EVENT_CALL_RING, null);
        mDoesRilSendMultipleCallRing = SystemProperties.getBoolean(
                TelephonyProperties.PROPERTY_RIL_SENDS_MULTIPLE_CALL_RING, true);
        Log.d(LOG_TAG, "mDoesRilSendMultipleCallRing=" + mDoesRilSendMultipleCallRing);
        mCallRingDelay = SystemProperties.getInt(
                TelephonyProperties.PROPERTY_CALL_RING_DELAY, 3000);
        Log.d(LOG_TAG, "mCallRingDelay=" + mCallRingDelay);
    }
    public void dispose() {
        synchronized(PhoneProxy.lockForRadioTechnologyChange) {
            mCM.unSetOnCallRing(this);
            mDataConnection.onCleanUpConnection(false, REASON_RADIO_TURNED_OFF);
            mIsTheCurrentActivePhone = false;
        }
    }
    @Override
    public void handleMessage(Message msg) {
        AsyncResult ar;
        switch(msg.what) {
            case EVENT_CALL_RING:
                Log.d(LOG_TAG, "Event EVENT_CALL_RING Received state=" + getState());
                ar = (AsyncResult)msg.obj;
                if (ar.exception == null) {
                    Phone.State state = getState();
                    if ((!mDoesRilSendMultipleCallRing)
                            && ((state == Phone.State.RINGING) || (state == Phone.State.IDLE))) {
                        mCallRingContinueToken += 1;
                        sendIncomingCallRingNotification(mCallRingContinueToken);
                    } else {
                        notifyIncomingRing();
                    }
                }
                break;
            case EVENT_CALL_RING_CONTINUE:
                Log.d(LOG_TAG, "Event EVENT_CALL_RING_CONTINUE Received stat=" + getState());
                if (getState() == Phone.State.RINGING) {
                    sendIncomingCallRingNotification(msg.arg1);
                }
                break;
            default:
                throw new RuntimeException("unexpected event not handled");
        }
    }
    public Context getContext() {
        return mContext;
    }
    public void disableDnsCheck(boolean b) {
        mDnsCheckDisabled = b;
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(getContext());
        SharedPreferences.Editor editor = sp.edit();
        editor.putBoolean(DNS_SERVER_CHECK_DISABLED_KEY, b);
        editor.commit();
    }
    public boolean isDnsCheckDisabled() {
        return mDnsCheckDisabled;
    }
    public void registerForPreciseCallStateChanged(Handler h, int what, Object obj) {
        checkCorrectThread(h);
        mPreciseCallStateRegistrants.addUnique(h, what, obj);
    }
    public void unregisterForPreciseCallStateChanged(Handler h) {
        mPreciseCallStateRegistrants.remove(h);
    }
    protected void notifyPreciseCallStateChangedP() {
        AsyncResult ar = new AsyncResult(null, this, null);
        mPreciseCallStateRegistrants.notifyRegistrants(ar);
    }
    public void registerForUnknownConnection(Handler h, int what, Object obj) {
        checkCorrectThread(h);
        mUnknownConnectionRegistrants.addUnique(h, what, obj);
    }
    public void unregisterForUnknownConnection(Handler h) {
        mUnknownConnectionRegistrants.remove(h);
    }
    public void registerForNewRingingConnection(
            Handler h, int what, Object obj) {
        checkCorrectThread(h);
        mNewRingingConnectionRegistrants.addUnique(h, what, obj);
    }
    public void unregisterForNewRingingConnection(Handler h) {
        mNewRingingConnectionRegistrants.remove(h);
    }
    public void registerForInCallVoicePrivacyOn(Handler h, int what, Object obj){
        mCM.registerForInCallVoicePrivacyOn(h,what,obj);
    }
    public void unregisterForInCallVoicePrivacyOn(Handler h){
        mCM.unregisterForInCallVoicePrivacyOn(h);
    }
    public void registerForInCallVoicePrivacyOff(Handler h, int what, Object obj){
        mCM.registerForInCallVoicePrivacyOff(h,what,obj);
    }
    public void unregisterForInCallVoicePrivacyOff(Handler h){
        mCM.unregisterForInCallVoicePrivacyOff(h);
    }
    public void registerForIncomingRing(
            Handler h, int what, Object obj) {
        checkCorrectThread(h);
        mIncomingRingRegistrants.addUnique(h, what, obj);
    }
    public void unregisterForIncomingRing(Handler h) {
        mIncomingRingRegistrants.remove(h);
    }
    public void registerForDisconnect(Handler h, int what, Object obj) {
        checkCorrectThread(h);
        mDisconnectRegistrants.addUnique(h, what, obj);
    }
    public void unregisterForDisconnect(Handler h) {
        mDisconnectRegistrants.remove(h);
    }
    public void registerForSuppServiceFailed(Handler h, int what, Object obj) {
        checkCorrectThread(h);
        mSuppServiceFailedRegistrants.addUnique(h, what, obj);
    }
    public void unregisterForSuppServiceFailed(Handler h) {
        mSuppServiceFailedRegistrants.remove(h);
    }
    public void registerForMmiInitiate(Handler h, int what, Object obj) {
        checkCorrectThread(h);
        mMmiRegistrants.addUnique(h, what, obj);
    }
    public void unregisterForMmiInitiate(Handler h) {
        mMmiRegistrants.remove(h);
    }
    public void registerForMmiComplete(Handler h, int what, Object obj) {
        checkCorrectThread(h);
        mMmiCompleteRegistrants.addUnique(h, what, obj);
    }
    public void unregisterForMmiComplete(Handler h) {
        checkCorrectThread(h);
        mMmiCompleteRegistrants.remove(h);
    }
    private String getSavedNetworkSelection() {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(getContext());
        return sp.getString(NETWORK_SELECTION_KEY, "");
    }
    public void restoreSavedNetworkSelection(Message response) {
        String networkSelection = getSavedNetworkSelection();
        if (TextUtils.isEmpty(networkSelection)) {
            mCM.setNetworkSelectionModeAutomatic(response);
        } else {
            mCM.setNetworkSelectionModeManual(networkSelection, response);
        }
    }
    public void setUnitTestMode(boolean f) {
        mUnitTestMode = f;
    }
    public boolean getUnitTestMode() {
        return mUnitTestMode;
    }
    protected void notifyDisconnectP(Connection cn) {
        AsyncResult ar = new AsyncResult(null, cn, null);
        mDisconnectRegistrants.notifyRegistrants(ar);
    }
    public void registerForServiceStateChanged(
            Handler h, int what, Object obj) {
        checkCorrectThread(h);
        mServiceStateRegistrants.add(h, what, obj);
    }
    public void unregisterForServiceStateChanged(Handler h) {
        mServiceStateRegistrants.remove(h);
    }
    public void registerForRingbackTone(Handler h, int what, Object obj) {
        mCM.registerForRingbackTone(h,what,obj);
    }
    public void unregisterForRingbackTone(Handler h) {
        mCM.unregisterForRingbackTone(h);
    }
    public void registerForResendIncallMute(Handler h, int what, Object obj) {
        mCM.registerForResendIncallMute(h,what,obj);
    }
    public void unregisterForResendIncallMute(Handler h) {
        mCM.unregisterForResendIncallMute(h);
    }
    protected void notifyServiceStateChangedP(ServiceState ss) {
        AsyncResult ar = new AsyncResult(null, ss, null);
        mServiceStateRegistrants.notifyRegistrants(ar);
        mNotifier.notifyServiceState(this);
    }
    public SimulatedRadioControl getSimulatedRadioControl() {
        return mSimulatedRadioControl;
    }
    private void checkCorrectThread(Handler h) {
        if (h.getLooper() != mLooper) {
            throw new RuntimeException(
                    "com.android.internal.telephony.Phone must be used from within one thread");
        }
    }
    private void setPropertiesByCarrier() {
        String carrier = SystemProperties.get("ro.carrier");
        if (null == carrier || 0 == carrier.length()) {
            return;
        }
        CharSequence[] carrierLocales = mContext.
                getResources().getTextArray(R.array.carrier_properties);
        for (int i = 0; i < carrierLocales.length; i+=3) {
            String c = carrierLocales[i].toString();
            if (carrier.equals(c)) {
                String l = carrierLocales[i+1].toString();
                int wifiChannels = 0;
                try {
                    wifiChannels = Integer.parseInt(
                            carrierLocales[i+2].toString());
                } catch (NumberFormatException e) { }
                String language = l.substring(0, 2);
                String country = "";
                if (l.length() >=5) {
                    country = l.substring(3, 5);
                }
                setSystemLocale(language, country);
                if (wifiChannels != 0) {
                    try {
                        Settings.Secure.getInt(mContext.getContentResolver(),
                                Settings.Secure.WIFI_NUM_ALLOWED_CHANNELS);
                    } catch (Settings.SettingNotFoundException e) {
                        WifiManager wM = (WifiManager)
                                mContext.getSystemService(Context.WIFI_SERVICE);
                        wM.setNumAllowedChannels(wifiChannels, false);
                    }
                }
                return;
            }
        }
    }
    public void setSystemLocale(String language, String country) {
        String l = SystemProperties.get("persist.sys.language");
        String c = SystemProperties.get("persist.sys.country");
        if (null == language) {
            return; 
        }
        language = language.toLowerCase();
        if (null == country) {
            country = "";
        }
        country = country.toUpperCase();
        if((null == l || 0 == l.length()) && (null == c || 0 == c.length())) {
            try {
                String[] locales = mContext.getAssets().getLocales();
                final int N = locales.length;
                String bestMatch = null;
                for(int i = 0; i < N; i++) {
                    if (locales[i]!=null && locales[i].length() >= 5 &&
                            locales[i].substring(0,2).equals(language)) {
                        if (locales[i].substring(3,5).equals(country)) {
                            bestMatch = locales[i];
                            break;
                        } else if (null == bestMatch) {
                            bestMatch = locales[i];
                        }
                    }
                }
                if (null != bestMatch) {
                    IActivityManager am = ActivityManagerNative.getDefault();
                    Configuration config = am.getConfiguration();
                    config.locale = new Locale(bestMatch.substring(0,2),
                                               bestMatch.substring(3,5));
                    config.userSetLocale = true;
                    am.updateConfiguration(config);
                }
            } catch (Exception e) {
            }
        }
    }
    public abstract Phone.State getState();
    public abstract IccFileHandler getIccFileHandler();
    public Handler getHandler() {
        return this;
    }
    public void queryCdmaRoamingPreference(Message response) {
        mCM.queryCdmaRoamingPreference(response);
    }
    public void setCdmaRoamingPreference(int cdmaRoamingType, Message response) {
        mCM.setCdmaRoamingPreference(cdmaRoamingType, response);
    }
    public void setCdmaSubscription(int cdmaSubscriptionType, Message response) {
        mCM.setCdmaSubscription(cdmaSubscriptionType, response);
    }
    public void setPreferredNetworkType(int networkType, Message response) {
        mCM.setPreferredNetworkType(networkType, response);
    }
    public void getPreferredNetworkType(Message response) {
        mCM.getPreferredNetworkType(response);
    }
    public void getSmscAddress(Message result) {
        mCM.getSmscAddress(result);
    }
    public void setSmscAddress(String address, Message result) {
        mCM.setSmscAddress(address, result);
    }
    public void setTTYMode(int ttyMode, Message onComplete) {
        logUnexpectedCdmaMethodCall("setTTYMode");
    }
    public void queryTTYMode(Message onComplete) {
        logUnexpectedCdmaMethodCall("queryTTYMode");
    }
    public void enableEnhancedVoicePrivacy(boolean enable, Message onComplete) {
        logUnexpectedCdmaMethodCall("enableEnhancedVoicePrivacy");
    }
    public void getEnhancedVoicePrivacy(Message onComplete) {
        logUnexpectedCdmaMethodCall("getEnhancedVoicePrivacy");
    }
    public void setBandMode(int bandMode, Message response) {
        mCM.setBandMode(bandMode, response);
    }
    public void queryAvailableBandMode(Message response) {
        mCM.queryAvailableBandMode(response);
    }
    public void invokeOemRilRequestRaw(byte[] data, Message response) {
        mCM.invokeOemRilRequestRaw(data, response);
    }
    public void invokeOemRilRequestStrings(String[] strings, Message response) {
        mCM.invokeOemRilRequestStrings(strings, response);
    }
    public void notifyDataActivity() {
        mNotifier.notifyDataActivity(this);
    }
    public void notifyMessageWaitingIndicator() {
        mNotifier.notifyMessageWaitingChanged(this);
    }
    public void notifyDataConnection(String reason) {
        mNotifier.notifyDataConnection(this, reason);
    }
    public abstract String getPhoneName();
    public abstract int getPhoneType();
    public int getVoiceMessageCount(){
        return 0;
    }
    public int getCdmaEriIconIndex() {
        logUnexpectedCdmaMethodCall("getCdmaEriIconIndex");
        return -1;
    }
    public int getCdmaEriIconMode() {
        logUnexpectedCdmaMethodCall("getCdmaEriIconMode");
        return -1;
    }
    public String getCdmaEriText() {
        logUnexpectedCdmaMethodCall("getCdmaEriText");
        return "GSM nw, no ERI";
    }
    public String getCdmaMin() {
        logUnexpectedCdmaMethodCall("getCdmaMin");
        return null;
    }
    public boolean isMinInfoReady() {
        logUnexpectedCdmaMethodCall("isMinInfoReady");
        return false;
    }
    public String getCdmaPrlVersion(){
        logUnexpectedCdmaMethodCall("getCdmaPrlVersion");
        return null;
    }
    public void sendBurstDtmf(String dtmfString, int on, int off, Message onComplete) {
        logUnexpectedCdmaMethodCall("sendBurstDtmf");
    }
    public void exitEmergencyCallbackMode() {
        logUnexpectedCdmaMethodCall("exitEmergencyCallbackMode");
    }
    public void registerForCdmaOtaStatusChange(Handler h, int what, Object obj) {
        logUnexpectedCdmaMethodCall("registerForCdmaOtaStatusChange");
    }
    public void unregisterForCdmaOtaStatusChange(Handler h) {
        logUnexpectedCdmaMethodCall("unregisterForCdmaOtaStatusChange");
    }
    public void registerForSubscriptionInfoReady(Handler h, int what, Object obj) {
        logUnexpectedCdmaMethodCall("registerForSubscriptionInfoReady");
    }
    public void unregisterForSubscriptionInfoReady(Handler h) {
        logUnexpectedCdmaMethodCall("unregisterForSubscriptionInfoReady");
    }
    public  boolean isOtaSpNumber(String dialStr) {
        logUnexpectedCdmaMethodCall("isOtaSpNumber");
        return false;
    }
    public void registerForCallWaiting(Handler h, int what, Object obj){
        logUnexpectedCdmaMethodCall("registerForCallWaiting");
    }
    public void unregisterForCallWaiting(Handler h){
        logUnexpectedCdmaMethodCall("unregisterForCallWaiting");
    }
    public void registerForEcmTimerReset(Handler h, int what, Object obj) {
        logUnexpectedCdmaMethodCall("registerForEcmTimerReset");
    }
    public void unregisterForEcmTimerReset(Handler h) {
        logUnexpectedCdmaMethodCall("unregisterForEcmTimerReset");
    }
    public void registerForSignalInfo(Handler h, int what, Object obj) {
        mCM.registerForSignalInfo(h, what, obj);
    }
    public void unregisterForSignalInfo(Handler h) {
        mCM.unregisterForSignalInfo(h);
    }
    public void registerForDisplayInfo(Handler h, int what, Object obj) {
        mCM.registerForDisplayInfo(h, what, obj);
    }
     public void unregisterForDisplayInfo(Handler h) {
         mCM.unregisterForDisplayInfo(h);
     }
    public void registerForNumberInfo(Handler h, int what, Object obj) {
        mCM.registerForNumberInfo(h, what, obj);
    }
    public void unregisterForNumberInfo(Handler h) {
        mCM.unregisterForNumberInfo(h);
    }
    public void registerForRedirectedNumberInfo(Handler h, int what, Object obj) {
        mCM.registerForRedirectedNumberInfo(h, what, obj);
    }
    public void unregisterForRedirectedNumberInfo(Handler h) {
        mCM.unregisterForRedirectedNumberInfo(h);
    }
    public void registerForLineControlInfo(Handler h, int what, Object obj) {
        mCM.registerForLineControlInfo( h, what, obj);
    }
    public void unregisterForLineControlInfo(Handler h) {
        mCM.unregisterForLineControlInfo(h);
    }
    public void registerFoT53ClirlInfo(Handler h, int what, Object obj) {
        mCM.registerFoT53ClirlInfo(h, what, obj);
    }
    public void unregisterForT53ClirInfo(Handler h) {
        mCM.unregisterForT53ClirInfo(h);
    }
    public void registerForT53AudioControlInfo(Handler h, int what, Object obj) {
        mCM.registerForT53AudioControlInfo( h, what, obj);
    }
    public void unregisterForT53AudioControlInfo(Handler h) {
        mCM.unregisterForT53AudioControlInfo(h);
    }
     public void setOnEcbModeExitResponse(Handler h, int what, Object obj){
         logUnexpectedCdmaMethodCall("setOnEcbModeExitResponse");
     }
     public void unsetOnEcbModeExitResponse(Handler h){
         logUnexpectedCdmaMethodCall("unsetOnEcbModeExitResponse");
     }
    public String getInterfaceName(String apnType) {
        return mDataConnection.getInterfaceName(apnType);
    }
    public String getIpAddress(String apnType) {
        return mDataConnection.getIpAddress(apnType);
    }
    public boolean isDataConnectivityEnabled() {
        return mDataConnection.getDataEnabled();
    }
    public String getGateway(String apnType) {
        return mDataConnection.getGateway(apnType);
    }
    public String[] getDnsServers(String apnType) {
        return mDataConnection.getDnsServers(apnType);
    }
    public String[] getActiveApnTypes() {
        return mDataConnection.getActiveApnTypes();
    }
    public String getActiveApn() {
        return mDataConnection.getActiveApnString();
    }
    public int enableApnType(String type) {
        return mDataConnection.enableApnType(type);
    }
    public int disableApnType(String type) {
        return mDataConnection.disableApnType(type);
    }
    public void simulateDataConnection(Phone.DataState state) {
        DataConnectionTracker.State dcState;
        switch (state) {
            case CONNECTED:
                dcState = DataConnectionTracker.State.CONNECTED;
                break;
            case SUSPENDED:
                dcState = DataConnectionTracker.State.CONNECTED;
                break;
            case DISCONNECTED:
                dcState = DataConnectionTracker.State.FAILED;
                break;
            default:
                dcState = DataConnectionTracker.State.CONNECTING;
                break;
        }
        mDataConnection.setState(dcState);
        notifyDataConnection(null);
    }
    protected void notifyNewRingingConnectionP(Connection cn) {
        AsyncResult ar = new AsyncResult(null, cn, null);
        mNewRingingConnectionRegistrants.notifyRegistrants(ar);
    }
    private void notifyIncomingRing() {
        AsyncResult ar = new AsyncResult(null, this, null);
        mIncomingRingRegistrants.notifyRegistrants(ar);
    }
    private void sendIncomingCallRingNotification(int token) {
        if (!mDoesRilSendMultipleCallRing && (token == mCallRingContinueToken)) {
            Log.d(LOG_TAG, "Sending notifyIncomingRing");
            notifyIncomingRing();
            sendMessageDelayed(
                    obtainMessage(EVENT_CALL_RING_CONTINUE, token, 0), mCallRingDelay);
        } else {
            Log.d(LOG_TAG, "Ignoring ring notification request,"
                    + " mDoesRilSendMultipleCallRing=" + mDoesRilSendMultipleCallRing
                    + " token=" + token
                    + " mCallRingContinueToken=" + mCallRingContinueToken);
        }
    }
    private void logUnexpectedCdmaMethodCall(String name)
    {
        Log.e(LOG_TAG, "Error! " + name + "() in PhoneBase should not be " +
                "called, CDMAPhone inactive.");
    }
}
