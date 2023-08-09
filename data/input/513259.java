class XmlParserUtils {
    public static Node getFirstChild(Node node, String xmlLocalName) {
        String nsUri = node.getNamespaceURI();
        for(Node child = node.getFirstChild(); child != null; child = child.getNextSibling()) {
            if (child.getNodeType() == Node.ELEMENT_NODE &&
                    nsUri.equals(child.getNamespaceURI())) {
                if (xmlLocalName == null || xmlLocalName.equals(child.getLocalName())) {
                    return child;
                }
            }
        }
        return null;
    }
    public static String getXmlString(Node node, String xmlLocalName) {
        Node child = getFirstChild(node, xmlLocalName);
        return child == null ? "" : child.getTextContent();  
    }
    public static String getOptionalXmlString(Node node, String xmlLocalName) {
        Node child = getFirstChild(node, xmlLocalName);
        return child == null ? null : child.getTextContent();  
    }
    public static int getXmlInt(Node node, String xmlLocalName, int defaultValue) {
        String s = getXmlString(node, xmlLocalName);
        try {
            return Integer.parseInt(s);
        } catch (NumberFormatException e) {
            return defaultValue;
        }
    }
    public static long getXmlLong(Node node, String xmlLocalName, long defaultValue) {
        String s = getXmlString(node, xmlLocalName);
        try {
            return Long.parseLong(s);
        } catch (NumberFormatException e) {
            return defaultValue;
        }
    }
    public static Object getEnumAttribute(
            Node archiveNode,
            String attrName,
            Object[] values,
            Object defaultValue) {
        Node attr = archiveNode.getAttributes().getNamedItem(attrName);
        if (attr != null) {
            String found = attr.getNodeValue();
            for (Object value : values) {
                if (value.toString().equalsIgnoreCase(found)) {
                    return value;
                }
            }
        }
        return defaultValue;
    }
}
