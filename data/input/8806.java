public class LibprocTest {
   public static void main(String[] args) throws Exception {
      String myStr = "";
      System.out.println("main start");
      synchronized(myStr) {
         try {
            myStr.wait();
         } catch (InterruptedException ee) {
         }
      }
      System.out.println("main end");
   }
}
