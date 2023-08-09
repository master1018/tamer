@TestTargetClass(GpsSatellite.class)
public class GpsSatelliteTest extends AndroidTestCase {
    private GpsSatellite mGpsSatellite;
    @Override
    protected void setUp() throws Exception {
        super.setUp();
        LocationManager lm =
            (LocationManager) getContext().getSystemService(Context.LOCATION_SERVICE);
        GpsStatus gpsStatus = lm.getGpsStatus(null);
        Iterator<GpsSatellite> iterator = gpsStatus.getSatellites().iterator();
        if (iterator.hasNext()) {
            mGpsSatellite = iterator.next();
        }
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        method = "getAzimuth",
        args = {}
    )
    public void testGetAzimuth() {
        if (mGpsSatellite != null) {
            assertTrue(mGpsSatellite.getAzimuth() >= 0 && mGpsSatellite.getAzimuth() <= 360);
        }
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        method = "getElevation",
        args = {}
    )
    public void testGetElevation() {
        if (mGpsSatellite != null) {
            assertTrue(mGpsSatellite.getElevation() >= 0 && mGpsSatellite.getElevation() <= 90);
        }
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        method = "getPrn",
        args = {}
    )
    public void testGetPrn() {
        if (mGpsSatellite != null) {
            mGpsSatellite.getPrn();
        }
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        method = "getSnr",
        args = {}
    )
    public void testGetSnr() {
        if (mGpsSatellite != null) {
            mGpsSatellite.getSnr();
        }
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        method = "hasAlmanac",
        args = {}
    )
    public void testHasAlmanac() {
        if (mGpsSatellite != null) {
            mGpsSatellite.hasAlmanac();
        }
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        method = "hasEphemeris",
        args = {}
    )
    public void testHasEphemeris() {
        if (mGpsSatellite != null) {
            mGpsSatellite.hasEphemeris();
        }
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        method = "usedInFix",
        args = {}
    )
    public void testUsedInFix() {
        if (mGpsSatellite != null) {
            mGpsSatellite.usedInFix();
        }
    }
}
