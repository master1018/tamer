public class Test_opc_throw extends DxTestCase {
    public void testN1() {
        T_opc_throw_1 t = new T_opc_throw_1();
        try {
            t.run();
            fail("must throw a RuntimeException");
        } catch (RuntimeException re) {
        }
    }
    public void testN2() {
        T_opc_throw_2 t = new T_opc_throw_2();
        try {
            t.run();
            fail("must throw a Throwable");
        } catch (Throwable e) {
        }
    }
    public void testN3() {
        T_opc_throw_8 t = new T_opc_throw_8();
        try {
            t.run();
            fail("must throw a Error");
        } catch (Error e) {
        }
    }
    public void testN4() {
        T_opc_throw_12 t = new T_opc_throw_12();
        assertTrue(t.run());
    }
    public void testE1() {
        T_opc_throw_4 t = new T_opc_throw_4();
        try {
            t.run();
            fail("expected NullPointerException");
        } catch (NullPointerException npe) {
        }
    }
    public void testE2() {
        T_opc_throw_5 t = new T_opc_throw_5();
        try {
            t.run();
            fail("expected IllegalMonitorStateException");
        } catch (IllegalMonitorStateException imse) {
        }
    }
    public void testVFE2() {
        try {
            Class.forName("dxc.junit.opcodes.opc_throw.jm.T_opc_throw_6");
            fail("expected a verification exception");
        } catch (Throwable t) {
            DxUtil.checkVerifyException(t);
        }
    }
    public void testVFE3() {
        try {
            Class.forName("dxc.junit.opcodes.opc_throw.jm.T_opc_throw_7");
            fail("expected a verification exception");
        } catch (Throwable t) {
            DxUtil.checkVerifyException(t);
        }
    }
    public void testVFE4() {
        try {
            Class.forName("dxc.junit.opcodes.opc_throw.jm.T_opc_throw_7");
            fail("expected a verification exception");
        } catch (Throwable t) {
            DxUtil.checkVerifyException(t);
        }
    }
    public void testVFE5() {
        try {
            Class.forName("dxc.junit.opcodes.opc_throw.jm.T_opc_throw_10");
            fail("expected a verification exception");
        } catch (Throwable t) {
            DxUtil.checkVerifyException(t);
        }
    }
}
