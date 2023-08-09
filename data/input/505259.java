public class Test_return_object extends DxTestCase {
    public void testN1() {
        T_return_object_1 t = new T_return_object_1();
        assertEquals("hello", t.run());
    }
    public void testN2() {
        T_return_object_1 t = new T_return_object_1();
        assertEquals(t, t.run2());
    }
    public void testN4() {
        T_return_object_2 t = new T_return_object_2();
        assertNull(t.run());
    }
    public void testN5() {
        T_return_object_6 t = new T_return_object_6();
        assertEquals("hello", t.run());
    }
    public void testN7() {
        T_return_object_12 t = new T_return_object_12();
        assertTrue(t.run());
    }
    public void testN8() {
        T_return_object_13 t = new T_return_object_13();
        assertTrue(t.run());
    }
    public void testE1() {
        T_return_object_8 t = new T_return_object_8();
        try {
            assertTrue(t.run());
            fail("expected IllegalMonitorStateException");
        } catch (IllegalMonitorStateException imse) {
        }
    }
    public void testVFE1() {
        try {
            Class.forName("dxc.junit.opcodes.return_object.jm.T_return_object_3");
            fail("expected a verification exception");
        } catch (Throwable t) {
            DxUtil.checkVerifyException(t);
        }
    }
    public void testVFE2() {
        try {
            Class.forName("dxc.junit.opcodes.return_object.jm.T_return_object_4");
            fail("expected a verification exception");
        } catch (Throwable t) {
            DxUtil.checkVerifyException(t);
        }
    }
    public void testVFE3() {
        try {
            Class.forName("dxc.junit.opcodes.return_object.jm.T_return_object_16");
            fail("expected a verification exception");
        } catch (Throwable t) {
            DxUtil.checkVerifyException(t);
        }
    }
    public void testVFE4() {
        try {
            Class.forName("dxc.junit.opcodes.return_object.jm.T_return_object_5");
            fail("expected a verification exception");
        } catch (Throwable t) {
            DxUtil.checkVerifyException(t);
        }
    }
    public void testVFE6() {
        try {
            Class.forName("dxc.junit.opcodes.return_object.jm.T_return_object_10");
            fail("expected a verification exception");
        } catch (Throwable t) {
            DxUtil.checkVerifyException(t);
        }
    }
    public void testVFE7() {
        try {
            Class.forName("dxc.junit.opcodes.return_object.jm.T_return_object_11");
            fail("expected a verification exception");
        } catch (Throwable t) {
            DxUtil.checkVerifyException(t);
        }
    }
    public void testVFE8() {
        try {
            Class.forName("dxc.junit.opcodes.return_object.jm.T_return_object_14");
            fail("expected a verification exception");
        } catch (Throwable t) {
            DxUtil.checkVerifyException(t);
        }
    }
    public void testVFE9() {
        try {
            RunnerGenerator rg = (RunnerGenerator) Class.forName(
                    "dot.junit.opcodes.return_object.d.T_return_object_15").newInstance();
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
