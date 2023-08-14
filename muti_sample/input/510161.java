public class Test_long_to_double extends DxTestCase {
    public void testN1() {
        T_long_to_double_1 t = new T_long_to_double_1();
        assertEquals(5.0E10d, t.run(50000000000l), 0d);
    }
    public void testN2() {
        T_long_to_double_1 t = new T_long_to_double_1();
        assertEquals(1d, t.run(1l), 0d);
    }
    public void testN3() {
        T_long_to_double_1 t = new T_long_to_double_1();
        assertEquals(-1d, t.run(-1l), 0d);
    }
    public void testN4() {
        T_long_to_double_6 t = new T_long_to_double_6();
        try {
            t.run(12345);
        } catch (Throwable e) {
        }
    }       
    public void testB1() {
        T_long_to_double_1 t = new T_long_to_double_1();
        assertEquals(9.223372036854776E18d, t.run(Long.MAX_VALUE), 0d);
    }
    public void testB2() {
        T_long_to_double_1 t = new T_long_to_double_1();
        assertEquals(-9.223372036854776E18, t.run(Long.MIN_VALUE), 0d);
    }
    public void testB3() {
        T_long_to_double_1 t = new T_long_to_double_1();
        assertEquals(0d, t.run(0), 0d);
    }
    public void testVFE1() {
        try {
            Class.forName("dot.junit.opcodes.long_to_double.d.T_long_to_double_2");
            fail("expected a verification exception");
        } catch (Throwable t) {
            DxUtil.checkVerifyException(t);
        }
    }
    public void testVFE2() {
        try {
            Class.forName("dot.junit.opcodes.long_to_double.d.T_long_to_double_3");
            fail("expected a verification exception");
        } catch (Throwable t) {
            DxUtil.checkVerifyException(t);
        }
    }
    public void testVFE3() {
        try {
            Class.forName("dot.junit.opcodes.long_to_double.d.T_long_to_double_4");
            fail("expected a verification exception");
        } catch (Throwable t) {
            DxUtil.checkVerifyException(t);
        }
    }
    public void testVFE4() {
        try {
            Class.forName("dot.junit.opcodes.long_to_double.d.T_long_to_double_5");
            fail("expected a verification exception");
        } catch (Throwable t) {
            DxUtil.checkVerifyException(t);
        }
    }
}
