public class PassiveProvider implements LocationProviderInterface {
    private static final String TAG = "PassiveProvider";
    private final ILocationManager mLocationManager;
    private boolean mTracking;
    public PassiveProvider(ILocationManager locationManager) {
        mLocationManager = locationManager;
    }
    public String getName() {
        return LocationManager.PASSIVE_PROVIDER;
    }
    public boolean requiresNetwork() {
        return false;
    }
    public boolean requiresSatellite() {
        return false;
    }
    public boolean requiresCell() {
        return false;
    }
    public boolean hasMonetaryCost() {
        return false;
    }
    public boolean supportsAltitude() {
        return false;
    }
    public boolean supportsSpeed() {
        return false;
    }
    public boolean supportsBearing() {
        return false;
    }
    public int getPowerRequirement() {
        return -1;
    }
    public int getAccuracy() {
        return -1;
    }
    public boolean isEnabled() {
        return true;
    }
    public void enable() {
    }
    public void disable() {
    }
    public int getStatus(Bundle extras) {
        if (mTracking) {
            return LocationProvider.AVAILABLE;
        } else {
            return LocationProvider.TEMPORARILY_UNAVAILABLE;
        }
    }
    public long getStatusUpdateTime() {
        return -1;
    }
    public String getInternalState() {
        return null;
    }
    public void enableLocationTracking(boolean enable) {
        mTracking = enable;
    }
    public void setMinTime(long minTime) {
    }
    public void updateNetworkState(int state, NetworkInfo info) {
    }
    public void updateLocation(Location location) {
        if (mTracking) {
            try {
                mLocationManager.reportLocation(location, true);
            } catch (RemoteException e) {
                Log.e(TAG, "RemoteException calling reportLocation");
            }
        }
    }
    public boolean sendExtraCommand(String command, Bundle extras) {
        return false;
    }
    public void addListener(int uid) {
    }
    public void removeListener(int uid) {
    }
}
