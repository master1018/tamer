public class Test_dastore extends DxTestCase {
    public void testN1() {
        T_dastore_1 t = new T_dastore_1();
        double[] arr = new double[2];
        t.run(arr, 1, 2.7d);
        assertEquals(2.7d, arr[1]);
    }
    public void testN2() {
        T_dastore_1 t = new T_dastore_1();
        double[] arr = new double[2];
        t.run(arr, 0, 2.7d);
        assertEquals(2.7d, arr[0]);
    }
    public void testE1() {
        T_dastore_1 t = new T_dastore_1();
        double[] arr = new double[2];
        try {
            t.run(arr, 2, 2.7d);
            fail("expected ArrayIndexOutOfBoundsException");
        } catch (ArrayIndexOutOfBoundsException aie) {
        }
    }
    public void testE2() {
        T_dastore_1 t = new T_dastore_1();
        try {
            t.run(null, 2, 2.7d);
            fail("expected NullPointerException");
        } catch (NullPointerException aie) {
        }
    }
    public void testE3() {
        T_dastore_1 t = new T_dastore_1();
        double[] arr = new double[2];
        try {
            t.run(arr, -1, 2.7d);
            fail("expected ArrayIndexOutOfBoundsException");
        } catch (ArrayIndexOutOfBoundsException aie) {
        }
    }
    public void testVFE1() {
        try {
            Class.forName("dxc.junit.opcodes.dastore.jm.T_dastore_2");
            fail("expected a verification exception");
        } catch (Throwable t) {
            DxUtil.checkVerifyException(t);
        }
    }
    public void testVFE2() {
        try {
            Class.forName("dxc.junit.opcodes.dastore.jm.T_dastore_3");
            fail("expected a verification exception");
        } catch (Throwable t) {
            DxUtil.checkVerifyException(t);
        }
    }
    public void testVFE3() {
        try {
            Class.forName("dxc.junit.opcodes.dastore.jm.T_dastore_4");
            fail("expected a verification exception");
        } catch (Throwable t) {
            DxUtil.checkVerifyException(t);
        }
    }
    public void testVFE4() {
        try {
            Class.forName("dxc.junit.opcodes.dastore.jm.T_dastore_5");
            fail("expected a verification exception");
        } catch (Throwable t) {
            DxUtil.checkVerifyException(t);
        }
    }
    public void testVFE5() {
        try {
            Class.forName("dxc.junit.opcodes.dastore.jm.T_dastore_6");
            fail("expected a verification exception");
        } catch (Throwable t) {
            DxUtil.checkVerifyException(t);
        }
    }
    public void testVFE6() {
        try {
            Class.forName("dxc.junit.opcodes.dastore.jm.T_dastore_7");
            fail("expected a verification exception");
        } catch (Throwable t) {
            DxUtil.checkVerifyException(t);
        }
    }
    public void testVFE7() {
        try {
            Class.forName("dxc.junit.opcodes.dastore.jm.T_dastore_8");
            fail("expected a verification exception");
        } catch (Throwable t) {
            DxUtil.checkVerifyException(t);
        }
    }
    public void testVFE8() {
        try {
            Class.forName("dxc.junit.opcodes.dastore.jm.T_dastore_9");
            fail("expected a verification exception");
        } catch (Throwable t) {
            DxUtil.checkVerifyException(t);
        }
    }
}
