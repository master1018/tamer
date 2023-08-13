public class Test_long_to_float extends DxTestCase {
    public void testN1() {
        T_long_to_float_1 t = new T_long_to_float_1();
        assertEquals(1.23456788E14f, t.run(123456789012345l), 0f);
    }
    public void testN2() {
        T_long_to_float_1 t = new T_long_to_float_1();
        assertEquals(1f, t.run(1l), 0f);
    }
    public void testN3() {
        T_long_to_float_1 t = new T_long_to_float_1();
        assertEquals(-1f, t.run(-1l), 0f);
    }
    public void testN4() {
        T_long_to_float_2 t = new T_long_to_float_2();
        try {
            t.run(12345);
        } catch (Throwable e) {
        }
    }
    public void testB1() {
        T_long_to_float_1 t = new T_long_to_float_1();
        assertEquals(9.223372036854776E18, t.run(Long.MAX_VALUE), 0f);
    }
    public void testB2() {
        T_long_to_float_1 t = new T_long_to_float_1();
        assertEquals(-9.223372036854776E18, t.run(Long.MIN_VALUE), 0f);
    }
    public void testB3() {
        T_long_to_float_1 t = new T_long_to_float_1();
        assertEquals(0f, t.run(0l), 0f);
    }
    public void testVFE2() {
        try {
            Class.forName("dot.junit.opcodes.long_to_float.d.T_long_to_float_3");
            fail("expected a verification exception");
        } catch (Throwable t) {
            DxUtil.checkVerifyException(t);
        }
    }
    public void testVFE3() {
        try {
            Class.forName("dot.junit.opcodes.long_to_float.d.T_long_to_float_4");
            fail("expected a verification exception");
        } catch (Throwable t) {
            DxUtil.checkVerifyException(t);
        }
    }
    public void testVFE4() {
        try {
            Class.forName("dot.junit.opcodes.long_to_float.d.T_long_to_float_5");
            fail("expected a verification exception");
        } catch (Throwable t) {
            DxUtil.checkVerifyException(t);
        }
    }
}
