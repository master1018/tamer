public class Test_newarray extends DxTestCase {
    public void testN1() {
        T_newarray_1 t = new T_newarray_1();
        int[] r = t.run(10);
        int l = r.length;
        assertEquals(10, l);
        for (int i = 0; i < l; i++) {
            assertEquals(0, r[i]);
        }
    }
    public void testN2() {
        T_newarray_2 t = new T_newarray_2();
        float[] r = t.run(10);
        int l = r.length;
        assertEquals(10, l);
        for (int i = 0; i < l; i++) {
            assertEquals(0f, r[i]);
        }
    }
    public void testE1() {
        T_newarray_2 t = new T_newarray_2();
        try {
            t.run(-1);
            fail("expected NegativeArraySizeException");
        } catch (NegativeArraySizeException nase) {
        }
    }
    public void testB1() {
        T_newarray_1 t = new T_newarray_1();
        int[] r = t.run(0);
        assertNotNull(r);
        assertEquals(0, r.length);
    }
    public void testVFE1() {
        try {
            Class.forName("dxc.junit.opcodes.newarray.jm.T_newarray_3");
            fail("expected a verification exception");
        } catch (Throwable t) {
            DxUtil.checkVerifyException(t);
        }
    }
    public void testVFE2() {
        try {
            Class.forName("dxc.junit.opcodes.newarray.jm.T_newarray_4");
            fail("expected a verification exception");
        } catch (Throwable t) {
            DxUtil.checkVerifyException(t);
        }
    }
    public void testVFE3() {
        try {
            Class.forName("dxc.junit.opcodes.newarray.jm.T_newarray_5");
            fail("expected a verification exception");
        } catch (Throwable t) {
            DxUtil.checkVerifyException(t);
        }
    }
    public void testVFE4() {
        try {
            Class.forName("dxc.junit.opcodes.newarray.jm.T_newarray_6");
            fail("expected a verification exception");
        } catch (Throwable t) {
            DxUtil.checkVerifyException(t);
        }
    }
}
