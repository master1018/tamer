public class Test_bipush extends DxTestCase {
    public void testN1() {
        T_bipush_1 t = new T_bipush_1();
        assertEquals(100, t.run());
    }
    public void testB1() {
        T_bipush_2 t = new T_bipush_2();
        assertEquals(0, t.run());
    }
    public void testB2() {
        T_bipush_3 t = new T_bipush_3();
        assertEquals(-1, t.run());
    }
    public void testVFE1() {
        try {
            Class.forName("dxc.junit.opcodes.bipush.jm.T_bipush_4");
            fail("expected a verification exception");
        } catch (Throwable t) {
            DxUtil.checkVerifyException(t);
        }
    }
}
