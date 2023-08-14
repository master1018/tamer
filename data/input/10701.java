public class LocalSocketAddress  {
    public static void main(String[] args) throws SocketException {
        InetAddress IPv6LoopbackAddr = null;
        DatagramSocket soc = null;
        try {
            List<NetworkInterface> nics = Collections.list(NetworkInterface.getNetworkInterfaces());
            for (NetworkInterface nic : nics) {
                if (!nic.isLoopback())
                    continue;
                List<InetAddress> addrs = Collections.list(nic.getInetAddresses());
                for (InetAddress addr : addrs) {
                    if (addr instanceof Inet6Address) {
                        IPv6LoopbackAddr = addr;
                        break;
                    }
                }
            }
            if (IPv6LoopbackAddr == null) {
                System.out.println("IPv6 is not available, exiting test.");
                return;
            }
            soc = new DatagramSocket(0, IPv6LoopbackAddr);
            if (!IPv6LoopbackAddr.equals(soc.getLocalAddress())) {
                throw new RuntimeException("Bound address is " + soc.getLocalAddress() +
                                   ", but should be " + IPv6LoopbackAddr);
            }
        }  finally {
            if (soc != null) { soc.close(); }
        }
    }
}
