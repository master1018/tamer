public class Test_astore extends DxTestCase {
    public void testN1() {
        assertEquals("hello", T_astore_1.run());
    }
    public void testN2() {
        assertEquals("world", T_astore_5.run());
    }
    public void testVFE1() {
        try {
            Class.forName("dxc.junit.opcodes.astore.jm.T_astore_2");
            fail("expected a verification exception");
        } catch (Throwable t) {
            DxUtil.checkVerifyException(t);
        }
    }
    public void testVFE2() {
        try {
            Class.forName("dxc.junit.opcodes.astore.jm.T_astore_3");
            fail("expected a verification exception");
        } catch (Throwable t) {
            DxUtil.checkVerifyException(t);
        }
    }
    public void testVFE3() {
        try {
            Class.forName("dxc.junit.opcodes.astore.jm.T_astore_4");
            fail("expected a verification exception");
        } catch (Throwable t) {
            DxUtil.checkVerifyException(t);
        }
    }
    public void testN3() {
        assertEquals("hello", T_astore_1_w.run());
    }
    public void testN4() {
        assertEquals("world", T_astore_5_w.run());
    }
    public void testVFE4() {
        try {
            Class.forName("dxc.junit.opcodes.astore.jm.T_astore_2_w");
            fail("expected a verification exception");
        } catch (Throwable t) {
            DxUtil.checkVerifyException(t);
        }
    }
    public void testVFE5() {
        try {
            Class.forName("dxc.junit.opcodes.astore.jm.T_astore_3_w");
            fail("expected a verification exception");
        } catch (Throwable t) {
            DxUtil.checkVerifyException(t);
        }
    }
    public void testVFE6() {
        try {
            Class.forName("dxc.junit.opcodes.astore.jm.T_astore_4_w");
            fail("expected a verification exception");
        } catch (Throwable t) {
            DxUtil.checkVerifyException(t);
        }
    }
}
