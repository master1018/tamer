public class Test_const_string extends DxTestCase {
    public void testN1() {
        T_const_string_1 t = new T_const_string_1();
        String s = t.run();
        assertEquals(s.compareTo("android"), 0);
        String s2 = t.run();
        assertTrue(s == s2);
    }
    public void testVFE1() {
        try {
            Class.forName("dot.junit.opcodes.const_string.d.T_const_string_3");
            fail("expected a verification exception");
        } catch (Throwable t) {
            DxUtil.checkVerifyException(t);
        }
    }
    public void testVFE2() {
        try {
            Class.forName("dot.junit.opcodes.const_string.d.T_const_string_4");
            fail("expected a verification exception");
        } catch (Throwable t) {
            DxUtil.checkVerifyException(t);
        }
    }
    public void testVFE3() {
        try {
            Class.forName("dot.junit.opcodes.const_string.d.T_const_string_5");
            fail("expected a verification exception");
        } catch (Throwable t) {
            DxUtil.checkVerifyException(t);
        }
    }
}
