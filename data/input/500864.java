public class GpsLocationProvider implements LocationProviderInterface {
    private static final String TAG = "GpsLocationProvider";
    private static final boolean DEBUG = false;
    private static final boolean VERBOSE = false;
    public static final String GPS_ENABLED_CHANGE_ACTION =
        "android.location.GPS_ENABLED_CHANGE";
    public static final String GPS_FIX_CHANGE_ACTION =
        "android.location.GPS_FIX_CHANGE";
    public static final String EXTRA_ENABLED = "enabled";
    private static final int GPS_POSITION_MODE_STANDALONE = 0;
    private static final int GPS_POSITION_MODE_MS_BASED = 1;
    private static final int GPS_POSITION_MODE_MS_ASSISTED = 2;
    private static final int GPS_STATUS_NONE = 0;
    private static final int GPS_STATUS_SESSION_BEGIN = 1;
    private static final int GPS_STATUS_SESSION_END = 2;
    private static final int GPS_STATUS_ENGINE_ON = 3;
    private static final int GPS_STATUS_ENGINE_OFF = 4;
    private static final int GPS_REQUEST_AGPS_DATA_CONN = 1;
    private static final int GPS_RELEASE_AGPS_DATA_CONN = 2;
    private static final int GPS_AGPS_DATA_CONNECTED = 3;
    private static final int GPS_AGPS_DATA_CONN_DONE = 4;
    private static final int GPS_AGPS_DATA_CONN_FAILED = 5;
    private static final int LOCATION_INVALID = 0;
    private static final int LOCATION_HAS_LAT_LONG = 1;
    private static final int LOCATION_HAS_ALTITUDE = 2;
    private static final int LOCATION_HAS_SPEED = 4;
    private static final int LOCATION_HAS_BEARING = 8;
    private static final int LOCATION_HAS_ACCURACY = 16;
    private static final int GPS_DELETE_EPHEMERIS = 0x0001;
    private static final int GPS_DELETE_ALMANAC = 0x0002;
    private static final int GPS_DELETE_POSITION = 0x0004;
    private static final int GPS_DELETE_TIME = 0x0008;
    private static final int GPS_DELETE_IONO = 0x0010;
    private static final int GPS_DELETE_UTC = 0x0020;
    private static final int GPS_DELETE_HEALTH = 0x0040;
    private static final int GPS_DELETE_SVDIR = 0x0080;
    private static final int GPS_DELETE_SVSTEER = 0x0100;
    private static final int GPS_DELETE_SADATA = 0x0200;
    private static final int GPS_DELETE_RTI = 0x0400;
    private static final int GPS_DELETE_CELLDB_INFO = 0x8000;
    private static final int GPS_DELETE_ALL = 0xFFFF;
    private static final int AGPS_TYPE_SUPL = 1;
    private static final int AGPS_TYPE_C2K = 2;
    private static final int AGPS_DATA_CONNECTION_CLOSED = 0;
    private static final int AGPS_DATA_CONNECTION_OPENING = 1;
    private static final int AGPS_DATA_CONNECTION_OPEN = 2;
    private static final int CHECK_LOCATION = 1;
    private static final int ENABLE = 2;
    private static final int ENABLE_TRACKING = 3;
    private static final int UPDATE_NETWORK_STATE = 4;
    private static final int INJECT_NTP_TIME = 5;
    private static final int DOWNLOAD_XTRA_DATA = 6;
    private static final int UPDATE_LOCATION = 7;
    private static final int ADD_LISTENER = 8;
    private static final int REMOVE_LISTENER = 9;
    private static final String PROPERTIES_FILE = "/etc/gps.conf";
    private int mLocationFlags = LOCATION_INVALID;
    private int mStatus = LocationProvider.TEMPORARILY_UNAVAILABLE;
    private long mStatusUpdateTime = SystemClock.elapsedRealtime();
    private static final long RECENT_FIX_TIMEOUT = 10;
    private static final int MIN_FIX_COUNT = 10;
    private static final int NO_FIX_TIMEOUT = 60;
    private volatile boolean mEnabled;
    private boolean mNetworkAvailable;
    private boolean mInjectNtpTimePending = true;
    private boolean mDownloadXtraDataPending = true;
    private boolean mNavigating;
    private boolean mEngineOn;
    private int mFixInterval = 1;
    private int mFixCount;
    private boolean mStarted;
    private long mFixRequestTime = 0;
    private int mTTFF = 0;
    private long mLastFixTime;
    private Properties mProperties;
    private String mNtpServer;
    private String mSuplServerHost;
    private int mSuplServerPort;
    private String mC2KServerHost;
    private int mC2KServerPort;
    private final Context mContext;
    private final ILocationManager mLocationManager;
    private Location mLocation = new Location(LocationManager.GPS_PROVIDER);
    private Bundle mLocationExtras = new Bundle();
    private ArrayList<Listener> mListeners = new ArrayList<Listener>();
    private final Thread mThread;
    private Handler mHandler;
    private final CountDownLatch mInitializedLatch = new CountDownLatch(1);
    private Thread mEventThread;
    private String mAGpsApn;
    private int mAGpsDataConnectionState;
    private final ConnectivityManager mConnMgr;
    private final GpsNetInitiatedHandler mNIHandler; 
    private final static String WAKELOCK_KEY = "GpsLocationProvider";
    private final PowerManager.WakeLock mWakeLock;
    private final static String ALARM_WAKEUP = "com.android.internal.location.ALARM_WAKEUP";
    private final static String ALARM_TIMEOUT = "com.android.internal.location.ALARM_TIMEOUT";
    private final AlarmManager mAlarmManager;
    private final PendingIntent mWakeupIntent;
    private final PendingIntent mTimeoutIntent;
    private final IBatteryStats mBatteryStats;
    private final SparseIntArray mClientUids = new SparseIntArray();
    private static final long NTP_INTERVAL = 4*60*60*1000;
    private static final long RETRY_INTERVAL = 5*60*1000;
    private static final long MAX_NTP_SYSTEM_TIME_OFFSET = 5*60*1000;
    private final IGpsStatusProvider mGpsStatusProvider = new IGpsStatusProvider.Stub() {
        public void addGpsStatusListener(IGpsStatusListener listener) throws RemoteException {
            if (listener == null) {
                throw new NullPointerException("listener is null in addGpsStatusListener");
            }
            synchronized(mListeners) {
                IBinder binder = listener.asBinder();
                int size = mListeners.size();
                for (int i = 0; i < size; i++) {
                    Listener test = mListeners.get(i);
                    if (binder.equals(test.mListener.asBinder())) {
                        return;
                    }
                }
                Listener l = new Listener(listener);
                binder.linkToDeath(l, 0);
                mListeners.add(l);
            }
        }
        public void removeGpsStatusListener(IGpsStatusListener listener) {
            if (listener == null) {
                throw new NullPointerException("listener is null in addGpsStatusListener");
            }
            synchronized(mListeners) {
                IBinder binder = listener.asBinder();
                Listener l = null;
                int size = mListeners.size();
                for (int i = 0; i < size && l == null; i++) {
                    Listener test = mListeners.get(i);
                    if (binder.equals(test.mListener.asBinder())) {
                        l = test;
                    }
                }
                if (l != null) {
                    mListeners.remove(l);
                    binder.unlinkToDeath(l, 0);
                }
            }
        }
    };
    public IGpsStatusProvider getGpsStatusProvider() {
        return mGpsStatusProvider;
    }
    private final BroadcastReceiver mBroadcastReciever = new BroadcastReceiver() {
        @Override public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (action.equals(ALARM_WAKEUP)) {
                if (DEBUG) Log.d(TAG, "ALARM_WAKEUP");
                startNavigating();
            } else if (action.equals(ALARM_TIMEOUT)) {
                if (DEBUG) Log.d(TAG, "ALARM_TIMEOUT");
                hibernate();
            }
        }
    };
    public static boolean isSupported() {
        return native_is_supported();
    }
    public GpsLocationProvider(Context context, ILocationManager locationManager) {
        mContext = context;
        mLocationManager = locationManager;
        mNIHandler = new GpsNetInitiatedHandler(context, this);
        mLocation.setExtras(mLocationExtras);
        PowerManager powerManager = (PowerManager) mContext.getSystemService(Context.POWER_SERVICE);
        mWakeLock = powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, WAKELOCK_KEY);
        mAlarmManager = (AlarmManager)mContext.getSystemService(Context.ALARM_SERVICE);
        mWakeupIntent = PendingIntent.getBroadcast(mContext, 0, new Intent(ALARM_WAKEUP), 0);
        mTimeoutIntent = PendingIntent.getBroadcast(mContext, 0, new Intent(ALARM_TIMEOUT), 0);
        mConnMgr = (ConnectivityManager)context.getSystemService(Context.CONNECTIVITY_SERVICE);
        mBatteryStats = IBatteryStats.Stub.asInterface(ServiceManager.getService("batteryinfo"));
        mProperties = new Properties();
        try {
            File file = new File(PROPERTIES_FILE);
            FileInputStream stream = new FileInputStream(file);
            mProperties.load(stream);
            stream.close();
            mNtpServer = mProperties.getProperty("NTP_SERVER", null);
            mSuplServerHost = mProperties.getProperty("SUPL_HOST");
            String portString = mProperties.getProperty("SUPL_PORT");
            if (mSuplServerHost != null && portString != null) {
                try {
                    mSuplServerPort = Integer.parseInt(portString);
                } catch (NumberFormatException e) {
                    Log.e(TAG, "unable to parse SUPL_PORT: " + portString);
                }
            }
            mC2KServerHost = mProperties.getProperty("C2K_HOST");
            portString = mProperties.getProperty("C2K_PORT");
            if (mC2KServerHost != null && portString != null) {
                try {
                    mC2KServerPort = Integer.parseInt(portString);
                } catch (NumberFormatException e) {
                    Log.e(TAG, "unable to parse C2K_PORT: " + portString);
                }
            }
        } catch (IOException e) {
            Log.w(TAG, "Could not open GPS configuration file " + PROPERTIES_FILE);
        }
        mThread = new GpsLocationProviderThread();
        mThread.start();
        while (true) {
            try {
                mInitializedLatch.await();
                break;
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
    }
    private void initialize() {
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(ALARM_WAKEUP);
        intentFilter.addAction(ALARM_TIMEOUT);
        mContext.registerReceiver(mBroadcastReciever, intentFilter);
    }
    public String getName() {
        return LocationManager.GPS_PROVIDER;
    }
    public boolean requiresNetwork() {
        return true;
    }
    public void updateNetworkState(int state, NetworkInfo info) {
        mHandler.removeMessages(UPDATE_NETWORK_STATE);
        Message m = Message.obtain(mHandler, UPDATE_NETWORK_STATE);
        m.arg1 = state;
        m.obj = info;
        mHandler.sendMessage(m);
    }
    private void handleUpdateNetworkState(int state, NetworkInfo info) {
        mNetworkAvailable = (state == LocationProvider.AVAILABLE);
        if (DEBUG) {
            Log.d(TAG, "updateNetworkState " + (mNetworkAvailable ? "available" : "unavailable")
                + " info: " + info);
        }
        if (info != null && info.getType() == ConnectivityManager.TYPE_MOBILE_SUPL
                && mAGpsDataConnectionState == AGPS_DATA_CONNECTION_OPENING) {
            String apnName = info.getExtraInfo();
            if (mNetworkAvailable && apnName != null && apnName.length() > 0) {
                mAGpsApn = apnName;
                if (DEBUG) Log.d(TAG, "call native_agps_data_conn_open");
                native_agps_data_conn_open(apnName);
                mAGpsDataConnectionState = AGPS_DATA_CONNECTION_OPEN;
            } else {
                if (DEBUG) Log.d(TAG, "call native_agps_data_conn_failed");
                mAGpsApn = null;
                mAGpsDataConnectionState = AGPS_DATA_CONNECTION_CLOSED;
                native_agps_data_conn_failed();
            }
        }
        if (mNetworkAvailable) {
            if (mInjectNtpTimePending) {
                mHandler.removeMessages(INJECT_NTP_TIME);
                mHandler.sendMessage(Message.obtain(mHandler, INJECT_NTP_TIME));
            }
            if (mDownloadXtraDataPending) {
                mHandler.removeMessages(DOWNLOAD_XTRA_DATA);
                mHandler.sendMessage(Message.obtain(mHandler, DOWNLOAD_XTRA_DATA));
            }
        }
    }
    private void handleInjectNtpTime() {
        if (!mNetworkAvailable) {
            mInjectNtpTimePending = true;
            return;
        }
        mInjectNtpTimePending = false;
        SntpClient client = new SntpClient();
        long delay;
        if (client.requestTime(mNtpServer, 10000)) {
            long time = client.getNtpTime();
            long timeReference = client.getNtpTimeReference();
            int certainty = (int)(client.getRoundTripTime()/2);
            long now = System.currentTimeMillis();
            long systemTimeOffset = time - now;
            Log.d(TAG, "NTP server returned: "
                    + time + " (" + new Date(time)
                    + ") reference: " + timeReference
                    + " certainty: " + certainty
                    + " system time offset: " + systemTimeOffset);
            if (systemTimeOffset < 0) {
                systemTimeOffset = -systemTimeOffset;
            }
            if (systemTimeOffset < MAX_NTP_SYSTEM_TIME_OFFSET) {
                native_inject_time(time, timeReference, certainty);
            } else {
                Log.e(TAG, "NTP time differs from system time by " + systemTimeOffset
                        + "ms.  Ignoring.");
            }
            delay = NTP_INTERVAL;
        } else {
            if (DEBUG) Log.d(TAG, "requestTime failed");
            delay = RETRY_INTERVAL;
        }
        mHandler.removeMessages(INJECT_NTP_TIME);
        mHandler.sendMessageDelayed(Message.obtain(mHandler, INJECT_NTP_TIME), delay);
    }
    private void handleDownloadXtraData() {
        if (!mDownloadXtraDataPending) {
            mDownloadXtraDataPending = true;
            return;
        }
        mDownloadXtraDataPending = false;
        GpsXtraDownloader xtraDownloader = new GpsXtraDownloader(mContext, mProperties);
        byte[] data = xtraDownloader.downloadXtraData();
        if (data != null) {
            if (DEBUG) {
                Log.d(TAG, "calling native_inject_xtra_data");
            }
            native_inject_xtra_data(data, data.length);
        } else {
            mHandler.removeMessages(DOWNLOAD_XTRA_DATA);
            mHandler.sendMessageDelayed(Message.obtain(mHandler, DOWNLOAD_XTRA_DATA), RETRY_INTERVAL);
        }
    }
    public void updateLocation(Location location) {
        mHandler.removeMessages(UPDATE_LOCATION);
        Message m = Message.obtain(mHandler, UPDATE_LOCATION);
        m.obj = location;
        mHandler.sendMessage(m);
    }
    private void handleUpdateLocation(Location location) {
        if (location.hasAccuracy()) {
            native_inject_location(location.getLatitude(), location.getLongitude(),
                    location.getAccuracy());
        }
    }
    public boolean requiresSatellite() {
        return true;
    }
    public boolean requiresCell() {
        return false;
    }
    public boolean hasMonetaryCost() {
        return false;
    }
    public boolean supportsAltitude() {
        return true;
    }
    public boolean supportsSpeed() {
        return true;
    }
    public boolean supportsBearing() {
        return true;
    }
    public int getPowerRequirement() {
        return Criteria.POWER_HIGH;
    }
    public int getAccuracy() {
        return Criteria.ACCURACY_FINE;
    }
    public void enable() {
        synchronized (mHandler) {
            mHandler.removeMessages(ENABLE);
            Message m = Message.obtain(mHandler, ENABLE);
            m.arg1 = 1;
            mHandler.sendMessage(m);
        }
    }
    private void handleEnable() {
        if (DEBUG) Log.d(TAG, "handleEnable");
        if (mEnabled) return;
        mEnabled = native_init();
        if (mEnabled) {
            if (mSuplServerHost != null) {
                native_set_agps_server(AGPS_TYPE_SUPL, mSuplServerHost, mSuplServerPort);
            }
            if (mC2KServerHost != null) {
                native_set_agps_server(AGPS_TYPE_C2K, mC2KServerHost, mC2KServerPort);
            }
            mEventThread = new GpsEventThread();
            mEventThread.start();
        } else {
            Log.w(TAG, "Failed to enable location provider");
        }
    }
    public void disable() {
        synchronized (mHandler) {
            mHandler.removeMessages(ENABLE);
            Message m = Message.obtain(mHandler, ENABLE);
            m.arg1 = 0;
            mHandler.sendMessage(m);
        }
    }
    private void handleDisable() {
        if (DEBUG) Log.d(TAG, "handleDisable");
        if (!mEnabled) return;
        mEnabled = false;
        stopNavigating();
        native_disable();
        if (mEventThread != null) {
            try {
                mEventThread.join();
            } catch (InterruptedException e) {
                Log.w(TAG, "InterruptedException when joining mEventThread");
            }
            mEventThread = null;
        }
        native_cleanup();
        if (mNavigating) {
            reportStatus(GPS_STATUS_SESSION_END);
        }
        if (mEngineOn) {
            reportStatus(GPS_STATUS_ENGINE_OFF);
        }
    }
    public boolean isEnabled() {
        return mEnabled;
    }
    public int getStatus(Bundle extras) {
        if (extras != null) {
            extras.putInt("satellites", mSvCount);
        }
        return mStatus;
    }
    private void updateStatus(int status, int svCount) {
        if (status != mStatus || svCount != mSvCount) {
            mStatus = status;
            mSvCount = svCount;
            mLocationExtras.putInt("satellites", svCount);
            mStatusUpdateTime = SystemClock.elapsedRealtime();
        }
    }
    public long getStatusUpdateTime() {
        return mStatusUpdateTime;
    }
    public void enableLocationTracking(boolean enable) {
        synchronized (mHandler) {
            mHandler.removeMessages(ENABLE_TRACKING);
            Message m = Message.obtain(mHandler, ENABLE_TRACKING);
            m.arg1 = (enable ? 1 : 0);
            mHandler.sendMessage(m);
        }
    }
    private void handleEnableLocationTracking(boolean enable) {
        if (enable) {
            mTTFF = 0;
            mLastFixTime = 0;
            startNavigating();
        } else {
            mAlarmManager.cancel(mWakeupIntent);
            mAlarmManager.cancel(mTimeoutIntent);
            stopNavigating();
        }
    }
    public void setMinTime(long minTime) {
        if (DEBUG) Log.d(TAG, "setMinTime " + minTime);
        if (minTime >= 0) {
            int interval = (int)(minTime/1000);
            if (interval < 1) {
                interval = 1;
            }
            mFixInterval = interval;
        }
    }
    public String getInternalState() {
        return native_get_internal_state();
    }
    private final class Listener implements IBinder.DeathRecipient {
        final IGpsStatusListener mListener;
        int mSensors = 0;
        Listener(IGpsStatusListener listener) {
            mListener = listener;
        }
        public void binderDied() {
            if (DEBUG) Log.d(TAG, "GPS status listener died");
            synchronized(mListeners) {
                mListeners.remove(this);
            }
            if (mListener != null) {
                mListener.asBinder().unlinkToDeath(this, 0);
            }
        }
    }
    public void addListener(int uid) {
        Message m = Message.obtain(mHandler, ADD_LISTENER);
        m.arg1 = uid;
        mHandler.sendMessage(m);
    }
    private void handleAddListener(int uid) {
        synchronized(mListeners) {
            if (mClientUids.indexOfKey(uid) >= 0) {
                Log.w(TAG, "Duplicate add listener for uid " + uid);
                return;
            }
            mClientUids.put(uid, 0);
            if (mNavigating) {
                try {
                    mBatteryStats.noteStartGps(uid);
                } catch (RemoteException e) {
                    Log.w(TAG, "RemoteException in addListener");
                }
            }
        }
    }
    public void removeListener(int uid) {
        Message m = Message.obtain(mHandler, REMOVE_LISTENER);
        m.arg1 = uid;
        mHandler.sendMessage(m);
    }
    private void handleRemoveListener(int uid) {
        synchronized(mListeners) {
            if (mClientUids.indexOfKey(uid) < 0) {
                Log.w(TAG, "Unneeded remove listener for uid " + uid);
                return;
            }
            mClientUids.delete(uid);
            if (mNavigating) {
                try {
                    mBatteryStats.noteStopGps(uid);
                } catch (RemoteException e) {
                    Log.w(TAG, "RemoteException in removeListener");
                }
            }
        }
    }
    public boolean sendExtraCommand(String command, Bundle extras) {
        if ("delete_aiding_data".equals(command)) {
            return deleteAidingData(extras);
        }
        if ("force_time_injection".equals(command)) {
            mHandler.removeMessages(INJECT_NTP_TIME);
            mHandler.sendMessage(Message.obtain(mHandler, INJECT_NTP_TIME));
            return true;
        }
        if ("force_xtra_injection".equals(command)) {
            if (native_supports_xtra()) {
                xtraDownloadRequest();
                return true;
            }
            return false;
        }
        Log.w(TAG, "sendExtraCommand: unknown command " + command);
        return false;
    }
    private boolean deleteAidingData(Bundle extras) {
        int flags;
        if (extras == null) {
            flags = GPS_DELETE_ALL;
        } else {
            flags = 0;
            if (extras.getBoolean("ephemeris")) flags |= GPS_DELETE_EPHEMERIS;
            if (extras.getBoolean("almanac")) flags |= GPS_DELETE_ALMANAC;
            if (extras.getBoolean("position")) flags |= GPS_DELETE_POSITION;
            if (extras.getBoolean("time")) flags |= GPS_DELETE_TIME;
            if (extras.getBoolean("iono")) flags |= GPS_DELETE_IONO;
            if (extras.getBoolean("utc")) flags |= GPS_DELETE_UTC;
            if (extras.getBoolean("health")) flags |= GPS_DELETE_HEALTH;
            if (extras.getBoolean("svdir")) flags |= GPS_DELETE_SVDIR;
            if (extras.getBoolean("svsteer")) flags |= GPS_DELETE_SVSTEER;
            if (extras.getBoolean("sadata")) flags |= GPS_DELETE_SADATA;
            if (extras.getBoolean("rti")) flags |= GPS_DELETE_RTI;
            if (extras.getBoolean("celldb-info")) flags |= GPS_DELETE_CELLDB_INFO;
            if (extras.getBoolean("all")) flags |= GPS_DELETE_ALL;
        }
        if (flags != 0) {
            native_delete_aiding_data(flags);
            return true;
        }
        return false;
    }
    private void startNavigating() {
        if (!mStarted) {
            if (DEBUG) Log.d(TAG, "startNavigating");
            mStarted = true;
            int positionMode;
            if (Settings.Secure.getInt(mContext.getContentResolver(),
                    Settings.Secure.ASSISTED_GPS_ENABLED, 1) != 0) {
                positionMode = GPS_POSITION_MODE_MS_BASED;
            } else {
                positionMode = GPS_POSITION_MODE_STANDALONE;
            }
            if (!native_start(positionMode, false, 1)) {
                mStarted = false;
                Log.e(TAG, "native_start failed in startNavigating()");
                return;
            }
            updateStatus(LocationProvider.TEMPORARILY_UNAVAILABLE, 0);
            mFixCount = 0;
            mFixRequestTime = System.currentTimeMillis();
            if (mFixInterval >= NO_FIX_TIMEOUT) {
                mAlarmManager.set(AlarmManager.ELAPSED_REALTIME_WAKEUP,
                        SystemClock.elapsedRealtime() + NO_FIX_TIMEOUT * 1000, mTimeoutIntent);
            }
        }
    }
    private void stopNavigating() {
        if (DEBUG) Log.d(TAG, "stopNavigating");
        if (mStarted) {
            mStarted = false;
            native_stop();
            mTTFF = 0;
            mLastFixTime = 0;
            mLocationFlags = LOCATION_INVALID;
            updateStatus(LocationProvider.TEMPORARILY_UNAVAILABLE, 0);
        }
    }
    private void hibernate() {
        stopNavigating();
        mFixCount = 0;
        mAlarmManager.cancel(mTimeoutIntent);
        mAlarmManager.cancel(mWakeupIntent);
        long now = SystemClock.elapsedRealtime();
        mAlarmManager.set(AlarmManager.ELAPSED_REALTIME_WAKEUP,
                SystemClock.elapsedRealtime() + mFixInterval * 1000, mWakeupIntent);
    }
    private void reportLocation(int flags, double latitude, double longitude, double altitude,
            float speed, float bearing, float accuracy, long timestamp) {
        if (VERBOSE) Log.v(TAG, "reportLocation lat: " + latitude + " long: " + longitude +
                " timestamp: " + timestamp);
        mLastFixTime = System.currentTimeMillis();
        if (mTTFF == 0 && (flags & LOCATION_HAS_LAT_LONG) == LOCATION_HAS_LAT_LONG) {
            mTTFF = (int)(mLastFixTime - mFixRequestTime);
            if (DEBUG) Log.d(TAG, "TTFF: " + mTTFF);
            synchronized(mListeners) {
                int size = mListeners.size();
                for (int i = 0; i < size; i++) {
                    Listener listener = mListeners.get(i);
                    try {
                        listener.mListener.onFirstFix(mTTFF); 
                    } catch (RemoteException e) {
                        Log.w(TAG, "RemoteException in stopNavigating");
                        mListeners.remove(listener);
                        size--;
                    }
                }
            }
        }
        synchronized (mLocation) {
            mLocationFlags = flags;
            if ((flags & LOCATION_HAS_LAT_LONG) == LOCATION_HAS_LAT_LONG) {
                mLocation.setLatitude(latitude);
                mLocation.setLongitude(longitude);
                mLocation.setTime(timestamp);
            }
            if ((flags & LOCATION_HAS_ALTITUDE) == LOCATION_HAS_ALTITUDE) {
                mLocation.setAltitude(altitude);
            } else {
                mLocation.removeAltitude();
            }
            if ((flags & LOCATION_HAS_SPEED) == LOCATION_HAS_SPEED) {
                mLocation.setSpeed(speed);
            } else {
                mLocation.removeSpeed();
            }
            if ((flags & LOCATION_HAS_BEARING) == LOCATION_HAS_BEARING) {
                mLocation.setBearing(bearing);
            } else {
                mLocation.removeBearing();
            }
            if ((flags & LOCATION_HAS_ACCURACY) == LOCATION_HAS_ACCURACY) {
                mLocation.setAccuracy(accuracy);
            } else {
                mLocation.removeAccuracy();
            }
            try {
                mLocationManager.reportLocation(mLocation, false);
            } catch (RemoteException e) {
                Log.e(TAG, "RemoteException calling reportLocation");
            }
        }
        if (mStarted && mStatus != LocationProvider.AVAILABLE) {
            if (mFixInterval < NO_FIX_TIMEOUT) {
                mAlarmManager.cancel(mTimeoutIntent);
            }
            Intent intent = new Intent(GPS_FIX_CHANGE_ACTION);
            intent.putExtra(EXTRA_ENABLED, true);
            mContext.sendBroadcast(intent);
            updateStatus(LocationProvider.AVAILABLE, mSvCount);
        }
        if (mFixCount++ >= MIN_FIX_COUNT && mFixInterval > 1) {
            if (DEBUG) Log.d(TAG, "exceeded MIN_FIX_COUNT");
            hibernate();
        }
   }
    private void reportStatus(int status) {
        if (VERBOSE) Log.v(TAG, "reportStatus status: " + status);
        synchronized(mListeners) {
            boolean wasNavigating = mNavigating;
            switch (status) {
                case GPS_STATUS_SESSION_BEGIN:
                    mNavigating = true;
                    mEngineOn = true;
                    break;
                case GPS_STATUS_SESSION_END:
                    mNavigating = false;
                    break;
                case GPS_STATUS_ENGINE_ON:
                    mEngineOn = true;
                    break;
                case GPS_STATUS_ENGINE_OFF:
                    mEngineOn = false;
                    mNavigating = false;
                    break;
            }
            if ((mNavigating || mEngineOn) && !mWakeLock.isHeld()) {
                if (DEBUG) Log.d(TAG, "Acquiring wakelock");
                 mWakeLock.acquire();
            }
            if (wasNavigating != mNavigating) {
                int size = mListeners.size();
                for (int i = 0; i < size; i++) {
                    Listener listener = mListeners.get(i);
                    try {
                        if (mNavigating) {
                            listener.mListener.onGpsStarted();
                        } else {
                            listener.mListener.onGpsStopped();
                        }
                    } catch (RemoteException e) {
                        Log.w(TAG, "RemoteException in reportStatus");
                        mListeners.remove(listener);
                        size--;
                    }
                }
                try {
                    for (int i=mClientUids.size() - 1; i >= 0; i--) {
                        int uid = mClientUids.keyAt(i);
                        if (mNavigating) {
                            mBatteryStats.noteStartGps(uid);
                        } else {
                            mBatteryStats.noteStopGps(uid);
                        }
                    }
                } catch (RemoteException e) {
                    Log.w(TAG, "RemoteException in reportStatus");
                }
                Intent intent = new Intent(GPS_ENABLED_CHANGE_ACTION);
                intent.putExtra(EXTRA_ENABLED, mNavigating);
                mContext.sendBroadcast(intent);
            }
            if (!mNavigating && !mEngineOn && mWakeLock.isHeld()) {
                if (DEBUG) Log.d(TAG, "Releasing wakelock");
                mWakeLock.release();
            }
        }
    }
    private void reportSvStatus() {
        int svCount = native_read_sv_status(mSvs, mSnrs, mSvElevations, mSvAzimuths, mSvMasks);
        synchronized(mListeners) {
            int size = mListeners.size();
            for (int i = 0; i < size; i++) {
                Listener listener = mListeners.get(i);
                try {
                    listener.mListener.onSvStatusChanged(svCount, mSvs, mSnrs, 
                            mSvElevations, mSvAzimuths, mSvMasks[EPHEMERIS_MASK], 
                            mSvMasks[ALMANAC_MASK], mSvMasks[USED_FOR_FIX_MASK]); 
                } catch (RemoteException e) {
                    Log.w(TAG, "RemoteException in reportSvInfo");
                    mListeners.remove(listener);
                    size--;
                }
            }
        }
        if (VERBOSE) {
            Log.v(TAG, "SV count: " + svCount +
                    " ephemerisMask: " + Integer.toHexString(mSvMasks[EPHEMERIS_MASK]) +
                    " almanacMask: " + Integer.toHexString(mSvMasks[ALMANAC_MASK]));
            for (int i = 0; i < svCount; i++) {
                Log.v(TAG, "sv: " + mSvs[i] +
                        " snr: " + (float)mSnrs[i]/10 +
                        " elev: " + mSvElevations[i] +
                        " azimuth: " + mSvAzimuths[i] +
                        ((mSvMasks[EPHEMERIS_MASK] & (1 << (mSvs[i] - 1))) == 0 ? "  " : " E") +
                        ((mSvMasks[ALMANAC_MASK] & (1 << (mSvs[i] - 1))) == 0 ? "  " : " A") +
                        ((mSvMasks[USED_FOR_FIX_MASK] & (1 << (mSvs[i] - 1))) == 0 ? "" : "U"));
            }
        }
        updateStatus(mStatus, svCount);
        if (mNavigating && mStatus == LocationProvider.AVAILABLE && mLastFixTime > 0 &&
            System.currentTimeMillis() - mLastFixTime > RECENT_FIX_TIMEOUT * 1000) {
            Intent intent = new Intent(GPS_FIX_CHANGE_ACTION);
            intent.putExtra(EXTRA_ENABLED, false);
            mContext.sendBroadcast(intent);
            updateStatus(LocationProvider.TEMPORARILY_UNAVAILABLE, mSvCount);
        }
    }
    private void reportAGpsStatus(int type, int status) {
        switch (status) {
            case GPS_REQUEST_AGPS_DATA_CONN:
                 int result = mConnMgr.startUsingNetworkFeature(
                        ConnectivityManager.TYPE_MOBILE, Phone.FEATURE_ENABLE_SUPL);
                if (result == Phone.APN_ALREADY_ACTIVE) {
                    if (mAGpsApn != null) {
                        native_agps_data_conn_open(mAGpsApn);
                        mAGpsDataConnectionState = AGPS_DATA_CONNECTION_OPEN;
                    } else {
                        Log.e(TAG, "mAGpsApn not set when receiving Phone.APN_ALREADY_ACTIVE");
                        native_agps_data_conn_failed();
                    }
                } else if (result == Phone.APN_REQUEST_STARTED) {
                    mAGpsDataConnectionState = AGPS_DATA_CONNECTION_OPENING;
                } else {
                    native_agps_data_conn_failed();
                }
                break;
            case GPS_RELEASE_AGPS_DATA_CONN:
                if (mAGpsDataConnectionState != AGPS_DATA_CONNECTION_CLOSED) {
                    mConnMgr.stopUsingNetworkFeature(
                            ConnectivityManager.TYPE_MOBILE, Phone.FEATURE_ENABLE_SUPL);
                    native_agps_data_conn_closed();
                    mAGpsDataConnectionState = AGPS_DATA_CONNECTION_CLOSED;
                }
                break;
            case GPS_AGPS_DATA_CONNECTED:
                break;
            case GPS_AGPS_DATA_CONN_DONE:
                break;
            case GPS_AGPS_DATA_CONN_FAILED:
                break;
        }
    }
    private void reportNmea(int index, long timestamp) {
        synchronized(mListeners) {
            int size = mListeners.size();
            if (size > 0) {
                int length = native_read_nmea(index, mNmeaBuffer, mNmeaBuffer.length);
                String nmea = new String(mNmeaBuffer, 0, length);
                for (int i = 0; i < size; i++) {
                    Listener listener = mListeners.get(i);
                    try {
                        listener.mListener.onNmeaReceived(timestamp, nmea);
                    } catch (RemoteException e) {
                        Log.w(TAG, "RemoteException in reportNmea");
                        mListeners.remove(listener);
                        size--;
                    }
                }
            }
        }
    }
    private void xtraDownloadRequest() {
        if (DEBUG) Log.d(TAG, "xtraDownloadRequest");
        mHandler.removeMessages(DOWNLOAD_XTRA_DATA);
        mHandler.sendMessage(Message.obtain(mHandler, DOWNLOAD_XTRA_DATA));
    }
    private final INetInitiatedListener mNetInitiatedListener = new INetInitiatedListener.Stub() {
    	public boolean sendNiResponse(int notificationId, int userResponse)
    	{
    		StringBuilder extrasBuf = new StringBuilder();
    		if (DEBUG) Log.d(TAG, "sendNiResponse, notifId: " + notificationId +
    				", response: " + userResponse);
    		native_send_ni_response(notificationId, userResponse);
    		return true;
    	}        
    };
    public INetInitiatedListener getNetInitiatedListener() {
        return mNetInitiatedListener;
    }
	@SuppressWarnings("deprecation")
	public void reportNiNotification(
        	int notificationId,
        	int niType,
        	int notifyFlags,
        	int timeout,
        	int defaultResponse,
        	String requestorId,
        	String text,
        	int requestorIdEncoding,
        	int textEncoding,
        	String extras  
        )
	{
		Log.i(TAG, "reportNiNotification: entered");
		Log.i(TAG, "notificationId: " + notificationId +
				", niType: " + niType +
				", notifyFlags: " + notifyFlags +
				", timeout: " + timeout +
				", defaultResponse: " + defaultResponse);
		Log.i(TAG, "requestorId: " + requestorId +
				", text: " + text +
				", requestorIdEncoding: " + requestorIdEncoding +
				", textEncoding: " + textEncoding);
		GpsNiNotification notification = new GpsNiNotification();
		notification.notificationId = notificationId;
		notification.niType = niType;
		notification.needNotify = (notifyFlags & GpsNetInitiatedHandler.GPS_NI_NEED_NOTIFY) != 0;
		notification.needVerify = (notifyFlags & GpsNetInitiatedHandler.GPS_NI_NEED_VERIFY) != 0;
		notification.privacyOverride = (notifyFlags & GpsNetInitiatedHandler.GPS_NI_PRIVACY_OVERRIDE) != 0;
		notification.timeout = timeout;
		notification.defaultResponse = defaultResponse;
		notification.requestorId = requestorId;
		notification.text = text;
		notification.requestorIdEncoding = requestorIdEncoding;
		notification.textEncoding = textEncoding;
		Bundle bundle = new Bundle();
		if (extras == null) extras = "";
		Properties extraProp = new Properties();
		try {
			extraProp.load(new StringBufferInputStream(extras));
		}
		catch (IOException e)
		{
			Log.e(TAG, "reportNiNotification cannot parse extras data: " + extras);
		}
		for (Entry<Object, Object> ent : extraProp.entrySet())
		{
			bundle.putString((String) ent.getKey(), (String) ent.getValue());
		}		
		notification.extras = bundle;
		mNIHandler.handleNiNotification(notification);		
	}
    private final class GpsEventThread extends Thread {
        public GpsEventThread() {
            super("GpsEventThread");
        }
        public void run() {
            if (DEBUG) Log.d(TAG, "GpsEventThread starting");
            while (mEnabled) {
                native_wait_for_event();
            }
            if (DEBUG) Log.d(TAG, "GpsEventThread exiting");
        }
    }
    private final class ProviderHandler extends Handler {
        @Override
        public void handleMessage(Message msg)
        {
            switch (msg.what) {
                case ENABLE:
                    if (msg.arg1 == 1) {
                        handleEnable();
                    } else {
                        handleDisable();
                    }
                    break;
                case ENABLE_TRACKING:
                    handleEnableLocationTracking(msg.arg1 == 1);
                    break;
                case UPDATE_NETWORK_STATE:
                    handleUpdateNetworkState(msg.arg1, (NetworkInfo)msg.obj);
                    break;
                case INJECT_NTP_TIME:
                    handleInjectNtpTime();
                    break;
                case DOWNLOAD_XTRA_DATA:
                    if (native_supports_xtra()) {
                        handleDownloadXtraData();
                    }
                    break;
                case UPDATE_LOCATION:
                    handleUpdateLocation((Location)msg.obj);
                    break;
                case ADD_LISTENER:
                    handleAddListener(msg.arg1);
                    break;
                case REMOVE_LISTENER:
                    handleRemoveListener(msg.arg1);
                    break;
            }
        }
    };
    private final class GpsLocationProviderThread extends Thread {
        public GpsLocationProviderThread() {
            super("GpsLocationProvider");
        }
        public void run() {
            Process.setThreadPriority(Process.THREAD_PRIORITY_BACKGROUND);
            initialize();
            Looper.prepare();
            mHandler = new ProviderHandler();
            mInitializedLatch.countDown();
            Looper.loop();
        }
    }
    private static final int MAX_SVS = 32;
    private static final int EPHEMERIS_MASK = 0;
    private static final int ALMANAC_MASK = 1;
    private static final int USED_FOR_FIX_MASK = 2;
    private int mSvs[] = new int[MAX_SVS];
    private float mSnrs[] = new float[MAX_SVS];
    private float mSvElevations[] = new float[MAX_SVS];
    private float mSvAzimuths[] = new float[MAX_SVS];
    private int mSvMasks[] = new int[3];
    private int mSvCount;
    private byte[] mNmeaBuffer = new byte[120];
    static { class_init_native(); }
    private static native void class_init_native();
    private static native boolean native_is_supported();
    private native boolean native_init();
    private native void native_disable();
    private native void native_cleanup();
    private native boolean native_start(int positionMode, boolean singleFix, int fixInterval);
    private native boolean native_stop();
    private native void native_set_fix_frequency(int fixFrequency);
    private native void native_delete_aiding_data(int flags);
    private native void native_wait_for_event();
    private native int native_read_sv_status(int[] svs, float[] snrs,
            float[] elevations, float[] azimuths, int[] masks);
    private native int native_read_nmea(int index, byte[] buffer, int bufferSize);
    private native void native_inject_location(double latitude, double longitude, float accuracy);
    private native void native_inject_time(long time, long timeReference, int uncertainty);
    private native boolean native_supports_xtra();
    private native void native_inject_xtra_data(byte[] data, int length);
    private native String native_get_internal_state();
    private native void native_agps_data_conn_open(String apn);
    private native void native_agps_data_conn_closed();
    private native void native_agps_data_conn_failed();
    private native void native_set_agps_server(int type, String hostname, int port);
    private native void native_send_ni_response(int notificationId, int userResponse);
}
