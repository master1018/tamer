class IIODOMException extends DOMException {
    public IIODOMException(short code, String message) {
        super(code, message);
    }
}
class IIONamedNodeMap implements NamedNodeMap {
    List nodes;
    public IIONamedNodeMap(List nodes) {
        this.nodes = nodes;
    }
    public int getLength() {
        return nodes.size();
    }
    public Node getNamedItem(String name) {
        Iterator iter = nodes.iterator();
        while (iter.hasNext()) {
            Node node = (Node)iter.next();
            if (name.equals(node.getNodeName())) {
                return node;
            }
        }
        return null;
    }
    public Node item(int index) {
        Node node = (Node)nodes.get(index);
        return node;
    }
    public Node removeNamedItem(java.lang.String name) {
        throw new DOMException(DOMException.NO_MODIFICATION_ALLOWED_ERR,
                               "This NamedNodeMap is read-only!");
    }
    public Node setNamedItem(Node arg) {
        throw new DOMException(DOMException.NO_MODIFICATION_ALLOWED_ERR,
                               "This NamedNodeMap is read-only!");
    }
    public Node getNamedItemNS(String namespaceURI, String localName) {
        return getNamedItem(localName);
    }
    public Node setNamedItemNS(Node arg) {
        return setNamedItem(arg);
    }
    public Node removeNamedItemNS(String namespaceURI, String localName) {
        return removeNamedItem(localName);
    }
}
class IIONodeList implements NodeList {
    List nodes;
    public IIONodeList(List nodes) {
        this.nodes = nodes;
    }
    public int getLength() {
        return nodes.size();
    }
    public Node item(int index) {
        if (index < 0 || index > nodes.size()) {
            return null;
        }
        return (Node)nodes.get(index);
    }
}
class IIOAttr extends IIOMetadataNode implements Attr {
    Element owner;
    String name;
    String value;
    public IIOAttr(Element owner, String name, String value) {
        this.owner = owner;
        this.name = name;
        this.value = value;
    }
    public String getName() {
        return name;
    }
    public String getNodeName() {
        return name;
    }
    public short getNodeType() {
        return ATTRIBUTE_NODE;
    }
    public boolean getSpecified() {
        return true;
    }
    public String getValue() {
        return value;
    }
    public String getNodeValue() {
        return value;
    }
    public void setValue(String value) {
        this.value = value;
    }
    public void setNodeValue(String value) {
        this.value = value;
    }
    public Element getOwnerElement() {
        return owner;
    }
    public void setOwnerElement(Element owner) {
        this.owner = owner;
    }
    public boolean isId() {
        return false;
    }
}
public class IIOMetadataNode implements Element, NodeList {
    private String nodeName = null;
    private String nodeValue = null;
    private Object userObject = null;
    private IIOMetadataNode parent = null;
    private int numChildren = 0;
    private IIOMetadataNode firstChild = null;
    private IIOMetadataNode lastChild = null;
    private IIOMetadataNode nextSibling = null;
    private IIOMetadataNode previousSibling = null;
    private List attributes = new ArrayList();
    public IIOMetadataNode() {}
    public IIOMetadataNode(String nodeName) {
        this.nodeName = nodeName;
    }
    private void checkNode(Node node) throws DOMException {
        if (node == null) {
            return;
        }
        if (!(node instanceof IIOMetadataNode)) {
            throw new IIODOMException(DOMException.WRONG_DOCUMENT_ERR,
                                      "Node not an IIOMetadataNode!");
        }
    }
    public String getNodeName() {
        return nodeName;
    }
    public String getNodeValue(){
        return nodeValue;
    }
    public void setNodeValue(String nodeValue) {
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
        return new IIONamedNodeMap(attributes);
    }
    public Document getOwnerDocument() {
        return null;
    }
    public Node insertBefore(Node newChild,
                             Node refChild) {
        if (newChild == null) {
            throw new IllegalArgumentException("newChild == null!");
        }
        checkNode(newChild);
        checkNode(refChild);
        IIOMetadataNode newChildNode = (IIOMetadataNode)newChild;
        IIOMetadataNode refChildNode = (IIOMetadataNode)refChild;
        IIOMetadataNode previous = null;
        IIOMetadataNode next = null;
        if (refChild == null) {
            previous = this.lastChild;
            next = null;
            this.lastChild = newChildNode;
        } else {
            previous = refChildNode.previousSibling;
            next = refChildNode;
        }
        if (previous != null) {
            previous.nextSibling = newChildNode;
        }
        if (next != null) {
            next.previousSibling = newChildNode;
        }
        newChildNode.parent = this;
        newChildNode.previousSibling = previous;
        newChildNode.nextSibling = next;
        if (this.firstChild == refChildNode) {
            this.firstChild = newChildNode;
        }
        ++numChildren;
        return newChildNode;
    }
    public Node replaceChild(Node newChild,
                             Node oldChild) {
        if (newChild == null) {
            throw new IllegalArgumentException("newChild == null!");
        }
        checkNode(newChild);
        checkNode(oldChild);
        IIOMetadataNode newChildNode = (IIOMetadataNode)newChild;
        IIOMetadataNode oldChildNode = (IIOMetadataNode)oldChild;
        IIOMetadataNode previous = oldChildNode.previousSibling;
        IIOMetadataNode next = oldChildNode.nextSibling;
        if (previous != null) {
            previous.nextSibling = newChildNode;
        }
        if (next != null) {
            next.previousSibling = newChildNode;
        }
        newChildNode.parent = this;
        newChildNode.previousSibling = previous;
        newChildNode.nextSibling = next;
        if (firstChild == oldChildNode) {
            firstChild = newChildNode;
        }
        if (lastChild == oldChildNode) {
            lastChild = newChildNode;
        }
        oldChildNode.parent = null;
        oldChildNode.previousSibling = null;
        oldChildNode.nextSibling = null;
        return oldChildNode;
    }
    public Node removeChild(Node oldChild) {
        if (oldChild == null) {
            throw new IllegalArgumentException("oldChild == null!");
        }
        checkNode(oldChild);
        IIOMetadataNode oldChildNode = (IIOMetadataNode)oldChild;
        IIOMetadataNode previous = oldChildNode.previousSibling;
        IIOMetadataNode next = oldChildNode.nextSibling;
        if (previous != null) {
            previous.nextSibling = next;
        }
        if (next != null) {
            next.previousSibling = previous;
        }
        if (this.firstChild == oldChildNode) {
            this.firstChild = next;
        }
        if (this.lastChild == oldChildNode) {
            this.lastChild = previous;
        }
        oldChildNode.parent = null;
        oldChildNode.previousSibling = null;
        oldChildNode.nextSibling = null;
        --numChildren;
        return oldChildNode;
    }
    public Node appendChild(Node newChild) {
        if (newChild == null) {
            throw new IllegalArgumentException("newChild == null!");
        }
        checkNode(newChild);
        return insertBefore(newChild, null);
    }
    public boolean hasChildNodes() {
        return numChildren > 0;
    }
    public Node cloneNode(boolean deep) {
        IIOMetadataNode newNode = new IIOMetadataNode(this.nodeName);
        newNode.setUserObject(getUserObject());
        if (deep) {
            for (IIOMetadataNode child = firstChild;
                 child != null;
                 child = child.nextSibling) {
                newNode.appendChild(child.cloneNode(true));
            }
        }
        return newNode;
    }
    public void normalize() {
    }
    public boolean isSupported(String feature, String version) {
        return false;
    }
    public String getNamespaceURI() throws DOMException {
        return null;
    }
    public String getPrefix() {
        return null;
    }
    public void setPrefix(String prefix) {
    }
    public String getLocalName() {
        return nodeName;
    }
    public String getTagName() {
        return nodeName;
    }
    public String getAttribute(String name) {
        Attr attr = getAttributeNode(name);
        if (attr == null) {
            return "";
        }
        return attr.getValue();
    }
    public String getAttributeNS(String namespaceURI, String localName) {
        return getAttribute(localName);
    }
    public void setAttribute(String name, String value) {
        boolean valid = true;
        char[] chs = name.toCharArray();
        for (int i=0;i<chs.length;i++) {
            if (chs[i] >= 0xfffe) {
                valid = false;
                break;
            }
        }
        if (!valid) {
            throw new IIODOMException(DOMException.INVALID_CHARACTER_ERR,
                                      "Attribute name is illegal!");
        }
        removeAttribute(name, false);
        attributes.add(new IIOAttr(this, name, value));
    }
    public void setAttributeNS(String namespaceURI,
                               String qualifiedName, String value) {
        setAttribute(qualifiedName, value);
    }
    public void removeAttribute(String name) {
        removeAttribute(name, true);
    }
    private void removeAttribute(String name, boolean checkPresent) {
        int numAttributes = attributes.size();
        for (int i = 0; i < numAttributes; i++) {
            IIOAttr attr = (IIOAttr)attributes.get(i);
            if (name.equals(attr.getName())) {
                attr.setOwnerElement(null);
                attributes.remove(i);
                return;
            }
        }
        if (checkPresent) {
            throw new IIODOMException(DOMException.NOT_FOUND_ERR,
                                      "No such attribute!");
        }
    }
    public void removeAttributeNS(String namespaceURI,
                                  String localName) {
        removeAttribute(localName);
    }
    public Attr getAttributeNode(String name) {
        Node node = getAttributes().getNamedItem(name);
        return (Attr)node;
    }
   public Attr getAttributeNodeNS(String namespaceURI,
                                   String localName) {
        return getAttributeNode(localName);
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
        IIOAttr attr;
        if (newAttr instanceof IIOAttr) {
            attr = (IIOAttr)newAttr;
            attr.setOwnerElement(this);
        } else {
            attr = new IIOAttr(this,
                               newAttr.getName(),
                               newAttr.getValue());
        }
        Attr oldAttr = getAttributeNode(attr.getName());
        if (oldAttr != null) {
            removeAttributeNode(oldAttr);
        }
        attributes.add(attr);
        return oldAttr;
    }
    public Attr setAttributeNodeNS(Attr newAttr) {
        return setAttributeNode(newAttr);
    }
    public Attr removeAttributeNode(Attr oldAttr) {
        removeAttribute(oldAttr.getName());
        return oldAttr;
    }
    public NodeList getElementsByTagName(String name) {
        List l = new ArrayList();
        getElementsByTagName(name, l);
        return new IIONodeList(l);
    }
    private void getElementsByTagName(String name, List l) {
        if (nodeName.equals(name)) {
            l.add(this);
        }
        Node child = getFirstChild();
        while (child != null) {
            ((IIOMetadataNode)child).getElementsByTagName(name, l);
            child = child.getNextSibling();
        }
    }
    public NodeList getElementsByTagNameNS(String namespaceURI,
                                           String localName) {
        return getElementsByTagName(localName);
    }
    public boolean hasAttributes() {
        return attributes.size() > 0;
    }
    public boolean hasAttribute(String name) {
        return getAttributeNode(name) != null;
    }
    public boolean hasAttributeNS(String namespaceURI,
                                  String localName) {
        return hasAttribute(localName);
    }
    public int getLength() {
        return numChildren;
    }
    public Node item(int index) {
        if (index < 0) {
            return null;
        }
        Node child = getFirstChild();
        while (child != null && index-- > 0) {
            child = child.getNextSibling();
        }
        return child;
    }
    public Object getUserObject() {
        return userObject;
    }
    public void setUserObject(Object userObject) {
        this.userObject = userObject;
    }
    public void setIdAttribute(String name,
                               boolean isId)
                               throws DOMException {
        throw new DOMException(DOMException.NOT_SUPPORTED_ERR,
                               "Method not supported");
    }
    public void setIdAttributeNS(String namespaceURI,
                                 String localName,
                                 boolean isId)
                                 throws DOMException {
        throw new DOMException(DOMException.NOT_SUPPORTED_ERR,
                               "Method not supported");
    }
    public void setIdAttributeNode(Attr idAttr,
                                   boolean isId)
                                   throws DOMException {
        throw new DOMException(DOMException.NOT_SUPPORTED_ERR,
                               "Method not supported");
    }
    public TypeInfo getSchemaTypeInfo() throws DOMException {
        throw new DOMException(DOMException.NOT_SUPPORTED_ERR,
                               "Method not supported");
    }
    public Object setUserData(String key,
                              Object data,
                              UserDataHandler handler) throws DOMException {
        throw new DOMException(DOMException.NOT_SUPPORTED_ERR,
                               "Method not supported");
    }
    public Object getUserData(String key) throws DOMException {
        throw new DOMException(DOMException.NOT_SUPPORTED_ERR,
                               "Method not supported");
    }
    public Object getFeature(String feature, String version)
                              throws DOMException {
        throw new DOMException(DOMException.NOT_SUPPORTED_ERR,
                               "Method not supported");
    }
    public boolean isSameNode(Node node) throws DOMException {
        throw new DOMException(DOMException.NOT_SUPPORTED_ERR,
                               "Method not supported");
    }
    public boolean isEqualNode(Node node) throws DOMException {
        throw new DOMException(DOMException.NOT_SUPPORTED_ERR,
                               "Method not supported");
    }
    public String lookupNamespaceURI(String prefix) throws DOMException {
        throw new DOMException(DOMException.NOT_SUPPORTED_ERR,
                               "Method not supported");
    }
    public boolean isDefaultNamespace(String namespaceURI)
                                               throws DOMException {
        throw new DOMException(DOMException.NOT_SUPPORTED_ERR,
                               "Method not supported");
    }
    public String lookupPrefix(String namespaceURI) throws DOMException {
        throw new DOMException(DOMException.NOT_SUPPORTED_ERR,
                               "Method not supported");
    }
    public String getTextContent() throws DOMException {
        throw new DOMException(DOMException.NOT_SUPPORTED_ERR,
                               "Method not supported");
    }
    public void setTextContent(String textContent) throws DOMException {
        throw new DOMException(DOMException.NOT_SUPPORTED_ERR,
                               "Method not supported");
    }
    public short compareDocumentPosition(Node other)
                                         throws DOMException {
        throw new DOMException(DOMException.NOT_SUPPORTED_ERR,
                               "Method not supported");
    }
    public String getBaseURI() throws DOMException {
        throw new DOMException(DOMException.NOT_SUPPORTED_ERR,
                               "Method not supported");
    }
}
