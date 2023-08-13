public class Test_int_to_long extends DxTestCase {
    public void testN1() {
        T_int_to_long_1 t = new T_int_to_long_1();
        assertEquals(123456l, t.run(123456));
    }
    public void testN2() {
        T_int_to_long_1 t = new T_int_to_long_1();
        assertEquals(1l, t.run(1));
    }
    public void testN3() {
        T_int_to_long_1 t = new T_int_to_long_1();
        assertEquals(-1l, t.run(-1));
    }
    public void testN8() {
        T_int_to_long_6 t = new T_int_to_long_6();
        try {
            t.run(1.333f);
        } catch (Throwable e) {
        }
    } 
    public void testB1() {
        T_int_to_long_1 t = new T_int_to_long_1();
        assertEquals(0l, t.run(0));
    }
    public void testB2() {
        T_int_to_long_1 t = new T_int_to_long_1();
        assertEquals(2147483647l, t.run(Integer.MAX_VALUE));
    }
    public void testB3() {
        T_int_to_long_1 t = new T_int_to_long_1();
        assertEquals(-2147483648l, t.run(Integer.MIN_VALUE));
    }
    public void testVFE1() {
        try {
            Class.forName("dot.junit.opcodes.int_to_long.d.T_int_to_long_2");
            fail("expected a verification exception");
        } catch (Throwable t) {
            DxUtil.checkVerifyException(t);
        }
    }
    public void testVFE2() {
        try {
            Class.forName("dot.junit.opcodes.int_to_long.d.T_int_to_long_3");
            fail("expected a verification exception");
        } catch (Throwable t) {
            DxUtil.checkVerifyException(t);
        }
    }
    public void testVFE3() {
        try {
            Class.forName("dot.junit.opcodes.int_to_long.d.T_int_to_long_4");
            fail("expected a verification exception");
        } catch (Throwable t) {
            DxUtil.checkVerifyException(t);
        }
    }
    public void testVFE4() {
        try {
            Class.forName("dot.junit.opcodes.int_to_long.d.T_int_to_long_5");
            fail("expected a verification exception");
        } catch (Throwable t) {
            DxUtil.checkVerifyException(t);
        }
    }
    public void testVFE5() {
        try {
            Class.forName("dot.junit.opcodes.int_to_long.d.T_int_to_long_7");
            fail("expected a verification exception");
        } catch (Throwable t) {
            DxUtil.checkVerifyException(t);
        }
    }
}
