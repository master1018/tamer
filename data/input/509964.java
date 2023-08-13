public class WordComposer {
    private ArrayList<int[]> mCodes;
    private String mPreferredWord;
    private StringBuilder mTypedWord;
    private int mCapsCount;
    private boolean mAutoCapitalized;
    private boolean mIsCapitalized;
    WordComposer() {
        mCodes = new ArrayList<int[]>(12);
        mTypedWord = new StringBuilder(20);
    }
    public void reset() {
        mCodes.clear();
        mIsCapitalized = false;
        mPreferredWord = null;
        mTypedWord.setLength(0);
        mCapsCount = 0;
    }
    public int size() {
        return mCodes.size();
    }
    public int[] getCodesAt(int index) {
        return mCodes.get(index);
    }
    public void add(int primaryCode, int[] codes) {
        mTypedWord.append((char) primaryCode);
        correctPrimaryJuxtapos(primaryCode, codes);
        mCodes.add(codes);
        if (Character.isUpperCase((char) primaryCode)) mCapsCount++;
    }
    private void correctPrimaryJuxtapos(int primaryCode, int[] codes) {
        if (codes.length < 2) return;
        if (codes[0] > 0 && codes[1] > 0 && codes[0] != primaryCode && codes[1] == primaryCode) {
            codes[1] = codes[0];
            codes[0] = primaryCode;
        }
    }
    public void deleteLast() {
        mCodes.remove(mCodes.size() - 1);
        final int lastPos = mTypedWord.length() - 1;
        char last = mTypedWord.charAt(lastPos);
        mTypedWord.deleteCharAt(lastPos);
        if (Character.isUpperCase(last)) mCapsCount--;
    }
    public CharSequence getTypedWord() {
        int wordSize = mCodes.size();
        if (wordSize == 0) {
            return null;
        }
        return mTypedWord;
    }
    public void setCapitalized(boolean capitalized) {
        mIsCapitalized = capitalized;
    }
    public boolean isCapitalized() {
        return mIsCapitalized;
    }
    public void setPreferredWord(String preferred) {
        mPreferredWord = preferred;
    }
    public CharSequence getPreferredWord() {
        return mPreferredWord != null ? mPreferredWord : getTypedWord();
    }
    public boolean isMostlyCaps() {
        return mCapsCount > 1;
    }
    public void setAutoCapitalized(boolean auto) {
        mAutoCapitalized = auto;
    }
    public boolean isAutoCapitalized() {
        return mAutoCapitalized;
    }
}
