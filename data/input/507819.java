public class EventObject implements Serializable {
    private static final long serialVersionUID = 5516075349620653480L;
    protected transient Object source;
    public EventObject(Object source) {
        if (source != null) {
            this.source = source;
        } else {
            throw new IllegalArgumentException();
        }
    }
    public Object getSource() {
        return source;
    }
    @Override
    public String toString() {
        return getClass().getName() + "[source=" + source + ']'; 
    }
}
