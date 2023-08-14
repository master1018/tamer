public final class RemoveAction extends NodeAction {
    protected void performAction(Node[] activatedNodes) {
        for (Node n : activatedNodes) {
            RemoveCookie removeCookie = n.getCookie(RemoveCookie.class);
            if (removeCookie != null) {
                removeCookie.remove();
            }
        }
    }
    public RemoveAction() {
        putValue(Action.SHORT_DESCRIPTION, "Remove");
    }
    public String getName() {
        return NbBundle.getMessage(RemoveAction.class, "CTL_RemoveAction");
    }
    @Override
    protected String iconResource() {
        return "com/sun/hotspot/igv/coordinator/images/remove.gif";
    }
    public HelpCtx getHelpCtx() {
        return HelpCtx.DEFAULT_HELP;
    }
    @Override
    protected boolean asynchronous() {
        return false;
    }
    protected boolean enable(Node[] nodes) {
        return nodes.length > 0;
    }
}
