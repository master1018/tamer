public final class CdmaDataConnectionTracker extends DataConnectionTracker {
    protected final String LOG_TAG = "CDMA";
    private CDMAPhone mCdmaPhone;
    private boolean noAutoAttach = false;
    private boolean mIsScreenOn = true;
    boolean failNextConnect = false;
    private ArrayList<DataConnection> dataConnectionList;
    private CdmaDataConnection mActiveDataConnection;
    private boolean mPendingRestartRadio = false;
    private static final int TIME_DELAYED_TO_RESTART_RADIO =
            SystemProperties.getInt("ro.cdma.timetoradiorestart", 60000);
    private static final int DATA_CONNECTION_POOL_SIZE = 1;
    private static final int POLL_CONNECTION_MILLIS = 5 * 1000;
    private static final String INTENT_RECONNECT_ALARM =
            "com.android.internal.telephony.cdma-reconnect";
    private static final String INTENT_RECONNECT_ALARM_EXTRA_REASON = "reason";
     private static final int DATA_CONNECTION_ACTIVE_PH_LINK_INACTIVE = 0;
     private static final int DATA_CONNECTION_ACTIVE_PH_LINK_DOWN = 1;
     private static final int DATA_CONNECTION_ACTIVE_PH_LINK_UP = 2;
    private static final String[] mSupportedApnTypes = {
            Phone.APN_TYPE_DEFAULT,
            Phone.APN_TYPE_MMS,
            Phone.APN_TYPE_DUN,
            Phone.APN_TYPE_HIPRI };
    private static final String[] mDefaultApnTypes = {
            Phone.APN_TYPE_DEFAULT,
            Phone.APN_TYPE_MMS,
            Phone.APN_TYPE_HIPRI };
    protected ApnSetting mActiveApn;
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
                Log.d(LOG_TAG, "Data reconnect alarm. Previous state was " + state);
                String reason = intent.getStringExtra(INTENT_RECONNECT_ALARM_EXTRA_REASON);
                if (state == State.FAILED) {
                    cleanUpConnection(false, reason);
                }
                trySetupData(reason);
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
    CdmaDataConnectionTracker(CDMAPhone p) {
        super(p);
        mCdmaPhone = p;
        p.mCM.registerForAvailable (this, EVENT_RADIO_AVAILABLE, null);
        p.mCM.registerForOffOrNotAvailable(this, EVENT_RADIO_OFF_OR_NOT_AVAILABLE, null);
        p.mRuimRecords.registerForRecordsLoaded(this, EVENT_RECORDS_LOADED, null);
        p.mCM.registerForNVReady(this, EVENT_NV_READY, null);
        p.mCM.registerForDataStateChanged (this, EVENT_DATA_STATE_CHANGED, null);
        p.mCT.registerForVoiceCallEnded (this, EVENT_VOICE_CALL_ENDED, null);
        p.mCT.registerForVoiceCallStarted (this, EVENT_VOICE_CALL_STARTED, null);
        p.mSST.registerForCdmaDataConnectionAttached(this, EVENT_TRY_SETUP_DATA, null);
        p.mSST.registerForCdmaDataConnectionDetached(this, EVENT_CDMA_DATA_DETACHED, null);
        p.mSST.registerForRoamingOn(this, EVENT_ROAMING_ON, null);
        p.mSST.registerForRoamingOff(this, EVENT_ROAMING_OFF, null);
        p.mCM.registerForCdmaOtaProvision(this, EVENT_CDMA_OTA_PROVISION, null);
        IntentFilter filter = new IntentFilter();
        filter.addAction(INTENT_RECONNECT_ALARM);
        filter.addAction(Intent.ACTION_SCREEN_ON);
        filter.addAction(Intent.ACTION_SCREEN_OFF);
        filter.addAction(WifiManager.NETWORK_STATE_CHANGED_ACTION);
        filter.addAction(WifiManager.WIFI_STATE_CHANGED_ACTION);
        p.getContext().registerReceiver(mIntentReceiver, filter, null, p);
        mDataConnectionTracker = this;
        createAllDataConnectionList();
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(phone.getContext());
        boolean dataEnabledSetting = true;
        try {
            dataEnabledSetting = IConnectivityManager.Stub.asInterface(ServiceManager.
                    getService(Context.CONNECTIVITY_SERVICE)).getMobileDataEnabled();
        } catch (Exception e) {
        }
        dataEnabled[APN_DEFAULT_ID] =
                !sp.getBoolean(CDMAPhone.DATA_DISABLED_ON_BOOT_KEY, false) &&
                dataEnabledSetting;
        if (dataEnabled[APN_DEFAULT_ID]) {
            enabledCount++;
        }
        noAutoAttach = !dataEnabled[APN_DEFAULT_ID];
        if (!mRetryMgr.configure(SystemProperties.get("ro.cdma.data_retry_config"))) {
            if (!mRetryMgr.configure(DEFAULT_DATA_RETRY_CONFIG)) {
                Log.e(LOG_TAG, "Could not configure using DEFAULT_DATA_RETRY_CONFIG="
                        + DEFAULT_DATA_RETRY_CONFIG);
                mRetryMgr.configure(20, 2000, 1000);
            }
        }
    }
    public void dispose() {
        phone.mCM.unregisterForAvailable(this);
        phone.mCM.unregisterForOffOrNotAvailable(this);
        mCdmaPhone.mRuimRecords.unregisterForRecordsLoaded(this);
        phone.mCM.unregisterForNVReady(this);
        phone.mCM.unregisterForDataStateChanged(this);
        mCdmaPhone.mCT.unregisterForVoiceCallEnded(this);
        mCdmaPhone.mCT.unregisterForVoiceCallStarted(this);
        mCdmaPhone.mSST.unregisterForCdmaDataConnectionAttached(this);
        mCdmaPhone.mSST.unregisterForCdmaDataConnectionDetached(this);
        mCdmaPhone.mSST.unregisterForRoamingOn(this);
        mCdmaPhone.mSST.unregisterForRoamingOff(this);
        phone.mCM.unregisterForCdmaOtaProvision(this);
        phone.getContext().unregisterReceiver(this.mIntentReceiver);
        destroyAllDataConnectionList();
    }
    protected void finalize() {
        if(DBG) Log.d(LOG_TAG, "CdmaDataConnectionTracker finalized");
    }
    protected void setState(State s) {
        if (DBG) log ("setState: " + s);
        if (state != s) {
            EventLog.writeEvent(EventLogTags.CDMA_DATA_STATE_CHANGE,
                    state.toString(), s.toString());
            state = s;
        }
    }
    @Override
    protected boolean isApnTypeActive(String type) {
        return mActiveApn != null && mActiveApn.canHandleType(type);
    }
    @Override
    protected boolean isApnTypeAvailable(String type) {
        for (String s : mSupportedApnTypes) {
            if (TextUtils.equals(type, s)) {
                return true;
            }
        }
        return false;
    }
    protected String[] getActiveApnTypes() {
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
        return null;
    }
    public boolean isDataConnectionAsDesired() {
        boolean roaming = phone.getServiceState().getRoaming();
        if (((phone.mCM.getRadioState() == CommandsInterface.RadioState.NV_READY) ||
                 mCdmaPhone.mRuimRecords.getRecordsLoaded()) &&
                (mCdmaPhone.mSST.getCurrentCdmaDataConnectionState() ==
                 ServiceState.STATE_IN_SERVICE) &&
                (!roaming || getDataOnRoamingEnabled()) &&
                !mIsWifiConnected ) {
            return (state == State.CONNECTED);
        }
        return true;
    }
    private boolean isDataAllowed() {
        boolean roaming = phone.getServiceState().getRoaming();
        return getAnyDataEnabled() && (!roaming || getDataOnRoamingEnabled()) && mMasterDataEnabled;
    }
    private boolean trySetupData(String reason) {
        if (DBG) log("***trySetupData due to " + (reason == null ? "(unspecified)" : reason));
        if (phone.getSimulatedRadioControl() != null) {
            setState(State.CONNECTED);
            phone.notifyDataConnection(reason);
            Log.i(LOG_TAG, "(fix?) We're on the simulator; assuming data is connected");
            return true;
        }
        int psState = mCdmaPhone.mSST.getCurrentCdmaDataConnectionState();
        boolean roaming = phone.getServiceState().getRoaming();
        boolean desiredPowerState = mCdmaPhone.mSST.getDesiredPowerState();
        if ((state == State.IDLE || state == State.SCANNING)
                && (psState == ServiceState.STATE_IN_SERVICE)
                && ((phone.mCM.getRadioState() == CommandsInterface.RadioState.NV_READY) ||
                        mCdmaPhone.mRuimRecords.getRecordsLoaded())
                && (mCdmaPhone.mSST.isConcurrentVoiceAndData() ||
                        phone.getState() == Phone.State.IDLE )
                && isDataAllowed()
                && desiredPowerState
                && !mPendingRestartRadio
                && !mCdmaPhone.needsOtaServiceProvisioning()) {
            return setupData(reason);
        } else {
            if (DBG) {
                    log("trySetupData: Not ready for data: " +
                    " dataState=" + state +
                    " PS state=" + psState +
                    " radio state=" + phone.mCM.getRadioState() +
                    " ruim=" + mCdmaPhone.mRuimRecords.getRecordsLoaded() +
                    " concurrentVoice&Data=" + mCdmaPhone.mSST.isConcurrentVoiceAndData() +
                    " phoneState=" + phone.getState() +
                    " dataEnabled=" + getAnyDataEnabled() +
                    " roaming=" + roaming +
                    " dataOnRoamingEnable=" + getDataOnRoamingEnabled() +
                    " desiredPowerState=" + desiredPowerState +
                    " PendingRestartRadio=" + mPendingRestartRadio +
                    " MasterDataEnabled=" + mMasterDataEnabled +
                    " needsOtaServiceProvisioning=" + mCdmaPhone.needsOtaServiceProvisioning());
            }
            return false;
        }
    }
    private void cleanUpConnection(boolean tearDown, String reason) {
        if (DBG) log("cleanUpConnection: reason: " + reason);
        if (mReconnectIntent != null) {
            AlarmManager am =
                (AlarmManager) phone.getContext().getSystemService(Context.ALARM_SERVICE);
            am.cancel(mReconnectIntent);
            mReconnectIntent = null;
        }
        setState(State.DISCONNECTING);
        boolean notificationDeferred = false;
        for (DataConnection conn : dataConnectionList) {
            if(conn != null) {
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
        }
        stopNetStatPoll();
        if (!notificationDeferred) {
            if (DBG) log("cleanupConnection: !notificationDeferred");
            gotoIdleAndNotifyDataConnection(reason);
        }
    }
    private CdmaDataConnection findFreeDataConnection() {
        for (DataConnection connBase : dataConnectionList) {
            CdmaDataConnection conn = (CdmaDataConnection) connBase;
            if (conn.isInactive()) {
                return conn;
            }
        }
        return null;
    }
    private boolean setupData(String reason) {
        CdmaDataConnection conn = findFreeDataConnection();
        if (conn == null) {
            if (DBG) log("setupData: No free CdmaDataConnectionfound!");
            return false;
        }
        mActiveDataConnection = conn;
        String[] types;
        if (mRequestedApnType.equals(Phone.APN_TYPE_DUN)) {
            types = new String[1];
            types[0] = Phone.APN_TYPE_DUN;
        } else {
            types = mDefaultApnTypes;
        }
        mActiveApn = new ApnSetting(0, "", "", "", "", "", "", "", "", "", "", 0, types);
        Message msg = obtainMessage();
        msg.what = EVENT_DATA_SETUP_COMPLETE;
        msg.obj = reason;
        conn.connect(msg, mActiveApn);
        setState(State.INITING);
        phone.notifyDataConnection(reason);
        return true;
    }
    private void notifyDefaultData(String reason) {
        setState(State.CONNECTED);
        phone.notifyDataConnection(reason);
        startNetStatPoll();
        mRetryMgr.resetRetryCount();
    }
    private void resetPollStats() {
        txPkts = -1;
        rxPkts = -1;
        sentSinceLastRecv = 0;
        netStatPollPeriod = POLL_NETSTAT_MILLIS;
        mNoRecvPollCount = 0;
    }
    protected void startNetStatPoll() {
        if (state == State.CONNECTED && netStatPollEnabled == false) {
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
        if (DBG) log("Cleanup connection and wait " +
                (TIME_DELAYED_TO_RESTART_RADIO / 1000) + "s to restart radio");
        cleanUpConnection(true, Phone.REASON_RADIO_TURNED_OFF);
        sendEmptyMessageDelayed(EVENT_RESTART_RADIO, TIME_DELAYED_TO_RESTART_RADIO);
        mPendingRestartRadio = true;
    }
    private Runnable mPollNetStat = new Runnable() {
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
                } else if (sent > 0 && received == 0) {
                    if (phone.getState()  == Phone.State.IDLE) {
                        sentSinceLastRecv += sent;
                    } else {
                        sentSinceLastRecv = 0;
                    }
                    newActivity = Activity.DATAOUT;
                } else if (sent == 0 && received > 0) {
                    sentSinceLastRecv = 0;
                    newActivity = Activity.DATAIN;
                } else if (sent == 0 && received == 0) {
                    newActivity = (activity == Activity.DORMANT) ? activity : Activity.NONE;
                } else {
                    sentSinceLastRecv = 0;
                    newActivity = (activity == Activity.DORMANT) ? activity : Activity.NONE;
                }
                if (activity != newActivity) {
                    activity = newActivity;
                    phone.notifyDataActivity();
                }
            }
            if (sentSinceLastRecv >= NUMBER_SENT_PACKETS_OF_HANG) {
                if (mNoRecvPollCount == 0) {
                    EventLog.writeEvent(
                            EventLogTags.PDP_RADIO_RESET_COUNTDOWN_TRIGGERED,
                            sentSinceLastRecv);
                }
                if (mNoRecvPollCount < NO_RECV_POLL_LIMIT) {
                    mNoRecvPollCount++;
                    netStatPollPeriod = POLL_NETSTAT_SLOW_MILLIS;
                } else {
                    if (DBG) log("Sent " + String.valueOf(sentSinceLastRecv) +
                                        " pkts since last received");
                    netStatPollEnabled = false;
                    stopNetStatPoll();
                    restartRadio();
                    EventLog.writeEvent(EventLogTags.PDP_RADIO_RESET, NO_RECV_POLL_LIMIT);
                }
            } else {
                mNoRecvPollCount = 0;
                netStatPollPeriod = POLL_NETSTAT_MILLIS;
            }
            if (netStatPollEnabled) {
                mDataConnectionTracker.postDelayed(this, netStatPollPeriod);
            }
        }
    };
    private boolean
    shouldPostNotification(FailCause cause) {
        return (cause != FailCause.UNKNOWN);
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
            int nextReconnectDelay = mRetryMgr.getRetryTimer();
            Log.d(LOG_TAG, "Data Connection activate failed. Scheduling next attempt for "
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
                Log.d(LOG_TAG,"NOT Posting Data Connection Unavailable notification "
                                + "-- likely transient error");
            } else {
                notifyNoData(lastFailCauseCode);
            }
        }
    }
    private void notifyNoData(FailCause lastFailCauseCode) {
        setState(State.FAILED);
    }
    private void gotoIdleAndNotifyDataConnection(String reason) {
        if (DBG) log("gotoIdleAndNotifyDataConnection: reason=" + reason);
        setState(State.IDLE);
        phone.notifyDataConnection(reason);
        mActiveApn = null;
    }
    protected void onRecordsLoaded() {
        if (state == State.FAILED) {
            cleanUpConnection(false, null);
        }
        sendMessage(obtainMessage(EVENT_TRY_SETUP_DATA, Phone.REASON_SIM_LOADED));
    }
    protected void onNVReady() {
        if (state == State.FAILED) {
            cleanUpConnection(false, null);
        }
        sendMessage(obtainMessage(EVENT_TRY_SETUP_DATA));
    }
    @Override
    protected void onEnableNewApn() {
          cleanUpConnection(true, Phone.REASON_APN_SWITCHED);
    }
    protected boolean onTrySetupData(String reason) {
        return trySetupData(reason);
    }
    protected void onRoamingOff() {
        trySetupData(Phone.REASON_ROAMING_OFF);
    }
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
            notifyDefaultData(reason);
        } else {
            FailCause cause = (FailCause) (ar.result);
            if(DBG) log("Data Connection setup failed " + cause);
            if (cause.isPermanentFail()) {
                notifyNoData(cause);
                return;
            }
            startDelayedRetry(cause, reason);
        }
    }
    protected void onDisconnectDone(AsyncResult ar) {
        if(DBG) log("EVENT_DISCONNECT_DONE");
        String reason = null;
        if (ar.userObj instanceof String) {
            reason = (String) ar.userObj;
        }
        setState(State.IDLE);
        if (mPendingRestartRadio) removeMessages(EVENT_RESTART_RADIO);
        CdmaServiceStateTracker ssTracker = mCdmaPhone.mSST;
        if (ssTracker.processPendingRadioPowerOffAfterDataOff()) {
            mPendingRestartRadio = false;
        } else {
            onRestartRadio();
        }
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
    protected void onVoiceCallStarted() {
        if (state == State.CONNECTED && !mCdmaPhone.mSST.isConcurrentVoiceAndData()) {
            stopNetStatPoll();
            phone.notifyDataConnection(Phone.REASON_VOICE_CALL_STARTED);
        }
    }
    protected void onVoiceCallEnded() {
        if (state == State.CONNECTED) {
            if (!mCdmaPhone.mSST.isConcurrentVoiceAndData()) {
                startNetStatPoll();
                phone.notifyDataConnection(Phone.REASON_VOICE_CALL_ENDED);
            } else {
                resetPollStats();
            }
        } else {
            mRetryMgr.resetRetryCount();
            trySetupData(Phone.REASON_VOICE_CALL_ENDED);
        }
    }
    protected void onCleanUpConnection(boolean tearDown, String reason) {
        cleanUpConnection(tearDown, reason);
    }
    private void createAllDataConnectionList() {
       dataConnectionList = new ArrayList<DataConnection>();
        CdmaDataConnection dataConn;
       for (int i = 0; i < DATA_CONNECTION_POOL_SIZE; i++) {
            dataConn = CdmaDataConnection.makeDataConnection(mCdmaPhone);
            dataConnectionList.add(dataConn);
       }
    }
    private void destroyAllDataConnectionList() {
        if(dataConnectionList != null) {
            dataConnectionList.removeAll(dataConnectionList);
        }
    }
    private void onCdmaDataDetached() {
        if (state == State.CONNECTED) {
            startNetStatPoll();
            phone.notifyDataConnection(Phone.REASON_CDMA_DATA_DETACHED);
        } else {
            if (state == State.FAILED) {
                cleanUpConnection(false, Phone.REASON_CDMA_DATA_DETACHED);
                mRetryMgr.resetRetryCount();
                CdmaCellLocation loc = (CdmaCellLocation)(phone.getCellLocation());
                EventLog.writeEvent(EventLogTags.CDMA_DATA_SETUP_FAILED,
                        loc != null ? loc.getBaseStationId() : -1,
                        TelephonyManager.getDefault().getNetworkType());
            }
            trySetupData(Phone.REASON_CDMA_DATA_DETACHED);
        }
    }
    private void onCdmaOtaProvision(AsyncResult ar) {
        if (ar.exception != null) {
            int [] otaPrivision = (int [])ar.result;
            if ((otaPrivision != null) && (otaPrivision.length > 1)) {
                switch (otaPrivision[0]) {
                case Phone.CDMA_OTA_PROVISION_STATUS_COMMITTED:
                case Phone.CDMA_OTA_PROVISION_STATUS_OTAPA_STOPPED:
                    mRetryMgr.resetRetryCount();
                    break;
                default:
                    break;
                }
            }
        }
    }
    private void onRestartRadio() {
        if (mPendingRestartRadio) {
            Log.d(LOG_TAG, "************TURN OFF RADIO**************");
            phone.mCM.setRadioPower(false, null);
            mPendingRestartRadio = false;
        }
    }
    private void writeEventLogCdmaDataDrop() {
        CdmaCellLocation loc = (CdmaCellLocation)(phone.getCellLocation());
        EventLog.writeEvent(EventLogTags.CDMA_DATA_DROP,
                loc != null ? loc.getBaseStationId() : -1,
                TelephonyManager.getDefault().getNetworkType());
    }
    protected void onDataStateChanged(AsyncResult ar) {
        ArrayList<DataCallState> dataCallStates = (ArrayList<DataCallState>)(ar.result);
        if (ar.exception != null) {
            return;
        }
        if (state == State.CONNECTED) {
            boolean isActiveOrDormantConnectionPresent = false;
            int connectionState = DATA_CONNECTION_ACTIVE_PH_LINK_INACTIVE;
            for (int index = 0; index < dataCallStates.size(); index++) {
                connectionState = dataCallStates.get(index).active;
                if (connectionState != DATA_CONNECTION_ACTIVE_PH_LINK_INACTIVE) {
                    isActiveOrDormantConnectionPresent = true;
                    break;
                }
            }
            if (!isActiveOrDormantConnectionPresent) {
                Log.i(LOG_TAG, "onDataStateChanged: No active connection"
                        + "state is CONNECTED, disconnecting/cleanup");
                writeEventLogCdmaDataDrop();
                cleanUpConnection(true, null);
                return;
            }
            switch (connectionState) {
                case DATA_CONNECTION_ACTIVE_PH_LINK_UP:
                    Log.v(LOG_TAG, "onDataStateChanged: active=LINK_ACTIVE && CONNECTED, ignore");
                    activity = Activity.NONE;
                    phone.notifyDataActivity();
                    startNetStatPoll();
                    break;
                case DATA_CONNECTION_ACTIVE_PH_LINK_DOWN:
                    Log.v(LOG_TAG, "onDataStateChanged active=LINK_DOWN && CONNECTED, dormant");
                    activity = Activity.DORMANT;
                    phone.notifyDataActivity();
                    stopNetStatPoll();
                    break;
                default:
                    Log.v(LOG_TAG, "onDataStateChanged: IGNORE unexpected DataCallState.active="
                            + connectionState);
            }
        } else {
            Log.i(LOG_TAG, "onDataStateChanged: not connected, state=" + state + " ignoring");
        }
    }
    protected String getInterfaceName(String apnType) {
        if (mActiveDataConnection != null) {
            return mActiveDataConnection.getInterface();
        }
        return null;
    }
    protected String getIpAddress(String apnType) {
        if (mActiveDataConnection != null) {
            return mActiveDataConnection.getIpAddress();
        }
        return null;
    }
    protected String getGateway(String apnType) {
        if (mActiveDataConnection != null) {
            return mActiveDataConnection.getGatewayAddress();
        }
        return null;
    }
    protected String[] getDnsServers(String apnType) {
        if (mActiveDataConnection != null) {
            return mActiveDataConnection.getDnsServers();
        }
        return null;
    }
    public ArrayList<DataConnection> getAllDataConnections() {
        return dataConnectionList;
    }
    private void startDelayedRetry(FailCause cause, String reason) {
        notifyNoData(cause);
        reconnectAfterFail(cause, reason);
    }
    public void handleMessage (Message msg) {
        if (!phone.mIsTheCurrentActivePhone) {
            Log.d(LOG_TAG, "Ignore CDMA msgs since CDMA phone is inactive");
            return;
        }
        switch (msg.what) {
            case EVENT_RECORDS_LOADED:
                onRecordsLoaded();
                break;
            case EVENT_NV_READY:
                onNVReady();
                break;
            case EVENT_CDMA_DATA_DETACHED:
                onCdmaDataDetached();
                break;
            case EVENT_DATA_STATE_CHANGED:
                onDataStateChanged((AsyncResult) msg.obj);
                break;
            case EVENT_CDMA_OTA_PROVISION:
                onCdmaOtaProvision((AsyncResult) msg.obj);
                break;
            case EVENT_RESTART_RADIO:
                if (DBG) log("EVENT_RESTART_RADIO");
                onRestartRadio();
                break;
            default:
                super.handleMessage(msg);
                break;
        }
    }
    protected void log(String s) {
        Log.d(LOG_TAG, "[CdmaDataConnectionTracker] " + s);
    }
}
