public final class DOMKeyInfo extends DOMStructure implements KeyInfo {
    private final String id;
    private final List keyInfoTypes;
    public DOMKeyInfo(List content, String id) {
        if (content == null) {
            throw new NullPointerException("content cannot be null");
        }
        List typesCopy = new ArrayList(content);
        if (typesCopy.isEmpty()) {
            throw new IllegalArgumentException("content cannot be empty");
        }
        for (int i = 0, size = typesCopy.size(); i < size; i++) {
            if (!(typesCopy.get(i) instanceof XMLStructure)) {
                throw new ClassCastException
                    ("content["+i+"] is not a valid KeyInfo type");
            }
        }
        this.keyInfoTypes = Collections.unmodifiableList(typesCopy);
        this.id = id;
    }
    public DOMKeyInfo(Element kiElem, XMLCryptoContext context,
        Provider provider) throws MarshalException {
        id = DOMUtils.getAttributeValue(kiElem, "Id");
        NodeList nl = kiElem.getChildNodes();
        int length = nl.getLength();
        if (length < 1) {
            throw new MarshalException
                ("KeyInfo must contain at least one type");
        }
        List content = new ArrayList(length);
        for (int i = 0; i < length; i++) {
            Node child = nl.item(i);
            if (child.getNodeType() != Node.ELEMENT_NODE) {
                continue;
            }
            Element childElem = (Element) child;
            String localName = childElem.getLocalName();
            if (localName.equals("X509Data")) {
                content.add(new DOMX509Data(childElem));
            } else if (localName.equals("KeyName")) {
                content.add(new DOMKeyName(childElem));
            } else if (localName.equals("KeyValue")) {
                content.add(new DOMKeyValue(childElem));
            } else if (localName.equals("RetrievalMethod")) {
                content.add
                    (new DOMRetrievalMethod(childElem, context, provider));
            } else if (localName.equals("PGPData")) {
                content.add(new DOMPGPData(childElem));
            } else { 
                content.add(new javax.xml.crypto.dom.DOMStructure((childElem)));
            }
        }
        keyInfoTypes = Collections.unmodifiableList(content);
    }
    public String getId() {
        return id;
    }
    public List getContent() {
        return keyInfoTypes;
    }
    public void marshal(XMLStructure parent, XMLCryptoContext context)
        throws MarshalException {
        if (parent == null) {
            throw new NullPointerException("parent is null");
        }
        Node pNode = ((javax.xml.crypto.dom.DOMStructure) parent).getNode();
        String dsPrefix = DOMUtils.getSignaturePrefix(context);
        Element kiElem = DOMUtils.createElement
            (DOMUtils.getOwnerDocument(pNode), "KeyInfo",
             XMLSignature.XMLNS, dsPrefix);
        if (dsPrefix == null || dsPrefix.length() == 0) {
            kiElem.setAttributeNS
                ("http:
        } else {
            kiElem.setAttributeNS
                ("http:
                 XMLSignature.XMLNS);
        }
        marshal(pNode, kiElem, null, dsPrefix, (DOMCryptoContext) context);
    }
    public void marshal(Node parent, String dsPrefix,
        DOMCryptoContext context) throws MarshalException {
        marshal(parent, null, dsPrefix, context);
    }
    public void marshal(Node parent, Node nextSibling, String dsPrefix,
        DOMCryptoContext context) throws MarshalException {
        Document ownerDoc = DOMUtils.getOwnerDocument(parent);
        Element kiElem = DOMUtils.createElement
            (ownerDoc, "KeyInfo", XMLSignature.XMLNS, dsPrefix);
        marshal(parent, kiElem, nextSibling, dsPrefix, context);
    }
    private void marshal(Node parent, Element kiElem, Node nextSibling,
        String dsPrefix, DOMCryptoContext context) throws MarshalException {
        for (int i = 0, size = keyInfoTypes.size(); i < size; i++) {
            XMLStructure kiType = (XMLStructure) keyInfoTypes.get(i);
            if (kiType instanceof DOMStructure) {
                ((DOMStructure) kiType).marshal(kiElem, dsPrefix, context);
            } else {
                DOMUtils.appendChild(kiElem,
                    ((javax.xml.crypto.dom.DOMStructure) kiType).getNode());
            }
        }
        DOMUtils.setAttributeID(kiElem, "Id", id);
        parent.insertBefore(kiElem, nextSibling);
    }
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof KeyInfo)) {
            return false;
        }
        KeyInfo oki = (KeyInfo) o;
        boolean idsEqual = (id == null ? oki.getId() == null :
            id.equals(oki.getId()));
        return (keyInfoTypes.equals(oki.getContent()) && idsEqual);
    }
}
