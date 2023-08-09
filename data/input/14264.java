public class EmptyBuffer {
    static PrintStream log = System.err;
    public static void main(String[] args) throws Exception {
        test();
    }
    static void test() throws Exception {
        Server server = new Server();
        Thread serverThread = new Thread(server);
        serverThread.start();
        DatagramChannel dc = DatagramChannel.open();
        try {
            ByteBuffer bb = ByteBuffer.allocateDirect(12);
            bb.order(ByteOrder.BIG_ENDIAN);
            bb.putInt(1).putLong(1);
            bb.flip();
            InetAddress address = InetAddress.getLocalHost();
            InetSocketAddress isa = new InetSocketAddress(address, server.port());
            dc.connect(isa);
            dc.write(bb);
            bb.rewind();
            dc.write(bb);
            bb.rewind();
            dc.write(bb);
            Thread.sleep(2000);
            serverThread.interrupt();
            server.throwException();
        } finally {
            dc.close();
        }
    }
    public static class Server implements Runnable {
        final DatagramChannel dc;
        Exception e = null;
        Server() throws IOException {
            this.dc = DatagramChannel.open().bind(new InetSocketAddress(0));
        }
        int port() {
            return dc.socket().getLocalPort();
        }
        void throwException() throws Exception {
            if (e != null)
                throw e;
        }
        void showBuffer(String s, ByteBuffer bb) {
            log.println(s);
            bb.rewind();
            for (int i=0; i<bb.limit(); i++) {
                byte element = bb.get();
                log.print(element);
            }
            log.println();
        }
        public void run() {
            SocketAddress sa = null;
            int numberReceived = 0;
            try {
                ByteBuffer bb = ByteBuffer.allocateDirect(12);
                bb.clear();
                while (!Thread.interrupted()) {
                    try {
                        sa = dc.receive(bb);
                    } catch (ClosedByInterruptException cbie) {
                        log.println("Took expected exit");
                        break;
                    }
                    if (sa != null) {
                        log.println("Client: " + sa);
                        showBuffer("RECV", bb);
                        sa = null;
                        numberReceived++;
                        if (numberReceived > 3)
                            throw new RuntimeException("Test failed");
                    }
                }
            } catch (Exception ex) {
                e = ex;
            } finally {
                try { dc.close(); } catch (IOException ignore) { }
            }
        }
    }
}
