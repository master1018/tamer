public class Test_t482_20 extends DxTestCase {
    public void testVFE1() {
        try {
            Class.forName("dxc.junit.verify.t482_20.jm.T_t482_20_1");
            fail("expected a verification exception");
        } catch (Throwable t) {
            DxUtil.checkVerifyException(t);
        }
    }
}
