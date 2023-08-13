public class DummyLocationProvider extends LocationProvider {
    private static final String TAG = "DummyLocationProvider";
    String mName;
    boolean mRequiresNetwork;
    boolean mRequiresSatellite;
    boolean mRequiresCell;
    boolean mHasMonetaryCost;
    boolean mSupportsAltitude;
    boolean mSupportsSpeed;
    boolean mSupportsBearing;
    int mPowerRequirement;
    int mAccuracy;
    public DummyLocationProvider(String name) {
        super(name);
    }
    public void setRequiresNetwork(boolean requiresNetwork) {
        mRequiresNetwork = requiresNetwork;
    }
    public void setRequiresSatellite(boolean requiresSatellite) {
        mRequiresSatellite = requiresSatellite;
    }
    public void setRequiresCell(boolean requiresCell) {
        mRequiresCell = requiresCell;
    }
    public void setHasMonetaryCost(boolean hasMonetaryCost) {
        mHasMonetaryCost = hasMonetaryCost;
    }
    public void setSupportsAltitude(boolean supportsAltitude) {
        mSupportsAltitude = supportsAltitude;
    }
    public void setSupportsSpeed(boolean supportsSpeed) {
        mSupportsSpeed = supportsSpeed;
    }
    public void setSupportsBearing(boolean supportsBearing) {
        mSupportsBearing = supportsBearing;
    }
    public void setPowerRequirement(int powerRequirement) {
        mPowerRequirement = powerRequirement;
    }
    public void setAccuracy(int accuracy) {
        mAccuracy = accuracy;
    }
    public boolean requiresNetwork() {
        return mRequiresNetwork;
    }
    public boolean requiresSatellite() {
        return mRequiresSatellite;
    }
    public boolean requiresCell() {
        return mRequiresCell;
    }
    public boolean hasMonetaryCost() {
        return mHasMonetaryCost;
    }
    public boolean supportsAltitude() {
        return mSupportsAltitude;
    }
    public boolean supportsSpeed() {
        return mSupportsSpeed;
    }
    public boolean supportsBearing() {
        return mSupportsBearing;
    }
    public int getPowerRequirement() {
        return mPowerRequirement;
    }
    public int getAccuracy() {
        return mAccuracy;
    }
}
