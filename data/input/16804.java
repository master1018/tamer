public class RMIHttpToPortSocketFactory extends RMISocketFactory {
    public Socket createSocket(String host, int port)
        throws IOException
    {
        return new HttpSendSocket(host, port,
                                  new URL("http", host, port, "/"));
    }
    public ServerSocket createServerSocket(int port)
        throws IOException
    {
        return new HttpAwareServerSocket(port);
    }
}
