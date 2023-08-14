public class Test_dup2_x1 extends DxTestCase {
    public void testN1() {
        T_dup2_x1_1 t = new T_dup2_x1_1();
        assertTrue(t.run());
    }
    public void testN2() {
        T_dup2_x1_6 t = new T_dup2_x1_6();
        assertTrue(t.run());
    }
    public void testN3() {
        T_dup2_x1_3 t = new T_dup2_x1_3();
        assertTrue(t.run());
    }
    public void testN4() {
        T_dup2_x1_4 t = new T_dup2_x1_4();
        assertTrue(t.run());
    }
    public void testVFE1() {
        try {
            Class.forName("dxc.junit.opcodes.dup2_x1.jm.T_dup2_x1_2");
            fail("expected a verification exception");
        } catch (Throwable t) {
            DxUtil.checkVerifyException(t);
        }
    }
    public void testVFE4() {
        try {
            Class.forName("dxc.junit.opcodes.dup2_x1.jm.T_dup2_x1_5");
            fail("expected a verification exception");
        } catch (Throwable t) {
            DxUtil.checkVerifyException(t);
        }
    }
}
