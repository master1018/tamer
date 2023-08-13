public final class GsmDataConnectionTracker extends DataConnectionTracker {
    protected final String LOG_TAG = "GSM";
    private GSMPhone mGsmPhone;
    private class ApnChangeObserver extends ContentObserver {
        public ApnChangeObserver () {
            super(mDataConnectionTracker);
        }
        @Override
        public void onChange(boolean selfChange) {
            sendMessage(obtainMessage(EVENT_APN_CHANGED));
        }
    }
    private boolean noAutoAttach = false;
    private boolean mReregisterOnReconnectFailure = false;
    private ContentResolver mResolver;
    private boolean mPingTestActive = false;
    private int mPdpResetCount = 0;
    private boolean mIsScreenOn = true;
    protected static final int APN_DELAY_MILLIS = 5000;
    boolean failNextConnect = false;
    private ArrayList<ApnSetting> allApns = null;
    private ArrayList<ApnSetting> waitingApns = null;
    private ApnSetting preferredApn = null;
    protected ApnSetting mActiveApn;
    private ArrayList<DataConnection> pdpList;
    private GsmDataConnection mActivePdp;
    private boolean mIsPsRestricted = false;
    private static final int PDP_CONNECTION_POOL_SIZE = 1;
    private static final int POLL_PDP_MILLIS = 5 * 1000;
    private static final String INTENT_RECONNECT_ALARM = "com.android.internal.telephony.gprs-reconnect";
    private static final String INTENT_RECONNECT_ALARM_EXTRA_REASON = "reason";
    static final Uri PREFERAPN_URI = Uri.parse("content:
    static final String APN_ID = "apn_id";
    private boolean canSetPreferApn = false;
    private RetryManager mDefaultRetryManager;
    private RetryManager mSecondaryRetryManager;
    BroadcastReceiver mIntentReceiver = new BroadcastReceiver ()
    {
        @Override
        public void onReceive(Context context, Intent intent)
        {
            String action = intent.getAction();
            if (action.equals(Intent.ACTION_SCREEN_ON)) {
                mIsScreenOn = true;
                stopNetStatPoll();
                startNetStatPoll();
            } else if (action.equals(Intent.ACTION_SCREEN_OFF)) {
                mIsScreenOn = false;
                stopNetStatPoll();
                startNetStatPoll();
            } else if (action.equals((INTENT_RECONNECT_ALARM))) {
                Log.d(LOG_TAG, "GPRS reconnect alarm. Previous state was " + state);
                String reason = intent.getStringExtra(INTENT_RECONNECT_ALARM_EXTRA_REASON);
                if (state == State.FAILED) {
                    Message msg = obtainMessage(EVENT_CLEAN_UP_CONNECTION);
                    msg.arg1 = 0; 
                    msg.obj = (String) reason;
                    sendMessage(msg);
                }
                sendMessage(obtainMessage(EVENT_TRY_SETUP_DATA));
            } else if (action.equals(WifiManager.NETWORK_STATE_CHANGED_ACTION)) {
                final android.net.NetworkInfo networkInfo = (NetworkInfo)
                        intent.getParcelableExtra(WifiManager.EXTRA_NETWORK_INFO);
                mIsWifiConnected = (networkInfo != null && networkInfo.isConnected());
            } else if (action.equals(WifiManager.WIFI_STATE_CHANGED_ACTION)) {
                final boolean enabled = intent.getIntExtra(WifiManager.EXTRA_WIFI_STATE,
                        WifiManager.WIFI_STATE_UNKNOWN) == WifiManager.WIFI_STATE_ENABLED;
                if (!enabled) {
                    mIsWifiConnected = false;
                }
            }
        }
    };
    private ApnChangeObserver apnObserver;
    GsmDataConnectionTracker(GSMPhone p) {
        super(p);
        mGsmPhone = p;
        p.mCM.registerForAvailable (this, EVENT_RADIO_AVAILABLE, null);
        p.mCM.registerForOffOrNotAvailable(this, EVENT_RADIO_OFF_OR_NOT_AVAILABLE, null);
        p.mSIMRecords.registerForRecordsLoaded(this, EVENT_RECORDS_LOADED, null);
        p.mCM.registerForDataStateChanged (this, EVENT_DATA_STATE_CHANGED, null);
        p.mCT.registerForVoiceCallEnded (this, EVENT_VOICE_CALL_ENDED, null);
        p.mCT.registerForVoiceCallStarted (this, EVENT_VOICE_CALL_STARTED, null);
        p.mSST.registerForGprsAttached(this, EVENT_GPRS_ATTACHED, null);
        p.mSST.registerForGprsDetached(this, EVENT_GPRS_DETACHED, null);
        p.mSST.registerForRoamingOn(this, EVENT_ROAMING_ON, null);
        p.mSST.registerForRoamingOff(this, EVENT_ROAMING_OFF, null);
        p.mSST.registerForPsRestrictedEnabled(this, EVENT_PS_RESTRICT_ENABLED, null);
        p.mSST.registerForPsRestrictedDisabled(this, EVENT_PS_RESTRICT_DISABLED, null);
        IntentFilter filter = new IntentFilter();
        filter.addAction(INTENT_RECONNECT_ALARM);
        filter.addAction(Intent.ACTION_SCREEN_ON);
        filter.addAction(Intent.ACTION_SCREEN_OFF);
        filter.addAction(WifiManager.NETWORK_STATE_CHANGED_ACTION);
        filter.addAction(WifiManager.WIFI_STATE_CHANGED_ACTION);
        p.getContext().registerReceiver(mIntentReceiver, filter, null, p);
        mDataConnectionTracker = this;
        mResolver = phone.getContext().getContentResolver();
        apnObserver = new ApnChangeObserver();
        p.getContext().getContentResolver().registerContentObserver(
                Telephony.Carriers.CONTENT_URI, true, apnObserver);
        createAllPdpList();
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(phone.getContext());
        boolean dataEnabledSetting = true;
        try {
            dataEnabledSetting = IConnectivityManager.Stub.asInterface(ServiceManager.
                getService(Context.CONNECTIVITY_SERVICE)).getMobileDataEnabled();
        } catch (Exception e) {
        }
        dataEnabled[APN_DEFAULT_ID] = !sp.getBoolean(GSMPhone.DATA_DISABLED_ON_BOOT_KEY, false) &&
                dataEnabledSetting;
        if (dataEnabled[APN_DEFAULT_ID]) {
            enabledCount++;
        }
        noAutoAttach = !dataEnabled[APN_DEFAULT_ID];
        if (!mRetryMgr.configure(SystemProperties.get("ro.gsm.data_retry_config"))) {
            if (!mRetryMgr.configure(DEFAULT_DATA_RETRY_CONFIG)) {
                Log.e(LOG_TAG, "Could not configure using DEFAULT_DATA_RETRY_CONFIG="
                        + DEFAULT_DATA_RETRY_CONFIG);
                mRetryMgr.configure(20, 2000, 1000);
            }
        }
        mDefaultRetryManager = mRetryMgr;
        mSecondaryRetryManager = new RetryManager();
        if (!mSecondaryRetryManager.configure(SystemProperties.get(
                "ro.gsm.2nd_data_retry_config"))) {
            if (!mSecondaryRetryManager.configure(SECONDARY_DATA_RETRY_CONFIG)) {
                Log.e(LOG_TAG, "Could note configure using SECONDARY_DATA_RETRY_CONFIG="
                        + SECONDARY_DATA_RETRY_CONFIG);
                mSecondaryRetryManager.configure("max_retries=3, 333, 333, 333");
            }
        }
    }
    public void dispose() {
        phone.mCM.unregisterForAvailable(this);
        phone.mCM.unregisterForOffOrNotAvailable(this);
        mGsmPhone.mSIMRecords.unregisterForRecordsLoaded(this);
        phone.mCM.unregisterForDataStateChanged(this);
        mGsmPhone.mCT.unregisterForVoiceCallEnded(this);
        mGsmPhone.mCT.unregisterForVoiceCallStarted(this);
        mGsmPhone.mSST.unregisterForGprsAttached(this);
        mGsmPhone.mSST.unregisterForGprsDetached(this);
        mGsmPhone.mSST.unregisterForRoamingOn(this);
        mGsmPhone.mSST.unregisterForRoamingOff(this);
        mGsmPhone.mSST.unregisterForPsRestrictedEnabled(this);
        mGsmPhone.mSST.unregisterForPsRestrictedDisabled(this);
        phone.getContext().unregisterReceiver(this.mIntentReceiver);
        phone.getContext().getContentResolver().unregisterContentObserver(this.apnObserver);
        destroyAllPdpList();
    }
    protected void finalize() {
        if(DBG) Log.d(LOG_TAG, "GsmDataConnectionTracker finalized");
    }
    protected void setState(State s) {
        if (DBG) log ("setState: " + s);
        if (state != s) {
            EventLog.writeEvent(EventLogTags.GSM_DATA_STATE_CHANGE, state.toString(), s.toString());
            state = s;
        }
        if (state == State.FAILED) {
            if (waitingApns != null)
                waitingApns.clear(); 
        }
    }
    public String[] getActiveApnTypes() {
        String[] result;
        if (mActiveApn != null) {
            result = mActiveApn.types;
        } else {
            result = new String[1];
            result[0] = Phone.APN_TYPE_DEFAULT;
        }
        return result;
    }
    protected String getActiveApnString() {
        String result = null;
        if (mActiveApn != null) {
            result = mActiveApn.apn;
        }
        return result;
    }
    public boolean isDataConnectionAsDesired() {
        boolean roaming = phone.getServiceState().getRoaming();
        if (mGsmPhone.mSIMRecords.getRecordsLoaded() &&
                mGsmPhone.mSST.getCurrentGprsState() == ServiceState.STATE_IN_SERVICE &&
                (!roaming || getDataOnRoamingEnabled()) &&
            !mIsWifiConnected &&
            !mIsPsRestricted ) {
            return (state == State.CONNECTED);
        }
        return true;
    }
    @Override
    protected boolean isApnTypeActive(String type) {
        return mActiveApn != null && mActiveApn.canHandleType(type);
    }
    @Override
    protected boolean isApnTypeAvailable(String type) {
        if (type.equals(Phone.APN_TYPE_DUN)) {
            return (fetchDunApn() != null);
        }
        if (allApns != null) {
            for (ApnSetting apn : allApns) {
                if (apn.canHandleType(type)) {
                    return true;
                }
            }
        }
        return false;
    }
    public ArrayList<DataConnection> getAllDataConnections() {
        ArrayList<DataConnection> pdps = (ArrayList<DataConnection>)pdpList.clone();
        return pdps;
    }
    private boolean isDataAllowed() {
        boolean roaming = phone.getServiceState().getRoaming();
        return getAnyDataEnabled() && (!roaming || getDataOnRoamingEnabled()) &&
                mMasterDataEnabled;
    }
    protected void onGprsDetached() {
        stopNetStatPoll();
        phone.notifyDataConnection(Phone.REASON_GPRS_DETACHED);
    }
    private void onGprsAttached() {
        if (state == State.CONNECTED) {
            startNetStatPoll();
            phone.notifyDataConnection(Phone.REASON_GPRS_ATTACHED);
        } else {
            if (state == State.FAILED) {
                cleanUpConnection(false, Phone.REASON_GPRS_ATTACHED);
                mRetryMgr.resetRetryCount();
            }
            trySetupData(Phone.REASON_GPRS_ATTACHED);
        }
    }
    private boolean trySetupData(String reason) {
        if (DBG) log("***trySetupData due to " + (reason == null ? "(unspecified)" : reason));
        Log.d(LOG_TAG, "[DSAC DEB] " + "trySetupData with mIsPsRestricted=" + mIsPsRestricted);
        if (phone.getSimulatedRadioControl() != null) {
            setState(State.CONNECTED);
            phone.notifyDataConnection(reason);
            Log.i(LOG_TAG, "(fix?) We're on the simulator; assuming data is connected");
            return true;
        }
        int gprsState = mGsmPhone.mSST.getCurrentGprsState();
        boolean desiredPowerState = mGsmPhone.mSST.getDesiredPowerState();
        if ((state == State.IDLE || state == State.SCANNING)
                && (gprsState == ServiceState.STATE_IN_SERVICE || noAutoAttach)
                && mGsmPhone.mSIMRecords.getRecordsLoaded()
                && phone.getState() == Phone.State.IDLE
                && isDataAllowed()
                && !mIsPsRestricted
                && desiredPowerState ) {
            if (state == State.IDLE) {
                waitingApns = buildWaitingApns();
                if (waitingApns.isEmpty()) {
                    if (DBG) log("No APN found");
                    notifyNoData(GsmDataConnection.FailCause.MISSING_UKNOWN_APN);
                    return false;
                } else {
                    log ("Create from allApns : " + apnListToString(allApns));
                }
            }
            if (DBG) {
                log ("Setup waitngApns : " + apnListToString(waitingApns));
            }
            return setupData(reason);
        } else {
            if (DBG)
                log("trySetupData: Not ready for data: " +
                    " dataState=" + state +
                    " gprsState=" + gprsState +
                    " sim=" + mGsmPhone.mSIMRecords.getRecordsLoaded() +
                    " UMTS=" + mGsmPhone.mSST.isConcurrentVoiceAndData() +
                    " phoneState=" + phone.getState() +
                    " isDataAllowed=" + isDataAllowed() +
                    " dataEnabled=" + getAnyDataEnabled() +
                    " roaming=" + phone.getServiceState().getRoaming() +
                    " dataOnRoamingEnable=" + getDataOnRoamingEnabled() +
                    " ps restricted=" + mIsPsRestricted +
                    " desiredPowerState=" + desiredPowerState +
                    " MasterDataEnabled=" + mMasterDataEnabled);
            return false;
        }
    }
    private void cleanUpConnection(boolean tearDown, String reason) {
        if (DBG) log("Clean up connection due to " + reason);
        if (mReconnectIntent != null) {
            AlarmManager am =
                (AlarmManager) phone.getContext().getSystemService(Context.ALARM_SERVICE);
            am.cancel(mReconnectIntent);
            mReconnectIntent = null;
        }
        setState(State.DISCONNECTING);
        boolean notificationDeferred = false;
        for (DataConnection conn : pdpList) {
            if (tearDown) {
                if (DBG) log("cleanUpConnection: teardown, call conn.disconnect");
                conn.disconnect(obtainMessage(EVENT_DISCONNECT_DONE, reason));
                notificationDeferred = true;
            } else {
                if (DBG) log("cleanUpConnection: !tearDown, call conn.resetSynchronously");
                conn.resetSynchronously();
                notificationDeferred = false;
            }
        }
        stopNetStatPoll();
        if (!notificationDeferred) {
            if (DBG) log("cleanupConnection: !notificationDeferred");
            gotoIdleAndNotifyDataConnection(reason);
        }
    }
    private String[] parseTypes(String types) {
        String[] result;
        if (types == null || types.equals("")) {
            result = new String[1];
            result[0] = Phone.APN_TYPE_ALL;
        } else {
            result = types.split(",");
        }
        return result;
    }
    private ArrayList<ApnSetting> createApnList(Cursor cursor) {
        ArrayList<ApnSetting> result = new ArrayList<ApnSetting>();
        if (cursor.moveToFirst()) {
            do {
                String[] types = parseTypes(
                        cursor.getString(cursor.getColumnIndexOrThrow(Telephony.Carriers.TYPE)));
                ApnSetting apn = new ApnSetting(
                        cursor.getInt(cursor.getColumnIndexOrThrow(Telephony.Carriers._ID)),
                        cursor.getString(cursor.getColumnIndexOrThrow(Telephony.Carriers.NUMERIC)),
                        cursor.getString(cursor.getColumnIndexOrThrow(Telephony.Carriers.NAME)),
                        cursor.getString(cursor.getColumnIndexOrThrow(Telephony.Carriers.APN)),
                        cursor.getString(cursor.getColumnIndexOrThrow(Telephony.Carriers.PROXY)),
                        cursor.getString(cursor.getColumnIndexOrThrow(Telephony.Carriers.PORT)),
                        cursor.getString(cursor.getColumnIndexOrThrow(Telephony.Carriers.MMSC)),
                        cursor.getString(cursor.getColumnIndexOrThrow(Telephony.Carriers.MMSPROXY)),
                        cursor.getString(cursor.getColumnIndexOrThrow(Telephony.Carriers.MMSPORT)),
                        cursor.getString(cursor.getColumnIndexOrThrow(Telephony.Carriers.USER)),
                        cursor.getString(cursor.getColumnIndexOrThrow(Telephony.Carriers.PASSWORD)),
                        cursor.getInt(cursor.getColumnIndexOrThrow(Telephony.Carriers.AUTH_TYPE)),
                        types);
                result.add(apn);
            } while (cursor.moveToNext());
        }
        return result;
    }
    private GsmDataConnection findFreePdp() {
        for (DataConnection conn : pdpList) {
            GsmDataConnection pdp = (GsmDataConnection) conn;
            if (pdp.isInactive()) {
                return pdp;
            }
        }
        return null;
    }
    private boolean setupData(String reason) {
        ApnSetting apn;
        GsmDataConnection pdp;
        apn = getNextApn();
        if (apn == null) return false;
        pdp = findFreePdp();
        if (pdp == null) {
            if (DBG) log("setupData: No free GsmDataConnection found!");
            return false;
        }
        mActiveApn = apn;
        mActivePdp = pdp;
        Message msg = obtainMessage();
        msg.what = EVENT_DATA_SETUP_COMPLETE;
        msg.obj = reason;
        pdp.connect(msg, apn);
        setState(State.INITING);
        phone.notifyDataConnection(reason);
        return true;
    }
    protected String getInterfaceName(String apnType) {
        if (mActivePdp != null &&
                (apnType == null ||
                (mActiveApn != null && mActiveApn.canHandleType(apnType)))) {
            return mActivePdp.getInterface();
        }
        return null;
    }
    protected String getIpAddress(String apnType) {
        if (mActivePdp != null &&
                (apnType == null ||
                (mActiveApn != null && mActiveApn.canHandleType(apnType)))) {
            return mActivePdp.getIpAddress();
        }
        return null;
    }
    public String getGateway(String apnType) {
        if (mActivePdp != null &&
                (apnType == null ||
                (mActiveApn != null && mActiveApn.canHandleType(apnType)))) {
            return mActivePdp.getGatewayAddress();
        }
        return null;
    }
    protected String[] getDnsServers(String apnType) {
        if (mActivePdp != null &&
                (apnType == null ||
                (mActiveApn != null && mActiveApn.canHandleType(apnType)))) {
            return mActivePdp.getDnsServers();
        }
        return null;
    }
    private boolean
    pdpStatesHasCID (ArrayList<DataCallState> states, int cid) {
        for (int i = 0, s = states.size() ; i < s ; i++) {
            if (states.get(i).cid == cid) return true;
        }
        return false;
    }
    private boolean
    pdpStatesHasActiveCID (ArrayList<DataCallState> states, int cid) {
        for (int i = 0, s = states.size() ; i < s ; i++) {
            if ((states.get(i).cid == cid) && (states.get(i).active != 0)) {
                return true;
            }
        }
        return false;
    }
    private void onApnChanged() {
        boolean isConnected;
        isConnected = (state != State.IDLE && state != State.FAILED);
        mGsmPhone.updateCurrentCarrierInProvider();
        createAllApnList();
        if (state != State.DISCONNECTING) {
            cleanUpConnection(isConnected, Phone.REASON_APN_CHANGED);
            if (!isConnected) {
                mRetryMgr.resetRetryCount();
                mReregisterOnReconnectFailure = false;
                trySetupData(Phone.REASON_APN_CHANGED);
            }
        }
    }
    protected void onPdpStateChanged (AsyncResult ar, boolean explicitPoll) {
        ArrayList<DataCallState> pdpStates;
        pdpStates = (ArrayList<DataCallState>)(ar.result);
        if (ar.exception != null) {
            return;
        }
        if (state == State.CONNECTED) {
            if (!pdpStatesHasCID(pdpStates, cidActive)) {
                Log.i(LOG_TAG, "PDP connection has dropped. Reconnecting");
                GsmCellLocation loc = ((GsmCellLocation)phone.getCellLocation());
                EventLog.writeEvent(EventLogTags.PDP_NETWORK_DROP,
                        loc != null ? loc.getCid() : -1,
                        TelephonyManager.getDefault().getNetworkType());
                cleanUpConnection(true, null);
                return;
            } else if (!pdpStatesHasActiveCID(pdpStates, cidActive)) {
                if (!explicitPoll) {
                    phone.mCM.getPDPContextList(
                            this.obtainMessage(EVENT_GET_PDP_LIST_COMPLETE));
                } else {
                    Log.i(LOG_TAG, "PDP connection has dropped (active=false case). "
                                    + " Reconnecting");
                    GsmCellLocation loc = ((GsmCellLocation)phone.getCellLocation());
                    EventLog.writeEvent(EventLogTags.PDP_NETWORK_DROP,
                            loc != null ? loc.getCid() : -1,
                            TelephonyManager.getDefault().getNetworkType());
                    cleanUpConnection(true, null);
                }
            }
        }
    }
    private void notifyDefaultData(String reason) {
        setState(State.CONNECTED);
        phone.notifyDataConnection(reason);
        startNetStatPoll();
        mRetryMgr.resetRetryCount();
        mReregisterOnReconnectFailure = false;
    }
    private void gotoIdleAndNotifyDataConnection(String reason) {
        if (DBG) log("gotoIdleAndNotifyDataConnection: reason=" + reason);
        setState(State.IDLE);
        phone.notifyDataConnection(reason);
        mActiveApn = null;
    }
    private void startPeriodicPdpPoll() {
        removeMessages(EVENT_POLL_PDP);
        sendMessageDelayed(obtainMessage(EVENT_POLL_PDP), POLL_PDP_MILLIS);
    }
    private void resetPollStats() {
        txPkts = -1;
        rxPkts = -1;
        sentSinceLastRecv = 0;
        netStatPollPeriod = POLL_NETSTAT_MILLIS;
        mNoRecvPollCount = 0;
    }
    private void doRecovery() {
        if (state == State.CONNECTED) {
            int maxPdpReset = Settings.Secure.getInt(mResolver,
                    Settings.Secure.PDP_WATCHDOG_MAX_PDP_RESET_FAIL_COUNT,
                    DEFAULT_MAX_PDP_RESET_FAIL);
            if (mPdpResetCount < maxPdpReset) {
                mPdpResetCount++;
                EventLog.writeEvent(EventLogTags.PDP_RADIO_RESET, sentSinceLastRecv);
                cleanUpConnection(true, Phone.REASON_PDP_RESET);
            } else {
                mPdpResetCount = 0;
                EventLog.writeEvent(EventLogTags.PDP_REREGISTER_NETWORK, sentSinceLastRecv);
                mGsmPhone.mSST.reRegisterNetwork(null);
            }
        }
    }
    protected void startNetStatPoll() {
        if (state == State.CONNECTED && mPingTestActive == false && netStatPollEnabled == false) {
            Log.d(LOG_TAG, "[DataConnection] Start poll NetStat");
            resetPollStats();
            netStatPollEnabled = true;
            mPollNetStat.run();
        }
    }
    protected void stopNetStatPoll() {
        netStatPollEnabled = false;
        removeCallbacks(mPollNetStat);
        Log.d(LOG_TAG, "[DataConnection] Stop poll NetStat");
    }
    protected void restartRadio() {
        Log.d(LOG_TAG, "************TURN OFF RADIO**************");
        cleanUpConnection(true, Phone.REASON_RADIO_TURNED_OFF);
        mGsmPhone.mSST.powerOffRadioSafely();
        int reset = Integer.parseInt(SystemProperties.get("net.ppp.reset-by-timeout", "0"));
        SystemProperties.set("net.ppp.reset-by-timeout", String.valueOf(reset+1));
    }
    private Runnable mPollNetStat = new Runnable()
    {
        public void run() {
            long sent, received;
            long preTxPkts = -1, preRxPkts = -1;
            Activity newActivity;
            preTxPkts = txPkts;
            preRxPkts = rxPkts;
            txPkts = TrafficStats.getMobileTxPackets();
            rxPkts = TrafficStats.getMobileRxPackets();
            if (netStatPollEnabled && (preTxPkts > 0 || preRxPkts > 0)) {
                sent = txPkts - preTxPkts;
                received = rxPkts - preRxPkts;
                if ( sent > 0 && received > 0 ) {
                    sentSinceLastRecv = 0;
                    newActivity = Activity.DATAINANDOUT;
                    mPdpResetCount = 0;
                } else if (sent > 0 && received == 0) {
                    if (phone.getState() == Phone.State.IDLE) {
                        sentSinceLastRecv += sent;
                    } else {
                        sentSinceLastRecv = 0;
                    }
                    newActivity = Activity.DATAOUT;
                } else if (sent == 0 && received > 0) {
                    sentSinceLastRecv = 0;
                    newActivity = Activity.DATAIN;
                    mPdpResetCount = 0;
                } else if (sent == 0 && received == 0) {
                    newActivity = Activity.NONE;
                } else {
                    sentSinceLastRecv = 0;
                    newActivity = Activity.NONE;
                }
                if (activity != newActivity && mIsScreenOn) {
                    activity = newActivity;
                    phone.notifyDataActivity();
                }
            }
            int watchdogTrigger = Settings.Secure.getInt(mResolver,
                    Settings.Secure.PDP_WATCHDOG_TRIGGER_PACKET_COUNT,
                    NUMBER_SENT_PACKETS_OF_HANG);
            if (sentSinceLastRecv >= watchdogTrigger) {
                if (mNoRecvPollCount == 0) {
                    EventLog.writeEvent(EventLogTags.PDP_RADIO_RESET_COUNTDOWN_TRIGGERED,
                            sentSinceLastRecv);
                }
                int noRecvPollLimit = Settings.Secure.getInt(mResolver,
                        Settings.Secure.PDP_WATCHDOG_ERROR_POLL_COUNT, NO_RECV_POLL_LIMIT);
                if (mNoRecvPollCount < noRecvPollLimit) {
                    if (DBG) log("no DATAIN in a while; polling PDP");
                    phone.mCM.getDataCallList(obtainMessage(EVENT_GET_PDP_LIST_COMPLETE));
                    mNoRecvPollCount++;
                    netStatPollPeriod = Settings.Secure.getInt(mResolver,
                            Settings.Secure.PDP_WATCHDOG_ERROR_POLL_INTERVAL_MS,
                            POLL_NETSTAT_SLOW_MILLIS);
                } else {
                    if (DBG) log("Sent " + String.valueOf(sentSinceLastRecv) +
                                        " pkts since last received");
                    stopNetStatPoll();
                    Thread pingTest = new Thread() {
                        public void run() {
                            runPingTest();
                        }
                    };
                    mPingTestActive = true;
                    pingTest.start();
                }
            } else {
                mNoRecvPollCount = 0;
                if (mIsScreenOn) {
                    netStatPollPeriod = Settings.Secure.getInt(mResolver,
                            Settings.Secure.PDP_WATCHDOG_POLL_INTERVAL_MS, POLL_NETSTAT_MILLIS);
                } else {
                    netStatPollPeriod = Settings.Secure.getInt(mResolver,
                            Settings.Secure.PDP_WATCHDOG_LONG_POLL_INTERVAL_MS,
                            POLL_NETSTAT_SCREEN_OFF_MILLIS);
                }
            }
            if (netStatPollEnabled) {
                mDataConnectionTracker.postDelayed(this, netStatPollPeriod);
            }
        }
    };
    private void runPingTest () {
        int status = -1;
        try {
            String address = Settings.Secure.getString(mResolver,
                    Settings.Secure.PDP_WATCHDOG_PING_ADDRESS);
            int deadline = Settings.Secure.getInt(mResolver,
                        Settings.Secure.PDP_WATCHDOG_PING_DEADLINE, DEFAULT_PING_DEADLINE);
            if (DBG) log("pinging " + address + " for " + deadline + "s");
            if (address != null && !NULL_IP.equals(address)) {
                Process p = Runtime.getRuntime()
                                .exec("ping -c 1 -i 1 -w "+ deadline + " " + address);
                status = p.waitFor();
            }
        } catch (IOException e) {
            Log.w(LOG_TAG, "ping failed: IOException");
        } catch (Exception e) {
            Log.w(LOG_TAG, "exception trying to ping");
        }
        if (status == 0) {
            EventLog.writeEvent(EventLogTags.PDP_RADIO_RESET, -1);
            mPdpResetCount = 0;
            sendMessage(obtainMessage(EVENT_START_NETSTAT_POLL));
        } else {
            sendMessage(obtainMessage(EVENT_START_RECOVERY));
        }
    }
    private boolean shouldPostNotification(GsmDataConnection.FailCause  cause) {
        return (cause != GsmDataConnection.FailCause.UNKNOWN);
    }
    private boolean retryAfterDisconnected(String reason) {
        boolean retry = true;
        if ( Phone.REASON_RADIO_TURNED_OFF.equals(reason) ) {
            retry = false;
        }
        return retry;
    }
    private void reconnectAfterFail(FailCause lastFailCauseCode, String reason) {
        if (state == State.FAILED) {
            if (!mRetryMgr.isRetryNeeded()) {
                if (!mRequestedApnType.equals(Phone.APN_TYPE_DEFAULT)) {
                    phone.notifyDataConnection(Phone.REASON_APN_FAILED);
                    onEnableApn(apnTypeToId(mRequestedApnType), DISABLED);
                    return;
                }
                if (mReregisterOnReconnectFailure) {
                    mRetryMgr.retryForeverUsingLastTimeout();
                } else {
                    Log.d(LOG_TAG, "PDP activate failed, Reregistering to the network");
                    mReregisterOnReconnectFailure = true;
                    mGsmPhone.mSST.reRegisterNetwork(null);
                    mRetryMgr.resetRetryCount();
                    return;
                }
            }
            int nextReconnectDelay = mRetryMgr.getRetryTimer();
            Log.d(LOG_TAG, "PDP activate failed. Scheduling next attempt for "
                    + (nextReconnectDelay / 1000) + "s");
            AlarmManager am =
                (AlarmManager) phone.getContext().getSystemService(Context.ALARM_SERVICE);
            Intent intent = new Intent(INTENT_RECONNECT_ALARM);
            intent.putExtra(INTENT_RECONNECT_ALARM_EXTRA_REASON, reason);
            mReconnectIntent = PendingIntent.getBroadcast(
                    phone.getContext(), 0, intent, 0);
            am.set(AlarmManager.ELAPSED_REALTIME_WAKEUP,
                    SystemClock.elapsedRealtime() + nextReconnectDelay,
                    mReconnectIntent);
            mRetryMgr.increaseRetryCount();
            if (!shouldPostNotification(lastFailCauseCode)) {
                Log.d(LOG_TAG,"NOT Posting GPRS Unavailable notification "
                                + "-- likely transient error");
            } else {
                notifyNoData(lastFailCauseCode);
            }
        }
    }
    private void notifyNoData(GsmDataConnection.FailCause lastFailCauseCode) {
        setState(State.FAILED);
    }
    protected void onRecordsLoaded() {
        createAllApnList();
        if (state == State.FAILED) {
            cleanUpConnection(false, null);
        }
        sendMessage(obtainMessage(EVENT_TRY_SETUP_DATA, Phone.REASON_SIM_LOADED));
    }
    @Override
    protected void onEnableNewApn() {
        if (mRequestedApnType.equals(Phone.APN_TYPE_DEFAULT)) {
            mRetryMgr = mDefaultRetryManager;
        } else {
            mRetryMgr = mSecondaryRetryManager;
        }
        mRetryMgr.resetRetryCount();
        cleanUpConnection(true, Phone.REASON_APN_SWITCHED);
    }
    protected boolean onTrySetupData(String reason) {
        return trySetupData(reason);
    }
    @Override
    protected void onRoamingOff() {
        trySetupData(Phone.REASON_ROAMING_OFF);
    }
    @Override
    protected void onRoamingOn() {
        if (getDataOnRoamingEnabled()) {
            trySetupData(Phone.REASON_ROAMING_ON);
        } else {
            if (DBG) log("Tear down data connection on roaming.");
            cleanUpConnection(true, Phone.REASON_ROAMING_ON);
        }
    }
    protected void onRadioAvailable() {
        if (phone.getSimulatedRadioControl() != null) {
            setState(State.CONNECTED);
            phone.notifyDataConnection(null);
            Log.i(LOG_TAG, "We're on the simulator; assuming data is connected");
        }
        if (state != State.IDLE) {
            cleanUpConnection(true, null);
        }
    }
    protected void onRadioOffOrNotAvailable() {
        mRetryMgr.resetRetryCount();
        mReregisterOnReconnectFailure = false;
        if (phone.getSimulatedRadioControl() != null) {
            Log.i(LOG_TAG, "We're on the simulator; assuming radio off is meaningless");
        } else {
            if (DBG) log("Radio is off and clean up all connection");
            cleanUpConnection(false, Phone.REASON_RADIO_TURNED_OFF);
        }
    }
    protected void onDataSetupComplete(AsyncResult ar) {
        String reason = null;
        if (ar.userObj instanceof String) {
            reason = (String) ar.userObj;
        }
        if (ar.exception == null) {
            if (isApnTypeActive(Phone.APN_TYPE_DEFAULT)) {
                SystemProperties.set("gsm.defaultpdpcontext.active", "true");
                        if (canSetPreferApn && preferredApn == null) {
                            Log.d(LOG_TAG, "PREFERED APN is null");
                            preferredApn = mActiveApn;
                            setPreferredApn(preferredApn.id);
                        }
            } else {
                SystemProperties.set("gsm.defaultpdpcontext.active", "false");
            }
            notifyDefaultData(reason);
        } else {
            GsmDataConnection.FailCause cause;
            cause = (GsmDataConnection.FailCause) (ar.result);
            if(DBG) log("PDP setup failed " + cause);
            if (cause.isEventLoggable()) {
                GsmCellLocation loc = ((GsmCellLocation)phone.getCellLocation());
                EventLog.writeEvent(EventLogTags.PDP_SETUP_FAIL,
                        cause.ordinal(), loc != null ? loc.getCid() : -1,
                        TelephonyManager.getDefault().getNetworkType());
            }
            if (cause.isPermanentFail()) {
                notifyNoData(cause);
                if (!mRequestedApnType.equals(Phone.APN_TYPE_DEFAULT)) {
                    phone.notifyDataConnection(Phone.REASON_APN_FAILED);
                    onEnableApn(apnTypeToId(mRequestedApnType), DISABLED);
                }
                return;
            }
            waitingApns.remove(0);
            if (waitingApns.isEmpty()) {
                startDelayedRetry(cause, reason);
            } else {
                setState(State.SCANNING);
                sendMessageDelayed(obtainMessage(EVENT_TRY_SETUP_DATA, reason), APN_DELAY_MILLIS);
            }
        }
    }
    protected void onDisconnectDone(AsyncResult ar) {
        String reason = null;
        if(DBG) log("EVENT_DISCONNECT_DONE");
        if (ar.userObj instanceof String) {
           reason = (String) ar.userObj;
        }
        setState(State.IDLE);
        phone.notifyDataConnection(reason);
        mActiveApn = null;
        if (retryAfterDisconnected(reason)) {
            trySetupData(reason);
        }
    }
    @Override
    protected void onResetDone(AsyncResult ar) {
        if (DBG) log("EVENT_RESET_DONE");
        String reason = null;
        if (ar.userObj instanceof String) {
            reason = (String) ar.userObj;
        }
        gotoIdleAndNotifyDataConnection(reason);
    }
    protected void onPollPdp() {
        if (state == State.CONNECTED) {
            phone.mCM.getPDPContextList(this.obtainMessage(EVENT_GET_PDP_LIST_COMPLETE));
            sendMessageDelayed(obtainMessage(EVENT_POLL_PDP), POLL_PDP_MILLIS);
        }
    }
    protected void onVoiceCallStarted() {
        if (state == State.CONNECTED && ! mGsmPhone.mSST.isConcurrentVoiceAndData()) {
            stopNetStatPoll();
            phone.notifyDataConnection(Phone.REASON_VOICE_CALL_STARTED);
        }
    }
    protected void onVoiceCallEnded() {
        if (state == State.CONNECTED) {
            if (!mGsmPhone.mSST.isConcurrentVoiceAndData()) {
                startNetStatPoll();
                phone.notifyDataConnection(Phone.REASON_VOICE_CALL_ENDED);
            } else {
                resetPollStats();
            }
        } else {
            mRetryMgr.resetRetryCount();
            mReregisterOnReconnectFailure = false;
            trySetupData(Phone.REASON_VOICE_CALL_ENDED);
        }
    }
    protected void onCleanUpConnection(boolean tearDown, String reason) {
        cleanUpConnection(tearDown, reason);
    }
    private void createAllApnList() {
        allApns = new ArrayList<ApnSetting>();
        String operator = mGsmPhone.mSIMRecords.getSIMOperatorNumeric();
        if (operator != null) {
            String selection = "numeric = '" + operator + "'";
            Cursor cursor = phone.getContext().getContentResolver().query(
                    Telephony.Carriers.CONTENT_URI, null, selection, null, null);
            if (cursor != null) {
                if (cursor.getCount() > 0) {
                    allApns = createApnList(cursor);
                }
                cursor.close();
            }
        }
        if (allApns.isEmpty()) {
            if (DBG) log("No APN found for carrier: " + operator);
            preferredApn = null;
            notifyNoData(GsmDataConnection.FailCause.MISSING_UKNOWN_APN);
        } else {
            preferredApn = getPreferredApn();
            Log.d(LOG_TAG, "Get PreferredAPN");
            if (preferredApn != null && !preferredApn.numeric.equals(operator)) {
                preferredApn = null;
                setPreferredApn(-1);
            }
        }
    }
    private void createAllPdpList() {
        pdpList = new ArrayList<DataConnection>();
        DataConnection pdp;
        for (int i = 0; i < PDP_CONNECTION_POOL_SIZE; i++) {
            pdp = GsmDataConnection.makeDataConnection(mGsmPhone);
            pdpList.add(pdp);
         }
    }
    private void destroyAllPdpList() {
        if(pdpList != null) {
            GsmDataConnection pdp;
            pdpList.removeAll(pdpList);
        }
    }
    private ApnSetting fetchDunApn() {
        Context c = phone.getContext();
        String apnData = Settings.Secure.getString(c.getContentResolver(),
                                    Settings.Secure.TETHER_DUN_APN);
        ApnSetting dunSetting = ApnSetting.fromString(apnData);
        if (dunSetting != null) return dunSetting;
        apnData = c.getResources().getString(R.string.config_tether_apndata);
        return ApnSetting.fromString(apnData);
    }
    private ArrayList<ApnSetting> buildWaitingApns() {
        ArrayList<ApnSetting> apnList = new ArrayList<ApnSetting>();
        if (mRequestedApnType.equals(Phone.APN_TYPE_DUN)) {
            ApnSetting dun = fetchDunApn();
            if (dun != null) apnList.add(dun);
            return apnList;
        }
        String operator = mGsmPhone.mSIMRecords.getSIMOperatorNumeric();
        if (mRequestedApnType.equals(Phone.APN_TYPE_DEFAULT)) {
            if (canSetPreferApn && preferredApn != null) {
                Log.i(LOG_TAG, "Preferred APN:" + operator + ":"
                        + preferredApn.numeric + ":" + preferredApn);
                if (preferredApn.numeric.equals(operator)) {
                    Log.i(LOG_TAG, "Waiting APN set to preferred APN");
                    apnList.add(preferredApn);
                    return apnList;
                } else {
                    setPreferredApn(-1);
                    preferredApn = null;
                }
            }
        }
        if (allApns != null) {
            for (ApnSetting apn : allApns) {
                if (apn.canHandleType(mRequestedApnType)) {
                    apnList.add(apn);
                }
            }
        }
        return apnList;
    }
    private ApnSetting getNextApn() {
        ArrayList<ApnSetting> list = waitingApns;
        ApnSetting apn = null;
        if (list != null) {
            if (!list.isEmpty()) {
                apn = list.get(0);
            }
        }
        return apn;
    }
    private String apnListToString (ArrayList<ApnSetting> apns) {
        StringBuilder result = new StringBuilder();
        for (int i = 0, size = apns.size(); i < size; i++) {
            result.append('[')
                  .append(apns.get(i).toString())
                  .append(']');
        }
        return result.toString();
    }
    private void startDelayedRetry(GsmDataConnection.FailCause cause, String reason) {
        notifyNoData(cause);
        reconnectAfterFail(cause, reason);
    }
    private void setPreferredApn(int pos) {
        if (!canSetPreferApn) {
            return;
        }
        ContentResolver resolver = phone.getContext().getContentResolver();
        resolver.delete(PREFERAPN_URI, null, null);
        if (pos >= 0) {
            ContentValues values = new ContentValues();
            values.put(APN_ID, pos);
            resolver.insert(PREFERAPN_URI, values);
        }
    }
    private ApnSetting getPreferredApn() {
        if (allApns.isEmpty()) {
            return null;
        }
        Cursor cursor = phone.getContext().getContentResolver().query(
                PREFERAPN_URI, new String[] { "_id", "name", "apn" },
                null, null, Telephony.Carriers.DEFAULT_SORT_ORDER);
        if (cursor != null) {
            canSetPreferApn = true;
        } else {
            canSetPreferApn = false;
        }
        if (canSetPreferApn && cursor.getCount() > 0) {
            int pos;
            cursor.moveToFirst();
            pos = cursor.getInt(cursor.getColumnIndexOrThrow(Telephony.Carriers._ID));
            for(ApnSetting p:allApns) {
                if (p.id == pos && p.canHandleType(mRequestedApnType)) {
                    cursor.close();
                    return p;
                }
            }
        }
        if (cursor != null) {
            cursor.close();
        }
        return null;
    }
    public void handleMessage (Message msg) {
        if (DBG) Log.d(LOG_TAG,"GSMDataConnTrack handleMessage "+msg);
        if (!mGsmPhone.mIsTheCurrentActivePhone) {
            Log.d(LOG_TAG, "Ignore GSM msgs since GSM phone is inactive");
            return;
        }
        switch (msg.what) {
            case EVENT_RECORDS_LOADED:
                onRecordsLoaded();
                break;
            case EVENT_GPRS_DETACHED:
                onGprsDetached();
                break;
            case EVENT_GPRS_ATTACHED:
                onGprsAttached();
                break;
            case EVENT_DATA_STATE_CHANGED:
                onPdpStateChanged((AsyncResult) msg.obj, false);
                break;
            case EVENT_GET_PDP_LIST_COMPLETE:
                onPdpStateChanged((AsyncResult) msg.obj, true);
                break;
            case EVENT_POLL_PDP:
                onPollPdp();
                break;
            case EVENT_START_NETSTAT_POLL:
                mPingTestActive = false;
                startNetStatPoll();
                break;
            case EVENT_START_RECOVERY:
                mPingTestActive = false;
                doRecovery();
                break;
            case EVENT_APN_CHANGED:
                onApnChanged();
                break;
            case EVENT_PS_RESTRICT_ENABLED:
                Log.d(LOG_TAG, "[DSAC DEB] " + "EVENT_PS_RESTRICT_ENABLED " + mIsPsRestricted);
                stopNetStatPoll();
                mIsPsRestricted = true;
                break;
            case EVENT_PS_RESTRICT_DISABLED:
                Log.d(LOG_TAG, "[DSAC DEB] " + "EVENT_PS_RESTRICT_DISABLED " + mIsPsRestricted);
                mIsPsRestricted  = false;
                if (state == State.CONNECTED) {
                    startNetStatPoll();
                } else {
                    if (state == State.FAILED) {
                        cleanUpConnection(false, Phone.REASON_PS_RESTRICT_ENABLED);
                        mRetryMgr.resetRetryCount();
                        mReregisterOnReconnectFailure = false;
                    }
                    trySetupData(Phone.REASON_PS_RESTRICT_ENABLED);
                }
                break;
            default:
                super.handleMessage(msg);
                break;
        }
    }
    protected void log(String s) {
        Log.d(LOG_TAG, "[GsmDataConnectionTracker] " + s);
    }
}
