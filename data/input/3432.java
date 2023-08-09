public class DOMUtils {
    private DOMUtils() {}
    public static Document getOwnerDocument(Node node) {
        if (node.getNodeType() == Node.DOCUMENT_NODE) {
            return (Document) node;
        } else {
            return node.getOwnerDocument();
        }
    }
    public static Element createElement(Document doc, String tag, String nsURI,
        String prefix) {
        String qName = (prefix == null || prefix.length() == 0)
                       ? tag : prefix + ":" + tag;
        return doc.createElementNS(nsURI, qName);
    }
    public static void setAttribute(Element elem, String name, String value) {
        if (value == null) return;
        elem.setAttributeNS(null, name, value);
    }
    public static void setAttributeID(Element elem, String name, String value) {
        if (value == null) return;
        elem.setAttributeNS(null, name, value);
        IdResolver.registerElementById(elem, value);
    }
    public static Element getFirstChildElement(Node node) {
        Node child = node.getFirstChild();
        while (child != null && child.getNodeType() != Node.ELEMENT_NODE) {
            child = child.getNextSibling();
        }
        return (Element) child;
    }
    public static Element getLastChildElement(Node node) {
        Node child = node.getLastChild();
        while (child != null && child.getNodeType() != Node.ELEMENT_NODE) {
            child = child.getPreviousSibling();
        }
        return (Element) child;
    }
    public static Element getNextSiblingElement(Node node) {
        Node sibling = node.getNextSibling();
        while (sibling != null && sibling.getNodeType() != Node.ELEMENT_NODE) {
            sibling = sibling.getNextSibling();
        }
        return (Element) sibling;
    }
    public static String getAttributeValue(Element elem, String name) {
        Attr attr = elem.getAttributeNodeNS(null, name);
        return (attr == null) ? null : attr.getValue();
    }
    public static Set nodeSet(NodeList nl) {
        return new NodeSet(nl);
    }
    static class NodeSet extends AbstractSet {
        private NodeList nl;
        public NodeSet(NodeList nl) {
            this.nl = nl;
        }
        public int size() { return nl.getLength(); }
        public Iterator iterator() {
            return new Iterator() {
                int index = 0;
                public void remove() {
                    throw new UnsupportedOperationException();
                }
                public Object next() {
                    if (!hasNext()) {
                        throw new NoSuchElementException();
                    }
                    return nl.item(index++);
                }
                public boolean hasNext() {
                    return index < nl.getLength() ? true : false;
                }
            };
        }
    }
    public static String getNSPrefix(XMLCryptoContext context, String nsURI) {
        if (context != null) {
            return context.getNamespacePrefix
                (nsURI, context.getDefaultNamespacePrefix());
        } else {
            return null;
        }
    }
    public static String getSignaturePrefix(XMLCryptoContext context) {
        return getNSPrefix(context, XMLSignature.XMLNS);
    }
    public static void removeAllChildren(Node node) {
        NodeList children = node.getChildNodes();
        for (int i = 0, length = children.getLength(); i < length; i++) {
            node.removeChild(children.item(i));
        }
    }
    public static boolean nodesEqual(Node thisNode, Node otherNode) {
        if (thisNode == otherNode) {
            return true;
        }
        if (thisNode.getNodeType() != otherNode.getNodeType()) {
            return false;
        }
        return true;
    }
    public static void appendChild(Node parent, Node child) {
        Document ownerDoc = getOwnerDocument(parent);
        if (child.getOwnerDocument() != ownerDoc) {
            parent.appendChild(ownerDoc.importNode(child, true));
        } else {
            parent.appendChild(child);
        }
    }
    public static boolean paramsEqual(AlgorithmParameterSpec spec1,
        AlgorithmParameterSpec spec2) {
        if (spec1 == spec2) {
            return true;
        }
        if (spec1 instanceof XPathFilter2ParameterSpec &&
            spec2 instanceof XPathFilter2ParameterSpec) {
            return paramsEqual((XPathFilter2ParameterSpec) spec1,
                (XPathFilter2ParameterSpec) spec2);
        }
        if (spec1 instanceof ExcC14NParameterSpec &&
            spec2 instanceof ExcC14NParameterSpec) {
            return paramsEqual((ExcC14NParameterSpec) spec1,
                (ExcC14NParameterSpec) spec2);
        }
        if (spec1 instanceof XPathFilterParameterSpec &&
            spec2 instanceof XPathFilterParameterSpec) {
            return paramsEqual((XPathFilterParameterSpec) spec1,
                (XPathFilterParameterSpec) spec2);
        }
        if (spec1 instanceof XSLTTransformParameterSpec &&
            spec2 instanceof XSLTTransformParameterSpec) {
            return paramsEqual((XSLTTransformParameterSpec) spec1,
                (XSLTTransformParameterSpec) spec2);
        }
        return false;
    }
    private static boolean paramsEqual(XPathFilter2ParameterSpec spec1,
        XPathFilter2ParameterSpec spec2) {
        List types = spec1.getXPathList();
        List otypes = spec2.getXPathList();
        int size = types.size();
        if (size != otypes.size()) {
            return false;
        }
        for (int i = 0; i < size; i++) {
            XPathType type = (XPathType) types.get(i);
            XPathType otype = (XPathType) otypes.get(i);
            if (!type.getExpression().equals(otype.getExpression()) ||
                !type.getNamespaceMap().equals(otype.getNamespaceMap()) ||
                type.getFilter() != otype.getFilter()) {
                return false;
            }
        }
        return true;
    }
    private static boolean paramsEqual(ExcC14NParameterSpec spec1,
        ExcC14NParameterSpec spec2) {
        return spec1.getPrefixList().equals(spec2.getPrefixList());
    }
    private static boolean paramsEqual(XPathFilterParameterSpec spec1,
        XPathFilterParameterSpec spec2) {
        return (spec1.getXPath().equals(spec2.getXPath()) &&
            spec1.getNamespaceMap().equals(spec2.getNamespaceMap()));
    }
    private static boolean paramsEqual(XSLTTransformParameterSpec spec1,
        XSLTTransformParameterSpec spec2) {
        XMLStructure ostylesheet = spec2.getStylesheet();
        if (!(ostylesheet instanceof javax.xml.crypto.dom.DOMStructure)) {
            return false;
        }
        Node ostylesheetElem =
            ((javax.xml.crypto.dom.DOMStructure) ostylesheet).getNode();
        XMLStructure stylesheet = spec1.getStylesheet();
        Node stylesheetElem =
            ((javax.xml.crypto.dom.DOMStructure) stylesheet).getNode();
        return nodesEqual(stylesheetElem, ostylesheetElem);
    }
}
