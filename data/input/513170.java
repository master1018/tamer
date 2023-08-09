public class JsResult {
    private boolean mReady;
    private boolean mTriedToNotifyBeforeReady;
    protected boolean mResult;
    protected final CallbackProxy mProxy;
    private final boolean mDefaultValue;
    public final void cancel() {
        mResult = false;
        wakeUp();
    }
    public final void confirm() {
        mResult = true;
        wakeUp();
    }
     JsResult(CallbackProxy proxy, boolean defaultVal) {
        mProxy = proxy;
        mDefaultValue = defaultVal;
    }
     final boolean getResult() {
        return mResult;
    }
     final void setReady() {
        mReady = true;
        if (mTriedToNotifyBeforeReady) {
            wakeUp();
        }
    }
     void handleDefault() {
        setReady();
        mResult = mDefaultValue;
        wakeUp();
    }
    protected final void wakeUp() {
        if (mReady) {
            synchronized (mProxy) {
                mProxy.notify();
            }
        } else {
            mTriedToNotifyBeforeReady = true;
        }
    }
}
