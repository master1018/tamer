public class Test_rsub_int_lit8 extends DxTestCase {
    public void testN1() {
        T_rsub_int_lit8_1 t = new T_rsub_int_lit8_1();
        assertEquals("Subtest_1 is failed", -4, t.run(8));
        assertEquals("Subtest_2 is failed",45, t.run1(15));
        assertEquals("Subtest_3 is failed",0, t.run2(20));
        assertEquals("Subtest_4 is failed",-35, t.run3(10));
        assertEquals("Subtest_5 is failed",-20, t.run4(-50));
        assertEquals("Subtest_6 is failed",20, t.run5(-70));
    }
    public void testN2() {
        T_rsub_int_lit8_2 t = new T_rsub_int_lit8_2();
        assertEquals("Subtest_1 is failed",123, t.run(0));
        assertEquals("Subtest_2 is failed",-123, t.run1(0));
        assertEquals("Subtest_3 is failed",-15, t.run2(15));
        assertEquals("Subtest_4 is failed",85, t.run2(-85));
    }
    public void testN3() {
        T_rsub_int_lit8_12 t = new T_rsub_int_lit8_12();
        try {
            t.run(3.14f);
        } catch (Throwable e) {
        }
    }
    public void testB1() {
        T_rsub_int_lit8_3 t = new T_rsub_int_lit8_3();
        assertEquals(-Integer.MAX_VALUE, t.run(Integer.MAX_VALUE));
        assertEquals(-Byte.MAX_VALUE, t.run(Byte.MAX_VALUE));
    }
    public void testB2() {
        T_rsub_int_lit8_3 t = new T_rsub_int_lit8_3();
        assertEquals(Integer.MIN_VALUE, t.run(Integer.MIN_VALUE));
        assertEquals(128, t.run(Byte.MIN_VALUE));
    }
    public void testB3() {
        T_rsub_int_lit8_3 t = new T_rsub_int_lit8_3();
        assertEquals(0, t.run(0));
    }
    public void testB4() {
        T_rsub_int_lit8_4 t = new T_rsub_int_lit8_4();
        assertEquals(Byte.MAX_VALUE, t.run(0));
        assertEquals(126, t.run(1));
        assertEquals(128, t.run(-1));
    }
    public void testB5() {
        T_rsub_int_lit8_4 t = new T_rsub_int_lit8_4();
        assertEquals(-2147483521, t.run(Integer.MIN_VALUE));
        assertEquals(-2147483520, t.run(Integer.MAX_VALUE));
        assertEquals(255, t.run(Byte.MIN_VALUE));
    }
    public void testB6() {
        T_rsub_int_lit8_5 t = new T_rsub_int_lit8_5();
        assertEquals(Byte.MIN_VALUE, t.run(0));
        assertEquals(-129, t.run(1));
        assertEquals(-127, t.run(-1));
    }
    public void testB7() {
        T_rsub_int_lit8_5 t = new T_rsub_int_lit8_5();
        assertEquals(2147483521, t.run(Integer.MAX_VALUE));
        assertEquals(2147483520, t.run(Integer.MIN_VALUE));
        assertEquals(-255, t.run(Byte.MAX_VALUE));
    }
    public void testB8() {
        T_rsub_int_lit8_6 t = new T_rsub_int_lit8_6();
        assertEquals(Integer.MAX_VALUE, t.run(Integer.MIN_VALUE));
        assertEquals(Byte.MAX_VALUE, t.run(Byte.MIN_VALUE));
    }
    public void testB9() {
        T_rsub_int_lit8_6 t = new T_rsub_int_lit8_6();
        assertEquals(Integer.MIN_VALUE, t.run(Integer.MAX_VALUE));
        assertEquals(Byte.MIN_VALUE, t.run(Byte.MAX_VALUE));
    }
    public void testB10() {
        T_rsub_int_lit8_7 t = new T_rsub_int_lit8_7();
        assertEquals(-Integer.MAX_VALUE, t.run(Integer.MIN_VALUE));
        assertEquals(-2147483646, t.run(Integer.MAX_VALUE));
    }
    public void testB11() {
        T_rsub_int_lit8_7 t = new T_rsub_int_lit8_7();
        assertEquals(129, t.run(Byte.MIN_VALUE));
        assertEquals(-126, t.run(Byte.MAX_VALUE));
    }
    public void testB12() {
        T_rsub_int_lit8_7 t = new T_rsub_int_lit8_7();
        assertEquals(0, t.run(1));
    }
    public void testVFE1() {
        try {
            Class.forName("dot.junit.opcodes.rsub_int_lit8.d.T_rsub_int_lit8_8");
            fail("expected a verification exception");
        } catch (Throwable t) {
            DxUtil.checkVerifyException(t);
        }
    }
    public void testVFE2() {
        try {
            Class.forName("dot.junit.opcodes.rsub_int_lit8.d.T_rsub_int_lit8_9");
            fail("expected a verification exception");
        } catch (Throwable t) {
            DxUtil.checkVerifyException(t);
        }
    }
    public void testVFE3() {
        try {
            Class.forName("dot.junit.opcodes.rsub_int_lit8.d.T_rsub_int_lit8_10");
            fail("expected a verification exception");
        } catch (Throwable t) {
            DxUtil.checkVerifyException(t);
        }
    }
    public void testVFE4() {
        try {
            Class.forName("dot.junit.opcodes.rsub_int_lit8.d.T_rsub_int_lit8_11");
            fail("expected a verification exception");
        } catch (Throwable t) {
            DxUtil.checkVerifyException(t);
        }
    }
}
