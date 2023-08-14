public class Test_div_long_2addr extends DxTestCase {
    public void testN1() {
        T_div_long_2addr_1 t = new T_div_long_2addr_1();
        assertEquals(2l, t.run(100000000000l, 40000000000l));
    }
    public void testN2() {
        T_div_long_2addr_1 t = new T_div_long_2addr_1();
        assertEquals(8l, t.run(98765432123456l, 12345678912345l));
    }
    public void testN3() {
        T_div_long_2addr_1 t = new T_div_long_2addr_1();
        assertEquals(0l, t.run(0l, 98765432123456l));
    }
    public void testN4() {
        T_div_long_2addr_1 t = new T_div_long_2addr_1();
        assertEquals(-8, t.run(-98765432123456l, 12345678912345l));
    }
    public void testN5() {
        T_div_long_2addr_1 t = new T_div_long_2addr_1();
        assertEquals(-8, t.run(98765432123456l, -12345678912345l));
    }
    public void testN6() {
        T_div_long_2addr_1 t = new T_div_long_2addr_1();
        assertEquals(80l, t.run(-98765432123456l, -1234567891234l));
    }
    public void testN7() {
        T_div_long_2addr_2 t = new T_div_long_2addr_2();
        try {
            t.run(1234l, 3.1415d);
        } catch (Throwable e) {
        }
    }    
    public void testB1() {
        T_div_long_2addr_1 t = new T_div_long_2addr_1();
        assertEquals(-9223372036854775808L, t.run(Long.MIN_VALUE, -1));
    }
    public void testB2() {
        T_div_long_2addr_1 t = new T_div_long_2addr_1();
        assertEquals(-9223372036854775808L, t.run(Long.MIN_VALUE, 1));
    }
    public void testB3() {
        T_div_long_2addr_1 t = new T_div_long_2addr_1();
        assertEquals(9223372036854775807L, t.run(Long.MAX_VALUE, 1));
    }
    public void testB4() {
        T_div_long_2addr_1 t = new T_div_long_2addr_1();
        assertEquals(-1, t.run(Long.MIN_VALUE, Long.MAX_VALUE));
    }
    public void testB5() {
        T_div_long_2addr_1 t = new T_div_long_2addr_1();
        assertEquals(0, t.run(1, Long.MAX_VALUE));
    }
    public void testB6() {
        T_div_long_2addr_1 t = new T_div_long_2addr_1();
        assertEquals(0, t.run(1, Long.MIN_VALUE));
    }
    public void testE1() {
        T_div_long_2addr_1 t = new T_div_long_2addr_1();
        try {
            t.run(12345678912345l, 0);
            fail("expected ArithmeticException");
        } catch (ArithmeticException ae) {
        }
    }
    public void testVFE1() {
        try {
            Class.forName("dot.junit.opcodes.div_long_2addr.d.T_div_long_2addr_6");
            fail("expected a verification exception");
        } catch (Throwable t) {
            DxUtil.checkVerifyException(t);
        }
    }
    public void testVFE2() {
        try {
            Class.forName("dot.junit.opcodes.div_long_2addr.d.T_div_long_2addr_3");
            fail("expected a verification exception");
        } catch (Throwable t) {
            DxUtil.checkVerifyException(t);
        }
    }
    public void testVFE3() {
        try {
            Class.forName("dot.junit.opcodes.div_long_2addr.d.T_div_long_2addr_4");
            fail("expected a verification exception");
        } catch (Throwable t) {
            DxUtil.checkVerifyException(t);
        }
    }
    public void testVFE4() {
        try {
            Class.forName("dot.junit.opcodes.div_long_2addr.d.T_div_long_2addr_5");
            fail("expected a verification exception");
        } catch (Throwable t) {
            DxUtil.checkVerifyException(t);
        }
    }
}
