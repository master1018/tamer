public class InCallScreen extends Activity
        implements View.OnClickListener, View.OnTouchListener {
    private static final String LOG_TAG = "InCallScreen";
    private static final boolean DBG =
            (PhoneApp.DBG_LEVEL >= 1) && (SystemProperties.getInt("ro.debuggable", 0) == 1);
    private static final boolean VDBG = (PhoneApp.DBG_LEVEL >= 2);
    static final String SHOW_DIALPAD_EXTRA = "com.android.phone.ShowDialpad";
     static final String EXTRA_GATEWAY_PROVIDER_PACKAGE =
            "com.android.phone.extra.GATEWAY_PROVIDER_PACKAGE";
     static final String EXTRA_GATEWAY_URI =
            "com.android.phone.extra.GATEWAY_URI";
    private static final int CALL_ENDED_SHORT_DELAY =  200;  
    private static final int CALL_ENDED_LONG_DELAY = 2000;  
    private static final int MENU_DISMISS_DELAY =  1000;  
    private static final int PAUSE_PROMPT_DIALOG_TIMEOUT = 2000;  
    private static final int TOUCH_LOCK_DELAY_DEFAULT =  6000;  
    private static final int THREEWAY_CALLERINFO_DISPLAY_TIME = 3000; 
    private static final int PROVIDER_OVERLAY_TIMEOUT = 5000;  
    static final int AUTO_RETRY_OFF = 0;
    static final int AUTO_RETRY_ON = 1;
    private static final int PHONE_STATE_CHANGED = 101;
    private static final int PHONE_DISCONNECT = 102;
    private static final int EVENT_HEADSET_PLUG_STATE_CHANGED = 103;
    private static final int POST_ON_DIAL_CHARS = 104;
    private static final int WILD_PROMPT_CHAR_ENTERED = 105;
    private static final int ADD_VOICEMAIL_NUMBER = 106;
    private static final int DONT_ADD_VOICEMAIL_NUMBER = 107;
    private static final int DELAYED_CLEANUP_AFTER_DISCONNECT = 108;
    private static final int SUPP_SERVICE_FAILED = 110;
    private static final int DISMISS_MENU = 111;
    private static final int ALLOW_SCREEN_ON = 112;
    private static final int TOUCH_LOCK_TIMER = 113;
    private static final int REQUEST_UPDATE_BLUETOOTH_INDICATION = 114;
    private static final int PHONE_CDMA_CALL_WAITING = 115;
    private static final int THREEWAY_CALLERINFO_DISPLAY_DONE = 116;
    private static final int EVENT_OTA_PROVISION_CHANGE = 117;
    private static final int REQUEST_CLOSE_SPC_ERROR_NOTICE = 118;
    private static final int REQUEST_CLOSE_OTA_FAILURE_NOTICE = 119;
    private static final int EVENT_PAUSE_DIALOG_COMPLETE = 120;
    private static final int EVENT_HIDE_PROVIDER_OVERLAY = 121;  
    private static final int REQUEST_UPDATE_TOUCH_UI = 122;
    public static final String ACTION_SHOW_ACTIVATION =
           "com.android.phone.InCallScreen.SHOW_ACTIVATION";
    public static final String OTA_NUMBER = "*228";
    public static final String EXTRA_OTA_CALL = "android.phone.extra.OTA_CALL";
    public static final String ACTION_UNDEFINED = "com.android.phone.InCallScreen.UNDEFINED";
    private enum InCallScreenMode {
        NORMAL,
        MANAGE_CONFERENCE,
        CALL_ENDED,
        OTA_NORMAL,
        OTA_ENDED,
        UNDEFINED
    }
    private InCallScreenMode mInCallScreenMode = InCallScreenMode.UNDEFINED;
    private enum InCallInitStatus {
        SUCCESS,
        VOICEMAIL_NUMBER_MISSING,
        POWER_OFF,
        EMERGENCY_ONLY,
        OUT_OF_SERVICE,
        PHONE_NOT_IN_USE,
        NO_PHONE_NUMBER_SUPPLIED,
        DIALED_MMI,
        CALL_FAILED
    }
    private InCallInitStatus mInCallInitialStatus;  
    private boolean mRegisteredForPhoneStates;
    private boolean mNeedShowCallLostDialog;
    private Phone mPhone;
    private Call mForegroundCall;
    private Call mBackgroundCall;
    private Call mRingingCall;
    private BluetoothHandsfree mBluetoothHandsfree;
    private BluetoothHeadset mBluetoothHeadset;
    private boolean mBluetoothConnectionPending;
    private long mBluetoothConnectionRequestTime;
    private ViewGroup mMainFrame;
    private ViewGroup mInCallPanel;
    private CallCard mCallCard;
    private InCallControlState mInCallControlState;
    private InCallMenu mInCallMenu;  
    private InCallTouchUi mInCallTouchUi;  
    private ManageConferenceUtils mManageConferenceUtils;
    private DTMFTwelveKeyDialer mDialer;
    private DTMFTwelveKeyDialerView mDialerView;
    private boolean mProviderOverlayVisible = false;
    private CharSequence mProviderLabel;
    private Drawable mProviderIcon;
    private Uri mProviderGatewayUri;
    private String mProviderAddress;
    public OtaUtils otaUtils;
    private EditText mWildPromptText;
    private boolean mUseTouchLockOverlay;  
    private View mTouchLockOverlay;  
    private View mTouchLockIcon;  
    private Animation mTouchLockFadeIn;
    private long mTouchLockLastTouchTime;  
    private Dialog mMmiStartedDialog;
    private AlertDialog mMissingVoicemailDialog;
    private AlertDialog mGenericErrorDialog;
    private AlertDialog mSuppServiceFailureDialog;
    private AlertDialog mWaitPromptDialog;
    private AlertDialog mWildPromptDialog;
    private AlertDialog mCallLostDialog;
    private AlertDialog mPausePromptDialog;
    private boolean mIsDestroyed = false;
    private boolean mIsForegroundActivity = false;
    private String mPostDialStrAfterPause;
    private boolean mPauseInProgress = false;
    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            if (mIsDestroyed) {
                if (DBG) log("Handler: ignoring message " + msg + "; we're destroyed!");
                return;
            }
            if (!mIsForegroundActivity) {
                if (DBG) log("Handler: handling message " + msg + " while not in foreground");
            }
            PhoneApp app = PhoneApp.getInstance();
            switch (msg.what) {
                case SUPP_SERVICE_FAILED:
                    onSuppServiceFailed((AsyncResult) msg.obj);
                    break;
                case PHONE_STATE_CHANGED:
                    onPhoneStateChanged((AsyncResult) msg.obj);
                    break;
                case PHONE_DISCONNECT:
                    onDisconnect((AsyncResult) msg.obj);
                    break;
                case EVENT_HEADSET_PLUG_STATE_CHANGED:
                    if (!isBluetoothAudioConnected()) {
                        if (msg.arg1 == 1) {
                            if (mDialer.isOpened() && !isTouchLocked()) {
                                resetTouchLockTimer();
                            }
                        }
                    }
                    updateScreen();
                    break;
                case PhoneApp.MMI_INITIATE:
                    onMMIInitiate((AsyncResult) msg.obj);
                    break;
                case PhoneApp.MMI_CANCEL:
                    onMMICancel();
                    break;
                case PhoneApp.MMI_COMPLETE:
                    MmiCode mmiCode = (MmiCode) ((AsyncResult) msg.obj).result;
                    int phoneType = mPhone.getPhoneType();
                    if (phoneType == Phone.PHONE_TYPE_CDMA) {
                        PhoneUtils.displayMMIComplete(mPhone, app, mmiCode, null, null);
                    } else if (phoneType == Phone.PHONE_TYPE_GSM) {
                        if (mmiCode.getState() != MmiCode.State.PENDING) {
                            if (DBG) log("Got MMI_COMPLETE, finishing InCallScreen...");
                            endInCallScreenSession();
                        }
                    }
                    break;
                case POST_ON_DIAL_CHARS:
                    handlePostOnDialChars((AsyncResult) msg.obj, (char) msg.arg1);
                    break;
                case ADD_VOICEMAIL_NUMBER:
                    addVoiceMailNumberPanel();
                    break;
                case DONT_ADD_VOICEMAIL_NUMBER:
                    dontAddVoiceMailNumber();
                    break;
                case DELAYED_CLEANUP_AFTER_DISCONNECT:
                    delayedCleanupAfterDisconnect();
                    break;
                case DISMISS_MENU:
                    dismissMenu(true);  
                    break;
                case ALLOW_SCREEN_ON:
                    if (VDBG) log("ALLOW_SCREEN_ON message...");
                    app.preventScreenOn(false);
                    break;
                case TOUCH_LOCK_TIMER:
                    if (VDBG) log("TOUCH_LOCK_TIMER...");
                    touchLockTimerExpired();
                    break;
                case REQUEST_UPDATE_BLUETOOTH_INDICATION:
                    if (VDBG) log("REQUEST_UPDATE_BLUETOOTH_INDICATION...");
                    updateScreen();
                    break;
                case PHONE_CDMA_CALL_WAITING:
                    if (DBG) log("Received PHONE_CDMA_CALL_WAITING event ...");
                    Connection cn = mRingingCall.getLatestConnection();
                    if (cn != null) {
                        updateScreen();
                        app.updateWakeState();
                    }
                    break;
                case THREEWAY_CALLERINFO_DISPLAY_DONE:
                    if (DBG) log("Received THREEWAY_CALLERINFO_DISPLAY_DONE event ...");
                    if (app.cdmaPhoneCallState.getCurrentCallState()
                            == CdmaPhoneCallState.PhoneCallState.THRWAY_ACTIVE) {
                        app.cdmaPhoneCallState.setThreeWayCallOrigState(false);
                        updateScreen();
                    }
                    break;
                case EVENT_OTA_PROVISION_CHANGE:
                    if (otaUtils != null) {
                        otaUtils.onOtaProvisionStatusChanged((AsyncResult) msg.obj);
                    }
                    break;
                case REQUEST_CLOSE_SPC_ERROR_NOTICE:
                    if (otaUtils != null) {
                        otaUtils.onOtaCloseSpcNotice();
                    }
                    break;
                case REQUEST_CLOSE_OTA_FAILURE_NOTICE:
                    if (otaUtils != null) {
                        otaUtils.onOtaCloseFailureNotice();
                    }
                    break;
                case EVENT_PAUSE_DIALOG_COMPLETE:
                    if (mPausePromptDialog != null) {
                        if (DBG) log("- DISMISSING mPausePromptDialog.");
                        mPausePromptDialog.dismiss();  
                        mPausePromptDialog = null;
                    }
                    break;
                case EVENT_HIDE_PROVIDER_OVERLAY:
                    mProviderOverlayVisible = false;
                    updateProviderOverlay();  
                    break;
                case REQUEST_UPDATE_TOUCH_UI:
                    updateInCallTouchUi();
                    break;
            }
        }
    };
    private final BroadcastReceiver mReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                String action = intent.getAction();
                if (action.equals(Intent.ACTION_HEADSET_PLUG)) {
                    Message message = Message.obtain(mHandler, EVENT_HEADSET_PLUG_STATE_CHANGED,
                            intent.getIntExtra("state", 0), 0);
                    mHandler.sendMessage(message);
                }
            }
        };
    @Override
    protected void onCreate(Bundle icicle) {
        if (DBG) log("onCreate()...  this = " + this);
        Profiler.callScreenOnCreate();
        super.onCreate(icicle);
        final PhoneApp app = PhoneApp.getInstance();
        app.setInCallScreenInstance(this);
        int flags = WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED;
        if (app.getPhoneState() == Phone.State.OFFHOOK) {
            flags |= WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD;
        }
        getWindow().addFlags(flags);
        setPhone(app.phone);  
        mBluetoothHandsfree = app.getBluetoothHandsfree();
        if (VDBG) log("- mBluetoothHandsfree: " + mBluetoothHandsfree);
        if (mBluetoothHandsfree != null) {
            mBluetoothHeadset = new BluetoothHeadset(this, null);
            if (VDBG) log("- Got BluetoothHeadset: " + mBluetoothHeadset);
        }
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.incall_screen);
        initInCallScreen();
        SlidingDrawer dialerDrawer;
        if (isTouchUiEnabled()) {
            mDialerView = (DTMFTwelveKeyDialerView) findViewById(R.id.non_drawer_dtmf_dialer);
            if (DBG) log("- Full touch device!  Found dialerView: " + mDialerView);
            dialerDrawer = null;  
        } else {
            mDialerView = (DTMFTwelveKeyDialerView) findViewById(R.id.dtmf_dialer);
            if (DBG) log("- Using SlidingDrawer-based dialpad.  Found dialerView: " + mDialerView);
            dialerDrawer = (SlidingDrawer) findViewById(R.id.dialer_container);
            if (DBG) log("  ...and the SlidingDrawer: " + dialerDrawer);
        }
        if (mDialerView == null) {
            Log.e(LOG_TAG, "onCreate: couldn't find dialerView", new IllegalStateException());
        }
        mDialer = new DTMFTwelveKeyDialer(this, mDialerView, dialerDrawer);
        registerForPhoneStates();
        if (icicle == null) {
            if (DBG) log("onCreate(): this is our very first launch, checking intent...");
            mInCallInitialStatus = internalResolveIntent(getIntent());
            if (DBG) log("onCreate(): mInCallInitialStatus = " + mInCallInitialStatus);
            if (mInCallInitialStatus != InCallInitStatus.SUCCESS) {
                Log.w(LOG_TAG, "onCreate: status " + mInCallInitialStatus
                      + " from internalResolveIntent()");
            }
        } else {
            mInCallInitialStatus = InCallInitStatus.SUCCESS;
        }
        mUseTouchLockOverlay = !app.proximitySensorModeEnabled();
        Profiler.callScreenCreated();
        if (DBG) log("onCreate(): exit");
    }
     void setPhone(Phone phone) {
        mPhone = phone;
        mForegroundCall = mPhone.getForegroundCall();
        mBackgroundCall = mPhone.getBackgroundCall();
        mRingingCall = mPhone.getRingingCall();
    }
    @Override
    protected void onResume() {
        if (DBG) log("onResume()...");
        super.onResume();
        mIsForegroundActivity = true;
        final PhoneApp app = PhoneApp.getInstance();
        app.disableStatusBar();
        app.setIgnoreTouchUserActivity(true);
        NotificationMgr.getDefault().getStatusBarMgr().enableExpandedView(false);
        registerReceiver(mReceiver, new IntentFilter(Intent.ACTION_HEADSET_PLUG));
        mDialer.startDialerSession();
        if (DBG) log("- onResume: initial status = " + mInCallInitialStatus);
        if (mInCallInitialStatus != InCallInitStatus.SUCCESS) {
            if (DBG) log("- onResume: failure during startup: " + mInCallInitialStatus);
            handleStartupError(mInCallInitialStatus);
            mInCallInitialStatus = InCallInitStatus.SUCCESS;
        }
        final boolean bluetoothConnected = isBluetoothAudioConnected();
        if (bluetoothConnected) {
            setVolumeControlStream(AudioManager.STREAM_BLUETOOTH_SCO);
        } else {
            setVolumeControlStream(AudioManager.STREAM_VOICE_CALL);
        }
        takeKeyEvents(true);
        boolean phoneIsCdma = (mPhone.getPhoneType() == Phone.PHONE_TYPE_CDMA);
        boolean inOtaCall = false;
        if (phoneIsCdma) {
            inOtaCall = initOtaState();
        }
        if (!inOtaCall) {
            setInCallScreenMode(InCallScreenMode.NORMAL);
        }
        mPhone.clearDisconnected();
        InCallInitStatus status = syncWithPhoneState();
        if (status != InCallInitStatus.SUCCESS) {
            if (DBG) log("- syncWithPhoneState failed! status = " + status);
        } else if (phoneIsCdma) {
            if (mInCallScreenMode == InCallScreenMode.OTA_NORMAL ||
                    mInCallScreenMode == InCallScreenMode.OTA_ENDED) {
                mDialer.setHandleVisible(false);
                if (mInCallPanel != null) mInCallPanel.setVisibility(View.GONE);
                updateScreen();
                return;
            }
        }
        EventLog.writeEvent(EventLogTags.PHONE_UI_ENTER);
        if (mPhone.getState() == Phone.State.RINGING) {
            if (VDBG) log("- posting ALLOW_SCREEN_ON message...");
            mHandler.removeMessages(ALLOW_SCREEN_ON);
            mHandler.sendEmptyMessage(ALLOW_SCREEN_ON);
        } else {
            app.preventScreenOn(false);
        }
        app.updateWakeState();
        enableTouchLock(false);
        if (mDialer.isOpened()) resetTouchLockTimer();
        if (app.getRestoreMuteOnInCallResume()) {
            PhoneUtils.restoreMuteState(mPhone);
            app.setRestoreMuteOnInCallResume(false);
        }
        Profiler.profileViewCreate(getWindow(), InCallScreen.class.getName());
        if (VDBG) log("onResume() done.");
    }
    @Override
    protected void onPause() {
        if (DBG) log("onPause()...");
        super.onPause();
        mIsForegroundActivity = false;
        mProviderOverlayVisible = false;
        updateProviderOverlay();
        final PhoneApp app = PhoneApp.getInstance();
        app.setBeginningCall(false);
        mManageConferenceUtils.stopConferenceTime();
        mDialer.onDialerKeyUp(null);
        mDialer.stopDialerSession();
        if (mHandler.hasMessages(DELAYED_CLEANUP_AFTER_DISCONNECT)
                && mPhone.getState() != Phone.State.RINGING) {
            if (DBG) log("DELAYED_CLEANUP_AFTER_DISCONNECT detected, moving UI to background.");
            endInCallScreenSession();
        }
        EventLog.writeEvent(EventLogTags.PHONE_UI_EXIT);
        dismissMenu(true);  
        dismissAllDialogs();
        NotificationMgr.getDefault().getStatusBarMgr().enableExpandedView(true);
        unregisterReceiver(mReceiver);
        mHandler.postDelayed(new Runnable() {
                public void run() {
                    app.setIgnoreTouchUserActivity(false);
                }
            }, 500);
        app.reenableStatusBar();
        app.updateWakeState();
        updateKeyguardPolicy(false);
    }
    @Override
    protected void onStop() {
        if (DBG) log("onStop()...");
        super.onStop();
        stopTimer();
        Phone.State state = mPhone.getState();
        if (DBG) log("onStop: state = " + state);
        if (state == Phone.State.IDLE) {
            final PhoneApp app = PhoneApp.getInstance();
            if ((app.cdmaOtaProvisionData != null) && (app.cdmaOtaScreenState != null)
                    && ((app.cdmaOtaScreenState.otaScreenState !=
                            CdmaOtaScreenState.OtaScreenState.OTA_STATUS_ACTIVATION)
                        && (app.cdmaOtaScreenState.otaScreenState !=
                            CdmaOtaScreenState.OtaScreenState.OTA_STATUS_SUCCESS_FAILURE_DLG)
                        && (!app.cdmaOtaProvisionData.inOtaSpcState))) {
                if (DBG) log("- onStop: calling finish() to clear activity history...");
                moveTaskToBack(true);
                if (otaUtils != null) {
                    otaUtils.cleanOtaScreen(true);
                }
            }
        }
    }
    @Override
    protected void onDestroy() {
        if (DBG) log("onDestroy()...");
        super.onDestroy();
        mIsDestroyed = true;
        final PhoneApp app = PhoneApp.getInstance();
        app.setInCallScreenInstance(null);
        if (mInCallMenu != null) {
            mInCallMenu.clearInCallScreenReference();
        }
        if (mCallCard != null) {
            mCallCard.setInCallScreenInstance(null);
        }
        if (mInCallTouchUi != null) {
            mInCallTouchUi.setInCallScreenInstance(null);
        }
        mDialer.clearInCallScreenReference();
        mDialer = null;
        unregisterForPhoneStates();
        if (mBluetoothHeadset != null) {
            mBluetoothHeadset.close();
            mBluetoothHeadset = null;
        }
        dismissAllDialogs();
    }
    @Override
    public void finish() {
        if (DBG) log("finish()...");
        moveTaskToBack(true);
    }
    public void endInCallScreenSession() {
        if (DBG) log("endInCallScreenSession()...");
        moveTaskToBack(true);
        setInCallScreenMode(InCallScreenMode.UNDEFINED);
    }
     boolean isForegroundActivity() {
        return mIsForegroundActivity;
    }
     void updateKeyguardPolicy(boolean dismissKeyguard) {
        if (dismissKeyguard) {
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD);
        } else {
            getWindow().clearFlags(WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD);
        }
    }
    private void registerForPhoneStates() {
        if (!mRegisteredForPhoneStates) {
            mPhone.registerForPreciseCallStateChanged(mHandler, PHONE_STATE_CHANGED, null);
            mPhone.registerForDisconnect(mHandler, PHONE_DISCONNECT, null);
            int phoneType = mPhone.getPhoneType();
            if (phoneType == Phone.PHONE_TYPE_GSM) {
                mPhone.registerForMmiInitiate(mHandler, PhoneApp.MMI_INITIATE, null);
                mPhone.registerForMmiComplete(mHandler, PhoneApp.MMI_COMPLETE, null);
            } else if (phoneType == Phone.PHONE_TYPE_CDMA) {
                if (DBG) log("Registering for Call Waiting.");
                mPhone.registerForCallWaiting(mHandler, PHONE_CDMA_CALL_WAITING, null);
            } else {
                throw new IllegalStateException("Unexpected phone type: " + phoneType);
            }
            mPhone.setOnPostDialCharacter(mHandler, POST_ON_DIAL_CHARS, null);
            mPhone.registerForSuppServiceFailed(mHandler, SUPP_SERVICE_FAILED, null);
            if (phoneType == Phone.PHONE_TYPE_CDMA) {
                mPhone.registerForCdmaOtaStatusChange(mHandler, EVENT_OTA_PROVISION_CHANGE, null);
            }
            mRegisteredForPhoneStates = true;
        }
    }
    private void unregisterForPhoneStates() {
        mPhone.unregisterForPreciseCallStateChanged(mHandler);
        mPhone.unregisterForDisconnect(mHandler);
        mPhone.unregisterForMmiInitiate(mHandler);
        mPhone.unregisterForCallWaiting(mHandler);
        mPhone.setOnPostDialCharacter(null, POST_ON_DIAL_CHARS, null);
        mPhone.unregisterForCdmaOtaStatusChange(mHandler);
        mRegisteredForPhoneStates = false;
    }
     void updateAfterRadioTechnologyChange() {
        if (DBG) Log.d(LOG_TAG, "updateAfterRadioTechnologyChange()...");
        unregisterForPhoneStates();
        registerForPhoneStates();
    }
    @Override
    protected void onNewIntent(Intent intent) {
        if (DBG) log("onNewIntent: intent=" + intent);
        setIntent(intent);
        mInCallInitialStatus = internalResolveIntent(intent);
        if (mInCallInitialStatus != InCallInitStatus.SUCCESS) {
            Log.w(LOG_TAG, "onNewIntent: status " + mInCallInitialStatus
                  + " from internalResolveIntent()");
        }
    }
     InCallInitStatus internalResolveIntent(Intent intent) {
        if (intent == null || intent.getAction() == null) {
            return InCallInitStatus.SUCCESS;
        }
        checkIsOtaCall(intent);
        String action = intent.getAction();
        if (DBG) log("internalResolveIntent: action=" + action);
        final PhoneApp app = PhoneApp.getInstance();
        if ((action.equals(ACTION_SHOW_ACTIVATION))
                && ((mPhone.getPhoneType() == Phone.PHONE_TYPE_CDMA))) {
            setInCallScreenMode(InCallScreenMode.OTA_NORMAL);
            if ((app.cdmaOtaProvisionData != null)
                    && (!app.cdmaOtaProvisionData.isOtaCallIntentProcessed)) {
                app.cdmaOtaProvisionData.isOtaCallIntentProcessed = true;
                app.cdmaOtaScreenState.otaScreenState =
                        CdmaOtaScreenState.OtaScreenState.OTA_STATUS_ACTIVATION;
            }
            return InCallInitStatus.SUCCESS;
        } else if (action.equals(Intent.ACTION_ANSWER)) {
            internalAnswerCall();
            app.setRestoreMuteOnInCallResume(false);
            return InCallInitStatus.SUCCESS;
        } else if (action.equals(Intent.ACTION_CALL)
                || action.equals(Intent.ACTION_CALL_EMERGENCY)) {
            app.setRestoreMuteOnInCallResume(false);
            if (PhoneUtils.hasPhoneProviderExtras(intent)) {
                mProviderLabel = PhoneUtils.getProviderLabel(this, intent);
                mProviderIcon = PhoneUtils.getProviderIcon(this, intent);
                mProviderGatewayUri = PhoneUtils.getProviderGatewayUri(intent);
                mProviderAddress = PhoneUtils.formatProviderUri(mProviderGatewayUri);
                mProviderOverlayVisible = true;
                if (TextUtils.isEmpty(mProviderLabel) || null == mProviderIcon ||
                    null == mProviderGatewayUri || TextUtils.isEmpty(mProviderAddress)) {
                    clearProvider();
                }
            } else {
                clearProvider();
            }
            InCallInitStatus status = placeCall(intent);
            if (status == InCallInitStatus.SUCCESS) {
                app.setBeginningCall(true);
            }
            return status;
        } else if (action.equals(intent.ACTION_MAIN)) {
            if ((mInCallScreenMode == InCallScreenMode.OTA_NORMAL)
                    || (mInCallScreenMode == InCallScreenMode.OTA_ENDED)) {
                updateScreen();
                return InCallInitStatus.SUCCESS;
            }
            if (intent.hasExtra(SHOW_DIALPAD_EXTRA)) {
                boolean showDialpad = intent.getBooleanExtra(SHOW_DIALPAD_EXTRA, false);
                if (VDBG) log("- internalResolveIntent: SHOW_DIALPAD_EXTRA: " + showDialpad);
                if (showDialpad) {
                    mDialer.openDialer(false);  
                } else {
                    mDialer.closeDialer(false);  
                }
            }
            return InCallInitStatus.SUCCESS;
        } else if (action.equals(ACTION_UNDEFINED)) {
            return InCallInitStatus.SUCCESS;
        } else {
            Log.w(LOG_TAG, "internalResolveIntent: unexpected intent action: " + action);
            return InCallInitStatus.SUCCESS;
        }
    }
    private void stopTimer() {
        if (mCallCard != null) mCallCard.stopTimer();
    }
    private void initInCallScreen() {
        if (VDBG) log("initInCallScreen()...");
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_IGNORE_CHEEK_PRESSES);
        getWindow().setFormat(PixelFormat.RGBX_8888);
        mMainFrame = (ViewGroup) findViewById(R.id.mainFrame);
        mInCallPanel = (ViewGroup) findViewById(R.id.inCallPanel);
        mCallCard = (CallCard) findViewById(R.id.callCard);
        if (VDBG) log("  - mCallCard = " + mCallCard);
        mCallCard.setInCallScreenInstance(this);
        initInCallTouchUi();
        mInCallControlState = new InCallControlState(this, mPhone);
        mManageConferenceUtils = new ManageConferenceUtils(this, mPhone);
    }
    private boolean phoneIsInUse() {
        return mPhone.getState() != Phone.State.IDLE;
    }
    private boolean handleDialerKeyDown(int keyCode, KeyEvent event) {
        if (VDBG) log("handleDialerKeyDown: keyCode " + keyCode + ", event " + event + "...");
        if (okToDialDTMFTones()) {
            return mDialer.onDialerKeyDown(event);
        }
        return false;
    }
    @Override
    public void onBackPressed() {
        if (DBG) log("onBackPressed()...");
        if (!mRingingCall.isIdle()) {
            if (getResources().getBoolean(R.bool.allow_back_key_to_reject_incoming_call)) {
                if (DBG) log("BACK key while ringing: reject the call");
                internalHangupRingingCall();
                super.onBackPressed();
                return;
            } else {
                if (DBG) log("BACK key while ringing: ignored");
                return;
            }
        }
        if (mDialer.isOpened()) {
            enableTouchLock(false);
            mDialer.closeDialer(true);  
            return;
        }
        if (mInCallScreenMode == InCallScreenMode.MANAGE_CONFERENCE) {
            setInCallScreenMode(InCallScreenMode.NORMAL);
            return;
        }
        super.onBackPressed();
    }
    private boolean handleCallKey() {
        final boolean hasRingingCall = !mRingingCall.isIdle();
        final boolean hasActiveCall = !mForegroundCall.isIdle();
        final boolean hasHoldingCall = !mBackgroundCall.isIdle();
        int phoneType = mPhone.getPhoneType();
        if (phoneType == Phone.PHONE_TYPE_CDMA) {
            PhoneApp app = PhoneApp.getInstance();
            CdmaPhoneCallState.PhoneCallState currCallState =
                app.cdmaPhoneCallState.getCurrentCallState();
            if (hasRingingCall) {
                if (DBG) log("answerCall: First Incoming and Call Waiting scenario");
                internalAnswerCall();  
            } else if ((currCallState == CdmaPhoneCallState.PhoneCallState.THRWAY_ACTIVE)
                    && (hasActiveCall)) {
                if (DBG) log("answerCall: Merge 3-way call scenario");
                PhoneUtils.mergeCalls(mPhone);
            } else if (currCallState == CdmaPhoneCallState.PhoneCallState.CONF_CALL) {
                if (DBG) log("answerCall: Switch btwn 2 calls scenario");
                internalSwapCalls();
            }
        } else if (phoneType == Phone.PHONE_TYPE_GSM) {
            if (hasRingingCall) {
                Log.w(LOG_TAG, "handleCallKey: incoming call is ringing!"
                      + " (PhoneWindowManager should have handled this key.)");
                internalAnswerCall();
            } else if (hasActiveCall && hasHoldingCall) {
                if (DBG) log("handleCallKey: both lines in use ==> swap calls.");
                internalSwapCalls();
            } else if (hasHoldingCall) {
                if (DBG) log("handleCallKey: call on hold ==> unhold.");
                PhoneUtils.switchHoldingAndActive(mPhone);  
            } else {
                if (VDBG) log("handleCallKey: call in foregound ==> ignoring.");
            }
        } else {
            throw new IllegalStateException("Unexpected phone type: " + phoneType);
        }
        return true;
    }
    boolean isKeyEventAcceptableDTMF (KeyEvent event) {
        return (mDialer != null && mDialer.isKeyEventAcceptable(event));
    }
    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        if (VDBG) log("onWindowFocusChanged(" + hasFocus + ")...");
        if (!hasFocus && mDialer != null) {
            if (VDBG) log("- onWindowFocusChanged: faking onDialerKeyUp()...");
            mDialer.onDialerKeyUp(null);
        }
    }
    @Override
    public boolean dispatchKeyEvent(KeyEvent event) {
        switch (event.getKeyCode()) {
            case KeyEvent.KEYCODE_DPAD_CENTER:
            case KeyEvent.KEYCODE_DPAD_UP:
            case KeyEvent.KEYCODE_DPAD_DOWN:
            case KeyEvent.KEYCODE_DPAD_LEFT:
            case KeyEvent.KEYCODE_DPAD_RIGHT:
                if (mDialer.isOpened() && isTouchLocked()) {
                    if (DBG) log("- ignoring DPAD event while touch-locked...");
                    return true;
                }
                break;
            default:
                break;
        }
        return super.dispatchKeyEvent(event);
    }
    @Override
    public boolean onKeyUp(int keyCode, KeyEvent event) {
        if ((mDialer != null) && (mDialer.onDialerKeyUp(event))){
            return true;
        } else if (keyCode == KeyEvent.KEYCODE_CALL) {
            return true;
        }
        return super.onKeyUp(keyCode, event);
    }
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        switch (keyCode) {
            case KeyEvent.KEYCODE_CALL:
                boolean handled = handleCallKey();
                if (!handled) {
                    Log.w(LOG_TAG, "InCallScreen should always handle KEYCODE_CALL in onKeyDown");
                }
                return true;
            case KeyEvent.KEYCODE_CAMERA:
                return true;
            case KeyEvent.KEYCODE_VOLUME_UP:
            case KeyEvent.KEYCODE_VOLUME_DOWN:
                if (mPhone.getState() == Phone.State.RINGING) {
                    Log.w(LOG_TAG, "VOLUME key: incoming call is ringing!"
                          + " (PhoneWindowManager should have handled this key.)");
                    final CallNotifier notifier = PhoneApp.getInstance().notifier;
                    if (notifier.isRinging()) {
                        PhoneUtils.setAudioControlState(PhoneUtils.AUDIO_IDLE);
                        if (DBG) log("VOLUME key: silence ringer");
                        notifier.silenceRinger();
                    }
                    return true;
                }
                break;
            case KeyEvent.KEYCODE_MENU:
                if (mDialer.isOpened() && isTouchLocked()) {
                    if (VDBG) log("- allowing MENU to dismiss touch lock overlay...");
                    enableTouchLock(false);
                    resetTouchLockTimer();
                    return true;
                }
                break;
            case KeyEvent.KEYCODE_MUTE:
                PhoneUtils.setMute(mPhone, !PhoneUtils.getMute(mPhone));
                return true;
            case KeyEvent.KEYCODE_SLASH:
                if (VDBG) {
                    log("----------- InCallScreen View dump --------------");
                    Window w = this.getWindow();
                    View decorView = w.getDecorView();
                    decorView.debug();
                    return true;
                }
                break;
            case KeyEvent.KEYCODE_EQUALS:
                if (VDBG) {
                    log("----------- InCallScreen call state dump --------------");
                    PhoneUtils.dumpCallState(mPhone);
                    return true;
                }
                break;
            case KeyEvent.KEYCODE_GRAVE:
                if (VDBG) {
                    log("------------ Temp testing -----------------");
                    return true;
                }
                break;
        }
        if (event.getRepeatCount() == 0 && handleDialerKeyDown(keyCode, event)) {
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }
    void onSuppServiceFailed(AsyncResult r) {
        Phone.SuppService service = (Phone.SuppService) r.result;
        if (DBG) log("onSuppServiceFailed: " + service);
        int errorMessageResId;
        switch (service) {
            case SWITCH:
                errorMessageResId = R.string.incall_error_supp_service_switch;
                break;
            case SEPARATE:
                errorMessageResId = R.string.incall_error_supp_service_separate;
                break;
            case TRANSFER:
                errorMessageResId = R.string.incall_error_supp_service_transfer;
                break;
            case CONFERENCE:
                errorMessageResId = R.string.incall_error_supp_service_conference;
                break;
            case REJECT:
                errorMessageResId = R.string.incall_error_supp_service_reject;
                break;
            case HANGUP:
                errorMessageResId = R.string.incall_error_supp_service_hangup;
                break;
            case UNKNOWN:
            default:
                errorMessageResId = R.string.incall_error_supp_service_unknown;
                break;
        }
        if (mSuppServiceFailureDialog != null) {
            if (DBG) log("- DISMISSING mSuppServiceFailureDialog.");
            mSuppServiceFailureDialog.dismiss();  
            mSuppServiceFailureDialog = null;
        }
        mSuppServiceFailureDialog = new AlertDialog.Builder(this)
                .setMessage(errorMessageResId)
                .setPositiveButton(R.string.ok, null)
                .setCancelable(true)
                .create();
        mSuppServiceFailureDialog.getWindow().addFlags(
                WindowManager.LayoutParams.FLAG_BLUR_BEHIND);
        mSuppServiceFailureDialog.show();
    }
    private void onPhoneStateChanged(AsyncResult r) {
        if (DBG) log("onPhoneStateChanged()...");
        if (!mIsForegroundActivity) {
            if (DBG) log("onPhoneStateChanged: Activity not in foreground! Bailing out...");
            return;
        }
        updateScreen();
        PhoneApp.getInstance().updateWakeState();
    }
    private void onDisconnect(AsyncResult r) {
        Connection c = (Connection) r.result;
        Connection.DisconnectCause cause = c.getDisconnectCause();
        if (DBG) log("onDisconnect: " + c + ", cause=" + cause);
        boolean currentlyIdle = !phoneIsInUse();
        int autoretrySetting = AUTO_RETRY_OFF;
        boolean phoneIsCdma = (mPhone.getPhoneType() == Phone.PHONE_TYPE_CDMA);
        if (phoneIsCdma) {
            if (currentlyIdle) {
                autoretrySetting = android.provider.Settings.System.getInt(mPhone.getContext().
                        getContentResolver(), android.provider.Settings.System.CALL_AUTO_RETRY, 0);
            }
        }
        final PhoneApp app = PhoneApp.getInstance();
        if ((mInCallScreenMode == InCallScreenMode.OTA_NORMAL)
                && ((app.cdmaOtaProvisionData != null)
                && (!app.cdmaOtaProvisionData.inOtaSpcState))) {
            setInCallScreenMode(InCallScreenMode.OTA_ENDED);
            updateScreen();
            return;
        } else if ((mInCallScreenMode == InCallScreenMode.OTA_ENDED)
                || ((app.cdmaOtaProvisionData != null) && app.cdmaOtaProvisionData.inOtaSpcState)) {
           if (DBG) log("onDisconnect: OTA Call end already handled");
           return;
        }
        mDialer.clearDigits();
        if (cause == Connection.DisconnectCause.CALL_BARRED) {
            showGenericErrorDialog(R.string.callFailed_cb_enabled, false);
            return;
        } else if (cause == Connection.DisconnectCause.FDN_BLOCKED) {
            showGenericErrorDialog(R.string.callFailed_fdn_only, false);
            return;
        } else if (cause == Connection.DisconnectCause.CS_RESTRICTED) {
            showGenericErrorDialog(R.string.callFailed_dsac_restricted, false);
            return;
        } else if (cause == Connection.DisconnectCause.CS_RESTRICTED_EMERGENCY) {
            showGenericErrorDialog(R.string.callFailed_dsac_restricted_emergency, false);
            return;
        } else if (cause == Connection.DisconnectCause.CS_RESTRICTED_NORMAL) {
            showGenericErrorDialog(R.string.callFailed_dsac_restricted_normal, false);
            return;
        }
        if (phoneIsCdma) {
            Call.State callState = PhoneApp.getInstance().notifier.getPreviousCdmaCallState();
            if ((callState == Call.State.ACTIVE)
                    && (cause != Connection.DisconnectCause.INCOMING_MISSED)
                    && (cause != Connection.DisconnectCause.NORMAL)
                    && (cause != Connection.DisconnectCause.LOCAL)
                    && (cause != Connection.DisconnectCause.INCOMING_REJECTED)) {
                showCallLostDialog();
            } else if ((callState == Call.State.DIALING || callState == Call.State.ALERTING)
                        && (cause != Connection.DisconnectCause.INCOMING_MISSED)
                        && (cause != Connection.DisconnectCause.NORMAL)
                        && (cause != Connection.DisconnectCause.LOCAL)
                        && (cause != Connection.DisconnectCause.INCOMING_REJECTED)) {
                    if (mNeedShowCallLostDialog) {
                        showCallLostDialog();
                        mNeedShowCallLostDialog = false;
                    } else {
                        if (autoretrySetting == AUTO_RETRY_OFF) {
                            showCallLostDialog();
                            mNeedShowCallLostDialog = false;
                        } else {
                            mNeedShowCallLostDialog = true;
                        }
                    }
            }
        }
        Call call = c.getCall();
        if (call != null) {
            List<Connection> connections = call.getConnections();
            if (connections != null && connections.size() > 1) {
                for (Connection conn : connections) {
                    if (conn.getState() == Call.State.ACTIVE) {
                        if (VDBG) log("- Still-active conf call; clearing DISCONNECTED...");
                        app.updateWakeState();
                        mPhone.clearDisconnected();  
                        break;
                    }
                }
            }
        }
        int emergencyCallRetryCount = getIntent().getIntExtra(
                EmergencyCallHandler.EMERGENCY_CALL_RETRY_KEY,
                EmergencyCallHandler.INITIAL_ATTEMPT);
        boolean bailOutImmediately =
                ((cause == Connection.DisconnectCause.INCOMING_MISSED)
                 || (cause == Connection.DisconnectCause.INCOMING_REJECTED)
                 || ((cause == Connection.DisconnectCause.OUT_OF_SERVICE)
                         && (emergencyCallRetryCount > 0)))
                && currentlyIdle;
        if (bailOutImmediately) {
            if (VDBG) log("- onDisconnect: bailOutImmediately...");
            delayedCleanupAfterDisconnect();
            if ((cause == Connection.DisconnectCause.OUT_OF_SERVICE)
                    && (emergencyCallRetryCount > 0)) {
                startActivity(getIntent()
                        .setClassName(this, EmergencyCallHandler.class.getName()));
            }
        } else {
            if (VDBG) log("- onDisconnect: delayed bailout...");
            updateScreen();
            if (currentlyIdle
                && ((mForegroundCall.getState() == Call.State.DISCONNECTED)
                    || (mBackgroundCall.getState() == Call.State.DISCONNECTED))) {
                if (VDBG) log("- onDisconnect: switching to 'Call ended' state...");
                setInCallScreenMode(InCallScreenMode.CALL_ENDED);
            }
            final boolean hasActiveCall = !mForegroundCall.isIdle();
            if (!hasActiveCall) {
                if (VDBG) log("- onDisconnect: cleaning up after FG call disconnect...");
                if (mWaitPromptDialog != null) {
                    if (VDBG) log("- DISMISSING mWaitPromptDialog.");
                    mWaitPromptDialog.dismiss();  
                    mWaitPromptDialog = null;
                }
                if (mWildPromptDialog != null) {
                    if (VDBG) log("- DISMISSING mWildPromptDialog.");
                    mWildPromptDialog.dismiss();  
                    mWildPromptDialog = null;
                }
                if (mPausePromptDialog != null) {
                    if (DBG) log("- DISMISSING mPausePromptDialog.");
                    mPausePromptDialog.dismiss();  
                    mPausePromptDialog = null;
                }
            }
            if (mPhone.getPhoneType() == Phone.PHONE_TYPE_CDMA) {
                if (!currentlyIdle) {
                    mPhone.clearDisconnected();
                    if (DBG) log("onDisconnect: Call Collision case - staying on InCallScreen.");
                    if (DBG) PhoneUtils.dumpCallState(mPhone);
                    return;
                }
            }
            int callEndedDisplayDelay =
                    (cause == Connection.DisconnectCause.LOCAL)
                    ? CALL_ENDED_SHORT_DELAY : CALL_ENDED_LONG_DELAY;
            mHandler.removeMessages(DELAYED_CLEANUP_AFTER_DISCONNECT);
            mHandler.sendEmptyMessageDelayed(DELAYED_CLEANUP_AFTER_DISCONNECT,
                                             callEndedDisplayDelay);
        }
        mHandler.removeMessages(THREEWAY_CALLERINFO_DISPLAY_DONE);
    }
    private void onMMIInitiate(AsyncResult r) {
        if (VDBG) log("onMMIInitiate()...  AsyncResult r = " + r);
        if (!mIsForegroundActivity) {
            if (VDBG) log("Activity not in foreground! Bailing out...");
            return;
        }
        dismissAllDialogs();
        MmiCode mmiCode = (MmiCode) r.result;
        if (VDBG) log("  - MmiCode: " + mmiCode);
        Message message = Message.obtain(mHandler, PhoneApp.MMI_CANCEL);
        mMmiStartedDialog = PhoneUtils.displayMMIInitiate(this, mmiCode,
                                                          message, mMmiStartedDialog);
    }
    private void onMMICancel() {
        if (VDBG) log("onMMICancel()...");
        PhoneUtils.cancelMmiCode(mPhone);
        if (DBG) log("onMMICancel: finishing InCallScreen...");
        endInCallScreenSession();
    }
    private void handlePostOnDialChars(AsyncResult r, char ch) {
        Connection c = (Connection) r.result;
        if (c != null) {
            Connection.PostDialState state =
                    (Connection.PostDialState) r.userObj;
            if (VDBG) log("handlePostOnDialChar: state = " +
                    state + ", ch = " + ch);
            int phoneType = mPhone.getPhoneType();
            switch (state) {
                case STARTED:
                    if (phoneType == Phone.PHONE_TYPE_CDMA) {
                        mDialer.stopLocalToneCdma();
                        if (mPauseInProgress) {
                            showPausePromptDialogCDMA(c, mPostDialStrAfterPause);
                        }
                        mPauseInProgress = false;
                        mDialer.startLocalToneCdma(ch);
                    }
                    break;
                case WAIT:
                    if (DBG) log("handlePostOnDialChars: show WAIT prompt...");
                    String postDialStr = c.getRemainingPostDialString();
                    if (phoneType == Phone.PHONE_TYPE_CDMA) {
                        mDialer.stopLocalToneCdma();
                        showWaitPromptDialogCDMA(c, postDialStr);
                    } else if (phoneType == Phone.PHONE_TYPE_GSM) {
                        showWaitPromptDialogGSM(c, postDialStr);
                    } else {
                        throw new IllegalStateException("Unexpected phone type: " + phoneType);
                    }
                    break;
                case WILD:
                    if (DBG) log("handlePostOnDialChars: show WILD prompt");
                    showWildPromptDialog(c);
                    break;
                case COMPLETE:
                    if (phoneType == Phone.PHONE_TYPE_CDMA) {
                        mDialer.stopLocalToneCdma();
                    }
                    break;
                case PAUSE:
                    if (phoneType == Phone.PHONE_TYPE_CDMA) {
                        mPostDialStrAfterPause = c.getRemainingPostDialString();
                        mDialer.stopLocalToneCdma();
                        mPauseInProgress = true;
                    }
                    break;
                default:
                    break;
            }
        }
    }
    private void showWaitPromptDialogGSM(final Connection c, String postDialStr) {
        if (DBG) log("showWaitPromptDialogGSM: '" + postDialStr + "'...");
        Resources r = getResources();
        StringBuilder buf = new StringBuilder();
        buf.append(r.getText(R.string.wait_prompt_str));
        buf.append(postDialStr);
        if (mWaitPromptDialog != null) {
            if (DBG) log("- DISMISSING mWaitPromptDialog.");
            mWaitPromptDialog.dismiss();  
            mWaitPromptDialog = null;
        }
        mWaitPromptDialog = new AlertDialog.Builder(this)
                .setMessage(buf.toString())
                .setPositiveButton(R.string.send_button, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int whichButton) {
                            if (DBG) log("handle WAIT_PROMPT_CONFIRMED, proceed...");
                            c.proceedAfterWaitChar();
                            PhoneApp.getInstance().pokeUserActivity();
                        }
                    })
                .setOnCancelListener(new DialogInterface.OnCancelListener() {
                        public void onCancel(DialogInterface dialog) {
                            if (DBG) log("handle POST_DIAL_CANCELED!");
                            c.cancelPostDial();
                            PhoneApp.getInstance().pokeUserActivity();
                        }
                    })
                .create();
        mWaitPromptDialog.getWindow().addFlags(
                WindowManager.LayoutParams.FLAG_BLUR_BEHIND);
        mWaitPromptDialog.show();
    }
    private void showWaitPromptDialogCDMA(final Connection c, String postDialStr) {
        if (DBG) log("showWaitPromptDialogCDMA: '" + postDialStr + "'...");
        Resources r = getResources();
        StringBuilder buf = new StringBuilder();
        buf.append(r.getText(R.string.wait_prompt_str));
        buf.append(postDialStr);
        if (mWaitPromptDialog != null) {
            if (DBG) log("- DISMISSING mWaitPromptDialog.");
            mWaitPromptDialog.dismiss();  
            mWaitPromptDialog = null;
        }
        mWaitPromptDialog = new AlertDialog.Builder(this)
                .setMessage(buf.toString())
                .setPositiveButton(R.string.pause_prompt_yes,
                    new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int whichButton) {
                            if (DBG) log("handle WAIT_PROMPT_CONFIRMED, proceed...");
                            c.proceedAfterWaitChar();
                        }
                    })
                .setNegativeButton(R.string.pause_prompt_no, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int whichButton) {
                            if (DBG) log("handle POST_DIAL_CANCELED!");
                            c.cancelPostDial();
                        }
                    })
                .create();
        mWaitPromptDialog.getWindow().addFlags(
                WindowManager.LayoutParams.FLAG_BLUR_BEHIND);
        mWaitPromptDialog.show();
    }
    private void showPausePromptDialogCDMA(final Connection c, String postDialStrAfterPause) {
        Resources r = getResources();
        StringBuilder buf = new StringBuilder();
        buf.append(r.getText(R.string.pause_prompt_str));
        buf.append(postDialStrAfterPause);
        if (mPausePromptDialog != null) {
            if (DBG) log("- DISMISSING mPausePromptDialog.");
            mPausePromptDialog.dismiss();  
            mPausePromptDialog = null;
        }
        mPausePromptDialog = new AlertDialog.Builder(this)
                .setMessage(buf.toString())
                .create();
        mPausePromptDialog.show();
        Message msg = Message.obtain(mHandler, EVENT_PAUSE_DIALOG_COMPLETE);
        mHandler.sendMessageDelayed(msg, PAUSE_PROMPT_DIALOG_TIMEOUT);
    }
    private View createWildPromptView() {
        LinearLayout result = new LinearLayout(this);
        result.setOrientation(LinearLayout.VERTICAL);
        result.setPadding(5, 5, 5, 5);
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.WRAP_CONTENT);
        TextView promptMsg = new TextView(this);
        promptMsg.setTextSize(14);
        promptMsg.setTypeface(Typeface.DEFAULT_BOLD);
        promptMsg.setText(getResources().getText(R.string.wild_prompt_str));
        result.addView(promptMsg, lp);
        mWildPromptText = new EditText(this);
        mWildPromptText.setKeyListener(DialerKeyListener.getInstance());
        mWildPromptText.setMovementMethod(null);
        mWildPromptText.setTextSize(14);
        mWildPromptText.setMaxLines(1);
        mWildPromptText.setHorizontallyScrolling(true);
        mWildPromptText.setBackgroundResource(android.R.drawable.editbox_background);
        LinearLayout.LayoutParams lp2 = new LinearLayout.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.WRAP_CONTENT);
        lp2.setMargins(0, 3, 0, 0);
        result.addView(mWildPromptText, lp2);
        return result;
    }
    private void showWildPromptDialog(final Connection c) {
        View v = createWildPromptView();
        if (mWildPromptDialog != null) {
            if (VDBG) log("- DISMISSING mWildPromptDialog.");
            mWildPromptDialog.dismiss();  
            mWildPromptDialog = null;
        }
        mWildPromptDialog = new AlertDialog.Builder(this)
                .setView(v)
                .setPositiveButton(
                        R.string.send_button,
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int whichButton) {
                                if (VDBG) log("handle WILD_PROMPT_CHAR_ENTERED, proceed...");
                                String replacement = null;
                                if (mWildPromptText != null) {
                                    replacement = mWildPromptText.getText().toString();
                                    mWildPromptText = null;
                                }
                                c.proceedAfterWildChar(replacement);
                                PhoneApp.getInstance().pokeUserActivity();
                            }
                        })
                .setOnCancelListener(
                        new DialogInterface.OnCancelListener() {
                            public void onCancel(DialogInterface dialog) {
                                if (VDBG) log("handle POST_DIAL_CANCELED!");
                                c.cancelPostDial();
                                PhoneApp.getInstance().pokeUserActivity();
                            }
                        })
                .create();
        mWildPromptDialog.getWindow().addFlags(
                WindowManager.LayoutParams.FLAG_BLUR_BEHIND);
        mWildPromptDialog.show();
        mWildPromptText.requestFocus();
    }
    private void updateScreen() {
        if (DBG) log("updateScreen()...");
        if (!mIsForegroundActivity) {
            if (DBG) log("- updateScreen: not the foreground Activity! Bailing out...");
            return;
        }
        if (mInCallMenu != null) {
            if (DBG) log("- updateScreen: updating menu items...");
            boolean okToShowMenu = mInCallMenu.updateItems(mPhone);
            if (!okToShowMenu) {
                if (DBG) log("- updateScreen: Tried to update menu; now need to dismiss!");
                dismissMenu(true);  
            }
        }
        final PhoneApp app = PhoneApp.getInstance();
        if (mInCallScreenMode == InCallScreenMode.OTA_NORMAL) {
            if (DBG) log("- updateScreen: OTA call state NORMAL...");
            if (otaUtils != null) {
                if (DBG) log("- updateScreen: otaUtils is not null, call otaShowProperScreen");
                otaUtils.otaShowProperScreen();
            }
            return;
        } else if (mInCallScreenMode == InCallScreenMode.OTA_ENDED) {
            if (DBG) log("- updateScreen: OTA call ended state ...");
            PhoneApp.getInstance().wakeUpScreen();
            if (app.cdmaOtaScreenState.otaScreenState
                == CdmaOtaScreenState.OtaScreenState.OTA_STATUS_ACTIVATION) {
                if (DBG) log("- updateScreen: OTA_STATUS_ACTIVATION");
                if (otaUtils != null) {
                    if (DBG) log("- updateScreen: otaUtils is not null, "
                                  + "call otaShowActivationScreen");
                    otaUtils.otaShowActivateScreen();
                }
            } else {
                if (DBG) log("- updateScreen: OTA Call end state for Dialogs");
                if (otaUtils != null) {
                    if (DBG) log("- updateScreen: Show OTA Success Failure dialog");
                    otaUtils.otaShowSuccessFailure();
                }
            }
            return;
        } else if (mInCallScreenMode == InCallScreenMode.MANAGE_CONFERENCE) {
            if (DBG) log("- updateScreen: manage conference mode (NOT updating in-call UI)...");
            updateManageConferencePanelIfNecessary();
            return;
        } else if (mInCallScreenMode == InCallScreenMode.CALL_ENDED) {
            if (DBG) log("- updateScreen: call ended state (NOT updating in-call UI)...");
            updateInCallBackground();
            return;
        }
        if (DBG) log("- updateScreen: updating the in-call UI...");
        mCallCard.updateState(mPhone);
        updateDialpadVisibility();
        updateInCallTouchUi();
        updateProviderOverlay();
        updateMenuButtonHint();
        updateInCallBackground();
        if (!mRingingCall.isIdle()) {
            dismissAllDialogs();
        } else {
            String postDialStr = null;
            List<Connection> fgConnections = mForegroundCall.getConnections();
            int phoneType = mPhone.getPhoneType();
            if (phoneType == Phone.PHONE_TYPE_CDMA) {
                Connection fgLatestConnection = mForegroundCall.getLatestConnection();
                if (PhoneApp.getInstance().cdmaPhoneCallState.getCurrentCallState() ==
                        CdmaPhoneCallState.PhoneCallState.CONF_CALL) {
                    for (Connection cn : fgConnections) {
                        if ((cn != null) && (cn.getPostDialState() ==
                                Connection.PostDialState.WAIT)) {
                            cn.cancelPostDial();
                        }
                    }
                } else if ((fgLatestConnection != null)
                     && (fgLatestConnection.getPostDialState() == Connection.PostDialState.WAIT)) {
                    if(DBG) log("show the Wait dialog for CDMA");
                    postDialStr = fgLatestConnection.getRemainingPostDialString();
                    showWaitPromptDialogCDMA(fgLatestConnection, postDialStr);
                }
            } else if (phoneType == Phone.PHONE_TYPE_GSM) {
                for (Connection cn : fgConnections) {
                    if ((cn != null) && (cn.getPostDialState() == Connection.PostDialState.WAIT)) {
                        postDialStr = cn.getRemainingPostDialString();
                        showWaitPromptDialogGSM(cn, postDialStr);
                    }
                }
            } else {
                throw new IllegalStateException("Unexpected phone type: " + phoneType);
            }
        }
    }
    private InCallInitStatus syncWithPhoneState() {
        boolean updateSuccessful = false;
        if (DBG) log("syncWithPhoneState()...");
        if (DBG) PhoneUtils.dumpCallState(mPhone);
        if (VDBG) dumpBluetoothState();
        int phoneType = mPhone.getPhoneType();
        if ((phoneType == Phone.PHONE_TYPE_CDMA)
                && ((mInCallScreenMode == InCallScreenMode.OTA_NORMAL)
                || (mInCallScreenMode == InCallScreenMode.OTA_ENDED))) {
            return InCallInitStatus.SUCCESS;
        }
        if ((phoneType == Phone.PHONE_TYPE_CDMA)
                || !mForegroundCall.isIdle() || !mBackgroundCall.isIdle() || !mRingingCall.isIdle()
                || !mPhone.getPendingMmiCodes().isEmpty()) {
            if (VDBG) log("syncWithPhoneState: it's ok to be here; update the screen...");
            updateScreen();
            return InCallInitStatus.SUCCESS;
        }
        if (DBG) log("syncWithPhoneState: phone is idle; we shouldn't be here!");
        return InCallInitStatus.PHONE_NOT_IN_USE;
    }
    private String getInitialNumber(Intent intent)
            throws PhoneUtils.VoiceMailNumberMissingException {
        String action = intent.getAction();
        if (action == null) {
            return null;
        }
        if (action != null && action.equals(Intent.ACTION_CALL) &&
                intent.hasExtra(Intent.EXTRA_PHONE_NUMBER)) {
            return intent.getStringExtra(Intent.EXTRA_PHONE_NUMBER);
        }
        return PhoneUtils.getNumberFromIntent(this, mPhone, intent);
    }
    private InCallInitStatus placeCall(Intent intent) {
        if (VDBG) log("placeCall()...  intent = " + intent);
        String number;
        InCallInitStatus okToCallStatus = checkIfOkToInitiateOutgoingCall();
        try {
            number = getInitialNumber(intent);
        } catch (PhoneUtils.VoiceMailNumberMissingException ex) {
            if (okToCallStatus != InCallInitStatus.SUCCESS) {
                if (DBG) log("Voicemail number not reachable in current SIM card state.");
                return okToCallStatus;
            }
            if (DBG) log("VoiceMailNumberMissingException from getInitialNumber()");
            return InCallInitStatus.VOICEMAIL_NUMBER_MISSING;
        }
        if (number == null) {
            Log.w(LOG_TAG, "placeCall: couldn't get a phone number from Intent " + intent);
            return InCallInitStatus.NO_PHONE_NUMBER_SUPPLIED;
        }
        boolean isEmergencyNumber = PhoneNumberUtils.isEmergencyNumber(number);
        boolean isEmergencyIntent = Intent.ACTION_CALL_EMERGENCY.equals(intent.getAction());
        if (isEmergencyNumber && !isEmergencyIntent) {
            Log.e(LOG_TAG, "Non-CALL_EMERGENCY Intent " + intent
                    + " attempted to call emergency number " + number
                    + ".");
            return InCallInitStatus.CALL_FAILED;
        } else if (!isEmergencyNumber && isEmergencyIntent) {
            Log.e(LOG_TAG, "Received CALL_EMERGENCY Intent " + intent
                    + " with non-emergency number " + number
                    + " -- failing call.");
            return InCallInitStatus.CALL_FAILED;
        }
        if (isEmergencyNumber
            && ((okToCallStatus == InCallInitStatus.EMERGENCY_ONLY)
                || (okToCallStatus == InCallInitStatus.OUT_OF_SERVICE))) {
            if (DBG) log("placeCall: Emergency number detected with status = " + okToCallStatus);
            okToCallStatus = InCallInitStatus.SUCCESS;
            if (DBG) log("==> UPDATING status to: " + okToCallStatus);
        }
        if (okToCallStatus != InCallInitStatus.SUCCESS) {
            if (isEmergencyNumber && (okToCallStatus == InCallInitStatus.POWER_OFF)) {
                startActivity(intent.setClassName(this, EmergencyCallHandler.class.getName()));
                if (DBG) log("placeCall: starting EmergencyCallHandler, finishing InCallScreen...");
                endInCallScreenSession();
                return InCallInitStatus.SUCCESS;
            } else {
                return okToCallStatus;
            }
        }
        final PhoneApp app = PhoneApp.getInstance();
        if ((mPhone.getPhoneType() == Phone.PHONE_TYPE_CDMA) && (mPhone.isOtaSpNumber(number))) {
            if (DBG) log("placeCall: isOtaSpNumber() returns true");
            setInCallScreenMode(InCallScreenMode.OTA_NORMAL);
            if (app.cdmaOtaProvisionData != null) {
                app.cdmaOtaProvisionData.isOtaCallCommitted = false;
            }
        }
        mNeedShowCallLostDialog = false;
        int callStatus;
        Uri contactUri = intent.getData();
        if (null != mProviderGatewayUri &&
            !(isEmergencyNumber || isEmergencyIntent) &&
            PhoneUtils.isRoutableViaGateway(number)) {  
            callStatus = PhoneUtils.placeCallVia(
                this, mPhone, number, contactUri, mProviderGatewayUri);
        } else {
            callStatus = PhoneUtils.placeCall(mPhone, number, contactUri);
        }
        switch (callStatus) {
            case PhoneUtils.CALL_STATUS_DIALED:
                if (VDBG) log("placeCall: PhoneUtils.placeCall() succeeded for regular call '"
                             + number + "'.");
                if (mInCallScreenMode == InCallScreenMode.OTA_NORMAL) {
                    app.cdmaOtaScreenState.otaScreenState =
                            CdmaOtaScreenState.OtaScreenState.OTA_STATUS_LISTENING;
                    updateScreen();
                }
                mDialer.closeDialer(false);  
                mDialer.clearDigits();
                if (mPhone.getPhoneType() == Phone.PHONE_TYPE_CDMA) {
                    if (app.cdmaPhoneCallState.getCurrentCallState()
                            == CdmaPhoneCallState.PhoneCallState.THRWAY_ACTIVE) {
                        PhoneUtils.setMuteInternal(mPhone, false);
                        Message msg = Message.obtain(mHandler, THREEWAY_CALLERINFO_DISPLAY_DONE);
                        mHandler.sendMessageDelayed(msg, THREEWAY_CALLERINFO_DISPLAY_TIME);
                        app.cdmaPhoneCallState.setThreeWayCallOrigState(true);
                        updateScreen();
                    }
                }
                return InCallInitStatus.SUCCESS;
            case PhoneUtils.CALL_STATUS_DIALED_MMI:
                if (DBG) log("placeCall: specified number was an MMI code: '" + number + "'.");
                return InCallInitStatus.DIALED_MMI;
            case PhoneUtils.CALL_STATUS_FAILED:
                Log.w(LOG_TAG, "placeCall: PhoneUtils.placeCall() FAILED for number '"
                      + number + "'.");
                return InCallInitStatus.CALL_FAILED;
            default:
                Log.w(LOG_TAG, "placeCall: unknown callStatus " + callStatus
                      + " from PhoneUtils.placeCall() for number '" + number + "'.");
                return InCallInitStatus.SUCCESS;  
        }
    }
    private InCallInitStatus checkIfOkToInitiateOutgoingCall() {
        int state = mPhone.getServiceState().getState();
        if (VDBG) log("checkIfOkToInitiateOutgoingCall: ServiceState = " + state);
        switch (state) {
            case ServiceState.STATE_IN_SERVICE:
                return InCallInitStatus.SUCCESS;
            case ServiceState.STATE_POWER_OFF:
                return InCallInitStatus.POWER_OFF;
            case ServiceState.STATE_EMERGENCY_ONLY:
                return InCallInitStatus.EMERGENCY_ONLY;
            case ServiceState.STATE_OUT_OF_SERVICE:
                return InCallInitStatus.OUT_OF_SERVICE;
            default:
                throw new IllegalStateException("Unexpected ServiceState: " + state);
        }
    }
    private void handleMissingVoiceMailNumber() {
        if (DBG) log("handleMissingVoiceMailNumber");
        final Message msg = Message.obtain(mHandler);
        msg.what = DONT_ADD_VOICEMAIL_NUMBER;
        final Message msg2 = Message.obtain(mHandler);
        msg2.what = ADD_VOICEMAIL_NUMBER;
        mMissingVoicemailDialog = new AlertDialog.Builder(this)
                .setTitle(R.string.no_vm_number)
                .setMessage(R.string.no_vm_number_msg)
                .setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            if (VDBG) log("Missing voicemail AlertDialog: POSITIVE click...");
                            msg.sendToTarget();  
                            PhoneApp.getInstance().pokeUserActivity();
                        }})
                .setNegativeButton(R.string.add_vm_number_str,
                                   new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            if (VDBG) log("Missing voicemail AlertDialog: NEGATIVE click...");
                            msg2.sendToTarget();  
                            PhoneApp.getInstance().pokeUserActivity();
                        }})
                .setOnCancelListener(new OnCancelListener() {
                        public void onCancel(DialogInterface dialog) {
                            if (VDBG) log("Missing voicemail AlertDialog: CANCEL handler...");
                            msg.sendToTarget();  
                            PhoneApp.getInstance().pokeUserActivity();
                        }})
                .create();
        mMissingVoicemailDialog.getWindow().addFlags(
                WindowManager.LayoutParams.FLAG_DIM_BEHIND);
        mMissingVoicemailDialog.show();
    }
    private void addVoiceMailNumberPanel() {
        if (mMissingVoicemailDialog != null) {
            mMissingVoicemailDialog.dismiss();
            mMissingVoicemailDialog = null;
        }
        if (DBG) log("addVoiceMailNumberPanel: finishing InCallScreen...");
        endInCallScreenSession();
        if (DBG) log("show vm setting");
        Intent intent = new Intent(CallFeaturesSetting.ACTION_ADD_VOICEMAIL);
        intent.setClass(this, CallFeaturesSetting.class);
        startActivity(intent);
    }
    private void dontAddVoiceMailNumber() {
        if (mMissingVoicemailDialog != null) {
            mMissingVoicemailDialog.dismiss();
            mMissingVoicemailDialog = null;
        }
        if (DBG) log("dontAddVoiceMailNumber: finishing InCallScreen...");
        endInCallScreenSession();
    }
    private void delayedCleanupAfterDisconnect() {
        if (VDBG) log("delayedCleanupAfterDisconnect()...  Phone state = " + mPhone.getState());
        mPhone.clearDisconnected();
        if (!phoneIsInUse()) {
            if (DBG) log("- delayedCleanupAfterDisconnect: phone is idle...");
            if (mIsForegroundActivity) {
                if (DBG) log("- delayedCleanupAfterDisconnect: finishing InCallScreen...");
                if (!isPhoneStateRestricted()) {
                    if (VDBG) log("- Show Call Log after disconnect...");
                    final Intent intent = PhoneApp.createCallLogIntent();
                    intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
                    startActivity(intent);
                }
                endInCallScreenSession();
            }
        } else {
            if (DBG) log("- delayedCleanupAfterDisconnect: staying on the InCallScreen...");
            if (DBG) PhoneUtils.dumpCallState(mPhone);
        }
    }
    public void onClick(View view) {
        int id = view.getId();
        if (VDBG) log("onClick(View " + view + ", id " + id + ")...");
        if (VDBG && view instanceof InCallMenuItemView) {
            InCallMenuItemView item = (InCallMenuItemView) view;
            log("  ==> menu item! " + item);
        }
        boolean dismissMenuImmediate = true;
        switch (id) {
            case R.id.menuAnswerAndHold:
                if (VDBG) log("onClick: AnswerAndHold...");
                internalAnswerCall();  
                break;
            case R.id.menuAnswerAndEnd:
                if (VDBG) log("onClick: AnswerAndEnd...");
                internalAnswerAndEnd();
                break;
            case R.id.menuAnswer:
                if (DBG) log("onClick: Answer...");
                internalAnswerCall();
                break;
            case R.id.menuIgnore:
                if (DBG) log("onClick: Ignore...");
                internalHangupRingingCall();
                break;
            case R.id.menuSwapCalls:
                if (DBG) log("onClick: SwapCalls...");
                internalSwapCalls();
                break;
            case R.id.menuMergeCalls:
                if (VDBG) log("onClick: MergeCalls...");
                PhoneUtils.mergeCalls(mPhone);
                break;
            case R.id.menuManageConference:
                if (VDBG) log("onClick: ManageConference...");
                setInCallScreenMode(InCallScreenMode.MANAGE_CONFERENCE);
                break;
            case R.id.menuShowDialpad:
                if (VDBG) log("onClick: Show/hide dialpad...");
                onShowHideDialpad();
                break;
            case R.id.manage_done:  
                if (VDBG) log("onClick: mButtonManageConferenceDone...");
                setInCallScreenMode(InCallScreenMode.NORMAL);
                break;
            case R.id.menuSpeaker:
                if (VDBG) log("onClick: Speaker...");
                onSpeakerClick();
                dismissMenuImmediate = false;
                break;
            case R.id.menuBluetooth:
                if (VDBG) log("onClick: Bluetooth...");
                onBluetoothClick();
                dismissMenuImmediate = false;
                break;
            case R.id.menuMute:
                if (VDBG) log("onClick: Mute...");
                onMuteClick();
                dismissMenuImmediate = false;
                break;
            case R.id.menuHold:
                if (VDBG) log("onClick: Hold...");
                onHoldClick();
                dismissMenuImmediate = false;
                break;
            case R.id.menuAddCall:
                if (VDBG) log("onClick: AddCall...");
                PhoneUtils.startNewCall(mPhone);  
                break;
            case R.id.menuEndCall:
                if (VDBG) log("onClick: EndCall...");
                internalHangup();
                break;
            default:
                if  ((mInCallScreenMode == InCallScreenMode.OTA_NORMAL
                        || mInCallScreenMode == InCallScreenMode.OTA_ENDED)
                        && otaUtils != null) {
                    otaUtils.onClickHandler(id);
                } else {
                    Log.w(LOG_TAG,
                            "Got click from unexpected View ID " + id + " (View = " + view + ")");
                }
                break;
        }
        EventLog.writeEvent(EventLogTags.PHONE_UI_BUTTON_CLICK,
                (view instanceof TextView) ? ((TextView) view).getText() : "");
        if (!dismissMenuImmediate) {
            if (VDBG) log("- onClick: updating menu to show 'new' current state...");
            boolean okToShowMenu = mInCallMenu.updateItems(mPhone);
            if (!okToShowMenu) {
                if (VDBG) log("onClick: Tried to update menu, but now need to take it down!");
                dismissMenuImmediate = true;
            }
        }
        PhoneApp.getInstance().pokeUserActivity();
        dismissMenu(dismissMenuImmediate);
    }
    private void onHoldClick() {
        if (VDBG) log("onHoldClick()...");
        final boolean hasActiveCall = !mForegroundCall.isIdle();
        final boolean hasHoldingCall = !mBackgroundCall.isIdle();
        if (VDBG) log("- hasActiveCall = " + hasActiveCall
                      + ", hasHoldingCall = " + hasHoldingCall);
        boolean newHoldState;
        boolean holdButtonEnabled;
        if (hasActiveCall && !hasHoldingCall) {
            PhoneUtils.switchHoldingAndActive(mPhone);  
            newHoldState = true;
            holdButtonEnabled = true;
        } else if (!hasActiveCall && hasHoldingCall) {
            PhoneUtils.switchHoldingAndActive(mPhone);  
            newHoldState = false;
            holdButtonEnabled = true;
        } else {
            newHoldState = false;
            holdButtonEnabled = false;
        }
        mDialer.closeDialer(true);  
    }
    private void onSpeakerClick() {
        if (VDBG) log("onSpeakerClick()...");
        boolean newSpeakerState = !PhoneUtils.isSpeakerOn(this);
        if (newSpeakerState && isBluetoothAvailable() && isBluetoothAudioConnected()) {
            disconnectBluetoothAudio();
        }
        PhoneUtils.turnOnSpeaker(this, newSpeakerState, true);
        if (newSpeakerState) {
            enableTouchLock(false);
        } else {
            if (mDialer.isOpened() && !isTouchLocked()) {
                resetTouchLockTimer();
            }
        }
    }
    private void onMuteClick() {
        if (VDBG) log("onMuteClick()...");
        boolean newMuteState = !PhoneUtils.getMute(mPhone);
        PhoneUtils.setMute(mPhone, newMuteState);
    }
    private void onBluetoothClick() {
        if (VDBG) log("onBluetoothClick()...");
        if (isBluetoothAvailable()) {
            if (isBluetoothAudioConnected()) {
                disconnectBluetoothAudio();
            } else {
                if (PhoneUtils.isSpeakerOn(this)) {
                    PhoneUtils.turnOnSpeaker(this, false, true);
                }
                connectBluetoothAudio();
            }
        } else {
            Log.w(LOG_TAG, "Got onBluetoothClick, but bluetooth is unavailable");
        }
    }
    private void onShowHideDialpad() {
        if (VDBG) log("onShowHideDialpad()...");
        if (mDialer.isOpened()) {
            mDialer.closeDialer(true);  
        } else {
            mDialer.openDialer(true);  
        }
        mDialer.setHandleVisible(true);
    }
     void handleOnscreenButtonClick(int id) {
        if (DBG) log("handleOnscreenButtonClick(id " + id + ")...");
        switch (id) {
            case R.id.answerButton:
                internalAnswerCall();
                break;
            case R.id.rejectButton:
                internalHangupRingingCall();
                break;
            case R.id.holdButton:
                onHoldClick();
                break;
            case R.id.swapButton:
                internalSwapCalls();
                break;
            case R.id.endButton:
                internalHangup();
                break;
            case R.id.dialpadButton:
                onShowHideDialpad();
                break;
            case R.id.bluetoothButton:
                onBluetoothClick();
                break;
            case R.id.muteButton:
                onMuteClick();
                break;
            case R.id.speakerButton:
                onSpeakerClick();
                break;
            case R.id.addButton:
                PhoneUtils.startNewCall(mPhone);  
                break;
            case R.id.mergeButton:
            case R.id.cdmaMergeButton:
                PhoneUtils.mergeCalls(mPhone);
                break;
            case R.id.manageConferencePhotoButton:
                setInCallScreenMode(InCallScreenMode.MANAGE_CONFERENCE);
                break;
            default:
                Log.w(LOG_TAG, "handleOnscreenButtonClick: unexpected ID " + id);
                break;
        }
        updateInCallTouchUi();
    }
    private void updateProviderOverlay() {
        if (VDBG) log("updateProviderOverlay: " + mProviderOverlayVisible);
        ViewGroup overlay = (ViewGroup) findViewById(R.id.inCallProviderOverlay);
        if (mProviderOverlayVisible) {
            CharSequence template = getText(R.string.calling_via_template);
            CharSequence text = TextUtils.expandTemplate(template, mProviderLabel,
                                                         mProviderAddress);
            TextView message = (TextView) findViewById(R.id.callingVia);
            message.setCompoundDrawablesWithIntrinsicBounds(mProviderIcon, null, null, null);
            message.setText(text);
            overlay.setVisibility(View.VISIBLE);
            mHandler.removeMessages(EVENT_HIDE_PROVIDER_OVERLAY);
            Message msg = Message.obtain(mHandler, EVENT_HIDE_PROVIDER_OVERLAY);
            mHandler.sendMessageDelayed(msg, PROVIDER_OVERLAY_TIMEOUT);
        } else {
            overlay.setVisibility(View.GONE);
        }
    }
    private void updateMenuButtonHint() {
        if (VDBG) log("updateMenuButtonHint()...");
        boolean hintVisible = true;
        final boolean hasRingingCall = !mRingingCall.isIdle();
        final boolean hasActiveCall = !mForegroundCall.isIdle();
        final boolean hasHoldingCall = !mBackgroundCall.isIdle();
        if (mInCallScreenMode == InCallScreenMode.CALL_ENDED) {
            hintVisible = false;
        } else if (hasRingingCall && !(hasActiveCall && !hasHoldingCall)) {
            hintVisible = false;
        } else if (!phoneIsInUse()) {
            hintVisible = false;
        }
        if (isTouchUiEnabled()) {
            hintVisible = false;
        }
        if (hasRingingCall && isIncomingCallTouchUiEnabled()) {
            hintVisible = false;
        }
        int hintVisibility = (hintVisible) ? View.VISIBLE : View.GONE;
        mCallCard.getMenuButtonHint().setVisibility(hintVisibility);
    }
    private void handleStartupError(InCallInitStatus status) {
        if (DBG) log("handleStartupError(): status = " + status);
        switch (status) {
            case VOICEMAIL_NUMBER_MISSING:
                handleMissingVoiceMailNumber();
                break;
            case POWER_OFF:
                showGenericErrorDialog(R.string.incall_error_power_off, true);
                break;
            case EMERGENCY_ONLY:
                showGenericErrorDialog(R.string.incall_error_emergency_only, true);
                break;
            case OUT_OF_SERVICE:
                showGenericErrorDialog(R.string.incall_error_out_of_service, true);
                break;
            case PHONE_NOT_IN_USE:
                Log.w(LOG_TAG,
                      "handleStartupError: unexpected PHONE_NOT_IN_USE status");
                break;
            case NO_PHONE_NUMBER_SUPPLIED:
                showGenericErrorDialog(R.string.incall_error_no_phone_number_supplied, true);
                break;
            case DIALED_MMI:
                if (mPhone.getState() == Phone.State.OFFHOOK) {
                    Toast.makeText(this, R.string.incall_status_dialed_mmi, Toast.LENGTH_SHORT)
                        .show();
                }
                break;
            case CALL_FAILED:
                showGenericErrorDialog(R.string.incall_error_call_failed, true);
                break;
            default:
                Log.w(LOG_TAG, "handleStartupError: unexpected status code " + status);
                showGenericErrorDialog(R.string.incall_error_call_failed, true);
                break;
        }
    }
    private void showGenericErrorDialog(int resid, boolean isStartupError) {
        CharSequence msg = getResources().getText(resid);
        if (DBG) log("showGenericErrorDialog('" + msg + "')...");
        DialogInterface.OnClickListener clickListener;
        OnCancelListener cancelListener;
        if (isStartupError) {
            clickListener = new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int which) {
                    bailOutAfterErrorDialog();
                }};
            cancelListener = new OnCancelListener() {
                public void onCancel(DialogInterface dialog) {
                    bailOutAfterErrorDialog();
                }};
        } else {
            clickListener = new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int which) {
                    delayedCleanupAfterDisconnect();
                }};
            cancelListener = new OnCancelListener() {
                public void onCancel(DialogInterface dialog) {
                    delayedCleanupAfterDisconnect();
                }};
        }
        mGenericErrorDialog = new AlertDialog.Builder(this)
                .setMessage(msg)
                .setPositiveButton(R.string.ok, clickListener)
                .setOnCancelListener(cancelListener)
                .create();
        mGenericErrorDialog.getWindow().addFlags(
                WindowManager.LayoutParams.FLAG_DIM_BEHIND);
        mGenericErrorDialog.show();
    }
    private void showCallLostDialog() {
        if (DBG) log("showCallLostDialog()...");
        if (!mIsForegroundActivity) {
            if (DBG) log("showCallLostDialog: not the foreground Activity! Bailing out...");
            return;
        }
        if (mCallLostDialog != null) {
            if (DBG) log("showCallLostDialog: There is a mCallLostDialog already.");
            return;
        }
        mCallLostDialog = new AlertDialog.Builder(this)
                .setMessage(R.string.call_lost)
                .setIcon(android.R.drawable.ic_dialog_alert)
                .create();
        mCallLostDialog.show();
    }
    private void bailOutAfterErrorDialog() {
        if (mGenericErrorDialog != null) {
            if (DBG) log("bailOutAfterErrorDialog: DISMISSING mGenericErrorDialog.");
            mGenericErrorDialog.dismiss();
            mGenericErrorDialog = null;
        }
        if (DBG) log("bailOutAfterErrorDialog(): end InCallScreen session...");
        endInCallScreenSession();
    }
    private void dismissAllDialogs() {
        if (DBG) log("dismissAllDialogs()...");
        if (mMissingVoicemailDialog != null) {
            if (VDBG) log("- DISMISSING mMissingVoicemailDialog.");
            mMissingVoicemailDialog.dismiss();
            mMissingVoicemailDialog = null;
        }
        if (mMmiStartedDialog != null) {
            if (VDBG) log("- DISMISSING mMmiStartedDialog.");
            mMmiStartedDialog.dismiss();
            mMmiStartedDialog = null;
        }
        if (mGenericErrorDialog != null) {
            if (VDBG) log("- DISMISSING mGenericErrorDialog.");
            mGenericErrorDialog.dismiss();
            mGenericErrorDialog = null;
        }
        if (mSuppServiceFailureDialog != null) {
            if (VDBG) log("- DISMISSING mSuppServiceFailureDialog.");
            mSuppServiceFailureDialog.dismiss();
            mSuppServiceFailureDialog = null;
        }
        if (mWaitPromptDialog != null) {
            if (VDBG) log("- DISMISSING mWaitPromptDialog.");
            mWaitPromptDialog.dismiss();
            mWaitPromptDialog = null;
        }
        if (mWildPromptDialog != null) {
            if (VDBG) log("- DISMISSING mWildPromptDialog.");
            mWildPromptDialog.dismiss();
            mWildPromptDialog = null;
        }
        if (mCallLostDialog != null) {
            if (VDBG) log("- DISMISSING mCallLostDialog.");
            mCallLostDialog.dismiss();
            mCallLostDialog = null;
        }
        if ((mInCallScreenMode == InCallScreenMode.OTA_NORMAL
                || mInCallScreenMode == InCallScreenMode.OTA_ENDED)
                && otaUtils != null) {
            otaUtils.dismissAllOtaDialogs();
        }
        if (mPausePromptDialog != null) {
            if (DBG) log("- DISMISSING mPausePromptDialog.");
            mPausePromptDialog.dismiss();
            mPausePromptDialog = null;
        }
    }
     void internalAnswerCall() {
        final boolean hasRingingCall = !mRingingCall.isIdle();
        if (hasRingingCall) {
            int phoneType = mPhone.getPhoneType();
            if (phoneType == Phone.PHONE_TYPE_CDMA) {
                if (DBG) log("internalAnswerCall: answering (CDMA)...");
                PhoneUtils.answerCall(mPhone);  
            } else if (phoneType == Phone.PHONE_TYPE_GSM) {
                final boolean hasActiveCall = !mForegroundCall.isIdle();
                final boolean hasHoldingCall = !mBackgroundCall.isIdle();
                if (hasActiveCall && hasHoldingCall) {
                    if (DBG) log("internalAnswerCall: answering (both lines in use!)...");
                    PhoneUtils.answerAndEndActive(mPhone);
                } else {
                    if (DBG) log("internalAnswerCall: answering...");
                    PhoneUtils.answerCall(mPhone);  
                }
            } else {
                throw new IllegalStateException("Unexpected phone type: " + phoneType);
            }
        }
    }
     void internalAnswerAndEnd() {
        if (DBG) log("internalAnswerAndEnd()...");
        PhoneUtils.answerAndEndActive(mPhone);
    }
     void internalHangupRingingCall() {
        if (DBG) log("internalHangupRingingCall()...");
        PhoneUtils.hangupRingingCall(mPhone);
    }
     void internalHangup() {
        if (DBG) log("internalHangup()...");
        PhoneUtils.hangup(mPhone);
    }
    private void internalSwapCalls() {
        if (DBG) log("internalSwapCalls()...");
        mDialer.closeDialer(true);  
        mDialer.clearDigits();
        PhoneUtils.switchHoldingAndActive(mPhone);
        if (mPhone.getPhoneType() == Phone.PHONE_TYPE_CDMA) {
            BluetoothHandsfree bthf = PhoneApp.getInstance().getBluetoothHandsfree();
            if (bthf != null) {
                bthf.cdmaSwapSecondCallState();
            }
        }
    }
    private void setInCallScreenMode(InCallScreenMode newMode) {
        if (DBG) log("setInCallScreenMode: " + newMode);
        mInCallScreenMode = newMode;
        switch (mInCallScreenMode) {
            case MANAGE_CONFERENCE:
                if (!PhoneUtils.isConferenceCall(mForegroundCall)) {
                    Log.w(LOG_TAG, "MANAGE_CONFERENCE: no active conference call!");
                    setInCallScreenMode(InCallScreenMode.NORMAL);
                    return;
                }
                List<Connection> connections = mForegroundCall.getConnections();
                if ((connections == null) || (connections.size() <= 1)) {
                    Log.w(LOG_TAG,
                          "MANAGE_CONFERENCE: Bogus TRUE from isConferenceCall(); connections = "
                          + connections);
                    setInCallScreenMode(InCallScreenMode.NORMAL);
                    return;
                }
                mManageConferenceUtils.initManageConferencePanel();  
                mManageConferenceUtils.updateManageConferencePanel(connections);
                mManageConferenceUtils.setPanelVisible(true);
                long callDuration = mForegroundCall.getEarliestConnection().getDurationMillis();
                mManageConferenceUtils.startConferenceTime(
                        SystemClock.elapsedRealtime() - callDuration);
                mInCallPanel.setVisibility(View.GONE);
                break;
            case CALL_ENDED:
                mManageConferenceUtils.setPanelVisible(false);
                mManageConferenceUtils.stopConferenceTime();
                updateMenuButtonHint();  
                mInCallPanel.setVisibility(View.VISIBLE);
                break;
            case NORMAL:
                mInCallPanel.setVisibility(View.VISIBLE);
                mManageConferenceUtils.setPanelVisible(false);
                mManageConferenceUtils.stopConferenceTime();
                break;
            case OTA_NORMAL:
                otaUtils.setCdmaOtaInCallScreenUiState(
                        OtaUtils.CdmaOtaInCallScreenUiState.State.NORMAL);
                mInCallPanel.setVisibility(View.GONE);
                break;
            case OTA_ENDED:
                otaUtils.setCdmaOtaInCallScreenUiState(
                        OtaUtils.CdmaOtaInCallScreenUiState.State.ENDED);
                mInCallPanel.setVisibility(View.GONE);
                break;
            case UNDEFINED:
                setIntent(new Intent(ACTION_UNDEFINED));
                if (mPhone.getState() != Phone.State.OFFHOOK) {
                    if (otaUtils != null) {
                        otaUtils.cleanOtaScreen(true);
                    }
                } else {
                    log("WARNING: Setting mode to UNDEFINED but phone is OFFHOOK,"
                            + " skip cleanOtaScreen.");
                }
                mInCallPanel.setVisibility(View.VISIBLE);
                break;
        }
        updateDialpadVisibility();
        updateInCallTouchUi();
    }
     boolean isManageConferenceMode() {
        return (mInCallScreenMode == InCallScreenMode.MANAGE_CONFERENCE);
    }
    private void updateManageConferencePanelIfNecessary() {
        if (VDBG) log("updateManageConferencePanelIfNecessary: " + mForegroundCall + "...");
        List<Connection> connections = mForegroundCall.getConnections();
        if (connections == null) {
            if (VDBG) log("==> no connections on foreground call!");
            setInCallScreenMode(InCallScreenMode.NORMAL);
            InCallInitStatus status = syncWithPhoneState();
            if (status != InCallInitStatus.SUCCESS) {
                Log.w(LOG_TAG, "- syncWithPhoneState failed! status = " + status);
                if (DBG) log("updateManageConferencePanelIfNecessary: endInCallScreenSession... 1");
                endInCallScreenSession();
                return;
            }
            return;
        }
        int numConnections = connections.size();
        if (numConnections <= 1) {
            if (VDBG) log("==> foreground call no longer a conference!");
            setInCallScreenMode(InCallScreenMode.NORMAL);
            InCallInitStatus status = syncWithPhoneState();
            if (status != InCallInitStatus.SUCCESS) {
                Log.w(LOG_TAG, "- syncWithPhoneState failed! status = " + status);
                if (DBG) log("updateManageConferencePanelIfNecessary: endInCallScreenSession... 2");
                endInCallScreenSession();
                return;
            }
            return;
        }
        if (numConnections != mManageConferenceUtils.getNumCallersInConference()) {
            if (VDBG) log("==> Conference size has changed; need to rebuild UI!");
            mManageConferenceUtils.updateManageConferencePanel(connections);
        }
    }
    private void updateDialpadVisibility() {
        if (mPhone.getState() == Phone.State.RINGING) {
            mDialer.closeDialer(false);  
            mDialer.clearDigits();
        }
        mDialer.setHandleVisible(okToShowDialpad());
        if (!mDialer.usingSlidingDrawer()) {
            if (mDialerView != null) {
                mDialerView.setKeysBackgroundResource(
                        isBluetoothAudioConnected() ? R.drawable.btn_dial_blue
                        : R.drawable.btn_dial_green);
            }
            if (isDialerOpened()) {
                mInCallPanel.setVisibility(View.GONE);
            } else {
                if ((mInCallScreenMode == InCallScreenMode.NORMAL)
                    || (mInCallScreenMode == InCallScreenMode.CALL_ENDED)) {
                    mInCallPanel.setVisibility(View.VISIBLE);
                }
            }
        }
    }
     boolean isDialerOpened() {
        return (mDialer != null && mDialer.isOpened());
    }
     void onDialerOpen() {
        if (DBG) log("onDialerOpen()...");
        resetTouchLockTimer();
        updateInCallTouchUi();
        updateDialpadVisibility();
        PhoneApp.getInstance().pokeUserActivity();
        if  ((mInCallScreenMode == InCallScreenMode.OTA_NORMAL
                || mInCallScreenMode == InCallScreenMode.OTA_ENDED)
                && otaUtils != null) {
            otaUtils.hideOtaScreen();
        }
    }
     void onDialerClose() {
        if (DBG) log("onDialerClose()...");
        final PhoneApp app = PhoneApp.getInstance();
        if ((mInCallScreenMode == InCallScreenMode.OTA_NORMAL)
            || (mInCallScreenMode == InCallScreenMode.OTA_ENDED)
            || ((app.cdmaOtaScreenState != null)
                && (app.cdmaOtaScreenState.otaScreenState ==
                    CdmaOtaScreenState.OtaScreenState.OTA_STATUS_ACTIVATION))) {
            mDialer.setHandleVisible(false);
            if (otaUtils != null) {
                otaUtils.otaShowProperScreen();
            }
        }
        enableTouchLock(false);
        updateInCallTouchUi();
        updateDialpadVisibility();
        app.getInstance().pokeUserActivity();
    }
    private boolean okToDialDTMFTones() {
        final boolean hasRingingCall = !mRingingCall.isIdle();
        final Call.State fgCallState = mForegroundCall.getState();
        boolean canDial =
            (fgCallState == Call.State.ACTIVE || fgCallState == Call.State.ALERTING)
            && !hasRingingCall
            && (mInCallScreenMode != InCallScreenMode.MANAGE_CONFERENCE);
        if (VDBG) log ("[okToDialDTMFTones] foreground state: " + fgCallState +
                ", ringing state: " + hasRingingCall +
                ", call screen mode: " + mInCallScreenMode +
                ", result: " + canDial);
        return canDial;
    }
     boolean okToShowDialpad() {
        return okToDialDTMFTones();
    }
    private void initInCallTouchUi() {
        if (DBG) log("initInCallTouchUi()...");
        mInCallTouchUi = (InCallTouchUi) findViewById(R.id.inCallTouchUi);
        mInCallTouchUi.setInCallScreenInstance(this);
    }
    private void updateInCallTouchUi() {
        if (mInCallTouchUi != null) {
            mInCallTouchUi.updateState(mPhone);
        }
    }
    public boolean isTouchUiEnabled() {
        return (mInCallTouchUi != null) && mInCallTouchUi.isTouchUiEnabled();
    }
    public boolean isIncomingCallTouchUiEnabled() {
        return (mInCallTouchUi != null) && mInCallTouchUi.isIncomingCallTouchUiEnabled();
    }
     void requestUpdateTouchUi() {
        if (DBG) log("requestUpdateTouchUi()...");
        mHandler.removeMessages(REQUEST_UPDATE_TOUCH_UI);
        mHandler.sendEmptyMessage(REQUEST_UPDATE_TOUCH_UI);
    }
     boolean okToShowInCallTouchUi() {
        return (mInCallScreenMode == InCallScreenMode.NORMAL);
    }
    public boolean isPhoneStateRestricted() {
        return ((mPhone.getServiceState().getState() == ServiceState.STATE_EMERGENCY_ONLY) ||
                (mPhone.getServiceState().getState() == ServiceState.STATE_OUT_OF_SERVICE) ||
                (PhoneApp.getInstance().getKeyguardManager().inKeyguardRestrictedInputMode()));
    }
    @Override
    public View onCreatePanelView(int featureId) {
        if (VDBG) log("onCreatePanelView(featureId = " + featureId + ")...");
        if (featureId != Window.FEATURE_OPTIONS_PANEL) {
            return null;
        }
        if (isTouchUiEnabled()) {
            return null;
        }
        mHandler.removeMessages(DISMISS_MENU);
        if (mInCallMenu == null) {
            if (VDBG) log("onCreatePanelView: creating mInCallMenu (first time)...");
            mInCallMenu = new InCallMenu(this);
            mInCallMenu.initMenu();
        }
        boolean okToShowMenu = mInCallMenu.updateItems(mPhone);
        return okToShowMenu ? mInCallMenu.getView() : null;
    }
     void dismissMenu(boolean dismissImmediate) {
        if (VDBG) log("dismissMenu(immediate = " + dismissImmediate + ")...");
        if (dismissImmediate) {
            closeOptionsMenu();
        } else {
            mHandler.removeMessages(DISMISS_MENU);
            mHandler.sendEmptyMessageDelayed(DISMISS_MENU, MENU_DISMISS_DELAY);
        }
    }
    @Override
    public void onPanelClosed(int featureId, Menu menu) {
        if (VDBG) log("onPanelClosed(featureId = " + featureId + ")...");
        if (featureId == Window.FEATURE_OPTIONS_PANEL) {
        }
        super.onPanelClosed(featureId, menu);
    }
     boolean isBluetoothAvailable() {
        if (VDBG) log("isBluetoothAvailable()...");
        if (mBluetoothHandsfree == null) {
            if (VDBG) log("  ==> FALSE (not BT capable)");
            return false;
        }
        boolean isConnected = false;
        if (mBluetoothHeadset != null) {
            if (VDBG) log("  - headset state = " + mBluetoothHeadset.getState());
            BluetoothDevice headset = mBluetoothHeadset.getCurrentHeadset();
            if (VDBG) log("  - headset address: " + headset);
            if (headset != null) {
                isConnected = mBluetoothHeadset.isConnected(headset);
                if (VDBG) log("  - isConnected: " + isConnected);
            }
        }
        if (VDBG) log("  ==> " + isConnected);
        return isConnected;
    }
     boolean isBluetoothAudioConnected() {
        if (mBluetoothHandsfree == null) {
            if (VDBG) log("isBluetoothAudioConnected: ==> FALSE (null mBluetoothHandsfree)");
            return false;
        }
        boolean isAudioOn = mBluetoothHandsfree.isAudioOn();
        if (VDBG) log("isBluetoothAudioConnected: ==> isAudioOn = " + isAudioOn);
        return isAudioOn;
    }
     boolean isBluetoothAudioConnectedOrPending() {
        if (isBluetoothAudioConnected()) {
            if (VDBG) log("isBluetoothAudioConnectedOrPending: ==> TRUE (really connected)");
            return true;
        }
        if (mBluetoothConnectionPending) {
            long timeSinceRequest =
                    SystemClock.elapsedRealtime() - mBluetoothConnectionRequestTime;
            if (timeSinceRequest < 5000 ) {
                if (VDBG) log("isBluetoothAudioConnectedOrPending: ==> TRUE (requested "
                             + timeSinceRequest + " msec ago)");
                return true;
            } else {
                if (VDBG) log("isBluetoothAudioConnectedOrPending: ==> FALSE (request too old: "
                             + timeSinceRequest + " msec ago)");
                mBluetoothConnectionPending = false;
                return false;
            }
        }
        if (VDBG) log("isBluetoothAudioConnectedOrPending: ==> FALSE");
        return false;
    }
     void requestUpdateBluetoothIndication() {
        if (VDBG) log("requestUpdateBluetoothIndication()...");
        mHandler.removeMessages(REQUEST_UPDATE_BLUETOOTH_INDICATION);
        mHandler.sendEmptyMessage(REQUEST_UPDATE_BLUETOOTH_INDICATION);
    }
    private void dumpBluetoothState() {
        log("============== dumpBluetoothState() =============");
        log("= isBluetoothAvailable: " + isBluetoothAvailable());
        log("= isBluetoothAudioConnected: " + isBluetoothAudioConnected());
        log("= isBluetoothAudioConnectedOrPending: " + isBluetoothAudioConnectedOrPending());
        log("= PhoneApp.showBluetoothIndication: "
            + PhoneApp.getInstance().showBluetoothIndication());
        log("=");
        if (mBluetoothHandsfree != null) {
            log("= BluetoothHandsfree.isAudioOn: " + mBluetoothHandsfree.isAudioOn());
            if (mBluetoothHeadset != null) {
                BluetoothDevice headset = mBluetoothHeadset.getCurrentHeadset();
                log("= BluetoothHeadset.getCurrentHeadset: " + headset);
                if (headset != null) {
                    log("= BluetoothHeadset.isConnected: "
                        + mBluetoothHeadset.isConnected(headset));
                }
            } else {
                log("= mBluetoothHeadset is null");
            }
        } else {
            log("= mBluetoothHandsfree is null; device is not BT capable");
        }
    }
     void connectBluetoothAudio() {
        if (VDBG) log("connectBluetoothAudio()...");
        if (mBluetoothHandsfree != null) {
            mBluetoothHandsfree.userWantsAudioOn();
        }
        mBluetoothConnectionPending = true;
        mBluetoothConnectionRequestTime = SystemClock.elapsedRealtime();
    }
     void disconnectBluetoothAudio() {
        if (VDBG) log("disconnectBluetoothAudio()...");
        if (mBluetoothHandsfree != null) {
            mBluetoothHandsfree.userWantsAudioOff();
        }
        mBluetoothConnectionPending = false;
    }
    private void initTouchLock() {
        if (VDBG) log("initTouchLock()...");
        if (mTouchLockOverlay != null) {
            Log.w(LOG_TAG, "initTouchLock: already initialized!");
            return;
        }
        if (!mUseTouchLockOverlay) {
            Log.w(LOG_TAG, "initTouchLock: touch lock isn't used on this device!");
            return;
        }
        mTouchLockOverlay = (View) findViewById(R.id.touchLockOverlay);
        mTouchLockIcon = (View) findViewById(R.id.touchLockIcon);
        mTouchLockOverlay.setOnTouchListener(this);
        mTouchLockIcon.setOnTouchListener(this);
        mTouchLockFadeIn = AnimationUtils.loadAnimation(this, R.anim.touch_lock_fade_in);
    }
    private boolean isTouchLocked() {
        return mUseTouchLockOverlay
                && (mTouchLockOverlay != null)
                && (mTouchLockOverlay.getVisibility() == View.VISIBLE);
    }
    private void enableTouchLock(boolean enable) {
        if (VDBG) log("enableTouchLock(" + enable + ")...");
        if (enable) {
            if (!mUseTouchLockOverlay) {
                Log.w(LOG_TAG, "enableTouchLock: touch lock isn't used on this device!");
                return;
            }
            if (!mDialer.isOpened()) {
                if (VDBG) log("enableTouchLock: dialpad isn't up, no need to lock screen.");
                return;
            }
            if (PhoneUtils.isSpeakerOn(this)) {
                if (VDBG) log("enableTouchLock: speaker is on, no need to lock screen.");
                return;
            }
            if (mTouchLockOverlay == null) {
                initTouchLock();
            }
            dismissMenu(true);  
            mTouchLockOverlay.setVisibility(View.VISIBLE);
            mTouchLockOverlay.startAnimation(mTouchLockFadeIn);
        } else {
            if (mTouchLockOverlay != null) mTouchLockOverlay.setVisibility(View.GONE);
        }
    }
    private void resetTouchLockTimer() {
        if (VDBG) log("resetTouchLockTimer()...");
        if (!mUseTouchLockOverlay) return;
        mHandler.removeMessages(TOUCH_LOCK_TIMER);
        if (mDialer.isOpened() && !isTouchLocked()) {
            int touchLockDelay = Settings.Secure.getInt(
                    getContentResolver(),
                    Settings.Secure.SHORT_KEYLIGHT_DELAY_MS,
                    TOUCH_LOCK_DELAY_DEFAULT);
            mHandler.sendEmptyMessageDelayed(TOUCH_LOCK_TIMER, touchLockDelay);
        }
    }
    private void touchLockTimerExpired() {
        enableTouchLock(true);
    }
    public boolean onTouch(View v, MotionEvent event) {
        if (VDBG) log ("onTouch(View " + v + ")...");
        if ((v == mTouchLockIcon) || (v == mTouchLockOverlay)) {
            if (!isTouchLocked()) {
                return false;
            }
            if (v == mTouchLockIcon) {
                if (event.getAction() == MotionEvent.ACTION_DOWN) {
                    long now = SystemClock.uptimeMillis();
                    if (VDBG) log("- touch lock icon: handling a DOWN event, t = " + now);
                    if (now < mTouchLockLastTouchTime + ViewConfiguration.getDoubleTapTimeout()) {
                        if (VDBG) log("==> touch lock icon: DOUBLE-TAP!");
                        enableTouchLock(false);
                        resetTouchLockTimer();
                        PhoneApp.getInstance().pokeUserActivity();
                    }
                } else if (event.getAction() == MotionEvent.ACTION_UP) {
                    mTouchLockLastTouchTime = SystemClock.uptimeMillis();
                }
                return true;
            } else {  
                return true;
            }
        } else {
            Log.w(LOG_TAG, "onTouch: event from unexpected View: " + v);
            return false;
        }
    }
    @Override
    public void onUserInteraction() {
        if (mDialer.isOpened() && !isTouchLocked()) {
            resetTouchLockTimer();
        }
    }
     void requestCloseOtaFailureNotice(long timeout) {
        if (DBG) log("requestCloseOtaFailureNotice() with timeout: " + timeout);
        mHandler.sendEmptyMessageDelayed(REQUEST_CLOSE_OTA_FAILURE_NOTICE, timeout);
    }
     void requestCloseSpcErrorNotice(long timeout) {
        if (DBG) log("requestCloseSpcErrorNotice() with timeout: " + timeout);
        mHandler.sendEmptyMessageDelayed(REQUEST_CLOSE_SPC_ERROR_NOTICE, timeout);
    }
    public boolean isOtaCallInActiveState() {
        final PhoneApp app = PhoneApp.getInstance();
        if ((mInCallScreenMode == InCallScreenMode.OTA_NORMAL)
                || ((app.cdmaOtaScreenState != null)
                    && (app.cdmaOtaScreenState.otaScreenState ==
                        CdmaOtaScreenState.OtaScreenState.OTA_STATUS_ACTIVATION))) {
            return true;
        } else {
            return false;
        }
    }
    public void handleOtaCallEnd() {
        final PhoneApp app = PhoneApp.getInstance();
        if (DBG) log("handleOtaCallEnd entering");
        if (((mInCallScreenMode == InCallScreenMode.OTA_NORMAL)
                || ((app.cdmaOtaScreenState != null)
                && (app.cdmaOtaScreenState.otaScreenState !=
                    CdmaOtaScreenState.OtaScreenState.OTA_STATUS_UNDEFINED)))
                && ((app.cdmaOtaProvisionData != null)
                && (!app.cdmaOtaProvisionData.inOtaSpcState))) {
            if (DBG) log("handleOtaCallEnd - Set OTA Call End stater");
            setInCallScreenMode(InCallScreenMode.OTA_ENDED);
            updateScreen();
        }
    }
    public boolean isOtaCallInEndState() {
        return (mInCallScreenMode == InCallScreenMode.OTA_ENDED);
    }
    private boolean checkIsOtaCall(Intent intent) {
        if (VDBG) log("checkIsOtaCall...");
        if (intent == null || intent.getAction() == null) {
            return false;
        }
        if (mPhone.getPhoneType() != Phone.PHONE_TYPE_CDMA) {
            return false;
        }
        final PhoneApp app = PhoneApp.getInstance();
        if ((app.cdmaOtaScreenState == null)
                || (app.cdmaOtaProvisionData == null)) {
            if (DBG) log("checkIsOtaCall: OtaUtils.CdmaOtaScreenState not initialized");
            return false;
        }
        String action = intent.getAction();
        boolean isOtaCall = false;
        if (action.equals(ACTION_SHOW_ACTIVATION)) {
            if (DBG) log("checkIsOtaCall action = ACTION_SHOW_ACTIVATION");
            if (!app.cdmaOtaProvisionData.isOtaCallIntentProcessed) {
                if (DBG) log("checkIsOtaCall: ACTION_SHOW_ACTIVATION is not handled before");
                app.cdmaOtaProvisionData.isOtaCallIntentProcessed = true;
                app.cdmaOtaScreenState.otaScreenState =
                        CdmaOtaScreenState.OtaScreenState.OTA_STATUS_ACTIVATION;
            }
            isOtaCall = true;
        } else if (action.equals(Intent.ACTION_CALL)
                || action.equals(Intent.ACTION_CALL_EMERGENCY)) {
            String number;
            try {
                number = getInitialNumber(intent);
            } catch (PhoneUtils.VoiceMailNumberMissingException ex) {
                if (DBG) log("Error retrieving number using the api getInitialNumber()");
                return false;
            }
            if (mPhone.isOtaSpNumber(number)) {
                if (DBG) log("checkIsOtaCall action ACTION_CALL, it is valid OTA number");
                isOtaCall = true;
            }
        } else if (action.equals(intent.ACTION_MAIN)) {
            if (DBG) log("checkIsOtaCall action ACTION_MAIN");
            boolean isRingingCall = !mRingingCall.isIdle();
            if (isRingingCall) {
                if (DBG) log("checkIsOtaCall isRingingCall: " + isRingingCall);
                return false;
            } else if ((app.cdmaOtaInCallScreenUiState.state
                            == CdmaOtaInCallScreenUiState.State.NORMAL)
                    || (app.cdmaOtaInCallScreenUiState.state
                            == CdmaOtaInCallScreenUiState.State.ENDED)) {
                if (DBG) log("checkIsOtaCall action ACTION_MAIN, OTA call already in progress");
                isOtaCall = true;
            } else {
                if (app.cdmaOtaScreenState.otaScreenState !=
                        CdmaOtaScreenState.OtaScreenState.OTA_STATUS_UNDEFINED) {
                    if (DBG) log("checkIsOtaCall action ACTION_MAIN, "
                                 + "OTA call in progress with UNDEFINED");
                    isOtaCall = true;
                }
            }
        }
        if (DBG) log("checkIsOtaCall: isOtaCall =" + isOtaCall);
        if (isOtaCall && (otaUtils == null)) {
            if (DBG) log("checkIsOtaCall: creating OtaUtils...");
            otaUtils = new OtaUtils(getApplicationContext(),
                                    this, mInCallPanel, mCallCard, mDialer);
        }
        return isOtaCall;
    }
    private boolean initOtaState() {
        boolean inOtaCall = false;
        if (mPhone.getPhoneType() == Phone.PHONE_TYPE_CDMA) {
            final PhoneApp app = PhoneApp.getInstance();
            if ((app.cdmaOtaScreenState == null) || (app.cdmaOtaProvisionData == null)) {
                if (DBG) log("initOtaState func - All CdmaOTA utility classes not initialized");
                return false;
            }
            inOtaCall = checkIsOtaCall(getIntent());
            if (inOtaCall) {
                OtaUtils.CdmaOtaInCallScreenUiState.State cdmaOtaInCallScreenState =
                        otaUtils.getCdmaOtaInCallScreenUiState();
                if (cdmaOtaInCallScreenState == OtaUtils.CdmaOtaInCallScreenUiState.State.NORMAL) {
                    if (DBG) log("initOtaState - in OTA Normal mode");
                    setInCallScreenMode(InCallScreenMode.OTA_NORMAL);
                } else if (cdmaOtaInCallScreenState ==
                                OtaUtils.CdmaOtaInCallScreenUiState.State.ENDED) {
                    if (DBG) log("initOtaState - in OTA END mode");
                    setInCallScreenMode(InCallScreenMode.OTA_ENDED);
                } else if (app.cdmaOtaScreenState.otaScreenState ==
                                CdmaOtaScreenState.OtaScreenState.OTA_STATUS_SUCCESS_FAILURE_DLG) {
                    if (DBG) log("initOtaState - set OTA END Mode");
                    setInCallScreenMode(InCallScreenMode.OTA_ENDED);
                } else {
                    if (DBG) log("initOtaState - Set OTA NORMAL Mode");
                    setInCallScreenMode(InCallScreenMode.OTA_NORMAL);
                }
            } else {
                if (otaUtils != null) {
                    otaUtils.cleanOtaScreen(false);
                }
            }
        }
        return inOtaCall;
    }
    public void updateMenuItems() {
        if (mInCallMenu != null) {
            boolean okToShowMenu =  mInCallMenu.updateItems(PhoneApp.getInstance().phone);
            if (!okToShowMenu) {
                dismissMenu(true);
            }
        }
    }
    public InCallControlState getUpdatedInCallControlState() {
        mInCallControlState.update();
        return mInCallControlState;
    }
    private void updateInCallBackground() {
        final boolean hasRingingCall = !mRingingCall.isIdle();
        final boolean hasActiveCall = !mForegroundCall.isIdle();
        final boolean hasHoldingCall = !mPhone.getBackgroundCall().isIdle();
        final PhoneApp app = PhoneApp.getInstance();
        final boolean bluetoothActive = app.showBluetoothIndication();
        int backgroundResId = R.drawable.bg_in_call_gradient_unidentified;
        if (hasRingingCall) {
            if (bluetoothActive) {
                backgroundResId = R.drawable.bg_in_call_gradient_bluetooth;
            } else {
                backgroundResId = R.drawable.bg_in_call_gradient_unidentified;
            }
        } else if (hasHoldingCall && !hasActiveCall) {
            backgroundResId = R.drawable.bg_in_call_gradient_on_hold;
        } else {
            final Call.State fgState = mForegroundCall.getState();
            switch (fgState) {
                case ACTIVE:
                case DISCONNECTING:  
                    if (bluetoothActive) {
                        backgroundResId = R.drawable.bg_in_call_gradient_bluetooth;
                    } else {
                        backgroundResId = R.drawable.bg_in_call_gradient_connected;
                    }
                    break;
                case DISCONNECTED:
                    backgroundResId = R.drawable.bg_in_call_gradient_ended;
                    break;
                case DIALING:
                case ALERTING:
                    if (bluetoothActive) {
                        backgroundResId = R.drawable.bg_in_call_gradient_bluetooth;
                    } else {
                        backgroundResId = R.drawable.bg_in_call_gradient_unidentified;
                    }
                    break;
                default:
                    backgroundResId = R.drawable.bg_in_call_gradient_unidentified;
                    break;
            }
        }
        mMainFrame.setBackgroundResource(backgroundResId);
    }
    public void resetInCallScreenMode() {
        if (DBG) log("resetInCallScreenMode - InCallScreenMode set to UNDEFINED");
        setInCallScreenMode(InCallScreenMode.UNDEFINED);
    }
    private void clearProvider() {
        mProviderOverlayVisible = false;
        mProviderLabel = null;
        mProviderIcon = null;
        mProviderGatewayUri = null;
        mProviderAddress = null;
    }
     void updateSlidingTabHint(int hintTextResId, int hintColorResId) {
        if (VDBG) log("updateRotarySelectorHint(" + hintTextResId + ")...");
        if (mCallCard != null) {
            mCallCard.setRotarySelectorHint(hintTextResId, hintColorResId);
            mCallCard.updateState(mPhone);
        }
    }
    @Override
    public boolean dispatchPopulateAccessibilityEvent(AccessibilityEvent event) {
        super.dispatchPopulateAccessibilityEvent(event);
        mCallCard.dispatchPopulateAccessibilityEvent(event);
        return true;
    }
    private void log(String msg) {
        Log.d(LOG_TAG, msg);
    }
}
