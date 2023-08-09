public class ExternalSharedPermsTest extends InstrumentationTestCase
{
    private static final int REQUEST_ENABLE_BT = 2;
    public void testRunLocationAndBluetooth()
    {
        LocationManager locationManager = (LocationManager)getInstrumentation().getContext(
                ).getSystemService(Context.LOCATION_SERVICE);
        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0,
                new LocationListener() {
                        public void onLocationChanged(Location location) {}
                        public void onProviderDisabled(String provider) {}
                        public void onProviderEnabled(String provider) {}
                        public void onStatusChanged(String provider, int status, Bundle extras) {}
                }
        );
        BluetoothAdapter mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if ((mBluetoothAdapter != null) && (!mBluetoothAdapter.isEnabled())) {
            mBluetoothAdapter.getName();
        }
    }
}
