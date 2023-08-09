public class IIOMetadataNode implements Element, NodeList {
    private String nodeName;
    private String nodeValue;
    private IIOMetadataNodeList attrs = new IIOMetadataNodeList(new ArrayList<IIOMetadataNode>());
    private IIOMetadataNode parent;
    private IIOMetadataNode firstChild;
    private IIOMetadataNode lastChild;
    private IIOMetadataNode previousSibling;
    private IIOMetadataNode nextSibling;
    private int nChildren;
    private Object userObject;
    private String textContent;
    public IIOMetadataNode() {
    }
    public IIOMetadataNode(String nodeName) {
        this.nodeName = nodeName;
    }
    private IIOMetadataNode(String nodeName, String nodeValue) {
        this.nodeName = nodeName;
        this.nodeValue = nodeValue;
    }
    public String getTagName() {
        return nodeName;
    }
    public String getAttribute(String name) {
        Attr attrNode = (Attr)attrs.getNamedItem(name);
        return (attrNode == null) ? "" : attrNode.getValue();
    }
    public void setAttribute(String name, String value) throws DOMException {
        Attr attr = (Attr)attrs.getNamedItem(name);
        if (attr != null) {
            attr.setValue(value);
        } else {
            attrs.list.add(new IIOMetadataAttr(name, value, this));
        }
    }
    public void removeAttribute(String name) throws DOMException {
        IIOMetadataAttr attr = (IIOMetadataAttr)attrs.getNamedItem(name);
        if (attr != null) {
            attr.setOwnerElement(null);
            attrs.list.remove(attr);
        }
    }
    public Attr getAttributeNode(String name) {
        return (Attr)attrs.getNamedItem(name);
    }
    public Attr setAttributeNode(Attr newAttr) throws DOMException {
        Element owner = newAttr.getOwnerElement();
        if (owner != null) {
            if (owner == this) { 
                return null;
            } else {
                throw new DOMException(DOMException.INUSE_ATTRIBUTE_ERR,
                        "Attribute is already in use");
            }
        }
        String name = newAttr.getName();
        Attr oldAttr = getAttributeNode(name);
        if (oldAttr != null) {
            removeAttributeNode(oldAttr);
        }
        IIOMetadataAttr iioAttr;
        if (newAttr instanceof IIOMetadataAttr) {
            iioAttr = (IIOMetadataAttr)newAttr;
            iioAttr.setOwnerElement(this);
        } else {
            iioAttr = new IIOMetadataAttr(name, newAttr.getValue(), this);
        }
        attrs.list.add(iioAttr);
        return oldAttr;
    }
    public Attr removeAttributeNode(Attr oldAttr) throws DOMException {
        if (!attrs.list.remove(oldAttr)) { 
            throw new DOMException(DOMException.NOT_FOUND_ERR, "No such attribute!");
        }
        ((IIOMetadataAttr)oldAttr).setOwnerElement(null);
        return oldAttr;
    }
    public NodeList getElementsByTagName(String name) {
        ArrayList<IIOMetadataNode> nodes = new ArrayList<IIOMetadataNode>();
        Node pos = this;
        while (pos != null) {
            if (pos.getNodeName().equals(name)) {
                nodes.add((IIOMetadataNode)pos);
            }
            Node nextNode = pos.getFirstChild();
            while (nextNode == null) {
                if (pos == this) {
                    break;
                }
                nextNode = pos.getNextSibling();
                if (nextNode == null) {
                    pos = pos.getParentNode();
                    if (pos == null || pos == this) {
                        nextNode = null;
                        break;
                    }
                }
            }
            pos = nextNode;
        }
        return new IIOMetadataNodeList(nodes);
    }
    public String getAttributeNS(String namespaceURI, String localName) throws DOMException {
        return getAttribute(localName);
    }
    public void setAttributeNS(String namespaceURI, String qualifiedName, String value)
            throws DOMException {
        setAttribute(qualifiedName, value);
    }
    public void removeAttributeNS(String namespaceURI, String localName) throws DOMException {
        removeAttribute(localName);
    }
    public Attr getAttributeNodeNS(String namespaceURI, String localName) throws DOMException {
        return getAttributeNode(localName);
    }
    public Attr setAttributeNodeNS(Attr newAttr) throws DOMException {
        return setAttributeNode(newAttr);
    }
    public NodeList getElementsByTagNameNS(String namespaceURI, String localName)
            throws DOMException {
        return getElementsByTagName(localName);
    }
    public boolean hasAttribute(String name) {
        return attrs.getNamedItem(name) != null;
    }
    public boolean hasAttributeNS(String namespaceURI, String localName) throws DOMException {
        return hasAttribute(localName);
    }
    public void setIdAttribute(String name, boolean isId) throws DOMException {
        throw new DOMException(DOMException.NOT_SUPPORTED_ERR, "Method not supported");
    }
    public void setIdAttributeNS(String namespaceURI, String localName, boolean isId)
            throws DOMException {
        throw new DOMException(DOMException.NOT_SUPPORTED_ERR, "Method not supported");
    }
    public void setIdAttributeNode(Attr idAttr, boolean isId) throws DOMException {
        throw new DOMException(DOMException.NOT_SUPPORTED_ERR, "Method not supported");
    }
    public String getNodeName() {
        return nodeName;
    }
    public String getNodeValue() throws DOMException {
        return nodeValue;
    }
    public void setNodeValue(String nodeValue) throws DOMException {
        this.nodeValue = nodeValue;
    }
    public short getNodeType() {
        return ELEMENT_NODE;
    }
    public Node getParentNode() {
        return parent;
    }
    public NodeList getChildNodes() {
        return this;
    }
    public Node getFirstChild() {
        return firstChild;
    }
    public Node getLastChild() {
        return lastChild;
    }
    public Node getPreviousSibling() {
        return previousSibling;
    }
    public Node getNextSibling() {
        return nextSibling;
    }
    public NamedNodeMap getAttributes() {
        return attrs;
    }
    public Document getOwnerDocument() {
        return null;
    }
    public Node insertBefore(Node newChild, Node refChild) throws DOMException {
        if (newChild == null) {
            throw new IllegalArgumentException("newChild == null!");
        }
        IIOMetadataNode newIIOChild = (IIOMetadataNode)newChild;
        IIOMetadataNode refIIOChild = (IIOMetadataNode)refChild;
        newIIOChild.parent = this;
        if (refIIOChild == null) {
            newIIOChild.nextSibling = null;
            newIIOChild.previousSibling = lastChild;
            lastChild = newIIOChild;
            if (firstChild == null) {
                firstChild = newIIOChild;
            }
        } else {
            newIIOChild.nextSibling = refIIOChild;
            newIIOChild.previousSibling = refIIOChild.previousSibling;
            if (firstChild == refIIOChild) {
                firstChild = newIIOChild;
            }
            if (refIIOChild != null) {
                refIIOChild.previousSibling = newIIOChild;
            }
        }
        if (newIIOChild.previousSibling != null) {
            newIIOChild.previousSibling.nextSibling = newIIOChild;
        }
        nChildren++;
        return newIIOChild;
    }
    public Node replaceChild(Node newChild, Node oldChild) throws DOMException {
        if (newChild == null) {
            throw new IllegalArgumentException("newChild == null!");
        }
        IIOMetadataNode newIIOChild = (IIOMetadataNode)newChild;
        IIOMetadataNode oldIIOChild = (IIOMetadataNode)oldChild;
        IIOMetadataNode next = oldIIOChild.nextSibling;
        IIOMetadataNode previous = oldIIOChild.previousSibling;
        newIIOChild.parent = this;
        newIIOChild.nextSibling = next;
        newIIOChild.previousSibling = previous;
        if (lastChild == oldIIOChild) {
            lastChild = newIIOChild;
        }
        if (firstChild == oldIIOChild) {
            firstChild = newIIOChild;
        }
        if (next != null) {
            next.previousSibling = newIIOChild;
        }
        if (previous != null) {
            previous.nextSibling = newIIOChild;
        }
        oldIIOChild.parent = null;
        oldIIOChild.nextSibling = next;
        oldIIOChild.previousSibling = previous;
        return oldIIOChild;
    }
    public Node removeChild(Node oldChild) throws DOMException {
        if (oldChild == null) {
            throw new IllegalArgumentException("oldChild == null!");
        }
        IIOMetadataNode oldIIOChild = (IIOMetadataNode)oldChild;
        IIOMetadataNode previous = oldIIOChild.previousSibling;
        IIOMetadataNode next = oldIIOChild.nextSibling;
        if (previous != null) {
            previous.nextSibling = next;
        }
        if (next != null) {
            next.previousSibling = previous;
        }
        if (lastChild == oldIIOChild) {
            lastChild = previous;
        }
        if (firstChild == oldIIOChild) {
            firstChild = next;
        }
        nChildren--;
        oldIIOChild.parent = null;
        oldIIOChild.previousSibling = null;
        oldIIOChild.nextSibling = null;
        return oldIIOChild;
    }
    public Node appendChild(Node newChild) throws DOMException {
        return insertBefore(newChild, null);
    }
    public boolean hasChildNodes() {
        return nChildren != 0;
    }
    public Node cloneNode(boolean deep) {
        IIOMetadataNode cloned = new IIOMetadataNode(nodeName);
        cloned.setUserObject(getUserObject());
        if (deep) { 
            IIOMetadataNode c = firstChild;
            while (c != null) {
                cloned.insertBefore(c.cloneNode(true), null);
                c = c.nextSibling;
            }
        }
        return cloned; 
    }
    public void normalize() {
    }
    public boolean isSupported(String feature, String version) {
        return false;
    }
    public String getNamespaceURI() {
        return null;
    }
    public String getPrefix() {
        return null;
    }
    public void setPrefix(String prefix) throws DOMException {
    }
    public String getLocalName() {
        return nodeName;
    }
    public boolean hasAttributes() {
        return attrs.list.size() > 0;
    }
    public String getBaseURI() {
        throw new DOMException(DOMException.NOT_SUPPORTED_ERR, "Method not supported");
    }
    public short compareDocumentPosition(Node other) throws DOMException {
        throw new DOMException(DOMException.NOT_SUPPORTED_ERR, "Method not supported");
    }
    public String getTextContent() throws DOMException {
        return textContent;
    }
    public void setTextContent(String textContent) throws DOMException {
        this.textContent = textContent;
    }
    public boolean isSameNode(Node other) {
        throw new DOMException(DOMException.NOT_SUPPORTED_ERR, "Method not supported");
    }
    public String lookupPrefix(String namespaceURI) {
        throw new DOMException(DOMException.NOT_SUPPORTED_ERR, "Method not supported");
    }
    public boolean isDefaultNamespace(String namespaceURI) {
        throw new DOMException(DOMException.NOT_SUPPORTED_ERR, "Method not supported");
    }
    public String lookupNamespaceURI(String prefix) {
        throw new DOMException(DOMException.NOT_SUPPORTED_ERR, "Method not supported");
    }
    public boolean isEqualNode(Node arg) {
        throw new DOMException(DOMException.NOT_SUPPORTED_ERR, "Method not supported");
    }
    public Object getFeature(String feature, String version) {
        throw new DOMException(DOMException.NOT_SUPPORTED_ERR, "Method not supported");
    }
    public Object getUserData(String key) {
        throw new DOMException(DOMException.NOT_SUPPORTED_ERR, "Method not supported");
    }
    public Node item(int index) {
        if (index < 0 || index >= nChildren) {
            return null;
        }
        Node n;
        for (n = getFirstChild(); index > 0; index--) {
            n = n.getNextSibling();
        }
        return n;
    }
    public int getLength() {
        return nChildren;
    }
    public Object getUserObject() {
        return userObject;
    }
    public TypeInfo getSchemaTypeInfo() {
        throw new UnsupportedOperationException();
    }
    public Object setUserData(String key, Object data, UserDataHandler handler) {
        throw new UnsupportedOperationException();
    }
    public void setUserObject(Object userObject) {
        this.userObject = userObject;
    }
    private class IIOMetadataAttr extends IIOMetadataNode implements Attr {
        private Element ownerElement;
        public IIOMetadataAttr(String name, String value, Element owner) {
            super(name, value);
            this.ownerElement = owner;
        }
        public String getName() {
            return getNodeName();
        }
        public boolean getSpecified() {
            return true;
        }
        public String getValue() {
            return nodeValue;
        }
        public void setValue(String value) throws DOMException {
            nodeValue = value;
        }
        public Element getOwnerElement() {
            return ownerElement;
        }
        public void setOwnerElement(Element ownerElement) {
            this.ownerElement = ownerElement;
        }
        public boolean isId() {
            throw new DOMException(DOMException.NOT_SUPPORTED_ERR, "Method not supported");
        }
        @Override
        public short getNodeType() {
            return ATTRIBUTE_NODE;
        }
    }
    private class IIOMetadataNodeList implements NodeList, NamedNodeMap {
        private List<IIOMetadataNode> list;
        IIOMetadataNodeList(List<IIOMetadataNode> list) {
            this.list = list;
        }
        public Node item(int index) {
            try {
                return list.get(index);
            } catch (IndexOutOfBoundsException e) {
                return null;
            }
        }
        public int getLength() {
            return list.size();
        }
        public Node getNamedItem(String name) {
            for (IIOMetadataNode node : list) {
                if (name.equals(node.getNodeName())) {
                    return node;
                }
            }
            return null;
        }
        public Node setNamedItem(Node arg) throws DOMException {
            throw new DOMException(DOMException.NO_MODIFICATION_ALLOWED_ERR,
                    "This NamedNodeMap is read-only!");
        }
        public Node removeNamedItem(String name) throws DOMException {
            throw new DOMException(DOMException.NO_MODIFICATION_ALLOWED_ERR,
                    "This NamedNodeMap is read-only!");
        }
        public Node getNamedItemNS(String namespaceURI, String localName) throws DOMException {
            return getNamedItem(localName);
        }
        public Node setNamedItemNS(Node arg) throws DOMException {
            throw new DOMException(DOMException.NO_MODIFICATION_ALLOWED_ERR,
                    "This NamedNodeMap is read-only!");
        }
        public Node removeNamedItemNS(String namespaceURI, String localName) throws DOMException {
            throw new DOMException(DOMException.NO_MODIFICATION_ALLOWED_ERR,
                    "This NamedNodeMap is read-only!");
        }
    }
}
