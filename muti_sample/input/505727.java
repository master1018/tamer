public class Test_arraylength extends DxTestCase {
    public void testN1() {
        T_arraylength_1 t = new T_arraylength_1();
        String[] a = new String[5];
        assertEquals(5, t.run(a));
    }
    public void testNPE1() {
        T_arraylength_1 t = new T_arraylength_1();
        try {
            t.run(null);
            fail("NPE expected");
        } catch (NullPointerException npe) {
        }
    }
    public void testVFE2() {
        try {
            Class.forName("dxc.junit.opcodes.arraylength.jm.T_arraylength_2");
            fail("expected a verification exception");
        } catch (Throwable t) {
            DxUtil.checkVerifyException(t);
        }
    }
    public void testVFE1() {
        try {
            Class.forName("dxc.junit.opcodes.arraylength.jm.T_arraylength_3");
            fail("expected a verification exception");
        } catch (Throwable t) {
            DxUtil.checkVerifyException(t);
        }
    }
}
