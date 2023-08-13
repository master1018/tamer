public class Test_opc_new extends DxTestCase {
    public void testN1() {
        T_opc_new_1 t = new T_opc_new_1();
        String s = t.run();
        assertNotNull(s);
        assertEquals(0, s.compareTo("abc"));
    }
    public void testE1() {
        try {
            T_opc_new_3.run();
            fail("expected Error");
        } catch (Error e) {
        }
    }
    public void testE2() {
        try {
            T_opc_new_4 t = new T_opc_new_4();
            t.run();
            fail("expected IllegalAccessError");
        } catch (IllegalAccessError iae) {
        } catch (VerifyError vfe) { 
            System.out.print("dvmvfe:"); 
        }
    }
    public void testE3() {
        try {
            T_opc_new_5 t = new T_opc_new_5();
            t.run();
            fail("expected NoClassDefFoundError");
        } catch (NoClassDefFoundError iae) {
        } catch (VerifyError vfe) { 
            System.out.print("dvmvfe:"); 
        }
    }
    public void testE4() {
        try {
            T_opc_new_8 t = new T_opc_new_8();
            t.run();
            fail("expected InstantiationError");
        } catch (InstantiationError ie) {
        } catch (VerifyError vfe) { 
            System.out.print("dvmvfe:"); 
        }
    }
    public void testE5() {
        T_opc_new_9 t = new T_opc_new_9();
        try {
            t.run();
            fail("expected Error");
        } catch (Error iae) {
        }
    }
    public void testVFE1() {
        try {
            Class.forName("dxc.junit.opcodes.opc_new.jm.T_opc_new_6");
            fail("expected a verification exception");
        } catch (Throwable t) {
            DxUtil.checkVerifyException(t);
        }
    }
    public void testVFE2() {
        try {
            Class.forName("dxc.junit.opcodes.opc_new.jm.T_opc_new_7");
            fail("expected a verification exception");
        } catch (Throwable t) {
            DxUtil.checkVerifyException(t);
        }
    }
    public void testVFE3() {
        try {
            Class.forName("dxc.junit.opcodes.opc_new.jm.T_opc_new_2");
            fail("expected a verification exception");
        } catch (Throwable t) {
            DxUtil.checkVerifyException(t);
        }
    }
    public void testVFE4() {
        try {
            Class.forName("dxc.junit.opcodes.opc_new.jm.T_opc_new_10");
            fail("expected a verification exception");
        } catch (Throwable t) {
            DxUtil.checkVerifyException(t);
        }
    }
}
