public class Test_multianewarray extends DxTestCase {
    public void testN1() {
        T_multianewarray_1 t = new T_multianewarray_1();
        String[][][] res = t.run(2, 5, 4);
        assertEquals(2, res.length);
        for (int i = 0; i < 2; i++) {
            assertEquals(5, res[i].length);
            for (int j = 0; j < 5; j++) {
                assertEquals(4, res[i][j].length);
                for (int k = 0; j < 4; j++) {
                    assertNull(res[i][j][k]);
                }
            }
        }
    }
    public void testN2() {
        T_multianewarray_1 t = new T_multianewarray_1();
        String[][][] res = t.run(2, 0, 4);
        try {
            String s = res[2][0][0];
            fail("expected ArrayIndexOutOfBoundsException");
            fail("dummy for s "+s);
        } catch (ArrayIndexOutOfBoundsException ae) {
        }
    }
    public void testN3() {
        T_multianewarray_9 t = new T_multianewarray_9();
        String[][][] res = t.run(2, 1, 4);
        if (res.length != 2) fail("incorrect multiarray length");
        if (res[0].length != 1) fail("incorrect array length");
        try {
            int i = res[0][0].length;
            fail("expected NullPointerException");
            fail("dummy for i "+i);
        } catch (NullPointerException npe) {
        }
    }
    public void testE1() {
        T_multianewarray_1 t = new T_multianewarray_1();
        try {
            t.run(2, -5, 3);
            fail("expected NegativeArraySizeException");
        } catch (NegativeArraySizeException nase) {
        }
    }
    public void testE2() {
        try {
            T_multianewarray_2 t = new T_multianewarray_2();
            t.run(2, 5, 3);
            fail("expected IllegalAccessError");
        } catch (IllegalAccessError iae) {
        } catch (VerifyError vfe) { 
            System.out.print("dvmvfe:"); 
        }
    }
    public void testE3() {
        try {
        T_multianewarray_7 t = new T_multianewarray_7();
            t.run(2, 5, 3);
            fail("expected NoClassDefFoundError");
        } catch (NoClassDefFoundError e) {
        } catch (VerifyError vfe) { 
            System.out.print("dvmvfe:"); 
        }
    }
    public void testVFE1() {
        try {
            Class
                    .forName("dxc.junit.opcodes.multianewarray.jm.T_multianewarray_3");
            fail("expected a verification exception");
        } catch (Throwable t) {
            DxUtil.checkVerifyException(t);
        }
    }
    public void testVFE2() {
        try {
            Class
                    .forName("dxc.junit.opcodes.multianewarray.jm.T_multianewarray_4");
            fail("expected a verification exception");
        } catch (Throwable t) {
            DxUtil.checkVerifyException(t);
        }
    }
    public void testVFE3() {
        try {
            Class
                    .forName("dxc.junit.opcodes.multianewarray.jm.T_multianewarray_5");
            fail("expected a verification exception");
        } catch (Throwable t) {
            DxUtil.checkVerifyException(t);
        }
    }
    public void testVFE4() {
        try {
            Class
                    .forName("dxc.junit.opcodes.multianewarray.jm.T_multianewarray_6");
            fail("expected a verification exception");
        } catch (Throwable t) {
            DxUtil.checkVerifyException(t);
        }
    }
    public void testVFE5() {
        try {
            Class
                    .forName("dxc.junit.opcodes.multianewarray.jm.T_multianewarray_8");
            fail("expected a verification exception");
        } catch (Throwable t) {
            DxUtil.checkVerifyException(t);
        }
    }
    public void testVFE6() {
        try {
            Class
                    .forName("dxc.junit.opcodes.multianewarray.jm.T_multianewarray_10");
            fail("expected a verification exception");
        } catch (Throwable t) {
            DxUtil.checkVerifyException(t);
        }
    }
}
