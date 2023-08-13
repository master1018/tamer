public class Test_f2i extends DxTestCase {
    public void testN1() {
        T_f2i_1 t = new T_f2i_1();
        assertEquals(2, t.run(2.999999f));
    }
    public void testN2() {
        T_f2i_1 t = new T_f2i_1();
        assertEquals(1, t.run(1f));
    }
    public void testN3() {
        T_f2i_1 t = new T_f2i_1();
        assertEquals(-1, t.run(-1f));
    }
    public void testB1() {
        T_f2i_1 t = new T_f2i_1();
        assertEquals(0, t.run(-0f));
    }
    public void testB2() {
        T_f2i_1 t = new T_f2i_1();
        assertEquals(Integer.MAX_VALUE, t.run(Float.MAX_VALUE));
    }
    public void testB3() {
        T_f2i_1 t = new T_f2i_1();
        assertEquals(0, t.run(Float.MIN_VALUE));
    }
    public void testB4() {
        T_f2i_1 t = new T_f2i_1();
        assertEquals(0, t.run(Float.NaN));
    }
    public void testB5() {
        T_f2i_1 t = new T_f2i_1();
        assertEquals(Integer.MAX_VALUE, t.run(Float.POSITIVE_INFINITY));
    }
    public void testB6() {
        T_f2i_1 t = new T_f2i_1();
        assertEquals(Integer.MIN_VALUE, t.run(Float.NEGATIVE_INFINITY));
    }
    public void testVFE1() {
        try {
            Class.forName("dxc.junit.opcodes.f2i.jm.T_f2i_2");
            fail("expected a verification exception");
        } catch (Throwable t) {
            DxUtil.checkVerifyException(t);
        }
    }
    public void testVFE2() {
        try {
            Class.forName("dxc.junit.opcodes.f2i.jm.T_f2i_3");
            fail("expected a verification exception");
        } catch (Throwable t) {
            DxUtil.checkVerifyException(t);
        }
    }
    public void testVFE3() {
        try {
            Class.forName("dxc.junit.opcodes.f2i.jm.T_f2i_4");
            fail("expected a verification exception");
        } catch (Throwable t) {
            DxUtil.checkVerifyException(t);
        }
    }
    public void testVFE4() {
        try {
            Class.forName("dxc.junit.opcodes.f2i.jm.T_f2i_5");
            fail("expected a verification exception");
        } catch (Throwable t) {
            DxUtil.checkVerifyException(t);
        }
    }
}
