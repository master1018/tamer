public class Test_add_int_lit16 extends DxTestCase {
    public void testN1() {
        T_add_int_lit16_1 t = new T_add_int_lit16_1();
        assertEquals(12, t.run());
    }
    public void testN2() {
        T_add_int_lit16_2 t = new T_add_int_lit16_2();
        assertEquals(255, t.run());
    }
    public void testN3() {
        T_add_int_lit16_3 t = new T_add_int_lit16_3();
        assertEquals(-32768, t.run());
    }
    public void testN4() {
        T_add_int_lit16_4 t = new T_add_int_lit16_4();
        assertEquals(-2147483647, t.run());
    }
    public void testN5() {
        T_add_int_lit16_5 t = new T_add_int_lit16_5();
        assertEquals(-2147483648, t.run());
    }
    public void testN6() {
        T_add_int_lit16_6 t = new T_add_int_lit16_6();
        assertEquals(0, t.run());
    }
    public void testN7() {
        T_add_int_lit16_16 t = new T_add_int_lit16_16();
        try {
            t.run(3.14f);
        } catch (Throwable e) {
        }
    }
    public void testB1() {
        T_add_int_lit16_7 t = new T_add_int_lit16_7();
        assertEquals(Short.MAX_VALUE, t.run());
    }
    public void testB2() {
        T_add_int_lit16_8 t = new T_add_int_lit16_8();
        assertEquals(-2147450882, t.run());
    }
    public void testB3() {
        T_add_int_lit16_9 t = new T_add_int_lit16_9();
        assertEquals(Integer.MIN_VALUE, t.run());
    }
    public void testB4() {
        T_add_int_lit16_10 t = new T_add_int_lit16_10();
        assertEquals(-2147483647, t.run());
    }
    public void testB5() {
        T_add_int_lit16_11 t = new T_add_int_lit16_11();
        assertEquals(0, t.run());
    }
    public void testB6() {
        T_add_int_lit16_12 t = new T_add_int_lit16_12();
        assertEquals(-65536, t.run());
    }
    public void testVFE1() {
        try {
            Class.forName("dot.junit.opcodes.add_int_lit16.d.T_add_int_lit16_13");
            fail("expected a verification exception");
        } catch (Throwable t) {
            DxUtil.checkVerifyException(t);
        }
    }
    public void testVFE2() {
        try {
            Class.forName("dot.junit.opcodes.add_int_lit16.d.T_add_int_lit16_14");
            fail("expected a verification exception");
        } catch (Throwable t) {
            DxUtil.checkVerifyException(t);
        }
    }
    public void testVFE3() {
        try {
            Class.forName("dot.junit.opcodes.add_int_lit16.d.T_add_int_lit16_15");
            fail("expected a verification exception");
        } catch (Throwable t) {
            DxUtil.checkVerifyException(t);
        }
    }
    public void testVFE4() {
        try {
            Class.forName("dot.junit.opcodes.add_int_lit16.d.T_add_int_lit16_17");
            fail("expected a verification exception");
        } catch (Throwable t) {
            DxUtil.checkVerifyException(t);
        }
    }
}
