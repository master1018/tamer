public class UiElementTreeEditPart extends AbstractTreeEditPart {
    public UiElementTreeEditPart(UiElementNode uiElementNode) {
        setModel(uiElementNode);
    }
    @Override
    protected void createEditPolicies() {
        super.createEditPolicies();
    }
    @Override
    protected Image getImage() {
        return getUiNode().getDescriptor().getIcon();
    }
    @Override
    protected String getText() {
        return getUiNode().getShortDescription();
    }
    @Override
    public void activate() {
        if (!isActive()) {
            super.activate();
        }
    }
    @Override
    public void deactivate() {
        if (isActive()) {
            super.deactivate();
        }
    }
    protected UiElementNode getUiNode() {
        return (UiElementNode)getModel();
    }
}
