public class Test_iload extends DxTestCase {
    public void testN1() {
        T_iload_1 t = new T_iload_1();
        assertEquals(4, t.run());
    }
    public void testN2() {
        T_iload_2 t = new T_iload_2();
        assertEquals(3, t.run());
    }
    public void testVFE1() {
        try {
            Class.forName("dxc.junit.opcodes.iload.jm.T_iload_3");
            fail("expected a verification exception");
        } catch (Throwable t) {
            DxUtil.checkVerifyException(t);
        }
    }
    public void testVFE2() {
        try {
            Class.forName("dxc.junit.opcodes.iload.jm.T_iload_4");
            fail("expected a verification exception");
        } catch (Throwable t) {
            DxUtil.checkVerifyException(t);
        }
    }
    public void testVFE3() {
        try {
            Class.forName("dxc.junit.opcodes.iload.jm.T_iload_5");
            fail("expected a verification exception");
        } catch (Throwable t) {
            DxUtil.checkVerifyException(t);
        }
    }
    public void testVFE4() {
        try {
            Class.forName("dxc.junit.opcodes.iload.jm.T_iload_6");
            fail("expected a verification exception");
        } catch (Throwable t) {
            DxUtil.checkVerifyException(t);
        }
    }
    public void testN3() {
        T_iload_1_w t = new T_iload_1_w();
        assertEquals(4, t.run());
    }
    public void testN4() {
        T_iload_2_w t = new T_iload_2_w();
        assertEquals(3, t.run());
    }
    public void testVFE5() {
        try {
            Class.forName("dxc.junit.opcodes.iload.jm.T_iload_3_w");
            fail("expected a verification exception");
        } catch (Throwable t) {
            DxUtil.checkVerifyException(t);
        }
    }
    public void testVFE6() {
        try {
            Class.forName("dxc.junit.opcodes.iload.jm.T_iload_4_w");
            fail("expected a verification exception");
        } catch (Throwable t) {
            DxUtil.checkVerifyException(t);
        }
    }
    public void testVFE7() {
        try {
            Class.forName("dxc.junit.opcodes.iload.jm.T_iload_5_w");
            fail("expected a verification exception");
        } catch (Throwable t) {
            DxUtil.checkVerifyException(t);
        }
    }
    public void testVFE8() {
        try {
            Class.forName("dxc.junit.opcodes.iload.jm.T_iload_6_w");
            fail("expected a verification exception");
        } catch (Throwable t) {
            DxUtil.checkVerifyException(t);
        }
    }
}
