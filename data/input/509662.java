public class Test_istore extends DxTestCase {
    public void testN1() {
        assertEquals(3, T_istore_1.run());
    }
    public void testN2() {
        assertEquals(3, T_istore_5.run());
    }
    public void testVFE1() {
        try {
            Class.forName("dxc.junit.opcodes.istore.jm.T_istore_2");
            fail("expected a verification exception");
        } catch (Throwable t) {
            DxUtil.checkVerifyException(t);
        }
    }
    public void testVFE2() {
        try {
            Class.forName("dxc.junit.opcodes.istore.jm.T_istore_3");
            fail("expected a verification exception");
        } catch (Throwable t) {
            DxUtil.checkVerifyException(t);
        }
    }
    public void testVFE3() {
        try {
            Class.forName("dxc.junit.opcodes.istore.jm.T_istore_4");
            fail("expected a verification exception");
        } catch (Throwable t) {
            DxUtil.checkVerifyException(t);
        }
    }
    public void testN3() {
        assertEquals(3, T_istore_1_w.run());
    }
    public void testN4() {
        assertEquals(3, T_istore_5_w.run());
    }
    public void testVFE4() {
        try {
            Class.forName("dxc.junit.opcodes.istore.jm.T_istore_2_w");
            fail("expected a verification exception");
        } catch (Throwable t) {
            DxUtil.checkVerifyException(t);
        }
    }
    public void testVFE5() {
        try {
            Class.forName("dxc.junit.opcodes.istore.jm.T_istore_3_w");
            fail("expected a verification exception");
        } catch (Throwable t) {
            DxUtil.checkVerifyException(t);
        }
    }
    public void testVFE6() {
        try {
            Class.forName("dxc.junit.opcodes.istore.jm.T_istore_4_w");
            fail("expected a verification exception");
        } catch (Throwable t) {
            DxUtil.checkVerifyException(t);
        }
    }
}
