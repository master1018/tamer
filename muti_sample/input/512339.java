public class Test_mul_int_lit8 extends DxTestCase {
    public void testN1() {
        T_mul_int_lit8_1 t = new T_mul_int_lit8_1();
        assertEquals(550, t.run(55));
    }
    public void testN2() {
        T_mul_int_lit8_1 t = new T_mul_int_lit8_1();
        assertEquals(-250, t.run(-25));
    }
    public void testN3() {
        T_mul_int_lit8_2 t = new T_mul_int_lit8_2();
        assertEquals(345, t.run(-23));
    }
    public void testN4() {
        T_mul_int_lit8_1 t = new T_mul_int_lit8_1();
        assertEquals(-20, t.run(0x7ffffffe));
    }
    public void testN5() {
        T_mul_int_lit8_3 t = new T_mul_int_lit8_3();
        try {
            t.run(3.14f);
        } catch (Throwable e) {
        }
    }
    public void testB1() {
        T_mul_int_lit8_4 t = new T_mul_int_lit8_4();
        assertEquals(0, t.run(0));
    }
    public void testB2() {
        T_mul_int_lit8_4 t = new T_mul_int_lit8_4();
        assertEquals(0, t.run(Byte.MAX_VALUE));
    }
    public void testB3() {
        T_mul_int_lit8_5 t = new T_mul_int_lit8_5();
        assertEquals(Byte.MAX_VALUE, t.run(Byte.MAX_VALUE));
    }
    public void testB4() {
        T_mul_int_lit8_5 t = new T_mul_int_lit8_5();
        assertEquals(Short.MIN_VALUE, t.run(Short.MIN_VALUE));
    }
    public void testB5() {
        T_mul_int_lit8_6 t = new T_mul_int_lit8_6();
        assertEquals(-4161536, t.run(Short.MIN_VALUE));
    }
    public void testVFE1() {
        try {
            Class.forName("dot.junit.opcodes.mul_int_lit8.d.T_mul_int_lit8_7");
            fail("expected a verification exception");
        } catch (Throwable t) {
            DxUtil.checkVerifyException(t);
        }
    }
    public void testVFE2() {
        try {
            Class.forName("dot.junit.opcodes.mul_int_lit8.d.T_mul_int_lit8_8");
            fail("expected a verification exception");
        } catch (Throwable t) {
            DxUtil.checkVerifyException(t);
        }
    }
    public void testVFE3() {
        try {
            Class.forName("dot.junit.opcodes.mul_int_lit8.d.T_mul_int_lit8_9");
            fail("expected a verification exception");
        } catch (Throwable t) {
            DxUtil.checkVerifyException(t);
        }
    }
    public void testVFE4() {
        try {
            Class.forName("dot.junit.opcodes.mul_int_lit8.d.T_mul_int_lit8_10");
            fail("expected a verification exception");
        } catch (Throwable t) {
            DxUtil.checkVerifyException(t);
        }
    }
}
