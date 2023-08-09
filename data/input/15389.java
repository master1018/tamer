public final class DOMKeyName extends DOMStructure implements KeyName {
    private final String name;
    public DOMKeyName(String name) {
        if (name == null) {
            throw new NullPointerException("name cannot be null");
        }
        this.name = name;
    }
    public DOMKeyName(Element knElem) {
        name = knElem.getFirstChild().getNodeValue();
    }
    public String getName() {
        return name;
    }
    public void marshal(Node parent, String dsPrefix, DOMCryptoContext context)
        throws MarshalException {
        Document ownerDoc = DOMUtils.getOwnerDocument(parent);
        Element knElem = DOMUtils.createElement
            (ownerDoc, "KeyName", XMLSignature.XMLNS, dsPrefix);
        knElem.appendChild(ownerDoc.createTextNode(name));
        parent.appendChild(knElem);
    }
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof KeyName)) {
            return false;
        }
        KeyName okn = (KeyName) obj;
        return name.equals(okn.getName());
    }
}
