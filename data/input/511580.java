public class Test_t481_3 extends DxTestCase {
    public void testVFE1() {
        try {
            Class.forName("dxc.junit.verify.t481_3.jm.T_t481_3_1");
            fail("expected a verification exception");
        } catch (Throwable t) {
            DxUtil.checkVerifyException(t);
        }
    }
}
