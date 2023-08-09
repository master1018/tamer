public class PropertyChangeEvent extends java.util.EventObject {
    private static final long serialVersionUID = 7042693688939648123L;
    public PropertyChangeEvent(Object source, String propertyName,
                                     Object oldValue, Object newValue) {
        super(source);
        this.propertyName = propertyName;
        this.newValue = newValue;
        this.oldValue = oldValue;
    }
    public String getPropertyName() {
        return propertyName;
    }
    public Object getNewValue() {
        return newValue;
    }
    public Object getOldValue() {
        return oldValue;
    }
    public void setPropagationId(Object propagationId) {
        this.propagationId = propagationId;
    }
    public Object getPropagationId() {
        return propagationId;
    }
    private String propertyName;
    private Object newValue;
    private Object oldValue;
    private Object propagationId;
    public String toString() {
        StringBuilder sb = new StringBuilder(getClass().getName());
        sb.append("[propertyName=").append(getPropertyName());
        appendTo(sb);
        sb.append("; oldValue=").append(getOldValue());
        sb.append("; newValue=").append(getNewValue());
        sb.append("; propagationId=").append(getPropagationId());
        sb.append("; source=").append(getSource());
        return sb.append("]").toString();
    }
    void appendTo(StringBuilder sb) {
    }
}
