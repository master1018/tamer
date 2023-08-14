public final class DefaultedHttpParams extends AbstractHttpParams {
    private final HttpParams local;
    private final HttpParams defaults;
    public DefaultedHttpParams(final HttpParams local, final HttpParams defaults) {
        super();
        if (local == null) {
            throw new IllegalArgumentException("HTTP parameters may not be null");
        }
        this.local = local;
        this.defaults = defaults;
    }
    public HttpParams copy() {
        HttpParams clone = this.local.copy();
        return new DefaultedHttpParams(clone, this.defaults);
    }
    public Object getParameter(final String name) {
        Object obj = this.local.getParameter(name);
        if (obj == null && this.defaults != null) {
            obj = this.defaults.getParameter(name);
        }
        return obj;
    }
    public boolean removeParameter(final String name) {
        return this.local.removeParameter(name);
    }
    public HttpParams setParameter(final String name, final Object value) {
        return this.local.setParameter(name, value);
    }
    public HttpParams getDefaults() {
        return this.defaults;
    }
}
