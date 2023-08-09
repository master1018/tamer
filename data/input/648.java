public class BigBacklog
{
  public static void main(String args[]) throws Exception {
    ServerSocket        soc = null;
    Socket              csoc = null;
    int                 port = 0;
    try {
      soc = new ServerSocket(port, Integer.MAX_VALUE);
      port = soc.getLocalPort();
    } catch(Exception e) {
      System.err.println("Failed. Unexpected exception:" + e);
      throw e;
    }
    try {
      csoc = new Socket(InetAddress.getLocalHost(), port);
    } catch(Exception e) {
      System.err.println("Failed. Unexpected exception:" + e);
      throw e;
    }
    try {
        soc.close();
        csoc.close();
    } catch (Exception e) {
    }
    System.err.println("Passed. OKAY");
  }
}
