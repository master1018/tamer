public class LocationEditPart extends AbstractBorderedShapeEditPart {
    public static final int VISUAL_ID = 1003;
    protected IFigure contentPane;
    protected IFigure primaryShape;
    public LocationEditPart(View view) {
        super(view);
    }
    protected void createDefaultEditPolicies() {
        super.createDefaultEditPolicies();
        installEditPolicy(EditPolicyRoles.SEMANTIC_ROLE, new se.mdh.mrtc.save.taEditor.diagram.edit.policies.LocationItemSemanticEditPolicy());
        installEditPolicy(EditPolicy.LAYOUT_ROLE, createLayoutEditPolicy());
    }
    protected LayoutEditPolicy createLayoutEditPolicy() {
        LayoutEditPolicy lep = new LayoutEditPolicy() {
            protected EditPolicy createChildEditPolicy(EditPart child) {
                if (child instanceof IBorderItemEditPart) {
                    return new BorderItemSelectionEditPolicy();
                }
                EditPolicy result = child.getEditPolicy(EditPolicy.PRIMARY_DRAG_ROLE);
                if (result == null) {
                    result = new NonResizableEditPolicy();
                }
                return result;
            }
            protected Command getMoveChildrenCommand(Request request) {
                return null;
            }
            protected Command getCreateCommand(CreateRequest request) {
                return null;
            }
        };
        return lep;
    }
    protected IFigure createNodeShape() {
        LocationFigure figure = new LocationFigure();
        return primaryShape = figure;
    }
    public LocationFigure getPrimaryShape() {
        return (LocationFigure) primaryShape;
    }
    protected void addBorderItem(IFigure borderItemContainer, IBorderItemEditPart borderItemEditPart) {
        if (borderItemEditPart instanceof se.mdh.mrtc.save.taEditor.diagram.edit.parts.LocationLocationNameEditPart || borderItemEditPart instanceof se.mdh.mrtc.save.taEditor.diagram.edit.parts.LocationInvariantEditPart) {
            BorderItemLocator locator = new BorderItemLocator(getMainFigure(), PositionConstants.SOUTH);
            locator.setBorderItemOffset(new Dimension(-20, -20));
            borderItemContainer.add(borderItemEditPart.getFigure(), locator);
        } else {
            super.addBorderItem(borderItemContainer, borderItemEditPart);
        }
    }
    protected NodeFigure createNodePlate() {
        DefaultSizeNodeFigure result = new DefaultSizeNodeFigure(getMapMode().DPtoLP(40), getMapMode().DPtoLP(40));
        return result;
    }
    public EditPolicy getPrimaryDragEditPolicy() {
        EditPolicy result = super.getPrimaryDragEditPolicy();
        if (result instanceof ResizableEditPolicy) {
            ResizableEditPolicy ep = (ResizableEditPolicy) result;
            ep.setResizeDirections(PositionConstants.NONE);
        }
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
        return nodeShape;
    }
    public IFigure getContentPane() {
        if (contentPane != null) {
            return contentPane;
        }
        return super.getContentPane();
    }
    public EditPart getPrimaryChildEditPart() {
        return getChildBySemanticHint(se.mdh.mrtc.save.taEditor.diagram.part.TaEditorVisualIDRegistry.getType(se.mdh.mrtc.save.taEditor.diagram.edit.parts.LocationLocationNameEditPart.VISUAL_ID));
    }
    public class LocationFigure extends Ellipse {
        public LocationFigure() {
            this.setForegroundColor(ColorConstants.black);
            this.setBackgroundColor(ColorConstants.lightGray);
            this.setSize(getMapMode().DPtoLP(40), getMapMode().DPtoLP(40));
        }
        private boolean myUseLocalCoordinates = false;
        protected boolean useLocalCoordinates() {
            return myUseLocalCoordinates;
        }
        protected void setUseLocalCoordinates(boolean useLocalCoordinates) {
            myUseLocalCoordinates = useLocalCoordinates;
        }
    }
}
