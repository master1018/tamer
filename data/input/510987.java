public class Test_sput_object extends DxTestCase {
    public void testN1() {
        T_sput_object_1 t = new T_sput_object_1();
        assertEquals(null, T_sput_object_1.st_i1);
        t.run();
        assertEquals(t, T_sput_object_1.st_i1);
    }
    public void testN2() {
        T_sput_object_12 t = new T_sput_object_12();
        assertEquals(null, T_sput_object_12.st_i1);
        t.run();
        assertEquals(t, T_sput_object_12.st_i1);
    }
    public void testN4() {
        T_sput_object_14 t = new T_sput_object_14();
        assertEquals(null, T_sput_object_14.getProtectedField());
        t.run();
        assertEquals(t, T_sput_object_14.getProtectedField());
    }
    public void testE6() {
        T_sput_object_13 t = new T_sput_object_13();
        try {
            t.run();
            fail("expected Error");
        } catch (Error e) {
        }
    }
    public void testVFE1() {
        try {
            Class.forName("dot.junit.opcodes.sput_object.d.T_sput_object_3");
            fail("expected a verification exception");
        } catch (Throwable t) {
            DxUtil.checkVerifyException(t);
        }
    }
    public void testVFE2() {
        try {
            Class.forName("dot.junit.opcodes.sput_object.d.T_sput_object_4");
            fail("expected a verification exception");
        } catch (Throwable t) {
            DxUtil.checkVerifyException(t);
        }
    }
    public void testVFE5() {
        try {
            Class.forName("dot.junit.opcodes.sput_object.d.T_sput_object_17");
            fail("expected a verification exception");
        } catch (Throwable t) {
            DxUtil.checkVerifyException(t);
        }
    }
    public void testVFE7() {
        try {
            Class.forName("dot.junit.opcodes.sput_object.d.T_sput_object_18");
            fail("expected a verification exception");
        } catch (Throwable t) {
            DxUtil.checkVerifyException(t);
        }
    }
    public void testVFE8() {
         try {
             Class.forName("dot.junit.opcodes.sput_object.d.T_sput_object_7");
             fail("expected a verification exception");
         } catch (Throwable t) {
             DxUtil.checkVerifyException(t);
         }
    }
    public void testVFE9() {
        try {
            Class.forName("dot.junit.opcodes.sput_object.d.T_sput_object_8");
            fail("expected a verification exception");
        } catch (Throwable t) {
            DxUtil.checkVerifyException(t);
        }
    }
    public void testVFE10() {
        try {
            Class.forName("dot.junit.opcodes.sput_object.d.T_sput_object_9");
            fail("expected a verification exception");
        } catch (Throwable t) {
            DxUtil.checkVerifyException(t);
        }
    }
    public void testVFE11() {
        try {
            Class.forName("dot.junit.opcodes.sput_object.d.T_sput_object_10");
            fail("expected a verification exception");
        } catch (Throwable t) {
            DxUtil.checkVerifyException(t);
        }
    }
    public void testVFE12() {
        try {
            Class.forName("dot.junit.opcodes.sput_object.d.T_sput_object_15");
            fail("expected a verification exception");
        } catch (Throwable t) {
            DxUtil.checkVerifyException(t);
        }
    }
    public void testVFE13() {
        try {
            Class.forName("dot.junit.opcodes.sput_object.d.T_sput_object_2");
            fail("expected a verification exception");
        } catch (Throwable t) {
            DxUtil.checkVerifyException(t);
        }
    }
    public void testVFE14() {
        try {
            Class.forName("dot.junit.opcodes.sput_object.d.T_sput_object_20");
            fail("expected a verification exception");
        } catch (Throwable t) {
            DxUtil.checkVerifyException(t);
        }
    }
    public void testVFE15() {
        try {
            Class.forName("dot.junit.opcodes.sput_object.d.T_sput_object_21");
            fail("expected a verification exception");
        } catch (Throwable t) {
            DxUtil.checkVerifyException(t);
        }
    }
    public void testVFE16() {
        try {
            Class.forName("dot.junit.opcodes.sput_object.d.T_sput_object_22");
            fail("expected a verification exception");
        } catch (Throwable t) {
            DxUtil.checkVerifyException(t);
        }
    }
    public void testVFE17() {
        try {
            Class.forName("dot.junit.opcodes.sput_object.d.T_sput_object_23");
            fail("expected a verification exception");
        } catch (Throwable t) {
            DxUtil.checkVerifyException(t);
        }
    }
    public void testVFE18() {
        try {
            Class.forName("dot.junit.opcodes.sput_object.d.T_sput_object_24");
            fail("expected a verification exception");
        } catch (Throwable t) {
            DxUtil.checkVerifyException(t);
        }
    }
    public void testVFE6() {
        try {
            Class.forName("dot.junit.opcodes.sput_object.d.T_sput_object_6");
            fail("expected a verification exception");
        } catch (Throwable t) {
            DxUtil.checkVerifyException(t);
        }
    }
    public void testVFE19() {
    	try {
            Class.forName("dot.junit.opcodes.sput_object.d.T_sput_object_11");
            fail("expected a verification exception");
        } catch (Throwable t) {
            DxUtil.checkVerifyException(t);
        }
    }
}
