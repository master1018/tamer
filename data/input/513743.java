public class Test_ushr_long extends DxTestCase {
    public void testN1() {
        T_ushr_long_1 t = new T_ushr_long_1();
        assertEquals(5000000000l, t.run(40000000000l, 3));
    }
    public void testN2() {
        T_ushr_long_1 t = new T_ushr_long_1();
        assertEquals(20000000000l, t.run(40000000000l, 1));
    }
    public void testN3() {
        T_ushr_long_1 t = new T_ushr_long_1();
        assertEquals(0x7FFFFFFFFC521975l, t.run(-123456789l, 1));
    }
    public void testN4() {
        T_ushr_long_1 t = new T_ushr_long_1();
        assertEquals(0l, t.run(1l, -1));
    }
    public void testN5() {
        T_ushr_long_1 t = new T_ushr_long_1();
        assertEquals(123456789l, t.run(123456789l, 64));
    }
    public void testN6() {
        T_ushr_long_1 t = new T_ushr_long_1();
        assertEquals(0l, t.run(123456789l, 63));
    }
    public void testN7() {
        T_ushr_long_2 t = new T_ushr_long_2();
        try {
            t.run(3.14d, 1);
        } catch (Throwable e) {
        }
    } 
    public void testB1() {
        T_ushr_long_1 t = new T_ushr_long_1();
        assertEquals(0l, t.run(0l, -1));
    }
    public void testB2() {
        T_ushr_long_1 t = new T_ushr_long_1();
        assertEquals(0x3FFFFFFFFFFFFFFFl, t.run(Long.MAX_VALUE, 1));
    }
    public void testB3() {
        T_ushr_long_1 t = new T_ushr_long_1();
        assertEquals(0x4000000000000000l, t.run(Long.MIN_VALUE, 1));
    }
    public void testB4() {
        T_ushr_long_1 t = new T_ushr_long_1();
        assertEquals(1l, t.run(1l, 0));
    }
    public void testVFE1() {
        try {
            Class.forName("dot.junit.opcodes.ushr_long.d.T_ushr_long_6");
            fail("expected a verification exception");
        } catch (Throwable t) {
            DxUtil.checkVerifyException(t);
        }
    }
    public void testVFE2() {
        try {
            Class.forName("dot.junit.opcodes.ushr_long.d.T_ushr_long_7");
            fail("expected a verification exception");
        } catch (Throwable t) {
            DxUtil.checkVerifyException(t);
        }
    }
    public void testVFE3() {
        try {
            Class.forName("dot.junit.opcodes.ushr_long.d.T_ushr_long_3");
            fail("expected a verification exception");
        } catch (Throwable t) {
            DxUtil.checkVerifyException(t);
        }
    }
    public void testVFE4() {
        try {
            Class.forName("dot.junit.opcodes.ushr_long.d.T_ushr_long_4");
            fail("expected a verification exception");
        } catch (Throwable t) {
            DxUtil.checkVerifyException(t);
        }
    }
    public void testVFE5() {
        try {
            Class.forName("dot.junit.opcodes.ushr_long.d.T_ushr_long_5");
            fail("expected a verification exception");
        } catch (Throwable t) {
            DxUtil.checkVerifyException(t);
        }
    }
}
