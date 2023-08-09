public class Test_lstore_1 extends DxTestCase {
    public void testN1() {
        assertEquals(1234567890123l, T_lstore_1_1.run());
    }
    public void testN2() {
        assertTrue(T_lstore_1_2.run());
    }
    public void testVFE1() {
        try {
            Class.forName("dxc.junit.opcodes.lstore_1.jm.T_lstore_1_3");
            fail("expected a verification exception");
        } catch (Throwable t) {
            DxUtil.checkVerifyException(t);
        }
    }
    public void testVFE2() {
        try {
            Class.forName("dxc.junit.opcodes.lstore_1.jm.T_lstore_1_4");
            fail("expected a verification exception");
        } catch (Throwable t) {
            DxUtil.checkVerifyException(t);
        }
    }
    public void testVFE3() {
        try {
            Class.forName("dxc.junit.opcodes.lstore_1.jm.T_lstore_1_5");
            fail("expected a verification exception");
        } catch (Throwable t) {
            DxUtil.checkVerifyException(t);
        }
    }
    public void testVFE4() {
        try {
            Class.forName("dxc.junit.opcodes.lstore_1.jm.T_lstore_1_6");
            fail("expected a verification exception");
        } catch (Throwable t) {
            DxUtil.checkVerifyException(t);
        }
    }
}
