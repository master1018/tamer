public class ThereCanBeOnlyOne {
    static void doTest(InetAddress ia, boolean testSend) throws Exception {
        DatagramChannel dc1 = DatagramChannel.open();
        dc1.socket().bind((SocketAddress)null);
        int port = dc1.socket().getLocalPort();
        InetSocketAddress isa = new InetSocketAddress(ia, port);
        dc1.connect(isa);
        ByteBuffer bb = ByteBuffer.allocateDirect(512);
        bb.put("hello".getBytes());
        bb.flip();
        int outstanding = 0;
        for (int i=0; i<20; i++) {
            try {
                bb.rewind();
                dc1.write(bb);
                outstanding++;
            } catch (PortUnreachableException e) {
                outstanding = 0;
            }
            if (outstanding > 1) {
                break;
            }
        }
        if (outstanding < 1) {
            System.err.println("Insufficient exceptions outstanding - Test Skipped (Passed).");
            dc1.close();
            return;
        }
        Thread.currentThread().sleep(5000);
        boolean gotPUE = false;
        boolean gotTimeout = false;
        dc1.configureBlocking(false);
        try {
            if (testSend) {
                bb.rewind();
                dc1.write(bb);
            } else {
                bb.clear();
                dc1.receive(bb);
            }
        } catch (PortUnreachableException pue) {
            System.err.println("Got one PUE...");
            gotPUE = true;
        }
        if (gotPUE) {
            try {
                dc1.receive(bb);
            } catch (PortUnreachableException pue) {
                throw new Exception("PUs should have been consumed");
            }
        } else {
        }
        dc1.close();
    }
    public static void main(String args[]) throws Exception {
        InetAddress ia = InetAddress.getLocalHost();
        doTest(ia, true);
        doTest(ia, false);
    }
}
