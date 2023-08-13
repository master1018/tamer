public class Test_invoke_interface_range extends DxTestCase {
    public void testN1() {
        T_invoke_interface_range_1 t = new T_invoke_interface_range_1();
        assertEquals(0, t.run("aa", "aa"));
        assertEquals(-1, t.run("aa", "bb"));
        assertEquals(1, t.run("bb", "aa"));
    }
    public void testN2() {
        T_invoke_interface_range_14 t = new T_invoke_interface_range_14();
        ITestImpl impl = new ITestImpl();
        assertEquals(1, t.run(impl));
    }
    public void testE3() {
        try {
            new T_invoke_interface_range_3(null);
            fail("expected NullPointerException");
        } catch (NullPointerException npe) {
        }
    }
    public void testE4() {
        T_invoke_interface_range_11 t = new T_invoke_interface_range_11();
        try {
            t.run();
            fail("expected IncompatibleClassChangeError");
        } catch (IncompatibleClassChangeError e) {
        }
    }
    public void testE5() {
        T_invoke_interface_range_12 t = new T_invoke_interface_range_12();
        ITestImpl impl = new ITestImpl();
        try {
            t.run(impl);
            fail("expected UnsatisfiedLinkError");
        } catch (UnsatisfiedLinkError e) {
        }
    }
    public void testE6() {
        T_invoke_interface_range_13 t = new T_invoke_interface_range_13();
        try {
            t.run();
            fail("expected AbstractMethodError");
        } catch (AbstractMethodError e) {
        }
    }
    public void testE8() {
        try {
            T_invoke_interface_range_17 t = new T_invoke_interface_range_17();
            t.run();
            fail("expected a verification exception");
        } catch (IllegalAccessError e) {
        } catch (VerifyError e) {
        }
    }
    public void testVFE1() {
        try {
            Class.forName("dot.junit.opcodes.invoke_interface_range.d.T_invoke_interface_range_2");
            fail("expected a verification exception");
        } catch (Throwable t) {
            DxUtil.checkVerifyException(t);
        }
    }
    public void testVFE2() {
        try {
            Class.forName("dot.junit.opcodes.invoke_interface_range.d.T_invoke_interface_range_4");
            fail("expected a verification exception");
        } catch (Throwable t) {
            DxUtil.checkVerifyException(t);
        }
    }
    public void testVFE5() {
        try {
            Class.forName("dot.junit.opcodes.invoke_interface_range.d.T_invoke_interface_range_5");
            fail("expected a verification exception");
        } catch (Throwable t) {
            DxUtil.checkVerifyException(t);
        }
    }
    public void testVFE6() {
        try {
            Class.forName("dot.junit.opcodes.invoke_interface_range.d.T_invoke_interface_range_10");
            fail("expected a verification exception");
        } catch (Throwable t) {
            DxUtil.checkVerifyException(t);
        }
    }
    public void testVFE9() {
        try {
            Class.forName("dot.junit.opcodes.invoke_interface_range.d.T_invoke_interface_range_9");
            fail("expected a verification exception");
        } catch (Throwable t) {
            DxUtil.checkVerifyException(t);
        }
    }
    public void testVFE10() {
        try {
            Class.forName("dot.junit.opcodes.invoke_interface_range.d.T_invoke_interface_range_18");
            fail("expected a verification exception");
        } catch (Throwable t) {
            DxUtil.checkVerifyException(t);
        }
    }
    public void testVFE11() {
        try {
            Class.forName("dot.junit.opcodes.invoke_interface_range.d.T_invoke_interface_range_20");
            fail("expected a verification exception");
        } catch (Throwable t) {
            DxUtil.checkVerifyException(t);
        }
    }
    public void testVFE12() {
        try {
            Class.forName("dot.junit.opcodes.invoke_interface_range.d.T_invoke_interface_range_21");
            fail("expected a verification exception");
        } catch (Throwable t) {
            DxUtil.checkVerifyException(t);
        }
    }
    public void testVFE13() {
        try {
            Class.forName("dot.junit.opcodes.invoke_interface_range.d.T_invoke_interface_range_8");
            fail("expected a verification exception");
        } catch (Throwable t) {
            DxUtil.checkVerifyException(t);
        }
    }
    public void testVFE14() {
        try {
            Class.forName("dot.junit.opcodes.invoke_interface_range.d.T_invoke_interface_range_7");
            fail("expected a verification exception");
        } catch (Throwable t) {
            DxUtil.checkVerifyException(t);
        }
    }
    public void testVFE15() {
        try {
            Class.forName("dot.junit.opcodes.invoke_interface_range.d.T_invoke_interface_range_16");
            fail("expected a verification exception");
        } catch (Throwable t) {
            DxUtil.checkVerifyException(t);
        }
    }
    public void testVFE21() {
        try {
            Class.forName("dot.junit.opcodes.invoke_interface_range.d.T_invoke_interface_range_22");
            fail("expected a verification exception");
        } catch (Throwable t) {
            DxUtil.checkVerifyException(t);
        }
    }
}
