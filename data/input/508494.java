public class Test_filled_new_array extends DxTestCase {
    public void testN1() {
        T_filled_new_array_1 t = new T_filled_new_array_1();
        int[] arr = t.run(1, 2, 3, 4, 5);
        assertNotNull(arr);
        assertEquals(5, arr.length);
        for(int i = 0; i < 5; i++)
            assertEquals(i + 1, arr[i]);
     }
    public void testN2() {
        T_filled_new_array_2 t = new T_filled_new_array_2();
        String s = "android";
        Object[] arr = t.run(t, s);
        assertNotNull(arr);
        assertEquals(2, arr.length);
        assertEquals(t, arr[0]);
        assertEquals(s, arr[1]);
    }
    public void testVFE1() {
         try {
             Class.forName("dot.junit.opcodes.filled_new_array.d.T_filled_new_array_3");
             fail("expected a verification exception");
         } catch (Throwable t) {
             DxUtil.checkVerifyException(t);
         }
    }
    public void testVFE2() {
        try {
            Class.forName("dot.junit.opcodes.filled_new_array.d.T_filled_new_array_4");
            fail("expected a verification exception");
        } catch (Throwable t) {
            DxUtil.checkVerifyException(t);
        }
    }
    public void testVFE3() {
        try {
            Class.forName("dot.junit.opcodes.filled_new_array.d.T_filled_new_array_5");
            fail("expected a verification exception");
        } catch (Throwable t) {
            DxUtil.checkVerifyException(t);
        }
    }
    public void testVFE4() {
        try {
            Class.forName("dot.junit.opcodes.filled_new_array.d.T_filled_new_array_6");
            fail("expected a verification exception");
        } catch (Throwable t) {
            DxUtil.checkVerifyException(t);
        }
    }
    public void testVFE5() {
        try {
            Class.forName("dot.junit.opcodes.filled_new_array.d.T_filled_new_array_7");
            fail("expected a verification exception");
        } catch (Throwable t) {
            DxUtil.checkVerifyException(t);
        }
    }
    public void testVFE6() {
        try {
            Class.forName("dot.junit.opcodes.filled_new_array.d.T_filled_new_array_8");
            fail("expected a verification exception");
        } catch (Throwable t) {
            DxUtil.checkVerifyException(t);
        }
    }
    public void testVFE7() {
        try {
            Class.forName("dot.junit.opcodes.filled_new_array.d.T_filled_new_array_9");
            fail("expected a verification exception"); 
        } catch(Throwable t) { 
            DxUtil.checkVerifyException(t);    
        }
    }
    public void testVFE8() {
        try {
            Class.forName("dot.junit.opcodes.filled_new_array.d.T_filled_new_array_10");
            fail("expected a verification exception");
        } catch(Throwable t) {
            DxUtil.checkVerifyException(t);    
        }
    }
    public void testVFE9() {
        try {
            Class.forName("dot.junit.opcodes.filled_new_array.d.T_filled_new_array_11");
            fail("expected a verification exception");
        } catch(Throwable t) {
            DxUtil.checkVerifyException(t);    
        }
    }
}
