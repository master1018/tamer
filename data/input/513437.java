public class Test_const_wide_16 extends DxTestCase {
    public void testN1() {
        T_const_wide_16_1 t = new T_const_wide_16_1();
         long a = 10000l;
         long b = 10000l;
        assertEquals(a + b, t.run());
    }
    public void testVFE1() {
        try {
            Class.forName("dot.junit.opcodes.const_wide_16.d.T_const_wide_16_3");
            fail("expected a verification exception");
        } catch (Throwable t) {
            DxUtil.checkVerifyException(t);
        }
    }
    public void testVFE2() {
        try {
            Class.forName("dot.junit.opcodes.const_wide_16.d.T_const_wide_16_4");
            fail("expected a verification exception");
        } catch (Throwable t) {
            DxUtil.checkVerifyException(t);
        }
    }
}
