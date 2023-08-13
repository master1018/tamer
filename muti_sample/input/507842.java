public class Test_b17 extends DxTestCase {
    public void testVFE1() {
        try {
            Class.forName("dot.junit.verify.b17.d.T_b17_1");
            fail("expected a verification exception");
        } catch (Throwable t) {
            DxUtil.checkVerifyException(t);
        }
    }
}
