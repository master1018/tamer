public class Test_putfield extends DxTestCase {
    public void testN1() {
        T_putfield_1 t = new T_putfield_1();
        assertEquals(0, t.st_i1);
        t.run();
        assertEquals(1000000, t.st_i1);
    }
    public void testN2() {
        T_putfield_2 t = new T_putfield_2();
        assertEquals(0d, t.st_d1);
        t.run();
        assertEquals(1000000d, t.st_d1);
    }
    public void testN3() {
        T_putfield_12 t = new T_putfield_12();
        assertEquals(0, t.st_i1);
        t.run();
        assertEquals(1000000, t.st_i1);
    }
    public void testN4() {
        T_putfield_14 t = new T_putfield_14();
        assertEquals(0, t.getProtectedField());
        t.run();
        assertEquals(1000000, t.getProtectedField());
    }
    public void testN5() {
        T_putfield_18 t = new T_putfield_18();
        assertEquals(0, t.run().compareTo("xyz"));
    }
    public void testN6() {
        T_putfield_16 t = new T_putfield_16();
        assertNull(t.o);
        t.run();
        assertEquals("", (String) t.o);
    }
    public void testE1() {
        try {
            T_putfield_7 t = new T_putfield_7();
            t.run();
            fail("expected IncompatibleClassChangeError");
        } catch (IncompatibleClassChangeError e) {
        } catch (VerifyError vfe) { 
            System.out.print("dvmvfe:"); 
        }
    }
    public void testE2() {
        try {
            T_putfield_8 t = new T_putfield_8();
            t.run();
            fail("expected IllegalAccessError");
        } catch (IllegalAccessError iae) {
        } catch (VerifyError vfe) { 
            System.out.print("dvmvfe:"); 
        }
    }
    public void testE3() {
        T_putfield_9 t = new T_putfield_9();
        try {
            t.run();
            fail("expected NullPointerException");
        } catch (NullPointerException e) {
        }
    }
    public void testE4() {
        try {
        T_putfield_10 t = new T_putfield_10();
            t.run();
            fail("expected NoSuchFieldError");
        } catch (NoSuchFieldError e) {
        } catch (VerifyError vfe) { 
            System.out.print("dvmvfe:"); 
        }
    }
    public void testE5() {
        try {
            T_putfield_11 t = new T_putfield_11();
            t.run();
            fail("expected IllegalAccessError");
        } catch (IllegalAccessError iae) {
        } catch (VerifyError vfe) { 
            System.out.print("dvmvfe:"); 
        }
    }
    public void testE6() {
        try {
            T_putfield_15 t = new T_putfield_15();
            assertEquals(0, t.getPvtField());
            t.run();
            assertEquals(10101, t.getPvtField());
        } catch (IllegalAccessError e) {
        } catch (VerifyError vfe) { 
            System.out.print("dvmvfe:"); 
        }
    }
    public void testE7() {
        try {
            T_putfield_13 t = new T_putfield_13();
            t.run();
            fail("expected IllegalAccessError");
        } catch (IllegalAccessError iae) {
        } catch (VerifyError vfe) { 
            System.out.print("dvmvfe:"); 
        }
    }
    public void testVFE1() {
        try {
            Class.forName("dxc.junit.opcodes.putfield.jm.T_putfield_3");
            fail("expected a verification exception");
        } catch (Throwable t) {
            DxUtil.checkVerifyException(t);
        }
    }
    public void testVFE2() {
        try {
            Class.forName("dxc.junit.opcodes.putfield.jm.T_putfield_4");
            fail("expected a verification exception");
        } catch (Throwable t) {
            DxUtil.checkVerifyException(t);
        }
    }
    public void testVFE3() {
        try {
            Class.forName("dxc.junit.opcodes.putfield.jm.T_putfield_5");
            fail("expected a verification exception");
        } catch (Throwable t) {
            DxUtil.checkVerifyException(t);
        }
    }
    public void testVFE4() {
        try {
            Class.forName("dxc.junit.opcodes.putfield.jm.T_putfield_6");
            fail("expected a verification exception");
        } catch (Throwable t) {
            DxUtil.checkVerifyException(t);
        }
    }
    public void testVFE5() {
        try {
            Class.forName("dxc.junit.opcodes.putfield.jm.T_putfield_20");
            fail("expected a verification exception");
        } catch (Throwable t) {
            DxUtil.checkVerifyException(t);
        }
    }
    public void testVFE6() {
        try {
            Class.forName("dxc.junit.opcodes.putfield.jm.T_putfield_19");
            fail("expected a verification exception");
        } catch (Throwable t) {
            DxUtil.checkVerifyException(t);
        }
    }
}
