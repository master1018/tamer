public class UiViewEditPart extends UiElementEditPart {
    public UiViewEditPart(UiElementNode uiElementNode) {
        super(uiElementNode);
    }
    @Override
    protected IFigure createFigure() {
        IFigure f = new ElementFigure(null);
        f.setLayoutManager(new XYLayout());
        return f;
    }
    @Override
    protected void showSelection() {
        IFigure f = getFigure();
        if (f instanceof ElementFigure) {
            ((ElementFigure) f).setSelected(true);
        }
    }
    @Override
    protected void hideSelection() {
        IFigure f = getFigure();
        if (f instanceof ElementFigure) {
            ((ElementFigure) f).setSelected(false);
        }
    }
}
