public abstract class TextSyntax implements Serializable, Cloneable {
    private static final long serialVersionUID = -8130648736378144102L;
    private String value;
    private Locale locale;
    protected TextSyntax(String value, Locale locale) {
        this.value = verify (value);
        this.locale = verify (locale);
    }
    private static String verify(String value) {
        if (value == null) {
            throw new NullPointerException(" value is null");
        }
        return value;
    }
    private static Locale verify(Locale locale) {
        if (locale == null) {
            return Locale.getDefault();
        }
        return locale;
    }
    public String getValue() {
        return value;
    }
    public Locale getLocale() {
        return locale;
    }
    public int hashCode() {
        return value.hashCode() ^ locale.hashCode();
    }
    public boolean equals(Object object) {
        return(object != null &&
               object instanceof TextSyntax &&
               this.value.equals (((TextSyntax) object).value) &&
               this.locale.equals (((TextSyntax) object).locale));
    }
    public String toString(){
        return value;
    }
}
