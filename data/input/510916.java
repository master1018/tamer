public class AutoText {
    private static final int TRIE_C = 0;
    private static final int TRIE_OFF = 1;
    private static final int TRIE_CHILD = 2;
    private static final int TRIE_NEXT = 3;
    private static final int TRIE_SIZEOF = 4;
    private static final char TRIE_NULL = (char) -1;
    private static final int TRIE_ROOT = 0;
    private static final int INCREMENT = 1024;
    private static final int DEFAULT = 14337; 
    private static final int RIGHT = 9300; 
    private static AutoText sInstance = new AutoText(Resources.getSystem());
    private static Object sLock = new Object();
    private char[] mTrie;
    private char mTrieUsed;
    private String mText;
    private Locale mLocale;
    private int mSize;
    private AutoText(Resources resources) {
        mLocale = resources.getConfiguration().locale;
        init(resources);
    }
    private static AutoText getInstance(View view) {
        Resources res = view.getContext().getResources();
        Locale locale = res.getConfiguration().locale;
        AutoText instance;
        synchronized (sLock) {
            instance = sInstance;
            if (!locale.equals(instance.mLocale)) {
                instance = new AutoText(res);
                sInstance = instance;
            }
        }
        return instance;
    }
    public static String get(CharSequence src, final int start, final int end,
                             View view) {
        return getInstance(view).lookup(src, start, end);
    }
    public static int getSize(View view) {
        return getInstance(view).getSize(); 
    }
    private int getSize() {
        return mSize;
    }
    private String lookup(CharSequence src, final int start, final int end) {
        int here = mTrie[TRIE_ROOT];
        for (int i = start; i < end; i++) {
            char c = src.charAt(i);
            for (; here != TRIE_NULL; here = mTrie[here + TRIE_NEXT]) {
                if (c == mTrie[here + TRIE_C]) {
                    if ((i == end - 1) 
                            && (mTrie[here + TRIE_OFF] != TRIE_NULL)) {
                        int off = mTrie[here + TRIE_OFF];
                        int len = mText.charAt(off);
                        return mText.substring(off + 1, off + 1 + len);
                    }
                    here = mTrie[here + TRIE_CHILD];
                    break;
                }
            }
            if (here == TRIE_NULL) {
                return null;
            }
        }
        return null;
    }
    private void init(Resources r) {
        XmlResourceParser parser = r.getXml(com.android.internal.R.xml.autotext);
        StringBuilder right = new StringBuilder(RIGHT);
        mTrie = new char[DEFAULT];
        mTrie[TRIE_ROOT] = TRIE_NULL;
        mTrieUsed = TRIE_ROOT + 1;
        try {
            XmlUtils.beginDocument(parser, "words");
            String odest = "";
            char ooff = 0;
            while (true) {
                XmlUtils.nextElement(parser);
                String element = parser.getName(); 
                if (element == null || !(element.equals("word"))) {
                    break;
                }
                String src = parser.getAttributeValue(null, "src");
                if (parser.next() == XmlPullParser.TEXT) {
                    String dest = parser.getText();
                    char off;
                    if (dest.equals(odest)) {
                        off = ooff;
                    } else {
                        off = (char) right.length();
                        right.append((char) dest.length());
                        right.append(dest);
                    }
                    add(src, off);
                }
            }
            r.flushLayoutCache();
        } catch (XmlPullParserException e) {
            throw new RuntimeException(e);
        } catch (IOException e) {
            throw new RuntimeException(e);
        } finally {
            parser.close();
        }
        mText = right.toString();
    }
    private void add(String src, char off) {
        int slen = src.length();
        int herep = TRIE_ROOT;
        mSize++;
        for (int i = 0; i < slen; i++) {
            char c = src.charAt(i);
            boolean found = false;
            for (; mTrie[herep] != TRIE_NULL;
                    herep = mTrie[herep] + TRIE_NEXT) {
                if (c == mTrie[mTrie[herep] + TRIE_C]) {
                    if (i == slen - 1) {
                        mTrie[mTrie[herep] + TRIE_OFF] = off;
                        return;
                    }
                    herep = mTrie[herep] + TRIE_CHILD;
                    found = true;
                    break;
                }
            }
            if (!found) {
                char node = newTrieNode();
                mTrie[herep] = node;
                mTrie[mTrie[herep] + TRIE_C] = c;
                mTrie[mTrie[herep] + TRIE_OFF] = TRIE_NULL;
                mTrie[mTrie[herep] + TRIE_NEXT] = TRIE_NULL;
                mTrie[mTrie[herep] + TRIE_CHILD] = TRIE_NULL;
                if (i == slen - 1) {
                    mTrie[mTrie[herep] + TRIE_OFF] = off;
                    return;
                }
                herep = mTrie[herep] + TRIE_CHILD;
            }
        }
    }
    private char newTrieNode() {
        if (mTrieUsed + TRIE_SIZEOF > mTrie.length) {
            char[] copy = new char[mTrie.length + INCREMENT];
            System.arraycopy(mTrie, 0, copy, 0, mTrie.length);
            mTrie = copy;
        }
        char ret = mTrieUsed;
        mTrieUsed += TRIE_SIZEOF;
        return ret;
    }
}
