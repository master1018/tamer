public class Test_const_string_jumbo extends DxTestCase {
    public void testN1() {
        T_const_string_jumbo_1 t = new T_const_string_jumbo_1();
        String s = t.run();
        assertEquals(0, s.compareTo("android jumbo"));
        String s2 = t.run();
        assertTrue(s == s2);
    }
    public void testVFE1() {
        try {
            Class.forName("dot.junit.opcodes.const_string_jumbo.d.T_const_string_jumbo_3");
            fail("expected a verification exception");
        } catch (Throwable t) {
            DxUtil.checkVerifyException(t);
        }
    }
    public void testVFE2() {
        try {
            Class.forName("dot.junit.opcodes.const_string_jumbo.d.T_const_string_jumbo_4");
            fail("expected a verification exception");
        } catch (Throwable t) {
            DxUtil.checkVerifyException(t);
        }
    }
    public void testVFE3() {
        try {
            Class.forName("dot.junit.opcodes.const_string_jumbo.d.T_const_string_jumbo_5");
            fail("expected a verification exception");
        } catch (Throwable t) {
            DxUtil.checkVerifyException(t);
        }
    }
}
