public class Test_instance_of extends DxTestCase {
    public void testN1() {
        T_instance_of_1 t = new T_instance_of_1();
        String s = "";
        assertTrue(t.run(s));
    }
    public void testN2() {
        T_instance_of_1 t = new T_instance_of_1();
        assertFalse(t.run(null));
    }
    public void testN4() {
        T_instance_of_2 t = new T_instance_of_2();
        assertTrue(t.run());
    }
    public void testE1() {
        T_instance_of_1 t = new T_instance_of_1();
        assertFalse(t.run(t));
    }
    public void testE2() {
        try {
            T_instance_of_3 tt = new T_instance_of_3();
            tt.run();
            fail("expected a verification exception");
        } catch (IllegalAccessError e) {
        } catch(VerifyError e) {
        }
    }
    public void testE3() {
        try {
            T_instance_of_7 tt = new T_instance_of_7();
            tt.run();
            fail("expected a verification exception");
        } catch (NoClassDefFoundError e) {
        } catch(VerifyError e) {
        }
    }
    public void testVFE1() {
        try {
            Class.forName("dot.junit.opcodes.instance_of.d.T_instance_of_4");
            fail("expected a verification exception");
        } catch (Throwable t) {
            DxUtil.checkVerifyException(t);
        }
    }
    public void testVFE2() {
        try {
            Class.forName("dot.junit.opcodes.instance_of.d.T_instance_of_5");
            fail("expected a verification exception");
        } catch (Throwable t) {
            DxUtil.checkVerifyException(t);
        }
    }
    public void testVFE3() {
        try {
            Class.forName("dot.junit.opcodes.instance_of.d.T_instance_of_8");
            fail("expected a verification exception");
        } catch (Throwable t) {
            DxUtil.checkVerifyException(t);
        }
    }
    public void testVFE4() {
        try {
            Class.forName("dot.junit.opcodes.instance_of.d.T_instance_of_6");
            fail("expected a verification exception");
        } catch (Throwable t) {
            DxUtil.checkVerifyException(t);
        }
    }
}
