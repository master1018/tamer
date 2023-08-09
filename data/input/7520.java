public class LanguageCallback implements Callback, java.io.Serializable {
    private static final long serialVersionUID = 2019050433478903213L;
    private Locale locale;
    public LanguageCallback() { }
    public void setLocale(Locale locale) {
        this.locale = locale;
    }
    public Locale getLocale() {
        return locale;
    }
}
