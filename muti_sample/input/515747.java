public class Test_dload_2 extends DxTestCase {
    public void testN1() {
        assertEquals(1d, T_dload_2_1.run());
    }
    public void testN2() {
        assertTrue(T_dload_2_6.run());
    }
    public void testVFE1() {
        try {
            Class.forName("dxc.junit.opcodes.dload_2.jm.T_dload_2_2");
            fail("expected a verification exception");
        } catch (Throwable t) {
            DxUtil.checkVerifyException(t);
        }
    }
    public void testVFE2() {
        try {
            Class.forName("dxc.junit.opcodes.dload_2.jm.T_dload_2_3");
            fail("expected a verification exception");
        } catch (Throwable t) {
            DxUtil.checkVerifyException(t);
        }
    }
    public void testVFE3() {
        try {
            Class.forName("dxc.junit.opcodes.dload_2.jm.T_dload_2_4");
            fail("expected a verification exception");
        } catch (Throwable t) {
            DxUtil.checkVerifyException(t);
        }
    }
    public void testVFE4() {
        try {
            Class.forName("dxc.junit.opcodes.dload_2.jm.T_dload_2_5");
            fail("expected a verification exception");
        } catch (Throwable t) {
            DxUtil.checkVerifyException(t);
        }
    }
}
