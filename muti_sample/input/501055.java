public final class BasicHttpParams extends AbstractHttpParams
    implements Serializable, Cloneable {
    private static final long serialVersionUID = -7086398485908701455L;
    private HashMap parameters;
    public BasicHttpParams() {
        super();
    }
    public Object getParameter(final String name) {
        Object param = null;
        if (this.parameters != null) {
            param = this.parameters.get(name);
        }    
        return param;
    }
    public HttpParams setParameter(final String name, final Object value) {
        if (this.parameters == null) {
            this.parameters = new HashMap();
        }
        this.parameters.put(name, value);
        return this;
    }
    public boolean removeParameter(String name) {
        if (this.parameters == null) {
            return false;
        }
        if (this.parameters.containsKey(name)) {
            this.parameters.remove(name);
            return true;
        } else {
            return false;
        }
    }
    public void setParameters(final String[] names, final Object value) {
        for (int i = 0; i < names.length; i++) {
            setParameter(names[i], value);
        }
    }
    public boolean isParameterSet(final String name) {
        return getParameter(name) != null;
    }
    public boolean isParameterSetLocally(final String name) {
        return this.parameters != null && this.parameters.get(name) != null;
    }
    public void clear() {
        this.parameters = null;
    }
    public HttpParams copy() {
        BasicHttpParams clone = new BasicHttpParams();
        copyParams(clone);
        return clone;
    }
    public Object clone() throws CloneNotSupportedException {
        BasicHttpParams clone = (BasicHttpParams) super.clone();
        copyParams(clone);
        return clone;
    }
    protected void copyParams(HttpParams target) {
        if (this.parameters == null)
            return;
        Iterator iter = parameters.entrySet().iterator();
        while (iter.hasNext()) {
            Map.Entry me = (Map.Entry) iter.next();
            if (me.getKey() instanceof String)
                target.setParameter((String)me.getKey(), me.getValue());
        }
    }
}
