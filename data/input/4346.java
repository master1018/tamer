public abstract class DecimalFormatSymbolsProvider extends LocaleServiceProvider {
    protected DecimalFormatSymbolsProvider() {
    }
    public abstract DecimalFormatSymbols getInstance(Locale locale);
}
