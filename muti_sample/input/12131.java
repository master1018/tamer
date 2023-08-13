public class SnmpTooBigException extends Exception {
  private static final long serialVersionUID = 4754796246674803969L;
  public SnmpTooBigException() {
    varBindCount = 0 ;
  }
  public SnmpTooBigException(int n) {
    varBindCount = n ;
  }
  public int getVarBindCount() {
    return varBindCount ;
  }
  private int varBindCount ;
}
