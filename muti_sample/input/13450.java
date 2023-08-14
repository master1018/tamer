public class OneExceptionOnly {
    static void doTest(InetAddress ia, int port, boolean testSend) throws Exception {
        System.out.println("");
        System.out.println("***");
        System.out.println("Test Description:");
        System.out.println("    - Send 10 datagrams to bad destination");
        System.out.println("    - <wait a wee while>");
        if (testSend) {
            System.out.println("    - Send another datagram - should throw PUE or timeout");
        } else {
            System.out.println("    - Receive another datagram - should throw PUE or timeout");
        }
        System.out.println("    - Receive another receive - a SocketTimeoutException expected");
        System.out.println("");
        DatagramSocket s1 = new DatagramSocket();
        s1.connect(ia, port);
        byte b[] = new byte[512];
        DatagramPacket p = new DatagramPacket(b, b.length);
        int outstanding = 0;
        for (int i=0; i<20; i++) {
            try {
                s1.send(p);
                outstanding++;
            } catch (PortUnreachableException e) {
                outstanding = 0;
            }
            if (outstanding > 1) {
                break;
            }
        }
        if (outstanding < 1) {
            System.out.println("Insufficient exceptions outstanding - Test Skipped (Passed).");
            s1.close();
            return;
        }
        Thread.currentThread().sleep(5000);
        boolean gotPUE = false;
        boolean gotTimeout = false;
        s1.setSoTimeout(2000);
        try {
            if (testSend) {
                s1.send(p);
            } else {
                s1.receive(p);
            }
        } catch (PortUnreachableException pue) {
            gotPUE = true;
            System.out.println("Expected PortUnreachableException thrown - good!");
        } catch (SocketTimeoutException exc) {
        }
        if (gotPUE) {
            try {
                s1.receive(p);
            } catch (PortUnreachableException pue) {
                throw new Exception("Unexpected PUE received - assumed that PUs would be consumed");
            } catch (SocketTimeoutException exc) {
                System.out.println("Expected SocketTimeoutException thrown - excellent! - Test Passed.");
            }
        } else {
            System.out.println("Expected PUE not thrown - packets probably discarded (Passed).");
        }
        s1.close();
    }
    public static void main(String args[]) throws Exception {
        InetAddress ia;
        int port;
        if (args.length >= 2) {
            ia = InetAddress.getByName(args[0]);
            port = Integer.parseInt(args[1]);
        } else {
            ia = InetAddress.getLocalHost();
            DatagramSocket s1 = new DatagramSocket();
            port = s1.getLocalPort();
            s1.close();
        }
        doTest(ia, port, true);
        doTest(ia, port, false);
    }
}
