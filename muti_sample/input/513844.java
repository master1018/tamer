public class Test_if_acmpeq extends DxTestCase {
    public void testN1() {
        T_if_acmpeq_1 t = new T_if_acmpeq_1();
        String a = "a";
        String b = "b";
        assertEquals(1234, t.run(a, b));
    }
    public void testN2() {
        T_if_acmpeq_1 t = new T_if_acmpeq_1();
        String a = "a";
        assertEquals(1, t.run(a, a));
    }
    public void testB1() {
        T_if_acmpeq_1 t = new T_if_acmpeq_1();
        String a = "a";
        assertEquals(1234, t.run(null, a));
    }
    public void testVFE1() {
        try {
            Class.forName("dxc.junit.opcodes.if_acmpeq.jm.T_if_acmpeq_2");
            fail("expected a verification exception");
        } catch (Throwable t) {
            DxUtil.checkVerifyException(t);
        }
    }
    public void testVFE2() {
        try {
            Class.forName("dxc.junit.opcodes.if_acmpeq.jm.T_if_acmpeq_3");
            fail("expected a verification exception");
        } catch (Throwable t) {
            DxUtil.checkVerifyException(t);
        }
    }
    public void testVFE3() {
        try {
            Class.forName("dxc.junit.opcodes.if_acmpeq.jm.T_if_acmpeq_4");
            fail("expected a verification exception");
        } catch (Throwable t) {
            DxUtil.checkVerifyException(t);
        }
    }
    public void testVFE4() {
        try {
            Class.forName("dxc.junit.opcodes.if_acmpeq.jm.T_if_acmpeq_5");
            fail("expected a verification exception");
        } catch (Throwable t) {
            DxUtil.checkVerifyException(t);
        }
    }
    public void testVFE5() {
        try {
            Class.forName("dxc.junit.opcodes.if_acmpeq.jm.T_if_acmpeq_6");
            fail("expected a verification exception");
        } catch (Throwable t) {
            DxUtil.checkVerifyException(t);
        }
    }
}
