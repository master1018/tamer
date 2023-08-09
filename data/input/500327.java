public class Test_const_wide_high16 extends DxTestCase {
    public void testN2() {
        T_const_wide_high16_1 t = new T_const_wide_high16_1();
        assertEquals(0x1234000000000000l, t.run());
    }
    public void testVFE1() {
        try {
            Class.forName("dot.junit.opcodes.const_wide_high16.d.T_const_wide_high16_3");
            fail("expected a verification exception");
        } catch (Throwable t) {
            DxUtil.checkVerifyException(t);
        }
    }
    public void testVFE2() {
        try {
            Class.forName("dot.junit.opcodes.const_wide_high16.d.T_const_wide_high16_4");
            fail("expected a verification exception");
        } catch (Throwable t) {
            DxUtil.checkVerifyException(t);
        }
    }
}
