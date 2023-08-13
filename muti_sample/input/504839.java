public final class GDataParser implements ContentHandler {
    public static final String APP_NAMESPACE = "http:
    public static final String ATOM_NAMESPACE = "http:
    public static final String GD_NAMESPACE = "http:
    public static final String GPHOTO_NAMESPACE = "http:
    public static final String MEDIA_RSS_NAMESPACE = "http:
    public static final String GML_NAMESPACE = "http:
    private static final String FEED_ELEMENT = "feed";
    private static final String ENTRY_ELEMENT = "entry";
    private static final int STATE_DOCUMENT = 0;
    private static final int STATE_FEED = 1;
    private static final int STATE_ENTRY = 2;
    private static final int NUM_LEVELS = 5;
    private Entry mEntry = null;
    private EntryHandler mHandler = null;
    private int mState = STATE_DOCUMENT;
    private int mLevel = 0;
    private String[] mUri = new String[NUM_LEVELS];
    private String[] mName = new String[NUM_LEVELS];
    private AttributesImpl[] mAttributes = new AttributesImpl[NUM_LEVELS];
    private final StringBuilder mValue = new StringBuilder(128);
    public interface EntryHandler {
        void handleEntry(Entry entry);
    }
    public GDataParser() {
        AttributesImpl[] attributes = mAttributes;
        for (int i = 0; i != NUM_LEVELS; ++i) {
            attributes[i] = new AttributesImpl();
        }
    }
    public void setEntry(Entry entry) {
        mEntry = entry;
    }
    public void setHandler(EntryHandler handler) {
        mHandler = handler;
    }
    public static long parseAtomTimestamp(String timestamp) {
        Time time = new Time();
        time.parse3339(timestamp);
        return time.toMillis(true);
    }
    public void startElement(String uri, String localName, String qName, Attributes attrs) throws SAXException {
        switch (mState) {
        case STATE_DOCUMENT:
            if (uri.equals(ATOM_NAMESPACE) && localName.equals(FEED_ELEMENT)) {
                mState = STATE_FEED;
            } else {
                throw new SAXException();
            }
            break;
        case STATE_FEED:
            if (uri.equals(ATOM_NAMESPACE) && localName.equals(ENTRY_ELEMENT)) {
                mState = STATE_ENTRY;
                mEntry.clear();
            } else {
                startProperty(uri, localName, attrs);
            }
            break;
        case STATE_ENTRY:
            startProperty(uri, localName, attrs);
            break;
        }
    }
    public void endElement(String uri, String localName, String qName) throws SAXException {
        if (mLevel > 0) {
            endProperty();
        } else {
            switch (mState) {
            case STATE_DOCUMENT:
                throw new SAXException();
            case STATE_FEED:
                mState = STATE_DOCUMENT;
                break;
            case STATE_ENTRY:
                mState = STATE_FEED;
                mHandler.handleEntry(mEntry);
                break;
            }
        }
    }
    private void startProperty(String uri, String localName, Attributes attrs) {
        int level = mLevel + 1;
        mLevel = level;
        mValue.setLength(0);
        mUri[level] = uri;
        mName[level] = localName;
        mAttributes[level].setAttributes(attrs);
    }
    private void endProperty() {
        int level = mLevel;
        mEntry.setPropertyFromXml(mUri[level], mName[level], mAttributes[level], mValue.toString());
        mLevel = level - 1;
    }
    public void characters(char[] ch, int start, int length) throws SAXException {
        mValue.append(ch, start, length);
    }
    public void ignorableWhitespace(char[] ch, int start, int length) throws SAXException {
    }
    public void processingInstruction(String target, String data) throws SAXException {
    }
    public void setDocumentLocator(Locator locator) {
    }
    public void skippedEntity(String name) throws SAXException {
    }
    public void startDocument() throws SAXException {
    }
    public void endDocument() throws SAXException {
    }
    public void startPrefixMapping(String prefix, String uri) throws SAXException {
    }
    public void endPrefixMapping(String prefix) throws SAXException {
    }
}
