public class SmilContentHandler extends DefaultHandler {
    private static final String TAG = "SmilContentHandler";
    private static final boolean DEBUG = false;
    private static final boolean LOCAL_LOGV = DEBUG ? Config.LOGD : Config.LOGV;
    private SMILDocument mSmilDocument;
    private Node mCurrentNode;
    public void reset() {
        mSmilDocument = new SmilDocumentImpl();
        mCurrentNode = mSmilDocument;
    }
    public SMILDocument getSmilDocument() {
        return mSmilDocument;
    }
    @Override
    public void startElement(String uri, String localName, String qName, Attributes attributes) {
        if (LOCAL_LOGV) {
            Log.v(TAG, "SmilContentHandler.startElement. Creating element " + localName);
        }
        Element element = mSmilDocument.createElement(localName);
        if (attributes != null) {
            for (int i = 0; i < attributes.getLength(); i++) {
                if (LOCAL_LOGV) {
                    Log.v(TAG, "Attribute " + i +
                        " lname = " + attributes.getLocalName(i) +
                        " value = " + attributes.getValue(i));
                }
                element.setAttribute(attributes.getLocalName(i),
                        attributes.getValue(i));
            }
        }
        if (LOCAL_LOGV) {
            Log.v(TAG, "Appending " + localName + " to " + mCurrentNode.getNodeName());
        }
        mCurrentNode.appendChild(element);
        mCurrentNode = element;
    }
    @Override
    public void endElement(String uri, String localName, String qName) {
        if (LOCAL_LOGV) {
            Log.v(TAG, "SmilContentHandler.endElement. localName " + localName);
        }
        mCurrentNode = mCurrentNode.getParentNode();
    }
    @Override
    public void characters(char[] ch, int start, int length) {
        if (LOCAL_LOGV) {
            Log.v(TAG, "SmilContentHandler.characters. ch = " + new String(ch, start, length));
        }
    }
}
