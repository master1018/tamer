public class Test_iand extends DxTestCase {
    public void testN1() {
        T_iand_1 t = new T_iand_1();
        assertEquals(8, t.run(15, 8));
    }
    public void testN2() {
        T_iand_1 t = new T_iand_1();
        assertEquals(0xfffffff0, t.run(0xfffffff8, 0xfffffff1));
    }
    public void testN3() {
        T_iand_1 t = new T_iand_1();
        assertEquals(0xcafe, t.run(0xcafe, -1));
    }
    public void testB1() {
        T_iand_1 t = new T_iand_1();
        assertEquals(0, t.run(0, -1));
    }
    public void testB2() {
        T_iand_1 t = new T_iand_1();
        assertEquals(0, t.run(Integer.MAX_VALUE, Integer.MIN_VALUE));
    }
    public void testVFE1() {
        try {
            Class.forName("dxc.junit.opcodes.iand.jm.T_iand_2");
            fail("expected a verification exception");
        } catch (Throwable t) {
            DxUtil.checkVerifyException(t);
        }
    }
    public void testVFE2() {
        try {
            Class.forName("dxc.junit.opcodes.iand.jm.T_iand_3");
            fail("expected a verification exception");
        } catch (Throwable t) {
            DxUtil.checkVerifyException(t);
        }
    }
    public void testVFE3() {
        try {
            Class.forName("dxc.junit.opcodes.iand.jm.T_iand_4");
            fail("expected a verification exception");
        } catch (Throwable t) {
            DxUtil.checkVerifyException(t);
        }
    }
    public void testVFE4() {
        try {
            Class.forName("dxc.junit.opcodes.iand.jm.T_iand_5");
            fail("expected a verification exception");
        } catch (Throwable t) {
            DxUtil.checkVerifyException(t);
        }
    }
}
