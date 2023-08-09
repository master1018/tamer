public final class DocumentTypeImpl extends LeafNodeImpl implements DocumentType {
    private String qualifiedName;
    private String publicId;
    private String systemId;
    public DocumentTypeImpl(DocumentImpl document, String qualifiedName,
            String publicId, String systemId) {
        super(document);
        if (qualifiedName == null || "".equals(qualifiedName)) {
            throw new DOMException(DOMException.NAMESPACE_ERR, qualifiedName);
        }
        int prefixSeparator = qualifiedName.lastIndexOf(":");
        if (prefixSeparator != -1) {
            String prefix = qualifiedName.substring(0, prefixSeparator);
            String localName = qualifiedName.substring(prefixSeparator + 1);
            if (!DocumentImpl.isXMLIdentifier(prefix)) {
                throw new DOMException(DOMException.NAMESPACE_ERR, qualifiedName);
            }
            if (!DocumentImpl.isXMLIdentifier(localName)) {
                throw new DOMException(DOMException.INVALID_CHARACTER_ERR, qualifiedName);
            }
        } else {
            if (!DocumentImpl.isXMLIdentifier(qualifiedName)) {
                throw new DOMException(DOMException.INVALID_CHARACTER_ERR, qualifiedName);
            }
        }
        this.qualifiedName = qualifiedName;
        this.publicId = publicId;
        this.systemId = systemId;
    }
    @Override
    public String getNodeName() {
        return qualifiedName;
    }
    @Override
    public short getNodeType() {
        return Node.DOCUMENT_TYPE_NODE;
    }
    public NamedNodeMap getEntities() {
        return null;
    }
    public String getInternalSubset() {
        return null;
    }
    public String getName() {
        return qualifiedName;
    }
    public NamedNodeMap getNotations() {
        return null;
    }
    public String getPublicId() {
        return publicId;
    }
    public String getSystemId() {
        return systemId;
    }
    @Override public String getTextContent() throws DOMException {
        return null;
    }
}
