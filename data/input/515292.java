public class Test_frem extends DxTestCase {
    public void testN1() {
        T_frem_1 t = new T_frem_1();
        assertEquals(2.7f, t.run(2.7f, 3.14f));
    }
    public void testN2() {
        T_frem_1 t = new T_frem_1();
        assertEquals(0f, t.run(0, 3.14f));
    }
    public void testN3() {
        T_frem_1 t = new T_frem_1();
        assertEquals(-0.44000006f, t.run(-3.14f, 2.7f));
    }
    public void testB1() {
        T_frem_1 t = new T_frem_1();
        assertEquals(Float.NaN, t.run(Float.MAX_VALUE, Float.NaN));
    }
    public void testB2() {
        T_frem_1 t = new T_frem_1();
        assertEquals(Float.NaN, t.run(Float.POSITIVE_INFINITY,
                Float.NEGATIVE_INFINITY));
    }
    public void testB3() {
        T_frem_1 t = new T_frem_1();
        assertEquals(Float.NaN, t.run(Float.POSITIVE_INFINITY, -2.7f));
    }
    public void testB4() {
        T_frem_1 t = new T_frem_1();
        assertEquals(-2.7f, t.run(-2.7f, Float.NEGATIVE_INFINITY));
    }
    public void testB5() {
        T_frem_1 t = new T_frem_1();
        assertEquals(Float.NaN, t.run(0, 0));
    }
    public void testB6() {
        T_frem_1 t = new T_frem_1();
        assertEquals(0f, t.run(0, -2.7f));
    }
    public void testB7() {
        T_frem_1 t = new T_frem_1();
        assertEquals(Float.NaN, t.run(-2.7f, 0));
    }
    public void testB8() {
        T_frem_1 t = new T_frem_1();
        assertEquals(0f, t.run(1, Float.MIN_VALUE));
    }
    public void testB9() {
        T_frem_1 t = new T_frem_1();
        assertEquals(7.2584893E-10f, t.run(Float.MAX_VALUE, -1E-9f));
    }
    public void testVFE1() {
        try {
            Class.forName("dxc.junit.opcodes.frem.jm.T_frem_2");
            fail("expected a verification exception");
        } catch (Throwable t) {
            DxUtil.checkVerifyException(t);
        }
    }
    public void testVFE2() {
        try {
            Class.forName("dxc.junit.opcodes.frem.jm.T_frem_3");
            fail("expected a verification exception");
        } catch (Throwable t) {
            DxUtil.checkVerifyException(t);
        }
    }
    public void testVFE3() {
        try {
            Class.forName("dxc.junit.opcodes.frem.jm.T_frem_4");
            fail("expected a verification exception");
        } catch (Throwable t) {
            DxUtil.checkVerifyException(t);
        }
    }
    public void testVFE4() {
        try {
            Class.forName("dxc.junit.opcodes.frem.jm.T_frem_5");
            fail("expected a verification exception");
        } catch (Throwable t) {
            DxUtil.checkVerifyException(t);
        }
    }
}
