class NetworkConfiguration {
    private Map<NetworkInterface,List<InetAddress>> ip4Interfaces;
    private Map<NetworkInterface,List<InetAddress>> ip6Interfaces;
    private NetworkConfiguration(Map<NetworkInterface,List<InetAddress>> ip4Interfaces,
                                 Map<NetworkInterface,List<InetAddress>> ip6Interfaces)
    {
        this.ip4Interfaces = ip4Interfaces;
        this.ip6Interfaces = ip6Interfaces;
    }
    Iterable<NetworkInterface> ip4Interfaces() {
        return ip4Interfaces.keySet();
    }
    Iterable<NetworkInterface> ip6Interfaces() {
        return ip6Interfaces.keySet();
    }
    Iterable<InetAddress> ip4Addresses(NetworkInterface nif) {
        return ip4Interfaces.get(nif);
    }
    Iterable<InetAddress> ip6Addresses(NetworkInterface nif) {
        return ip6Interfaces.get(nif);
    }
    static NetworkConfiguration probe() throws IOException {
        Map<NetworkInterface,List<InetAddress>> ip4Interfaces =
            new HashMap<NetworkInterface,List<InetAddress>>();
        Map<NetworkInterface,List<InetAddress>> ip6Interfaces =
            new HashMap<NetworkInterface,List<InetAddress>>();
        List<NetworkInterface> nifs = Collections
            .list(NetworkInterface.getNetworkInterfaces());
        for (NetworkInterface nif: nifs) {
            if (!nif.isUp() || !nif.supportsMulticast() || nif.isLoopback())
                continue;
            List<InetAddress> addrs = Collections.list(nif.getInetAddresses());
            for (InetAddress addr: addrs) {
                if (!addr.isAnyLocalAddress()) {
                    if (addr instanceof Inet4Address) {
                        List<InetAddress> list = ip4Interfaces.get(nif);
                        if (list == null) {
                            list = new LinkedList<InetAddress>();
                        }
                        list.add(addr);
                        ip4Interfaces.put(nif, list);
                    } else if (addr instanceof Inet6Address) {
                        List<InetAddress> list = ip6Interfaces.get(nif);
                        if (list == null) {
                            list = new LinkedList<InetAddress>();
                        }
                        list.add(addr);
                        ip6Interfaces.put(nif, list);
                    }
                }
            }
        }
        return new NetworkConfiguration(ip4Interfaces, ip6Interfaces);
    }
}
