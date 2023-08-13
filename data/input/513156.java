public class ElementImpl extends NodeImpl implements Element {
    private String mTagName;
    private NamedNodeMap mAttributes = new NamedNodeMapImpl();
    protected ElementImpl(DocumentImpl owner, String tagName) {
        super(owner);
        mTagName = tagName;
    }
    public String getAttribute(String name) {
        Attr attrNode = getAttributeNode(name);
        String attrValue = "";
        if (attrNode != null) {
            attrValue = attrNode.getValue();
        }
        return attrValue;
    }
    public String getAttributeNS(String namespaceURI, String localName) {
        return null;
    }
    public Attr getAttributeNode(String name) {
        return (Attr)mAttributes.getNamedItem(name);
    }
    public Attr getAttributeNodeNS(String namespaceURI, String localName) {
        return null;
    }
    public NodeList getElementsByTagName(String name) {
        return new NodeListImpl(this, name, true);
    }
    public NodeList getElementsByTagNameNS(String namespaceURI, String localName) {
        return null;
    }
    public String getTagName() {
        return mTagName;
    }
    public boolean hasAttribute(String name) {
        return (getAttributeNode(name) != null);
    }
    public boolean hasAttributeNS(String namespaceURI, String localName) {
        return false;
    }
    public void removeAttribute(String name) throws DOMException {
    }
    public void removeAttributeNS(String namespaceURI, String localName)
            throws DOMException {
    }
    public Attr removeAttributeNode(Attr oldAttr) throws DOMException {
        return null;
    }
    public void setAttribute(String name, String value) throws DOMException {
        Attr attribute = getAttributeNode(name);
        if (attribute == null) {
            attribute = mOwnerDocument.createAttribute(name);
        }
        attribute.setNodeValue(value);
        mAttributes.setNamedItem(attribute);
    }
    public void setAttributeNS(String namespaceURI, String qualifiedName,
            String value) throws DOMException {
    }
    public Attr setAttributeNode(Attr newAttr) throws DOMException {
        return null;
    }
    public Attr setAttributeNodeNS(Attr newAttr) throws DOMException {
        return null;
    }
    @Override
    public short getNodeType() {
        return ELEMENT_NODE;
    }
    @Override
    public String getNodeName() {
        return mTagName;
    }
    @Override
    public NamedNodeMap getAttributes() {
        return mAttributes;
    }
    @Override
    public boolean hasAttributes() {
        return (mAttributes.getLength() > 0);
    }
    public TypeInfo getSchemaTypeInfo() {
        return null;
    }
    public void setIdAttribute(String name, boolean isId) throws DOMException {
        throw new DOMException(DOMException.NOT_SUPPORTED_ERR, null);
    }
    public void setIdAttributeNS(String namespaceURI, String localName,
            boolean isId) throws DOMException {
        throw new DOMException(DOMException.NOT_SUPPORTED_ERR, null);
    }
    public void setIdAttributeNode(Attr idAttr, boolean isId)
            throws DOMException {
        throw new DOMException(DOMException.NOT_SUPPORTED_ERR, null);
    }
}
