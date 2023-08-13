public class Test_ishr extends DxTestCase {
    public void testN1() {
        T_ishr_1 t = new T_ishr_1();
        assertEquals(7, t.run(15, 1));
    }
    public void testN2() {
        T_ishr_1 t = new T_ishr_1();
        assertEquals(8, t.run(33, 2));
    }
    public void testN3() {
        T_ishr_1 t = new T_ishr_1();
        assertEquals(-8, t.run(-15, 1));
    }
    public void testN4() {
        T_ishr_1 t = new T_ishr_1();
        assertEquals(0, t.run(1, -1));
    }
    public void testN5() {
        T_ishr_1 t = new T_ishr_1();
        assertEquals(16, t.run(33, 33));
    }
    public void testB1() {
        T_ishr_1 t = new T_ishr_1();
        assertEquals(0, t.run(0, -1));
    }
    public void testB2() {
        T_ishr_1 t = new T_ishr_1();
        assertEquals(0x3FFFFFFF, t.run(Integer.MAX_VALUE, 1));
    }
    public void testB3() {
        T_ishr_1 t = new T_ishr_1();
        assertEquals(0xc0000000, t.run(Integer.MIN_VALUE, 1));
    }
    public void testB4() {
        T_ishr_1 t = new T_ishr_1();
        assertEquals(1, t.run(1, 0));
    }
    public void testVFE1() {
        try {
            Class.forName("dxc.junit.opcodes.ishr.jm.T_ishr_2");
            fail("expected a verification exception");
        } catch (Throwable t) {
            DxUtil.checkVerifyException(t);
        }
    }
    public void testVFE2() {
        try {
            Class.forName("dxc.junit.opcodes.ishr.jm.T_ishr_3");
            fail("expected a verification exception");
        } catch (Throwable t) {
            DxUtil.checkVerifyException(t);
        }
    }
    public void testVFE3() {
        try {
            Class.forName("dxc.junit.opcodes.ishr.jm.T_ishr_4");
            fail("expected a verification exception");
        } catch (Throwable t) {
            DxUtil.checkVerifyException(t);
        }
    }
    public void testVFE4() {
        try {
            Class.forName("dxc.junit.opcodes.ishr.jm.T_ishr_5");
            fail("expected a verification exception");
        } catch (Throwable t) {
            DxUtil.checkVerifyException(t);
        }
    }
}
