public final class CommentImpl extends CharacterDataImpl implements Comment {
    CommentImpl(DocumentImpl document, String data) {
        super(document, data);
    }
    @Override
    public String getNodeName() {
        return "#comment";
    }
    @Override
    public short getNodeType() {
        return Node.COMMENT_NODE;
    }
    public boolean containsDashDash() {
        return buffer.indexOf("--") != -1;
    }
}
