public class Test_double_to_int extends DxTestCase {
    public void testN1() {
        T_double_to_int_1 t = new T_double_to_int_1();
        assertEquals(2, t.run(2.9999999d));
    }
    public void testN2() {
        T_double_to_int_1 t = new T_double_to_int_1();
        assertEquals(1, t.run(1d));
    }
    public void testN3() {
        T_double_to_int_1 t = new T_double_to_int_1();
        assertEquals(-1, t.run(-1d));
    }
    public void testN4() {
        T_double_to_int_3 t = new T_double_to_int_3();
        try {
            t.run(12345l);
        } catch (Throwable e) {
        }
    }    
    public void testB1() {
        T_double_to_int_1 t = new T_double_to_int_1();
        assertEquals(0, t.run(-0d));
    }
    public void testB2() {
        T_double_to_int_1 t = new T_double_to_int_1();
        assertEquals(Integer.MAX_VALUE, t.run(Double.MAX_VALUE));
    }
    public void testB3() {
        T_double_to_int_1 t = new T_double_to_int_1();
        assertEquals(0, t.run(Double.MIN_VALUE));
    }
    public void testB4() {
        T_double_to_int_1 t = new T_double_to_int_1();
        assertEquals(0, t.run(Double.NaN));
    }
    public void testB5() {
        T_double_to_int_1 t = new T_double_to_int_1();
        assertEquals(Integer.MAX_VALUE, t.run(Double.POSITIVE_INFINITY));
    }
    public void testB6() {
        T_double_to_int_1 t = new T_double_to_int_1();
        assertEquals(Integer.MIN_VALUE, t.run(Double.NEGATIVE_INFINITY));
    }
    public void testVFE2() {
        try {
            Class.forName("dot.junit.opcodes.double_to_int.d.T_double_to_int_2");
            fail("expected a verification exception");
        } catch (Throwable t) {
            DxUtil.checkVerifyException(t);
        }
    }
    public void testVFE3() {
        try {
            Class.forName("dot.junit.opcodes.double_to_int.d.T_double_to_int_5");
            fail("expected a verification exception");
        } catch (Throwable t) {
            DxUtil.checkVerifyException(t);
        }
    }
    public void testVFE4() {
        try {
            Class.forName("dot.junit.opcodes.double_to_int.d.T_double_to_int_4");
            fail("expected a verification exception");
        } catch (Throwable t) {
            DxUtil.checkVerifyException(t);
        }
    }
    public void testVFE5() {
        try {
            Class.forName("dot.junit.opcodes.double_to_int.d.T_double_to_int_6");
            fail("expected a verification exception");
        } catch (Throwable t) {
            DxUtil.checkVerifyException(t);
        }
    }
}
