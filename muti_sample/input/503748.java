public class Test_mul_int_lit16 extends DxTestCase {
    public void testN1() {
        T_mul_int_lit16_1 t = new T_mul_int_lit16_1();
        assertEquals(26650, t.run(205));
    }
    public void testN2() {
        T_mul_int_lit16_1 t = new T_mul_int_lit16_1();
        assertEquals(-23400, t.run(-180));
    }
    public void testN3() {
        T_mul_int_lit16_1 t = new T_mul_int_lit16_1();
        assertEquals(0x7ef4, t.run(0xfa));
    }
    public void testN4() {
        T_mul_int_lit16_2 t = new T_mul_int_lit16_2();
        assertEquals(32421, t.run(-101));
    }
    public void testN5() {
        T_mul_int_lit16_3 t = new T_mul_int_lit16_3();
        try {
            t.run(3.14f);
        } catch (Throwable e) {
        }
    }
    public void testB1() {
        T_mul_int_lit16_4 t = new T_mul_int_lit16_4();
        assertEquals(0, t.run(0));
    }
    public void testB2() {
        T_mul_int_lit16_4 t = new T_mul_int_lit16_4();
        assertEquals(0, t.run(Short.MAX_VALUE));
    }
    public void testB3() {
        T_mul_int_lit16_5 t = new T_mul_int_lit16_5();
        assertEquals(Short.MAX_VALUE, t.run(Short.MAX_VALUE));
    }
    public void testB4() {
        T_mul_int_lit16_5 t = new T_mul_int_lit16_5();
        assertEquals(Short.MIN_VALUE, t.run(Short.MIN_VALUE));
    }
    public void testB5() {
        T_mul_int_lit16_6 t = new T_mul_int_lit16_6();
        assertEquals(-1073709056, t.run(Short.MIN_VALUE));
    }
    public void testVFE1() {
        try {
            Class.forName("dot.junit.opcodes.mul_int_lit16.d.T_mul_int_lit16_7");
            fail("expected a verification exception");
        } catch (Throwable t) {
            DxUtil.checkVerifyException(t);
        }
    }
    public void testVFE2() {
        try {
            Class.forName("dot.junit.opcodes.mul_int_lit16.d.T_mul_int_lit16_8");
            fail("expected a verification exception");
        } catch (Throwable t) {
            DxUtil.checkVerifyException(t);
        }
    }
    public void testVFE3() {
        try {
            Class.forName("dot.junit.opcodes.mul_int_lit16.d.T_mul_int_lit16_9");
            fail("expected a verification exception");
        } catch (Throwable t) {
            DxUtil.checkVerifyException(t);
        }
    }
    public void testVFE4() {
        try {
            Class.forName("dot.junit.opcodes.mul_int_lit16.d.T_mul_int_lit16_10");
            fail("expected a verification exception");
        } catch (Throwable t) {
            DxUtil.checkVerifyException(t);
        }
    }
}