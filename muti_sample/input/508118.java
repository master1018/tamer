public class Test_shl_int_2addr extends DxTestCase {
    public void testN1() {
        T_shl_int_2addr_1 t = new T_shl_int_2addr_1();
        assertEquals(30, t.run(15, 1));
    }
    public void testN2() {
        T_shl_int_2addr_1 t = new T_shl_int_2addr_1();
        assertEquals(132, t.run(33, 2));
    }
    public void testN3() {
        T_shl_int_2addr_1 t = new T_shl_int_2addr_1();
        assertEquals(-30, t.run(-15, 1));
    }
    public void testN4() {
        T_shl_int_2addr_1 t = new T_shl_int_2addr_1();
        assertEquals(0x80000000, t.run(1, -1));
    }
    public void testN5() {
        T_shl_int_2addr_1 t = new T_shl_int_2addr_1();
        assertEquals(66, t.run(33, 33));
    }
    public void testN6() {
        T_shl_int_2addr_6 t = new T_shl_int_2addr_6();
        try {
            t.run(3.14f, 1.2f);
        } catch (Throwable e) {
        }
    }
    public void testB1() {
        T_shl_int_2addr_1 t = new T_shl_int_2addr_1();
        assertEquals(0, t.run(0, -1));
    }
    public void testB2() {
        T_shl_int_2addr_1 t = new T_shl_int_2addr_1();
        assertEquals(0xfffffffe, t.run(Integer.MAX_VALUE, 1));
    }
    public void testB3() {
        T_shl_int_2addr_1 t = new T_shl_int_2addr_1();
        assertEquals(0, t.run(Integer.MIN_VALUE, 1));
    }
    public void testB4() {
        T_shl_int_2addr_1 t = new T_shl_int_2addr_1();
        assertEquals(1, t.run(1, 0));
    }
    public void testVFE1() {
        try {
            Class.forName("dot.junit.opcodes.shl_int_2addr.d.T_shl_int_2addr_2");
            fail("expected a verification exception");
        } catch (Throwable t) {
            DxUtil.checkVerifyException(t);
        }
    }
    public void testVFE2() {
        try {
            Class.forName("dot.junit.opcodes.shl_int_2addr.d.T_shl_int_2addr_3");
            fail("expected a verification exception");
        } catch (Throwable t) {
            DxUtil.checkVerifyException(t);
        }
    }
    public void testVFE3() {
        try {
            Class.forName("dot.junit.opcodes.shl_int_2addr.d.T_shl_int_2addr_4");
            fail("expected a verification exception");
        } catch (Throwable t) {
            DxUtil.checkVerifyException(t);
        }
    }
    public void testVFE4() {
        try {
            Class.forName("dot.junit.opcodes.shl_int_2addr.d.T_shl_int_2addr_5");
            fail("expected a verification exception");
        } catch (Throwable t) {
            DxUtil.checkVerifyException(t);
        }
    }
}
