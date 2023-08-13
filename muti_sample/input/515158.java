@TestTargetClass(LocationProvider.class)
public class LocationProviderTest extends AndroidTestCase {
    private LocationManager mLocationManager;
    @Override
    protected void setUp() throws Exception {
        super.setUp();
        mLocationManager =
            (LocationManager) getContext().getSystemService(Context.LOCATION_SERVICE);
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        method = "getName",
        args = {}
    )
    public void testGetName() {
        String name = "gps";
        LocationProvider locationProvider = mLocationManager.getProvider(name);
        assertEquals(name, locationProvider.getName());
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        method = "meetsCriteria",
        args = {android.location.Criteria.class}
    )
    public void testMeetsCriteria() {
        LocationProvider locationProvider = mLocationManager.getProvider("gps");
        Criteria criteria = new Criteria();
        criteria.setAltitudeRequired(true);
        criteria.setBearingRequired(true);
        assertTrue(locationProvider.meetsCriteria(criteria));
    }
}
