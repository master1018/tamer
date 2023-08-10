public class NodeDesc implements INodeDesc {
    protected static final String USER_AT_HOSTNAME_REGEX = "([^@])+@([^@])+";
    private final String nodename;
    private final String fwkNode;
    protected NodeDesc(final String nodename, final String fwkNodeName) {
        this.nodename = nodename;
        this.fwkNode = fwkNodeName;
    }
    public static NodeDesc create(final String nodename, final String fwkNodeName) {
        return new NodeDesc(nodename, fwkNodeName);
    }
    public String getNodename() {
        return nodename;
    }
    public boolean isLocal() {
        final String hostname;
        if (containsUserName()) {
            hostname = extractHostname();
        } else {
            hostname = nodename;
        }
        final String fwkNodeHostname;
        if (containsUserName(fwkNode)) {
            fwkNodeHostname = extractHostname(fwkNode);
        } else {
            fwkNodeHostname = fwkNode;
        }
        return fwkNodeHostname.equals(hostname);
    }
    public boolean isNotLocal() {
        return !isLocal();
    }
    public String getFrameworkNodename() {
        return fwkNode;
    }
    public String toString() {
        return "NodeDesc{" + "nodename=" + nodename + ", fwkNode=" + fwkNode + ", islocal=" + isLocal() + "}";
    }
    public boolean equals(final INodeDesc node) {
        return nodename.equals(node.getNodename()) && fwkNode.equals(node.getFrameworkNodename());
    }
    public int hashCode() {
        return nodename.hashCode();
    }
    public boolean containsUserName(final String nodename) {
        if (null == nodename) throw new IllegalArgumentException("Null hostname value");
        return nodename.matches(USER_AT_HOSTNAME_REGEX);
    }
    public boolean containsUserName() {
        return containsUserName(getNodename());
    }
    public String extractUserName(final String nodename) {
        if (containsUserName(nodename)) {
            return nodename.substring(0, nodename.indexOf("@"));
        } else {
            throw new IllegalArgumentException("nodename does not contain a user: " + nodename);
        }
    }
    public String extractUserName() {
        return extractUserName(nodename);
    }
    public String extractHostname(final String nodename) {
        if (containsUserName(nodename)) {
            return nodename.substring(nodename.indexOf("@") + 1, nodename.length());
        } else {
            return nodename;
        }
    }
    public String extractHostname() {
        return extractHostname(nodename);
    }
}
