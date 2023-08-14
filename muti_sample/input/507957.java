public class Test_castore extends DxTestCase {
    public void testN1() {
        T_castore_1 t = new T_castore_1();
        char[] arr = new char[2];
        t.run(arr, 1, 'g');
        assertEquals('g', arr[1]);
    }
    public void testN2() {
        T_castore_1 t = new T_castore_1();
        char[] arr = new char[2];
        t.run(arr, 0, 'g');
        assertEquals('g', arr[0]);
    }
    public void testE1() {
        T_castore_1 t = new T_castore_1();
        char[] arr = new char[2];
        try {
            t.run(arr, 2, 'g');
            fail("expected ArrayIndexOutOfBoundsException");
        } catch (ArrayIndexOutOfBoundsException aie) {
        }
    }
    public void testE2() {
        T_castore_1 t = new T_castore_1();
        try {
            t.run(null, 2, 'g');
            fail("expected NullPointerException");
        } catch (NullPointerException aie) {
        }
    }
    public void testE3() {
        T_castore_1 t = new T_castore_1();
        char[] arr = new char[2];
        try {
            t.run(arr, -1, 'g');
            fail("expected ArrayIndexOutOfBoundsException");
        } catch (ArrayIndexOutOfBoundsException aie) {
        }
    }
    public void testVFE1() {
        try {
            Class.forName("dxc.junit.opcodes.castore.jm.T_castore_2");
            fail("expected a verification exception");
        } catch (Throwable t) {
            DxUtil.checkVerifyException(t);
        }
    }
    public void testVFE2() {
        try {
            Class.forName("dxc.junit.opcodes.castore.jm.T_castore_3");
            fail("expected a verification exception");
        } catch (Throwable t) {
            DxUtil.checkVerifyException(t);
        }
    }
    public void testVFE3() {
        try {
            Class.forName("dxc.junit.opcodes.castore.jm.T_castore_4");
            fail("expected a verification exception");
        } catch (Throwable t) {
            DxUtil.checkVerifyException(t);
        }
    }
    public void testVFE4() {
        try {
            Class.forName("dxc.junit.opcodes.castore.jm.T_castore_5");
            fail("expected a verification exception");
        } catch (Throwable t) {
            DxUtil.checkVerifyException(t);
        }
    }
    public void testVFE5() {
        try {
            Class.forName("dxc.junit.opcodes.castore.jm.T_castore_6");
            fail("expected a verification exception");
        } catch (Throwable t) {
            DxUtil.checkVerifyException(t);
        }
    }
    public void testVFE6() {
        try {
            Class.forName("dxc.junit.opcodes.castore.jm.T_castore_7");
            fail("expected a verification exception");
        } catch (Throwable t) {
            DxUtil.checkVerifyException(t);
        }
    }
    public void testVFE7() {
        try {
            Class.forName("dxc.junit.opcodes.castore.jm.T_castore_8");
            fail("expected a verification exception");
        } catch (Throwable t) {
            DxUtil.checkVerifyException(t);
        }
    }
    public void testVFE8() {
        try {
            Class.forName("dxc.junit.opcodes.castore.jm.T_castore_9");
            fail("expected a verification exception");
        } catch (Throwable t) {
            DxUtil.checkVerifyException(t);
        }
    }
}
