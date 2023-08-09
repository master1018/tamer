public class MissingResourceException extends RuntimeException {
    private static final long serialVersionUID = -4876345176062000401L;
    String className, key;
    public MissingResourceException(String detailMessage, String className,
            String resourceName) {
        super(detailMessage);
        this.className = className;
        key = resourceName;
    }
    public String getClassName() {
        return className;
    }
    public String getKey() {
        return key;
    }
}
