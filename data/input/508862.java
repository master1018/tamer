public class Test_fload_3 extends DxTestCase {
    public void testN1() {
        assertEquals(2f, T_fload_3_1.run());
    }
    public void testN2() {
        assertTrue(T_fload_3_6.run());
    }
    public void testVFE1() {
        try {
            Class.forName("dxc.junit.opcodes.fload_3.jm.T_fload_3_2");
            fail("expected a verification exception");
        } catch (Throwable t) {
            DxUtil.checkVerifyException(t);
        }
    }
    public void testVFE2() {
        try {
            Class.forName("dxc.junit.opcodes.fload_3.jm.T_fload_3_3");
            fail("expected a verification exception");
        } catch (Throwable t) {
            DxUtil.checkVerifyException(t);
        }
    }
    public void testVFE3() {
        try {
            Class.forName("dxc.junit.opcodes.fload_3.jm.T_fload_3_4");
            fail("expected a verification exception");
        } catch (Throwable t) {
            DxUtil.checkVerifyException(t);
        }
    }
    public void testVFE4() {
        try {
            Class.forName("dxc.junit.opcodes.fload_3.jm.T_fload_3_5");
            fail("expected a verification exception");
        } catch (Throwable t) {
            DxUtil.checkVerifyException(t);
        }
    }
}
