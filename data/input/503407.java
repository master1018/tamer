public class Test_fstore_2 extends DxTestCase {
    public void testN1() {
        assertEquals(2f, T_fstore_2_1.run());
    }
    public void testN2() {
        assertTrue(T_fstore_2_5.run());
    }
    public void testVFE1() {
        try {
            Class.forName("dxc.junit.opcodes.fstore_2.jm.T_fstore_2_2");
            fail("expected a verification exception");
        } catch (Throwable t) {
            DxUtil.checkVerifyException(t);
        }
    }
    public void testVFE2() {
        try {
            Class.forName("dxc.junit.opcodes.fstore_2.jm.T_fstore_2_3");
            fail("expected a verification exception");
        } catch (Throwable t) {
            DxUtil.checkVerifyException(t);
        }
    }
    public void testVFE3() {
        try {
            Class.forName("dxc.junit.opcodes.fstore_2.jm.T_fstore_2_4");
            fail("expected a verification exception");
        } catch (Throwable t) {
            DxUtil.checkVerifyException(t);
        }
    }
}
