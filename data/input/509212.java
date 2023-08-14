public class Test_xor_int_lit8 extends DxTestCase {
    public void testN1() {
         T_xor_int_lit8_1 t = new T_xor_int_lit8_1();
         assertEquals(7, t.run());
     }
    public void testN2() {
         T_xor_int_lit8_2 t = new T_xor_int_lit8_2();
         assertEquals(9, t.run());
     }
    public void testN3() {
        T_xor_int_lit8_7 t = new T_xor_int_lit8_7();
        try {
            t.run(3.14f);
        } catch (Throwable e) {
        }
    }
    public void testB1() {
        T_xor_int_lit8_3 t = new T_xor_int_lit8_3();
            assertEquals(-1, t.run());
    }
    public void testB2() {
        T_xor_int_lit8_4 t = new T_xor_int_lit8_4();
            assertEquals(-2147483521, t.run());
    }
    public void testVFE1() {
        try {
            Class.forName("dot.junit.opcodes.xor_int_lit8.d.T_xor_int_lit8_5");
            fail("expected a verification exception");
        } catch (Throwable t) {
            DxUtil.checkVerifyException(t);
        }
    }
    public void testVFE2() {
        try {
            Class.forName("dot.junit.opcodes.xor_int_lit8.d.T_xor_int_lit8_6");
            fail("expected a verification exception");
        } catch (Throwable t) {
            DxUtil.checkVerifyException(t);
        }
    }
    public void testVFE3() {
        try {
            Class.forName("dot.junit.opcodes.xor_int_lit8.d.T_xor_int_lit8_8");
            fail("expected a verification exception");
        } catch (Throwable t) {
            DxUtil.checkVerifyException(t);
        }
    }
}
