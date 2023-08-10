public class Replicator extends Resource {
    private static final long serialVersionUID = 8153881753668230575L;
    private int port = 0;
    private String host = null;
    private String role = null;
    private String source = null;
    private String dataServiceName = null;
    public Replicator(String name, String description) {
        super(REPLICATOR, name, description);
    }
    public Replicator(TungstenProperties props) {
        props.applyProperties(this, true);
    }
    public int getPort() {
        return port;
    }
    public void setPort(int port) {
        this.port = port;
    }
    public String getHost() {
        return host;
    }
    public void setHost(String host) {
        this.host = host;
    }
    public String getRole() {
        return role;
    }
    public void setRole(String role) {
        this.role = role;
    }
    public String getSource() {
        return source;
    }
    public void setSource(String source) {
        this.source = source;
    }
    public String getDataServiceName() {
        return dataServiceName;
    }
    public void setDataServiceName(String dataServiceName) {
        this.dataServiceName = dataServiceName;
    }
    public void setName(String name) {
        this.name = name;
    }
    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append(getName()).append("(").append("source=").append(getSource());
        builder.append(", role=").append(getRole()).append(", address=").append(getHost());
        builder.append(":").append(getPort()).append(")");
        return builder.toString();
    }
}
