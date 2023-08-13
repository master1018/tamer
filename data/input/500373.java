@TestTargetClass(SupplicantState.class)
public class SupplicantStateTest extends AndroidTestCase {
    @TestTargets({
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "isValidState",
            args = {android.net.wifi.SupplicantState.class}
        )
    })
    public void testIsValidState() {
        assertTrue(SupplicantState.isValidState(SupplicantState.DISCONNECTED));
        assertTrue(SupplicantState.isValidState(SupplicantState.INACTIVE));
        assertTrue(SupplicantState.isValidState(SupplicantState.SCANNING));
        assertTrue(SupplicantState.isValidState(SupplicantState.ASSOCIATING));
        assertTrue(SupplicantState.isValidState(SupplicantState.ASSOCIATED));
        assertTrue(SupplicantState.isValidState(SupplicantState.FOUR_WAY_HANDSHAKE));
        assertTrue(SupplicantState.isValidState(SupplicantState.GROUP_HANDSHAKE));
        assertTrue(SupplicantState.isValidState(SupplicantState.COMPLETED));
        assertTrue(SupplicantState.isValidState(SupplicantState.DORMANT));
        assertFalse(SupplicantState.isValidState(SupplicantState.UNINITIALIZED));
        assertFalse(SupplicantState.isValidState(SupplicantState.INVALID));
    }
}
