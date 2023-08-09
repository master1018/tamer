public class LocationManager {
    private static final String TAG = "LocationManager";
    private ILocationManager mService;
    private final HashMap<GpsStatus.Listener, GpsStatusListenerTransport> mGpsStatusListeners =
            new HashMap<GpsStatus.Listener, GpsStatusListenerTransport>();
    private final HashMap<GpsStatus.NmeaListener, GpsStatusListenerTransport> mNmeaListeners =
            new HashMap<GpsStatus.NmeaListener, GpsStatusListenerTransport>();
    private final GpsStatus mGpsStatus = new GpsStatus();
    public static final String NETWORK_PROVIDER = "network";
    public static final String GPS_PROVIDER = "gps";
    public static final String PASSIVE_PROVIDER = "passive";
    public static final String KEY_PROXIMITY_ENTERING = "entering";
    public static final String KEY_STATUS_CHANGED = "status";
    public static final String KEY_PROVIDER_ENABLED = "providerEnabled";
    public static final String KEY_LOCATION_CHANGED = "location";
    private HashMap<LocationListener,ListenerTransport> mListeners =
        new HashMap<LocationListener,ListenerTransport>();
    private class ListenerTransport extends ILocationListener.Stub {
        private static final int TYPE_LOCATION_CHANGED = 1;
        private static final int TYPE_STATUS_CHANGED = 2;
        private static final int TYPE_PROVIDER_ENABLED = 3;
        private static final int TYPE_PROVIDER_DISABLED = 4;
        private LocationListener mListener;
        private final Handler mListenerHandler;
        ListenerTransport(LocationListener listener, Looper looper) {
            mListener = listener;
            if (looper == null) {
                mListenerHandler = new Handler() {
                    @Override
                    public void handleMessage(Message msg) {
                        _handleMessage(msg);
                    }
                };
            } else {
                mListenerHandler = new Handler(looper) {
                    @Override
                    public void handleMessage(Message msg) {
                        _handleMessage(msg);
                    }
                };
            }
        }
        public void onLocationChanged(Location location) {
            Message msg = Message.obtain();
            msg.what = TYPE_LOCATION_CHANGED;
            msg.obj = location;
            mListenerHandler.sendMessage(msg);
        }
        public void onStatusChanged(String provider, int status, Bundle extras) {
            Message msg = Message.obtain();
            msg.what = TYPE_STATUS_CHANGED;
            Bundle b = new Bundle();
            b.putString("provider", provider);
            b.putInt("status", status);
            if (extras != null) {
                b.putBundle("extras", extras);
            }
            msg.obj = b;
            mListenerHandler.sendMessage(msg);
        }
        public void onProviderEnabled(String provider) {
            Message msg = Message.obtain();
            msg.what = TYPE_PROVIDER_ENABLED;
            msg.obj = provider;
            mListenerHandler.sendMessage(msg);
        }
        public void onProviderDisabled(String provider) {
            Message msg = Message.obtain();
            msg.what = TYPE_PROVIDER_DISABLED;
            msg.obj = provider;
            mListenerHandler.sendMessage(msg);
        }
        private void _handleMessage(Message msg) {
            switch (msg.what) {
                case TYPE_LOCATION_CHANGED:
                    Location location = new Location((Location) msg.obj);
                    mListener.onLocationChanged(location);
                    break;
                case TYPE_STATUS_CHANGED:
                    Bundle b = (Bundle) msg.obj;
                    String provider = b.getString("provider");
                    int status = b.getInt("status");
                    Bundle extras = b.getBundle("extras");
                    mListener.onStatusChanged(provider, status, extras);
                    break;
                case TYPE_PROVIDER_ENABLED:
                    mListener.onProviderEnabled((String) msg.obj);
                    break;
                case TYPE_PROVIDER_DISABLED:
                    mListener.onProviderDisabled((String) msg.obj);
                    break;
            }
            try {
                mService.locationCallbackFinished(this);
            } catch (RemoteException e) {
                Log.e(TAG, "locationCallbackFinished: RemoteException", e);
            }
        }
    }
    public LocationManager(ILocationManager service) {
        if (false) {
            Log.d(TAG, "Constructor: service = " + service);
        }
        mService = service;
    }
    private LocationProvider createProvider(String name, Bundle info) {
        DummyLocationProvider provider =
            new DummyLocationProvider(name);
        provider.setRequiresNetwork(info.getBoolean("network"));
        provider.setRequiresSatellite(info.getBoolean("satellite"));
        provider.setRequiresCell(info.getBoolean("cell"));
        provider.setHasMonetaryCost(info.getBoolean("cost"));
        provider.setSupportsAltitude(info.getBoolean("altitude"));
        provider.setSupportsSpeed(info.getBoolean("speed"));
        provider.setSupportsBearing(info.getBoolean("bearing"));
        provider.setPowerRequirement(info.getInt("power"));
        provider.setAccuracy(info.getInt("accuracy"));
        return provider;
    }
    public List<String> getAllProviders() {
        if (false) {
            Log.d(TAG, "getAllProviders");
        }
        try {
            return mService.getAllProviders();
        } catch (RemoteException ex) {
            Log.e(TAG, "getAllProviders: RemoteException", ex);
        }
        return null;
    }
    public List<String> getProviders(boolean enabledOnly) {
        try {
            return mService.getProviders(enabledOnly);
        } catch (RemoteException ex) {
            Log.e(TAG, "getProviders: RemoteException", ex);
        }
        return null;
    }
    public LocationProvider getProvider(String name) {
        if (name == null) {
            throw new IllegalArgumentException("name==null");
        }
        try {
            Bundle info = mService.getProviderInfo(name);
            if (info == null) {
                return null;
            }
            return createProvider(name, info);
        } catch (RemoteException ex) {
            Log.e(TAG, "getProvider: RemoteException", ex);
        }
        return null;
    }
    public List<String> getProviders(Criteria criteria, boolean enabledOnly) {
        List<String> goodProviders = Collections.emptyList();
        List<String> providers = getProviders(enabledOnly);
        for (String providerName : providers) {
            LocationProvider provider = getProvider(providerName);
            if (provider != null && provider.meetsCriteria(criteria)) {
                if (goodProviders.isEmpty()) {
                    goodProviders = new ArrayList<String>();
                }
                goodProviders.add(providerName);
            }
        }
        return goodProviders;
    }
    private int nextPower(int power) {
        switch (power) {
        case Criteria.POWER_LOW:
            return Criteria.POWER_MEDIUM;
        case Criteria.POWER_MEDIUM:
            return Criteria.POWER_HIGH;
        case Criteria.POWER_HIGH:
            return Criteria.NO_REQUIREMENT;
        case Criteria.NO_REQUIREMENT:
        default:
            return Criteria.NO_REQUIREMENT;
        }
    }
    private int nextAccuracy(int accuracy) {
        if (accuracy == Criteria.ACCURACY_FINE) {
            return Criteria.ACCURACY_COARSE;
        } else {
            return Criteria.NO_REQUIREMENT;
        }
    }
    private abstract class LpComparator implements Comparator<LocationProvider> {
        public int compare(int a1, int a2) {
            if (a1 < a2) {
                return -1;
            } else if (a1 > a2) {
                return 1;
            } else {
                return 0;
            }
        }
        public int compare(float a1, float a2) {
            if (a1 < a2) {
                return -1;
            } else if (a1 > a2) {
                return 1;
            } else {
                return 0;
            }
        }
    }
    private class LpPowerComparator extends LpComparator {
        public int compare(LocationProvider l1, LocationProvider l2) {
            int a1 = l1.getPowerRequirement();
            int a2 = l2.getPowerRequirement();
            return compare(a1, a2); 
         }
         public boolean equals(LocationProvider l1, LocationProvider l2) {
             int a1 = l1.getPowerRequirement();
             int a2 = l2.getPowerRequirement();
             return a1 == a2;
         }
    }
    private class LpAccuracyComparator extends LpComparator {
        public int compare(LocationProvider l1, LocationProvider l2) {
            int a1 = l1.getAccuracy();
            int a2 = l2.getAccuracy();
            return compare(a1, a2); 
         }
         public boolean equals(LocationProvider l1, LocationProvider l2) {
             int a1 = l1.getAccuracy();
             int a2 = l2.getAccuracy();
             return a1 == a2;
         }
    }
    private class LpCapabilityComparator extends LpComparator {
        private static final int ALTITUDE_SCORE = 4;
        private static final int BEARING_SCORE = 4;
        private static final int SPEED_SCORE = 4;
        private int score(LocationProvider p) {
            return (p.supportsAltitude() ? ALTITUDE_SCORE : 0) +
                (p.supportsBearing() ? BEARING_SCORE : 0) +
                (p.supportsSpeed() ? SPEED_SCORE : 0);
        }
        public int compare(LocationProvider l1, LocationProvider l2) {
            int a1 = score(l1);
            int a2 = score(l2);
            return compare(-a1, -a2); 
         }
         public boolean equals(LocationProvider l1, LocationProvider l2) {
             int a1 = score(l1);
             int a2 = score(l2);
             return a1 == a2;
         }
    }
    private LocationProvider best(List<String> providerNames) {
        List<LocationProvider> providers = new ArrayList<LocationProvider>(providerNames.size());
        for (String name : providerNames) {
            providers.add(getProvider(name));
        }
        if (providers.size() < 2) {
            return providers.get(0);
        }
        Collections.sort(providers, new LpPowerComparator());
        int power = providers.get(0).getPowerRequirement();
        if (power < providers.get(1).getPowerRequirement()) {
            return providers.get(0);
        }
        int idx, size;
        List<LocationProvider> tmp = new ArrayList<LocationProvider>();
        idx = 0;
        size = providers.size();
        while ((idx < size) && (providers.get(idx).getPowerRequirement() == power)) {
            tmp.add(providers.get(idx));
            idx++;
        }
        Collections.sort(tmp, new LpAccuracyComparator());
        int acc = tmp.get(0).getAccuracy();
        if (acc < tmp.get(1).getAccuracy()) {
            return tmp.get(0);
        }
        List<LocationProvider> tmp2 = new ArrayList<LocationProvider>();
        idx = 0;
        size = tmp.size();
        while ((idx < size) && (tmp.get(idx).getAccuracy() == acc)) {
            tmp2.add(tmp.get(idx));
            idx++;
        }
        Collections.sort(tmp2, new LpCapabilityComparator());
        return tmp2.get(0);
    }
    public String getBestProvider(Criteria criteria, boolean enabledOnly) {
        List<String> goodProviders = getProviders(criteria, enabledOnly);
        if (!goodProviders.isEmpty()) {
            return best(goodProviders).getName();
        }
        criteria = new Criteria(criteria);
        int power = criteria.getPowerRequirement();
        while (goodProviders.isEmpty() && (power != Criteria.NO_REQUIREMENT)) {
            power = nextPower(power);
            criteria.setPowerRequirement(power);
            goodProviders = getProviders(criteria, enabledOnly);
        }
        if (!goodProviders.isEmpty()) {
            return best(goodProviders).getName();
        }
        int accuracy = criteria.getAccuracy();
        while (goodProviders.isEmpty() && (accuracy != Criteria.NO_REQUIREMENT)) {
            accuracy = nextAccuracy(accuracy);
            criteria.setAccuracy(accuracy);
            goodProviders = getProviders(criteria, enabledOnly);
        }
        if (!goodProviders.isEmpty()) {
            return best(goodProviders).getName();
        }
        criteria.setBearingRequired(false);
        goodProviders = getProviders(criteria, enabledOnly);
        if (!goodProviders.isEmpty()) {
            return best(goodProviders).getName();
        }
        criteria.setSpeedRequired(false);
        goodProviders = getProviders(criteria, enabledOnly);
        if (!goodProviders.isEmpty()) {
            return best(goodProviders).getName();
        }
        criteria.setAltitudeRequired(false);
        goodProviders = getProviders(criteria, enabledOnly);
        if (!goodProviders.isEmpty()) {
            return best(goodProviders).getName();
        }
        return null;
    }
    public void requestLocationUpdates(String provider,
        long minTime, float minDistance, LocationListener listener) {
        if (provider == null) {
            throw new IllegalArgumentException("provider==null");
        }
        if (listener == null) {
            throw new IllegalArgumentException("listener==null");
        }
        _requestLocationUpdates(provider, minTime, minDistance, listener, null);
    }
    public void requestLocationUpdates(String provider,
        long minTime, float minDistance, LocationListener listener,
        Looper looper) {
        if (provider == null) {
            throw new IllegalArgumentException("provider==null");
        }
        if (listener == null) {
            throw new IllegalArgumentException("listener==null");
        }
        if (looper == null) {
            throw new IllegalArgumentException("looper==null");
        }
        _requestLocationUpdates(provider, minTime, minDistance, listener, looper);
    }
    private void _requestLocationUpdates(String provider,
        long minTime, float minDistance, LocationListener listener,
        Looper looper) {
        if (minTime < 0L) {
            minTime = 0L;
        }
        if (minDistance < 0.0f) {
            minDistance = 0.0f;
        }
        try {
            synchronized (mListeners) {
                ListenerTransport transport = mListeners.get(listener);
                if (transport == null) {
                    transport = new ListenerTransport(listener, looper);
                }
                mListeners.put(listener, transport);
                mService.requestLocationUpdates(provider, minTime, minDistance, transport);
            }
        } catch (RemoteException ex) {
            Log.e(TAG, "requestLocationUpdates: DeadObjectException", ex);
        }
    }
    public void requestLocationUpdates(String provider,
            long minTime, float minDistance, PendingIntent intent) {
        if (provider == null) {
            throw new IllegalArgumentException("provider==null");
        }
        if (intent == null) {
            throw new IllegalArgumentException("intent==null");
        }
        _requestLocationUpdates(provider, minTime, minDistance, intent);
    }
    private void _requestLocationUpdates(String provider,
            long minTime, float minDistance, PendingIntent intent) {
        if (minTime < 0L) {
            minTime = 0L;
        }
        if (minDistance < 0.0f) {
            minDistance = 0.0f;
        }
        try {
            mService.requestLocationUpdatesPI(provider, minTime, minDistance, intent);
        } catch (RemoteException ex) {
            Log.e(TAG, "requestLocationUpdates: RemoteException", ex);
        }
    }
    public void removeUpdates(LocationListener listener) {
        if (listener == null) {
            throw new IllegalArgumentException("listener==null");
        }
        if (false) {
            Log.d(TAG, "removeUpdates: listener = " + listener);
        }
        try {
            ListenerTransport transport = mListeners.remove(listener);
            if (transport != null) {
                mService.removeUpdates(transport);
            }
        } catch (RemoteException ex) {
            Log.e(TAG, "removeUpdates: DeadObjectException", ex);
        }
    }
    public void removeUpdates(PendingIntent intent) {
        if (intent == null) {
            throw new IllegalArgumentException("intent==null");
        }
        if (false) {
            Log.d(TAG, "removeUpdates: intent = " + intent);
        }
        try {
            mService.removeUpdatesPI(intent);
        } catch (RemoteException ex) {
            Log.e(TAG, "removeUpdates: RemoteException", ex);
        }
    }
    public void addProximityAlert(double latitude, double longitude,
        float radius, long expiration, PendingIntent intent) {
        if (false) {
            Log.d(TAG, "addProximityAlert: latitude = " + latitude +
                ", longitude = " + longitude + ", radius = " + radius +
                ", expiration = " + expiration +
                ", intent = " + intent);
        }
        try {
            mService.addProximityAlert(latitude, longitude, radius,
                                       expiration, intent);
        } catch (RemoteException ex) {
            Log.e(TAG, "addProximityAlert: RemoteException", ex);
        }
    }
    public void removeProximityAlert(PendingIntent intent) {
        if (false) {
            Log.d(TAG, "removeProximityAlert: intent = " + intent);
        }
        try {
            mService.removeProximityAlert(intent);
        } catch (RemoteException ex) {
            Log.e(TAG, "removeProximityAlert: RemoteException", ex);
        }
    }
    public boolean isProviderEnabled(String provider) {
        if (provider == null) {
            throw new IllegalArgumentException("provider==null");
        }
        try {
            return mService.isProviderEnabled(provider);
        } catch (RemoteException ex) {
            Log.e(TAG, "isProviderEnabled: RemoteException", ex);
            return false;
        }
    }
    public Location getLastKnownLocation(String provider) {
        if (provider == null) {
            throw new IllegalArgumentException("provider==null");
        }
        try {
            return mService.getLastKnownLocation(provider);
        } catch (RemoteException ex) {
            Log.e(TAG, "getLastKnowLocation: RemoteException", ex);
            return null;
        }
    }
    public void addTestProvider(String name, boolean requiresNetwork, boolean requiresSatellite,
        boolean requiresCell, boolean hasMonetaryCost, boolean supportsAltitude,
        boolean supportsSpeed, boolean supportsBearing, int powerRequirement, int accuracy) {
        try {
            mService.addTestProvider(name, requiresNetwork, requiresSatellite, requiresCell,
                hasMonetaryCost, supportsAltitude, supportsSpeed, supportsBearing, powerRequirement,
                accuracy);
        } catch (RemoteException ex) {
            Log.e(TAG, "addTestProvider: RemoteException", ex);
        }
    }
    public void removeTestProvider(String provider) {
        try {
            mService.removeTestProvider(provider);
        } catch (RemoteException ex) {
            Log.e(TAG, "removeTestProvider: RemoteException", ex);
        }
    }
    public void setTestProviderLocation(String provider, Location loc) {
        try {
            mService.setTestProviderLocation(provider, loc);
        } catch (RemoteException ex) {
            Log.e(TAG, "setTestProviderLocation: RemoteException", ex);
        }
    }
    public void clearTestProviderLocation(String provider) {
        try {
            mService.clearTestProviderLocation(provider);
        } catch (RemoteException ex) {
            Log.e(TAG, "clearTestProviderLocation: RemoteException", ex);
        }
    }
    public void setTestProviderEnabled(String provider, boolean enabled) {
        try {
            mService.setTestProviderEnabled(provider, enabled);
        } catch (RemoteException ex) {
            Log.e(TAG, "setTestProviderEnabled: RemoteException", ex);
        }
    }
    public void clearTestProviderEnabled(String provider) {
        try {
            mService.clearTestProviderEnabled(provider);
        } catch (RemoteException ex) {
            Log.e(TAG, "clearTestProviderEnabled: RemoteException", ex);
        }
    }
    public void setTestProviderStatus(String provider, int status, Bundle extras, long updateTime) {
        try {
            mService.setTestProviderStatus(provider, status, extras, updateTime);
        } catch (RemoteException ex) {
            Log.e(TAG, "setTestProviderStatus: RemoteException", ex);
        }
    }
    public void clearTestProviderStatus(String provider) {
        try {
            mService.clearTestProviderStatus(provider);
        } catch (RemoteException ex) {
            Log.e(TAG, "clearTestProviderStatus: RemoteException", ex);
        }
    }
    private class GpsStatusListenerTransport extends IGpsStatusListener.Stub {
        private final GpsStatus.Listener mListener;
        private final GpsStatus.NmeaListener mNmeaListener;
        private static final int NMEA_RECEIVED = 1000;
        private class Nmea {
            long mTimestamp;
            String mNmea;
            Nmea(long timestamp, String nmea) {
                mTimestamp = timestamp;
                mNmea = nmea;
            }
        }
        private ArrayList<Nmea> mNmeaBuffer;
        GpsStatusListenerTransport(GpsStatus.Listener listener) {
            mListener = listener;
            mNmeaListener = null;
        }
        GpsStatusListenerTransport(GpsStatus.NmeaListener listener) {
            mNmeaListener = listener;
            mListener = null;
            mNmeaBuffer = new ArrayList<Nmea>();
        }
        public void onGpsStarted() {
            if (mListener != null) {
                Message msg = Message.obtain();
                msg.what = GpsStatus.GPS_EVENT_STARTED;
                mGpsHandler.sendMessage(msg);
            }
        }
        public void onGpsStopped() {
            if (mListener != null) {
                Message msg = Message.obtain();
                msg.what = GpsStatus.GPS_EVENT_STOPPED;
                mGpsHandler.sendMessage(msg);
            }
        }
        public void onFirstFix(int ttff) {
            if (mListener != null) {
                mGpsStatus.setTimeToFirstFix(ttff);
                Message msg = Message.obtain();
                msg.what = GpsStatus.GPS_EVENT_FIRST_FIX;
                mGpsHandler.sendMessage(msg);
            }
        }
        public void onSvStatusChanged(int svCount, int[] prns, float[] snrs,
                float[] elevations, float[] azimuths, int ephemerisMask,
                int almanacMask, int usedInFixMask) {
            if (mListener != null) {
                mGpsStatus.setStatus(svCount, prns, snrs, elevations, azimuths,
                        ephemerisMask, almanacMask, usedInFixMask);
                Message msg = Message.obtain();
                msg.what = GpsStatus.GPS_EVENT_SATELLITE_STATUS;
                mGpsHandler.removeMessages(GpsStatus.GPS_EVENT_SATELLITE_STATUS);
                mGpsHandler.sendMessage(msg);
            }
        }
        public void onNmeaReceived(long timestamp, String nmea) {
            if (mNmeaListener != null) {
                synchronized (mNmeaBuffer) {
                    mNmeaBuffer.add(new Nmea(timestamp, nmea));
                }
                Message msg = Message.obtain();
                msg.what = NMEA_RECEIVED;
                mGpsHandler.removeMessages(NMEA_RECEIVED);
                mGpsHandler.sendMessage(msg);
            }
        }
        private final Handler mGpsHandler = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                if (msg.what == NMEA_RECEIVED) {
                    synchronized (mNmeaBuffer) {
                        int length = mNmeaBuffer.size();
                        for (int i = 0; i < length; i++) {
                            Nmea nmea = mNmeaBuffer.get(i);
                            mNmeaListener.onNmeaReceived(nmea.mTimestamp, nmea.mNmea);
                        }
                        mNmeaBuffer.clear();
                    }
                } else {
                    synchronized(mGpsStatus) {
                        mListener.onGpsStatusChanged(msg.what);
                    }
                }
            }
        };
    }
    public boolean addGpsStatusListener(GpsStatus.Listener listener) {
        boolean result;
        if (mGpsStatusListeners.get(listener) != null) {
            return true;
        }
        try {
            GpsStatusListenerTransport transport = new GpsStatusListenerTransport(listener);
            result = mService.addGpsStatusListener(transport);
            if (result) {
                mGpsStatusListeners.put(listener, transport);
            }
        } catch (RemoteException e) {
            Log.e(TAG, "RemoteException in registerGpsStatusListener: ", e);
            result = false;
        }
        return result;
    }
    public void removeGpsStatusListener(GpsStatus.Listener listener) {
        try {
            GpsStatusListenerTransport transport = mGpsStatusListeners.remove(listener);
            if (transport != null) {
                mService.removeGpsStatusListener(transport);
            }
        } catch (RemoteException e) {
            Log.e(TAG, "RemoteException in unregisterGpsStatusListener: ", e);
        }
    }
    public boolean addNmeaListener(GpsStatus.NmeaListener listener) {
        boolean result;
        if (mNmeaListeners.get(listener) != null) {
            return true;
        }
        try {
            GpsStatusListenerTransport transport = new GpsStatusListenerTransport(listener);
            result = mService.addGpsStatusListener(transport);
            if (result) {
                mNmeaListeners.put(listener, transport);
            }
        } catch (RemoteException e) {
            Log.e(TAG, "RemoteException in registerGpsStatusListener: ", e);
            result = false;
        }
        return result;
    }
    public void removeNmeaListener(GpsStatus.NmeaListener listener) {
        try {
            GpsStatusListenerTransport transport = mNmeaListeners.remove(listener);
            if (transport != null) {
                mService.removeGpsStatusListener(transport);
            }
        } catch (RemoteException e) {
            Log.e(TAG, "RemoteException in unregisterGpsStatusListener: ", e);
        }
    }
    public GpsStatus getGpsStatus(GpsStatus status) {
        if (status == null) {
            status = new GpsStatus();
       }
       status.setStatus(mGpsStatus);
       return status;
    }
    public boolean sendExtraCommand(String provider, String command, Bundle extras) {
        try {
            return mService.sendExtraCommand(provider, command, extras);
        } catch (RemoteException e) {
            Log.e(TAG, "RemoteException in sendExtraCommand: ", e);
            return false;
        }
    }
    public boolean sendNiResponse(int notifId, int userResponse) {
    	try {
            return mService.sendNiResponse(notifId, userResponse);
        } catch (RemoteException e) {
            Log.e(TAG, "RemoteException in sendNiResponse: ", e);
            return false;
        }
    }
}
