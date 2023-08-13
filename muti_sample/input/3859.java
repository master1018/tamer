public class UnicodeConstructor {
      public static void main(String args[]) {
         try {
             BigInteger b1 = new BigInteger("\uff10");
             System.err.println(b1.toString());
             BigInteger b2 = new BigInteger("\uff11\uff10\uff11\uff10");
             System.err.println(b2.toString());
          }
          catch (ArrayIndexOutOfBoundsException e) {
              throw new RuntimeException(
                       "BigInteger is not accepting unicode initializers.");
          }
     }
}
