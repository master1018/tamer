public class UiElementTreeEditPartFactory implements EditPartFactory {
    public EditPart createEditPart(EditPart context, Object model) {
        if (model instanceof UiDocumentNode) {
            return new UiDocumentTreeEditPart((UiDocumentNode) model);
        } else if (model instanceof UiElementNode) {
            UiElementNode node = (UiElementNode) model;
            if (node.getDescriptor().hasChildren()) {
                return new UiLayoutTreeEditPart(node);
            } else {
                return new UiViewTreeEditPart(node);
            }
        }
        return null;
    }
}
