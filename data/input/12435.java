public final class NodeFindAction extends CallableSystemAction {
    public void performAction() {
        EditorTopComponent comp = EditorTopComponent.getActive();
        if (comp != null) {
            comp.findNode();
        }
    }
    public NodeFindAction() {
        putValue(Action.SHORT_DESCRIPTION, "Find nodes");
    }
    public String getName() {
        return NbBundle.getMessage(NodeFindAction.class, "CTL_NodeFindAction");
    }
    public HelpCtx getHelpCtx() {
        return HelpCtx.DEFAULT_HELP;
    }
    @Override
    protected boolean asynchronous() {
        return false;
    }
    @Override
    public boolean isEnabled() {
        return true;
    }
    @Override
    protected String iconResource() {
        return "com/sun/hotspot/igv/view/images/search.gif";
    }
}
