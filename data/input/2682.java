public class DOML3Node {
    public static void main(String args[]) {
        IIOMetadataNode node = new IIOMetadataNode("node");
        try {
            node.setIdAttribute("name", true);
            throw new RuntimeException("No expected DOM exception");
        } catch (DOMException e) {
        }
        try {
            node.setIdAttributeNS("namespaceURI", "localName", true);
            throw new RuntimeException("No expected DOM exception");
        } catch (DOMException e) {
        }
        try {
            node.setIdAttributeNode((Attr)null, true);
            throw new RuntimeException("No expected DOM exception");
        } catch (DOMException e) {
        }
        try {
            node.getSchemaTypeInfo();
            throw new RuntimeException("No expected DOM exception");
        } catch (DOMException e) {
        }
        try {
            node.setUserData("key", null, (UserDataHandler)null);
            throw new RuntimeException("No expected DOM exception");
        } catch (DOMException e) {
        }
        try {
            node.getUserData("key");
            throw new RuntimeException("No expected DOM exception");
        } catch (DOMException e) {
        }
        try {
            node.getFeature("feature", "version");
            throw new RuntimeException("No expected DOM exception");
        } catch (DOMException e) {
        }
        try {
            node.isSameNode((Node)null);
            throw new RuntimeException("No expected DOM exception");
        } catch (DOMException e) {
        }
        try {
            node.isEqualNode((Node)null);
            throw new RuntimeException("No expected DOM exception");
        } catch (DOMException e) {
        }
        try {
            node.lookupNamespaceURI("prefix");
            throw new RuntimeException("No expected DOM exception");
        } catch (DOMException e) {
        }
        try {
            node.isDefaultNamespace("namespaceURI");
            throw new RuntimeException("No expected DOM exception");
        } catch (DOMException e) {
        }
        try {
            node.lookupPrefix("namespaceURI");
            throw new RuntimeException("No expected DOM exception");
        } catch (DOMException e) {
        }
        try {
            node.getTextContent();
            throw new RuntimeException("No expected DOM exception");
        } catch (DOMException e) {
        }
        try {
            node.setTextContent("textContent");
            throw new RuntimeException("No expected DOM exception");
        } catch (DOMException e) {
        }
        try {
            node.compareDocumentPosition((Node)null);
            throw new RuntimeException("No expected DOM exception");
        } catch (DOMException e) {
        }
        try {
            node.getBaseURI();
            throw new RuntimeException("No expected DOM exception");
        } catch (DOMException e) {
        }
    }
}
