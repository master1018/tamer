public class Test_dload extends DxTestCase {
    public void testN1() {
        T_dload_1 t = new T_dload_1();
        assertEquals(1d, t.run());
    }
    public void testN2() {
        T_dload_2 t = new T_dload_2();
        assertEquals(1d, t.run());
    }
    public void testVFE1() {
        try {
            Class.forName("dxc.junit.opcodes.dload.jm.T_dload_3");
            fail("expected a verification exception");
        } catch (Throwable t) {
            DxUtil.checkVerifyException(t);
        }
    }
    public void testVFE2() {
        try {
            Class.forName("dxc.junit.opcodes.dload.jm.T_dload_4");
            fail("expected a verification exception");
        } catch (Throwable t) {
            DxUtil.checkVerifyException(t);
        }
    }
    public void testVFE3() {
        try {
            Class.forName("dxc.junit.opcodes.dload.jm.T_dload_5");
            fail("expected a verification exception");
        } catch (Throwable t) {
            DxUtil.checkVerifyException(t);
        }
    }
    public void testVFE4() {
        try {
            Class.forName("dxc.junit.opcodes.dload.jm.T_dload_6");
            fail("expected a verification exception");
        } catch (Throwable t) {
            DxUtil.checkVerifyException(t);
        }
    }
    public void testN3() {
        T_dload_1_w t = new T_dload_1_w();
        assertEquals(1d, t.run());
    }
    public void testN4() {
        T_dload_2_w t = new T_dload_2_w();
        assertEquals(1d, t.run());
    }
    public void testVFE5() {
        try {
            Class.forName("dxc.junit.opcodes.dload.jm.T_dload_3_w");
            fail("expected a verification exception");
        } catch (Throwable t) {
            DxUtil.checkVerifyException(t);
        }
    }
    public void testVFE6() {
        try {
            Class.forName("dxc.junit.opcodes.dload.jm.T_dload_4_w");
            fail("expected a verification exception");
        } catch (Throwable t) {
            DxUtil.checkVerifyException(t);
        }
    }
    public void testVFE7() {
        try {
            Class.forName("dxc.junit.opcodes.dload.jm.T_dload_5_w");
            fail("expected a verification exception");
        } catch (Throwable t) {
            DxUtil.checkVerifyException(t);
        }
    }
    public void testVFE8() {
        try {
            Class.forName("dxc.junit.opcodes.dload.jm.T_dload_6_w");
            fail("expected a verification exception");
        } catch (Throwable t) {
            DxUtil.checkVerifyException(t);
        }
    }
}
