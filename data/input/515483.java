public class Test_opc_instanceof extends DxTestCase {
    public void testN1() {
        T_opc_instanceof_1 t = new T_opc_instanceof_1();
        assertTrue(t.run(""));
    }
    public void testN2() {
        T_opc_instanceof_1 t = new T_opc_instanceof_1();
        assertFalse(t.run(null));
    }
    public void testN3() {
        T_opc_instanceof_1 t = new T_opc_instanceof_1();
        assertFalse(t.run(this));
    }
    public void testN4() {
        T_opc_instanceof_2 t = new T_opc_instanceof_2();
        assertEquals(0, t.run());
    }
    public void testE1() {
        T_opc_instanceof_3 t = new T_opc_instanceof_3();
        try {
            t.run();
            fail("expected IllegalAccessError");
        } catch (IllegalAccessError e) {
        } catch (VerifyError vfe) { 
            System.out.print("dvmvfe:"); 
        }
    }
    public void testE2() {
        T_opc_instanceof_7 t = new T_opc_instanceof_7();
        try {
            t.run();
            fail("expected NoClassDefFoundError");
        } catch (NoClassDefFoundError e) {
        } catch (VerifyError vfe) { 
            System.out.print("dvmvfe:"); 
        }
    }
    public void testVFE1() {
        try {
            Class
                    .forName("dxc.junit.opcodes.opc_instanceof.jm.T_opc_instanceof_4");
            fail("expected a verification exception");
        } catch (Throwable t) {
            DxUtil.checkVerifyException(t);
        }
    }
    public void testVFE2() {
        try {
            Class
                    .forName("dxc.junit.opcodes.opc_instanceof.jm.T_opc_instanceof_5");
            fail("expected a verification exception");
        } catch (Throwable t) {
            DxUtil.checkVerifyException(t);
        }
    }
    public void testVFE3() {
        try {
            Class
                    .forName("dxc.junit.opcodes.opc_instanceof.jm.T_opc_instanceof_6");
            fail("expected a verification exception");
        } catch (Throwable t) {
            DxUtil.checkVerifyException(t);
        }
    }
    public void testVFE4() {
        try {
            Class
                    .forName("dxc.junit.opcodes.opc_instanceof.jm.T_opc_instanceof_8");
            fail("expected a verification exception");
        } catch (Throwable t) {
            DxUtil.checkVerifyException(t);
        }
    }
}
