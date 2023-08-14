public abstract class AccessibilityService extends Service {
    public static final String SERVICE_INTERFACE =
        "android.accessibilityservice.AccessibilityService";
    private static final String LOG_TAG = "AccessibilityService";
    private AccessibilityServiceInfo mInfo;
    IAccessibilityServiceConnection mConnection;
    public abstract void onAccessibilityEvent(AccessibilityEvent event);
    public abstract void onInterrupt();
    protected void onServiceConnected() {
    }
    public final void setServiceInfo(AccessibilityServiceInfo info) {
        mInfo = info;
        sendServiceInfo();
    }
    private void sendServiceInfo() {
        if (mInfo != null && mConnection != null) {
            try {
                mConnection.setServiceInfo(mInfo);
            } catch (RemoteException re) {
                Log.w(LOG_TAG, "Error while setting AccessibilityServiceInfo", re);
            }
        }
    }
    @Override
    public final IBinder onBind(Intent intent) {
        return new IEventListenerWrapper(this);
    }
    class IEventListenerWrapper extends IEventListener.Stub
            implements HandlerCaller.Callback {
        private static final int DO_SET_SET_CONNECTION = 10;
        private static final int DO_ON_INTERRUPT = 20;
        private static final int DO_ON_ACCESSIBILITY_EVENT = 30;
        private final HandlerCaller mCaller;
        private final AccessibilityService mTarget;
        public IEventListenerWrapper(AccessibilityService context) {
            mTarget = context;
            mCaller = new HandlerCaller(context, this);
        }
        public void setConnection(IAccessibilityServiceConnection connection) {
            Message message = mCaller.obtainMessageO(DO_SET_SET_CONNECTION, connection);
            mCaller.sendMessage(message);
        }
        public void onInterrupt() {
            Message message = mCaller.obtainMessage(DO_ON_INTERRUPT);
            mCaller.sendMessage(message);
        }
        public void onAccessibilityEvent(AccessibilityEvent event) {
            Message message = mCaller.obtainMessageO(DO_ON_ACCESSIBILITY_EVENT, event);
            mCaller.sendMessage(message);
        }
        public void executeMessage(Message message) {
            switch (message.what) {
                case DO_ON_ACCESSIBILITY_EVENT :
                    AccessibilityEvent event = (AccessibilityEvent) message.obj;
                    if (event != null) {
                        mTarget.onAccessibilityEvent(event);
                        event.recycle();
                    }
                    return;
                case DO_ON_INTERRUPT :
                    mTarget.onInterrupt();
                    return;
                case DO_SET_SET_CONNECTION :
                    mConnection = ((IAccessibilityServiceConnection) message.obj);
                    mTarget.onServiceConnected();
                    return;
                default :
                    Log.w(LOG_TAG, "Unknown message type " + message.what);
            }
        }
    }
}
