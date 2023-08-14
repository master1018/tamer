public class Test_new_instance extends DxTestCase {
    public void testN1() {
        T_new_instance_1 t = new T_new_instance_1();
        String s = t.run();
        assertNotNull(s);
        assertEquals(0, s.compareTo("abc"));
    }
    public void testE1() {
        try {
            T_new_instance_3.run();
            fail("expected Error");
        } catch (Error e) {
        }
    }
    public void testE4() {
        T_new_instance_8 t = new T_new_instance_8();
        try {
            t.run();
            fail("expected InstantiationError");
        } catch (InstantiationError ie) {
        }
    }
    public void testE5() {
        T_new_instance_9 t = new T_new_instance_9();
        try {
            t.run();
            fail("expected Error");
        } catch (Error iae) {
        }
    }
    public void testVFE1() {
        try {
            Class.forName("dot.junit.opcodes.new_instance.d.T_new_instance_6");
            fail("expected a verification exception");
        } catch (Throwable t) {
            DxUtil.checkVerifyException(t);
        }
    }
    public void testVFE2() {
        try {
            Class.forName("dot.junit.opcodes.new_instance.d.T_new_instance_7");
            fail("expected a verification exception");
        } catch (Throwable t) {
            DxUtil.checkVerifyException(t);
        }
    }
    public void testVFE3() {
        try {
            Class.forName("dot.junit.opcodes.new_instance.d.T_new_instance_2");
            fail("expected a verification exception");
        } catch (Throwable t) {
            DxUtil.checkVerifyException(t);
        }
    }
    public void testVFE4() {
        try {
            Class.forName("dot.junit.opcodes.new_instance.d.T_new_instance_10");
            fail("expected a verification exception");
        } catch (Throwable t) {
            DxUtil.checkVerifyException(t);
        }
    }
    public void testVFE5() {
        try {
            Class.forName("dot.junit.opcodes.new_instance.d.T_new_instance_4");
            fail("expected a verification exception");
        } catch (Throwable t) {
            DxUtil.checkVerifyException(t);
        }
    }
    public void testVFE6() {
        try {
            Class.forName("dot.junit.opcodes.new_instance.d.T_new_instance_5");
            fail("expected a verification exception");
        } catch (Throwable t) {
            DxUtil.checkVerifyException(t);
        }
    }
    public void testVFE7() {
        try {
            Class.forName("dot.junit.opcodes.new_instance.d.T_new_instance_11");
            fail("expected a verification exception");
        } catch (Throwable t) {
            DxUtil.checkVerifyException(t);
        }
    }
    public void testVFE8() {
        try {
            Class.forName("dot.junit.opcodes.new_instance.d.T_new_instance_12");
            fail("expected a verification exception");
        } catch (Throwable t) {
            DxUtil.checkVerifyException(t);
        }
    }
}
