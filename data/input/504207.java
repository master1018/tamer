abstract class VpnService<E extends VpnProfile> implements Serializable {
    static final long serialVersionUID = 1L;
    private static final boolean DBG = true;
    private static final int NOTIFICATION_ID = 1;
    private static final String DNS1 = "net.dns1";
    private static final String DNS2 = "net.dns2";
    private static final String VPN_DNS1 = "vpn.dns1";
    private static final String VPN_DNS2 = "vpn.dns2";
    private static final String VPN_STATUS = "vpn.status";
    private static final String VPN_IS_UP = "ok";
    private static final String VPN_IS_DOWN = "down";
    private static final String REMOTE_IP = "net.ipremote";
    private static final String DNS_DOMAIN_SUFFICES = "net.dns.search";
    private final String TAG = VpnService.class.getSimpleName();
    E mProfile;
    transient VpnServiceBinder mContext;
    private VpnState mState = VpnState.IDLE;
    private Throwable mError;
    private String mOriginalDns1;
    private String mOriginalDns2;
    private String mOriginalDomainSuffices;
    private String mLocalIp;
    private String mLocalIf;
    private long mStartTime; 
    private VpnDaemons mDaemons = new VpnDaemons();
    private transient NotificationHelper mNotification;
    protected abstract void connect(String serverIp, String username,
            String password) throws IOException;
    protected VpnDaemons getDaemons() {
        return mDaemons;
    }
    protected E getProfile() {
        return mProfile;
    }
    protected String getIp(String hostName) throws IOException {
        return InetAddress.getByName(hostName).getHostAddress();
    }
    void setContext(VpnServiceBinder context, E profile) {
        mProfile = profile;
        recover(context);
    }
    void recover(VpnServiceBinder context) {
        mContext = context;
        mNotification = new NotificationHelper();
        if (VpnState.CONNECTED.equals(mState)) {
            Log.i("VpnService", "     recovered: " + mProfile.getName());
            startConnectivityMonitor();
        }
    }
    VpnState getState() {
        return mState;
    }
    synchronized boolean onConnect(String username, String password) {
        try {
            setState(VpnState.CONNECTING);
            mDaemons.stopAll();
            String serverIp = getIp(getProfile().getServerName());
            saveLocalIpAndInterface(serverIp);
            onBeforeConnect();
            connect(serverIp, username, password);
            waitUntilConnectedOrTimedout();
            return true;
        } catch (Throwable e) {
            onError(e);
            return false;
        }
    }
    synchronized void onDisconnect() {
        try {
            Log.i(TAG, "disconnecting VPN...");
            setState(VpnState.DISCONNECTING);
            mNotification.showDisconnect();
            mDaemons.stopAll();
        } catch (Throwable e) {
            Log.e(TAG, "onDisconnect()", e);
        } finally {
            onFinalCleanUp();
        }
    }
    private void onError(Throwable error) {
        if (mError != null) {
            Log.w(TAG, "   multiple errors occur, record the last one: "
                    + error);
        }
        Log.e(TAG, "onError()", error);
        mError = error;
        onDisconnect();
    }
    private void onError(int errorCode) {
        onError(new VpnConnectingError(errorCode));
    }
    private void onBeforeConnect() throws IOException {
        mNotification.disableNotification();
        SystemProperties.set(VPN_DNS1, "");
        SystemProperties.set(VPN_DNS2, "");
        SystemProperties.set(VPN_STATUS, VPN_IS_DOWN);
        if (DBG) {
            Log.d(TAG, "       VPN UP: " + SystemProperties.get(VPN_STATUS));
        }
    }
    private void waitUntilConnectedOrTimedout() throws IOException {
        sleep(2000); 
        for (int i = 0; i < 80; i++) {
            if (mState != VpnState.CONNECTING) {
                break;
            } else if (VPN_IS_UP.equals(
                    SystemProperties.get(VPN_STATUS))) {
                onConnected();
                return;
            } else {
                int err = mDaemons.getSocketError();
                if (err != 0) {
                    onError(err);
                    return;
                }
            }
            sleep(500); 
        }
        if (mState == VpnState.CONNECTING) {
            onError(new IOException("Connecting timed out"));
        }
    }
    private synchronized void onConnected() throws IOException {
        if (DBG) Log.d(TAG, "onConnected()");
        mDaemons.closeSockets();
        saveOriginalDns();
        saveAndSetDomainSuffices();
        mStartTime = System.currentTimeMillis();
        setState(VpnState.CONNECTED);
        saveSelf();
        setVpnDns();
        startConnectivityMonitor();
    }
    private void saveSelf() throws IOException {
        mContext.saveStates();
    }
    private synchronized void onFinalCleanUp() {
        if (DBG) Log.d(TAG, "onFinalCleanUp()");
        if (mState == VpnState.IDLE) return;
        if (!anyError()) mNotification.disableNotification();
        restoreOriginalDns();
        restoreOriginalDomainSuffices();
        setState(VpnState.IDLE);
        SystemProperties.set(VPN_STATUS, VPN_IS_DOWN);
        mContext.removeStates();
        mContext.stopSelf();
    }
    private boolean anyError() {
        return (mError != null);
    }
    private void restoreOriginalDns() {
        String vpnDns1 = SystemProperties.get(VPN_DNS1);
        if (vpnDns1.equals(SystemProperties.get(DNS1))) {
            Log.i(TAG, String.format("restore original dns prop: %s --> %s",
                    SystemProperties.get(DNS1), mOriginalDns1));
            Log.i(TAG, String.format("restore original dns prop: %s --> %s",
                    SystemProperties.get(DNS2), mOriginalDns2));
            SystemProperties.set(DNS1, mOriginalDns1);
            SystemProperties.set(DNS2, mOriginalDns2);
        }
    }
    private void saveOriginalDns() {
        mOriginalDns1 = SystemProperties.get(DNS1);
        mOriginalDns2 = SystemProperties.get(DNS2);
        Log.i(TAG, String.format("save original dns prop: %s, %s",
                mOriginalDns1, mOriginalDns2));
    }
    private void setVpnDns() {
        String vpnDns1 = SystemProperties.get(VPN_DNS1);
        String vpnDns2 = SystemProperties.get(VPN_DNS2);
        SystemProperties.set(DNS1, vpnDns1);
        SystemProperties.set(DNS2, vpnDns2);
        Log.i(TAG, String.format("set vpn dns prop: %s, %s",
                vpnDns1, vpnDns2));
    }
    private void saveAndSetDomainSuffices() {
        mOriginalDomainSuffices = SystemProperties.get(DNS_DOMAIN_SUFFICES);
        Log.i(TAG, "save original suffices: " + mOriginalDomainSuffices);
        String list = mProfile.getDomainSuffices();
        if (!TextUtils.isEmpty(list)) {
            SystemProperties.set(DNS_DOMAIN_SUFFICES, list);
        }
    }
    private void restoreOriginalDomainSuffices() {
        Log.i(TAG, "restore original suffices --> " + mOriginalDomainSuffices);
        SystemProperties.set(DNS_DOMAIN_SUFFICES, mOriginalDomainSuffices);
    }
    private void setState(VpnState newState) {
        mState = newState;
        broadcastConnectivity(newState);
    }
    private void broadcastConnectivity(VpnState s) {
        VpnManager m = new VpnManager(mContext);
        Throwable err = mError;
        if ((s == VpnState.IDLE) && (err != null)) {
            if (err instanceof UnknownHostException) {
                m.broadcastConnectivity(mProfile.getName(), s,
                        VpnManager.VPN_ERROR_UNKNOWN_SERVER);
            } else if (err instanceof VpnConnectingError) {
                m.broadcastConnectivity(mProfile.getName(), s,
                        ((VpnConnectingError) err).getErrorCode());
            } else if (VPN_IS_UP.equals(SystemProperties.get(VPN_STATUS))) {
                m.broadcastConnectivity(mProfile.getName(), s,
                        VpnManager.VPN_ERROR_CONNECTION_LOST);
            } else {
                m.broadcastConnectivity(mProfile.getName(), s,
                        VpnManager.VPN_ERROR_CONNECTION_FAILED);
            }
        } else {
            m.broadcastConnectivity(mProfile.getName(), s);
        }
    }
    private void startConnectivityMonitor() {
        new Thread(new Runnable() {
            public void run() {
                Log.i(TAG, "VPN connectivity monitor running");
                try {
                    for (int i = 10; ; i--) {
                        long now = System.currentTimeMillis();
                        boolean heavyCheck = i == 0;
                        synchronized (VpnService.this) {
                            if (mState != VpnState.CONNECTED) break;
                            mNotification.update(now);
                            if (heavyCheck) {
                                i = 10;
                                if (checkConnectivity()) checkDns();
                            }
                            long t = 1000L - System.currentTimeMillis() + now;
                            if (t > 100L) VpnService.this.wait(t);
                        }
                    }
                } catch (InterruptedException e) {
                    onError(e);
                }
                Log.i(TAG, "VPN connectivity monitor stopped");
            }
        }).start();
    }
    private void saveLocalIpAndInterface(String serverIp) throws IOException {
        DatagramSocket s = new DatagramSocket();
        int port = 80; 
        s.connect(InetAddress.getByName(serverIp), port);
        InetAddress localIp = s.getLocalAddress();
        mLocalIp = localIp.getHostAddress();
        NetworkInterface localIf = NetworkInterface.getByInetAddress(localIp);
        mLocalIf = (localIf == null) ? null : localIf.getName();
        if (TextUtils.isEmpty(mLocalIf)) {
            throw new IOException("Local interface is empty!");
        }
        if (DBG) {
            Log.d(TAG, "  Local IP: " + mLocalIp + ", if: " + mLocalIf);
        }
    }
    private boolean checkConnectivity() {
        if (mDaemons.anyDaemonStopped() || isLocalIpChanged()) {
            onError(new IOException("Connectivity lost"));
            return false;
        } else {
            return true;
        }
    }
    private void checkDns() {
        String dns1 = SystemProperties.get(DNS1);
        String vpnDns1 = SystemProperties.get(VPN_DNS1);
        if (!dns1.equals(vpnDns1) && dns1.equals(mOriginalDns1)) {
            setVpnDns();
        }
    }
    private boolean isLocalIpChanged() {
        try {
            InetAddress localIp = InetAddress.getByName(mLocalIp);
            NetworkInterface localIf =
                    NetworkInterface.getByInetAddress(localIp);
            if (localIf == null || !mLocalIf.equals(localIf.getName())) {
                Log.w(TAG, "       local If changed from " + mLocalIf
                        + " to " + localIf);
                return true;
            } else {
                return false;
            }
        } catch (IOException e) {
            Log.w(TAG, "isLocalIpChanged()", e);
            return true;
        }
    }
    protected void sleep(int ms) {
        try {
            Thread.currentThread().sleep(ms);
        } catch (InterruptedException e) {
        }
    }
    private class DaemonHelper implements Serializable {
    }
    private class NotificationHelper {
        void update(long now) {
            String title = getNotificationTitle(true);
            Notification n = new Notification(R.drawable.vpn_connected, title,
                    mStartTime);
            n.setLatestEventInfo(mContext, title,
                    getConnectedNotificationMessage(now),
                    prepareNotificationIntent());
            n.flags |= Notification.FLAG_NO_CLEAR;
            n.flags |= Notification.FLAG_ONGOING_EVENT;
            enableNotification(n);
        }
        void showDisconnect() {
            String title = getNotificationTitle(false);
            Notification n = new Notification(R.drawable.vpn_disconnected,
                    title, System.currentTimeMillis());
            n.setLatestEventInfo(mContext, title,
                    getDisconnectedNotificationMessage(),
                    prepareNotificationIntent());
            n.flags |= Notification.FLAG_AUTO_CANCEL;
            disableNotification();
            enableNotification(n);
        }
        void disableNotification() {
            ((NotificationManager) mContext.getSystemService(
                    Context.NOTIFICATION_SERVICE)).cancel(NOTIFICATION_ID);
        }
        private void enableNotification(Notification n) {
            ((NotificationManager) mContext.getSystemService(
                    Context.NOTIFICATION_SERVICE)).notify(NOTIFICATION_ID, n);
        }
        private PendingIntent prepareNotificationIntent() {
            return PendingIntent.getActivity(mContext, 0,
                    new VpnManager(mContext).createSettingsActivityIntent(), 0);
        }
        private String getNotificationTitle(boolean connected) {
            String formatString = connected
                    ? mContext.getString(
                            R.string.vpn_notification_title_connected)
                    : mContext.getString(
                            R.string.vpn_notification_title_disconnected);
            return String.format(formatString, mProfile.getName());
        }
        private String getFormattedTime(int duration) {
            int hours = duration / 3600;
            StringBuilder sb = new StringBuilder();
            if (hours > 0) sb.append(hours).append(':');
            sb.append(String.format("%02d:%02d", (duration % 3600 / 60),
                    (duration % 60)));
            return sb.toString();
        }
        private String getConnectedNotificationMessage(long now) {
            return getFormattedTime((int) (now - mStartTime) / 1000);
        }
        private String getDisconnectedNotificationMessage() {
            return mContext.getString(
                    R.string.vpn_notification_hint_disconnected);
        }
    }
}
