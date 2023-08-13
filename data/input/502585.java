public class DatagramTest extends TestCase {
    class Reflector extends Thread {
        DatagramSocket socket;
        boolean alive = true;
        byte[] buffer = new byte[256];
        DatagramPacket packet;
        @Override
        public void run() {
            try {
                while (alive) {
                    try {
                        packet.setLength(buffer.length);
                        socket.receive(packet);
                        String s = stringFromPacket(packet);
                        try {
                            Thread.sleep(100);
                        } catch (InterruptedException ex) {
                        }
                        stringToPacket(s.toUpperCase(), packet);
                        packet.setAddress(InetAddress.getLocalHost());
                        packet.setPort(2345);
                        socket.send(packet);
                    } catch (java.io.InterruptedIOException e) {
                    }
                }
            } catch (java.io.IOException ex) {
                ex.printStackTrace();
            } finally {
                socket.close();
            }
        }
        public Reflector(int port, InetAddress address) {
            try {
                packet = new DatagramPacket(buffer, buffer.length);
                socket = new DatagramSocket(port, address);
            } catch (IOException ex) {
                throw new RuntimeException(
                        "Creating datagram reflector failed", ex);
            }
        }
    }
    static String stringFromPacket(DatagramPacket packet) {
        return new String(packet.getData(), 0, packet.getLength());
    }
    static void stringToPacket(String s, DatagramPacket packet) {
        byte[] bytes = s.getBytes();
        System.arraycopy(bytes, 0, packet.getData(), 0, bytes.length);
        packet.setLength(bytes.length);
    }
    @LargeTest
    public void testDatagram() throws Exception {
        Reflector reflector = null;
        DatagramSocket socket = null;
        try {
            reflector = new Reflector(1234, InetAddress.getLocalHost());
            reflector.start();
            byte[] buffer = new byte[256];
            DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
            socket = new DatagramSocket(2345, InetAddress.getLocalHost());
            for (int i = 1; i <= 10; i++) {
                String s = "Hello, Android world #" + i + "!";
                stringToPacket(s, packet);
                packet.setAddress(InetAddress.getLocalHost());
                packet.setPort(1234);
                socket.send(packet);
                try {
                    Thread.sleep(100);
                } catch (InterruptedException ex) {
                }
                packet.setLength(buffer.length);
                socket.receive(packet);
                String t = stringFromPacket(packet);
                assertEquals(s.toUpperCase(), t);
            }
        } finally {
            if (reflector != null) {
                reflector.alive = false;
            }
            if (socket != null) {
                socket.close();
            }
        }
    }
    @LargeTest
    public void testDatagramSocketSetSOTimeout() throws Exception {
        DatagramSocket sock = null;
        int timeout = 5000;
        long start = System.currentTimeMillis();
        try {
            sock = new DatagramSocket();
            DatagramPacket pack = new DatagramPacket(new byte[100], 100);
            sock.setSoTimeout(timeout);
            sock.receive(pack);
        } catch (SocketTimeoutException e) {
            long delay = System.currentTimeMillis() - start;
            if (Math.abs(delay - timeout) > 1000) {
                fail("timeout was not accurate. expected: " + timeout
                        + " actual: " + delay + " miliseconds.");
            }
        } finally {
            if (sock != null) {
                sock.close();
            }
        }
    }
}
