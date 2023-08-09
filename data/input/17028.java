public class B6411513 {
    public static void main( String[] args ) throws Exception {
        Enumeration<NetworkInterface> nics = NetworkInterface.getNetworkInterfaces();
        while (nics.hasMoreElements()) {
            NetworkInterface nic = nics.nextElement();
            if (nic.isUp() && !nic.isVirtual()) {
                Enumeration<InetAddress> addrs = nic.getInetAddresses();
                while (addrs.hasMoreElements()) {
                    InetAddress addr = addrs.nextElement();
                    if (addr instanceof Inet4Address) {
                        System.out.printf("%s : %s\n", nic.getName(), addr);
                        testConnectedUDP(addr);
                    }
                }
            }
        }
    }
    private static void testConnectedUDP(InetAddress addr) throws Exception {
        try {
            DatagramSocket s = new DatagramSocket(0, addr);
            DatagramSocket ss = new DatagramSocket(0, addr);
            System.out.print("\tconnect...");
            s.connect(ss.getLocalAddress(), ss.getLocalPort());
            System.out.print("disconnect...");
            s.disconnect();
            byte[] data = { 0, 1, 2 };
            DatagramPacket p = new DatagramPacket(data, data.length,
                    s.getLocalAddress(), s.getLocalPort());
            s.setSoTimeout( 10000 );
            System.out.print("send...");
            s.send( p );
            System.out.print("recv...");
            s.receive( p );
            System.out.println("OK");
            ss.close();
            s.close();
        } catch( Exception e ){
            e.printStackTrace();
            throw e;
        }
    }
}
