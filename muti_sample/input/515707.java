public class Test_astore_3 extends DxTestCase {
    public void testN1() {
        assertEquals("hello", T_astore_3_1.run());
    }
    public void testN2() {
        assertTrue(T_astore_3_5.run());
    }
    public void testVFE1() {
        try {
            Class.forName("dxc.junit.opcodes.astore_3.jm.T_astore_3_2");
            fail("expected a verification exception");
        } catch (Throwable t) {
            DxUtil.checkVerifyException(t);
        }
    }
    public void testVFE2() {
        try {
            Class.forName("dxc.junit.opcodes.astore_3.jm.T_astore_3_3");
            fail("expected a verification exception");
        } catch (Throwable t) {
            DxUtil.checkVerifyException(t);
        }
    }
    public void testVFE3() {
        try {
            Class.forName("dxc.junit.opcodes.astore_3.jm.T_astore_3_4");
            fail("expected a verification exception");
        } catch (Throwable t) {
            DxUtil.checkVerifyException(t);
        }
    }
}
