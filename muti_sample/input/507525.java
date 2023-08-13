public class Test_t481_1 extends DxTestCase {
    public void testVFE1() {
        try {
            Class.forName("dxc.junit.verify.t481_1.jm.T_t481_1_1");
            fail("expected a verification exception");
        } catch (Throwable t) {
            DxUtil.checkVerifyException(t);
        }
    }
}
