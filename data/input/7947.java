public final class HideAction extends CallableSystemAction {
    public void performAction() {
        EditorTopComponent editor = EditorTopComponent.getActive();
        if (editor != null) {
            editor.hideNodes();
        }
    }
    public HideAction() {
        putValue(Action.SHORT_DESCRIPTION, "Hide selected nodes");
        putValue(Action.ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_H, Event.CTRL_MASK, false));
    }
    public String getName() {
        return "Hide";
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
        return "com/sun/hotspot/igv/view/images/hide.gif";
    }
}
