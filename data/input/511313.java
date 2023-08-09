public class UiDocumentNode extends UiElementNode {
    public UiDocumentNode(DocumentDescriptor documentDescriptor) {
        super(documentDescriptor);
    }
    @Override
    public String getShortDescription() {
        return "Document"; 
    }
    @Override
    public String getBreadcrumbTrailDescription(boolean include_root) {
        return "Document"; 
    }
    @Override
    protected void setUiParent(UiElementNode parent) {
        if (parent != null) {
            throw new UnsupportedOperationException("Documents can't have UI parents"); 
        }
        super.setUiParent(null);
    }
    @Override
    public boolean loadFromXmlNode(Node xml_node) {
        boolean structure_changed = (getXmlDocument() != xml_node);
        setXmlDocument((Document) xml_node);
        structure_changed |= super.loadFromXmlNode(xml_node);
        if (structure_changed) {
            invokeUiUpdateListeners(UiUpdateState.CHILDREN_CHANGED);
        }
        return structure_changed;
    }
    @Override
    public Node createXmlNode() {
        if (getXmlDocument() == null) {
            throw new UnsupportedOperationException("Documents cannot be created"); 
        }
        return getXmlDocument();
    }
    @Override
    public Node deleteXmlNode() {
        throw new UnsupportedOperationException("Documents cannot be deleted"); 
    }
}
