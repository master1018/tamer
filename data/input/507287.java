public class EntityReferenceImpl extends LeafNodeImpl implements EntityReference {
    private String name;
    EntityReferenceImpl(DocumentImpl document, String name) {
        super(document);
        this.name = name;
    }
    @Override
    public String getNodeName() {
        return name;
    }
    @Override
    public short getNodeType() {
        return Node.ENTITY_REFERENCE_NODE;
    }
}
