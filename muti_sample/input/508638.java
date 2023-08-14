public class Test_sastore extends DxTestCase {
    public void testN1() {
        T_sastore_1 t = new T_sastore_1();
        short[] arr = new short[2];
        t.run(arr, 1, (short) 10000);
        assertEquals(10000, arr[1]);
    }
    public void testN2() {
        T_sastore_1 t = new T_sastore_1();
        short[] arr = new short[2];
        t.run(arr, 0, (short) 10000);
        assertEquals(10000, arr[0]);
    }
    public void testE1() {
        T_sastore_1 t = new T_sastore_1();
        short[] arr = new short[2];
        try {
            t.run(arr, 2, (short) 10000);
            fail("expected ArrayIndexOutOfBoundsException");
        } catch (ArrayIndexOutOfBoundsException aie) {
        }
    }
    public void testE2() {
        T_sastore_1 t = new T_sastore_1();
        try {
            t.run(null, 2, (short) 10000);
            fail("expected NullPointerException");
        } catch (NullPointerException aie) {
        }
    }
    public void testE3() {
        T_sastore_1 t = new T_sastore_1();
        short[] arr = new short[2];
        try {
            t.run(arr, -1, (short) 10000);
            fail("expected ArrayIndexOutOfBoundsException");
        } catch (ArrayIndexOutOfBoundsException aie) {
        }
    }
    public void testVFE1() {
        try {
            Class.forName("dxc.junit.opcodes.sastore.jm.T_sastore_2");
            fail("expected a verification exception");
        } catch (Throwable t) {
            DxUtil.checkVerifyException(t);
        }
    }
    public void testVFE2() {
        try {
            Class.forName("dxc.junit.opcodes.sastore.jm.T_sastore_3");
            fail("expected a verification exception");
        } catch (Throwable t) {
            DxUtil.checkVerifyException(t);
        }
    }
    public void testVFE3() {
        try {
            Class.forName("dxc.junit.opcodes.sastore.jm.T_sastore_4");
            fail("expected a verification exception");
        } catch (Throwable t) {
            DxUtil.checkVerifyException(t);
        }
    }
    public void testVFE4() {
        try {
            Class.forName("dxc.junit.opcodes.sastore.jm.T_sastore_5");
            fail("expected a verification exception");
        } catch (Throwable t) {
            DxUtil.checkVerifyException(t);
        }
    }
    public void testVFE5() {
        try {
            Class.forName("dxc.junit.opcodes.sastore.jm.T_sastore_6");
            fail("expected a verification exception");
        } catch (Throwable t) {
            DxUtil.checkVerifyException(t);
        }
    }
    public void testVFE6() {
        try {
            Class.forName("dxc.junit.opcodes.sastore.jm.T_sastore_7");
            fail("expected a verification exception");
        } catch (Throwable t) {
            DxUtil.checkVerifyException(t);
        }
    }
    public void testVFE7() {
        try {
            Class.forName("dxc.junit.opcodes.sastore.jm.T_sastore_8");
            fail("expected a verification exception");
        } catch (Throwable t) {
            DxUtil.checkVerifyException(t);
        }
    }
    public void testVFE8() {
        try {
            Class.forName("dxc.junit.opcodes.sastore.jm.T_sastore_9");
            fail("expected a verification exception");
        } catch (Throwable t) {
            DxUtil.checkVerifyException(t);
        }
    }
}
