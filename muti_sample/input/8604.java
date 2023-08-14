public class EndPointInfoImpl
    implements
        SocketInfo,
        LegacyServerSocketEndPointInfo
{
    protected String type;
    protected String hostname;
    protected int port;
    protected int locatorPort;
    protected String name;
    public EndPointInfoImpl(String type, int port, String hostname) {
        this.type = type;
        this.port = port;
        this.hostname = hostname;
        this.locatorPort = -1;
        this.name = LegacyServerSocketEndPointInfo.NO_NAME;
    }
    public String getType() {
        return type;
    }
    public String getHost() {
        return hostname;
    }
    public String getHostName() {
        return hostname;
    }
    public int getPort() {
        return port;
    }
    public int getLocatorPort ()
    {
        return locatorPort;
    }
    public void setLocatorPort (int port)
    {
        locatorPort = port;
    }
    public String getName()
    {
        return name;
    }
    public int hashCode() {
        return type.hashCode() ^ hostname.hashCode() ^ port;
    }
    public boolean equals(Object obj) {
        if (!(obj instanceof EndPointInfoImpl)) {
            return false;
        }
        EndPointInfoImpl other = (EndPointInfoImpl)obj;
        if (type == null) {
            if (other.type != null) {
                return false;
            }
        } else if (!type.equals(other.type)) {
            return false;
        }
        if (port != other.port) {
            return false;
        }
        if (!hostname.equals(other.hostname)) {
            return false;
        }
        return true;
    }
    public String toString ()
    {
        return
            type + " " +
            name + " " +
            hostname + " " +
            port;
    }
}
