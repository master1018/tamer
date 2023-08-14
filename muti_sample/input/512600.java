public class Test_lmul extends DxTestCase {
    public void testN1() {
        T_lmul_1 t = new T_lmul_1();
        assertEquals(3195355577426903040l, t.run(222000000000l, 5000000000l));
    }
    public void testN2() {
        T_lmul_1 t = new T_lmul_1();
        assertEquals(-15241578750190521l, t.run(-123456789l, 123456789l));
    }
    public void testN3() {
        T_lmul_1 t = new T_lmul_1();
        assertEquals(15241578750190521l, t.run(-123456789l, -123456789l));
    }
    public void testB1() {
        T_lmul_1 t = new T_lmul_1();
        assertEquals(0, t.run(0, Long.MAX_VALUE));
    }
    public void testB2() {
        T_lmul_1 t = new T_lmul_1();
        assertEquals(9223372036854775807L, t.run(Long.MAX_VALUE, 1));
    }
    public void testB3() {
        T_lmul_1 t = new T_lmul_1();
        assertEquals(-9223372036854775808L, t.run(Long.MIN_VALUE, 1));
    }
    public void testB4() {
        T_lmul_1 t = new T_lmul_1();
        assertEquals(-9223372036854775808L, t.run(Long.MAX_VALUE,
                Long.MIN_VALUE));
    }
    public void testB5() {
        T_lmul_1 t = new T_lmul_1();
        assertEquals(0, t.run(0, 0));
    }
    public void testB6() {
        T_lmul_1 t = new T_lmul_1();
        assertEquals(-9223372036854775807L, t.run(Long.MAX_VALUE, -1));
    }
    public void testB7() {
        T_lmul_1 t = new T_lmul_1();
        assertEquals(-9223372036854775808L, t.run(Long.MIN_VALUE, -1));
    }
    public void testVFE1() {
        try {
            Class.forName("dxc.junit.opcodes.lmul.jm.T_lmul_2");
            fail("expected a verification exception");
        } catch (Throwable t) {
            DxUtil.checkVerifyException(t);
        }
    }
    public void testVFE2() {
        try {
            Class.forName("dxc.junit.opcodes.lmul.jm.T_lmul_3");
            fail("expected a verification exception");
        } catch (Throwable t) {
            DxUtil.checkVerifyException(t);
        }
    }
    public void testVFE3() {
        try {
            Class.forName("dxc.junit.opcodes.lmul.jm.T_lmul_4");
            fail("expected a verification exception");
        } catch (Throwable t) {
            DxUtil.checkVerifyException(t);
        }
    }
    public void testVFE4() {
        try {
            Class.forName("dxc.junit.opcodes.lmul.jm.T_lmul_5");
            fail("expected a verification exception");
        } catch (Throwable t) {
            DxUtil.checkVerifyException(t);
        }
    }
    public void testVFE5() {
        try {
            Class.forName("dxc.junit.opcodes.lmul.jm.T_lmul_6");
            fail("expected a verification exception");
        } catch (Throwable t) {
            DxUtil.checkVerifyException(t);
        }
    }
    public void testVFE6() {
        try {
            Class.forName("dxc.junit.opcodes.lmul.jm.T_lmul_7");
            fail("expected a verification exception");
        } catch (Throwable t) {
            DxUtil.checkVerifyException(t);
        }
    }
}
