public abstract class CollatorProvider extends LocaleServiceProvider {
    protected CollatorProvider() {
    }
    public abstract Collator getInstance(Locale locale);
}
