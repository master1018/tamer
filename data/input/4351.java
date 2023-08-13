public class B6499348 {
    public static void main(String[] args) throws java.io.IOException {
        Socket s = new Socket();
        ServerSocket ss = new ServerSocket();
        DatagramSocket ds =  new DatagramSocket((SocketAddress) null);
        if (! (s instanceof Closeable))
            throw new RuntimeException("Socket is not a java.io.Closeable");
        if (! (ss instanceof Closeable))
            throw new RuntimeException("ServerSocket is not a java.io.Closeable");
        if (! (ds instanceof Closeable))
            throw new RuntimeException("DatagramSocket is not a java.io.Closeable");
        s.close();
        ss.close();
        ds.close();
    }
}
