public final class RemoveAllAction extends CallableSystemAction {
    public String getName() {
        return NbBundle.getMessage(RemoveAllAction.class, "CTL_ImportAction");
    }
    public RemoveAllAction() {
        putValue(Action.SHORT_DESCRIPTION, "Remove all methods");
        putValue(Action.ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_SHIFT, InputEvent.CTRL_MASK));
    }
    @Override
    protected String iconResource() {
        return "com/sun/hotspot/igv/coordinator/images/removeall.gif";
    }
    public HelpCtx getHelpCtx() {
        return HelpCtx.DEFAULT_HELP;
    }
    @Override
    protected boolean asynchronous() {
        return false;
    }
    @Override
    public void performAction() {
        OutlineTopComponent.findInstance().clear();
    }
}
