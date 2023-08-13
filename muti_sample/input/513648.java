public class Test_bastore extends DxTestCase {
    public void testN1() {
        T_bastore_1 t = new T_bastore_1();
        byte[] arr = new byte[2];
        t.run(arr, 1, (byte) 100);
        assertEquals(100, arr[1]);
    }
    public void testN2() {
        T_bastore_1 t = new T_bastore_1();
        byte[] arr = new byte[2];
        t.run(arr, 0, (byte) 100);
        assertEquals(100, arr[0]);
    }
    public void testE1() {
        T_bastore_1 t = new T_bastore_1();
        byte[] arr = new byte[2];
        try {
            t.run(arr, 2, (byte) 100);
            fail("expected ArrayIndexOutOfBoundsException");
        } catch (ArrayIndexOutOfBoundsException aie) {
        }
    }
    public void testE2() {
        T_bastore_1 t = new T_bastore_1();
        try {
            t.run(null, 2, (byte) 100);
            fail("expected NullPointerException");
        } catch (NullPointerException aie) {
        }
    }
    public void testE3() {
        T_bastore_1 t = new T_bastore_1();
        byte[] arr = new byte[2];
        try {
            t.run(arr, -1, (byte) 100);
            fail("expected ArrayIndexOutOfBoundsException");
        } catch (ArrayIndexOutOfBoundsException aie) {
        }
    }
    public void testVFE1() {
        try {
            Class.forName("dxc.junit.opcodes.bastore.jm.T_bastore_2");
            fail("expected a verification exception");
        } catch (Throwable t) {
            DxUtil.checkVerifyException(t);
        }
    }
    public void testVFE2() {
        try {
            Class.forName("dxc.junit.opcodes.bastore.jm.T_bastore_3");
            fail("expected a verification exception");
        } catch (Throwable t) {
            DxUtil.checkVerifyException(t);
        }
    }
    public void testVFE3() {
        try {
            Class.forName("dxc.junit.opcodes.bastore.jm.T_bastore_4");
            fail("expected a verification exception");
        } catch (Throwable t) {
            DxUtil.checkVerifyException(t);
        }
    }
    public void testVFE4() {
        try {
            Class.forName("dxc.junit.opcodes.bastore.jm.T_bastore_5");
            fail("expected a verification exception");
        } catch (Throwable t) {
            DxUtil.checkVerifyException(t);
        }
    }
    public void testVFE5() {
        try {
            Class.forName("dxc.junit.opcodes.bastore.jm.T_bastore_6");
            fail("expected a verification exception");
        } catch (Throwable t) {
            DxUtil.checkVerifyException(t);
        }
    }
    public void testVFE6() {
        try {
            Class.forName("dxc.junit.opcodes.bastore.jm.T_bastore_7");
            fail("expected a verification exception");
        } catch (Throwable t) {
            DxUtil.checkVerifyException(t);
        }
    }
    public void testVFE7() {
        try {
            Class.forName("dxc.junit.opcodes.bastore.jm.T_bastore_8");
            fail("expected a verification exception");
        } catch (Throwable t) {
            DxUtil.checkVerifyException(t);
        }
    }
    public void testVFE8() {
        try {
            Class.forName("dxc.junit.opcodes.bastore.jm.T_bastore_9");
            fail("expected a verification exception");
        } catch (Throwable t) {
            DxUtil.checkVerifyException(t);
        }
    }
}
