@TestTargetClass(RSAKeyGenParameterSpec.class)
public class RSAKeyGenParameterSpecTest extends TestCase {
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "RSAKeyGenParameterSpec",
        args = {int.class, java.math.BigInteger.class}
    )
    public final void testRSAKeyGenParameterSpec() {
        AlgorithmParameterSpec aps =
            new RSAKeyGenParameterSpec(512, BigInteger.valueOf(0L));
        assertTrue(aps instanceof RSAKeyGenParameterSpec);
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "getKeysize",
        args = {}
    )
    public final void testGetKeysize() {
        RSAKeyGenParameterSpec rkgps =
            new RSAKeyGenParameterSpec(512, BigInteger.valueOf(0L));
        assertEquals(512, rkgps.getKeysize());
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "getPublicExponent",
        args = {}
    )
    public final void testGetPublicExponent() {
        RSAKeyGenParameterSpec rkgps =
            new RSAKeyGenParameterSpec(512, BigInteger.valueOf(0L));
        assertEquals(0, rkgps.getPublicExponent().intValue());
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "Test for F0 field.",
        method = "!Constants",
        args = {}
    )
    public final void testF0Value() {
        assertEquals(3, RSAKeyGenParameterSpec.F0.intValue());
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "Test for F4 field.",
        method = "!Constants",
        args = {}
    )
    public final void testF4Value() {
        assertEquals(65537, RSAKeyGenParameterSpec.F4.intValue());
    }
}
