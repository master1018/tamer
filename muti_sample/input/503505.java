public class Test_aastore extends DxTestCase {
    public void testN1() {
        T_aastore_1 t = new T_aastore_1();
        String[] arr = new String[2];
        t.run(arr, 0, "hello");
        assertEquals("hello", arr[0]);
    }
    public void testN2() {
        T_aastore_1 t = new T_aastore_1();
        String[] value = {"world", null, ""};
        String[] arr = new String[2];
        for (int i = 0; i < value.length; i++) {
            t.run(arr, 1, value[i]);
            assertEquals(value[i], arr[1]);
        }
    }
    public void testN3() {
        T_aastore_10 t = new T_aastore_10();
        Integer[] arr = new Integer[2];
        Integer value = new Integer(12345);
        t.run(arr, 0, value);
        assertEquals(value, arr[0]);
    }
    public void testN4() {
        T_aastore_11 t = new T_aastore_11();
        assertEquals(3, t.run());
    }
    public void testE1() {
        T_aastore_1 t = new T_aastore_1();
        String[] arr = new String[2];
        try {
            t.run(arr, arr.length, "abc");
            fail("expected ArrayIndexOutOfBoundsException");
        } catch (ArrayIndexOutOfBoundsException aie) {
        }
    }
    public void testE2() {
        T_aastore_1 t = new T_aastore_1();
        String[] arr = new String[2];
        try {
            t.run(arr, -1, "abc");
            fail("expected ArrayIndexOutOfBoundsException");
        } catch (ArrayIndexOutOfBoundsException aie) {
        }
    }
    public void testE3() {
        T_aastore_1 t = new T_aastore_1();
        String[] arr = null;
        try {
            t.run(arr, 0, "abc");
            fail("expected NullPointerException");
        } catch (NullPointerException aie) {
        }
    }
    public void testE4() {
        T_aastore_4 t = new T_aastore_4();
        String[] arr = new String[2];
        try {
            t.run(arr, 0, this);
            fail("expected ArrayStoreException");
        } catch (ArrayStoreException aie) {
        }
    }
    public void testVFE1() {
        try {
            Class.forName("dxc.junit.opcodes.aastore.jm.T_aastore_2");
            fail("expected a verification exception");
        } catch (Throwable t) {
            DxUtil.checkVerifyException(t);
        }
    }
    public void testVFE2() {
        try {
            Class.forName("dxc.junit.opcodes.aastore.jm.T_aastore_3");
            fail("expected a verification exception");
        } catch (Throwable t) {
            DxUtil.checkVerifyException(t);
        }
    }
    public void testVFE3() {
        try {
            Class.forName("dxc.junit.opcodes.aastore.jm.T_aastore_5");
            fail("expected a verification exception");
        } catch (Throwable t) {
            DxUtil.checkVerifyException(t);
        }
    }
    public void testVFE4() {
        try {
            Class.forName("dxc.junit.opcodes.aastore.jm.T_aastore_6");
            fail("expected a verification exception");
        } catch (Throwable t) {
            DxUtil.checkVerifyException(t);
        }
    }
    public void testVFE5() {
        try {
            Class.forName("dxc.junit.opcodes.aastore.jm.T_aastore_7");
            fail("expected a verification exception");
        } catch (Throwable t) {
            DxUtil.checkVerifyException(t);
        }
    }
    public void testVFE6() {
        try {
            Class.forName("dxc.junit.opcodes.aastore.jm.T_aastore_8");
            fail("expected a verification exception");
        } catch (Throwable t) {
            DxUtil.checkVerifyException(t);
        }
    }
    public void testVFE7() {
        try {
            Class.forName("dxc.junit.opcodes.aastore.jm.T_aastore_9");
            fail("expected a verification exception");
        } catch (Throwable t) {
            DxUtil.checkVerifyException(t);
        }
    }
    public void testVFE8() {
        try {
            Class.forName("dxc.junit.opcodes.aastore.jm.T_aastore_12");
            fail("expected a verification exception");
        } catch (Throwable t) {
            DxUtil.checkVerifyException(t);
        }
    }
}
