public class Test_dload_1 extends DxTestCase {
    public void testN1() {
        assertEquals(1d, T_dload_1_1.run());
    }
    public void testN2() {
        assertTrue(T_dload_1_6.run());
    }
    public void testVFE1() {
        try {
            Class.forName("dxc.junit.opcodes.dload_1.jm.T_dload_1_2");
            fail("expected a verification exception");
        } catch (Throwable t) {
            DxUtil.checkVerifyException(t);
        }
    }
    public void testVFE2() {
        try {
            Class.forName("dxc.junit.opcodes.dload_1.jm.T_dload_1_3");
            fail("expected a verification exception");
        } catch (Throwable t) {
            DxUtil.checkVerifyException(t);
        }
    }
    public void testVFE3() {
        try {
            Class.forName("dxc.junit.opcodes.dload_1.jm.T_dload_1_4");
            fail("expected a verification exception");
        } catch (Throwable t) {
            DxUtil.checkVerifyException(t);
        }
    }
    public void testVFE4() {
        try {
            Class.forName("dxc.junit.opcodes.dload_1.jm.T_dload_1_5");
            fail("expected a verification exception");
        } catch (Throwable t) {
            DxUtil.checkVerifyException(t);
        }
    }
}
