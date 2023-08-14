public class NoLocationPermissionTest extends AndroidTestCase {
    private static final String TEST_PROVIDER_NAME = "testProvider";
    private LocationManager mLocationManager;
    private List<String> mAllProviders;
    @Override
    protected void setUp() throws Exception {
        super.setUp();
        mLocationManager = (LocationManager) getContext().getSystemService(
                Context.LOCATION_SERVICE);
        mAllProviders = mLocationManager.getAllProviders();
        assertNotNull(mLocationManager);
        assertNotNull(mAllProviders);
    }
    private boolean isKnownLocationProvider(String provider) {
        return mAllProviders.contains(provider);
    }
    @SmallTest
    public void testListenCellLocation() {
        TelephonyManager telephonyManager = (TelephonyManager) getContext().getSystemService(
                   Context.TELEPHONY_SERVICE);
        PhoneStateListener phoneStateListener = new PhoneStateListener();
        try {
            telephonyManager.listen(phoneStateListener, PhoneStateListener.LISTEN_CELL_LOCATION);
            fail("TelephonyManager.listen(LISTEN_CELL_LOCATION) did not" +
                    " throw SecurityException as expected");
        } catch (SecurityException e) {
        }
        try {
            telephonyManager.getCellLocation();
            fail("TelephonyManager.getCellLocation did not throw SecurityException as expected");
        } catch (SecurityException e) {
        }
    }
    private void checkRequestLocationUpdates(String provider) {
        if ( !isKnownLocationProvider(provider) ) {
            return;
        }
        LocationListener mockListener = new MockLocationListener();
        Looper looper = Looper.myLooper();
        try {
            mLocationManager.requestLocationUpdates(provider, 0, 0, mockListener);
            fail("LocationManager.requestLocationUpdates did not" +
                    " throw SecurityException as expected");
        } catch (SecurityException e) {
        }
        try {
            mLocationManager.requestLocationUpdates(provider, 0, 0, mockListener, looper);
            fail("LocationManager.requestLocationUpdates did not" +
                    " throw SecurityException as expected");
        } catch (SecurityException e) {
        }
    }
    @SmallTest
    public void testRequestLocationUpdatesNetwork() {
        checkRequestLocationUpdates(LocationManager.NETWORK_PROVIDER);
    }
    @SmallTest
    public void testRequestLocationUpdatesGps() {
        checkRequestLocationUpdates(LocationManager.GPS_PROVIDER);
    }
    @SmallTest
    public void testAddProximityAlert() {
        PendingIntent mockPendingIntent = PendingIntent.getBroadcast(getContext(),
                0, new Intent("mockIntent"), PendingIntent.FLAG_ONE_SHOT);
        try {
            mLocationManager.addProximityAlert(0, 0, 100, -1, mockPendingIntent);
            fail("LocationManager.addProximityAlert did not throw SecurityException as expected");
        } catch (SecurityException e) {
        }
    }
    private void checkGetLastKnownLocation(String provider) {
        if ( !isKnownLocationProvider(provider) ) {
            return;
        }
        try {
            mLocationManager.getLastKnownLocation(provider);
            fail("LocationManager.getLastKnownLocation did not" +
                    " throw SecurityException as expected");
        } catch (SecurityException e) {
        }
    }
    @SmallTest
    public void testGetLastKnownLocationGps() {
        checkGetLastKnownLocation(LocationManager.GPS_PROVIDER);
    }
    @SmallTest
    public void testGetLastKnownLocationNetwork() {
        checkGetLastKnownLocation(LocationManager.NETWORK_PROVIDER);
    }
    private void checkGetProvider(String provider) {
        if ( !isKnownLocationProvider(provider) ) {
            return;
        }
        try {
            mLocationManager.getProvider(provider);
            fail("LocationManager.getProvider did not throw SecurityException as expected");
        } catch (SecurityException e) {
        }
    }
    @SmallTest
    public void testGetProviderGps() {
        checkGetProvider(LocationManager.GPS_PROVIDER);
    }
    public void testGetProviderNetwork() {
        checkGetProvider(LocationManager.NETWORK_PROVIDER);
    }
    private void checkIsProviderEnabled(String provider) {
        if ( !isKnownLocationProvider(provider) ) {
            return;
        }
        try {
            mLocationManager.isProviderEnabled(provider);
            fail("LocationManager.isProviderEnabled did not throw SecurityException as expected");
        } catch (SecurityException e) {
        }
    }
    @SmallTest
    public void testIsProviderEnabledGps() {
        checkIsProviderEnabled(LocationManager.GPS_PROVIDER);
    }
    @SmallTest
    public void testIsProviderEnabledNetwork() {
        checkIsProviderEnabled(LocationManager.NETWORK_PROVIDER);
    }
    @SmallTest
    public void testAddTestProvider() {
        final int TEST_POWER_REQUIREMENT_VALE = 0;
        final int TEST_ACCURACY_VALUE = 1;
        try {
            mLocationManager.addTestProvider(TEST_PROVIDER_NAME, true, true, true, true,
                    true, true, true, TEST_POWER_REQUIREMENT_VALE, TEST_ACCURACY_VALUE);
            fail("LocationManager.addTestProvider did not throw SecurityException as expected");
        } catch (SecurityException e) {
        }
    }
    @SmallTest
    public void testRemoveTestProvider() {
        try {
            mLocationManager.removeTestProvider(TEST_PROVIDER_NAME);
            fail("LocationManager.removeTestProvider did not throw SecurityException as"
                    + " expected");
        } catch (SecurityException e) {
        }
    }
    @SmallTest
    public void testSetTestProviderLocation() {
        Location location = new Location(TEST_PROVIDER_NAME);
        try {
            mLocationManager.setTestProviderLocation(TEST_PROVIDER_NAME, location);
            fail("LocationManager.setTestProviderLocation did not throw SecurityException as"
                    + " expected");
        } catch (SecurityException e) {
        }
    }
    @SmallTest
    public void testClearTestProviderLocation() {
        try {
            mLocationManager.clearTestProviderLocation(TEST_PROVIDER_NAME);
            fail("LocationManager.clearTestProviderLocation did not throw SecurityException as"
                    + " expected");
        } catch (SecurityException e) {
        }
    }
    @SmallTest
    public void testSetTestProviderEnabled() {
        try {
            mLocationManager.setTestProviderEnabled(TEST_PROVIDER_NAME, true);
            fail("LocationManager.setTestProviderEnabled did not throw SecurityException as"
                    + " expected");
        } catch (SecurityException e) {
        }
    }
    @SmallTest
    public void testClearTestProviderEnabled() {
        try {
            mLocationManager.clearTestProviderEnabled(TEST_PROVIDER_NAME);
            fail("LocationManager.setTestProviderEnabled did not throw SecurityException as"
                    + " expected");
        } catch (SecurityException e) {
        }
    }
    @SmallTest
    public void testSetTestProviderStatus() {
        try {
            mLocationManager.setTestProviderStatus(TEST_PROVIDER_NAME, 0, Bundle.EMPTY, 0);
            fail("LocationManager.setTestProviderStatus did not throw SecurityException as"
                    + " expected");
        } catch (SecurityException e) {
        }
    }
    @SmallTest
    public void testClearTestProviderStatus() {
        try {
            mLocationManager.clearTestProviderStatus(TEST_PROVIDER_NAME);
            fail("LocationManager.setTestProviderStatus did not throw SecurityException as"
                    + " expected");
        } catch (SecurityException e) {
        }
    }
    private static class MockLocationListener implements LocationListener {
        public void onLocationChanged(Location location) {
        }
        public void onProviderDisabled(String provider) {
        }
        public void onProviderEnabled(String provider) {
        }
        public void onStatusChanged(String provider, int status, Bundle extras) {
        }
    }
}
