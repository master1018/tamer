public class Test_check_cast extends DxTestCase {
    public void testN1() {
        T_check_cast_1 t = new T_check_cast_1();
        String s = "";
        assertEquals(s, t.run(s));
    }
    public void testN2() {
        T_check_cast_1 t = new T_check_cast_1();
        assertNull(t.run(null));
    }
    public void testN4() {
        T_check_cast_2 t = new T_check_cast_2();
        assertEquals(5, t.run());
    }
    public void testE1() {
        T_check_cast_1 t = new T_check_cast_1();
        try {
            t.run(t);
            fail("expected ClassCastException");
        } catch (ClassCastException iae) {
        }
    }
    public void testVFE1() {
        try {
            Class.forName("dot.junit.opcodes.check_cast.d.T_check_cast_4");
            fail("expected a verification exception");
        } catch (Throwable t) {
            DxUtil.checkVerifyException(t);
        }
    }
    public void testVFE2() {
        try {
            Class.forName("dot.junit.opcodes.check_cast.d.T_check_cast_5");
            fail("expected a verification exception");
        } catch (Throwable t) {
            DxUtil.checkVerifyException(t);
        }
    }
    public void testVFE3() {
        try {
            Class.forName("dot.junit.opcodes.check_cast.d.T_check_cast_8");
            fail("expected a verification exception");
        } catch (Throwable t) {
            DxUtil.checkVerifyException(t);
        }
    }
    public void testVFE4() {
        try {
            Class.forName("dot.junit.opcodes.check_cast.d.T_check_cast_6");
            fail("expected a verification exception");
        } catch (Throwable t) {
            DxUtil.checkVerifyException(t);
        }
    }
    public void testVFE5() {
        try {
            Class.forName("dot.junit.opcodes.check_cast.d.T_check_cast_3");
            fail("expected a verification exception");
        } catch (Throwable t) {
            DxUtil.checkVerifyException(t);
        }
    }
    public void testVFE6() {
        try {
            Class.forName("dot.junit.opcodes.check_cast.d.T_check_cast_7");
            fail("expected a verification exception");
        } catch (Throwable t) {
            DxUtil.checkVerifyException(t);
        }
    }
    public void testVFE7() {
        try {
            Class.forName("dot.junit.opcodes.check_cast.d.T_check_cast_9");
            fail("expected a verification exception");
        } catch (Throwable t) {
            DxUtil.checkVerifyException(t);
        }
    }
}
