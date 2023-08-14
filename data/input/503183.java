final class GeolocationService implements LocationListener {
    private static final String TAG = "geolocationService";
    private long mNativeObject;
    private LocationManager mLocationManager;
    private boolean mIsGpsEnabled;
    private boolean mIsRunning;
    private boolean mIsNetworkProviderAvailable;
    private boolean mIsGpsProviderAvailable;
    public GeolocationService(long nativeObject) {
        mNativeObject = nativeObject;
        ActivityThread thread = ActivityThread.systemMain();
        Context context = thread.getApplication();
        mLocationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
        if (mLocationManager == null) {
            Log.e(TAG, "Could not get location manager.");
        }
     }
    public void start() {
        registerForLocationUpdates();
        mIsRunning = true;
    }
    public void stop() {
        unregisterFromLocationUpdates();
        mIsRunning = false;
    }
    public void setEnableGps(boolean enable) {
        if (mIsGpsEnabled != enable) {
            mIsGpsEnabled = enable;
            if (mIsRunning) {
                unregisterFromLocationUpdates();
                registerForLocationUpdates();
            }
        }
    }
    public void onLocationChanged(Location location) {
        if (mIsRunning) {
            nativeNewLocationAvailable(mNativeObject, location);
        }
    }
    public void onStatusChanged(String providerName, int status, Bundle extras) {
        boolean isAvailable = (status == LocationProvider.AVAILABLE);
        if (LocationManager.NETWORK_PROVIDER.equals(providerName)) {
            mIsNetworkProviderAvailable = isAvailable;
        } else if (LocationManager.GPS_PROVIDER.equals(providerName)) {
            mIsGpsProviderAvailable = isAvailable;
        }
        maybeReportError("The last location provider is no longer available");
    }
    public void onProviderEnabled(String providerName) {
        if (LocationManager.NETWORK_PROVIDER.equals(providerName)) {
            mIsNetworkProviderAvailable = true;
        } else if (LocationManager.GPS_PROVIDER.equals(providerName)) {
            mIsGpsProviderAvailable = true;
        }
    }
    public void onProviderDisabled(String providerName) {
        if (LocationManager.NETWORK_PROVIDER.equals(providerName)) {
            mIsNetworkProviderAvailable = false;
        } else if (LocationManager.GPS_PROVIDER.equals(providerName)) {
            mIsGpsProviderAvailable = false;
        }
        maybeReportError("The last location provider was disabled");
    }
    private void registerForLocationUpdates() {
        try {
            mLocationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 0, 0, this);
            mIsNetworkProviderAvailable = true;
            if (mIsGpsEnabled) {
                mLocationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, this);
                mIsGpsProviderAvailable = true;
            }
        } catch(SecurityException e) {
            Log.e(TAG, "Caught security exception registering for location updates from system. " +
                "This should only happen in DumpRenderTree.");
        }
    }
    private void unregisterFromLocationUpdates() {
        mLocationManager.removeUpdates(this);
    }
    private void maybeReportError(String message) {
        if (mIsRunning && !mIsNetworkProviderAvailable && !mIsGpsProviderAvailable) {
            nativeNewErrorAvailable(mNativeObject, message);
        }
    }
    private static native void nativeNewLocationAvailable(long nativeObject, Location location);
    private static native void nativeNewErrorAvailable(long nativeObject, String message);
}
