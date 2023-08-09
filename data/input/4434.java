public class BigInteger0 {
  private final static byte[] INT_LEN0 = { 2, 0 };
  private final static byte[] INT0 = { 2, 1, 0 };
  public static void main(String args[]) throws Exception {
    try {
      DerInputStream derin = new DerInputStream(INT_LEN0);
      BigInteger bi = derin.getBigInteger();
      throw new Exception("Succeeded parsing invalid zero length value");
    } catch( IOException e ) {
      System.out.println("OK, zero length value rejected.");
    }
    DerInputStream derin = new DerInputStream(INT0);
    BigInteger bi = derin.getBigInteger();
    if( bi.equals(BigInteger.ZERO) == false ) {
      throw new Exception("Failed to parse Integer 0");
    }
  }
}
