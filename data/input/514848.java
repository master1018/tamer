public class InheritableThreadLocal<T> extends ThreadLocal<T> {
    public InheritableThreadLocal() {
        super();
    }
    protected T childValue(T parentValue) {
        return parentValue;
    }
    @Override
    Values values(Thread current) {
        return current.inheritableValues;
    }
    @Override
    Values initializeValues(Thread current) {
        return current.inheritableValues = new Values();
    }
}
