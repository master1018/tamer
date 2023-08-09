public class Test_float_to_int extends DxTestCase {
    public void testN1() {
        T_float_to_int_1 t = new T_float_to_int_1();
        assertEquals(2, t.run(2.999999f));
    }
    public void testN2() {
        T_float_to_int_1 t = new T_float_to_int_1();
        assertEquals(1, t.run(1f));
    }
    public void testN3() {
        T_float_to_int_1 t = new T_float_to_int_1();
        assertEquals(-1, t.run(-1f));
    }
    public void testN4() {
        T_float_to_int_5 t = new T_float_to_int_5();
        try {
            t.run(1);
        } catch (Throwable e) {
        }
    }  
    public void testB1() {
        T_float_to_int_1 t = new T_float_to_int_1();
        assertEquals(0, t.run(-0f));
    }
    public void testB2() {
        T_float_to_int_1 t = new T_float_to_int_1();
        assertEquals(Integer.MAX_VALUE, t.run(Float.MAX_VALUE));
    }
    public void testB3() {
        T_float_to_int_1 t = new T_float_to_int_1();
        assertEquals(0, t.run(Float.MIN_VALUE));
    }
    public void testB4() {
        T_float_to_int_1 t = new T_float_to_int_1();
        assertEquals(0, t.run(Float.NaN));
    }
    public void testB5() {
        T_float_to_int_1 t = new T_float_to_int_1();
        assertEquals(Integer.MAX_VALUE, t.run(Float.POSITIVE_INFINITY));
    }
    public void testB6() {
        T_float_to_int_1 t = new T_float_to_int_1();
        assertEquals(Integer.MIN_VALUE, t.run(Float.NEGATIVE_INFINITY));
    }
    public void testVFE2() {
        try {
            Class.forName("dot.junit.opcodes.float_to_int.d.T_float_to_int_2");
            fail("expected a verification exception");
        } catch (Throwable t) {
            DxUtil.checkVerifyException(t);
        }
    }
    public void testVFE3() {
        try {
            Class.forName("dot.junit.opcodes.float_to_int.d.T_float_to_int_3");
            fail("expected a verification exception");
        } catch (Throwable t) {
            DxUtil.checkVerifyException(t);
        }
    }
    public void testVFE4() {
        try {
            Class.forName("dot.junit.opcodes.float_to_int.d.T_float_to_int_4");
            fail("expected a verification exception");
        } catch (Throwable t) {
            DxUtil.checkVerifyException(t);
        }
    }
    public void testVFE5() {
        try {
            Class.forName("dot.junit.opcodes.float_to_int.d.T_float_to_int_6");
            fail("expected a verification exception");
        } catch (Throwable t) {
            DxUtil.checkVerifyException(t);
        }
    }
}
