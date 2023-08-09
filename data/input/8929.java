public class NoLoopbackPackets {
    private static String osname;
    static boolean isWindows() {
        if (osname == null)
            osname = System.getProperty("os.name");
        return osname.contains("Windows");
    }
    private static boolean hasIPv6() throws Exception {
        List<NetworkInterface> nics = Collections.list(
                                        NetworkInterface.getNetworkInterfaces());
        for (NetworkInterface nic : nics) {
            if (!nic.isLoopback()) {
                List<InetAddress> addrs = Collections.list(nic.getInetAddresses());
                for (InetAddress addr : addrs) {
                    if (addr instanceof Inet6Address) {
                        return true;
                    }
                }
            }
        }
        return false;
    }
    public static void main(String[] args) throws Exception {
        if (isWindows()) {
            System.out.println("The test only run on non-Windows OS. Bye.");
            return;
        }
        if (!hasIPv6()) {
            System.out.println("No IPv6 available. Bye.");
            return;
        }
        MulticastSocket msock = null;
        List<SocketAddress> failedGroups = new ArrayList<SocketAddress>();
        try {
            msock = new MulticastSocket();
            int port = msock.getLocalPort();
            List<SocketAddress> groups = new ArrayList<SocketAddress>();
            groups.add(new InetSocketAddress(InetAddress.getByName("224.1.1.1"), port));
            groups.add(new InetSocketAddress(InetAddress.getByName("::ffff:224.1.1.2"), port));
            groups.add(new InetSocketAddress(InetAddress.getByName("ff02::1:1"), port));
            Thread sender = new Thread(new Sender(groups));
            sender.setDaemon(true); 
            sender.start();
            msock.setSoTimeout(5000);       
            byte[] buf = new byte[1024];
            DatagramPacket packet = new DatagramPacket(buf, 0, buf.length);
            for (SocketAddress group : groups) {
                msock.joinGroup(group, null);
                try {
                    msock.receive(packet);
                    failedGroups.add(group);
                } catch (SocketTimeoutException e) {
                }
                msock.leaveGroup(group, null);
            }
        } finally {
            if (msock != null) try { msock.close(); } catch (Exception e) {}
        }
        if (failedGroups.size() > 0) {
            System.out.println("We should not receive anything from following groups, but we did:");
            for (SocketAddress group : failedGroups)
                System.out.println(group);
            throw new RuntimeException("test failed.");
        }
    }
    static class Sender implements Runnable {
        private List<SocketAddress> sendToGroups;
        public Sender(List<SocketAddress> groups) {
            sendToGroups = groups;
        }
        public void run() {
            byte[] buf = "hello world".getBytes();
            List<DatagramPacket> packets = new ArrayList<DatagramPacket>();
            try {
                for (SocketAddress group : sendToGroups) {
                    DatagramPacket packet = new DatagramPacket(buf, buf.length, group);
                    packets.add(packet);
                }
                MulticastSocket msock = new MulticastSocket();
                msock.setLoopbackMode(true);    
                for (;;) {
                    for (DatagramPacket packet : packets) {
                        msock.send(packet);
                    }
                    Thread.sleep(1000);     
                }
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
    }
}
