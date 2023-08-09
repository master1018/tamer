public final class RemoveFilterSettingsAction extends CallableSystemAction {
    public void performAction() {
        FilterTopComponent.findInstance().removeFilterSetting();
    }
    public String getName() {
        return NbBundle.getMessage(RemoveFilterSettingsAction.class, "CTL_RemoveFilterSettingsAction");
    }
    public RemoveFilterSettingsAction() {
        putValue(Action.SHORT_DESCRIPTION, "Remove filter profile");
    }
    @Override
    protected void initialize() {
        super.initialize();
    }
    public HelpCtx getHelpCtx() {
        return HelpCtx.DEFAULT_HELP;
    }
    @Override
    protected boolean asynchronous() {
        return false;
    }
    @Override
    protected String iconResource() {
        return "com/sun/hotspot/igv/filterwindow/images/delete.gif";
    }
}
