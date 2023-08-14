public class DOM2DTMdefaultNamespaceDeclarationNode implements Attr,TypeInfo
{
  final String NOT_SUPPORTED_ERR="Unsupported operation on pseudonode";
  Element pseudoparent;
  String prefix,uri,nodename;
  int handle;
  DOM2DTMdefaultNamespaceDeclarationNode(Element pseudoparent,String prefix,String uri,int handle)
  {
    this.pseudoparent=pseudoparent;
    this.prefix=prefix;
    this.uri=uri;
    this.handle=handle;
    this.nodename="xmlns:"+prefix;
  }
  public String getNodeName() {return nodename;}
  public String getName() {return nodename;}
  public String getNamespaceURI() {return "http:
  public String getPrefix() {return prefix;}
  public String getLocalName() {return prefix;}
  public String getNodeValue() {return uri;}
  public String getValue() {return uri;}
  public Element getOwnerElement() {return pseudoparent;}
  public boolean isSupported(String feature, String version) {return false;}
  public boolean hasChildNodes() {return false;}
  public boolean hasAttributes() {return false;}
  public Node getParentNode() {return null;}
  public Node getFirstChild() {return null;}
  public Node getLastChild() {return null;}
  public Node getPreviousSibling() {return null;}
  public Node getNextSibling() {return null;}
  public boolean getSpecified() {return false;}
  public void normalize() {return;}
  public NodeList getChildNodes() {return null;}
  public NamedNodeMap getAttributes() {return null;}
  public short getNodeType() {return Node.ATTRIBUTE_NODE;}
  public void setNodeValue(String value) {throw new DTMException(NOT_SUPPORTED_ERR);}
  public void setValue(String value) {throw new DTMException(NOT_SUPPORTED_ERR);}
  public void setPrefix(String value) {throw new DTMException(NOT_SUPPORTED_ERR);}
  public Node insertBefore(Node a, Node b) {throw new DTMException(NOT_SUPPORTED_ERR);}
  public Node replaceChild(Node a, Node b) {throw new DTMException(NOT_SUPPORTED_ERR);}
  public Node appendChild(Node a) {throw new DTMException(NOT_SUPPORTED_ERR);}
  public Node removeChild(Node a) {throw new DTMException(NOT_SUPPORTED_ERR);}
  public Document getOwnerDocument() {return pseudoparent.getOwnerDocument();}
  public Node cloneNode(boolean deep) {throw new DTMException(NOT_SUPPORTED_ERR);}
    public int getHandleOfNode()		
    {
        return handle;
    }
    public String getTypeName() {return null; }
    public String getTypeNamespace() { return null;}
    public boolean isDerivedFrom( String ns, String localName, int derivationMethod ) {
        return false;
    }
    public TypeInfo getSchemaTypeInfo() { return this; }
    public boolean isId( ) { return false; }
    public Object setUserData(String key,
                              Object data,
                              UserDataHandler handler) {
        return getOwnerDocument().setUserData( key, data, handler);
    }
    public Object getUserData(String key) {
        return getOwnerDocument().getUserData( key);
    } 
    public Object getFeature(String feature, String version) {
        return isSupported(feature, version) ? this : null;
    }
    public boolean isEqualNode(Node arg) {
        if (arg == this) {
            return true;
        }
        if (arg.getNodeType() != getNodeType()) {
            return false;
        }
        if (getNodeName() == null) {
            if (arg.getNodeName() != null) {
                return false;
            }
        }
        else if (!getNodeName().equals(arg.getNodeName())) {
            return false;
        }
        if (getLocalName() == null) {
            if (arg.getLocalName() != null) {
                return false;
            }
        }
        else if (!getLocalName().equals(arg.getLocalName())) {
            return false;
        }
        if (getNamespaceURI() == null) {
            if (arg.getNamespaceURI() != null) {
                return false;
            }
        }
        else if (!getNamespaceURI().equals(arg.getNamespaceURI())) {
            return false;
        }
        if (getPrefix() == null) {
            if (arg.getPrefix() != null) {
                return false;
            }
        }
        else if (!getPrefix().equals(arg.getPrefix())) {
            return false;
        }
        if (getNodeValue() == null) {
            if (arg.getNodeValue() != null) {
                return false;
            }
        }
        else if (!getNodeValue().equals(arg.getNodeValue())) {
            return false;
        }
             return true;
    }
    public String lookupNamespaceURI(String specifiedPrefix) {
        short type = this.getNodeType();
        switch (type) {
        case Node.ELEMENT_NODE : {
                String namespace = this.getNamespaceURI();
                String prefix = this.getPrefix();
                if (namespace !=null) {
                    if (specifiedPrefix== null && prefix==specifiedPrefix) {
                        return namespace;
                    } else if (prefix != null && prefix.equals(specifiedPrefix)) {
                        return namespace;
                    }
                }
                if (this.hasAttributes()) {
                    NamedNodeMap map = this.getAttributes();
                    int length = map.getLength();
                    for (int i=0;i<length;i++) {
                        Node attr = map.item(i);
                        String attrPrefix = attr.getPrefix();
                        String value = attr.getNodeValue();
                        namespace = attr.getNamespaceURI();
                        if (namespace !=null && namespace.equals("http:
                            if (specifiedPrefix == null &&
                                attr.getNodeName().equals("xmlns")) {
                                return value;
                            } else if (attrPrefix !=null &&
                                       attrPrefix.equals("xmlns") &&
                                       attr.getLocalName().equals(specifiedPrefix)) {
                                return value;
                            }
                        }
                    }
                }
                return null;
            }
        case Node.ENTITY_NODE :
        case Node.NOTATION_NODE:
        case Node.DOCUMENT_FRAGMENT_NODE:
        case Node.DOCUMENT_TYPE_NODE:
            return null;
        case Node.ATTRIBUTE_NODE:{
                if (this.getOwnerElement().getNodeType() == Node.ELEMENT_NODE) {
                    return getOwnerElement().lookupNamespaceURI(specifiedPrefix);
                }
                return null;
            }
        default:{
                return null;
            }
        }
    }
    public boolean isDefaultNamespace(String namespaceURI){
        return false;
    }
    public String lookupPrefix(String namespaceURI){
        if (namespaceURI == null) {
            return null;
        }
        short type = this.getNodeType();
        switch (type) {
        case Node.ENTITY_NODE :
        case Node.NOTATION_NODE:
        case Node.DOCUMENT_FRAGMENT_NODE:
        case Node.DOCUMENT_TYPE_NODE:
            return null;
        case Node.ATTRIBUTE_NODE:{
                if (this.getOwnerElement().getNodeType() == Node.ELEMENT_NODE) {
                    return getOwnerElement().lookupPrefix(namespaceURI);
                }
                return null;
            }
        default:{ 
                return null;
            }
         }
    }
    public boolean isSameNode(Node other) {
        return this == other;
    }
    public void setTextContent(String textContent)
        throws DOMException {
        setNodeValue(textContent);
    }
    public String getTextContent() throws DOMException {
        return getNodeValue();  
    }
    public short compareDocumentPosition(Node other) throws DOMException {
        return 0;
    }
    public String getBaseURI() {
        return null;
    }
}
