public class XmlFile {
    public final Element Root;
    private XPath xpath;
    public XmlFile(String filename) throws ParserConfigurationException, SAXException, IOException {
        DocumentBuilderFactory docBuilderFactory;
        docBuilderFactory = DocumentBuilderFactory.newInstance();
        DocumentBuilder docBuilder = docBuilderFactory.newDocumentBuilder();
        Document doc = docBuilder.parse(new File(filename));
        doc.getDocumentElement().normalize();
        Root = (Element) doc.getLastChild();
    }
    public XmlFile(Element root) {
        Root = root;
    }
    public XPath XPath() {
        return xpath != null ? xpath : (xpath = XPathFactory.newInstance().newXPath());
    }
    public String evalXPath(String expression) throws XPathExpressionException {
        return (String) XPath().evaluate(expression, Root, XPathConstants.STRING);
    }
    @SuppressWarnings("unchecked")
    public <T> T evalXPath(String expression, QName returnType) throws XPathExpressionException {
        return (T) XPath().evaluate(expression, Root, returnType);
    }
    public String evalXPath(String expression, Element context) throws XPathExpressionException {
        return (String) XPath().evaluate(expression, context, XPathConstants.STRING);
    }
    @SuppressWarnings("unchecked")
    public <T> T evalXPath(String expression, Element context, QName returnType) throws XPathExpressionException {
        return (T) XPath().evaluate(expression, context, returnType);
    }
    @SuppressWarnings("unchecked")
    public <T extends Node> T selectSingleNode(String expression) throws XPathExpressionException {
        return (T) XPath().evaluate(expression, Root, XPathConstants.NODE);
    }
    @SuppressWarnings("unchecked")
    public <T extends Node> T selectSingleNode(String expression, Element context) throws XPathExpressionException {
        return (T) XPath().evaluate(expression, context, XPathConstants.NODE);
    }
    public NodeList selectNodes(String expression) throws XPathExpressionException {
        return (NodeList) XPath().evaluate(expression, Root, XPathConstants.NODESET);
    }
    public NodeList selectNodes(String expression, Element context) throws XPathExpressionException {
        return (NodeList) XPath().evaluate(expression, context, XPathConstants.NODESET);
    }
}
