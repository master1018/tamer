public class Test_not_int extends DxTestCase {
    public void testN1() {
        T_not_int_1 t = new T_not_int_1();
        assertEquals(-6, t.run(5));
        assertEquals(-257, t.run(256));
    }
    public void testN2() {
        T_not_int_1 t = new T_not_int_1();
        assertEquals(4, t.run(-5));
        assertEquals(255, t.run(-256));
    }
    public void testN3() {
        T_not_int_1 t = new T_not_int_1();
        assertEquals(-0xcaff, t.run(0xcafe));
        assertEquals(-0x12d, t.run(0x12c));
    }
    public void testN4() {
        T_not_int_2 t = new T_not_int_2();
        try {
            t.run(3.14f);
        } catch (Throwable e) {
        }
    }
    public void testB1() {
        T_not_int_1 t = new T_not_int_1();
        assertEquals(Integer.MIN_VALUE, t.run(Integer.MAX_VALUE));
    }
    public void testB2() {
        T_not_int_1 t = new T_not_int_1();
        assertEquals(Integer.MAX_VALUE, t.run(Integer.MIN_VALUE));
    }
    public void testB3() {
        T_not_int_1 t = new T_not_int_1();
        assertEquals(-2, t.run(1));
    }
    public void testB4() {
        T_not_int_1 t = new T_not_int_1();
        assertEquals(-1, t.run(0));
    }
    public void testB5() {
        T_not_int_1 t = new T_not_int_1();
        assertEquals(0, t.run(-1));
    }
    public void testB6() {
        T_not_int_1 t = new T_not_int_1();
        assertEquals(Short.MIN_VALUE, t.run(Short.MAX_VALUE));
    }
    public void testB7() {
        T_not_int_1 t = new T_not_int_1();
        assertEquals(Short.MAX_VALUE, t.run(Short.MIN_VALUE));
    }
    public void testVFE1() {
        try {
            Class.forName("dot.junit.opcodes.not_int.d.T_not_int_3");
            fail("expected a verification exception");
        } catch (Throwable t) {
            DxUtil.checkVerifyException(t);
        }
    }
    public void testVFE2() {
        try {
            Class.forName("dot.junit.opcodes.not_int.d.T_not_int_4");
            fail("expected a verification exception");
        } catch (Throwable t) {
            DxUtil.checkVerifyException(t);
        }
    }
    public void testVFE3() {
        try {
            Class.forName("dot.junit.opcodes.not_int.d.T_not_int_5");
            fail("expected a verification exception");
        } catch (Throwable t) {
            DxUtil.checkVerifyException(t);
        }
    }
    public void testVFE4() {
        try {
            Class.forName("dot.junit.opcodes.not_int.d.T_not_int_6");
            fail("expected a verification exception");
        } catch (Throwable t) {
            DxUtil.checkVerifyException(t);
        }
    }
}
