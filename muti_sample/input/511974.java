public class Test_add_float extends DxTestCase {
    public void testN1() {
        T_add_float_1 t = new T_add_float_1();
        assertEquals(5.84f, t.run(2.7f, 3.14f));
    }
    public void testN2() {
        T_add_float_1 t = new T_add_float_1();
        assertEquals(-3.14f, t.run(0, -3.14f));
    }
    public void testN3() {
        T_add_float_1 t = new T_add_float_1();
        assertEquals(-5.84f, t.run(-3.14f, -2.7f));
    }
    public void testN5() {
        T_add_float_5 t = new T_add_float_5();
        try {
            t.run(1, 3.14f);
        } catch (Throwable e) {
        }
    }
    public void testB1() {
        T_add_float_1 t = new T_add_float_1();
        assertEquals(Float.POSITIVE_INFINITY, t.run(3.3028235E38f, 0.11E38f));
    }
    public void testB2() {
        T_add_float_1 t = new T_add_float_1();
        assertTrue(Float.isNaN(t.run(Float.POSITIVE_INFINITY,
                Float.NEGATIVE_INFINITY)));
    }
    public void testB3() {
        T_add_float_1 t = new T_add_float_1();
        assertEquals(Float.POSITIVE_INFINITY, t.run(Float.POSITIVE_INFINITY,
                Float.POSITIVE_INFINITY));
    }
    public void testB4() {
        T_add_float_1 t = new T_add_float_1();
        assertEquals(Float.POSITIVE_INFINITY, t.run(Float.POSITIVE_INFINITY,
                -2.7f));
    }
    public void testB5() {
        T_add_float_1 t = new T_add_float_1();
        assertEquals(+0f, t.run(+0f, -0f));
    }
    public void testB6() {
        T_add_float_1 t = new T_add_float_1();
        assertEquals(-0f, t.run(-0f, -0f));
    }
    public void testB7() {
        T_add_float_1 t = new T_add_float_1();
        assertEquals(+0f, t.run(-2.7f, 2.7f));
    }
    public void testB8() {
        T_add_float_1 t = new T_add_float_1();
        assertEquals(Float.POSITIVE_INFINITY, t.run(Float.MAX_VALUE,
                Float.MAX_VALUE));
    }
    public void testB9() {
        T_add_float_1 t = new T_add_float_1();
        assertEquals(0f, t.run(Float.MIN_VALUE, -1.4E-45f));
    }
    public void testVFE1() {
        try {
            Class.forName("dot.junit.opcodes.add_float.d.T_add_float_2");
            fail("expected a verification exception");
        } catch (Throwable t) {
            DxUtil.checkVerifyException(t);
        }
    }
    public void testVFE2() {
        try {
            Class.forName("dot.junit.opcodes.add_float.d.T_add_float_3");
            fail("expected a verification exception");
        } catch (Throwable t) {
            DxUtil.checkVerifyException(t);
        }
    }
    public void testVFE3() {
        try {
            Class.forName("dot.junit.opcodes.add_float.d.T_add_float_4");
            fail("expected a verification exception");
        } catch (Throwable t) {
            DxUtil.checkVerifyException(t);
        }
    }
    public void testVFE4() {
        try {
            Class.forName("dot.junit.opcodes.add_float.d.T_add_float_6");
            fail("expected a verification exception");
        } catch (Throwable t) {
            DxUtil.checkVerifyException(t);
        }
    }
}
