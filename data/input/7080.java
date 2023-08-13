public class Sender {
    private static void usage() {
        System.err.println("usage: java Sender group:port[@interface] message");
        System.exit(-1);
    }
    public static void main(String[] args) throws IOException {
        if (args.length < 2)
            usage();
        MulticastAddress target = MulticastAddress.parse(args[0]);
        ProtocolFamily family = StandardProtocolFamily.INET;
        if (target.group() instanceof Inet6Address)
            family = StandardProtocolFamily.INET6;
        DatagramChannel dc = DatagramChannel.open(family).bind(new InetSocketAddress(0));
        if (target.interf() != null) {
            dc.setOption(StandardSocketOptions.IP_MULTICAST_IF, target.interf());
        }
        dc.send(Charset.defaultCharset().encode(args[1]),
                new InetSocketAddress(target.group(), target.port()));
        dc.close();
    }
}
