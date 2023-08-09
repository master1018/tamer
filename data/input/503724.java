public class Test_float_to_long extends DxTestCase {
    public void testN1() {
        T_float_to_long_1 t = new T_float_to_long_1();
        assertEquals(2l, t.run(2.999999f));
    }
    public void testN2() {
         T_float_to_long_1 t = new T_float_to_long_1();
         assertEquals(1l, t.run(1));
    }
    public void testN3() {
         T_float_to_long_1 t = new T_float_to_long_1();
         assertEquals(-1l, t.run(-1));
    }
    public void testN4() {
        T_float_to_long_7 t = new T_float_to_long_7();
        try {
            t.run(1);
        } catch (Throwable e) {
        }
    } 
    public void testB1() {
        T_float_to_long_1 t = new T_float_to_long_1();
        assertEquals(Long.MAX_VALUE, t.run(Float.MAX_VALUE));
    }
    public void testB2() {
        T_float_to_long_1 t = new T_float_to_long_1();
        assertEquals(0, t.run(Float.MIN_VALUE));
    }
    public void testB3() {
        T_float_to_long_1 t = new T_float_to_long_1();
        assertEquals(0l, t.run(0));
    }
    public void testB4() {
        T_float_to_long_1 t = new T_float_to_long_1();
        assertEquals(0l, t.run(Float.NaN));
    }
    public void testB5() {
        T_float_to_long_1 t = new T_float_to_long_1();
        assertEquals(Long.MAX_VALUE, t.run(Float.POSITIVE_INFINITY));
    }
    public void testB6() {
        T_float_to_long_1 t = new T_float_to_long_1();
        assertEquals(Long.MIN_VALUE, t.run(Float.NEGATIVE_INFINITY));
    }
    public void testVFE1() {
        try
        {
            Class.forName("dxc.junit.opcodes.float_to_long.jm.T_float_to_long_2");
            fail("expected a verification exception");
        } catch (Throwable t) {
            DxUtil.checkVerifyException(t);
        }
    }
    public void testVFE2() {
        try
        {
            Class.forName("dot.junit.opcodes.float_to_long.d.T_float_to_long_2");
            fail("expected a verification exception");
        } catch (Throwable t) {
            DxUtil.checkVerifyException(t);
        }
    }
    public void testVFE3() {
        try
        {
            Class.forName("dot.junit.opcodes.float_to_long.d.T_float_to_long_3");
            fail("expected a verification exception");
        } catch (Throwable t) {
            DxUtil.checkVerifyException(t);
        }
    }
    public void testVFE4() {
        try
        {
            Class.forName("dot.junit.opcodes.float_to_long.d.T_float_to_long_4");
            fail("expected a verification exception");
        } catch (Throwable t) {
            DxUtil.checkVerifyException(t);
        }
    }
    public void testVFE5() {
        try
        {
            Class.forName("dot.junit.opcodes.float_to_long.d.T_float_to_long_5");
            fail("expected a verification exception");
        } catch (Throwable t) {
            DxUtil.checkVerifyException(t);
        }
    }
    public void testVFE6() {
        try {
            Class.forName("dot.junit.opcodes.float_to_long.d.T_float_to_long_6");
            fail("expected a verification exception");
        } catch (Throwable t) {
            DxUtil.checkVerifyException(t);
        }
    }
}
