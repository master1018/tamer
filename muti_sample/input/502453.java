public class Test_iload_0 extends DxTestCase {
    public void testN1() {
        assertEquals(3, T_iload_0_1.run());
    }
    public void testN2() {
        assertTrue(T_iload_0_6.run());
    }
    public void testVFE1() {
        try {
            Class.forName("dxc.junit.opcodes.iload_0.jm.T_iload_0_2");
            fail("expected a verification exception");
        } catch (Throwable t) {
            DxUtil.checkVerifyException(t);
        }
    }
    public void testVFE2() {
        try {
            Class.forName("dxc.junit.opcodes.iload_0.jm.T_iload_0_3");
            fail("expected a verification exception");
        } catch (Throwable t) {
            DxUtil.checkVerifyException(t);
        }
    }
    public void testVFE3() {
        try {
            Class.forName("dxc.junit.opcodes.iload_0.jm.T_iload_0_4");
            fail("expected a verification exception");
        } catch (Throwable t) {
            DxUtil.checkVerifyException(t);
        }
    }
    public void testVFE4() {
        try {
            Class.forName("dxc.junit.opcodes.iload_0.jm.T_iload_0_5");
            fail("expected a verification exception");
        } catch (Throwable t) {
            DxUtil.checkVerifyException(t);
        }
    }
}
