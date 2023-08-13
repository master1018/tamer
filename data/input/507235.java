public class Test_dup2_x2 extends DxTestCase {
    public void testN1() {
        T_dup2_x2_1 t = new T_dup2_x2_1();
        assertTrue(t.run());
    }
    public void testN2() {
        T_dup2_x2_2 t = new T_dup2_x2_2();
        assertTrue(t.run());
    }
    public void testN3() {
        T_dup2_x2_3 t = new T_dup2_x2_3();
        assertTrue(t.run());
    }
    public void testN4() {
        T_dup2_x2_4 t = new T_dup2_x2_4();
        assertTrue(t.run());
    }
    public void testN5() {
        T_dup2_x2_5 t = new T_dup2_x2_5();
        assertTrue(t.run());
    }
    public void testVFE1() {
        try {
            Class.forName("dxc.junit.opcodes.dup2_x2.jm.T_dup2_x2_6");
            fail("expected a verification exception");
        } catch (Throwable t) {
            DxUtil.checkVerifyException(t);
        }
    }
    public void testVFE4() {
        try {
            Class.forName("dxc.junit.opcodes.dup2_x2.jm.T_dup2_x2_7");
            fail("expected a verification exception");
        } catch (Throwable t) {
            DxUtil.checkVerifyException(t);
        }
    }
}
