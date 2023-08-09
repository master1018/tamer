public class IHashCode {
  public static void main (String argv[]) throws Exception {
       int test = System.identityHashCode(null);
       if (test != 0)
           throw new RuntimeException("identityHashCode(null) is "+test);
  }
}
