public final class ConnPerRouteBean implements ConnPerRoute {
    public static final int DEFAULT_MAX_CONNECTIONS_PER_ROUTE = 2;   
    private final Map<HttpRoute, Integer> maxPerHostMap;
    private int defaultMax;
    public ConnPerRouteBean(int defaultMax) {
        super();
        this.maxPerHostMap = new HashMap<HttpRoute, Integer>();
        setDefaultMaxPerRoute(defaultMax);
    }
    public ConnPerRouteBean() {
        this(DEFAULT_MAX_CONNECTIONS_PER_ROUTE);
    }
    public int getDefaultMax() {
        return this.defaultMax;
    }
    public void setDefaultMaxPerRoute(int max) {
        if (max < 1) {
            throw new IllegalArgumentException
                ("The maximum must be greater than 0.");
        }
        this.defaultMax = max;
    }
    public void setMaxForRoute(final HttpRoute route, int max) {
        if (route == null) {
            throw new IllegalArgumentException
                ("HTTP route may not be null.");
        }
        if (max < 1) {
            throw new IllegalArgumentException
                ("The maximum must be greater than 0.");
        }
        this.maxPerHostMap.put(route, Integer.valueOf(max));
    }
    public int getMaxForRoute(final HttpRoute route) {
        if (route == null) {
            throw new IllegalArgumentException
                ("HTTP route may not be null.");
        }
        Integer max = this.maxPerHostMap.get(route);
        if (max != null) {
            return max.intValue();
        } else {
            return this.defaultMax;
        }
    }
    public void setMaxForRoutes(final Map<HttpRoute, Integer> map) {
        if (map == null) {
            return;
        }
        this.maxPerHostMap.clear();
        this.maxPerHostMap.putAll(map);
    }
}
