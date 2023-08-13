public class Test_checkcast extends DxTestCase {
    public void testN1() {
        T_checkcast_1 t = new T_checkcast_1();
        String s = "";
        assertEquals(s, t.run(s));
    }
    public void testN2() {
        T_checkcast_1 t = new T_checkcast_1();
        assertNull(t.run(null));
    }
    public void testN4() {
        T_checkcast_2 t = new T_checkcast_2();
        assertEquals(5, t.run());
    }
    public void testE1() {
        T_checkcast_1 t = new T_checkcast_1();
        try {
            t.run(this);
            fail("expected ClassCastException");
        } catch (ClassCastException iae) {
        }
    }
    public void testE2() {
        try {
            T_checkcast_3 t = new T_checkcast_3();
            t.run();
            fail("expected ClassCastException");
        } catch (ClassCastException cce) {
        }
    }
    public void testE3() {
        try {
            T_checkcast_7 t = new T_checkcast_7();
            t.run();
            fail("expected NoClassDefFoundError");
        } catch (NoClassDefFoundError iae) {
        } catch (VerifyError vfe) {
            System.out.print("dvmvfe:");
        }
    }
    public void testVFE1() {
        try {
            Class.forName("dxc.junit.opcodes.checkcast.jm.T_checkcast_4");
            fail("expected a verification exception");
        } catch (Throwable t) {
            DxUtil.checkVerifyException(t);
        }
    }
    public void testVFE2() {
        try {
            Class.forName("dxc.junit.opcodes.checkcast.jm.T_checkcast_5");
            fail("expected a verification exception");
        } catch (Throwable t) {
            DxUtil.checkVerifyException(t);
        }
    }
    public void testVFE3() {
        try {
            Class.forName("dxc.junit.opcodes.checkcast.jm.T_checkcast_6");
            fail("expected a verification exception");
        } catch (Throwable t) {
            DxUtil.checkVerifyException(t);
        }
    }
    public void testVFE4() {
        try {
            Class.forName("dxc.junit.opcodes.checkcast.jm.T_checkcast_8");
            fail("expected a verification exception");
        } catch (Throwable t) {
            DxUtil.checkVerifyException(t);
        }
    }
}
