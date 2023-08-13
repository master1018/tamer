public class DocumentDescriptor extends ElementDescriptor {
    public DocumentDescriptor(String xml_name, ElementDescriptor[] children) {
        super(xml_name, children, true );
    }
    @Override
    public UiElementNode createUiNode() {
        return new UiDocumentNode(this);
    }
}
