public class CircularList {
   public static void main (String argv[]) throws IOException {
       System.err.println("\nRegression test for testing of " +
            "serialization/deserialization of " +
            "objects with CirculalListType types \n");
       FileInputStream istream = null;
       FileOutputStream ostream = null;
       try {
           ostream = new FileOutputStream("piotest7.tmp");
           ObjectOutputStream p = new ObjectOutputStream(ostream);
           CircularListTest.setup();
           p.writeObject(CircularListTest.list);
           p.flush();
           istream = new FileInputStream("piotest7.tmp");
           ObjectInputStream q = new ObjectInputStream(istream);
           CircularListTest cv = (CircularListTest)q.readObject();
           if (cv != cv.next) {
               System.err.println("\nTEST FAILED: " +
                    "Circular List Test failed, next != self");
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
class CircularListTest implements java.io.Serializable {
    public CircularListTest next = null;
    public static CircularListTest list = null;
    public static void setup() {
        list = new CircularListTest();
        list.next = list;
    }
}
