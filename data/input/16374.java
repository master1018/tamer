public abstract class AbstractFilter implements Filter {
    private ChangedEvent<Filter> changedEvent;
    private Properties properties;
    public AbstractFilter() {
        changedEvent = new ChangedEvent<Filter>(this);
        properties = new Properties();
    }
    public Properties getProperties() {
        return properties;
    }
    public OpenCookie getEditor() {
        return null;
    }
    public ChangedEvent<Filter> getChangedEvent() {
        return changedEvent;
    }
    protected void fireChangedEvent() {
        changedEvent.fire();
    }
}
