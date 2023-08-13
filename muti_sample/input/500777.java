public class UiElementsEditPartFactory implements EditPartFactory {
    private Display mDisplay;
    private boolean mShowOutline = false;
    private IOutlineProvider mProvider;
    public interface IOutlineProvider {
        boolean hasOutline();
    }
    public UiElementsEditPartFactory(Display display, IOutlineProvider provider) {
        mDisplay = display;
        mProvider = provider;
    }
    public EditPart createEditPart(EditPart context, Object model) {
        if (model instanceof UiDocumentNode) {
            return new UiDocumentEditPart((UiDocumentNode) model, mDisplay);
        } else if (model instanceof UiElementNode) {
            UiElementNode node = (UiElementNode) model;
            if (node.getDescriptor().hasChildren()) {
                return new UiLayoutEditPart(node, mProvider);
            }
            return new UiViewEditPart(node);
        }
        return null;
    }
    public void setShowOutline(boolean showOutline) {
        mShowOutline  = showOutline;
    }
}
