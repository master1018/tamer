public class Test_imul extends DxTestCase {
    public void testN1() {
        T_imul_1 t = new T_imul_1();
        assertEquals(32, t.run(8, 4));
    }
    public void testN2() {
        T_imul_1 t = new T_imul_1();
        assertEquals(-510, t.run(-2, 255));
    }
    public void testN3() {
        T_imul_1 t = new T_imul_1();
        assertEquals(-4, t.run(0x7ffffffe, 2));
    }
    public void testN4() {
        T_imul_1 t = new T_imul_1();
        assertEquals(4, t.run(4, 0x80000001));
    }
    public void testB1() {
        T_imul_1 t = new T_imul_1();
        assertEquals(0, t.run(0, Integer.MAX_VALUE));
    }
    public void testB2() {
        T_imul_1 t = new T_imul_1();
        assertEquals(Integer.MAX_VALUE, t.run(Integer.MAX_VALUE, 1));
    }
    public void testB3() {
        T_imul_1 t = new T_imul_1();
        assertEquals(Integer.MIN_VALUE, t.run(Integer.MIN_VALUE, 1));
    }
    public void testB4() {
        T_imul_1 t = new T_imul_1();
        assertEquals(Integer.MIN_VALUE, t.run(Integer.MAX_VALUE,
                Integer.MIN_VALUE));
    }
    public void testB5() {
        T_imul_1 t = new T_imul_1();
        assertEquals(0, t.run(0, 0));
    }
    public void testVFE1() {
        try {
            Class.forName("dxc.junit.opcodes.imul.jm.T_imul_2");
            fail("expected a verification exception");
        } catch (Throwable t) {
            DxUtil.checkVerifyException(t);
        }
    }
    public void testVFE2() {
        try {
            Class.forName("dxc.junit.opcodes.imul.jm.T_imul_3");
            fail("expected a verification exception");
        } catch (Throwable t) {
            DxUtil.checkVerifyException(t);
        }
    }
    public void testVFE3() {
        try {
            Class.forName("dxc.junit.opcodes.imul.jm.T_imul_4");
            fail("expected a verification exception");
        } catch (Throwable t) {
            DxUtil.checkVerifyException(t);
        }
    }
    public void testVFE4() {
        try {
            Class.forName("dxc.junit.opcodes.imul.jm.T_imul_5");
            fail("expected a verification exception");
        } catch (Throwable t) {
            DxUtil.checkVerifyException(t);
        }
    }
}
