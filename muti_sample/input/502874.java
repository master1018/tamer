public class Test_invokestatic extends DxTestCase {
    public void testN1() {
        T_invokestatic_1 t = new T_invokestatic_1();
        assertEquals(1234567, t.run());
    }
    public void testN2() {
        T_invokestatic_2 t = new T_invokestatic_2();
        assertEquals(777, t.run());
    }
    public void testN3() {
        assertEquals(123456789l, T_invokestatic_4.run());
    }
    public void testN4() {
        assertTrue(T_invokestatic_12.execute());
    }
    public void testN5() {
        T_invokestatic_15 t = new T_invokestatic_15();
        assertTrue(t.run());
    }
    public void testN6() {
        T_invokestatic_18 t = new T_invokestatic_18();
        assertEquals(888, t.run());
    }
    public void testE1() {
        try {
            T_invokestatic_5 t = new T_invokestatic_5();
            t.run();
            fail("expected IncompatibleClassChangeError");
        } catch (IncompatibleClassChangeError icce) {
        } catch (VerifyError vfe) { 
            System.out.print("dvmvfe:"); 
        }
    }
    public void testE2() {
        T_invokestatic_6 t = new T_invokestatic_6();
        try {
            t.run();
            fail("expected UnsatisfiedLinkError");
        } catch (UnsatisfiedLinkError ule) {
        }
    }
    public void testE3() {
        try {
            T_invokestatic_7 t = new T_invokestatic_7();
            t.run();
            fail("expected NoSuchMethodError");
        } catch (NoSuchMethodError nsme) {
        } catch (VerifyError vfe) { 
            System.out.print("dvmvfe:"); 
        }
    }
    public void testE5() {
        try {
            T_invokestatic_8 t = new T_invokestatic_8();
            t.run();
            fail("expected IllegalAccessError");
        } catch (IllegalAccessError iae) {
        } catch (VerifyError vfe) { 
            System.out.print("dvmvfe:"); 
        }
    }
    public void testE6() {
        try {
               T_invokestatic_13 t = new T_invokestatic_13();
            t.run();
            fail("expected NoSuchMethodError");
        } catch (NoSuchMethodError nsme) {
        } catch (VerifyError vfe) { 
            System.out.print("dvmvfe:"); 
        }
    }
    public void testE7() {
        T_invokestatic_14 t = new T_invokestatic_14();
        try {
            t.run();
            fail("expected Error");
        } catch (Error e) {
        }
    }
    public void testE8() {
        try {
            T_invokestatic_16 t = new T_invokestatic_16();
            t.run();
            fail("expected IncompatibleClassChangeError");
        } catch (IncompatibleClassChangeError iae) {
        } catch (VerifyError vfe) { 
            System.out.print("dvmvfe:"); 
        }
    }
    public void testE9() {
        try {
            T_invokestatic_17 t = new T_invokestatic_17();
            t.run();
            fail("expected IllegalAccessError");
        } catch (IllegalAccessError iae) {
        } catch (VerifyError vfe) { 
            System.out.print("dvmvfe:"); 
        }
    }
    public void testVFE1() {
        try {
            Class.forName("dxc.junit.opcodes.invokestatic.jm.T_invokestatic_3");
            fail("expected a verification exception");
        } catch (Throwable t) {
            DxUtil.checkVerifyException(t);
        }
    }
    public void testVFE2() {
        try {
            Class.forName("dxc.junit.opcodes.invokestatic.jm.T_invokestatic_9");
            fail("expected a verification exception");
        } catch (Throwable t) {
            DxUtil.checkVerifyException(t);
        }
    }
    public void testVFE3() {
        try {
            Class
                    .forName("dxc.junit.opcodes.invokestatic.jm.T_invokestatic_10");
            fail("expected a verification exception");
        } catch (Throwable t) {
            DxUtil.checkVerifyException(t);
        }
    }
    public void testVFE4() {
        try {
            Class
                    .forName("dxc.junit.opcodes.invokestatic.jm.T_invokestatic_11");
            fail("expected a verification exception");
        } catch (Throwable t) {
            DxUtil.checkVerifyException(t);
        }
    }
    public void testVFE5() {
        try {
            Class
                    .forName("dxc.junit.opcodes.invokestatic.jm.T_invokestatic_19");
            fail("expected a verification exception");
        } catch (Throwable t) {
            DxUtil.checkVerifyException(t);
        }
    }
    public void testVFE6() {
        try {
            Class
                    .forName("dxc.junit.opcodes.invokestatic.jm.T_invokestatic_20");
            fail("expected a verification exception");
        } catch (Throwable t) {
            DxUtil.checkVerifyException(t);
        }
    }
}
