public class Test_iget_boolean extends DxTestCase {
    public void testN1() {
        T_iget_boolean_1 t = new T_iget_boolean_1();
        assertEquals(true, t.run());
    }
    public void testN3() {
        T_iget_boolean_11 t = new T_iget_boolean_11();
        assertEquals(true, t.run());
    }
    public void testE2() {
        T_iget_boolean_9 t = new T_iget_boolean_9();
        try {
            t.run();
            fail("expected NullPointerException");
        } catch (NullPointerException e) {
        }
    }
    public void testVFE1() {
        try {
            Class.forName("dot.junit.opcodes.iget_boolean.d.T_iget_boolean_4");
            fail("expected a verification exception");
        } catch (Throwable t) {
            DxUtil.checkVerifyException(t);
        }
    }
    public void testVFE2() {
        try {
            Class.forName("dot.junit.opcodes.iget_boolean.d.T_iget_boolean_3");
            fail("expected a verification exception");
        } catch (Throwable t) {
            DxUtil.checkVerifyException(t);
        }
    }
    public void testVFE3() {
        try {
            Class.forName("dot.junit.opcodes.iget_boolean.d.T_iget_boolean_13");
            fail("expected a verification exception");
        } catch (Throwable t) {
            DxUtil.checkVerifyException(t);
        }
    }
    public void testVFE4() {
        try {
            Class.forName("dot.junit.opcodes.iget_boolean.d.T_iget_boolean_6");
            fail("expected a verification exception");
        } catch (Throwable t) {
            DxUtil.checkVerifyException(t);
        }
    }
    public void testVFE5() {
        try {
            Class.forName("dot.junit.opcodes.iget_boolean.d.T_iget_boolean_7");
            fail("expected a verification exception");
        } catch (Throwable t) {
            DxUtil.checkVerifyException(t);
        }
    }
    public void testVFE6() {
        try {
            Class.forName("dot.junit.opcodes.iget_boolean.d.T_iget_boolean_8");
            fail("expected a verification exception");
        } catch (Throwable t) {
            DxUtil.checkVerifyException(t);
        }
    }
    public void testVFE7() {
        try {
            Class.forName("dot.junit.opcodes.iget_boolean.d.T_iget_boolean_12");
            fail("expected a verification exception");
        } catch (Throwable t) {
            DxUtil.checkVerifyException(t);
        }
    }
    public void testVFE8() {
        try {
            Class.forName("dot.junit.opcodes.iget_boolean.d.T_iget_boolean_14");
            fail("expected a verification exception");
        } catch (Throwable t) {
            DxUtil.checkVerifyException(t);
        }
    }
    public void testVFE9() {
        try {
            Class.forName("dot.junit.opcodes.iget_boolean.d.T_iget_boolean_15");
            fail("expected a verification exception");
        } catch (Throwable t) {
            DxUtil.checkVerifyException(t);
        }
    }
    public void testVFE10() {
        try {
            Class.forName("dot.junit.opcodes.iget_boolean.d.T_iget_boolean_16");
            fail("expected a verification exception");
        } catch (Throwable t) {
            DxUtil.checkVerifyException(t);
        }
    }
    public void testVFE11() {
        try {
            Class.forName("dot.junit.opcodes.iget_boolean.d.T_iget_boolean_17");
            fail("expected a verification exception");
        } catch (Throwable t) {
            DxUtil.checkVerifyException(t);
        }
    }
    public void testVFE12() {
        try {
            Class.forName("dot.junit.opcodes.iget_boolean.d.T_iget_boolean_18");
            fail("expected a verification exception");
        } catch (Throwable t) {
            DxUtil.checkVerifyException(t);
        }
    }    
    public void testVFE13() {
        try {
            Class.forName("dot.junit.opcodes.iget_boolean.d.T_iget_boolean_19");
            fail("expected a verification exception");
        } catch (Throwable t) {
            DxUtil.checkVerifyException(t);
        }
    } 
    public void testVFE14() {
        try {
            Class.forName("dot.junit.opcodes.iget_boolean.d.T_iget_boolean_20");
            fail("expected a verification exception");
        } catch (Throwable t) {
            DxUtil.checkVerifyException(t);
        }
    }
    public void testVFE15() {
        try {
            Class.forName("dot.junit.opcodes.iget_boolean.d.T_iget_boolean_21");
            fail("expected a verification exception");
        } catch (Throwable t) {
            DxUtil.checkVerifyException(t);
        }
    }
    public void testVFE16() {
        try {
            Class.forName("dot.junit.opcodes.iget_boolean.d.T_iget_boolean_5");
            fail("expected a verification exception");
        } catch (Throwable t) {
            DxUtil.checkVerifyException(t);
        }
    }
    public void testVFE30() {
        try {
            Class.forName("dot.junit.opcodes.iget_boolean.d.T_iget_boolean_30");
            fail("expected a verification exception");
        } catch (Throwable t) {
            DxUtil.checkVerifyException(t);
        }
    }
}
