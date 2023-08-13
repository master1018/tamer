public class Test_getfield extends DxTestCase {
    private int TestStubField = 123;
    protected int TestStubFieldP = 0;
    private int privateInt = 456;
    public void testN1() {
        T_getfield_1 t = new T_getfield_1();
        assertEquals(5, t.run());
    }
    public void testN2() {
        T_getfield_2 t = new T_getfield_2();
        assertEquals(123d, t.run());
    }
    public void testN3() {
        T_getfield_11 t = new T_getfield_11();
        assertEquals(10, t.run());
    }
    public void testN4() {
        T_getfield_14 t = new T_getfield_14();
        assertEquals(0, t.run().compareTo("abc"));
    }
    public void testE1() {
        try {
            T_getfield_5 t = new T_getfield_5();
            t.run();
            fail("expected IncompatibleClassChangeError");
        } catch (IncompatibleClassChangeError e) {
        } catch (VerifyError vfe) { 
            System.out.print("dvmvfe:"); 
        } 
    }
    public void testE2() {
        try {
            T_getfield_6 t = new T_getfield_6();
            int res = t.run();
            System.out.println("res:"+res);
            fail("expected IllegalAccessError");
        } catch (IllegalAccessError iae) {
        } catch (VerifyError vfe) { 
            System.out.print("dvmvfe:"); 
        }
    }
    public void testE3() {
        try {
            T_getfield_7 t = new T_getfield_7();
            t.run();
            fail("expected NoClassDefFoundError");
        } catch (NoClassDefFoundError e) {
        } catch (VerifyError vfe) { 
            System.out.print("dvmvfe:"); 
        }
    }
    public void testE4() {
        try {
            T_getfield_8 t = new T_getfield_8();
            t.run();
            fail("expected NoSuchFieldError");
        } catch (NoSuchFieldError e) {
        } catch (VerifyError vfe) { 
            System.out.print("dvmvfe:"); 
        }
    }
    public void testE5() {
        try {
            T_getfield_10 t = new T_getfield_10();
            t.run();
            fail("expected NoSuchFieldError");
        } catch (NoSuchFieldError e) {
        } catch (VerifyError vfe) { 
            System.out.print("dvmvfe:"); 
        }
    }
    public void testE6() {
        T_getfield_9 t = new T_getfield_9();
        try {
            t.run();
            fail("expected NullPointerException");
        } catch (NullPointerException e) {
        }
    }
    public void testE7() {
        try {
            T_getfield_12 t = new T_getfield_12();
        } catch (IllegalAccessError e) {
        } catch (VerifyError vfe) { 
            System.out.print("dvmvfe:"); 
        }
    }
    public void testE9() {
        try {
            T_getfield_17 t = new T_getfield_17();
            t.run(new dxc.junit.opcodes.getfield.jm.TestStubs());
            fail("expected IllegalAccessError");
        } catch (IllegalAccessError e) {
        } catch (VerifyError vfe) { 
            System.out.print("dvmvfe:"); 
        }
    }
    public void testE8() {
        try {
            T_getfield_16 t = new T_getfield_16();
            t.run();
            fail("expected IllegalAccessError");
        } catch (IllegalAccessError iae) {
        } catch (VerifyError vfe) { 
            System.out.print("dvmvfe:"); 
        }
    }
    public void testVFE1() {
        try {
            Class.forName("dxc.junit.opcodes.getfield.jm.T_getfield_4");
            fail("expected a verification exception");
        } catch (Throwable t) {
            DxUtil.checkVerifyException(t);
        }
    }
    public void testVFE2() {
        try {
            Class.forName("dxc.junit.opcodes.getfield.jm.T_getfield_3");
            fail("expected a verification exception");
        } catch (Throwable t) {
            DxUtil.checkVerifyException(t);
        }
    }
    public void testVFE3() {
        try {
            Class.forName("dxc.junit.opcodes.getfield.jm.T_getfield_13");
            fail("expected a verification exception");
        } catch (Throwable t) {
            DxUtil.checkVerifyException(t);
        }
    }
    public void testVFE4() {
        try {
            Class.forName("dxc.junit.opcodes.getfield.jm.T_getfield_15");
            fail("expected a verification exception");
        } catch (Throwable t) {
            DxUtil.checkVerifyException(t);
        }
    }
}
