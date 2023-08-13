public class Test_fstore extends DxTestCase {
    public void testN1() {
        assertEquals(2f, T_fstore_1.run());
    }
    public void testN2() {
        assertEquals(2f, T_fstore_5.run());
    }
    public void testVFE1() {
        try {
            Class.forName("dxc.junit.opcodes.fstore.jm.T_fstore_2");
            fail("expected a verification exception");
        } catch (Throwable t) {
            DxUtil.checkVerifyException(t);
        }
    }
    public void testVFE2() {
        try {
            Class.forName("dxc.junit.opcodes.fstore.jm.T_fstore_3");
            fail("expected a verification exception");
        } catch (Throwable t) {
            DxUtil.checkVerifyException(t);
        }
    }
    public void testVFE3() {
        try {
            Class.forName("dxc.junit.opcodes.fstore.jm.T_fstore_4");
            fail("expected a verification exception");
        } catch (Throwable t) {
            DxUtil.checkVerifyException(t);
        }
    }
    public void testN3() {
        assertEquals(2f, T_fstore_1_w.run());
    }
    public void testN4() {
        assertEquals(2f, T_fstore_5_w.run());
    }
    public void testVFE4() {
        try {
            Class.forName("dxc.junit.opcodes.fstore.jm.T_fstore_2_w");
            fail("expected a verification exception");
        } catch (Throwable t) {
            DxUtil.checkVerifyException(t);
        }
    }
    public void testVFE5() {
        try {
            Class.forName("dxc.junit.opcodes.fstore.jm.T_fstore_3_w");
            fail("expected a verification exception");
        } catch (Throwable t) {
            DxUtil.checkVerifyException(t);
        }
    }
    public void testVFE6() {
        try {
            Class.forName("dxc.junit.opcodes.fstore.jm.T_fstore_4_w");
            fail("expected a verification exception");
        } catch (Throwable t) {
            DxUtil.checkVerifyException(t);
        }
    }
}
