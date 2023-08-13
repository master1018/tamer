public class GeocoderProxy {
    private static final String TAG = "GeocoderProxy";
    private final Context mContext;
    private final Intent mIntent;
    private final Connection mServiceConnection = new Connection();
    private IGeocodeProvider mProvider;
    public GeocoderProxy(Context context, String serviceName) {
        mContext = context;
        mIntent = new Intent(serviceName);
        mContext.bindService(mIntent, mServiceConnection, Context.BIND_AUTO_CREATE);
    }
    private class Connection implements ServiceConnection {
        public void onServiceConnected(ComponentName className, IBinder service) {
            Log.d(TAG, "onServiceConnected " + className);
            synchronized (this) {
                mProvider = IGeocodeProvider.Stub.asInterface(service);
            }
        }
        public void onServiceDisconnected(ComponentName className) {
            Log.d(TAG, "onServiceDisconnected " + className);
            synchronized (this) {
                mProvider = null;
            }
        }
    }
    public String getFromLocation(double latitude, double longitude, int maxResults,
            GeocoderParams params, List<Address> addrs) {
        IGeocodeProvider provider;
        synchronized (mServiceConnection) {
            provider = mProvider;
        }
        if (provider != null) {
            try {
                return provider.getFromLocation(latitude, longitude, maxResults,
                        params, addrs);
            } catch (RemoteException e) {
                Log.e(TAG, "getFromLocation failed", e);
            }
        }
        return "Service not Available";
    }
    public String getFromLocationName(String locationName,
            double lowerLeftLatitude, double lowerLeftLongitude,
            double upperRightLatitude, double upperRightLongitude, int maxResults,
            GeocoderParams params, List<Address> addrs) {
        IGeocodeProvider provider;
        synchronized (mServiceConnection) {
            provider = mProvider;
        }
        if (provider != null) {
            try {
                return provider.getFromLocationName(locationName, lowerLeftLatitude,
                        lowerLeftLongitude, upperRightLatitude, upperRightLongitude,
                        maxResults, params, addrs);
            } catch (RemoteException e) {
                Log.e(TAG, "getFromLocationName failed", e);
            }
        }
        return "Service not Available";
    }
}
