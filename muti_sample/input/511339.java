abstract public class Dictionary {
    protected static final boolean INCLUDE_TYPED_WORD_IF_VALID = false;
    protected static final int FULL_WORD_FREQ_MULTIPLIER = 2;
    public interface WordCallback {
        boolean addWord(char[] word, int wordOffset, int wordLength, int frequency);
    }
    abstract public void getWords(final WordComposer composer, final WordCallback callback,
            int[] nextLettersFrequencies);
    abstract public boolean isValidWord(CharSequence word);
    protected boolean same(final char[] word, final int length, final CharSequence typedWord) {
        if (typedWord.length() != length) {
            return false;
        }
        for (int i = 0; i < length; i++) {
            if (word[i] != typedWord.charAt(i)) {
                return false;
            }
        }
        return true;
    }
    public void close() {
    }
}
