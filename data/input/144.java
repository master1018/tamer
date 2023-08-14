public class BerException extends Exception {
  private static final long serialVersionUID = 494709767137042951L;
  public static final int BAD_VERSION=1;
  private int errorType= 0;
  public BerException() {
    errorType= 0;
  }
  public BerException(int x) {
    errorType= x;
  }
  public boolean isInvalidSnmpVersion() {
    if (errorType == BAD_VERSION)
      return true;
    else
      return false;
  }
}
