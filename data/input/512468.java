public abstract class XMLResourceHandler {
    protected String getElementContent(Node elem) {
        return elem.getChildNodes().item(0).getNodeValue().trim();
    }
    static public String getStringAttributeValue(Node elem, String attrName) {
        Node node = elem.getAttributes().getNamedItem(attrName);
        if (node == null) {
            return null;
        }
        return node.getNodeValue().trim();
    }
    protected int getAttributeValue(Node elem, String attrName) {
        return Integer.parseInt(getStringAttributeValue(elem, attrName));
    }
    protected Node getChildByAttribute(Node parent, String attrName, String attrValue) {
        if (parent == null || attrName == null || attrValue == null) {
            return null;
        }
        NodeList children = parent.getChildNodes();
        for (int i = 0; i < children.getLength(); i++) {
            Node child = children.item(i);
            if (child.getNodeType() == Node.ELEMENT_NODE) {
                if (attrValue.equals(getStringAttributeValue(child, attrName))) {
                    return child;
                }
            }
        }
        return null;
    }
    protected void setAttribute(Document doc, Node elem, String name, int value) {
        setAttribute(doc, elem, name, Integer.toString(value));
    }
    protected void setAttribute(Document doc, Node elem, String name, String value) {
        Attr attrNode = doc.createAttribute(name);
        attrNode.setNodeValue(value);
        elem.getAttributes().setNamedItem(attrNode);
    }
    protected static void writeToFile(File file, Document doc) throws FileNotFoundException,
            TransformerFactoryConfigurationError, TransformerException {
        Transformer t = TransformerFactory.newInstance().newTransformer();
        t.setOutputProperty("indent", "yes");
        t.transform(new DOMSource(doc),
                new StreamResult(new FileOutputStream(file)));
    }
}
