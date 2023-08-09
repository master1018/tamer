public class Test_opc_return extends DxTestCase {
    public void testN1() {
        T_opc_return_1 t = new T_opc_return_1();
        assertEquals(123456, t.run());
    }
    public void testN2() {
        assertTrue(T_opc_return_2.execute());
    }
    public void testE1() {
        T_opc_return_3 t = new T_opc_return_3();
        try {
            assertTrue(t.run());
            fail("expected IllegalMonitorStateException");
        } catch (IllegalMonitorStateException imse) {
        }
    }
    public void testE2() {
        T_opc_return_4 t = new T_opc_return_4();
        try {
            t.run();
            System.out.print("dvmvfe:");
        } catch (IllegalMonitorStateException imse) {
        }
    }
    public void testVFE1() {
        try {
            Class.forName("dxc.junit.opcodes.opc_return.jm.T_opc_return_5");
            fail("expected a verification exception");
        } catch (Throwable t) {
            DxUtil.checkVerifyException(t);
        }
    }
    public void testVFE2() {
        try {
            Class.forName("dxc.junit.opcodes.opc_return.jm.T_opc_return_6");
            fail("expected a verification exception");
        } catch (Throwable t) {
            DxUtil.checkVerifyException(t);
        }
    }
}
