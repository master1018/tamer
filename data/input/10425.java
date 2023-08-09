public abstract class DateTimeSyntax implements Serializable, Cloneable {
    private static final long serialVersionUID = -1400819079791208582L;
    private Date value;
    protected DateTimeSyntax(Date value) {
        if (value == null) {
            throw new NullPointerException("value is null");
        }
        this.value = value;
    }
    public Date getValue() {
        return new Date (value.getTime());
    }
    public boolean equals(Object object) {
        return (object != null &&
                object instanceof DateTimeSyntax &&
                value.equals(((DateTimeSyntax) object).value));
    }
    public int hashCode() {
        return value.hashCode();
    }
    public String toString() {
        return "" + value;
    }
}
