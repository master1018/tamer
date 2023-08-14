public class MulticastTTL {
    public static void main(String args[]) throws Exception {
        MulticastSocket soc = null;
        DatagramPacket pac = null;
        InetAddress sin = null;
        byte [] array = new byte[65537];
        int port = 0;
        byte old_ttl = 0;
        byte new_ttl = 64;
        byte ttl = 0;
        sin = InetAddress.getByName("224.80.80.80");
        soc = new MulticastSocket();
        port = soc.getLocalPort();
        old_ttl = soc.getTTL();
        pac = new DatagramPacket(array, array.length, sin, port);
        try {
            soc.send(pac, new_ttl);
        } catch(java.io.IOException e) {
            ttl = soc.getTTL();
            soc.close();
            if(ttl != old_ttl)
                throw new RuntimeException("TTL ");
        }
        soc.close();
    }
}
