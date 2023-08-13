public class BluetoothHandsfree {
    private static final String TAG = "BT HS/HF";
    private static final boolean DBG = (PhoneApp.DBG_LEVEL >= 1)
            && (SystemProperties.getInt("ro.debuggable", 0) == 1);
    private static final boolean VDBG = (PhoneApp.DBG_LEVEL >= 2);  
    public static final int TYPE_UNKNOWN           = 0;
    public static final int TYPE_HEADSET           = 1;
    public static final int TYPE_HANDSFREE         = 2;
    private final Context mContext;
    private final Phone mPhone;
    private final BluetoothA2dp mA2dp;
    private BluetoothDevice mA2dpDevice;
    private int mA2dpState;
    private ServiceState mServiceState;
    private HeadsetBase mHeadset;  
    private int mHeadsetType;
    private boolean mAudioPossible;
    private ScoSocket mIncomingSco;
    private ScoSocket mOutgoingSco;
    private ScoSocket mConnectedSco;
    private Call mForegroundCall;
    private Call mBackgroundCall;
    private Call mRingingCall;
    private AudioManager mAudioManager;
    private PowerManager mPowerManager;
    private boolean mPendingSco;  
    private boolean mA2dpSuspended;
    private boolean mUserWantsAudio;
    private WakeLock mStartCallWakeLock;  
    private WakeLock mStartVoiceRecognitionWakeLock;  
    private static final int GSM_MAX_CONNECTIONS = 6;  
    private static final int CDMA_MAX_CONNECTIONS = 2;  
    private long mBgndEarliestConnectionTime = 0;
    private boolean mClip = false;  
    private boolean mIndicatorsEnabled = false;
    private boolean mCmee = false;  
    private long[] mClccTimestamps; 
    private boolean[] mClccUsed;     
    private boolean mWaitingForCallStart;
    private boolean mWaitingForVoiceRecognition;
    private boolean mServiceConnectionEstablished;
    private final BluetoothPhoneState mBluetoothPhoneState;  
    private final BluetoothAtPhonebook mPhonebook;
    private Phone.State mPhoneState = Phone.State.IDLE;
    CdmaPhoneCallState.PhoneCallState mCdmaThreeWayCallState =
                                            CdmaPhoneCallState.PhoneCallState.IDLE;
    private DebugThread mDebugThread;
    private int mScoGain = Integer.MIN_VALUE;
    private static Intent sVoiceCommandIntent;
    private static final String HEADSET_NREC = "bt_headset_nrec";
    private static final String HEADSET_NAME = "bt_headset_name";
    private int mRemoteBrsf = 0;
    private int mLocalBrsf = 0;
    private boolean mCdmaIsSecondCallActive = false;
    private static final int BRSF_AG_THREE_WAY_CALLING = 1 << 0;
    private static final int BRSF_AG_EC_NR = 1 << 1;
    private static final int BRSF_AG_VOICE_RECOG = 1 << 2;
    private static final int BRSF_AG_IN_BAND_RING = 1 << 3;
    private static final int BRSF_AG_VOICE_TAG_NUMBE = 1 << 4;
    private static final int BRSF_AG_REJECT_CALL = 1 << 5;
    private static final int BRSF_AG_ENHANCED_CALL_STATUS = 1 <<  6;
    private static final int BRSF_AG_ENHANCED_CALL_CONTROL = 1 << 7;
    private static final int BRSF_AG_ENHANCED_ERR_RESULT_CODES = 1 << 8;
    private static final int BRSF_HF_EC_NR = 1 << 0;
    private static final int BRSF_HF_CW_THREE_WAY_CALLING = 1 << 1;
    private static final int BRSF_HF_CLIP = 1 << 2;
    private static final int BRSF_HF_VOICE_REG_ACT = 1 << 3;
    private static final int BRSF_HF_REMOTE_VOL_CONTROL = 1 << 4;
    private static final int BRSF_HF_ENHANCED_CALL_STATUS = 1 <<  5;
    private static final int BRSF_HF_ENHANCED_CALL_CONTROL = 1 << 6;
    public static String typeToString(int type) {
        switch (type) {
        case TYPE_UNKNOWN:
            return "unknown";
        case TYPE_HEADSET:
            return "headset";
        case TYPE_HANDSFREE:
            return "handsfree";
        }
        return null;
    }
    public BluetoothHandsfree(Context context, Phone phone) {
        mPhone = phone;
        mContext = context;
        BluetoothAdapter adapter = BluetoothAdapter.getDefaultAdapter();
        boolean bluetoothCapable = (adapter != null);
        mHeadset = null;  
        mA2dp = new BluetoothA2dp(mContext);
        mA2dpState = BluetoothA2dp.STATE_DISCONNECTED;
        mA2dpDevice = null;
        mA2dpSuspended = false;
        mPowerManager = (PowerManager) context.getSystemService(Context.POWER_SERVICE);
        mStartCallWakeLock = mPowerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK,
                                                       TAG + ":StartCall");
        mStartCallWakeLock.setReferenceCounted(false);
        mStartVoiceRecognitionWakeLock = mPowerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK,
                                                       TAG + ":VoiceRecognition");
        mStartVoiceRecognitionWakeLock.setReferenceCounted(false);
        mLocalBrsf = BRSF_AG_THREE_WAY_CALLING |
                     BRSF_AG_EC_NR |
                     BRSF_AG_REJECT_CALL |
                     BRSF_AG_ENHANCED_CALL_STATUS;
        if (sVoiceCommandIntent == null) {
            sVoiceCommandIntent = new Intent(Intent.ACTION_VOICE_COMMAND);
            sVoiceCommandIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        }
        if (mContext.getPackageManager().resolveActivity(sVoiceCommandIntent, 0) != null &&
                BluetoothHeadset.isBluetoothVoiceDialingEnabled(mContext)) {
            mLocalBrsf |= BRSF_AG_VOICE_RECOG;
        }
        if (bluetoothCapable) {
            resetAtState();
        }
        mRingingCall = mPhone.getRingingCall();
        mForegroundCall = mPhone.getForegroundCall();
        mBackgroundCall = mPhone.getBackgroundCall();
        mBluetoothPhoneState = new BluetoothPhoneState();
        mUserWantsAudio = true;
        mPhonebook = new BluetoothAtPhonebook(mContext, this);
        mAudioManager = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
        cdmaSetSecondCallState(false);
    }
     synchronized void onBluetoothEnabled() {
        if (mIncomingSco == null) {
            mIncomingSco = createScoSocket();
            mIncomingSco.accept();
        }
    }
     synchronized void onBluetoothDisabled() {
        audioOff();
        if (mIncomingSco != null) {
            mIncomingSco.close();
            mIncomingSco = null;
        }
    }
    private boolean isHeadsetConnected() {
        if (mHeadset == null) {
            return false;
        }
        return mHeadset.isConnected();
    }
     void connectHeadset(HeadsetBase headset, int headsetType) {
        mHeadset = headset;
        mHeadsetType = headsetType;
        if (mHeadsetType == TYPE_HEADSET) {
            initializeHeadsetAtParser();
        } else {
            initializeHandsfreeAtParser();
        }
        headset.startEventThread();
        configAudioParameters();
        if (inDebug()) {
            startDebug();
        }
        if (isIncallAudio()) {
            audioOn();
        }
    }
    private boolean isIncallAudio() {
        Call.State state = mForegroundCall.getState();
        return (state == Call.State.ACTIVE || state == Call.State.ALERTING);
    }
     synchronized void disconnectHeadset() {
        audioOff();
        mHeadset = null;
        stopDebug();
        resetAtState();
    }
    private void resetAtState() {
        mClip = false;
        mIndicatorsEnabled = false;
        mServiceConnectionEstablished = false;
        mCmee = false;
        mClccTimestamps = new long[GSM_MAX_CONNECTIONS];
        mClccUsed = new boolean[GSM_MAX_CONNECTIONS];
        for (int i = 0; i < GSM_MAX_CONNECTIONS; i++) {
            mClccUsed[i] = false;
        }
        mRemoteBrsf = 0;
    }
    private void configAudioParameters() {
        String name = mHeadset.getRemoteDevice().getName();
        if (name == null) {
            name = "<unknown>";
        }
        mAudioManager.setParameters(HEADSET_NAME+"="+name+";"+HEADSET_NREC+"=on");
    }
    private class BluetoothPhoneState {
        private int mService;
        private int mCall;
        private int mCallsetup;
        private int mCallheld;
        private int mSignal;
        private int mRssi;  
        private int mRoam;
        private int mBattchg;
        private int mStat;  
        private String mRingingNumber;  
        private int    mRingingType;
        private boolean mIgnoreRing = false;
        private boolean mStopRing = false;
        private static final int SERVICE_STATE_CHANGED = 1;
        private static final int PRECISE_CALL_STATE_CHANGED = 2;
        private static final int RING = 3;
        private static final int PHONE_CDMA_CALL_WAITING = 4;
        private Handler mStateChangeHandler = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                switch(msg.what) {
                case RING:
                    AtCommandResult result = ring();
                    if (result != null) {
                        sendURC(result.toString());
                    }
                    break;
                case SERVICE_STATE_CHANGED:
                    ServiceState state = (ServiceState) ((AsyncResult) msg.obj).result;
                    updateServiceState(sendUpdate(), state);
                    break;
                case PRECISE_CALL_STATE_CHANGED:
                case PHONE_CDMA_CALL_WAITING:
                    Connection connection = null;
                    if (((AsyncResult) msg.obj).result instanceof Connection) {
                        connection = (Connection) ((AsyncResult) msg.obj).result;
                    }
                    handlePreciseCallStateChange(sendUpdate(), connection);
                    break;
                }
            }
        };
        private BluetoothPhoneState() {
            updateServiceState(false, mPhone.getServiceState());
            handlePreciseCallStateChange(false, null);
            mBattchg = 5;  
            mSignal = asuToSignal(mPhone.getSignalStrength());
            mPhone.registerForServiceStateChanged(mStateChangeHandler,
                                                  SERVICE_STATE_CHANGED, null);
            mPhone.registerForPreciseCallStateChanged(mStateChangeHandler,
                    PRECISE_CALL_STATE_CHANGED, null);
            if (mPhone.getPhoneType() == Phone.PHONE_TYPE_CDMA) {
                mPhone.registerForCallWaiting(mStateChangeHandler,
                                              PHONE_CDMA_CALL_WAITING, null);
            }
            IntentFilter filter = new IntentFilter(Intent.ACTION_BATTERY_CHANGED);
            filter.addAction(TelephonyIntents.ACTION_SIGNAL_STRENGTH_CHANGED);
            filter.addAction(BluetoothA2dp.ACTION_SINK_STATE_CHANGED);
            mContext.registerReceiver(mStateReceiver, filter);
        }
        private void updateBtPhoneStateAfterRadioTechnologyChange() {
            if(VDBG) Log.d(TAG, "updateBtPhoneStateAfterRadioTechnologyChange...");
            mPhone.unregisterForServiceStateChanged(mStateChangeHandler);
            mPhone.unregisterForPreciseCallStateChanged(mStateChangeHandler);
            mPhone.unregisterForCallWaiting(mStateChangeHandler);
            mPhone.registerForServiceStateChanged(mStateChangeHandler,
                                                  SERVICE_STATE_CHANGED, null);
            mPhone.registerForPreciseCallStateChanged(mStateChangeHandler,
                    PRECISE_CALL_STATE_CHANGED, null);
            if (mPhone.getPhoneType() == Phone.PHONE_TYPE_CDMA) {
                mPhone.registerForCallWaiting(mStateChangeHandler,
                                              PHONE_CDMA_CALL_WAITING, null);
            }
        }
        private boolean sendUpdate() {
            return isHeadsetConnected() && mHeadsetType == TYPE_HANDSFREE && mIndicatorsEnabled;
        }
        private boolean sendClipUpdate() {
            return isHeadsetConnected() && mHeadsetType == TYPE_HANDSFREE && mClip;
        }
        private void stopRing() {
            mStopRing = true;
        }
        private int gsmAsuToSignal(SignalStrength signalStrength) {
            int asu = signalStrength.getGsmSignalStrength();
            if      (asu >= 16) return 5;
            else if (asu >= 8)  return 4;
            else if (asu >= 4)  return 3;
            else if (asu >= 2)  return 2;
            else if (asu >= 1)  return 1;
            else                return 0;
        }
        private int cdmaDbmEcioToSignal(SignalStrength signalStrength) {
            int levelDbm = 0;
            int levelEcio = 0;
            int cdmaIconLevel = 0;
            int evdoIconLevel = 0;
            int cdmaDbm = signalStrength.getCdmaDbm();
            int cdmaEcio = signalStrength.getCdmaEcio();
            if (cdmaDbm >= -75) levelDbm = 4;
            else if (cdmaDbm >= -85) levelDbm = 3;
            else if (cdmaDbm >= -95) levelDbm = 2;
            else if (cdmaDbm >= -100) levelDbm = 1;
            else levelDbm = 0;
            if (cdmaEcio >= -90) levelEcio = 4;
            else if (cdmaEcio >= -110) levelEcio = 3;
            else if (cdmaEcio >= -130) levelEcio = 2;
            else if (cdmaEcio >= -150) levelEcio = 1;
            else levelEcio = 0;
            cdmaIconLevel = (levelDbm < levelEcio) ? levelDbm : levelEcio;
            if (mServiceState != null &&
                  (mServiceState.getRadioTechnology() == ServiceState.RADIO_TECHNOLOGY_EVDO_0 ||
                   mServiceState.getRadioTechnology() == ServiceState.RADIO_TECHNOLOGY_EVDO_A)) {
                  int evdoEcio = signalStrength.getEvdoEcio();
                  int evdoSnr = signalStrength.getEvdoSnr();
                  int levelEvdoEcio = 0;
                  int levelEvdoSnr = 0;
                  if (evdoEcio >= -650) levelEvdoEcio = 4;
                  else if (evdoEcio >= -750) levelEvdoEcio = 3;
                  else if (evdoEcio >= -900) levelEvdoEcio = 2;
                  else if (evdoEcio >= -1050) levelEvdoEcio = 1;
                  else levelEvdoEcio = 0;
                  if (evdoSnr > 7) levelEvdoSnr = 4;
                  else if (evdoSnr > 5) levelEvdoSnr = 3;
                  else if (evdoSnr > 3) levelEvdoSnr = 2;
                  else if (evdoSnr > 1) levelEvdoSnr = 1;
                  else levelEvdoSnr = 0;
                  evdoIconLevel = (levelEvdoEcio < levelEvdoSnr) ? levelEvdoEcio : levelEvdoSnr;
            }
            return (cdmaIconLevel > evdoIconLevel) ?  cdmaIconLevel : evdoIconLevel;
        }
        private int asuToSignal(SignalStrength signalStrength) {
            if (signalStrength.isGsm()) {
                return gsmAsuToSignal(signalStrength);
            } else {
                return cdmaDbmEcioToSignal(signalStrength);
            }
        }
        private int signalToRssi(int signal) {
            switch (signal) {
            case 0: return 0;
            case 1: return 4;
            case 2: return 8;
            case 3: return 13;
            case 4: return 19;
            case 5: return 31;
            }
            return 0;
        }
        private final BroadcastReceiver mStateReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                if (intent.getAction().equals(Intent.ACTION_BATTERY_CHANGED)) {
                    updateBatteryState(intent);
                } else if (intent.getAction().equals(
                            TelephonyIntents.ACTION_SIGNAL_STRENGTH_CHANGED)) {
                    updateSignalState(intent);
                } else if (intent.getAction().equals(BluetoothA2dp.ACTION_SINK_STATE_CHANGED)) {
                    int state = intent.getIntExtra(BluetoothA2dp.EXTRA_SINK_STATE,
                            BluetoothA2dp.STATE_DISCONNECTED);
                    int oldState = intent.getIntExtra(BluetoothA2dp.EXTRA_PREVIOUS_SINK_STATE,
                            BluetoothA2dp.STATE_DISCONNECTED);
                    BluetoothDevice device =
                            intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                    if (mA2dpDevice != null && !device.equals(mA2dpDevice)) return;
                    synchronized (BluetoothHandsfree.this) {
                        mA2dpState = state;
                        if (state == BluetoothA2dp.STATE_DISCONNECTED) {
                            mA2dpDevice = null;
                        } else {
                            mA2dpDevice = device;
                        }
                        if (oldState == BluetoothA2dp.STATE_PLAYING &&
                            mA2dpState == BluetoothA2dp.STATE_CONNECTED) {
                            if (mA2dpSuspended) {
                                if (mPendingSco) {
                                    mHandler.removeMessages(MESSAGE_CHECK_PENDING_SCO);
                                    if (DBG) log("A2DP suspended, completing SCO");
                                    mOutgoingSco = createScoSocket();
                                    if (!mOutgoingSco.connect(
                                            mHeadset.getRemoteDevice().getAddress(),
                                            mHeadset.getRemoteDevice().getName())) {
                                        mOutgoingSco = null;
                                    }
                                    mPendingSco = false;
                                }
                            }
                        }
                    }
                }
            }
        };
        private synchronized void updateBatteryState(Intent intent) {
            int batteryLevel = intent.getIntExtra("level", -1);
            int scale = intent.getIntExtra("scale", -1);
            if (batteryLevel == -1 || scale == -1) {
                return;  
            }
            batteryLevel = batteryLevel * 5 / scale;
            if (mBattchg != batteryLevel) {
                mBattchg = batteryLevel;
                if (sendUpdate()) {
                    sendURC("+CIEV: 7," + mBattchg);
                }
            }
        }
        private synchronized void updateSignalState(Intent intent) {
            SignalStrength signalStrength = SignalStrength.newFromBundle(intent.getExtras());
            int signal;
            if (signalStrength != null) {
                signal = asuToSignal(signalStrength);
                mRssi = signalToRssi(signal);  
                if (signal != mSignal) {
                    mSignal = signal;
                    if (sendUpdate()) {
                        sendURC("+CIEV: 5," + mSignal);
                    }
                }
            } else {
                Log.e(TAG, "Signal Strength null");
            }
        }
        private synchronized void updateServiceState(boolean sendUpdate, ServiceState state) {
            int service = state.getState() == ServiceState.STATE_IN_SERVICE ? 1 : 0;
            int roam = state.getRoaming() ? 1 : 0;
            int stat;
            AtCommandResult result = new AtCommandResult(AtCommandResult.UNSOLICITED);
            mServiceState = state;
            if (service == 0) {
                stat = 0;
            } else {
                stat = (roam == 1) ? 5 : 1;
            }
            if (service != mService) {
                mService = service;
                if (sendUpdate) {
                    result.addResponse("+CIEV: 1," + mService);
                }
            }
            if (roam != mRoam) {
                mRoam = roam;
                if (sendUpdate) {
                    result.addResponse("+CIEV: 6," + mRoam);
                }
            }
            if (stat != mStat) {
                mStat = stat;
                if (sendUpdate) {
                    result.addResponse(toCregString());
                }
            }
            sendURC(result.toString());
        }
        private synchronized void handlePreciseCallStateChange(boolean sendUpdate,
                Connection connection) {
            int call = 0;
            int callsetup = 0;
            int callheld = 0;
            int prevCallsetup = mCallsetup;
            AtCommandResult result = new AtCommandResult(AtCommandResult.UNSOLICITED);
            if (VDBG) log("updatePhoneState()");
            Phone.State newState = mPhone.getState();
            if (newState != mPhoneState) {
                mPhoneState = newState;
                switch (mPhoneState) {
                case IDLE:
                    mUserWantsAudio = true;  
                    audioOff();
                    break;
                default:
                    callStarted();
                }
            }
            switch(mForegroundCall.getState()) {
            case ACTIVE:
                call = 1;
                mAudioPossible = true;
                break;
            case DIALING:
                callsetup = 2;
                mAudioPossible = true;
                if (mPhone.getPhoneType() == Phone.PHONE_TYPE_GSM) {
                    callStarted();
                }
                break;
            case ALERTING:
                callsetup = 3;
                audioOn();
                mAudioPossible = true;
                break;
            default:
                mAudioPossible = false;
            }
            switch(mRingingCall.getState()) {
            case INCOMING:
            case WAITING:
                callsetup = 1;
                break;
            }
            switch(mBackgroundCall.getState()) {
            case HOLDING:
                if (call == 1) {
                    callheld = 1;
                } else {
                    call = 1;
                    callheld = 2;
                }
                break;
            }
            if (mCall != call) {
                if (call == 1) {
                    audioOn();
                }
                mCall = call;
                if (sendUpdate) {
                    result.addResponse("+CIEV: 2," + mCall);
                }
            }
            if (mCallsetup != callsetup) {
                mCallsetup = callsetup;
                if (sendUpdate) {
                    if (mCall != 1 || mCallsetup == 0 ||
                        mCallsetup != 1 && (mRemoteBrsf & BRSF_HF_CW_THREE_WAY_CALLING) != 0x0) {
                        result.addResponse("+CIEV: 3," + mCallsetup);
                    }
                }
            }
            if (mPhone.getPhoneType() == Phone.PHONE_TYPE_CDMA) {
                PhoneApp app = PhoneApp.getInstance();
                if (app.cdmaPhoneCallState != null) {
                    CdmaPhoneCallState.PhoneCallState currCdmaThreeWayCallState =
                            app.cdmaPhoneCallState.getCurrentCallState();
                    CdmaPhoneCallState.PhoneCallState prevCdmaThreeWayCallState =
                        app.cdmaPhoneCallState.getPreviousCallState();
                    callheld = getCdmaCallHeldStatus(currCdmaThreeWayCallState,
                                                     prevCdmaThreeWayCallState);
                    if (mCdmaThreeWayCallState != currCdmaThreeWayCallState) {
                        if ((currCdmaThreeWayCallState ==
                                CdmaPhoneCallState.PhoneCallState.THRWAY_ACTIVE)
                                    && app.cdmaPhoneCallState.IsThreeWayCallOrigStateDialing()) {
                            mAudioPossible = true;
                            if (sendUpdate) {
                                if ((mRemoteBrsf & BRSF_HF_CW_THREE_WAY_CALLING) != 0x0) {
                                    result.addResponse("+CIEV: 3,2");
                                    result.addResponse("+CIEV: 3,3");
                                    result.addResponse("+CIEV: 3,0");
                                }
                            }
                            callStarted();
                        }
                        if (currCdmaThreeWayCallState ==
                                CdmaPhoneCallState.PhoneCallState.CONF_CALL) {
                            mAudioPossible = true;
                            if (sendUpdate) {
                                if ((mRemoteBrsf & BRSF_HF_CW_THREE_WAY_CALLING) != 0x0) {
                                    result.addResponse("+CIEV: 2,1");
                                    result.addResponse("+CIEV: 3,0");
                                }
                            }
                        }
                    }
                    mCdmaThreeWayCallState = currCdmaThreeWayCallState;
                }
            }
            boolean callsSwitched =
                (callheld == 1 && ! (mBackgroundCall.getEarliestConnectTime() ==
                    mBgndEarliestConnectionTime));
            mBgndEarliestConnectionTime = mBackgroundCall.getEarliestConnectTime();
            if (mCallheld != callheld || callsSwitched) {
                mCallheld = callheld;
                if (sendUpdate) {
                    result.addResponse("+CIEV: 4," + mCallheld);
                }
            }
            if (callsetup == 1 && callsetup != prevCallsetup) {
                String number = null;
                int type = 128;
                if (connection == null) {
                    connection = mRingingCall.getEarliestConnection();
                    if (connection == null) {
                        Log.e(TAG, "Could not get a handle on Connection object for new " +
                              "incoming call");
                    }
                }
                if (connection != null) {
                    number = connection.getAddress();
                    if (number != null) {
                        type = PhoneNumberUtils.toaFromString(number);
                    }
                }
                if (number == null) {
                    number = "";
                }
                if ((call != 0 || callheld != 0) && sendUpdate) {
                    if ((mRemoteBrsf & BRSF_HF_CW_THREE_WAY_CALLING) != 0x0) {
                        result.addResponse("+CCWA: \"" + number + "\"," + type);
                        result.addResponse("+CIEV: 3," + callsetup);
                    }
                } else {
                    mRingingNumber = number;
                    mRingingType = type;
                    mIgnoreRing = false;
                    mStopRing = false;
                    if ((mLocalBrsf & BRSF_AG_IN_BAND_RING) != 0x0) {
                        audioOn();
                    }
                    result.addResult(ring());
                }
            }
            sendURC(result.toString());
        }
        private int getCdmaCallHeldStatus(CdmaPhoneCallState.PhoneCallState currState,
                                  CdmaPhoneCallState.PhoneCallState prevState) {
            int callheld;
            if (currState == CdmaPhoneCallState.PhoneCallState.CONF_CALL) {
                if (prevState == CdmaPhoneCallState.PhoneCallState.THRWAY_ACTIVE) {
                    callheld = 0; 
                } else {
                    callheld = 1; 
                }
            } else if (currState == CdmaPhoneCallState.PhoneCallState.THRWAY_ACTIVE) {
                callheld = 1; 
            } else {
                callheld = 0; 
            }
            return callheld;
        }
        private AtCommandResult ring() {
            if (!mIgnoreRing && !mStopRing && mRingingCall.isRinging()) {
                AtCommandResult result = new AtCommandResult(AtCommandResult.UNSOLICITED);
                result.addResponse("RING");
                if (sendClipUpdate()) {
                    result.addResponse("+CLIP: \"" + mRingingNumber + "\"," + mRingingType);
                }
                Message msg = mStateChangeHandler.obtainMessage(RING);
                mStateChangeHandler.sendMessageDelayed(msg, 3000);
                return result;
            }
            return null;
        }
        private synchronized String toCregString() {
            return new String("+CREG: 1," + mStat);
        }
        private synchronized AtCommandResult toCindResult() {
            AtCommandResult result = new AtCommandResult(AtCommandResult.OK);
            String status = "+CIND: " + mService + "," + mCall + "," + mCallsetup + "," +
                            mCallheld + "," + mSignal + "," + mRoam + "," + mBattchg;
            result.addResponse(status);
            return result;
        }
        private synchronized AtCommandResult toCsqResult() {
            AtCommandResult result = new AtCommandResult(AtCommandResult.OK);
            String status = "+CSQ: " + mRssi + ",99";
            result.addResponse(status);
            return result;
        }
        private synchronized AtCommandResult getCindTestResult() {
            return new AtCommandResult("+CIND: (\"service\",(0-1))," + "(\"call\",(0-1))," +
                        "(\"callsetup\",(0-3)),(\"callheld\",(0-2)),(\"signal\",(0-5))," +
                        "(\"roam\",(0-1)),(\"battchg\",(0-5))");
        }
        private synchronized void ignoreRing() {
            mCallsetup = 0;
            mIgnoreRing = true;
            if (sendUpdate()) {
                sendURC("+CIEV: 3," + mCallsetup);
            }
        }
    };
    private static final int SCO_ACCEPTED = 1;
    private static final int SCO_CONNECTED = 2;
    private static final int SCO_CLOSED = 3;
    private static final int CHECK_CALL_STARTED = 4;
    private static final int CHECK_VOICE_RECOGNITION_STARTED = 5;
    private static final int MESSAGE_CHECK_PENDING_SCO = 6;
    private final Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            synchronized (BluetoothHandsfree.this) {
                switch (msg.what) {
                case SCO_ACCEPTED:
                    if (msg.arg1 == ScoSocket.STATE_CONNECTED) {
                        if (isHeadsetConnected() && (mAudioPossible || allowAudioAnytime()) &&
                                mConnectedSco == null) {
                            Log.i(TAG, "Routing audio for incoming SCO connection");
                            mConnectedSco = (ScoSocket)msg.obj;
                            mAudioManager.setBluetoothScoOn(true);
                            broadcastAudioStateIntent(BluetoothHeadset.AUDIO_STATE_CONNECTED,
                                    mHeadset.getRemoteDevice());
                        } else {
                            Log.i(TAG, "Rejecting incoming SCO connection");
                            ((ScoSocket)msg.obj).close();
                        }
                    } 
                    mIncomingSco = createScoSocket();
                    mIncomingSco.accept();
                    break;
                case SCO_CONNECTED:
                    if (msg.arg1 == ScoSocket.STATE_CONNECTED && isHeadsetConnected() &&
                            mConnectedSco == null) {
                        if (VDBG) log("Routing audio for outgoing SCO conection");
                        mConnectedSco = (ScoSocket)msg.obj;
                        mAudioManager.setBluetoothScoOn(true);
                        broadcastAudioStateIntent(BluetoothHeadset.AUDIO_STATE_CONNECTED,
                                mHeadset.getRemoteDevice());
                    } else if (msg.arg1 == ScoSocket.STATE_CONNECTED) {
                        if (VDBG) log("Rejecting new connected outgoing SCO socket");
                        ((ScoSocket)msg.obj).close();
                        mOutgoingSco.close();
                    }
                    mOutgoingSco = null;
                    break;
                case SCO_CLOSED:
                    if (mConnectedSco == (ScoSocket)msg.obj) {
                        mConnectedSco.close();
                        mConnectedSco = null;
                        mAudioManager.setBluetoothScoOn(false);
                        broadcastAudioStateIntent(BluetoothHeadset.AUDIO_STATE_DISCONNECTED,
                               mHeadset.getRemoteDevice());
                    } else if (mOutgoingSco == (ScoSocket)msg.obj) {
                        mOutgoingSco.close();
                        mOutgoingSco = null;
                    }
                    break;
                case CHECK_CALL_STARTED:
                    if (mWaitingForCallStart) {
                        mWaitingForCallStart = false;
                        Log.e(TAG, "Timeout waiting for call to start");
                        sendURC("ERROR");
                        if (mStartCallWakeLock.isHeld()) {
                            mStartCallWakeLock.release();
                        }
                    }
                    break;
                case CHECK_VOICE_RECOGNITION_STARTED:
                    if (mWaitingForVoiceRecognition) {
                        mWaitingForVoiceRecognition = false;
                        Log.e(TAG, "Timeout waiting for voice recognition to start");
                        sendURC("ERROR");
                    }
                    break;
                case MESSAGE_CHECK_PENDING_SCO:
                    if (mPendingSco && isA2dpMultiProfile()) {
                        Log.w(TAG, "Timeout suspending A2DP for SCO (mA2dpState = " +
                                mA2dpState + "). Starting SCO anyway");
                        mOutgoingSco = createScoSocket();
                        if (!(isHeadsetConnected() &&
                                mOutgoingSco.connect(mHeadset.getRemoteDevice().getAddress(),
                                 mHeadset.getRemoteDevice().getName()))) {
                            mOutgoingSco = null;
                        }
                        mPendingSco = false;
                    }
                    break;
                }
            }
        }
    };
    private ScoSocket createScoSocket() {
        return new ScoSocket(mPowerManager, mHandler, SCO_ACCEPTED, SCO_CONNECTED, SCO_CLOSED);
    }
    private void broadcastAudioStateIntent(int state, BluetoothDevice device) {
        if (VDBG) log("broadcastAudioStateIntent(" + state + ")");
        Intent intent = new Intent(BluetoothHeadset.ACTION_AUDIO_STATE_CHANGED);
        intent.putExtra(BluetoothHeadset.EXTRA_AUDIO_STATE, state);
        intent.putExtra(BluetoothDevice.EXTRA_DEVICE, device);
        mContext.sendBroadcast(intent, android.Manifest.permission.BLUETOOTH);
    }
    void updateBtHandsfreeAfterRadioTechnologyChange() {
        if(VDBG) Log.d(TAG, "updateBtHandsfreeAfterRadioTechnologyChange...");
        mRingingCall = mPhone.getRingingCall();
        mForegroundCall = mPhone.getForegroundCall();
        mBackgroundCall = mPhone.getBackgroundCall();
        mBluetoothPhoneState.updateBtPhoneStateAfterRadioTechnologyChange();
    }
     synchronized boolean audioOn() {
        if (VDBG) log("audioOn()");
        if (!isHeadsetConnected()) {
            if (DBG) log("audioOn(): headset is not connected!");
            return false;
        }
        if (mHeadsetType == TYPE_HANDSFREE && !mServiceConnectionEstablished) {
            if (DBG) log("audioOn(): service connection not yet established!");
            return false;
        }
        if (mConnectedSco != null) {
            if (DBG) log("audioOn(): audio is already connected");
            return true;
        }
        if (!mUserWantsAudio) {
            if (DBG) log("audioOn(): user requested no audio, ignoring");
            return false;
        }
        if (mOutgoingSco != null) {
            if (DBG) log("audioOn(): outgoing SCO already in progress");
            return true;
        }
        if (mPendingSco) {
            if (DBG) log("audioOn(): SCO already pending");
            return true;
        }
        mA2dpSuspended = false;
        mPendingSco = false;
        if (isA2dpMultiProfile() && mA2dpState == BluetoothA2dp.STATE_PLAYING) {
            if (DBG) log("suspending A2DP stream for SCO");
            mA2dpSuspended = mA2dp.suspendSink(mA2dpDevice);
            if (mA2dpSuspended) {
                mPendingSco = true;
                Message msg = mHandler.obtainMessage(MESSAGE_CHECK_PENDING_SCO);
                mHandler.sendMessageDelayed(msg, 2000);
            } else {
                Log.w(TAG, "Could not suspend A2DP stream for SCO, going ahead with SCO");
            }
        }
        if (!mPendingSco) {
            mOutgoingSco = createScoSocket();
            if (!mOutgoingSco.connect(mHeadset.getRemoteDevice().getAddress(),
                    mHeadset.getRemoteDevice().getName())) {
                mOutgoingSco = null;
            }
        }
        return true;
    }
     synchronized void userWantsAudioOn() {
        mUserWantsAudio = true;
        audioOn();
    }
     synchronized void userWantsAudioOff() {
        mUserWantsAudio = false;
        audioOff();
    }
     synchronized void audioOff() {
        if (VDBG) log("audioOff(): mPendingSco: " + mPendingSco +
                ", mConnectedSco: " + mConnectedSco +
                ", mOutgoingSco: " + mOutgoingSco  +
                ", mA2dpState: " + mA2dpState +
                ", mA2dpSuspended: " + mA2dpSuspended +
                ", mIncomingSco:" + mIncomingSco);
        if (mA2dpSuspended) {
            if (isA2dpMultiProfile()) {
              if (DBG) log("resuming A2DP stream after disconnecting SCO");
              mA2dp.resumeSink(mA2dpDevice);
            }
            mA2dpSuspended = false;
        }
        mPendingSco = false;
        if (mConnectedSco != null) {
            BluetoothDevice device = null;
            if (mHeadset != null) {
                device = mHeadset.getRemoteDevice();
            }
            mConnectedSco.close();
            mConnectedSco = null;
            mAudioManager.setBluetoothScoOn(false);
            broadcastAudioStateIntent(BluetoothHeadset.AUDIO_STATE_DISCONNECTED, device);
        }
        if (mOutgoingSco != null) {
            mOutgoingSco.close();
            mOutgoingSco = null;
        }
    }
     boolean isAudioOn() {
        return (mConnectedSco != null);
    }
    private boolean isA2dpMultiProfile() {
        return mA2dp != null && mHeadset != null && mA2dpDevice != null &&
                mA2dpDevice.equals(mHeadset.getRemoteDevice());
    }
     void ignoreRing() {
        mBluetoothPhoneState.ignoreRing();
    }
    private void sendURC(String urc) {
        if (isHeadsetConnected()) {
            mHeadset.sendURC(urc);
        }
    }
    private AtCommandResult redial() {
        String number = mPhonebook.getLastDialledNumber();
        if (number == null) {
            if (VDBG) log("Bluetooth redial requested (+BLDN), but no previous " +
                  "outgoing calls found. Ignoring");
            return new AtCommandResult(AtCommandResult.ERROR);
        }
        Intent intent = new Intent(Intent.ACTION_CALL_PRIVILEGED,
                Uri.fromParts("tel", number, null));
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        mContext.startActivity(intent);
        expectCallStart();
        return new AtCommandResult(AtCommandResult.UNSOLICITED);  
    }
    private synchronized AtCommandResult gsmGetClccResult() {
        Connection[] clccConnections = new Connection[GSM_MAX_CONNECTIONS];  
        LinkedList<Connection> newConnections = new LinkedList<Connection>();
        LinkedList<Connection> connections = new LinkedList<Connection>();
        if (mRingingCall.getState().isAlive()) {
            connections.addAll(mRingingCall.getConnections());
        }
        if (mForegroundCall.getState().isAlive()) {
            connections.addAll(mForegroundCall.getConnections());
        }
        if (mBackgroundCall.getState().isAlive()) {
            connections.addAll(mBackgroundCall.getConnections());
        }
        boolean clccUsed[] = new boolean[GSM_MAX_CONNECTIONS];
        for (int i = 0; i < GSM_MAX_CONNECTIONS; i++) {
            clccUsed[i] = mClccUsed[i];
            mClccUsed[i] = false;
        }
        for (Connection c : connections) {
            boolean found = false;
            long timestamp = c.getCreateTime();
            for (int i = 0; i < GSM_MAX_CONNECTIONS; i++) {
                if (clccUsed[i] && timestamp == mClccTimestamps[i]) {
                    mClccUsed[i] = true;
                    found = true;
                    clccConnections[i] = c;
                    break;
                }
            }
            if (!found) {
                newConnections.add(c);
            }
        }
        while (!newConnections.isEmpty()) {
            int i = 0;
            while (mClccUsed[i]) i++;
            long earliestTimestamp = newConnections.get(0).getCreateTime();
            Connection earliestConnection = newConnections.get(0);
            for (int j = 0; j < newConnections.size(); j++) {
                long timestamp = newConnections.get(j).getCreateTime();
                if (timestamp < earliestTimestamp) {
                    earliestTimestamp = timestamp;
                    earliestConnection = newConnections.get(j);
                }
            }
            mClccUsed[i] = true;
            mClccTimestamps[i] = earliestTimestamp;
            clccConnections[i] = earliestConnection;
            newConnections.remove(earliestConnection);
        }
        AtCommandResult result = new AtCommandResult(AtCommandResult.OK);
        for (int i = 0; i < clccConnections.length; i++) {
            if (mClccUsed[i]) {
                String clccEntry = connectionToClccEntry(i, clccConnections[i]);
                if (clccEntry != null) {
                    result.addResponse(clccEntry);
                }
            }
        }
        return result;
    }
    private String connectionToClccEntry(int index, Connection c) {
        int state;
        switch (c.getState()) {
        case ACTIVE:
            state = 0;
            break;
        case HOLDING:
            state = 1;
            break;
        case DIALING:
            state = 2;
            break;
        case ALERTING:
            state = 3;
            break;
        case INCOMING:
            state = 4;
            break;
        case WAITING:
            state = 5;
            break;
        default:
            return null;  
        }
        int mpty = 0;
        Call call = c.getCall();
        if (call != null) {
            mpty = call.isMultiparty() ? 1 : 0;
        }
        int direction = c.isIncoming() ? 1 : 0;
        String number = c.getAddress();
        int type = -1;
        if (number != null) {
            type = PhoneNumberUtils.toaFromString(number);
        }
        String result = "+CLCC: " + (index + 1) + "," + direction + "," + state + ",0," + mpty;
        if (number != null) {
            result += ",\"" + number + "\"," + type;
        }
        return result;
    }
    private synchronized AtCommandResult cdmaGetClccResult() {
        Connection[] clccConnections = new Connection[CDMA_MAX_CONNECTIONS];
        Call.State ringingCallState = mRingingCall.getState();
        if (ringingCallState == Call.State.INCOMING) {
            if (VDBG) log("Filling clccConnections[0] for INCOMING state");
            clccConnections[0] = mRingingCall.getLatestConnection();
        } else if (mForegroundCall.getState().isAlive()) {
            if (mRingingCall.isRinging()) {
                if (VDBG) log("Filling clccConnections[0] & [1] for CALL WAITING state");
                clccConnections[0] = mForegroundCall.getEarliestConnection();
                clccConnections[1] = mRingingCall.getLatestConnection();
            } else {
                if (mForegroundCall.getConnections().size() <= 1) {
                    if (VDBG) log("Filling clccConnections[0] with ForgroundCall latest connection");
                    clccConnections[0] = mForegroundCall.getLatestConnection();
                } else {
                    if (VDBG) log("Filling clccConnections[0] & [1] with ForgroundCall connections");
                    clccConnections[0] = mForegroundCall.getEarliestConnection();
                    clccConnections[1] = mForegroundCall.getLatestConnection();
                }
            }
        }
        if (PhoneApp.getInstance().cdmaPhoneCallState.getCurrentCallState()
                == CdmaPhoneCallState.PhoneCallState.SINGLE_ACTIVE) {
            cdmaSetSecondCallState(false);
        } else if (PhoneApp.getInstance().cdmaPhoneCallState.getCurrentCallState()
                == CdmaPhoneCallState.PhoneCallState.THRWAY_ACTIVE) {
            cdmaSetSecondCallState(true);
        }
        AtCommandResult result = new AtCommandResult(AtCommandResult.OK);
        for (int i = 0; (i < clccConnections.length) && (clccConnections[i] != null); i++) {
            String clccEntry = cdmaConnectionToClccEntry(i, clccConnections[i]);
            if (clccEntry != null) {
                result.addResponse(clccEntry);
            }
        }
        return result;
    }
    private String cdmaConnectionToClccEntry(int index, Connection c) {
        int state;
        PhoneApp app = PhoneApp.getInstance();
        CdmaPhoneCallState.PhoneCallState currCdmaCallState =
                app.cdmaPhoneCallState.getCurrentCallState();
        CdmaPhoneCallState.PhoneCallState prevCdmaCallState =
                app.cdmaPhoneCallState.getPreviousCallState();
        if ((prevCdmaCallState == CdmaPhoneCallState.PhoneCallState.THRWAY_ACTIVE)
                && (currCdmaCallState == CdmaPhoneCallState.PhoneCallState.CONF_CALL)) {
            state = 0;
        } else {
            switch (c.getState()) {
            case ACTIVE:
                if (index == 0) { 
                    state = mCdmaIsSecondCallActive ? 1 : 0;
                } else { 
                    state = mCdmaIsSecondCallActive ? 0 : 1;
                }
                break;
            case HOLDING:
                state = 1;
                break;
            case DIALING:
                state = 2;
                break;
            case ALERTING:
                state = 3;
                break;
            case INCOMING:
                state = 4;
                break;
            case WAITING:
                state = 5;
                break;
            default:
                return null;  
            }
        }
        int mpty = 0;
        if (currCdmaCallState == CdmaPhoneCallState.PhoneCallState.CONF_CALL) {
            mpty = 1;
        } else {
            mpty = 0;
        }
        int direction = c.isIncoming() ? 1 : 0;
        String number = c.getAddress();
        int type = -1;
        if (number != null) {
            type = PhoneNumberUtils.toaFromString(number);
        }
        String result = "+CLCC: " + (index + 1) + "," + direction + "," + state + ",0," + mpty;
        if (number != null) {
            result += ",\"" + number + "\"," + type;
        }
        return result;
    }
    private void initializeHeadsetAtParser() {
        if (VDBG) log("Registering Headset AT commands");
        AtParser parser = mHeadset.getAtParser();
        parser.register("+CKPD", new AtCommandHandler() {
            private AtCommandResult headsetButtonPress() {
                if (mRingingCall.isRinging()) {
                    mBluetoothPhoneState.stopRing();
                    sendURC("OK");
                    PhoneUtils.answerCall(mPhone);
                    audioOn();
                    return new AtCommandResult(AtCommandResult.UNSOLICITED);
                } else if (mForegroundCall.getState().isAlive()) {
                    if (!isAudioOn()) {
                        audioOn();
                    } else {
                        if (mHeadset.getDirection() == HeadsetBase.DIRECTION_INCOMING &&
                          (System.currentTimeMillis() - mHeadset.getConnectTimestamp()) < 5000) {
                        } else {
                            audioOff();
                            PhoneUtils.hangup(mPhone);
                        }
                    }
                    return new AtCommandResult(AtCommandResult.OK);
                } else {
                    return redial();
                }
            }
            @Override
            public AtCommandResult handleActionCommand() {
                return headsetButtonPress();
            }
            @Override
            public AtCommandResult handleSetCommand(Object[] args) {
                return headsetButtonPress();
            }
        });
    }
    private void initializeHandsfreeAtParser() {
        if (VDBG) log("Registering Handsfree AT commands");
        AtParser parser = mHeadset.getAtParser();
        parser.register('A', new AtCommandHandler() {
            @Override
            public AtCommandResult handleBasicCommand(String args) {
                sendURC("OK");
                mBluetoothPhoneState.stopRing();
                PhoneUtils.answerCall(mPhone);
                return new AtCommandResult(AtCommandResult.UNSOLICITED);
            }
        });
        parser.register('D', new AtCommandHandler() {
            @Override
            public AtCommandResult handleBasicCommand(String args) {
                if (args.length() > 0) {
                    if (args.charAt(0) == '>') {
                        if (args.startsWith(">9999")) {   
                            return new AtCommandResult(AtCommandResult.ERROR);
                        }
                        return redial();
                    } else {
                        if (args.charAt(args.length() - 1) == ';') {
                            args = args.substring(0, args.length() - 1);
                        }
                        Intent intent = new Intent(Intent.ACTION_CALL_PRIVILEGED,
                                Uri.fromParts("tel", args, null));
                        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        mContext.startActivity(intent);
                        expectCallStart();
                        return new AtCommandResult(AtCommandResult.UNSOLICITED);  
                    }
                }
                return new AtCommandResult(AtCommandResult.ERROR);
            }
        });
        parser.register("+CHUP", new AtCommandHandler() {
            @Override
            public AtCommandResult handleActionCommand() {
                sendURC("OK");
                if (!mForegroundCall.isIdle()) {
                    PhoneUtils.hangupActiveCall(mPhone);
                } else if (!mRingingCall.isIdle()) {
                    PhoneUtils.hangupRingingCall(mPhone);
                } else if (!mBackgroundCall.isIdle()) {
                    PhoneUtils.hangupHoldingCall(mPhone);
                }
                return new AtCommandResult(AtCommandResult.UNSOLICITED);
            }
        });
        parser.register("+BRSF", new AtCommandHandler() {
            private AtCommandResult sendBRSF() {
                return new AtCommandResult("+BRSF: " + mLocalBrsf);
            }
            @Override
            public AtCommandResult handleSetCommand(Object[] args) {
                if (args.length == 1 && (args[0] instanceof Integer)) {
                    mRemoteBrsf = (Integer) args[0];
                } else {
                    Log.w(TAG, "HF didn't sent BRSF assuming 0");
                }
                return sendBRSF();
            }
            @Override
            public AtCommandResult handleActionCommand() {
                return sendBRSF();
            }
            @Override
            public AtCommandResult handleReadCommand() {
                return sendBRSF();
            }
        });
        parser.register("+CCWA", new AtCommandHandler() {
            @Override
            public AtCommandResult handleActionCommand() {
                return new AtCommandResult(AtCommandResult.OK);
            }
            @Override
            public AtCommandResult handleReadCommand() {
                return new AtCommandResult("+CCWA: 1");
            }
            @Override
            public AtCommandResult handleSetCommand(Object[] args) {
                return new AtCommandResult(AtCommandResult.OK);
            }
            @Override
            public AtCommandResult handleTestCommand() {
                return new AtCommandResult("+CCWA: (\"n\",(1))");
            }
        });
        parser.register("+CMER", new AtCommandHandler() {
            @Override
            public AtCommandResult handleReadCommand() {
                return new AtCommandResult(
                        "+CMER: 3,0,0," + (mIndicatorsEnabled ? "1" : "0"));
            }
            @Override
            public AtCommandResult handleSetCommand(Object[] args) {
                if (args.length < 4) {
                    return new AtCommandResult(AtCommandResult.ERROR);
                } else if (args[0].equals(3) && args[1].equals(0) &&
                           args[2].equals(0)) {
                    boolean valid = false;
                    if (args[3].equals(0)) {
                        mIndicatorsEnabled = false;
                        valid = true;
                    } else if (args[3].equals(1)) {
                        mIndicatorsEnabled = true;
                        valid = true;
                    }
                    if (valid) {
                        if ((mRemoteBrsf & BRSF_HF_CW_THREE_WAY_CALLING) == 0x0) {
                            mServiceConnectionEstablished = true;
                            sendURC("OK");  
                            if (isIncallAudio()) {
                                audioOn();
                            }
                            return new AtCommandResult(AtCommandResult.UNSOLICITED);
                        } else {
                            return new AtCommandResult(AtCommandResult.OK);
                        }
                    }
                }
                return reportCmeError(BluetoothCmeError.OPERATION_NOT_SUPPORTED);
            }
            @Override
            public AtCommandResult handleTestCommand() {
                return new AtCommandResult("+CMER: (3),(0),(0),(0-1)");
            }
        });
        parser.register("+CMEE", new AtCommandHandler() {
            @Override
            public AtCommandResult handleActionCommand() {
                mCmee = true;
                return new AtCommandResult(AtCommandResult.OK);
            }
            @Override
            public AtCommandResult handleReadCommand() {
                return new AtCommandResult("+CMEE: " + (mCmee ? "1" : "0"));
            }
            @Override
            public AtCommandResult handleSetCommand(Object[] args) {
                if (args.length == 0) {
                    mCmee = false;
                    return new AtCommandResult(AtCommandResult.OK);
                } else if (!(args[0] instanceof Integer)) {
                    return new AtCommandResult(AtCommandResult.ERROR);
                } else {
                    mCmee = ((Integer)args[0] == 1);
                    return new AtCommandResult(AtCommandResult.OK);
                }
            }
            @Override
            public AtCommandResult handleTestCommand() {
                return new AtCommandResult("+CMEE: (0-1)");
            }
        });
        parser.register("+BLDN", new AtCommandHandler() {
            @Override
            public AtCommandResult handleActionCommand() {
                return redial();
            }
        });
        parser.register("+CIND", new AtCommandHandler() {
            @Override
            public AtCommandResult handleReadCommand() {
                return mBluetoothPhoneState.toCindResult();
            }
            @Override
            public AtCommandResult handleTestCommand() {
                return mBluetoothPhoneState.getCindTestResult();
            }
        });
        parser.register("+CSQ", new AtCommandHandler() {
            @Override
            public AtCommandResult handleActionCommand() {
                return mBluetoothPhoneState.toCsqResult();
            }
        });
        parser.register("+CREG", new AtCommandHandler() {
            @Override
            public AtCommandResult handleReadCommand() {
                return new AtCommandResult(mBluetoothPhoneState.toCregString());
            }
        });
        parser.register("+VTS", new AtCommandHandler() {
            @Override
            public AtCommandResult handleSetCommand(Object[] args) {
                if (args.length >= 1) {
                    char c;
                    if (args[0] instanceof Integer) {
                        c = ((Integer) args[0]).toString().charAt(0);
                    } else {
                        c = ((String) args[0]).charAt(0);
                    }
                    if (isValidDtmf(c)) {
                        mPhone.sendDtmf(c);
                        return new AtCommandResult(AtCommandResult.OK);
                    }
                }
                return new AtCommandResult(AtCommandResult.ERROR);
            }
            private boolean isValidDtmf(char c) {
                switch (c) {
                case '#':
                case '*':
                    return true;
                default:
                    if (Character.digit(c, 14) != -1) {
                        return true;  
                    }
                    return false;
                }
            }
        });
        parser.register("+CLCC", new AtCommandHandler() {
            @Override
            public AtCommandResult handleActionCommand() {
                int phoneType = mPhone.getPhoneType();
                if (phoneType == Phone.PHONE_TYPE_CDMA) {
                    return cdmaGetClccResult();
                } else if (phoneType == Phone.PHONE_TYPE_GSM) {
                    return gsmGetClccResult();
                } else {
                    throw new IllegalStateException("Unexpected phone type: " + phoneType);
                }
            }
        });
        parser.register("+CHLD", new AtCommandHandler() {
            @Override
            public AtCommandResult handleSetCommand(Object[] args) {
                int phoneType = mPhone.getPhoneType();
                if (args.length >= 1) {
                    if (args[0].equals(0)) {
                        boolean result;
                        if (mRingingCall.isRinging()) {
                            result = PhoneUtils.hangupRingingCall(mPhone);
                        } else {
                            result = PhoneUtils.hangupHoldingCall(mPhone);
                        }
                        if (result) {
                            return new AtCommandResult(AtCommandResult.OK);
                        } else {
                            return new AtCommandResult(AtCommandResult.ERROR);
                        }
                    } else if (args[0].equals(1)) {
                        if (phoneType == Phone.PHONE_TYPE_CDMA) {
                            if (mRingingCall.isRinging()) {
                                if (VDBG) log("CHLD:1 Callwaiting Answer call");
                                PhoneUtils.answerCall(mPhone);
                                PhoneUtils.setMute(mPhone, false);
                                cdmaSetSecondCallState(true);
                            } else {
                                if (VDBG) log("CHLD:1 Hangup Call");
                                PhoneUtils.hangup(mPhone);
                            }
                            return new AtCommandResult(AtCommandResult.OK);
                        } else if (phoneType == Phone.PHONE_TYPE_GSM) {
                            if (PhoneUtils.answerAndEndActive(mPhone)) {
                                return new AtCommandResult(AtCommandResult.OK);
                            } else {
                                return new AtCommandResult(AtCommandResult.ERROR);
                            }
                        } else {
                            throw new IllegalStateException("Unexpected phone type: " + phoneType);
                        }
                    } else if (args[0].equals(2)) {
                        if (phoneType == Phone.PHONE_TYPE_CDMA) {
                            if (mRingingCall.isRinging()) {
                                if (VDBG) log("CHLD:2 Callwaiting Answer call");
                                PhoneUtils.answerCall(mPhone);
                                PhoneUtils.setMute(mPhone, false);
                                cdmaSetSecondCallState(true);
                            } else if (PhoneApp.getInstance().cdmaPhoneCallState
                                    .getCurrentCallState()
                                    == CdmaPhoneCallState.PhoneCallState.CONF_CALL) {
                                if (VDBG) log("CHLD:2 Swap Calls");
                                PhoneUtils.switchHoldingAndActive(mPhone);
                                cdmaSwapSecondCallState();
                            }
                        } else if (phoneType == Phone.PHONE_TYPE_GSM) {
                            PhoneUtils.switchHoldingAndActive(mPhone);
                        } else {
                            throw new IllegalStateException("Unexpected phone type: " + phoneType);
                        }
                        return new AtCommandResult(AtCommandResult.OK);
                    } else if (args[0].equals(3)) {
                        if (phoneType == Phone.PHONE_TYPE_CDMA) {
                            if (PhoneApp.getInstance().cdmaPhoneCallState.getCurrentCallState()
                                    == CdmaPhoneCallState.PhoneCallState.THRWAY_ACTIVE) {
                                if (VDBG) log("CHLD:3 Merge Calls");
                                PhoneUtils.mergeCalls(mPhone);
                            }
                        } else if (phoneType == Phone.PHONE_TYPE_GSM) {
                            if (mForegroundCall.getState().isAlive() &&
                                    mBackgroundCall.getState().isAlive()) {
                                PhoneUtils.mergeCalls(mPhone);
                            }
                        } else {
                            throw new IllegalStateException("Unexpected phone type: " + phoneType);
                        }
                        return new AtCommandResult(AtCommandResult.OK);
                    }
                }
                return new AtCommandResult(AtCommandResult.ERROR);
            }
            @Override
            public AtCommandResult handleTestCommand() {
                mServiceConnectionEstablished = true;
                sendURC("+CHLD: (0,1,2,3)");
                sendURC("OK");  
                if (isIncallAudio()) {
                    audioOn();
                }
                return new AtCommandResult(AtCommandResult.UNSOLICITED);
            }
        });
        parser.register("+COPS", new AtCommandHandler() {
            @Override
            public AtCommandResult handleReadCommand() {
                String operatorName = mPhone.getServiceState().getOperatorAlphaLong();
                if (operatorName != null) {
                    if (operatorName.length() > 16) {
                        operatorName = operatorName.substring(0, 16);
                    }
                    return new AtCommandResult(
                            "+COPS: 0,0,\"" + operatorName + "\"");
                } else {
                    return new AtCommandResult(
                            "+COPS: 0,0,\"UNKNOWN\",0");
                }
            }
            @Override
            public AtCommandResult handleSetCommand(Object[] args) {
                if (args.length != 2 || !(args[0] instanceof Integer)
                    || !(args[1] instanceof Integer)) {
                    return new AtCommandResult(AtCommandResult.ERROR);
                } else if ((Integer)args[0] != 3 || (Integer)args[1] != 0) {
                    return reportCmeError(BluetoothCmeError.OPERATION_NOT_SUPPORTED);
                } else {
                    return new AtCommandResult(AtCommandResult.OK);
                }
            }
            @Override
            public AtCommandResult handleTestCommand() {
                return new AtCommandResult("+COPS: (3),(0)");
            }
        });
        parser.register("+CPIN", new AtCommandHandler() {
            @Override
            public AtCommandResult handleReadCommand() {
                return new AtCommandResult("+CPIN: READY");
            }
        });
        parser.register("+BTRH", new AtCommandHandler() {
            @Override
            public AtCommandResult handleReadCommand() {
                return new AtCommandResult(AtCommandResult.OK);
            }
            @Override
            public AtCommandResult handleSetCommand(Object[] args) {
                return new AtCommandResult(AtCommandResult.ERROR);
            }
        });
        parser.register("+CIMI", new AtCommandHandler() {
            @Override
            public AtCommandResult handleActionCommand() {
                String imsi = mPhone.getSubscriberId();
                if (imsi == null || imsi.length() == 0) {
                    return reportCmeError(BluetoothCmeError.SIM_FAILURE);
                } else {
                    return new AtCommandResult(imsi);
                }
            }
        });
        parser.register("+CLIP", new AtCommandHandler() {
            @Override
            public AtCommandResult handleReadCommand() {
                return new AtCommandResult("+CLIP: " + (mClip ? "1" : "0") + ",1");
            }
            @Override
            public AtCommandResult handleSetCommand(Object[] args) {
                if (args.length >= 1 && (args[0].equals(0) || args[0].equals(1))) {
                    mClip = args[0].equals(1);
                    return new AtCommandResult(AtCommandResult.OK);
                } else {
                    return new AtCommandResult(AtCommandResult.ERROR);
                }
            }
            @Override
            public AtCommandResult handleTestCommand() {
                return new AtCommandResult("+CLIP: (0-1)");
            }
        });
        parser.register("+CGSN", new AtCommandHandler() {
            @Override
            public AtCommandResult handleActionCommand() {
                return new AtCommandResult("+CGSN: " + mPhone.getDeviceId());
            }
        });
        parser.register("+CGMM", new AtCommandHandler() {
            @Override
            public AtCommandResult handleActionCommand() {
                String model = SystemProperties.get("ro.product.model");
                if (model != null) {
                    return new AtCommandResult("+CGMM: " + model);
                } else {
                    return new AtCommandResult(AtCommandResult.ERROR);
                }
            }
        });
        parser.register("+CGMI", new AtCommandHandler() {
            @Override
            public AtCommandResult handleActionCommand() {
                String manuf = SystemProperties.get("ro.product.manufacturer");
                if (manuf != null) {
                    return new AtCommandResult("+CGMI: " + manuf);
                } else {
                    return new AtCommandResult(AtCommandResult.ERROR);
                }
            }
        });
        parser.register("+NREC", new AtCommandHandler() {
            @Override
            public AtCommandResult handleSetCommand(Object[] args) {
                if (args[0].equals(0)) {
                    mAudioManager.setParameters(HEADSET_NREC+"=off");
                    return new AtCommandResult(AtCommandResult.OK);
                } else if (args[0].equals(1)) {
                    mAudioManager.setParameters(HEADSET_NREC+"=on");
                    return new AtCommandResult(AtCommandResult.OK);
                }
                return new AtCommandResult(AtCommandResult.ERROR);
            }
        });
        parser.register("+BVRA", new AtCommandHandler() {
            @Override
            public AtCommandResult handleSetCommand(Object[] args) {
                if (!BluetoothHeadset.isBluetoothVoiceDialingEnabled(mContext)) {
                    return new AtCommandResult(AtCommandResult.ERROR);
                }
                if (args.length >= 1 && args[0].equals(1)) {
                    synchronized (BluetoothHandsfree.this) {
                        if (!mWaitingForVoiceRecognition) {
                            try {
                                mContext.startActivity(sVoiceCommandIntent);
                            } catch (ActivityNotFoundException e) {
                                return new AtCommandResult(AtCommandResult.ERROR);
                            }
                            expectVoiceRecognition();
                        }
                    }
                    return new AtCommandResult(AtCommandResult.UNSOLICITED);  
                } else if (args.length >= 1 && args[0].equals(0)) {
                    audioOff();
                    return new AtCommandResult(AtCommandResult.OK);
                }
                return new AtCommandResult(AtCommandResult.ERROR);
            }
            @Override
            public AtCommandResult handleTestCommand() {
                return new AtCommandResult("+BVRA: (0-1)");
            }
        });
        parser.register("+CNUM", new AtCommandHandler() {
            @Override
            public AtCommandResult handleActionCommand() {
                String number = mPhone.getLine1Number();
                if (number == null) {
                    return new AtCommandResult(AtCommandResult.OK);
                }
                return new AtCommandResult("+CNUM: ,\"" + number + "\"," +
                        PhoneNumberUtils.toaFromString(number) + ",,4");
            }
        });
        parser.register("+VGM", new AtCommandHandler() {
            @Override
            public AtCommandResult handleSetCommand(Object[] args) {
                return new AtCommandResult(AtCommandResult.OK);
            }
        });
        parser.register("+VGS", new AtCommandHandler() {
            @Override
            public AtCommandResult handleSetCommand(Object[] args) {
                if (args.length != 1 || !(args[0] instanceof Integer)) {
                    return new AtCommandResult(AtCommandResult.ERROR);
                }
                mScoGain = (Integer) args[0];
                int flag =  mAudioManager.isBluetoothScoOn() ? AudioManager.FLAG_SHOW_UI:0;
                mAudioManager.setStreamVolume(AudioManager.STREAM_BLUETOOTH_SCO, mScoGain, flag);
                return new AtCommandResult(AtCommandResult.OK);
            }
        });
        parser.register("+CPAS", new AtCommandHandler() {
            @Override
            public AtCommandResult handleActionCommand() {
                int status = 0;
                switch (mPhone.getState()) {
                case IDLE:
                    status = 0;
                    break;
                case RINGING:
                    status = 3;
                    break;
                case OFFHOOK:
                    status = 4;
                    break;
                }
                return new AtCommandResult("+CPAS: " + status);
            }
        });
        mPhonebook.register(parser);
    }
    public void sendScoGainUpdate(int gain) {
        if (mScoGain != gain && (mRemoteBrsf & BRSF_HF_REMOTE_VOL_CONTROL) != 0x0) {
            sendURC("+VGS:" + gain);
            mScoGain = gain;
        }
    }
    public AtCommandResult reportCmeError(int error) {
        if (mCmee) {
            AtCommandResult result = new AtCommandResult(AtCommandResult.UNSOLICITED);
            result.addResponse("+CME ERROR: " + error);
            return result;
        } else {
            return new AtCommandResult(AtCommandResult.ERROR);
        }
    }
    private static final int START_CALL_TIMEOUT = 10000;  
    private synchronized void expectCallStart() {
        mWaitingForCallStart = true;
        Message msg = Message.obtain(mHandler, CHECK_CALL_STARTED);
        mHandler.sendMessageDelayed(msg, START_CALL_TIMEOUT);
        if (!mStartCallWakeLock.isHeld()) {
            mStartCallWakeLock.acquire(START_CALL_TIMEOUT);
        }
    }
    private synchronized void callStarted() {
        if (mWaitingForCallStart) {
            mWaitingForCallStart = false;
            sendURC("OK");
            if (mStartCallWakeLock.isHeld()) {
                mStartCallWakeLock.release();
            }
        }
    }
    private static final int START_VOICE_RECOGNITION_TIMEOUT = 5000;  
    private synchronized void expectVoiceRecognition() {
        mWaitingForVoiceRecognition = true;
        Message msg = Message.obtain(mHandler, CHECK_VOICE_RECOGNITION_STARTED);
        mHandler.sendMessageDelayed(msg, START_VOICE_RECOGNITION_TIMEOUT);
        if (!mStartVoiceRecognitionWakeLock.isHeld()) {
            mStartVoiceRecognitionWakeLock.acquire(START_VOICE_RECOGNITION_TIMEOUT);
        }
    }
     synchronized boolean startVoiceRecognition() {
        if (mWaitingForVoiceRecognition) {
            mWaitingForVoiceRecognition = false;
            sendURC("OK");
        } else {
            sendURC("+BVRA: 1");
        }
        boolean ret = audioOn();
        if (mStartVoiceRecognitionWakeLock.isHeld()) {
            mStartVoiceRecognitionWakeLock.release();
        }
        return ret;
    }
     synchronized boolean stopVoiceRecognition() {
        sendURC("+BVRA: 0");
        audioOff();
        return true;
    }
    private boolean inDebug() {
        return DBG && SystemProperties.getBoolean(DebugThread.DEBUG_HANDSFREE, false);
    }
    private boolean allowAudioAnytime() {
        return inDebug() && SystemProperties.getBoolean(DebugThread.DEBUG_HANDSFREE_AUDIO_ANYTIME,
                false);
    }
    private void startDebug() {
        if (DBG && mDebugThread == null) {
            mDebugThread = new DebugThread();
            mDebugThread.start();
        }
    }
    private void stopDebug() {
        if (mDebugThread != null) {
            mDebugThread.interrupt();
            mDebugThread = null;
        }
    }
    private class DebugThread extends Thread {
        private static final String DEBUG_HANDSFREE = "debug.bt.hfp";
        private static final String DEBUG_HANDSFREE_BATTERY = "debug.bt.hfp.battery";
        private static final String DEBUG_HANDSFREE_SERVICE = "debug.bt.hfp.service";
        private static final String DEBUG_HANDSFREE_ROAM = "debug.bt.hfp.roam";
        private static final String DEBUG_HANDSFREE_AUDIO = "debug.bt.hfp.audio";
        private static final String DEBUG_HANDSFREE_AUDIO_ANYTIME = "debug.bt.hfp.audio_anytime";
        private static final String DEBUG_HANDSFREE_SIGNAL = "debug.bt.hfp.signal";
        private static final String DEBUG_HANDSFREE_CLCC = "debug.bt.hfp.clcc";
        private static final String DEBUG_UNSOL_INBAND_RINGTONE =
            "debug.bt.unsol.inband";
        @Override
        public void run() {
            boolean oldService = true;
            boolean oldRoam = false;
            boolean oldAudio = false;
            while (!isInterrupted() && inDebug()) {
                int batteryLevel = SystemProperties.getInt(DEBUG_HANDSFREE_BATTERY, -1);
                if (batteryLevel >= 0 && batteryLevel <= 5) {
                    Intent intent = new Intent();
                    intent.putExtra("level", batteryLevel);
                    intent.putExtra("scale", 5);
                    mBluetoothPhoneState.updateBatteryState(intent);
                }
                boolean serviceStateChanged = false;
                if (SystemProperties.getBoolean(DEBUG_HANDSFREE_SERVICE, true) != oldService) {
                    oldService = !oldService;
                    serviceStateChanged = true;
                }
                if (SystemProperties.getBoolean(DEBUG_HANDSFREE_ROAM, false) != oldRoam) {
                    oldRoam = !oldRoam;
                    serviceStateChanged = true;
                }
                if (serviceStateChanged) {
                    Bundle b = new Bundle();
                    b.putInt("state", oldService ? 0 : 1);
                    b.putBoolean("roaming", oldRoam);
                    mBluetoothPhoneState.updateServiceState(true, ServiceState.newFromBundle(b));
                }
                if (SystemProperties.getBoolean(DEBUG_HANDSFREE_AUDIO, false) != oldAudio) {
                    oldAudio = !oldAudio;
                    if (oldAudio) {
                        audioOn();
                    } else {
                        audioOff();
                    }
                }
                int signalLevel = SystemProperties.getInt(DEBUG_HANDSFREE_SIGNAL, -1);
                if (signalLevel >= 0 && signalLevel <= 31) {
                    SignalStrength signalStrength = new SignalStrength(signalLevel, -1, -1, -1,
                            -1, -1, -1, true);
                    Intent intent = new Intent();
                    Bundle data = new Bundle();
                    signalStrength.fillInNotifierBundle(data);
                    intent.putExtras(data);
                    mBluetoothPhoneState.updateSignalState(intent);
                }
                if (SystemProperties.getBoolean(DEBUG_HANDSFREE_CLCC, false)) {
                    log(gsmGetClccResult().toString());
                }
                try {
                    sleep(1000);  
                } catch (InterruptedException e) {
                    break;
                }
                int inBandRing =
                    SystemProperties.getInt(DEBUG_UNSOL_INBAND_RINGTONE, -1);
                if (inBandRing == 0 || inBandRing == 1) {
                    AtCommandResult result =
                        new AtCommandResult(AtCommandResult.UNSOLICITED);
                    result.addResponse("+BSIR: " + inBandRing);
                    sendURC(result.toString());
                }
            }
        }
    }
    public void cdmaSwapSecondCallState() {
        if (VDBG) log("cdmaSetSecondCallState: Toggling mCdmaIsSecondCallActive");
        mCdmaIsSecondCallActive = !mCdmaIsSecondCallActive;
    }
    public void cdmaSetSecondCallState(boolean state) {
        if (VDBG) log("cdmaSetSecondCallState: Setting mCdmaIsSecondCallActive to " + state);
        mCdmaIsSecondCallActive = state;
    }
    private static void log(String msg) {
        Log.d(TAG, msg);
    }
}
