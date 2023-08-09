public class Test_lstore_2 extends DxTestCase {
    public void testN1() {
        assertEquals(1234567890123l, T_lstore_2_1.run());
    }
    public void testN2() {
        assertTrue(T_lstore_2_2.run());
    }
    public void testVFE1() {
        try {
            Class.forName("dxc.junit.opcodes.lstore_2.jm.T_lstore_2_3");
            fail("expected a verification exception");
        } catch (Throwable t) {
            DxUtil.checkVerifyException(t);
        }
    }
    public void testVFE2() {
        try {
            Class.forName("dxc.junit.opcodes.lstore_2.jm.T_lstore_2_4");
            fail("expected a verification exception");
        } catch (Throwable t) {
            DxUtil.checkVerifyException(t);
        }
    }
    public void testVFE3() {
        try {
            Class.forName("dxc.junit.opcodes.lstore_2.jm.T_lstore_2_5");
            fail("expected a verification exception");
        } catch (Throwable t) {
            DxUtil.checkVerifyException(t);
        }
    }
    public void testVFE4() {
        try {
            Class.forName("dxc.junit.opcodes.lstore_2.jm.T_lstore_2_6");
            fail("expected a verification exception");
        } catch (Throwable t) {
            DxUtil.checkVerifyException(t);
        }
    }
}
