@TestTargetClass(DetailedState.class)
public class NetworkInfo_DetailedStateTest extends AndroidTestCase {
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "Test valueOf(String name).",
        method = "valueOf",
        args = {java.lang.String.class}
    )
    public void testValueOf() {
        assertEquals(DetailedState.AUTHENTICATING, DetailedState.valueOf("AUTHENTICATING"));
        assertEquals(DetailedState.CONNECTED, DetailedState.valueOf("CONNECTED"));
        assertEquals(DetailedState.CONNECTING, DetailedState.valueOf("CONNECTING"));
        assertEquals(DetailedState.DISCONNECTED, DetailedState.valueOf("DISCONNECTED"));
        assertEquals(DetailedState.DISCONNECTING, DetailedState.valueOf("DISCONNECTING"));
        assertEquals(DetailedState.FAILED, DetailedState.valueOf("FAILED"));
        assertEquals(DetailedState.IDLE, DetailedState.valueOf("IDLE"));
        assertEquals(DetailedState.OBTAINING_IPADDR, DetailedState.valueOf("OBTAINING_IPADDR"));
        assertEquals(DetailedState.SCANNING, DetailedState.valueOf("SCANNING"));
        assertEquals(DetailedState.SUSPENDED, DetailedState.valueOf("SUSPENDED"));
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "Test values().",
        method = "values",
        args = {}
    )
    public void testValues() {
        DetailedState[] expected = DetailedState.values();
        assertEquals(10, expected.length);
        assertEquals(DetailedState.IDLE, expected[0]);
        assertEquals(DetailedState.SCANNING, expected[1]);
        assertEquals(DetailedState.CONNECTING, expected[2]);
        assertEquals(DetailedState.AUTHENTICATING, expected[3]);
        assertEquals(DetailedState.OBTAINING_IPADDR, expected[4]);
        assertEquals(DetailedState.CONNECTED, expected[5]);
        assertEquals(DetailedState.SUSPENDED, expected[6]);
        assertEquals(DetailedState.DISCONNECTING, expected[7]);
        assertEquals(DetailedState.DISCONNECTED, expected[8]);
        assertEquals(DetailedState.FAILED, expected[9]);
    }
}
