public class SwitchEditPart extends AbstractBorderedShapeEditPart {
    public static final int VISUAL_ID = 1002;
    protected IFigure contentPane;
    protected IFigure primaryShape;
    public SwitchEditPart(View view) {
        super(view);
    }
    protected void createDefaultEditPolicies() {
        installEditPolicy(EditPolicyRoles.CREATION_ROLE, new CreationEditPolicy());
        super.createDefaultEditPolicies();
        installEditPolicy(EditPolicyRoles.SEMANTIC_ROLE, new SwitchItemSemanticEditPolicy());
        installEditPolicy(EditPolicyRoles.DRAG_DROP_ROLE, new DragDropEditPolicy());
        installEditPolicy(EditPolicyRoles.CANONICAL_ROLE, new SwitchCanonicalEditPolicy());
        installEditPolicy(EditPolicy.LAYOUT_ROLE, createLayoutEditPolicy());
        installEditPolicy(EditPolicyRoles.OPEN_ROLE, new OpenDiagramEditPolicySwitch());
    }
    protected LayoutEditPolicy createLayoutEditPolicy() {
        ConstrainedToolbarLayoutEditPolicy lep = new ConstrainedToolbarLayoutEditPolicy() {
            protected EditPolicy createChildEditPolicy(EditPart child) {
                if (child instanceof IBorderItemEditPart) {
                    return new BorderItemSelectionEditPolicy();
                }
                if (child.getEditPolicy(EditPolicy.PRIMARY_DRAG_ROLE) == null) {
                    if (child instanceof ITextAwareEditPart) {
                        return new SaveccmTextSelectionEditPolicy();
                    }
                }
                return super.createChildEditPolicy(child);
            }
        };
        return lep;
    }
    protected IFigure createNodeShape() {
        SwitchFigure figure = new SwitchFigure();
        return primaryShape = figure;
    }
    public SwitchFigure getPrimaryShape() {
        return (SwitchFigure) primaryShape;
    }
    protected boolean addFixedChild(EditPart childEditPart) {
        if (childEditPart instanceof SwitchNameEditPart) {
            ((SwitchNameEditPart) childEditPart).setLabel(getPrimaryShape().getFigureSwitchNameLabel());
            return true;
        }
        if (childEditPart instanceof TriggerIn3EditPart) {
            BorderItemLocator locator = new BorderItemLocator(getMainFigure(), PositionConstants.WEST);
            getBorderedFigure().getBorderItemContainer().add(((TriggerIn3EditPart) childEditPart).getFigure(), locator);
            return true;
        }
        if (childEditPart instanceof TriggerOut3EditPart) {
            BorderItemLocator locator = new BorderItemLocator(getMainFigure(), PositionConstants.EAST);
            getBorderedFigure().getBorderItemContainer().add(((TriggerOut3EditPart) childEditPart).getFigure(), locator);
            return true;
        }
        if (childEditPart instanceof DataIn3EditPart) {
            BorderItemLocator locator = new BorderItemLocator(getMainFigure(), PositionConstants.WEST);
            getBorderedFigure().getBorderItemContainer().add(((DataIn3EditPart) childEditPart).getFigure(), locator);
            return true;
        }
        if (childEditPart instanceof DataOut3EditPart) {
            BorderItemLocator locator = new BorderItemLocator(getMainFigure(), PositionConstants.EAST);
            getBorderedFigure().getBorderItemContainer().add(((DataOut3EditPart) childEditPart).getFigure(), locator);
            return true;
        }
        if (childEditPart instanceof CombinedIn3EditPart) {
            BorderItemLocator locator = new BorderItemLocator(getMainFigure(), PositionConstants.WEST);
            getBorderedFigure().getBorderItemContainer().add(((CombinedIn3EditPart) childEditPart).getFigure(), locator);
            return true;
        }
        if (childEditPart instanceof CombinedOut3EditPart) {
            BorderItemLocator locator = new BorderItemLocator(getMainFigure(), PositionConstants.EAST);
            getBorderedFigure().getBorderItemContainer().add(((CombinedOut3EditPart) childEditPart).getFigure(), locator);
            return true;
        }
        return false;
    }
    protected boolean removeFixedChild(EditPart childEditPart) {
        if (childEditPart instanceof TriggerIn3EditPart) {
            getBorderedFigure().getBorderItemContainer().remove(((TriggerIn3EditPart) childEditPart).getFigure());
            return true;
        }
        if (childEditPart instanceof TriggerOut3EditPart) {
            getBorderedFigure().getBorderItemContainer().remove(((TriggerOut3EditPart) childEditPart).getFigure());
            return true;
        }
        if (childEditPart instanceof DataIn3EditPart) {
            getBorderedFigure().getBorderItemContainer().remove(((DataIn3EditPart) childEditPart).getFigure());
            return true;
        }
        if (childEditPart instanceof DataOut3EditPart) {
            getBorderedFigure().getBorderItemContainer().remove(((DataOut3EditPart) childEditPart).getFigure());
            return true;
        }
        if (childEditPart instanceof CombinedIn3EditPart) {
            getBorderedFigure().getBorderItemContainer().remove(((CombinedIn3EditPart) childEditPart).getFigure());
            return true;
        }
        if (childEditPart instanceof CombinedOut3EditPart) {
            getBorderedFigure().getBorderItemContainer().remove(((CombinedOut3EditPart) childEditPart).getFigure());
            return true;
        }
        return false;
    }
    protected void addChildVisual(EditPart childEditPart, int index) {
        if (addFixedChild(childEditPart)) {
            return;
        }
        super.addChildVisual(childEditPart, -1);
    }
    protected void removeChildVisual(EditPart childEditPart) {
        if (removeFixedChild(childEditPart)) {
            return;
        }
        super.removeChildVisual(childEditPart);
    }
    protected IFigure getContentPaneFor(IGraphicalEditPart editPart) {
        if (editPart instanceof TriggerIn3EditPart) {
            return getBorderedFigure().getBorderItemContainer();
        }
        if (editPart instanceof TriggerOut3EditPart) {
            return getBorderedFigure().getBorderItemContainer();
        }
        if (editPart instanceof DataIn3EditPart) {
            return getBorderedFigure().getBorderItemContainer();
        }
        if (editPart instanceof DataOut3EditPart) {
            return getBorderedFigure().getBorderItemContainer();
        }
        if (editPart instanceof CombinedIn3EditPart) {
            return getBorderedFigure().getBorderItemContainer();
        }
        if (editPart instanceof CombinedOut3EditPart) {
            return getBorderedFigure().getBorderItemContainer();
        }
        return super.getContentPaneFor(editPart);
    }
    protected NodeFigure createNodePlate() {
        DefaultSizeNodeFigure result = new DefaultSizeNodeFigure(getMapMode().DPtoLP(40), getMapMode().DPtoLP(40));
        return result;
    }
    protected NodeFigure createMainFigure() {
        NodeFigure figure = createNodePlate();
        figure.setLayoutManager(new StackLayout());
        IFigure shape = createNodeShape();
        figure.add(shape);
        contentPane = setupContentPane(shape);
        return figure;
    }
    protected IFigure setupContentPane(IFigure nodeShape) {
        if (nodeShape.getLayoutManager() == null) {
            ConstrainedToolbarLayout layout = new ConstrainedToolbarLayout();
            layout.setSpacing(getMapMode().DPtoLP(5));
            nodeShape.setLayoutManager(layout);
        }
        return nodeShape;
    }
    public IFigure getContentPane() {
        if (contentPane != null) {
            return contentPane;
        }
        return super.getContentPane();
    }
    public EditPart getPrimaryChildEditPart() {
        return getChildBySemanticHint(SaveccmVisualIDRegistry.getType(SwitchNameEditPart.VISUAL_ID));
    }
    public class SwitchFigure extends RectangleFigure {
        private WrapLabel fFigureSwitchNameLabel;
        public SwitchFigure() {
            ToolbarLayout layoutThis = new ToolbarLayout();
            layoutThis.setStretchMinorAxis(true);
            layoutThis.setMinorAlignment(ToolbarLayout.ALIGN_CENTER);
            layoutThis.setSpacing(0);
            layoutThis.setVertical(true);
            this.setLayoutManager(layoutThis);
            createContents();
        }
        private void createContents() {
            WrapLabel switchLabel0 = new WrapLabel();
            switchLabel0.setText("<<Switch>>");
            this.add(switchLabel0);
            fFigureSwitchNameLabel = new WrapLabel();
            fFigureSwitchNameLabel.setText("");
            this.add(fFigureSwitchNameLabel);
        }
        private boolean myUseLocalCoordinates = false;
        protected boolean useLocalCoordinates() {
            return myUseLocalCoordinates;
        }
        protected void setUseLocalCoordinates(boolean useLocalCoordinates) {
            myUseLocalCoordinates = useLocalCoordinates;
        }
        public WrapLabel getFigureSwitchNameLabel() {
            return fFigureSwitchNameLabel;
        }
    }
    public String getElementGuid() {
        return this.elementGuid;
    }
}
