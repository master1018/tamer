public class Test_dneg extends DxTestCase {
    public void testN1() {
        T_dneg_1 t = new T_dneg_1();
        assertEquals(-1d, t.run(1d));
    }
    public void testN2() {
        T_dneg_1 t = new T_dneg_1();
        assertEquals(1d, t.run(-1d));
    }
    public void testN3() {
        T_dneg_1 t = new T_dneg_1();
        assertEquals(-0d, t.run(+0d));
    }
    public void testN4() {
        T_dneg_1 t = new T_dneg_1();
        assertEquals(2.7d, t.run(-2.7d));
    }
    public void testB1() {
        T_dneg_1 t = new T_dneg_1();
        assertEquals(Double.NaN, t.run(Double.NaN));
    }
    public void testB2() {
        T_dneg_1 t = new T_dneg_1();
        assertEquals(Double.POSITIVE_INFINITY, t.run(Double.NEGATIVE_INFINITY));
    }
    public void testB3() {
        T_dneg_1 t = new T_dneg_1();
        assertEquals(Double.NEGATIVE_INFINITY, t.run(Double.POSITIVE_INFINITY));
    }
    public void testB4() {
        T_dneg_1 t = new T_dneg_1();
        assertEquals(-1.7976931348623157E308d, t.run(Double.MAX_VALUE));
    }
    public void testB5() {
        T_dneg_1 t = new T_dneg_1();
        assertEquals(-4.9E-324d, t.run(Double.MIN_VALUE));
    }
    public void testVFE1() {
        try {
            Class.forName("dxc.junit.opcodes.dneg.jm.T_dneg_2");
            fail("expected a verification exception");
        } catch (Throwable t) {
            DxUtil.checkVerifyException(t);
        }
    }
    public void testVFE2() {
        try {
            Class.forName("dxc.junit.opcodes.dneg.jm.T_dneg_3");
            fail("expected a verification exception");
        } catch (Throwable t) {
            DxUtil.checkVerifyException(t);
        }
    }
    public void testVFE3() {
        try {
            Class.forName("dxc.junit.opcodes.dneg.jm.T_dneg_4");
            fail("expected a verification exception");
        } catch (Throwable t) {
            DxUtil.checkVerifyException(t);
        }
    }
    public void testVFE4() {
        try {
            Class.forName("dxc.junit.opcodes.dneg.jm.T_dneg_5");
            fail("expected a verification exception");
        } catch (Throwable t) {
            DxUtil.checkVerifyException(t);
        }
    }
}
