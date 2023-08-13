public class SetOutgoingIf {
    private static int PORT = 9001;
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
            List<InetAddress> addrs = Collections.list(nic.getInetAddresses());
            for (InetAddress addr : addrs) {
                if (addr instanceof Inet6Address)
                    return true;
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
        List<NetIf> netIfs = new ArrayList<NetIf>();
        int index = 1;
        for (NetworkInterface nic : Collections.list(NetworkInterface.getNetworkInterfaces())) {
            if (!nic.isLoopback() && nic.supportsMulticast() && nic.isUp()) {
                NetIf netIf = NetIf.create(nic);
                for (InetAddress addr : Collections.list(nic.getInetAddresses())) {
                    if (addr.isAnyLocalAddress())
                        continue;
                    System.out.println("    addr " + addr);
                    if (addr instanceof Inet4Address) {
                        netIf.ipv4Address(true);
                    } else if (addr instanceof Inet6Address) {
                        netIf.ipv6Address(true);
                    }
                }
                if (netIf.ipv4Address() || netIf.ipv6Address()) {
                    netIf.index(index++);
                    netIfs.add(netIf);
                    debug("Using: " + nic);
                }
            }
        }
        if (netIfs.size() <= 1) {
            System.out.println("Need 2 or more network interfaces to run. Bye.");
            return;
        }
        for (NetIf netIf : netIfs) {
            int NetIfIndex = netIf.index();
            List<InetAddress> groups = new ArrayList<InetAddress>();
            if (netIf.ipv4Address()) {
                InetAddress groupv4 = InetAddress.getByName("224.1." + NetIfIndex + ".1");
                groups.add(groupv4);
            }
            if (netIf.ipv6Address()) {
                InetAddress groupv6 = InetAddress.getByName("ff02::1:" + NetIfIndex);
                groups.add(groupv6);
            }
            debug("Adding " + groups + " groups for " + netIf.nic().getName());
            netIf.groups(groups);
            Thread sender = new Thread(new Sender(netIf,
                                                  groups,
                                                  PORT));
            sender.setDaemon(true); 
            sender.start();
        }
        byte[] buf = new byte[1024];
        for (NetIf netIf : netIfs) {
            NetworkInterface nic = netIf.nic();
            for (InetAddress group : netIf.groups()) {
                MulticastSocket mcastsock = new MulticastSocket(PORT);
                mcastsock.setSoTimeout(5000);   
                DatagramPacket packet = new DatagramPacket(buf, 0, buf.length);
                debug("Joining " + group + " on " + nic.getName());
                mcastsock.joinGroup(new InetSocketAddress(group, PORT), nic);
                try {
                    mcastsock.receive(packet);
                    debug("received packet on " + packet.getAddress());
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
                NetworkInterface from = NetworkInterface.getByInetAddress(packet.getAddress());
                NetworkInterface shouldbe = nic;
                if (!from.equals(shouldbe)) {
                    System.out.println("Packets on group "
                                        + group + " should come from "
                                        + shouldbe.getName() + ", but came from "
                                        + from.getName());
                }
                mcastsock.leaveGroup(new InetSocketAddress(group, PORT), nic);
            }
        }
    }
    private static boolean debug = true;
    static void debug(String message) {
        if (debug)
            System.out.println(message);
    }
}
class Sender implements Runnable {
    private NetIf netIf;
    private List<InetAddress> groups;
    private int port;
    public Sender(NetIf netIf,
                  List<InetAddress> groups,
                  int port) {
        this.netIf = netIf;
        this.groups = groups;
        this.port = port;
    }
    public void run() {
        try {
            MulticastSocket mcastsock = new MulticastSocket();
            mcastsock.setNetworkInterface(netIf.nic());
            List<DatagramPacket> packets = new LinkedList<DatagramPacket>();
            byte[] buf = "hello world".getBytes();
            for (InetAddress group : groups) {
                packets.add(new DatagramPacket(buf, buf.length, new InetSocketAddress(group, port)));
            }
            for (;;) {
                for (DatagramPacket packet : packets)
                    mcastsock.send(packet);
                Thread.sleep(1000);   
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
@SuppressWarnings("unchecked")
class NetIf {
    private boolean ipv4Address; 
    private boolean ipv6Address; 
    private int index;
    List<InetAddress> groups = Collections.EMPTY_LIST;
    private final NetworkInterface nic;
    private NetIf(NetworkInterface nic) {
        this.nic = nic;
    }
    static NetIf create(NetworkInterface nic) {
        return new NetIf(nic);
    }
    NetworkInterface nic() {
        return nic;
    }
    boolean ipv4Address() {
        return ipv4Address;
    }
    void ipv4Address(boolean ipv4Address) {
        this.ipv4Address = ipv4Address;
    }
    boolean ipv6Address() {
        return ipv6Address;
    }
    void ipv6Address(boolean ipv6Address) {
        this.ipv6Address = ipv6Address;
    }
    int index() {
        return index;
    }
    void index(int index) {
        this.index = index;
    }
    List<InetAddress> groups() {
        return groups;
    }
    void groups(List<InetAddress> groups) {
        this.groups = groups;
    }
}
