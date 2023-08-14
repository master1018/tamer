public class AttributeChangeNotification extends javax.management.Notification {
    private static final long serialVersionUID = 535176054565814134L;
    public static final String ATTRIBUTE_CHANGE = "jmx.attribute.change";
    private String attributeName = null;
    private String attributeType = null;
    private Object oldValue = null;
    private Object newValue = null;
    public AttributeChangeNotification(Object source, long sequenceNumber, long timeStamp, String msg,
                                       String attributeName, String attributeType, Object oldValue, Object newValue) {
        super(AttributeChangeNotification.ATTRIBUTE_CHANGE, source, sequenceNumber, timeStamp, msg);
        this.attributeName = attributeName;
        this.attributeType = attributeType;
        this.oldValue = oldValue;
        this.newValue = newValue;
    }
    public String getAttributeName() {
        return attributeName;
    }
    public String getAttributeType() {
        return attributeType;
    }
    public Object getOldValue() {
        return oldValue;
    }
    public Object getNewValue() {
        return newValue;
    }
}
