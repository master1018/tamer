public class Test_goto_16 extends DxTestCase {
       public void testN1() {
           T_goto_16_1 t = new T_goto_16_1();
           assertEquals(0, t.run(20));
       }
       public void testVFE1() {
           try {
               Class.forName("dot.junit.opcodes.goto_16.d.T_goto_16_3");
               fail("expected a verification exception");
           } catch (Throwable t) {
               DxUtil.checkVerifyException(t);
           }
       }
       public void testVFE2() {
           try {
               Class.forName("dot.junit.opcodes.goto_16.d.T_goto_16_2");
               fail("expected a verification exception");
           } catch (Throwable t) {
               DxUtil.checkVerifyException(t);
           }
       }
       public void testVFE3() {
           try {
               Class.forName("dot.junit.opcodes.goto_16.d.T_goto_16_4");
               fail("expected a verification exception");
           } catch (Throwable t) {
               DxUtil.checkVerifyException(t);
           }
       }
}
