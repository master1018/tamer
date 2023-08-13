public class OpenWnnEngineEN implements WnnEngine {
    public static final int DICT_DEFAULT              = 0;
    public static final int DICT_FOR_CORRECT_MISTYPE  = 1;
    public static final int FREQ_LEARN = 600;
    public static final int FREQ_USER = 500;
    public static final int PREDICT_LIMIT = 300;
	private   WnnDictionary mDictionary;
    private ArrayList<WnnWord> mConvResult;
    private HashMap<String, WnnWord> mCandTable;
    private String        mInputString;
    private String        mSearchKey;
    private int           mOutputNum;
    private CandidateFilter mFilter = null;
    private int           mCandidateCase;
    private static final int CASE_LOWER = 0;
    private static final int CASE_UPPER = 1;
    private static final int CASE_HEAD_UPPER = 3;
    public OpenWnnEngineEN(String writableDictionaryName) {
        mConvResult = new ArrayList<WnnWord>();
        mCandTable = new HashMap<String, WnnWord>();
        mSearchKey = null;
        mOutputNum = 0;
        mDictionary = new OpenWnnDictionaryImpl( 
        		"/data/data/jp.co.omronsoft.openwnn/lib/libWnnEngDic.so",
        		writableDictionaryName);
        if (!mDictionary.isActive()) {
        	mDictionary = new OpenWnnDictionaryImpl(
        			"/system/lib/libWnnEngDic.so",
        			writableDictionaryName);
        }
        mDictionary.clearDictionary( );
        mDictionary.setDictionary(0, 400, 550);
        mDictionary.setDictionary(1, 400, 550);
        mDictionary.setDictionary(2, 400, 550);
        mDictionary.setDictionary(WnnDictionary.INDEX_USER_DICTIONARY, FREQ_USER, FREQ_USER);
        mDictionary.setDictionary(WnnDictionary.INDEX_LEARN_DICTIONARY, FREQ_LEARN, FREQ_LEARN);
        mDictionary.setApproxPattern(WnnDictionary.APPROX_PATTERN_EN_QWERTY_NEAR);
        mDictionary.setInUseState( false );
    }
    private WnnWord getCandidate(int index) {
        WnnWord word;
        while (mConvResult.size() < PREDICT_LIMIT && index >= mConvResult.size()) {
            while ((word = mDictionary.getNextWord()) != null) {
                char c = word.candidate.charAt(0);
                if (mCandidateCase == CASE_LOWER) {
                    if (Character.isLowerCase(c)) {
                        break;
                    }
                } else if (mCandidateCase == CASE_HEAD_UPPER) {
                    if (Character.isLowerCase(c)) {
                        word.candidate = Character.toString(Character.toUpperCase(c)) + word.candidate.substring(1);
                    }
                    break;
                } else {
                    word.candidate = word.candidate.toUpperCase();
                    break;
                }
            }
            if (word == null) {
                break;
            }
            addCandidate(word);
        }
        if (index >= mConvResult.size()) {
            addCandidate(new WnnWord(mInputString, mSearchKey));
            if (mSearchKey.length() > 1) {
                addCandidate(new WnnWord(mSearchKey.substring(0,1).toUpperCase() + mSearchKey.substring(1),
                                         mSearchKey));
            }
            addCandidate(new WnnWord(mSearchKey.toUpperCase(), mSearchKey));
        }
        if (index >= mConvResult.size()) {
            return null;
        }
        return mConvResult.get(index);
    }
    private boolean addCandidate(WnnWord word) {
        if (word.candidate == null || mCandTable.containsKey(word.candidate)) {
            return false;
        }
        if (mFilter != null && !mFilter.isAllowed(word)) {
        	return false;
        }
        mCandTable.put(word.candidate, word);
        mConvResult.add(word);
        return true;
    }
    private void clearCandidates() {
        mConvResult.clear();
        mCandTable.clear();
        mOutputNum = 0;
        mSearchKey = null;
    }
    public boolean setDictionary(int type) {
        if (type == DICT_FOR_CORRECT_MISTYPE) {
            mDictionary.clearApproxPattern();
            mDictionary.setApproxPattern(WnnDictionary.APPROX_PATTERN_EN_QWERTY_NEAR);
        } else {
            mDictionary.clearApproxPattern();
        }
        return true;
    }
    private boolean setSearchKey(String input) {
        if (input.length() == 0) {
            return false;
        }
        mInputString = input;
        mSearchKey = input.toLowerCase();
        if (Character.isUpperCase(input.charAt(0))) {
            if (input.length() > 1 && Character.isUpperCase(input.charAt(1))) {
                mCandidateCase = CASE_UPPER;
            } else {
                mCandidateCase = CASE_HEAD_UPPER;
            }
        } else {
            mCandidateCase = CASE_LOWER;
        }
        return true;
    }
    public void setFilter(CandidateFilter filter) {
    	mFilter = filter;
    }
    public void init() {}
    public void close() {}
    public int predict(ComposingText text, int minLen, int maxLen) {
        clearCandidates();
        if (text == null) { return 0; }
        String input = text.toString(2);
        if (!setSearchKey(input)) {
            return 0;
        }
        WnnDictionary dict = mDictionary;
        dict.setInUseState( true );
        dict.clearDictionary();
        dict.setDictionary(0, 400, 550);
        if (input.length() > 1) {
            dict.setDictionary(1, 400, 550);
        }
        if (input.length() > 2) {
            dict.setDictionary(2, 400, 550);
        }
        dict.setDictionary(WnnDictionary.INDEX_USER_DICTIONARY, FREQ_USER, FREQ_USER);
        dict.setDictionary(WnnDictionary.INDEX_LEARN_DICTIONARY, FREQ_LEARN, FREQ_LEARN);
        dict.searchWord(WnnDictionary.SEARCH_PREFIX, WnnDictionary.ORDER_BY_FREQUENCY, mSearchKey);
        return 1;
    }
    public int convert(ComposingText text) {
        clearCandidates();
        return 0;
    }
    public int searchWords(String key) {
        clearCandidates();
        return 0;
    }
    public int searchWords(WnnWord word) {
        clearCandidates();
        return 0;
    }
    public WnnWord getNextCandidate() {
        if (mSearchKey == null) {
            return null;
        }
        WnnWord word = getCandidate(mOutputNum);
        if (word != null) {
            mOutputNum++;
        }
        return word;
    }
    public boolean learn(WnnWord word) {
        return ( mDictionary.learnWord(word) == 0 );
    }
    public int addWord(WnnWord word) {
        WnnDictionary dict = mDictionary;
        dict.setInUseState( true );
        dict.addWordToUserDictionary(word);
        dict.setInUseState( false );
        return 0;
    }
    public boolean deleteWord(WnnWord word) {
        WnnDictionary dict = mDictionary;
        dict.setInUseState( true );
        dict.removeWordFromUserDictionary(word);
        dict.setInUseState( false );
        return false;
    }
    public void setPreferences(SharedPreferences pref) {}
    public void breakSequence()  {}
    public int makeCandidateListOf(int clausePosition)  {return 0;}
    public boolean initializeDictionary(int dictionary)  {
        WnnDictionary dict = mDictionary;
        switch( dictionary ) {
        case WnnEngine.DICTIONARY_TYPE_LEARN:
            dict.setInUseState( true );
            dict.clearLearnDictionary();
            dict.setInUseState( false );
            return true;
        case WnnEngine.DICTIONARY_TYPE_USER:
            dict.setInUseState( true );
            dict.clearUserDictionary();
            dict.setInUseState( false );
            return true;
        }
        return false;
    }
    public boolean initializeDictionary(int dictionary, int type) {
    	return initializeDictionary(dictionary);
    }
    public WnnWord[] getUserDictionaryWords( ) {
        WnnDictionary dict = mDictionary;
        dict.setInUseState( true );
        WnnWord[] result = dict.getUserDictionaryWords( );
        dict.setInUseState( false );
        return result;
    }
}
