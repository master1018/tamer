public class Test_aput_boolean extends DxTestCase {
    public void testN1() {
        T_aput_boolean_1 t = new T_aput_boolean_1();
        boolean[] arr = new boolean[2];
        t.run(arr, 1, true);
        assertEquals(true, arr[1]);
    }
    public void testN2() {
        T_aput_boolean_1 t = new T_aput_boolean_1();
        boolean[] arr = new boolean[2];
        t.run(arr, 0, true);
        assertEquals(true, arr[0]);
    }
    public void testN3() {
        boolean[] arr = new boolean[2];
        T_aput_boolean_8 t = new T_aput_boolean_8();
        try {
            t.run(arr, 3.14f, true);
        } catch (Throwable e) {
        }
    }
    public void testE1() {
        T_aput_boolean_1 t = new T_aput_boolean_1();
        boolean[] arr = new boolean[2];
        try {
            t.run(arr, 2, true);
            fail("expected ArrayIndexOutOfBoundsException");
        } catch (ArrayIndexOutOfBoundsException aie) {
        }
    }
    public void testE2() {
        T_aput_boolean_1 t = new T_aput_boolean_1();
        try {
            t.run(null, 2, true);
            fail("expected NullPointerException");
        } catch (NullPointerException aie) {
        }
    }
    public void testE3() {
        T_aput_boolean_1 t = new T_aput_boolean_1();
        boolean[] arr = new boolean[2];
        try {
            t.run(arr, -1, true);
            fail("expected ArrayIndexOutOfBoundsException");
        } catch (ArrayIndexOutOfBoundsException aie) {
        }
    }
    public void testVFE1() {
        try {
            Class.forName("dot.junit.opcodes.aput_boolean.d.T_aput_boolean_2");
            fail("expected a verification exception");
        } catch (Throwable t) {
            DxUtil.checkVerifyException(t);
        }
    }
    public void testVFE2() {
        try {
            Class.forName("dot.junit.opcodes.aput_boolean.d.T_aput_boolean_3");
            fail("expected a verification exception");
        } catch (Throwable t) {
            DxUtil.checkVerifyException(t);
        }
    }
    public void testVFE3() {
        try {
            Class.forName("dot.junit.opcodes.aput_boolean.d.T_aput_boolean_4");
            fail("expected a verification exception");
        } catch (Throwable t) {
            DxUtil.checkVerifyException(t);
        }
    }
    public void testVFE4() {
        try {
            Class.forName("dot.junit.opcodes.aput_boolean.d.T_aput_boolean_5");
            fail("expected a verification exception");
        } catch (Throwable t) {
            DxUtil.checkVerifyException(t);
        }
    }
    public void testVFE5() {
        try {
            Class.forName("dot.junit.opcodes.aput_boolean.d.T_aput_boolean_6");
            fail("expected a verification exception");
        } catch (Throwable t) {
            DxUtil.checkVerifyException(t);
        }
    }
    public void testVFE6() {
        try {
            Class.forName("dot.junit.opcodes.aput_boolean.d.T_aput_boolean_7");
            fail("expected a verification exception");
        } catch (Throwable t) {
            DxUtil.checkVerifyException(t);
        }
    }
    public void testVFE7() {
        try {
            Class.forName("dot.junit.opcodes.aput_boolean.d.T_aput_boolean_9");
            fail("expected a verification exception");
        } catch (Throwable t) {
            DxUtil.checkVerifyException(t);
        }
    }
    public void testVFE9() {
        try {
            Class.forName("dot.junit.opcodes.aput_boolean.d.T_aput_boolean_10");
            fail("expected a verification exception");
        } catch (Throwable t) {
            DxUtil.checkVerifyException(t);
        }
    }
}
