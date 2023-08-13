public class LDAPCertStoreParameters implements CertStoreParameters {
    private static final String DEFAULT_LDAP_SERVER_NAME = "localhost"; 
    private static final int DEFAULT_LDAP_PORT  = 389;
    private final String serverName;
    private final int port;
    public LDAPCertStoreParameters(String serverName, int port) {
        this.port = port;
        this.serverName = serverName;
        if (this.serverName == null) {
            throw new NullPointerException();
        }
    }
    public LDAPCertStoreParameters() {
        this.serverName = DEFAULT_LDAP_SERVER_NAME;
        this.port = DEFAULT_LDAP_PORT;
    }
    public LDAPCertStoreParameters(String serverName) {
        this.port = DEFAULT_LDAP_PORT;
        this.serverName = serverName;
        if (this.serverName == null) {
            throw new NullPointerException();
        }
    }
    public Object clone() {
    	try {
			return super.clone();
		} catch (CloneNotSupportedException e) {
			return null;
		}
    }
    public int getPort() {
        return port;
    }
    public String getServerName() {
        return serverName;
    }
    public String toString() {
        StringBuilder sb =
            new StringBuilder("LDAPCertStoreParameters: [\n serverName: "); 
        sb.append(getServerName());
        sb.append("\n port: "); 
        sb.append(getPort());
        sb.append("\n]"); 
        return sb.toString();
    }
}
