public class ConnRouteParamBean extends HttpAbstractParamBean {
    public ConnRouteParamBean (final HttpParams params) {
        super(params);
    }
    public void setDefaultProxy (final HttpHost defaultProxy) {
        params.setParameter(ConnRoutePNames.DEFAULT_PROXY, defaultProxy);
    }
    public void setLocalAddress (final InetAddress address) {
        params.setParameter(ConnRoutePNames.LOCAL_ADDRESS, address);
    }
    public void setForcedRoute (final HttpRoute route) {
        params.setParameter(ConnRoutePNames.FORCED_ROUTE, route);
    }
}
