public class Test_putstatic extends DxTestCase {
    public void testN1() {
        T_putstatic_1 t = new T_putstatic_1();
        assertEquals(0, T_putstatic_1.st_i1);
        t.run();
        assertEquals(1000000, T_putstatic_1.st_i1);
    }
    public void testN2() {
        T_putstatic_2 t = new T_putstatic_2();
        assertEquals(0d, T_putstatic_2.st_d1);
        t.run();
        assertEquals(1000000d, T_putstatic_2.st_d1);
    }
    public void testN3() {
        T_putstatic_12 t = new T_putstatic_12();
        assertEquals(0, T_putstatic_12.st_i1);
        t.run();
        assertEquals(1000000, T_putstatic_12.st_i1);
    }
    public void testN4() {
        T_putstatic_14 t = new T_putstatic_14();
        assertEquals(0, T_putstatic_14.getProtectedField());
        t.run();
        assertEquals(1000000, T_putstatic_14.getProtectedField());
    }
    public void testN5() {
        T_putstatic_16 t = new T_putstatic_16();
        assertNull(T_putstatic_16.o);
        t.run();
        assertEquals("", (String) T_putstatic_16.o);
    }
    public void testE1() {
        try {
            T_putstatic_7 t = new T_putstatic_7();
            t.run();
            fail("expected IncompatibleClassChangeError");
        } catch (IncompatibleClassChangeError e) {
        } catch (VerifyError vfe) { 
            System.out.print("dvmvfe:"); 
        }
    }
    public void testE2() {
        try {
            T_putstatic_8 t = new T_putstatic_8();
            t.run();
            fail("expected IllegalAccessError");
        } catch (IllegalAccessError iae) {
        } catch (VerifyError vfe) { 
            System.out.print("dvmvfe:"); 
        }
    }
    public void testE3() {
        try {
            T_putstatic_9 t = new T_putstatic_9();
            t.run();
            fail("expected NoClassDefFoundError");
        } catch (NoClassDefFoundError e) {
        } catch (VerifyError vfe) { 
            System.out.print("dvmvfe:"); 
        }
    }
    public void testE4() {
        try {
            T_putstatic_10 t = new T_putstatic_10();
            t.run();
            fail("expected NoSuchFieldError");
        } catch (NoSuchFieldError e) {
        } catch (VerifyError vfe) { 
            System.out.print("dvmvfe:"); 
        }
    }
    public void testE5() {
        try {
            T_putstatic_11 t = new T_putstatic_11();
            t.run();
            fail("expected IllegalAccessError");
        } catch (IllegalAccessError iae) {
        } catch (VerifyError vfe) { 
            System.out.print("dvmvfe:"); 
        }
    }
    public void testE6() {
        T_putstatic_13 t = new T_putstatic_13();
        try {
            t.run();
            fail("expected Error");
        } catch (Error e) {
        }
    }
    public void testE7() {
        try {
            assertEquals(0, T_putstatic_15.getPvtField());
            T_putstatic_15 t = new T_putstatic_15();
            t.run();
            assertEquals(12321, T_putstatic_15.getPvtField());
        } catch (IllegalAccessError e) {
        } catch (VerifyError vfe) { 
            System.out.print("dvmvfe:"); 
        }
    }
    public void testVFE1() {
        try {
            Class.forName("dxc.junit.opcodes.putstatic.jm.T_putstatic_3");
            fail("expected a verification exception");
        } catch (Throwable t) {
            DxUtil.checkVerifyException(t);
        }
    }
    public void testVFE2() {
        try {
            Class.forName("dxc.junit.opcodes.putstatic.jm.T_putstatic_4");
            fail("expected a verification exception");
        } catch (Throwable t) {
            DxUtil.checkVerifyException(t);
        }
    }
    public void testVFE3() {
        try {
            Class.forName("dxc.junit.opcodes.putstatic.jm.T_putstatic_5");
            fail("expected a verification exception");
        } catch (Throwable t) {
            DxUtil.checkVerifyException(t);
        }
    }
    public void testVFE4() {
        try {
            Class.forName("dxc.junit.opcodes.putstatic.jm.T_putstatic_6");
            fail("expected a verification exception");
        } catch (Throwable t) {
            DxUtil.checkVerifyException(t);
        }
    }
    public void testVFE5() {
        try {
            Class.forName("dxc.junit.opcodes.putstatic.jm.T_putstatic_18");
            fail("expected a verification exception");
        } catch (Throwable t) {
            DxUtil.checkVerifyException(t);
        }
    }
    public void testVFE6() {
        try {
            Class.forName("dxc.junit.opcodes.putstatic.jm.T_putstatic_17");
            fail("expected a verification exception");
        } catch (Throwable t) {
            DxUtil.checkVerifyException(t);
        }
    }
}
