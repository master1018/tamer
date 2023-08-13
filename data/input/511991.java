public class Test_pop extends DxTestCase {
    public void testN1() {
        T_pop_1 t = new T_pop_1();
        assertEquals(1234, t.run());
    }
    public void testN2() {
        T_pop_2 t = new T_pop_2();
        assertEquals(1234f, t.run());
    }
    public void testVFE1() {
        try {
            Class.forName("dxc.junit.opcodes.pop.jm.T_pop_3");
            fail("expected a verification exception");
        } catch (Throwable t) {
            DxUtil.checkVerifyException(t);
        }
    }
    public void testVFE2() {
        try {
            Class.forName("dxc.junit.opcodes.pop.jm.T_pop_4");
            fail("expected a verification exception");
        } catch (Throwable t) {
            DxUtil.checkVerifyException(t);
        }
    }
}
