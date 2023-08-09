public abstract class LocationProvider {
    private static final String TAG = "LocationProvider";
    static final String BAD_CHARS_REGEX = "[^a-zA-Z0-9]";
    private String mName;
    public static final int OUT_OF_SERVICE = 0;
    public static final int TEMPORARILY_UNAVAILABLE = 1;
    public static final int AVAILABLE = 2;
    public LocationProvider(String name) {
        if (name.matches(BAD_CHARS_REGEX)) {
            throw new IllegalArgumentException("name " + name +
                " contains an illegal character");
        }
        mName = name;
    }
    public String getName() {
        return mName;
    }
    public boolean meetsCriteria(Criteria criteria) {
        if (LocationManager.PASSIVE_PROVIDER.equals(mName)) {
            return false;
        }
        if ((criteria.getAccuracy() != Criteria.NO_REQUIREMENT) && 
            (criteria.getAccuracy() < getAccuracy())) {
            return false;
        }
        int criteriaPower = criteria.getPowerRequirement();
        if ((criteriaPower != Criteria.NO_REQUIREMENT) &&
            (criteriaPower < getPowerRequirement())) {
            return false;
        }
        if (criteria.isAltitudeRequired() && !supportsAltitude()) {
            return false;
        }
        if (criteria.isSpeedRequired() && !supportsSpeed()) {
            return false;
        }
        if (criteria.isBearingRequired() && !supportsBearing()) {
            return false;
        }
        return true;
    }
    public abstract boolean requiresNetwork();
    public abstract boolean requiresSatellite();
    public abstract boolean requiresCell();
    public abstract boolean hasMonetaryCost();
    public abstract boolean supportsAltitude();
    public abstract boolean supportsSpeed();
    public abstract boolean supportsBearing();
    public abstract int getPowerRequirement();
    public abstract int getAccuracy();
}
