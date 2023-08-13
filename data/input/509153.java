public class Test_drem extends DxTestCase {
    public void testN1() {
        T_drem_1 t = new T_drem_1();
        assertEquals(2.7d, t.run(2.7d, 3.14d));
    }
    public void testN2() {
        T_drem_1 t = new T_drem_1();
        assertEquals(0d, t.run(0, 3.14d));
    }
    public void testN3() {
        T_drem_1 t = new T_drem_1();
        assertEquals(-0.43999999999999995d, t.run(-3.14d, 2.7d));
    }
    public void testB1() {
        T_drem_1 t = new T_drem_1();
        assertEquals(Double.NaN, t.run(Double.MAX_VALUE, Double.NaN));
    }
    public void testB2() {
        T_drem_1 t = new T_drem_1();
        assertEquals(Double.NaN, t.run(Double.POSITIVE_INFINITY,
                Double.NEGATIVE_INFINITY));
    }
    public void testB3() {
        T_drem_1 t = new T_drem_1();
        assertEquals(Double.NaN, t.run(Double.POSITIVE_INFINITY, -2.7d));
    }
    public void testB4() {
        T_drem_1 t = new T_drem_1();
        assertEquals(-2.7d, t.run(-2.7d, Double.NEGATIVE_INFINITY));
    }
    public void testB5() {
        T_drem_1 t = new T_drem_1();
        assertEquals(Double.NaN, t.run(0, 0));
    }
    public void testB6() {
        T_drem_1 t = new T_drem_1();
        assertEquals(0d, t.run(0, -2.7d));
    }
    public void testB7() {
        T_drem_1 t = new T_drem_1();
        assertEquals(Double.NaN, t.run(-2.7d, 0));
    }
    public void testB8() {
        T_drem_1 t = new T_drem_1();
        assertEquals(0d, t.run(1, Double.MIN_VALUE));
    }
    public void testB9() {
        T_drem_1 t = new T_drem_1();
        assertEquals(1.543905285031139E-10d, t.run(Double.MAX_VALUE, -1E-9d));
    }
    public void testVFE1() {
        try {
            Class.forName("dxc.junit.opcodes.drem.jm.T_drem_2");
            fail("expected a verification exception");
        } catch (Throwable t) {
            DxUtil.checkVerifyException(t);
        }
    }
    public void testVFE2() {
        try {
            Class.forName("dxc.junit.opcodes.drem.jm.T_drem_3");
            fail("expected a verification exception");
        } catch (Throwable t) {
            DxUtil.checkVerifyException(t);
        }
    }
    public void testVFE3() {
        try {
            Class.forName("dxc.junit.opcodes.drem.jm.T_drem_4");
            fail("expected a verification exception");
        } catch (Throwable t) {
            DxUtil.checkVerifyException(t);
        }
    }
    public void testVFE4() {
        try {
            Class.forName("dxc.junit.opcodes.drem.jm.T_drem_5");
            fail("expected a verification exception");
        } catch (Throwable t) {
            DxUtil.checkVerifyException(t);
        }
    }
}
