public final class SaveAllAction extends CallableSystemAction {
    public void performAction() {
        final OutlineTopComponent component = OutlineTopComponent.findInstance();
        SaveAsAction.save(component.getDocument());
    }
    public String getName() {
        return NbBundle.getMessage(SaveAllAction.class, "CTL_SaveAllAction");
    }
    public SaveAllAction() {
        putValue(Action.SHORT_DESCRIPTION, "Save all methods to XML file");
        putValue(Action.ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_S, InputEvent.CTRL_MASK));
    }
    @Override
    protected String iconResource() {
        return "com/sun/hotspot/igv/coordinator/images/saveall.gif";
    }
    public HelpCtx getHelpCtx() {
        return HelpCtx.DEFAULT_HELP;
    }
    @Override
    protected boolean asynchronous() {
        return false;
    }
}
