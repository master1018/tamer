public abstract class BreakIteratorProvider extends LocaleServiceProvider {
    protected BreakIteratorProvider() {
    }
    public abstract BreakIterator getWordInstance(Locale locale);
    public abstract BreakIterator getLineInstance(Locale locale);
    public abstract BreakIterator getCharacterInstance(Locale locale);
    public abstract BreakIterator getSentenceInstance(Locale locale);
}
