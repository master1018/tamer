public class Test_fcmpl extends DxTestCase {
    public void testN1() {
        T_fcmpl_1 t = new T_fcmpl_1();
        assertEquals(1, t.run(3.14f, 2.7f));
    }
    public void testN2() {
        T_fcmpl_1 t = new T_fcmpl_1();
        assertEquals(-1, t.run(-3.14f, 2.7f));
    }
    public void testN3() {
        T_fcmpl_1 t = new T_fcmpl_1();
        assertEquals(0, t.run(3.14f, 3.14f));
    }
    public void testB1() {
        T_fcmpl_1 t = new T_fcmpl_1();
        assertEquals(-1, t.run(Float.NaN, Float.MAX_VALUE));
    }
    public void testB2() {
        T_fcmpl_1 t = new T_fcmpl_1();
        assertEquals(0, t.run(+0f, -0f));
    }
    public void testB3() {
        T_fcmpl_1 t = new T_fcmpl_1();
        assertEquals(-1, t.run(Float.NEGATIVE_INFINITY, Float.MIN_VALUE));
    }
    public void testB4() {
        T_fcmpl_1 t = new T_fcmpl_1();
        assertEquals(1, t.run(Float.POSITIVE_INFINITY, Float.MAX_VALUE));
    }
    public void testB5() {
        T_fcmpl_1 t = new T_fcmpl_1();
        assertEquals(1, t.run(Float.POSITIVE_INFINITY, Float.NEGATIVE_INFINITY));
    }
    public void testVFE1() {
        try {
            Class.forName("dxc.junit.opcodes.fcmpl.jm.T_fcmpl_2");
            fail("expected a verification exception");
        } catch (Throwable t) {
            DxUtil.checkVerifyException(t);
        }
    }
    public void testVFE2() {
        try {
            Class.forName("dxc.junit.opcodes.fcmpl.jm.T_fcmpl_3");
            fail("expected a verification exception");
        } catch (Throwable t) {
            DxUtil.checkVerifyException(t);
        }
    }
    public void testVFE3() {
        try {
            Class.forName("dxc.junit.opcodes.fcmpl.jm.T_fcmpl_4");
            fail("expected a verification exception");
        } catch (Throwable t) {
            DxUtil.checkVerifyException(t);
        }
    }
    public void testVFE4() {
        try {
            Class.forName("dxc.junit.opcodes.fcmpl.jm.T_fcmpl_5");
            fail("expected a verification exception");
        } catch (Throwable t) {
            DxUtil.checkVerifyException(t);
        }
    }
}
