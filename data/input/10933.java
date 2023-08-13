public class ChangingAddress {
    static void check(DatagramSocket ds, DatagramChannel dc) {
        InetAddress expected = ds.getLocalAddress();
        InetAddress actual = dc.socket().getLocalAddress();
        if ((expected.isAnyLocalAddress() != actual.isAnyLocalAddress()) &&
            !expected.equals(actual))
        {
            throw new RuntimeException("Expected: " + expected + ", actual: " + actual);
        }
    }
    public static void main(String[] args) throws Exception {
        InetAddress lh = InetAddress.getLocalHost();
        SocketAddress remote = new InetSocketAddress(lh, 1234);
        DatagramSocket ds = null;
        DatagramChannel dc = null;
        try {
            ds = new DatagramSocket();
            dc = DatagramChannel.open().bind(new InetSocketAddress(0));
            check(ds, dc);
            ds.connect(remote);
            dc.connect(remote);
            check(ds, dc);
            ds.disconnect();
            dc.disconnect();
            check(ds, dc);
            ds.connect(remote);
            dc.socket().connect(remote);
            check(ds, dc);
            ds.disconnect();
            dc.socket().disconnect();
            check(ds, dc);
       } finally {
            if (ds != null) ds.close();
            if (dc != null) dc.close();
       }
    }
}
