public class OpenLeak {
    public static void main(String[] args) throws Exception {
        InetAddress lh = InetAddress.getLocalHost();
        InetSocketAddress isa = new InetSocketAddress(lh, 12345);
        System.setSecurityManager( new SecurityManager() );
        for (int i=0; i<100000; i++) {
            try {
                SocketChannel.open(isa);
                throw new RuntimeException("This should not happen");
            } catch (SecurityException x) { }
        }
    }
}
