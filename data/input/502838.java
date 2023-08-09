public class Test_invoke_static_range extends DxTestCase {
    public void testN1() {
        T_invoke_static_range_1 t = new T_invoke_static_range_1();
        assertEquals(1234567, t.run());
    }
    public void testN2() {
        T_invoke_static_range_2 t = new T_invoke_static_range_2();
        assertEquals(777, t.run());
    }
    public void testN3() {
        assertEquals(1, T_invoke_static_range_4.run());
    }
    public void testN5() {
        T_invoke_static_range_15 t = new T_invoke_static_range_15();
        assertTrue(t.run());
    }
    public void testN6() {
        T_invoke_static_range_18 t = new T_invoke_static_range_18();
        assertEquals(888, t.run());
    }
    public void testE2() {
        T_invoke_static_range_6 t = new T_invoke_static_range_6();
        try {
            t.run();
            fail("expected UnsatisfiedLinkError");
        } catch (UnsatisfiedLinkError ule) {
        }
    }
    public void testE7() {
        T_invoke_static_range_14 t = new T_invoke_static_range_14();
        try {
            t.run();
            fail("expected Error");
        } catch (Error e) {
        }
    }
    public void testVFE1() {
        try {
            Class.forName("dot.junit.opcodes.invoke_static_range.d.T_invoke_static_range_3");
            fail("expected a verification exception");
        } catch (Throwable t) {
            DxUtil.checkVerifyException(t);
        }
    }
    public void testVFE3() {
        try {
            Class.forName("dot.junit.opcodes.invoke_static_range.d.T_invoke_static_range_10");
            fail("expected a verification exception");
        } catch (Throwable t) {
            DxUtil.checkVerifyException(t);
        }
    }
    public void testVFE4() {
        try {
            Class.forName("dot.junit.opcodes.invoke_static_range.d.T_invoke_static_range_11");
            fail("expected a verification exception");
        } catch (Throwable t) {
            DxUtil.checkVerifyException(t);
        }
    }
    public void testVFE5() {
        try {
            Class.forName("dot.junit.opcodes.invoke_static_range.d.T_invoke_static_range_19");
            fail("expected a verification exception");
        } catch (Throwable t) {
            DxUtil.checkVerifyException(t);
        }
    }
    public void testVFE6() {
        try {
            Class.forName("dot.junit.opcodes.invoke_static_range.d.T_invoke_static_range_20");
            fail("expected a verification exception");
        } catch (Throwable t) {
            DxUtil.checkVerifyException(t);
        }
    }
    public void testVFE7() {
         try {
             Class.forName("dot.junit.opcodes.invoke_static_range.d.T_invoke_static_range_5");
             fail("expected a verification exception");
         } catch (Throwable t) {
             DxUtil.checkVerifyException(t);
         }
    }
    public void testVFE8() {
        try {
            Class.forName("dot.junit.opcodes.invoke_static_range.d.T_invoke_static_range_7");
            fail("expected a verification exception");
        } catch (Throwable t) {
            DxUtil.checkVerifyException(t);
        }
    }
    public void testVFE9() {
        try {
            Class.forName("dot.junit.opcodes.invoke_static_range.d.T_invoke_static_range_8");
            fail("expected a verification exception");
        } catch (Throwable t) {
            DxUtil.checkVerifyException(t);
        }
    }
    public void testVFE10() {
        try {
            Class.forName("dot.junit.opcodes.invoke_static_range.d.T_invoke_static_range_13");
            fail("expected a verification exception");
        } catch (Throwable t) {
            DxUtil.checkVerifyException(t);
        }
    }
    public void testVFE12() {
        try {
            Class.forName("dot.junit.opcodes.invoke_static_range.d.T_invoke_static_range_17");
            fail("expected a verification exception");
        } catch (Throwable t) {
            DxUtil.checkVerifyException(t);
        }
    }
    public void testVFE13() {
        try {
            Class.forName("dot.junit.opcodes.invoke_static_range.d.T_invoke_static_range_16");
            fail("expected a verification exception");
        } catch (Throwable t) {
            DxUtil.checkVerifyException(t);
        }
    }
    public void testVFE18() {
        try {
            Class.forName("dot.junit.opcodes.invoke_static_range.d.T_invoke_static_range_24");
            fail("expected a verification exception");
        } catch (Throwable t) {
            DxUtil.checkVerifyException(t);
        }
    }
}
