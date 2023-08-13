public class B6425815 {
    public static void main(String[] args) throws Exception {
        InetAddress ia;
        MulticastSocket ms;
        try {
            ia = InetAddress.getByName("::1");
            ms = new MulticastSocket(new InetSocketAddress(ia, 1234));
        } catch (Exception e) {
            ia = null;
            ms = null;
        }
        if (ms != null) {
            ms.setTimeToLive(254);
            if (ms.getTimeToLive() != 254) {
                throw new RuntimeException("time to live is incorrect!");
            }
            ms.close();
        }
    }
}
