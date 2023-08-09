public abstract class BroadcastReceiver {
    public BroadcastReceiver() {
    }
    public abstract void onReceive(Context context, Intent intent);
    public IBinder peekService(Context myContext, Intent service) {
        IActivityManager am = ActivityManagerNative.getDefault();
        IBinder binder = null;
        try {
            binder = am.peekService(service, service.resolveTypeIfNeeded(
                    myContext.getContentResolver()));
        } catch (RemoteException e) {
        }
        return binder;
    }
    public final void setResultCode(int code) {
        checkSynchronousHint();
        mResultCode = code;
    }
    public final int getResultCode() {
        return mResultCode;
    }
    public final void setResultData(String data) {
        checkSynchronousHint();
        mResultData = data;
    }
    public final String getResultData() {
        return mResultData;
    }
    public final void setResultExtras(Bundle extras) {
        checkSynchronousHint();
        mResultExtras = extras;
    }
    public final Bundle getResultExtras(boolean makeMap) {
        Bundle e = mResultExtras;
        if (!makeMap) return e;
        if (e == null) mResultExtras = e = new Bundle();
        return e;
    }
    public final void setResult(int code, String data, Bundle extras) {
        checkSynchronousHint();
        mResultCode = code;
        mResultData = data;
        mResultExtras = extras;
    }
    public final boolean getAbortBroadcast() {
        return mAbortBroadcast;
    }
    public final void abortBroadcast() {
        checkSynchronousHint();
        mAbortBroadcast = true;
    }
    public final void clearAbortBroadcast() {
        mAbortBroadcast = false;
    }
    public final boolean isOrderedBroadcast() {
        return mOrderedHint;
    }
    public final boolean isInitialStickyBroadcast() {
        return mInitialStickyHint;
    }
    public final void setOrderedHint(boolean isOrdered) {
        mOrderedHint = isOrdered;
    }
    public final void setInitialStickyHint(boolean isInitialSticky) {
        mInitialStickyHint = isInitialSticky;
    }
    public final void setDebugUnregister(boolean debug) {
        mDebugUnregister = debug;
    }
    public final boolean getDebugUnregister() {
        return mDebugUnregister;
    }
    void checkSynchronousHint() {
        if (mOrderedHint || mInitialStickyHint) {
            return;
        }
        RuntimeException e = new RuntimeException(
                "BroadcastReceiver trying to return result during a non-ordered broadcast");
        e.fillInStackTrace();
        Log.e("BroadcastReceiver", e.getMessage(), e);
    }
    private int mResultCode;
    private String mResultData;
    private Bundle mResultExtras;
    private boolean mAbortBroadcast;
    private boolean mDebugUnregister;
    private boolean mOrderedHint;
    private boolean mInitialStickyHint;
}
