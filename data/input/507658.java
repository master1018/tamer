public class Test_long_to_int extends DxTestCase {
    public void testN1() {
        T_long_to_int_1 t = new T_long_to_int_1();
        assertEquals(0xFFEEDDCC, t.run(0xAAAAFFEEDDCCl));
    }
    public void testN2() {
        T_long_to_int_1 t = new T_long_to_int_1();
        assertEquals(-123456789, t.run(-123456789l));
    }
    public void testN3() {
        T_long_to_int_1 t = new T_long_to_int_1();
        assertEquals(1, t.run(1l));
    }
    public void testN4() {
        T_long_to_int_1 t = new T_long_to_int_1();
        assertEquals(-1, t.run(-1l));
    }
    public void testN5() {
        T_long_to_int_2 t = new T_long_to_int_2();
        try {
            t.run(12345);
        } catch (Throwable e) {
        }
    }
    public void testB1() {
        T_long_to_int_1 t = new T_long_to_int_1();
        assertEquals(-1, t.run(Long.MAX_VALUE));
    }
    public void testB2() {
        T_long_to_int_1 t = new T_long_to_int_1();
        assertEquals(0, t.run(Long.MIN_VALUE));
    }
    public void testB3() {
        T_long_to_int_1 t = new T_long_to_int_1();
        assertEquals(0, t.run(0l));
    }
    public void testVFE1() {
        try {
            Class.forName("dot.junit.opcodes.long_to_int.d.T_long_to_int_3");
            fail("expected a verification exception");
        } catch (Throwable t) {
            DxUtil.checkVerifyException(t);
        }
    }
    public void testVFE2() {
        try {
            Class.forName("dot.junit.opcodes.long_to_int.d.T_long_to_int_4");
            fail("expected a verification exception");
        } catch (Throwable t) {
            DxUtil.checkVerifyException(t);
        }
    }
    public void testVFE3() {
        try {
            Class.forName("dot.junit.opcodes.long_to_int.d.T_long_to_int_5");
            fail("expected a verification exception");
        } catch (Throwable t) {
            DxUtil.checkVerifyException(t);
        }
    }
    public void testVFE4() {
        try {
            Class.forName("dot.junit.opcodes.long_to_int.d.T_long_to_int_6");
            fail("expected a verification exception");
        } catch (Throwable t) {
            DxUtil.checkVerifyException(t);
        }
    }
}
