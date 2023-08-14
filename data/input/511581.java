public class Test_lshl extends DxTestCase {
    public void testN1() {
        T_lshl_1 t = new T_lshl_1();
        assertEquals(40000000000l, t.run(5000000000l, 3));
    }
    public void testN2() {
        T_lshl_1 t = new T_lshl_1();
        assertEquals(10000000000l, t.run(5000000000l, 1));
    }
    public void testN3() {
        T_lshl_1 t = new T_lshl_1();
        assertEquals(-10000000000l, t.run(-5000000000l, 1));
    }
    public void testN4() {
        T_lshl_1 t = new T_lshl_1();
        assertEquals(0x8000000000000000l, t.run(1l, -1));
    }
    public void testN5() {
        T_lshl_1 t = new T_lshl_1();
        assertEquals(130l, t.run(65l, 65));
    }
    public void testB1() {
        T_lshl_1 t = new T_lshl_1();
        assertEquals(0, t.run(0, -1));
    }
    public void testB2() {
        T_lshl_1 t = new T_lshl_1();
        assertEquals(1, t.run(1, 0));
    }
    public void testB3() {
        T_lshl_1 t = new T_lshl_1();
        assertEquals(0xfffffffe, t.run(Long.MAX_VALUE, 1));
    }
    public void testB4() {
        T_lshl_1 t = new T_lshl_1();
        assertEquals(0l, t.run(Long.MIN_VALUE, 1));
    }
    public void testVFE1() {
        try {
            Class.forName("dxc.junit.opcodes.lshl.jm.T_lshl_2");
            fail("expected a verification exception");
        } catch (Throwable t) {
            DxUtil.checkVerifyException(t);
        }
    }
    public void testVFE2() {
        try {
            Class.forName("dxc.junit.opcodes.lshl.jm.T_lshl_3");
            fail("expected a verification exception");
        } catch (Throwable t) {
            DxUtil.checkVerifyException(t);
        }
    }
    public void testVFE3() {
        try {
            Class.forName("dxc.junit.opcodes.lshl.jm.T_lshl_4");
            fail("expected a verification exception");
        } catch (Throwable t) {
            DxUtil.checkVerifyException(t);
        }
    }
    public void testVFE4() {
        try {
            Class.forName("dxc.junit.opcodes.lshl.jm.T_lshl_5");
            fail("expected a verification exception");
        } catch (Throwable t) {
            DxUtil.checkVerifyException(t);
        }
    }
    public void testVFE5() {
        try {
            Class.forName("dxc.junit.opcodes.lshl.jm.T_lshl_6");
            fail("expected a verification exception");
        } catch (Throwable t) {
            DxUtil.checkVerifyException(t);
        }
    }
}
