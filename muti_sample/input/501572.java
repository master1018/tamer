public final class HttpConnectionPool {
    public static final HttpConnectionPool INSTANCE = new HttpConnectionPool();
    private final int maxConnections;
    private final HashMap<HttpConfiguration, List<HttpConnection>> connectionPool
            = new HashMap<HttpConfiguration, List<HttpConnection>>();
    private HttpConnectionPool() {
        SecurityManager security = System.getSecurityManager();
        if (security != null) {
            maxConnections = 0;
            return;
        }
        String keepAlive = System.getProperty("http.keepAlive");
        if (keepAlive != null && !Boolean.parseBoolean(keepAlive)) {
            maxConnections = 0;
            return;
        }
        String maxConnectionsString = System.getProperty("http.maxConnections");
        this.maxConnections = maxConnectionsString != null
                ? Integer.parseInt(maxConnectionsString)
                : 5;
    }
    public HttpConnection get(HttpConfiguration config, int connectTimeout) throws IOException {
        synchronized (connectionPool) {
            List<HttpConnection> connections = connectionPool.get(config);
            if (connections != null) {
                while (!connections.isEmpty()) {
                    HttpConnection connection = connections.remove(connections.size() - 1);
                    if (!connection.isStale()) {
                        return connection;
                    }
                }
                connectionPool.remove(config);
            }
        }
        return new HttpConnection(config, connectTimeout);
    }
    public void recycle(HttpConnection connection) {
        if (maxConnections > 0 && connection.isEligibleForRecycling()) {
            HttpConfiguration config = connection.getHttpConfiguration();
            synchronized (connectionPool) {
                List<HttpConnection> connections = connectionPool.get(config);
                if (connections == null) {
                    connections = new ArrayList<HttpConnection>();
                    connectionPool.put(config, connections);
                }
                if (connections.size() < maxConnections) {
                    connections.add(connection);
                    return; 
                }
            }
        }
        connection.closeSocketAndStreams();
    }
}