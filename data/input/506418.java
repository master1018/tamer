public class Test_opc_goto extends DxTestCase {
    public void testN1() {
        T_opc_goto_1 t = new T_opc_goto_1();
        assertEquals(0, t.run(20));
    }
    public void testN2() {
        T_opc_goto_1 t = new T_opc_goto_1();
        assertEquals(-20, t.run(-20));
    }
    public void testN3() {
        T_opc_goto_5 t = new T_opc_goto_5();
        assertTrue(t.run());
    }
    public void testVFE1() {
        try {
            Class.forName("dxc.junit.opcodes.opc_goto.jm.T_opc_goto_2");
            fail("expected a verification exception");
        } catch (Throwable t) {
            DxUtil.checkVerifyException(t);
        }
    }
    public void testVFE2() {
        try {
            Class.forName("dxc.junit.opcodes.opc_goto.jm.T_opc_goto_3");
            fail("expected a verification exception");
        } catch (Throwable t) {
            DxUtil.checkVerifyException(t);
        }
    }
    public void testVFE3() {
        try {
            Class.forName("dxc.junit.opcodes.opc_goto.jm.T_opc_goto_4");
            fail("expected a verification exception");
        } catch (Throwable t) {
            DxUtil.checkVerifyException(t);
        }
    }
}
