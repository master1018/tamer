public class LockPatternKeyguardView extends KeyguardViewBase {
    static final boolean DEBUG_CONFIGURATION = false;
    private static final int EMERGENCY_CALL_TIMEOUT = 10000;
    static final String ACTION_EMERGENCY_DIAL = "com.android.phone.EmergencyDialer.DIAL";
    private static final boolean DEBUG = false;
    private static final String TAG = "LockPatternKeyguardView";
    private final KeyguardUpdateMonitor mUpdateMonitor;
    private final KeyguardWindowController mWindowController;
    private View mLockScreen;
    private View mUnlockScreen;
    private boolean mScreenOn = false;
    private boolean mEnableFallback = false; 
    KeyguardScreenCallback mKeyguardScreenCallback;
    private boolean mRequiresSim;
    enum Mode {
        LockScreen,
        UnlockScreen
    }
    enum UnlockMode {
        Pattern,
        SimPin,
        Account,
        Password,
        Unknown
    }
    private Mode mMode = Mode.LockScreen;
    private UnlockMode mUnlockScreenMode;
    private boolean mForgotPattern;
    private boolean mIsVerifyUnlockOnly = false;
    private final LockPatternUtils mLockPatternUtils;
    private UnlockMode mCurrentUnlockMode = UnlockMode.Unknown;
    private Configuration mConfiguration;
    private boolean stuckOnLockScreenBecauseSimMissing() {
        return mRequiresSim
                && (!mUpdateMonitor.isDeviceProvisioned())
                && (mUpdateMonitor.getSimState() == IccCard.State.ABSENT);
    }
    public LockPatternKeyguardView(
            Context context,
            KeyguardUpdateMonitor updateMonitor,
            LockPatternUtils lockPatternUtils,
            KeyguardWindowController controller) {
        super(context);
        mConfiguration = context.getResources().getConfiguration();
        mEnableFallback = false;
        mRequiresSim =
                TextUtils.isEmpty(SystemProperties.get("keyguard.no_require_sim"));
        mUpdateMonitor = updateMonitor;
        mLockPatternUtils = lockPatternUtils;
        mWindowController = controller;
        mMode = getInitialMode();
        mKeyguardScreenCallback = new KeyguardScreenCallback() {
            public void goToLockScreen() {
                mForgotPattern = false;
                if (mIsVerifyUnlockOnly) {
                    mIsVerifyUnlockOnly = false;
                    getCallback().keyguardDone(false);
                } else {
                    updateScreen(Mode.LockScreen);
                }
            }
            public void goToUnlockScreen() {
                final IccCard.State simState = mUpdateMonitor.getSimState();
                if (stuckOnLockScreenBecauseSimMissing()
                         || (simState == IccCard.State.PUK_REQUIRED)){
                    return;
                }
                if (!isSecure()) {
                    getCallback().keyguardDone(true);
                } else {
                    updateScreen(Mode.UnlockScreen);
                }
            }
            public void forgotPattern(boolean isForgotten) {
                if (mEnableFallback) {
                    mForgotPattern = isForgotten;
                    updateScreen(Mode.UnlockScreen);
                }
            }
            public boolean isSecure() {
                return LockPatternKeyguardView.this.isSecure();
            }
            public boolean isVerifyUnlockOnly() {
                return mIsVerifyUnlockOnly;
            }
            public void recreateMe(Configuration config) {
                mConfiguration = config;
                recreateScreens();
            }
            public void takeEmergencyCallAction() {
                pokeWakelock(EMERGENCY_CALL_TIMEOUT);
                if (TelephonyManager.getDefault().getCallState()
                        == TelephonyManager.CALL_STATE_OFFHOOK) {
                    mLockPatternUtils.resumeCall();
                } else {
                    Intent intent = new Intent(ACTION_EMERGENCY_DIAL);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK
                            | Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS);
                    getContext().startActivity(intent);
                }
            }
            public void pokeWakelock() {
                getCallback().pokeWakelock();
            }
            public void pokeWakelock(int millis) {
                getCallback().pokeWakelock(millis);
            }
            public void keyguardDone(boolean authenticated) {
                getCallback().keyguardDone(authenticated);
            }
            public void keyguardDoneDrawing() {
            }
            public void reportFailedUnlockAttempt() {
                mUpdateMonitor.reportFailedAttempt();
                final int failedAttempts = mUpdateMonitor.getFailedAttempts();
                if (DEBUG) Log.d(TAG,
                    "reportFailedPatternAttempt: #" + failedAttempts +
                    " (enableFallback=" + mEnableFallback + ")");
                final boolean usingLockPattern = mLockPatternUtils.getKeyguardStoredPasswordQuality()
                        == DevicePolicyManager.PASSWORD_QUALITY_SOMETHING;
                if (usingLockPattern && mEnableFallback && failedAttempts ==
                        (LockPatternUtils.FAILED_ATTEMPTS_BEFORE_RESET
                                - LockPatternUtils.FAILED_ATTEMPTS_BEFORE_TIMEOUT)) {
                    showAlmostAtAccountLoginDialog();
                } else if (usingLockPattern && mEnableFallback
                        && failedAttempts >= LockPatternUtils.FAILED_ATTEMPTS_BEFORE_RESET) {
                    mLockPatternUtils.setPermanentlyLocked(true);
                    updateScreen(mMode);
                } else if ((failedAttempts % LockPatternUtils.FAILED_ATTEMPTS_BEFORE_TIMEOUT)
                        == 0) {
                    showTimeoutDialog();
                }
                mLockPatternUtils.reportFailedPasswordAttempt();
            }
            public boolean doesFallbackUnlockScreenExist() {
                return mEnableFallback;
            }
            public void reportSuccessfulUnlockAttempt() {
                mLockPatternUtils.reportSuccessfulPasswordAttempt();
            }
        };
        setFocusableInTouchMode(true);
        setDescendantFocusability(FOCUS_AFTER_DESCENDANTS);
        mLockScreen = createLockScreen();
        addView(mLockScreen);
        final UnlockMode unlockMode = getUnlockMode();
        if (DEBUG) Log.d(TAG,
            "LockPatternKeyguardView ctor: about to createUnlockScreenFor; mEnableFallback="
            + mEnableFallback);
        mUnlockScreen = createUnlockScreenFor(unlockMode);
        mUnlockScreenMode = unlockMode;
        maybeEnableFallback(context);
        addView(mUnlockScreen);
        updateScreen(mMode);
    }
    private class AccountAnalyzer implements AccountManagerCallback<Bundle> {
        private final AccountManager mAccountManager;
        private final Account[] mAccounts;
        private int mAccountIndex;
        private AccountAnalyzer(AccountManager accountManager) {
            mAccountManager = accountManager;
            mAccounts = accountManager.getAccountsByType("com.google");
        }
        private void next() {
            if (mEnableFallback || mAccountIndex >= mAccounts.length) {
                if (mUnlockScreen == null) {
                    Log.w(TAG, "no unlock screen when trying to enable fallback");
                } else if (mUnlockScreen instanceof PatternUnlockScreen) {
                    ((PatternUnlockScreen)mUnlockScreen).setEnableFallback(mEnableFallback);
                }
                return;
            }
            mAccountManager.confirmCredentials(mAccounts[mAccountIndex], null, null, this, null);
        }
        public void start() {
            mEnableFallback = false;
            mAccountIndex = 0;
            next();
        }
        public void run(AccountManagerFuture<Bundle> future) {
            try {
                Bundle result = future.getResult();
                if (result.getParcelable(AccountManager.KEY_INTENT) != null) {
                    mEnableFallback = true;
                }
            } catch (OperationCanceledException e) {
            } catch (IOException e) {
            } catch (AuthenticatorException e) {
            } finally {
                mAccountIndex++;
                next();
            }
        }
    }
    private void maybeEnableFallback(Context context) {
        AccountAnalyzer accountAnalyzer = new AccountAnalyzer(AccountManager.get(context));
        accountAnalyzer.start();
    }
    @Override
    protected void dispatchDraw(Canvas canvas) {
        if (DEBUG) Log.v(TAG, "*** dispatchDraw() time: " + SystemClock.elapsedRealtime());
        super.dispatchDraw(canvas);
    }
    @Override
    public void reset() {
        mIsVerifyUnlockOnly = false;
        mForgotPattern = false;
        updateScreen(getInitialMode());
    }
    @Override
    public void onScreenTurnedOff() {
        mScreenOn = false;
        mForgotPattern = false;
        if (mMode == Mode.LockScreen) {
           ((KeyguardScreen) mLockScreen).onPause();
        } else {
            ((KeyguardScreen) mUnlockScreen).onPause();
        }
    }
    @Override
    public void onScreenTurnedOn() {
        mScreenOn = true;
        if (mMode == Mode.LockScreen) {
           ((KeyguardScreen) mLockScreen).onResume();
        } else {
            ((KeyguardScreen) mUnlockScreen).onResume();
        }
    }
    private void recreateLockScreen() {
        if (mLockScreen.getVisibility() == View.VISIBLE) {
            ((KeyguardScreen) mLockScreen).onPause();
        }
        ((KeyguardScreen) mLockScreen).cleanUp();
        removeView(mLockScreen);
        mLockScreen = createLockScreen();
        mLockScreen.setVisibility(View.INVISIBLE);
        addView(mLockScreen);
    }
    private void recreateUnlockScreen() {
        if (mUnlockScreen.getVisibility() == View.VISIBLE) {
            ((KeyguardScreen) mUnlockScreen).onPause();
        }
        ((KeyguardScreen) mUnlockScreen).cleanUp();
        removeView(mUnlockScreen);
        final UnlockMode unlockMode = getUnlockMode();
        mUnlockScreen = createUnlockScreenFor(unlockMode);
        mUnlockScreen.setVisibility(View.INVISIBLE);
        mUnlockScreenMode = unlockMode;
        addView(mUnlockScreen);
    }
    private void recreateScreens() {
        recreateLockScreen();
        recreateUnlockScreen();
        updateScreen(mMode);
    }
    @Override
    public void wakeWhenReadyTq(int keyCode) {
        if (DEBUG) Log.d(TAG, "onWakeKey");
        if (keyCode == KeyEvent.KEYCODE_MENU && isSecure() && (mMode == Mode.LockScreen)
                && (mUpdateMonitor.getSimState() != IccCard.State.PUK_REQUIRED)) {
            if (DEBUG) Log.d(TAG, "switching screens to unlock screen because wake key was MENU");
            updateScreen(Mode.UnlockScreen);
            getCallback().pokeWakelock();
        } else {
            if (DEBUG) Log.d(TAG, "poking wake lock immediately");
            getCallback().pokeWakelock();
        }
    }
    @Override
    public void verifyUnlock() {
        if (!isSecure()) {
            getCallback().keyguardDone(true);
        } else if (mUnlockScreenMode != UnlockMode.Pattern) {
            getCallback().keyguardDone(false);
        } else {
            mIsVerifyUnlockOnly = true;
            updateScreen(Mode.UnlockScreen);
        }
    }
    @Override
    public void cleanUp() {
        ((KeyguardScreen) mLockScreen).onPause();
        ((KeyguardScreen) mLockScreen).cleanUp();
        ((KeyguardScreen) mUnlockScreen).onPause();
        ((KeyguardScreen) mUnlockScreen).cleanUp();
    }
    private boolean isSecure() {
        UnlockMode unlockMode = getUnlockMode();
        boolean secure = false;
        switch (unlockMode) {
            case Pattern:
                secure = mLockPatternUtils.isLockPatternEnabled();
                break;
            case SimPin:
                secure = mUpdateMonitor.getSimState() == IccCard.State.PIN_REQUIRED
                            || mUpdateMonitor.getSimState() == IccCard.State.PUK_REQUIRED;
                break;
            case Account:
                secure = true;
                break;
            case Password:
                secure = mLockPatternUtils.isLockPasswordEnabled();
                break;
            default:
                throw new IllegalStateException("unknown unlock mode " + unlockMode);
        }
        return secure;
    }
    private void updateScreen(final Mode mode) {
        if (DEBUG_CONFIGURATION) Log.v(TAG, "**** UPDATE SCREEN: mode=" + mode
                + " last mode=" + mMode, new RuntimeException());
        mMode = mode;
        if (mode == Mode.UnlockScreen && mCurrentUnlockMode != getUnlockMode()) {
            recreateUnlockScreen();
        }
        final View goneScreen = (mode == Mode.LockScreen) ? mUnlockScreen : mLockScreen;
        final View visibleScreen = (mode == Mode.LockScreen) ? mLockScreen : mUnlockScreen;
        mWindowController.setNeedsInput(((KeyguardScreen)visibleScreen).needsInput());
        if (DEBUG_CONFIGURATION) {
            Log.v(TAG, "Gone=" + goneScreen);
            Log.v(TAG, "Visible=" + visibleScreen);
        }
        if (mScreenOn) {
            if (goneScreen.getVisibility() == View.VISIBLE) {
                ((KeyguardScreen) goneScreen).onPause();
            }
            if (visibleScreen.getVisibility() != View.VISIBLE) {
                ((KeyguardScreen) visibleScreen).onResume();
            }
        }
        goneScreen.setVisibility(View.GONE);
        visibleScreen.setVisibility(View.VISIBLE);
        requestLayout();
        if (!visibleScreen.requestFocus()) {
            throw new IllegalStateException("keyguard screen must be able to take "
                    + "focus when shown " + visibleScreen.getClass().getCanonicalName());
        }
    }
    View createLockScreen() {
        return new LockScreen(
                mContext,
                mConfiguration,
                mLockPatternUtils,
                mUpdateMonitor,
                mKeyguardScreenCallback);
    }
    View createUnlockScreenFor(UnlockMode unlockMode) {
        View unlockView = null;
        if (unlockMode == UnlockMode.Pattern) {
            PatternUnlockScreen view = new PatternUnlockScreen(
                    mContext,
                    mConfiguration,
                    mLockPatternUtils,
                    mUpdateMonitor,
                    mKeyguardScreenCallback,
                    mUpdateMonitor.getFailedAttempts());
            if (DEBUG) Log.d(TAG,
                "createUnlockScreenFor(" + unlockMode + "): mEnableFallback=" + mEnableFallback);
            view.setEnableFallback(mEnableFallback);
            unlockView = view;
        } else if (unlockMode == UnlockMode.SimPin) {
            unlockView = new SimUnlockScreen(
                    mContext,
                    mConfiguration,
                    mUpdateMonitor,
                    mKeyguardScreenCallback,
                    mLockPatternUtils);
        } else if (unlockMode == UnlockMode.Account) {
            try {
                unlockView = new AccountUnlockScreen(
                        mContext,
                        mConfiguration,
                        mUpdateMonitor,
                        mKeyguardScreenCallback,
                        mLockPatternUtils);
            } catch (IllegalStateException e) {
                Log.i(TAG, "Couldn't instantiate AccountUnlockScreen"
                      + " (IAccountsService isn't available)");
                unlockView = createUnlockScreenFor(UnlockMode.Pattern);
            }
        } else if (unlockMode == UnlockMode.Password) {
            unlockView = new PasswordUnlockScreen(
                    mContext,
                    mConfiguration,
                    mLockPatternUtils,
                    mUpdateMonitor,
                    mKeyguardScreenCallback);
        } else {
            throw new IllegalArgumentException("unknown unlock mode " + unlockMode);
        }
        mCurrentUnlockMode = unlockMode;
        return unlockView;
    }
    private Mode getInitialMode() {
        final IccCard.State simState = mUpdateMonitor.getSimState();
        if (stuckOnLockScreenBecauseSimMissing() || (simState == IccCard.State.PUK_REQUIRED)) {
            return Mode.LockScreen;
        } else {
            final boolean usingLockPattern = mLockPatternUtils.getKeyguardStoredPasswordQuality()
                    == DevicePolicyManager.PASSWORD_QUALITY_SOMETHING;
            if (isSecure() && usingLockPattern) {
                return Mode.UnlockScreen;
            } else {
                return Mode.LockScreen;
            }
        }
    }
    private UnlockMode getUnlockMode() {
        final IccCard.State simState = mUpdateMonitor.getSimState();
        UnlockMode currentMode;
        if (simState == IccCard.State.PIN_REQUIRED || simState == IccCard.State.PUK_REQUIRED) {
            currentMode = UnlockMode.SimPin;
        } else {
            final int mode = mLockPatternUtils.getKeyguardStoredPasswordQuality();
            switch (mode) {
                case DevicePolicyManager.PASSWORD_QUALITY_NUMERIC:
                case DevicePolicyManager.PASSWORD_QUALITY_ALPHABETIC:
                case DevicePolicyManager.PASSWORD_QUALITY_ALPHANUMERIC:
                    currentMode = UnlockMode.Password;
                    break;
                case DevicePolicyManager.PASSWORD_QUALITY_SOMETHING:
                case DevicePolicyManager.PASSWORD_QUALITY_UNSPECIFIED:
                    if (mForgotPattern || mLockPatternUtils.isPermanentlyLocked()) {
                        currentMode = UnlockMode.Account;
                    } else {
                        currentMode = UnlockMode.Pattern;
                    }
                    break;
                default:
                   throw new IllegalStateException("Unknown unlock mode:" + mode);
            }
        }
        return currentMode;
    }
    private void showTimeoutDialog() {
        int timeoutInSeconds = (int) LockPatternUtils.FAILED_ATTEMPT_TIMEOUT_MS / 1000;
        String message = mContext.getString(
                R.string.lockscreen_too_many_failed_attempts_dialog_message,
                mUpdateMonitor.getFailedAttempts(),
                timeoutInSeconds);
        final AlertDialog dialog = new AlertDialog.Builder(mContext)
                .setTitle(null)
                .setMessage(message)
                .setNeutralButton(R.string.ok, null)
                .create();
        dialog.getWindow().setType(WindowManager.LayoutParams.TYPE_KEYGUARD_DIALOG);
        if (!mContext.getResources().getBoolean(
                com.android.internal.R.bool.config_sf_slowBlur)) {
            dialog.getWindow().setFlags(
                    WindowManager.LayoutParams.FLAG_BLUR_BEHIND,
                    WindowManager.LayoutParams.FLAG_BLUR_BEHIND);
        }
        dialog.show();
    }
    private void showAlmostAtAccountLoginDialog() {
        int timeoutInSeconds = (int) LockPatternUtils.FAILED_ATTEMPT_TIMEOUT_MS / 1000;
        String message = mContext.getString(
                R.string.lockscreen_failed_attempts_almost_glogin,
                LockPatternUtils.FAILED_ATTEMPTS_BEFORE_RESET
                - LockPatternUtils.FAILED_ATTEMPTS_BEFORE_TIMEOUT,
                LockPatternUtils.FAILED_ATTEMPTS_BEFORE_TIMEOUT,
                timeoutInSeconds);
        final AlertDialog dialog = new AlertDialog.Builder(mContext)
                .setTitle(null)
                .setMessage(message)
                .setNeutralButton(R.string.ok, null)
                .create();
        dialog.getWindow().setType(WindowManager.LayoutParams.TYPE_KEYGUARD_DIALOG);
        if (!mContext.getResources().getBoolean(
                com.android.internal.R.bool.config_sf_slowBlur)) {
            dialog.getWindow().setFlags(
                    WindowManager.LayoutParams.FLAG_BLUR_BEHIND,
                    WindowManager.LayoutParams.FLAG_BLUR_BEHIND);
        }
        dialog.show();
    }
    static private class FastBitmapDrawable extends Drawable {
        private Bitmap mBitmap;
        private int mOpacity;
        private FastBitmapDrawable(Bitmap bitmap) {
            mBitmap = bitmap;
            mOpacity = mBitmap.hasAlpha() ? PixelFormat.TRANSLUCENT : PixelFormat.OPAQUE;
        }
        @Override
        public void draw(Canvas canvas) {
            canvas.drawBitmap(
                    mBitmap,
                    (getBounds().width() - mBitmap.getWidth()) / 2,
                    (getBounds().height() - mBitmap.getHeight()),
                    null);
        }
        @Override
        public int getOpacity() {
            return mOpacity;
        }
        @Override
        public void setAlpha(int alpha) {
        }
        @Override
        public void setColorFilter(ColorFilter cf) {
        }
        @Override
        public int getIntrinsicWidth() {
            return mBitmap.getWidth();
        }
        @Override
        public int getIntrinsicHeight() {
            return mBitmap.getHeight();
        }
        @Override
        public int getMinimumWidth() {
            return mBitmap.getWidth();
        }
        @Override
        public int getMinimumHeight() {
            return mBitmap.getHeight();
        }
    }
}
