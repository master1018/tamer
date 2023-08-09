public class CallNotifier extends Handler
        implements CallerInfoAsyncQuery.OnQueryCompleteListener {
    private static final String LOG_TAG = "CallNotifier";
    private static final boolean DBG =
            (PhoneApp.DBG_LEVEL >= 1) && (SystemProperties.getInt("ro.debuggable", 0) == 1);
    private static final boolean VDBG = (PhoneApp.DBG_LEVEL >= 2);
    private static final int RINGTONE_QUERY_WAIT_TIME = 500;  
    private static final int CALLWAITING_CALLERINFO_DISPLAY_TIME = 20000; 
    private static final int CALLWAITING_ADDCALL_DISABLE_TIME = 30000; 
    private static final int DISPLAYINFO_NOTIFICATION_TIME = 2000; 
    private boolean mCallWaitingTimeOut = false;
    private static final int CALLERINFO_QUERY_READY = 0;
    private static final int CALLERINFO_QUERYING = -1;
    private int mCallerInfoQueryState;
    private Object mCallerInfoQueryStateGuard = new Object();
    private static final int RINGER_CUSTOM_RINGTONE_QUERY_TIMEOUT = 100;
    private static final int PHONE_STATE_CHANGED = 1;
    private static final int PHONE_NEW_RINGING_CONNECTION = 2;
    private static final int PHONE_DISCONNECT = 3;
    private static final int PHONE_UNKNOWN_CONNECTION_APPEARED = 4;
    private static final int PHONE_INCOMING_RING = 5;
    private static final int PHONE_STATE_DISPLAYINFO = 6;
    private static final int PHONE_STATE_SIGNALINFO = 7;
    private static final int PHONE_CDMA_CALL_WAITING = 8;
    private static final int PHONE_ENHANCED_VP_ON = 9;
    private static final int PHONE_ENHANCED_VP_OFF = 10;
    private static final int PHONE_RINGBACK_TONE = 11;
    private static final int PHONE_RESEND_MUTE = 12;
    private static final int PHONE_MWI_CHANGED = 21;
    private static final int PHONE_BATTERY_LOW = 22;
    private static final int CALLWAITING_CALLERINFO_DISPLAY_DONE = 23;
    private static final int CALLWAITING_ADDCALL_DISABLE_TIMEOUT = 24;
    private static final int DISPLAYINFO_NOTIFICATION_DONE = 25;
    private static final int EVENT_OTA_PROVISION_CHANGE = 26;
    private static final int CDMA_CALL_WAITING_REJECT = 27;
    private static final int EMERGENCY_TONE_OFF = 0;
    private static final int EMERGENCY_TONE_ALERT = 1;
    private static final int EMERGENCY_TONE_VIBRATE = 2;
    private PhoneApp mApplication;
    private Phone mPhone;
    private Ringer mRinger;
    private BluetoothHandsfree mBluetoothHandsfree;
    private CallLogAsync mCallLog;
    private boolean mSilentRingerRequested;
    private ToneGenerator mSignalInfoToneGenerator;
    private static final int TONE_RELATIVE_VOLUME_SIGNALINFO = 80;
    private Call.State mPreviousCdmaCallState;
    private boolean mCdmaVoicePrivacyState = false;
    private boolean mIsCdmaRedialCall = false;
    private int mIsEmergencyToneOn;
    private int mCurrentEmergencyToneState = EMERGENCY_TONE_OFF;
    private EmergencyTonePlayerVibrator mEmergencyTonePlayerVibrator;
    private InCallTonePlayer mInCallRingbackTonePlayer;
    private InCallTonePlayer mCallWaitingTonePlayer;
    private AudioManager mAudioManager;
    public CallNotifier(PhoneApp app, Phone phone, Ringer ringer,
                        BluetoothHandsfree btMgr, CallLogAsync callLog) {
        mApplication = app;
        mPhone = phone;
        mCallLog = callLog;
        mAudioManager = (AudioManager) mPhone.getContext().getSystemService(Context.AUDIO_SERVICE);
        mPhone.registerForNewRingingConnection(this, PHONE_NEW_RINGING_CONNECTION, null);
        mPhone.registerForPreciseCallStateChanged(this, PHONE_STATE_CHANGED, null);
        mPhone.registerForDisconnect(this, PHONE_DISCONNECT, null);
        mPhone.registerForUnknownConnection(this, PHONE_UNKNOWN_CONNECTION_APPEARED, null);
        mPhone.registerForIncomingRing(this, PHONE_INCOMING_RING, null);
        if (mPhone.getPhoneType() == Phone.PHONE_TYPE_CDMA) {
            mPhone.registerForCdmaOtaStatusChange(this, EVENT_OTA_PROVISION_CHANGE, null);
            if (DBG) log("Registering for Call Waiting, Signal and Display Info.");
            mPhone.registerForCallWaiting(this, PHONE_CDMA_CALL_WAITING, null);
            mPhone.registerForDisplayInfo(this, PHONE_STATE_DISPLAYINFO, null);
            mPhone.registerForSignalInfo(this, PHONE_STATE_SIGNALINFO, null);
            mPhone.registerForInCallVoicePrivacyOn(this, PHONE_ENHANCED_VP_ON, null);
            mPhone.registerForInCallVoicePrivacyOff(this, PHONE_ENHANCED_VP_OFF, null);
            try {
                mSignalInfoToneGenerator = new ToneGenerator(AudioManager.STREAM_VOICE_CALL,
                        TONE_RELATIVE_VOLUME_SIGNALINFO);
            } catch (RuntimeException e) {
                Log.w(LOG_TAG, "CallNotifier: Exception caught while creating " +
                        "mSignalInfoToneGenerator: " + e);
                mSignalInfoToneGenerator = null;
            }
        }
        if (mPhone.getPhoneType() == Phone.PHONE_TYPE_GSM) {
            mPhone.registerForRingbackTone(this, PHONE_RINGBACK_TONE, null);
            mPhone.registerForResendIncallMute(this, PHONE_RESEND_MUTE, null);
        }
        mRinger = ringer;
        mBluetoothHandsfree = btMgr;
        TelephonyManager telephonyManager = (TelephonyManager)app.getSystemService(
                Context.TELEPHONY_SERVICE);
        telephonyManager.listen(mPhoneStateListener,
                PhoneStateListener.LISTEN_MESSAGE_WAITING_INDICATOR
                | PhoneStateListener.LISTEN_CALL_FORWARDING_INDICATOR);
    }
    @Override
    public void handleMessage(Message msg) {
        switch (msg.what) {
            case PHONE_NEW_RINGING_CONNECTION:
                if (DBG) log("RINGING... (new)");
                onNewRingingConnection((AsyncResult) msg.obj);
                mSilentRingerRequested = false;
                break;
            case PHONE_INCOMING_RING:
                if (msg.obj != null && ((AsyncResult) msg.obj).result != null) {
                    PhoneBase pb =  (PhoneBase)((AsyncResult)msg.obj).result;
                    if ((pb.getState() == Phone.State.RINGING)
                            && (mSilentRingerRequested == false)) {
                        if (DBG) log("RINGING... (PHONE_INCOMING_RING event)");
                        mRinger.ring();
                    } else {
                        if (DBG) log("RING before NEW_RING, skipping");
                    }
                }
                break;
            case PHONE_STATE_CHANGED:
                onPhoneStateChanged((AsyncResult) msg.obj);
                break;
            case PHONE_DISCONNECT:
                if (DBG) log("DISCONNECT");
                onDisconnect((AsyncResult) msg.obj);
                break;
            case PHONE_UNKNOWN_CONNECTION_APPEARED:
                onUnknownConnectionAppeared((AsyncResult) msg.obj);
                break;
            case RINGER_CUSTOM_RINGTONE_QUERY_TIMEOUT:
                Log.w(LOG_TAG, "CallerInfo query took too long; manually starting ringer");
                onCustomRingQueryComplete();
                break;
            case PHONE_MWI_CHANGED:
                onMwiChanged(mPhone.getMessageWaitingIndicator());
                break;
            case PHONE_BATTERY_LOW:
                onBatteryLow();
                break;
            case PHONE_CDMA_CALL_WAITING:
                if (DBG) log("Received PHONE_CDMA_CALL_WAITING event");
                onCdmaCallWaiting((AsyncResult) msg.obj);
                break;
            case CDMA_CALL_WAITING_REJECT:
                Log.i(LOG_TAG, "Received CDMA_CALL_WAITING_REJECT event");
                onCdmaCallWaitingReject();
                break;
            case CALLWAITING_CALLERINFO_DISPLAY_DONE:
                Log.i(LOG_TAG, "Received CALLWAITING_CALLERINFO_DISPLAY_DONE event");
                mCallWaitingTimeOut = true;
                onCdmaCallWaitingReject();
                break;
            case CALLWAITING_ADDCALL_DISABLE_TIMEOUT:
                if (DBG) log("Received CALLWAITING_ADDCALL_DISABLE_TIMEOUT event ...");
                mApplication.cdmaPhoneCallState.setAddCallMenuStateAfterCallWaiting(true);
                mApplication.updateInCallScreenTouchUi();
                break;
            case PHONE_STATE_DISPLAYINFO:
                if (DBG) log("Received PHONE_STATE_DISPLAYINFO event");
                onDisplayInfo((AsyncResult) msg.obj);
                break;
            case PHONE_STATE_SIGNALINFO:
                if (DBG) log("Received PHONE_STATE_SIGNALINFO event");
                onSignalInfo((AsyncResult) msg.obj);
                break;
            case DISPLAYINFO_NOTIFICATION_DONE:
                if (DBG) log("Received Display Info notification done event ...");
                CdmaDisplayInfo.dismissDisplayInfoRecord();
                break;
            case EVENT_OTA_PROVISION_CHANGE:
                mApplication.handleOtaEvents(msg);
                break;
            case PHONE_ENHANCED_VP_ON:
                if (DBG) log("PHONE_ENHANCED_VP_ON...");
                if (!mCdmaVoicePrivacyState) {
                    int toneToPlay = InCallTonePlayer.TONE_VOICE_PRIVACY;
                    new InCallTonePlayer(toneToPlay).start();
                    mCdmaVoicePrivacyState = true;
                    NotificationMgr.getDefault().updateInCallNotification();
                }
                break;
            case PHONE_ENHANCED_VP_OFF:
                if (DBG) log("PHONE_ENHANCED_VP_OFF...");
                if (mCdmaVoicePrivacyState) {
                    int toneToPlay = InCallTonePlayer.TONE_VOICE_PRIVACY;
                    new InCallTonePlayer(toneToPlay).start();
                    mCdmaVoicePrivacyState = false;
                    NotificationMgr.getDefault().updateInCallNotification();
                }
                break;
            case PHONE_RINGBACK_TONE:
                onRingbackTone((AsyncResult) msg.obj);
                break;
            case PHONE_RESEND_MUTE:
                onResendMute();
                break;
            default:
        }
    }
    PhoneStateListener mPhoneStateListener = new PhoneStateListener() {
        @Override
        public void onMessageWaitingIndicatorChanged(boolean mwi) {
            onMwiChanged(mwi);
        }
        @Override
        public void onCallForwardingIndicatorChanged(boolean cfi) {
            onCfiChanged(cfi);
        }
    };
    private void onNewRingingConnection(AsyncResult r) {
        Connection c = (Connection) r.result;
        if (DBG) log("onNewRingingConnection(): " + c);
        boolean provisioned = Settings.Secure.getInt(mPhone.getContext().getContentResolver(),
            Settings.Secure.DEVICE_PROVISIONED, 0) != 0;
        if (!provisioned && !PhoneUtils.isPhoneInEcm(mPhone)) {
            Log.i(LOG_TAG, "CallNotifier: rejecting incoming call: not provisioned / ECM");
            PhoneUtils.hangupRingingCall(mPhone);
            return;
        }
        if (mPhone.getPhoneType() == Phone.PHONE_TYPE_CDMA) {
            boolean activateState = (mApplication.cdmaOtaScreenState.otaScreenState
                    == OtaUtils.CdmaOtaScreenState.OtaScreenState.OTA_STATUS_ACTIVATION);
            boolean dialogState = (mApplication.cdmaOtaScreenState.otaScreenState
                    == OtaUtils.CdmaOtaScreenState.OtaScreenState.OTA_STATUS_SUCCESS_FAILURE_DLG);
            boolean spcState = mApplication.cdmaOtaProvisionData.inOtaSpcState;
            if (spcState) {
                Log.i(LOG_TAG, "CallNotifier: rejecting incoming call: OTA call is active");
                PhoneUtils.hangupRingingCall(mPhone);
                return;
            } else if (activateState || dialogState) {
                if (dialogState) mApplication.dismissOtaDialogs();
                mApplication.clearOtaState();
                mApplication.clearInCallScreenMode();
            }
        }
        if (c != null && c.isRinging()) {
            if (mPhone.getPhoneType() == Phone.PHONE_TYPE_CDMA) {
                stopSignalInfoTone();
            }
            Call.State state = c.getState();
            if (VDBG) log("- connection is ringing!  state = " + state);
            if (VDBG) log("Holding wake lock on new incoming connection.");
            mApplication.requestWakeState(PhoneApp.WakeState.PARTIAL);
            if (state == Call.State.INCOMING) {
                PhoneUtils.setAudioControlState(PhoneUtils.AUDIO_RINGING);
                startIncomingCallQuery(c);
            } else {
                if (VDBG) log("- starting call waiting tone...");
                if (mCallWaitingTonePlayer == null) {
                    mCallWaitingTonePlayer = new InCallTonePlayer(InCallTonePlayer.TONE_CALL_WAITING);
                    mCallWaitingTonePlayer.start();
                }
                PhoneUtils.showIncomingCallUi();
            }
        }
        if (VDBG) log("- onNewRingingConnection() done.");
    }
    private void startIncomingCallQuery(Connection c) {
        boolean shouldStartQuery = false;
        synchronized (mCallerInfoQueryStateGuard) {
            if (mCallerInfoQueryState == CALLERINFO_QUERY_READY) {
                mCallerInfoQueryState = CALLERINFO_QUERYING;
                shouldStartQuery = true;
            }
        }
        if (shouldStartQuery) {
            mRinger.setCustomRingtoneUri(Settings.System.DEFAULT_RINGTONE_URI);
            PhoneUtils.CallerInfoToken cit = PhoneUtils.startGetCallerInfo(
                    mPhone.getContext(), c, this, this);
            if (cit.isFinal) {
                if (VDBG) log("- CallerInfo already up to date, using available data");
                onQueryComplete(0, this, cit.currentInfo);
            } else {
                if (VDBG) log("- Starting query, posting timeout message.");
                sendEmptyMessageDelayed(RINGER_CUSTOM_RINGTONE_QUERY_TIMEOUT,
                        RINGTONE_QUERY_WAIT_TIME);
            }
        } else {
            EventLog.writeEvent(EventLogTags.PHONE_UI_MULTIPLE_QUERY);
            if (VDBG) log("RINGING... (request to ring arrived while query is running)");
            mRinger.ring();
            PhoneUtils.showIncomingCallUi();
        }
    }
    private void onCustomRingQueryComplete() {
        boolean isQueryExecutionTimeExpired = false;
        synchronized (mCallerInfoQueryStateGuard) {
            if (mCallerInfoQueryState == CALLERINFO_QUERYING) {
                mCallerInfoQueryState = CALLERINFO_QUERY_READY;
                isQueryExecutionTimeExpired = true;
            }
        }
        if (isQueryExecutionTimeExpired) {
            Log.w(LOG_TAG, "CallerInfo query took too long; falling back to default ringtone");
            EventLog.writeEvent(EventLogTags.PHONE_UI_RINGER_QUERY_ELAPSED);
        }
        if (mPhone.getState() != Phone.State.RINGING) {
            Log.i(LOG_TAG, "onCustomRingQueryComplete: No incoming call! Bailing out...");
            return;
        }
        if (VDBG) log("RINGING... (onCustomRingQueryComplete)");
        mRinger.ring();
        PhoneUtils.showIncomingCallUi();
    }
    private void onUnknownConnectionAppeared(AsyncResult r) {
        Phone.State state = mPhone.getState();
        if (state == Phone.State.OFFHOOK) {
            onPhoneStateChanged(r);
            PhoneUtils.showIncomingCallUi();
        }
    }
    private void onPhoneStateChanged(AsyncResult r) {
        Phone.State state = mPhone.getState();
        if (VDBG) log("onPhoneStateChanged: state = " + state);
        NotificationMgr.getDefault().getStatusBarMgr()
                .enableNotificationAlerts(state == Phone.State.IDLE);
        if (mPhone.getPhoneType() == Phone.PHONE_TYPE_CDMA) {
            if ((mPhone.getForegroundCall().getState() == Call.State.ACTIVE)
                    && ((mPreviousCdmaCallState == Call.State.DIALING)
                    ||  (mPreviousCdmaCallState == Call.State.ALERTING))) {
                if (mIsCdmaRedialCall) {
                    int toneToPlay = InCallTonePlayer.TONE_REDIAL;
                    new InCallTonePlayer(toneToPlay).start();
                }
                stopSignalInfoTone();
            }
            mPreviousCdmaCallState = mPhone.getForegroundCall().getState();
        }
        mApplication.updateBluetoothIndication(false);
        mApplication.updatePhoneState(state);
        if (state == Phone.State.OFFHOOK) {
            if (mCallWaitingTonePlayer != null) {
                mCallWaitingTonePlayer.stopTone();
                mCallWaitingTonePlayer = null;
            }
            PhoneUtils.setAudioControlState(PhoneUtils.AUDIO_OFFHOOK);
            if (VDBG) log("onPhoneStateChanged: OFF HOOK");
            if (mPhone.getPhoneType() == Phone.PHONE_TYPE_CDMA) {
                if (mAudioManager.getMode() != AudioManager.MODE_IN_CALL) {
                    PhoneUtils.setAudioMode(mPhone.getContext(), AudioManager.MODE_IN_CALL);
                }
            }
            if (!mApplication.isShowingCallScreen()) {
                mApplication.setScreenTimeout(PhoneApp.ScreenTimeoutDuration.DEFAULT);
                mApplication.requestWakeState(PhoneApp.WakeState.SLEEP);
            }
            if (DBG) log("stopRing()... (OFFHOOK state)");
            mRinger.stopRing();
            NotificationMgr.getDefault().updateInCallNotification();
        }
        if (mPhone.getPhoneType() == Phone.PHONE_TYPE_CDMA) {
            Connection c = mPhone.getForegroundCall().getLatestConnection();
            if ((c != null) && (PhoneNumberUtils.isEmergencyNumber(c.getAddress()))) {
                if (VDBG) log("onPhoneStateChanged: it is an emergency call.");
                Call.State callState = mPhone.getForegroundCall().getState();
                if (mEmergencyTonePlayerVibrator == null) {
                    mEmergencyTonePlayerVibrator = new EmergencyTonePlayerVibrator();
                }
                if (callState == Call.State.DIALING || callState == Call.State.ALERTING) {
                    mIsEmergencyToneOn = Settings.System.getInt(
                            mPhone.getContext().getContentResolver(),
                            Settings.System.EMERGENCY_TONE, EMERGENCY_TONE_OFF);
                    if (mIsEmergencyToneOn != EMERGENCY_TONE_OFF &&
                        mCurrentEmergencyToneState == EMERGENCY_TONE_OFF) {
                        if (mEmergencyTonePlayerVibrator != null) {
                            mEmergencyTonePlayerVibrator.start();
                        }
                    }
                } else if (callState == Call.State.ACTIVE) {
                    if (mCurrentEmergencyToneState != EMERGENCY_TONE_OFF) {
                        if (mEmergencyTonePlayerVibrator != null) {
                            mEmergencyTonePlayerVibrator.stop();
                        }
                    }
                }
            }
        }
        if (mPhone.getPhoneType() == Phone.PHONE_TYPE_GSM) {
            Call.State callState = mPhone.getForegroundCall().getState();
            if (!callState.isDialing()) {
                if (mInCallRingbackTonePlayer != null) {
                    mInCallRingbackTonePlayer.stopTone();
                    mInCallRingbackTonePlayer = null;
                }
            }
        }
    }
    void updateCallNotifierRegistrationsAfterRadioTechnologyChange() {
        if (DBG) Log.d(LOG_TAG, "updateCallNotifierRegistrationsAfterRadioTechnologyChange...");
        mPhone.unregisterForNewRingingConnection(this);
        mPhone.unregisterForPreciseCallStateChanged(this);
        mPhone.unregisterForDisconnect(this);
        mPhone.unregisterForUnknownConnection(this);
        mPhone.unregisterForIncomingRing(this);
        mPhone.unregisterForCallWaiting(this);
        mPhone.unregisterForDisplayInfo(this);
        mPhone.unregisterForSignalInfo(this);
        mPhone.unregisterForCdmaOtaStatusChange(this);
        mPhone.unregisterForRingbackTone(this);
        mPhone.unregisterForResendIncallMute(this);
        if (mSignalInfoToneGenerator != null) {
            mSignalInfoToneGenerator.release();
        }
        mInCallRingbackTonePlayer = null;
        mCallWaitingTonePlayer = null;
        mPhone.unregisterForInCallVoicePrivacyOn(this);
        mPhone.unregisterForInCallVoicePrivacyOff(this);
        mPhone.registerForNewRingingConnection(this, PHONE_NEW_RINGING_CONNECTION, null);
        mPhone.registerForPreciseCallStateChanged(this, PHONE_STATE_CHANGED, null);
        mPhone.registerForDisconnect(this, PHONE_DISCONNECT, null);
        mPhone.registerForUnknownConnection(this, PHONE_UNKNOWN_CONNECTION_APPEARED, null);
        mPhone.registerForIncomingRing(this, PHONE_INCOMING_RING, null);
        if (mPhone.getPhoneType() == Phone.PHONE_TYPE_CDMA) {
            if (DBG) log("Registering for Call Waiting, Signal and Display Info.");
            mPhone.registerForCallWaiting(this, PHONE_CDMA_CALL_WAITING, null);
            mPhone.registerForDisplayInfo(this, PHONE_STATE_DISPLAYINFO, null);
            mPhone.registerForSignalInfo(this, PHONE_STATE_SIGNALINFO, null);
            mPhone.registerForCdmaOtaStatusChange(this, EVENT_OTA_PROVISION_CHANGE, null);
            try {
                mSignalInfoToneGenerator = new ToneGenerator(AudioManager.STREAM_VOICE_CALL,
                        TONE_RELATIVE_VOLUME_SIGNALINFO);
            } catch (RuntimeException e) {
                Log.w(LOG_TAG, "CallNotifier: Exception caught while creating " +
                        "mSignalInfoToneGenerator: " + e);
                mSignalInfoToneGenerator = null;
            }
            mPhone.registerForInCallVoicePrivacyOn(this, PHONE_ENHANCED_VP_ON, null);
            mPhone.registerForInCallVoicePrivacyOff(this, PHONE_ENHANCED_VP_OFF, null);
        }
        if (mPhone.getPhoneType() == Phone.PHONE_TYPE_GSM) {
            mPhone.registerForRingbackTone(this, PHONE_RINGBACK_TONE, null);
            mPhone.registerForResendIncallMute(this, PHONE_RESEND_MUTE, null);
        }
    }
    public void onQueryComplete(int token, Object cookie, CallerInfo ci) {
        if (cookie instanceof Long) {
            if (VDBG) log("CallerInfo query complete, posting missed call notification");
            NotificationMgr.getDefault().notifyMissedCall(ci.name, ci.phoneNumber,
                    ci.phoneLabel, ((Long) cookie).longValue());
        } else if (cookie instanceof CallNotifier) {
            if (VDBG) log("CallerInfo query complete, updating data");
            removeMessages(RINGER_CUSTOM_RINGTONE_QUERY_TIMEOUT);
            boolean isQueryExecutionTimeOK = false;
            synchronized (mCallerInfoQueryStateGuard) {
                if (mCallerInfoQueryState == CALLERINFO_QUERYING) {
                    mCallerInfoQueryState = CALLERINFO_QUERY_READY;
                    isQueryExecutionTimeOK = true;
                }
            }
            if (isQueryExecutionTimeOK) {
                if (ci.shouldSendToVoicemail) {
                    if (DBG) log("send to voicemail flag detected. hanging up.");
                    PhoneUtils.hangupRingingCall(mPhone);
                    return;
                }
                if (ci.contactRingtoneUri != null) {
                    if (DBG) log("custom ringtone found, setting up ringer.");
                    Ringer r = ((CallNotifier) cookie).mRinger;
                    r.setCustomRingtoneUri(ci.contactRingtoneUri);
                }
                onCustomRingQueryComplete();
            }
        }
    }
    private void onDisconnect(AsyncResult r) {
        if (VDBG) log("onDisconnect()...  phone state: " + mPhone.getState());
        mCdmaVoicePrivacyState = false;
        int autoretrySetting = 0;
        if (mPhone.getPhoneType() == Phone.PHONE_TYPE_CDMA) {
            autoretrySetting = android.provider.Settings.System.getInt(mPhone.getContext().
                    getContentResolver(),android.provider.Settings.System.CALL_AUTO_RETRY, 0);
        }
        if (mPhone.getState() == Phone.State.IDLE) {
            PhoneUtils.setAudioControlState(PhoneUtils.AUDIO_IDLE);
        }
        if (mPhone.getPhoneType() == Phone.PHONE_TYPE_CDMA) {
            stopSignalInfoTone();
            mApplication.cdmaPhoneCallState.resetCdmaPhoneCallState();
            removeMessages(CALLWAITING_CALLERINFO_DISPLAY_DONE);
            removeMessages(CALLWAITING_ADDCALL_DISABLE_TIMEOUT);
        }
        Connection c = (Connection) r.result;
        if (DBG && c != null) {
            log("- onDisconnect: cause = " + c.getDisconnectCause()
                + ", incoming = " + c.isIncoming()
                + ", date = " + c.getCreateTime());
        }
        if (mPhone.getPhoneType() == Phone.PHONE_TYPE_CDMA) {
            if (mPhone.getRingingCall().getState() == Call.State.INCOMING) {
                if (DBG) log("cancelCallInProgressNotification()... (onDisconnect)");
                NotificationMgr.getDefault().cancelCallInProgressNotification();
            } else {
                if (DBG) log("stopRing()... (onDisconnect)");
                mRinger.stopRing();
            }
        } else { 
            if (DBG) log("stopRing()... (onDisconnect)");
            mRinger.stopRing();
        }
        if (mCallWaitingTonePlayer != null) {
            mCallWaitingTonePlayer.stopTone();
            mCallWaitingTonePlayer = null;
        }
        int toneToPlay = InCallTonePlayer.TONE_NONE;
        if (c != null) {
            Connection.DisconnectCause cause = c.getDisconnectCause();
            if (cause == Connection.DisconnectCause.BUSY) {
                if (DBG) log("- need to play BUSY tone!");
                toneToPlay = InCallTonePlayer.TONE_BUSY;
            } else if (cause == Connection.DisconnectCause.CONGESTION) {
                if (DBG) log("- need to play CONGESTION tone!");
                toneToPlay = InCallTonePlayer.TONE_CONGESTION;
            } else if (((cause == Connection.DisconnectCause.NORMAL)
                    || (cause == Connection.DisconnectCause.LOCAL))
                    && (mApplication.isOtaCallInActiveState())) {
                if (DBG) log("- need to play OTA_CALL_END tone!");
                toneToPlay = InCallTonePlayer.TONE_OTA_CALL_END;
            } else if (cause == Connection.DisconnectCause.CDMA_REORDER) {
                if (DBG) log("- need to play CDMA_REORDER tone!");
                toneToPlay = InCallTonePlayer.TONE_REORDER;
            } else if (cause == Connection.DisconnectCause.CDMA_INTERCEPT) {
                if (DBG) log("- need to play CDMA_INTERCEPT tone!");
                toneToPlay = InCallTonePlayer.TONE_INTERCEPT;
            } else if (cause == Connection.DisconnectCause.CDMA_DROP) {
                if (DBG) log("- need to play CDMA_DROP tone!");
                toneToPlay = InCallTonePlayer.TONE_CDMA_DROP;
            } else if (cause == Connection.DisconnectCause.OUT_OF_SERVICE) {
                if (DBG) log("- need to play OUT OF SERVICE tone!");
                toneToPlay = InCallTonePlayer.TONE_OUT_OF_SERVICE;
            } else if (cause == Connection.DisconnectCause.ERROR_UNSPECIFIED) {
                if (DBG) log("- DisconnectCause is ERROR_UNSPECIFIED: play TONE_CALL_ENDED!");
                toneToPlay = InCallTonePlayer.TONE_CALL_ENDED;
            }
        }
        if ((toneToPlay == InCallTonePlayer.TONE_NONE)
            && (mPhone.getState() == Phone.State.IDLE)
            && (c != null)) {
            Connection.DisconnectCause cause = c.getDisconnectCause();
            if ((cause == Connection.DisconnectCause.NORMAL)  
                || (cause == Connection.DisconnectCause.LOCAL)) {  
                if (VDBG) log("- need to play CALL_ENDED tone!");
                toneToPlay = InCallTonePlayer.TONE_CALL_ENDED;
                mIsCdmaRedialCall = false;
            }
        }
        if (mPhone.getState() == Phone.State.IDLE) {
            if (toneToPlay == InCallTonePlayer.TONE_NONE) {
                resetAudioStateAfterDisconnect();
            }
            NotificationMgr.getDefault().cancelCallInProgressNotification();
            if (!mApplication.isShowingCallScreen()) {
                if (VDBG) log("onDisconnect: force InCallScreen to finish()");
                mApplication.dismissCallScreen();
            } else {
                if (VDBG) log("onDisconnect: In call screen. Set short timeout.");
                mApplication.clearUserActivityTimeout();
            }
        }
        if (c != null) {
            final String number = c.getAddress();
            final long date = c.getCreateTime();
            final long duration = c.getDurationMillis();
            final Connection.DisconnectCause cause = c.getDisconnectCause();
            final int callLogType;
            if (c.isIncoming()) {
                callLogType = (cause == Connection.DisconnectCause.INCOMING_MISSED ?
                               Calls.MISSED_TYPE : Calls.INCOMING_TYPE);
            } else {
                callLogType = Calls.OUTGOING_TYPE;
            }
            if (VDBG) log("- callLogType: " + callLogType + ", UserData: " + c.getUserData());
            {
                final CallerInfo ci = getCallerInfoFromConnection(c);  
                final String logNumber = getLogNumber(c, ci);
                if (DBG) log("- onDisconnect(): logNumber set to: " + logNumber);
                final int presentation = getPresentation(c, ci);
                if (mPhone.getPhoneType() == Phone.PHONE_TYPE_CDMA) {
                    if ((PhoneNumberUtils.isEmergencyNumber(number))
                            && (mCurrentEmergencyToneState != EMERGENCY_TONE_OFF)) {
                        if (mEmergencyTonePlayerVibrator != null) {
                            mEmergencyTonePlayerVibrator.stop();
                        }
                    }
                }
                final boolean shouldNotlogEmergencyNumber =
                        (mPhone.getPhoneType() == Phone.PHONE_TYPE_CDMA);
                final boolean isOtaNumber = (mPhone.getPhoneType() == Phone.PHONE_TYPE_CDMA)
                        && mPhone.isOtaSpNumber(number);
                final boolean isEmergencyNumber = PhoneNumberUtils.isEmergencyNumber(number);
                if (!(isOtaNumber || isEmergencyNumber && shouldNotlogEmergencyNumber)) {
                    CallLogAsync.AddCallArgs args =
                            new CallLogAsync.AddCallArgs(
                                mPhone.getContext(), ci, logNumber, presentation,
                                callLogType, date, duration);
                    mCallLog.addCall(args);
                }
            }
            if (callLogType == Calls.MISSED_TYPE) {
                showMissedCallNotification(c, date);
            }
            if (toneToPlay != InCallTonePlayer.TONE_NONE) {
                if (VDBG) log("- starting post-disconnect tone (" + toneToPlay + ")...");
                new InCallTonePlayer(toneToPlay).start();
            }
            if (mPhone.getState() == Phone.State.IDLE) {
                if (!mApplication.isShowingCallScreen()) {
                    if (VDBG) log("- NOT showing in-call screen; releasing wake locks!");
                    mApplication.setScreenTimeout(PhoneApp.ScreenTimeoutDuration.DEFAULT);
                    mApplication.requestWakeState(PhoneApp.WakeState.SLEEP);
                } else {
                    if (VDBG) log("- still showing in-call screen; not releasing wake locks.");
                }
            } else {
                if (VDBG) log("- phone still in use; not releasing wake locks.");
            }
            if (((mPreviousCdmaCallState == Call.State.DIALING)
                    || (mPreviousCdmaCallState == Call.State.ALERTING))
                    && (!PhoneNumberUtils.isEmergencyNumber(number))
                    && (cause != Connection.DisconnectCause.INCOMING_MISSED )
                    && (cause != Connection.DisconnectCause.NORMAL)
                    && (cause != Connection.DisconnectCause.LOCAL)
                    && (cause != Connection.DisconnectCause.INCOMING_REJECTED)) {
                if (!mIsCdmaRedialCall) {
                    if (autoretrySetting == InCallScreen.AUTO_RETRY_ON) {
                        PhoneUtils.placeCall(mPhone, number, null);
                        mIsCdmaRedialCall = true;
                    } else {
                        mIsCdmaRedialCall = false;
                    }
                } else {
                    mIsCdmaRedialCall = false;
                }
            }
        }
    }
    private void resetAudioStateAfterDisconnect() {
        if (VDBG) log("resetAudioStateAfterDisconnect()...");
        if (mBluetoothHandsfree != null) {
            mBluetoothHandsfree.audioOff();
        }
        PhoneUtils.turnOnSpeaker(mPhone.getContext(), false, true);
        PhoneUtils.setAudioMode(mPhone.getContext(), AudioManager.MODE_NORMAL);
    }
    private void onMwiChanged(boolean visible) {
        if (VDBG) log("onMwiChanged(): " + visible);
        NotificationMgr.getDefault().updateMwi(visible);
    }
     void sendMwiChangedDelayed(long delayMillis) {
        Message message = Message.obtain(this, PHONE_MWI_CHANGED);
        sendMessageDelayed(message, delayMillis);
    }
    private void onCfiChanged(boolean visible) {
        if (VDBG) log("onCfiChanged(): " + visible);
        NotificationMgr.getDefault().updateCfi(visible);
    }
    boolean isRinging() {
        return mRinger.isRinging();
    }
    void silenceRinger() {
        mSilentRingerRequested = true;
        if (DBG) log("stopRing()... (silenceRinger)");
        mRinger.stopRing();
    }
     void sendBatteryLow() {
        Message message = Message.obtain(this, PHONE_BATTERY_LOW);
        sendMessage(message);
    }
    private void onBatteryLow() {
        if (DBG) log("onBatteryLow()...");
    }
    private class InCallTonePlayer extends Thread {
        private int mToneId;
        private int mState;
        public static final int TONE_NONE = 0;
        public static final int TONE_CALL_WAITING = 1;
        public static final int TONE_BUSY = 2;
        public static final int TONE_CONGESTION = 3;
        public static final int TONE_BATTERY_LOW = 4;
        public static final int TONE_CALL_ENDED = 5;
        public static final int TONE_VOICE_PRIVACY = 6;
        public static final int TONE_REORDER = 7;
        public static final int TONE_INTERCEPT = 8;
        public static final int TONE_CDMA_DROP = 9;
        public static final int TONE_OUT_OF_SERVICE = 10;
        public static final int TONE_REDIAL = 11;
        public static final int TONE_OTA_CALL_END = 12;
        public static final int TONE_RING_BACK = 13;
        private static final int TONE_RELATIVE_VOLUME_HIPRI = 80;
        private static final int TONE_RELATIVE_VOLUME_LOPRI = 50;
        private static final int TONE_TIMEOUT_BUFFER = 20;
        private static final int TONE_OFF = 0;
        private static final int TONE_ON = 1;
        private static final int TONE_STOPPED = 2;
        InCallTonePlayer(int toneId) {
            super();
            mToneId = toneId;
            mState = TONE_OFF;
        }
        @Override
        public void run() {
            if (VDBG) log("InCallTonePlayer.run(toneId = " + mToneId + ")...");
            int toneType = 0;  
            int toneVolume;  
            int toneLengthMillis;
            switch (mToneId) {
                case TONE_CALL_WAITING:
                    toneType = ToneGenerator.TONE_SUP_CALL_WAITING;
                    toneVolume = TONE_RELATIVE_VOLUME_HIPRI;
                    toneLengthMillis = Integer.MAX_VALUE - TONE_TIMEOUT_BUFFER;
                    break;
                case TONE_BUSY:
                    int phoneType = mPhone.getPhoneType();
                    if (phoneType == Phone.PHONE_TYPE_CDMA) {
                        toneType = ToneGenerator.TONE_CDMA_NETWORK_BUSY_ONE_SHOT;
                        toneVolume = TONE_RELATIVE_VOLUME_LOPRI;
                        toneLengthMillis = 1000;
                    } else if (phoneType == Phone.PHONE_TYPE_GSM) {
                        toneType = ToneGenerator.TONE_SUP_BUSY;
                        toneVolume = TONE_RELATIVE_VOLUME_HIPRI;
                        toneLengthMillis = 4000;
                    } else {
                        throw new IllegalStateException("Unexpected phone type: " + phoneType);
                    }
                    break;
                case TONE_CONGESTION:
                    toneType = ToneGenerator.TONE_SUP_CONGESTION;
                    toneVolume = TONE_RELATIVE_VOLUME_HIPRI;
                    toneLengthMillis = 4000;
                    break;
                case TONE_BATTERY_LOW:
                    toneType = ToneGenerator.TONE_PROP_ACK;
                    toneVolume = TONE_RELATIVE_VOLUME_HIPRI;
                    toneLengthMillis = 1000;
                    break;
                case TONE_CALL_ENDED:
                    toneType = ToneGenerator.TONE_PROP_PROMPT;
                    toneVolume = TONE_RELATIVE_VOLUME_HIPRI;
                    toneLengthMillis = 200;
                    break;
                 case TONE_OTA_CALL_END:
                    if (mApplication.cdmaOtaConfigData.otaPlaySuccessFailureTone ==
                            OtaUtils.OTA_PLAY_SUCCESS_FAILURE_TONE_ON) {
                        toneType = ToneGenerator.TONE_CDMA_ALERT_CALL_GUARD;
                        toneVolume = TONE_RELATIVE_VOLUME_HIPRI;
                        toneLengthMillis = 750;
                    } else {
                        toneType = ToneGenerator.TONE_PROP_PROMPT;
                        toneVolume = TONE_RELATIVE_VOLUME_HIPRI;
                        toneLengthMillis = 200;
                    }
                    break;
                case TONE_VOICE_PRIVACY:
                    toneType = ToneGenerator.TONE_CDMA_ALERT_NETWORK_LITE;
                    toneVolume = TONE_RELATIVE_VOLUME_HIPRI;
                    toneLengthMillis = 5000;
                    break;
                case TONE_REORDER:
                    toneType = ToneGenerator.TONE_CDMA_ABBR_REORDER;
                    toneVolume = TONE_RELATIVE_VOLUME_LOPRI;
                    toneLengthMillis = 4000;
                    break;
                case TONE_INTERCEPT:
                    toneType = ToneGenerator.TONE_CDMA_ABBR_INTERCEPT;
                    toneVolume = TONE_RELATIVE_VOLUME_LOPRI;
                    toneLengthMillis = 500;
                    break;
                case TONE_CDMA_DROP:
                case TONE_OUT_OF_SERVICE:
                    toneType = ToneGenerator.TONE_CDMA_CALLDROP_LITE;
                    toneVolume = TONE_RELATIVE_VOLUME_LOPRI;
                    toneLengthMillis = 375;
                    break;
                case TONE_REDIAL:
                    toneType = ToneGenerator.TONE_CDMA_ALERT_AUTOREDIAL_LITE;
                    toneVolume = TONE_RELATIVE_VOLUME_LOPRI;
                    toneLengthMillis = 5000;
                    break;
                case TONE_RING_BACK:
                    toneType = ToneGenerator.TONE_SUP_RINGTONE;
                    toneVolume = TONE_RELATIVE_VOLUME_HIPRI;
                    toneLengthMillis = Integer.MAX_VALUE - TONE_TIMEOUT_BUFFER;
                    break;
                default:
                    throw new IllegalArgumentException("Bad toneId: " + mToneId);
            }
            ToneGenerator toneGenerator;
            try {
                int stream;
                if (mBluetoothHandsfree != null) {
                    stream = mBluetoothHandsfree.isAudioOn() ? AudioManager.STREAM_BLUETOOTH_SCO:
                        AudioManager.STREAM_VOICE_CALL;
                } else {
                    stream = AudioManager.STREAM_VOICE_CALL;
                }
                toneGenerator = new ToneGenerator(stream, toneVolume);
            } catch (RuntimeException e) {
                Log.w(LOG_TAG,
                      "InCallTonePlayer: Exception caught while creating ToneGenerator: " + e);
                toneGenerator = null;
            }
            boolean needToStopTone = true;
            boolean okToPlayTone = false;
            if (toneGenerator != null) {
                int phoneType = mPhone.getPhoneType();
                int ringerMode = mAudioManager.getRingerMode();
                if (phoneType == Phone.PHONE_TYPE_CDMA) {
                    if (toneType == ToneGenerator.TONE_CDMA_ALERT_CALL_GUARD) {
                        if ((ringerMode != AudioManager.RINGER_MODE_SILENT) &&
                                (ringerMode != AudioManager.RINGER_MODE_VIBRATE)) {
                            if (DBG) log("- InCallTonePlayer: start playing call tone=" + toneType);
                            okToPlayTone = true;
                            needToStopTone = false;
                        }
                    } else if ((toneType == ToneGenerator.TONE_CDMA_NETWORK_BUSY_ONE_SHOT) ||
                            (toneType == ToneGenerator.TONE_CDMA_ABBR_REORDER) ||
                            (toneType == ToneGenerator.TONE_CDMA_ABBR_INTERCEPT) ||
                            (toneType == ToneGenerator.TONE_CDMA_CALLDROP_LITE)) {
                        if (ringerMode != AudioManager.RINGER_MODE_SILENT) {
                            if (DBG) log("InCallTonePlayer:playing call fail tone:" + toneType);
                            okToPlayTone = true;
                            needToStopTone = false;
                        }
                    } else if ((toneType == ToneGenerator.TONE_CDMA_ALERT_AUTOREDIAL_LITE) ||
                               (toneType == ToneGenerator.TONE_CDMA_ALERT_NETWORK_LITE)) {
                        if ((ringerMode != AudioManager.RINGER_MODE_SILENT) &&
                                (ringerMode != AudioManager.RINGER_MODE_VIBRATE)) {
                            if (DBG) log("InCallTonePlayer:playing tone for toneType=" + toneType);
                            okToPlayTone = true;
                            needToStopTone = false;
                        }
                    } else { 
                        okToPlayTone = true;
                    }
                } else {  
                    okToPlayTone = true;
                }
                synchronized (this) {
                    if (okToPlayTone && mState != TONE_STOPPED) {
                        mState = TONE_ON;
                        toneGenerator.startTone(toneType);
                        try {
                            wait(toneLengthMillis + TONE_TIMEOUT_BUFFER);
                        } catch  (InterruptedException e) {
                            Log.w(LOG_TAG,
                                  "InCallTonePlayer stopped: " + e);
                        }
                        if (needToStopTone) {
                            toneGenerator.stopTone();
                        }
                    }
                    toneGenerator.release();
                    mState = TONE_OFF;
                }
            }
            if (mPhone.getState() == Phone.State.IDLE) {
                resetAudioStateAfterDisconnect();
            }
        }
        public void stopTone() {
            synchronized (this) {
                if (mState == TONE_ON) {
                    notify();
                }
                mState = TONE_STOPPED;
            }
        }
    }
    private void onDisplayInfo(AsyncResult r) {
        CdmaDisplayInfoRec displayInfoRec = (CdmaDisplayInfoRec)(r.result);
        if (displayInfoRec != null) {
            String displayInfo = displayInfoRec.alpha;
            if (DBG) log("onDisplayInfo: displayInfo=" + displayInfo);
            CdmaDisplayInfo.displayInfoRecord(mApplication, displayInfo);
            sendEmptyMessageDelayed(DISPLAYINFO_NOTIFICATION_DONE,
                    DISPLAYINFO_NOTIFICATION_TIME);
        }
    }
    private class SignalInfoTonePlayer extends Thread {
        private int mToneId;
        SignalInfoTonePlayer(int toneId) {
            super();
            mToneId = toneId;
        }
        @Override
        public void run() {
            if (DBG) log("SignalInfoTonePlayer.run(toneId = " + mToneId + ")...");
            if (mSignalInfoToneGenerator != null) {
                mSignalInfoToneGenerator.stopTone();
                mSignalInfoToneGenerator.startTone(mToneId);
            }
        }
    }
    private void onSignalInfo(AsyncResult r) {
        if (mPhone.getRingingCall().getState() == Call.State.INCOMING) {
            stopSignalInfoTone();
        } else {
            CdmaSignalInfoRec signalInfoRec = (CdmaSignalInfoRec)(r.result);
            if (signalInfoRec != null) {
                boolean isPresent = signalInfoRec.isPresent;
                if (DBG) log("onSignalInfo: isPresent=" + isPresent);
                if (isPresent) {
                    int uSignalType = signalInfoRec.signalType;
                    int uAlertPitch = signalInfoRec.alertPitch;
                    int uSignal = signalInfoRec.signal;
                    if (DBG) log("onSignalInfo: uSignalType=" + uSignalType + ", uAlertPitch=" +
                            uAlertPitch + ", uSignal=" + uSignal);
                    int toneID = SignalToneUtil.getAudioToneFromSignalInfo
                            (uSignalType, uAlertPitch, uSignal);
                    new SignalInfoTonePlayer(toneID).start();
                }
            }
        }
    }
     void stopSignalInfoTone() {
        if (DBG) log("stopSignalInfoTone: Stopping SignalInfo tone player");
        new SignalInfoTonePlayer(ToneGenerator.TONE_CDMA_SIGNAL_OFF).start();
    }
    private void onCdmaCallWaiting(AsyncResult r) {
        removeMessages(CALLWAITING_CALLERINFO_DISPLAY_DONE);
        removeMessages(CALLWAITING_ADDCALL_DISABLE_TIMEOUT);
        mApplication.cdmaPhoneCallState.setCurrentCallState(
                CdmaPhoneCallState.PhoneCallState.SINGLE_ACTIVE);
        if (!mApplication.isShowingCallScreen()) {
            PhoneUtils.showIncomingCallUi();
        }
        mCallWaitingTimeOut = false;
        sendEmptyMessageDelayed(CALLWAITING_CALLERINFO_DISPLAY_DONE,
                CALLWAITING_CALLERINFO_DISPLAY_TIME);
        mApplication.cdmaPhoneCallState.setAddCallMenuStateAfterCallWaiting(false);
        sendEmptyMessageDelayed(CALLWAITING_ADDCALL_DISABLE_TIMEOUT,
                CALLWAITING_ADDCALL_DISABLE_TIME);
        CdmaCallWaitingNotification infoCW = (CdmaCallWaitingNotification) r.result;
        int isPresent = infoCW.isPresent;
        if (DBG) log("onCdmaCallWaiting: isPresent=" + isPresent);
        if (isPresent == 1 ) {
            int uSignalType = infoCW.signalType;
            int uAlertPitch = infoCW.alertPitch;
            int uSignal = infoCW.signal;
            if (DBG) log("onCdmaCallWaiting: uSignalType=" + uSignalType + ", uAlertPitch="
                    + uAlertPitch + ", uSignal=" + uSignal);
            int toneID =
                SignalToneUtil.getAudioToneFromSignalInfo(uSignalType, uAlertPitch, uSignal);
            new SignalInfoTonePlayer(toneID).start();
        }
    }
     void sendCdmaCallWaitingReject() {
        sendEmptyMessage(CDMA_CALL_WAITING_REJECT);
    }
    private void onCdmaCallWaitingReject() {
        final Call ringingCall = mPhone.getRingingCall();
        if (ringingCall.getState() == Call.State.WAITING) {
            Connection c = ringingCall.getLatestConnection();
            if (c != null) {
                String number = c.getAddress();
                int presentation = c.getNumberPresentation();
                final long date = c.getCreateTime();
                final long duration = c.getDurationMillis();
                final int callLogType = mCallWaitingTimeOut ?
                        Calls.MISSED_TYPE : Calls.INCOMING_TYPE;
                Object o = c.getUserData();
                final CallerInfo ci;
                if ((o == null) || (o instanceof CallerInfo)) {
                    ci = (CallerInfo) o;
                } else {
                    ci = ((PhoneUtils.CallerInfoToken) o).currentInfo;
                }
                final String logNumber = PhoneUtils.modifyForSpecialCnapCases(
                        mPhone.getContext(), ci, number, presentation);
                final int newPresentation = (ci != null) ? ci.numberPresentation : presentation;
                if (DBG) log("- onCdmaCallWaitingReject(): logNumber set to: " + logNumber
                        + ", newPresentation value is: " + newPresentation);
                CallLogAsync.AddCallArgs args =
                        new CallLogAsync.AddCallArgs(
                            mPhone.getContext(), ci, logNumber, presentation,
                            callLogType, date, duration);
                mCallLog.addCall(args);
                if (callLogType == Calls.MISSED_TYPE) {
                    showMissedCallNotification(c, date);
                } else {
                    removeMessages(CALLWAITING_CALLERINFO_DISPLAY_DONE);
                }
                PhoneUtils.hangup(c);
            }
            mCallWaitingTimeOut = false;
        }
    }
     Call.State getPreviousCdmaCallState() {
        return mPreviousCdmaCallState;
    }
     boolean getCdmaVoicePrivacyState() {
        return mCdmaVoicePrivacyState;
    }
     boolean getIsCdmaRedialCall() {
        return mIsCdmaRedialCall;
    }
    private void showMissedCallNotification(Connection c, final long date) {
        PhoneUtils.CallerInfoToken info =
            PhoneUtils.startGetCallerInfo(mApplication, c, this, Long.valueOf(date));
        if (info != null) {
            if (VDBG) log("showMissedCallNotification: Querying for CallerInfo on missed call...");
            if (info.isFinal) {
                CallerInfo ci = info.currentInfo;
                String name = ci.name;
                String number = ci.phoneNumber;
                if (ci.numberPresentation == Connection.PRESENTATION_RESTRICTED) {
                    name = mPhone.getContext().getString(R.string.private_num);
                } else if (ci.numberPresentation != Connection.PRESENTATION_ALLOWED) {
                    name = mPhone.getContext().getString(R.string.unknown);
                } else {
                    number = PhoneUtils.modifyForSpecialCnapCases(mPhone.getContext(),
                            ci, number, ci.numberPresentation);
                }
                NotificationMgr.getDefault().notifyMissedCall(name, number,
                        ci.phoneLabel, date);
            }
        } else {
            Log.w(LOG_TAG, "showMissedCallNotification: got null CallerInfo for Connection " + c);
        }
    }
    private class EmergencyTonePlayerVibrator {
        private final int EMG_VIBRATE_LENGTH = 1000;  
        private final int EMG_VIBRATE_PAUSE  = 1000;  
        private final long[] mVibratePattern =
                new long[] { EMG_VIBRATE_LENGTH, EMG_VIBRATE_PAUSE };
        private ToneGenerator mToneGenerator;
        private Vibrator mEmgVibrator;
        public EmergencyTonePlayerVibrator() {
        }
        private void start() {
            if (VDBG) log("call startEmergencyToneOrVibrate.");
            int ringerMode = mAudioManager.getRingerMode();
            if ((mIsEmergencyToneOn == EMERGENCY_TONE_ALERT) &&
                    (ringerMode == AudioManager.RINGER_MODE_NORMAL)) {
                if (VDBG) log("Play Emergency Tone.");
                mToneGenerator = new ToneGenerator (AudioManager.STREAM_VOICE_CALL,
                        InCallTonePlayer.TONE_RELATIVE_VOLUME_HIPRI);
                if (mToneGenerator != null) {
                    mToneGenerator.startTone(ToneGenerator.TONE_CDMA_EMERGENCY_RINGBACK);
                    mCurrentEmergencyToneState = EMERGENCY_TONE_ALERT;
                }
            } else if (mIsEmergencyToneOn == EMERGENCY_TONE_VIBRATE) {
                if (VDBG) log("Play Emergency Vibrate.");
                mEmgVibrator = new Vibrator();
                if (mEmgVibrator != null) {
                    mEmgVibrator.vibrate(mVibratePattern, 0);
                    mCurrentEmergencyToneState = EMERGENCY_TONE_VIBRATE;
                }
            }
        }
        private void stop() {
            if (VDBG) log("call stopEmergencyToneOrVibrate.");
            if ((mCurrentEmergencyToneState == EMERGENCY_TONE_ALERT)
                    && (mToneGenerator != null)) {
                mToneGenerator.stopTone();
                mToneGenerator.release();
            } else if ((mCurrentEmergencyToneState == EMERGENCY_TONE_VIBRATE)
                    && (mEmgVibrator != null)) {
                mEmgVibrator.cancel();
            }
            mCurrentEmergencyToneState = EMERGENCY_TONE_OFF;
        }
    }
    private void onRingbackTone(AsyncResult r) {
        boolean playTone = (Boolean)(r.result);
        if (playTone == true) {
            if (mPhone.getForegroundCall().getState().isDialing() &&
                mInCallRingbackTonePlayer == null) {
                mInCallRingbackTonePlayer = new InCallTonePlayer(InCallTonePlayer.TONE_RING_BACK);
                mInCallRingbackTonePlayer.start();
            }
        } else {
            if (mInCallRingbackTonePlayer != null) {
                mInCallRingbackTonePlayer.stopTone();
                mInCallRingbackTonePlayer = null;
            }
        }
    }
    private void onResendMute() {
        boolean muteState = PhoneUtils.getMute(mPhone);
        PhoneUtils.setMuteInternal(mPhone, !muteState);
        PhoneUtils.setMuteInternal(mPhone, muteState);
    }
    private String getLogNumber(Connection conn, CallerInfo callerInfo) {
        String number = null;
        if (conn.isIncoming()) {
            number = conn.getAddress();
        } else {
            if (null == callerInfo || TextUtils.isEmpty(callerInfo.phoneNumber) ||
                callerInfo.isEmergencyNumber() || callerInfo.isVoiceMailNumber()) {
                if (mPhone.getPhoneType() == Phone.PHONE_TYPE_CDMA) {
                    number = conn.getOrigDialString();
                } else {
                    number = conn.getAddress();
                }
            } else {
                number = callerInfo.phoneNumber;
            }
        }
        if (null == number) {
            return null;
        } else {
            int presentation = conn.getNumberPresentation();
            number = PhoneUtils.modifyForSpecialCnapCases(mPhone.getContext(), callerInfo,
                                                          number, presentation);
            number = PhoneNumberUtils.stripSeparators(number);
            if (VDBG) log("getLogNumber: " + number);
            return number;
        }
    }
    private CallerInfo getCallerInfoFromConnection(Connection conn) {
        CallerInfo ci = null;
        Object o = conn.getUserData();
        if ((o == null) || (o instanceof CallerInfo)) {
            ci = (CallerInfo) o;
        } else {
            ci = ((PhoneUtils.CallerInfoToken) o).currentInfo;
        }
        return ci;
    }
    private int getPresentation(Connection conn, CallerInfo callerInfo) {
        int presentation;
        if (null == callerInfo) {
            presentation = conn.getNumberPresentation();
        } else {
            presentation = callerInfo.numberPresentation;
            if (DBG) log("- getPresentation(): ignoring connection's presentation: " +
                         conn.getNumberPresentation());
        }
        if (DBG) log("- getPresentation: presentation: " + presentation);
        return presentation;
    }
    private void log(String msg) {
        Log.d(LOG_TAG, msg);
    }
}
