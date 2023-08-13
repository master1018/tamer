public class Test_astore_0 extends DxTestCase {
    public void testN1() {
        assertEquals("hello", T_astore_0_1.run());
    }
    public void testN2() {
        assertTrue(T_astore_0_5.run());
    }
    public void testVFE1() {
        try {
            Class.forName("dxc.junit.opcodes.astore_0.jm.T_astore_0_2");
            fail("expected a verification exception");
        } catch (Throwable t) {
            DxUtil.checkVerifyException(t);
        }
    }
    public void testVFE2() {
        try {
            Class.forName("dxc.junit.opcodes.astore_0.jm.T_astore_0_3");
            fail("expected a verification exception");
        } catch (Throwable t) {
            DxUtil.checkVerifyException(t);
        }
    }
    public void testVFE3() {
        try {
            Class.forName("dxc.junit.opcodes.astore_0.jm.T_astore_0_4");
            fail("expected a verification exception");
        } catch (Throwable t) {
            DxUtil.checkVerifyException(t);
        }
    }
}
