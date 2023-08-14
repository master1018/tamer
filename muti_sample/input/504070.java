public class Test_b3 extends DxTestCase {
    public void testVFE1() {
        try {
            Class.forName("dot.junit.verify.b3.d.T_b3_1");
            fail("expected a verification exception");
        } catch (Throwable t) {
            DxUtil.checkVerifyException(t);
        }
    }
    public void testVFE2() {
        try {
            Class.forName("dot.junit.verify.b3.d.T_b3_2");
            fail("expected a verification exception");
        } catch (Throwable t) {
            DxUtil.checkVerifyException(t);
        }
    }
}
