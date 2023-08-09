public class Test_saload extends DxTestCase {
    public void testN1() {
        T_saload_1 t = new T_saload_1();
        short[] arr = new short[2];
        arr[1] = 10000;
        assertEquals(10000, t.run(arr, 1));
    }
    public void testN2() {
        T_saload_1 t = new T_saload_1();
        short[] arr = new short[2];
        arr[0] = 10000;
        assertEquals(10000, t.run(arr, 0));
    }
    public void testE1() {
        T_saload_1 t = new T_saload_1();
        short[] arr = new short[2];
        try {
            t.run(arr, 2);
            fail("expected ArrayIndexOutOfBoundsException");
        } catch (ArrayIndexOutOfBoundsException aie) {
        }
    }
    public void testE2() {
        T_saload_1 t = new T_saload_1();
        try {
            t.run(null, 2);
            fail("expected NullPointerException");
        } catch (NullPointerException aie) {
        }
    }
    public void testE3() {
        T_saload_1 t = new T_saload_1();
        short[] arr = new short[2];
        try {
            t.run(arr, -1);
            fail("expected ArrayIndexOutOfBoundsException");
        } catch (ArrayIndexOutOfBoundsException aie) {
        }
    }
    public void testVFE1() {
        try {
            Class.forName("dxc.junit.opcodes.saload.jm.T_saload_2");
            fail("expected a verification exception");
        } catch (Throwable t) {
            DxUtil.checkVerifyException(t);
        }
    }
    public void testVFE2() {
        try {
            Class.forName("dxc.junit.opcodes.saload.jm.T_saload_3");
            fail("expected a verification exception");
        } catch (Throwable t) {
            DxUtil.checkVerifyException(t);
        }
    }
    public void testVFE3() {
        try {
            Class.forName("dxc.junit.opcodes.saload.jm.T_saload_4");
            fail("expected a verification exception");
        } catch (Throwable t) {
            DxUtil.checkVerifyException(t);
        }
    }
    public void testVFE4() {
        try {
            Class.forName("dxc.junit.opcodes.saload.jm.T_saload_5");
            fail("expected a verification exception");
        } catch (Throwable t) {
            DxUtil.checkVerifyException(t);
        }
    }
    public void testVFE5() {
        try {
            Class.forName("dxc.junit.opcodes.saload.jm.T_saload_6");
            fail("expected a verification exception");
        } catch (Throwable t) {
            DxUtil.checkVerifyException(t);
        }
    }
    public void testVFE6() {
        try {
            Class.forName("dxc.junit.opcodes.saload.jm.T_saload_7");
            fail("expected a verification exception");
        } catch (Throwable t) {
            DxUtil.checkVerifyException(t);
        }
    }
    public void testVFE7() {
        try {
            Class.forName("dxc.junit.opcodes.saload.jm.T_saload_8");
            fail("expected a verification exception");
        } catch (Throwable t) {
            DxUtil.checkVerifyException(t);
        }
    }
    public void testVFE8() {
        try {
            Class.forName("dxc.junit.opcodes.saload.jm.T_saload_9");
            fail("expected a verification exception");
        } catch (Throwable t) {
            DxUtil.checkVerifyException(t);
        }
    }
}
