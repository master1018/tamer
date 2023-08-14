public class Test_goto_32 extends DxTestCase {
       public void testN1() {
           T_goto_32_1 t = new T_goto_32_1();
           assertEquals(0, t.run(20));
       }
       public void testVFE1() {
           try {
               Class.forName("dot.junit.opcodes.goto_32.d.T_goto_32_2");
               fail("expected a verification exception");
           } catch (Throwable t) {
               DxUtil.checkVerifyException(t);
           }
       }
       public void testVFE2() {
           try {
               Class.forName("dot.junit.opcodes.goto_32.d.T_goto_32_3");
               fail("expected a verification exception");
           } catch (Throwable t) {
               DxUtil.checkVerifyException(t);
           }
       }
       public void testVFE3() {
           try {
               Class.forName("dot.junit.opcodes.goto_32.d.T_goto_32_4");
           } catch (Throwable t) {
               fail("not expected" + t);
           }
       }
}
