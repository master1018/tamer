public class Test_lneg extends DxTestCase {
    public void testN1() {
        T_lneg_1 t = new T_lneg_1();
        assertEquals(-123123123272432432l, t.run(123123123272432432l));
    }
    public void testN2() {
        T_lneg_1 t = new T_lneg_1();
        assertEquals(-1l, t.run(1l));
    }
    public void testN3() {
        T_lneg_1 t = new T_lneg_1();
        assertEquals(1l, t.run(-1l));
    }
    public void testN4() {
        T_lneg_2 t = new T_lneg_2();
        assertTrue(t.run(123123123272432432l));
    }
    public void testB1() {
        T_lneg_1 t = new T_lneg_1();
        assertEquals(0, t.run(0));
    }
    public void testB2() {
        T_lneg_1 t = new T_lneg_1();
        assertEquals(-9223372036854775807L, t.run(Long.MAX_VALUE));
    }
    public void testB3() {
        T_lneg_1 t = new T_lneg_1();
        assertEquals(-9223372036854775808L, t.run(Long.MIN_VALUE));
    }
    public void testVFE1() {
        try {
            Class.forName("dxc.junit.opcodes.lneg.jm.T_lneg_3");
            fail("expected a verification exception");
        } catch (Throwable t) {
            DxUtil.checkVerifyException(t);
        }
    }
    public void testVFE2() {
        try {
            Class.forName("dxc.junit.opcodes.lneg.jm.T_lneg_4");
            fail("expected a verification exception");
        } catch (Throwable t) {
            DxUtil.checkVerifyException(t);
        }
    }
    public void testVFE3() {
        try {
            Class.forName("dxc.junit.opcodes.lneg.jm.T_lneg_5");
            fail("expected a verification exception");
        } catch (Throwable t) {
            DxUtil.checkVerifyException(t);
        }
    }
    public void testVFE4() {
        try {
            Class.forName("dxc.junit.opcodes.lneg.jm.T_lneg_6");
            fail("expected a verification exception");
        } catch (Throwable t) {
            DxUtil.checkVerifyException(t);
        }
    }
    public void testVFE5() {
        try {
            Class.forName("dxc.junit.opcodes.lneg.jm.T_lneg_7");
            fail("expected a verification exception");
        } catch (Throwable t) {
            DxUtil.checkVerifyException(t);
        }
    }
}
