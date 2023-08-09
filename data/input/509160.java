@TestTargetClass(GpsStatus.class)
public class GpsStatusTest extends AndroidTestCase {
    private GpsStatus mGpsStatus;
    @Override
    protected void setUp() throws Exception {
        super.setUp();
        LocationManager lm =
            (LocationManager) getContext().getSystemService(Context.LOCATION_SERVICE);
        mGpsStatus = lm.getGpsStatus(null);
    }
    @TestTargets({
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "getSatellites",
            args = {}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "getMaxSatellites",
            args = {}
        )
    })
    public void testGetSatellites() {
        Iterable<GpsSatellite> satellites = mGpsStatus.getSatellites();
        assertNotNull(satellites);
        final int maxSatellites = mGpsStatus.getMaxSatellites();
        assertTrue(maxSatellites > 0);
        Iterator<GpsSatellite> iterator = satellites.iterator();
        int count = 0;
        while (iterator.hasNext()) {
            count++;
        }
        assertTrue(count <= maxSatellites);
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        method = "getTimeToFirstFix",
        args = {}
    )
    public void testGetTimeToFirstFix() {
        mGpsStatus.getTimeToFirstFix();
    }
}
