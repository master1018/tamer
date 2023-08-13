public class NotificationFilterSupport implements NotificationFilter {
    private static final long serialVersionUID = 6579080007561786969L;
    private List<String> enabledTypes = new Vector<String>();
    public synchronized boolean isNotificationEnabled(Notification notification) {
        String type = notification.getType();
        if (type == null) {
            return false;
        }
        try {
            for (String prefix : enabledTypes) {
                if (type.startsWith(prefix)) {
                    return true;
                }
            }
        } catch (java.lang.NullPointerException e) {
            return false;
        }
        return false;
    }
    public synchronized void enableType(String prefix)
            throws IllegalArgumentException {
        if (prefix == null) {
            throw new IllegalArgumentException("The prefix cannot be null.");
        }
        if (!enabledTypes.contains(prefix)) {
            enabledTypes.add(prefix);
        }
    }
    public synchronized void disableType(String prefix) {
        enabledTypes.remove(prefix);
    }
    public synchronized void disableAllTypes() {
        enabledTypes.clear();
    }
    public synchronized Vector<String> getEnabledTypes() {
        return (Vector<String>)enabledTypes;
    }
}
