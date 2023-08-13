public class Test_and_int_lit16 extends DxTestCase {
    public void testN1() {
        T_and_int_lit16_1 t = new T_and_int_lit16_1();
        assertEquals(8, t.run());
    }
    public void testN2() {
        T_and_int_lit16_2 t = new T_and_int_lit16_2();
        assertEquals(-8, t.run());
    }
    public void testN3() {
        T_and_int_lit16_3 t = new T_and_int_lit16_3();
        assertEquals(0xcafe, t.run());
    }
    public void testN4() {
        T_and_int_lit16_9 t = new T_and_int_lit16_9();
        try {
            t.run(11.33f);
        } catch (Throwable e) {
        }
    }
    public void testB1() {
        T_and_int_lit16_4 t = new T_and_int_lit16_4();
        assertEquals(0, t.run());
    }
    public void testB2() {
        T_and_int_lit16_5 t = new T_and_int_lit16_5();
        assertEquals(0, t.run());
    }
    public void testVFE1() {
        try {
            Class.forName("dot.junit.opcodes.and_int_lit16.d.T_and_int_lit16_6");
            fail("expected a verification exception");
        } catch (Throwable t) {
            DxUtil.checkVerifyException(t);
        }
    }
    public void testVFE2() {
        try {
            Class.forName("dot.junit.opcodes.and_int_lit16.d.T_and_int_lit16_7");
            fail("expected a verification exception");
        } catch (Throwable t) {
            DxUtil.checkVerifyException(t);
        }
    }
    public void testVFE3() {
        try {
            Class.forName("dot.junit.opcodes.and_int_lit16.d.T_and_int_lit16_8");
            fail("expected a verification exception");
        } catch (Throwable t) {
            DxUtil.checkVerifyException(t);
        }
    }
    public void testVFE5() {
        try {
            Class.forName("dot.junit.opcodes.and_int_lit16.d.T_and_int_lit16_10");
            fail("expected a verification exception");
        } catch (Throwable t) {
            DxUtil.checkVerifyException(t);
        }
    }
}
