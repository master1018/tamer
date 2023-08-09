public class Test_invokevirtual extends DxTestCase {
    public void testN1() {
        T_invokevirtual_1 t = new T_invokevirtual_1();
        int a = 1;
        String sa = "a" + a;
        String sb = "a1";
        assertTrue(t.run(sa, sb));
        assertFalse(t.run(this, sa));
        assertFalse(t.run(sb, this));
    }
    public void testN2() {
        assertTrue(T_invokevirtual_2.execute());
    }
    public void testN3() {
        T_invokevirtual_7 t = new T_invokevirtual_7();
        assertEquals(5, t.run());
    }
    public void testN4() {
        T_invokevirtual_13 t = new T_invokevirtual_13();
        assertEquals(345, t.run());
    }
    public void testN5() {
        T_invokevirtual_14 t = new T_invokevirtual_14();
        assertTrue(t.run());
    }
    public void testN6() {
        T_invokevirtual_17 t = new T_invokevirtual_17();
        assertEquals(5, t.run());
    }
    public void testE1() {
        T_invokevirtual_3 t = new T_invokevirtual_3();
        String s = "s";
        try {
            t.run(null, s);
            fail("expected NullPointerException");
        } catch (NullPointerException npe) {
        }
    }
    public void testE2() {
        T_invokevirtual_4 t = new T_invokevirtual_4();
        try {
            t.run();
            fail("expected UnsatisfiedLinkError");
        } catch (UnsatisfiedLinkError ule) {
        }
    }
    public void testE3() {
        try {
            T_invokevirtual_5 t = new T_invokevirtual_5();
            t.run();
            fail("expected IncompatibleClassChangeError");
        } catch (IncompatibleClassChangeError icce) {
        } catch (VerifyError vfe) { 
            System.out.print("dvmvfe:"); 
        }
    }
    public void testE4() {
        T_invokevirtual_6 t = new T_invokevirtual_6();
        try {
            t.run();
            fail("expected AbstractMethodError");
        } catch (AbstractMethodError iae) {
        }
    }
    public void testE5() {
        try {
            T_invokevirtual_15 t = new T_invokevirtual_15();
            t.run();
            fail("expected NoSuchMethodError");
        } catch (NoSuchMethodError iae) {
        } catch (VerifyError vfe) { 
            System.out.print("dvmvfe:"); 
        }
    }
    public void testE6() {
        try {
            T_invokevirtual_18 t = new T_invokevirtual_18();
            t.run(new TProtected());
            fail("expected IllegalAccessError");
        } catch (IllegalAccessError iae) {
        } catch (VerifyError vfe) { 
            System.out.print("dvmvfe:"); 
        }
    }
    public void testE7() {
        try {
            T_invokevirtual_20 t = new T_invokevirtual_20();
            t.run(new TProtected());
            fail("expected IllegalAccessError");
        } catch (IllegalAccessError iae) {
        } catch (VerifyError vfe) { 
            System.out.print("dvmvfe:"); 
        }
    }
    public void testE8() {
        try {
            T_invokevirtual_19 t = new T_invokevirtual_19();
            t.run();
            fail("expected NoSuchMethodError");
        } catch (NoSuchMethodError iae) {
        } catch (VerifyError vfe) { 
            System.out.print("dvmvfe:"); 
        }
    }
    public void testVFE1() {
        try {
            Class
                    .forName("dxc.junit.opcodes.invokevirtual.jm.T_invokevirtual_8");
            fail("expected a verification exception");
        } catch (Throwable t) {
            DxUtil.checkVerifyException(t);
        }
    }
    public void testVFE2() {
        try {
            Class
                    .forName("dxc.junit.opcodes.invokevirtual.jm.T_invokevirtual_9");
            fail("expected a verification exception");
        } catch (Throwable t) {
            DxUtil.checkVerifyException(t);
        }
    }
    public void testVFE3() {
        try {
            Class
                    .forName("dxc.junit.opcodes.invokevirtual.jm.T_invokevirtual_10");
            fail("expected a verification exception");
        } catch (Throwable t) {
            DxUtil.checkVerifyException(t);
        }
    }
    public void testVFE4() {
        try {
            Class
                    .forName("dxc.junit.opcodes.invokevirtual.jm.T_invokevirtual_11");
            fail("expected a verification exception");
        } catch (Throwable t) {
            DxUtil.checkVerifyException(t);
        }
    }
    public void testVFE5() {
        try {
            Class
                    .forName("dxc.junit.opcodes.invokevirtual.jm.T_invokevirtual_12");
            fail("expected a verification exception");
        } catch (Throwable t) {
            DxUtil.checkVerifyException(t);
        }
    }
    public void testVFE6() {
        try {
            Class
                    .forName("dxc.junit.opcodes.invokevirtual.jm.T_invokevirtual_16");
            fail("expected a verification exception");
        } catch (Throwable t) {
            DxUtil.checkVerifyException(t);
        }
    }
    public void testVFE7() {
        try {
            Class
                    .forName("dxc.junit.opcodes.invokevirtual.jm.T_invokevirtual_21");
            fail("expected a verification exception");
        } catch (Throwable t) {
            DxUtil.checkVerifyException(t);
        }
    }
    public void testVFE8() {
        try {
            Class
                    .forName("dxc.junit.opcodes.invokevirtual.jm.T_invokevirtual_22");
            fail("expected a verification exception");
        } catch (Throwable t) {
            DxUtil.checkVerifyException(t);
        }
    }
    public void testVFE9() {
        try {
            Class
                    .forName("dxc.junit.opcodes.invokevirtual.jm.T_invokevirtual_23");
            fail("expected a verification exception");
        } catch (Throwable t) {
            DxUtil.checkVerifyException(t);
        }
    }
}
