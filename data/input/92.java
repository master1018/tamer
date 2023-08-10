public class GeoPos {
    public static final int GEO_STD_DEVIATION = 1000;
    private static final int GEO_MS2DEGREES_FACTOR = 3600000;
    private static final int GEO_M2N_FACTOR = 1000;
    private int longitude;
    private int latitude;
    private int altitude;
    private int exactitude;
    public GeoPos(int longitude, int latitude, int altitude) {
        this(longitude, latitude, altitude, GeoPos.GEO_STD_DEVIATION);
    }
    public GeoPos(int longitude, int latitude, int altitude, int exactitude) {
        this.longitude = longitude;
        this.latitude = latitude;
        this.altitude = altitude;
        this.exactitude = exactitude;
    }
    public GeoPos(boolean east, Integer longDegrees, Integer longMinutes, Double longSeconds, boolean north, Integer latDegrees, Integer latMinutes, Double latSeconds, Integer altitude, Integer exactitude) {
        int tmpLongMs = longDegrees.intValue() * 3600000;
        tmpLongMs += longMinutes.intValue() * 60000;
        tmpLongMs += longSeconds.doubleValue() * 1000;
        if (east) this.longitude = tmpLongMs; else this.longitude = -tmpLongMs;
        int tmpLatMs = latDegrees.intValue() * 3600000;
        tmpLatMs += latMinutes.intValue() * 60000;
        tmpLatMs += latSeconds.doubleValue() * GeoPos.GEO_M2N_FACTOR;
        if (north) this.latitude = tmpLatMs; else this.latitude = -tmpLatMs;
        this.altitude = altitude.intValue() * GeoPos.GEO_M2N_FACTOR;
        this.exactitude = exactitude.intValue() * GeoPos.GEO_M2N_FACTOR;
    }
    public GeoPos(Double longitude, Double latitude, Integer altitude, Integer exactitude) {
        this.longitude = (int) longitude.doubleValue() * GeoPos.GEO_MS2DEGREES_FACTOR;
        this.latitude = (int) latitude.doubleValue() * GeoPos.GEO_MS2DEGREES_FACTOR;
        this.altitude = altitude.intValue() * GeoPos.GEO_M2N_FACTOR;
        this.exactitude = exactitude.intValue() * GeoPos.GEO_M2N_FACTOR;
    }
    public GeoPos(double longitude, double latitude, double altitude, double exactitude) {
        this.longitude = (int) (longitude * GeoPos.GEO_MS2DEGREES_FACTOR);
        this.latitude = (int) (latitude * GeoPos.GEO_MS2DEGREES_FACTOR);
        this.altitude = (int) (altitude * GeoPos.GEO_M2N_FACTOR);
        this.exactitude = (int) (exactitude * GeoPos.GEO_M2N_FACTOR);
    }
    public Double getWholeLongitudeInDegrees() {
        return new Double(this.longitude / (double) GeoPos.GEO_MS2DEGREES_FACTOR);
    }
    public Double getWholeLatitudeInDegrees() {
        return new Double(this.latitude / (double) GeoPos.GEO_MS2DEGREES_FACTOR);
    }
    public Double getAltitudeInM() {
        return new Double(this.altitude / (double) GeoPos.GEO_M2N_FACTOR);
    }
    public Double getExactitude() {
        return new Double(this.exactitude / (double) GeoPos.GEO_M2N_FACTOR);
    }
    public Integer getLongitudeDegrees() {
        return new Integer(this.longitude / GeoPos.GEO_MS2DEGREES_FACTOR);
    }
    public Integer getLongitudeMinutes() {
        int minutes = this.longitude % GeoPos.GEO_MS2DEGREES_FACTOR;
        return new Integer(minutes / 60 / GeoPos.GEO_M2N_FACTOR);
    }
    public Integer getLongitudeSeconds() {
        int minutes = this.longitude % GeoPos.GEO_MS2DEGREES_FACTOR;
        int seconds = minutes % 60000;
        return new Integer(seconds / GeoPos.GEO_M2N_FACTOR);
    }
    public Integer getLatitudeDegrees() {
        return new Integer(this.latitude / GeoPos.GEO_MS2DEGREES_FACTOR);
    }
    public Integer getLatitudeMinutes() {
        int minutes = this.latitude % GeoPos.GEO_MS2DEGREES_FACTOR;
        return new Integer(minutes / 60 / GeoPos.GEO_M2N_FACTOR);
    }
    public Integer getLatitudeSeconds() {
        int minutes = this.latitude % GeoPos.GEO_MS2DEGREES_FACTOR;
        int seconds = minutes % 60000;
        return new Integer(seconds / GeoPos.GEO_M2N_FACTOR);
    }
    @Override
    public String toString() {
        return "longitude: " + this.longitude + ", latitude: " + this.latitude + ", altitude: " + this.altitude + ", exactitude: " + this.exactitude;
    }
    public String getStringRepresentation() {
        return getLongitudeDegrees().intValue() + "� " + getLongitudeMinutes().intValue() + "' " + getLongitudeSeconds().intValue() + "'' :: " + getLatitudeDegrees().intValue() + "� " + getLatitudeMinutes().intValue() + "' " + getLatitudeSeconds().intValue() + "''";
    }
    public int getLatitude() {
        return this.latitude;
    }
    public int getLongitude() {
        return this.longitude;
    }
    public int getAltitude() {
        return this.altitude;
    }
}
