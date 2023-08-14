public class Test_t482_11 extends DxTestCase {
    public void testN1() {
        T_t482_11_2 t = new T_t482_11_2();
        assertEquals(11, t.v);
    }
    public void testVFE1() {
        try {
            Class.forName("dxc.junit.verify.t482_11.jm.T_t482_11_1");
            fail("expected a verification exception");
        } catch (Throwable t) {
            DxUtil.checkVerifyException(t);
        }
    }
    public void testVFE2() {
        try {
            Class.forName("dxc.junit.verify.t482_11.jm.T_t482_11_3");
            fail("expected a verification exception");
        } catch (Throwable t) {
            DxUtil.checkVerifyException(t);
        }
    }
}
