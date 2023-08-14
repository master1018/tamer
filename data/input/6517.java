public class GetChannel  {
    public static void main(String args[]) throws Exception {
    InetAddress  sin = null;
    Socket       soc = null,soc1 = null;
    InputStream  is = null;
    OutputStream os = null;
    ServerSocket srv = null;
    int          port = 0;
    int          tout = 1000;
    sin = InetAddress.getLocalHost();
    srv = new ServerSocket(port);
    port = srv.getLocalPort();
    soc = new Socket(sin, port);
    soc1 = srv.accept();
    BufferedReader bin = new BufferedReader(
                         new InputStreamReader(soc.getInputStream()));
    BufferedWriter bout = new BufferedWriter(
                          new OutputStreamWriter(soc1.getOutputStream()));
    bout.write("hello");
    bout.newLine();
    bout.flush();
    String reply = bin.readLine();
    if (!reply.equals("hello"))
        throw new RuntimeException("Test failed");
    soc.close();
    soc1.close();
    srv.close();
  }
}
