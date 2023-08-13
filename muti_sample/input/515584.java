public class Test_ldc_w extends DxTestCase {
    public void testN1() {
        T_ldc_w_1 t = new T_ldc_w_1();
        float a = 1.5f;
        float b = 0.04f;
        assertEquals(a + b, t.run(), 0f);
        assertEquals(1.54f, t.run(), 0f);
    }
    public void testN2() {
        T_ldc_w_2 t = new T_ldc_w_2();
        int a = 1000000000;
        int b = 1000000000;
        assertEquals(a + b, t.run());
    }
    public void testVFE1() {
        try {
            Class.forName("dxc.junit.opcodes.ldc_w.jm.T_ldc_w_4");
            fail("expected a verification exception");
        } catch (Throwable t) {
            DxUtil.checkVerifyException(t);
        }
    }
    public void testVFE2() {
        try {
            Class.forName("dxc.junit.opcodes.ldc_w.jm.T_ldc_w_5");
            fail("expected a verification exception");
        } catch (Throwable t) {
            DxUtil.checkVerifyException(t);
        }
    }
}
