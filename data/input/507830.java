public class Test_t482_4 extends DxTestCase {
    public void testVFE1() {
        try {
            Class.forName("dxc.junit.verify.t482_4.jm.T_t482_4_1");
            fail("expected a verification exception");
        } catch (Throwable t) {
            DxUtil.checkVerifyException(t);
        }
    }
    public void testVFE2() {
        try {
            Class.forName("dxc.junit.verify.t482_4.jm.T_t482_4_2");
            fail("expected a verification exception");
        } catch (Throwable t) {
            DxUtil.checkVerifyException(t);
        }
    }
    public void testVFE3() {
        try {
            Class.forName("dxc.junit.verify.t482_4.jm.T_t482_4_3");
            fail("expected a verification exception");
        } catch (Throwable t) {
            DxUtil.checkVerifyException(t);
        }
    }
}
