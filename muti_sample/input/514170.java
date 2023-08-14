public class Test_fload extends DxTestCase {
    public void testN1() {
        T_fload_1 t = new T_fload_1();
        assertEquals(2f, t.run());
    }
    public void testN2() {
        T_fload_2 t = new T_fload_2();
        assertEquals(2f, t.run());
    }
    public void testVFE1() {
        try {
            Class.forName("dxc.junit.opcodes.fload.jm.T_fload_3");
            fail("expected a verification exception");
        } catch (Throwable t) {
            DxUtil.checkVerifyException(t);
        }
    }
    public void testVFE2() {
        try {
            Class.forName("dxc.junit.opcodes.fload.jm.T_fload_4");
            fail("expected a verification exception");
        } catch (Throwable t) {
            DxUtil.checkVerifyException(t);
        }
    }
    public void testVFE3() {
        try {
            Class.forName("dxc.junit.opcodes.fload.jm.T_fload_5");
            fail("expected a verification exception");
        } catch (Throwable t) {
            DxUtil.checkVerifyException(t);
        }
    }
    public void testVFE4() {
        try {
            Class.forName("dxc.junit.opcodes.fload.jm.T_fload_6");
            fail("expected a verification exception");
        } catch (Throwable t) {
            DxUtil.checkVerifyException(t);
        }
    }
    public void testN3() {
        T_fload_1_w t = new T_fload_1_w();
        assertEquals(2f, t.run());
    }
    public void testN4() {
        T_fload_2_w t = new T_fload_2_w();
        assertEquals(2f, t.run());
    }
    public void testVFE5() {
        try {
            Class.forName("dxc.junit.opcodes.fload.jm.T_fload_3_w");
            fail("expected a verification exception");
        } catch (Throwable t) {
            DxUtil.checkVerifyException(t);
        }
    }
    public void testVFE6() {
        try {
            Class.forName("dxc.junit.opcodes.fload.jm.T_fload_4_w");
            fail("expected a verification exception");
        } catch (Throwable t) {
            DxUtil.checkVerifyException(t);
        }
    }
    public void testVFE7() {
        try {
            Class.forName("dxc.junit.opcodes.fload.jm.T_fload_5_w");
            fail("expected a verification exception");
        } catch (Throwable t) {
            DxUtil.checkVerifyException(t);
        }
    }
    public void testVFE8() {
        try {
            Class.forName("dxc.junit.opcodes.fload.jm.T_fload_6_w");
            fail("expected a verification exception");
        } catch (Throwable t) {
            DxUtil.checkVerifyException(t);
        }
    }
}
