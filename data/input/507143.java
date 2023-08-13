public class Test_ldc extends DxTestCase {
    public void testN1() {
        T_ldc_1 t = new T_ldc_1();
        String res = t.run();
        assertEquals(5, res.length());
        assertEquals('h', res.charAt(0));
    }
    public void testN2() {
        T_ldc_2 t = new T_ldc_2();
        float a = 1.5f;
        float b = 0.04f;
        assertEquals(a + b, t.run(), 0f);
        assertEquals(1.54f, t.run(), 0f);
    }
    public void testN3() {
        T_ldc_3 t = new T_ldc_3();
        int a = 1000000000;
        int b = 1000000000;
        assertEquals(a + b, t.run());
    }
    public void testVFE1() {
        try {
            Class.forName("dxc.junit.opcodes.ldc.jm.T_ldc_4");
            fail("expected a verification exception");
        } catch (Throwable t) {
            DxUtil.checkVerifyException(t);
        }
    }
    public void testVFE2() {
        try {
            Class.forName("dxc.junit.opcodes.ldc.jm.T_ldc_5");
            fail("expected a verification exception");
        } catch (Throwable t) {
            DxUtil.checkVerifyException(t);
        }
    }
}
