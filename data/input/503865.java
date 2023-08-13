public abstract class BasePullParser implements IXmlPullParser {
    protected int mParsingState = START_DOCUMENT;
    public BasePullParser() {
    }
    public abstract void onNextFromStartDocument();
    public abstract void onNextFromStartTag();
    public abstract void onNextFromEndTag();
    public void setFeature(String name, boolean state) throws XmlPullParserException {
        if (FEATURE_PROCESS_NAMESPACES.equals(name) && state) {
            return;
        }
        if (FEATURE_REPORT_NAMESPACE_ATTRIBUTES.equals(name) && state) {
            return;
        }
        throw new XmlPullParserException("Unsupported feature: " + name);
    }
    public boolean getFeature(String name) {
        if (FEATURE_PROCESS_NAMESPACES.equals(name)) {
            return true;
        }
        if (FEATURE_REPORT_NAMESPACE_ATTRIBUTES.equals(name)) {
            return true;
        }
        return false;
    }
    public void setProperty(String name, Object value) throws XmlPullParserException {
        throw new XmlPullParserException("setProperty() not supported");
    }
    public Object getProperty(String name) {
        return null;
    }
    public void setInput(Reader in) throws XmlPullParserException {
        throw new XmlPullParserException("setInput() not supported");
    }
    public void setInput(InputStream inputStream, String inputEncoding)
            throws XmlPullParserException {
        throw new XmlPullParserException("setInput() not supported");
    }
    public void defineEntityReplacementText(String entityName, String replacementText)
            throws XmlPullParserException {
        throw new XmlPullParserException("defineEntityReplacementText() not supported");
    }
    public String getNamespacePrefix(int pos) throws XmlPullParserException {
        throw new XmlPullParserException("getNamespacePrefix() not supported");
    }
    public String getInputEncoding() {
        return null;
    }
    public String getNamespace(String prefix) {
        throw new RuntimeException("getNamespace() not supported");
    }
    public int getNamespaceCount(int depth) throws XmlPullParserException {
        throw new XmlPullParserException("getNamespaceCount() not supported");
    }
    public String getNamespaceUri(int pos) throws XmlPullParserException {
        throw new XmlPullParserException("getNamespaceUri() not supported");
    }
    public int getColumnNumber() {
        return -1;
    }
    public int getLineNumber() {
        return -1;
    }
    public String getAttributeType(int arg0) {
        return "CDATA";
    }
    public int getEventType() {
        return mParsingState;
    }
    public String getText() {
        return null;
    }
    public char[] getTextCharacters(int[] arg0) {
        return null;
    }
    public boolean isAttributeDefault(int arg0) {
        return false;
    }
    public boolean isWhitespace() {
        return false;
    }
    public int next() throws XmlPullParserException {
        switch (mParsingState) {
            case END_DOCUMENT:
                throw new XmlPullParserException("Nothing after the end");
            case START_DOCUMENT:
                onNextFromStartDocument();
                break;
            case START_TAG:
                onNextFromStartTag();
                break;
            case END_TAG:
                onNextFromEndTag();
                break;
            case TEXT:
                break;
            case CDSECT:
                break;
            case ENTITY_REF:
                break;
            case IGNORABLE_WHITESPACE:
                break;
            case PROCESSING_INSTRUCTION:
                break;
            case COMMENT:
                break;
            case DOCDECL:
                break;
        }
        return mParsingState;
    }
    public int nextTag() throws XmlPullParserException {
        int eventType = next();
        if (eventType != START_TAG && eventType != END_TAG) {
            throw new XmlPullParserException("expected start or end tag", this, null);
        }
        return eventType;
    }
    public String nextText() throws XmlPullParserException {
        if (getEventType() != START_TAG) {
            throw new XmlPullParserException("parser must be on START_TAG to read next text", this,
                    null);
        }
        int eventType = next();
        if (eventType == TEXT) {
            String result = getText();
            eventType = next();
            if (eventType != END_TAG) {
                throw new XmlPullParserException(
                        "event TEXT it must be immediately followed by END_TAG", this, null);
            }
            return result;
        } else if (eventType == END_TAG) {
            return "";
        } else {
            throw new XmlPullParserException("parser must be on START_TAG or TEXT to read text",
                    this, null);
        }
    }
    public int nextToken() throws XmlPullParserException {
        return next();
    }
    public void require(int type, String namespace, String name) throws XmlPullParserException {
        if (type != getEventType() || (namespace != null && !namespace.equals(getNamespace()))
                || (name != null && !name.equals(getName())))
            throw new XmlPullParserException("expected " + TYPES[type] + getPositionDescription());
    }
}
