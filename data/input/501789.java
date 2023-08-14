public class Test_goto_w extends DxTestCase {
    public void testN1() {
        T_goto_w_1 t = new T_goto_w_1();
        assertEquals(0, t.run(20));
    }
    public void testN2() {
        T_goto_w_1 t = new T_goto_w_1();
        assertEquals(-20, t.run(-20));
    }
    public void testN3() {
        T_goto_w_5 t = new T_goto_w_5();
        assertTrue(t.run());
    }
    public void testVFE1() {
        try {
            Class.forName("dxc.junit.opcodes.goto_w.jm.T_goto_w_2");
            fail("expected a verification exception");
        } catch (Throwable t) {
            DxUtil.checkVerifyException(t);
        }
    }
    public void testVFE2() {
        try {
            Class.forName("dxc.junit.opcodes.goto_w.jm.T_goto_w_3");
            fail("expected a verification exception");
        } catch (Throwable t) {
            DxUtil.checkVerifyException(t);
        }
    }
    public void testVFE3() {
        try {
            Class.forName("dxc.junit.opcodes.goto_w.jm.T_goto_w_4");
            fail("expected a verification exception");
        } catch (Throwable t) {
            DxUtil.checkVerifyException(t);
        }
    }
}
