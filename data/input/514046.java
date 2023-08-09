public class Test_lload_3 extends DxTestCase {
    public void testN1() {
        assertEquals(1234567890123l, T_lload_3_1.run());
    }
    public void testN2() {
        assertTrue(T_lload_3_6.run());
    }
    public void testVFE1() {
        try {
            Class.forName("dxc.junit.opcodes.lload_3.jm.T_lload_3_2");
            fail("expected a verification exception");
        } catch (Throwable t) {
            DxUtil.checkVerifyException(t);
        }
    }
    public void testVFE2() {
        try {
            Class.forName("dxc.junit.opcodes.lload_3.jm.T_lload_3_3");
            fail("expected a verification exception");
        } catch (Throwable t) {
            DxUtil.checkVerifyException(t);
        }
    }
    public void testVFE3() {
        try {
            Class.forName("dxc.junit.opcodes.lload_3.jm.T_lload_3_4");
            fail("expected a verification exception");
        } catch (Throwable t) {
            DxUtil.checkVerifyException(t);
        }
    }
    public void testVFE4() {
        try {
            Class.forName("dxc.junit.opcodes.lload_3.jm.T_lload_3_5");
            fail("expected a verification exception");
        } catch (Throwable t) {
            DxUtil.checkVerifyException(t);
        }
    }
}
