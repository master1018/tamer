public class Test_sub_int extends DxTestCase {
    public void testN1() {
        T_sub_int_1 t = new T_sub_int_1();
        assertEquals(4, t.run(8, 4));
    }
    public void testN2() {
        T_sub_int_1 t = new T_sub_int_1();
        assertEquals(-255, t.run(0, 255));
    }
    public void testN3() {
        T_sub_int_1 t = new T_sub_int_1();
        assertEquals(65536, t.run(0, -65536));
    }
    public void testN4() {
        T_sub_int_1 t = new T_sub_int_1();
        assertEquals(Integer.MAX_VALUE, t.run(0, -2147483647));
    }
    public void testN5() {
        T_sub_int_5 t = new  T_sub_int_5();
        try {
            t.run(1, 3.14f);
        } catch (Throwable e) {
        }
    }
    public void testB1() {
        T_sub_int_1 t = new T_sub_int_1();
        assertEquals(-2147483647, t.run(0, Integer.MAX_VALUE));
    }
    public void testB2() {
        T_sub_int_1 t = new T_sub_int_1();
        assertEquals(0, t.run(Integer.MAX_VALUE, Integer.MAX_VALUE));
    }
    public void testB3() {
        T_sub_int_1 t = new T_sub_int_1();
        assertEquals(Integer.MIN_VALUE, t.run(Integer.MAX_VALUE, -1));
    }
    public void testB4() {
        T_sub_int_1 t = new T_sub_int_1();
        assertEquals(Integer.MAX_VALUE, t.run(Integer.MIN_VALUE, 1));
    }
    public void testB5() {
        T_sub_int_1 t = new T_sub_int_1();
        assertEquals(0, t.run(0, 0));
    }
    public void testB6() {
        T_sub_int_1 t = new T_sub_int_1();
        assertEquals(-2147483648, t.run(0, -Integer.MIN_VALUE));
    }
    public void testB7() {
        T_sub_int_1 t = new T_sub_int_1();
        assertEquals(2147483646, t.run(Integer.MAX_VALUE, 1));
    }
    public void testB8() {
        T_sub_int_1 t = new T_sub_int_1();
        assertEquals(-2147483647, t.run(1, Integer.MIN_VALUE));
    }
    public void testB9() {
        T_sub_int_1 t = new T_sub_int_1();
        assertEquals(-1, t.run(Integer.MAX_VALUE, Integer.MIN_VALUE));
    }
    public void testVFE2() {
        try {
            Class.forName("dot.junit.opcodes.sub_int.d.T_sub_int_2");
            fail("expected a verification exception");
        } catch (Throwable t) {
            DxUtil.checkVerifyException(t);
        }
    }
    public void testVFE3() {
        try {
            Class.forName("dot.junit.opcodes.sub_int.d.T_sub_int_3");
            fail("expected a verification exception");
        } catch (Throwable t) {
            DxUtil.checkVerifyException(t);
        }
    }
    public void testVFE4() {
        try {
            Class.forName("dot.junit.opcodes.sub_int.d.T_sub_int_4");
            fail("expected a verification exception");
        } catch (Throwable t) {
            DxUtil.checkVerifyException(t);
        }
    }
    public void testVFE5() {
        try {
            Class.forName("dot.junit.opcodes.sub_int.d.T_sub_int_6");
            fail("expected a verification exception");
        } catch (Throwable t) {
            DxUtil.checkVerifyException(t);
        }
    }
}
