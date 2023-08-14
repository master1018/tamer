public class ArrayFields {
   public static void main (String argv[]) throws IOException {
       System.err.println("\nRegression test for testing of " +
           "serialization/deserialization of objects with " +
           "fields of array type\n");
       FileOutputStream ostream = null;
       FileInputStream istream = null;
       try {
           ostream = new FileOutputStream("piotest4.tmp");
           ObjectOutputStream p = new ObjectOutputStream(ostream);
           ArrayTest array = new ArrayTest();
           p.writeObject(array);
           p.flush();
           istream = new FileInputStream("piotest4.tmp");
           ObjectInputStream q = new ObjectInputStream(istream);
           Object obj = null;
           try {
               obj = q.readObject();
           } catch (ClassCastException ee) {
               System.err.println("\nTEST FAILED: An Exception occurred " +
                   ee.getMessage());
               System.err.println("\nBoolean array read as byte array" +
                   " could not be assigned to field z");
               throw new Error();
           }
           ArrayTest array_u = (ArrayTest)obj;
           if (!array.equals(array_u)) {
               System.out.println("\nTEST FAILED: Unpickling of objects " +
                                  "with ArrayTest failed");
               throw new Error();
           }
           System.err.println("\nTEST PASSED");
       } catch (Exception e) {
           System.err.print("TEST FAILED: ");
           e.printStackTrace();
           throw new Error();
       } finally {
           if (istream != null) istream.close();
           if (ostream != null) ostream.close();
       }
   }
}
