public class Test_jsr_w extends DxTestCase {
    public void testN1() {
        T_jsr_w_1 t = new T_jsr_w_1();
        assertTrue(t.run());
    }
    public void testN2() {
        T_jsr_w_2 t = new T_jsr_w_2();
        assertTrue(t.run());
    }
    public void testVFE1() {
        try {
            Class.forName("dxc.junit.opcodes.jsr_w.jm.T_jsr_w_4");
            fail("expected a verification exception");
        } catch (Throwable t) {
            DxUtil.checkVerifyException(t);
        }
    }
    public void testVFE2() {
        try {
            Class.forName("dxc.junit.opcodes.jsr_w.jm.T_jsr_w_5");
            fail("expected a verification exception");
        } catch (Throwable t) {
            DxUtil.checkVerifyException(t);
        }
    }
    public void testVFE3() {
        try {
            Class.forName("dxc.junit.opcodes.jsr_w.jm.T_jsr_w_3");
            fail("expected a verification exception");
        } catch (Throwable t) {
            DxUtil.checkVerifyException(t);
        }
    }
}
