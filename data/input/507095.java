public class Test_ineg extends DxTestCase {
    public void testN1() {
        T_ineg_1 t = new T_ineg_1();
        assertEquals(-1, t.run(1));
    }
    public void testN2() {
        T_ineg_1 t = new T_ineg_1();
        assertEquals(1, t.run(-1));
    }
    public void testN3() {
        T_ineg_1 t = new T_ineg_1();
        assertEquals(-32768, t.run(32768));
    }
    public void testN4() {
        T_ineg_1 t = new T_ineg_1();
        assertEquals(0, t.run(0));
    }
    public void testN5() {
        T_ineg_2 t = new T_ineg_2();
        assertTrue(t.run(12345));
    }
    public void testB1() {
        T_ineg_1 t = new T_ineg_1();
        assertEquals(0x80000001, t.run(Integer.MAX_VALUE));
    }
    public void testB2() {
        T_ineg_1 t = new T_ineg_1();
        assertEquals(Integer.MIN_VALUE, t.run(Integer.MIN_VALUE));
    }
    public void testVFE1() {
        try {
            Class.forName("dxc.junit.opcodes.ineg.jm.T_ineg_3");
            fail("expected a verification exception");
        } catch (Throwable t) {
            DxUtil.checkVerifyException(t);
        }
    }
    public void testVFE2() {
        try {
            Class.forName("dxc.junit.opcodes.ineg.jm.T_ineg_4");
            fail("expected a verification exception");
        } catch (Throwable t) {
            DxUtil.checkVerifyException(t);
        }
    }
    public void testVFE3() {
        try {
            Class.forName("dxc.junit.opcodes.ineg.jm.T_ineg_5");
            fail("expected a verification exception");
        } catch (Throwable t) {
            DxUtil.checkVerifyException(t);
        }
    }
    public void testVFE4() {
        try {
            Class.forName("dxc.junit.opcodes.ineg.jm.T_ineg_6");
            fail("expected a verification exception");
        } catch (Throwable t) {
            DxUtil.checkVerifyException(t);
        }
    }
}
