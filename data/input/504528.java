public class Test_shl_long_2addr extends DxTestCase {
    public void testN1() {
        T_shl_long_2addr_1 t = new T_shl_long_2addr_1();
        assertEquals(40000000000l, t.run(5000000000l, 3));
    }
    public void testN2() {
        T_shl_long_2addr_1 t = new T_shl_long_2addr_1();
        assertEquals(10000000000l, t.run(5000000000l, 1));
    }
    public void testN3() {
        T_shl_long_2addr_1 t = new T_shl_long_2addr_1();
        assertEquals(-10000000000l, t.run(-5000000000l, 1));
    }
    public void testN4() {
        T_shl_long_2addr_1 t = new T_shl_long_2addr_1();
        assertEquals(0x8000000000000000l, t.run(1l, -1));
    }
    public void testN5() {
        T_shl_long_2addr_1 t = new T_shl_long_2addr_1();
        assertEquals(130l, t.run(65l, 65));
    }
    public void testN6() {
        T_shl_long_2addr_7 t = new T_shl_long_2addr_7();
        try {
            t.run(4.67d, 1);
        } catch (Throwable e) {
        }
    }
    public void testB1() {
        T_shl_long_2addr_1 t = new T_shl_long_2addr_1();
        assertEquals(0, t.run(0, -1));
    }
    public void testB2() {
        T_shl_long_2addr_1 t = new T_shl_long_2addr_1();
        assertEquals(1, t.run(1, 0));
    }
    public void testB3() {
        T_shl_long_2addr_1 t = new T_shl_long_2addr_1();
        assertEquals(0xfffffffe, t.run(Long.MAX_VALUE, 1));
    }
    public void testB4() {
        T_shl_long_2addr_1 t = new T_shl_long_2addr_1();
        assertEquals(0l, t.run(Long.MIN_VALUE, 1));
    }
    public void testVFE1() {
        try {
            Class.forName("dot.junit.opcodes.shl_long_2addr.d.T_shl_long_2addr_2");
            fail("expected a verification exception");
        } catch (Throwable t) {
            DxUtil.checkVerifyException(t);
        }
    }
    public void testVFE2() {
        try {
            Class.forName("dot.junit.opcodes.shl_long_2addr.d.T_shl_long_2addr_3");
            fail("expected a verification exception");
        } catch (Throwable t) {
            DxUtil.checkVerifyException(t);
        }
    }
    public void testVFE3() {
        try {
            Class.forName("dot.junit.opcodes.shl_long_2addr.d.T_shl_long_2addr_4");
            fail("expected a verification exception");
        } catch (Throwable t) {
            DxUtil.checkVerifyException(t);
        }
    }
    public void testVFE4() {
        try {
            Class.forName("dot.junit.opcodes.shl_long_2addr.d.T_shl_long_2addr_5");
            fail("expected a verification exception");
        } catch (Throwable t) {
            DxUtil.checkVerifyException(t);
        }
    }
    public void testVFE5() {
        try {
            Class.forName("dot.junit.opcodes.shl_long_2addr.d.T_shl_long_2addr_6");
            fail("expected a verification exception");
        } catch (Throwable t) {
            DxUtil.checkVerifyException(t);
        }
    }
}
