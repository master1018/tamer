public class WifiMonitor {
    private static final String TAG = "WifiMonitor";
    private static final int CONNECTED    = 1;
    private static final int DISCONNECTED = 2;
    private static final int STATE_CHANGE = 3;
    private static final int SCAN_RESULTS = 4;
    private static final int LINK_SPEED   = 5;
    private static final int TERMINATING  = 6;
    private static final int DRIVER_STATE = 7;
    private static final int UNKNOWN      = 8;
    private static final String eventPrefix = "CTRL-EVENT-";
    private static final int eventPrefixLen = eventPrefix.length();
    private static final String wpaEventPrefix = "WPA:";
    private static final String passwordKeyMayBeIncorrectEvent =
       "pre-shared key may be incorrect";
    private static final String connectedEvent =    "CONNECTED";
    private static final String disconnectedEvent = "DISCONNECTED";
    private static final String stateChangeEvent =  "STATE-CHANGE";
    private static final String scanResultsEvent =  "SCAN-RESULTS";
    private static final String linkSpeedEvent = "LINK-SPEED";
    private static final String terminatingEvent =  "TERMINATING";
    private static final String driverStateEvent = "DRIVER-STATE";
    private static Pattern mConnectedEventPattern =
        Pattern.compile("((?:[0-9a-f]{2}:){5}[0-9a-f]{2}) .* \\[id=([0-9]+) ");
    private final WifiStateTracker mWifiStateTracker;
    private static final String monitorSocketClosed = "connection closed";
    private static final String wpaRecvError = "recv error";
    private int mRecvErrors = 0;
    private static final int MAX_RECV_ERRORS    = 10;
    public WifiMonitor(WifiStateTracker tracker) {
        mWifiStateTracker = tracker;
    }
    public void startMonitoring() {
        new MonitorThread().start();
    }
    public NetworkStateTracker getNetworkStateTracker() {
        return mWifiStateTracker;
    }
    class MonitorThread extends Thread {
        public MonitorThread() {
            super("WifiMonitor");
        }
        public void run() {
            if (connectToSupplicant()) {
                mWifiStateTracker.notifySupplicantConnection();
            } else {
                mWifiStateTracker.notifySupplicantLost();
                return;
            }
            for (;;) {
                String eventStr = WifiNative.waitForEvent();
                if (Config.LOGD && eventStr.indexOf(scanResultsEvent) == -1) {
                    Log.v(TAG, "Event [" + eventStr + "]");
                }
                if (!eventStr.startsWith(eventPrefix)) {
                    if (eventStr.startsWith(wpaEventPrefix) &&
                            0 < eventStr.indexOf(passwordKeyMayBeIncorrectEvent)) {
                        handlePasswordKeyMayBeIncorrect();
                    }
                    continue;
                }
                String eventName = eventStr.substring(eventPrefixLen);
                int nameEnd = eventName.indexOf(' ');
                if (nameEnd != -1)
                    eventName = eventName.substring(0, nameEnd);
                if (eventName.length() == 0) {
                    if (Config.LOGD) Log.i(TAG, "Received wpa_supplicant event with empty event name");
                    continue;
                }
                int event;
                if (eventName.equals(connectedEvent))
                    event = CONNECTED;
                else if (eventName.equals(disconnectedEvent))
                    event = DISCONNECTED;
                else if (eventName.equals(stateChangeEvent))
                    event = STATE_CHANGE;
                else if (eventName.equals(scanResultsEvent))
                    event = SCAN_RESULTS;
                else if (eventName.equals(linkSpeedEvent))
                    event = LINK_SPEED;
                else if (eventName.equals(terminatingEvent))
                    event = TERMINATING;
                else if (eventName.equals(driverStateEvent)) {
                    event = DRIVER_STATE;
                }
                else
                    event = UNKNOWN;
                String eventData = eventStr;
                if (event == DRIVER_STATE || event == LINK_SPEED)
                    eventData = eventData.split(" ")[1];
                else if (event == STATE_CHANGE) {
                    int ind = eventStr.indexOf(" ");
                    if (ind != -1) {
                        eventData = eventStr.substring(ind + 1);
                    }
                } else {
                    int ind = eventStr.indexOf(" - ");
                    if (ind != -1) {
                        eventData = eventStr.substring(ind + 3);
                    }
                }
                if (event == STATE_CHANGE) {
                    handleSupplicantStateChange(eventData);
                } else if (event == DRIVER_STATE) {
                    handleDriverEvent(eventData);
                } else if (event == TERMINATING) {
                    if (eventData.startsWith(monitorSocketClosed)) {
                        if (Config.LOGD) {
                            Log.d(TAG, "Monitor socket is closed, exiting thread");
                        }
                        break;
                    }
                    if (eventData.startsWith(wpaRecvError)) {
                        if (++mRecvErrors > MAX_RECV_ERRORS) {
                            if (Config.LOGD) {
                                Log.d(TAG, "too many recv errors, closing connection");
                            }
                        } else {
                            continue;
                        }
                    }
                    mWifiStateTracker.notifySupplicantLost();
                    break;
                } else {
                    handleEvent(event, eventData);
                }
                mRecvErrors = 0;
            }
        }
        private boolean connectToSupplicant() {
            int connectTries = 0;
            while (true) {
                if (mWifiStateTracker.connectToSupplicant()) {
                    return true;
                }
                if (connectTries++ < 3) {
                    nap(5);
                } else {
                    break;
                }
            }
            return false;
        }
        private void handlePasswordKeyMayBeIncorrect() {
            mWifiStateTracker.notifyPasswordKeyMayBeIncorrect();
        }
        private void handleDriverEvent(String state) {
            if (state == null) {
                return;
            }
            if (state.equals("STOPPED")) {
                mWifiStateTracker.notifyDriverStopped();
            } else if (state.equals("STARTED")) {
                mWifiStateTracker.notifyDriverStarted();
            } else if (state.equals("HANGED")) {
                mWifiStateTracker.notifyDriverHung();
            }
        }
        void handleEvent(int event, String remainder) {
            switch (event) {
                case DISCONNECTED:
                    handleNetworkStateChange(NetworkInfo.DetailedState.DISCONNECTED, remainder);
                    break;
                case CONNECTED:
                    handleNetworkStateChange(NetworkInfo.DetailedState.CONNECTED, remainder);
                    break;
                case SCAN_RESULTS:
                    mWifiStateTracker.notifyScanResultsAvailable();
                    break;
                case UNKNOWN:
                    break;
            }
        }
        private void handleSupplicantStateChange(String dataString) {
            String[] dataTokens = dataString.split(" ");
            String BSSID = null;
            int networkId = -1;
            int newState  = -1;
            for (String token : dataTokens) {
                String[] nameValue = token.split("=");
                if (nameValue.length != 2) {
                    continue;
                }
                if (nameValue[0].equals("BSSID")) {
                    BSSID = nameValue[1];
                    continue;
                }
                int value;
                try {
                    value = Integer.parseInt(nameValue[1]);
                } catch (NumberFormatException e) {
                    Log.w(TAG, "STATE-CHANGE non-integer parameter: " + token);
                    continue;
                }
                if (nameValue[0].equals("id")) {
                    networkId = value;
                } else if (nameValue[0].equals("state")) {
                    newState = value;
                }
            }
            if (newState == -1) return;
            SupplicantState newSupplicantState = SupplicantState.INVALID;
            for (SupplicantState state : SupplicantState.values()) {
                if (state.ordinal() == newState) {
                    newSupplicantState = state;
                    break;
                }
            }
            if (newSupplicantState == SupplicantState.INVALID) {
                Log.w(TAG, "Invalid supplicant state: " + newState);
            }
            mWifiStateTracker.notifyStateChange(networkId, BSSID, newSupplicantState);
        }
    }
    private void handleNetworkStateChange(NetworkInfo.DetailedState newState, String data) {
        String BSSID = null;
        int networkId = -1;
        if (newState == NetworkInfo.DetailedState.CONNECTED) {
            Matcher match = mConnectedEventPattern.matcher(data);
            if (!match.find()) {
                if (Config.LOGD) Log.d(TAG, "Could not find BSSID in CONNECTED event string");
            } else {
                BSSID = match.group(1);
                try {
                    networkId = Integer.parseInt(match.group(2));
                } catch (NumberFormatException e) {
                    networkId = -1;
                }
            }
        }
        mWifiStateTracker.notifyStateChange(newState, BSSID, networkId);
    }
    private static void nap(int secs) {
        try {
            Thread.sleep(secs * 1000);
        } catch (InterruptedException ignore) {
        }
    }
}
