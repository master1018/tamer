public class UiDocumentTreeEditPart extends UiElementTreeEditPart {
    public UiDocumentTreeEditPart(UiDocumentNode model) {
        super(model);
    }
    @SuppressWarnings("unchecked")
    @Override
    protected List getModelChildren() {
        return getUiNode().getUiChildren();
    }
}
