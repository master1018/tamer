public abstract class UiElementEditPart extends AbstractGraphicalEditPart
    implements IUiUpdateListener {
    public UiElementEditPart(UiElementNode uiElementNode) {
        setModel(uiElementNode);
    }
    abstract protected void hideSelection();
    abstract protected void showSelection();
    @Override
    public DragTracker getDragTracker(Request request) {
        return new SelectEditPartTracker(this);
    }
    @Override
    protected void createEditPolicies() {
    }
    @SuppressWarnings("unchecked")
    @Override
    protected List getModelChildren() {
        return getUiNode().getUiChildren();
    }
    @Override
    public void activate() {
        super.activate();
        getUiNode().addUpdateListener(this);
    }
    @Override
    public void deactivate() {
        super.deactivate();
        getUiNode().removeUpdateListener(this);
    }
    @Override
    protected void refreshVisuals() {
        if (getFigure().getParent() != null) {
            ((GraphicalEditPart) getParent()).setLayoutConstraint(this, getFigure(), getBounds());
        }
        refreshChildrenVisuals();
    }
    protected void refreshChildrenVisuals() {
        if (children != null) {
            for (Object child : children) {
                if (child instanceof UiElementEditPart) {
                    UiElementEditPart childPart = (UiElementEditPart)child;
                    childPart.refreshVisuals();
                }
            }
        }
    }
    public void uiElementNodeUpdated(UiElementNode ui_node, UiUpdateState state) {
        switch(state) {
        case ATTR_UPDATED:
            refreshVisuals();
            break;
        case CHILDREN_CHANGED:
            refreshChildren();
            refreshVisuals();
            break;
        case CREATED:
            refreshVisuals();
            break;
        case DELETED:
            break;
        }
    }
    public final UiElementNode getUiNode() {
        return (UiElementNode) getModel();
    }
    protected final ElementDescriptor getDescriptor() {
        return getUiNode().getDescriptor();
    }
    protected final UiElementEditPart getEditPartParent() {
        EditPart parent = getParent();
        if (parent instanceof UiElementEditPart) {
            return (UiElementEditPart)parent;
        }
        return null;
    }
    protected final String getStringAttr(String attrName) {
        UiElementNode uiNode = getUiNode();
        if (uiNode.getXmlNode() != null) {
            Node xmlNode = uiNode.getXmlNode();
            if (xmlNode != null) {
                NamedNodeMap nodeAttributes = xmlNode.getAttributes();
                if (nodeAttributes != null) {
                    Node attr = nodeAttributes.getNamedItemNS(
                            SdkConstants.NS_RESOURCES, attrName);
                    if (attr != null) {
                        return attr.getNodeValue();
                    }
                }
            }
        }
        return null;
    }
    protected final Rectangle getBounds() {
        UiElementNode model = (UiElementNode)getModel();
        Object editData = model.getEditData();
        if (editData != null) {
            assert (editData instanceof org.eclipse.draw2d.geometry.Rectangle);
            return (Rectangle)editData;
        }
        return new Rectangle(0, 0, 0, 0);
    }
    @Override
    public EditPart getTargetEditPart(Request request) {
        if (request != null && request.getType() == RequestConstants.REQ_CREATE) {
            if (!getUiNode().getDescriptor().hasChildren()) {
                return null;
            }
        }
        return super.getTargetEditPart(request);
    }
    protected void installLayoutEditPolicy(final UiElementEditPart layoutEditPart) {
        installEditPolicy(EditPolicy.LAYOUT_ROLE,  new LayoutEditPolicy() {
            @Override
            protected EditPolicy createChildEditPolicy(EditPart child) {
                if (child instanceof UiElementEditPart) {
                    return new NonResizableSelectionEditPolicy((UiElementEditPart) child);
                }
                return null;
            }
            @Override
            protected Command getCreateCommand(CreateRequest request) {
                Object newType = request.getNewObjectType();
                if (newType instanceof ElementDescriptor) {
                    Point where = request.getLocation().getCopy();
                    Point origin = getLayoutContainer().getClientArea().getLocation();
                    where.translate(origin.getNegated());
                    EditPart host = getHost();
                    if (host instanceof UiElementEditPart) {
                        return new ElementCreateCommand((ElementDescriptor) newType,
                                (UiElementEditPart) host,
                                where);
                    }
                }
                return null;
            }
            @Override
            protected Command getMoveChildrenCommand(Request request) {
                return null;
            }
            @Override
            public void showLayoutTargetFeedback(Request request) {
                super.showLayoutTargetFeedback(request);
                if (layoutEditPart instanceof UiLayoutEditPart &&
                        request instanceof DropRequest) {
                    Point where = ((DropRequest) request).getLocation().getCopy();
                    Point origin = getLayoutContainer().getClientArea().getLocation();
                    where.translate(origin.getNegated());
                    ((UiLayoutEditPart) layoutEditPart).showDropTarget(where);
                }
            }
            @Override
            protected void eraseLayoutTargetFeedback(Request request) {
                super.eraseLayoutTargetFeedback(request);
                if (layoutEditPart instanceof UiLayoutEditPart) {
                    ((UiLayoutEditPart) layoutEditPart).hideDropTarget();
                }
            }
            @Override
            protected IFigure createSizeOnDropFeedback(CreateRequest createRequest) {
                return super.createSizeOnDropFeedback(createRequest);
            }
        });
    }
    protected static class NonResizableSelectionEditPolicy extends SelectionEditPolicy {
        private final UiElementEditPart mEditPart;
        public NonResizableSelectionEditPolicy(UiElementEditPart editPart) {
            mEditPart = editPart;
        }
        @Override
        protected void hideSelection() {
            mEditPart.hideSelection();
        }
        @Override
        protected void showSelection() {
            mEditPart.showSelection();
        }
    }
}
