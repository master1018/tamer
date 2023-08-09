public class Test_rem_int_lit16 extends DxTestCase {
    public void testN1() {
        T_rem_int_lit16_1 t = new T_rem_int_lit16_1();
        assertEquals(0, t.run(8));
    }
    public void testN2() {
        T_rem_int_lit16_1 t = new T_rem_int_lit16_1();
        assertEquals(1, t.run(10737));
    }
    public void testN3() {
        T_rem_int_lit16_1 t = new T_rem_int_lit16_1();
        assertEquals(0, t.run(0));
    }
    public void testN4() {
        T_rem_int_lit16_1 t = new T_rem_int_lit16_1();
        assertEquals(-2, t.run(-10));
    }
    public void testN5() {
        T_rem_int_lit16_2 t = new T_rem_int_lit16_2();
        assertEquals(1, t.run(22222));
    }
    public void testN6() {
        T_rem_int_lit16_3 t = new T_rem_int_lit16_3();
        assertEquals(-2235, t.run(-23235));
    }
    public void testN7() {
        T_rem_int_lit16_4 t = new T_rem_int_lit16_4();
        try {
            t.run(3.14f);
        } catch (Throwable e) {
        }
    }
    public void testB1() {
        T_rem_int_lit16_5 t = new T_rem_int_lit16_5();
        assertEquals(0, t.run(Short.MIN_VALUE));
    }
    public void testB2() {
        T_rem_int_lit16_6 t = new T_rem_int_lit16_6();
        assertEquals(0, t.run(Short.MIN_VALUE));
    }
    public void testB3() {
        T_rem_int_lit16_6 t = new T_rem_int_lit16_6();
        assertEquals(0, t.run(Short.MAX_VALUE));
    }
    public void testB4() {
        T_rem_int_lit16_7 t = new T_rem_int_lit16_7();
        assertEquals(-1, t.run(Short.MIN_VALUE));
    }
    public void testB5() {
        T_rem_int_lit16_7 t = new T_rem_int_lit16_7();
        assertEquals(1, t.run(1));
    }
    public void testB6() {
        T_rem_int_lit16_8 t = new T_rem_int_lit16_8();
        assertEquals(1, t.run(1));
    }
    public void testE1() {
        T_rem_int_lit16_9 t = new T_rem_int_lit16_9();
        try {
            t.run(1);
            fail("expected ArithmeticException");
        } catch (ArithmeticException ae) {
        }
    }
    public void testVFE1() {
        try {
            Class.forName("dot.junit.opcodes.rem_int_lit16.d.T_rem_int_lit16_10");
            fail("expected a verification exception");
        } catch (Throwable t) {
            DxUtil.checkVerifyException(t);
        }
    }
    public void testVFE2() {
        try {
            Class.forName("dot.junit.opcodes.rem_int_lit16.d.T_rem_int_lit16_11");
            fail("expected a verification exception");
        } catch (Throwable t) {
            DxUtil.checkVerifyException(t);
        }
    }
    public void testVFE3() {
        try {
            Class.forName("dot.junit.opcodes.rem_int_lit16.d.T_rem_int_lit16_12");
            fail("expected a verification exception");
        } catch (Throwable t) {
            DxUtil.checkVerifyException(t);
        }
    }
    public void testVFE4() {
        try {
            Class.forName("dot.junit.opcodes.rem_int_lit16.d.T_rem_int_lit16_13");
            fail("expected a verification exception");
        } catch (Throwable t) {
            DxUtil.checkVerifyException(t);
        }
    }
}
