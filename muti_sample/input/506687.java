public class Test_sipush extends DxTestCase {
    public void testN1() {
        T_sipush_1 t = new T_sipush_1();
        assertEquals(-13570, t.run());
    }
    public void testB1() {
        T_sipush_2 t = new T_sipush_2();
        assertEquals(0, t.run());
    }
    public void testB2() {
        T_sipush_3 t = new T_sipush_3();
        assertEquals(-1, t.run());
    }
    public void testVFE1() {
        try {
            Class.forName("dxc.junit.opcodes.sipush.jm.T_sipush_4");
            fail("expected a verification exception");
        } catch (Throwable t) {
            DxUtil.checkVerifyException(t);
        }
    }
}
