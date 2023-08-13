public class Test_t482_10 extends DxTestCase {
    public void testVFE1() {
        try {
            Class.forName("dxc.junit.verify.t482_10.jm.T_t482_10_1");
            fail("expected a verification exception");
        } catch (Throwable t) {
            DxUtil.checkVerifyException(t);
        }
    }
    public void testVFE2() {
        try {
            Class.forName("dxc.junit.verify.t482_10.jm.T_t482_10_2");
            fail("expected a verification exception");
        } catch (Throwable t) {
            DxUtil.checkVerifyException(t);
        }
    }
}
