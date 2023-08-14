public class SymbolList implements WnnEngine {
    public static final int LANG_EN = 0;
    public static final int LANG_JA = 1;
    public static final int LANG_ZHCN = 2;
    public static final String SYMBOL_JAPANESE = "j";
    public static final String SYMBOL_ENGLISH = "e";
    public static final String SYMBOL_CHINESE = "c1";
    public static final String SYMBOL_JAPANESE_FACE  = "j_face";
    private static final String XMLTAG_KEY = "string";
    protected HashMap<String,ArrayList<String>> mSymbols;
    private OpenWnn mWnn;
    private ArrayList<String> mCurrentList;
    private Iterator<String> mCurrentListIterator;
    public SymbolList(OpenWnn parent, int lang) {
        mWnn = parent;
        mSymbols = new HashMap<String, ArrayList<String>>();
        switch (lang) {
        case LANG_EN:
            mSymbols.put(SYMBOL_ENGLISH, getXmlfile(R.xml.symbols_latin12_list));
            mCurrentList = mSymbols.get(SYMBOL_ENGLISH);
            break;
        case LANG_JA:
            mSymbols.put(SYMBOL_ENGLISH, getXmlfile(R.xml.symbols_latin1_list));
            mSymbols.put(SYMBOL_JAPANESE, getXmlfile(R.xml.symbols_japan_list));
            mSymbols.put(SYMBOL_JAPANESE_FACE, getXmlfile(R.xml.symbols_japan_face_list));
            mCurrentList = mSymbols.get(SYMBOL_ENGLISH);
            break;
        case LANG_ZHCN: 
            mSymbols.put(SYMBOL_CHINESE, getXmlfile(R.xml.symbols_china_list));
            mSymbols.put(SYMBOL_ENGLISH, getXmlfile(R.xml.symbols_latin1_list));
            mCurrentList = mSymbols.get(SYMBOL_CHINESE);
            break;
        }        
        mCurrentList = null;
    }
    private String getXmlAttribute(XmlResourceParser xrp, String name) {
        int resId = xrp.getAttributeResourceValue(null, name, 0);
        if (resId == 0) {
            return xrp.getAttributeValue(null, name);
        } else {
            return mWnn.getString(resId);
        }
    }
    private ArrayList<String> getXmlfile(int id) {
        ArrayList<String> list = new ArrayList<String>();
        XmlResourceParser xrp = mWnn.getResources().getXml(id);
        try {
            int xmlEventType;
            while ((xmlEventType = xrp.next()) != XmlResourceParser.END_DOCUMENT) {
                if (xmlEventType == XmlResourceParser.START_TAG) {
                    String attribute = xrp.getName();
                    if (XMLTAG_KEY.equals(attribute)) {
                        String value = getXmlAttribute(xrp, "value");
                        if (value != null) {
                            list.add(value);
                        }
                    }
                }
            }
            xrp.close();
        } catch (XmlPullParserException e) {
            Log.e("OpenWnn", "Ill-formatted keybaord resource file");
        } catch (IOException e) {
            Log.e("OpenWnn", "Unable to read keyboard resource file");
        }
        return list;
    }
    public boolean setDictionary(String listType) {
        mCurrentList = mSymbols.get(listType);
        return (mCurrentList != null);
    }
    public void init() {}
    public void close() {}
    public int predict(ComposingText text, int minLen, int maxLen) {
        if (mCurrentList == null) {
            mCurrentListIterator = null;
            return 0;
        }
        mCurrentListIterator = mCurrentList.iterator();
        return 1;
    }
    public int convert(ComposingText text) {
        return 0;
    }
    public int searchWords(String key) {return 0;}
    public int searchWords(WnnWord word) {return 0;}
    public WnnWord getNextCandidate() {
        if (mCurrentListIterator == null || !mCurrentListIterator.hasNext()) {
            return null;
        }
        String str = mCurrentListIterator.next();
        WnnWord word = new WnnWord(str, str);
        return word;
    }
    public boolean learn(WnnWord word) {return false;}
    public int addWord(WnnWord word) {return 0;}
    public boolean deleteWord(WnnWord word) {return false;}
    public void setPreferences(SharedPreferences pref) {}
    public void breakSequence() {}
    public int makeCandidateListOf(int clausePosition) {return 0;}
    public boolean initializeDictionary(int dictionary) {return true;}
    public boolean initializeDictionary(int dictionary, int type) {return true;}
    public WnnWord[] getUserDictionaryWords() {return null;}
}
