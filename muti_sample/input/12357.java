abstract class AttributeValue {
    private static final PlatformLogger log = PlatformLogger.getLogger("java.awt.AttributeValue");
    private final int value;
    private final String[] names;
    protected AttributeValue(int value, String[] names) {
        if (log.isLoggable(PlatformLogger.FINEST)) {
            log.finest("value = " + value + ", names = " + names);
        }
        if (log.isLoggable(PlatformLogger.FINER)) {
            if ((value < 0) || (names == null) || (value >= names.length)) {
                log.finer("Assertion failed");
            }
        }
        this.value = value;
        this.names = names;
    }
    public int hashCode() {
        return value;
    }
    public String toString() {
        return names[value];
    }
}
