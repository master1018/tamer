public class Test_jsr extends DxTestCase {
    public void testN1() {
        T_jsr_1 t = new T_jsr_1();
        assertTrue(t.run());
    }
    public void testN2() {
        T_jsr_2 t = new T_jsr_2();
        assertTrue(t.run());
    }
    public void testVFE1() {
        try {
            Class.forName("dxc.junit.opcodes.jsr.jm.T_jsr_4");
            fail("expected a verification exception");
        } catch (Throwable t) {
            DxUtil.checkVerifyException(t);
        }
    }
    public void testVFE2() {
        try {
            Class.forName("dxc.junit.opcodes.jsr.jm.T_jsr_5");
            fail("expected a verification exception");
        } catch (Throwable t) {
            DxUtil.checkVerifyException(t);
        }
    }
    public void testVFE3() {
        try {
            Class.forName("dxc.junit.opcodes.jsr.jm.T_jsr_3");
            fail("expected a verification exception");
        } catch (Throwable t) {
            DxUtil.checkVerifyException(t);
        }
    }
}
