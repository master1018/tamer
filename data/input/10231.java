public class GE_init6 {
    private static boolean passed = false;
    public static void main(String[] args) {
         try {
             new Frame("Test3").setVisible(true);
         } catch (HeadlessException e){
             passed = true;
         }
         if (!passed){
             throw new RuntimeException("Should have thrown HE but it either didn't throw any or just passed through.");
         }
    }
}
