public class CachedUnknownHostName {
  public static void main(String argv[]) throws Exception {
    String hostname = new String("bogusHostName");
    try {
      InetAddress.getByName(hostname);
      System.err.println("Missing java.net.UnknownHostException for host " +
                         hostname);
      throw new Exception("Missing java.net.UnknownHostException");
    } catch(java.net.UnknownHostException e) {
      System.out.println("Caught expected exception:" + e);
    }
    try {
      InetAddress.getByName(hostname);
      System.err.println("Missing java.net.UnknownHostException for host " +
                         hostname);
      throw new Exception("Missing java.net.UnknownHostException");
    } catch(java.net.UnknownHostException e) {
      System.out.println("Caught expected exception:" + e);
    }
    System.out.println("Passed. OKAY");
  }
}
