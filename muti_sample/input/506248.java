public class Test_getstatic extends DxTestCase {
    public void testN1() {
        T_getstatic_1 t = new T_getstatic_1();
        assertEquals(5, t.run());
    }
    public void testN2() {
        T_getstatic_2 t = new T_getstatic_2();
        assertEquals(123d, t.run());
    }
    public void testN3() {
        T_getstatic_11 t = new T_getstatic_11();
        assertEquals(10, t.run());
    }
    public void testE1() {
        T_getstatic_5 t = new T_getstatic_5();
        try {
            t.run();
            fail("expected IncompatibleClassChangeError");
        } catch (IncompatibleClassChangeError e) {
        }
    }
    public void testE2() {
        try {
            T_getstatic_6 t = new T_getstatic_6();
            t.run();
            fail("expected IllegalAccessError");
        } catch (IllegalAccessError iae) {
        } catch (VerifyError vfe) { 
            System.out.print("dvmvfe:"); 
        }
    }
    public void testE3() {
        try {
            T_getstatic_7 t = new T_getstatic_7();
            t.run();
            fail("expected NoClassDefFoundError");
        } catch (NoClassDefFoundError e) {
        } catch (VerifyError vfe) { 
            System.out.print("dvmvfe:"); 
        }
    }
    public void testE4() {
        try {
               T_getstatic_8 t = new T_getstatic_8();
            t.run();
            fail("expected NoSuchFieldError");
        } catch (NoSuchFieldError e) {
        } catch (VerifyError vfe) { 
            System.out.print("dvmvfe:"); 
        }
    }
    public void testE5() {
        try {
            T_getstatic_10 t = new T_getstatic_10();
            t.run();
            fail("expected NoSuchFieldError");
        } catch (NoSuchFieldError e) {
        } catch (VerifyError vfe) { 
            System.out.print("dvmvfe:"); 
        }
    }
    public void testE6() {
        T_getstatic_9 t = new T_getstatic_9();
        try {
            t.run();
            fail("expected Error");
        } catch (Error e) {
        }
    }
    public void testE7() {
        try {
            T_getstatic_12 t = new T_getstatic_12();
            t.run();
        } catch (IllegalAccessError e) {
        } catch (VerifyError vfe) { 
            System.out.print("dvmvfe:"); 
        }
    }
    public void testVFE1() {
        try {
            Class.forName("dxc.junit.opcodes.getstatic.jm.T_getstatic_4");
            fail("expected a verification exception");
        } catch (Throwable t) {
            DxUtil.checkVerifyException(t);
        }
    }
    public void testVFE2() {
        try {
            Class.forName("dxc.junit.opcodes.getstatic.jm.T_getstatic_3");
            fail("expected a verification exception");
        } catch (Throwable t) {
            DxUtil.checkVerifyException(t);
        }
    }
    public void testVFE3() {
        try {
            Class.forName("dxc.junit.opcodes.getstatic.jm.T_getstatic_13");
            fail("expected a verification exception");
        } catch (Throwable t) {
            DxUtil.checkVerifyException(t);
        }
    }
}
