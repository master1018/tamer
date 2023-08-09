public class UiLayoutTreeEditPart extends UiElementTreeEditPart {
    public UiLayoutTreeEditPart(UiElementNode node) {
        super(node);
    }
    @SuppressWarnings("unchecked")
    @Override
    protected List getModelChildren() {
        return getUiNode().getUiChildren();
    }
}
