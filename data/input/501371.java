public class Test_iastore extends DxTestCase {
    public void testN1() {
        T_iastore_1 t = new T_iastore_1();
        int[] arr = new int[2];
        t.run(arr, 1, 100000000);
        assertEquals(100000000, arr[1]);
    }
    public void testN2() {
        T_iastore_1 t = new T_iastore_1();
        int[] arr = new int[2];
        t.run(arr, 0, 100000000);
        assertEquals(100000000, arr[0]);
    }
    public void testE1() {
        T_iastore_1 t = new T_iastore_1();
        int[] arr = new int[2];
        try {
            t.run(arr, 2, 100000000);
            fail("expected ArrayIndexOutOfBoundsException");
        } catch (ArrayIndexOutOfBoundsException aie) {
        }
    }
    public void testE2() {
        T_iastore_1 t = new T_iastore_1();
        try {
            t.run(null, 2, 100000000);
            fail("expected NullPointerException");
        } catch (NullPointerException aie) {
        }
    }
    public void testE3() {
        T_iastore_1 t = new T_iastore_1();
        int[] arr = new int[2];
        try {
            t.run(arr, -1, 100000000);
            fail("expected ArrayIndexOutOfBoundsException");
        } catch (ArrayIndexOutOfBoundsException aie) {
        }
    }
    public void testVFE1() {
        try {
            Class.forName("dxc.junit.opcodes.iastore.jm.T_iastore_2");
            fail("expected a verification exception");
        } catch (Throwable t) {
            DxUtil.checkVerifyException(t);
        }
    }
    public void testVFE2() {
        try {
            Class.forName("dxc.junit.opcodes.iastore.jm.T_iastore_3");
            fail("expected a verification exception");
        } catch (Throwable t) {
            DxUtil.checkVerifyException(t);
        }
    }
    public void testVFE3() {
        try {
            Class.forName("dxc.junit.opcodes.iastore.jm.T_iastore_4");
            fail("expected a verification exception");
        } catch (Throwable t) {
            DxUtil.checkVerifyException(t);
        }
    }
    public void testVFE4() {
        try {
            Class.forName("dxc.junit.opcodes.iastore.jm.T_iastore_5");
            fail("expected a verification exception");
        } catch (Throwable t) {
            DxUtil.checkVerifyException(t);
        }
    }
    public void testVFE5() {
        try {
            Class.forName("dxc.junit.opcodes.iastore.jm.T_iastore_6");
            fail("expected a verification exception");
        } catch (Throwable t) {
            DxUtil.checkVerifyException(t);
        }
    }
    public void testVFE6() {
        try {
            Class.forName("dxc.junit.opcodes.iastore.jm.T_iastore_7");
            fail("expected a verification exception");
        } catch (Throwable t) {
            DxUtil.checkVerifyException(t);
        }
    }
    public void testVFE7() {
        try {
            Class.forName("dxc.junit.opcodes.iastore.jm.T_iastore_8");
            fail("expected a verification exception");
        } catch (Throwable t) {
            DxUtil.checkVerifyException(t);
        }
    }
    public void testVFE8() {
        try {
            Class.forName("dxc.junit.opcodes.iastore.jm.T_iastore_9");
            fail("expected a verification exception");
        } catch (Throwable t) {
            DxUtil.checkVerifyException(t);
        }
    }
}
