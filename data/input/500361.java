public class Test_aput_byte extends DxTestCase {
    public void testN1() {
        T_aput_byte_1 t = new T_aput_byte_1();
        byte[] arr = new byte[2];
        t.run(arr, 1, (byte) 100);
        assertEquals(100, arr[1]);
    }
    public void testN2() {
        T_aput_byte_1 t = new T_aput_byte_1();
        byte[] arr = new byte[2];
        t.run(arr, 0, (byte) 100);
        assertEquals(100, arr[0]);
    }
    public void testN3() {
        byte[] arr = new byte[2];
        T_aput_byte_8 t = new T_aput_byte_8();
        try {
            t.run(arr, 3.14f, (byte)1);
        } catch (Throwable e) {
        }
    }
    public void testE1() {
        T_aput_byte_1 t = new T_aput_byte_1();
        byte[] arr = new byte[2];
        try {
            t.run(arr, 2, (byte) 100);
            fail("expected ArrayIndexOutOfBoundsException");
        } catch (ArrayIndexOutOfBoundsException aie) {
        }
    }
    public void testE2() {
        T_aput_byte_1 t = new T_aput_byte_1();
        try {
            t.run(null, 2, (byte) 100);
            fail("expected NullPointerException");
        } catch (NullPointerException aie) {
        }
    }
    public void testE3() {
        T_aput_byte_1 t = new T_aput_byte_1();
        byte[] arr = new byte[2];
        try {
            t.run(arr, -1, (byte) 100);
            fail("expected ArrayIndexOutOfBoundsException");
        } catch (ArrayIndexOutOfBoundsException aie) {
        }
    }
    public void testVFE1() {
        try {
            Class.forName("dot.junit.opcodes.aput_byte.d.T_aput_byte_2");
            fail("expected a verification exception");
        } catch (Throwable t) {
            DxUtil.checkVerifyException(t);
        }
    }
    public void testVFE2() {
        try {
            Class.forName("dot.junit.opcodes.aput_byte.d.T_aput_byte_3");
            fail("expected a verification exception");
        } catch (Throwable t) {
            DxUtil.checkVerifyException(t);
        }
    }
    public void testVFE3() {
        try {
            Class.forName("dot.junit.opcodes.aput_byte.d.T_aput_byte_4");
            fail("expected a verification exception");
        } catch (Throwable t) {
            DxUtil.checkVerifyException(t);
        }
    }
    public void testVFE4() {
        try {
            Class.forName("dot.junit.opcodes.aput_byte.d.T_aput_byte_5");
            fail("expected a verification exception");
        } catch (Throwable t) {
            DxUtil.checkVerifyException(t);
        }
    }
    public void testVFE5() {
        try {
            Class.forName("dot.junit.opcodes.aput_byte.d.T_aput_byte_6");
            fail("expected a verification exception");
        } catch (Throwable t) {
            DxUtil.checkVerifyException(t);
        }
    }
    public void testVFE6() {
        try {
            Class.forName("dot.junit.opcodes.aput_byte.d.T_aput_byte_7");
            fail("expected a verification exception");
        } catch (Throwable t) {
            DxUtil.checkVerifyException(t);
        }
    }
    public void testVFE7() {
        try {
            Class.forName("dot.junit.opcodes.aput_byte.d.T_aput_byte_9");
            fail("expected a verification exception");
        } catch (Throwable t) {
            DxUtil.checkVerifyException(t);
        }
    }
    public void testVFE8() {
        try {
            Class.forName("dot.junit.opcodes.aput_byte.d.T_aput_byte_10");
            fail("expected a verification exception");
        } catch (Throwable t) {
            DxUtil.checkVerifyException(t);
        }
    }
}
