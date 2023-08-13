public class SocketTimeout  {
    static final int TIMEOUT = 1000;
    public static void main(String args[]) throws Exception {
    InetAddress  sin = InetAddress.getLocalHost();
    Socket       soc = null,soc1 = null;
    InputStream  is = null;
    ServerSocket srv = null;
    int          port = 0;
    srv = new ServerSocket(0);
    port = srv.getLocalPort();
    soc = new Socket(sin, port);
    soc1 = srv.accept();
    soc.setSoTimeout(TIMEOUT);
    srv.setSoTimeout(TIMEOUT);
    try {
      is = soc.getInputStream();
      is.read();
    } catch(InterruptedIOException e) {
        try {
            if (! (e instanceof java.net.SocketTimeoutException))
                throw new Exception ("Wrong exception class thrown");
        } catch(NoClassDefFoundError e1) {
            throw new Exception ("SocketTimeoutException: not found");
        }
    } finally {
        soc.close();
        soc1.close();
    }
    try {
      srv.accept ();
    } catch(InterruptedIOException e) {
        try {
            if (! (e instanceof java.net.SocketTimeoutException))
                throw new Exception ("Wrong exception class thrown");
        } catch(NoClassDefFoundError e1) {
            throw new Exception ("SocketTimeoutException: not found");
        }
    } finally {
        srv.close();
    }
    DatagramSocket dg = new DatagramSocket ();
    dg.setSoTimeout (TIMEOUT);
    try {
      dg.receive (new DatagramPacket (new byte [64], 64));
    } catch(InterruptedIOException e) {
        try {
            if (! (e instanceof java.net.SocketTimeoutException))
                throw new Exception ("Wrong exception class thrown");
        } catch(NoClassDefFoundError e1) {
            throw new Exception ("SocketTimeoutException: not found");
        }
    } finally {
        dg.close();
    }
  }
}
