public class ConnManagerParamBean extends HttpAbstractParamBean {
    public ConnManagerParamBean (final HttpParams params) {
        super(params);
    }
    public void setTimeout (final long timeout) {
        params.setLongParameter(ConnManagerPNames.TIMEOUT, timeout);
    }
    public void setMaxTotalConnections (final int maxConnections) {
        params.setIntParameter(ConnManagerPNames.MAX_TOTAL_CONNECTIONS, maxConnections);
    }
    public void setConnectionsPerRoute(final ConnPerRouteBean connPerRoute) {
        params.setParameter(ConnManagerPNames.MAX_CONNECTIONS_PER_ROUTE, connPerRoute);
    }
}
