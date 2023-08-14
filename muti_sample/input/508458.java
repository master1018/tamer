public class Test_sput_wide extends DxTestCase {
    public void testN1() {
        T_sput_wide_1 t = new T_sput_wide_1();
        assertEquals(0, T_sput_wide_1.st_i1);
        t.run();
        assertEquals(778899112233l, T_sput_wide_1.st_i1);
    }
    public void testN2() {
        T_sput_wide_5 t = new T_sput_wide_5();
        assertEquals(0.0d, T_sput_wide_5.st_i1);
        t.run();
        assertEquals(0.5d, T_sput_wide_5.st_i1);
    }
    public void testN3() {
        T_sput_wide_12 t = new T_sput_wide_12();
        assertEquals(0, T_sput_wide_12.st_i1);
        t.run();
        assertEquals(77, T_sput_wide_12.st_i1);
    }
    public void testN4() {
        T_sput_wide_14 t = new T_sput_wide_14();
        assertEquals(0, T_sput_wide_14.getProtectedField());
        t.run();
        assertEquals(77, T_sput_wide_14.getProtectedField());
    }
    public void testE6() {
        T_sput_wide_13 t = new T_sput_wide_13();
        try {
            t.run();
            fail("expected Error");
        } catch (Error e) {
        }
    }
    public void testVFE1() {
        try {
            Class.forName("dot.junit.opcodes.sput_wide.d.T_sput_wide_3");
            fail("expected a verification exception");
        } catch (Throwable t) {
            DxUtil.checkVerifyException(t);
        }
    }
    public void testVFE2() {
        try {
            Class.forName("dot.junit.opcodes.sput_wide.d.T_sput_wide_4");
            fail("expected a verification exception");
        } catch (Throwable t) {
            DxUtil.checkVerifyException(t);
        }
    }
    public void testVFE5() {
        try {
            Class.forName("dot.junit.opcodes.sput_wide.d.T_sput_wide_17");
            fail("expected a verification exception");
        } catch (Throwable t) {
            DxUtil.checkVerifyException(t);
        }
    }
    public void testVFE7() {
        try {
            Class.forName("dot.junit.opcodes.sput_wide.d.T_sput_wide_18");
            fail("expected a verification exception");
        } catch (Throwable t) {
            DxUtil.checkVerifyException(t);
        }
    }
    public void testVFE8() {
         try {
             Class.forName("dot.junit.opcodes.sput_wide.d.T_sput_wide_7");
             fail("expected a verification exception");
         } catch (Throwable t) {
             DxUtil.checkVerifyException(t);
         }
    }
    public void testVFE9() {
        try {
            Class.forName("dot.junit.opcodes.sput_wide.d.T_sput_wide_8");
            fail("expected a verification exception");
        } catch (Throwable t) {
            DxUtil.checkVerifyException(t);
        }
    }
    public void testVFE10() {
        try {
            Class.forName("dot.junit.opcodes.sput_wide.d.T_sput_wide_9");
            fail("expected a verification exception");
        } catch (Throwable t) {
            DxUtil.checkVerifyException(t);
        }
    }
    public void testVFE11() {
        try {
            Class.forName("dot.junit.opcodes.sput_wide.d.T_sput_wide_10");
            fail("expected a verification exception");
        } catch (Throwable t) {
            DxUtil.checkVerifyException(t);
        }
    }
    public void testVFE12() {
        try {
            Class.forName("dot.junit.opcodes.sput_wide.d.T_sput_wide_15");
            fail("expected a verification exception");
        } catch (Throwable t) {
            DxUtil.checkVerifyException(t);
        }
    }
    public void testVFE13() {
        try {
            Class.forName("dot.junit.opcodes.sput_wide.d.T_sput_wide_2");
            fail("expected a verification exception");
        } catch (Throwable t) {
            DxUtil.checkVerifyException(t);
        }
    }
    public void testVFE14() {
        try {
            Class.forName("dot.junit.opcodes.sput_wide.d.T_sput_wide_20");
            fail("expected a verification exception");
        } catch (Throwable t) {
            DxUtil.checkVerifyException(t);
        }
    }
    public void testVFE15() {
        try {
            Class.forName("dot.junit.opcodes.sput_wide.d.T_sput_wide_21");
            fail("expected a verification exception");
        } catch (Throwable t) {
            DxUtil.checkVerifyException(t);
        }
    }
    public void testVFE16() {
        try {
            Class.forName("dot.junit.opcodes.sput_wide.d.T_sput_wide_22");
            fail("expected a verification exception");
        } catch (Throwable t) {
            DxUtil.checkVerifyException(t);
        }
    }
    public void testVFE17() {
        try {
            Class.forName("dot.junit.opcodes.sput_wide.d.T_sput_wide_23");
            fail("expected a verification exception");
        } catch (Throwable t) {
            DxUtil.checkVerifyException(t);
        }
    }
    public void testVFE18() {
        try {
            Class.forName("dot.junit.opcodes.sput_wide.d.T_sput_wide_24");
            fail("expected a verification exception");
        } catch (Throwable t) {
            DxUtil.checkVerifyException(t);
        }
    }
    public void testVFE6() {
        try {
            Class.forName("dot.junit.opcodes.sput_wide.d.T_sput_wide_6");
            fail("expected a verification exception");
        } catch (Throwable t) {
            DxUtil.checkVerifyException(t);
        }
    }
    public void testVFE19() {
    	try {
            Class.forName("dot.junit.opcodes.sput_wide.d.T_sput_wide_11");
            fail("expected a verification exception");
        } catch (Throwable t) {
            DxUtil.checkVerifyException(t);
        }
    }
}
