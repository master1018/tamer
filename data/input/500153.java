public abstract class BreakIterator implements Cloneable
{
    protected static final int BI_CHAR_INSTANCE = 1;
    protected static final int BI_WORD_INSTANCE = 2;
    protected static final int BI_LINE_INSTANCE = 3;
    protected static final int BI_SENT_INSTANCE = 4;
    protected int type = 0;
    public static Locale[] getAvailableLocales() {
        String[] locales = NativeBreakIterator.getAvailableLocalesImpl();
        Locale[] result = new Locale[locales.length];
        String locale;
        int index, index2;
        for(int i = 0; i < locales.length; i++) {
            locale = locales[i];
            index = locale.indexOf('_');
            index2 = locale.lastIndexOf('_');
            if(index == -1) {
                result[i] = new Locale(locales[i]);
            } else if(index > 0 && index == index2) {
                result[i] = new Locale(
                        locale.substring(0,index),
                        locale.substring(index+1));
            } else if(index > 0 && index2 > index) {
                result[i] = new Locale(
                        locale.substring(0,index),
                        locale.substring(index+1,index2),
                        locale.substring(index2+1));
            }
        }
        return result;
    }
    public static BreakIterator getCharacterInstance() {
        int iter = NativeBreakIterator.getCharacterInstanceImpl("");
        return new RuleBasedBreakIterator(iter, BI_CHAR_INSTANCE);
    }
    public static BreakIterator getCharacterInstance(Locale where) {
        int iter = NativeBreakIterator.getCharacterInstanceImpl(where.toString());
        return new RuleBasedBreakIterator(iter, BI_CHAR_INSTANCE);
    }
    public static BreakIterator getLineInstance() {
        int iter = NativeBreakIterator.getLineInstanceImpl("");
        return new RuleBasedBreakIterator(iter, BI_LINE_INSTANCE);
    }
    public static BreakIterator getLineInstance(Locale where) {
        int iter = NativeBreakIterator.getLineInstanceImpl(where.toString());
        return new RuleBasedBreakIterator(iter, BI_LINE_INSTANCE);
    }
    public static BreakIterator getSentenceInstance() {
        int iter = NativeBreakIterator.getSentenceInstanceImpl("");
        return new RuleBasedBreakIterator(iter, BI_SENT_INSTANCE);
    }
    public static BreakIterator getSentenceInstance(Locale where) {
        int iter = NativeBreakIterator.getSentenceInstanceImpl(where.toString());
        return new RuleBasedBreakIterator(iter, BI_SENT_INSTANCE);
    }
    public static BreakIterator getWordInstance() {
        int iter = NativeBreakIterator.getWordInstanceImpl("");
        return new RuleBasedBreakIterator(iter, BI_WORD_INSTANCE);
    }
    public static BreakIterator getWordInstance(Locale where) {
        int iter = NativeBreakIterator.getWordInstanceImpl(where.toString());
        return new RuleBasedBreakIterator(iter, BI_WORD_INSTANCE);
    }
    public void setText(String newText) {
        setText(new StringCharacterIterator(newText));
    }
    public abstract boolean isBoundary(int offset);
    public abstract int preceding(int offset);
    public abstract Object clone();
    public abstract int current();
    public abstract int first();
    public abstract int following(int offset);
    public abstract CharacterIterator getText();
    public abstract int last();
    public abstract int next(int n);
    public abstract int next();
    public abstract int previous();
    public abstract void setText(CharacterIterator newText);
}
