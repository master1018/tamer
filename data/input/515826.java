public class OpenWnnClauseConverterJAJP {
    private static final int FREQ_LEARN = 600;
    private static final int FREQ_USER  = 500;
    public static final int MAX_INPUT_LENGTH = 50;
    private HashMap<String, ArrayList<WnnWord>> mIndepWordBag;
    private HashMap<String, ArrayList<WnnWord>> mAllIndepWordBag;
    private HashMap<String, ArrayList<WnnWord>> mFzkPatterns;
    private byte[][] mConnectMatrix;
    private WnnDictionary mDictionary;
    private LinkedList mConvertResult;
    private WnnSentence[] mSentenceBuffer;
    private WnnPOS mPosDefault;
    private WnnPOS mPosEndOfClause1;
    private WnnPOS mPosEndOfClause2;
    private WnnPOS mPosEndOfClause3;
    private static final int CLAUSE_COST = -1000;
    private CandidateFilter mFilter = null;
    public OpenWnnClauseConverterJAJP() {
        mIndepWordBag  = new HashMap<String, ArrayList<WnnWord>>();
        mAllIndepWordBag  = new HashMap<String, ArrayList<WnnWord>>();
        mFzkPatterns   = new HashMap();
        mConvertResult = new LinkedList();
        mSentenceBuffer = new WnnSentence[MAX_INPUT_LENGTH];
    }
    public void setDictionary(WnnDictionary dict) {
        mConnectMatrix = dict.getConnectMatrix();
        mDictionary = dict;
        dict.clearDictionary();
        dict.clearApproxPattern();                    
        mIndepWordBag.clear();
        mAllIndepWordBag.clear();
        mFzkPatterns.clear();
        mPosDefault      = dict.getPOS(WnnDictionary.POS_TYPE_MEISI);
        mPosEndOfClause1 = dict.getPOS(WnnDictionary.POS_TYPE_V1);
        mPosEndOfClause2 = dict.getPOS(WnnDictionary.POS_TYPE_V2);
        mPosEndOfClause3 = dict.getPOS(WnnDictionary.POS_TYPE_V3);
    }
    public void setFilter(CandidateFilter filter) {
    	mFilter = filter;
    }
     public Iterator convert(String input) {
        if (mConnectMatrix == null || mDictionary == null) {
            return null;
        }
        if (input.length() > MAX_INPUT_LENGTH) {
            return null;
        }
        mConvertResult.clear();
        if (!singleClauseConvert(mConvertResult, input, mPosEndOfClause2, true)) {
            return null;
        }
        return mConvertResult.iterator();
    }
    public WnnSentence consecutiveClauseConvert(String input) {
        LinkedList clauses = new LinkedList();
        for (int i = 0; i < input.length(); i++) {
            mSentenceBuffer[i] = null;
        }
        WnnSentence[] sentence = mSentenceBuffer;
        for (int start = 0; start < input.length(); start++) {
            if (start != 0 && sentence[start-1] == null) {
                continue;
            }
            int end = input.length();
            if (end > start + 20) {
                end = start + 20;
            }
            for ( ; end > start; end--) {
                int idx = end - 1;
                if (sentence[idx] != null) {
                    if (start != 0) {
                        if (sentence[idx].frequency > sentence[start-1].frequency + CLAUSE_COST + FREQ_LEARN) {
                            break;
                        }
                    } else {
                        if (sentence[idx].frequency > CLAUSE_COST + FREQ_LEARN) {
                            break;
                        }
                    }
                }
                String key = input.substring(start, end);
                clauses.clear();
                WnnClause bestClause = null;
                if (end == input.length()) {
                    singleClauseConvert(clauses, key, mPosEndOfClause1, false);
                } else {
                    singleClauseConvert(clauses, key, mPosEndOfClause3, false);
                }
                if (clauses.isEmpty()) {
                    bestClause = defaultClause(key);
                } else {
                    bestClause = (WnnClause)clauses.get(0);
                }
                WnnSentence ws;
                if (start == 0) {
                    ws = new WnnSentence(key, bestClause);
                } else {
                    ws = new WnnSentence(sentence[start-1], bestClause);
                }
                ws.frequency += CLAUSE_COST;
                if (sentence[idx] == null || (sentence[idx].frequency < ws.frequency)) {
                    sentence[idx] = ws;
                }
            }
        }
        if (sentence[input.length() - 1] != null) {
            return sentence[input.length() - 1];
        }
        return null;
    }
    private boolean consecutiveClauseConvert(LinkedList resultList, String input) {
        WnnSentence sentence = consecutiveClauseConvert(input);
        if (sentence != null) {
            resultList.add(0, sentence);
            return true;
        }
        return false;
    }
    private boolean singleClauseConvert(LinkedList clauseList, String input, WnnPOS terminal, boolean all) {
        boolean ret = false;
        ArrayList<WnnWord> stems = getIndependentWords(input, all);
        if (stems != null && (!stems.isEmpty())) {
            Iterator<WnnWord> stemsi = stems.iterator();
            while (stemsi.hasNext()) {
                WnnWord stem = stemsi.next();
                if (addClause(clauseList, input, stem, null, terminal, all)) {
                    ret = true;
                }
            }
        }
        int max = CLAUSE_COST * 2;
        for (int split = 1; split < input.length(); split++) {
            String str = input.substring(split);
            ArrayList<WnnWord> fzks = getAncillaryPattern(str);
            if (fzks == null || fzks.isEmpty()) {
                continue;
            }
            str = input.substring(0, split);
            stems = getIndependentWords(str, all);
            if (stems == null || stems.isEmpty()) {
                if (mDictionary.searchWord(WnnDictionary.SEARCH_PREFIX, WnnDictionary.ORDER_BY_FREQUENCY, str) <= 0) {
                    break;
                } else {
                    continue;
                }
            }
            Iterator<WnnWord> stemsi = stems.iterator();
            while (stemsi.hasNext()) {
                WnnWord stem = stemsi.next();
                if (all || stem.frequency > max) {
                    Iterator<WnnWord> fzksi  = fzks.iterator();
                    while (fzksi.hasNext()) {
                        WnnWord fzk = fzksi.next();
                        if (addClause(clauseList, input, stem, fzk, terminal, all)) {
                            ret = true;
                            max = stem.frequency;
                        }
                    }
                }
            }
        }
        return ret;
    }
    private boolean addClause(LinkedList<WnnClause> clauseList, String input, WnnWord stem, WnnWord fzk,
                              WnnPOS terminal, boolean all) {
        WnnClause clause = null;
        if (fzk == null) {
            if (connectible(stem.partOfSpeech.right, terminal.left)) {
                clause = new WnnClause(input, stem);
            }
        } else {
            if (connectible(stem.partOfSpeech.right, fzk.partOfSpeech.left)
                && connectible(fzk.partOfSpeech.right, terminal.left)) {
                clause = new WnnClause(input, stem, fzk);
            }
        }
        if (clause == null) {
            return false;
        }
        if (mFilter != null && !mFilter.isAllowed(clause)) {
        	return false;
        }
        if (clauseList.isEmpty()) {
            clauseList.add(0, clause);
            return true;
        } else {
            if (!all) {
                WnnClause best = (WnnClause)clauseList.get(0);
                if (best.frequency < clause.frequency) {
                    clauseList.set(0, clause);
                    return true;
                }
            } else {
                Iterator clauseListi = clauseList.iterator();
                int index = 0;
                while (clauseListi.hasNext()) {
                    WnnClause clausei = (WnnClause)clauseListi.next();
                    if (clausei.frequency < clause.frequency) {
                        break;
                    }
                    index++;
                }
                clauseList.add(index, clause);
                return true;
            }
        }
        return false;
    }
    private boolean connectible(int right, int left) {
        try {
            if (mConnectMatrix[left][right] != 0) {
                return true;
            }
        } catch (Exception ex) {
        }
        return false;
    }
    private ArrayList<WnnWord> getAncillaryPattern(String input) {
        if (input.length() == 0) {
            return null;
        }
        HashMap<String,ArrayList<WnnWord>> fzkPat = mFzkPatterns;
        ArrayList<WnnWord> fzks = fzkPat.get(input);
        if (fzks != null) {
            return fzks;
        }
        WnnDictionary dict = mDictionary;
        dict.clearDictionary();
        dict.clearApproxPattern();                    
        dict.setDictionary(6, 400, 500);
        for (int start = input.length() - 1; start >= 0; start--) {
            String key = input.substring(start);
            fzks = fzkPat.get(key);
            if (fzks != null) {
                continue;
            }
            fzks = new ArrayList<WnnWord>();
            mFzkPatterns.put(key, fzks);
            dict.searchWord(WnnDictionary.SEARCH_EXACT, WnnDictionary.ORDER_BY_FREQUENCY, key);
            WnnWord word;
            while ((word = dict.getNextWord()) != null) {
                fzks.add(word);
            }
            for (int end = input.length() - 1; end > start; end--) {
                ArrayList<WnnWord> followFzks = fzkPat.get(input.substring(end));
                if (followFzks == null ||  followFzks.isEmpty()) {
                    continue;
                }
                dict.searchWord(WnnDictionary.SEARCH_EXACT, WnnDictionary.ORDER_BY_FREQUENCY, input.substring(start, end));
                while ((word = dict.getNextWord()) != null) {
                    Iterator<WnnWord> followFzksi = followFzks.iterator();
                    while (followFzksi.hasNext()) {
                        WnnWord follow = followFzksi.next();
                        if (connectible(word.partOfSpeech.right, follow.partOfSpeech.left)) {
                            fzks.add(new WnnWord(key, key, new WnnPOS(word.partOfSpeech.left, follow.partOfSpeech.right)));
                        }
                    }
                }
            }
        }
        return fzks;
    }
    private ArrayList<WnnWord> getIndependentWords(String input, boolean all) {
        if (input.length() == 0) {
            return null;
        }
        ArrayList<WnnWord> words = (all)? mAllIndepWordBag.get(input) : mIndepWordBag.get(input);
        if (words == null) {
            WnnDictionary dict = mDictionary;
            dict.clearDictionary();
            dict.clearApproxPattern();                    
            dict.setDictionary(4, 0, 10);   
            dict.setDictionary(5, 400, 500);
            dict.setDictionary(WnnDictionary.INDEX_USER_DICTIONARY, FREQ_USER, FREQ_USER); 
            dict.setDictionary(WnnDictionary.INDEX_LEARN_DICTIONARY, FREQ_LEARN, FREQ_LEARN);
            words = new ArrayList<WnnWord>();
            WnnWord word;
            if (all) {
            	mAllIndepWordBag.put(input, words);
                dict.searchWord(WnnDictionary.SEARCH_EXACT, WnnDictionary.ORDER_BY_FREQUENCY, input);
                while ((word = dict.getNextWord()) != null) {
                    if (input.equals(word.stroke)) {
                        words.add(word);
                    }
                }
            } else {
            	mIndepWordBag.put(input, words);
                dict.searchWord(WnnDictionary.SEARCH_EXACT, WnnDictionary.ORDER_BY_FREQUENCY, input);
                while ((word = dict.getNextWord()) != null) {
                    if (input.equals(word.stroke)) {
                        Iterator<WnnWord> list = words.iterator();
                        boolean found = false;
                        while (list.hasNext()) {
                            WnnWord w = (WnnWord)list.next();
                                if (w.partOfSpeech.right == word.partOfSpeech.right) {
                                    found = true;
                                    break;
                                }
                        }
                        if (!found) {
                            words.add(word);
                        }
                        if (word.frequency < 400) {
                            break;
                        }
                    }
                }
            }
            addAutoGeneratedCandidates(input, words, all);
        }
        return words;
    }
    private void addAutoGeneratedCandidates(String input, ArrayList wordList, boolean all) {
        wordList.add(new WnnWord(input, input, mPosDefault, (CLAUSE_COST - 1) * input.length()));
    }
    private WnnClause defaultClause(String input) {
        return (new WnnClause(input, input, mPosDefault, (CLAUSE_COST - 1) * input.length()));
    }
}
