public class Test_const_wide extends DxTestCase {
    public void testN1() {
        T_const_wide_1 t = new T_const_wide_1();
        double a = 1234567890123232323232232323232323232323232323456788d;
        double b = 1d;
        assertEquals(a + b, t.run(), 0d);
    }
    public void testN2() {
        T_const_wide_2 t = new T_const_wide_2();
         long a = 10000000000l;
         long b = 10000000000l;
        assertEquals(a + b, t.run());
    }
    public void testVFE1() {
        try {
            Class.forName("dot.junit.opcodes.const_wide.d.T_const_wide_3");
            fail("expected a verification exception");
        } catch (Throwable t) {
            DxUtil.checkVerifyException(t);
        }
    }
    public void testVFE2() {
        try {
            Class.forName("dot.junit.opcodes.const_wide.d.T_const_wide_4");
            fail("expected a verification exception");
        } catch (Throwable t) {
            DxUtil.checkVerifyException(t);
        }
    }
}
