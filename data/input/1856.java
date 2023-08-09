public class XNodeInfo {
    public static enum Type {
        MBEAN, NONMBEAN,
        ATTRIBUTES, OPERATIONS, NOTIFICATIONS,
        ATTRIBUTE, OPERATION, NOTIFICATION
    };
    public XNodeInfo(Type type, Object data, String label, String tooltip) {
        this.type = type;
        this.data = data;
        this.label = label;
        this.tooltip = tooltip;
    }
    public Type getType() {
        return type;
    }
    public Object getData() {
        return data;
    }
    public String getLabel() {
        return label;
    }
    public String getToolTipText() {
        return tooltip;
    }
    public String toString() {
        return label;
    }
    private Type type;
    private Object data;
    private String label;
    private String tooltip;
}
