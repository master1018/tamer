public class Test_istore_1 extends DxTestCase {
    public void testN1() {
        assertEquals(3, T_istore_1_1.run());
    }
    public void testN2() {
        assertTrue(T_istore_1_5.run());
    }
    public void testVFE1() {
        try {
            Class.forName("dxc.junit.opcodes.istore_1.jm.T_istore_1_2");
            fail("expected a verification exception");
        } catch (Throwable t) {
            DxUtil.checkVerifyException(t);
        }
    }
    public void testVFE2() {
        try {
            Class.forName("dxc.junit.opcodes.istore_1.jm.T_istore_1_3");
            fail("expected a verification exception");
        } catch (Throwable t) {
            DxUtil.checkVerifyException(t);
        }
    }
    public void testVFE3() {
        try {
            Class.forName("dxc.junit.opcodes.istore_1.jm.T_istore_1_4");
            fail("expected a verification exception");
        } catch (Throwable t) {
            DxUtil.checkVerifyException(t);
        }
    }
}
