public class Test_sub_float extends DxTestCase {
    public void testN1() {
        T_sub_float_1 t = new T_sub_float_1();
        assertEquals(-0.44000006f, t.run(2.7f, 3.14f));
    }
    public void testN2() {
        T_sub_float_1 t = new T_sub_float_1();
        assertEquals(3.14f, t.run(0, -3.14f));
    }
    public void testN3() {
        T_sub_float_1 t = new T_sub_float_1();
        assertEquals(-0.44000006f, t.run(-3.14f, -2.7f));
    }
    public void testN4() {
        T_sub_float_5 t = new T_sub_float_5();
        try {
            t.run(1, 3.14f);
        } catch (Throwable e) {
        }
    }
    public void testB1() {
        T_sub_float_1 t = new T_sub_float_1();
        assertEquals(Float.NaN, t.run(Float.MAX_VALUE, Float.NaN));
    }
    public void testB2() {
        T_sub_float_1 t = new T_sub_float_1();
        assertEquals(Float.POSITIVE_INFINITY, t.run(Float.POSITIVE_INFINITY,
                Float.NEGATIVE_INFINITY));
    }
    public void testB3() {
        T_sub_float_1 t = new T_sub_float_1();
        assertEquals(Float.NaN, t.run(Float.POSITIVE_INFINITY,
                Float.POSITIVE_INFINITY));
    }
    public void testB4() {
        T_sub_float_1 t = new T_sub_float_1();
        assertEquals(Float.POSITIVE_INFINITY, t.run(Float.POSITIVE_INFINITY,
                -2.7f));
    }
    public void testB5() {
        T_sub_float_1 t = new T_sub_float_1();
        assertEquals(+0f, t.run(+0f, -0f));
    }
    public void testB6() {
        T_sub_float_1 t = new T_sub_float_1();
        assertEquals(0f, t.run(-0f, -0f));
    }
    public void testB7() {
        T_sub_float_1 t = new T_sub_float_1();
        assertEquals(+0f, t.run(+0f, +0f));
    }
    public void testB8() {
        T_sub_float_1 t = new T_sub_float_1();
        assertEquals(0f, t.run(2.7f, 2.7f));
    }
    public void testB9() {
        T_sub_float_1 t = new T_sub_float_1();
        assertEquals(0f, t.run(Float.MAX_VALUE, Float.MAX_VALUE));
    }
    public void testB10() {
        T_sub_float_1 t = new T_sub_float_1();
        assertEquals(0f, t.run(Float.MIN_VALUE, 1.4E-45f));
    }
    public void testB11() {
        T_sub_float_1 t = new T_sub_float_1();
        assertEquals(Float.POSITIVE_INFINITY, t.run(Float.MAX_VALUE,
                -3.402823E+38F));
    }
    public void testVFE2() {
        try {
            Class.forName("dot.junit.opcodes.sub_float.d.T_sub_float_2");
            fail("expected a verification exception");
        } catch (Throwable t) {
            DxUtil.checkVerifyException(t);
        }
    }
    public void testVFE3() {
        try {
            Class.forName("dot.junit.opcodes.sub_float.d.T_sub_float_3");
            fail("expected a verification exception");
        } catch (Throwable t) {
            DxUtil.checkVerifyException(t);
        }
    }
    public void testVFE4() {
        try {
            Class.forName("dot.junit.opcodes.sub_float.d.T_sub_float_4");
            fail("expected a verification exception");
        } catch (Throwable t) {
            DxUtil.checkVerifyException(t);
        }
    }
    public void testVFE5() {
        try {
            Class.forName("dot.junit.opcodes.sub_float.d.T_sub_float_6");
            fail("expected a verification exception");
        } catch (Throwable t) {
            DxUtil.checkVerifyException(t);
        }
    }
}
