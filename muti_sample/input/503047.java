public class Test_or_int_lit16 extends DxTestCase {
    public void testN1() {
        T_or_int_lit16_1 t = new T_or_int_lit16_1();
        assertEquals(15, t.run(15));
    }
    public void testN2() {
        T_or_int_lit16_2 t = new T_or_int_lit16_2();
        assertEquals(0x7ff7, t.run(0x5ff5));
    }
    public void testN3() {
        T_or_int_lit16_3 t = new T_or_int_lit16_3();
        assertEquals(-1, t.run(0xcaf));
    }
    public void testN4() {
        T_or_int_lit16_4 t = new T_or_int_lit16_4();
        try {
            t.run(3.14f);
        } catch (Throwable e) {
        }
    }
    public void testB1() {
        T_or_int_lit16_3 t = new T_or_int_lit16_3();
        assertEquals(-1, t.run(0));
    }
    public void testB2() {
        T_or_int_lit16_5 t = new T_or_int_lit16_5();
        assertEquals(0xffffffff, t.run(Short.MIN_VALUE));
    }
    public void testVFE1() {
        try {
            Class.forName("dot.junit.opcodes.or_int_lit16.d.T_or_int_lit16_6");
            fail("expected a verification exception");
        } catch (Throwable t) {
            DxUtil.checkVerifyException(t);
        }
    }
    public void testVFE2() {
        try {
            Class.forName("dot.junit.opcodes.or_int_lit16.d.T_or_int_lit16_7");
            fail("expected a verification exception");
        } catch (Throwable t) {
            DxUtil.checkVerifyException(t);
        }
    }
    public void testVFE3() {
        try {
            Class.forName("dot.junit.opcodes.or_int_lit16.d.T_or_int_lit16_8");
            fail("expected a verification exception");
        } catch (Throwable t) {
            DxUtil.checkVerifyException(t);
        }
    }
    public void testVFE4() {
        try {
            Class.forName("dot.junit.opcodes.or_int_lit16.d.T_or_int_lit16_9");
            fail("expected a verification exception");
        } catch (Throwable t) {
            DxUtil.checkVerifyException(t);
        }
    }
}
