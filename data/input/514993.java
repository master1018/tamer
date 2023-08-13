public class EntityImpl extends NodeImpl implements Entity {
    private String notationName;
    private String publicID;
    private String systemID;
    EntityImpl(DocumentImpl document, String notationName, String publicID,
            String systemID) {
        super(document);
        this.notationName = notationName;
        this.publicID = publicID;
        this.systemID = systemID;
    }
    @Override
    public String getNodeName() {
        return getNotationName();
    }
    @Override
    public short getNodeType() {
        return Node.ENTITY_NODE;
    }
    public String getNotationName() {
        return notationName;
    }
    public String getPublicId() {
        return publicID;
    }
    public String getSystemId() {
        return systemID;
    }
    public String getInputEncoding() {
        throw new UnsupportedOperationException(); 
    }
    public String getXmlEncoding() {
        throw new UnsupportedOperationException(); 
    }
    public String getXmlVersion() {
        throw new UnsupportedOperationException(); 
    }
}
