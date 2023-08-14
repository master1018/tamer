public final class UiLayoutEditPart extends UiElementEditPart {
    static class HighlightInfo {
        public boolean drawDropBorder;
        public UiElementEditPart[] childParts;
        public Point anchorPoint;
        public Point linePoints[];
        public final Point tempPoints[] = new Point[] { new Point(), new Point() };
        public void clear() {
            drawDropBorder = false;
            childParts = null;
            anchorPoint = null;
            linePoints = null;
        }
    }
    private final HighlightInfo mHighlightInfo = new HighlightInfo();
    private final IOutlineProvider mProvider;
    public UiLayoutEditPart(UiElementNode uiElementNode, IOutlineProvider provider) {
        super(uiElementNode);
        mProvider = provider;
    }
    @Override
    protected void createEditPolicies() {
        super.createEditPolicies();
        installEditPolicy(EditPolicy.CONTAINER_ROLE, new ContainerEditPolicy() {
            @Override
            protected Command getCreateCommand(CreateRequest request) {
                return null;
            }
        });
        installLayoutEditPolicy(this);
    }
    @Override
    protected IFigure createFigure() {
        IFigure f = new LayoutFigure(mProvider);
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
    public void showDropTarget(Point where) {
        if (where != null) {
            mHighlightInfo.clear();
            mHighlightInfo.drawDropBorder = true;
            DropFeedback.computeDropFeedback(this, mHighlightInfo, where);
            IFigure f = getFigure();
            if (f instanceof LayoutFigure) {
                ((LayoutFigure) f).setHighlighInfo(mHighlightInfo);
            }
        }
    }
    public void hideDropTarget() {
        mHighlightInfo.clear();
        IFigure f = getFigure();
        if (f instanceof LayoutFigure) {
            ((LayoutFigure) f).setHighlighInfo(mHighlightInfo);
        }
    }
}
