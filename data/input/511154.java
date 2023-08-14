public abstract class GeocodeProvider {
    private IGeocodeProvider.Stub mProvider = new IGeocodeProvider.Stub() {
        public String getFromLocation(double latitude, double longitude, int maxResults,
                GeocoderParams params, List<Address> addrs) {
            return GeocodeProvider.this.onGetFromLocation(latitude, longitude, maxResults,
                    params, addrs);
        }
        public String getFromLocationName(String locationName,
                double lowerLeftLatitude, double lowerLeftLongitude,
                double upperRightLatitude, double upperRightLongitude, int maxResults,
                GeocoderParams params, List<Address> addrs) {
            return GeocodeProvider.this.onGetFromLocationName(locationName, lowerLeftLatitude,
                    lowerLeftLongitude, upperRightLatitude, upperRightLongitude,
                    maxResults, params, addrs);
        }
    };
    public abstract String onGetFromLocation(double latitude, double longitude, int maxResults,
            GeocoderParams params, List<Address> addrs);
    public abstract String onGetFromLocationName(String locationName,
            double lowerLeftLatitude, double lowerLeftLongitude,
            double upperRightLatitude, double upperRightLongitude, int maxResults,
            GeocoderParams params, List<Address> addrs);
    public IBinder getBinder() {
        return mProvider;
    }
}
