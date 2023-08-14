public class Test_ishl extends DxTestCase {
    public void testN1() {
        T_ishl_1 t = new T_ishl_1();
        assertEquals(30, t.run(15, 1));
    }
    public void testN2() {
        T_ishl_1 t = new T_ishl_1();
        assertEquals(132, t.run(33, 2));
    }
    public void testN3() {
        T_ishl_1 t = new T_ishl_1();
        assertEquals(-30, t.run(-15, 1));
    }
    public void testN4() {
        T_ishl_1 t = new T_ishl_1();
        assertEquals(0x80000000, t.run(1, -1));
    }
    public void testN5() {
        T_ishl_1 t = new T_ishl_1();
        assertEquals(66, t.run(33, 33));
    }
    public void testB1() {
        T_ishl_1 t = new T_ishl_1();
        assertEquals(0, t.run(0, -1));
    }
    public void testB2() {
        T_ishl_1 t = new T_ishl_1();
        assertEquals(0xfffffffe, t.run(Integer.MAX_VALUE, 1));
    }
    public void testB3() {
        T_ishl_1 t = new T_ishl_1();
        assertEquals(0, t.run(Integer.MIN_VALUE, 1));
    }
    public void testB4() {
        T_ishl_1 t = new T_ishl_1();
        assertEquals(1, t.run(1, 0));
    }
    public void testVFE1() {
        try {
            Class.forName("dxc.junit.opcodes.ishl.jm.T_ishl_2");
            fail("expected a verification exception");
        } catch (Throwable t) {
            DxUtil.checkVerifyException(t);
        }
    }
    public void testVFE2() {
        try {
            Class.forName("dxc.junit.opcodes.ishl.jm.T_ishl_3");
            fail("expected a verification exception");
        } catch (Throwable t) {
            DxUtil.checkVerifyException(t);
        }
    }
    public void testVFE3() {
        try {
            Class.forName("dxc.junit.opcodes.ishl.jm.T_ishl_4");
            fail("expected a verification exception");
        } catch (Throwable t) {
            DxUtil.checkVerifyException(t);
        }
    }
    public void testVFE4() {
        try {
            Class.forName("dxc.junit.opcodes.ishl.jm.T_ishl_5");
            fail("expected a verification exception");
        } catch (Throwable t) {
            DxUtil.checkVerifyException(t);
        }
    }
}
