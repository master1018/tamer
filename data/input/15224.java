public class LayoutParser extends DefaultHandler {
    private Map<String,XMLNode> xmlElementsMap;
    private XMLNode currentNode;
    private Configuration configuration;
    private static LayoutParser instance;
    private String currentRoot;
    private boolean isParsing;
    private LayoutParser(Configuration configuration) {
        xmlElementsMap = new HashMap<String,XMLNode>();
        this.configuration = configuration;
    }
    public static LayoutParser getInstance(Configuration configuration) {
        if (instance == null) {
            instance = new LayoutParser(configuration);
        }
        return instance;
    }
    public XMLNode parseXML(String root) {
        if (xmlElementsMap.containsKey(root)) {
            return xmlElementsMap.get(root);
        }
        try {
            currentRoot = root;
            isParsing = false;
            SAXParserFactory factory = SAXParserFactory.newInstance();
            SAXParser saxParser = factory.newSAXParser();
            InputStream in = configuration.getBuilderXML();
            saxParser.parse(in, this);
            return xmlElementsMap.get(root);
        } catch (Throwable t) {
            t.printStackTrace();
            throw new DocletAbortException();
        }
    }
    @Override
    public void startElement(String namespaceURI, String sName, String qName,
        Attributes attrs)
    throws SAXException {
        if (isParsing || qName.equals(currentRoot)) {
            isParsing = true;
            currentNode = new XMLNode(currentNode, qName);
            for (int i = 0; i < attrs.getLength(); i++)
                currentNode.attrs.put(attrs.getLocalName(i), attrs.getValue(i));
            if (qName.equals(currentRoot))
                xmlElementsMap.put(qName, currentNode);
        }
    }
    @Override
    public void endElement(String namespaceURI, String sName, String qName)
    throws SAXException {
        if (! isParsing) {
            return;
        }
        currentNode = currentNode.parent;
        isParsing = ! qName.equals(currentRoot);
    }
}
