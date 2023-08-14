@TestTargetClass(GsmCellLocation.class)
public class GsmCellLocationTest extends AndroidTestCase {
    private static final int CID_VALUE = 20;
    private static final int LAC_VALUE = 10;
    private static final int INVALID_CID = -1;
    private static final int INVALID_LAC = -1;
    @TestTargets({
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "GsmCellLocation",
            args = {}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "GsmCellLocation",
            args = {Bundle.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "getCid",
            args = {}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "getLac",
            args = {}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "setLacAndCid",
            args = {int.class, int.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "setStateInvalid",
            args = {}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "hashCode",
            args = {}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "toString",
            args = {}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "fillInNotifierBundle",
            args = {Bundle.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "equals",
            args = {Object.class}
        )
    })
    public void testGsmCellLocation() {
        Bundle bundle = new Bundle();
        GsmCellLocation gsmCellLocation = new GsmCellLocation();
        checkLacAndCid(INVALID_LAC, INVALID_CID, gsmCellLocation);
        gsmCellLocation.setLacAndCid(LAC_VALUE, CID_VALUE);
        gsmCellLocation.fillInNotifierBundle(bundle);
        gsmCellLocation = new GsmCellLocation(bundle);
        checkLacAndCid(LAC_VALUE, CID_VALUE, gsmCellLocation);
        gsmCellLocation.setStateInvalid();
        checkLacAndCid(INVALID_LAC, INVALID_CID, gsmCellLocation);
        gsmCellLocation.setLacAndCid(LAC_VALUE, CID_VALUE);
        checkLacAndCid(LAC_VALUE, CID_VALUE, gsmCellLocation);
        assertEquals(LAC_VALUE ^ CID_VALUE, gsmCellLocation.hashCode());
        assertNotNull(gsmCellLocation.toString());
        GsmCellLocation testGCSEquals = new GsmCellLocation();
        assertFalse(gsmCellLocation.equals(testGCSEquals));
        testGCSEquals.setLacAndCid(LAC_VALUE, CID_VALUE);
        assertTrue(gsmCellLocation.equals(testGCSEquals));
    }
    private void checkLacAndCid(int expectedLac, int expectedCid, GsmCellLocation gsmCellLocation) {
        assertEquals(expectedLac, gsmCellLocation.getLac());
        assertEquals(expectedCid, gsmCellLocation.getCid());
    }
}
