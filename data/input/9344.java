public class InsertMaxValue {
   public static void main (String argv[]) throws Exception {
       StringBuffer sb = new StringBuffer("");
       StringBuffer sb1 = new StringBuffer("Some test StringBuffer");
       try {
           sb.insert(0, new char[5], 1, Integer.MAX_VALUE);
           throw new RuntimeException("Exception expected");
       } catch (StringIndexOutOfBoundsException sobe) {
       } catch (OutOfMemoryError oome) {
           throw new RuntimeException("Wrong exception thrown.");
       }
       try {
           sb1.insert(2, new char[25], 5, Integer.MAX_VALUE);
           throw new RuntimeException("Exception expected");
       } catch (StringIndexOutOfBoundsException sobe) {
       } catch (ArrayIndexOutOfBoundsException aioe) {
           throw new RuntimeException("Wrong exception thrown.");
       }
   }
}
