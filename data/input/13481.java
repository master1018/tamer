public class NotificationTargetImpl implements NotificationTarget {
    private InetAddress address;
    private int port;
    private String community;
    public NotificationTargetImpl(String target)
        throws IllegalArgumentException, UnknownHostException  {
        parseTarget(target);
    }
    public NotificationTargetImpl(String address, int port,
                                  String community)
        throws UnknownHostException {
        this(InetAddress.getByName(address),port,community);
    }
    public NotificationTargetImpl(InetAddress address, int port,
                                  String community) {
        this.address = address;
        this.port = port;
        this.community = community;
    }
    private void parseTarget(String target)
        throws IllegalArgumentException, UnknownHostException {
        if(target == null ||
           target.length() == 0) throw new
               IllegalArgumentException("Invalid target [" + target + "]");
        String addrStr;
        if (target.startsWith("[")) {
            final int index = target.indexOf("]");
            final int index2 = target.lastIndexOf(":");
            if(index == -1)
                throw new IllegalArgumentException("Host starts with [ but " +
                                                   "does not end with ]");
            addrStr = target.substring(1, index);
            port = Integer.parseInt(target.substring(index + 2,
                                                     index2));
            if (!isNumericIPv6Address(addrStr)) {
            throw new IllegalArgumentException("Address inside [...] must " +
                                               "be numeric IPv6 address");
            }
            if (addrStr.startsWith("["))
                throw new IllegalArgumentException("More than one [[...]]");
        } else {
            final int index = target.indexOf(":");
            final int index2 = target.lastIndexOf(":");
            if(index == -1) throw new
                IllegalArgumentException("Missing port separator \":\"");
            addrStr = target.substring(0, index);
            port = Integer.parseInt(target.substring(index + 1,
                                                     index2));
        }
        address = InetAddress.getByName(addrStr);
        final int index = target.lastIndexOf(":");
        community = target.substring(index + 1, target.length());
    }
    private static boolean isNumericIPv6Address(String s) {
        return (s.indexOf(':') >= 0);
    }
    public String getCommunity() {
        return community;
    }
    public InetAddress getAddress() {
        return address;
    }
    public int getPort() {
        return port;
    }
    public String toString() {
        return "address : " + address + " port : " + port +
            " community : " + community;
    }
}
