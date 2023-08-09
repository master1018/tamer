public class B6427403 {
    public static void main( String[] args ) throws IOException {
        InetAddress lh = InetAddress.getLocalHost();
        MulticastSocket ms = new MulticastSocket( new InetSocketAddress(lh, 0) );
        ms.joinGroup( InetAddress.getByName("224.80.80.80") );
        ms.close();
    }
}
