public final class DOMManifest extends DOMStructure implements Manifest {
    private final List references;
    private final String id;
    public DOMManifest(List references, String id) {
        if (references == null) {
            throw new NullPointerException("references cannot be null");
        }
        List refCopy = new ArrayList(references);
        if (refCopy.isEmpty()) {
            throw new IllegalArgumentException("list of references must " +
                "contain at least one entry");
        }
        for (int i = 0, size = refCopy.size(); i < size; i++) {
            if (!(refCopy.get(i) instanceof Reference)) {
                throw new ClassCastException
                    ("references["+i+"] is not a valid type");
            }
        }
        this.references = Collections.unmodifiableList(refCopy);
        this.id = id;
    }
    public DOMManifest(Element manElem, XMLCryptoContext context,
        Provider provider) throws MarshalException {
        this.id = DOMUtils.getAttributeValue(manElem, "Id");
        Element refElem = DOMUtils.getFirstChildElement(manElem);
        List refs = new ArrayList();
        while (refElem != null) {
            refs.add(new DOMReference(refElem, context, provider));
            refElem = DOMUtils.getNextSiblingElement(refElem);
        }
        this.references = Collections.unmodifiableList(refs);
    }
    public String getId() {
        return id;
    }
    public List getReferences() {
        return references;
    }
    public void marshal(Node parent, String dsPrefix, DOMCryptoContext context)
        throws MarshalException {
        Document ownerDoc = DOMUtils.getOwnerDocument(parent);
        Element manElem = DOMUtils.createElement
            (ownerDoc, "Manifest", XMLSignature.XMLNS, dsPrefix);
        DOMUtils.setAttributeID(manElem, "Id", id);
        for (int i = 0, size = references.size(); i < size; i++) {
            DOMReference ref = (DOMReference) references.get(i);
            ref.marshal(manElem, dsPrefix, context);
        }
        parent.appendChild(manElem);
    }
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof Manifest)) {
            return false;
        }
        Manifest oman = (Manifest) o;
        boolean idsEqual = (id == null ? oman.getId() == null :
            id.equals(oman.getId()));
        return (idsEqual && references.equals(oman.getReferences()));
    }
}
