public class Test_lload extends DxTestCase {
    public void testN1() {
        T_lload_1 t = new T_lload_1();
        assertEquals(1234567890123l, t.run());
    }
    public void testN2() {
        T_lload_2 t = new T_lload_2();
        assertEquals(9876543210123l, t.run());
    }
    public void testVFE1() {
        try {
            Class.forName("dxc.junit.opcodes.lload.jm.T_lload_3");
            fail("expected a verification exception");
        } catch (Throwable t) {
            DxUtil.checkVerifyException(t);
        }
    }
    public void testVFE2() {
        try {
            Class.forName("dxc.junit.opcodes.lload.jm.T_lload_4");
            fail("expected a verification exception");
        } catch (Throwable t) {
            DxUtil.checkVerifyException(t);
        }
    }
    public void testVFE3() {
        try {
            Class.forName("dxc.junit.opcodes.lload.jm.T_lload_5");
            fail("expected a verification exception");
        } catch (Throwable t) {
            DxUtil.checkVerifyException(t);
        }
    }
    public void testVFE4() {
        try {
            Class.forName("dxc.junit.opcodes.lload.jm.T_lload_6");
            fail("expected a verification exception");
        } catch (Throwable t) {
            DxUtil.checkVerifyException(t);
        }
    }
    public void testN3() {
        T_lload_1_w t = new T_lload_1_w();
        assertEquals(1234567890123l, t.run());
    }
    public void testN4() {
        T_lload_2_w t = new T_lload_2_w();
        assertEquals(9876543210123l, t.run());
    }
    public void testVFE5() {
        try {
            Class.forName("dxc.junit.opcodes.lload.jm.T_lload_3_w");
            fail("expected a verification exception");
        } catch (Throwable t) {
            DxUtil.checkVerifyException(t);
        }
    }
    public void testVFE6() {
        try {
            Class.forName("dxc.junit.opcodes.lload.jm.T_lload_4_w");
            fail("expected a verification exception");
        } catch (Throwable t) {
            DxUtil.checkVerifyException(t);
        }
    }
    public void testVFE7() {
        try {
            Class.forName("dxc.junit.opcodes.lload.jm.T_lload_5_w");
            fail("expected a verification exception");
        } catch (Throwable t) {
            DxUtil.checkVerifyException(t);
        }
    }
    public void testVFE8() {
        try {
            Class.forName("dxc.junit.opcodes.lload.jm.T_lload_6_w");
            fail("expected a verification exception");
        } catch (Throwable t) {
            DxUtil.checkVerifyException(t);
        }
    }
}
