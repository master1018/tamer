public class Test_iinc extends DxTestCase {
    public void testN1() {
        T_iinc_1 t = new T_iinc_1();
        assertEquals(5, t.run(4));
    }
    public void testN2() {
        T_iinc_2 t = new T_iinc_2();
        assertEquals(3, t.run(4));
    }
    public void testN3() {
        T_iinc_3 t = new T_iinc_3();
        assertEquals(67, t.run(4));
    }
    public void testB1() {
        T_iinc_4 t = new T_iinc_4();
        assertEquals(Integer.MAX_VALUE, t.run(Integer.MAX_VALUE));
    }
    public void testB2() {
        T_iinc_4 t = new T_iinc_4();
        assertEquals(Integer.MIN_VALUE, t.run(Integer.MIN_VALUE));
    }
    public void testB3() {
        T_iinc_5 t = new T_iinc_5();
        assertEquals(128, t.run(1));
    }
    public void testB4() {
        T_iinc_5 t = new T_iinc_5();
        assertEquals(126, t.run(-1));
    }
    public void testB5() {
        T_iinc_5 t = new T_iinc_5();
        assertEquals(-2147483521, t.run(Integer.MIN_VALUE));
    }
    public void testB6() {
        T_iinc_6 t = new T_iinc_6();
        assertEquals(-127, t.run(1));
    }
    public void testB7() {
        T_iinc_6 t = new T_iinc_6();
        assertEquals(-128, t.run(0));
    }
    public void testVFE1() {
        try {
            Class.forName("dxc.junit.opcodes.iinc.jm.T_iinc_7");
            fail("expected a verification exception");
        } catch (Throwable t) {
            DxUtil.checkVerifyException(t);
        }
    }
    public void testVFE2() {
        try {
            Class.forName("dxc.junit.opcodes.iinc.jm.T_iinc_8");
            fail("expected a verification exception");
        } catch (Throwable t) {
            DxUtil.checkVerifyException(t);
        }
    }
    public void testVFE3() {
        try {
            Class.forName("dxc.junit.opcodes.iinc.jm.T_iinc_9");
            fail("expected a verification exception");
        } catch (Throwable t) {
            DxUtil.checkVerifyException(t);
        }
    }
    public void testVFE4() {
        try {
            Class.forName("dxc.junit.opcodes.iinc.jm.T_iinc_10");
            fail("expected a verification exception");
        } catch (Throwable t) {
            DxUtil.checkVerifyException(t);
        }
    }
    public void testN4() {
        T_iinc_1_w t = new T_iinc_1_w();
        assertEquals(5, t.run(4));
    }
    public void testN5() {
        T_iinc_2_w t = new T_iinc_2_w();
        assertEquals(3, t.run(4));
    }
    public void testN6() {
        T_iinc_3_w t = new T_iinc_3_w();
        assertEquals(7767, t.run(4));
    }
    public void testB8() {
        T_iinc_4_w t = new T_iinc_4_w();
        assertEquals(Integer.MAX_VALUE, t.run(Integer.MAX_VALUE));
    }
    public void testB9() {
        T_iinc_4_w t = new T_iinc_4_w();
        assertEquals(Integer.MIN_VALUE, t.run(Integer.MIN_VALUE));
    }
    public void testB10() {
        T_iinc_5_w t = new T_iinc_5_w();
        assertEquals(32768, t.run(1));
    }
    public void testB11() {
        T_iinc_5_w t = new T_iinc_5_w();
        assertEquals(32766, t.run(-1));
    }
    public void testB12() {
        T_iinc_5_w t = new T_iinc_5_w();
        assertEquals(-2147450881, t.run(Integer.MIN_VALUE));
    }
    public void testB13() {
        T_iinc_6_w t = new T_iinc_6_w();
        assertEquals(-32767, t.run(1));
    }
    public void testB14() {
        T_iinc_6_w t = new T_iinc_6_w();
        assertEquals(-32768, t.run(0));
    }
    public void testVFE5() {
        try {
            Class.forName("dxc.junit.opcodes.iinc.jm.T_iinc_7_w");
            fail("expected a verification exception");
        } catch (Throwable t) {
            DxUtil.checkVerifyException(t);
        }
    }
    public void testVFE6() {
        try {
            Class.forName("dxc.junit.opcodes.iinc.jm.T_iinc_8_w");
            fail("expected a verification exception");
        } catch (Throwable t) {
            DxUtil.checkVerifyException(t);
        }
    }
    public void testVFE7() {
        try {
            Class.forName("dxc.junit.opcodes.iinc.jm.T_iinc_9_w");
            fail("expected a verification exception");
        } catch (Throwable t) {
            DxUtil.checkVerifyException(t);
        }
    }
    public void testVFE8() {
        try {
            Class.forName("dxc.junit.opcodes.iinc.jm.T_iinc_10_w");
            fail("expected a verification exception");
        } catch (Throwable t) {
            DxUtil.checkVerifyException(t);
        }
    }
}
