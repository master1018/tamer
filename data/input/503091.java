public class Test_fill_array_data extends DxTestCase {
    public void testN1() {
        int arr[] = new int[5];
        T_fill_array_data_1 t = new T_fill_array_data_1();
        t.run(arr);
        for(int i = 0; i < 5; i++)
            assertEquals(i + 1, arr[i]);
     }
    public void testN2() {
        double arr[] = new double[5];
        T_fill_array_data_2 t = new T_fill_array_data_2();
        t.run(arr);
        for(int i = 0; i < 5; i++)
            assertEquals((double)(i + 1), arr[i]);
     }
    public void testN3() {
        int arr[] = new int[10];
        T_fill_array_data_1 t = new T_fill_array_data_1();
        t.run(arr);
        for(int i = 0; i < 5; i++)
            assertEquals(i + 1, arr[i]);
        for(int i = 5; i < 10; i++)
            assertEquals(0, arr[i]);
     }
    public void testE1() {
        T_fill_array_data_1 t = new T_fill_array_data_1();
        try {
            t.run(null);
        } catch(NullPointerException npe) {
        }
     }
    public void testE2() {
        int arr[] = new int[2];
        T_fill_array_data_1 t = new T_fill_array_data_1();
       try {
            t.run(arr);
        } catch(ArrayIndexOutOfBoundsException e) {
           }
     }
    public void testVFE1() {
        try {
            Class.forName("dot.junit.opcodes.fill_array_data.d.T_fill_array_data_3");
            fail("expected a verification exception");
        } catch (Throwable t) {
            DxUtil.checkVerifyException(t);
        }
    }
    public void testVFE2() {
        try {
            Class.forName("dot.junit.opcodes.fill_array_data.d.T_fill_array_data_4");
            fail("expected a verification exception");
        } catch (Throwable t) {
            DxUtil.checkVerifyException(t);
        }
    }
    public void testVFE3() {
        try {
            Class.forName("dot.junit.opcodes.fill_array_data.d.T_fill_array_data_5");
            fail("expected a verification exception");
        } catch (Throwable t) {
            DxUtil.checkVerifyException(t);
        }
    }
    public void testVFE4() {
        try {
            Class.forName("dot.junit.opcodes.fill_array_data.d.T_fill_array_data_6");
            fail("expected a verification exception");
        } catch (Throwable t) {
            DxUtil.checkVerifyException(t);
        }
    }
    public void testVFE5() {
        try {
            Class.forName("dot.junit.opcodes.fill_array_data.d.T_fill_array_data_7");
            fail("expected a verification exception");
        } catch (Throwable t) {
            DxUtil.checkVerifyException(t);
        }
    }
    public void testVFE6() {
        try {
            Class.forName("dot.junit.opcodes.fill_array_data.d.T_fill_array_data_8");
            fail("expected a verification exception");
        } catch (Throwable t) {
            DxUtil.checkVerifyException(t);
        }
    }
    public void testVFE7() {
        try {
            Class.forName("dot.junit.opcodes.fill_array_data.d.T_fill_array_data_9");
            fail("expected a verification exception");
        } catch (Throwable t) {
            DxUtil.checkVerifyException(t);
        }
    }
    public void testVFE9() {
        try {
            Class.forName("dot.junit.opcodes.fill_array_data.d.T_fill_array_data_11");
            fail("expected a verification exception");
        } catch (Throwable t) {
            DxUtil.checkVerifyException(t);
        }
    }
    public void testVFE10() {
        try {
            Class.forName("dot.junit.opcodes.fill_array_data.d.T_fill_array_data_12");
            fail("expected a verification exception");
        } catch (Throwable t) {
            DxUtil.checkVerifyException(t);
        }
    }
    public void testVFE11() {
        try {
            Class.forName("dot.junit.opcodes.fill_array_data.d.T_fill_array_data_13");
            fail("expected a verification exception");
        } catch (Throwable t) {
            DxUtil.checkVerifyException(t);
        }
    }
}
