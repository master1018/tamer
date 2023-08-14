public class Test_ushr_int extends DxTestCase {
    public void testN1() {
        T_ushr_int_1 t = new T_ushr_int_1();
        assertEquals(7, t.run(15, 1));
    }
    public void testN2() {
        T_ushr_int_1 t = new T_ushr_int_1();
        assertEquals(8, t.run(33, 2));
    }
    public void testN3() {
        T_ushr_int_1 t = new T_ushr_int_1();
        assertEquals(0x7FFFFFF8, t.run(-15, 1));
    }
    public void testN4() {
        T_ushr_int_1 t = new T_ushr_int_1();
        assertEquals(0, t.run(1, -1));
    }
    public void testN5() {
        T_ushr_int_1 t = new T_ushr_int_1();
        assertEquals(16, t.run(33, 33));
    }
    public void testN6() {
        T_ushr_int_5 t = new  T_ushr_int_5();
        try {
            t.run(1, 3.14f);
        } catch (Throwable e) {
        }
    }
    public void testB1() {
        T_ushr_int_1 t = new T_ushr_int_1();
        assertEquals(0, t.run(0, -1));
    }
    public void testB2() {
        T_ushr_int_1 t = new T_ushr_int_1();
        assertEquals(0x3FFFFFFF, t.run(Integer.MAX_VALUE, 1));
    }
    public void testB3() {
        T_ushr_int_1 t = new T_ushr_int_1();
        assertEquals(0x40000000, t.run(Integer.MIN_VALUE, 1));
    }
    public void testB4() {
        T_ushr_int_1 t = new T_ushr_int_1();
        assertEquals(1, t.run(1, 0));
    }
    public void testVFE1() {
        try {
            Class.forName("dot.junit.opcodes.ushr_int.d.T_ushr_int_2");
            fail("expected a verification exception");
        } catch (Throwable t) {
            DxUtil.checkVerifyException(t);
        }
    }
    public void testVFE2() {
        try {
            Class.forName("dot.junit.opcodes.ushr_int.d.T_ushr_int_3");
            fail("expected a verification exception");
        } catch (Throwable t) {
            DxUtil.checkVerifyException(t);
        }
    }
    public void testVFE3() {
        try {
            Class.forName("dot.junit.opcodes.ushr_int.d.T_ushr_int_4");
            fail("expected a verification exception");
        } catch (Throwable t) {
            DxUtil.checkVerifyException(t);
        }
    }
    public void testVFE4() {
        try {
            Class.forName("dot.junit.opcodes.ushr_int.d.T_ushr_int_6");
            fail("expected a verification exception");
        } catch (Throwable t) {
            DxUtil.checkVerifyException(t);
        }
    }
}
