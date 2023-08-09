public class Test_invoke_super_range extends DxTestCase {
    public void testN1() {
        T_invoke_super_range_1 t = new T_invoke_super_range_1();
        assertEquals(5, t.run());
    }
    public void testN3() {
        T_invoke_super_range_7 t = new T_invoke_super_range_7();
        assertEquals(5, t.run());
    }
    public void testN5() {
        T_invoke_super_range_14 t = new T_invoke_super_range_14();
        assertTrue(t.run());
    }
    public void testN6() {
        T_invoke_super_range_17 t = new T_invoke_super_range_17();
        assertEquals(5, t.run());
    }
    public void testE1() {
        T_invoke_super_range_2 t = new T_invoke_super_range_2();
        try {
            t.run();
            fail("expected NullPointerException");
        } catch (NullPointerException npe) {
        }
    }
    public void testE2() {
        T_invoke_super_range_4 t = new T_invoke_super_range_4();
        try {
            t.run();
            fail("expected UnsatisfiedLinkError");
        } catch (UnsatisfiedLinkError ule) {
        }
    }
    public void testE4() {
        T_invoke_super_range_6 t = new T_invoke_super_range_6();
        try {
            t.run();
            fail("expected AbstractMethodError");
        } catch (AbstractMethodError iae) {
        }
    }
    public void testVFE1() {
        try {
            Class.forName("dot.junit.opcodes.invoke_super_range.d.T_invoke_super_range_8");
            fail("expected a verification exception");
        } catch (Throwable t) {
            DxUtil.checkVerifyException(t);
        }
    }
    public void testVFE3() {
        try {
            Class.forName("dot.junit.opcodes.invoke_super_range.d.T_invoke_super_range_10");
            fail("expected a verification exception");
        } catch (Throwable t) {
            DxUtil.checkVerifyException(t);
        }
    }
    public void testVFE4() {
        try {
            Class.forName("dot.junit.opcodes.invoke_super_range.d.T_invoke_super_range_11");
            fail("expected a verification exception");
        } catch (Throwable t) {
            DxUtil.checkVerifyException(t);
        }
    }
    public void testVFE5() {
        try {
            Class.forName("dot.junit.opcodes.invoke_super_range.d.T_invoke_super_range_12");
            fail("expected a verification exception");
        } catch (Throwable t) {
            DxUtil.checkVerifyException(t);
        }
    }
    public void testVFE6() {
        try {
            Class.forName("dot.junit.opcodes.invoke_super_range.d.T_invoke_super_range_16");
            fail("expected a verification exception");
        } catch (Throwable t) {
            DxUtil.checkVerifyException(t);
        }
    }
    public void testVFE8() {
        try {
            Class.forName("dot.junit.opcodes.invoke_super_range.d.T_invoke_super_range_22");
            fail("expected a verification exception");
        } catch (Throwable t) {
            DxUtil.checkVerifyException(t);
        }
    }
    public void testVFE9() {
        try {
            Class.forName("dot.junit.opcodes.invoke_super_range.d.T_invoke_super_range_23");
            fail("expected a verification exception");
        } catch (Throwable t) {
            DxUtil.checkVerifyException(t);
        }
    }
    public void testVFE10() {
         try {
             Class.forName("dot.junit.opcodes.invoke_super_range.d.T_invoke_super_range_5");
             fail("expected a verification exception");
         } catch (Throwable t) {
             DxUtil.checkVerifyException(t);
         }
    }
    public void testVFE12() {
         try {
             Class.forName("dot.junit.opcodes.invoke_super_range.d.T_invoke_super_range_15");
             fail("expected a verification exception");
         } catch (Throwable t) {
             DxUtil.checkVerifyException(t);
         }
    }
    public void testVFE13() {
         try {
             Class.forName("dot.junit.opcodes.invoke_super_range.d.T_invoke_super_range_18");
             fail("expected a verification exception");
         } catch (Throwable t) {
             DxUtil.checkVerifyException(t);
         }
    }
    public void testVFE14() {
         try {
             Class.forName("dot.junit.opcodes.invoke_super_range.d.T_invoke_super_range_20");
             fail("expected a verification exception");
         } catch (Throwable t) {
             DxUtil.checkVerifyException(t);
         }
    }
    public void testVFE15() {
         try {
             Class.forName("dot.junit.opcodes.invoke_super_range.d.T_invoke_super_range_19");
             fail("expected a verification exception");
         } catch (Throwable t) {
             DxUtil.checkVerifyException(t);
         }
    }
    public void testVFE16() {
         try {
             Class.forName("dot.junit.opcodes.invoke_super_range.d.T_invoke_super_range_13");
             fail("expected a verification exception");
         } catch (Throwable t) {
             DxUtil.checkVerifyException(t);
         }
    }
    public void testVFE17() {
        try {
            Class.forName("dot.junit.opcodes.invoke_super_range.d.T_invoke_super_range_9");
            fail("expected a verification exception");
        } catch (Throwable t) {
            DxUtil.checkVerifyException(t);
        }
    }
    public void testVFE18() {
        try {
            Class.forName("dot.junit.opcodes.invoke_super_range.d.T_invoke_super_range_24");
            fail("expected a verification exception");
        } catch (Throwable t) {
            DxUtil.checkVerifyException(t);
        }
    }
    public void testVFE19() {
         try {
             Class.forName("dot.junit.opcodes.invoke_super_range.d.T_invoke_super_range_25");
             fail("expected a verification exception");
         } catch (Throwable t) {
             DxUtil.checkVerifyException(t);
         }
    }
}
