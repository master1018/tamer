public class Test_t482_2 extends DxTestCase {
    public void testVFE1() {
        try {
            Class.forName("dxc.junit.verify.t482_2.jm.T_t482_2_1");
            fail("expected a verification exception");
        } catch (Throwable t) {
            DxUtil.checkVerifyException(t);
        }
    }
}
