public class DefaultDataHandler implements ContentInsertHandler {
    private final static String ROW = "row";
    private final static String COL = "col";
    private final static String URI_STR = "uri";
    private final static String POSTFIX = "postfix";
    private final static String DEL = "del";
    private final static String SELECT = "select";
    private final static String ARG = "arg";   
    private Stack<Uri> mUris = new Stack<Uri>();
    private ContentValues mValues;
    private ContentResolver mContentResolver;   
    public void insert(ContentResolver contentResolver, InputStream in)
            throws IOException, SAXException {
        mContentResolver = contentResolver;
        Xml.parse(in, Xml.Encoding.UTF_8, this);
    }
    public void insert(ContentResolver contentResolver, String in)
        throws SAXException {
        mContentResolver = contentResolver;
        Xml.parse(in, this);
    }
    private void parseRow(Attributes atts) throws SAXException {
        String uriStr = atts.getValue(URI_STR);
        Uri uri;
        if (uriStr != null) {
            uri = Uri.parse(uriStr);
            if (uri == null) {
                throw new SAXException("attribute " +
                        atts.getValue(URI_STR) + " parsing failure"); 
            }
        } else if (mUris.size() > 0){
            String postfix = atts.getValue(POSTFIX);
            if (postfix != null) {
                uri = Uri.withAppendedPath(mUris.lastElement(),
                        postfix);
            } else {
                uri = mUris.lastElement();
            } 
        } else {
            throw new SAXException("attribute parsing failure"); 
        }
        mUris.push(uri);
    }
    private Uri insertRow() {
        Uri u = mContentResolver.insert(mUris.lastElement(), mValues);
        mValues = null;
        return u;
    }
    public void startElement(String uri, String localName, String name,
            Attributes atts) throws SAXException {
        if (ROW.equals(localName)) {            
            if (mValues != null) {
                if (mUris.empty()) {
                    throw new SAXException("uri is empty");
                }
                Uri nextUri = insertRow();
                if (nextUri == null) {
                    throw new SAXException("insert to uri " + 
                            mUris.lastElement().toString() + " failure");
                } else {
                    mUris.pop();
                    mUris.push(nextUri);
                    parseRow(atts);
                }
            } else {
                int attrLen = atts.getLength();
                if (attrLen == 0) {
                    mUris.push(mUris.lastElement());
                } else {
                    parseRow(atts);
                }
            }                
        } else if (COL.equals(localName)) {
            int attrLen = atts.getLength();
            if (attrLen != 2) {
                throw new SAXException("illegal attributes number " + attrLen);
            }
            String key = atts.getValue(0);
            String value = atts.getValue(1);
            if (key != null && key.length() > 0 && value != null && value.length() > 0) {
                if (mValues == null) {
                    mValues = new ContentValues();
                }
                mValues.put(key, value);
            } else {
                throw new SAXException("illegal attributes value");
            }            
        } else if (DEL.equals(localName)){
            Uri u = Uri.parse(atts.getValue(URI_STR));
            if (u == null) {
                throw new SAXException("attribute " +
                        atts.getValue(URI_STR) + " parsing failure"); 
            }
            int attrLen = atts.getLength() - 2;
            if (attrLen > 0) {
                String[] selectionArgs = new String[attrLen];
                for (int i = 0; i < attrLen; i++) {
                    selectionArgs[i] = atts.getValue(i+2);
                }
                mContentResolver.delete(u, atts.getValue(1), selectionArgs);
            } else if (attrLen == 0){
                mContentResolver.delete(u, atts.getValue(1), null);
            } else {
                mContentResolver.delete(u, null, null);
            }
        } else {
            throw new SAXException("unknown element: " + localName);
        }
    }
    public void endElement(String uri, String localName, String name)
            throws SAXException {
        if (ROW.equals(localName)) {
            if (mUris.empty()) {
                throw new SAXException("uri mismatch"); 
            }
            if (mValues != null) {
                insertRow();
            }
            mUris.pop();                
        } 
    }
    public void characters(char[] ch, int start, int length)
            throws SAXException {
    }
    public void endDocument() throws SAXException {
    }
    public void endPrefixMapping(String prefix) throws SAXException {
    }
    public void ignorableWhitespace(char[] ch, int start, int length)
            throws SAXException {
    }
    public void processingInstruction(String target, String data)
            throws SAXException {
    }
    public void setDocumentLocator(Locator locator) {
    }
    public void skippedEntity(String name) throws SAXException {
    }
    public void startDocument() throws SAXException {
    }
    public void startPrefixMapping(String prefix, String uri)
            throws SAXException {
    }
}
