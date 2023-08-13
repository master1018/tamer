public class Test_areturn extends DxTestCase {
    public void testN1() {
        T_areturn_1 t = new T_areturn_1();
        assertEquals("hello", t.run());
    }
    public void testN2() {
        T_areturn_1 t = new T_areturn_1();
        assertEquals(t, t.run2());
    }
    public void testN3() {
        T_areturn_1 t = new T_areturn_1();
        Integer a = 12345;
        assertEquals(a, t.run3());
    }
    public void testN4() {
        T_areturn_2 t = new T_areturn_2();
        assertNull(t.run());
    }
    public void testN5() {
        T_areturn_6 t = new T_areturn_6();
        assertEquals("hello", t.run());
    }
    public void testN6() {
        assertTrue(T_areturn_7.execute());
    }
    public void testN7() {
        T_areturn_12 t = new T_areturn_12();
        assertTrue(t.run());
    }
    public void testN8() {
        T_areturn_13 t = new T_areturn_13();
        assertTrue(t.run());
    }
    public void testE1() {
        T_areturn_8 t = new T_areturn_8();
        try {
            assertTrue(t.run());
            fail("expected IllegalMonitorStateException");
        } catch (IllegalMonitorStateException imse) {
        }
    }
    public void testE2() {
        T_areturn_9 t = new T_areturn_9();
        try {
            assertEquals("abc", t.run());
            System.out.print("dvmvfe:");
        } catch (IllegalMonitorStateException imse) {
        }
    }
    public void testVFE1() {
        try {
            Class.forName("dxc.junit.opcodes.areturn.jm.T_areturn_3");
            fail("expected a verification exception");
        } catch (Throwable t) {
            DxUtil.checkVerifyException(t);
        }
    }
    public void testVFE2() {
        try {
            Class.forName("dxc.junit.opcodes.areturn.jm.T_areturn_4");
            fail("expected a verification exception");
        } catch (Throwable t) {
            DxUtil.checkVerifyException(t);
        }
    }
    public void testVFE3() {
        try {
            Class.forName("dxc.junit.opcodes.areturn.jm.T_areturn_5");
            fail("expected a verification exception");
        } catch (Throwable t) {
            DxUtil.checkVerifyException(t);
        }
    }
    public void testVFE4() {
        try {
            Class.forName("dxc.junit.opcodes.areturn.jm.T_areturn_10");
            fail("expected a verification exception");
        } catch (Throwable t) {
            DxUtil.checkVerifyException(t);
        }
    }
    public void testVFE5() {
        try {
            Class.forName("dxc.junit.opcodes.areturn.jm.T_areturn_11");
            fail("expected a verification exception");
        } catch (Throwable t) {
            DxUtil.checkVerifyException(t);
        }
    }
    public void testVFE6() {
        try {
            Class.forName("dxc.junit.opcodes.areturn.jm.T_areturn_14");
            fail("expected a verification exception");
        } catch (Throwable t) {
            DxUtil.checkVerifyException(t);
        }
    }
    @SuppressWarnings("cast")
    public void testVFE7() {
        try {
            RunnerGenerator rg = (RunnerGenerator) Class.forName(
                    "dxc.junit.opcodes.areturn.jm.T_areturn_15").newInstance();
            Runner r = rg.run();
            assertFalse(r instanceof Runner);
            assertFalse(Runner.class.isAssignableFrom(r.getClass()));
            r.doit();
            fail("expected a verification exception");
        } catch (Throwable t) {
            DxUtil.checkVerifyException(t);
        }
    }
}
