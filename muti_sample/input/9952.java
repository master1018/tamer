class OnThrowException extends Exception {
    private String myname = "OnThrowException";
}
public class OnThrowTarget extends Thread {
  static void doThrow() throws OnThrowException {
    System.out.println( "target doing throw");
    throw new OnThrowException();
  }
  public static void main( final String[] args )  throws OnThrowException {
    System.out.println( "start of target main" );
    doThrow();
  }
}
