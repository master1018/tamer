public final class AccessibilityManager {
    private static final String LOG_TAG = "AccessibilityManager";
    static final Object sInstanceSync = new Object();
    private static AccessibilityManager sInstance;
    private static final int DO_SET_ENABLED = 10;
    final IAccessibilityManager mService;
    final Handler mHandler;
    boolean mIsEnabled;
    final IAccessibilityManagerClient.Stub mClient = new IAccessibilityManagerClient.Stub() {
        public void setEnabled(boolean enabled) {
            mHandler.obtainMessage(DO_SET_ENABLED, enabled ? 1 : 0, 0).sendToTarget();
        }
    };
    class MyHandler extends Handler {
        MyHandler(Looper mainLooper) {
            super(mainLooper);
        }
        @Override
        public void handleMessage(Message message) {
            switch (message.what) {
                case DO_SET_ENABLED :
                    synchronized (mHandler) {
                        mIsEnabled = (message.arg1 == 1);
                    }
                    return;
                default :
                    Log.w(LOG_TAG, "Unknown message type: " + message.what);
            }
        }
    }
    public static AccessibilityManager getInstance(Context context) {
        synchronized (sInstanceSync) {
            if (sInstance == null) {
                sInstance = new AccessibilityManager(context);
            }
        }
        return sInstance;
    }
    private AccessibilityManager(Context context) {
        mHandler = new MyHandler(context.getMainLooper());
        IBinder iBinder = ServiceManager.getService(Context.ACCESSIBILITY_SERVICE);
        mService = IAccessibilityManager.Stub.asInterface(iBinder);
        try {
            mService.addClient(mClient);
        } catch (RemoteException re) {
            Log.e(LOG_TAG, "AccessibilityManagerService is dead", re);
        }
    }
    public boolean isEnabled() {
        synchronized (mHandler) {
            return mIsEnabled;
        }
    }
    public void sendAccessibilityEvent(AccessibilityEvent event) {
        if (!mIsEnabled) {
            throw new IllegalStateException("Accessibility off. Did you forget to check that?");
        }
        boolean doRecycle = false;
        try {
            event.setEventTime(SystemClock.uptimeMillis());
            long identityToken = Binder.clearCallingIdentity();
            doRecycle = mService.sendAccessibilityEvent(event);
            Binder.restoreCallingIdentity(identityToken);
            if (LOGV) {
                Log.i(LOG_TAG, event + " sent");
            }
        } catch (RemoteException re) {
            Log.e(LOG_TAG, "Error during sending " + event + " ", re);
        } finally {
            if (doRecycle) {
                event.recycle();
            }
        }
    }
    public void interrupt() {
        if (!mIsEnabled) {
            throw new IllegalStateException("Accessibility off. Did you forget to check that?");
        }
        try {
            mService.interrupt();
            if (LOGV) {
                Log.i(LOG_TAG, "Requested interrupt from all services");
            }
        } catch (RemoteException re) {
            Log.e(LOG_TAG, "Error while requesting interrupt from all services. ", re);
        }
    }
    public List<ServiceInfo> getAccessibilityServiceList() {
        List<ServiceInfo> services = null;
        try {
            services = mService.getAccessibilityServiceList();
            if (LOGV) {
                Log.i(LOG_TAG, "Installed AccessibilityServices " + services);
            }
        } catch (RemoteException re) {
            Log.e(LOG_TAG, "Error while obtaining the installed AccessibilityServices. ", re);
        }
        return Collections.unmodifiableList(services);
    }
}
