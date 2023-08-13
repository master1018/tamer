public class Test_array_length extends DxTestCase {
    public void testN1() {
        T_array_length_1 t = new T_array_length_1();
        String[] a = new String[5];
        assertEquals(5, t.run(a));
    }
    public void testN2() {
        T_array_length_4 t = new T_array_length_4();
        double[] a = new double[10];
        assertEquals(10, t.run(a));
    }
    public void testNPE1() {
        T_array_length_1 t = new T_array_length_1();
        try {
            t.run(null);
            fail("NPE expected");
        } catch (NullPointerException npe) {
        }
    }
    public void testVFE1() {
        try {
            Class.forName("dxc.junit.opcodes.array_length.jm.T_array_length_2");
            fail("expected a verification exception");
        } catch (Throwable t) {
            DxUtil.checkVerifyException(t);
        }
    }
    public void testVFE2() {
        try {
            Class.forName("dxc.junit.opcodes.array_length.jm.T_array_length_3");
            fail("expected a verification exception");
        } catch (Throwable t) {
            DxUtil.checkVerifyException(t);
        }
    }
    public void testVFE3() {
        try {
            Class.forName("dxc.junit.opcodes.array_length.jm.T_array_length_5");
            fail("expected a verification exception");
        } catch (Throwable t) {
            DxUtil.checkVerifyException(t);
        }
    }
}
