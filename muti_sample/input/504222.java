public class Test_wide extends DxTestCase {
    public void testVFE1() {
        try {
            Class.forName("dxc.junit.opcodes.wide.jm.T_wide_1");
            fail("expected a verification exception");
        } catch (Throwable t) {
            DxUtil.checkVerifyException(t);
        }
    }
    public void testVFE2() {
        try {
            Class.forName("dxc.junit.opcodes.wide.jm.T_wide_2");
            fail("expected a verification exception");
        } catch (Throwable t) {
            DxUtil.checkVerifyException(t);
        }
    }
}
