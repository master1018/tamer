public class Test_dstore extends DxTestCase {
    public void testN1() {
        assertEquals(1d, T_dstore_1.run());
    }
    public void testN2() {
        assertEquals(1d, T_dstore_5.run());
    }
    public void testVFE1() {
        try {
            Class.forName("dxc.junit.opcodes.dstore.jm.T_dstore_2");
            fail("expected a verification exception");
        } catch (Throwable t) {
            DxUtil.checkVerifyException(t);
        }
    }
    public void testVFE2() {
        try {
            Class.forName("dxc.junit.opcodes.dstore.jm.T_dstore_3");
            fail("expected a verification exception");
        } catch (Throwable t) {
            DxUtil.checkVerifyException(t);
        }
    }
    public void testVFE3() {
        try {
            Class.forName("dxc.junit.opcodes.dstore.jm.T_dstore_4");
            fail("expected a verification exception");
        } catch (Throwable t) {
            DxUtil.checkVerifyException(t);
        }
    }
    public void testN3() {
        assertEquals(1d, T_dstore_1_w.run());
    }
    public void testN4() {
        assertEquals(1d, T_dstore_5_w.run());
    }
    public void testVFE4() {
        try {
            Class.forName("dxc.junit.opcodes.dstore.jm.T_dstore_2_w");
            fail("expected a verification exception");
        } catch (Throwable t) {
            DxUtil.checkVerifyException(t);
        }
    }
    public void testVFE5() {
        try {
            Class.forName("dxc.junit.opcodes.dstore.jm.T_dstore_3_w");
            fail("expected a verification exception");
        } catch (Throwable t) {
            DxUtil.checkVerifyException(t);
        }
    }
    public void testVFE6() {
        try {
            Class.forName("dxc.junit.opcodes.dstore.jm.T_dstore_4_w");
            fail("expected a verification exception");
        } catch (Throwable t) {
            DxUtil.checkVerifyException(t);
        }
    }
}
