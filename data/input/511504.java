public class Test_move_result_object extends DxTestCase {
    public void testN1() {
        T_move_result_object_1 t = new T_move_result_object_1();
        assertTrue(t.run());
    }
    public void testN2() {
        T_move_result_object_8 t = new T_move_result_object_8();
        int[] arr = t.run();
        if(arr.length != 2 || arr[0] != 1 || arr[1] != 2)
            fail("wrong array size or content");
    }
    public void testVFE1() {
        try {
            Class.forName("dot.junit.opcodes.move_result_object.d.T_move_result_object_2");
            fail("expected a verification exception");
        } catch (Throwable t) {
            DxUtil.checkVerifyException(t);
        }
    }
    public void testVFE2() {
        try {
            Class.forName("dot.junit.opcodes.move_result_object.d.T_move_result_object_3");
            fail("expected a verification exception");
        } catch (Throwable t) {
            DxUtil.checkVerifyException(t);
        }
    }
    public void testVFE3() {
        try {
            Class.forName("dot.junit.opcodes.move_result_object.d.T_move_result_object_4");
            fail("expected a verification exception");
        } catch (Throwable t) {
            DxUtil.checkVerifyException(t);
        }
    }
    public void testVFE4() {
        try {
            Class.forName("dot.junit.opcodes.move_result_object.d.T_move_result_object_5");
            fail("expected a verification exception");
        } catch (Throwable t) {
            DxUtil.checkVerifyException(t);
        }
    }
    public void testVFE5() {
        try {
            Class.forName("dot.junit.opcodes.move_result_object.d.T_move_result_object_6");
            fail("expected a verification exception");
        } catch (Throwable t) {
            DxUtil.checkVerifyException(t);
        }
    }
    public void testVFE6() {
        try {
            Class.forName("dot.junit.opcodes.move_result_object.d.T_move_result_object_7");
            fail("expected a verification exception");
        } catch (Throwable t) {
            DxUtil.checkVerifyException(t);
        }
    }
    public void testVFE7() {
        try {
            Class.forName("dot.junit.opcodes.move_result_object.d.T_move_result_object_9");
            fail("expected a verification exception");
        } catch (Throwable t) {
            DxUtil.checkVerifyException(t);
        }
    }
}
