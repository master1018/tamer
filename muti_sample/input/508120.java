class MockNamedNodeMap implements NamedNodeMap {
    private final HashMap<String, HashMap<String, Node>> mNodeMap =
        new HashMap<String, HashMap<String, Node>>();
    private final ArrayList<Node> mNodeList = new ArrayList<Node>();
    public MockXmlNode addAttribute(String namespace, String localName, String value) {
        MockXmlNode node = new MockXmlNode(namespace, localName, value);
        if (namespace == null) {
            namespace = ""; 
        }
        HashMap<String, Node> map = mNodeMap.get(namespace);
        if (map == null) {
            map = new HashMap<String, Node>();
            mNodeMap.put(namespace, map);
        }
        map.put(localName, node);
        mNodeList.add(node);
        return node;
    }
    public int getLength() {
        return mNodeList.size();
    }
    public Node getNamedItem(String name) {
        HashMap<String, Node> map = mNodeMap.get(""); 
        if (map != null) {
            return map.get(name);
        }
        return null;
    }
    public Node getNamedItemNS(String namespaceURI, String localName) throws DOMException {
        if (namespaceURI == null) {
            namespaceURI = ""; 
        }
        HashMap<String, Node> map = mNodeMap.get(namespaceURI);
        if (map != null) {
            return map.get(localName);
        }
        return null;
    }
    public Node item(int index) {
        return mNodeList.get(index);
    }
    public Node removeNamedItem(String name) throws DOMException {
        throw new UnsupportedOperationException("Operation not implemented.");  
    }
    public Node removeNamedItemNS(String namespaceURI, String localName) throws DOMException {
        throw new UnsupportedOperationException("Operation not implemented.");  
    }
    public Node setNamedItem(Node arg) throws DOMException {
        throw new UnsupportedOperationException("Operation not implemented.");  
    }
    public Node setNamedItemNS(Node arg) throws DOMException {
        throw new UnsupportedOperationException("Operation not implemented.");  
    }
}
