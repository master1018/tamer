public class NotationImpl extends LeafNodeImpl implements Notation {
    private String notationName;
    private String publicID;
    private String systemID;
    NotationImpl(DocumentImpl document, String notationName, String publicID,
            String systemID) {
        super(document);
    }
    @Override
    public String getNodeName() {
        return notationName;
    }
    @Override
    public short getNodeType() {
        return Node.NOTATION_NODE;
    }
    public String getPublicId() {
        return publicID;
    }
    public String getSystemId() {
        return systemID;
    }
}
