public class Test_if_nez extends DxTestCase {
    public void testN1() {
        T_if_nez_1 t = new T_if_nez_1();
        assertEquals(1, t.run(5));
        assertEquals(1, t.run(-5));
    }
    public void testN2() {
        T_if_nez_2 t = new T_if_nez_2();
        String str = null;
        assertEquals(1234, t.run(str));
    }
    public void testN3() {
        T_if_nez_2 t = new T_if_nez_2();
        String str = "abc";
        assertEquals(1, t.run(str));
    }
    public void testN4() {
        T_if_nez_3 t = new T_if_nez_3();
        assertEquals(1, t.run(3.123f));
    }
    public void testB1() {
        T_if_nez_1 t = new T_if_nez_1();
        assertEquals(1, t.run(Integer.MAX_VALUE));
    }
    public void testB2() {
        T_if_nez_1 t = new T_if_nez_1();
        assertEquals(1, t.run(Integer.MIN_VALUE));
    }
    public void testB3() {
        T_if_nez_1 t = new T_if_nez_1();
        assertEquals(1234, t.run(0));
    }
    public void testB4() {
        T_if_nez_4 t = new T_if_nez_4();
        assertEquals(1234, t.run(null));
    }
    public void testVFE1() {
        try {
            Class.forName("dot.junit.opcodes.if_nez.d.T_if_nez_5");
            fail("expected a verification exception");
        } catch (Throwable t) {
            DxUtil.checkVerifyException(t);
        }
    }
    public void testVFE2() {
        try {
            Class.forName("dot.junit.opcodes.if_nez.d.T_if_nez_6");
            fail("expected a verification exception");
        } catch (Throwable t) {
            DxUtil.checkVerifyException(t);
        }
    }
    public void testVFE3() {
        try {
            Class.forName("dot.junit.opcodes.if_nez.d.T_if_nez_7");
            fail("expected a verification exception");
        } catch (Throwable t) {
            DxUtil.checkVerifyException(t);
        }
    }
    public void testVFE5() {
        try {
            Class.forName("dot.junit.opcodes.if_nez.d.T_if_nez_9");
            fail("expected a verification exception");
        } catch (Throwable t) {
            DxUtil.checkVerifyException(t);
        }
    }
    public void testVFE6() {
        try {
            Class.forName("dot.junit.opcodes.if_nez.d.T_if_nez_10");
            fail("expected a verification exception");
        } catch (Throwable t) {
            DxUtil.checkVerifyException(t);
        }
    }
    public void testVFE7() {
        try {
            Class.forName("dot.junit.opcodes.if_nez.d.T_if_nez_11");
            fail("expected a verification exception");
        } catch (Throwable t) {
            DxUtil.checkVerifyException(t);
        }
    }
}
