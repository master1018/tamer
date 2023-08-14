public class KeyguardManager {
    private IWindowManager mWM;
    public class KeyguardLock {
        private IBinder mToken = new Binder();
        private String mTag;
        KeyguardLock(String tag) {
            mTag = tag;
        }
        public void disableKeyguard() {
            try {
                mWM.disableKeyguard(mToken, mTag);
            } catch (RemoteException ex) {
            }
        }
        public void reenableKeyguard() {
            try {
                mWM.reenableKeyguard(mToken);
            } catch (RemoteException ex) {
            }
        }
    }
    public interface OnKeyguardExitResult {
        void onKeyguardExitResult(boolean success);
    }
    KeyguardManager() {
        mWM = IWindowManager.Stub.asInterface(ServiceManager.getService(Context.WINDOW_SERVICE));
    }
    public KeyguardLock newKeyguardLock(String tag) {
        return new KeyguardLock(tag);
    }
    public boolean inKeyguardRestrictedInputMode() {
        try {
            return mWM.inKeyguardRestrictedInputMode();
        } catch (RemoteException ex) {
            return false;
        }
    }
    public void exitKeyguardSecurely(final OnKeyguardExitResult callback) {
        try {
            mWM.exitKeyguardSecurely(new IOnKeyguardExitResult.Stub() {
                public void onKeyguardExitResult(boolean success) throws RemoteException {
                    callback.onKeyguardExitResult(success);
                }
            });
        } catch (RemoteException e) {
        }
    }
}
