public class NodeFactory {
    private final HashMap<UiViewElementNode, NodeProxy> mNodeMap =
        new HashMap<UiViewElementNode, NodeProxy>();
    public NodeFactory() {
    }
    public NodeProxy create(CanvasViewInfo canvasViewInfo) {
        return create(canvasViewInfo.getUiViewKey(), canvasViewInfo.getAbsRect());
    }
    public NodeProxy create(UiViewElementNode uiNode) {
        return create(uiNode, null );
    }
    public void clear() {
        mNodeMap.clear();
    }
    private NodeProxy create(UiViewElementNode uiNode, Rectangle bounds) {
        NodeProxy proxy = mNodeMap.get(uiNode);
        if (proxy == null) {
            proxy = new NodeProxy(uiNode, bounds, this);
            mNodeMap.put(uiNode, proxy);
        } else if (bounds != null && !proxy.getBounds().equals(bounds)) {
            proxy.setBounds(bounds);
        }
        return proxy;
    }
}
