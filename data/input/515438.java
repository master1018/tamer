public class Test_if_ne extends DxTestCase {
    public void testN1() {
        T_if_ne_1 t = new T_if_ne_1();
        assertEquals(1, t.run(5, 6));
    }
    public void testN2() {
        T_if_ne_1 t = new T_if_ne_1();
        assertEquals(1234, t.run(0x0f0e0d0c, 0x0f0e0d0c));
    }
    public void testN3() {
        T_if_ne_1 t = new T_if_ne_1();
        assertEquals(1, t.run(5, -5));
    }
    public void testN4() {
        T_if_ne_1 t = new T_if_ne_1();
        assertEquals(1, t.run(0x01001234, 0x1234));
    }
    public void testN5() {
        T_if_ne_2 t = new T_if_ne_2();
        String a = "a";
        String b = "b";
        assertEquals(1, t.run(a, b));
    }
    public void testN6() {
        T_if_ne_2 t = new T_if_ne_2();
        String a = "a";
        assertEquals(1234, t.run(a, a));
    }
    public void testN7() {
        T_if_ne_4 t = new T_if_ne_4();
        assertEquals(1, t.run(1f, 1));
    }  
    public void testB1() {
        T_if_ne_1 t = new T_if_ne_1();
        assertEquals(1234, t.run(Integer.MAX_VALUE, Integer.MAX_VALUE));
    }
    public void testB2() {
        T_if_ne_1 t = new T_if_ne_1();
        assertEquals(1234, t.run(Integer.MIN_VALUE, Integer.MIN_VALUE));
    }
    public void testB3() {
        T_if_ne_1 t = new T_if_ne_1();
        assertEquals(1, t.run(0, 1234567));
    }
    public void testB4() {
        T_if_ne_1 t = new T_if_ne_1();
        assertEquals(1234, t.run(0, 0));
    }
    public void testB5() {
        T_if_ne_2 t = new T_if_ne_2();
        String a = "a";
        assertEquals(1, t.run(null, a));
    }
    public void testVFE1() {
        try {
            Class.forName("dot.junit.opcodes.if_ne.d.T_if_ne_5");
            fail("expected a verification exception");
        } catch (Throwable t) {
            DxUtil.checkVerifyException(t);
        }
    }
    public void testVFE2() {
        try {
            Class.forName("dot.junit.opcodes.if_ne.d.T_if_ne_7");
            fail("expected a verification exception");
        } catch (Throwable t) {
            DxUtil.checkVerifyException(t);
        }
    }
    public void testVFE3() {
        try {
            Class.forName("dot.junit.opcodes.if_ne.d.T_if_ne_8");
            fail("expected a verification exception");
        } catch (Throwable t) {
            DxUtil.checkVerifyException(t);
        }
    }
    public void testVFE4() {
        try {
            Class.forName("dot.junit.opcodes.if_ne.d.T_if_ne_9");
            fail("expected a verification exception");
        } catch (Throwable t) {
            DxUtil.checkVerifyException(t);
        }
    }
    public void testVFE5() {
        try {
            Class.forName("dot.junit.opcodes.if_ne.d.T_if_ne_10");
            fail("expected a verification exception");
        } catch (Throwable t) {
            DxUtil.checkVerifyException(t);
        }
    }
    public void testVFE6() {
        try {
            Class.forName("dot.junit.opcodes.if_ne.d.T_if_ne_11");
            fail("expected a verification exception");
        } catch (Throwable t) {
            DxUtil.checkVerifyException(t);
        }
    }
    public void testVFE7() {
        try {
            Class.forName("dot.junit.opcodes.if_ne.d.T_if_ne_12");
            fail("expected a verification exception");
        } catch (Throwable t) {
            DxUtil.checkVerifyException(t);
        }
    }
}
