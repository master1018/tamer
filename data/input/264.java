class MethodContact implements Contact {
    private Annotation label;
    private MethodPart set;
    private Class[] items;
    private Class item;
    private Class type;
    private Method get;
    private String name;
    public MethodContact(MethodPart get) {
        this(get, null);
    }
    public MethodContact(MethodPart get, MethodPart set) {
        this.label = get.getAnnotation();
        this.items = get.getDependents();
        this.item = get.getDependent();
        this.get = get.getMethod();
        this.type = get.getType();
        this.name = get.getName();
        this.set = set;
    }
    public boolean isReadOnly() {
        return set == null;
    }
    public Annotation getAnnotation() {
        return label;
    }
    public <T extends Annotation> T getAnnotation(Class<T> type) {
        T result = get.getAnnotation(type);
        if (type == label.annotationType()) {
            return (T) label;
        }
        if (result == null && set != null) {
            return set.getAnnotation(type);
        }
        return result;
    }
    public Class getType() {
        return type;
    }
    public Class getDependent() {
        return item;
    }
    public Class[] getDependents() {
        return items;
    }
    public String getName() {
        return name;
    }
    public void set(Object source, Object value) throws Exception {
        Class type = get.getDeclaringClass();
        if (set == null) {
            throw new MethodException("Property '%s' is read only in %s", name, type);
        }
        set.getMethod().invoke(source, value);
    }
    public Object get(Object source) throws Exception {
        return get.invoke(source);
    }
    public String toString() {
        return String.format("method '%s'", name);
    }
}
