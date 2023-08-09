public class Test_invokeinterface extends DxTestCase {
    public void testN1() {
        T_invokeinterface_1 t = new T_invokeinterface_1();
        assertEquals(0, t.run("aa", "aa"));
        assertEquals(-1, t.run("aa", "bb"));
        assertEquals(1, t.run("bb", "aa"));
    }
    public void testN2() {
        T_invokeinterface_14 t = new T_invokeinterface_14();
        ITestImpl impl = new ITestImpl();
        assertEquals(1, t.run(impl));
    }
    public void testN3() {
        assertTrue(T_invokeinterface_19.execute());
    }
    public void testE1() {
        try {
            T_invokeinterface_7 t = new T_invokeinterface_7();
            ITestImpl impl = new ITestImpl();
            t.run(impl);
            fail("expected NoSuchMethodError");
        } catch (NoSuchMethodError e) {
        } catch (VerifyError vfe) { 
            System.out.print("dvmvfe:"); 
        }
    }
    public void testE2() {
        try {
            T_invokeinterface_16 t = new T_invokeinterface_16();
            ITestImpl impl = new ITestImpl();
            t.run(impl);
            fail("expected NoSuchMethodError");
        } catch (NoSuchMethodError e) {
        } catch (VerifyError vfe) { 
            System.out.print("dvmvfe:"); 
        }
    }
    public void testE3() {
        try {
            new T_invokeinterface_3(null);
            fail("expected NullPointerException");
        } catch (NullPointerException npe) {
        }
    }
    public void testE4() {
        T_invokeinterface_11 t = new T_invokeinterface_11();
        ITestImpl impl = new ITestImpl();
        try {
            t.run(impl);
            fail("expected IncompatibleClassChangeError");
        } catch (IncompatibleClassChangeError e) {
        }
    }
    public void testE5() {
        T_invokeinterface_12 t = new T_invokeinterface_12();
        ITestImpl impl = new ITestImpl();
        try {
            t.run(impl);
            fail("expected UnsatisfiedLinkError");
        } catch (UnsatisfiedLinkError e) {
        }
    }
    public void testE6() {
        try {
            T_invokeinterface_13 t = new T_invokeinterface_13();
            ITestImpl impl = new ITestImpl();
            t.run(impl);
            fail("expected AbstractMethodError");
        } catch (AbstractMethodError e) {
        } catch (VerifyError vfe) { 
            System.out.print("dvmvfe:"); 
        }
    }
    public void testE7() {
        try {
            T_invokeinterface_15 t = new T_invokeinterface_15();
            ITestImpl impl = new ITestImpl();
            t.run(impl);
            fail("expected AbstractMethodError");
        } catch (AbstractMethodError e) {
        } catch (VerifyError vfe) { 
            System.out.print("dvmvfe:"); 
        }
    }
    public void testE8() {
        try {
            T_invokeinterface_17 t = new T_invokeinterface_17();
            ITestImpl impl = new ITestImpl();
            t.run(impl);
            fail("expected IllegalAccessError");
        } catch (IllegalAccessError e) {
        } catch (VerifyError vfe) { 
            System.out.print("dvmvfe:"); 
        }
    }
    public void testVFE1() {
        try {
            Class
                    .forName("dxc.junit.opcodes.invokeinterface.jm.T_invokeinterface_2");
            fail("expected a verification exception");
        } catch (Throwable t) {
            DxUtil.checkVerifyException(t);
        }
    }
    public void testVFE2() {
        try {
            Class
                    .forName("dxc.junit.opcodes.invokeinterface.jm.T_invokeinterface_23");
            fail("expected a verification exception");
        } catch (Throwable t) {
            DxUtil.checkVerifyException(t);
        }
    }
    public void testVFE5() {
        try {
            Class
                    .forName("dxc.junit.opcodes.invokeinterface.jm.T_invokeinterface_5");
            fail("expected a verification exception");
        } catch (Throwable t) {
            DxUtil.checkVerifyException(t);
        }
    }
    public void testVFE6() {
        try {
            Class
                    .forName("dxc.junit.opcodes.invokeinterface.jm.T_invokeinterface_10");
            fail("expected a verification exception");
        } catch (Throwable t) {
            DxUtil.checkVerifyException(t);
        }
    }
    public void testVFE7() {
        try {
            Class
                    .forName("dxc.junit.opcodes.invokeinterface.jm.T_invokeinterface_8");
            fail("expected a verification exception");
        } catch (Throwable t) {
            DxUtil.checkVerifyException(t);
        }
    }
    public void testVFE8() {
        try {
            Class
                    .forName("dxc.junit.opcodes.invokeinterface.jm.T_invokeinterface_6");
            fail("expected a verification exception");
        } catch (Throwable t) {
            DxUtil.checkVerifyException(t);
        }
    }
    public void testVFE9() {
        try {
            Class
                    .forName("dxc.junit.opcodes.invokeinterface.jm.T_invokeinterface_9");
            fail("expected a verification exception");
        } catch (Throwable t) {
            DxUtil.checkVerifyException(t);
        }
    }
    public void testVFE10() {
        try {
            Class
                    .forName("dxc.junit.opcodes.invokeinterface.jm.T_invokeinterface_18");
            fail("expected a verification exception");
        } catch (Throwable t) {
            DxUtil.checkVerifyException(t);
        }
    }
    public void testVFE11() {
        try {
            Class
                    .forName("dxc.junit.opcodes.invokeinterface.jm.T_invokeinterface_20");
            fail("expected a verification exception");
        } catch (Throwable t) {
            DxUtil.checkVerifyException(t);
        }
    }
    public void testVFE12() {
        try {
            Class
                    .forName("dxc.junit.opcodes.invokeinterface.jm.T_invokeinterface_21");
            fail("expected a verification exception");
        } catch (Throwable t) {
            DxUtil.checkVerifyException(t);
        }
    }
}
