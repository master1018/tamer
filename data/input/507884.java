@TestTargetClass(State.class)
public class NetworkInfo_StateTest extends AndroidTestCase {
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "Test valueOf(String name).",
        method = "valueOf",
        args = {java.lang.String.class}
    )
    public void testValueOf() {
        assertEquals(State.CONNECTED, State.valueOf("CONNECTED"));
        assertEquals(State.CONNECTING, State.valueOf("CONNECTING"));
        assertEquals(State.DISCONNECTED, State.valueOf("DISCONNECTED"));
        assertEquals(State.DISCONNECTING, State.valueOf("DISCONNECTING"));
        assertEquals(State.SUSPENDED, State.valueOf("SUSPENDED"));
        assertEquals(State.UNKNOWN, State.valueOf("UNKNOWN"));
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "Test values().",
        method = "values",
        args = {}
    )
    public void testValues() {
        State[] expected = State.values();
        assertEquals(6, expected.length);
        assertEquals(State.CONNECTING, expected[0]);
        assertEquals(State.CONNECTED, expected[1]);
        assertEquals(State.SUSPENDED, expected[2]);
        assertEquals(State.DISCONNECTING, expected[3]);
        assertEquals(State.DISCONNECTED, expected[4]);
        assertEquals(State.UNKNOWN, expected[5]);
    }
}
