public final class Geocoder {
    private static final String TAG = "Geocoder";
    private GeocoderParams mParams;
    private ILocationManager mService;
    public Geocoder(Context context, Locale locale) {
        if (locale == null) {
            throw new NullPointerException("locale == null");
        }
        mParams = new GeocoderParams(context, locale);
        IBinder b = ServiceManager.getService(Context.LOCATION_SERVICE);
        mService = ILocationManager.Stub.asInterface(b);
    }
    public Geocoder(Context context) {
        this(context, Locale.getDefault());
    }
    public List<Address> getFromLocation(double latitude, double longitude, int maxResults)
        throws IOException {
        if (latitude < -90.0 || latitude > 90.0) {
            throw new IllegalArgumentException("latitude == " + latitude);
        }
        if (longitude < -180.0 || longitude > 180.0) {
            throw new IllegalArgumentException("longitude == " + longitude);
        }
        try {
            List<Address> results = new ArrayList<Address>();
            String ex =  mService.getFromLocation(latitude, longitude, maxResults,
                mParams, results);
            if (ex != null) {
                throw new IOException(ex);
            } else {
                return results;
            }
        } catch (RemoteException e) {
            Log.e(TAG, "getFromLocation: got RemoteException", e);
            return null;
        }
    }
    public List<Address> getFromLocationName(String locationName, int maxResults) throws IOException {
        if (locationName == null) {
            throw new IllegalArgumentException("locationName == null");
        }
        try {
            List<Address> results = new ArrayList<Address>();
            String ex = mService.getFromLocationName(locationName,
                0, 0, 0, 0, maxResults, mParams, results);
            if (ex != null) {
                throw new IOException(ex);
            } else {
                return results;
            }
        } catch (RemoteException e) {
            Log.e(TAG, "getFromLocationName: got RemoteException", e);
            return null;
        }
    }
    public List<Address> getFromLocationName(String locationName, int maxResults,
        double lowerLeftLatitude, double lowerLeftLongitude,
        double upperRightLatitude, double upperRightLongitude) throws IOException {
        if (locationName == null) {
            throw new IllegalArgumentException("locationName == null");
        }
        if (lowerLeftLatitude < -90.0 || lowerLeftLatitude > 90.0) {
            throw new IllegalArgumentException("lowerLeftLatitude == "
                + lowerLeftLatitude);
        }
        if (lowerLeftLongitude < -180.0 || lowerLeftLongitude > 180.0) {
            throw new IllegalArgumentException("lowerLeftLongitude == "
                + lowerLeftLongitude);
        }
        if (upperRightLatitude < -90.0 || upperRightLatitude > 90.0) {
            throw new IllegalArgumentException("upperRightLatitude == "
                + upperRightLatitude);
        }
        if (upperRightLongitude < -180.0 || upperRightLongitude > 180.0) {
            throw new IllegalArgumentException("upperRightLongitude == "
                + upperRightLongitude);
        }
        try {
            ArrayList<Address> result = new ArrayList<Address>();
            String ex =  mService.getFromLocationName(locationName,
                lowerLeftLatitude, lowerLeftLongitude, upperRightLatitude, upperRightLongitude,
                maxResults, mParams, result);
            if (ex != null) {
                throw new IOException(ex);
            } else {
                return result;
            }
        } catch (RemoteException e) {
            Log.e(TAG, "getFromLocationName: got RemoteException", e);
            return null;
        }
    }
}
