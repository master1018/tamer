public abstract class NumberFormatProvider extends LocaleServiceProvider {
    protected NumberFormatProvider() {
    }
    public abstract NumberFormat getCurrencyInstance(Locale locale);
    public abstract NumberFormat getIntegerInstance(Locale locale);
    public abstract NumberFormat getNumberInstance(Locale locale);
    public abstract NumberFormat getPercentInstance(Locale locale);
}
