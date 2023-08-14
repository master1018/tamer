public class Test_fneg extends DxTestCase {
    public void testN1() {
        T_fneg_1 t = new T_fneg_1();
        assertEquals(-1f, t.run(1f));
    }
    public void testN2() {
        T_fneg_1 t = new T_fneg_1();
        assertEquals(1f, t.run(-1f));
    }
    public void testN3() {
        T_fneg_1 t = new T_fneg_1();
        assertEquals(-0f, t.run(+0f));
    }
    public void testN4() {
        T_fneg_1 t = new T_fneg_1();
        assertEquals(2.7f, t.run(-2.7f));
    }
    public void testB1() {
        T_fneg_1 t = new T_fneg_1();
        assertEquals(Float.NaN, t.run(Float.NaN));
    }
    public void testB2() {
        T_fneg_1 t = new T_fneg_1();
        assertEquals(Float.POSITIVE_INFINITY, t.run(Float.NEGATIVE_INFINITY));
    }
    public void testB3() {
        T_fneg_1 t = new T_fneg_1();
        assertEquals(Float.NEGATIVE_INFINITY, t.run(Float.POSITIVE_INFINITY));
    }
    public void testB4() {
        T_fneg_1 t = new T_fneg_1();
        assertEquals(-3.4028235E38f, t.run(Float.MAX_VALUE));
    }
    public void testB5() {
        T_fneg_1 t = new T_fneg_1();
        assertEquals(-1.4E-45f, t.run(Float.MIN_VALUE));
    }
    public void testVFE1() {
        try {
            Class.forName("dxc.junit.opcodes.fneg.jm.T_fneg_2");
            fail("expected a verification exception");
        } catch (Throwable t) {
            DxUtil.checkVerifyException(t);
        }
    }
    public void testVFE2() {
        try {
            Class.forName("dxc.junit.opcodes.fneg.jm.T_fneg_3");
            fail("expected a verification exception");
        } catch (Throwable t) {
            DxUtil.checkVerifyException(t);
        }
    }
    public void testVFE3() {
        try {
            Class.forName("dxc.junit.opcodes.fneg.jm.T_fneg_4");
            fail("expected a verification exception");
        } catch (Throwable t) {
            DxUtil.checkVerifyException(t);
        }
    }
    public void testVFE4() {
        try {
            Class.forName("dxc.junit.opcodes.fneg.jm.T_fneg_5");
            fail("expected a verification exception");
        } catch (Throwable t) {
            DxUtil.checkVerifyException(t);
        }
    }
}
