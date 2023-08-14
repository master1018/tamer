public class NetworkConnectivityListener {
    private static final String TAG = "NetworkConnectivityListener";
    private static final boolean DBG = false;
    private Context mContext;
    private HashMap<Handler, Integer> mHandlers = new HashMap<Handler, Integer>();
    private State mState;
    private boolean mListening;
    private String mReason;
    private boolean mIsFailover;
    private NetworkInfo mNetworkInfo;
    private NetworkInfo mOtherNetworkInfo;
    private ConnectivityBroadcastReceiver mReceiver;
    private class ConnectivityBroadcastReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (!action.equals(ConnectivityManager.CONNECTIVITY_ACTION) ||
                mListening == false) {
                Log.w(TAG, "onReceived() called with " + mState.toString() + " and " + intent);
                return;
            }
            boolean noConnectivity =
                intent.getBooleanExtra(ConnectivityManager.EXTRA_NO_CONNECTIVITY, false);
            if (noConnectivity) {
                mState = State.NOT_CONNECTED;
            } else {
                mState = State.CONNECTED;
            }
            mNetworkInfo = (NetworkInfo)
                intent.getParcelableExtra(ConnectivityManager.EXTRA_NETWORK_INFO);
            mOtherNetworkInfo = (NetworkInfo)
                intent.getParcelableExtra(ConnectivityManager.EXTRA_OTHER_NETWORK_INFO);
            mReason = intent.getStringExtra(ConnectivityManager.EXTRA_REASON);
            mIsFailover =
                intent.getBooleanExtra(ConnectivityManager.EXTRA_IS_FAILOVER, false);
            if (DBG) {
                Log.d(TAG, "onReceive(): mNetworkInfo=" + mNetworkInfo +  " mOtherNetworkInfo = "
                        + (mOtherNetworkInfo == null ? "[none]" : mOtherNetworkInfo +
                        " noConn=" + noConnectivity) + " mState=" + mState.toString());
            }
            Iterator<Handler> it = mHandlers.keySet().iterator();
            while (it.hasNext()) {
                Handler target = it.next();
                Message message = Message.obtain(target, mHandlers.get(target));
                target.sendMessage(message);
            }
        }
    };
    public enum State {
        UNKNOWN,
        CONNECTED,
        NOT_CONNECTED
    }
    public NetworkConnectivityListener() {
        mState = State.UNKNOWN;
        mReceiver = new ConnectivityBroadcastReceiver();
    }
    public synchronized void startListening(Context context) {
        if (!mListening) {
            mContext = context;
            IntentFilter filter = new IntentFilter();
            filter.addAction(ConnectivityManager.CONNECTIVITY_ACTION);
            context.registerReceiver(mReceiver, filter);
            mListening = true;
        }
    }
    public synchronized void stopListening() {
        if (mListening) {
            mContext.unregisterReceiver(mReceiver);
            mContext = null;
            mNetworkInfo = null;
            mOtherNetworkInfo = null;
            mIsFailover = false;
            mReason = null;
            mListening = false;
        }
    }
    public void registerHandler(Handler target, int what) {
        mHandlers.put(target, what);
    }
    public void unregisterHandler(Handler target) {
        mHandlers.remove(target);
    }
    public State getState() {
        return mState;
    }
    public NetworkInfo getNetworkInfo() {
        return mNetworkInfo;
    }
    public NetworkInfo getOtherNetworkInfo() {
        return mOtherNetworkInfo;
    }
    public boolean isFailover() {
        return mIsFailover;
    }
    public String getReason() {
        return mReason;
    }
}
