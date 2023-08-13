public class Test_iput extends DxTestCase {
    public void testN1() {
        T_iput_1 t = new T_iput_1();
        assertEquals(0, t.st_i1);
        t.run();
        assertEquals(1000000, t.st_i1);
    }
    public void testN2() {
        T_iput_19 t = new T_iput_19();
        assertEquals(0.0f, t.st_f1);
        t.run();
        assertEquals(3.14f, t.st_f1);
    }
    public void testN3() {
        T_iput_12 t = new T_iput_12();
        assertEquals(0, t.st_i1);
        t.run();
        assertEquals(1000000, t.st_i1);
    }
    public void testN4() {
        T_iput_14 t = new T_iput_14();
        assertEquals(0, t.getProtectedField());
        t.run();
        assertEquals(1000000, t.getProtectedField());
    }
    public void testN6() {
        T_iput_5 t = new  T_iput_5();
        try {
            t.run(3.14f);
        } catch (Throwable e) {
        }
    }
    public void testE2() {
        T_iput_13 t = new T_iput_13();
        try {
            t.run();
            fail("expected NullPointerException");
        } catch (NullPointerException e) {
        }
    }
    public void testVFE1() {
        try {
            Class.forName("dot.junit.opcodes.iput.d.T_iput_3");
            fail("expected a verification exception");
        } catch (Throwable t) {
            DxUtil.checkVerifyException(t);
        }
    }
    public void testVFE2() {
        try {
            Class.forName("dot.junit.opcodes.iput.d.T_iput_4");
            fail("expected a verification exception");
        } catch (Throwable t) {
            DxUtil.checkVerifyException(t);
        }
    }
    public void testVFE5() {
        try {
            Class.forName("dot.junit.opcodes.iput.d.T_iput_17");
            fail("expected a verification exception");
        } catch (Throwable t) {
            DxUtil.checkVerifyException(t);
        }
    }
    public void testVFE7() {
        try {
            Class.forName("dot.junit.opcodes.iput.d.T_iput_18");
            fail("expected a verification exception");
        } catch (Throwable t) {
            DxUtil.checkVerifyException(t);
        }
    }
    public void testVFE8() {
         try {
             Class.forName("dot.junit.opcodes.iput.d.T_iput_7");
             fail("expected a verification exception");
         } catch (Throwable t) {
             DxUtil.checkVerifyException(t);
         }
    }
    public void testVFE9() {
        try {
            Class.forName("dot.junit.opcodes.iput.d.T_iput_8");
            fail("expected a verification exception");
        } catch (Throwable t) {
            DxUtil.checkVerifyException(t);
        }
    }
    public void testVFE10() {
        try {
            Class.forName("dot.junit.opcodes.iput.d.T_iput_9");
            fail("expected a verification exception");
        } catch (Throwable t) {
            DxUtil.checkVerifyException(t);
        }
    }
    public void testVFE11() {
        try {
            Class.forName("dot.junit.opcodes.iput.d.T_iput_10");
            fail("expected a verification exception");
        } catch (Throwable t) {
            DxUtil.checkVerifyException(t);
        }
    }
    public void testVFE12() {
        try {
            Class.forName("dot.junit.opcodes.iput.d.T_iput_15");
            fail("expected a verification exception");
        } catch (Throwable t) {
            DxUtil.checkVerifyException(t);
        }
    }
    public void testVFE13() {
        try {
            Class.forName("dot.junit.opcodes.iput.d.T_iput_2");
            fail("expected a verification exception");
        } catch (Throwable t) {
            DxUtil.checkVerifyException(t);
        }
    }
    public void testVFE14() {
        try {
            Class.forName("dot.junit.opcodes.iput.d.T_iput_20");
            fail("expected a verification exception");
        } catch (Throwable t) {
            DxUtil.checkVerifyException(t);
        }
    }
    public void testVFE15() {
        try {
            Class.forName("dot.junit.opcodes.iput.d.T_iput_21");
            fail("expected a verification exception");
        } catch (Throwable t) {
            DxUtil.checkVerifyException(t);
        }
    }
    public void testVFE16() {
        try {
            Class.forName("dot.junit.opcodes.iput.d.T_iput_22");
            fail("expected a verification exception");
        } catch (Throwable t) {
            DxUtil.checkVerifyException(t);
        }
    }
    public void testVFE17() {
        try {
            Class.forName("dot.junit.opcodes.iput.d.T_iput_23");
            fail("expected a verification exception");
        } catch (Throwable t) {
            DxUtil.checkVerifyException(t);
        }
    }
    public void testVFE18() {
        try {
            Class.forName("dot.junit.opcodes.iput.d.T_iput_24");
            fail("expected a verification exception");
        } catch (Throwable t) {
            DxUtil.checkVerifyException(t);
        }
    }
    public void testVFE30() {
        try {
            Class.forName("dot.junit.opcodes.iput.d.T_iput_30");
            fail("expected a verification exception");
        } catch (Throwable t) {
            DxUtil.checkVerifyException(t);
        }
    }
    public void testE5() {
    	try {
            Class.forName("dot.junit.opcodes.iput.d.T_iput_11");
            fail("expected a verification exception");
        } catch (Throwable t) {
            DxUtil.checkVerifyException(t);
        }
    }
}
