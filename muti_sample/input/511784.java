public class Test_aget_char extends DxTestCase {
    public void testN1() {
        T_aget_char_1 t = new T_aget_char_1();
        char[] arr = new char[2];
        arr[1] = 'g';
        assertEquals('g', t.run(arr, 1));
    }
    public void testN2() {
        T_aget_char_1 t = new T_aget_char_1();
        char[] arr = new char[2];
        arr[0] = 'g';
        assertEquals('g', t.run(arr, 0));
    }
    public void testN3() {
        char[] arr = new char[2];
        T_aget_char_8 t = new T_aget_char_8();
        try {
            t.run(arr, 3.14f);
        } catch (Throwable e) {
        }
    }
    public void testE1() {
        T_aget_char_1 t = new T_aget_char_1();
        char[] arr = new char[2];
        try {
            t.run(arr, 2);
            fail("expected ArrayIndexOutOfBoundsException");
        } catch (ArrayIndexOutOfBoundsException aie) {
        }
    }
    public void testE2() {
        T_aget_char_1 t = new T_aget_char_1();
        try {
            t.run(null, 2);
            fail("expected NullPointerException");
        } catch (NullPointerException aie) {
        }
    }
    public void testE3() {
        T_aget_char_1 t = new T_aget_char_1();
        char[] arr = new char[2];
        try {
            t.run(arr, -1);
            fail("expected ArrayIndexOutOfBoundsException");
        } catch (ArrayIndexOutOfBoundsException aie) {
        }
    }
    public void testVFE1() {
        try {
            Class.forName("dot.junit.opcodes.aget_char.d.T_aget_char_2");
            fail("expected a verification exception");
        } catch (Throwable t) {
            DxUtil.checkVerifyException(t);
        }
    }
    public void testVFE2() {
        try {
            Class.forName("dot.junit.opcodes.aget_char.d.T_aget_char_3");
            fail("expected a verification exception");
        } catch (Throwable t) {
            DxUtil.checkVerifyException(t);
        }
    }
    public void testVFE3() {
        try {
            Class.forName("dot.junit.opcodes.aget_char.d.T_aget_char_4");
            fail("expected a verification exception");
        } catch (Throwable t) {
            DxUtil.checkVerifyException(t);
        }
    }
    public void testVFE4() {
        try {
            Class.forName("dot.junit.opcodes.aget_char.d.T_aget_char_5");
            fail("expected a verification exception");
        } catch (Throwable t) {
            DxUtil.checkVerifyException(t);
        }
    }
    public void testVFE5() {
        try {
            Class.forName("dot.junit.opcodes.aget_char.d.T_aget_char_6");
            fail("expected a verification exception");
        } catch (Throwable t) {
            DxUtil.checkVerifyException(t);
        }
    }
    public void testVFE6() {
        try {
            Class.forName("dot.junit.opcodes.aget_char.d.T_aget_char_7");
            fail("expected a verification exception");
        } catch (Throwable t) {
            DxUtil.checkVerifyException(t);
        }
    }
    public void testVFE7() {
        try {
            Class.forName("dot.junit.opcodes.aget_char.d.T_aget_char_9");
            fail("expected a verification exception");
        } catch (Throwable t) {
            DxUtil.checkVerifyException(t);
        }
    }
}
