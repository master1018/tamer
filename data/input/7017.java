public class ExceptionTranslation {
    public static void main(String args[]) throws Exception {
        InetSocketAddress iAddr = new InetSocketAddress("nosuchhostname",5182);
        try {
            SocketChannel channel = SocketChannel.open();
            channel.socket().connect(iAddr, 30000);
            throw new RuntimeException("Expected exception not thrown");
        } catch (UnknownHostException x) {
        }
        try {
            SocketChannel chan1 = SocketChannel.open();
            chan1.socket().bind(new InetSocketAddress(0));
            chan1.socket().bind(new InetSocketAddress(0));
            throw new RuntimeException("Expected exception not thrown");
        } catch(IOException e) {
        }
    }
}
