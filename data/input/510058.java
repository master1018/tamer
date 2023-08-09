public class Test_invoke_direct_range extends DxTestCase {
    public void testN2() {
        T_invoke_direct_range_2 t = new T_invoke_direct_range_2();
        assertEquals(345, t.run());
    }
    public void testN7() {
        T_invoke_direct_range_21 t = new T_invoke_direct_range_21();
        assertEquals(1, t.run());
    }
    public void testE3() {
        T_invoke_direct_range_8 t = new T_invoke_direct_range_8();
        try {
            assertEquals(5, t.run());
            fail("expected NullPointerException");
        } catch (NullPointerException e) {
        }
    }
    public void testE5() {
        T_invoke_direct_range_9 t = new T_invoke_direct_range_9();
        try {
            assertEquals(5, t.run());
            fail("expected UnsatisfiedLinkError");
        } catch (UnsatisfiedLinkError e) {
        }
    }   
    public void testVFE1() {
        try {
            Class.forName("dot.junit.opcodes.invoke_direct_range.d.T_invoke_direct_range_3");
            fail("expected a verification exception");
        } catch (Throwable t) {
            DxUtil.checkVerifyException(t);
        }
    }
    public void testVFE3() {
        try {
            Class.forName("dot.junit.opcodes.invoke_direct_range.d.T_invoke_direct_range_4");
            fail("expected a verification exception");
        } catch (Throwable t) {
            DxUtil.checkVerifyException(t);
        }
    }
    public void testVFE4() {
        try {
            Class.forName("dot.junit.opcodes.invoke_direct_range.d.T_invoke_direct_range_6");
            fail("expected a verification exception");
        } catch (Throwable t) {
            DxUtil.checkVerifyException(t);
        }
    }
    public void testVFE5() {
        try {
            Class.forName("dot.junit.opcodes.invoke_direct_range.d.T_invoke_direct_range_5");
            fail("expected a verification exception");
        } catch (Throwable t) {
            DxUtil.checkVerifyException(t);
        }
    }
    public void testVFE6() {
        try {
            Class.forName("dot.junit.opcodes.invoke_direct_range.d.T_invoke_direct_range_10");
            fail("expected a verification exception");
        } catch (Throwable t) {
            DxUtil.checkVerifyException(t);
        }
    }
    public void testVFE8() {
        try {
            Class.forName("dot.junit.opcodes.invoke_direct_range.d.T_invoke_direct_range_14");
            fail("expected a verification exception");
        } catch (Throwable t) {
            DxUtil.checkVerifyException(t);
        }
    }
    public void testVFE9() {
        try {
            Class.forName("dot.junit.opcodes.invoke_direct_range.d.T_invoke_direct_range_24");
            fail("expected a verification exception");
        } catch (Throwable t) {
            DxUtil.checkVerifyException(t);
        }
    }
    public void testVFE10() {
        try {
            Class.forName("dot.junit.opcodes.invoke_direct_range.d.T_invoke_direct_range_25");
            fail("expected a verification exception");
        } catch (Throwable t) {
            DxUtil.checkVerifyException(t);
        }
    }
    public void testVFE11() {
        try {
            Class.forName("dot.junit.opcodes.invoke_direct_range.d.T_invoke_direct_range_1");
            fail("expected a verification exception");
        } catch (Throwable t) {
            DxUtil.checkVerifyException(t);
        }
    }
    public void testVFE12() {
        try {
            Class.forName("dot.junit.opcodes.invoke_direct_range.d.T_invoke_direct_range_15");
            fail("expected a verification exception");
        } catch (Throwable t) {
            DxUtil.checkVerifyException(t);
        }
    }
    public void testVFE13() {
        try {
            Class.forName("dot.junit.opcodes.invoke_direct_range.d.T_invoke_direct_range_7");
            fail("expected a verification exception");
        } catch (Throwable t) {
            DxUtil.checkVerifyException(t);
        }
    }
    public void testVFE14() {
        try {
            Class.forName("dot.junit.opcodes.invoke_direct_range.d.T_invoke_direct_range_16");
            fail("expected a verification exception");
        } catch (Throwable t) {
            DxUtil.checkVerifyException(t);
        }
    }
    public void testVFE15() {
        try {
            Class.forName("dot.junit.opcodes.invoke_direct_range.d.T_invoke_direct_range_11");
            fail("expected a verification exception");
        } catch (Throwable t) {
            DxUtil.checkVerifyException(t);
        }
    }
    public void testVFE16() {
        try {
            Class.forName("dot.junit.opcodes.invoke_direct_range.d.T_invoke_direct_range_12");
            fail("expected a verification exception");
        } catch (Throwable t) {
            DxUtil.checkVerifyException(t);
        }
    }
    public void testVFE17() {
        try {
            Class.forName("dot.junit.opcodes.invoke_direct_range.d.T_invoke_direct_range_13");
            fail("expected a verification exception");
        } catch (Throwable t) {
            DxUtil.checkVerifyException(t);
        }
    }
    public void testVFE18() {
        try {
            Class.forName("dot.junit.opcodes.invoke_direct_range.d.T_invoke_direct_range_17");
            fail("expected a verification exception");
        } catch (Throwable t) {
            DxUtil.checkVerifyException(t);
        }
    }
    public void testVFE19() {
        try {
            Class.forName("dot.junit.opcodes.invoke_direct_range.d.T_invoke_direct_range_18");
            fail("expected a verification exception");
        } catch (Throwable t) {
            DxUtil.checkVerifyException(t);
        }
    }
    public void testVFE20() {
        try {
            Class.forName("dot.junit.opcodes.invoke_direct_range.d.T_invoke_direct_range_26");
            fail("expected a verification exception");
        } catch (Throwable t) {
            DxUtil.checkVerifyException(t);
        }
    }
    public void testVFE21() {
        try {
            Class.forName("dot.junit.opcodes.invoke_direct_range.d.T_invoke_direct_range_27");
            fail("expected a verification exception");
        } catch (Throwable t) {
            DxUtil.checkVerifyException(t);
        }
    }
}
