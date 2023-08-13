public class Test_aload extends DxTestCase {
    public void testN1() {
        T_aload_1 t = new T_aload_1();
        assertEquals("hello", t.run());
    }
    public void testN2() {
        T_aload_2 t = new T_aload_2();
        assertEquals("hello", t.run());
    }
    public void testVFE1() {
        try {
            Class.forName("dxc.junit.opcodes.aload.jm.T_aload_3");
            fail("expected a verification exception");
        } catch (Throwable t) {
            DxUtil.checkVerifyException(t);
        }
    }
    public void testVFE2() {
        try {
            Class.forName("dxc.junit.opcodes.aload.jm.T_aload_4");
            fail("expected a verification exception");
        } catch (Throwable t) {
            DxUtil.checkVerifyException(t);
        }
    }
    public void testVFE3() {
        try {
            Class.forName("dxc.junit.opcodes.aload.jm.T_aload_5");
            fail("expected a verification exception");
        } catch (Throwable t) {
            DxUtil.checkVerifyException(t);
        }
    }
    public void testVFE4() {
        try {
            Class.forName("dxc.junit.opcodes.aload.jm.T_aload_6");
            fail("expected a verification exception");
        } catch (Throwable t) {
            DxUtil.checkVerifyException(t);
        }
    }
    public void testVFE5() {
        try {
            Class.forName("dxc.junit.opcodes.aload.jm.T_aload_7");
            fail("expected a verification exception");
        } catch (Throwable t) {
            DxUtil.checkVerifyException(t);
        }
    }
    public void testN3() {
        T_aload_1_w t = new T_aload_1_w();
        assertEquals("hello", t.run());
    }
    public void testN4() {
        T_aload_2_w t = new T_aload_2_w();
        assertEquals("hello", t.run());
    }
    public void testVFE6() {
        try {
            Class.forName("dxc.junit.opcodes.aload.jm.T_aload_3_w");
            fail("expected a verification exception");
        } catch (Throwable t) {
            DxUtil.checkVerifyException(t);
        }
    }
    public void testVFE7() {
        try {
            Class.forName("dxc.junit.opcodes.aload.jm.T_aload_4_w");
            fail("expected a verification exception");
        } catch (Throwable t) {
            DxUtil.checkVerifyException(t);
        }
    }
    public void testVFE8() {
        try {
            Class.forName("dxc.junit.opcodes.aload.jm.T_aload_5_w");
            fail("expected a verification exception");
        } catch (Throwable t) {
            DxUtil.checkVerifyException(t);
        }
    }
    public void testVFE9() {
        try {
            Class.forName("dxc.junit.opcodes.aload.jm.T_aload_6_w");
            fail("expected a verification exception");
        } catch (Throwable t) {
            DxUtil.checkVerifyException(t);
        }
    }
    public void testVFE10() {
        try {
            Class.forName("dxc.junit.opcodes.aload.jm.T_aload_7_w");
            fail("expected a verification exception");
        } catch (Throwable t) {
            DxUtil.checkVerifyException(t);
        }
    }
}
