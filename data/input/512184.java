public class Test_sput_boolean extends DxTestCase {
    public void testN1() {
        T_sput_boolean_1 t = new T_sput_boolean_1();
        assertEquals(false, T_sput_boolean_1.st_i1);
        t.run();
        assertEquals(true, T_sput_boolean_1.st_i1);
    }
    public void testN2() {
        T_sput_boolean_12 t = new T_sput_boolean_12();
        assertEquals(false, T_sput_boolean_12.st_i1);
        t.run();
        assertEquals(true, T_sput_boolean_12.st_i1);
    }
    public void testN4() {
        T_sput_boolean_14 t = new T_sput_boolean_14();
        assertEquals(false, T_sput_boolean_14.getProtectedField());
        t.run();
        assertEquals(true, T_sput_boolean_14.getProtectedField());
    }
    public void testE6() {
        T_sput_boolean_13 t = new T_sput_boolean_13();
        try {
            t.run();
            fail("expected Error");
        } catch (Error e) {
        }
    }
    public void testVFE1() {
        try {
            Class.forName("dot.junit.opcodes.sput_boolean.d.T_sput_boolean_3");
            fail("expected a verification exception");
        } catch (Throwable t) {
            DxUtil.checkVerifyException(t);
        }
    }
    public void testVFE2() {
        try {
            Class.forName("dot.junit.opcodes.sput_boolean.d.T_sput_boolean_4");
            fail("expected a verification exception");
        } catch (Throwable t) {
            DxUtil.checkVerifyException(t);
        }
    }
    public void testVFE5() {
        try {
            Class.forName("dot.junit.opcodes.sput_boolean.d.T_sput_boolean_17");
            fail("expected a verification exception");
        } catch (Throwable t) {
            DxUtil.checkVerifyException(t);
        }
    }
    public void testVFE6() {
        try {
            Class.forName("dot.junit.opcodes.sput_boolean.d.T_sput_boolean_6");
            fail("expected a verification exception");
        } catch (Throwable t) {
            DxUtil.checkVerifyException(t);
        }
    }
    public void testVFE7() {
        try {
            Class.forName("dot.junit.opcodes.sput_boolean.d.T_sput_boolean_18");
            fail("expected a verification exception");
        } catch (Throwable t) {
            DxUtil.checkVerifyException(t);
        }
    }
    public void testVFE8() {
         try {
             Class.forName("dot.junit.opcodes.sput_boolean.d.T_sput_boolean_7");
             fail("expected a verification exception");
         } catch (Throwable t) {
             DxUtil.checkVerifyException(t);
         }
    }
    public void testVFE9() {
        try {
            Class.forName("dot.junit.opcodes.sput_boolean.d.T_sput_boolean_8");
            fail("expected a verification exception");
        } catch (Throwable t) {
            DxUtil.checkVerifyException(t);
        }
    }
    public void testVFE10() {
        try {
            Class.forName("dot.junit.opcodes.sput_boolean.d.T_sput_boolean_9");
            fail("expected a verification exception");
        } catch (Throwable t) {
            DxUtil.checkVerifyException(t);
        }
    }
    public void testVFE11() {
        try {
            Class.forName("dot.junit.opcodes.sput_boolean.d.T_sput_boolean_10");
            fail("expected a verification exception");
        } catch (Throwable t) {
            DxUtil.checkVerifyException(t);
        }
    }
    public void testVFE12() {
        try {
            Class.forName("dot.junit.opcodes.sput_boolean.d.T_sput_boolean_15");
            fail("expected a verification exception");
        } catch (Throwable t) {
            DxUtil.checkVerifyException(t);
        }
    }
    public void testVFE13() {
        try {
            Class.forName("dot.junit.opcodes.sput_boolean.d.T_sput_boolean_2");
            fail("expected a verification exception");
        } catch (Throwable t) {
            DxUtil.checkVerifyException(t);
        }
    }
    public void testVFE14() {
        try {
            Class.forName("dot.junit.opcodes.sput_boolean.d.T_sput_boolean_20");
            fail("expected a verification exception");
        } catch (Throwable t) {
            DxUtil.checkVerifyException(t);
        }
    }
    public void testVFE15() {
        try {
            Class.forName("dot.junit.opcodes.sput_boolean.d.T_sput_boolean_21");
            fail("expected a verification exception");
        } catch (Throwable t) {
            DxUtil.checkVerifyException(t);
        }
    }
    public void testVFE16() {
        try {
            Class.forName("dot.junit.opcodes.sput_boolean.d.T_sput_boolean_22");
            fail("expected a verification exception");
        } catch (Throwable t) {
            DxUtil.checkVerifyException(t);
        }
    }
    public void testVFE17() {
        try {
            Class.forName("dot.junit.opcodes.sput_boolean.d.T_sput_boolean_23");
            fail("expected a verification exception");
        } catch (Throwable t) {
            DxUtil.checkVerifyException(t);
        }
    }
    public void testVFE18() {
        try {
            Class.forName("dot.junit.opcodes.sput_boolean.d.T_sput_boolean_24");
            fail("expected a verification exception");
        } catch (Throwable t) {
            DxUtil.checkVerifyException(t);
        }
    }
    public void testVFE19() {
    	try {
    		Class.forName("dot.junit.opcodes.sput_boolean.d.T_sput_boolean_11");
    	}catch (Throwable t) {
            DxUtil.checkVerifyException(t);
        }
    }
}
