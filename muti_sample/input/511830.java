public class Test_rsub_int extends DxTestCase {
    public void testN1() {
        T_rsub_int_1 t = new T_rsub_int_1();
        assertEquals("Subtest_1 is failed", -4, t.run(8));
        assertEquals("Subtest_2 is failed",45, t.run1(15));
        assertEquals("Subtest_3 is failed",0, t.run2(20));
        assertEquals("Subtest_4 is failed",-35, t.run3(10));
        assertEquals("Subtest_5 is failed",-20, t.run4(-50));
        assertEquals("Subtest_6 is failed",20, t.run5(-70));
    }
    public void testN2() {
        T_rsub_int_2 t = new T_rsub_int_2();
        assertEquals("Subtest_1 is failed",255, t.run(0));
        assertEquals("Subtest_2 is failed",-32768, t.run1(0));
        assertEquals("Subtest_3 is failed",-15, t.run2(15));
        assertEquals("Subtest_4 is failed",123, t.run2(-123));
    }
    public void testN3() {
        T_rsub_int_12 t = new T_rsub_int_12();
        try {
            t.run(3.14f);
        } catch (Throwable e) {
        }
    }
    public void testB1() {
        T_rsub_int_3 t = new T_rsub_int_3();
        assertEquals(-Integer.MAX_VALUE, t.run(Integer.MAX_VALUE));
        assertEquals(-Short.MAX_VALUE, t.run(Short.MAX_VALUE));
    }
    public void testB2() {
        T_rsub_int_3 t = new T_rsub_int_3();
        assertEquals(Integer.MIN_VALUE, t.run(Integer.MIN_VALUE));
        assertEquals(32768, t.run(Short.MIN_VALUE));
    }
    public void testB3() {
        T_rsub_int_3 t = new T_rsub_int_3();
        assertEquals(0, t.run(0));
    }
    public void testB4() {
        T_rsub_int_4 t = new T_rsub_int_4();
        assertEquals(Short.MAX_VALUE, t.run(0));
        assertEquals(32766, t.run(1));
        assertEquals(32768, t.run(-1));
    }
    public void testB5() {
        T_rsub_int_4 t = new T_rsub_int_4();
        assertEquals(-2147450881, t.run(Integer.MIN_VALUE));
        assertEquals(-2147450880, t.run(Integer.MAX_VALUE));
        assertEquals(65535, t.run(Short.MIN_VALUE));
    }
    public void testB6() {
        T_rsub_int_5 t = new T_rsub_int_5();
        assertEquals(Short.MIN_VALUE, t.run(0));
        assertEquals(-32769, t.run(1));
        assertEquals(-32767, t.run(-1));
    }
    public void testB7() {
        T_rsub_int_5 t = new T_rsub_int_5();
        assertEquals(2147450881, t.run(Integer.MAX_VALUE));
        assertEquals(2147450880, t.run(Integer.MIN_VALUE));
        assertEquals(-65535, t.run(Short.MAX_VALUE));
    }
    public void testB8() {
        T_rsub_int_6 t = new T_rsub_int_6();
        assertEquals(Integer.MAX_VALUE, t.run(Integer.MIN_VALUE));
        assertEquals(Short.MAX_VALUE, t.run(Short.MIN_VALUE));
    }
    public void testB9() {
        T_rsub_int_6 t = new T_rsub_int_6();
        assertEquals(Integer.MIN_VALUE, t.run(Integer.MAX_VALUE));
        assertEquals(-32768, t.run(Short.MAX_VALUE));
    }
    public void testB10() {
        T_rsub_int_7 t = new T_rsub_int_7();
        assertEquals(-Integer.MAX_VALUE, t.run(Integer.MIN_VALUE));
        assertEquals(-2147483646, t.run(Integer.MAX_VALUE));
    }
    public void testB11() {
        T_rsub_int_7 t = new T_rsub_int_7();
        assertEquals(32769, t.run(Short.MIN_VALUE));
        assertEquals(-32766, t.run(Short.MAX_VALUE));
    }
    public void testB12() {
        T_rsub_int_7 t = new T_rsub_int_7();
        assertEquals(0, t.run(1));
    }
    public void testVFE1() {
        try {
            Class.forName("dot.junit.opcodes.rsub_int.d.T_rsub_int_8");
            fail("expected a verification exception");
        } catch (Throwable t) {
            DxUtil.checkVerifyException(t);
        }
    }
    public void testVFE2() {
        try {
            Class.forName("dot.junit.opcodes.rsub_int.d.T_rsub_int_9");
            fail("expected a verification exception");
        } catch (Throwable t) {
            DxUtil.checkVerifyException(t);
        }
    }
    public void testVFE3() {
        try {
            Class.forName("dot.junit.opcodes.rsub_int.d.T_rsub_int_10");
            fail("expected a verification exception");
        } catch (Throwable t) {
            DxUtil.checkVerifyException(t);
        }
    }
    public void testVFE4() {
        try {
            Class.forName("dot.junit.opcodes.rsub_int.d.T_rsub_int_11");
            fail("expected a verification exception");
        } catch (Throwable t) {
            DxUtil.checkVerifyException(t);
        }
    }
}
