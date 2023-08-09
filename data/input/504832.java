public class Test_fastore extends DxTestCase {
    public void testN1() {
        T_fastore_1 t = new T_fastore_1();
        float[] arr = new float[2];
        t.run(arr, 1, 2.7f);
        assertEquals(2.7f, arr[1]);
    }
    public void testN2() {
        T_fastore_1 t = new T_fastore_1();
        float[] arr = new float[2];
        t.run(arr, 0, 2.7f);
        assertEquals(2.7f, arr[0]);
    }
    public void testE1() {
        T_fastore_1 t = new T_fastore_1();
        float[] arr = new float[2];
        try {
            t.run(arr, 2, 2.7f);
            fail("expected ArrayIndexOutOfBoundsException");
        } catch (ArrayIndexOutOfBoundsException aie) {
        }
    }
    public void testE2() {
        T_fastore_1 t = new T_fastore_1();
        try {
            t.run(null, 2, 2.7f);
            fail("expected NullPointerException");
        } catch (NullPointerException aie) {
        }
    }
    public void testE3() {
        T_fastore_1 t = new T_fastore_1();
        float[] arr = new float[2];
        try {
            t.run(arr, -1, 2.7f);
            fail("expected ArrayIndexOutOfBoundsException");
        } catch (ArrayIndexOutOfBoundsException aie) {
        }
    }
    public void testVFE1() {
        try {
            Class.forName("dxc.junit.opcodes.fastore.jm.T_fastore_2");
            fail("expected a verification exception");
        } catch (Throwable t) {
            DxUtil.checkVerifyException(t);
        }
    }
    public void testVFE2() {
        try {
            Class.forName("dxc.junit.opcodes.fastore.jm.T_fastore_3");
            fail("expected a verification exception");
        } catch (Throwable t) {
            DxUtil.checkVerifyException(t);
        }
    }
    public void testVFE3() {
        try {
            Class.forName("dxc.junit.opcodes.fastore.jm.T_fastore_4");
            fail("expected a verification exception");
        } catch (Throwable t) {
            DxUtil.checkVerifyException(t);
        }
    }
    public void testVFE4() {
        try {
            Class.forName("dxc.junit.opcodes.fastore.jm.T_fastore_5");
            fail("expected a verification exception");
        } catch (Throwable t) {
            DxUtil.checkVerifyException(t);
        }
    }
    public void testVFE5() {
        try {
            Class.forName("dxc.junit.opcodes.fastore.jm.T_fastore_6");
            fail("expected a verification exception");
        } catch (Throwable t) {
            DxUtil.checkVerifyException(t);
        }
    }
    public void testVFE6() {
        try {
            Class.forName("dxc.junit.opcodes.fastore.jm.T_fastore_7");
            fail("expected a verification exception");
        } catch (Throwable t) {
            DxUtil.checkVerifyException(t);
        }
    }
    public void testVFE7() {
        try {
            Class.forName("dxc.junit.opcodes.fastore.jm.T_fastore_8");
            fail("expected a verification exception");
        } catch (Throwable t) {
            DxUtil.checkVerifyException(t);
        }
    }
    public void testVFE8() {
        try {
            Class.forName("dxc.junit.opcodes.fastore.jm.T_fastore_9");
            fail("expected a verification exception");
        } catch (Throwable t) {
            DxUtil.checkVerifyException(t);
        }
    }
}
