public class BasicMulticastTests {
    static void membershipKeyTests(NetworkInterface nif,
                                   InetAddress group,
                                   InetAddress source)
        throws IOException
    {
        System.out.format("MembershipKey test using %s @ %s\n",
            group.getHostAddress(), nif.getName());
        ProtocolFamily family = (group instanceof Inet4Address) ?
            StandardProtocolFamily.INET : StandardProtocolFamily.INET6;
        DatagramChannel dc = DatagramChannel.open(family)
            .setOption(StandardSocketOptions.SO_REUSEADDR, true)
            .bind(new InetSocketAddress(source, 0));
        MembershipKey key = dc.join(group, nif);
        MembershipKey other = dc.join(group, nif);
        if (other != key) {
            throw new RuntimeException("existing key not returned");
        }
        if (!key.isValid())
            throw new RuntimeException("key is not valid");
        if (!key.group().equals(group))
            throw new RuntimeException("group is incorrect");
        if (!key.networkInterface().equals(nif))
            throw new RuntimeException("network interface is incorrect");
        if (key.sourceAddress() != null)
            throw new RuntimeException("key is source specific");
        key.drop();
        if (key.isValid()) {
            throw new RuntimeException("key is still valid");
        }
        try {
            key = dc.join(group, nif, source);
            other = dc.join(group, nif, source);
            if (other != key) {
                throw new RuntimeException("existing key not returned");
            }
            if (!key.isValid())
                throw new RuntimeException("key is not valid");
            if (!key.group().equals(group))
                throw new RuntimeException("group is incorrect");
            if (!key.networkInterface().equals(nif))
                throw new RuntimeException("network interface is incorrect");
            if (!key.sourceAddress().equals(source))
                throw new RuntimeException("key's source address incorrect");
            key.drop();
            if (key.isValid()) {
                throw new RuntimeException("key is still valid");
            }
        } catch (UnsupportedOperationException x) {
        }
        dc.close();
    }
    static void exceptionTests(NetworkInterface nif)
        throws IOException
    {
        System.out.println("Exception Tests");
        DatagramChannel dc = DatagramChannel.open(StandardProtocolFamily.INET)
            .setOption(StandardSocketOptions.SO_REUSEADDR, true)
            .bind(new InetSocketAddress(0));
        InetAddress group = InetAddress.getByName("225.4.5.6");
        InetAddress notGroup = InetAddress.getByName("1.2.3.4");
        InetAddress thisHost = InetAddress.getLocalHost();
        MembershipKey key;
        key = dc.join(group, nif);
        try {
            dc.join(group, nif, thisHost);
            throw new RuntimeException("IllegalStateException not thrown");
        } catch (IllegalStateException x) {
        } catch (UnsupportedOperationException x) {
        }
        key.drop();
        try {
            key = dc.join(group, nif, thisHost);
            try {
                dc.join(group, nif);
                throw new RuntimeException("IllegalStateException not thrown");
            } catch (IllegalStateException x) {
            }
            key.drop();
        } catch (UnsupportedOperationException x) {
        }
        try {
            dc.join(notGroup, nif);
            throw new RuntimeException("IllegalArgumentException not thrown");
        } catch (IllegalArgumentException x) {
        }
        try {
            dc.join(notGroup, nif, thisHost);
            throw new RuntimeException("IllegalArgumentException not thrown");
        } catch (IllegalArgumentException x) {
        } catch (UnsupportedOperationException x) {
        }
        try {
            dc.join(null, nif);
            throw new RuntimeException("NullPointerException not thrown");
        } catch (NullPointerException x) {
        }
        try {
            dc.join(group, null);
            throw new RuntimeException("NullPointerException not thrown");
        } catch (NullPointerException x) {
        }
        try {
            dc.join(group, nif, null);
            throw new RuntimeException("NullPointerException not thrown");
        } catch (NullPointerException x) {
        } catch (UnsupportedOperationException x) {
        }
        dc.close();
        try {
            dc.join(group, nif);
            throw new RuntimeException("ClosedChannelException not thrown");
        } catch (ClosedChannelException x) {
        }
        try {
            dc.join(group, nif, thisHost);
            throw new RuntimeException("ClosedChannelException not thrown");
        } catch (ClosedChannelException x) {
        } catch (UnsupportedOperationException x) {
        }
    }
    public static void main(String[] args) throws IOException {
        InetAddress ip4Group = InetAddress.getByName("225.4.5.6");
        InetAddress ip6Group = InetAddress.getByName("ff02::a");
        NetworkConfiguration config = NetworkConfiguration.probe();
        NetworkInterface nif = config.ip4Interfaces().iterator().next();
        InetAddress anySource = config.ip4Addresses(nif).iterator().next();
        membershipKeyTests(nif, ip4Group, anySource);
        exceptionTests(nif);
        Iterator<NetworkInterface> iter = config.ip6Interfaces().iterator();
        if (iter.hasNext()) {
            nif = iter.next();
            anySource = config.ip6Addresses(nif).iterator().next();
            membershipKeyTests(nif, ip6Group, anySource);
        }
    }
}
