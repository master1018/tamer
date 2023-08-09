public class Test_return_void extends DxTestCase {
    public void testN1() {
        T_return_void_1 t = new T_return_void_1();
        assertEquals(123456, t.run());
    }
    public void testE1() {
        T_return_void_3 t = new T_return_void_3();
        try {
            assertTrue(t.run());
            fail("expected IllegalMonitorStateException");
        } catch (IllegalMonitorStateException imse) {
        }
    }
    public void testVFE1() {
        try {
            Class.forName("dot.junit.opcodes.return_void.d.T_return_void_5");
            fail("expected a verification exception");
        } catch (Throwable t) {
            DxUtil.checkVerifyException(t);
        }
    }
    public void testVFE2() {
        try {
            Class.forName("dot.junit.opcodes.return_void.d.T_return_void_6");
            fail("expected a verification exception");
        } catch (Throwable t) {
            DxUtil.checkVerifyException(t);
        }
    }
    public void testVFE3() {
        try {
            Class.forName("dot.junit.opcodes.return_void.d.T_return_void_7");
            fail("expected a verification exception");
        } catch (Throwable t) {
            DxUtil.checkVerifyException(t);
        }
    }
}
