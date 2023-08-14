public class Test_opc_const extends DxTestCase {
    public void testN1() {
        T_opc_const_1 t = new T_opc_const_1();
        float a = 1.5f;
        float b = 0.04f;
        assertEquals(a + b, t.run(), 0f);
        assertEquals(1.54f, t.run(), 0f);
    }
    public void testN2() {
        T_opc_const_2 t = new T_opc_const_2();
         int a = 10000000;
         int b = 10000000;
        assertEquals(a + b, t.run());
    }
    public void testVFE1() {
        try {
            Class.forName("dot.junit.opcodes.opc_const.d.T_opc_const_3");
            fail("expected a verification exception");
        } catch (Throwable t) {
            DxUtil.checkVerifyException(t);
        }
    }
    public void testVFE2() {
        try {
            Class.forName("dot.junit.opcodes.opc_const.d.T_opc_const_4");
            fail("expected a verification exception");
        } catch (Throwable t) {
            DxUtil.checkVerifyException(t);
        }
    }
}
