public final class Boolean implements Serializable, Comparable<Boolean> {
    private static final long serialVersionUID = -3665804199014368530L;
    private final boolean value;
    @SuppressWarnings("unchecked")
    public static final Class<Boolean> TYPE
             = (Class<Boolean>) boolean[].class.getComponentType();
    public static final Boolean TRUE = new Boolean(true);
    public static final Boolean FALSE = new Boolean(false);
    public Boolean(String string) {
        this(parseBoolean(string));
    }
    public Boolean(boolean value) {
        this.value = value;
    }
    public boolean booleanValue() {
        return value;
    }
    @Override
    public boolean equals(Object o) {
        return (o == this)
                || ((o instanceof Boolean) && (value == ((Boolean) o).value));
    }
    public int compareTo(Boolean that) {
        return value == that.value ? 0 : value ? 1 : -1;
    }
    @Override
    public int hashCode() {
        return value ? 1231 : 1237;
    }
    @Override
    public String toString() {
        return String.valueOf(value);
    }
    public static boolean getBoolean(String string) {
        if (string == null || string.length() == 0) {
            return false;
        }
        return (parseBoolean(System.getProperty(string)));
    }
    public static boolean parseBoolean(String s) {
        return "true".equalsIgnoreCase(s); 
    }
    public static String toString(boolean value) {
        return String.valueOf(value);
    }
    public static Boolean valueOf(String string) {
        return parseBoolean(string) ? Boolean.TRUE : Boolean.FALSE;
    }
    public static Boolean valueOf(boolean b) {
        return b ? Boolean.TRUE : Boolean.FALSE;
    }
}
