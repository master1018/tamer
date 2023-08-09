public class Suggest implements Dictionary.WordCallback {
    public static final int CORRECTION_NONE = 0;
    public static final int CORRECTION_BASIC = 1;
    public static final int CORRECTION_FULL = 2;
    static final int LARGE_DICTIONARY_THRESHOLD = 200 * 1000;
    private BinaryDictionary mMainDict;
    private Dictionary mUserDictionary;
    private Dictionary mAutoDictionary;
    private Dictionary mContactsDictionary;
    private int mPrefMaxSuggestions = 12;
    private boolean mAutoTextEnabled;
    private int[] mPriorities = new int[mPrefMaxSuggestions];
    private int[] mNextLettersFrequencies = new int[1280];
    private ArrayList<CharSequence> mSuggestions = new ArrayList<CharSequence>();
    private ArrayList<CharSequence> mStringPool = new ArrayList<CharSequence>();
    private boolean mHaveCorrection;
    private CharSequence mOriginalWord;
    private String mLowerOriginalWord;
    private boolean mCapitalize;
    private int mCorrectionMode = CORRECTION_BASIC;
    public Suggest(Context context, int dictionaryResId) {
        mMainDict = new BinaryDictionary(context, dictionaryResId);
        for (int i = 0; i < mPrefMaxSuggestions; i++) {
            StringBuilder sb = new StringBuilder(32);
            mStringPool.add(sb);
        }
    }
    public void setAutoTextEnabled(boolean enabled) {
        mAutoTextEnabled = enabled;
    }
    public int getCorrectionMode() {
        return mCorrectionMode;
    }
    public void setCorrectionMode(int mode) {
        mCorrectionMode = mode;
    }
    public boolean hasMainDictionary() {
        return mMainDict.getSize() > LARGE_DICTIONARY_THRESHOLD;
    }
    public void setUserDictionary(Dictionary userDictionary) {
        mUserDictionary = userDictionary;
    }
    public void setContactsDictionary(Dictionary userDictionary) {
        mContactsDictionary = userDictionary;
    }
    public void setAutoDictionary(Dictionary autoDictionary) {
        mAutoDictionary = autoDictionary;
    }
    public void setMaxSuggestions(int maxSuggestions) {
        if (maxSuggestions < 1 || maxSuggestions > 100) {
            throw new IllegalArgumentException("maxSuggestions must be between 1 and 100");
        }
        mPrefMaxSuggestions = maxSuggestions;
        mPriorities = new int[mPrefMaxSuggestions];
        collectGarbage();
        while (mStringPool.size() < mPrefMaxSuggestions) {
            StringBuilder sb = new StringBuilder(32);
            mStringPool.add(sb);
        }
    }
    private boolean haveSufficientCommonality(String original, CharSequence suggestion) {
        final int originalLength = original.length();
        final int suggestionLength = suggestion.length();
        final int minLength = Math.min(originalLength, suggestionLength);
        if (minLength <= 2) return true;
        int matching = 0;
        int lessMatching = 0; 
        int i;
        for (i = 0; i < minLength; i++) {
            final char origChar = ExpandableDictionary.toLowerCase(original.charAt(i));
            if (origChar == ExpandableDictionary.toLowerCase(suggestion.charAt(i))) {
                matching++;
                lessMatching++;
            } else if (i + 1 < suggestionLength
                    && origChar == ExpandableDictionary.toLowerCase(suggestion.charAt(i + 1))) {
                lessMatching++;
            }
        }
        matching = Math.max(matching, lessMatching);
        if (minLength <= 4) {
            return matching >= 2;
        } else {
            return matching > minLength / 2;
        }
    }
    public List<CharSequence> getSuggestions(View view, WordComposer wordComposer, 
            boolean includeTypedWordIfValid) {
        mHaveCorrection = false;
        mCapitalize = wordComposer.isCapitalized();
        collectGarbage();
        Arrays.fill(mPriorities, 0);
        Arrays.fill(mNextLettersFrequencies, 0);
        mOriginalWord = wordComposer.getTypedWord();
        if (mOriginalWord != null) {
            mOriginalWord = mOriginalWord.toString();
            mLowerOriginalWord = mOriginalWord.toString().toLowerCase();
        } else {
            mLowerOriginalWord = "";
        }
        if (wordComposer.size() > 1) {
            if (mUserDictionary != null || mContactsDictionary != null) {
                if (mUserDictionary != null) {
                    mUserDictionary.getWords(wordComposer, this, mNextLettersFrequencies);
                }
                if (mContactsDictionary != null) {
                    mContactsDictionary.getWords(wordComposer, this, mNextLettersFrequencies);
                }
                if (mSuggestions.size() > 0 && isValidWord(mOriginalWord)
                        && mCorrectionMode == CORRECTION_FULL) {
                    mHaveCorrection = true;
                }
            }
            mMainDict.getWords(wordComposer, this, mNextLettersFrequencies);
            if (mCorrectionMode == CORRECTION_FULL && mSuggestions.size() > 0) {
                mHaveCorrection = true;
            }
        }
        if (mOriginalWord != null) {
            mSuggestions.add(0, mOriginalWord.toString());
        }
        if (mCorrectionMode == CORRECTION_FULL && mSuggestions.size() > 1) {
            if (!haveSufficientCommonality(mLowerOriginalWord, mSuggestions.get(1))) {
                mHaveCorrection = false;
            }
        }
        if (mAutoTextEnabled) {
            int i = 0;
            int max = 6;
            if (mCorrectionMode == CORRECTION_BASIC) max = 1;
            while (i < mSuggestions.size() && i < max) {
                String suggestedWord = mSuggestions.get(i).toString().toLowerCase();
                CharSequence autoText =
                        AutoText.get(suggestedWord, 0, suggestedWord.length(), view);
                boolean canAdd = autoText != null;
                canAdd &= !TextUtils.equals(autoText, mSuggestions.get(i));
                if (canAdd && i + 1 < mSuggestions.size() && mCorrectionMode != CORRECTION_BASIC) {
                    canAdd &= !TextUtils.equals(autoText, mSuggestions.get(i + 1));
                }
                if (canAdd) {
                    mHaveCorrection = true;
                    mSuggestions.add(i + 1, autoText);
                    i++;
                }
                i++;
            }
        }
        removeDupes();
        return mSuggestions;
    }
    public int[] getNextLettersFrequencies() {
        return mNextLettersFrequencies;
    }
    private void removeDupes() {
        final ArrayList<CharSequence> suggestions = mSuggestions;
        if (suggestions.size() < 2) return;
        int i = 1;
        while (i < suggestions.size()) {
            final CharSequence cur = suggestions.get(i);
            for (int j = 0; j < i; j++) {
                CharSequence previous = suggestions.get(j);
                if (TextUtils.equals(cur, previous)) {
                    removeFromSuggestions(i);
                    i--;
                    break;
                }
            }
            i++;
        }
    }
    private void removeFromSuggestions(int index) {
        CharSequence garbage = mSuggestions.remove(index);
        if (garbage != null && garbage instanceof StringBuilder) {
            mStringPool.add(garbage);
        }
    }
    public boolean hasMinimalCorrection() {
        return mHaveCorrection;
    }
    private boolean compareCaseInsensitive(final String mLowerOriginalWord, 
            final char[] word, final int offset, final int length) {
        final int originalLength = mLowerOriginalWord.length();
        if (originalLength == length && Character.isUpperCase(word[offset])) {
            for (int i = 0; i < originalLength; i++) {
                if (mLowerOriginalWord.charAt(i) != Character.toLowerCase(word[offset+i])) {
                    return false;
                }
            }
            return true;
        }
        return false;
    }
    public boolean addWord(final char[] word, final int offset, final int length, final int freq) {
        int pos = 0;
        final int[] priorities = mPriorities;
        final int prefMaxSuggestions = mPrefMaxSuggestions;
        if (compareCaseInsensitive(mLowerOriginalWord, word, offset, length)) {
            pos = 0;
        } else {
            if (priorities[prefMaxSuggestions - 1] >= freq) return true;
            while (pos < prefMaxSuggestions) {
                if (priorities[pos] < freq
                        || (priorities[pos] == freq && length < mSuggestions
                                .get(pos).length())) {
                    break;
                }
                pos++;
            }
        }
        if (pos >= prefMaxSuggestions) {
            return true;
        }
        System.arraycopy(priorities, pos, priorities, pos + 1,
                prefMaxSuggestions - pos - 1);
        priorities[pos] = freq;
        int poolSize = mStringPool.size();
        StringBuilder sb = poolSize > 0 ? (StringBuilder) mStringPool.remove(poolSize - 1) 
                : new StringBuilder(32);
        sb.setLength(0);
        if (mCapitalize) {
            sb.append(Character.toUpperCase(word[offset]));
            if (length > 1) {
                sb.append(word, offset + 1, length - 1);
            }
        } else {
            sb.append(word, offset, length);
        }
        mSuggestions.add(pos, sb);
        if (mSuggestions.size() > prefMaxSuggestions) {
            CharSequence garbage = mSuggestions.remove(prefMaxSuggestions);
            if (garbage instanceof StringBuilder) {
                mStringPool.add(garbage);
            }
        }
        return true;
    }
    public boolean isValidWord(final CharSequence word) {
        if (word == null || word.length() == 0) {
            return false;
        }
        return mMainDict.isValidWord(word)
                || (mUserDictionary != null && mUserDictionary.isValidWord(word))
                || (mAutoDictionary != null && mAutoDictionary.isValidWord(word))
                || (mContactsDictionary != null && mContactsDictionary.isValidWord(word));
    }
    private void collectGarbage() {
        int poolSize = mStringPool.size();
        int garbageSize = mSuggestions.size();
        while (poolSize < mPrefMaxSuggestions && garbageSize > 0) {
            CharSequence garbage = mSuggestions.get(garbageSize - 1);
            if (garbage != null && garbage instanceof StringBuilder) {
                mStringPool.add(garbage);
                poolSize++;
            }
            garbageSize--;
        }
        if (poolSize == mPrefMaxSuggestions + 1) {
            Log.w("Suggest", "String pool got too big: " + poolSize);
        }
        mSuggestions.clear();
    }
    public void close() {
        if (mMainDict != null) {
            mMainDict.close();
        }
    }
}
