 class CanvasSelection {
    private final CanvasViewInfo mCanvasViewInfo;
    private final Rectangle mRect;
    private final NodeProxy mNodeProxy;
    private final String mName;
    public CanvasSelection(CanvasViewInfo canvasViewInfo,
            RulesEngine gre,
            NodeFactory nodeFactory) {
        assert canvasViewInfo != null;
        mCanvasViewInfo = canvasViewInfo;
        if (canvasViewInfo == null) {
            mRect = null;
            mNodeProxy = null;
        } else {
            Rectangle r = canvasViewInfo.getSelectionRect();
            mRect = new Rectangle(r.x, r.y, r.width, r.height);
            mNodeProxy = nodeFactory.create(canvasViewInfo);
        }
        mName = initDisplayName(canvasViewInfo, gre);
    }
    public CanvasViewInfo getViewInfo() {
        return mCanvasViewInfo;
    }
    public Rectangle getRect() {
        return mRect;
    }
    public String getName() {
        return mName;
    }
     void paintSelection(RulesEngine gre,
            GCWrapper gcWrapper,
            boolean isMultipleSelection) {
        if (mNodeProxy != null) {
            gre.callOnSelected(gcWrapper, mNodeProxy, mName, isMultipleSelection);
        }
    }
    public void paintParentSelection(RulesEngine gre, GCWrapper gcWrapper) {
        if (mNodeProxy != null) {
            INode parent = mNodeProxy.getParent();
            if (parent instanceof NodeProxy) {
                gre.callOnChildSelected(gcWrapper, (NodeProxy)parent, mNodeProxy);
            }
        }
    }
    private String initDisplayName(CanvasViewInfo canvasViewInfo, RulesEngine gre) {
        if (canvasViewInfo == null) {
            return null;
        }
        String fqcn = canvasViewInfo.getName();
        if (fqcn == null) {
            return null;
        }
        String name = gre.callGetDisplayName(canvasViewInfo.getUiViewKey());
        if (name == null) {
            if (fqcn.startsWith("android.")) {                                      
                int first = fqcn.indexOf('.');
                int last = fqcn.lastIndexOf('.');
                if (last > first) {
                    name = fqcn.substring(0, first) + ".." + fqcn.substring(last);   
                }
            } else {
                int first = fqcn.indexOf('.');
                first = fqcn.indexOf('.', first + 1);
                int last = fqcn.lastIndexOf('.');
                if (last > first) {
                    name = fqcn.substring(0, first) + ".." + fqcn.substring(last);   
                }
            }
        }
        return name;
    }
}
