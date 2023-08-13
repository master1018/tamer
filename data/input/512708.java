public class Test_lshr extends DxTestCase {
    public void testN1() {
        T_lshr_1 t = new T_lshr_1();
        assertEquals(5000000000l, t.run(40000000000l, 3));
    }
    public void testN2() {
        T_lshr_1 t = new T_lshr_1();
        assertEquals(20000000000l, t.run(40000000000l, 1));
    }
    public void testN3() {
        T_lshr_1 t = new T_lshr_1();
        assertEquals(-20000000000l, t.run(-40000000000l, 1));
    }
    public void testN4() {
        T_lshr_1 t = new T_lshr_1();
        assertEquals(0l, t.run(1l, -1));
    }
    public void testN5() {
        T_lshr_1 t = new T_lshr_1();
        assertEquals(32, t.run(65l, 65));
    }
    public void testB1() {
        T_lshr_1 t = new T_lshr_1();
        assertEquals(0l, t.run(0l, -1));
    }
    public void testB2() {
        T_lshr_1 t = new T_lshr_1();
        assertEquals(1l, t.run(1l, 0));
    }
    public void testB3() {
        T_lshr_1 t = new T_lshr_1();
        assertEquals(0x3FFFFFFFFFFFFFFFl, t.run(Long.MAX_VALUE, 1));
    }
    public void testB4() {
        T_lshr_1 t = new T_lshr_1();
        assertEquals(0xc000000000000000l, t.run(Long.MIN_VALUE, 1));
    }
    public void testVFE1() {
        try {
            Class.forName("dxc.junit.opcodes.lshr.jm.T_lshr_2");
            fail("expected a verification exception");
        } catch (Throwable t) {
            DxUtil.checkVerifyException(t);
        }
    }
    public void testVFE2() {
        try {
            Class.forName("dxc.junit.opcodes.lshr.jm.T_lshr_3");
            fail("expected a verification exception");
        } catch (Throwable t) {
            DxUtil.checkVerifyException(t);
        }
    }
    public void testVFE3() {
        try {
            Class.forName("dxc.junit.opcodes.lshr.jm.T_lshr_4");
            fail("expected a verification exception");
        } catch (Throwable t) {
            DxUtil.checkVerifyException(t);
        }
    }
    public void testVFE4() {
        try {
            Class.forName("dxc.junit.opcodes.lshr.jm.T_lshr_5");
            fail("expected a verification exception");
        } catch (Throwable t) {
            DxUtil.checkVerifyException(t);
        }
    }
    public void testVFE5() {
        try {
            Class.forName("dxc.junit.opcodes.lshr.jm.T_lshr_6");
            fail("expected a verification exception");
        } catch (Throwable t) {
            DxUtil.checkVerifyException(t);
        }
    }
}
