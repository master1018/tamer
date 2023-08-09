public class Test_const_4 extends DxTestCase {
    public void testN2() {
        T_const_4_1 t = new T_const_4_1();
         int a = -2;
         int b = -2;
        assertEquals(a + b, t.run());
    }
    public void testVFE1() {
        try {
            Class.forName("dot.junit.opcodes.const_4.d.T_const_4_3");
            fail("expected a verification exception");
        } catch (Throwable t) {
            DxUtil.checkVerifyException(t);
        }
    }
    public void testVFE2() {
        try {
            Class.forName("dot.junit.opcodes.const_4.d.T_const_4_4");
            fail("expected a verification exception");
        } catch (Throwable t) {
            DxUtil.checkVerifyException(t);
        }
    }
}
