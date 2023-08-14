public final class DefaultedHttpContext implements HttpContext {
    private final HttpContext local;
    private final HttpContext defaults;
    public DefaultedHttpContext(final HttpContext local, final HttpContext defaults) {
        super();
        if (local == null) {
            throw new IllegalArgumentException("HTTP context may not be null");
        }
        this.local = local;
        this.defaults = defaults;
    }
    public Object getAttribute(final String id) {
        Object obj = this.local.getAttribute(id);
        if (obj == null) {
            return this.defaults.getAttribute(id);
        } else {
            return obj;
        }
    }
    public Object removeAttribute(final String id) {
        return this.local.removeAttribute(id);
    }
    public void setAttribute(final String id, final Object obj) {
        this.local.setAttribute(id, obj);
    }
    public HttpContext getDefaults() {
        return this.defaults;
    }
}
