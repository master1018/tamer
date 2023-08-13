public class Test_f2d extends DxTestCase {
    public void testN1() {
        T_f2d_1 t = new T_f2d_1();
        assertEquals(0.5d, t.run(0.5f), 0d);
    }
    public void testN2() {
        T_f2d_1 t = new T_f2d_1();
        assertEquals(1d, t.run(1), 0d);
    }
    public void testN3() {
        T_f2d_1 t = new T_f2d_1();
        assertEquals(-1d, t.run(-1), 0d);
    }
    public void testB1() {
        T_f2d_1 t = new T_f2d_1();
        double r = 0x1.fffffeP+127d;
        assertEquals(r, t.run(Float.MAX_VALUE), 0d);
    }
    public void testB2() {
        T_f2d_1 t = new T_f2d_1();
        double r = 0x0.000002P-126d;
        assertEquals(r, t.run(Float.MIN_VALUE), 0d);
    }
    public void testB3() {
        T_f2d_1 t = new T_f2d_1();
        assertEquals(-0d, t.run(-0), 0d);
    }
    public void testB4() {
        T_f2d_1 t = new T_f2d_1();
        assertTrue(Double.isNaN(t.run(Float.NaN)));
    }
    public void testB5() {
        T_f2d_1 t = new T_f2d_1();
        assertTrue(Double.isInfinite(t.run(Float.POSITIVE_INFINITY)));
    }
    public void testB6() {
        T_f2d_1 t = new T_f2d_1();
        assertTrue(Double.isInfinite(t.run(Float.NEGATIVE_INFINITY)));
    }
    public void testVFE1() {
        try {
            Class.forName("dxc.junit.opcodes.f2d.jm.T_f2d_2");
            fail("expected a verification exception");
        } catch (Throwable t) {
            DxUtil.checkVerifyException(t);
        }
    }
    public void testVFE2() {
        try {
            Class.forName("dxc.junit.opcodes.f2d.jm.T_f2d_3");
            fail("expected a verification exception");
        } catch (Throwable t) {
            DxUtil.checkVerifyException(t);
        }
    }
    public void testVFE3() {
        try {
            Class.forName("dxc.junit.opcodes.f2d.jm.T_f2d_4");
            fail("expected a verification exception");
        } catch (Throwable t) {
            DxUtil.checkVerifyException(t);
        }
    }
    public void testVFE4() {
        try {
            Class.forName("dxc.junit.opcodes.f2d.jm.T_f2d_5");
            fail("expected a verification exception");
        } catch (Throwable t) {
            DxUtil.checkVerifyException(t);
        }
    }
    public void testVFE5() {
        try {
            Class.forName("dxc.junit.opcodes.f2d.jm.T_f2d_6");
            fail("expected a verification exception");
        } catch (Throwable t) {
            DxUtil.checkVerifyException(t);
        }
    }
}
