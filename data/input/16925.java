public abstract class DateFormatSymbolsProvider extends LocaleServiceProvider {
    protected DateFormatSymbolsProvider() {
    }
    public abstract DateFormatSymbols getInstance(Locale locale);
}
