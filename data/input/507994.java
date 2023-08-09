public class Test_lstore extends DxTestCase {
    public void testN1() {
        assertEquals(1234567890123l, T_lstore_1.run());
    }
    public void testN2() {
        assertEquals(1234567890123l, T_lstore_2.run());
    }
    public void testVFE1() {
        try {
            Class.forName("dxc.junit.opcodes.lstore.jm.T_lstore_3");
            fail("expected a verification exception");
        } catch (Throwable t) {
            DxUtil.checkVerifyException(t);
        }
    }
    public void testVFE2() {
        try {
            Class.forName("dxc.junit.opcodes.lstore.jm.T_lstore_4");
            fail("expected a verification exception");
        } catch (Throwable t) {
            DxUtil.checkVerifyException(t);
        }
    }
    public void testVFE3() {
        try {
            Class.forName("dxc.junit.opcodes.lstore.jm.T_lstore_5");
            fail("expected a verification exception");
        } catch (Throwable t) {
            DxUtil.checkVerifyException(t);
        }
    }
    public void testVFE4() {
        try {
            Class.forName("dxc.junit.opcodes.lstore.jm.T_lstore_6");
            fail("expected a verification exception");
        } catch (Throwable t) {
            DxUtil.checkVerifyException(t);
        }
    }
    public void testN3() {
        assertEquals(1234567890123l, T_lstore_1_w.run());
    }
    public void testN4() {
        assertEquals(1234567890123l, T_lstore_2_w.run());
    }
    public void testVFE5() {
        try {
            Class.forName("dxc.junit.opcodes.lstore.jm.T_lstore_3_w");
            fail("expected a verification exception");
        } catch (Throwable t) {
            DxUtil.checkVerifyException(t);
        }
    }
    public void testVFE6() {
        try {
            Class.forName("dxc.junit.opcodes.lstore.jm.T_lstore_4_w");
            fail("expected a verification exception");
        } catch (Throwable t) {
            DxUtil.checkVerifyException(t);
        }
    }
    public void testVFE7() {
        try {
            Class.forName("dxc.junit.opcodes.lstore.jm.T_lstore_5_w");
            fail("expected a verification exception");
        } catch (Throwable t) {
            DxUtil.checkVerifyException(t);
        }
    }
    public void testVFE8() {
        try {
            Class.forName("dxc.junit.opcodes.lstore.jm.T_lstore_6_w");
            fail("expected a verification exception");
        } catch (Throwable t) {
            DxUtil.checkVerifyException(t);
        }
    }
}
