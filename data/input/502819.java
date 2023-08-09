public class Test_cmp_long extends DxTestCase {
    public void testN1() {
        T_cmp_long_1 t = new T_cmp_long_1();
        assertEquals(1, t.run(111234567891l, 111234567890l));
    }
    public void testN2() {
        T_cmp_long_1 t = new T_cmp_long_1();
        assertEquals(0, t.run(112234567890l, 112234567890l));
    }
    public void testN3() {
        T_cmp_long_1 t = new T_cmp_long_1();
        assertEquals(-1, t.run(112234567890l, 998876543210l));
    }
    public void testN4() {
         T_cmp_long_2 t = new T_cmp_long_2();
        try {
            t.run(123l, 3.145f);
        } catch (Throwable e) {
        }
    }  
    public void testB1() {
        T_cmp_long_1 t = new T_cmp_long_1();
        assertEquals(1, t.run(Long.MAX_VALUE, Long.MIN_VALUE));
    }
    public void testB2() {
        T_cmp_long_1 t = new T_cmp_long_1();
        assertEquals(-1, t.run(Long.MIN_VALUE, Long.MAX_VALUE));
    }
    public void testB3() {
        T_cmp_long_1 t = new T_cmp_long_1();
        assertEquals(1, t.run(1l, 0l));
    }
    public void testB4() {
        T_cmp_long_1 t = new T_cmp_long_1();
        assertEquals(1, t.run(0l, -1l));
    }
    public void testB5() {
        T_cmp_long_1 t = new T_cmp_long_1();
        assertEquals(-1, t.run(-1l, 0l));
    }
    public void testB6() {
        T_cmp_long_1 t = new T_cmp_long_1();
        assertEquals(0, t.run(0l, 0l));
    }
    public void testVFE1() {
        try {
            Class.forName("dot.junit.opcodes.cmp_long.d.T_cmp_long_3");
            fail("expected a verification exception");
        } catch (Throwable t) {
            DxUtil.checkVerifyException(t);
        }
    }
    public void testVFE2() {
        try {
            Class.forName("dot.junit.opcodes.cmp_long.d.T_cmp_long_4");
            fail("expected a verification exception");
        } catch (Throwable t) {
            DxUtil.checkVerifyException(t);
        }
    }
    public void testVFE3() {
        try {
            Class.forName("dot.junit.opcodes.cmp_long.d.T_cmp_long_5");
            fail("expected a verification exception");
        } catch (Throwable t) {
            DxUtil.checkVerifyException(t);
        }
    }
    public void testVFE4() {
        try {
            Class.forName("dot.junit.opcodes.cmp_long.d.T_cmp_long_6");
            fail("expected a verification exception");
        } catch (Throwable t) {
            DxUtil.checkVerifyException(t);
        }
    }
}
