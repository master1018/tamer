public class Test_astore_2 extends DxTestCase {
    public void testN1() {
        assertEquals("hello", T_astore_2_1.run());
    }
    public void testN2() {
        assertTrue(T_astore_2_5.run());
    }
    public void testVFE1() {
        try {
            Class.forName("dxc.junit.opcodes.astore_2.jm.T_astore_2_2");
            fail("expected a verification exception");
        } catch (Throwable t) {
            DxUtil.checkVerifyException(t);
        }
    }
    public void testVFE2() {
        try {
            Class.forName("dxc.junit.opcodes.astore_2.jm.T_astore_2_3");
            fail("expected a verification exception");
        } catch (Throwable t) {
            DxUtil.checkVerifyException(t);
        }
    }
    public void testVFE3() {
        try {
            Class.forName("dxc.junit.opcodes.astore_2.jm.T_astore_2_4");
            fail("expected a verification exception");
        } catch (Throwable t) {
            DxUtil.checkVerifyException(t);
        }
    }
}
