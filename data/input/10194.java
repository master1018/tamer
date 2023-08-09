public final class ShowAllAction extends CallableSystemAction {
    public void performAction() {
        EditorTopComponent editor = EditorTopComponent.getActive();
        if (editor != null) {
            editor.showAll();
        }
    }
    public ShowAllAction() {
        putValue(Action.SHORT_DESCRIPTION, "Show all nodes");
        putValue(Action.ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_A, InputEvent.CTRL_MASK));
    }
    public String getName() {
        return "Show all";
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
        return "com/sun/hotspot/igv/view/images/expand.gif";
    }
}
