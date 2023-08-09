public class Test_sget extends DxTestCase {
    public void testN1() {
        T_sget_1 t = new T_sget_1();
        assertEquals(5, t.run());
    }
    public void testN2() {
        T_sget_2 t = new T_sget_2();
        assertEquals(123f, t.run());
    }
    public void testN3() {
        T_sget_11 t = new T_sget_11();
        assertEquals(10, t.run());
    }
    public void testE1() {
        T_sget_5 t = new T_sget_5();
        try {
            t.run();
            fail("expected IncompatibleClassChangeError");
        } catch (IncompatibleClassChangeError e) {
        }
    }
    public void testE6() {
        T_sget_9 t = new T_sget_9();
        try {
            t.run();
            fail("expected Error");
        } catch (Error e) {
        }
    }
    public void testVFE1() {
        try {
            Class.forName("dot.junit.opcodes.sget.d.T_sget_4");
            fail("expected a verification exception");
        } catch (Throwable t) {
            DxUtil.checkVerifyException(t);
        }
    }
    public void testVFE2() {
        try {
            Class.forName("dot.junit.opcodes.sget.d.T_sget_3");
            fail("expected a verification exception");
        } catch (Throwable t) {
            DxUtil.checkVerifyException(t);
        }
    }
    public void testVFE3() {
        try {
            Class.forName("dot.junit.opcodes.sget.d.T_sget_13");
            fail("expected a verification exception");
        } catch (Throwable t) {
            DxUtil.checkVerifyException(t);
        }
    }
    public void testVFE4() {
        try {
            Class.forName("dot.junit.opcodes.sget.d.T_sget_6");
            fail("expected a verification exception");
        } catch (Throwable t) {
            DxUtil.checkVerifyException(t);
        }
    }
    public void testVFE5() {
        try {
            Class.forName("dot.junit.opcodes.sget.d.T_sget_7");
            fail("expected a verification exception");
        } catch (Throwable t) {
            DxUtil.checkVerifyException(t);
        }
    }
    public void testVFE6() {
        try {
            Class.forName("dot.junit.opcodes.sget.d.T_sget_8");
            fail("expected a verification exception");
        } catch (Throwable t) {
            DxUtil.checkVerifyException(t);
        }
    }
    public void testVFE7() {
        try {
            Class.forName("dot.junit.opcodes.sget.d.T_sget_12");
            fail("expected a verification exception");
        } catch (Throwable t) {
            DxUtil.checkVerifyException(t);
        }
    }
    public void testVFE8() {
        try {
            Class.forName("dot.junit.opcodes.sget.d.T_sget_14");
            fail("expected a verification exception");
        } catch (Throwable t) {
            DxUtil.checkVerifyException(t);
        }
    }
    public void testVFE9() {
        try {
            Class.forName("dot.junit.opcodes.sget.d.T_sget_15");
            fail("expected a verification exception");
        } catch (Throwable t) {
            DxUtil.checkVerifyException(t);
        }
    }
    public void testVFE10() {
        try {
            Class.forName("dot.junit.opcodes.sget.d.T_sget_16");
            fail("expected a verification exception");
        } catch (Throwable t) {
            DxUtil.checkVerifyException(t);
        }
    }
    public void testVFE11() {
        try {
            Class.forName("dot.junit.opcodes.sget.d.T_sget_17");
            fail("expected a verification exception");
        } catch (Throwable t) {
            DxUtil.checkVerifyException(t);
        }
    }
    public void testVFE12() {
        try {
            Class.forName("dot.junit.opcodes.sget.d.T_sget_18");
            fail("expected a verification exception");
        } catch (Throwable t) {
            DxUtil.checkVerifyException(t);
        }
    }    
    public void testVFE13() {
        try {
            Class.forName("dot.junit.opcodes.sget.d.T_sget_19");
            fail("expected a verification exception");
        } catch (Throwable t) {
            DxUtil.checkVerifyException(t);
        }
    } 
    public void testVFE14() {
        try {
            Class.forName("dot.junit.opcodes.sget.d.T_sget_20");
            fail("expected a verification exception");
        } catch (Throwable t) {
            DxUtil.checkVerifyException(t);
        }
    } 
}
