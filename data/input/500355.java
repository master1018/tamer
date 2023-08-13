public class CdmaCellLocation extends CellLocation {
    private int mBaseStationId = -1;
    public final static int INVALID_LAT_LONG = Integer.MAX_VALUE;
    private int mBaseStationLatitude = INVALID_LAT_LONG;
    private int mBaseStationLongitude = INVALID_LAT_LONG;
    private int mSystemId = -1;
    private int mNetworkId = -1;
    public CdmaCellLocation() {
        this.mBaseStationId = -1;
        this.mBaseStationLatitude = INVALID_LAT_LONG;
        this.mBaseStationLongitude = INVALID_LAT_LONG;
        this.mSystemId = -1;
        this.mNetworkId = -1;
    }
    public CdmaCellLocation(Bundle bundle) {
        this.mBaseStationId = bundle.getInt("baseStationId", mBaseStationId);
        this.mBaseStationLatitude = bundle.getInt("baseStationLatitude", mBaseStationLatitude);
        this.mBaseStationLongitude = bundle.getInt("baseStationLongitude", mBaseStationLongitude);
        this.mSystemId = bundle.getInt("systemId", mSystemId);
        this.mNetworkId = bundle.getInt("networkId", mNetworkId);
    }
    public int getBaseStationId() {
        return this.mBaseStationId;
    }
    public int getBaseStationLatitude() {
        return this.mBaseStationLatitude;
    }
    public int getBaseStationLongitude() {
        return this.mBaseStationLongitude;
    }
    public int getSystemId() {
        return this.mSystemId;
    }
    public int getNetworkId() {
        return this.mNetworkId;
    }
    public void setStateInvalid() {
        this.mBaseStationId = -1;
        this.mBaseStationLatitude = INVALID_LAT_LONG;
        this.mBaseStationLongitude = INVALID_LAT_LONG;
        this.mSystemId = -1;
        this.mNetworkId = -1;
    }
     public void setCellLocationData(int baseStationId, int baseStationLatitude,
         int baseStationLongitude) {
         this.mBaseStationId = baseStationId;
         this.mBaseStationLatitude = baseStationLatitude;   
         this.mBaseStationLongitude = baseStationLongitude; 
    }
     public void setCellLocationData(int baseStationId, int baseStationLatitude,
         int baseStationLongitude, int systemId, int networkId) {
         this.mBaseStationId = baseStationId;
         this.mBaseStationLatitude = baseStationLatitude;   
         this.mBaseStationLongitude = baseStationLongitude; 
         this.mSystemId = systemId;
         this.mNetworkId = networkId;
    }
    @Override
    public int hashCode() {
        return this.mBaseStationId ^ this.mBaseStationLatitude ^ this.mBaseStationLongitude
                ^ this.mSystemId ^ this.mNetworkId;
    }
    @Override
    public boolean equals(Object o) {
        CdmaCellLocation s;
        try {
            s = (CdmaCellLocation)o;
        } catch (ClassCastException ex) {
            return false;
        }
        if (o == null) {
            return false;
        }
        return (equalsHandlesNulls(this.mBaseStationId, s.mBaseStationId) &&
                equalsHandlesNulls(this.mBaseStationLatitude, s.mBaseStationLatitude) &&
                equalsHandlesNulls(this.mBaseStationLongitude, s.mBaseStationLongitude) &&
                equalsHandlesNulls(this.mSystemId, s.mSystemId) &&
                equalsHandlesNulls(this.mNetworkId, s.mNetworkId)
        );
    }
    @Override
    public String toString() {
        return "[" + this.mBaseStationId + ","
                   + this.mBaseStationLatitude + ","
                   + this.mBaseStationLongitude + ","
                   + this.mSystemId + ","
                   + this.mNetworkId + "]";
    }
    private static boolean equalsHandlesNulls(Object a, Object b) {
        return (a == null) ? (b == null) : a.equals (b);
    }
    public void fillInNotifierBundle(Bundle bundleToFill) {
        bundleToFill.putInt("baseStationId", this.mBaseStationId);
        bundleToFill.putInt("baseStationLatitude", this.mBaseStationLatitude);
        bundleToFill.putInt("baseStationLongitude", this.mBaseStationLongitude);
        bundleToFill.putInt("systemId", this.mSystemId);
        bundleToFill.putInt("networkId", this.mNetworkId);
    }
    public boolean isEmpty() {
        return (this.mBaseStationId == -1 &&
                this.mBaseStationLatitude == INVALID_LAT_LONG &&
                this.mBaseStationLongitude == INVALID_LAT_LONG &&
                this.mSystemId == -1 &&
                this.mNetworkId == -1);
    }
}
