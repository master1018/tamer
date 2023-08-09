public class XmlDocumentBuilder {
    public static final String NODE_START_LINE = "startLine";
    public static final String NODE_END_LINE = "endLine";
    private final DocumentBuilder mBuilder;
    private boolean mHasLineNumbersSupport;
    public XmlDocumentBuilder() {
        try {
            Class.forName("com.sun.org.apache.xerces.internal.parsers.DOMParser");
            mHasLineNumbersSupport = true;
        } catch (ClassNotFoundException e) {
        }
        if (!mHasLineNumbersSupport) {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            try {
                mBuilder = factory.newDocumentBuilder();
            } catch (ParserConfigurationException e) {
                throw new IllegalStateException("Could not initialize the XML parser");
            }
        } else {
            mBuilder = null;
        }
    }
    public boolean isHasLineNumbersSupport() {
        return mHasLineNumbersSupport;
    }
    public Document parse(InputStream inputStream) throws SAXException, IOException {
        if (!mHasLineNumbersSupport) {
            return mBuilder.parse(inputStream);
        } else {
            DOMParser parser = new LineNumberDOMParser();
            parser.parse(new InputSource(inputStream));
            return parser.getDocument();
        }
    }
    public Document parse(String content) throws SAXException, IOException {
        if (!mHasLineNumbersSupport) {
            return mBuilder.parse(content);
        } else {
            DOMParser parser = new LineNumberDOMParser();
            parser.parse(content);
            return parser.getDocument();
        }
    }
    public Document parse(File file) throws SAXException, IOException {
        return parse(new FileInputStream(file));
    }
    private static class LineNumberDOMParser extends DOMParser {
        private static final String FEATURE_NODE_EXPANSION =
                "http:
        private static final String CURRENT_NODE =
                "http:
        private XMLLocator mLocator;
        private LinkedList<Node> mStack = new LinkedList<Node>();
        private LineNumberDOMParser() {
            try {
                setFeature(FEATURE_NODE_EXPANSION, false);
            } catch (SAXNotRecognizedException e) {
                e.printStackTrace();
            } catch (SAXNotSupportedException e) {
                e.printStackTrace();
            }
        }
        @Override
        public void startDocument(XMLLocator xmlLocator, String s,
                NamespaceContext namespaceContext, Augmentations augmentations)
                throws XNIException {
            super.startDocument(xmlLocator, s, namespaceContext, augmentations);
            mLocator = xmlLocator;
            mStack.add(setNodeLineNumber(NODE_START_LINE));
        }
        private Node setNodeLineNumber(String tag) {
            Node node = null;
            try {
                node = (Node) getProperty(CURRENT_NODE);
            } catch (SAXNotRecognizedException e) {
                e.printStackTrace();
            } catch (SAXNotSupportedException e) {
                e.printStackTrace();
            }
            if (node != null) {
                node.setUserData(tag, mLocator.getLineNumber(), null);
            }
            return node;
        }
        @Override
        public void startElement(QName qName, XMLAttributes xmlAttributes,
                Augmentations augmentations) throws XNIException {
            super.startElement(qName, xmlAttributes, augmentations);
            mStack.add(setNodeLineNumber(NODE_START_LINE));            
        }
        @Override
        public void endElement(QName qName, Augmentations augmentations) throws XNIException {
            super.endElement(qName, augmentations);
            Node node = mStack.removeLast();
            if (node != null) {
                node.setUserData(NODE_END_LINE, mLocator.getLineNumber(), null);
            }
        }
    }
}
