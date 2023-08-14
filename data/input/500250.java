public class Test_int_to_float extends DxTestCase {
    public void testN1() {
        T_int_to_float_1 t = new T_int_to_float_1();
        assertEquals(123456f, t.run(123456), 0f);
    }
    public void testN2() {
        T_int_to_float_1 t = new T_int_to_float_1();
        assertEquals(1f, t.run(1), 0f);
    }
    public void testN3() {
        T_int_to_float_1 t = new T_int_to_float_1();
        assertEquals(-1f, t.run(-1), 0f);
    }
    public void testN4() {
        T_int_to_float_1 t = new T_int_to_float_1();
        assertEquals(3.356444E7f, t.run(33564439), 0f);
    }
    public void testN5() {
        T_int_to_float_5 t = new T_int_to_float_5();
        try {
            t.run(1.333f);
        } catch (Throwable e) {
        }
    } 
    public void testB1() {
        T_int_to_float_1 t = new T_int_to_float_1();
        assertEquals(0f, t.run(0), 0f);
    }
    public void testB2() {
        T_int_to_float_1 t = new T_int_to_float_1();
        assertEquals(2147483650f, t.run(Integer.MAX_VALUE), 0f);
    }
    public void testB3() {
        T_int_to_float_1 t = new T_int_to_float_1();
        assertEquals(-2147483650f, t.run(Integer.MIN_VALUE), 0f);
    }
    public void testVFE1() {
        try {
            Class.forName("dot.junit.opcodes.int_to_float.d.T_int_to_float_2");
            fail("expected a verification exception");
        } catch (Throwable t) {
            DxUtil.checkVerifyException(t);
        }
    }
    public void testVFE2() {
        try {
            Class.forName("dot.junit.opcodes.int_to_float.d.T_int_to_float_3");
            fail("expected a verification exception");
        } catch (Throwable t) {
            DxUtil.checkVerifyException(t);
        }
    }
    public void testVFE3() {
        try {
            Class.forName("dot.junit.opcodes.int_to_float.d.T_int_to_float_4");
            fail("expected a verification exception");
        } catch (Throwable t) {
            DxUtil.checkVerifyException(t);
        }
    }
    public void testVFE4() {
        try {
            Class.forName("dot.junit.opcodes.int_to_float.d.T_int_to_float_6");
            fail("expected a verification exception");
        } catch (Throwable t) {
            DxUtil.checkVerifyException(t);
        }
    }
}
