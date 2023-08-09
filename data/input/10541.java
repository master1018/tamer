public class Reader {
    static void usage() {
        System.err.println("usage: java Reader group:port@interf [-only source...] [-block source...]");
        System.exit(-1);
    }
    static void printDatagram(SocketAddress sa, ByteBuffer buf) {
        System.out.format("-- datagram from %s --\n",
            ((InetSocketAddress)sa).getAddress().getHostAddress());
        System.out.println(Charset.defaultCharset().decode(buf));
    }
    static void parseAddessList(String s, List<InetAddress> list)
        throws UnknownHostException
    {
        String[] sources = s.split(",");
        for (int i=0; i<sources.length; i++) {
            list.add(InetAddress.getByName(sources[i]));
        }
    }
    public static void main(String[] args) throws IOException {
        if (args.length == 0)
            usage();
        MulticastAddress target = MulticastAddress.parse(args[0]);
        if (target.interf() == null)
            usage();
        List<InetAddress> includeList = new ArrayList<InetAddress>();
        List<InetAddress> excludeList = new ArrayList<InetAddress>();
        int argc = 1;
        while (argc < args.length) {
            String option = args[argc++];
            if (argc >= args.length)
                usage();
            String value = args[argc++];
            if (option.equals("-only")) {
                 parseAddessList(value, includeList);
                continue;
            }
            if (option.equals("-block")) {
                parseAddessList(value, excludeList);
                continue;
            }
            usage();
        }
        if (!includeList.isEmpty() && !excludeList.isEmpty()) {
            usage();
        }
        ProtocolFamily family = StandardProtocolFamily.INET;
        if (target.group() instanceof Inet6Address) {
            family = StandardProtocolFamily.INET6;
        }
        DatagramChannel dc = DatagramChannel.open(family)
            .setOption(StandardSocketOptions.SO_REUSEADDR, true)
            .bind(new InetSocketAddress(target.port()));
        if (includeList.isEmpty()) {
            MembershipKey key = dc.join(target.group(), target.interf());
            for (InetAddress source: excludeList) {
                key.block(source);
            }
        } else {
            for (InetAddress source: includeList) {
                dc.join(target.group(), target.interf(), source);
            }
        }
        Selector sel = Selector.open();
        dc.configureBlocking(false);
        dc.register(sel, SelectionKey.OP_READ);
        ByteBuffer buf = ByteBuffer.allocateDirect(4096);
        for (;;) {
            int updated = sel.select();
            if (updated > 0) {
                Iterator<SelectionKey> iter = sel.selectedKeys().iterator();
                while (iter.hasNext()) {
                    SelectionKey sk = iter.next();
                    iter.remove();
                    DatagramChannel ch = (DatagramChannel)sk.channel();
                    SocketAddress sa = ch.receive(buf);
                    if (sa != null) {
                        buf.flip();
                        printDatagram(sa, buf);
                        buf.rewind();
                        buf.limit(buf.capacity());
                    }
                }
            }
        }
    }
}
