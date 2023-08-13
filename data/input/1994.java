public class RMIDirectSocketFactory extends RMISocketFactory {
    public Socket createSocket(String host, int port) throws IOException
    {
        return new Socket(host, port);
    }
    public ServerSocket createServerSocket(int port) throws IOException
    {
        return new ServerSocket(port);
    }
}
