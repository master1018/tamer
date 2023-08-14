public final class MoveFilterUpAction extends CookieAction {
    protected void performAction(Node[] activatedNodes) {
        for (Node n : activatedNodes) {
            Filter c = n.getLookup().lookup(Filter.class);
            FilterTopComponent.findInstance().getSequence().moveFilterUp(c);
        }
    }
    protected int mode() {
        return CookieAction.MODE_EXACTLY_ONE;
    }
    public MoveFilterUpAction() {
        putValue(Action.SHORT_DESCRIPTION, "Move filter upwards");
    }
    public String getName() {
        return NbBundle.getMessage(MoveFilterUpAction.class, "CTL_MoveFilterUpAction");
    }
    protected Class[] cookieClasses() {
        return new Class[]{
            Filter.class
        };
    }
    @Override
    protected String iconResource() {
        return "com/sun/hotspot/igv/filterwindow/images/up.gif";
    }
    @Override
    protected void initialize() {
        super.initialize();
        putValue("noIconInMenu", Boolean.TRUE);
    }
    public HelpCtx getHelpCtx() {
        return HelpCtx.DEFAULT_HELP;
    }
    @Override
    protected boolean asynchronous() {
        return false;
    }
}
