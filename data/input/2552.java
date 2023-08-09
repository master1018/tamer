public class AttributeChangeNotificationFilter implements NotificationFilter {
    private static final long serialVersionUID = -6347317584796410029L;
    private Vector<String> enabledAttributes = new Vector<String>();
    public synchronized boolean isNotificationEnabled(Notification notification) {
        String type = notification.getType();
        if ((type == null) ||
            (type.equals(AttributeChangeNotification.ATTRIBUTE_CHANGE) == false) ||
            (!(notification instanceof AttributeChangeNotification))) {
            return false;
        }
        String attributeName =
          ((AttributeChangeNotification)notification).getAttributeName();
        return enabledAttributes.contains(attributeName);
    }
    public synchronized void enableAttribute(String name) throws java.lang.IllegalArgumentException {
        if (name == null) {
            throw new java.lang.IllegalArgumentException("The name cannot be null.");
        }
        if (!enabledAttributes.contains(name)) {
            enabledAttributes.addElement(name);
        }
    }
    public synchronized void disableAttribute(String name) {
        enabledAttributes.removeElement(name);
    }
    public synchronized void disableAllAttributes() {
        enabledAttributes.removeAllElements();
    }
    public synchronized Vector<String> getEnabledAttributes() {
        return enabledAttributes;
    }
}
