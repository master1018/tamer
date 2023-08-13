public final class DOMSignatureProperty extends DOMStructure
    implements SignatureProperty {
    private final String id;
    private final String target;
    private final List content;
    public DOMSignatureProperty(List content, String target, String id) {
        if (target == null) {
            throw new NullPointerException("target cannot be null");
        } else if (content == null) {
            throw new NullPointerException("content cannot be null");
        } else if (content.isEmpty()) {
            throw new IllegalArgumentException("content cannot be empty");
        } else {
            List contentCopy = new ArrayList(content);
            for (int i = 0, size = contentCopy.size(); i < size; i++) {
                if (!(contentCopy.get(i) instanceof XMLStructure)) {
                    throw new ClassCastException
                        ("content["+i+"] is not a valid type");
                }
            }
            this.content = Collections.unmodifiableList(contentCopy);
        }
        this.target = target;
        this.id = id;
    }
    public DOMSignatureProperty(Element propElem) throws MarshalException {
        target = DOMUtils.getAttributeValue(propElem, "Target");
        if (target == null) {
            throw new MarshalException("target cannot be null");
        }
        id = DOMUtils.getAttributeValue(propElem, "Id");
        NodeList nodes = propElem.getChildNodes();
        int length = nodes.getLength();
        List content = new ArrayList(length);
        for (int i = 0; i < length; i++) {
            content.add(new javax.xml.crypto.dom.DOMStructure(nodes.item(i)));
        }
        if (content.isEmpty()) {
            throw new MarshalException("content cannot be empty");
        } else {
            this.content = Collections.unmodifiableList(content);
        }
    }
    public List getContent() {
        return content;
    }
    public String getId() {
        return id;
    }
    public String getTarget() {
        return target;
    }
    public void marshal(Node parent, String dsPrefix, DOMCryptoContext context)
        throws MarshalException {
        Document ownerDoc = DOMUtils.getOwnerDocument(parent);
        Element propElem = DOMUtils.createElement
            (ownerDoc, "SignatureProperty", XMLSignature.XMLNS, dsPrefix);
        DOMUtils.setAttributeID(propElem, "Id", id);
        DOMUtils.setAttribute(propElem, "Target", target);
        for (int i = 0, size = content.size(); i < size; i++) {
            javax.xml.crypto.dom.DOMStructure property =
                (javax.xml.crypto.dom.DOMStructure) content.get(i);
            DOMUtils.appendChild(propElem, property.getNode());
        }
        parent.appendChild(propElem);
    }
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof SignatureProperty)) {
            return false;
        }
        SignatureProperty osp = (SignatureProperty) o;
        boolean idsEqual = (id == null ? osp.getId() == null :
            id.equals(osp.getId()));
        return (equalsContent(osp.getContent()) &&
            target.equals(osp.getTarget()) && idsEqual);
    }
    private boolean equalsContent(List otherContent) {
        int osize = otherContent.size();
        if (content.size() != osize) {
            return false;
        }
        for (int i = 0; i < osize; i++) {
            XMLStructure oxs = (XMLStructure) otherContent.get(i);
            XMLStructure xs = (XMLStructure) content.get(i);
            if (oxs instanceof javax.xml.crypto.dom.DOMStructure) {
                if (!(xs instanceof javax.xml.crypto.dom.DOMStructure)) {
                    return false;
                }
                Node onode =
                    ((javax.xml.crypto.dom.DOMStructure) oxs).getNode();
                Node node =
                    ((javax.xml.crypto.dom.DOMStructure) xs).getNode();
                if (!DOMUtils.nodesEqual(node, onode)) {
                    return false;
                }
            } else {
                if (!(xs.equals(oxs))) {
                    return false;
                }
            }
        }
        return true;
    }
}
