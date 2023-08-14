public class Test_land extends DxTestCase {
    public void testN1() {
        T_land_1 t = new T_land_1();
        assertEquals(0xfffffff0aal, t.run(0xfffffff8aal, 0xfffffff1aal));
    }
    public void testN2() {
        T_land_1 t = new T_land_1();
        assertEquals(39471121, t.run(987654321, 123456789));
    }
    public void testN3() {
        T_land_1 t = new T_land_1();
        assertEquals(0xABCDEF, t.run(0xABCDEF, -1));
    }
    public void testB1() {
        T_land_1 t = new T_land_1();
        assertEquals(0, t.run(0, -1));
    }
    public void testB2() {
        T_land_1 t = new T_land_1();
        assertEquals(0, t.run(Long.MAX_VALUE, Long.MIN_VALUE));
    }
    public void testVFE1() {
        try {
            Class.forName("dxc.junit.opcodes.land.jm.T_land_2");
            fail("expected a verification exception");
        } catch (Throwable t) {
            DxUtil.checkVerifyException(t);
        }
    }
    public void testVFE2() {
        try {
            Class.forName("dxc.junit.opcodes.land.jm.T_land_3");
            fail("expected a verification exception");
        } catch (Throwable t) {
            DxUtil.checkVerifyException(t);
        }
    }
    public void testVFE3() {
        try {
            Class.forName("dxc.junit.opcodes.land.jm.T_land_4");
            fail("expected a verification exception");
        } catch (Throwable t) {
            DxUtil.checkVerifyException(t);
        }
    }
    public void testVFE4() {
        try {
            Class.forName("dxc.junit.opcodes.land.jm.T_land_7");
            fail("expected a verification exception");
        } catch (Throwable t) {
            DxUtil.checkVerifyException(t);
        }
    }
}
