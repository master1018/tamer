public class Test_anewarray extends DxTestCase {
    public void testN1() {
        T_anewarray_1 t = new T_anewarray_1();
        Object[] arr = t.run(10);
        assertNotNull(arr);
        assertEquals(10, arr.length);
        for (int i = 0; i < 10; i++)
            assertNull(arr[i]);
    }
    public void testN2() {
        T_anewarray_1 t = new T_anewarray_1();
        String[] arr2 = t.run2(5);
        assertNotNull(arr2);
        assertEquals(5, arr2.length);
        for (int i = 0; i < 5; i++)
            assertNull(arr2[i]);
    }
    public void testN3() {
        T_anewarray_1 t = new T_anewarray_1();
        Integer[] arr3 = t.run3(15);
        assertNotNull(arr3);
        assertEquals(15, arr3.length);
        for (int i = 0; i < 15; i++)
            assertNull(arr3[i]);
    }
    public void testE1() {
        T_anewarray_1 t = new T_anewarray_1();
        Object[] res = t.run(0);
        try {
            Object s = res[0];
            fail("expected ArrayIndexOutOfBoundsException");
        } catch (ArrayIndexOutOfBoundsException ae) {
        }
    }
    public void testE2() {
        T_anewarray_1 t = new T_anewarray_1();
        try {
            t.run(-2);
            fail("expected NegativeArraySizeException");
        } catch (NegativeArraySizeException nase) {
        }
    }
    public void testE3() {
        try {
            T_anewarray_6 t = new T_anewarray_6();
            t.run();
            fail("expected NoClassDefFoundError");
        } catch (NoClassDefFoundError e) {
        } catch (VerifyError vfe) { 
            System.out.print("dvmvfe:"); 
        }
    }
    public void testE4() {
        try {
            T_anewarray_7 t = new T_anewarray_7();
            t.run();
            fail("expected IllegalAccessError");
        } catch (IllegalAccessError e) {
        } catch (VerifyError vfe) { 
            System.out.print("dvmvfe:"); 
        }
    }
    public void testVFE1() {
        try {
            Class.forName("dxc.junit.opcodes.anewarray.jm.T_anewarray_2");
            fail("expected a verification exception");
        } catch (Throwable t) {
            DxUtil.checkVerifyException(t);
        }
    }
    public void testVFE2() {
        try {
            Class.forName("dxc.junit.opcodes.anewarray.jm.T_anewarray_3");
            fail("expected a verification exception");
        } catch (Throwable t) {
            DxUtil.checkVerifyException(t);
        }
    }
    public void testVFE3() {
        try {
            Class.forName("dxc.junit.opcodes.anewarray.jm.T_anewarray_4");
            fail("expected a verification exception");
        } catch (Throwable t) {
            DxUtil.checkVerifyException(t);
        }
    }
    public void testVFE4() {
        try {
            Class.forName("dxc.junit.opcodes.anewarray.jm.T_anewarray_5");
            fail("expected a verification exception");
        } catch (Throwable t) {
            DxUtil.checkVerifyException(t);
        }
    }
    public void testVFE5() {
        try {
            Class.forName("dxc.junit.opcodes.anewarray.jm.T_anewarray_8");
            fail("expected a verification exception");
        } catch (Throwable t) {
            DxUtil.checkVerifyException(t);
        }
    }
    public void testVFE6() {
        try {
            Class.forName("dxc.junit.opcodes.anewarray.jm.T_anewarray_9");
            fail("expected a verification exception");
        } catch (Throwable t) {
            DxUtil.checkVerifyException(t);
        }
    }
}
