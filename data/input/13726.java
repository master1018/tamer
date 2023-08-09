class InstanceFinder<T> {
    private static final String[] EMPTY = { };
    private final Class<? extends T> type;
    private final boolean allow;
    private final String suffix;
    private volatile String[] packages;
    InstanceFinder(Class<? extends T> type, boolean allow, String suffix, String... packages) {
        this.type = type;
        this.allow = allow;
        this.suffix = suffix;
        this.packages = packages.clone();
    }
    public String[] getPackages() {
        return this.packages.clone();
    }
    public void setPackages(String... packages) {
        this.packages = (packages != null) && (packages.length > 0)
                ? packages.clone()
                : EMPTY;
    }
    public T find(Class<?> type) {
        if (type == null) {
            return null;
        }
        String name = type.getName() + this.suffix;
        T object = instantiate(type, name);
        if (object != null) {
            return object;
        }
        if (this.allow) {
            object = instantiate(type, null);
            if (object != null) {
                return object;
            }
        }
        int index = name.lastIndexOf('.') + 1;
        if (index > 0) {
            name = name.substring(index);
        }
        for (String prefix : this.packages) {
            object = instantiate(type, prefix, name);
            if (object != null) {
                return object;
            }
        }
        return null;
    }
    protected T instantiate(Class<?> type, String name) {
        if (type != null) {
            try {
                if (name != null) {
                    type = ClassFinder.findClass(name, type.getClassLoader());
                }
                if (this.type.isAssignableFrom(type)) {
                    return (T) type.newInstance();
                }
            }
            catch (Exception exception) {
            }
        }
        return null;
    }
    protected T instantiate(Class<?> type, String prefix, String name) {
        return instantiate(type, prefix + '.' + name);
    }
}
