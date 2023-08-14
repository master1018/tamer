public class Test_move_exception extends DxTestCase {
    public void testN1() {
        T_move_exception_1 t = new T_move_exception_1();
        try {
            t.run();
            fail("ArithmeticException was not thrown");
        } catch (ArithmeticException ae) {
        } catch (Exception ex) {
            fail("Exception " + ex + " was thrown instead off ArithmeticException");
        }
    }
    public void testN2() {
        T_move_exception_2 t = new T_move_exception_2();
        assertTrue(t.run());
    }
    public void testVFE1() {
        try {
            Class.forName("dot.junit.opcodes.move_exception.d.T_move_exception_3");
            fail("expected a verification exception");
        } catch (Throwable t) {
            DxUtil.checkVerifyException(t);
        }
    }
    public void testVFE3() {
        try {
            Class.forName("dot.junit.opcodes.move_exception.d.T_move_exception_5");
            fail("expected a verification exception");
        } catch (Throwable t) {
            DxUtil.checkVerifyException(t);
        }
    }    
}
