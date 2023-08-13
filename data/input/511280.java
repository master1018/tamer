public class Test_sub_long_2addr extends DxTestCase {
    public void testN1() {
        T_sub_long_2addr_1 t = new T_sub_long_2addr_1();
        assertEquals(1100016237128l, t.run(1111127348242l, 11111111114l));
    }
    public void testN2() {
        T_sub_long_2addr_1 t = new T_sub_long_2addr_1();
        assertEquals(-1111127348242l, t.run(0, 1111127348242l));
    }
    public void testN3() {
        T_sub_long_2addr_1 t = new T_sub_long_2addr_1();
        assertEquals(11111111114l, t.run(0, -11111111114l));
    }
    public void testN4() {
        T_sub_long_2addr_2 t = new  T_sub_long_2addr_2();
        try {
            t.run(12345l, 3.14d);
        } catch (Throwable e) {
        }
    }  
    public void testB1() {
        T_sub_long_2addr_1 t = new T_sub_long_2addr_1();
        assertEquals(-9223372036854775807L, t.run(0l, Long.MAX_VALUE));
    }
    public void testB2() {
        T_sub_long_2addr_1 t = new T_sub_long_2addr_1();
        assertEquals(0l, t.run(9223372036854775807L, Long.MAX_VALUE));
    }
    public void testB3() {
        T_sub_long_2addr_1 t = new T_sub_long_2addr_1();
        assertEquals(-9223372036854775808L, t.run(Long.MAX_VALUE, -1l));
    }
    public void testB4() {
        T_sub_long_2addr_1 t = new T_sub_long_2addr_1();
        assertEquals(9223372036854775807L, t.run(Long.MIN_VALUE, 1l));
    }
    public void testB5() {
        T_sub_long_2addr_1 t = new T_sub_long_2addr_1();
        assertEquals(0l, t.run(0l, 0l));
    }
    public void testB6() {
        T_sub_long_2addr_1 t = new T_sub_long_2addr_1();
        assertEquals(-9223372036854775808L, t.run(0l, -Long.MIN_VALUE));
    }
    public void testVFE1() {
        try {
            Class.forName("dot.junit.opcodes.sub_long_2addr.d.T_sub_long_2addr_6");
            fail("expected a verification exception");
        } catch (Throwable t) {
            DxUtil.checkVerifyException(t);
        }
    }
    public void testVFE3() {
        try {
            Class.forName("dot.junit.opcodes.sub_long_2addr.d.T_sub_long_2addr_3");
            fail("expected a verification exception");
        } catch (Throwable t) {
            DxUtil.checkVerifyException(t);
        }
    }
    public void testVFE4() {
        try {
            Class.forName("dot.junit.opcodes.sub_long_2addr.d.T_sub_long_2addr_4");
            fail("expected a verification exception");
        } catch (Throwable t) {
            DxUtil.checkVerifyException(t);
        }
    }
    public void testVFE5() {
        try {
            Class.forName("dot.junit.opcodes.sub_long_2addr.d.T_sub_long_2addr_5");
            fail("expected a verification exception");
        } catch (Throwable t) {
            DxUtil.checkVerifyException(t);
        }
    }
}
