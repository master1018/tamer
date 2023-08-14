public class Test_new_array extends DxTestCase {
     @SuppressWarnings("unused")
     private class TestStub{
     }
    public void testN1() {
        T_new_array_1 t = new T_new_array_1();
        int[] r = t.run(10);
        int l = r.length;
        assertEquals(10, l);
        for (int i = 0; i < l; i++) {
            assertEquals(0, r[i]);
        }
    }
    public void testN2() {
        T_new_array_2 t = new T_new_array_2();
        boolean[] r = t.run(10);
        int l = r.length;
        assertEquals(10, l);
        for (int i = 0; i < l; i++) {
            assertFalse(r[i]);
        }
    }
    public void testN3() {
        T_new_array_3 t = new T_new_array_3();
        Object[] r = t.run(10);
        int l = r.length;
        assertEquals(10, l);
        for (int i = 0; i < l; i++) {
            assertNull(r[i]);
        }
    }
    public void testB1() {
        T_new_array_1 t = new T_new_array_1();
        int[] r = t.run(0);
        assertNotNull(r);
        assertEquals(0, r.length);
    }    
    public void testE1() {
        T_new_array_2 t = new T_new_array_2();
        try {
            t.run(-1);
            fail("expected NegativeArraySizeException");
        } catch (NegativeArraySizeException nase) {
        }
    }
    public void testVFE1() {
        try {
            Class.forName("dot.junit.opcodes.new_array.d.T_new_array_4");
            fail("expected a verification exception");
        } catch (Throwable t) {
            DxUtil.checkVerifyException(t);
        }
    }
    public void testVFE2() {
        try {
            Class.forName("dot.junit.opcodes.new_array.d.T_new_array_5");
            fail("expected a verification exception");
        } catch (Throwable t) {
            DxUtil.checkVerifyException(t);
        }
    }
    public void testVFE3() {
        try {
            Class.forName("dot.junit.opcodes.new_array.d.T_new_array_9");
            fail("expected a verification exception");
        } catch (Throwable t) {
            DxUtil.checkVerifyException(t);
        }
    }
    public void testVFE4() {
        try {
            Class.forName("dot.junit.opcodes.new_array.d.T_new_array_6");
            fail("expected a verification exception");
        } catch (Throwable t) {
            DxUtil.checkVerifyException(t);
        }
    }
    public void testVFE5() {
        try {
            Class.forName("dot.junit.opcodes.new_array.d.T_new_array_7");
            fail("expected a verification exception");
        } catch (Throwable t) {
            DxUtil.checkVerifyException(t);
        }
    }
    public void testVFE6() {
        try {
            Class.forName("dot.junit.opcodes.new_array.d.T_new_array_8");
            fail("expected a verification exception");
        } catch (Throwable t) {
            DxUtil.checkVerifyException(t);
        }
    }
    public void testVFE7() {
        try {
            Class.forName("dot.junit.opcodes.new_array.d.T_new_array_11");
            fail("expected a verification exception");
        } catch (Throwable t) {
            DxUtil.checkVerifyException(t);
        }
    }
    public void testVFE8() {
        try {
            Class.forName("dot.junit.opcodes.new_array.d.T_new_array_10");
            fail("expected a verification exception");
        } catch (Throwable t) {
            DxUtil.checkVerifyException(t);
        }
    }
}
