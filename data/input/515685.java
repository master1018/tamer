public class Test_fstore_0 extends DxTestCase {
    public void testN1() {
        assertEquals(2f, T_fstore_0_1.run());
    }
    public void testN2() {
        assertTrue(T_fstore_0_5.run());
    }
    public void testVFE1() {
        try {
            Class.forName("dxc.junit.opcodes.fstore_0.jm.T_fstore_0_2");
            fail("expected a verification exception");
        } catch (Throwable t) {
            DxUtil.checkVerifyException(t);
        }
    }
    public void testVFE2() {
        try {
            Class.forName("dxc.junit.opcodes.fstore_0.jm.T_fstore_0_3");
            fail("expected a verification exception");
        } catch (Throwable t) {
            DxUtil.checkVerifyException(t);
        }
    }
    public void testVFE3() {
        try {
            Class.forName("dxc.junit.opcodes.fstore_0.jm.T_fstore_0_4");
            fail("expected a verification exception");
        } catch (Throwable t) {
            DxUtil.checkVerifyException(t);
        }
    }
}
