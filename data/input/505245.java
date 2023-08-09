public class DocumentFragmentImpl extends InnerNodeImpl implements
        DocumentFragment {
    DocumentFragmentImpl(DocumentImpl document) {
        super(document);
    }
    @Override
    public String getNodeName() {
        return "#document-fragment";
    }
    @Override
    public short getNodeType() {
        return Node.DOCUMENT_FRAGMENT_NODE;
    }
}
