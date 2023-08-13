public class Test_dstore_1 extends DxTestCase {
    public void testN1() {
        assertEquals(1d, T_dstore_1_1.run());
    }
    public void testN2() {
        assertTrue(T_dstore_1_5.run());
    }
    public void testVFE1() {
        try {
            Class.forName("dxc.junit.opcodes.dstore_1.jm.T_dstore_1_2");
            fail("expected a verification exception");
        } catch (Throwable t) {
            DxUtil.checkVerifyException(t);
        }
    }
    public void testVFE2() {
        try {
            Class.forName("dxc.junit.opcodes.dstore_1.jm.T_dstore_1_3");
            fail("expected a verification exception");
        } catch (Throwable t) {
            DxUtil.checkVerifyException(t);
        }
    }
    public void testVFE3() {
        try {
            Class.forName("dxc.junit.opcodes.dstore_1.jm.T_dstore_1_4");
            fail("expected a verification exception");
        } catch (Throwable t) {
            DxUtil.checkVerifyException(t);
        }
    }
}
