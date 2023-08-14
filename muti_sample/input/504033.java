public class Test_invokespecial extends DxTestCase {
    public void testN1() {
        T_invokespecial_1 t = new T_invokespecial_1();
        assertEquals(5, t.run());
    }
    public void testN2() {
        T_invokespecial_2 t = new T_invokespecial_2();
        assertEquals(345, t.run());
    }
    public void testN3() {
        T_invokespecial_15 t = new T_invokespecial_15();
        assertEquals(5, t.run());
    }
    public void testN4() {
        try {
            T_invokespecial_17 t = new T_invokespecial_17();
            assertEquals(5, t.run());
        } catch (VerifyError vfe) { 
            System.out.print("dvmvfe:"); 
        }
    }
    public void testN5() {
        T_invokespecial_18 t = new T_invokespecial_18();
        assertEquals(5, t.run());
    }
    public void testN6() {
        T_invokespecial_19 t = new T_invokespecial_19();
        assertEquals(2, t.run());
    }
    public void testN7() {
        T_invokespecial_21 t = new T_invokespecial_21();
        assertEquals(1, t.run());
    }
    public void testN8() {
        assertTrue(T_invokespecial_22.execute());
    }
    public void testE1() {
        try {
            T_invokespecial_7 t = new T_invokespecial_7();
            assertEquals(5, t.run());
            fail("expected NoSuchMethodError");
        } catch (NoSuchMethodError e) {
        } catch (VerifyError vfe) {
            System.out.print("dvmvfe:");
        }
    }
    public void testE2() {
        try {
            T_invokespecial_16 t = new T_invokespecial_16();
            assertEquals(5, t.run());
            fail("expected NoSuchMethodError");
        } catch (NoSuchMethodError e) {
        } catch (VerifyError vfe) {
            System.out.print("dvmvfe:");
        }
    }
    public void testE3() {
        T_invokespecial_8 t = new T_invokespecial_8();
        try {
            assertEquals(5, t.run());
            fail("expected NullPointerException");
        } catch (NullPointerException e) {
        }
    }
    public void testE4() {
        try {
            T_invokespecial_11 t = new T_invokespecial_11();
            assertEquals(5, t.run());
            fail("expected IncompatibleClassChangeError");
        } catch (IncompatibleClassChangeError e) {
        } catch (VerifyError vfe) {
            System.out.print("dvmvfe:");
        }
    }
    public void testE5() {
        T_invokespecial_9 t = new T_invokespecial_9();
        try {
            assertEquals(5, t.run());
            fail("expected UnsatisfiedLinkError");
        } catch (UnsatisfiedLinkError e) {
        }
    }
    public void testE6() {
        try {
            T_invokespecial_12 t = new T_invokespecial_12();
            assertEquals(5, t.run());
            fail("expected IllegalAccessError");
        } catch (IllegalAccessError e) {
        } catch (VerifyError vfe) {
            System.out.print("dvmvfe:");
        }
    }
    public void testE7() {
        T_invokespecial_13 t = new T_invokespecial_13();
        try {
            assertEquals(5, t.run());
            fail("expected AbstractMethodError");
        } catch (AbstractMethodError e) {
        }
    }
    public void testVFE1() {
        try {
            Class
                    .forName("dxc.junit.opcodes.invokespecial.jm.T_invokespecial_3");
            fail("expected a verification exception");
        } catch (Throwable t) {
            DxUtil.checkVerifyException(t);
        }
    }
    public void testVFE2() {
        try {
            Class
                    .forName("dxc.junit.opcodes.invokespecial.jm.T_invokespecial_23");
            fail("expected a verification exception");
        } catch (Throwable t) {
            DxUtil.checkVerifyException(t);
        }
    }
    public void testVFE3() {
        try {
            Class
                    .forName("dxc.junit.opcodes.invokespecial.jm.T_invokespecial_4");
            fail("expected a verification exception");
        } catch (Throwable t) {
            DxUtil.checkVerifyException(t);
        }
    }
    public void testVFE4() {
        try {
            Class
                    .forName("dxc.junit.opcodes.invokespecial.jm.T_invokespecial_6");
            fail("expected a verification exception");
        } catch (Throwable t) {
            DxUtil.checkVerifyException(t);
        }
    }
    public void testVFE5() {
        try {
            Class
                    .forName("dxc.junit.opcodes.invokespecial.jm.T_invokespecial_5");
            fail("expected a verification exception");
        } catch (Throwable t) {
            DxUtil.checkVerifyException(t);
        }
    }
    public void testVFE6() {
        try {
            Class
                    .forName("dxc.junit.opcodes.invokespecial.jm.T_invokespecial_10");
            fail("expected a verification exception");
        } catch (Throwable t) {
            DxUtil.checkVerifyException(t);
        }
    }
    public void testVFE7() {
        try {
            Class
                    .forName("dxc.junit.opcodes.invokespecial.jm.T_invokespecial_20");
            fail("expected a verification exception");
        } catch (Throwable t) {
            DxUtil.checkVerifyException(t);
        }
    }
    public void testVFE8() {
        try {
            Class
                    .forName("dxc.junit.opcodes.invokespecial.jm.T_invokespecial_14");
            fail("expected a verification exception");
        } catch (Throwable t) {
            DxUtil.checkVerifyException(t);
        }
    }
    public void testVFE9() {
        try {
            Class
                    .forName("dxc.junit.opcodes.invokespecial.jm.T_invokespecial_24");
            fail("expected a verification exception");
        } catch (Throwable t) {
            DxUtil.checkVerifyException(t);
        }
    }
    public void testVFE10() {
        try {
            Class
                    .forName("dxc.junit.opcodes.invokespecial.jm.T_invokespecial_25");
            fail("expected a verification exception");
        } catch (Throwable t) {
            DxUtil.checkVerifyException(t);
        }
    }
    public void testVFE11() {
        try {
            Class
                    .forName("dxc.junit.opcodes.invokespecial.jm.T_invokespecial_26");
            fail("expected a verification exception");
        } catch (Throwable t) {
            DxUtil.checkVerifyException(t);
        }
    }
}
