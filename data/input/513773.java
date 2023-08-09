public class WifiWatchdogService {
    private static final String TAG = "WifiWatchdogService";
    private static final boolean V = false || Config.LOGV;
    private static final boolean D = true || Config.LOGD;
    private Context mContext;
    private ContentResolver mContentResolver;
    private WifiStateTracker mWifiStateTracker;
    private WifiManager mWifiManager;
    private WifiWatchdogThread mThread;
    private WifiWatchdogHandler mHandler;
    private ContentObserver mContentObserver;
    private WatchdogState mState = WatchdogState.IDLE;
    private String mSsid;
    private int mNumApsChecked;
    private boolean mShouldCancel;
    WifiWatchdogService(Context context, WifiStateTracker wifiStateTracker) {
        mContext = context;
        mContentResolver = context.getContentResolver();
        mWifiStateTracker = wifiStateTracker;
        mWifiManager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
        createThread();
        registerForSettingsChanges();
        if (isWatchdogEnabled()) {
            registerForWifiBroadcasts();
        }
        if (V) {
            myLogV("WifiWatchdogService: Created");
        }
    }
    private void registerForSettingsChanges() {
        ContentResolver contentResolver = mContext.getContentResolver();
        contentResolver.registerContentObserver(
                Settings.Secure.getUriFor(Settings.Secure.WIFI_WATCHDOG_ON), false,
                mContentObserver = new ContentObserver(mHandler) {
            @Override
            public void onChange(boolean selfChange) {
                if (isWatchdogEnabled()) {
                    registerForWifiBroadcasts();
                } else {
                    unregisterForWifiBroadcasts();
                    if (mHandler != null) {
                        mHandler.disableWatchdog();
                    }
                }
            }
        });
    }
    private boolean isWatchdogEnabled() {
        return Settings.Secure.getInt(mContentResolver, Settings.Secure.WIFI_WATCHDOG_ON, 1) == 1;
    }
    private int getApCount() {
        return Settings.Secure.getInt(mContentResolver,
            Settings.Secure.WIFI_WATCHDOG_AP_COUNT, 2);
    }
    private int getInitialIgnoredPingCount() {
        return Settings.Secure.getInt(mContentResolver,
            Settings.Secure.WIFI_WATCHDOG_INITIAL_IGNORED_PING_COUNT , 2);
    }
    private int getPingCount() {
        return Settings.Secure.getInt(mContentResolver,
            Settings.Secure.WIFI_WATCHDOG_PING_COUNT, 4);
    }
    private int getPingTimeoutMs() {
        return Settings.Secure.getInt(mContentResolver,
            Settings.Secure.WIFI_WATCHDOG_PING_TIMEOUT_MS, 500);
    }
    private int getPingDelayMs() {
        return Settings.Secure.getInt(mContentResolver,
            Settings.Secure.WIFI_WATCHDOG_PING_DELAY_MS, 250);
    }
    private int getAcceptablePacketLossPercentage() {
        return Settings.Secure.getInt(mContentResolver,
            Settings.Secure.WIFI_WATCHDOG_ACCEPTABLE_PACKET_LOSS_PERCENTAGE, 25);
    }
    private int getMaxApChecks() {
        return Settings.Secure.getInt(mContentResolver,
            Settings.Secure.WIFI_WATCHDOG_MAX_AP_CHECKS, 7);
    }
    private boolean isBackgroundCheckEnabled() {
        return Settings.Secure.getInt(mContentResolver,
            Settings.Secure.WIFI_WATCHDOG_BACKGROUND_CHECK_ENABLED, 1) == 1;
    }
    private int getBackgroundCheckDelayMs() {
        return Settings.Secure.getInt(mContentResolver,
            Settings.Secure.WIFI_WATCHDOG_BACKGROUND_CHECK_DELAY_MS, 60000);
    }
    private int getBackgroundCheckTimeoutMs() {
        return Settings.Secure.getInt(mContentResolver,
            Settings.Secure.WIFI_WATCHDOG_BACKGROUND_CHECK_TIMEOUT_MS, 1000);
    }
    private String getWatchList() {
        return Settings.Secure.getString(mContentResolver,
                Settings.Secure.WIFI_WATCHDOG_WATCH_LIST);
    }
    private void registerForWifiBroadcasts() {
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(WifiManager.NETWORK_STATE_CHANGED_ACTION);
        intentFilter.addAction(WifiManager.WIFI_STATE_CHANGED_ACTION);
        mContext.registerReceiver(mReceiver, intentFilter);
    }
    private void unregisterForWifiBroadcasts() {
        mContext.unregisterReceiver(mReceiver);
    }
    private void createThread() {
        mThread = new WifiWatchdogThread();
        mThread.start();
        waitForHandlerCreation();
    }
    private void quit() {
        unregisterForWifiBroadcasts();
        mContext.getContentResolver().unregisterContentObserver(mContentObserver);
        mHandler.removeAllActions();
        mHandler.getLooper().quit();
    }
    private void waitForHandlerCreation() {
        synchronized(this) {
            while (mHandler == null) {
                try {
                    wait();
                } catch (InterruptedException e) {
                    Slog.e(TAG, "Interrupted while waiting on handler.");
                }
            }
        }
    }
    private static void myLogV(String message) {
        Slog.v(TAG, "(" + Thread.currentThread().getName() + ") " + message);
    }
    private static void myLogD(String message) {
        Slog.d(TAG, "(" + Thread.currentThread().getName() + ") " + message);
    }
    private int getDns() {
        DhcpInfo addressInfo = mWifiManager.getDhcpInfo();
        if (addressInfo != null) {
            return addressInfo.dns1;
        } else {
            return -1;
        }
    }
    private boolean checkDnsConnectivity() {
        int dns = getDns();
        if (dns == -1) {
            if (V) {
                myLogV("checkDnsConnectivity: Invalid DNS, returning false");
            }
            return false;
        }
        if (V) {
            myLogV("checkDnsConnectivity: Checking 0x" +
                    Integer.toHexString(Integer.reverseBytes(dns)) + " for connectivity");
        }
        int numInitialIgnoredPings = getInitialIgnoredPingCount();
        int numPings = getPingCount();
        int pingDelay = getPingDelayMs();
        int acceptableLoss = getAcceptablePacketLossPercentage();
        int ignoredPingCounter = 0;
        int pingCounter = 0;
        int successCounter = 0;
        if (numPings == 0) {
            return true;
        }
        for (; ignoredPingCounter < numInitialIgnoredPings; ignoredPingCounter++) {
            if (shouldCancel()) return false;
            boolean dnsAlive = DnsPinger.isDnsReachable(dns, getPingTimeoutMs());
            if (dnsAlive) {
                pingCounter++;
                successCounter++;
            }
            if (V) {
                Slog.v(TAG, (dnsAlive ? "  +" : "  Ignored: -"));
            }
            if (shouldCancel()) return false;
            try {
                Thread.sleep(pingDelay);
            } catch (InterruptedException e) {
                Slog.w(TAG, "Interrupted while pausing between pings", e);
            }
        }
        for (; pingCounter < numPings; pingCounter++) {
            if (shouldCancel()) return false;
            if (DnsPinger.isDnsReachable(dns, getPingTimeoutMs())) {
                successCounter++;
                if (V) {
                    Slog.v(TAG, "  +");
                }
            } else {
                if (V) {
                    Slog.v(TAG, "  -");
                }
            }
            if (shouldCancel()) return false;
            try {
                Thread.sleep(pingDelay);
            } catch (InterruptedException e) {
                Slog.w(TAG, "Interrupted while pausing between pings", e);
            }
        }
        int packetLossPercentage = 100 * (numPings - successCounter) / numPings;
        if (D) {
            Slog.d(TAG, packetLossPercentage
                    + "% packet loss (acceptable is " + acceptableLoss + "%)");
        }
        return !shouldCancel() && (packetLossPercentage <= acceptableLoss);
    }
    private boolean backgroundCheckDnsConnectivity() {
        int dns = getDns();
        if (false && V) {
            myLogV("backgroundCheckDnsConnectivity: Background checking " + dns +
                    " for connectivity");
        }
        if (dns == -1) {
            if (V) {
                myLogV("backgroundCheckDnsConnectivity: DNS is empty, returning false");
            }
            return false;
        }
        return DnsPinger.isDnsReachable(dns, getBackgroundCheckTimeoutMs());
    }
    private void cancelCurrentAction() {
        mShouldCancel = true;
    }
    private boolean shouldCancel() {
        if (V && mShouldCancel) {
            myLogV("shouldCancel: Cancelling");
        }
        return mShouldCancel;
    }
    private void onConnected(String ssid, String bssid) {
        if (V) {
            myLogV("onConnected: SSID: " + ssid + ", BSSID: " + bssid);
        }
        cancelCurrentAction();
        if ((mSsid == null) || !mSsid.equals(ssid)) {
            mHandler.dispatchNetworkChanged(ssid);
        }
        if (requiresWatchdog(ssid, bssid)) {
            if (D) {
                myLogD(ssid + " (" + bssid + ") requires the watchdog");
            }
            mHandler.checkAp(new AccessPoint(ssid, bssid));
        } else {
            if (D) {
                myLogD(ssid + " (" + bssid + ") does not require the watchdog");
            }
            mHandler.idle();
        }
    }
    private void onEnabled() {
        cancelCurrentAction();
        mHandler.reset();
    }
    private void onDisconnected() {
        if (V) {
            myLogV("onDisconnected");
        }
        cancelCurrentAction();
        mHandler.dispatchDisconnected();
        mHandler.idle();
    }
    private boolean requiresWatchdog(String ssid, String bssid) {
        if (V) {
            myLogV("requiresWatchdog: SSID: " + ssid + ", BSSID: " + bssid);
        }
        WifiInfo info = null;
        if (ssid == null) {
            info = mWifiManager.getConnectionInfo();
            ssid = info.getSSID();
            if (ssid == null) {
                if (V) {
                    Slog.v(TAG, "  Invalid SSID, returning false");
                }
                return false;
            }
        }
        if (TextUtils.isEmpty(bssid)) {
            if (info == null) {
                info = mWifiManager.getConnectionInfo();
            }
            bssid = info.getBSSID();
            if (TextUtils.isEmpty(bssid)) {
                if (V) {
                    Slog.v(TAG, "  Invalid BSSID, returning false");
                }
                return false;
            }
        }
        if (!isOnWatchList(ssid)) {
            if (V) {
                Slog.v(TAG, "  SSID not on watch list, returning false");
            }
            return false;
        }
        if (!hasRequiredNumberOfAps(ssid)) {
            return false;
        }
        return true;
    }
    private boolean isOnWatchList(String ssid) {
        String watchList;
        if (ssid == null || (watchList = getWatchList()) == null) {
            return false;
        }
        String[] list = watchList.split(" *, *");
        for (String name : list) {
            if (ssid.equals(name)) {
                return true;
            }
        }
        return false;
    }
    private boolean hasRequiredNumberOfAps(String ssid) {
        List<ScanResult> results = mWifiManager.getScanResults();
        if (results == null) {
            if (V) {
                myLogV("hasRequiredNumberOfAps: Got null scan results, returning false");
            }
            return false;
        }
        int numApsRequired = getApCount();
        int numApsFound = 0;
        int resultsSize = results.size();
        for (int i = 0; i < resultsSize; i++) {
            ScanResult result = results.get(i);
            if (result == null) continue;
            if (result.SSID == null) continue;
            if (result.SSID.equals(ssid)) {
                numApsFound++;
                if (numApsFound >= numApsRequired) {
                    if (V) {
                        myLogV("hasRequiredNumberOfAps: SSID: " + ssid + ", returning true");
                    }
                    return true;
                }
            }
        }
        if (V) {
            myLogV("hasRequiredNumberOfAps: SSID: " + ssid + ", returning false");
        }
        return false;
    }
    private void handleNetworkChanged(String ssid) {
        mSsid = ssid;
        setIdleState(true);
    }
    private void handleCheckAp(AccessPoint ap) {
        mShouldCancel = false;
        if (V) {
            myLogV("handleCheckAp: AccessPoint: " + ap);
        }
        if (mState == WatchdogState.SLEEP) {
            if (V) {
                Slog.v(TAG, "  Sleeping (in " + mSsid + "), so returning");
            }
            return;
        }
        mState = WatchdogState.CHECKING_AP;
        mNumApsChecked++;
        if (mNumApsChecked > getMaxApChecks()) {
            if (V) {
                Slog.v(TAG, "  Passed the max attempts (" + getMaxApChecks()
                        + "), going to sleep for " + mSsid);
            }
            mHandler.sleep(mSsid);
            return;
        }
        boolean isApAlive = checkDnsConnectivity();
        if (V) {
            Slog.v(TAG, "  Is it alive: " + isApAlive);
        }
        if (isApAlive) {
            handleApAlive(ap);
        } else {
            handleApUnresponsive(ap);
        }
    }
    private void handleApAlive(AccessPoint ap) {
        if (shouldCancel()) return;
        setIdleState(false);
        if (D) {
            myLogD("AP is alive: " + ap.toString());
        }
        mHandler.backgroundCheckAp(ap);
    }
    private void handleApUnresponsive(AccessPoint ap) {
        if (shouldCancel()) return;
        mState = WatchdogState.SWITCHING_AP;
        if (D) {
            myLogD("AP is dead: " + ap.toString());
        }
        blacklistAp(ap.bssid);
        mWifiStateTracker.reassociate();
    }
    private void blacklistAp(String bssid) {
        if (TextUtils.isEmpty(bssid)) {
            return;
        }
        if (shouldCancel()) return;
        if (!mWifiStateTracker.addToBlacklist(bssid)) {
        }
        if (D) {
            myLogD("Blacklisting " + bssid);
        }
    }
    private void handleBackgroundCheckAp(AccessPoint ap) {
        mShouldCancel = false;
        if (false && V) {
            myLogV("handleBackgroundCheckAp: AccessPoint: " + ap);
        }
        if (mState == WatchdogState.SLEEP) {
            if (V) {
                Slog.v(TAG, "  handleBackgroundCheckAp: Sleeping (in " + mSsid + "), so returning");
            }
            return;
        }
        WifiInfo info = mWifiManager.getConnectionInfo();
        if (info.getSSID() == null || !info.getSSID().equals(ap.ssid)) {
            if (V) {
                myLogV("handleBackgroundCheckAp: We are no longer connected to "
                        + ap + ", and instead are on " + info);
            }
            return;
        }
        if (info.getBSSID() == null || !info.getBSSID().equals(ap.bssid)) {
            if (V) {
                myLogV("handleBackgroundCheckAp: We are no longer connected to "
                        + ap + ", and instead are on " + info);
            }
            return;
        }
        boolean isApAlive = backgroundCheckDnsConnectivity();
        if (V && !isApAlive) {
            Slog.v(TAG, "  handleBackgroundCheckAp: Is it alive: " + isApAlive);
        }
        if (shouldCancel()) {
            return;
        }
        if (isApAlive) {
            mHandler.backgroundCheckAp(ap);
        } else {
            if (D) {
                myLogD("Background check failed for " + ap.toString());
            }
            mHandler.checkAp(ap);
        }
    }
    private void handleSleep(String ssid) {
        if (ssid != null && ssid.equals(mSsid)) {
            mState = WatchdogState.SLEEP;
            if (D) {
                myLogD("Going to sleep for " + ssid);
            }
            if (!mWifiStateTracker.clearBlacklist()) {
            }
            if (V) {
                myLogV("handleSleep: Set state to SLEEP and cleared blacklist");
            }
        }
    }
    private void handleDisconnected() {
        setIdleState(false);
    }
    private void handleIdle() {
        mShouldCancel = false;
        if (V) {
            myLogV("handleSwitchToIdle");
        }
        if (mState == WatchdogState.SLEEP) {
            Slog.v(TAG, "  Sleeping (in " + mSsid + "), so returning");
            return;
        }
        setIdleState(false);
        if (V) {
            Slog.v(TAG, "  Set state to IDLE");
        }
    }
    private void setIdleState(boolean forceIdleState) {
        if (forceIdleState || (mState != WatchdogState.SLEEP)) {
            mState = WatchdogState.IDLE;
        }
        mNumApsChecked = 0;
    }
    private void handleReset() {
        mWifiStateTracker.clearBlacklist();
        setIdleState(true);
    }
    private static enum WatchdogState {
        IDLE,
        SLEEP,
        CHECKING_AP,
        SWITCHING_AP
    }
    private class WifiWatchdogThread extends Thread {
        WifiWatchdogThread() {
            super("WifiWatchdogThread");
        }
        @Override
        public void run() {
            Looper.prepare();
            synchronized(WifiWatchdogService.this) {
                mHandler = new WifiWatchdogHandler();
                WifiWatchdogService.this.notify();
            }
            Looper.loop();
        }
    }
    private class WifiWatchdogHandler extends Handler {
        static final int ACTION_CHECK_AP = 1;
        static final int ACTION_IDLE = 2;
        static final int ACTION_BACKGROUND_CHECK_AP = 3;
        static final int MESSAGE_SLEEP = 101;
        static final int MESSAGE_DISABLE_WATCHDOG = 102;
        static final int MESSAGE_NETWORK_CHANGED = 103;
        static final int MESSAGE_DISCONNECTED = 104;
        static final int MESSAGE_RESET = 105;
        void checkAp(AccessPoint ap) {
            removeAllActions();
            sendMessage(obtainMessage(ACTION_CHECK_AP, ap));
        }
        void backgroundCheckAp(AccessPoint ap) {
            if (!isBackgroundCheckEnabled()) return;
            removeAllActions();
            sendMessageDelayed(obtainMessage(ACTION_BACKGROUND_CHECK_AP, ap),
                    getBackgroundCheckDelayMs());
        }
        void idle() {
            removeAllActions();
            sendMessage(obtainMessage(ACTION_IDLE));
        }
        void sleep(String ssid) {
            removeAllActions();
            sendMessage(obtainMessage(MESSAGE_SLEEP, ssid));
        }
        void disableWatchdog() {
            removeAllActions();
            sendMessage(obtainMessage(MESSAGE_DISABLE_WATCHDOG));
        }
        void dispatchNetworkChanged(String ssid) {
            removeAllActions();
            sendMessage(obtainMessage(MESSAGE_NETWORK_CHANGED, ssid));
        }
        void dispatchDisconnected() {
            removeAllActions();
            sendMessage(obtainMessage(MESSAGE_DISCONNECTED));
        }
        void reset() {
            removeAllActions();
            sendMessage(obtainMessage(MESSAGE_RESET));
        }
        private void removeAllActions() {
            removeMessages(ACTION_CHECK_AP);
            removeMessages(ACTION_IDLE);
            removeMessages(ACTION_BACKGROUND_CHECK_AP);
        }
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case MESSAGE_NETWORK_CHANGED:
                    handleNetworkChanged((String) msg.obj);
                    break;
                case ACTION_CHECK_AP:
                    handleCheckAp((AccessPoint) msg.obj);
                    break;
                case ACTION_BACKGROUND_CHECK_AP:
                    handleBackgroundCheckAp((AccessPoint) msg.obj);
                    break;
                case MESSAGE_SLEEP:
                    handleSleep((String) msg.obj);
                    break;
                case ACTION_IDLE:
                    handleIdle();
                    break;
                case MESSAGE_DISABLE_WATCHDOG:
                    handleIdle();
                    break;
                case MESSAGE_DISCONNECTED:
                    handleDisconnected();
                    break;
                case MESSAGE_RESET:
                    handleReset();
                    break;
            }
        }
    }
    private BroadcastReceiver mReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            final String action = intent.getAction();
            if (action.equals(WifiManager.NETWORK_STATE_CHANGED_ACTION)) {
                handleNetworkStateChanged(
                        (NetworkInfo) intent.getParcelableExtra(WifiManager.EXTRA_NETWORK_INFO));
            } else if (action.equals(WifiManager.WIFI_STATE_CHANGED_ACTION)) {
                handleWifiStateChanged(intent.getIntExtra(WifiManager.EXTRA_WIFI_STATE,
                        WifiManager.WIFI_STATE_UNKNOWN));
            }
        }
        private void handleNetworkStateChanged(NetworkInfo info) {
            if (V) {
                myLogV("Receiver.handleNetworkStateChanged: NetworkInfo: "
                        + info);
            }
            switch (info.getState()) {
                case CONNECTED:
                    WifiInfo wifiInfo = mWifiManager.getConnectionInfo();
                    if (wifiInfo.getSSID() == null || wifiInfo.getBSSID() == null) {
                        if (V) {
                            myLogV("handleNetworkStateChanged: Got connected event but SSID or BSSID are null. SSID: "
                                + wifiInfo.getSSID()
                                + ", BSSID: "
                                + wifiInfo.getBSSID() + ", ignoring event");
                        }
                        return;
                    }
                    onConnected(wifiInfo.getSSID(), wifiInfo.getBSSID());
                    break;
                case DISCONNECTED:
                    onDisconnected();
                    break;
            }
        }
        private void handleWifiStateChanged(int wifiState) {
            if (wifiState == WifiManager.WIFI_STATE_DISABLED) {
                quit();
            } else if (wifiState == WifiManager.WIFI_STATE_ENABLED) {
                onEnabled();
            }
        }
    };
    private static class AccessPoint {
        String ssid;
        String bssid;
        AccessPoint(String ssid, String bssid) {
            this.ssid = ssid;
            this.bssid = bssid;
        }
        private boolean hasNull() {
            return ssid == null || bssid == null;
        }
        @Override
        public boolean equals(Object o) {
            if (!(o instanceof AccessPoint)) return false;
            AccessPoint otherAp = (AccessPoint) o;
            boolean iHaveNull = hasNull();
            return (iHaveNull && otherAp.hasNull()) || 
                    (otherAp.bssid != null && ssid.equals(otherAp.ssid)
                    && bssid.equals(otherAp.bssid));
        }
        @Override
        public int hashCode() {
            if (ssid == null || bssid == null) return 0;
            return ssid.hashCode() + bssid.hashCode();
        }
        @Override
        public String toString() {
            return ssid + " (" + bssid + ")";
        }
    }
    private static class DnsPinger {
        private static final int DNS_QUERY_BASE_SIZE = 33;
        private static final int DNS_PORT = 53;
        private static Random sRandom = new Random();
        static boolean isDnsReachable(int dns, int timeout) {
            DatagramSocket socket = null;
            try {
                socket = new DatagramSocket();
                socket.setSoTimeout(timeout);
                byte[] buf = new byte[DNS_QUERY_BASE_SIZE];
                fillQuery(buf);
                byte parts[] = new byte[4];
                parts[0] = (byte)(dns & 0xff);
                parts[1] = (byte)((dns >> 8) & 0xff);
                parts[2] = (byte)((dns >> 16) & 0xff);
                parts[3] = (byte)((dns >> 24) & 0xff);
                InetAddress dnsAddress = InetAddress.getByAddress(parts);
                DatagramPacket packet = new DatagramPacket(buf,
                        buf.length, dnsAddress, DNS_PORT);
                socket.send(packet);
                DatagramPacket replyPacket = new DatagramPacket(buf, buf.length);
                socket.receive(replyPacket);
                return true;
            } catch (SocketException e) {
                if (V) {
                    Slog.v(TAG, "DnsPinger.isReachable received SocketException", e);
                }
                return false;
            } catch (UnknownHostException e) {
                if (V) {
                    Slog.v(TAG, "DnsPinger.isReachable is unable to resolve the DNS host", e);
                }
                return false;
            } catch (SocketTimeoutException e) {
                return false;
            } catch (IOException e) {
                if (V) {
                    Slog.v(TAG, "DnsPinger.isReachable got an IOException", e);
                }
                return false;
            } catch (Exception e) {
                if (V || Config.LOGD) {
                    Slog.d(TAG, "DnsPinger.isReachable got an unknown exception", e);
                }
                return false;
            } finally {
                if (socket != null) {
                    socket.close();
                }
            }
        }
        private static void fillQuery(byte[] buf) {
            for (int i = 0; i < buf.length; i++) buf[i] = 0;
            buf[0] = (byte) sRandom.nextInt(256); 
            buf[1] = (byte) sRandom.nextInt(256); 
            buf[2] = 1; 
            buf[5] = 1; 
            writeString(buf, 12, "www");
            writeString(buf, 16, "android");
            writeString(buf, 24, "com");
            buf[30] = 1;
            buf[32] = 1;
        }
        private static void writeString(byte[] buf, int startPos, String string) {
            int pos = startPos;
            buf[pos++] = (byte) string.length();
            for (int i = 0; i < string.length(); i++) {
                buf[pos++] = (byte) string.charAt(i);
            }
        }
    }
}
