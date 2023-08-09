public class NetworkQueryService extends Service {
    private static final String LOG_TAG = "NetworkQuery";
    private static final boolean DBG = false;
    private static final int EVENT_NETWORK_SCAN_COMPLETED = 100; 
    private static final int QUERY_READY = -1;
    private static final int QUERY_IS_RUNNING = -2;
    public static final int QUERY_OK = 0;
    public static final int QUERY_EXCEPTION = 1;
    private int mState;
    private Phone mPhone;
    public class LocalBinder extends Binder {
        INetworkQueryService getService() {
            return mBinder;
        }
    }
    private final IBinder mLocalBinder = new LocalBinder();
    Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case EVENT_NETWORK_SCAN_COMPLETED:
                    if (DBG) log("scan completed, broadcasting results");
                    broadcastQueryResults((AsyncResult) msg.obj);
                    break;
            }
        }
    };
    final RemoteCallbackList<INetworkQueryServiceCallback> mCallbacks =
        new RemoteCallbackList<INetworkQueryServiceCallback> ();
    private final INetworkQueryService.Stub mBinder = new INetworkQueryService.Stub() {
        public void startNetworkQuery(INetworkQueryServiceCallback cb) {
            if (cb != null) {
                synchronized (mCallbacks) {
                    mCallbacks.register(cb);
                    if (DBG) log("registering callback " + cb.getClass().toString());
                    switch (mState) {
                        case QUERY_READY:
                            mPhone.getAvailableNetworks(
                                    mHandler.obtainMessage(EVENT_NETWORK_SCAN_COMPLETED));
                            mState = QUERY_IS_RUNNING;
                            if (DBG) log("starting new query");
                            break;
                        case QUERY_IS_RUNNING:
                            if (DBG) log("query already in progress");
                            break;
                        default:
                    }
                }
            }
        }
        public void stopNetworkQuery(INetworkQueryServiceCallback cb) {
            if (cb != null) {
                synchronized (mCallbacks) {
                    if (DBG) log("unregistering callback " + cb.getClass().toString());
                    mCallbacks.unregister(cb);
                }
            }            
        }
    };
    @Override
    public void onCreate() {
        mState = QUERY_READY;
        mPhone = PhoneFactory.getDefaultPhone();
    }
    @Override
    public void onStart(Intent intent, int startId) {
    }
    @Override
    public IBinder onBind(Intent intent) {
        if (DBG) log("binding service implementation");
        return mLocalBinder;
    }
    private void broadcastQueryResults (AsyncResult ar) {
        synchronized (mCallbacks) {
            mState = QUERY_READY;
            if (ar == null) {
                if (DBG) log("AsyncResult is null.");
                return;
            }
            int exception = (ar.exception == null) ? QUERY_OK : QUERY_EXCEPTION;
            if (DBG) log("AsyncResult has exception " + exception);
            for (int i = (mCallbacks.beginBroadcast() - 1); i >= 0; i--) {
                INetworkQueryServiceCallback cb = mCallbacks.getBroadcastItem(i); 
                if (DBG) log("broadcasting results to " + cb.getClass().toString());
                try {
                    cb.onQueryComplete((ArrayList<NetworkInfo>) ar.result, exception);
                } catch (RemoteException e) {
                }
            }
            mCallbacks.finishBroadcast();
        }
    }
    private static void log(String msg) {
        Log.d(LOG_TAG, msg);
    }    
}
