public class Test_invoke_virtual extends DxTestCase {
    public void testN1() {
        T_invoke_virtual_1 t = new T_invoke_virtual_1();
        int a = 1;
        String sa = "a" + a;
        String sb = "a1";
        assertTrue(t.run(sa, sb));
        assertFalse(t.run(t, sa));
        assertFalse(t.run(sb, t));
    }
    public void testN3() {
        T_invoke_virtual_7 t = new T_invoke_virtual_7();
        assertEquals(5, t.run());
    }
    public void testN5() {
        T_invoke_virtual_14 t = new T_invoke_virtual_14();
        assertTrue(t.run());
    }
    public void testN6() {
        T_invoke_virtual_17 t = new T_invoke_virtual_17();
        assertEquals(5, t.run());
    }
    public void testE1() {
        T_invoke_virtual_1 t = new T_invoke_virtual_1();
        String s = "s";
        try {
            t.run(null, s);
            fail("expected NullPointerException");
        } catch (NullPointerException npe) {
        }
    }
    public void testE2() {
        T_invoke_virtual_4 t = new T_invoke_virtual_4();
        try {
            t.run();
            fail("expected UnsatisfiedLinkError");
        } catch (UnsatisfiedLinkError ule) {
        }
    }
    public void testE4() {
        T_invoke_virtual_6 t = new T_invoke_virtual_6();
        try {
            t.run();
            fail("expected AbstractMethodError");
        } catch (AbstractMethodError iae) {
        }
    }
    public void testVFE1() {
        try {
            Class.forName("dot.junit.opcodes.invoke_virtual.d.T_invoke_virtual_8");
            fail("expected a verification exception");
        } catch (Throwable t) {
            DxUtil.checkVerifyException(t);
        }
    }
    public void testVFE3() {
        try {
            Class.forName("dot.junit.opcodes.invoke_virtual.d.T_invoke_virtual_10");
            fail("expected a verification exception");
        } catch (Throwable t) {
            DxUtil.checkVerifyException(t);
        }
    }
    public void testVFE4() {
        try {
            Class.forName("dot.junit.opcodes.invoke_virtual.d.T_invoke_virtual_11");
            fail("expected a verification exception");
        } catch (Throwable t) {
            DxUtil.checkVerifyException(t);
        }
    }
    public void testVFE5() {
        try {
            Class.forName("dot.junit.opcodes.invoke_virtual.d.T_invoke_virtual_12");
            fail("expected a verification exception");
        } catch (Throwable t) {
            DxUtil.checkVerifyException(t);
        }
    }
    public void testVFE6() {
        try {
            Class.forName("dot.junit.opcodes.invoke_virtual.d.T_invoke_virtual_16");
            fail("expected a verification exception");
        } catch (Throwable t) {
            DxUtil.checkVerifyException(t);
        }
    }
    public void testVFE8() {
        try {
            Class.forName("dot.junit.opcodes.invoke_virtual.d.T_invoke_virtual_22");
            fail("expected a verification exception");
        } catch (Throwable t) {
            DxUtil.checkVerifyException(t);
        }
    }
    public void testVFE9() {
        try {
            Class.forName("dot.junit.opcodes.invoke_virtual.d.T_invoke_virtual_23");
            fail("expected a verification exception");
        } catch (Throwable t) {
            DxUtil.checkVerifyException(t);
        }
    }
    public void testVFE10() {
         try {
             Class.forName("dot.junit.opcodes.invoke_virtual.d.T_invoke_virtual_5");
             fail("expected a verification exception");
         } catch (Throwable t) {
             DxUtil.checkVerifyException(t);
         }
    }
    public void testVFE12() {
         try {
             Class.forName("dot.junit.opcodes.invoke_virtual.d.T_invoke_virtual_15");
             fail("expected a verification exception");
         } catch (Throwable t) {
             DxUtil.checkVerifyException(t);
         }
    }
    public void testVFE13() {
         try {
             Class.forName("dot.junit.opcodes.invoke_virtual.d.T_invoke_virtual_18");
             fail("expected a verification exception");
         } catch (Throwable t) {
             DxUtil.checkVerifyException(t);
         }
    }
    public void testVFE14() {
         try {
             Class.forName("dot.junit.opcodes.invoke_virtual.d.T_invoke_virtual_20");
             fail("expected a verification exception");
         } catch (Throwable t) {
             DxUtil.checkVerifyException(t);
         }
    }
    public void testVFE15() {
         try {
             Class.forName("dot.junit.opcodes.invoke_virtual.d.T_invoke_virtual_19");
             fail("expected a verification exception");
         } catch (Throwable t) {
             DxUtil.checkVerifyException(t);
         }
    }
    public void testVFE16() {
         try {
             Class.forName("dot.junit.opcodes.invoke_virtual.d.T_invoke_virtual_13");
             fail("expected a verification exception");
         } catch (Throwable t) {
             DxUtil.checkVerifyException(t);
         }
    }
    public void testVFE17() {
        try {
            Class.forName("dot.junit.opcodes.invoke_virtual.d.T_invoke_virtual_9");
            fail("expected a verification exception");
        } catch (Throwable t) {
            DxUtil.checkVerifyException(t);
        }
    }
    public void testVFE18() {
        try {
            Class.forName("dot.junit.opcodes.invoke_virtual.d.T_invoke_virtual_24");
            fail("expected a verification exception");
        } catch (Throwable t) {
            DxUtil.checkVerifyException(t);
        }
    }
    public void testVFE19() {
        try {
            Class.forName("dot.junit.opcodes.invoke_virtual.d.T_invoke_virtual_25");
            fail("expected a verification exception");
        } catch (Throwable t) {
            DxUtil.checkVerifyException(t);
        }
    }
}
