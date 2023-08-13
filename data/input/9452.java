public class AddressTest {
    class MySocketAddress extends SocketAddress {
        public MySocketAddress() {
        }
    }
    public AddressTest() throws Exception {
        SocketAddress addr = new MySocketAddress();
        Socket soc = new Socket();
        ServerSocket serv = new ServerSocket();
        DatagramSocket ds = new DatagramSocket((SocketAddress)null);
        DatagramPacket pac = new DatagramPacket(new byte[20], 20);
        MulticastSocket mul = new MulticastSocket((SocketAddress) null);
        boolean ok = false;
        try {
            soc.bind(addr);
        } catch (IllegalArgumentException e) {
            ok = true;
        } catch (Exception e2) {
        }
        if (!ok)
            throw new RuntimeException("Socket.bind should throw IllegalArgumentException!");
        ok = false;
        soc.close();
        soc = new Socket();
        try {
            soc.connect(addr, 100);
        } catch (IllegalArgumentException e) {
            ok = true;
        } catch (Exception e2) {
        }
        if (!ok)
            throw new RuntimeException("Socket.connect should throw IllegalArgumentException!");
        ok = false;
        try {
            serv.bind(addr);
        } catch (IllegalArgumentException e) {
            ok = true;
        } catch (Exception e2) {
        }
        if (!ok)
            throw new RuntimeException("ServerSocket.bind should throw IllegalArgumentException!");
        ok = false;
        try {
            pac.setSocketAddress(addr);
        } catch (IllegalArgumentException e) {
            ok = true;
        } catch (Exception e2) {
        }
        if (!ok)
            throw new RuntimeException("DatagramPacket.setSocketAddress should throw IllegalArgumentException");
        ok = false;
        try {
            ds.bind(addr);
        } catch (IllegalArgumentException e) {
            ok = true;
        } catch (Exception e2) {
        }
        if (!ok)
            throw new RuntimeException("DatagramSocket.bind should throw IllegalArgumentException");
        ok = false;
        try {
            ds.connect(addr);
        } catch (IllegalArgumentException e) {
            ok = true;
        } catch (Exception e2) {
        }
        if (!ok)
            throw new RuntimeException("DatagramSocket.connect should throw IllegalArgumentException");
        ok = false;
        try {
            mul.bind(addr);
        } catch (IllegalArgumentException e) {
            ok = true;
        } catch (Exception e2) {
        }
        if (!ok)
            throw new RuntimeException("MulticastSocket.bind should throw IllegalArgumentException");
        ok = false;
        mul.close();
        mul = new MulticastSocket(new InetSocketAddress(0));
        try {
            mul.joinGroup(addr, null);
        } catch (IllegalArgumentException e) {
            ok = true;
        } catch (Exception e2) {
        }
        if (!ok)
            throw new RuntimeException("MulticastSocket.joinGroup should throw IllegalArgumentException");
        ok = false;
        try {
            mul.leaveGroup(addr, null);
        } catch (IllegalArgumentException e) {
            ok = true;
        } catch (Exception e2) {
        }
        if (!ok)
            throw new RuntimeException("MulticastSocket.leaveGroup should throw IllegalArgumentException");
    }
    public static void main(String[] args) throws Exception {
        new AddressTest();
    }
}
