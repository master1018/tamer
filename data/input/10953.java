final class LdapClientFactory implements PooledConnectionFactory {
    final private String host;
    final private int port;
    final private String socketFactory;
    final private int connTimeout;
    final private int readTimeout;
    final private OutputStream trace;
    LdapClientFactory(String host, int port, String socketFactory,
        int connTimeout, int readTimeout, OutputStream trace) {
        this.host = host;
        this.port = port;
        this.socketFactory = socketFactory;
        this.connTimeout = connTimeout;
        this.readTimeout = readTimeout;
        this.trace = trace;
    }
    public PooledConnection createPooledConnection(PoolCallback pcb)
        throws NamingException {
        return new LdapClient(host, port, socketFactory,
                connTimeout, readTimeout, trace, pcb);
    }
    public String toString() {
        return host + ":" + port;
    }
}
