public class BreakIteratorProviderImpl extends BreakIteratorProvider {
    static Locale[] avail = {
        Locale.JAPAN,
        new Locale("ja", "JP", "osaka"),
        new Locale("ja", "JP", "kyoto"),
        new Locale("xx", "YY")};
    static String[] dialect = {
        "\u3067\u3059\u3002",
        "\u3084\u3002",
        "\u3069\u3059\u3002",
        "-xx-YY"
    };
    static enum Type {CHARACTER, LINE, SENTENCE, WORD};
    public Locale[] getAvailableLocales() {
        return avail;
    }
    public BreakIterator getCharacterInstance(Locale locale) {
        for (int i = 0; i < avail.length; i ++) {
            if (Utils.supportsLocale(avail[i], locale)) {
                return new FooBreakIterator(Type.CHARACTER, i);
            }
        }
        throw new IllegalArgumentException("locale is not supported: "+locale);
    }
    public BreakIterator getLineInstance(Locale locale) {
        for (int i = 0; i < avail.length; i ++) {
            if (Utils.supportsLocale(avail[i], locale)) {
                return new FooBreakIterator(Type.LINE, i);
            }
        }
        throw new IllegalArgumentException("locale is not supported: "+locale);
    }
    public BreakIterator getSentenceInstance(Locale locale) {
        for (int i = 0; i < avail.length; i ++) {
            if (Utils.supportsLocale(avail[i], locale)) {
                return new FooBreakIterator(Type.SENTENCE, i);
            }
        }
        throw new IllegalArgumentException("locale is not supported: "+locale);
    }
    public BreakIterator getWordInstance(Locale locale) {
        for (int i = 0; i < avail.length; i ++) {
            if (Utils.supportsLocale(avail[i], locale)) {
                return new FooBreakIterator(Type.WORD, i);
            }
        }
        throw new IllegalArgumentException("locale is not supported: "+locale);
    }
    class FooBreakIterator extends BreakIterator {
        public FooBreakIterator(Type t, int index) {
            super();
        }
        public int current() {
            return 0;
        }
        public int first() {
            return 0;
        }
        public int following(int offset) {
            return 0;
        }
        public CharacterIterator getText() {
            return null;
        }
        public boolean isBoundary(int offset) {
            return true;
        }
        public int last() {
            return 0;
        }
        public int next() {
            return 0;
        }
        public int next(int n) {
            return 0;
        }
        public int preceding(int offset) {
            return 0;
        }
        public int previous() {
            return 0;
        }
        public void setText(CharacterIterator ci) {
        }
    }
}
