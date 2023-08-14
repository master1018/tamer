public class Test_l2f extends DxTestCase {
    public void testN1() {
        T_l2f_1 t = new T_l2f_1();
        assertEquals(1.23456788E14f, t.run(123456789012345l), 0f);
    }
    public void testN2() {
        T_l2f_1 t = new T_l2f_1();
        assertEquals(1f, t.run(1l), 0f);
    }
    public void testN3() {
        T_l2f_1 t = new T_l2f_1();
        assertEquals(-1f, t.run(-1l), 0f);
    }
    public void testB1() {
        T_l2f_1 t = new T_l2f_1();
        assertEquals(9.223372036854776E18, t.run(Long.MAX_VALUE), 0f);
    }
    public void testB2() {
        T_l2f_1 t = new T_l2f_1();
        assertEquals(-9.223372036854776E18, t.run(Long.MIN_VALUE), 0f);
    }
    public void testB3() {
        T_l2f_1 t = new T_l2f_1();
        assertEquals(0f, t.run(0l), 0f);
    }
    public void testVFE1() {
        try {
            Class.forName("dxc.junit.opcodes.l2f.jm.T_l2f_2");
            fail("expected a verification exception");
        } catch (Throwable t) {
            DxUtil.checkVerifyException(t);
        }
    }
    public void testVFE2() {
        try {
            Class.forName("dxc.junit.opcodes.l2f.jm.T_l2f_3");
            fail("expected a verification exception");
        } catch (Throwable t) {
            DxUtil.checkVerifyException(t);
        }
    }
    public void testVFE3() {
        try {
            Class.forName("dxc.junit.opcodes.l2f.jm.T_l2f_4");
            fail("expected a verification exception");
        } catch (Throwable t) {
            DxUtil.checkVerifyException(t);
        }
    }
    public void testVFE4() {
        try {
            Class.forName("dxc.junit.opcodes.l2f.jm.T_l2f_5");
            fail("expected a verification exception");
        } catch (Throwable t) {
            DxUtil.checkVerifyException(t);
        }
    }
}
