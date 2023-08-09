public class Test_and_int_lit8 extends DxTestCase {
    public void testN1() {
        T_and_int_lit8_1 t = new T_and_int_lit8_1();
        assertEquals(8, t.run());
    }
    public void testN2() {
        T_and_int_lit8_2 t = new T_and_int_lit8_2();
        assertEquals(-8, t.run());
    }
    public void testN3() {
        T_and_int_lit8_3 t = new T_and_int_lit8_3();
        assertEquals(0xcafe, t.run());
    }
    public void testN4() {
        T_and_int_lit8_9 t = new T_and_int_lit8_9();
        try {
            t.run(3.14f);
        } catch (Throwable e) {
        }
    }    
    public void testB1() {
        T_and_int_lit8_4 t = new T_and_int_lit8_4();
        assertEquals(0, t.run());
    }
    public void testB2() {
        T_and_int_lit8_5 t = new T_and_int_lit8_5();
        assertEquals(0, t.run());
    }
    public void testVFE1() {
        try {
            Class.forName("dot.junit.opcodes.and_int_lit8.d.T_and_int_lit8_6");
            fail("expected a verification exception");
        } catch (Throwable t) {
            DxUtil.checkVerifyException(t);
        }
    }
    public void testVFE2() {
        try {
            Class.forName("dot.junit.opcodes.and_int_lit8.d.T_and_int_lit8_7");
            fail("expected a verification exception");
        } catch (Throwable t) {
            DxUtil.checkVerifyException(t);
        }
    }
    public void testVFE3() {
        try {
            Class.forName("dot.junit.opcodes.and_int_lit8.d.T_and_int_lit8_8");
            fail("expected a verification exception");
        } catch (Throwable t) {
            DxUtil.checkVerifyException(t);
        }
    }
    public void testVFE5() {
        try {
            Class.forName("dot.junit.opcodes.and_int_lit8.d.T_and_int_lit8_10");
            fail("expected a verification exception");
        } catch (Throwable t) {
            DxUtil.checkVerifyException(t);
        }
    }
}
