public class Test_move_object_from16 extends DxTestCase {
    public void testN1() {
        T_move_object_from16_1 t = new T_move_object_from16_1(); 
        assertEquals(t.run(), t);
    }
    public void testVFE1() {
        try {
            Class.forName("dot.junit.opcodes.move_object_from16.d.T_move_object_from16_3");
            fail("expected a verification exception");
        } catch (Throwable t) {
            DxUtil.checkVerifyException(t);
        }
    }
    public void testVFE2() {
        try {
            Class.forName("dot.junit.opcodes.move_object_from16.d.T_move_object_from16_4");
            fail("expected a verification exception");
        } catch (Throwable t) {
            DxUtil.checkVerifyException(t);
        }
    }
    public void testVFE3() {
        try {
            Class.forName("dot.junit.opcodes.move_object_from16.d.T_move_object_from16_5");
            fail("expected a verification exception");
        } catch (Throwable t) {
            DxUtil.checkVerifyException(t);
        }
    }
    public void testVFE4() {
        try {
            Class.forName("dot.junit.opcodes.move_object_from16.d.T_move_object_from16_6");
            fail("expected a verification exception");
        } catch (Throwable t) {
            DxUtil.checkVerifyException(t);
        }
    }
    public void testVFE5() {
        try {
            Class.forName("dot.junit.opcodes.move_object_from16.d.T_move_object_from16_7");
            fail("expected a verification exception");
        } catch (Throwable t) {
            DxUtil.checkVerifyException(t);
        }
    }
    public void testVFE6() {
        try {
            Class.forName("dot.junit.opcodes.move_object_from16.d.T_move_object_from16_8");
            fail("expected a verification exception");
        } catch (Throwable t) {
            DxUtil.checkVerifyException(t);
        }
    }
}
