public class Test_f2l extends DxTestCase {
    public void testN1() {
        T_f2l_1 t = new T_f2l_1();
        assertEquals(2l, t.run(2.999999f));
    }
    public void testN2() {
         T_f2l_1 t = new T_f2l_1();
         assertEquals(1l, t.run(1));
    }
    public void testN3() {
         T_f2l_1 t = new T_f2l_1();
         assertEquals(-1l, t.run(-1));
    }
    public void testB1() {
        T_f2l_1 t = new T_f2l_1();
        assertEquals(Long.MAX_VALUE, t.run(Float.MAX_VALUE));
    }
    public void testB2() {
        T_f2l_1 t = new T_f2l_1();
        assertEquals(0, t.run(Float.MIN_VALUE));
    }
    public void testB3() {
        T_f2l_1 t = new T_f2l_1();
        assertEquals(0l, t.run(0));
    }
    public void testB4() {
        T_f2l_1 t = new T_f2l_1();
        assertEquals(0l, t.run(Float.NaN));
    }
    public void testB5() {
        T_f2l_1 t = new T_f2l_1();
        assertEquals(Long.MAX_VALUE, t.run(Float.POSITIVE_INFINITY));
    }
    public void testB6() {
        T_f2l_1 t = new T_f2l_1();
        assertEquals(Long.MIN_VALUE, t.run(Float.NEGATIVE_INFINITY));
    }
    public void testVFE1() {
        try
        {
            Class.forName("dxc.junit.opcodes.f2l.jm.T_f2l_2");
            fail("expected a verification exception");
        } catch (Throwable t) {
            DxUtil.checkVerifyException(t);
        }
    }
    public void testVFE2() {
        try
        {
            Class.forName("dxc.junit.opcodes.f2l.jm.T_f2l_3");
            fail("expected a verification exception");
        } catch (Throwable t) {
            DxUtil.checkVerifyException(t);
        }
    }
    public void testVFE3() {
        try
        {
            Class.forName("dxc.junit.opcodes.f2l.jm.T_f2l_4");
            fail("expected a verification exception");
        } catch (Throwable t) {
            DxUtil.checkVerifyException(t);
        }
    }
    public void testVFE4() {
        try
        {
            Class.forName("dxc.junit.opcodes.f2l.jm.T_f2l_5");
            fail("expected a verification exception");
        } catch (Throwable t) {
            DxUtil.checkVerifyException(t);
        }
    }
    public void testVFE5() {
        try
        {
            Class.forName("dxc.junit.opcodes.f2l.jm.T_f2l_6");
            fail("expected a verification exception");
        } catch (Throwable t) {
            DxUtil.checkVerifyException(t);
        }
    }
}
