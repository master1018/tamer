public class Test_div_int_lit16 extends DxTestCase {
    public void testN1() {
        T_div_int_lit16_1 t = new T_div_int_lit16_1();
        assertEquals(2, t.run());
    }
    public void testN2() {
        T_div_int_lit16_2 t = new T_div_int_lit16_2();
        assertEquals(268435455, t.run());
    }
    public void testN3() {
        T_div_int_lit16_3 t = new T_div_int_lit16_3();
        assertEquals(0, t.run());
    }
    public void testN4() {
        T_div_int_lit16_4 t = new T_div_int_lit16_4();
        assertEquals(-3, t.run());
    }
    public void testN5() {
        T_div_int_lit16_5 t = new T_div_int_lit16_5();
        assertEquals(-357913941, t.run());
    }
    public void testN6() {
        T_div_int_lit16_6 t = new T_div_int_lit16_6();
        assertEquals(5965, t.run());
    }
    public void testN7() {
        T_div_int_lit16_17 t = new T_div_int_lit16_17();
        try {
            t.run(3.14f);
        } catch (Throwable e) {
        }
    }
    public void testB1() {
        T_div_int_lit16_7 t = new T_div_int_lit16_7();
        assertEquals(Integer.MIN_VALUE, t.run());
    }
    public void testB2() {
        T_div_int_lit16_8 t = new T_div_int_lit16_8();
        assertEquals(Integer.MIN_VALUE, t.run());
    }
    public void testB3() {
        T_div_int_lit16_9 t = new T_div_int_lit16_9();
        assertEquals(Integer.MAX_VALUE, t.run());
    }
    public void testB4() {
        T_div_int_lit16_10 t = new T_div_int_lit16_10();
        assertEquals(-65538, t.run());
    }
    public void testB5() {
        T_div_int_lit16_11 t = new T_div_int_lit16_11();
        assertEquals(0, t.run());
    }
    public void testB6() {
        T_div_int_lit16_12 t = new T_div_int_lit16_12();
        assertEquals(0, t.run());
    }
    public void testE1() {
        T_div_int_lit16_13 t = new T_div_int_lit16_13();
        try {
            t.run();
            fail("expected ArithmeticException");
        } catch (ArithmeticException ae) {
        }
    }
    public void testVFE1() {
        try {
            Class.forName("dot.junit.opcodes.div_int_lit16.d.T_div_int_lit16_14");
            fail("expected a verification exception");
        } catch (Throwable t) {
            DxUtil.checkVerifyException(t);
        }
    }
    public void testVFE2() {
        try {
            Class.forName("dot.junit.opcodes.div_int_lit16.d.T_div_int_lit16_15");
            fail("expected a verification exception");
        } catch (Throwable t) {
            DxUtil.checkVerifyException(t);
        }
    }
    public void testVFE3() {
        try {
            Class.forName("dot.junit.opcodes.div_int_lit16.d.T_div_int_lit16_16");
            fail("expected a verification exception");
        } catch (Throwable t) {
            DxUtil.checkVerifyException(t);
        }
    }
    public void testVFE4() {
        try {
            Class.forName("dot.junit.opcodes.div_int_lit16.d.T_div_int_lit16_18");
            fail("expected a verification exception");
        } catch (Throwable t) {
            DxUtil.checkVerifyException(t);
        }
    }
}
