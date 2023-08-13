@TestTargetClass(DSAParams.class)
public class DSAParamsTest extends TestCase {
    private final BigInteger p = new BigInteger("4");
    private final BigInteger q = BigInteger.TEN;
    private final BigInteger g = BigInteger.ZERO;
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "getG",
        args = {}
    )
    public void test_getG() {
        DSAParams params = new DSAParameterSpec(p, q, g);
        assertEquals("Invalid G", g, params.getG());
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "getP",
        args = {}
    )
    public void test_getP() {
        DSAParams params = new DSAParameterSpec(p, q, g);
        assertEquals("Invalid P", p, params.getP());
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "getQ",
        args = {}
    )
    public void test_getQ() {
        DSAParams params = new DSAParameterSpec(p, q, g);
        assertEquals("Invalid Q", q, params.getQ());
    }
}
