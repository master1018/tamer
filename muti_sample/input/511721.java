public class Test_invoke_interface extends DxTestCase {
    public void testN1() {
        T_invoke_interface_1 t = new T_invoke_interface_1();
        assertEquals(0, t.run("aa", "aa"));
        assertEquals(-1, t.run("aa", "bb"));
        assertEquals(1, t.run("bb", "aa"));
    }
    public void testN2() {
        T_invoke_interface_14 t = new T_invoke_interface_14();
        ITestImpl impl = new ITestImpl();
        assertEquals(1, t.run(impl));
    }
    public void testE3() {
        try {
            new T_invoke_interface_3(null);
            fail("expected NullPointerException");
        } catch (NullPointerException npe) {
        }
    }
    public void testE4() {
        T_invoke_interface_11 t = new T_invoke_interface_11();
        try {
            t.run();
            fail("expected IncompatibleClassChangeError");
        } catch (IncompatibleClassChangeError e) {
        }
    }
    public void testE5() {
        T_invoke_interface_12 t = new T_invoke_interface_12();
        ITestImpl impl = new ITestImpl();
        try {
            t.run(impl);
            fail("expected UnsatisfiedLinkError");
        } catch (UnsatisfiedLinkError e) {
        }
    }
    public void testE6() {
        T_invoke_interface_13 t = new T_invoke_interface_13();
        try {
            t.run();
            fail("expected AbstractMethodError");
        } catch (AbstractMethodError e) {
        }
    }
    public void testE8() {
        try {
            T_invoke_interface_17 t = new T_invoke_interface_17();
            t.run();
            fail("expected a verification exception");
        } catch (IllegalAccessError e) {
        } catch (VerifyError e) {
        }
    }
    public void testVFE1() {
        try {
            Class.forName("dot.junit.opcodes.invoke_interface.d.T_invoke_interface_2");
            fail("expected a verification exception");
        } catch (Throwable t) {
            DxUtil.checkVerifyException(t);
        }
    }
    public void testVFE2() {
        try {
            Class.forName("dot.junit.opcodes.invoke_interface.d.T_invoke_interface_4");
            fail("expected a verification exception");
        } catch (Throwable t) {
            DxUtil.checkVerifyException(t);
        }
    }
    public void testVFE5() {
        try {
            Class.forName("dot.junit.opcodes.invoke_interface.d.T_invoke_interface_5");
            fail("expected a verification exception");
        } catch (Throwable t) {
            DxUtil.checkVerifyException(t);
        }
    }
    public void testVFE6() {
        try {
            Class.forName("dot.junit.opcodes.invoke_interface.d.T_invoke_interface_10");
            fail("expected a verification exception");
        } catch (Throwable t) {
            DxUtil.checkVerifyException(t);
        }
    }
    public void testVFE9() {
        try {
            Class.forName("dot.junit.opcodes.invoke_interface.d.T_invoke_interface_9");
            fail("expected a verification exception");
        } catch (Throwable t) {
            DxUtil.checkVerifyException(t);
        }
    }
    public void testVFE10() {
        try {
            Class.forName("dot.junit.opcodes.invoke_interface.d.T_invoke_interface_18");
            fail("expected a verification exception");
        } catch (Throwable t) {
            DxUtil.checkVerifyException(t);
        }
    }
    public void testVFE11() {
        try {
            Class.forName("dot.junit.opcodes.invoke_interface.d.T_invoke_interface_20");
            fail("expected a verification exception");
        } catch (Throwable t) {
            DxUtil.checkVerifyException(t);
        }
    }
    public void testVFE12() {
        try {
            Class.forName("dot.junit.opcodes.invoke_interface.d.T_invoke_interface_21");
            fail("expected a verification exception");
        } catch (Throwable t) {
            DxUtil.checkVerifyException(t);
        }
    }
    public void testVFE13() {
        try {
            Class.forName("dot.junit.opcodes.invoke_interface.d.T_invoke_interface_8");
            fail("expected a verification exception");
        } catch (Throwable t) {
            DxUtil.checkVerifyException(t);
        }
    }
    public void testVFE14() {
        try {
            Class.forName("dot.junit.opcodes.invoke_interface.d.T_invoke_interface_7");
            fail("expected a verification exception");
        } catch (Throwable t) {
            DxUtil.checkVerifyException(t);
        }
    }
    public void testVFE15() {
        try {
            Class.forName("dot.junit.opcodes.invoke_interface.d.T_invoke_interface_16");
            fail("expected a verification exception");
        } catch (Throwable t) {
            DxUtil.checkVerifyException(t);
        }
    }
    public void testVFE21() {
        try {
            Class.forName("dot.junit.opcodes.invoke_interface.d.T_invoke_interface_22");
            fail("expected a verification exception");
        } catch (Throwable t) {
            DxUtil.checkVerifyException(t);
        }
    }
}
