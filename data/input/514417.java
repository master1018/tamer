public class Test_ldc2_w extends DxTestCase {
    public void testN1() {
        T_ldc2_w_1 t = new T_ldc2_w_1();
        long a = 1234567890122l;
        long b = 1l;
        assertEquals(a + b, t.run());
    }
    public void testN2() {
        T_ldc2_w_2 t = new T_ldc2_w_2();
        double a = 1234567890123232323232232323232323232323232323456788d;
        double b = 1d;
        assertEquals(a + b, t.run(), 0d);
    }
    public void testVFE1() {
        try {
            Class.forName("dxc.junit.opcodes.ldc2_w.jm.T_ldc2_w_4");
            fail("expected a verification exception");
        } catch (Throwable t) {
            DxUtil.checkVerifyException(t);
        }
    }
    public void testVFE2() {
        try {
            Class.forName("dxc.junit.opcodes.ldc2_w.jm.T_ldc2_w_5");
            fail("expected a verification exception");
        } catch (Throwable t) {
            DxUtil.checkVerifyException(t);
        }
    }
}
