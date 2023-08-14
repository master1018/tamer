public class LockPatternKeyguardViewProperties implements KeyguardViewProperties {
    private final LockPatternUtils mLockPatternUtils;
    private final KeyguardUpdateMonitor mUpdateMonitor;
    public LockPatternKeyguardViewProperties(LockPatternUtils lockPatternUtils,
            KeyguardUpdateMonitor updateMonitor) {
        mLockPatternUtils = lockPatternUtils;
        mUpdateMonitor = updateMonitor;
    }
    public KeyguardViewBase createKeyguardView(Context context,
            KeyguardUpdateMonitor updateMonitor,
            KeyguardWindowController controller) {
        return new LockPatternKeyguardView(context, updateMonitor,
                mLockPatternUtils, controller);
    }
    public boolean isSecure() {
        return mLockPatternUtils.isSecure() || isSimPinSecure();
    }
    private boolean isSimPinSecure() {
        final IccCard.State simState = mUpdateMonitor.getSimState();
        return (simState == IccCard.State.PIN_REQUIRED || simState == IccCard.State.PUK_REQUIRED
            || simState == IccCard.State.ABSENT);
    }
}
