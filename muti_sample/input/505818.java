public class MonkeyNetworkMonitor extends IIntentReceiver.Stub {
    private static final boolean LDEBUG = false;
    private final IntentFilter filter = new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION);
    private long mCollectionStartTime; 
    private long mEventTime; 
    private int mLastNetworkType = -1; 
    private long mWifiElapsedTime = 0;  
    private long mMobileElapsedTime = 0; 
    private long mElapsedTime = 0; 
    public void performReceive(Intent intent, int resultCode, String data, Bundle extras,
            boolean ordered, boolean sticky) throws RemoteException {
        NetworkInfo ni = (NetworkInfo) intent.getParcelableExtra(
                ConnectivityManager.EXTRA_NETWORK_INFO);
        if (LDEBUG) System.out.println("Network state changed: " 
                + "type=" + ni.getType() + ", state="  + ni.getState());
        updateNetworkStats();
        if (NetworkInfo.State.CONNECTED == ni.getState()) {
            if (LDEBUG) System.out.println("Network connected");
            mLastNetworkType = ni.getType();
        } else if (NetworkInfo.State.DISCONNECTED == ni.getState()) {
            if (LDEBUG) System.out.println("Network not connected");
            mLastNetworkType = -1; 
        }
        mEventTime = SystemClock.elapsedRealtime();
    }
    private void updateNetworkStats() {
        long timeNow = SystemClock.elapsedRealtime();
        long delta = timeNow - mEventTime;
        switch (mLastNetworkType) {
            case ConnectivityManager.TYPE_MOBILE:
                if (LDEBUG) System.out.println("Adding to mobile: " + delta);
                mMobileElapsedTime += delta;
                break;
            case ConnectivityManager.TYPE_WIFI:
                if (LDEBUG) System.out.println("Adding to wifi: " + delta);
                mWifiElapsedTime += delta;
                break;
            default:
                if (LDEBUG) System.out.println("Unaccounted for: " + delta);
                break;
        }
        mElapsedTime = timeNow - mCollectionStartTime;
    }
    public void start() {
        mWifiElapsedTime = 0;
        mMobileElapsedTime = 0;
        mElapsedTime = 0;
        mEventTime = mCollectionStartTime = SystemClock.elapsedRealtime();
    }
    public void register(IActivityManager am) throws RemoteException {
        if (LDEBUG) System.out.println("registering Receiver");
        am.registerReceiver(null, this, filter, null); 
    }
    public void unregister(IActivityManager am) throws RemoteException {
        if (LDEBUG) System.out.println("unregistering Receiver");
        am.unregisterReceiver(this);
    }
    public void stop() {
        updateNetworkStats();
    }
    public void dump() {
        System.out.println("## Network stats: elapsed time=" + mElapsedTime + "ms (" 
                + mMobileElapsedTime + "ms mobile, "
                + mWifiElapsedTime + "ms wifi, "
                + (mElapsedTime - mMobileElapsedTime - mWifiElapsedTime) + "ms not connected)");
    }
 }