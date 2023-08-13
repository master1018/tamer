@TestTargetClass(DSAKey.class)
public class DSAKeyTest extends TestCase {
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "getParams",
        args = {}
    )
    public void test_getParams() throws Exception {
        DSAParams param = new DSAParameterSpec(Util.P, Util.Q, Util.G);
        KeyPairGenerator gen = KeyPairGenerator.getInstance("DSA");
        gen.initialize((DSAParameterSpec) param);
        DSAKey key = null;
        key = (DSAKey) gen.generateKeyPair().getPrivate();
        assertDSAParamsEquals(param, key.getParams());
        key = (DSAKey) gen.generateKeyPair().getPublic();                
        assertDSAParamsEquals(param, key.getParams());
    }
    private void assertDSAParamsEquals(DSAParams expected, DSAParams actual) {
        assertEquals("P differ", expected.getP(), actual.getP());
        assertEquals("Q differ", expected.getQ(), actual.getQ());
        assertEquals("G differ", expected.getG(), actual.getG());
    }
}
