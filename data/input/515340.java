public class Test_invoke_super extends DxTestCase {
    public void testN1() {
        T_invoke_super_1 t = new T_invoke_super_1();
        assertEquals(5, t.run());
    }
    public void testN3() {
        T_invoke_super_7 t = new T_invoke_super_7();
        assertEquals(5, t.run());
    }
    public void testN5() {
        T_invoke_super_14 t = new T_invoke_super_14();
        assertTrue(t.run());
    }
    public void testN6() {
        T_invoke_super_17 t = new T_invoke_super_17();
        assertEquals(5, t.run());
    }
    public void testE1() {
        T_invoke_super_2 t = new T_invoke_super_2();
        try {
            t.run();
            fail("expected NullPointerException");
        } catch (NullPointerException npe) {
        }
    }
    public void testE2() {
        T_invoke_super_4 t = new T_invoke_super_4();
        try {
            t.run();
            fail("expected UnsatisfiedLinkError");
        } catch (UnsatisfiedLinkError ule) {
        }
    }
    public void testE4() {
        T_invoke_super_6 t = new T_invoke_super_6();
        try {
            t.run();
            fail("expected AbstractMethodError");
        } catch (AbstractMethodError iae) {
        }
    }
    public void testVFE1() {
        try {
            Class.forName("dot.junit.opcodes.invoke_super.d.T_invoke_super_8");
            fail("expected a verification exception");
        } catch (Throwable t) {
            DxUtil.checkVerifyException(t);
        }
    }
    public void testVFE3() {
        try {
            Class.forName("dot.junit.opcodes.invoke_super.d.T_invoke_super_10");
            fail("expected a verification exception");
        } catch (Throwable t) {
            DxUtil.checkVerifyException(t);
        }
    }
    public void testVFE4() {
        try {
            Class.forName("dot.junit.opcodes.invoke_super.d.T_invoke_super_11");
            fail("expected a verification exception");
        } catch (Throwable t) {
            DxUtil.checkVerifyException(t);
        }
    }
    public void testVFE5() {
        try {
            Class.forName("dot.junit.opcodes.invoke_super.d.T_invoke_super_12");
            fail("expected a verification exception");
        } catch (Throwable t) {
            DxUtil.checkVerifyException(t);
        }
    }
    public void testVFE6() {
        try {
            Class.forName("dot.junit.opcodes.invoke_super.d.T_invoke_super_16");
            fail("expected a verification exception");
        } catch (Throwable t) {
            DxUtil.checkVerifyException(t);
        }
    }
    public void testVFE8() {
        try {
            Class.forName("dot.junit.opcodes.invoke_super.d.T_invoke_super_22");
            fail("expected a verification exception");
        } catch (Throwable t) {
            DxUtil.checkVerifyException(t);
        }
    }
    public void testVFE9() {
        try {
            Class.forName("dot.junit.opcodes.invoke_super.d.T_invoke_super_23");
            fail("expected a verification exception");
        } catch (Throwable t) {
            DxUtil.checkVerifyException(t);
        }
    }
    public void testVFE10() {
         try {
             Class.forName("dot.junit.opcodes.invoke_super.d.T_invoke_super_5");
             fail("expected a verification exception");
         } catch (Throwable t) {
             DxUtil.checkVerifyException(t);
         }
    }
    public void testVFE12() {
         try {
             Class.forName("dot.junit.opcodes.invoke_super.d.T_invoke_super_15");
             fail("expected a verification exception");
         } catch (Throwable t) {
             DxUtil.checkVerifyException(t);
         }
    }
    public void testVFE13() {
         try {
             Class.forName("dot.junit.opcodes.invoke_super.d.T_invoke_super_18");
             fail("expected a verification exception");
         } catch (Throwable t) {
             DxUtil.checkVerifyException(t);
         }
    }
    public void testVFE14() {
         try {
             Class.forName("dot.junit.opcodes.invoke_super.d.T_invoke_super_20");
             fail("expected a verification exception");
         } catch (Throwable t) {
             DxUtil.checkVerifyException(t);
         }
    }
    public void testVFE15() {
         try {
             Class.forName("dot.junit.opcodes.invoke_super.d.T_invoke_super_19");
             fail("expected a verification exception");
         } catch (Throwable t) {
             DxUtil.checkVerifyException(t);
         }
    }
    public void testVFE16() {
         try {
             Class.forName("dot.junit.opcodes.invoke_super.d.T_invoke_super_13");
             fail("expected a verification exception");
         } catch (Throwable t) {
             DxUtil.checkVerifyException(t);
         }
    }
    public void testVFE17() {
        try {
            Class.forName("dot.junit.opcodes.invoke_super.d.T_invoke_super_9");
            fail("expected a verification exception");
        } catch (Throwable t) {
            DxUtil.checkVerifyException(t);
        }
    }
    public void testVFE18() {
        try {
            Class.forName("dot.junit.opcodes.invoke_super.d.T_invoke_super_24");
            fail("expected a verification exception");
        } catch (Throwable t) {
            DxUtil.checkVerifyException(t);
        }
    }
    public void testVFE19() {
         try {
             Class.forName("dot.junit.opcodes.invoke_super.d.T_invoke_super_25");
             fail("expected a verification exception");
         } catch (Throwable t) {
             DxUtil.checkVerifyException(t);
         }
    }
}
