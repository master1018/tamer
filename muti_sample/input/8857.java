public final class SaveFilterSettingsAction extends CallableSystemAction {
    public void performAction() {
        FilterTopComponent.findInstance().addFilterSetting();
    }
    public String getName() {
        return NbBundle.getMessage(SaveFilterSettingsAction.class, "CTL_SaveFilterSettingsAction");
    }
    @Override
    protected void initialize() {
        super.initialize();
    }
    public SaveFilterSettingsAction() {
        putValue(Action.SHORT_DESCRIPTION, "Create new filter profile");
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
        return "com/sun/hotspot/igv/filterwindow/images/add.gif";
    }
}
