public class BasicHttpContext implements HttpContext {
    private final HttpContext parentContext;
    private Map map = null;
    public BasicHttpContext() {
        this(null);
    }
    public BasicHttpContext(final HttpContext parentContext) {
        super();
        this.parentContext = parentContext;
    }
    public Object getAttribute(final String id) {
        if (id == null) {
            throw new IllegalArgumentException("Id may not be null");
        }
        Object obj = null;
        if (this.map != null) {
            obj = this.map.get(id);
        }
        if (obj == null && this.parentContext != null) {
            obj = this.parentContext.getAttribute(id);
        }
        return obj;
    }
    public void setAttribute(final String id, final Object obj) {
        if (id == null) {
            throw new IllegalArgumentException("Id may not be null");
        }
        if (this.map == null) {
            this.map = new HashMap();
        }
        this.map.put(id, obj);
    }
    public Object removeAttribute(final String id) {
        if (id == null) {
            throw new IllegalArgumentException("Id may not be null");
        }
        if (this.map != null) {
            return this.map.remove(id);
        } else {
            return null;
        }
    }
}
