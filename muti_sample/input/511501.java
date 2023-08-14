public class TrackerService extends Service {
    private List<LocationTrackingListener> mListeners;
    private static final String LOG_TAG = TrackerActivity.LOG_TAG;
    private Set<String> mTrackedProviders;
    private TrackerDataHelper mTrackerData;
    private TelephonyManager mTelephonyManager;
    private Location mNetworkLocation;
    private NetworkStateBroadcastReceiver mNetwork;
    private static final String CELL_PROVIDER_TAG = "cell";
    private static final String SIGNAL_PROVIDER_TAG = "signal";
    private static final String WIFI_PROVIDER_TAG = "wifi";
    private static final String DATA_CONN_PROVIDER_TAG = "data";
    private static final String MIN_TIME_PREF = "mintime_preference";
    private static final String MIN_DIS_PREF = "mindistance_preference";
    private static final String GPS_PREF = "gps_preference";
    private static final String NETWORK_PREF = "network_preference";
    private static final String SIGNAL_PREF = "signal_preference";
    private static final String DEBUG_PREF = "advanced_log_preference";
    private PreferenceListener mPrefListener;
    public TrackerService() {
    }
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
    @Override
    public void onStart(Intent intent, int startId) {
        super.onStart(intent, startId);
        mNetworkLocation = null;
        initLocationListeners();
        Toast.makeText(this, "Tracking service started", Toast.LENGTH_SHORT);
    }
    private synchronized void initLocationListeners() {
        mTrackerData = new TrackerDataHelper(this);
        LocationManager lm = getLocationManager();
        mTrackedProviders = getTrackedProviders();
        List<String> locationProviders = lm.getAllProviders();
        mListeners = new ArrayList<LocationTrackingListener>(
                locationProviders.size());
        long minUpdateTime = getLocationUpdateTime();
        float minDistance = getLocationMinDistance();
        for (String providerName : locationProviders) {
            if (mTrackedProviders.contains(providerName)) {
                Log.d(LOG_TAG, "Adding location listener for provider " +
                        providerName);
                if (doDebugLogging()) {
                    mTrackerData.writeEntry("init", String.format(
                            "start listening to %s : %d ms; %f meters",
                            providerName, minUpdateTime, minDistance));
                }    
                LocationTrackingListener listener =
                    new LocationTrackingListener();
                lm.requestLocationUpdates(providerName, minUpdateTime,
                        minDistance, listener);
                mListeners.add(listener);
            }
        }
        mTelephonyManager = (TelephonyManager)getSystemService(Context.TELEPHONY_SERVICE);
        if (doDebugLogging()) {
            mTelephonyManager.listen(mPhoneStateListener, PhoneStateListener.LISTEN_CELL_LOCATION);
            mNetwork = new NetworkStateBroadcastReceiver();
            IntentFilter mIntentFilter;
            mIntentFilter = new IntentFilter();
            mIntentFilter.addAction(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION);
            mIntentFilter.addAction(WifiManager.WIFI_STATE_CHANGED_ACTION);
            mIntentFilter.addAction(ConnectivityManager.CONNECTIVITY_ACTION);
            Log.d(LOG_TAG, "registering receiver");
            registerReceiver(mNetwork, mIntentFilter);
        }
        if (trackSignalStrength()) {
            mTelephonyManager.listen(mPhoneStateListener,
                    PhoneStateListener.LISTEN_SIGNAL_STRENGTHS);
        }
        mPrefListener = new PreferenceListener();
        getPreferences().registerOnSharedPreferenceChangeListener(mPrefListener);
    }
    private Set<String> getTrackedProviders() {
        Set<String> providerSet = new HashSet<String>();
        if (trackGPS()) {
            providerSet.add(LocationManager.GPS_PROVIDER);
        }
        if (trackNetwork()) {
            providerSet.add(LocationManager.NETWORK_PROVIDER);
        }
        return providerSet;
    }
    private SharedPreferences getPreferences() {
        return PreferenceManager.getDefaultSharedPreferences(this);
    }
    private boolean trackNetwork() {
        return getPreferences().getBoolean(NETWORK_PREF, true);
    }
    private boolean trackGPS() {
        return getPreferences().getBoolean(GPS_PREF, true);
    }
    private boolean doDebugLogging() {
        return getPreferences().getBoolean(DEBUG_PREF, false);
    }
    private boolean trackSignalStrength() {
        return getPreferences().getBoolean(SIGNAL_PREF, false);
    }
    private float getLocationMinDistance() {
        try {
            String disString = getPreferences().getString(MIN_DIS_PREF, "0");
            return Float.parseFloat(disString);
        }
        catch (NumberFormatException e) {
            Log.e(LOG_TAG, "Invalid preference for location min distance", e);
        }
        return 0;
    }
    private long getLocationUpdateTime() {
        try {
            String timeString = getPreferences().getString(MIN_TIME_PREF, "0");
            long secondsTime = Long.valueOf(timeString);
            return secondsTime * 1000;
        }
        catch (NumberFormatException e) {
            Log.e(LOG_TAG, "Invalid preference for location min time", e);
        }
        return 0;
    }
    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.d(LOG_TAG, "Removing location listeners");
        stopListeners();
        Toast.makeText(this, "Tracking service stopped", Toast.LENGTH_SHORT);
    }
    protected synchronized void stopListeners() {
        LocationManager lm = getLocationManager();
        if (mListeners != null) {
            for (LocationTrackingListener listener : mListeners) {
                lm.removeUpdates(listener);
            }
            mListeners.clear();
        }
        mListeners = null;
        if (mTelephonyManager != null) {
            mTelephonyManager.listen(mPhoneStateListener, 0);
        }    
        if (mNetwork != null) {
            unregisterReceiver(mNetwork);
        }
        mNetwork = null;
        mTrackerData = null;
        if (mPrefListener != null) {
            getPreferences().unregisterOnSharedPreferenceChangeListener(mPrefListener);
            mPrefListener = null;
        }
    }
    private LocationManager getLocationManager() {
        return (LocationManager) getSystemService(Context.LOCATION_SERVICE);
    }
    private synchronized float getDistanceFromNetwork(Location location) {
        float value = 0;
        if (mNetworkLocation != null) {
            value = location.distanceTo(mNetworkLocation);
        }
        if (LocationManager.NETWORK_PROVIDER.equals(location.getProvider())) {
            mNetworkLocation = location;
        }
        return value;
    }
    private class LocationTrackingListener implements LocationListener {
        public void onLocationChanged(Location location) {
            if (location == null) {
                return;
            }
            float distance = getDistanceFromNetwork(location);
            mTrackerData.writeEntry(location, distance);
        }
        public void onProviderDisabled(String provider) {
            if (doDebugLogging()) {
                mTrackerData.writeEntry(provider, "provider disabled");
            }
        }
        public void onProviderEnabled(String provider) {
            if (doDebugLogging()) {
                mTrackerData.writeEntry(provider,  "provider enabled");
            }
        }
        public void onStatusChanged(String provider, int status, Bundle extras) {
            if (doDebugLogging()) {
                mTrackerData.writeEntry(provider,  "status change: " + status);
            }
        }
    }
    PhoneStateListener mPhoneStateListener = new PhoneStateListener() {
        @Override
        public void onCellLocationChanged(CellLocation location) {
            try {
                if (location instanceof GsmCellLocation) {
                    GsmCellLocation cellLocation = (GsmCellLocation)location;
                    String updateMsg = "cid=" + cellLocation.getCid() +
                            ", lac=" + cellLocation.getLac();
                    mTrackerData.writeEntry(CELL_PROVIDER_TAG, updateMsg);
                } else if (location instanceof CdmaCellLocation) {
                    CdmaCellLocation cellLocation = (CdmaCellLocation)location;
                    String updateMsg = "BID=" + cellLocation.getBaseStationId() +
                            ", SID=" + cellLocation.getSystemId() +
                            ", NID=" + cellLocation.getNetworkId() +
                            ", lat=" + cellLocation.getBaseStationLatitude() +
                            ", long=" + cellLocation.getBaseStationLongitude() +
                            ", SID=" + cellLocation.getSystemId() +
                            ", NID=" + cellLocation.getNetworkId();
                    mTrackerData.writeEntry(CELL_PROVIDER_TAG, updateMsg);
                }
            } catch (Exception e) {
                Log.e(LOG_TAG, "Exception in CellStateHandler.handleMessage:", e);
            }
        }
        public void onSignalStrengthsChanged(SignalStrength signalStrength) {
            if (mTelephonyManager.getPhoneType() == TelephonyManager.PHONE_TYPE_CDMA) {
                String updateMsg = "cdma dBM=" + signalStrength.getCdmaDbm();
                mTrackerData.writeEntry(SIGNAL_PROVIDER_TAG, updateMsg);
            } else if  (mTelephonyManager.getPhoneType() == TelephonyManager.PHONE_TYPE_GSM) {
                String updateMsg = "gsm signal=" + signalStrength.getGsmSignalStrength();
                mTrackerData.writeEntry(SIGNAL_PROVIDER_TAG, updateMsg);
            }
        }
    };
    private class NetworkStateBroadcastReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (action.equals(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION)) {
                WifiManager wifiManager =
                    (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
                List<ScanResult> wifiScanResults = wifiManager.getScanResults();
                String updateMsg = "num scan results=" +
                    (wifiScanResults == null ? "0" : wifiScanResults.size());
                mTrackerData.writeEntry(WIFI_PROVIDER_TAG, updateMsg);
            } else if (action.equals(ConnectivityManager.CONNECTIVITY_ACTION)) {
                String updateMsg;
                boolean noConnectivity =
                    intent.getBooleanExtra(
                            ConnectivityManager.EXTRA_NO_CONNECTIVITY, false);
                if (noConnectivity) {
                    updateMsg = "no connectivity";
                }
                else {
                    updateMsg = "connection available";
                }
                mTrackerData.writeEntry(DATA_CONN_PROVIDER_TAG, updateMsg);
            } else if (action.equals(WifiManager.WIFI_STATE_CHANGED_ACTION)) {
                int state = intent.getIntExtra(WifiManager.EXTRA_WIFI_STATE,
                    WifiManager.WIFI_STATE_UNKNOWN);
                String stateString = "unknown";
                switch (state) {
                    case WifiManager.WIFI_STATE_DISABLED:
                        stateString = "disabled";
                        break;
                    case WifiManager.WIFI_STATE_DISABLING:
                        stateString = "disabling";
                        break;
                    case WifiManager.WIFI_STATE_ENABLED:
                        stateString = "enabled";
                        break;
                    case WifiManager.WIFI_STATE_ENABLING:
                        stateString = "enabling";
                        break;
                }
                mTrackerData.writeEntry(WIFI_PROVIDER_TAG,
                        "state = " + stateString);
            }
        }
    }
    private class PreferenceListener implements OnSharedPreferenceChangeListener {
        public void onSharedPreferenceChanged(
                SharedPreferences sharedPreferences, String key) {
            Log.d(LOG_TAG, "restarting listeners due to preference change");
            synchronized (TrackerService.this) {
                stopListeners();
                initLocationListeners();
            }
        }
    }
}
