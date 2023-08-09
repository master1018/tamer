public class RMIHttpToCGISocketFactory extends RMISocketFactory {
    public Socket createSocket(String host, int port)
        throws IOException
    {
        return new HttpSendSocket(host, port,
                                  new URL("http", host,
                                          "/cgi-bin/java-rmi.cgi" +
                                          "?forward=" + port));
    }
    public ServerSocket createServerSocket(int port) throws IOException
    {
        return new HttpAwareServerSocket(port);
    }
}
