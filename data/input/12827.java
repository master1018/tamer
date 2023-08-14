public final class DOMSignatureProperties extends DOMStructure
    implements SignatureProperties {
    private final String id;
    private final List properties;
    public DOMSignatureProperties(List properties, String id) {
        if (properties == null) {
            throw new NullPointerException("properties cannot be null");
        } else if (properties.isEmpty()) {
            throw new IllegalArgumentException("properties cannot be empty");
        } else {
            List propsCopy = new ArrayList(properties);
            for (int i = 0, size = propsCopy.size(); i < size; i++) {
                if (!(propsCopy.get(i) instanceof SignatureProperty)) {
                    throw new ClassCastException
                        ("properties["+i+"] is not a valid type");
                }
            }
            this.properties = Collections.unmodifiableList(propsCopy);
        }
        this.id = id;
    }
    public DOMSignatureProperties(Element propsElem) throws MarshalException{
        id = DOMUtils.getAttributeValue(propsElem, "Id");
        NodeList nodes = propsElem.getChildNodes();
        int length = nodes.getLength();
        List properties = new ArrayList(length);
        for (int i = 0; i < length; i++) {
            Node child = nodes.item(i);
            if (child.getNodeType() == Node.ELEMENT_NODE) {
                properties.add(new DOMSignatureProperty((Element) child));
            }
        }
        if (properties.isEmpty()) {
            throw new MarshalException("properties cannot be empty");
        } else {
            this.properties = Collections.unmodifiableList(properties);
        }
    }
    public List getProperties() {
        return properties;
    }
    public String getId() {
        return id;
    }
    public void marshal(Node parent, String dsPrefix, DOMCryptoContext context)
        throws MarshalException {
        Document ownerDoc = DOMUtils.getOwnerDocument(parent);
        Element propsElem = DOMUtils.createElement
            (ownerDoc, "SignatureProperties", XMLSignature.XMLNS, dsPrefix);
        DOMUtils.setAttributeID(propsElem, "Id", id);
        for (int i = 0, size = properties.size(); i < size; i++) {
            DOMSignatureProperty property =
                (DOMSignatureProperty) properties.get(i);
            property.marshal(propsElem, dsPrefix, context);
        }
        parent.appendChild(propsElem);
    }
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof SignatureProperties)) {
            return false;
        }
        SignatureProperties osp = (SignatureProperties) o;
        boolean idsEqual = (id == null ? osp.getId() == null :
            id.equals(osp.getId()));
        return (properties.equals(osp.getProperties()) && idsEqual);
    }
}
