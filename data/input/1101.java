public class DiagramEditPart extends AbstractGenericEditPart {
    private static Logger logger = Logger.getLogger("net.sf.vgap4.projecteditor.editparts");
    @Override
    protected void createEditPolicies() {
        installEditPolicy(EditPolicy.COMPONENT_ROLE, new RootComponentEditPolicy());
        installEditPolicy(EditPolicy.LAYOUT_ROLE, new VGAP4XYLayoutEditPolicy());
    }
    @Override
    protected IFigure createFigure() {
        Figure fig = new FreeformLayer();
        fig.setOpaque(true);
        fig.setBorder(new MarginBorder(3));
        fig.setLayoutManager(new FreeformLayout());
        ConnectionLayer connLayer = (ConnectionLayer) getLayer(LayerConstants.CONNECTION_LAYER);
        connLayer.setConnectionRouter(new ShortestPathConnectionRouter(fig));
        return fig;
    }
    private AssistantMap getCastedModel() {
        return (AssistantMap) getModel();
    }
    @Override
    protected void addChildVisual(EditPart childEditPart, int index) {
        IFigure child = ((GraphicalEditPart) childEditPart).getFigure();
        if (childEditPart instanceof SpotEditPart) {
            FreeformLayer sectorLayer = (FreeformLayer) getLayer(VGAPScalableFreeformRootEditPart.PLANET_LAYER);
            sectorLayer.add(child);
            return;
        }
        super.addChildVisual(childEditPart, index);
    }
    @Override
    protected void removeChildVisual(EditPart childEditPart) {
        IFigure contentPane = getContentPane();
        if (childEditPart instanceof SpotEditPart) contentPane = getLayer(VGAPScalableFreeformRootEditPart.PLANET_LAYER);
        IFigure child = ((GraphicalEditPart) childEditPart).getFigure();
        try {
            contentPane.remove(child);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    @Override
    protected List<Object> getModelChildren() {
        return getCastedModel().getChildren();
    }
    @Override
    protected void refreshChildren() {
        try {
            super.refreshChildren();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    public void propertyChange(PropertyChangeEvent evt) {
        String prop = evt.getPropertyName();
        if (AssistantMap.DATA_ADDED_PROP.equals(prop)) {
            refreshChildren();
        }
        if (AssistantMap.CHANGE_ZOOMFACTOR.equals(prop)) {
            Iterator<Object> cit = this.getChildren().iterator();
            while (cit.hasNext()) {
                Object child = cit.next();
                if (child instanceof SpotEditPart) ((SpotEditPart) child).refreshVisuals();
            }
            this.refresh();
        }
    }
}
