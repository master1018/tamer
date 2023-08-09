public class KeyguardViewMediator implements KeyguardViewCallback,
        KeyguardUpdateMonitor.SimStateCallback {
    private final static boolean DEBUG = false && Config.LOGD;
    private final static boolean DBG_WAKE = DEBUG || true;
    private final static String TAG = "KeyguardViewMediator";
    private static final String DELAYED_KEYGUARD_ACTION =
        "com.android.internal.policy.impl.PhoneWindowManager.DELAYED_KEYGUARD";
    private static final int TIMEOUT = 1;
    private static final int SHOW = 2;
    private static final int HIDE = 3;
    private static final int RESET = 4;
    private static final int VERIFY_UNLOCK = 5;
    private static final int NOTIFY_SCREEN_OFF = 6;
    private static final int NOTIFY_SCREEN_ON = 7;
    private static final int WAKE_WHEN_READY = 8;
    private static final int KEYGUARD_DONE = 9;
    private static final int KEYGUARD_DONE_DRAWING = 10;
    private static final int KEYGUARD_DONE_AUTHENTICATING = 11;
    private static final int SET_HIDDEN = 12;
    private static final int KEYGUARD_TIMEOUT = 13;
    protected static final int AWAKE_INTERVAL_DEFAULT_MS = 5000;
    protected static final int AWAKE_INTERVAL_DEFAULT_KEYBOARD_OPEN_MS = 10000;
    private static final int KEYGUARD_DELAY_MS = 5000;
    private static final int KEYGUARD_DONE_DRAWING_TIMEOUT_MS = 2000;
    private Context mContext;
    private AlarmManager mAlarmManager;
    private StatusBarManager mStatusBarManager;
    private boolean mShowLockIcon = false;
    private IBinder mSecureLockIcon = null;
    private boolean mSystemReady;
    private boolean mSuppressNextLockSound = true;
    LocalPowerManager mRealPowerManager;
    private PowerManager mPM;
    private PowerManager.WakeLock mWakeLock;
    private PowerManager.WakeLock mShowKeyguardWakeLock;
    private PowerManager.WakeLock mWakeAndHandOff;
    private KeyguardViewManager mKeyguardViewManager;
    private boolean mExternallyEnabled = true;
    private boolean mNeedToReshowWhenReenabled = false;
    private boolean mShowing = false;
    private boolean mHidden = false;
    private int mDelayedShowingSequence;
    private int mWakelockSequence;
    private PhoneWindowManager mCallback;
    private WindowManagerPolicy.OnKeyguardExitResult mExitSecureCallback;
    private KeyguardViewProperties mKeyguardViewProperties;
    private KeyguardUpdateMonitor mUpdateMonitor;
    private boolean mKeyboardOpen = false;
    private boolean mScreenOn = false;
    private String mPhoneState = TelephonyManager.EXTRA_STATE_IDLE;
    private Intent mUserPresentIntent;
    private boolean mWaitingUntilKeyguardVisible = false;
    public KeyguardViewMediator(Context context, PhoneWindowManager callback,
            LocalPowerManager powerManager) {
        mContext = context;
        mRealPowerManager = powerManager;
        mPM = (PowerManager) context.getSystemService(Context.POWER_SERVICE);
        mWakeLock = mPM.newWakeLock(
                PowerManager.FULL_WAKE_LOCK | PowerManager.ACQUIRE_CAUSES_WAKEUP,
                "keyguard");
        mWakeLock.setReferenceCounted(false);
        mShowKeyguardWakeLock = mPM.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "show keyguard");
        mShowKeyguardWakeLock.setReferenceCounted(false);
        mWakeAndHandOff = mPM.newWakeLock(
                PowerManager.PARTIAL_WAKE_LOCK,
                "keyguardWakeAndHandOff");
        mWakeAndHandOff.setReferenceCounted(false);
        IntentFilter filter = new IntentFilter();
        filter.addAction(DELAYED_KEYGUARD_ACTION);
        filter.addAction(TelephonyManager.ACTION_PHONE_STATE_CHANGED);
        context.registerReceiver(mBroadCastReceiver, filter);
        mAlarmManager = (AlarmManager) context
                .getSystemService(Context.ALARM_SERVICE);
        mCallback = callback;
        mUpdateMonitor = new KeyguardUpdateMonitor(context);
        mUpdateMonitor.registerSimStateCallback(this);
        mKeyguardViewProperties = new LockPatternKeyguardViewProperties(
                new LockPatternUtils(mContext), mUpdateMonitor);
        mKeyguardViewManager = new KeyguardViewManager(
                context, WindowManagerImpl.getDefault(), this,
                mKeyguardViewProperties, mUpdateMonitor);
        mUserPresentIntent = new Intent(Intent.ACTION_USER_PRESENT);
        mUserPresentIntent.addFlags(Intent.FLAG_RECEIVER_REPLACE_PENDING);
        final ContentResolver cr = mContext.getContentResolver();
        mShowLockIcon = (Settings.System.getInt(cr, "show_status_bar_lock", 0) == 1);
    }
    public void onSystemReady() {
        synchronized (this) {
            if (DEBUG) Log.d(TAG, "onSystemReady");
            mSystemReady = true;
            doKeyguard();
        }
    }
    public void onScreenTurnedOff(int why) {
        synchronized (this) {
            mScreenOn = false;
            if (DEBUG) Log.d(TAG, "onScreenTurnedOff(" + why + ")");
            if (mExitSecureCallback != null) {
                if (DEBUG) Log.d(TAG, "pending exit secure callback cancelled");
                mExitSecureCallback.onKeyguardExitResult(false);
                mExitSecureCallback = null;
                if (!mExternallyEnabled) {
                    hideLocked();
                }
            } else if (mShowing) {
                notifyScreenOffLocked();
                resetStateLocked();
            } else if (why == WindowManagerPolicy.OFF_BECAUSE_OF_TIMEOUT) {
                long when = SystemClock.elapsedRealtime() + KEYGUARD_DELAY_MS;
                Intent intent = new Intent(DELAYED_KEYGUARD_ACTION);
                intent.putExtra("seq", mDelayedShowingSequence);
                PendingIntent sender = PendingIntent.getBroadcast(mContext,
                        0, intent, PendingIntent.FLAG_CANCEL_CURRENT);
                mAlarmManager.set(AlarmManager.ELAPSED_REALTIME_WAKEUP, when,
                        sender);
                if (DEBUG) Log.d(TAG, "setting alarm to turn off keyguard, seq = "
                                 + mDelayedShowingSequence);
            } else if (why == WindowManagerPolicy.OFF_BECAUSE_OF_PROX_SENSOR) {
            } else {
                doKeyguard();
            }
        }
    }
    public void onScreenTurnedOn() {
        synchronized (this) {
            mScreenOn = true;
            mDelayedShowingSequence++;
            if (DEBUG) Log.d(TAG, "onScreenTurnedOn, seq = " + mDelayedShowingSequence);
            notifyScreenOnLocked();
        }
    }
    public void setKeyguardEnabled(boolean enabled) {
        synchronized (this) {
            if (DEBUG) Log.d(TAG, "setKeyguardEnabled(" + enabled + ")");
            mExternallyEnabled = enabled;
            if (!enabled && mShowing) {
                if (mExitSecureCallback != null) {
                    if (DEBUG) Log.d(TAG, "in process of verifyUnlock request, ignoring");
                    return;
                }
                if (DEBUG) Log.d(TAG, "remembering to reshow, hiding keyguard, "
                        + "disabling status bar expansion");
                mNeedToReshowWhenReenabled = true;
                hideLocked();
            } else if (enabled && mNeedToReshowWhenReenabled) {
                if (DEBUG) Log.d(TAG, "previously hidden, reshowing, reenabling "
                        + "status bar expansion");
                mNeedToReshowWhenReenabled = false;
                if (mExitSecureCallback != null) {
                    if (DEBUG) Log.d(TAG, "onKeyguardExitResult(false), resetting");
                    mExitSecureCallback.onKeyguardExitResult(false);
                    mExitSecureCallback = null;
                    resetStateLocked();
                } else {
                    showLocked();
                    mWaitingUntilKeyguardVisible = true;
                    mHandler.sendEmptyMessageDelayed(KEYGUARD_DONE_DRAWING, KEYGUARD_DONE_DRAWING_TIMEOUT_MS);
                    if (DEBUG) Log.d(TAG, "waiting until mWaitingUntilKeyguardVisible is false");
                    while (mWaitingUntilKeyguardVisible) {
                        try {
                            wait();
                        } catch (InterruptedException e) {
                            Thread.currentThread().interrupt();
                        }
                    }
                    if (DEBUG) Log.d(TAG, "done waiting for mWaitingUntilKeyguardVisible");
                }
            }
        }
    }
    public void verifyUnlock(WindowManagerPolicy.OnKeyguardExitResult callback) {
        synchronized (this) {
            if (DEBUG) Log.d(TAG, "verifyUnlock");
            if (!mUpdateMonitor.isDeviceProvisioned()) {
                if (DEBUG) Log.d(TAG, "ignoring because device isn't provisioned");
                callback.onKeyguardExitResult(false);
            } else if (mExternallyEnabled) {
                Log.w(TAG, "verifyUnlock called when not externally disabled");
                callback.onKeyguardExitResult(false);
            } else if (mExitSecureCallback != null) {
                callback.onKeyguardExitResult(false);
            } else {
                mExitSecureCallback = callback;
                verifyUnlockLocked();
            }
        }
    }
    public boolean isShowing() {
        return mShowing;
    }
    public boolean isShowingAndNotHidden() {
        return mShowing && !mHidden;
    }
    public void setHidden(boolean isHidden) {
        if (DEBUG) Log.d(TAG, "setHidden " + isHidden);
        mHandler.removeMessages(SET_HIDDEN);
        Message msg = mHandler.obtainMessage(SET_HIDDEN, (isHidden ? 1 : 0), 0);
        mHandler.sendMessage(msg);
    }
    private void handleSetHidden(boolean isHidden) {
        synchronized (KeyguardViewMediator.this) {
            if (mHidden != isHidden) {
                mHidden = isHidden;
                adjustUserActivityLocked();
                adjustStatusBarLocked();
            }
        }
    }
    public void doKeyguardTimeout() {
        mHandler.removeMessages(KEYGUARD_TIMEOUT);
        Message msg = mHandler.obtainMessage(KEYGUARD_TIMEOUT);
        mHandler.sendMessage(msg);
    }
    public boolean isInputRestricted() {
        return mShowing || mNeedToReshowWhenReenabled || !mUpdateMonitor.isDeviceProvisioned();
    }
    public boolean doLidChangeTq(boolean isLidOpen) {
        mKeyboardOpen = isLidOpen;
        if (mUpdateMonitor.isKeyguardBypassEnabled() && mKeyboardOpen
                && !mKeyguardViewProperties.isSecure() && mKeyguardViewManager.isShowing()) {
            if (DEBUG) Log.d(TAG, "bypassing keyguard on sliding open of keyboard with non-secure keyguard");
            mHandler.sendEmptyMessage(KEYGUARD_DONE_AUTHENTICATING);
            return true;
        }
        return false;
    }
    private void doKeyguard() {
        synchronized (this) {
            if (!mExternallyEnabled) {
                if (DEBUG) Log.d(TAG, "doKeyguard: not showing because externally disabled");
                return;
            }
            if (mKeyguardViewManager.isShowing()) {
                if (DEBUG) Log.d(TAG, "doKeyguard: not showing because it is already showing");
                return;
            }
            final boolean requireSim = !SystemProperties.getBoolean("keyguard.no_require_sim",
                    false);
            final boolean provisioned = mUpdateMonitor.isDeviceProvisioned();
            final IccCard.State state = mUpdateMonitor.getSimState();
            final boolean lockedOrMissing = state.isPinLocked()
                    || ((state == IccCard.State.ABSENT) && requireSim);
            if (!lockedOrMissing && !provisioned) {
                if (DEBUG) Log.d(TAG, "doKeyguard: not showing because device isn't provisioned"
                        + " and the sim is not locked or missing");
                return;
            }
            if (DEBUG) Log.d(TAG, "doKeyguard: showing the lock screen");
            showLocked();
        }
    }
    private void resetStateLocked() {
        if (DEBUG) Log.d(TAG, "resetStateLocked");
        Message msg = mHandler.obtainMessage(RESET);
        mHandler.sendMessage(msg);
    }
    private void verifyUnlockLocked() {
        if (DEBUG) Log.d(TAG, "verifyUnlockLocked");
        mHandler.sendEmptyMessage(VERIFY_UNLOCK);
    }
    private void notifyScreenOffLocked() {
        if (DEBUG) Log.d(TAG, "notifyScreenOffLocked");
        mHandler.sendEmptyMessage(NOTIFY_SCREEN_OFF);
    }
    private void notifyScreenOnLocked() {
        if (DEBUG) Log.d(TAG, "notifyScreenOnLocked");
        mHandler.sendEmptyMessage(NOTIFY_SCREEN_ON);
    }
    private void wakeWhenReadyLocked(int keyCode) {
        if (DBG_WAKE) Log.d(TAG, "wakeWhenReadyLocked(" + keyCode + ")");
        mWakeAndHandOff.acquire();
        Message msg = mHandler.obtainMessage(WAKE_WHEN_READY, keyCode, 0);
        mHandler.sendMessage(msg);
    }
    private void showLocked() {
        if (DEBUG) Log.d(TAG, "showLocked");
        mShowKeyguardWakeLock.acquire();
        Message msg = mHandler.obtainMessage(SHOW);
        mHandler.sendMessage(msg);
    }
    private void hideLocked() {
        if (DEBUG) Log.d(TAG, "hideLocked");
        Message msg = mHandler.obtainMessage(HIDE);
        mHandler.sendMessage(msg);
    }
    public void onSimStateChanged(IccCard.State simState) {
        if (DEBUG) Log.d(TAG, "onSimStateChanged: " + simState);
        switch (simState) {
            case ABSENT:
                if (!mUpdateMonitor.isDeviceProvisioned()) {
                    if (!isShowing()) {
                        if (DEBUG) Log.d(TAG, "INTENT_VALUE_ICC_ABSENT and keygaurd isn't showing, we need "
                             + "to show the keyguard since the device isn't provisioned yet.");
                        doKeyguard();
                    } else {
                        resetStateLocked();
                    }
                }
                break;
            case PIN_REQUIRED:
            case PUK_REQUIRED:
                if (!isShowing()) {
                    if (DEBUG) Log.d(TAG, "INTENT_VALUE_ICC_LOCKED and keygaurd isn't showing, we need "
                            + "to show the keyguard so the user can enter their sim pin");
                    doKeyguard();
                } else {
                    resetStateLocked();
                }
                break;
            case READY:
                if (isShowing()) {
                    resetStateLocked();
                }
                break;
        }
    }
    public boolean isSecure() {
        return mKeyguardViewProperties.isSecure();
    }
    private BroadcastReceiver mBroadCastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            final String action = intent.getAction();
            if (action.equals(DELAYED_KEYGUARD_ACTION)) {
                int sequence = intent.getIntExtra("seq", 0);
                if (false) Log.d(TAG, "received DELAYED_KEYGUARD_ACTION with seq = "
                        + sequence + ", mDelayedShowingSequence = " + mDelayedShowingSequence);
                if (mDelayedShowingSequence == sequence) {
                    mSuppressNextLockSound = true;
                    doKeyguard();
                }
            } else if (TelephonyManager.ACTION_PHONE_STATE_CHANGED.equals(action)) {
                mPhoneState = intent.getStringExtra(TelephonyManager.EXTRA_STATE);
                if (TelephonyManager.EXTRA_STATE_IDLE.equals(mPhoneState)  
                        && !mScreenOn                           
                        && mExternallyEnabled) {                
                    if (DEBUG) Log.d(TAG, "screen is off and call ended, let's make sure the "
                            + "keyguard is showing");
                    doKeyguard();
                }
            }
        }
    };
    public boolean onWakeKeyWhenKeyguardShowingTq(int keyCode) {
        if (DEBUG) Log.d(TAG, "onWakeKeyWhenKeyguardShowing(" + keyCode + ")");
        if (isWakeKeyWhenKeyguardShowing(keyCode)) {
            wakeWhenReadyLocked(keyCode);
            return true;
        } else {
            return false;
        }
    }
    private boolean isWakeKeyWhenKeyguardShowing(int keyCode) {
        switch (keyCode) {
            case KeyEvent.KEYCODE_VOLUME_UP:
            case KeyEvent.KEYCODE_VOLUME_DOWN:
            case KeyEvent.KEYCODE_MUTE:
            case KeyEvent.KEYCODE_HEADSETHOOK:
            case KeyEvent.KEYCODE_MEDIA_PLAY_PAUSE:
            case KeyEvent.KEYCODE_MEDIA_STOP:
            case KeyEvent.KEYCODE_MEDIA_NEXT:
            case KeyEvent.KEYCODE_MEDIA_PREVIOUS:
            case KeyEvent.KEYCODE_MEDIA_REWIND:
            case KeyEvent.KEYCODE_MEDIA_FAST_FORWARD:
            case KeyEvent.KEYCODE_CAMERA:
                return false;
        }
        return true;
    }
    public void pokeWakelock() {
        pokeWakelock(mKeyboardOpen ?
                AWAKE_INTERVAL_DEFAULT_KEYBOARD_OPEN_MS : AWAKE_INTERVAL_DEFAULT_MS);
    }
    public void pokeWakelock(int holdMs) {
        synchronized (this) {
            if (DBG_WAKE) Log.d(TAG, "pokeWakelock(" + holdMs + ")");
            mWakeLock.acquire();
            mHandler.removeMessages(TIMEOUT);
            mWakelockSequence++;
            Message msg = mHandler.obtainMessage(TIMEOUT, mWakelockSequence, 0);
            mHandler.sendMessageDelayed(msg, holdMs);
        }
    }
    public void keyguardDone(boolean authenticated) {
        keyguardDone(authenticated, true);
    }
    public void keyguardDone(boolean authenticated, boolean wakeup) {
        synchronized (this) {
            EventLog.writeEvent(70000, 2);
            if (DEBUG) Log.d(TAG, "keyguardDone(" + authenticated + ")");
            Message msg = mHandler.obtainMessage(KEYGUARD_DONE);
            msg.arg1 = wakeup ? 1 : 0;
            mHandler.sendMessage(msg);
            if (authenticated) {
                mUpdateMonitor.clearFailedAttempts();
            }
            if (mExitSecureCallback != null) {
                mExitSecureCallback.onKeyguardExitResult(authenticated);
                mExitSecureCallback = null;
                if (authenticated) {
                    mExternallyEnabled = true;
                    mNeedToReshowWhenReenabled = false;
                }
            }
        }
    }
    public void keyguardDoneDrawing() {
        mHandler.sendEmptyMessage(KEYGUARD_DONE_DRAWING);
    }
    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case TIMEOUT:
                    handleTimeout(msg.arg1);
                    return ;
                case SHOW:
                    handleShow();
                    return ;
                case HIDE:
                    handleHide();
                    return ;
                case RESET:
                    handleReset();
                    return ;
                case VERIFY_UNLOCK:
                    handleVerifyUnlock();
                    return;
                case NOTIFY_SCREEN_OFF:
                    handleNotifyScreenOff();
                    return;
                case NOTIFY_SCREEN_ON:
                    handleNotifyScreenOn();
                    return;
                case WAKE_WHEN_READY:
                    handleWakeWhenReady(msg.arg1);
                    return;
                case KEYGUARD_DONE:
                    handleKeyguardDone(msg.arg1 != 0);
                    return;
                case KEYGUARD_DONE_DRAWING:
                    handleKeyguardDoneDrawing();
                    return;
                case KEYGUARD_DONE_AUTHENTICATING:
                    keyguardDone(true);
                    return;
                case SET_HIDDEN:
                    handleSetHidden(msg.arg1 != 0);
                    break;
                case KEYGUARD_TIMEOUT:
                    doKeyguard();
                    break;
            }
        }
    };
    private void handleKeyguardDone(boolean wakeup) {
        if (DEBUG) Log.d(TAG, "handleKeyguardDone");
        handleHide();
        if (wakeup) {
            mPM.userActivity(SystemClock.uptimeMillis(), true);
        }
        mWakeLock.release();
        mContext.sendBroadcast(mUserPresentIntent);
    }
    private void handleKeyguardDoneDrawing() {
        synchronized(this) {
            if (false) Log.d(TAG, "handleKeyguardDoneDrawing");
            if (mWaitingUntilKeyguardVisible) {
                if (DEBUG) Log.d(TAG, "handleKeyguardDoneDrawing: notifying mWaitingUntilKeyguardVisible");
                mWaitingUntilKeyguardVisible = false;
                notifyAll();
                mHandler.removeMessages(KEYGUARD_DONE_DRAWING);
            }
        }
    }
    private void handleTimeout(int seq) {
        synchronized (KeyguardViewMediator.this) {
            if (DEBUG) Log.d(TAG, "handleTimeout");
            if (seq == mWakelockSequence) {
                mWakeLock.release();
            }
        }
    }
    private void playSounds(boolean locked) {
        if (mSuppressNextLockSound) {
            mSuppressNextLockSound = false;
            return;
        }
        final ContentResolver cr = mContext.getContentResolver();
        if (Settings.System.getInt(cr, Settings.System.LOCKSCREEN_SOUNDS_ENABLED, 1) == 1)
        {
            final String whichSound = locked
                ? Settings.System.LOCK_SOUND
                : Settings.System.UNLOCK_SOUND;
            final String soundPath = Settings.System.getString(cr, whichSound);
            if (soundPath != null) {
                final Uri soundUri = Uri.parse("file:
                if (soundUri != null) {
                    final Ringtone sfx = RingtoneManager.getRingtone(mContext, soundUri);
                    if (sfx != null) {
                        sfx.setStreamType(AudioManager.STREAM_SYSTEM);
                        sfx.play();
                    } else {
                        Log.d(TAG, "playSounds: failed to load ringtone from uri: " + soundUri);
                    }
                } else {
                    Log.d(TAG, "playSounds: could not parse Uri: " + soundPath);
                }
            } else {
                Log.d(TAG, "playSounds: whichSound = " + whichSound + "; soundPath was null");
            }
        }
    }        
    private void handleShow() {
        synchronized (KeyguardViewMediator.this) {
            if (DEBUG) Log.d(TAG, "handleShow");
            if (!mSystemReady) return;
            playSounds(true);
            mKeyguardViewManager.show();
            mShowing = true;
            adjustUserActivityLocked();
            adjustStatusBarLocked();
            try {
                ActivityManagerNative.getDefault().closeSystemDialogs("lock");
            } catch (RemoteException e) {
            }
            mShowKeyguardWakeLock.release();
        }
    }
    private void handleHide() {
        synchronized (KeyguardViewMediator.this) {
            if (DEBUG) Log.d(TAG, "handleHide");
            if (mWakeAndHandOff.isHeld()) {
                Log.w(TAG, "attempt to hide the keyguard while waking, ignored");
                return;
            }
            if (TelephonyManager.EXTRA_STATE_IDLE.equals(mPhoneState)) {
                playSounds(false);
            }
            mKeyguardViewManager.hide();
            mShowing = false;
            adjustUserActivityLocked();
            adjustStatusBarLocked();
        }
    }
    private void adjustUserActivityLocked() {
        if (DEBUG) Log.d(TAG, "adjustUserActivityLocked mShowing: " + mShowing + " mHidden: " + mHidden);
        boolean enabled = !mShowing || mHidden;
        mRealPowerManager.enableUserActivity(enabled);
        if (!enabled && mScreenOn) {
            pokeWakelock();
        }
    }
    private void adjustStatusBarLocked() {
        if (mStatusBarManager == null) {
            mStatusBarManager = (StatusBarManager)
                    mContext.getSystemService(Context.STATUS_BAR_SERVICE);
        }
        if (mStatusBarManager == null) {
            Log.w(TAG, "Could not get status bar manager");
        } else {
            if (mShowLockIcon) {
                if (mShowing && isSecure()) {
                    if (mSecureLockIcon == null) {
                        mSecureLockIcon = mStatusBarManager.addIcon("secure",
                            com.android.internal.R.drawable.stat_sys_secure, 0);
                    }
                } else {
                    if (mSecureLockIcon != null) {
                        mStatusBarManager.removeIcon(mSecureLockIcon);
                        mSecureLockIcon = null;
                    }
                }
            }
            boolean enable = !mShowing || (mHidden && !isSecure());
            mStatusBarManager.disable(enable ?
                         StatusBarManager.DISABLE_NONE :
                         StatusBarManager.DISABLE_EXPAND);
        }
    }
    private void handleWakeWhenReady(int keyCode) {
        synchronized (KeyguardViewMediator.this) {
            if (DBG_WAKE) Log.d(TAG, "handleWakeWhenReady(" + keyCode + ")");
            if (!mKeyguardViewManager.wakeWhenReadyTq(keyCode)) {
                Log.w(TAG, "mKeyguardViewManager.wakeWhenReadyTq did not poke wake lock, so poke it ourselves");
                pokeWakelock();
            }
            mWakeAndHandOff.release();
            if (!mWakeLock.isHeld()) {
                Log.w(TAG, "mWakeLock not held in mKeyguardViewManager.wakeWhenReadyTq");
            }
        }
    }
    private void handleReset() {
        synchronized (KeyguardViewMediator.this) {
            if (DEBUG) Log.d(TAG, "handleReset");
            mKeyguardViewManager.reset();
        }
    }
    private void handleVerifyUnlock() {
        synchronized (KeyguardViewMediator.this) {
            if (DEBUG) Log.d(TAG, "handleVerifyUnlock");
            mKeyguardViewManager.verifyUnlock();
            mShowing = true;
        }
    }
    private void handleNotifyScreenOff() {
        synchronized (KeyguardViewMediator.this) {
            if (DEBUG) Log.d(TAG, "handleNotifyScreenOff");
            mKeyguardViewManager.onScreenTurnedOff();
        }
    }
    private void handleNotifyScreenOn() {
        synchronized (KeyguardViewMediator.this) {
            if (DEBUG) Log.d(TAG, "handleNotifyScreenOn");
            mKeyguardViewManager.onScreenTurnedOn();
        }
    }
}
