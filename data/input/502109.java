public class Test_lastore extends DxTestCase {
    public void testN1() {
        T_lastore_1 t = new T_lastore_1();
        long[] arr = new long[2];
        t.run(arr, 1, 100000000000l);
        assertEquals(100000000000l, arr[1]);
    }
    public void testN2() {
        T_lastore_1 t = new T_lastore_1();
        long[] arr = new long[2];
        t.run(arr, 0, 100000000000l);
        assertEquals(100000000000l, arr[0]);
    }
    public void testE1() {
        T_lastore_1 t = new T_lastore_1();
        long[] arr = new long[2];
        try {
            t.run(arr, 2, 100000000000l);
            fail("expected ArrayIndexOutOfBoundsException");
        } catch (ArrayIndexOutOfBoundsException aie) {
        }
    }
    public void testE2() {
        T_lastore_1 t = new T_lastore_1();
        try {
            t.run(null, 1, 100000000000l);
            fail("expected NullPointerException");
        } catch (NullPointerException np) {
        }
    }
    public void testE3() {
        T_lastore_1 t = new T_lastore_1();
        long[] arr = new long[2];
        try {
            t.run(arr, -1, 100000000000l);
            fail("expected ArrayIndexOutOfBoundsException");
        } catch (ArrayIndexOutOfBoundsException aie) {
        }
    }
    public void testVFE1() {
        try {
            Class.forName("dxc.junit.opcodes.lastore.jm.T_lastore_2");
            fail("expected a verification exception");
        } catch (Throwable t) {
            DxUtil.checkVerifyException(t);
        }
    }
    public void testVFE2() {
        try {
            Class.forName("dxc.junit.opcodes.lastore.jm.T_lastore_3");
            fail("expected a verification exception");
        } catch (Throwable t) {
            DxUtil.checkVerifyException(t);
        }
    }
    public void testVFE3() {
        try {
            Class.forName("dxc.junit.opcodes.lastore.jm.T_lastore_4");
            fail("expected a verification exception");
        } catch (Throwable t) {
            DxUtil.checkVerifyException(t);
        }
    }
    public void testVFE4() {
        try {
            Class.forName("dxc.junit.opcodes.lastore.jm.T_lastore_5");
            fail("expected a verification exception");
        } catch (Throwable t) {
            DxUtil.checkVerifyException(t);
        }
    }
    public void testVFE5() {
        try {
            Class.forName("dxc.junit.opcodes.lastore.jm.T_lastore_6");
            fail("expected a verification exception");
        } catch (Throwable t) {
            DxUtil.checkVerifyException(t);
        }
    }
    public void testVFE6() {
        try {
            Class.forName("dxc.junit.opcodes.lastore.jm.T_lastore_7");
            fail("expected a verification exception");
        } catch (Throwable t) {
            DxUtil.checkVerifyException(t);
        }
    }
    public void testVFE7() {
        try {
            Class.forName("dxc.junit.opcodes.lastore.jm.T_lastore_8");
            fail("expected a verification exception");
        } catch (Throwable t) {
            DxUtil.checkVerifyException(t);
        }
    }
    public void testVFE8() {
        try {
            Class.forName("dxc.junit.opcodes.lastore.jm.T_lastore_9");
            fail("expected a verification exception");
        } catch (Throwable t) {
            DxUtil.checkVerifyException(t);
        }
    }
}
