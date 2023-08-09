public class Test_move_wide_16 extends DxTestCase {
    public void testN1() {
        assertTrue(T_move_wide_16_1.run());
    }
    public void testN2() {
        assertTrue(T_move_wide_16_1.run());
    }
    public void testN3() {
        assertEquals(T_move_wide_16_9.run(), 0);
    }
    public void testVFE1() {
        try {
            Class.forName("dot.junit.opcodes.move_wide_16.d.T_move_wide_16_3");
            fail("expected a verification exception");
        } catch (Throwable t) {
            DxUtil.checkVerifyException(t);
        }
    }
    public void testVFE2() {
        try {
            Class.forName("dot.junit.opcodes.move_wide_16.d.T_move_wide_16_4");
            fail("expected a verification exception");
        } catch (Throwable t) {
            DxUtil.checkVerifyException(t);
        }
    }
    public void testVFE3() {
        try {
            Class.forName("dot.junit.opcodes.move_wide_16.d.T_move_wide_16_5");
            fail("expected a verification exception");
        } catch (Throwable t) {
            DxUtil.checkVerifyException(t);
        }
    }
    public void testVFE4() {
        try {
            Class.forName("dot.junit.opcodes.move_wide_16.d.T_move_wide_16_6");
            fail("expected a verification exception");
        } catch (Throwable t) {
            DxUtil.checkVerifyException(t);
        }
    }
    public void testVFE5() {
        try {
            Class.forName("dot.junit.opcodes.move_wide_16.d.T_move_wide_16_7");
            fail("expected a verification exception");
        } catch (Throwable t) {
            DxUtil.checkVerifyException(t);
        }
    }
    public void testVFE6() {
        try {
            Class.forName("dot.junit.opcodes.move_wide_16.d.T_move_wide_16_8");
            fail("expected a verification exception");
        } catch (Throwable t) {
            DxUtil.checkVerifyException(t);
        }
    }
}
