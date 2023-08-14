package dot.junit.opcodes.const_class;
import dot.junit.DxTestCase;
import dot.junit.DxUtil;
import dot.junit.opcodes.const_class.d.T_const_class_1;
import dot.junit.opcodes.const_class.d.T_const_class_2;
import dot.junit.opcodes.const_class.d.T_const_class_6;
import dot.junit.opcodes.const_class.d.T_const_class_7;
public class Test_const_class extends DxTestCase {
    public void testN1() {
        T_const_class_1 t = new T_const_class_1();
        Class c = t.run();
        assertEquals(0, c.getCanonicalName().compareTo("java.lang.String"));
    }
    public void testN2() {
        T_const_class_2 t = new T_const_class_2();
        Class c = t.run();
        assertEquals(c.getCanonicalName(), "int");
    }
    public void testE1() {
        try {
            T_const_class_6 t = new T_const_class_6();
            t.run();
            fail("expected a verification exception");
        } catch (NoClassDefFoundError e) {
        } catch(VerifyError e) {
        }
    }
    public void testE2() {
        try {
            T_const_class_7 t = new T_const_class_7();
            t.run();
            fail("expected a verification exception");
        } catch (IllegalAccessError e) {
        } catch(VerifyError e) {
        }
    }
    public void testVFE1() {
        try {
            Class.forName("dot.junit.opcodes.const_class.d.T_const_class_3");
            fail("expected a verification exception");
        } catch (Throwable t) {
            DxUtil.checkVerifyException(t);
        }
    }
    public void testVFE2() {
        try {
            Class.forName("dot.junit.opcodes.const_class.d.T_const_class_4");
            fail("expected a verification exception");
        } catch (Throwable t) {
            DxUtil.checkVerifyException(t);
        }
    }
    public void testVFE3() {
        try {
            Class.forName("dot.junit.opcodes.const_class.d.T_const_class_5");
            fail("expected a verification exception");
        } catch (Throwable t) {
            DxUtil.checkVerifyException(t);
        }
    }
}
