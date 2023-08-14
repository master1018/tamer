public class Test_dup2 extends DxTestCase {
    public void testN1() {
        T_dup2_1 t = new T_dup2_1();
        assertTrue(t.run());
    }
    public void testN2() {
        T_dup2_6 t = new T_dup2_6();
        assertTrue(t.run());
    }
    public void testN3() {
        T_dup2_3 t = new T_dup2_3();
        assertTrue(t.run());
    }
    public void testN4() {
        T_dup2_4 t = new T_dup2_4();
        assertTrue(t.run());
    }
    public void testVFE1() {
        try {
            Class.forName("dxc.junit.opcodes.dup2.jm.T_dup2_2");
            fail("expected a verification exception");
        } catch (Throwable t) {
            DxUtil.checkVerifyException(t);
        }
    }
    public void testVFE4() {
        try {
            Class.forName("dxc.junit.opcodes.dup2.jm.T_dup2_5");
            fail("expected a verification exception");
        } catch (Throwable t) {
            DxUtil.checkVerifyException(t);
        }
    }
}
