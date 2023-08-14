public class Test_aput_wide extends DxTestCase {
    public void testN1() {
        T_aput_wide_1 t = new T_aput_wide_1();
        long[] arr = new long[2];
        t.run(arr, 1, 100000000000l);
        assertEquals(100000000000l, arr[1]);
    }
    public void testN2() {
        T_aput_wide_1 t = new T_aput_wide_1();
        long[] arr = new long[2];
        t.run(arr, 0, 100000000000l);
        assertEquals(100000000000l, arr[0]);
    }
    public void testN3() {
        T_aput_wide_2 t = new T_aput_wide_2();
        double[] arr = new double[2];
        t.run(arr, 0, 3.1415d);
        assertEquals(3.1415d, arr[0]);
    }
    public void testN4() {
        long[] arr = new long[2];
        T_aput_wide_9 t = new T_aput_wide_9();
        try {
            t.run(arr, 3.14f, 111);
        } catch (Throwable e) {
        }
    }
    public void testN5() {
        double[] arr = new double[2];
        T_aput_wide_6 t = new T_aput_wide_6();
        try {
            t.run(arr, 0, 1234l);
        } catch (Throwable e) {
        }
    }
    public void testE1() {
        T_aput_wide_1 t = new T_aput_wide_1();
        long[] arr = new long[2];
        try {
            t.run(arr, 2, 100000000000l);
            fail("expected ArrayIndexOutOfBoundsException");
        } catch (ArrayIndexOutOfBoundsException aie) {
        }
    }
    public void testE2() {
        T_aput_wide_1 t = new T_aput_wide_1();
        try {
            t.run(null, 1, 100000000000l);
            fail("expected NullPointerException");
        } catch (NullPointerException np) {
        }
    }
    public void testE3() {
        T_aput_wide_1 t = new T_aput_wide_1();
        long[] arr = new long[2];
        try {
            t.run(arr, -1, 100000000000l);
            fail("expected ArrayIndexOutOfBoundsException");
        } catch (ArrayIndexOutOfBoundsException aie) {
        }
    }
    public void testVFE1() {
        try {
            Class.forName("dot.junit.opcodes.aput_wide.d.T_aput_wide_3");
            fail("expected a verification exception");
        } catch (Throwable t) {
            DxUtil.checkVerifyException(t);
        }
    }
    public void testVFE2() {
        try {
            Class.forName("dot.junit.opcodes.aput_wide.d.T_aput_wide_4");
            fail("expected a verification exception");
        } catch (Throwable t) {
            DxUtil.checkVerifyException(t);
        }
    }
    public void testVFE3() {
        try {
            Class.forName("dot.junit.opcodes.aput_wide.d.T_aput_wide_5");
            fail("expected a verification exception");
        } catch (Throwable t) {
            DxUtil.checkVerifyException(t);
        }
    }
    public void testVFE5() {
        try {
            Class.forName("dot.junit.opcodes.aput_wide.d.T_aput_wide_7");
            fail("expected a verification exception");
        } catch (Throwable t) {
            DxUtil.checkVerifyException(t);
        }
    }
    public void testVFE6() {
        try {
            Class.forName("dot.junit.opcodes.aput_wide.d.T_aput_wide_8");
            fail("expected a verification exception");
        } catch (Throwable t) {
            DxUtil.checkVerifyException(t);
        }
    }
    public void testVFE7() {
        try {
            Class.forName("dot.junit.opcodes.aput_wide.d.T_aput_wide_10");
            fail("expected a verification exception");
        } catch (Throwable t) {
            DxUtil.checkVerifyException(t);
        }
    }
}
