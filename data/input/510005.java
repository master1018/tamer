public class Test_sparse_switch extends DxTestCase {
    public void testN1() {
        T_sparse_switch_1 t = new T_sparse_switch_1();
        assertEquals(2, t.run(-1));
        assertEquals(-1, t.run(9));
        assertEquals(20, t.run(10));
        assertEquals(-1, t.run(11));
        assertEquals(-1, t.run(14));
        assertEquals(20, t.run(15));
        assertEquals(-1, t.run(16));
    }
    public void testN2() {
        try {
            T_sparse_switch_2 t = new T_sparse_switch_2();
            t.run(-1.23f);
        } catch(Throwable t) {
        }
    }
    public void testB1() {
        T_sparse_switch_1 t = new T_sparse_switch_1();
        assertEquals(-1, t.run(Integer.MAX_VALUE));
    }
    public void testB2() {
        T_sparse_switch_1 t = new T_sparse_switch_1();
        assertEquals(-1, t.run(Integer.MIN_VALUE));
    }
    public void testB3() {
        T_sparse_switch_1 t = new T_sparse_switch_1();
        assertEquals(-1, t.run(0));
    }
    public void testVFE1() {
        try {
            Class.forName("dot.junit.opcodes.sparse_switch.d.T_sparse_switch_3");
            fail("expected a verification exception");
        } catch (Throwable t) {
            DxUtil.checkVerifyException(t);
        }
    }
    public void testVFE2() {
        try {
            Class.forName("dot.junit.opcodes.sparse_switch.d.T_sparse_switch_4");
            fail("expected a verification exception");
        } catch (Throwable t) {
            DxUtil.checkVerifyException(t);
        }
    }
    public void testVFE3() {
        try {
            Class.forName("dot.junit.opcodes.sparse_switch.d.T_sparse_switch_5");
            fail("expected a verification exception");
        } catch (Throwable t) {
            DxUtil.checkVerifyException(t);
        }
    }
    public void testVFE4() {
        try {
            Class.forName("dot.junit.opcodes.sparse_switch.d.T_sparse_switch_6");
            fail("expected a verification exception");
        } catch (Throwable t) {
            DxUtil.checkVerifyException(t);
        }
    }
    public void testVFE5() {
        try {
            Class.forName("dot.junit.opcodes.sparse_switch.d.T_sparse_switch_7");
            fail("expected a verification exception");
        } catch (Throwable t) {
            DxUtil.checkVerifyException(t);
        }
    }
    public void testVFE6() {
        try {
            Class.forName("dot.junit.opcodes.sparse_switch.d.T_sparse_switch_8");
            fail("expected a verification exception");
        } catch (Throwable t) {
            DxUtil.checkVerifyException(t);
        }
    }
    public void testVFE7() {
        try {
            Class.forName("dot.junit.opcodes.sparse_switch.d.T_sparse_switch_9");
            fail("expected a verification exception");
        } catch (Throwable t) {
            DxUtil.checkVerifyException(t);
        }
    }
    public void testVFE9() {
        try {
            Class.forName("dot.junit.opcodes.sparse_switch.d.T_sparse_switch_11");
            fail("expected a verification exception");
        } catch (Throwable t) {
            DxUtil.checkVerifyException(t);
        }
    }
    public void testVFE10() {
        try {
            Class.forName("dot.junit.opcodes.sparse_switch.d.T_sparse_switch_12");
            fail("expected a verification exception");
        } catch (Throwable t) {
            DxUtil.checkVerifyException(t);
        }
    }
    public void testVFE11() {
        try {
            Class.forName("dot.junit.opcodes.sparse_switch.d.T_sparse_switch_13");
            fail("expected a verification exception");
        } catch (Throwable t) {
            DxUtil.checkVerifyException(t);
        }
    }
    public void testVFE12() {
        try {
            Class.forName("dot.junit.opcodes.sparse_switch.d.T_sparse_switch_14");
            fail("expected a verification exception");
        } catch (Throwable t) {
            DxUtil.checkVerifyException(t);
        }
    }
}
