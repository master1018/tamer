public class SelectWhenRefused {
    public static void main(String[] args) throws IOException {
        DatagramChannel dc = DatagramChannel.open().bind(new InetSocketAddress(0));
        int port = dc.socket().getLocalPort();
        dc.close();
        SocketAddress refuser = new InetSocketAddress(InetAddress.getLocalHost(), port);
        dc = DatagramChannel.open().bind(new InetSocketAddress(0));
        Selector sel = Selector.open();
        try {
            dc.configureBlocking(false);
            dc.register(sel, SelectionKey.OP_READ);
            sendDatagram(dc, refuser);
            int n = sel.select(2000);
            if (n > 0) {
                throw new RuntimeException("Unexpected wakeup");
            }
            dc.connect(refuser);
            try {
                sendDatagram(dc, refuser);
                n = sel.select(2000);
                if (n > 0) {
                    sel.selectedKeys().clear();
                    try {
                        n = dc.read(ByteBuffer.allocate(100));
                        throw new RuntimeException("Unexpected datagram received");
                    } catch (PortUnreachableException pue) {
                    }
                }
            } finally {
                dc.disconnect();
            }
            sendDatagram(dc, refuser);
            n = sel.select(2000);
            if (n > 0) {
                throw new RuntimeException("Unexpected wakeup after disconnect");
            }
        } finally {
            sel.close();
            dc.close();
        }
    }
    static void sendDatagram(DatagramChannel dc, SocketAddress remote)
        throws IOException
    {
        ByteBuffer bb = ByteBuffer.wrap("Greetings!".getBytes());
        dc.send(bb, remote);
    }
}
